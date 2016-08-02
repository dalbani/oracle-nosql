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

package oracle.kv.impl.admin;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.kv.KVVersion;
import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.admin.plan.DeploymentInfo;
import oracle.kv.impl.admin.plan.PlanStore;
import oracle.kv.impl.admin.plan.PlanStore.PlanCursor;
import oracle.kv.impl.admin.topo.RealizedTopology;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.topo.TopologyServerUtil;
import oracle.kv.impl.util.TxnUtil;
import oracle.kv.impl.util.VersionUtil;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Durability;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.rep.MasterTransferFailureException;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.je.rep.util.ReplicationGroupAdmin;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/**
 * The Admin service stores an integer which represents the version of the
 * schema of the Admin database. It's intentionally held within a non-DPL
 * database separate from the database that holds plans, params and the memo,
 * so that it can safely be used to check the version of that DPL store.
 *
 * Admin services can only be run on databases which have equal or older
 * schemas. They cannot run on newer schemas. To read older schemas, newer
 * software may not need to do anything at all (for example, new plans were
 * added) or may have to do some conversion if there have been semantic
 * changes. DPL changes may also require writing explicit converters, as
 * required by DPL rules.
 */
public class AdminSchemaVersion {

    /*
     * The first version of NoSQL DB did not have this mechanism. It is
     * implicitly schema version 1.
     *
     * changes in schema version 2:
     *  - add removeSNPlan
     * changes in schema version 3:
     *  - add repfactor field to Datacenter Component
     *  - add new plans and tasks: DeployTopoPlan, DeployShard,
     *    DeployNewRN, MigratePartition
     *  - store topologies in their own store, keyed by name.
     *  - TaskRun stores task
     * changes in schema version 4:
     *  - move all plan data from entity store to non-DPL database
     */
    private static final int SCHEMA_VERSION_3 = 3;
    public static final int SCHEMA_VERSION_4 = 4;
    public static final int CURRENT_SCHEMA = SCHEMA_VERSION_4;

    /*
     * There are two records in the version db. These two initial record types
     * cannot ever be deleted. If we need more metadata in the future, we can
     * add records, but would have to use the schema version to know to read
     * them.
     *   key="schemaVersion", data=<number>
     *   key="softwareVersion", data=<kvstore version>
     * The software version is used mainly to help construct an informative
     * error message.
     */
    private static final String DB_NAME = "AdminSchemaVersion";
    private static final String SCHEMA_VERSION_KEY = "schemaVersion";
    private static final String SOFTWARE_VERSION_KEY = "softwareVersion";

    /*
     * If no schema DB is present, or there is no version in the DB, we
     * assume R1 (1.2.123).
     */
    private static final KVVersion FIRST_KV_VERSION = KVVersion.R1_2_123;

    private final Admin admin;
    private final ReplicatedEnvironment repEnv;
    private final Logger logger;

    public AdminSchemaVersion(Admin admin, Logger logger) {
        this.logger = logger;
        this.admin = admin;
        this.repEnv = admin.getEnv();
    }

    /**
     * Throws IllegalStateException if the on-disk schema version is newer than
     * the version supported by this software package. Update the schema
     * version if the version is older.
     */
    void checkAndUpdateVersion(Transaction txn) {
        checkAndUpdateVersion(txn, CURRENT_SCHEMA, true);
    }

    /**
     * Throws IllegalStateException if the on-disk schema version is newer than
     * the version supported by this software package.
     */
    void checkVersion(Transaction txn) {
        checkAndUpdateVersion(txn, CURRENT_SCHEMA, false);
    }

