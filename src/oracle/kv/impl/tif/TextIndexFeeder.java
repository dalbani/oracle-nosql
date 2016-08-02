/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
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

package oracle.kv.impl.tif;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sleepycat.je.rep.subscription.SubscriptionConfig;
import com.sleepycat.je.rep.utilint.HostPortPair;
import com.sleepycat.je.utilint.VLSN;

import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.api.table.RowImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.util.FileNames;
import oracle.kv.impl.util.FileUtils;
import oracle.kv.impl.util.JsonUtils;
import oracle.kv.table.Index;

/**
 * TextIndexFeeder is responsible for
 *
 * 1. receiving data from source node, either via the replication stream or
 * partition transfer
 * 2. converting received data to ES operation based on the text index
 * 3. sending indexable operations to ES cluster
 * 4. conducting periodical checkpoint to ES cluster
 */
public class TextIndexFeeder {

    /* all es indices managed by TIF start with ondb */
    public static final String ES_INDEX_NAME_PREFIX = "ondb";
    /* constant es mapping for each es index */
    public static final String ES_INDEX_TYPE = "text_index_mapping";
    /* es index for internal use, _prefix to avoid conflict */
    public static final String ES_INDEX_NAME_FOR_CKPT = "_checkpoint";
    /* key to store ckpt info */
    public static final String CHECKPOINT_KEY_PREFIX = "tif_ckpt_key_";
    /* prefix of tif env diretory */
    public static final String TIF_ENV_DIR_PREFIX = "tif_env_";

    /* TODO: need parametrization */
    /* time based checkpoint interval in seconds */
    public static final long CKPT_INTERVAL_SECONDS = 2*60;

    private static final byte emptyBytes[] = {};

    /* logger */
    private final Logger logger;

    /* table meta from KV store */
    private TableMetadata tableMetadata;

    /* ES client and admin client */
    private final ElasticsearchHandler esHandler;

    /* configuration, subscription manager, and checkpoint manager */
    private final SubscriptionConfig config;
    private final SubscriptionManager subManager;
    private final CheckpointManager ckptManager;

    /* info about source RN */
    private final SourceRepNode sourceRN;
    /* name of kvstore from which TIF streams data  */
    private final String storeName;

    /* txn agenda for replication stream */
    private final TransactionAgenda txnAgendaForRepStream;
    /* txn agenda for partition transfer */
    private final TransactionAgenda txnAgendaForPartTransfer;

    /* worker to stream data in txn agenda for replication stream */
    private TextIndexFeederWorker workerProcessRepStream;
    /* group of workers to stream data in txn agenda for partition readers */
    private ArrayList<TextIndexFeederWorker> workerPoolProcessPartTransfer;

    /**
     * Constructor of TextIndexFeeder.
     *
     * Initialize internal data structures and start worker threads.
     *
     * @param sourceRN     rep node from which to stream data
     * @param tifRN        rep node hosting TIF
     * @param esHandler    ElasticSearch handler
     * @param logger       logger
     *
     * @throws IllegalStateException
     */
    TextIndexFeeder(SourceRepNode sourceRN,
                    HostRepNode tifRN,
                    ElasticsearchHandler esHandler,
                    Logger logger)
        throws IllegalStateException {

        this.logger = logger;
        this.sourceRN = sourceRN;
        this.esHandler = esHandler;
        storeName = sourceRN.getStoreName();
        tableMetadata = sourceRN.getTableMetadata();

        /* for each text index create ES index */
        initializeESIndices(tableMetadata.getTextIndexes());

        config = buildSubscriptionConfig(sourceRN, tifRN);
        subManager = new SubscriptionManager(sourceRN, tifRN, config, logger);

        ckptManager =
            new CheckpointManager(this,
                                  deriveCkptESIndexName(storeName),
                                  deriveESIndexType(),
                                  CHECKPOINT_KEY_PREFIX + config.getGroupName(),
                                  esHandler,
                                  logger);

        /* start time interval based checkpoint */
        ckptManager.startCheckpoint();
        /* create txn agendas */
        txnAgendaForRepStream = new TransactionAgenda(esHandler, logger, "ta-str");
        txnAgendaForPartTransfer = new TransactionAgenda(esHandler,logger, "ta-tra");

        /* start txn agenda workers */
        startWorkers();
    }

