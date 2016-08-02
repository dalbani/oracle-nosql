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

import com.sleepycat.je.utilint.VLSN;

/**
 * Interface to define client's callback to process each txn or operation
 * after txn agenda commit it to ES index.
 */
interface TransactionPostCommitCallback {

    /**
     * Callback after committing a transaction in agenda
     *
     * @param txn  txn committed
     * @param commitVLSN  vlsn of commit
     */
    void postCommit(TransactionAgenda.Transaction txn, VLSN commitVLSN);

    /**
     * Callback after committing COPY operation from partition transfer. In
     * this case there is no commit, and we commit each COPY operation from
     * partition transfer.
     *
     * @param op  index operation built on the COPY operation
     */
    void postCommit(IndexOperation op);
}
