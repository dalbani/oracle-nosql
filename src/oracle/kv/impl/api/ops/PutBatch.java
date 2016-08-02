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
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import oracle.kv.UnauthorizedException;
import oracle.kv.impl.api.bulk.BulkPut.KVPair;
import oracle.kv.impl.api.ops.OperationHandler.KVAuthorizer;
import oracle.kv.impl.api.ops.Result.PutBatchResult;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.topo.PartitionId;

import com.sleepycat.je.Transaction;

/**
 * A put-batch operation.
 */
public class PutBatch extends MultiKeyOperation {

    private final List<KVPair> kvPairs;
    private final long[] tableIds;

    public PutBatch(List<KVPair> le, long[] tableIds) {
        super(OpCode.PUT_BATCH, null, null, null);
        this.kvPairs = le;
        this.tableIds = tableIds;
    }

    PutBatch(ObjectInput in, short serialVersion) throws IOException {

        super(OpCode.PUT_BATCH, in, serialVersion);
        final int kvPairCount = in.readInt();

        kvPairs = new ArrayList<KVPair>();

        for (int i = 0; i < kvPairCount; i++) {

            final int keySize = in.readInt();

            final byte[] key = new byte[keySize];
            in.readFully(key);

            final int valueSize = in.readInt();

            final byte[] value = new byte[valueSize];
            in.readFully(value);

            kvPairs.add(new KVPair(key, value));
        }

        final int tableIdCount = in.readInt();
        if (tableIdCount == -1) {
            tableIds = null;
        } else {
            tableIds = new long[tableIdCount];
            for (int i = 0; i < tableIdCount; i++) {
                tableIds[i] = in.readLong();
            }
        }
    }

    @Override
    public void writeFastExternal(ObjectOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);

        out.writeInt(kvPairs.size());

        for (KVPair e : kvPairs) {

            final byte[] key = e.getKey();

            out.writeInt(key.length);
            out.write(key);

            final byte[] value = e.getValue();
            out.writeInt(value.length);
            out.write(value);
        }

        if (tableIds != null) {
            out.writeInt(tableIds.length);
            for (Long tableId : tableIds) {
                out.writeLong(tableId);
            }
        } else {
            out.writeInt(-1);
        }
    }

    @Override
    public Result execute(Transaction txn,
                          PartitionId partitionId,
                          OperationHandler operationHandler)
        throws UnauthorizedException {

        checkTableExists(operationHandler);

        final KVAuthorizer kvAuth = checkPermission(operationHandler);

        final List<Integer> keysPresent =
            operationHandler.putIfAbsentBatch(txn, partitionId,
                                              kvPairs, kvAuth);

        return new PutBatchResult(keysPresent);
    }

    @Override
    public List<? extends KVStorePrivilege> getRequiredPrivileges() {
        /*
         * Checks the basic privilege for authentication here, and leave the
         * keyspace checking and the table access checking in
         * {@code operationHandler.putIfAbsentBatch()}.
         */
        return SystemPrivilege.usrviewPrivList;
    }

    @Override
    List<? extends KVStorePrivilege> schemaAccessPrivileges() {
        return SystemPrivilege.schemaWritePrivList;
    }

    @Override
    List<? extends KVStorePrivilege> generalAccessPrivileges() {
        return SystemPrivilege.writeOnlyPrivList;
    }

    @Override
    public
    List<? extends KVStorePrivilege> tableAccessPrivileges(long tableId) {
        return Collections.singletonList(
                   new TablePrivilege.InsertTable(tableId));
    }

    private void checkTableExists(OperationHandler operationHandler) {
        if (tableIds != null) {
            for (long id : tableIds) {
                TableOperationHandler.getAndCheckTable(operationHandler, id);
            }
        }
    }
}
