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

import oracle.kv.impl.admin.AdminDatabase.DB_TYPE;
import oracle.kv.impl.admin.AdminDatabase.LongKeyDatabase;
import oracle.kv.impl.admin.AdminStores.AdminStore;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.metadata.MetadataHolder;
import oracle.kv.impl.security.metadata.SecurityMetadata;

public abstract class SecurityStore extends AdminStore {

    public static SecurityStore getReadOnlyInstance(Logger logger,
                                                    Environment env) {
        return new SecurityDatabaseStore(logger, env, true);
    }

    static SecurityStore getStoreByVersion(int schemaVersion,
                                           Admin admin, EntityStore eStore) {
        if (schemaVersion < AdminSchemaVersion.SCHEMA_VERSION_5) {
            assert eStore != null;
            return new SecurityDPLStore(admin.getLogger(), eStore);
        }
        return new SecurityDatabaseStore(admin.getLogger(), admin.getEnv(),
                                         false /* read only */);
    }

    /* For unit test only */
    public static SecurityStore getTestStore(Environment env) {
        return new SecurityDatabaseStore(
                                Logger.getLogger(SecurityStore.class.getName()),
                                env, false);
    }

    /**
     * Gets the SecurityMetadata object using the specified transaction.
     */
    public abstract SecurityMetadata getSecurityMetadata(Transaction txn);

    /**
     * Persists the specified metadata object with the specified transaction.
     */
    public abstract void putSecurityMetadata(Transaction txn,
                                             SecurityMetadata md);

    protected SecurityStore(Logger logger) {
        super(logger);
    }

    private static class SecurityDatabaseStore extends SecurityStore {
        private final LongKeyDatabase<SecurityMetadata> metadataDb;

         SecurityDatabaseStore(Logger logger, Environment env,
                               boolean readOnly) {
            super(logger);
            metadataDb = new LongKeyDatabase<>(DB_TYPE.SECURITY, logger,
                                               env, readOnly);
        }

        @Override
        public SecurityMetadata getSecurityMetadata(Transaction txn) {
            return metadataDb.get(txn, LongKeyDatabase.ZERO_KEY,
                                  LockMode.READ_COMMITTED,
                                  SecurityMetadata.class);
        }

        @Override
        public void putSecurityMetadata(Transaction txn, SecurityMetadata md) {
            metadataDb.put(txn, LongKeyDatabase.ZERO_KEY, md);
        }

        @Override
        public void close() {
            metadataDb.close();
        }
    }

    private static class SecurityDPLStore extends SecurityStore {
         private final EntityStore eStore;

         SecurityDPLStore(Logger logger, EntityStore eStore) {
            super(logger);
            this.eStore = eStore;
        }

        @Override
        public SecurityMetadata getSecurityMetadata(Transaction txn) {
            final PrimaryIndex<String, MetadataHolder> pi =
                    eStore.getPrimaryIndex(String.class, MetadataHolder.class);
            final MetadataHolder holder = pi.get(txn,
                                             MetadataType.SECURITY.getKey(),
                                             LockMode.READ_UNCOMMITTED);

            return SecurityMetadata.class.cast((holder == null) ? null :
                                                          holder.getMetadata());
        }

        @Override
        public void putSecurityMetadata(Transaction txn, SecurityMetadata md) {
            readOnly();
        }

        @Override
        protected void convertTo(int existingVersion, AdminStore newStore,
                                 Transaction txn) {
            final SecurityStore newSecurityStore = (SecurityStore)newStore;
            newSecurityStore.putSecurityMetadata(txn, getSecurityMetadata(txn));
        }
    }
}
