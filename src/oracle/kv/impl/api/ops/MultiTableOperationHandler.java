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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import oracle.kv.Direction;
import oracle.kv.FaultException;
import oracle.kv.Key;
import oracle.kv.MetadataNotFoundException;
import oracle.kv.UnauthorizedException;
import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TargetTables;
import oracle.kv.impl.rep.migration.MigrationStreamHandle;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.topo.PartitionId;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DbInternal;
import com.sleepycat.je.DbInternal.Search;
import com.sleepycat.je.Get;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationResult;
import com.sleepycat.je.Transaction;

/**
 * Base server handler for subclasses of MultiTableOperation.
 */
public abstract class MultiTableOperationHandler<T extends MultiTableOperation>
    extends MultiKeyOperationHandler<T> {

    /* Lowest possible value for a serialized key character. */
    private static final byte MIN_VALUE_BYTE = ((byte) 1);

    /*
     * These are possible results from a cursor reset operation in a scan
     * visitor function.
     */
    private static enum ResetResult { FOUND, /* found a key */
                                      STOP,  /* stop iteration */
                                      SKIP}  /* skip/ignore */

    MultiTableOperationHandler(OperationHandler handler,
                               OpCode opCode,
                               Class<T> operationType) {
        super(handler, opCode, operationType);
    }

    @Override
    public List<? extends KVStorePrivilege>
        tableAccessPrivileges(long tableId) {
        return Collections.singletonList(
            new TablePrivilege.ReadTable(tableId));
    }

    /**
     * Common code to add a key/value result.  This is shared by code that
     * iterates by primary key (TableIterate, MultiGetTable).
     */
    static void addValueResult(final OperationHandler operationHandler,
                               List<ResultKeyValueVersion> results,
                               Cursor cursor,
                               DatabaseEntry keyEntry,
                               DatabaseEntry dentry,
                               OperationResult result) {

        final ResultValueVersion valVers =
            operationHandler.makeValueVersion(cursor,
                                              dentry,
                                              result);
        results.add(new ResultKeyValueVersion
                    (keyEntry.getData(),
                     valVers.getValueBytes(),
                     valVers.getVersion(),
                     result.getExpirationTime()));
    }

    /**
     * A table-specific method that does 2 things:
     * 1. calls initTableLists() to set up state necessary
     * to check keys against target tables and other iteration state.
     * 2. creates a Scanner instance.
     *
     * The returned Scanner *must be* closed under all circumstances using
     * Scanner.close(). If not done the JE Cursor it contains will remain open.
     */
    public Scanner getScanner(T op,
                              OperationTableInfo tableInfo,
                              Transaction txn,
                              PartitionId partitionId,
                              boolean majorComplete,
                              Direction scanDirection,
                              byte[] resumeKey,
                              CursorConfig cursorConfig,
                              LockMode lockMode,
                              boolean keyOnly) {

        initTableLists(op, tableInfo, txn, scanDirection, resumeKey);

        return new Scanner(txn,
                           partitionId,
                           getRepNode(),
                           op.getParentKey(),
                           majorComplete,
                           op.getSubRange(),
                           op.getDepth(),
                           scanDirection,
                           resumeKey,
                           cursorConfig,
                           lockMode,
                           keyOnly);
    }

    /**
     * Check if a given key is in a target table for the iteration.
     *
     * Filtering is in 2 general categories -- efficient and not efficient.
     * Efficient filtering in this context means that uninteresting records
     * are never even part of the iteration.  Not efficient filtering requires
     * looking at the keys and matching them to target tables.
     *
     * Efficient filtering can work in these 2 cases:
     * 1.  A key fetched has more components than the maximum number of
     * components of any target table.  This efficiently avoids iterating child
     * tables when only the parent is a target.  When this situation is detected
     * the cursor is reset to the next possible target.  This code works.
     *
     * 2.  A key fetched is not part of a target table.  In this case the idea is
     * that the entire table would be skipped.  The code to attempt to do this is
     * currently disabled and does not work.  It should be addressed in the future
     * to the extent possible, at least if the table involved is a "leaf" table
     * that can have no interesting child tables of its own.
     *
     * Inefficient filtering works by comparing fetched keys to the target
     * tables.  This style of filtering will help avoid a little extra work by
     * the iteration (e.g. data fetch, and potentially locking of rows).
     *
     * TODO: think about how to combine the Key.countComponents() with the
     * Key iteration done in findTargetTable().  Currently this results in
     * 2 iterations of the byte[] version of the key.  It'd be good to do it
     * in a single iteration.  Perhaps findTargetTable() should be done first,
     * as it may be more selective.
     */
    public int keyInTargetTable(T op,
                                OperationTableInfo tableInfo,
                                DatabaseEntry keyEntry,
                                DatabaseEntry dataEntry,
                                Cursor cursor) {
        while (true) {
            final int nComponents = Key.countComponents(keyEntry.getData());
            if (nComponents > tableInfo.maxKeyComponents) {

                /*
                 * This key is out of range of any target table.  Reset the
                 * cursor to the next potential key, based on component count.
                 */
                ResetResult status =
                    resetCursorToMax(tableInfo, keyEntry, dataEntry,
                                     cursor, tableInfo.maxKeyComponents);

                /*
                 * If the reset didn't find a key, stop the iteration.
                 */
                if (status != ResetResult.FOUND) {
                    return -1;
                }
                /* fall through */
            }

            /*
             * Find the table in the hierarchy that contains this key.
             */
            final byte[] keyBytes = keyEntry.getData();
            TableImpl target =
                tableInfo.topLevelTable.findTargetTable(keyBytes);

            if (target == null) {

                /*
                 * This should not be possible unless there is a non-table key
                 * in the btree.
                 */
                String msg = "Key is not in a table: "  +
                    Key.fromByteArray(keyBytes);
                getLogger().log(Level.INFO, msg);
                return 0;
            }

            /*
             * Don't use List.contains() because Table.equals() is expensive.
             * Do a simple name match.  This is also where the value format
             * is double-checked to eliminate non-table records.
             */
            for (long tableId : op.getTargetTables().getTargetAndChildIds()) {
                if (tableId == target.getId()) {
                    return 1;
                }
            }

            /*
             * If it's a leaf table maybe it can be skipped.  TODO: make
             * resetCursorToTable() work.  Currently it is disabled so the
             * cursor is never reset.  Internal (non-leaf) tables cannot be
             * skipped if a child of that table may be a target.
             */
            if (!target.hasChildren()) {
                ResetResult status =
                    resetCursorToTable(target, keyEntry, dataEntry, cursor);
                if (status == ResetResult.FOUND) {
                    continue;
                } else if (status == ResetResult.STOP) {
                    return -1;
                }
            }
            return 0;
        }
    }

    /**
     * Return true if this data value is, or could be, from a table.
     * Could be means that if it's null or an Avro value it may, or
     * may not be from a table.
     */
    public static boolean isTableData(byte[] data, TableImpl table) {
        if (data == null ||      // not known
            data.length == 0 ||  // not known
            data[0] == 1     ||  // TABLE format
            /* accept NONE format if length is 1 */
            (data.length == 1 && data[0] == 0)  ||
            (data[0] < 0 && (table == null ||
                             (table.isR2compatible())))) {
            return true;
        }
        return false;
    }

    /** Stores table information about a multi-table operation. */
    public static class OperationTableInfo {
        AncestorList ancestors;
        TableImpl topLevelTable;
        int maxKeyComponents;
        int minKeyComponents;
        Direction direction;

        int addAncestorValues(Cursor cursor,
                              List<ResultKeyValueVersion> results,
                              DatabaseEntry keyEntry) {
            if (ancestors != null) {
                return ancestors.addAncestorValues(cursor.getDatabase(),
                                                   results,
                                                   keyEntry);
            }
            return 0;
        }

        int addAncestorKeys(Cursor cursor,
                            List<ResultKey> results,
                            DatabaseEntry keyEntry) {
            if (ancestors != null) {
                return ancestors.addAncestorKeys(cursor.getDatabase(),
                                                 results,
                                                 keyEntry);
            }
            return 0;
        }

        int deleteAncestorKeys(Cursor cursor,
                               DatabaseEntry keyEntry) {
            if (ancestors != null) {
                return ancestors.deleteAncestorKeys(cursor.getDatabase(),
                                                    keyEntry);
            }
            return 0;
        }
    }

    /**
     * Check that if the table request is legal.
     * <p>
     * We expect that all table iteration requests have a table that serves as
     * a prefix to ensure that no iteration step may visit internal system
     * content. If that's not the case, either there's a bug in the code or a
     * hacker attempting to cheat the system.
     * <p>
     * Also, we expect that all tables in the target table list are privileged
     * to access. Otherwise, the check fails.
     */
    public void verifyTableAccess(T op) throws UnauthorizedException {
        if (ExecutionContext.getCurrent() == null) {
            return;
        }

        checkKeyspacePermission(op);
        new TargetTableAccessChecker(operationHandler, this,
                                     op.getTargetTables())
            .checkAccess();
    }

    /**
     * Checks if parent key accesses legal keyspace.  Parent key of legal
     * multi-table operations should not access internal or schema keyspace.
     */
    private static void checkKeyspacePermission(MultiTableOperation op)
        throws UnauthorizedException {

        final byte[] parentKey = op.getParentKey();
        if (parentKey == null ||
            Keyspace.mayBePrivateAccess(parentKey) ||
            Keyspace.mayBeSchemaAccess(parentKey)) {
            throw new UnauthorizedException (
                "The iteration request is illegal and might access " +
                "unauthorized content");
        }
    }

    /*
     * Skip keys that are too large and reset the cursor to a known
     * possible size.
     *
     * Truncate the key to the known max number of components, then
     * append the miniumum value to move to the "next" key.
     */
    private ResetResult resetCursorToMax(OperationTableInfo tableInfo,
                                         DatabaseEntry keyEntry,
                                         DatabaseEntry dataEntry,
                                         Cursor cursor,
                                         int maxComponents) {
        byte[] bytes = keyEntry.getData();
        int newLen = Key.getPrefixKeySize(bytes, maxComponents);
        byte[] newBytes = new byte[newLen + 1];
        System.arraycopy(bytes, 0, newBytes, 0, newLen + 1);
        keyEntry.setData(newBytes);
        if (tableInfo.direction == Direction.FORWARD) {
            newBytes[newLen] = MIN_VALUE_BYTE;
        }

        /*
         * Reset the cursor.
         */
        Search search = (tableInfo.direction == Direction.REVERSE) ?
            Search.LT : Search.GTE;

        OperationResult result = DbInternal.search(
            cursor, keyEntry, null, dataEntry,
            search, LockMode.DEFAULT.toReadOptions());

        return (result != null ? ResetResult.FOUND : ResetResult.STOP);
    }

    /*
     * If the key points to an uninteresting (non-target) leaf table, attempt
     * to skip the table by resetting the cursor.  The reset is different
     * depending on direction of the scan.  For a forward scan the cursor
     * goes to the next record GTE <key> [TableIdString] [0].  For a reverse
     * scan it goes to the next record LT <key> [TableIdString].
     *
     * TODO: think about how to skip uninteresting non-leaf tables.  That can
     * be done only if it doesn't also skip a target table that might be a
     * child of a non-target.  Future optimization.
     */
    @SuppressWarnings("unused")
    private ResetResult resetCursorToTable(TableImpl table,
                                           DatabaseEntry keyEntry,
                                           DatabaseEntry dataEntry,
                                           Cursor cursor) {
        return ResetResult.SKIP;
    }

    protected void initTableLists(T op,
                                  OperationTableInfo tableInfo,
                                  Transaction txn,
                                  Direction scanDirection,
                                  byte[] resumeKey) {
        initTableLists(op, tableInfo, txn, resumeKey);
        tableInfo.direction = scanDirection;
    }

    /*
     * Initialize the table match list and ancestor list.
     */
    private void initTableLists(T op,
                                OperationTableInfo tableInfo,
                                Transaction txn,
                                byte[] resumeKey) {
        /*
         * Make sure the tables exist, create the ancestor list
         */
        initTables(op, tableInfo);
        initializeAncestorList(op, tableInfo, txn, resumeKey);
    }

    /**
     * Look at the table list and make sure they exist.  Set the
     * top-level table based on the first table in the list (the target).
     */
    private void initTables(T op, OperationTableInfo tableInfo) {
        boolean isFirst = true;
        for (long tableId : op.getTargetTables().getTargetAndChildIds()) {
            TableImpl table = getAndCheckTable(tableId);
            if (isFirst) {
                /*
                 * Set the top-level table
                 */
                tableInfo.topLevelTable = table.getTopLevelTable();
                isFirst = false;
            }
            /*
             * Calculate max and min key component length to assist
             * in key filtering.
             */
            int nkey = table.getNumKeyComponents();
            if (nkey > tableInfo.maxKeyComponents) {
                tableInfo.maxKeyComponents = nkey;
            }
            if (tableInfo.minKeyComponents > 0 &&
                nkey < tableInfo.minKeyComponents) {
                tableInfo.minKeyComponents = nkey;
            }
        }
    }

    private void initializeAncestorList(T op,
                                        OperationTableInfo tableInfo,
                                        Transaction txn,
                                        byte[] resumeKey) {
        tableInfo.ancestors = new AncestorList(
            operationHandler, txn, resumeKey,
            op.getTargetTables().getAncestorTableIds());
    }

    /**
     * AncestorList encapsulates a list of target ancestor tables for a table
     * operation.  An instance is constructed for each iteration execution and
     * reused on each iteration "hit" to add ancestor entries to results.
     */
    static class AncestorList {
        private final Set<AncestorListEntry> ancestors;
        private final OperationHandler operationHandler;
        private final Transaction txn;

        AncestorList(OperationHandler operationHandler,
                     Transaction txn,
                     byte[] resumeKey,
                     long[] ancestorTables) {
            this.operationHandler = operationHandler;
            this.txn = txn;
            if (ancestorTables.length > 0) {
                ancestors = new TreeSet<AncestorListEntry>
                    (new AncestorCompare());
                for (long tableId : ancestorTables) {
                    TableImpl table = operationHandler.getTable(tableId);
                    if (table == null) {
                        throw new MetadataNotFoundException
                            ("Cannot access ancestor table.  It may not " +
                             "exist, id: " + tableId);
                    }
                    ancestors.add(new AncestorListEntry(table, resumeKey));
                }
            } else {
                ancestors = null;
            }
        }

        /**
         * Adds ancestors to a list of rows returned from a table iteration
         * or multiGet operation.
         */
        int addAncestorValues(Database db,
                              List<ResultKeyValueVersion> results,
                              DatabaseEntry keyEntry) {
            int numAncestors = 0;
            if (ancestors != null) {
                final Cursor ancestorCursor =
                    db.openCursor(txn, CURSOR_READ_COMMITTED);
                try {
                    for (AncestorListEntry entry : ancestors) {
                        if (entry.setLastReturnedKey(keyEntry.getData())) {
                            DatabaseEntry ancestorKey =
                                new DatabaseEntry(entry.getLastReturnedKey());
                            DatabaseEntry ancestorValue = new DatabaseEntry();
                            OperationResult result = ancestorCursor.get(
                                ancestorKey,
                                ancestorValue,
                                Get.SEARCH,
                                LockMode.DEFAULT.toReadOptions());
                            if (result != null) {
                                numAncestors++;

                                addValueResult
                                    (operationHandler,
                                     results,
                                     ancestorCursor,
                                     ancestorKey,
                                     ancestorValue,
                                     result);
                            }
                        }
                    }
                } finally {
                    ancestorCursor.close();
                }
            }
            return numAncestors;
        }

        /**
         * Adds ancestors to a list of rows returned from an index row
         * iteration.
         */
        int addAncestorIndexValues(Database db,
                                   List<ResultIndexRows> results,
                                   DatabaseEntry keyEntry,
                                   byte[] indexKeyBytes) {

            int numAncestors = 0;
            if (ancestors != null) {
                final Cursor ancestorCursor =
                    db.openCursor(txn, CURSOR_READ_COMMITTED);
                try {
                    for (AncestorListEntry entry : ancestors) {
                        if (entry.setLastReturnedKey(keyEntry.getData())) {
                            DatabaseEntry ancestorKey =
                                new DatabaseEntry(entry.getLastReturnedKey());
                            DatabaseEntry ancestorValue = new DatabaseEntry();
                            OperationResult result = ancestorCursor.get(
                                ancestorKey,
                                ancestorValue,
                                Get.SEARCH,
                                LockMode.DEFAULT.toReadOptions());
                            if (result != null) {
                                numAncestors++;
                                final ResultValueVersion valVers =
                                    operationHandler.
                                    makeValueVersion(ancestorCursor,
                                                     ancestorValue,
                                                     result);

                                results.add(new ResultIndexRows
                                            (indexKeyBytes,
                                             ancestorKey.getData(),
                                             valVers.getValueBytes(),
                                             valVers.getVersion(),
                                             valVers.getExpirationTime()));
                            }
                        }
                    }
                } finally {
                    ancestorCursor.close();
                }
            }
            return numAncestors;
        }

        int addAncestorKeys(Database db,
                            List<ResultKey> results,
                            DatabaseEntry keyEntry) {
            int numAncestors = 0;
            if (ancestors != null) {
                final Cursor ancestorCursor =
                    db.openCursor(txn, CURSOR_READ_COMMITTED);
                try {
                    for (AncestorListEntry entry : ancestors) {
                        if (entry.setLastReturnedKey(keyEntry.getData())) {
                            /*
                             * Only return keys for records that actually exist vs a
                             * synthetic key.
                             */
                            DatabaseEntry ancestorKey =
                                new DatabaseEntry(entry.getLastReturnedKey());
                            final DatabaseEntry noDataEntry = new DatabaseEntry();
                            noDataEntry.setPartial(0, 0, true);
                            OperationResult result = ancestorCursor.get(
                                ancestorKey,
                                noDataEntry,
                                Get.SEARCH,
                                LockMode.DEFAULT.toReadOptions());
                            if (result != null) {
                                numAncestors++;
                                addKeyResult(results,
                                             ancestorKey.getData(),
                                             result.getExpirationTime());
                            }
                        }
                    }
                } finally {
                    ancestorCursor.close();
                }
            }
            return numAncestors;
        }

        int deleteAncestorKeys(Database db,
                               DatabaseEntry keyEntry) {
            int numAncestors = 0;
            if (ancestors != null) {
                final Cursor ancestorCursor =
                    db.openCursor(txn, CURSOR_READ_COMMITTED);
                try {
                    for (AncestorListEntry entry : ancestors) {
                        if (entry.setLastReturnedKey(keyEntry.getData())) {
                            /*
                             * Only return keys for records that actually exist vs a
                             * synthetic key.
                             */
                            DatabaseEntry ancestorKey =
                                new DatabaseEntry(entry.getLastReturnedKey());
                            final DatabaseEntry noDataEntry = new DatabaseEntry();
                            noDataEntry.setPartial(0, 0, true);
                            OperationResult result = ancestorCursor.get(
                                ancestorKey,
                                noDataEntry,
                                Get.SEARCH,
                                LockMode.DEFAULT.toReadOptions());
                            if (result != null) {
                                if (ancestorCursor.delete(null) != null) {
                                    numAncestors++;
                                    MigrationStreamHandle.get().
                                        addDelete(ancestorKey, ancestorCursor);
                                }
                            }
                        }
                    }
                } finally {
                    ancestorCursor.close();
                }
            }
            return numAncestors;
        }

        int addAncestorValues(DatabaseEntry keyEntry,
                              final List<ResultKeyValueVersion> results) {
            return addAncestorValues(getDatabase(keyEntry), results, keyEntry);
        }

        int addIndexAncestorValues(DatabaseEntry keyEntry,
                                   final List<ResultIndexRows> results,
                                   byte[] indexKeyBytes) {
            return addAncestorIndexValues(getDatabase(keyEntry), results, keyEntry,
                                          indexKeyBytes);
        }

        List<ResultKey> addAncestorKeys(DatabaseEntry keyEntry) {
            if (ancestors != null) {
                final List<ResultKey> list = new ArrayList<ResultKey>();
                final Database db = getDatabase(keyEntry);
                addAncestorKeys(db, list, keyEntry);
                return list;
            }
            return null;
        }

        protected Database getDatabase(DatabaseEntry keyEntry) {
            final PartitionId partitionId = operationHandler.
                getRepNode().getTopology().
                getPartitionId(keyEntry.getData());
            return operationHandler.getRepNode().getPartitionDB(partitionId);
        }

        /**
         * A class to keep a list of ancestor tables and the last key returned
         * for each.  Technically the last key returned is shared among all of
         * the ancestors but for speed of comparison each copies what it needs.
         */
        private static class AncestorListEntry {
            private final TableImpl table;
            private byte[] lastReturnedKey;

            AncestorListEntry(TableImpl table, byte[] key) {
                this.table = table;
                if (key != null) {
                    setLastReturnedKey(key);
                }
            }

            TableImpl getTable() {
                return table;
            }

            /**
             * Set the lastReturnedKey to this table's portion of the full key
             * passed.  Return true if the new key is different from the previous
             * key.  This information is used to trigger return of a new row from
             * this table.
             */
            boolean setLastReturnedKey(byte[] key) {
                byte[] oldKey = lastReturnedKey;
                lastReturnedKey = Key.getPrefixKey(key, table.getNumKeyComponents());
                if (!Arrays.equals(lastReturnedKey, oldKey)) {
                    return true;
                }
                return false;
            }

            byte[] getLastReturnedKey() {
                return lastReturnedKey;
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append(table.getFullName());
                sb.append(", key: ");
                if (lastReturnedKey != null) {
                    sb.append(com.sleepycat.je.tree.Key.getNoFormatString
                              (lastReturnedKey));
                } else {
                    sb.append("null");
                }
                return sb.toString();
            }
        }

        /**
         * An internal class to sort entries by length of primary key which
         * puts them in order of parent first.
         */
        private static class AncestorCompare
            implements Comparator<AncestorListEntry> {
            /**
             * Sort by key length.  Because the tables are in the same
             * hierarchy, sorting this way results in ancestors first.
             */
            @Override
            public int compare(AncestorListEntry a1,
                               AncestorListEntry a2) {
                return Integer.valueOf(a1.getTable().getNumKeyComponents())
                    .compareTo(Integer.valueOf
                               (a2.getTable().getNumKeyComponents()));
            }
        }
    }

    /**
     * A table access checker helps to check whether all tables in the target
     * table list are accessible by current user.
     */
    static class TargetTableAccessChecker extends TableAccessChecker {
        private final TargetTables targetTables;

        TargetTableAccessChecker(OperationHandler operationHandler,
                                 PrivilegedTableAccessor tableAccessor,
                                 TargetTables targetTables) {
            super(operationHandler, tableAccessor);
            this.targetTables = targetTables;
        }

        void checkAccess() throws FaultException, UnauthorizedException {
            for (long tableId : targetTables.getTargetAndChildIds()) {
                internalCheckAccess(tableId);
            }

            for (long tableId: targetTables.getAncestorTableIds()) {
                internalCheckAccess(tableId);
            }
        }

        private void internalCheckAccess(long tableId)
            throws FaultException, UnauthorizedException {

            final TableImpl table =
                operationHandler.getAndCheckTable(tableId);
            if (!internalCheckTableAccess(table)) {
                throw new UnauthorizedException (
                    "Insufficient access rights granted on table, id:" +
                     tableId);
            }
        }
    }

    @Override
    boolean hasSchemaAccessPrivileges() {
        if (ExecutionContext.getCurrent() == null) {
            return true;
        }

        /* Table operations do not access schema keyspace */
        return false;
    }

    @Override
    protected boolean isInternalRequestor() {
        if (ExecutionContext.getCurrent() == null) {
            return true;
        }

        /* Table operations do not access internal keyspace */
        return false;
    }

    @Override
    List<? extends KVStorePrivilege> getRequiredPrivileges(T op) {
        /*
         * Checks the basic privilege for authentication here, and leave the
         * keyspace checking and the table access checking in
         * {@code verifyTableAccess()}.
         */
        return SystemPrivilege.usrviewPrivList;
    }

    @Override
    /* Not used in MultiTableOperations, just to override the abstract method */
    List<? extends KVStorePrivilege> schemaAccessPrivileges() {
        throw new RuntimeException(
            "MultiTableOperation should not access schema keyspace");
    }

    @Override
    /* Not used in MultiTableOperations, just to override the abstract method */
    List<? extends KVStorePrivilege> generalAccessPrivileges() {
        throw new RuntimeException(
            "MultiTableOperation does not need general keyspace privileges");
    }
}
