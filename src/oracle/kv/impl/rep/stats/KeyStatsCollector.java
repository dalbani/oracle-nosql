/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2014 Oracle and/or its affiliates.  All rights reserved.
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

package oracle.kv.impl.rep.stats;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.kv.DurabilityException;
import oracle.kv.KVStore;
import oracle.kv.RequestTimeoutException;
import oracle.kv.impl.admin.param.RepNodeParams;
import oracle.kv.impl.api.RequestHandlerImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.api.table.TableMetadata.TableMetadataIteratorCallback;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.param.ParameterListener;
import oracle.kv.impl.param.ParameterMap;
import oracle.kv.impl.param.ParameterState;
import oracle.kv.impl.rep.RepNode;
import oracle.kv.impl.rep.RepNodeService;
import oracle.kv.impl.rep.RepNodeService.KVStoreCreator;
import oracle.kv.impl.rep.stats.IndexLeaseManager.IndexLeaseInfo;
import oracle.kv.impl.rep.stats.PartitionLeaseManager.PartitionLeaseInfo;
import oracle.kv.impl.rep.stats.StatsLeaseManager.LeaseInfo;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.util.PollCondition;
import oracle.kv.table.Index;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableIterator;

import com.sleepycat.je.rep.StateChangeEvent;

/**
 * The class is to gather statistics information. A lease mechanism is within
 * its inner class ScanningThread. The lease mechanism is to deal with
 * coordinating scanning among RNs in a shard and deal with the failures of RNs.
 * The mechanism is as follows:
 *
 * 1. Get a partition(or secondary database), and update database to get or
 * create a lease(if a lease associated with the target partition or secondary
 * database exists, then get it, or create a new lease), and then start to scan
 * the partition(or secondary database).
 *
 * 2. During the scanning, check whether the lease expires or not. If the
 * lease is within it's within 10% of its expiry time, extend the expiry time.
 *
 * 3. Extend a lease to ensure the scanning can be completed by a same RN, and
 * also deal with failures. If the current ScanningThread is down during
 * scanning partition(or secondary database), because a lease time is short ,
 * and another ScanningThread within another RN will continue to scan the
 * partition(or secondary database) after the lease time expires.
 *
 * 4. Modify last updated time to ensure the frequency of scanning partitions
 * (or secondary database), and also coordinate scanning among in RNs.
 */
public class KeyStatsCollector implements ParameterListener {
    /* Name of scanning thread */
    private static final String THREAD_NAME = "Key Stats Gather Thread";

    /*
     * A flag to mark whether the RN which StatisticsGater is within is alive
     * or not. That is, it's in the master or replica state.
     */
    private volatile boolean isActivated;

    private final RepNodeService repNodeService;
    private final Logger logger;

    private TableAPI tableAPI;
    private KVStoreCreator creator;

    /* The map is to store the table name and table pairs */
    private Map<String, TableImpl> tableListMap;

    /* A flag to mark whether StatisticsGather is shutdown or not */
    private volatile boolean shutdown = false;

    /* Lease managers to control the lease of scan partitions and indices */
    private PartitionLeaseManager partitionLeaseManager;
    private IndexLeaseManager indexLeaseManager;

    /*
     * A handler of StatsScan and used to stop scanning when thread is
     * stopped
     */
    private StatsScan<? extends LeaseInfo> statsScanHandler;

    /* Scanning thread handle */
    private ScanningThread scanningThread;

    /* Variables to control scanning which are loaded from parameters */
    private volatile boolean statsEnabled;
    private volatile long statsGatherInterval;
    private volatile int statsRequestThreshold;
    private volatile long statsLeaseDuration;
    private volatile long statsLowActivePeriod;

    /* It is used to control the sleep time or waiting time in scanning */
    private volatile long statsSleepWaitDuration;

    public KeyStatsCollector(RepNodeService repNodeService,
                                  Logger logger) {
        this.repNodeService = repNodeService;
        this.logger = logger;
    }

    /**
     * Attempt to start or stop ScanThread based on collector state.
     */
    private synchronized void startStopScanThread() {

        if (statsEnabled && isActivated && !shutdown) {

            /*
             * Create a new instance of ScanningThread and start it when there
             * is no instance of ScanningThread or no running ScanningThread
             */
            if (scanningThread == null || scanningThread.isStopping()) {
                scanningThread = new ScanningThread();
                scanningThread.start();
            }
        } else {
            if (scanningThread != null) {
                scanningThread.stopScan();
            }
        }
    }

