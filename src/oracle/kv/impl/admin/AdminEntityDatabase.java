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

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.kv.impl.admin.AdminEntity.EntityType;
import oracle.kv.impl.fault.DatabaseNotReadyException;
import oracle.kv.impl.util.DatabaseUtils;
import oracle.kv.impl.util.SerializationUtil;
import oracle.kv.impl.util.TxnUtil;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.rep.NoConsistencyRequiredPolicy;
import com.sleepycat.je.rep.ReplicatedEnvironment;

/**
 * A non-DPL JE database for all admin entities.
 * TODO: See the AdminDBCatalog in the unadvertised utility, 
 * oracle.kv.util.internal.AdminDump for a list of the entities stored in
 * the admin database now, which should be migrated to this class.
 *
 * @param <K> type of indexing key of an admin entity
 * @param <T> type of admin entity
 */
public abstract class AdminEntityDatabase <K, T extends AdminEntity<K>> {

    /* Delay between DB open attempts */
    private static final int DB_OPEN_RETRY_MS = 1000;

    /* Max DB open attempts */
    private static final int DB_OPEN_RETRY_MAX = 20;

    private Database entityDb;
    private final Admin admin;
    private final Logger logger;
    private volatile boolean closing = false;

    protected AdminEntityDatabase(Admin admin, Logger logger) {
        if (admin == null) {
            throw new IllegalArgumentException("admin cannot be null");
        }
        this.admin = admin;
        this.logger = logger;
    }

    /**
     * Opens and returns the underlying entity database.
     */
    public synchronized Database openEntityDb() {
        if (entityDb == null) {
            openEntityDb(admin.getEnv());
        }
        return entityDb;
    }

    private void openEntityDb(final ReplicatedEnvironment repEnv) {
        assert(Thread.holdsLock(this));

        if (repEnv == null) {
            return;
        }

        int retries = 0;
        Exception lastCause = null;
        while ((entityDb == null) && !closing && repEnv.isValid()) {

            final DatabaseConfig dbConfig =
                 new DatabaseConfig().setAllowCreate(true).
                                      setTransactional(true);

            try {
                entityDb = openDb(repEnv, dbConfig);
                assert entityDb != null;
                logger.log(Level.INFO, "Open Admin entity DB: {0}",
                           getDBName());
                return;
            } catch (RuntimeException re) {
                if (!DatabaseUtils.handleException(re, logger, getDBName())){
                    return;
                }
                lastCause = re;
            }

            if (retries >= DB_OPEN_RETRY_MAX) {
                throw new IllegalStateException(
                    String.format(
                        "Failed to open entity DB of %s after %d retries: %s",
                        getDBName(), retries, lastCause),
                    lastCause);
            }

            logger.log(Level.INFO,
                       "Retry opening Admin entity DB: {0} because {1}",
                       new Object[] { getDBName(), lastCause});

            retries++;

            /* Wait to retry */
            try {
                Thread.sleep(DB_OPEN_RETRY_MS);
            } catch (InterruptedException ie) {
                /* Should not happen. */
                throw new IllegalStateException(ie);
            }
        }
    }

    private Database openDb(Environment env, DatabaseConfig dbConfig) {

        final TransactionConfig txnConfig = new TransactionConfig().
            setConsistencyPolicy(NoConsistencyRequiredPolicy.NO_CONSISTENCY);

        Transaction txn = null;
        Database db = null;
        try {
            txn = env.beginTransaction(null, txnConfig);
            db = env.openDatabase(txn, getDBName(), dbConfig);
            txn.commit();
            txn = null;
            final Database ret = db;
            db = null;
            return ret;
        } finally {
            TxnUtil.abort(txn);

            if (db != null) {
                try {
                    db.close();
                } catch (DatabaseException de) {
                    /* Ignore */
                }
            }
        }
    }

    /**
     * Closes the underlying entity database.
     */
    public synchronized void closeEntityDb() {
        closing = true;
        if (entityDb == null) {
            return;
        }
        logger.log(Level.INFO, "Closing admin entity DB: {0}", getDBName());
        TxnUtil.close(logger, entityDb, getDBName());
        entityDb = null;
    }

    /**
     * Persists an entity into the database.  Callers are responsible for
     * exception handling. 
     * @param txn transaction
     * @param entity entity to be persisted
     * @throw {@link DatabaseNotReadyException} if the database handle is not
     * yet opened
     */
    public void persistEntity(final Transaction txn, final T entity) {
        checkIfEntityDbValid();

        final K indexKey = ((AdminEntity<K>) entity).getIndexKey();
        final DatabaseEntry key = keyToEntry(indexKey);
        final DatabaseEntry data =
            new DatabaseEntry(SerializationUtil.getBytes(entity));

        entityDb.put(txn, key, data);
        logger.log(Level.FINE, "Admin entity stored type: {0}", entity);
    }

    /**
     * Fetches an entity using the specified index key.  Callers are
     * responsible for exception handling. 
     *
     * @param txn transaction
     * @param indexKey index key
     * @param lockMode lockMode
     * @return an entity object corresponding to the index key
     * @throw {@link DatabaseNotReadyException} if the database handle is not
     * yet opened
     */
    @SuppressWarnings("unchecked")
    public T fetchEntity(final Transaction txn,
                         final K indexKey,
                         final LockMode lockMode) {
        checkIfEntityDbValid();

        final DatabaseEntry key = keyToEntry(indexKey);
        final DatabaseEntry value = new DatabaseEntry();

        entityDb.get(txn, key, value, lockMode);
        return (T) SerializationUtil.getObject(value.getData(),
                                               AdminEntity.class);
    }

    public boolean isClosing() {
        return closing;
    }

    private void checkIfEntityDbValid() {
        if (entityDb == null || closing) {
            /* shutting down */
            if (admin.isClosing()) {
                throw new NonfatalAssertionException(
                    getDBName() + " is closed in admin shutting down");
            }
            /* May be renewing repEnv, retry */
            throw new DatabaseNotReadyException(getDBName() + " is not ready");
        }
    }

    private String getDBName() {
        return String.format("Admin%sDatabase", getType().getKey());
    }

    /**
     * Returns the type of entity to store.
     */
    protected abstract EntityType getType();

    /**
     * Transforms the indexing key to a database entry so that it can be used
     * in underlying entity database.
     */
    protected abstract DatabaseEntry keyToEntry(K key);
}
