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


import com.sleepycat.je.rep.subscription.SubscriptionCallback;
import com.sleepycat.je.utilint.VLSN;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import static com.sleepycat.je.log.LogEntryType.LOG_TXN_ABORT;
import static com.sleepycat.je.log.LogEntryType.LOG_TXN_COMMIT;

/**
 * Default subscription callback in TIF to process each entry received from
 * replication stream.
 */
class FeederSubscriptionCbk implements SubscriptionCallback {

    protected final Logger logger;
    protected final SubscriptionManager.SubscriptionFilter filter;
    protected final BlockingQueue<DataItem> inputQueueRepStream;

    FeederSubscriptionCbk(SubscriptionManager.SubscriptionFilter filter,
                          BlockingQueue<DataItem> inputQueueRepStream,
                          Logger logger) {
        this.filter = filter;
        this.inputQueueRepStream = inputQueueRepStream;
        this.logger = logger;

    }

    /**
     * Process a put (insert or update) entry from stream
     *
     * @param vlsn   VLSN of the insert entry
     * @param key    key of the insert entry
     * @param value  value of the insert entry
     * @param txnId  id of txn the entry belongs to
     */
    @Override
    public void processPut(VLSN vlsn, byte[] key, byte[] value, long txnId) {
        processEntry(new DataItem(vlsn, txnId, key, value));
    }

    /**
     * Process a delete entry from stream
     *
     * @param vlsn   VLSN of the delete entry
     * @param key    key of the delete entry
     * @param txnId  id of txn the entry belongs to
     */
    @Override
    public void processDel(VLSN vlsn, byte[] key, long txnId) {
        processEntry( new DataItem(vlsn, txnId, key));
    }

    /**
     * Process a commit entry from stream
     *
     * @param vlsn  VLSN of commit entry
     * @param txnId id of txn to commit
     */
    @Override
    public void processCommit(VLSN vlsn, long txnId) {
        processEntry(new DataItem(vlsn, txnId, LOG_TXN_COMMIT));
    }

    /**
     * Process an abort entry from stream
     *
     * @param vlsn  VLSN of abort entry
     * @param txnId id of txn to abort
     */
    @Override
    public void processAbort(VLSN vlsn, long txnId) {
        processEntry(new DataItem(vlsn, txnId, LOG_TXN_ABORT));
    }

    /**
     * Process the exception from stream.
     *
     * @param exp  exception raised in service and to be processed by
     *             client
     */
    @Override
    public void processException(final Exception exp) {
        logger.info("Exception in subscription of replication stream "  +
                    exp.getLocalizedMessage());

        processEntry(new DataItem(exp));
    }

    /* internal helper to process each operation */
    private void processEntry(DataItem entry) {

        entry = filter.filter(entry);
        if (entry == null) {
            return;
        }

        /* enqueue data entry to be processed by TIF worker */
        while (true) {
            try {
                inputQueueRepStream.put(entry);
                return;
            } catch (InterruptedException e) {
                /* might have to get smarter. */
                logger.warning("Interrupted input queue operation put, " +
                               "retrying");
            }
        }
    }
}