    /**
     * Load statistics parameters
     */
    private void loadStatsParametersAndStart(RepNodeParams repNodeParams) {
        statsEnabled = repNodeParams.getStatsEnabled();
        statsGatherInterval = repNodeParams.getStatsGatherInterval();
        statsRequestThreshold = repNodeParams.getStatsRequestThreshold();
        statsLeaseDuration = repNodeParams.getStatsLeaseDuration();
        statsLowActivePeriod = repNodeParams.getStatsLowActivePeriod();
        statsSleepWaitDuration = repNodeParams.getStatsSleepWaitDuration();

        /* Start or stop scan thread in case statsEnabled has changed */
        startStopScanThread();
    }

    @Override
    public void newParameters(ParameterMap oldMap, ParameterMap newMap) {
        if (oldMap != null) {
            final ParameterMap filtered =
                oldMap.diff(newMap, true /* notReadOnly */).
                    filter(EnumSet.of(ParameterState.Info.POLICY));
            if (filtered.size() == 0) {
                return;
            }
        }

        /* If parameters changed, re-load statistics parameters */
        loadStatsParametersAndStart(new RepNodeParams(newMap));
    }

    /**
     * Used to inform the collector about state change events associated
     * with the replicated node.
     */
    public void noteStateChange(StateChangeEvent sce) {
        /*
         * The scanning only works when RN which KeyStatsCollector resides is
         * active, that is, the HA node is currently a master or a replica.
         */
        isActivated = sce.getState().isActive();

        /* Start or stop scan thread in case isActive has changed */
        startStopScanThread();
    }

    /**
     * Start scanning operation.
     * @param kvcreator is used to get KVStore handle.
     */
    public void startup(KVStoreCreator kvcreator) {
        this.creator = kvcreator;

        /* Waiting the old ScanningThread finishing */
        if (scanningThread != null && scanningThread.isAlive()) {
            try {
                scanningThread.join();
            } catch (InterruptedException ie) {
                /* Should not happen. */
                logger.log(Level.WARNING, "Old ScanningThread encounters " +
                                          "errors: " + ie);
            }
        }

        /* Attempt the latest parameters and start the thread of scanning */
        loadStatsParametersAndStart(repNodeService.getRepNodeParams());
    }

    /**
     * Stops the operation and waits for the thread to exit.
     */
    public void shutdown() {
        shutdown = true;

        /* Stop running scan */
        if (statsScanHandler != null) {
            statsScanHandler.stop();
        }

        try {
            if (scanningThread != null) {
                scanningThread.stopScan();
                /* Wait current ScanningThread exit */
                scanningThread.join();
            }
        } catch (InterruptedException ie) {
            /* Should not happen. */
            logger.log(Level.WARNING, "Stats gathering shutdown " +
                                      "encounter errors: " + ie);
        }
    }

    /**
     * This class is to do the real scanning work.
     */
    private class ScanningThread extends Thread {
        /* Set to true if the thread should stop */
        private boolean stop = false;

        private ScanningThread() {
            super(THREAD_NAME);
        }

        /* Returns true if stopScan() has been called */
        boolean isStopping() {
            return stop;
        }

        /* Stops the the scan */
        void stopScan() {
            stop = true;
        }

        /**
         * Initialize Table API
         */
        private boolean initializeTableAPI() {
            /*
             * Callers normally check that tableAPI is non-null before calling
             * this, but the tableAPI variable may become non-null by the time
             * they enter this method.
             */
            if (tableAPI != null) {
                return true;
            }

            final KVStore store = creator.getKVStore();
            /* Store is not ready */
            if (store == null) {
                return false;
            }

            try {
                tableAPI = store.getTableAPI();
            } catch (IllegalArgumentException iae) {
                throw new IllegalStateException("Unable to get Table API", iae);
            }

            return true;
        }

        /**
         * Scan partition databases and secondary databases. And delete
         * obsolete statistics info from all tables
         */
        private void scan() {

            /* Check whether can scan */
            if (stop) {
                return;
            }

            try {
                if (!initializeTableAPI()) {
                    logger.info("Unable to get Table API, scan exits");
                    return;
                }

                /* Check whether statistics tables exist or not */
                if (!checkLeaseTable()) {
                    return;
                }

                /* Scan partition databases */
                scanPartitions();

                /* Check whether to stop scanning */
                if (stop) {
                    return;
                }

                /* Scan secondary database */
                scanTableIndexes();

            } catch (Exception ignore) {
                /*
                 * Ignore all exceptions, and statements in the loop
                 * continue running in the next time, it is to ensure the
                 * statistics gathering always works even though exceptions
                 * are thrown.
                 */

                /* Log the exception */
                logger.log(Level.WARNING, "Stats scanning operation failed: " +
                                           ignore);
            }

            try {
                if (stop) {
                    return;
                }
                /*
                 * Delete obsolete statistics from statistics tables, because of
                 * the changes for tables and indexes
                 */
                deleteObsoleteStats();
            } catch (Exception ignore) {
                /*
                 * Ignore all exceptions, and statements in the loop
                 * continue running in the next time, it is to ensure the
                 * deletion of obsolete statistic always works even though
                 * exceptions are thrown.
                 */

                /* Log the exception */
                logger.log(Level.WARNING, "Obsolete statistics deleting " +
                                          "operation failed: " + ignore);
            }
        }

