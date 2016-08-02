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

import com.sleepycat.je.Transaction;

import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.ops.MultiGetBatchExecutor.MultiGetBatchHandler;
import oracle.kv.impl.topo.PartitionId;

/**
 * Base server handler for subclasses of MultiGetBatchTableOperation.
 */
abstract class MultiGetBatchTableOperationHandler
    <T extends MultiGetBatchTableOperation, V>
        extends MultiGetTableOperationHandler<T>
        implements MultiGetBatchHandler<T, V> {

    MultiGetBatchTableOperationHandler(OperationHandler handler,
                                       OpCode opCode,
                                       Class<T> operationType) {
        super(handler, opCode, operationType);
    }

    @Override
    Result execute(T op,
                   Transaction txn,
                   PartitionId partitionId) {

        verifyTableAccess(op);

        final MultiGetBatchExecutor<T, V> executor =
            new MultiGetBatchExecutor<T, V>(this);
        return executor.execute(op, txn, partitionId, op.getParentKeys(),
                                op.getResumeKey(), op.getBatchSize());
    }
}
