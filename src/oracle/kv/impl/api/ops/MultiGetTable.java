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
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Transaction;

import oracle.kv.KeyRange;
import oracle.kv.impl.api.table.TargetTables;
import oracle.kv.impl.topo.PartitionId;

/**
 * A multi-get table operation over a set of records in the same partition.
 * Entire Row records are returned which means that the data is fetched
 * from matching keys.
 */
public class MultiGetTable extends MultiGetTableOperation {

    /**
     * Construct a multi-get operation.
     */
    public MultiGetTable(byte[] parentKey,
                         TargetTables targetTables,
                         KeyRange subRange) {
        super(OpCode.MULTI_GET_TABLE, parentKey, targetTables, subRange);
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    MultiGetTable(ObjectInput in, short serialVersion)
        throws IOException {

        super(OpCode.MULTI_GET_TABLE, in, serialVersion);
    }

    @Override
    public Result execute(Transaction txn,
                          PartitionId partitionId,
                          final OperationHandler operationHandler) {

        verifyTableAccess(operationHandler);

        final List<ResultKeyValueVersion> results =
            new ArrayList<ResultKeyValueVersion>();

        final boolean moreElements =
            iterateTable(operationHandler, txn, partitionId, getParentKey(),
                         0, null, new TableScanValueVisitor(this,
                                                            operationHandler,
                                                            results));

        assert (!moreElements);
        return new Result.IterateResult(getOpCode(), results, moreElements);
    }
}