    /**
     * Switches to start everything under the hood!
     */
    void startFeeder() {

        /* first get the checkpoint to see where to start */
        CheckpointState ckpt = ckptManager.fetchCkptFromES();

        /* start from day 0 if no checkpoint */
        if (ckpt == null || !isValidCheckpoint(ckpt)) {
            logger.log(Level.INFO,
                       "Found no checkpoint or invalid checkpoint, start " +
                       "stream from {0}", VLSN.FIRST_VLSN);
            if (ckpt != null) {
                ckptManager.deleteCheckpointFromES();
            }
            subManager.startStream(VLSN.FIRST_VLSN);
        } else {

            /**
             * We only do checkpoint during ongoing phase, but partitions
             * may come and go during failure. After verifying a checkpoint
             * state from ES, we first check whether the list of completed
             * partitions matches the list of hosted partitions in source
             * node.
             *
             * If yes, we will resume TIF with ongoing phase from the
             * checkpoint VLSN.
             *
             * If no, the source may have newly immigrated partition
             * not in the completed list in checkpoint, such partition
             * need to be transferred by partition transfer.
             */
            final VLSN vlsn = ckpt.getCheckpointVLSN();
            Set<PartitionId> toTransfer =
                new HashSet<>(subManager.getManagedPartitions());
            toTransfer.removeAll(ckpt.getPartsTransferred());
            if (toTransfer.isEmpty()) {
                logger.log(Level.INFO,
                           "No new partitions found at source RN, resume " +
                            "stream from vlsn {0} in checkpoint: {1}",
                           new Object[]{vlsn.getNext(), ckpt.toString()});
                subManager.startStream(vlsn.getNext());
            } else {
                logger.log(Level.INFO,
                           "Found {0} new partitions at source, start stream " +
                           "from vlsn {1}, also start partition transfer for " +
                           "partitions: {2}.",
                           new Object[]{toTransfer.size(), vlsn,
                               SubscriptionManager
                                   .partitionListToString(toTransfer)});
                /* delete ckpt since go back to init phase */
                ckptManager.deleteCheckpointFromES();
                subManager.startStream(vlsn, toTransfer);
            }
        }
    }

    /**
     * Stops TIF, delete ES index if needed
     *
     * @param deleteIndex true if delete ES index
     */
    void stopFeeder(boolean deleteIndex) {

        /* shutdown ckpt manager, no more checkpoint */
        ckptManager.stop();

        /* Shutdown TransactionAgenda's batch timer. */
        txnAgendaForRepStream.stop();
        txnAgendaForPartTransfer.stop();

        /* shutdown all subscription */
        subManager.shutdown(SubscriptionState.SHUTDOWN);

        /* shut down all workers */
        workerProcessRepStream.cancel();
        for (TextIndexFeederWorker worker : workerPoolProcessPartTransfer) {
            worker.cancel();
        }

        /* finally delete ES index for each text index, if requested */
        if (deleteIndex) {

            if (tableMetadata != null) {
                deleteESIndices(tableMetadata.getTextIndexes());
            }

            /* delete checkpoint ES index */
            final String ckptESindex = deriveCkptESIndexName(storeName);
            if (esHandler.existESIndex(ckptESindex)) {
                esHandler.deleteESIndex(ckptESindex);
                logger.log(Level.INFO,
                           "Checkpoint ES index {0} has been deleted.",
                           ckptESindex);
            }
        }

        esHandler.close();
    }

    /**
     * Drops an ES index
     *
     * @param esIndexName name of ES index to drop
     */
    void dropIndex(final String esIndexName) throws IllegalStateException {

        if (!esHandler.existESIndex(esIndexName)) {
            logger.log(Level.WARNING, "ES index {0} does not exist, ignore.",
                       esIndexName);
        } else {
            esHandler.deleteESIndex(esIndexName);
        }
    }

