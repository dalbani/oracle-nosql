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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.FaultException;
import oracle.kv.MetadataNotFoundException;
import oracle.kv.Key;
import oracle.kv.KeyRange;
import oracle.kv.UnauthorizedException;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TargetTables;
import oracle.kv.impl.rep.migration.MigrationStreamHandle;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.topo.PartitionId;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DbInternal;
import com.sleepycat.je.DbInternal.Search;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * This is an intermediate class that encapsulates a get or iteration
 * operation on multiple tables.
 */
public abstract class MultiTableOperation extends MultiKeyOperation {

    /* Lowest possible value for a serialized key character. */
    private static final byte MIN_VALUE_BYTE = ((byte) 1);

    /*
     * These are possible results from a cursor reset operation in a scan
     * visitor function.
     */
    private static enum ResetResult { FOUND, /* found a key */
                                      STOP,  /* stop iteration */
                                      SKIP}  /* skip/ignore */

    /*
     * Encapsulates the target table, child tables, ancestor tables.
     */
    private final TargetTables targetTables;

    /*
     * The remaining state is server-local and used for operation execution.
     */

    /*
     * The list of ancestor tables to return.  These are not used for key
     * matching.
     */
    private AncestorList ancestors;
    private TableImpl topLevelTable;
    private int maxKeyComponents;
    private int minKeyComponents;
    private Direction direction;

    /**
     * Construct a multi-get operation.
     */
    public MultiTableOperation(OpCode opCode,
                               byte[] parentKey,
                               TargetTables targetTables,
                               KeyRange subRange) {
        super(opCode, parentKey, subRange, Depth.PARENT_AND_DESCENDANTS);
        this.targetTables = targetTables;
        ancestors = null;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    MultiTableOperation(OpCode opCode, ObjectInput in, short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);

        targetTables = new TargetTables(in, serialVersion);

        ancestors = null;
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(ObjectOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);

        targetTables.writeFastExternal(out, serialVersion);
    }

    /**
     * Common code to add a key/value result.  This is shared by code that
     * iterates by primary key (TableIterate, MultiGetTable).
     */
    static void addValueResult(final OperationHandler operationHandler,
                               List<ResultKeyValueVersion> results,
                               Cursor cursor,
                               DatabaseEntry keyEntry,
                               DatabaseEntry dentry) {

        final ResultValueVersion valVers =
            operationHandler.makeValueVersion(cursor,
                                              dentry);
        results.add(new ResultKeyValueVersion
                    (keyEntry.getData(),
                     valVers.getValueBytes(),
                     valVers.getVersion()));
    }

    static void addKeyResult(List<byte[]> results,
                             byte[] keyBytes) {
        results.add(keyBytes);
    }

    /**
     * A wrapper on OperationHandler.scan to perform table-specific
     * initialization.
     */
    boolean iterateTable(final OperationHandler operationHandler,
                         Transaction txn,
                         PartitionId partitionId,
                         boolean majorPathComplete,
                         Direction scanDirection,
                         int batchSize,
                         byte[] resumeKey,
                         CursorConfig cursorConfig,
                         LockMode lockMode,
                         OperationHandler.ScanVisitor visitor) {

        return iterateTable(operationHandler, txn, partitionId, getParentKey(),
                            majorPathComplete, scanDirection, batchSize,
                            resumeKey, cursorConfig, lockMode, visitor);
    }