        /**
         * Check whether lease tables exist or not
         * @return true when all lease tables exist; Or return false
         */
        private boolean checkLeaseTable() {
            final TableMetadata metadata =
                    (TableMetadata)repNodeService.getRepNode().
                    getMetadata(MetadataType.TABLE);
            /* No metadata exists means no table exists */
            if (metadata == null) {
                return false;
            }

            /* Four tables are need to check */
            final String[] tablesToCheck = new String[] {
                    PartitionLeaseManager.TABLE_NAME,
                    IndexLeaseManager.TABLE_NAME,
                    PartitionScan.TABLE_NAME,
                    TableIndexScan.TABLE_NAME };

            for (String table : tablesToCheck) {
                if (metadata.getTable(table) == null) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void run() {
            logger.log(Level.FINE, "{0} start", this);

            try {
                /*
                 * This loop is to scan partition databases and secondary
                 * databases. And delete obsolete statistics info from all
                 * tables.
                 */
                while (!stop) {

                    /*
                     * Start scanning all data stored within RNs which
                     * KeyStatsCollector resides
                     */
                    scan();

                    /* Check whether can continue scanning */
                    if (stop) {
                        return;
                    }

                    /* Sleep some time to decrease CPU usage in endless loop */
                    try {
                        Thread.sleep(statsSleepWaitDuration);
                    } catch (InterruptedException ie) {
                        logger.log(Level.WARNING, "Interval sleeping of " +
                                                  "ScanningThread is " +
                                                  "interrupted: " + ie);
                    }
                }

                logger.log(Level.FINE, "{0} completes", this);
            } catch (Exception e) {
                /* Should not happen */
                logger.log(Level.SEVERE, "Scanning thread encounters error", e);
            }
        }

        /**
         * Check the conditions of starting scan and determine when to start
         * scanning.
         *
         * @throws Exception
         */
        private void startGathering() throws Exception {
            /*
             * Check the current active request count to avoid too heavy load to
             * impact the normal service provided by the RN
             */
            boolean isBusy = waitUntilNotBusy();

            /* Check whether can start scanning */
            if (stop || isBusy) {
                return;
            }

            /* Start scanning */
            statsScanHandler.runScan();
        }

        /**
         * Wait until this node is not busy, as determined by the trailing
         * request count activity.
         * @return true if the node is busy or the thread is stopped, false
         * otherwise
         * TODO: This method, or a variant of it, belongs on the RepNode itself.
         */
        private boolean waitUntilNotBusy() {
            /*
             * Check the current active request count to avoid too heavy load to
             * impact the normal service provided by the RN
             */

            final RequestHandlerImpl reqHandler =
                    repNodeService.getReqHandler();

            /*
             * Compute busy metric as a sustained period of low activity over
             * at a time period, rather than as an instantaneous value, which
             * can be misleading, since requests are busy. Besides, should be
             * check whether the node is stopped and scanning is enabled in the
             * loop
             */
            boolean result = new PollCondition((int)statsSleepWaitDuration,
                                               (int)statsLowActivePeriod * 10) {
                long lastPeakAt = System.currentTimeMillis();

                @Override
                protected boolean condition() {
                    final int currentRequestCount =
                            reqHandler.getActiveRequests();
                    if (currentRequestCount > statsRequestThreshold) {
                        lastPeakAt = System.currentTimeMillis();
                    }

                    return stop || (System.currentTimeMillis() - lastPeakAt) >
                                                        statsLowActivePeriod;
                }
            }.await();

            return stop || !result;
        }

        /**
         * Delete all obsolete statistics information from statistics tables.
         */
        private void deleteObsoleteStats() {
            final RepNode repNode = repNodeService.getRepNode();
            final Set<PartitionId> partIdSet = repNode.getPartitions();
            if (partIdSet.isEmpty()) {
                return;
            }

            logger.log(Level.FINE, "Delete obsolete statistics from " +
                        "statistics tables");
            /*
             * Get all tables include top tables and inner tables
             */
            tableListMap = getAllTables();
            if (tableListMap == null) {
                return;
            }

            final TableMetadata metadata =
                    (TableMetadata) repNode.getMetadata(MetadataType.TABLE);

            if (metadata == null) {
                return;
            }

            /* Delete obsolete statistics from TableStatsTable */
            Table table = metadata.getTable(PartitionScan.TABLE_NAME);
            if (table != null) {
                deleteStatsByTable(table.createPrimaryKey(),
                        PartitionScan.COL_NAME_TABLE_NAME);
            }

            /* Delete obsolete statistics from IndexLeaseTable */
            table = metadata.getTable(IndexLeaseManager.TABLE_NAME);
            if (table != null) {
                deleteStatsByTable(table.createPrimaryKey(),
                                   IndexLeaseManager.COL_NAME_TABLE_NAME);
                deleteStatsByIndex(table.createPrimaryKey(),
                                   IndexLeaseManager.COL_NAME_TABLE_NAME,
                                   IndexLeaseManager.COL_NAME_INDEX_NAME);
            }

            /* Delete obsolete statistics from IndexStatsTable */
            table = metadata.getTable(TableIndexScan.TABLE_NAME);
            if (table != null) {
                deleteStatsByTable(table.createPrimaryKey(),
                                   TableIndexScan.COL_NAME_TABLE_NAME);
                deleteStatsByIndex(table.createPrimaryKey(),
                                   TableIndexScan.COL_NAME_TABLE_NAME,
                                   TableIndexScan.COL_NAME_INDEX_NAME);
            }
        }

        /**
         * Delete statistics belongs to deleted tables from statistics tables.
         * A table is already deleted, all the statistics of the table should
         * be deleted. This method is to iterate all records of a statistics
         * or lease table, get table name from the record and use the table name
         * to check whether the table is deleted or not. If the table is
         * deleted, then remove its statistics from statistics or lease table.
         *
         * @param primaryKey is the primary key of the statistics/lease tables.
         * @param tableNameField is to indicate which column is to store table
         * name in statistics tables.
         */
        private void deleteStatsByTable(PrimaryKey primaryKey,
                                        String tableNameField) {
            /*
             * Get all primary keys from statistics tables and check whether the
             * associated records should be deleted or not.
             */
            final TableIterator<PrimaryKey> itr =
                    tableAPI.tableKeysIterator(primaryKey,  null, null);

            try {
                while (itr.hasNext() && !stop) {
                    try {
                        final PrimaryKey pk = itr.next();
                        final String storedTableName =
                            pk.get(tableNameField).asString().get();
                        if (!tableListMap.containsKey(storedTableName) &&
                            !storedTableName.equals(
                                    PartitionScan.KV_STATS_TABLE_NAME)) {
                            tableAPI.delete(pk, null, null);
                        }
                    } catch (DurabilityException |
                             RequestTimeoutException ignore) {
                        /* Get it on the next pass. */
                    }
                }
            } finally {
                itr.close();
            }
        }

        /**
         * Delete statistics belongs to deleted indices from statistics/lease
         * tables. An index is already deleted, all the statistics of the index
         * should be deleted. This method is to iterate all records of a
         * statistics or lease table, get table name and index name and then
         * use them to check whether the index is deleted or not. If the index
         * is deleted, then remove its statistics from statistics or lease
         * table.
         *
         * @param primaryKey is the primary key of the statistics/lease table.
         * @param tableNameField is to indicate which column is to store table
         * name in statistics tables.
         * @param indexNameField is to indicate which column is to store index
         * name in statistics tables.
         */
        private void deleteStatsByIndex(PrimaryKey primaryKey,
                                        String tableNameField,
                                        String indexNameField) {
            /*
             * Get all primary keys from statistics tables and check whether
             * the associated records should be deleted or not.
             */
            final TableIterator<PrimaryKey> itr =
                    tableAPI.tableKeysIterator(primaryKey, null, null);

            try {
                while (itr.hasNext() && !stop) {
                    try {
                        final PrimaryKey pk = itr.next();
                        final String storedTableName =
                                pk.get(tableNameField).asString().get();
                        final String storedIndexName =
                                pk.get(indexNameField).asString().get();
                        final TableImpl table =
                                tableListMap.get(storedTableName);
                        if (table != null) {
                            if (table.getIndex(storedIndexName) == null) {
                                tableAPI.delete(pk, null, null);
                            }
                        }
                    } catch (DurabilityException |
                             RequestTimeoutException ignore) {
                        /* Get it on the next pass. */
                    }
                }
            } finally {
                itr.close();
            }
        }

        /**
         * Scan partition database with the RN which KeyStatsCollector
         * resides and store the collected statistics information into
         * statistics tables.
         * @throws Exception is thrown when the statistics tables do not exist
         */
        private void scanPartitions() throws Exception {
            /*
             * Initialize the partition lease manager. If it is set up before,
             * checks if partition lease table exist.
             */
            if (partitionLeaseManager == null) {
                partitionLeaseManager = new PartitionLeaseManager(tableAPI);
            } else if (!partitionLeaseManager.leaseTableExists()) {
                logger.info("Partition lease table not found. " +
                            "Parition scan stops.");
                return;
            }

            final RepNode repNode = repNodeService.getRepNode();

            /* Fetch all partition databases with RN */
            final Set<PartitionId> partIdSet = repNode.getPartitions();
            if (partIdSet.isEmpty()) {
                return;
            }

            /* Get RN name and group id where the RN is */
            final String rnName = repNode.getRepNodeId().getFullName();
            final int groupId = repNode.getRepNodeId().getGroupId();

            /*
            * Start gather the statistics information for the selected
            * partition
             */
            for (PartitionId partId : partIdSet) {
                /* Check whether can scanning */
                if (stop) {
                    return;
                }

                /* Create LeaseInfo for scanning of selected partition */
                final PartitionLeaseInfo leaseInfo =
                        new PartitionLeaseInfo(partId.getPartitionId(),
                                               rnName,
                                               statsLeaseDuration);

                /* Create a lease manager to control the lease */
                statsScanHandler = new PartitionScan(tableAPI,
                                                     partId,
                                                     groupId,
                                                     repNode,
                                                     partitionLeaseManager,
                                                     leaseInfo,
                                                     statsGatherInterval,
                                                     logger);

                /*
                 * Start gather the statistics information for the selected
                 * partition
                 */
                startGathering();
            }
        }


        /**
         * Get all tables store in KVStore
         * @return the map storing all tables and their names
         */
        private Map<String, TableImpl> getAllTables() {
            final TableMetadata metadata =
                    (TableMetadata)repNodeService.getRepNode().
                    getMetadata(MetadataType.TABLE);
            if (metadata == null) {
                return null;
            }

            /* Use the iterateTables method to get all TableMetadata */
            final Map<String, TableImpl> map = new HashMap<>();
            final TableMetadataIteratorCallback callback =
                new TableMetadata.TableMetadataIteratorCallback() {

                @Override
                public boolean tableCallback(Table t) {
                    map.put(t.getFullName(), (TableImpl)t);
                    return true;
                }
            };

            metadata.iterateTables(callback);
            return map;
        }

        /**
         * Scan index secondary database and store the results into statistics
         * tables
         * @throws Exception
         */
        private void scanTableIndexes() throws Exception {
            /*
             * Initialize the index lease manager. If it is set up before,
             * checks if index lease table exist.
             */
            if (indexLeaseManager == null) {
                indexLeaseManager = new IndexLeaseManager(tableAPI);
            } else if (!indexLeaseManager.leaseTableExists()) {
                logger.info("Index lease table not found. Index scan stops.");
                return;
            }

            final RepNode repNode = repNodeService.getRepNode();

            /* Get RN name and group id where the RN is */
            final String rnName = repNode.getRepNodeId().getFullName();
            final int groupId = repNode.getRepNodeId().getGroupId();

            /* Get all tables include top tables and inner tables */
            tableListMap = getAllTables();
            if (tableListMap == null || (tableListMap.isEmpty())) {
                return;
            }

            /*
             * Get table/index pairs and try to scan the mapped secondary
             * database
             */
            for (final TableImpl table : tableListMap.values()) {

                for (final Map.Entry<String, Index> entry :
                    table.getIndexes().entrySet()) {

                    /* Check whether can scanning */
                    if (stop) {
                        return;
                    }

                    final String tableName = table.getFullName();
                    final String indexName = entry.getValue().getName();

                    /*
                     * Create LeaseInfo for scanning of selected index
                     * secondary database
                     */
                    final IndexLeaseInfo leaseInfo =
                            new IndexLeaseInfo(tableName, indexName, groupId,
                                               rnName, statsLeaseDuration);

                    statsScanHandler = new TableIndexScan(tableAPI,
                                                          tableName,
                                                          indexName,
                                                          groupId,
                                                          repNode,
                                                          indexLeaseManager,
                                                          leaseInfo,
                                                          statsGatherInterval,
                                                          logger);

                    /*
                     * Start gather the statistics information for the selected
                     * index secondary database
                     */
                    startGathering();
                }
            }
        }
    }
}
