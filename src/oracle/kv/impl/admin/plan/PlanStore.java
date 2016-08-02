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

package oracle.kv.impl.admin.plan;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.AdminPlanDatabase;
import oracle.kv.impl.admin.AdminSchemaVersion;
import oracle.kv.impl.admin.NonfatalAssertionException;
import oracle.kv.impl.fault.DatabaseNotReadyException;
import oracle.kv.impl.util.SerializationUtil;
import oracle.kv.impl.util.TxnUtil;

import com.sleepycat.bind.EntityBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/**
 * A common base class for implementations of the plan store in Admin.
 */
public abstract class PlanStore {

    final Admin admin;
    final Logger logger;

    /*
     * We use non-sticky cursors here to obtain a slight performance advantage
     * and to run in a deadlock-free mode.
     */
    static final CursorConfig CURSOR_READ_COMMITTED =
        new CursorConfig().setNonSticky(true).setReadCommitted(true);

    /**
     * Creates a PlanStore instance according to the schema version.  If the
     * schema version is earlier than V4, a DPL-based plan store will be
     * returned for compatibility. Otherwise, a JE HA database-based plan
     * store will be returned.
     *
     * @param admin admin instance
     * @param schemaVersion schema version
     */
    public static PlanStore getStoreByVersion(Admin admin, int schemaVersion) {
        if (schemaVersion >= AdminSchemaVersion.SCHEMA_VERSION_4) {
            return new PlanDatabaseStore(admin);
        }
        return new DPLPlanStore(admin);
    }

    private PlanStore(Admin admin, Logger logger) {
        this.admin = admin;
        this.logger = logger;
    }

