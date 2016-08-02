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

import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.rep.migration.MigrationStreamHandle;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.util.TxnUtil;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Get;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationResult;
import com.sleepycat.je.Transaction;

/**
 * Server handler for {@link Delete}.
 */
class DeleteHandler extends BasicDeleteHandler<Delete> {

    DeleteHandler(OperationHandler handler) {
        super(handler, OpCode.DELETE, Delete.class);
    }

    @Override
    Result execute(Delete op, Transaction txn, PartitionId partitionId) {
        verifyDataAccess(op);

        final ReturnResultValueVersion prevVal =
            new ReturnResultValueVersion(op.getReturnValueVersionChoice());

        final boolean result =
            delete(txn, partitionId, op.getKeyBytes(), prevVal);

        return new Result.DeleteResult(getOpCode(), prevVal.getValueVersion(),
                                       result);
    }

    /**
     * Delete the key/value pair associated with the key.
     */
    private boolean delete(Transaction txn,
                           PartitionId partitionId,
                           byte[] keyBytes,
                           ReturnResultValueVersion prevValue) {

        assert (keyBytes != null);

        final Database db = getRepNode().getPartitionDB(partitionId);
        final DatabaseEntry keyEntry = new DatabaseEntry(keyBytes);

        /* Simple case: previous version and value are not returned. */
        if (!prevValue.getReturnChoice().needValueOrVersion()) {

            final OperationResult result = db.delete(txn, keyEntry, null);
            if (result != null) {
                MigrationStreamHandle.get().addDelete(keyEntry, null);
            }
            return (result != null);
        }

        /*
         * To return previous value/version, we must first position on the
         * existing record and then delete it.
         */
        final Cursor cursor = db.openCursor(txn, CURSOR_DEFAULT);
        try {
            final DatabaseEntry prevData =
                prevValue.getReturnChoice().needValue() ?
                new DatabaseEntry() :
                NO_DATA;
            final OperationResult result =
                cursor.get(keyEntry, prevData,
                           Get.SEARCH, LockMode.RMW.toReadOptions());
            if (result == null) {
                return false;
            }
            getPrevValueVersion(cursor, prevData, prevValue, result);
            cursor.delete(null);
            MigrationStreamHandle.get().addDelete(keyEntry, cursor);
            return true;
        } finally {
            TxnUtil.close(cursor);
        }
    }
}
