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

package oracle.kv.impl.query.runtime.server;

import static oracle.kv.impl.api.ops.OperationHandler.CURSOR_READ_COMMITTED;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;

import oracle.kv.Direction;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.impl.api.ops.IndexKeysIterate;
import oracle.kv.impl.api.ops.IndexKeysIterateHandler;
import oracle.kv.impl.api.ops.IndexScanner;
import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.ops.MultiGetTableKeys;
import oracle.kv.impl.api.ops.MultiGetTableKeysHandler;
import oracle.kv.impl.api.ops.MultiTableOperationHandler;
import oracle.kv.impl.api.ops.MultiTableOperationHandler.OperationTableInfo;
import oracle.kv.impl.api.ops.OperationHandler;
import oracle.kv.impl.api.ops.Scanner;
import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.api.table.IndexKeyImpl;
import oracle.kv.impl.api.table.IndexRange;
import oracle.kv.impl.api.table.RowImpl;
import oracle.kv.impl.api.table.TableAPIImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableKey;
import oracle.kv.impl.api.table.TargetTables;
import oracle.kv.impl.query.runtime.RuntimeControlBlock;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.table.FieldRange;
import oracle.kv.table.IndexKey;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;

/**
 * This class encapsulates 2-part table scanning performed from a query
 * operation. It allows filtering based on keys to be done before fetching
 * data associated with a query. An instance of this class is created before
 * the server-side query operation starts. When a table iterator is
 * encountered in the query it creates an instance of TableScanner and
 * then does one of several things:
 *  1. Calls Row nextKey() to get the "next" key, which returns only key
 *   fields in the Row.
 *    a. filters based on the key fields and then, if necessary.
 *    b. calls Row fetchData() to get the data
 * 2. Calls Row nextRow() if key-based filtering is not relevant.
 *
 * Because partially populated Row instances are returned this pattern works for
 * index scans as well as primary key scans.
 *
 * TableScanner instances must be closed in order to release resources.
 */
public class TableScannerFactory {

    final private Transaction txn;
    final private PartitionId pid;
    private final MultiGetTableKeysHandler primaryOpHandler;
    private final IndexKeysIterateHandler secondaryOpHandler;

    /*
     * The interface for scanners returned by this factory.
     */
    public interface TableScanner {

        /**
         * Returns the next key, deserialized, or null if there is no next key.
         * In this case the record may not be locked. If the row is to be used
         * as a result either lockKey() or fetchData() must be called to lock
         * the record. If either of those fails it means that the row was
         * removed out from under this operation, which is fine.
         */
        public Row nextKey();

        /**
         * Returns the next row, deserialized, or null if there is no next row.
         * The data is locked.
         */
        public Row nextRow();

        /**
         * Locks the current row. This interface may only be called after
         * nextKey() has returned non-null.  The intial scan may have been done
         * using READ_UNCOMMITTED, which is why this call, or fetchData(), is
         * necessary to lock the record.
         * @return true if the record is locked, false if it cannot be locked,
         * which means that the record is no longer present.
         */
        public boolean lockRow();

        /**
         * Locks and fetches the data associated with the "current" key. This
         * interface may only be called after nextKey() has returned non-null.
         * @param row the Row returned by nextKey()
         * @return the complete row or null if the row has been removed.
         */
        public Row fetchData(Row row);

        /**
         * Closes the scanner. This must be called to avoid resource leaks.
         */
        public void close();
    }

    public TableScannerFactory(
        final Transaction txn,
        final PartitionId pid,
        final OperationHandler oh) {

        this.txn = txn;
        this.pid = pid;
        primaryOpHandler = (MultiGetTableKeysHandler)
            oh.getHandler(OpCode.MULTI_GET_TABLE_KEYS);
        secondaryOpHandler = (IndexKeysIterateHandler)
            oh.getHandler(OpCode.INDEX_KEYS_ITERATE);
    }

    /*
     * Returns a TableScanner. This is an index scanner if indexKey is not null,
     * otherwise it is a primary key scanner. In both cases the object must be
     * closed to avoid leaking resources and/or leaving records locked.
     */
    public TableScanner getTableScanner(
        RuntimeControlBlock rcb,
        Direction dir,
        PrimaryKey primaryKey,
        IndexKey indexKey,
        FieldRange range) {

        assert primaryKey == null || indexKey == null;

        if (indexKey != null) {
            return getSecondaryTableScanner(rcb, dir, indexKey, range);
        }
        return getPrimaryTableScanner(rcb, dir, primaryKey, range);
    }

