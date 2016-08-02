/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2015 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database.  If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */

package oracle.kv.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.LoginCredentials;
import oracle.kv.Value;
import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.CommandServiceAPI;
import oracle.kv.impl.admin.client.CommandShell;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.api.table.TableMetadataProxy;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.metadata.MetadataStore;
import oracle.kv.impl.security.metadata.SecurityMetadata;
import oracle.kv.impl.security.metadata.SecurityMetadataProxy;
import oracle.kv.impl.security.util.KVStoreLogin;
import oracle.kv.impl.security.util.KVStoreLogin.CredentialsProvider;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.CommandParser;
import oracle.kv.impl.util.KVThreadFactory;
import oracle.kv.impl.util.TopologyLocator;
import oracle.kv.impl.util.server.LoggerUtils;
import oracle.kv.util.shell.ShellException;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DiskOrderedCursorConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentNotFoundException;
import com.sleepycat.je.ForwardCursor;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.StoreNotFoundException;
import com.sleepycat.persist.model.AnnotationModel;
import com.sleepycat.persist.model.EntityModel;

/**
 * Load from an environment directory (source) to a target store.
 *
 * Command line usage:
 *   Load -source <backup directory> -store <name> -host <hostname>
 *     -port <port> [-load-admin] [-force]
 *     [-security <security-file-path>]
 *     [-status <path to file>] [-verbose]
 *
 * 1. open the source environment read-only , not replicated
 * 2. get all databases that look like partitions (p* where * is an Integer)
 * 3. if the -load-admin flag is specified, skip to next section, below.
 * Otherwise, for each database that is not already loaded, according to the
 * optional status file:
 *   a.  Do a DOS
 *   b.  put each record in a list of KV pairs that is associated with the
 *       target partition in the new store, based on the key.  RecordListMap
 *       is the object that holds the lists, which are RecordList objects.
 *   c.  When the list of records reaches a threshold based on number of bytes,
 *       schedule a thread to write that list.  The list is "tear-away" in that
 *       its given to the writer thread so the main  thread can continue reading
 *       and inserting into a (new) list for that partition.  An additional
 *       threshold of total number of bytes on all lists exists to trigger
 *       tasks to clean out the lists.  WriteTask implements the task to write
 *       a single list.
 * 4. an ArrayBlockingQueue is used to synchronize the producer and consumers.
 *    This ensures that there are never too many lists waiting to be written,
 *    keeping memory use bounded when the producer outpaces consumers.
 * 5. a utility thread (TaskWaiter) waits for tasks to complete and
 *    aggregates status of the writer threads for output and success/error
 *    reporting.  If any task fails TaskWaiter will exit prematurely.  The
 *    main thread must detect this.
 * 6. output goes to stout.  verbose mode turns on a fair bit. TBD.
 *
 * If the -load-admin flag is specified the environment in the backup directory
 * must be that of an admin vs a RepNode.  This flag is used to load metadata
 * from the admin database.  If this is not done the resulting store will not
 * have required metadata (users, tables, indexes).  The admin load must be
 * done first, before loading data, in order for indexes to work.  If security
 * is to be used, the admin metadata should be loaded before security is
 * enabled on the store.  The -force flag will cause the -load-admin flag to
 * overwrite existing metadata, which it will not do by default.
 *
 * The -security-file argument is used to point to a file that specifies the
 * properties to be used for store login, if required.

 * TODO:
 *   o Maybe add a trigger for tests to simulate a failure in a WriteTask.
 *   o Could use test cases for failures here:
 *     -- reading JE
 *     -- opening env
 *     -- opening/writing store
 *     -- out of memory conditions
 *     -- status file handling
 */
public class Load implements CredentialsProvider {

    /* External commands, for "java -jar" usage. */
    public static final String COMMAND_NAME = "load";
    public static final String COMMAND_DESC =
        "loads data into a store from a backup";
    public static final String COMMAND_ARGS =
        "-source <backupDir> " +
        CommandParser.getHostUsage() + " " +
        CommandParser.getPortUsage() + "\n\t" +
        CommandParser.getStoreUsage() + " " +
        CommandParser.optional(CommandParser.getUserUsage()) + " " +
        CommandParser.optional(CommandParser.getSecurityUsage()) + "\n\t" +
        CommandParser.optional("-load-admin ") +
        CommandParser.optional("-force ") +
        CommandParser.optional("-status <pathToFile>");

