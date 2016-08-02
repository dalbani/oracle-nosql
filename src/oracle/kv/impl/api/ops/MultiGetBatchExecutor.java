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

package oracle.kv.impl.api.ops;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Transaction;

import oracle.kv.impl.topo.PartitionId;

/**
 * A class encapsulates the execute function for multi-get-batch operation.
 *
 * @param <T> the type of the associated operation
 * @param <V> the result type
 */
class MultiGetBatchExecutor<T extends InternalOperation, V> {

    final MultiGetBatchHandler<T, V> handler;

    MultiGetBatchExecutor(MultiGetBatchHandler<T, V> handler) {
        this.handler = handler;
    }

    public Result execute(T op,
                          Transaction txn,
                          PartitionId partitionId,
                          List<byte[]> keys,
                          byte[] resumeKey,
                          int batchSize) {

        final List<V> results = new ArrayList<V>();
        int index = 0;
        boolean hasMore = false;
        byte[] resumeSubKey = resumeKey;
        final List<V> subResults = new ArrayList<V>();
        for (; index < keys.size(); index++) {
            final byte[] parentKey = keys.get(index);
            final int subBatchSize = (batchSize > results.size()) ?
                                        batchSize - results.size() : 1;
            final boolean moreElements =
                handler.iterate(op, txn, partitionId, parentKey, subBatchSize,
                                resumeSubKey, subResults);

            results.addAll(subResults);

            if (moreElements) {
                hasMore = true;
                break;
            }
            if (resumeSubKey != null) {
                resumeSubKey = null;
            }

            subResults.clear();
        }
        /*
         * If the total number of fetched records exceeds the batchSize, then
         * get batch operation will stop fetching and return the number of
         * keys retrieved.
         */
        final int resumeParentKeyIndex = (hasMore ? index : -1);
        return handler.createIterateResult(results, hasMore,
                                           resumeParentKeyIndex);
    }

    /**
     * The interface to be implemented by multi-get-batch operations.
     */
    static interface MultiGetBatchHandler<T, V> {

        /* Iterate values and return the next batch. */
        boolean iterate(T op,
                        Transaction txn,
                        PartitionId partitionId,
                        byte[] parentKey,
                        int subBatchSize,
                        byte[] resumeSubKey,
                        List<V> results);

        /* Create bulk get iterate result. */
        Result createIterateResult(List<V> results,
                                   boolean hasMore,
                                   int resumeParentKeyIndex);
    }
}