    /*
     * Methods associated with a primary key scanner
     */
    private TableScanner getPrimaryTableScanner(
        RuntimeControlBlock rcb,
        Direction dir,
        PrimaryKey key,
        FieldRange range) {

        /*
         * Create a MultiTableOperation for a single target table
         */
        TableImpl table = (TableImpl) key.getTable();

        TableKey tableKey = TableKey.createKey(table, key, true);
        assert tableKey != null;

        final TargetTables targetTables = new TargetTables(table, null, null);

        final MultiGetTableKeys op =
            new MultiGetTableKeys(tableKey.getKeyBytes(),
                                  targetTables,
                                  (range != null ?
                                   TableAPIImpl.createKeyRange(range) :
                                   null));
        primaryOpHandler.verifyTableAccess(op);

        /*
         * Create a key-only scanner using dirty reads. This means that in order
         * to use the record, it must be locked, and if the data is required, it
         * must be fetched.
         */
        final OperationTableInfo tableInfo = new OperationTableInfo();
        Scanner scanner = primaryOpHandler.getScanner(
            op,
            tableInfo,
            txn,
            pid,
            tableKey.getMajorKeyComplete(),
            dir,
            rcb.getPrimaryResumeKey(),
            CURSOR_READ_COMMITTED,
            LockMode.READ_UNCOMMITTED_ALL,
            true); /* use a key-only scanner; fetch data in the "next" call */

        return new PrimaryTableScanner(op,
                                       tableInfo,
                                       scanner,
                                       (TableImpl)key.getTable(),
                                       rcb);
    }

    /*
     * Methods associated with a secondary key scanner
     */
    private TableScanner getSecondaryTableScanner(
        RuntimeControlBlock rcb,
        Direction dir,
        IndexKey key,
        FieldRange range) {

        IndexImpl index = (IndexImpl) key.getIndex();
        TableImpl table = (TableImpl) index.getTable();

        /*
         * Create an IndexOperation for a single target table
         */
        IndexRange indexRange = new IndexRange((IndexKeyImpl)key, range, dir);

        final TargetTables targetTables = new TargetTables(table, null, null);

        final IndexKeysIterate op =
            new IndexKeysIterate(index.getName(),
                                 targetTables,
                                 indexRange,
                                 rcb.getSecondaryResumeKey(),
                                 rcb.getPrimaryResumeKey(),
                                 0 /* batch size not needed */);

        secondaryOpHandler.verifyTableAccess(op);

        /*
         * Create a key-only scanner using dirty reads. This means that in order
         * to use the record, it must be locked, and if the data is required, it
         * must be fetched.
         */
        IndexScanner scanner =
            secondaryOpHandler.getIndexScanner(
                op,
                txn,
                OperationHandler.CURSOR_READ_COMMITTED,
                LockMode.READ_UNCOMMITTED_ALL,
                true);

        return new SecondaryTableScanner(scanner,
                                         (TableImpl) index.getTable(),
                                         index,
                                         rcb);
    }

    /*
     * The underlying Scanner used by PrimaryTableScanner uses
     * DIRTY_READ_ALL lockmode and does a key-only scan.
     */
    private class PrimaryTableScanner implements TableScanner {

        final MultiGetTableKeys op;
        final OperationTableInfo tableInfo;
        final TableImpl table;
        final RuntimeControlBlock rcb;
        final Scanner scanner;
        final DatabaseEntry dataEntry;
        boolean moreElements;
        Row current;

        PrimaryTableScanner(
            MultiGetTableKeys op,
            OperationTableInfo tableInfo,
            Scanner scanner,
            TableImpl table,
            RuntimeControlBlock rcb) {

            this.op = op;
            this.tableInfo = tableInfo;
            this.scanner = scanner;
            this.table = table;
            this.rcb = rcb;
            moreElements = true;
            dataEntry = new DatabaseEntry();
        }

        @Override
        public void close() {
            scanner.close();
        }

        @Override
        public Row nextKey() {
            getNextKey();
            return current;
        }

        @Override
        public boolean lockRow() {
            return scanner.getCurrent();
        }

        @Override
        public Row nextRow() {
            Row row = nextKey();
            while (row != null) {
                row = fetchData(row);
                if (row != null) {
                    return row;
                }
                row = nextKey();
            }
            return null;
        }