    File envDir;
    Environment env;
    Topology topo;
    File statusFile;
    HashSet<String> loadedDatabases;
    PrintStream output;
    boolean verboseOutput;
    RecordListMap recordList;
    int maxPartitionBytes;
    long totalBytesThreshold;

    /* Use for authentication */
    private KVStoreLogin storeLogin;
    private LoginCredentials loginCreds;

    /*
     * Use an internal store handle for writing any K/V pair, including those
     * in the internal keyspace (e.g., schemas).  KVStore.close should only be
     * called on the user store handle.
     */
    KVStore userStore;
    KVStore internalStore;

    /**
     * Asynchronous thread pool
     */
    ExecutorService threadPool;
    BlockingQueue<FutureHolder> taskWaitQueue;
    Future<Long> taskWait;
    private static final int NUM_THREADS = 20;

    /**
     * Don't allow more than 2x thread count entries in the queue to avoid
     * running out of memory if the producer is faster than the consumer.
     */
    private static final int TASK_QUEUE_SIZE = 2 * NUM_THREADS;

    /**
     * The number of bytes on a partition list that triggers a write task
     */
    private static final int DEFAULT_MAX_PARTITION_BYTES = 5000 * 1000;

    /**
     * Retry count for KVStore.putIfAbsent() operation.
     */
    private static final int RETRY_COUNT = 5;

    public Load(File env,
                String storeName,
                String targetHost,
                int targetPort,
                String user,
                String securityFile,
                String statusFile,
                boolean verboseOutput,
                PrintStream output)
        throws Exception {

        this.envDir = env;
        this.statusFile = (statusFile != null ? new File(statusFile) : null);
        this.verboseOutput = verboseOutput;
        this.output = output;
        String[] hosts = new String[1];
        hosts[0] = targetHost + ":" + targetPort;

        prepareAuthentication(user, securityFile);

        KVStoreConfig kvConfig = new KVStoreConfig(storeName, hosts[0]);
        kvConfig.setSecurityProperties(storeLogin.getSecurityProperties());

        userStore = KVStoreFactory.getStore(
            kvConfig, loginCreds, KVStoreLogin.makeReauthenticateHandler(this));
        internalStore = KVStoreImpl.makeInternalHandle(userStore);
        verbose("Opened store " + storeName);

        topo = TopologyLocator.get(hosts, 10,
                                   KVStoreImpl.getLoginManager(userStore),
                                   null /* expectedStoreName */);
        threadPool = Executors.newFixedThreadPool(NUM_THREADS,
                                                  new KVThreadFactory("Load",
                                                                      null));
        taskWaitQueue = new ArrayBlockingQueue<FutureHolder>(TASK_QUEUE_SIZE);
        taskWait = threadPool.submit(new TaskWaiter());
        recordList = new RecordListMap();
        totalBytesThreshold = Runtime.getRuntime().maxMemory()/4;
        maxPartitionBytes = DEFAULT_MAX_PARTITION_BYTES;
        verbose("Using byte threshold of " + totalBytesThreshold);
    }

    @Override
    public LoginCredentials getCredentials() {
        return loginCreds;
    }

    public void setStatusFile(File status) {
        statusFile = status;
    }

    public File getStatusFile() {
        return statusFile;
    }

    public void setOutput(PrintStream output) {
        this.output = output;
    }

    public PrintStream getOutput() {
        return output;
    }

    public void setVerbose(boolean verboseOutput) {
        this.verboseOutput = verboseOutput;
    }

    public boolean getVerbose() {
        return verboseOutput;
    }

    /**
     * The ability to set maxPartitionBytes is mostly useful for testing.
     */
    public void setPartitionBytes(int value) {
        maxPartitionBytes = value;
    }

    public int getPartitionBytes() {
        return maxPartitionBytes;
    }

