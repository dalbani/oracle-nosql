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

import java.util.Collections;
import java.util.List;

import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KeyRange;
import oracle.kv.impl.api.lob.KVLargeObjectImpl;
import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.ops.OperationHandler.KVAuthorizer;
import oracle.kv.impl.fault.WrappedClientException;
import oracle.kv.impl.rep.migration.MigrationStreamHandle;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.util.UserDataControl;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationResult;
import com.sleepycat.je.Transaction;

/**
 * Server handler for {@link MultiDelete}.
 */
class MultiDeleteHandler extends MultiKeyOperationHandler<MultiDelete> {

    MultiDeleteHandler(OperationHandler handler) {
        super(handler, OpCode.MULTI_DELETE, MultiDelete.class);
    }

    @Override
    Result execute(MultiDelete op,
                   Transaction txn,
                   PartitionId partitionId) {

        final KVAuthorizer kvAuth = checkPermission(op);

        final int result = multiDelete(
            txn, partitionId, op.getParentKey(), op.getSubRange(),
            op.getDepth(), op.getLobSuffixBytes(), kvAuth);

        return new Result.MultiDeleteResult(getOpCode(), result);
    }

    /**
     * Deletes the keys in a multi-key scan.
     */
    private int multiDelete(Transaction txn,
                            PartitionId partitionId,
                            byte[] parentKey,
                            KeyRange subRange,
                            Depth depth,
                            final byte[] lobSuffixBytes,
                            final KVAuthorizer auth) {

        int nDeletions = 0;

        Scanner scanner = new Scanner(txn,
                                      partitionId,
                                      getRepNode(),
                                      parentKey,
                                      true, // majorPathComplete
                                      subRange,
                                      depth,
                                      Direction.FORWARD,
                                      null, // resumeKey
                                      CURSOR_DEFAULT,
                                      LockMode.RMW,
                                      true); /* key-only */

        DatabaseEntry keyEntry = scanner.getKey();
        Cursor cursor = scanner.getCursor();
        boolean moreElements;
        try {
            while ((moreElements = scanner.next()) == true) {
                     if (!auth.allowAccess(keyEntry)) {

                         /*
                          * The requestor is not permitted to see this entry,
                          * so silently skip it.
                          */
                         continue;
                     }

                     if (KVLargeObjectImpl.hasLOBSuffix(keyEntry.getData(),
                                                        lobSuffixBytes)) {
                         final String msg =
                             "Operation: multiDelete" +
                             " Illegal LOB key argument: " +
                             UserDataControl.displayKey(keyEntry.getData()) +
                             ". Use LOB-specific APIs to modify a " +
                             "LOB key/value pair.";
                         throw new WrappedClientException
                             (new IllegalArgumentException(msg));
                     }

                     final OperationResult result = cursor.delete(null);
                     assert (result != null);
                     MigrationStreamHandle.get().addDelete(keyEntry, cursor);
                     nDeletions++;
            }
        } finally {
            scanner.close();
        }
        assert(!moreElements);
        return nDeletions;
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
    public List<? extends KVStorePrivilege>
        tableAccessPrivileges(long tableId) {
        return Collections.singletonList(
            new TablePrivilege.DeleteTable(tableId));
    }
}