        /**
         * Fetches the data for the Row.
         * @param key a partially populated Row. It has the primary key fields.
         */
        @Override
        public Row fetchData(Row key) {

            if (!scanner.getLockedData(dataEntry)) {
                return null;
            }

            byte[] data = dataEntry.getData();

            if (!MultiTableOperationHandler.isTableData(data, null)) {
                return null;
            }

            if (data == null || data.length <= 1) {
                /* a key-only table, no data to fetch */
                return key;
            }

            Value.Format format = Value.Format.fromFirstByte(data[0]);
            RowImpl row = (RowImpl) key;
            if (row.getTableImpl().initRowFromByteValue(row, data,
                                                        format, 1)) {
                return row;
            }

            return null;
        }

        private void getNextKey() {

            while (moreElements && scanner.next()) {
                current = createKey();
                if (current != null) {
                    moreElements = true;
                    return;
                }
            }

            rcb.setPrimaryResumeKey(null);
            current = null;
            moreElements = false;
        }

        private Row createKey() {

            int match = primaryOpHandler.keyInTargetTable(op,
                                                          tableInfo,
                                                          scanner.getKey(),
                                                          scanner.getData(),
                                                          scanner.getCursor());
            if (match <= 0) {
                if (match < 0) {
                    moreElements = false;
                }
                return null;
            }

            /*
             * Create the row from key bytes
             */
            byte[] keyBytes = scanner.getKey().getData();
            rcb.setPrimaryResumeKey(keyBytes);
            return table.createRowFromKeyBytes(keyBytes);
        }
    }

    /**
     * The underlying IndexScanner used by SecondaryTableScanner uses
     * DIRTY_READ_ALL lockmode and does a key-only scan.
     */
    private class SecondaryTableScanner implements TableScanner {

        final RuntimeControlBlock rcb;
        final IndexScanner scanner;
        final TableImpl table;
        final IndexImpl index;
        final DatabaseEntry dataEntry;
        boolean moreElements;
        Row current;


        SecondaryTableScanner(
            IndexScanner scanner,
            TableImpl table,
            IndexImpl index,
            RuntimeControlBlock rcb) {

            this.scanner = scanner;
            this.table = table;
            this.index = index;
            this.rcb = rcb;
            moreElements = true;
            dataEntry = new DatabaseEntry();
        }

        @Override
        public void close() {
            scanner.close();
        }

        @Override
        public Row nextKey() {
            getNextKey();
            return current;
        }

        @Override
        public boolean lockRow() {
            return scanner.getCurrent();
        }

        @Override
        public Row nextRow() {

            Row row = nextKey();

            while (row != null) {
                row = fetchData(row);
                if (row != null) {
                    return row;
                }
                row = nextKey();
            }
            return null;
        }

        private void getNextKey() {

            if (!moreElements) {
                return;
            }

            while (scanner.next()) {
                current = createKey();
                if (current != null) {
                    moreElements = true;
                    return;
                }
            }

            rcb.setPrimaryResumeKey(null);
            rcb.setSecondaryResumeKey(null);
            current = null;
            moreElements = false;
        }

        /**
         * Fetches the data for the Row.
         * @param key a partially populated Row. It has the primary key
         * fields.
         */
        @Override
        public Row fetchData(Row key) {

            if (!scanner.getLockedData(dataEntry)) {
                return null;
            }

            /* TBD: method to unpack Row directly from byte[] rather than
             * ValueVersion (see TableImpl).
             */
            ValueVersion vv =
                new ValueVersion(Value.fromByteArray(dataEntry.getData()),
                                 null);
            return ((RowImpl)key).rowFromValueVersion(vv, false);
        }

        private Row createKey() {

            DatabaseEntry indexKeyEntry = scanner.getIndexKey();
            DatabaseEntry primaryKeyEntry = scanner.getPrimaryKey();
            assert(indexKeyEntry != null && primaryKeyEntry != null);

            rcb.setPrimaryResumeKey(primaryKeyEntry.getData());
            rcb.setSecondaryResumeKey(indexKeyEntry.getData());

            /*
             * Create Row from primary key bytes
             */
            RowImpl row =
                table.createRowFromKeyBytes(primaryKeyEntry.getData());

            index.rowFromIndexKey(indexKeyEntry.getData(),
                                  row,
                                  true /* allowPartial */,
                                  true /*createTableRow*/);
            return row;
        }
    }
}