    /**
     * This flavor of checkAndUpdate is available for unit testing.
     */
    void checkAndUpdateVersion(Transaction txn,
                               int useSchemaVersion,
                               boolean doUpdate) {
        /*
         * First check to see if the environment is empty.  This is how we know
         * whether we are starting from scratch or upgrading an existing
         * environment.
         */
        final List<String> dbNames = repEnv.getDatabaseNames();
        if (dbNames.isEmpty()) {
            if (doUpdate) {
                init(txn, useSchemaVersion);
            }
            return;
        }

        /*
         * If the environment contains some databases, then it might or might
         * not contain a database named AdminSchemaVersion.  If it does not,
         * then we are dealing with a version 1 environment.  Otherwise, we'll
         * read the version number from the AdminSchemaVersion database.
         */
        int existingVersion;
        KVVersion existingKVVersion;

        if (!dbNames.contains(DB_NAME)) {
            existingVersion = 1;
            existingKVVersion = FIRST_KV_VERSION;
        } else {
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(false);
            dbConfig.setTransactional(true);
            dbConfig.setReadOnly(true);
            Database versionDb = null;
            try {
                versionDb = repEnv.openDatabase(null, DB_NAME, dbConfig);
                existingVersion = readSchemaVersion(txn, versionDb);
                existingKVVersion = readSoftwareVersion(txn, versionDb);

                if (existingVersion > useSchemaVersion) {

                    throw new IllegalStateException
                        ("This Admin Service software is at " +
                         KVVersion.CURRENT_VERSION.getNumericVersionString() +
                         ", schema version " + useSchemaVersion +
                         " but the stored schema is at version " +
                         existingVersion + "/" +
                         existingKVVersion.getNumericVersionString() +
                         ". Please upgrade this node's NoSQL Database" +
                         " software version to "
                         + existingKVVersion.getNumericVersionString() +
                         " or higher.");
                }

            } finally {
                if (versionDb != null) {
                    versionDb.close();
                }
            }
        }

        /* If the version has not changed, nothing else to do */
        if (existingKVVersion.equals(KVVersion.CURRENT_VERSION)) {
            assert existingVersion == CURRENT_SCHEMA;
            return;
        }

        /*
         * This is an upgrade (or downgrade) situation so make sure it is
         * legal. (The SNA should prevent improper upgrades)
         */
        VersionUtil.checkUpgrade(existingKVVersion);

        /* If the software is newer, we attempt to update the DB */
        if (existingKVVersion.compareTo(KVVersion.CURRENT_VERSION) < 0) {

            /*
             * If we are the master we update the DB with the new version
             * information. If we are not the master, we attempt to force this
             * node to become the master, eventually causing the update via
             * Admin.Listener.stateChange().
             */
            if (doUpdate) {

                update(existingVersion, existingKVVersion.toString(),
                       txn, useSchemaVersion);
            } else {

                /*
                 * A replica. If the admin master has already been upgraded to
                 * current software version, and we are upgrading schema from
                 * V3 to V4 now but not all admins are ready, we do not
                 * initiate master transfer. Otherwise an infinite master
                 * transfer loop will happen among upgraded admins, because
                 * currently masters are unable to upgrade the schema but all
                 * upgraded admins keep trying to become master to do this.
                 */
                if (masterHasCurrentVersion()) {
                    if (existingVersion < SCHEMA_VERSION_4 &&
                        existingVersion == SCHEMA_VERSION_3) {

                        if (!readyForV4Upgrade()) {
                            return;
                        }
                    }

                    /*
                     * If the admin master has current version but not all
                     * admins are in a ready to update state, don't do the
                     * master transfer.
                     */
                    if (!readyForSoftwareVersionUpdate()) {
                        return;
                    }
                }

                try {
                    final ReplicationGroupAdmin repGroupAdmin =
                        new ReplicationGroupAdmin(
                            repEnv.getGroup().getName(),
                            repEnv.getRepConfig().getHelperSockets(),
                            admin.getRepNetConfig());
                    repGroupAdmin.
                            transferMaster(Collections.
                                                singleton(repEnv.getNodeName()),
                                           1, TimeUnit.MINUTES,
                                           false /* Don't preempt an ongoing
                                                    MT operation. */);
                    /*
                     * Success. There will eventually be a master transition
                     * at this node.
                     */
                    logger.log(Level.INFO,
                               "Master transfer initiated due to upgrade " +
                               "to {0}",
                               KVVersion.CURRENT_VERSION.
                                                    getNumericVersionString());

                } catch (MasterTransferFailureException mtfe) {
                    /*
                     * This could be because some other replica beat us to it.
                     * Failing to transfer is not fatal.
                     */
                    logger.log(Level.INFO,
                               "Attempt to transfer master to this node " +
                               "failed: {0}",
                               mtfe.getMessage());
                } catch (Exception ex) {
                    /* Failing to transfter is not fatal. */
                    logger.log(Level.INFO,
                               "Attempt to transfer master to this node " +
                               "failed", ex);
                }
            }
        }
    }