    /**
     * Ensures ES index and mapping exists for each text index
     */
    void ensureESIndexAndMapping() {

        if (tableMetadata == null) {
            return;
        }

        final String esIndexType = deriveESIndexType();
        final List<Index> indices = tableMetadata.getTextIndexes();
        for (Index index : indices) {
            if (index.getType().equals(Index.IndexType.TEXT)) {
                final String tableName = index.getTable().getFullName();
                final String esIndexName = deriveESIndexName(storeName,
                                                             tableName,
                                                             index.getName());
                final IndexImpl indexImpl = (IndexImpl)index;

                /* ensure ES index exists */
                ensureESIndex(esIndexName, indexImpl.getProperties());

                /* ensure the mapping exists */
                final String mappingSpec =
                    ElasticsearchHandler.constructMapping(indexImpl);
                if (!esHandler.existESIndexMapping(esIndexName, esIndexType)) {
                    /* create new mapping if not exists */
                    esHandler.createESIndexMapping(esIndexName,
                                                   esIndexType,
                                                   mappingSpec);
                    logger.log(Level.INFO,
                               "ES mapping created for text index {0}",
                               new Object[]{index.getName()});
                } else {
                    /* if mapping exist, verify it */
                    final String existingMapping =
                        esHandler.getESIndexMapping(esIndexName, esIndexType);
                    if (JsonUtils.jsonStringsEqual(existingMapping,
                                                              mappingSpec)) {
                        logger.log(Level.INFO,
                                   "Mapping already exists for index " +
                                   esIndexName);
                    } else {
                        logger.log(Level.INFO,
                                   "Existing mapping spec does not match " +
                                   "that for es index {0} expected spec {1}  " +
                                   "existing spec {2}",
                                   new Object[]{esIndexName, mappingSpec,
                                       existingMapping});
                        /* unable to delete mapping, so just delete es index */
                        esHandler.deleteESIndex(esIndexName);
                        /* recreate a new index */
                        esHandler.createESIndex(esIndexName,
                                                indexImpl.getProperties());
                        /* create new mapping */
                        esHandler.createESIndexMapping(esIndexName,
                                                       esIndexType,
                                                       mappingSpec);
                        logger.log(Level.INFO,
                                   "Old mapping deleted and a new ES mapping" +
                                   " created for index: {0}, mapping spec: {1}",
                                   new Object[]{esIndexName, mappingSpec});
                    }

                }
            }
        }
    }

    /**
     * @hidden
     *
     * For internal test only: start tif from initial phase, regardless of
     * start VLSN.
     */
    void startFeederFromInitPhase() {
        subManager.startStream(subManager.getManagedPartitions());
    }

    /**
     * Adds a new incoming partition to TIF
     *
     * @param pid id of partition
     */
    synchronized void addPartition(PartitionId pid) {
        subManager.addPartition(pid);
        logger.log(Level.INFO,
                   "Partition {0} has been added, and TIF will start " +
                   "receiving entries from the partition", pid);
    }
    
    /**
     * Removes a partition tif no longer interested
     *
     * @param pid  id of partition to remove
     */
    synchronized void removePartition(PartitionId pid) {
        subManager.removePartition(pid);
        logger.log(Level.INFO,
                   "Partition pid has been removed, and TIF will no longer " +
                   "receive entries from the partition.", pid);
    }
    
    /**
    * Checks if a partition is managed by the TIF
    *
    *
    * @param pid  id of partition
    */
    boolean isManangedPartition(PartitionId pid) {
        return subManager.isManangedPartition(pid);
    }

    /**
     * Returns subscription manager
     *
     * @return subscription manager
     */
    SubscriptionManager getSubManager() {
        return subManager;
    }

    /**
     * Compares the text indices defined in table meatadata with those
     * already existent on ES, and returns a set of new indices
     *
     * @return a set of es indices to add
     */
    Set<String> esIndicesToAdd() {

        /* get all ES index names in the store */
        final Set<String> existingESIndices =
            esHandler.getAllESIndexNamesInStore(storeName);

        /* build list of all es index names for all text indices */
        final Set<String> allESIndices =
            buildESIndicesNames(storeName, tableMetadata.getTextIndexes());

        /* compute what's to add */
        allESIndices.removeAll(existingESIndices);
        if (allESIndices.isEmpty()) {
            logger.log(Level.FINE,
                       "All text indices in the table metadata have " +
                       "corresponding ES indices, nothing to add.");
        } else {
            logger.log(Level.INFO,
                       "Following ES indices for the text index in table " +
                       "metadata are not found in ES: {0}",
                       Arrays.toString(allESIndices.toArray()));

        }
        return allESIndices;
    }

