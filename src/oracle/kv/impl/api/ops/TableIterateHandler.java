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

import static oracle.kv.impl.api.ops.OperationHandler.CURSOR_READ_COMMITTED;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;

import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.topo.PartitionId;

/**
 * Server handler for {@link TableIterate}.
 */
public class TableIterateHandler
        extends TableIterateOperationHandler<TableIterate> {

    public TableIterateHandler(OperationHandler handler) {
        super(handler, OpCode.TABLE_ITERATE, TableIterate.class);
    }

    @Override
    Result execute(TableIterate op,
                   Transaction txn,
                   PartitionId partitionId) {

        verifyTableAccess(op);

        final List<ResultKeyValueVersion> results =
            new ArrayList<ResultKeyValueVersion>();

        final OperationTableInfo tableInfo = new OperationTableInfo();
        Scanner scanner = getScanner(op,
                                     tableInfo,
                                     txn,
                                     partitionId,
                                     CURSOR_READ_COMMITTED,
                                     LockMode.READ_UNCOMMITTED_ALL,
                                     true); // set keyOnly. Handle fetch here

        try {
            DatabaseEntry keyEntry = scanner.getKey();
            DatabaseEntry dataEntry = scanner.getData();

            /* this is used to do a full fetch of data when needed */
            DatabaseEntry dentry = new DatabaseEntry();
            Cursor cursor = scanner.getCursor();
            boolean moreElements;
            while ((moreElements = scanner.next()) == true) {

                int match = keyInTargetTable(op,
                                             tableInfo,
                                             keyEntry,
                                             dataEntry,
                                             cursor);
                if (match > 0) {

                    /*
                     * The iteration was done using READ_UNCOMMITTED_ALL
                     * and with the cursor set to getPartial().  It is
                     * necessary to call getLockedData() here to both lock
                     * the record and fetch the data.
                     */
                    if (scanner.getLockedData(dentry)) {

                        if (!isTableData(dentry.getData(), null)) {
                            continue;
                        }

                        /*
                         * Add ancestor table results.  These appear
                         * before targets, even for reverse iteration.
                         */
                        tableInfo.addAncestorValues(cursor, results, keyEntry);
                        addValueResult(operationHandler, results,
                                       cursor, keyEntry, dentry,
                                       scanner.getResult());
                    }
                } else if (match < 0) {
                    moreElements = false;
                    break;
                }
                if (op.getBatchSize() != 0 &&
                    results.size() >= op.getBatchSize()) {
                    break;
                }
            }
            return new Result.IterateResult(getOpCode(), results, moreElements);
        } finally {
            scanner.close();
        }
    }

    protected Scanner getScanner(TableIterate op,
                                 OperationTableInfo tableInfo,
                                 Transaction txn,
                                 PartitionId partitionId,
                                 CursorConfig cursorConfig,
                                 LockMode lockMode,
                                 boolean keyOnly) {
        return getScanner(op,
                          tableInfo,
                          txn,
                          partitionId,
                          op.getMajorComplete(),
                          op.getDirection(),
                          op.getResumeKey(),
                          cursorConfig,
                          lockMode,
                          keyOnly);
    }

    /**
     * Gets the cursor configuration to be used during the iteration.
     * @return the cursor configuration
     */
    protected CursorConfig getCursorConfig() {
        return CURSOR_READ_COMMITTED;
    }
}
