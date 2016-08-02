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

package oracle.kv.impl.api.ops;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.List;

import com.sleepycat.je.Transaction;

import oracle.kv.KeyRange;
import oracle.kv.impl.api.table.TargetTables;
import oracle.kv.impl.topo.PartitionId;

/**
 * A multi-get-batch-keys table operation
 */
public class MultiGetBatchTableKeys
    extends MultiGetBatchTableOperation<byte[]> {

    /**
     * Construct a multi-get-batch-keys table operation.
     *
     * @param parentKeys the batch of parent keys.
     * @param resumeKey is the key after which to resume the iteration of
     * descendants, or null to start at the parent.
     * @param targetTables encapsulates target tables including child and/or
     * ancestor tables.
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange.
     * @param batchSize the max number of keys to return in one call.
     */
    public MultiGetBatchTableKeys(List<byte[]> parentKeys,
                                  byte[] resumeKey,
                                  TargetTables targetTables,
                                  KeyRange subRange,
                                  int batchSize) {

        super(OpCode.MULTI_GET_BATCH_TABLE_KEYS, parentKeys, resumeKey,
              targetTables, subRange, batchSize);
    }

    public MultiGetBatchTableKeys(ObjectInput in, short serialVersion)
        throws IOException {

        super(OpCode.MULTI_GET_BATCH_TABLE_KEYS, in, serialVersion);
    }

    @Override
    public boolean iterate(Transaction txn,
                           PartitionId partitionId,
                           final OperationHandler operationHandler,
                           byte[] parentKey,
                           int subBatchSize,
                           byte[] resumeSubKey,
                           final List<byte[]> results) {

        return iterateTable(operationHandler, txn, partitionId,
                            parentKey, subBatchSize, resumeSubKey,
                            new TableScanKeyVisitor(this, operationHandler,
                                                    results));
    }

    @Override
    public Result createIterateResult(List<byte[]> results,
                                      boolean hasMore,
                                      int resumeParentKeyIndex) {

        return new Result.BulkGetKeysIterateResult(getOpCode(),
                                                   results, hasMore,
                                                   resumeParentKeyIndex);
    }
}