    private void message(String msg) {
        if (output != null) {
            output.println(msg);
        }
    }

    private void verbose(String msg) {
        if (verboseOutput) {
            message(msg);
        }
    }

    public long run()
        throws Exception {

        try {
            env = open(envDir);
            verbose("Opened source backup directory " + envDir);
            loadStatusFile();
        } catch (Exception e) {
            System.err.println("Could not open backup source directory: " + e);
            throw e;
        }
        try {
            List<String> dbs = env.getDatabaseNames();
            for (String db : dbs) {
                if (!PartitionId.isPartitionName(db)) {
                    verbose("Skipping non-partition database: " + db);
                    continue;
                }

                if (isLoaded(db)) {
                    verbose("Skipping already loaded database: " + db);
                    continue;
                }

                verbose("Starting database scan: " + db);
                scanDatabase(db);
                setLoaded(db);
                verbose("Completed database scan: " + db);
            }

            /**
             * Make tasks to write the last bunch of records.
             */
            recordList.createTasks();
            verbose("Done scanning databases, waiting for write tasks");

            /**
             * Wait for the writes to be done.
             */
            return waitForTasks();
        } finally {

            /**
             * If present, unconditionally update the status file with the
             * databases that have been successfully loaded.  This allows
             * more efficient restart in the case of failures.
             */
            writeStatusFile();
            close();
        }
    }

    /*
     * Check and set SSL connection
     */
    private void prepareAuthentication(final String user,
                                       final String securityFile)
        throws Exception {

        storeLogin = new KVStoreLogin(user, securityFile);
        try {
            storeLogin.loadSecurityProperties();
        } catch (IllegalArgumentException iae) {
            message(iae.getMessage());
        }

        /* Needs authentication */
        if (storeLogin.foundSSLTransport()) {
            loginCreds = storeLogin.makeShellLoginCredentials();
        }
    }

