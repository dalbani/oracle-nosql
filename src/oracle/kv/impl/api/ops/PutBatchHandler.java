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

import static oracle.kv.impl.api.ops.OperationHandler.CURSOR_DEFAULT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import oracle.kv.UnauthorizedException;
import oracle.kv.Version;
import oracle.kv.impl.api.bulk.BulkPut.KVPair;
import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.ops.OperationHandler.KVAuthorizer;
import oracle.kv.impl.api.ops.Result.PutBatchResult;
import oracle.kv.impl.rep.migration.MigrationStreamHandle;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.util.TxnUtil;
import oracle.kv.table.TimeToLive;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationResult;
import com.sleepycat.je.Put;
import com.sleepycat.je.Transaction;

/**
 * Server handler for {@link PutBatch}.
 */
class PutBatchHandler extends MultiKeyOperationHandler<PutBatch> {

    PutBatchHandler(OperationHandler handler) {
        super(handler, OpCode.PUT_BATCH, PutBatch.class);
    }

    @Override
    Result execute(PutBatch op,
                   Transaction txn,
                   PartitionId partitionId)
        throws UnauthorizedException {

        checkTableExists(op);

        final KVAuthorizer kvAuth = checkPermission(op);

        final List<Integer> keysPresent =
            putIfAbsentBatch(txn, partitionId, op.getKvPairs(), kvAuth);

        return new PutBatchResult(op.getKvPairs().size(), keysPresent);
    }

    private List<Integer> putIfAbsentBatch(Transaction txn,
                                           PartitionId partitionId,
                                           List<KVPair> kvPairs,
                                           KVAuthorizer kvAuth) {

        final com.sleepycat.je.WriteOptions noExpiry =
            makeOption(TimeToLive.DO_NOT_EXPIRE, false);

        final List<Integer> keysPresent = new ArrayList<Integer>();

        final Database db = getRepNode().getPartitionDB(partitionId);
        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry dataEntry = new DatabaseEntry();

        /*
         * To return previous value/version, we have to either position on the
         * existing record and update it, or insert without overwriting.
         */
        final Cursor cursor = db.openCursor(txn, CURSOR_DEFAULT);
        int i = -1;

        try {
            for (KVPair e : kvPairs) {
                i++;
                keyEntry.setData(e.getKey());
                /*
                 * The returned entry may be the same one passed in, but if the
                 * entry is empty, it'll be a static, shared value.
                 */
                DatabaseEntry dataEntryToUse =
                    valueDatabaseEntry(dataEntry, e.getValue());

                if (!kvAuth.allowAccess(keyEntry)) {
                    throw new UnauthorizedException("Insufficient access " +
                      "rights granted");
                }

                while (true) {
                    final com.sleepycat.je.WriteOptions jeOptions;
                    int ttlVal = e.getTTLVal();
                    if (ttlVal != 0) {
                        jeOptions = makeJEWriteOptions(ttlVal,
                                                       e.getTTLUnitOrdinal());
                    } else {
                        jeOptions = noExpiry;
                    }
                    final OperationResult result = cursor.put(keyEntry,
                                                              dataEntryToUse,
                                                              Put.NO_OVERWRITE,
                                                              jeOptions);
                    if (result != null) {
                        final Version v = getVersion(cursor);
                        MigrationStreamHandle.get().
                            addPut(keyEntry, dataEntryToUse,
                                   v.getVLSN(), result.getExpirationTime());
                        break;
                    }
                    /* Key already exists. */
                    keysPresent.add(i);
                    break;
                }
            }
        } finally {
            TxnUtil.close(cursor);
        }

        return keysPresent;
    }

    @Override
    List<? extends KVStorePrivilege> getRequiredPrivileges(PutBatch op) {
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

    private void checkTableExists(PutBatch op) {
        if (op.getTableIds() != null) {
            for (long id : op.getTableIds()) {
                getAndCheckTable(id);
            }
        }
    }

    private com.sleepycat.je.WriteOptions
        makeJEWriteOptions(int ttlVal,  byte ttlUnitOrdinal) {

        final TimeUnit ttlUnit = TimeUnit.values()[ttlUnitOrdinal];
        return new com.sleepycat.je.WriteOptions()
                    .setTTL(ttlVal, ttlUnit)
                    .setUpdateTTL(false);
    }
}