    /**
     * Compares the text indices defined in table meatadata with those
     * already existent on ES, and returns a set of ES indices that need
     * be dropped. Only ES indices corresponding to the user-defined text
     * indices are considered. The internal checkpoint ES index is excluded.
     *
     * @return a set of es indices to drop
     */
    Set<String> esIndicesToDrop() {

        /* get all ES index names under the store */
        final Set<String> existingESIndices =
            esHandler.getAllESIndexNamesInStore(storeName);
        /* exclude internal checkpoint es index */
        existingESIndices.remove(deriveCkptESIndexName(storeName));

        /* build list of all es index names for all text indices */
        final Set<String> allESIndices =
            buildESIndicesNames(storeName, tableMetadata.getTextIndexes());

        /* compute what's to drop */
        existingESIndices.removeAll(allESIndices);

        if (existingESIndices.isEmpty()) {
            logger.log(Level.FINE,
                       "All ES indices have corresponding text indices in " +
                       "the table metadata, nothing to drop.");

        } else {
            logger.log(Level.INFO,
                       "Following ES indices are not found in table " +
                       "metadata: {0}",
                       Arrays.toString(existingESIndices.toArray()));
        }
        return existingESIndices;
    }

    SubscriptionState getSubscriptionState() {
        return subManager.getState();
    }

    /**
     * Register a user-defined post commit callback
     *
     * @param cbk commit callback to register
     */
    void setPostCommitCbk(TransactionPostCommitCallback cbk) {
        txnAgendaForPartTransfer.setPostCommitCbk(cbk);
        txnAgendaForRepStream.setPostCommitCbk(cbk);
    }

    /**
     * Create a mapping in ES index for a given text index. ES will use the
     * mapping to index the doc from TIF.
     *
     * @param kvstoreName   name of store
     * @param tableName     name of table
     * @param textIndexName name of the text index defined on the table
     * @throws IllegalStateException
     */
    void createMappingForTextIndex(String kvstoreName,
                                   String tableName,
                                   String textIndexName)
        throws IllegalStateException {

        final String esIndexName = deriveESIndexName(kvstoreName,
                                                     tableName,
                                                     textIndexName);
        final String esIndexType = deriveESIndexType();
        /* create and add mapping to ES index */
        final TableImpl tableImpl = tableMetadata.getTable(tableName);
        final Index textIndex = tableImpl.getTextIndex(textIndexName);
        final String mappingSpec =
            ElasticsearchHandler.constructMapping((IndexImpl)textIndex);

        esHandler.createESIndexMapping(esIndexName, esIndexType, mappingSpec);
    }

    /**
     * Create a checkpoint state for checkpoint thread.
     *
     * @return a checkpoint state or null if no checkpoint
     */
    CheckpointState prepareCheckpointState() {

        /*
         * Resume from init phase is tricky. The bookmark VLSN
         * may not be still available in source node when TIF
         * resumes. For now we only do checkpoint in ongoing phase.
         */
        if (subManager.getState()
                      .equals(SubscriptionState.REPLICATION_STREAM)) {

            return new CheckpointState(config.getGroupName(),
                                       config.getGroupUUID(),
                                       config.getFeederHost(),
                                       txnAgendaForRepStream
                                           .getLastCommittedVLSN(),
                                       subManager.getManagedPartitions(),
                                       System.currentTimeMillis());

        }

        return null;
    }

    /**
     * Sets new table metadata for TIF
     *
     * @param newTmd  new table metadata
     */
    void setTableMetadata(TableMetadata newTmd) {
        tableMetadata = newTmd;
    }

    /*---------------------------------------------------------------*/
    /*---                    static functions                    ---*/
    /*---------------------------------------------------------------*/

    /**
     * Derives ES index prefix. The prefix is "ondb.<kvstore>"
     *
     * @param storeName  name of kv store
     * @return  es index prefix
     */
    public static String deriveESIndexPrefix(final String storeName) {
        return ES_INDEX_NAME_PREFIX + "." + storeName.toLowerCase();
    }

