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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.kv.impl.api.ops.Execute.OperationImpl;
import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.util.TxnUtil;

import com.sleepycat.je.Transaction;

/**
 * Server handler for {@link Execute}.
 */
class ExecuteHandler extends InternalOperationHandler<Execute> {

    ExecuteHandler(OperationHandler handler) {
        super(handler, OpCode.EXECUTE, Execute.class);
    }

    @Override
    Result execute(Execute op,
                   Transaction txn,
                   PartitionId partitionId) {

        /*
         * Sort operation indices by operation key, to avoid deadlocks when two
         * txns access records in a different order.
         */
        final List<OperationImpl> ops = op.getOperations();
        final int listSize = ops.size();
        final Integer[] sortedIndices = new Integer[listSize];
        for (int i = 0; i < listSize; i += 1) {
            sortedIndices[i] = i;
        }
        Arrays.sort(sortedIndices, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                return KEY_BYTES_COMPARATOR.compare(
                    ops.get(i1).getInternalOp().getKeyBytes(),
                    ops.get(i2).getInternalOp().getKeyBytes());
            }
        });

        /* Initialize result list with nulls, so we can call List.set below. */
        final List<Result> results = new ArrayList<Result>(listSize);
        for (int i = 0; i < listSize; i += 1) {
            results.add(null);
        }

        /* Process operations in key order. */
        for (final int i : sortedIndices) {

            final OperationImpl opImpl = ops.get(i);
            final SingleKeyOperation internalOp = opImpl.getInternalOp();
            final Result result =
                operationHandler.execute(internalOp, txn, partitionId);

            /* Abort if operation fails and user requests abort-on-failure. */
            if (opImpl.getAbortIfUnsuccessful() && !result.getSuccess()) {
                TxnUtil.abort(txn);
                return new Result.ExecuteResult(getOpCode(), i, result);
            }

            results.set(i, result);
        }

        /* All operations succeeded, or failed without causing an abort. */
        return new Result.ExecuteResult(getOpCode(), results);
    }

    @Override
    List<? extends KVStorePrivilege> getRequiredPrivileges(Execute op) {
        /*
         * Check all privileges required by all single operations in list. The
         * execute operation will be performed if and only if all the
         * privileges are met.
         */
        final Set<KVStorePrivilege> privSet = new HashSet<KVStorePrivilege>();
        for (final OperationImpl opImpl : op.getOperations()) {
            privSet.addAll(
                operationHandler.getRequiredPrivileges(
                    opImpl.getInternalOp()));
        }
        return Collections.unmodifiableList(
            new ArrayList<KVStorePrivilege>(privSet));
    }
}