    void logPersisting(AbstractPlan plan) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, getName() + ": stored plan of {0}", plan);
        }
    }

    void logFetching(int planId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,
                       getName() + ": fetching plan using id {0}", planId);
        }
    }

    /**
     * Persists an abstract plan into the store. Callers are responsible for
     * exception handling.
     */
    public abstract void persist(Transaction txn, AbstractPlan plan);

    /**
     * Fetches an abstract plan from the store. Callers are responsible for
     * exception handling.
     */
    public abstract AbstractPlan fetch(Transaction txn, int planId);

    /**
     * Returns an cursor for iterating all plans in the store.  Callers are
     * responsible for exception handling, and should close the cursor via
     * {@link PlanCursor#close} after use.
     */
    public abstract PlanCursor getPlanCursor(Transaction txn,
                                             Integer startPlanId);

    /**
     * Returns the name of the store.
     */
    public abstract String getName();

    /**
     * A provisional plan store used when not all the nodes in the store have
     * been upgraded to R3.1.0 or later.  The underlying storage is the
     * EntityStore.
     */
    private static class DPLPlanStore extends PlanStore {

        private DPLPlanStore(Admin admin) {
            super(admin, admin.getLogger());
        }

        @Override
        public void persist(Transaction txn, AbstractPlan plan) {
            final EntityStore eStore = admin.getEStore();
            checkStoreValid(eStore);

            final PrimaryIndex<Integer, AbstractPlan> pi =
                eStore.getPrimaryIndex(Integer.class, AbstractPlan.class);
            pi.put(txn, plan);
            logPersisting(plan);
        }

        @Override
        public AbstractPlan fetch(Transaction txn, int planId) {
            final EntityStore eStore = admin.getEStore();
            checkStoreValid(eStore);

            logFetching(planId);
            final PrimaryIndex<Integer, AbstractPlan> pi =
                eStore.getPrimaryIndex(Integer.class, AbstractPlan.class);
            return pi.get(txn, new Integer(planId), LockMode.READ_COMMITTED);
        }

        @Override
        public PlanCursor getPlanCursor(Transaction txn, Integer startPlanId) {
            final EntityStore eStore = admin.getEStore();
            checkStoreValid(eStore);

            final PrimaryIndex<Integer, AbstractPlan> pi =
                eStore.getPrimaryIndex(Integer.class, AbstractPlan.class);

            final Cursor cursor =
                pi.getDatabase().openCursor(txn, CURSOR_READ_COMMITTED);

            return new PlanCursor(cursor, LockMode.DEFAULT, startPlanId) {

                /*
                 * We need to use EntityBinding to convert a database entry
                 * to a plan object in DPL store.
                 */
                private final EntityBinding<AbstractPlan> planBinding =
                    eStore.getPrimaryIndex(Integer.class, AbstractPlan.class).
                        getEntityBinding();

                @Override
                AbstractPlan entryToPlan(DatabaseEntry value) {
                    return planBinding.entryToObject(key, data);
                }
            };
        }

        @Override
        public String getName() {
            return "DPLPlanStore";
        }

        private void checkStoreValid(EntityStore estore) {
            if (estore == null) {
                /* admin shutting down */
                if (admin.isClosing()) {
                    throw new NonfatalAssertionException(
                        "Admin entity store is closed in shutting down");
                }
                /* May be renewing repEnv, retry */
                throw new DatabaseNotReadyException(
                    "Admin entity store is not ready");
            }
        }
    }

    /**
     * A plan store using the non-DPL
     * {@link oracle.kv.impl.admin.AdminPlanDatabase} as the underlying storage.
     */
    private static class PlanDatabaseStore extends PlanStore {

        public PlanDatabaseStore(Admin admin) {
            super(admin, admin.getLogger());

            /* Admin plan db may have not been initialized */
            admin.initAdminPlanDb();
        }

        @Override
        public void persist(Transaction txn, AbstractPlan plan) {
            final AdminPlanDatabase planDb = admin.getAdminPlanDb();
            checkPlanDbValid(planDb);

            planDb.persistEntity(txn, plan);
            logPersisting(plan);
        }

        @Override
        public AbstractPlan fetch(Transaction txn, int planId) {
            final AdminPlanDatabase planDb = admin.getAdminPlanDb();
            checkPlanDbValid(planDb);

            logFetching(planId);
            return planDb.fetchEntity(txn, planId, LockMode.READ_COMMITTED);
        }

        @Override
        public PlanCursor getPlanCursor(Transaction txn, Integer startPlanId) {
            final AdminPlanDatabase planDb = admin.getAdminPlanDb();
            checkPlanDbValid(planDb);

            final Cursor cursor = planDb.openEntityDb().openCursor(
                txn, CURSOR_READ_COMMITTED);

            return new PlanCursor(cursor, LockMode.DEFAULT, startPlanId) {

                @Override
                AbstractPlan entryToPlan(DatabaseEntry value) {
                    return SerializationUtil.getObject(value.getData(),
                                                       AbstractPlan.class);
                }
            };
        }

        @Override
        public String getName() {
            return "PlanDatabaseStore";
        }

        private void checkPlanDbValid(AdminPlanDatabase planDb) {
            if (planDb == null || planDb.isClosing()) {
                /* admin shutting down */
                if (admin.isClosing()) {
                    throw new NonfatalAssertionException(
                        "Admin plan database is closed in shutting down");
                }
                /* May be renewing repEnv, retry */
                throw new DatabaseNotReadyException(
                    "Admin plan database is not ready");
            }
        }
    }

    /**
     * A simple wrap class of Cursor to facilitate the scan of the plan store.
     * Only a small subset of cursor methods are wrapped and provided.
     */
    public abstract static class PlanCursor {

        private final Cursor cursor;
        private final LockMode lockMode;
        private final Integer startKey;

        final DatabaseEntry key = new DatabaseEntry();
        final DatabaseEntry data = new DatabaseEntry();

        /**
         * Ctor
         *
         * @param cursor cursor to be wrapped
         * @param lockMode lockMode
         * @param startKey expected starting key in cursor range
         */
        public PlanCursor(Cursor cursor, LockMode lockMode, Integer startKey) {
            this.cursor = cursor;
            this.lockMode = lockMode;
            this.startKey = startKey;
        }

        /**
         * Moves the cursor to the next key/plan pair and returns that plan.  
         * Null will be return if the last key/plan pair is reached.
         */
        public AbstractPlan next() {
            final OperationStatus status =
                cursor.getNext(key, data, lockMode);
            if (status == OperationStatus.SUCCESS) {
                return entryToPlan(data);
            }
            return null;
        }

        /**
         * Moves the cursor to the previous key/plan and returns that plan.
         * Null will be return if the first key/plan pair is reached.
         */
        public AbstractPlan prev() {
            final OperationStatus status =
                cursor.getPrev(key, data, lockMode);
            if (status == OperationStatus.SUCCESS) {
                return entryToPlan(data);
            }
            return null;
        }

        /**
         * Returns first plan object in the store.  Null will be returned if no
         * data exists.
         * <p>
         * If the {@code startKey} is specified in constructor, this method
         * will return the plan with the {@code startKey} if found, otherwise
         * the plan with smallest key greater than or equal to {@code startKey}
         * will be returned. If such key is not found, null will be returned.
         * 
         */
        public AbstractPlan first() {
            /*
             * Try to move cursor to the start key if the specified.
             */
            if (startKey != null) {
                IntegerBinding.intToEntry(startKey, key);
                final OperationStatus status =
                    cursor.getSearchKeyRange(key, data, lockMode);
                if (status == OperationStatus.SUCCESS) {
                    return entryToPlan(data);
                }
                return null;
            }

            if (cursor.getFirst(key, data, lockMode) ==
                    OperationStatus.SUCCESS) {
                return entryToPlan(data);
            }
            return null;
        }

        /**
         * Moves the cursor to the last key/plan pair of the database, and
         * returns that plan. Null will be returned is no data exists.
         */
        public AbstractPlan last() {
            if (cursor.getLast(key, data, lockMode) ==
                    OperationStatus.SUCCESS) {
                return entryToPlan(data);
            }
            return null;
        }

        public void close() {
            TxnUtil.close(cursor);
        }

        /**
         * Converts a database entry to a AbstractPlan object.
         */
        abstract AbstractPlan entryToPlan(DatabaseEntry value);
    }
}