    /**
     * A wrapper on OperationHandler.scan to perform table-specific
     * initialization with a specified parent Key.
     */
    boolean iterateTable(final OperationHandler operationHandler,
                         Transaction txn,
                         PartitionId partitionId,
                         byte[] parentKey,
                         boolean majorPathComplete,
                         Direction scanDirection,
                         int batchSize,
                         byte[] resumeKey,
                         CursorConfig cursorConfig,
                         LockMode lockMode,
                         OperationHandler.ScanVisitor visitor) {

        initTableLists(operationHandler, txn, resumeKey);
        direction = scanDirection;

        /*
         * Scan only returns keys so that data fetch only happens for
         * matching records.  The ScanVisitor instance will perform the
         * necessary data fetch on matching records.  This avoids an
         * unnecessary data fetch for rows that are not in a target table.
         */
        return operationHandler.scan
            (txn, partitionId, parentKey, majorPathComplete,
             getSubRange(), getDepth(), true /*noData*/, resumeKey,
             batchSize, cursorConfig, lockMode, scanDirection, visitor);
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
    int keyInTargetTable(final OperationHandler operationHandler,
                         DatabaseEntry keyEntry,
                         DatabaseEntry dataEntry,
                         Cursor cursor) {
        while (true) {
            final int nComponents = Key.countComponents(keyEntry.getData());
            if (nComponents > maxKeyComponents) {

                /*
                 * This key is out of range of any target table.  Reset the
                 * cursor to the next potential key, based on component count.
                 */
                ResetResult status =
                    resetCursorToMax(keyEntry, dataEntry,
                                     cursor, maxKeyComponents);

                /*
                 * If the reset didn't find a key, stop the iteration.
                 */
                if (status != ResetResult.FOUND) {
                    return OperationHandler.ScanVisitor.STOP;
                }
                /* fall through */
            }

            /*
             * Find the table in the hierarchy that contains this key.
             */
            final byte[] keyBytes = keyEntry.getData();
            TableImpl target = topLevelTable.findTargetTable(keyBytes);

            if (target == null) {

                /*
                 * This should not be possible unless there is a non-table key
                 * in the btree.
                 */
                String msg = "Key is not in a table: "  +
                    Key.fromByteArray(keyBytes);
                operationHandler.getLogger().log(Level.INFO, msg);
                return 0;
            }

            /*
             * Don't use List.contains() because Table.equals() is expensive.
             * Do a simple name match.  This is also where the value format
             * is double-checked to eliminate non-table records.
             */
            for (long tableId : targetTables.getTargetAndChildIds()) {
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
                    return OperationHandler.ScanVisitor.STOP;
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
    boolean isTableData(byte[] data, TableImpl table) {
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
                        List<byte[]> results,
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
    protected void verifyTableAccess(OperationHandler operationHandler)
        throws UnauthorizedException {
        if (ExecutionContext.getCurrent() == null) {
            return;
        }

        checkKeyspacePermission();
        new TargetTableAccessChecker(operationHandler, this, targetTables).
            checkAccess();
    }

    /**
     * Checks if parent key accesses legal keyspace.  Parent key of legal
     * multi-table operations should not access internal or schema keyspace.
     */
    private void checkKeyspacePermission() throws UnauthorizedException {
        final byte[] parentKey = getParentKey();
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
    private ResetResult resetCursorToMax(DatabaseEntry keyEntry,
                                         DatabaseEntry dataEntry,
                                         Cursor cursor,
                                         int maxComponents) {
        byte[] bytes = keyEntry.getData();
        int newLen = Key.getPrefixKeySize(bytes, maxComponents);
        byte[] newBytes = new byte[newLen + 1];
        System.arraycopy(bytes, 0, newBytes, 0, newLen + 1);
        keyEntry.setData(newBytes);
        if (direction == Direction.FORWARD) {
            newBytes[newLen] = MIN_VALUE_BYTE;
        }

        /*
         * Reset the cursor.
         */
        Search search = (direction == Direction.REVERSE) ?
            Search.LT : Search.GTE;

        OperationStatus status = DbInternal.search(
            cursor, keyEntry, null, dataEntry, search, LockMode.DEFAULT);

        return (status == OperationStatus.SUCCESS ? ResetResult.FOUND :
                ResetResult.STOP);
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

    /*
     * Initialize the table match list and ancestor list.
     */
    private void initTableLists(OperationHandler operationHandler,
                                Transaction txn,
                                byte[] resumeKey) {
        /*
         * Make sure the tables exist, create the ancestor list
         */
        initTables(operationHandler);
        initializeAncestorList(operationHandler, txn, resumeKey);
    }

    /**
     * Look at the table list and make sure they exist.  Set the
     * top-level table based on the first table in the list (the target).
     */
    private void initTables(OperationHandler operationHandler) {
        boolean isFirst = true;
        for (long tableId : targetTables.getTargetAndChildIds()) {
            TableImpl table =
                TableOperationHandler.getAndCheckTable(operationHandler, tableId);

            if (isFirst) {
                /*
                 * Set the top-level table
                 */
                topLevelTable = table.getTopLevelTable();
                isFirst = false;
            }
            /*
             * Calculate max and min key component length to assist
             * in key filtering.
             */
            int nkey = table.getNumKeyComponents();
            if (nkey > maxKeyComponents) {
                maxKeyComponents = nkey;
            }
            if (minKeyComponents > 0 && nkey < minKeyComponents) {
                minKeyComponents = nkey;
            }
        }
    }

    private void initializeAncestorList(OperationHandler operationHandler,
                                        Transaction txn,
                                        byte[] resumeKey) {
        ancestors = new AncestorList(operationHandler, txn, resumeKey,
                                     targetTables.getAncestorTableIds());
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
                    TableImpl table = TableOperationHandler.getTable
                        (operationHandler, tableId);
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
                    db.openCursor(txn, OperationHandler.CURSOR_READ_COMMITTED);
                try {
                    for (AncestorListEntry entry : ancestors) {
                        if (entry.setLastReturnedKey(keyEntry.getData())) {
                            DatabaseEntry ancestorKey =
                                new DatabaseEntry(entry.getLastReturnedKey());
                            DatabaseEntry ancestorValue = new DatabaseEntry();
                            OperationStatus status =
                                ancestorCursor.getSearchKey(ancestorKey,
                                                            ancestorValue,
                                                            LockMode.DEFAULT);
                            if (status == OperationStatus.SUCCESS) {
                                numAncestors++;

                                addValueResult
                                    (operationHandler,
                                     results,
                                     ancestorCursor,
                                     ancestorKey,
                                     ancestorValue);
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
                    db.openCursor(txn, OperationHandler.CURSOR_READ_COMMITTED);
                try {
                    for (AncestorListEntry entry : ancestors) {
                        if (entry.setLastReturnedKey(keyEntry.getData())) {
                            DatabaseEntry ancestorKey =
                                new DatabaseEntry(entry.getLastReturnedKey());
                            DatabaseEntry ancestorValue = new DatabaseEntry();
                            OperationStatus status =
                                ancestorCursor.getSearchKey(ancestorKey,
                                                            ancestorValue,
                                                            LockMode.DEFAULT);
                            if (status == OperationStatus.SUCCESS) {
                                numAncestors++;
                                final ResultValueVersion valVers =
                                    operationHandler.
                                    makeValueVersion(ancestorCursor,
                                                     ancestorValue);

                                results.add(new ResultIndexRows
                                            (indexKeyBytes,
                                             ancestorKey.getData(),
                                             valVers.getValueBytes(),
                                             valVers.getVersion()));
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
                            List<byte[]> results,
                            DatabaseEntry keyEntry) {
            int numAncestors = 0;
            if (ancestors != null) {
                final Cursor ancestorCursor =
                    db.openCursor(txn, OperationHandler.CURSOR_READ_COMMITTED);
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
                            OperationStatus status =
                                ancestorCursor.getSearchKey(ancestorKey,
                                                            noDataEntry,
                                                            LockMode.DEFAULT);
                            if (status == OperationStatus.SUCCESS) {
                                numAncestors++;
                                MultiTableOperation.addKeyResult
                                    (results, ancestorKey.getData());
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
                    db.openCursor(txn, OperationHandler.CURSOR_READ_COMMITTED);
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
                            OperationStatus status =
                                ancestorCursor.getSearchKey(ancestorKey,
                                                            noDataEntry,
                                                            LockMode.DEFAULT);
                            if (status == OperationStatus.SUCCESS) {
                                if (ancestorCursor.delete() ==
                                    OperationStatus.SUCCESS) {
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

        List<byte[]> addAncestorKeys(DatabaseEntry keyEntry) {
            if (ancestors != null) {
                final List<byte[]> list = new ArrayList<byte[]>();
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
                TableOperationHandler.getAndCheckTable(operationHandler,
                                                       tableId);
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
    public List<? extends KVStorePrivilege> getRequiredPrivileges() {
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
