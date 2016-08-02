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

import java.util.Collections;
import java.util.List;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Transaction;

import oracle.kv.FaultException;
import oracle.kv.MetadataNotFoundException;
import oracle.kv.UnauthorizedException;
import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.ops.InternalOperationHandler.PrivilegedTableAccessor;
import oracle.kv.impl.api.ops.MultiTableOperationHandler.TargetTableAccessChecker;
import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.table.Index;
import oracle.kv.table.Table;

/**
 * Base server handler for subclasses of {@link IndexOperation}.
 */
public abstract class IndexOperationHandler<T extends IndexOperation>
        extends InternalOperationHandler<T>
    implements PrivilegedTableAccessor {

    IndexOperationHandler(OperationHandler handler,
                          OpCode opCode,
                          Class<T> operationType) {
        super(handler, opCode, operationType);
    }

    public IndexScanner getIndexScanner(T op,
                                        Transaction txn,
                                        CursorConfig cursorConfig,
                                        LockMode lockMode,
                                        boolean keyOnly) {
        final String tableName = getTableName(op);
        return new IndexScanner(txn,
                                getSecondaryDatabase(op, tableName),
                                getIndex(op, tableName),
                                op.getIndexRange(),
                                op.getResumeSecondaryKey(),
                                op.getResumePrimaryKey(),
                                cursorConfig,
                                lockMode,
                                keyOnly);
    }

    String getTableName(T op) {
        long id = op.getTargetTables().getTargetTableId();
        Table table = getRepNode().getTable(id);
        if (table == null) {
            throw new MetadataNotFoundException
                ("Cannot access table.  It may not exist, id: " + id);
        }
        return table.getFullName();
    }

    IndexImpl getIndex(T op, String tableName) {
        final Index index =
            getRepNode().getIndex(op.getIndexName(), tableName);
        if (index == null) {
            throw new MetadataNotFoundException
                ("Cannot find index " + op.getIndexName() + " in table "
                 + tableName);
        }
        return (IndexImpl) index;
    }

    SecondaryDatabase getSecondaryDatabase(T op, String tableName) {
        final SecondaryDatabase db =
            getRepNode().getIndexDB(op.getIndexName(), tableName);
        if (db == null) {
            throw new MetadataNotFoundException("Cannot find index database: " +
                                                op.getIndexName() + ", " +
                                                tableName);
        }
        return db;
    }

    public void verifyTableAccess(T op)
        throws UnauthorizedException, FaultException {

        if (ExecutionContext.getCurrent() == null) {
            return;
        }

        new TargetTableAccessChecker(operationHandler, this,
                                     op.getTargetTables())
            .checkAccess();
    }

    @Override
    List<? extends KVStorePrivilege> getRequiredPrivileges(T op) {
        /*
         * Checks the basic privilege for authentication here, and leave the
         * the table access checking in {@code verifyTableAccess()}.
         */
        return SystemPrivilege.usrviewPrivList;
    }

    @Override
    public List<? extends KVStorePrivilege>
        tableAccessPrivileges(long tableId) {
        return Collections.singletonList(
            new TablePrivilege.ReadTable(tableId));
    }
}