    /**
     * Derives a unique ES index name. We would create an ES index for each
     * text index defined on a table. To uniquely identify an ES index, its
     * name is derived from the name of store, table as well as the text index
     * name itself. For example, an ES index created for text index
     * mytextindex on table mytable in store mystore will be named as
     * "ondb.mystore.mytable.mytextindex"
     *
     * @param storeName      name of the kvstore
     * @param tableName      name of the table
     * @param textIndexName  name of the text index
     *
     * @return ES index name
     */
    public static String deriveESIndexName(final String storeName,
                                           final String tableName,
                                           final String textIndexName) {
        return deriveESIndexPrefix(storeName) + "." +
               tableName.toLowerCase() + "." +
               textIndexName.toLowerCase();
    }

    /**
     * Builds an ES index type. Because each ES index would be mapped to a
     * single text index, ES index name itself is sufficient to identify the
     * corresponding text index. Therefore the ES index type is named as a
     * constant.
     *
     * @return ES index type
     */
    public static String deriveESIndexType() {
        return ES_INDEX_TYPE;
    }

    /**
     * Builds ES index name for checkpoint. There is one ES index for
     * checkpoint per each store. The ckpt ES index would be like
     * "ondb.kvstore.tif_checkpoint"
     *
     * @return name of ES index for checkpoint
     */
    static String deriveCkptESIndexName(final String storeName) {
        return deriveESIndexPrefix(storeName) + "." + ES_INDEX_NAME_FOR_CKPT;
    }

    /**
     * Builds list of es index names for all text indices in table metadata
     *
     * @param textIndices  list of text indices
     * @return  set of ES index names corresponding to the text indices
     */
    static Set<String> buildESIndicesNames(final String storeName,
                                           final List<Index> textIndices) {
        final Set<String> allESIndices = new HashSet<>();
        for (Index index : textIndices) {
            /* must be text index */
            assert (index.getType().equals(Index.IndexType.TEXT));

            final String esIndexName = deriveESIndexName(storeName,
                                                         index.getTable()
                                                              .getFullName(),
                                                         index.getName());
            allESIndices.add(esIndexName);

        }
        return allESIndices;
    }

    /*---------------------------------------------------------------*/
    /*---                    private functions                    ---*/
    /*---------------------------------------------------------------*/
    /* check if the checkpoint state fetched from ES is valid */
    private boolean isValidCheckpoint(CheckpointState ckpt) {

        /* verify rep group name and its uuid id */
        if (!sourceRN.getGroupName().equals(ckpt.getGroupName()) ||
            !sourceRN.getGroupUUID().equals(ckpt.getGroupUUID())) {

            logger.log(Level.INFO,
                       "Mismatch rep group. group name (id) in ckpt:{0}({1}) " +
                       "while the expected group name (id) is: {2}({3})",
                       new Object[]{ckpt.getGroupName(), ckpt.getGroupUUID(),
                           sourceRN.getGroupName(), sourceRN.getGroupUUID()});

            return false;
        }

        /* if group matches but source has migrated, log it */
        if (!sourceRN.getSourceNodeName().equals(ckpt.getSrcNodeHost())) {
            logger.log(Level.INFO,
                       "Mismatch source node, source have migrated, source " +
                       " node name in the ckpt: {0}, while expected is: {1}",
                       new Object[]{ckpt.getSrcNodeHost(),
                           sourceRN.getSourceNodeName()});
        }

        return true;
    }

    /* start all worker threads */
    private void startWorkers() {

        workerProcessRepStream =
            new TextIndexFeederWorker("rep_str_agenda_worker", subManager
                .getInputQueueRepStream());
        workerProcessRepStream.start();
        logger.log(Level.INFO, "Worker thread processing rep stream started, " +
                               "worker id: " +
                               workerProcessRepStream.getWorkerId());

        /* start all partition transfer worker processes, id from 1 */
        final int dopPartTrans = subManager.getDOPForPartTransfer();
        workerPoolProcessPartTransfer = new ArrayList<>(dopPartTrans);
        for (int i = 0; i < dopPartTrans; i++) {
            TextIndexFeederWorker worker =
                new TextIndexFeederWorker("part_xfer_agenda_worker_" + i,
                                          subManager.getInputQueuePartReader());
            workerPoolProcessPartTransfer.add(worker);
            worker.start();
        }
        logger.log(Level.INFO,
                   "# worker threads for partition transfer started: {0}",
                   dopPartTrans);
    }