    /**
     * Perform a Disk Ordered Scan (DOS) of a database putting the entries into
     * a List associated with their PartitionId in the target store.
     */
    private void scanDatabase(String dbName)
        throws Exception {

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(false);
        dbConfig.setReadOnly(true);
        Database db = null;
        ForwardCursor cursor = null;
        try {
            db = env.openDatabase(null, dbName, dbConfig);
            cursor = db.openCursor(new DiskOrderedCursorConfig());
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();
            while (cursor.getNext(key, data, null) ==
                   OperationStatus.SUCCESS) {

                /**
                 * Check for exceptions from existing tasks.  When that happens
                 * the TaskWaiter will throw an exception and will be be in a
                 * "done" state.
                 */
                if (taskWait.isDone()) {
                    message("Task Waiter has exited, aborting load");
                    waitForTasks();
                }
                recordList.insert(key, data);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Wait for all tasks that have been scheduled.
     */
    private long waitForTasks()
        throws Exception {

        verbose("Collecting results from write tasks");

        try {
            taskWaitQueue.put(new FutureHolder(null));
            return taskWait.get();
        } catch (ExecutionException e) {
            message("waitForTasks: exception from a task: " + e);
            throw e;
        } catch (InterruptedException ie) {
            message("waitForTasks: task was interrupted: " + ie);
            throw ie;
        }
    }

    private void close() {
        env.close();
        if (userStore != null) {
            userStore.close();
        }
    }

    private void setLoaded(String dbname) {
        if (loadedDatabases != null) {
            loadedDatabases.add(dbname);
        }
    }

    private boolean isLoaded(String dbname) {
        if (loadedDatabases != null &&
            loadedDatabases.contains(dbname)) {
            return true;
        }
        return false;
    }

    private void loadStatusFile() {
        if (statusFile != null) {
            loadedDatabases = new HashSet<String>();
            if (statusFile.exists()) {
                try {
                    FileReader fr = new FileReader(statusFile);
                    BufferedReader br = new BufferedReader(fr);
                    String inputLine;
                    while ((inputLine = br.readLine()) != null) {
                        loadedDatabases.add(inputLine);
                    }
                    br.close();
                } catch (IOException e) {
                    throw new IllegalStateException
                        ("Failed to load from status file " + statusFile, e);
                }
                verbose("Loaded status from file " + statusFile);
            }
        }
    }

    /**
     * Rewrite the status file
     */
    private void writeStatusFile() {
        if (loadedDatabases != null) {
            PrintWriter writer = null;
            try {
                FileOutputStream fos = new FileOutputStream(statusFile);
                writer = new PrintWriter(fos);
                for (String dbname : loadedDatabases) {
                    writer.printf("%s\n", dbname);
                }
            } catch (Exception e) {
                throw new IllegalStateException
                    ("Failed to save status file " + statusFile, e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    /**
     * Open the JE environment and if present, load the status file.
     */
    static private Environment open(File envDir) {

        if (!envDir.isDirectory()) {
            System.err.println
                ("Environment path is not a directory or does not exist: " +
                 envDir);
            throw new IllegalArgumentException
                ("Bad environment directory: " + envDir);
        }
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(false);
        envConfig.setAllowCreate(false);
        envConfig.setReadOnly(true);
        try {
            Environment env = new Environment(envDir, envConfig);
            return env;
        } catch (EnvironmentNotFoundException e) {
            throw new IllegalArgumentException
                ("Cannot find valid Environment in directory: " +
                 envDir);
        }
    }

    class KV {
        public Key key;
        public Value value;
        public KV(Key key, Value value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * A simple class to encapsulate a list of key/value pairs per Partition to
     * be written by a writer task.
     */
    class RecordListMap {
        private final ConcurrentMap<PartitionId, RecordList> map;
        int totalBytes;

        public RecordListMap() {
            map = new ConcurrentHashMap<PartitionId, RecordList>();
            totalBytes = 0;
        }

        /**
         * Insert the key/value into the appropriate list based on the
         * PartitionId in the target store.
         */
        public void insert(DatabaseEntry key, DatabaseEntry data) {

            byte[] keyBytes = key.getData();
            byte[] valBytes = data.getData();
            PartitionId targetPartition = topo.getPartitionId(keyBytes);
            RecordList list = map.get(targetPartition);
            if (list == null) {
                list = new RecordList();
                map.put(targetPartition, list);
            }
            list.add(keyBytes, valBytes);
            totalBytes += list.byteSize();

            /**
             * If the threshold has been reached, spawn off a task to write the
             * list to the store.
             */
            if (list.byteSize() > maxPartitionBytes) {
                createTaskFromList(list, targetPartition);
            }

            if (totalBytes > totalBytesThreshold) {
                createTasks();
            }
        }

        /**
         * Create a task for the list and reset the partition list.
         */
        public void createTaskFromList(RecordList list, PartitionId id) {
            createTask(list.getList());
            totalBytes -= list.byteSize();
            map.remove(id);
        }

        /**
         * Create a task for each populated partition, cleaning out the
         * structure.
         */
        public void createTasks() {
            for (Map.Entry<PartitionId, RecordList> entry :
                     map.entrySet()) {
                createTaskFromList(entry.getValue(), entry.getKey());
            }
        }

        /**
         * Create a task that writes the list to the store.
         */
        private void createTask(List<KV> list) {
            verbose("Creating a task to write " + list.size() + " records");
            Future<Integer> future = threadPool.submit(new WriteTask(list));
            try {

                /**
                 * This call will block if the queue is full.  The prevents the
                 * producer from getting so far ahead of the consumer that
                 * memory becomes a problem.
                 */
                taskWaitQueue.put(new FutureHolder(future));
            } catch (InterruptedException e) {
                verbose("Load program was interrupted");
                throw new IllegalStateException(e);
            }
        }

        /**
         * This class exists to encapsulate List<KV> plus its byte size. If not
         * for tracking bytes this would just be a List in RecordListMap.
         */
        class RecordList {
            private final List<KV> list;
            private int bytes;

            public RecordList() {
                list = new ArrayList<KV>();
                bytes = 0;
            }

            public void add(byte[] keyBytes, byte[] valBytes) {
                bytes += keyBytes.length + valBytes.length;
                Key kvkey = Key.fromByteArray(keyBytes);
                Value kvvalue = Value.fromByteArray(valBytes);
                list.add(new KV(kvkey, kvvalue));
            }

            public List<KV> getList() {
                return list;
            }

            public int byteSize() {
                return bytes;
            }
        }
    }

    /**
     * In the future (post-R1) there should be a multi-put operation that will
     * allow this to be one call vs iteration.
     *
     * TODO: tune timeout on request, maybe.
     */
    private int writeListToStore(List<KV> list) {

        int num = 0;
        for (KV kv: list) {
            /* Retry a few times on failure */
            for (int i = 0; i < RETRY_COUNT; i++) {
                try {

                    /**
                     * putIfAbsent() is used in case the load is being retried
                     * on existing data.  It is more efficient in this case
                     * because it won't result in unnecessary modification of
                     * the target stores.
                     */
                    internalStore.putIfAbsent(kv.key, kv.value);
                    ++num;
                    break;
                } catch (FaultException fe) {
                    if (i == (RETRY_COUNT - 1)) {
                        throw fe;
                    }
                }
            }
        }
        return num;
    }

    /**
     * A simple class that reads all KV entries in a list and writes them to
     * the target store.  If/when multi-put is supported that could be used
     * but for now it's one at a time.  This class runs in a thread from the
     * ExecutorService (threadPool).  The return value is the number of
     * records put.
     *
     * Exceptions from the call() method are handled in the waitForTasks()
     * function and reported to its caller.
     */
    private class WriteTask implements Callable<Integer> {
        List<KV> list;

        public WriteTask(List<KV> list) {
            this.list = list;
        }

        @Override
        public Integer call() {
            return writeListToStore(list);
        }
    }

    /**
     * This class holds a Future<Integer> and exists so that a
     * BlockingQueue<FutureHolder> can be used to indicate which WriteTasks
     * need to be waited upon.  A FutureHolder with a null future indicates
     * the end of input and the waiting thread can exit.
     */
    class FutureHolder {
        Future<Integer> future;

        public FutureHolder(Future<Integer> future) {
            this.future = future;
        }

        public Future<Integer> getFuture() {
            return future;
        }
    }

    /**
     * A Callable that reaps all of the write tasks on the queue and
     * aggregates their results.  It returns when an empty Future or
     * FutureHolder is found in the queue or one of the Futures fails with an
     * exception.  In this case the main thread needs to occasionally check if
     * this Future is done.
     */
    private class TaskWaiter implements Callable<Long> {
        long totalRecords;

        public TaskWaiter() {
        }

        @Override
        public Long call()
            throws Exception {

            while (true) {
                FutureHolder holder;
                try {
                    holder = taskWaitQueue.take();
                } catch (InterruptedException e) {
                    verbose("Load program was interrupted");
                    throw new IllegalStateException(e);
                }

                Future<Integer> future;
                if (holder == null || holder.getFuture() == null) {
                    verbose("TaskWaitThread returning");
                    return totalRecords;
                }

                future = holder.getFuture();
                try {
                    int result = future.get();
                    totalRecords += result;
                } catch (ExecutionException e) {
                    message("TaskWaiter: exception from a task: " + e);
                    throw e;
                } catch (InterruptedException ie) {
                    message("TaskWaiter: task was interrupted: " + ie);
                    throw ie;
                }
            }
        }
    }

    /**
     * A class that loads metadata from an admin database environment in a
     * snapshot.  At this time it only loads table metadata, and it does this
     * via a single serialized object.  An instance of the CommandServiceAPI
     * is required to put the new metadata.  It is read directly from the
     * admin environment.
     */
    private static class LoadAdmin {

        private final Environment env;
        private final EntityStore estore;
        private final boolean verboseOutput;
        private final PrintStream output;
        private final String securityFile;
        private final String user;
        private final String targetHost;
        private final int targetPort;
        private final boolean forceLoad;

        public LoadAdmin(File envDir,
                         String targetHost,
                         int targetPort,
                         String user,
                         String securityFile,
                         boolean verboseOutput,
                         boolean forceLoad,
                         PrintStream output) {

            env = open(envDir);
            estore = initEstore(envDir);
            this.output = output;
            this.verboseOutput = verboseOutput;
            this.securityFile = securityFile;
            this.user = user;
            this.targetHost = targetHost;
            this.targetPort = targetPort;
            this.forceLoad = forceLoad;
            verbose("Opened environment for admin load: " + envDir);
        }

        /**
         * Loads both table and security metadata, if present.
         */
        public void loadMetadata() {
            TableMetadata tmd = MetadataStore.read(TableMetadata.class,
                                                   MetadataType.TABLE,
                                                   estore, null);
            if (tmd != null) {
                verbose("Found table metadata");
                if (verboseOutput) {
                    List<String> tablesToLoad = tmd.listTables();
                    if (!tablesToLoad.isEmpty()) {
                        message("Writing tables:");
                        for (String s : tablesToLoad) {
                            message("\t" + s);
                        }
                    }
                }
            } else {
                message("No tables to write");
            }

            SecurityMetadata smd = MetadataStore.read(SecurityMetadata.class,
                                                      MetadataType.SECURITY,
                                                      estore, null);
            if (smd != null) {
                verbose("Found security metadata");
            }

            if (tmd != null || smd != null) {
                writeMetadata(tmd, smd);
            }
            estore.close();
            env.close();
        }

        /**
         * Gets an instance of CommandServiceAPI from a CommandShell and uses
         * it to push the metadata to the store admin.
         */
        private void writeMetadata(TableMetadata tmd,
                                   SecurityMetadata smd) {

            ArrayList<String> argList = new ArrayList<String>();
            argList.add(CommandParser.HOST_FLAG);
            argList.add(targetHost);
            argList.add(CommandParser.PORT_FLAG);
            argList.add(Integer.toString(targetPort));
            if (securityFile != null) {
                argList.add(CommandParser.ADMIN_SECURITY_FLAG);
                argList.add(securityFile);
            }
            if (user != null) {
                argList.add(CommandParser.ADMIN_USER_FLAG);
                argList.add(user);
            }
            CommandShell shell = new CommandShell(null, output);
            shell.parseArgs(argList.toArray(new String[argList.size()]));
            shell.init();
            try {
                CommandServiceAPI cs = shell.getAdmin();
                TableMetadata existingTableMD = null;
                SecurityMetadata existingSecMD = null;
                if (tmd != null) {
                    existingTableMD = cs.getMetadata(TableMetadata.class,
                                                     MetadataType.TABLE);
                }
                if (smd != null) {
                    existingSecMD = cs.getMetadata(SecurityMetadata.class,
                                                   MetadataType.SECURITY);
                }
                if ((existingTableMD != null || existingSecMD != null) &&
                    !forceLoad) {
                        message("Metadata exists, use -force flag"
                                + " to overwrite");
                        return;
                }

                if (tmd != null) {
                    cs.putMetadata(tmd);
                    verbose("Wrote table metadata");
                }
                if (smd != null) {
                    cs.putMetadata(smd);
                    verbose("Wrote security metadata");
                }
            } catch (RemoteException re) {
                message("Failed to acquire admin interface or write" +
                        " metadata: " + re.getMessage());
            } catch (ShellException se) {
                message("Failed to acquire admin interface or write" +
                        " metadata: " + se.getMessage());
            }
        }

        private EntityStore initEstore(File envDir) {
            final EntityModel model = new AnnotationModel();
            model.registerClass(TableMetadataProxy.class);
            model.registerClass(SecurityMetadataProxy.class);
            final StoreConfig stConfig = new StoreConfig();
            stConfig.setAllowCreate(false);
            stConfig.setTransactional(false);
            stConfig.setReadOnly(true);
            stConfig.setModel(model);
            try {
                return new EntityStore(env, Admin.ADMIN_STORE_NAME, stConfig);
            } catch (StoreNotFoundException snf) {
                throw new IllegalArgumentException
                    ("Cannot find an admin database in the directory: " +
                     envDir);
            }
        }

        private void message(String msg) {
            output.println(msg);
        }

        private void verbose(String msg) {
            if (verboseOutput) {
                message(msg);
            }
        }
    }

    /**
     * Parser for the Load command line.
     */
    public static class LoadParser extends CommandParser {
        private static final String SOURCE_FLAG = "-source";
        private static final String STATUS_FLAG = "-status";
        private static final String ADMIN_LOAD_FLAG = "-load-admin";
        private static final String FORCE_ADMIN_FLAG = "-force";
        private String source = null;
        private String status = null;
        private boolean loadAdmin = false;
        private boolean forceLoadAdmin = false;

        LoadParser(String[] args) {
            super(args);
        }

        @Override
        public void usage(String errorMsg) {
            if (errorMsg != null) {
                System.err.println(errorMsg);
            }
            System.err.println(KVSTORE_USAGE_PREFIX + COMMAND_NAME + "\n\t" +
                               COMMAND_ARGS);
            System.exit(-1);
        }

        @Override
        protected boolean checkArg(String arg) {
            if (arg.equals(SOURCE_FLAG)) {
                source = nextArg(arg);
                return true;
            }
            if (arg.equals(STATUS_FLAG)) {
                status = nextArg(arg);
                return true;
            }
            if (arg.equals(ADMIN_LOAD_FLAG)) {
                loadAdmin = true;
                return true;
            }

            if (arg.equals(FORCE_ADMIN_FLAG)) {
                forceLoadAdmin = true;
                return true;
            }
            return false;
        }

        @Override
        protected void verifyArgs() {
            if (getHostname() == null) {
                missingArg(HOST_FLAG);
            }
            if (getRegistryPort() == 0) {
                missingArg(PORT_FLAG);
            }
            if (getStoreName() == null && !loadAdmin) {
                missingArg(STORE_FLAG);
            }
            if (source == null) {
                missingArg(SOURCE_FLAG);
            }
        }

        public String getStatusFile() {
            return status;
        }

        public String getSourceDir() {
            return source;
        }

        public boolean getLoadAdmin() {
            return loadAdmin;
        }

        public boolean getForceLoadAdmin() {
            return forceLoadAdmin;
        }
    }

    public static void main(String[] args)
        throws Exception {

        LoadParser lp = new LoadParser(args);
        lp.parseArgs();
        if (lp.getLoadAdmin()) {
            loadAdmin(lp);
            return;
        }
        Load load =
            new Load(new File(lp.getSourceDir()),
                     lp.getStoreName(),
                     lp.getHostname(),
                     lp.getRegistryPort(),
                     lp.getUserName(),
                     lp.getSecurityFile(),
                     lp.getStatusFile(),
                     lp.getVerbose(),
                     System.out);
        try {
            long total = load.run();
            System.out.println("Load succeeded, wrote " + total + " records");
        } catch (Exception e) {
            System.err.println("Load operation failed with exception: " +
                               LoggerUtils.getStackTrace(e));
            System.exit(-1);
        }
    }

    /**
     * Loads metadata from the admin rather than store data.  In this case
     * the source directory must be an Admin's environment.  The store name,
     * if provided, is ignored.
     *
     * This loads both table metadata and security metadata.
     */
    private static void loadAdmin(LoadParser lp) {
        loadAdmin(new File(lp.getSourceDir()),
                  lp.getHostname(),
                  lp.getRegistryPort(),
                  lp.getUserName(),
                  lp.getSecurityFile(),
                  lp.getVerbose(),
                  lp.getForceLoadAdmin(),
                  System.out);
    }

    public static void loadAdmin(File envDir,
                                 String targetHost,
                                 int targetPort,
                                 String user,
                                 String securityFile,
                                 boolean verboseOutput,
                                 boolean forceLoad,
                                 PrintStream output) {
        try {
            LoadAdmin load =
                new LoadAdmin(envDir, targetHost, targetPort, user,
                              securityFile, verboseOutput, forceLoad, output);
            load.loadMetadata();
        } catch (Exception e) {
            System.err.println("Admin load operation failed with exception: " +
                               LoggerUtils.getStackTrace(e));
        }
    }
}
