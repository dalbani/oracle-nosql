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

package oracle.kv.impl.admin;

import java.util.logging.Logger;

import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import oracle.kv.impl.admin.Admin.Memo;
import oracle.kv.impl.admin.AdminDatabase.DB_TYPE;
import oracle.kv.impl.admin.AdminDatabase.LongKeyDatabase;
import oracle.kv.impl.admin.AdminStores.AdminStore;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.param.ParametersHolder;

public abstract class GeneralStore extends AdminStore {

    public static GeneralStore getReadOnlyInstance(Logger logger,
                                                   Environment env) {
        return new GeneralDatabaseStore(logger, env, true);
    }

    static GeneralStore getStoreByVersion(int schemaVersion,
                                          Admin admin, EntityStore eStore) {
        if (schemaVersion < AdminSchemaVersion.SCHEMA_VERSION_5) {
            assert eStore != null;
            return new GeneralDPLStore(admin.getLogger(), eStore);
        }
        return new GeneralDatabaseStore(admin.getLogger(), admin.getEnv(),
                                        false /* read only */);
    }

    /* For unit tests only */
    public static GeneralStore getReadTestInstance(Environment env) {
        return new GeneralDatabaseStore(Logger.getLogger(
                                                  GeneralStore.class.getName()),
                                        env, false);
    }

    protected GeneralStore(Logger logger) {
        super(logger);
    }

    public Memo getMemo() {
        return getMemo(null);
    }

    /**
     * Gets Parameters from the store.
     */
    public abstract Parameters getParameters(Transaction txn);

    /**
     * Stores the Parameters object in the store using the specified
     * transaction.
     *
     * Public for unit tests.
     */
    public abstract void putParameters(Transaction txn, Parameters p);

    abstract Memo getMemo(Transaction txn);

    abstract void putMemo(Transaction txn, Memo memo);

    private static class GeneralDatabaseStore extends GeneralStore {
        private final LongKeyDatabase<Parameters> parametersDb;
        private final LongKeyDatabase<Memo> memoDb;

        private GeneralDatabaseStore(Logger logger, Environment env,
                                     boolean readOnly) {
            super(logger);
            parametersDb = new LongKeyDatabase<>(DB_TYPE.PARAMETERS, logger,
                                                 env, readOnly);
            memoDb = new LongKeyDatabase<>(DB_TYPE.MEMO, logger, env, readOnly);
        }

        @Override
        public Parameters getParameters(Transaction txn) {
            return parametersDb.get(txn, LongKeyDatabase.ZERO_KEY,
                                    LockMode.READ_COMMITTED, Parameters.class);
        }

        @Override
        public void putParameters(Transaction txn, Parameters p) {
            parametersDb.put(txn, LongKeyDatabase.ZERO_KEY, p);
        }

        @Override
        Memo getMemo(Transaction txn) {
            return memoDb.get(txn, LongKeyDatabase.ZERO_KEY,
                              LockMode.READ_COMMITTED, Memo.class);
        }

        @Override
        void putMemo(Transaction txn, Memo memo) {
            memoDb.put(txn, LongKeyDatabase.ZERO_KEY, memo);
        }

        @Override
        public void close() {
            parametersDb.close();
            memoDb.close();
        }
    }

    private static class GeneralDPLStore extends GeneralStore {
        private final EntityStore eStore;

        private GeneralDPLStore(Logger logger, EntityStore eStore) {
            super(logger);
            this.eStore = eStore;
        }

        @Override
        public Parameters getParameters(Transaction txn) {
            final PrimaryIndex<String, ParametersHolder> ti =
                                 eStore.getPrimaryIndex(String.class,
                                                        ParametersHolder.class);
            final ParametersHolder holder =
                ti.get(txn, ParametersHolder.getKey(), LockMode.READ_COMMITTED);

            return (holder == null) ? null : holder.getParameters();
        }

        @Override
        public void putParameters(Transaction txn, Parameters p) {
            readOnly();
        }

        @Override
        Memo getMemo(Transaction txn) {
            final PrimaryIndex<String, Admin.Memo> mi =
                            eStore.getPrimaryIndex(String.class, Memo.class);
            return mi.get(txn, Memo.MEMO_KEY, LockMode.READ_COMMITTED);
        }

        @Override
        void putMemo(Transaction txn, Memo memo) {
            readOnly();
        }

        @Override
        protected void convertTo(int existingVersion, AdminStore newStore,
                                 Transaction txn) {
            final GeneralStore newGeneralStore = (GeneralStore)newStore;
            newGeneralStore.putParameters(txn, getParameters(txn));
            newGeneralStore.putMemo(txn, getMemo(txn));
        }
    }
}
