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

package oracle.kv.impl.rep.subscription.partreader;

import oracle.kv.impl.topo.PartitionId;

/**
 * Interface of callback which is provided by clients to process
 * each entry received from source node partition migration service.
 */
public interface PartitionReaderCallBack {

    /**
     * callback function to process a COPY or PUT operation
     *
     * @param pid          id of partition
     * @param vlsn         vlsn of operation
     * @param key          key to copy or put
     * @param value        value part of operation
     */
    void processCopy(PartitionId pid, long vlsn, byte[] key, byte[] value);

    /**
     * callback function to process a COPY or PUT operation
     * 
     * @param pid          id of partition
     * @param vlsn         vlsn of operation
     * @param key          key to copy or put
     * @param value        value part of operation
     * @param txnId        id of transaction
     */
    void processPut(PartitionId pid, long vlsn, byte[] key, byte[] value,
                    long txnId);

    /**
     * callback function to process a DEL operation
     *
     * @param pid          id of partition
     * @param vlsn         vlsn of operation
     * @param key          key to delete
     * @param txnId        id of transaction
     */
    void processDel(PartitionId pid, long vlsn, byte[] key, long txnId);

    /**
     * callback function to process a PREPARE operation
     *
     * @param pid          id of partition
     * @param txnId        id of transaction
     */
    void processPrepare(PartitionId pid, long txnId);

    /**
     * callback function to COMMIT a transaction
     *
     * @param pid          id of partition
     * @param txnId        id of transaction
     */
    void processCommit(PartitionId pid, long txnId);

    /**
     * callback function to ABORT a transaction
     *
     * @param pid          id of partition
     * @param txnId        id of transaction
     */
    void processAbort(PartitionId pid, long txnId);

    /**
     * callback function to process EOD
     *
     * @param pid  id of partition 
     */
    void processEOD(PartitionId pid);

}