    /* build a subscription configuration from source and host node */
    private SubscriptionConfig buildSubscriptionConfig(SourceRepNode feederRN,
                                                       HostRepNode tifHostRN)
        throws IllegalStateException {

        final File tifEnvDir = createEnvDirectory(tifHostRN);
        final String tifHostPort =
            HostPortPair.getString(tifHostRN.getHost(), tifHostRN.getPort());
        final String feederHostPort =
            HostPortPair.getString(feederRN.getSourceHost(),
                                   feederRN.getSourcePort());
        try {
              return
                new SubscriptionConfig(tifHostRN.getTifNodeName(),
                                       tifEnvDir.getAbsolutePath(),
                                       tifHostPort,
                                       feederHostPort,
                                       feederRN.getGroupName(),
                                       feederRN.getGroupUUID());
        } catch (UnknownHostException e) {
            logger.warning("Unknown host " + e.getMessage());
            throw new IllegalStateException("Unknown host " + e.getMessage());
        }

    }

    /* create a TIF environment directory at TIF host node */
    private File createEnvDirectory(HostRepNode host)
        throws IllegalStateException {

        final File repHome = FileNames.getServiceDir(host.getRootDirPath(),
                                                     host.getStoreName(),
                                                     null,
                                                     host.getStorageNodeId(),
                                                     host.getRepNodeId());
        final String tifDir = TIF_ENV_DIR_PREFIX + host.getTifNodeName();
        final File tifEnvDir = new File(repHome, tifDir);

        if (tifEnvDir.exists() && !FileUtils.deleteDirectory(tifEnvDir)) {
            logger.log(Level.WARNING,
                       "Unable to delete an old TIF environment directory" +
                       " at: {0}.",
                       tifEnvDir.toString());

            throw new IllegalStateException("Unable to create TIF " +
                                            "env directory " + tifDir +
                                            " at TIF host node " +
                                            host.getRepNodeId()
                                                .getFullName());
        }

        if (!tifEnvDir.exists() && FileNames.makeDir(tifEnvDir)) {
            logger.log(Level.INFO,
                       "Successfully create new TIF environment directory " +
                       "{0} at: {1}" ,
                       new Object[]{tifDir, tifEnvDir.toString()});

            return tifEnvDir;
        }
        logger.log(Level.WARNING,
                   "Unable to create a new TIF env dir {0} at: {1}",
                   new Object[]{tifDir, tifEnvDir.toString()});

        throw new IllegalStateException("Unable to create TIF " +
                                        "environment directory at TIF " +
                                        "host node " +
                                        host.getRepNodeId()
                                            .getFullName());
    }

    /*
     * Initializes ES index for each text index
     *
     * @param textIndices  list of all text indices
     */
    private void initializeESIndices(final List<Index> textIndices) {
        for (Index index : textIndices) {
            /* create ES index for each text index */
            if (index.getType().equals(Index.IndexType.TEXT)) {
                final String esIndexName =
                    deriveESIndexName(storeName,
                                      index.getTable().getFullName(),
                                      index.getName());
                esHandler.createESIndex(esIndexName,
                                        ((IndexImpl)index).getProperties());
                logger.log(Level.FINE,
                           "ES index {0} created for text index {1} in " +
                           "kvstore {2}",
                           new Object[]{esIndexName, index.getName(),
                               storeName});
            }
        }
    }

    /*
     * Deletes ES index for each text index
     *
     * @param textIndices  list of all text indices
     */
    private void deleteESIndices(final List<Index> textIndices) {

        for (Index index : textIndices) {
            /* delete ES index for each text index */
            if (index.getType().equals(Index.IndexType.TEXT)) {
                final String esIndexName =
                    deriveESIndexName(storeName,
                                      index.getTable().getFullName(),
                                      index.getName());

                if (esHandler.existESIndex(esIndexName)) {
                    esHandler.deleteESIndex(esIndexName);
                }
                logger.log(Level.INFO,
                           "ES index {0} deleted for text index {1} in " +
                           "kvstore {2}",
                           new Object[]{esIndexName, index.getName(),
                               storeName});
            }
        }
    }

    /* Ensures ES index exists, create an ES index if not */
    private void ensureESIndex(final String esIndexName,
                               Map<String,String> properties) {
        if (!esHandler.existESIndex(esIndexName)) {
            esHandler.createESIndex(esIndexName, properties);
        }
    }

