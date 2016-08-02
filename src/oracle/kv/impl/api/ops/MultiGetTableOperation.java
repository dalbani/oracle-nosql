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
import java.util.Collections;
import java.util.List;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import oracle.kv.Direction;
import oracle.kv.KeyRange;
import oracle.kv.impl.api.ops.OperationHandler.ScanVisitor;
import oracle.kv.impl.api.table.TargetTables;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.topo.PartitionId;

/**
 * The base class for multi-get table operation.
 */
abstract class MultiGetTableOperation extends MultiTableOperation {

    /**
     * Construct a table multi-get table operation.
     *
     * @param parentKey the parent key whose "child" KV pairs are to be fetched.
     * @param targetTables encapsulates target tables including child and/or
     * ancestor tables.
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange.
     */
    public MultiGetTableOperation(OpCode opCode,
                                  byte[] parentKey,
                                  TargetTables targetTables,
                                  KeyRange subRange) {
        super(opCode, parentKey, targetTables, subRange);
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    MultiGetTableOperation(OpCode opCode, ObjectInput in, short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);
    }

    boolean iterateTable(OperationHandler operationHandler,
                         Transaction txn,
                         PartitionId partitionId,
                         byte[] parentKey,
                         int batchSize,
                         byte[] resumeKey,
                         OperationHandler.ScanVisitor visitor) {

        return iterateTable(operationHandler, txn, partitionId, parentKey,
                            true /*majorPathComplete*/, Direction.FORWARD,
                            batchSize, resumeKey,
                            OperationHandler.CURSOR_READ_COMMITTED,
                            LockMode.READ_UNCOMMITTED_ALL, visitor);
    }

    /**
     * The TableScanVisitor class implements ScanVisitor for table scan
     * operations.
     */
    static abstract class TableScanVisitor implements ScanVisitor {

        private final MultiTableOperation operation;
        private final OperationHandler operationHandler;

        TableScanVisitor(MultiTableOperation operation,
                         OperationHandler operationHandler) {
            this.operation = operation;
            this.operationHandler = operationHandler;
        }

        MultiTableOperation getOperation() {
            return operation;
        }

        OperationHandler getOperationHandler() {
            return operationHandler;
        }

        abstract int addResults(Cursor cursor,
                                DatabaseEntry keyEntry,
                                DatabaseEntry dataEntry);
        @Override
        public int visit(Cursor cursor,
                         DatabaseEntry keyEntry,
                         DatabaseEntry dataEntry) {

            /*
             * 1.  check to see if key is part of table
             * 2.  if so:
             *    - fetch data
             *    - add to results
             */
            int match = operation.keyInTargetTable(operationHandler,
                                                   keyEntry,
                                                   dataEntry,
                                                   cursor);
            if (match > 0) {
                final int nRecs = addResults(cursor, keyEntry, dataEntry);
                return (nRecs == 0) ? 0: match + nRecs;
            }
            return match;
        }
    }

    /**
     * An implementation of ScanVisitor that is used for table rows scan.
     */
    static class TableScanValueVisitor extends TableScanVisitor {

        private List<ResultKeyValueVersion> results;

        TableScanValueVisitor(MultiTableOperation operation,
                              OperationHandler operationHandler,
                              List<ResultKeyValueVersion> results) {

            super(operation, operationHandler);
            this.results = results;
        }

        @Override
        int addResults(Cursor cursor,
                       DatabaseEntry keyEntry,
                       DatabaseEntry dataEntry) {

            final MultiTableOperation operation = getOperation();
            final OperationHandler operationHandler = getOperationHandler();
            int nRecs =  0;
            /*
             * Because the iteration does not fetch the data, it is
             * necessary to lock the record and fetch the data now.
             * This is an optimization to avoid data fetches for
             * rows that are not in a target table.
             */
            assert dataEntry.getPartial();
            final DatabaseEntry dentry = new DatabaseEntry();
            if (cursor.getCurrent
                (keyEntry, dentry,
                 LockMode.DEFAULT) == OperationStatus.SUCCESS) {

                /*
                 * Filter out non-table data.
                 */
                if (!operation.isTableData(dentry.getData(), null)) {
                    return 0;
                }

                /*
                 * Add ancestor table results.  These appear
                 * before targets, even for reverse iteration.
                 */
                nRecs = operation.addAncestorValues(cursor, results, keyEntry);
                addValueResult(operationHandler, results, cursor, keyEntry,
                               dentry);
            }
            return nRecs;
        }
    }

    /**
     * An implementation of ScanVisitor that is used table keyonly scan.
     */
    static class TableScanKeyVisitor extends TableScanVisitor {

        private List<byte[]> results;

        TableScanKeyVisitor(MultiTableOperation operation,
                            OperationHandler operationHandler,
                            List<byte[]> results) {

            super(operation, operationHandler);
            this.results = results;
        }

        @Override
        int addResults(Cursor cursor,
                       DatabaseEntry keyEntry,
                       DatabaseEntry dataEntry) {

            final MultiTableOperation operation = getOperation();
            int nRecs =  0;

            /*
             * The iteration was done using READ_UNCOMMITTED_ALL so
             * it's necessary to call getCurrent() here to lock
             * the record.  The original DatabaseEntry is used
             * to avoid fetching data.  It had setPartial() called
             * on it.
             */
            assert dataEntry.getPartial();
            if (cursor.getCurrent
                (keyEntry, dataEntry,
                 LockMode.DEFAULT) == OperationStatus.SUCCESS) {

                /*
                 * Add ancestor table results.  These appear
                 * before targets, even for reverse iteration.
                 */
                nRecs = operation.addAncestorKeys(cursor, results, keyEntry);
                addKeyResult(results, keyEntry.getData());
            }
            return nRecs;
        }
    }

    @Override
    public List<? extends KVStorePrivilege>
        tableAccessPrivileges(long tableId) {

        return Collections.singletonList(new TablePrivilege.ReadTable(tableId));
    }
}