    /**
     * Update the version database, creating it if necessary.
     */
    private void update(int existingVersion,
                        String existingKVVersion,
                        Transaction txn,
                        int useSchemaVersion) {

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        Database versionDb = repEnv.openDatabase(txn, DB_NAME, dbConfig);
        updateSchemaVersion(existingVersion, existingKVVersion,
                            txn, versionDb, useSchemaVersion);
        versionDb.close();
    }

    /**
     * Create the version database. Should only be called the first time the
     * admin database is created, at bootstrap; will fail if the version
     * database already exists.
     */
    private void init(Transaction txn, int useSchemaVersion) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setExclusiveCreate(true);
        dbConfig.setTransactional(true);
        Database versionDb = repEnv.openDatabase(txn, DB_NAME, dbConfig);
        updateSchemaVersion(0, null, txn, versionDb, useSchemaVersion);
        versionDb.close();
    }

    /**
     * Write the current schema and software version in the version db.
     */
    private void updateSchemaVersion(int existingVersion,
                                     String existingKVVersion,
                                     Transaction txn,
                                     Database versionDb,
                                     int useSchemaVersion) {

        if (existingVersion == 0) {
            logger.info("Initializing Admin Schema to schema version " +
                        useSchemaVersion + "/NoSQL DB " +
                        KVVersion.CURRENT_VERSION);
        } else {

            if (existingVersion < SCHEMA_VERSION_4) {
                logger.info(
                        "Updating Admin Schema version from schema version " +
                        existingVersion + "/NoSQL DB " + existingKVVersion +
                        " to schema version " + useSchemaVersion +
                        "/NoSQL DB " + KVVersion.CURRENT_VERSION);

                if (!upgradeToV4(existingVersion, versionDb, txn)) {
                    return;
                }
            }
        }

        updateSchemaVersion(SCHEMA_VERSION_4, versionDb, txn);

        /*
         * Only check version upgrade for update case, not initialization case.
         */
        if (existingVersion != 0 && !readyForSoftwareVersionUpdate()) {
            return;
        }

        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();

        StringBinding.stringToEntry(SOFTWARE_VERSION_KEY, key);
        StringBinding.stringToEntry(KVVersion.CURRENT_VERSION.toString(), value);
        versionDb.put(txn, key, value);
    }

    private int readSchemaVersion(Transaction txn, Database versionDb) {

        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();

        StringBinding.stringToEntry(SCHEMA_VERSION_KEY, key);
        OperationStatus status =
                versionDb.get(txn, key, value, LockMode.DEFAULT);
        if (status == OperationStatus.SUCCESS) {
            return IntegerBinding.entryToInt(value);
        }

        /*
         * If no version record exists, this was schema version 1, which did
         * not have this mechanism.
         */
        return 1;
    }

    private KVVersion readSoftwareVersion(Transaction txn, Database versionDb) {

        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();

        StringBinding.stringToEntry(SOFTWARE_VERSION_KEY, key);
        OperationStatus status =
                versionDb.get(txn, key, value, LockMode.DEFAULT);
        if (status == OperationStatus.SUCCESS) {
            return KVVersion.parseVersion(StringBinding.entryToString(value));
        }

        /* If no version record exists, this was version 1.2.124 */
        return FIRST_KV_VERSION;
    }

    /**
     * Opens and reads the schema version
     */
    public int openAndReadSchemaVersion() {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(false);
        dbConfig.setTransactional(true);
        dbConfig.setReadOnly(true);
        Database versionDb = null;
        Transaction txn = null;
        int version = 0;
        try {
            txn = repEnv.beginTransaction
                (null,
                 new TransactionConfig().setDurability(Durability.COMMIT_SYNC));
            versionDb = repEnv.openDatabase(txn, DB_NAME, dbConfig);
            version = readSchemaVersion(txn, versionDb);
            txn.commit();
        } finally {
            if (versionDb != null) {
                versionDb.close();
            }
            TxnUtil.abort(txn);
        }
        return version;
    }

    /**
     * Do the actual work to upgrade the admin db.
     */
    private void upgradeToV3(int existingVersion,
                             Database versionDb,
                             Transaction txn) {

        if (existingVersion >= SCHEMA_VERSION_3) {
            return;
        }
        /* At this time the entity store has not been set up yet. */
        EntityStore eStore = admin.initEstore();
        final PlanStore planStore =
            PlanStore.getStoreByVersion(admin, SCHEMA_VERSION_3);

        upgradeAdminTopologyToV3(eStore, txn);

        upgradeAllPlansToV3(planStore, txn);

        updateSchemaVersion(SCHEMA_VERSION_3, versionDb, txn);

        /* TODO: Set capacity? */
    }

    private boolean upgradeToV4(int existingVersion,
                                Database versionDb,
                                Transaction txn) {
        /* If needed, upgrade to V3 first */
        upgradeToV3(existingVersion, versionDb, txn);

        if (!readyForV4Upgrade()) {
            return false;
        }

        /*
         * At this time the entity store and plan database may not have been
         * set up yet.
         */
        EntityStore eStore = admin.initEstore();
        AdminPlanDatabase planDb = admin.initAdminPlanDb();
        upgradeAllPlansToV4(eStore, planDb, txn);
        return true;
    }

    private boolean readyForV4Upgrade() {
        /* Initialize admin estore so that we can check admin group version */
        admin.initEstore();
        try {
            if (!admin.checkAdminGroupVersion(KVVersion.R3_1)) {
                logger.info("Unable to upgrade to Admin DB version " +
                            SCHEMA_VERSION_4 + " while not all Admins in the" +
                            " store have version of " +
                            KVVersion.R3_1.getNumericVersionString() +
                            " or later.");
                return false;
            }
        } catch (AdminFaultException afe) {
            logger.info("Unable to confirm the lowest supported version of " +
                        "all Admins. Skipped upgrading Admin DB version to " +
                        SCHEMA_VERSION_4);
            return false;
        }
        return true;
    }

    /**
     * Since the current KVVersion could be different for different nodes in
     * a distributed system. We need to rely on query of node status to
     * determine whether all the admins have completed upgrade process. If the
     * admin group version reach the highest version that ever seen, we
     * consider this is a ready to update state for the admin master, otherwise
     * not.
     */
    private boolean readyForSoftwareVersionUpdate() {
        admin.initEstore();
        try {
            final KVVersion versionLowest = admin.getAdminGroupVersion();
            final KVVersion versionHighest = admin.getAdminHighestVersion();
            return VersionUtil.compareMinorVersion(versionLowest,
                versionHighest) == 0;
        } catch (AdminFaultException afe) {
            logger.info("Unable to confirm the versions of all Admins, not " +
                        "ready for version DB update now.");
            return false;
        }
    }

    /**
     * Checks whether the admin master has been upgraded to current version. If
     * not, the current admin will become master to support possible new
     * commands via transfer.
     */
    private boolean masterHasCurrentVersion() {
        admin.initEstore();
        try {
            if (!admin.checkAdminMasterVersion(KVVersion.CURRENT_VERSION)) {
                logger.info(
                    "Admin master has not upgraded to current version of " +
                    KVVersion.CURRENT_VERSION.getNumericVersionString() +
                    ". Try to become master to support upgrade operations.");
                return false;
            }
        } catch (AdminFaultException afe) {
            /*
             * Returns true if could not determine, so as not to initiate
             * master transfer to avoid the possible infinite MT loop issue
             */
            logger.info(
                "Unable to confirm the version of current admin master.");
        }
        return true;
    }

    /**
     * Plans have changes in V3 that need attention during upgrade.
     * @param existingVersion
     */
    private void upgradeAllPlansToV3(PlanStore planStore, Transaction txn) {

        logger.info("Upgrading all plans to Admin DB version 3.");

        /*
         * Usually the plan mutex must be taken before saving the plan
         * to the database, and before acquiring any JE locks, per the
         * synchronization hierarchy described in AbstractPlan. In this case,
         * we assume that the upgrade method will be called while no plans
         * are executing.
         */
        final PlanCursor cursor =
            planStore.getPlanCursor(txn, null /* startPlanId */);

        try {
            for (AbstractPlan p = cursor.first();
                 p != null;
                 p = cursor.next()) {

                p.upgradeToV3();
                p.persist(planStore, txn);
            }
        } finally {
            cursor.close();
        }
    }

    private void upgradeAdminTopologyToV3(EntityStore eStore, Transaction txn) {

        Topology oldTopo = TopologyServerUtil.fetchCommitted(eStore, txn);

        logger.info("Upgrading current topology to Admin DB version 3.");

        if (oldTopo.getVersion() != 0) {

            /*
             * Something is wrong.  We are trying to upgrade the Admin database
             * to V3 from an earier version.  We know earlier versions of the
             * database contain only Topologies with Topology version 0.
             */
             throw new IllegalStateException
                 ("Unexpected Topology version " + oldTopo.getVersion() +
                  " found in legacy Admin database.");
        }

        if (!oldTopo.upgrade()) {

            /*
             * If Topology.upgrade returns false, the required upgrade failed.
             * In all likelihood, the problem is that the replicas are not
             * evenly distributed among multiple data centers, which prevents
             * Topology.upgrade from choosing a repfactor for the
             * datacenters.
             *
             * When this happens, the topology version number is not set to the
             * current version, and future attempts to rebalance or to perform
             * elasticity operations will fail because the datacenters will
             * have a repFactor of zero.  The user has no choice but to dump
             * and restore.
             */
            logger.severe
                ("Unable to upgrade the Topology from its earlier version. " +
                 "The store will continue to run with this Topology, " +
                 "But certain features that are new in R2, such " +
                 "as dynamic master rebalancing, and elasticity operations, " +
                 "will not be available. " +
                 "To enable these features will require dumping the " +
                 "database contents and restoring them to an R2 topology, " +
                 "using the snapshot and load commands.  Please see the " +
                 "NoSQL Database Administrator's Guide, chap. 7, " +
                 "for more information");
         }

        DeploymentInfo info = DeploymentInfo.makeStartupDeploymentInfo();
        RealizedTopology rt = new RealizedTopology(oldTopo, info);
        rt.setStartTime();

        admin.getTopoStore().save(txn, rt);

        /* Should the old topology be removed?  It's benign to leave it. */
    }

    /**
     * Transfers all plans stored in DPL EntityStore to non-DPL JE database.
     */
    private void upgradeAllPlansToV4(EntityStore eStore,
                                     AdminPlanDatabase planDb,
                                     Transaction txn) {

        logger.info("Upgrading all plans to store in non-DPL JE database.");

        PrimaryIndex<Integer, AbstractPlan> pi =
            eStore.getPrimaryIndex(Integer.class, AbstractPlan.class);

        EntityCursor<AbstractPlan> cursor = pi.entities(txn, null);
        try {
            for (AbstractPlan p = cursor.first();
                 p != null;
                 p = cursor.next()) {

                planDb.persistEntity(txn, p);

                /*
                 * Old plan data has been stored in new non-DPL store, remove
                 * them from the old DPL entity store.
                 */
                pi.delete(txn, p.getId());
            }
        } finally {
            cursor.close();
        }
    }

    /*
     * Updates the schema version to a new value in db.
     */
    private void updateSchemaVersion(int newVersion,
                                     Database versionDb,
                                     Transaction txn) {
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();

        StringBinding.stringToEntry(SCHEMA_VERSION_KEY, key);
        IntegerBinding.intToEntry(newVersion, value);
        versionDb.put(txn, key, value);
    }
}