    /*
     * TextIndexFeeder worker thread to dequeue each entry from queue, and
     * post the entry to TransactionAgenda if it is a data operation. If the
     * entry is an commit/abort operation, the worker will close the transaction
     * in agenda by committing it to ES or abort it.
     */
    private class TextIndexFeederWorker extends Thread {

        private final String workerId;
        private final BlockingQueue<DataItem> queue;
        private volatile boolean cancelled;

        TextIndexFeederWorker(String workerId,
                              BlockingQueue<DataItem> queue) {
            this.workerId = workerId;
            this.queue = queue;
            cancelled = false;
            /* Prevent its hanging the process on exit. */
            setDaemon(true);
        }

        @Override
        public void run() {

            while (!cancelled) {
                DataItem entry = null;
                /* TIF worker should keep running until it is cancelled */
                try {
                    entry = queue.take();
                    if (!entry.getPartitionId().equals(PartitionId.NULL_ID)) {
                        /* has a valid part. id, meaning it is from part.
                         * transfer
                         */
                        handleDataItemHelper(entry, txnAgendaForPartTransfer);
                    } else {
                        /* no part id, meaning it is from rep stream */
                        handleDataItemHelper(entry, txnAgendaForRepStream);
                    }

                } catch (InterruptedException e) {
                    logger.log(Level.WARNING,
                                "Interrupted input queue take operation, " +
                                "retrying. Reason: {0}.", e.getMessage());
                } catch (Exception exp) {
                    /*
                     * tif worker should not crash when it is unable to process
                     * an entry, instead, it should log the entry and continue.
                     */
                    logger.log(Level.WARNING,
                               "TIF worker " + workerId + " is unable to " +
                               "process an entry due to exception " +
                               exp.getMessage() +
                               ", log entry and continue [" +
                               ((entry == null) ?
                                "null" : entry.toString()) + "]", exp);
                }
            }
        }

        public void cancel() {
            cancelled = true;
        }

        public String getWorkerId() {
            return workerId;
        }

        /* process each item in the input queue */
        private void handleDataItemHelper(DataItem entry,
                                          TransactionAgenda agenda) {

            final byte[] keyBytes = entry.getKey();
            byte[] valueBytes = entry.getValue();
            
            valueBytes = (valueBytes == null) ? emptyBytes : valueBytes;

            /* abort or commit a txn to ES */
            if (entry.isTxnAbort()) {
                agenda.abort(entry.getTxnID());
                return;
            }
            if (entry.isTxnCommit()) {
                agenda.commit(entry.getTxnID(), entry.getVLSN());
                return;
            }

            /* must be a put or del operation with a valid key */
            assert (keyBytes != null);
            final String esIndexType = TextIndexFeeder.deriveESIndexType();
            /* check each text indices and convert entry to an index op */
            for (Index ti : tableMetadata.getTextIndexes()) {
                final String indexName = ti.getName();
                final String tableName = ti.getTable().getFullName();
                final String esIndexName =
                    deriveESIndexName(storeName, tableName, indexName);
                final IndexImpl tiImpl = (IndexImpl) ti;

                /*
                 * deserialize a key/value pair to a RowImpl iff the key is
                 * interesting to the text index. Null RowImpl indicates key
                 * is not interesting to the text index and can be ignored.
                 */
                RowImpl row = tiImpl.deserializeRow(keyBytes, valueBytes);
                if (row != null) {
                    /* this entry is interesting to the text index */
                    IndexOperation op;
                    if (entry.isDelete()) {
                        op = ElasticsearchHandler
                            .makeDeleteOperation(tiImpl, esIndexName,
                                                 esIndexType, row);
                    } else {
                        op = ElasticsearchHandler
                            .makePutOperation(tiImpl, esIndexName,
                                              esIndexType, row);
                    }

                    if (op == null) {
                        continue;
                    }
                    /*
                     * if entry is a COPY op from partition transfer,
                     * directly commit to ES
                     */
                    if(entry.isCopyInPartTrans()) {
                        agenda.commit(op);
                    } else {
                        /* post op to agenda */
                        agenda.addOp(entry.getTxnID(), op);
                    }
                }
            }
        }
    }
}
