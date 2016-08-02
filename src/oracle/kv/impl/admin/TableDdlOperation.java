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
package oracle.kv.impl.admin;

import java.util.ArrayList;
import java.util.List;

import oracle.kv.impl.admin.DdlHandler.DdlOperation;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.OperationContext;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.security.AccessCheckUtils.TableContext;
import oracle.kv.table.Index;
import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.api.table.IndexImpl.AnnotatedField;
import oracle.kv.table.Table;

/**
 * This class represents the table Ddl operations and their execution.
 * TableDDLOperations are generated after parsing a table ddl statement.
 */
public abstract class TableDdlOperation  implements DdlOperation {

    private final String opName;
    private final TableImpl table;

    TableDdlOperation(String opName, TableImpl table) {
        this.opName = opName;
        this.table = table;
    }

    TableImpl getTable() {
        return table;
    }

    String opName() {
        return opName;
    }

    /**
     * Operation of creating table, needing CREATE_ANY_TABLE privilege.
     */
    public static class CreateTable extends TableDdlOperation {
        private final boolean ifNotExists;

        public CreateTable(TableImpl table, boolean ifNotExists) {
            super("CREATE TABLE", table);
            this.ifNotExists = ifNotExists;
        }

        @Override
        public OperationContext getOperationCtx() {
            return new OperationContext() {
                @Override
                public String describe() {
                    return opName() + (ifNotExists ? " IF NOT EXISTS " : " ") +
                           getTable().getFullName();
                }

                @Override
                public List<? extends KVStorePrivilege>
                    getRequiredPrivileges() {
                        return SystemPrivilege.tableCreatePrivList;
                }
            };
        }

        @Override
        public void perform(DdlHandler ddlHandler) {
            final TableImpl table = getTable();
            try {
                final Admin admin = ddlHandler.getAdmin();

                /* tableExistsAndEqual will throw if it exists and not equal */
                if (ifNotExists && tableExistsAndEqual(admin)) {
                    ddlHandler.operationSucceeds();
                    return;
                }
                
                final int planId = admin.getPlanner().createAddTablePlan
                    ("CreateTable", table.getName(),
                     table.getParentName(),
                     table.getFieldMap(),
                     table.getPrimaryKey(),
                     table.getShardKey(),
                     false, 0, table.getDescription());
                ddlHandler.approveAndExecute(planId);
            } catch (IllegalCommandException ice) {
                ddlHandler.operationFails(opName() + " failed for table " +
                       table.getFullName() + ": " + ice.getMessage());
            }
        }

        /**
         * Returns true if the table exists in the current metadata and is the
         * same as the table parameter.
         *
         * If the named table does not exist return false
         * If the named table exists and the definitions match, return true.
         * If the named table exists and the definitions don't match, throw
         * an exception, handled by the caller.
         */
        private boolean tableExistsAndEqual(Admin admin) {
            final TableImpl table = getTable();
            TableMetadata metadata =
                admin.getMetadata(TableMetadata.class,
                                  MetadataType.TABLE);
            if (metadata != null) {
                TableImpl existing = metadata.getTable(table.getFullName());
                if (existing != null) {
                    if (existing.fieldsEqual(table)) {
                        return true;
                    }
                    throw new IllegalCommandException
                        ("Table exists but definitions do not match");
                }
            }
            return false;
        }
    }

    /**
     * Operation of evolving table, needing EVOLVE_TABLE privilege.
     */
    public static class EvolveTable extends TableDdlOperation {
        private final TableContext opCtx;

        public EvolveTable(TableImpl table) {
            super("EVOLVE TABLE", table);
            opCtx = new TableContext(opName(), table,
                new TablePrivilege.EvolveTable(table.getId()));
        }

        @Override
        public OperationContext getOperationCtx() {
            return opCtx;
        }

        @Override
        public void perform(DdlHandler ddlHandler) {
            final TableImpl table = getTable();
            try {
                /*
                 * When TableDdl evolved the table it incremented the
                 * table version, which means that the table version is
                 * one more than it should be.  Subtract that one to
                 * reflect the fact that the same evolution will happen again.
                 */
                int tableVersion = table.getTableVersion() - 1;
                assert(tableVersion > 0);
                final int planId =
                    ddlHandler.getAdmin().getPlanner().createEvolveTablePlan
                        ("AlterTable", table.getFullName(),
                         tableVersion, table.getFieldMap());
                ddlHandler.approveAndExecute(planId);
            } catch (IllegalCommandException ice) {
                ddlHandler.operationFails("ALTER TABLE failed for table " +
                       table.getFullName() + ": " + ice.getMessage());
            }
        }
    }
    
    /**
     * Operation of dropping table, needing DROP_ANY_TABLE privilege and
     * DROP_INDEX (if to remove data) for non-owners.
     */
    public static class DropTable extends TableDdlOperation {
        private final OperationContext opCtx;
        private final boolean ifExists;
        private final boolean removeData;
        private final String tableName;

        public DropTable(String tableName,
                         TableImpl tableIfExists,
                         boolean ifExists,
                         boolean removeData) {
            super("DROP TABLE", tableIfExists);

            this.ifExists = ifExists;
            this.removeData = removeData;
            this.tableName = tableName;

            if (tableIfExists == null) {
                opCtx = new NoTableOpContext(tableName);
            } else {
                final List<KVStorePrivilege> privsToCheck =
                    new ArrayList<KVStorePrivilege>();
                if (removeData && !tableIfExists.getIndexes().isEmpty()) {
                    privsToCheck.add(
                        new TablePrivilege.DropIndex(tableIfExists.getId()));
                }
                privsToCheck.add(SystemPrivilege.DROP_ANY_TABLE);
                opCtx = new TableContext(opName(), tableIfExists,
                                         privsToCheck);
            }
        }

        @Override
        public void perform(DdlHandler ddlHandler) {
            final Admin admin = ddlHandler.getAdmin();
            if (ifExists && getTable() == null) {
                ddlHandler.operationSucceeds();
                return;
            }
            try {
                final int planId = admin.getPlanner().createRemoveTablePlan
                    ("DropTable", tableName, removeData);
                ddlHandler.approveAndExecute(planId);
            } catch (IllegalCommandException ice) {
                ddlHandler.operationFails(opName() + " failed for table " +
                    tableName + ": " + ice.getMessage());
            }
        }

        @Override
        public OperationContext getOperationCtx() {
            return opCtx;
        }
    }

    /**
     * Operation of creating index, needing CREATE_INDEX privilege.
     */
    public static class CreateIndex extends TableDdlOperation {
        private final OperationContext opCtx;
        private final boolean ifNotExists;
        private final String indexName;
        private final String tableName;
        private final String[] newFields;
        private final String indexComments;

        public CreateIndex(TableImpl tableIfExists,
                           String tableName,
                           String indexName,
                           String[] newFields,
                           String indexComments,
                           boolean ifNotExists) {
            super("CREATE INDEX" + (ifNotExists ? " IF NOT EXISTS" : "") ,
                  tableIfExists);

            this.ifNotExists = ifNotExists;
            this.indexName = indexName;
            this.tableName = tableName;
            this.newFields = newFields;
            this.indexComments = indexComments;

            if (tableIfExists == null) {
                opCtx = new NoTableOpContext(tableName);
            } else {
                opCtx = new TableContext(
                    opName(), tableIfExists,
                    new TablePrivilege.CreateIndex(tableIfExists.getId()));
            }
        }

        @Override
        public OperationContext getOperationCtx() {
            return opCtx;
        }

        /**
         * Returns true if the index in the current tableDdl instance exists
         * and is equal to the fields of the current tableDdl.
         */
        private boolean indexExistsAndEqual(Admin admin) {
            TableMetadata metadata =
                admin.getMetadata(TableMetadata.class,
                                  MetadataType.TABLE);
            if (metadata != null) {
                Index index = metadata.getIndex(tableName, indexName);
                if (index != null) {
                    List<String> fields = index.getFields();
                    if (newFields.length == fields.size()) {
                        for (int i = 0; i < newFields.length; i++) {
                            if (!newFields[i].equalsIgnoreCase(fields.get(i))) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void perform(DdlHandler ddlHandler) {
            final Admin admin = ddlHandler.getAdmin();
            if (ifNotExists && indexExistsAndEqual(admin)) {
                ddlHandler.operationSucceeds();
                return;
            }
            try {
                final int planId = admin.getPlanner().createAddIndexPlan
                    ("CreateIndex", indexName, tableName, newFields,
                     indexComments);
                ddlHandler.approveAndExecute(planId);
            } catch (IllegalCommandException ice) {
                ddlHandler.operationFails("CREATE INDEX failed for table " +
                    tableName + ", index " + indexName + ": " +
                    ice.getMessage());

            }
        }
    }

    /**
     * Operation of creating fulltext index, needing CREATE_INDEX privilege.
     * TBD: consider merging with CreateIndex.
     */
    public static class CreateTextIndex extends TableDdlOperation {
        private final OperationContext opCtx;
        private final boolean ifNotExists;
        private final String indexName;
        private final String tableName;
        private AnnotatedField[] newFields;
        private final String indexComments;

        public CreateTextIndex(TableImpl tableIfExists,
                           String tableName,
                           String indexName,
                           AnnotatedField[] newFields,
                           String indexComments,
                           boolean ifNotExists) {
            super("CREATE FULLTEXT INDEX" +
                  (ifNotExists ? " IF NOT EXISTS" : "") ,
                  tableIfExists);

            this.ifNotExists = ifNotExists;
            this.indexName = indexName;
            this.tableName = tableName;
            this.newFields = newFields;
            this.indexComments = indexComments;

            if (tableIfExists == null) {
                opCtx = new NoTableOpContext(tableName);
            } else {
                opCtx = new TableContext(
                    opName(), tableIfExists,
                    new TablePrivilege.CreateIndex(tableIfExists.getId()));
            }
        }

        @Override
        public OperationContext getOperationCtx() {
            return opCtx;
        }

        /**
         * Returns true if the index in the current tableDdl instance exists
         * and is equal to the fields of the current tableDdl.
         */
        private boolean indexExistsAndEqual(Admin admin) {
            TableMetadata metadata =
                admin.getMetadata(TableMetadata.class,
                                  MetadataType.TABLE);
            if (metadata != null) {
                IndexImpl index = (IndexImpl)metadata.getIndex(tableName,
                                                               indexName);
                if (index != null) {
                    List<AnnotatedField> fields =
                        index.getFieldsWithAnnotations();
                    if (newFields.length == fields.size()) {
                        for (int i = 0; i < newFields.length; i++) {
                            if (!newFields[i].equals(fields.get(i))) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void perform(DdlHandler ddlHandler) {
            final Admin admin = ddlHandler.getAdmin();
            if (ifNotExists && indexExistsAndEqual(admin)) {
                ddlHandler.operationSucceeds();
                return;
            }
            try {
                final int planId = admin.getPlanner().createAddTextIndexPlan
                    ("CreateIndex", indexName, tableName, newFields,
                     indexComments);
                ddlHandler.approveAndExecute(planId);
            } catch (IllegalCommandException ice) {
                ddlHandler.operationFails
                    ("CREATE FULLTEXT INDEX failed for table " +
                    tableName + ", index " + indexName + ": " +
                    ice.getMessage());

            }
        }
    }

    /**
     * Operation of dropping index, needing DROP_INDEX privilege.
     */
    public static class DropIndex extends TableDdlOperation {
        private final OperationContext opCtx;
        private final boolean ifExists;
        private final String indexName;
        private final String tableName;

        public DropIndex(String tableName,
                         TableImpl tableIfExists,
                         String indexName,
                         boolean ifExists) {

            super("DROP INDEX", tableIfExists);
            this.ifExists = ifExists;
            this.indexName = indexName;
            this.tableName = tableName;

            if (tableIfExists == null) {
                opCtx = new NoTableOpContext(tableName);
            } else {
                opCtx = new TableContext(opName(), tableIfExists,
                    new TablePrivilege.DropIndex(tableIfExists.getId()));
            }
        }

        @Override
        public OperationContext getOperationCtx() {
            return opCtx;
        }

        @Override
        public void perform(DdlHandler ddlHandler) {
            final Admin admin = ddlHandler.getAdmin();

            if (ifExists && !indexExists(admin)) {
                ddlHandler.operationSucceeds();
                return;
            }
            try {
                final int planId = admin.getPlanner().createRemoveIndexPlan
                    ("DropIndex", indexName, tableName);
                ddlHandler.approveAndExecute(planId);
            } catch (IllegalCommandException ice) {
                ddlHandler.operationFails(
                    "DROP INDEX failed, index does not exist: " + tableName +
                    ":" + indexName);
            }
        }

        /**
         * Returns true if the named index exists in the current metadata
         */
        private boolean indexExists(Admin admin) {
            if (getTable() == null) {
                return false;
            }
            TableMetadata metadata =
                admin.getMetadata(TableMetadata.class,
                                  MetadataType.TABLE);
            return (metadata != null &&
                    metadata.getIndex(tableName, indexName) != null);
        }
    }


    /**
     * TODO for show and describe:
     *  o implement tabular output for tables and fields.  This may involve
     *  a new table formatter class or two
     *
     *  o implement index output (both types)
     *  o all indexes?
     *
     * Much of this display formatting should be modularized into formatting
     * interfaces and moved to other locations and perhaps be considered for
     * the public API (e.g. TableFormatter, IndexFormatter, etc).
     */

    /**
     * Operation of showing table or index, needing DBVIEW privilege.
     */
    public static class ShowTableOrIndex extends TableDdlOperation {

        private final String tableName;
        private final boolean isShowTables;
        private final boolean showIndexes;
        private final boolean asJson;
        
        public ShowTableOrIndex(String tableName,
                                boolean isShowTables,
                                boolean showIndexes,
                                boolean asJson) {
            super("ShowTableOrIndex", null /* no need table */);
            this.tableName = tableName;
            this.isShowTables = isShowTables;
            this.showIndexes = showIndexes;
            this.asJson = asJson;
        }

        @Override
        public void perform(DdlHandler ddlHandler) {
            final Admin admin = ddlHandler.getAdmin();

            String resultString = null;
            TableMetadata metadata =
                admin.getMetadata(TableMetadata.class,
                                  MetadataType.TABLE);

            /* show tables */
            if (isShowTables) {
                if (metadata == null) {
                    resultString = "";
                } else {
                    resultString = formatList("tables",
                                              metadata.listTables(),
                                              asJson);
                    ddlHandler.operationSucceeds();
                }
            } else {
                /* table or index */
                TableImpl table = null;
                if (metadata != null) {
                    table = metadata.getTable(tableName);
                }
                if (table == null) {
                    ddlHandler.operationFails(
                        "Table does not exist: " + tableName);
                    return;
                }

                ddlHandler.operationSucceeds();
                if (showIndexes) {
                    resultString = formatList
                        ("indexes",
                         new ArrayList<String>(table.getIndexes().keySet()),
                         asJson);
                } else {
                    resultString = formatTableNames(table);
                }
            }
            ddlHandler.setResultString(resultString);
        }

        private String formatTableNames(TableImpl table) {
            Table current = table;
            while (current.getParent() != null) {
                current = current.getParent();
            }
            List<String> tableNames = new ArrayList<String>();
            listTableHierarchy(current, tableNames);
            return formatList("tableHierarchy" , tableNames, asJson);
        }

        private void listTableHierarchy(Table table, List<String> tableNames) {
            tableNames.add(table.getFullName());
            for (Table t : table.getChildTables().values()) {
                listTableHierarchy(t, tableNames);
            }
        }

        /**
         * Formats the list of tables.  If asJson is true a JSON output format
         * is used, otherwise it is a CRLF separated string of names.
         * JSON:  {"tables" : ["t1", "t2", ..., "tN"]}
         */
        private static String formatList(String listName, List<String> list,
                                         boolean asJson) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            if (asJson) {
                sb.append("{\"");
                sb.append(listName);
                sb.append("\" : [");
                for (String s : list) {
                    if (!first) {
                        sb.append(","); 
                    }
                    first = false;
                    sb.append("\"");
                    sb.append(s);
                    sb.append("\"");
                }
                sb.append("]}");
            } else {
                sb.append(listName);
                for (String s : list) {
                    /*
                     * Indent list members by 2 spaces.
                     */
                    sb.append("\n  ");
                    sb.append(s);
                }
            }
            return sb.toString();
        }

        @Override
        public OperationContext getOperationCtx() {
            return new OperationContext() {
                @Override
                public String describe() {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("SHOW");
                    if (asJson) {
                        sb.append(" AS JSON");
                    }
                    if (isShowTables) {
                        sb.append(" TABLES");
                    } else if (showIndexes) {
                        sb.append( " INDEXES ON ");
                        sb.append(tableName);
                    } else {
                        sb.append(" TABLE ");
                        sb.append(tableName);
                    }
                    return sb.toString();
                }
                @Override
                public List<? extends KVStorePrivilege>
                    getRequiredPrivileges() {
                        return SystemPrivilege.dbviewPrivList;
                }
            };
        }
    }

    /**
     * Operation of describing table, needing DBVIEW privilege.
     */
    public static class DescribeTable extends TableDdlOperation {
        private final String tableName;
        private final String indexName;
        private final boolean asJson;
        private final String[] fields;

        public DescribeTable(String tableName,
                             String indexName,
                             String[] fields,
                             boolean asJson) {
            super("DESCRIBE TABLE", null /* table */);

            assert tableName != null;
            this.asJson = asJson;
            this.tableName = tableName;
            this.indexName = indexName;
            this.fields = fields;
        }

        /**
         * TODO:
         *  o implement tabular output for tables and fields.  This may involve
         *  a new table formatter class or two
         *
         * Much of this display formatting should be modularized into formatting
         * interfaces and moved to other locations and perhaps be considered for
         * the public API (e.g. TableFormatter, IndexFormatter, etc).
         */

        @Override
        public void perform(DdlHandler ddlHandler) {
            final Admin admin = ddlHandler.getAdmin();
            String resultString = null;
            TableMetadata metadata =
                admin.getMetadata(TableMetadata.class,
                                  MetadataType.TABLE);

            /* table or index */
            TableImpl table = null;
            if (metadata != null) {
                table = metadata.getTable(tableName);
            }
            if (table == null) {
                ddlHandler.operationFails("Table does not exist: " + tableName);
                return;
            }
            if (indexName != null) {
                Index index = table.getIndex(indexName);
                if (index == null) {
                    ddlHandler.operationFails(
                        "Index does not exist: " + indexName + ", on table " +
                        tableName);
                    return;
                }
                resultString = formatIndex(index, asJson);
            } else {

                /*
                 * formatTable can throw IAE.
                 */
                try {
                    resultString = formatTable(table, ddlHandler, asJson);
                    if (resultString == null) {
                        return;
                    }
                } catch (IllegalArgumentException iae) {
                    ddlHandler.operationFails(iae.getMessage());
                    return;
                }
            }
            ddlHandler.setResultString(resultString);
        }

        /**
         * TODO: handle non-JSON output and nested field lists (see TableImpl).
         */
        private String formatTable(TableImpl table,
                                   DdlHandler ddlHandler,
                                   boolean asJson1) {
            String retVal = table.formatTable(asJson1, fields);
            if (retVal == null) {
                ddlHandler.operationFails(
                    "DESCRIBE TABLE without 'AS JSON' not yet implemented");
            }
            return retVal;
        }

        private String formatIndex(Index index, boolean asJson1) {
            return ShowTableOrIndex.formatList(index.getName(),
                                               index.getFields(),
                                               asJson1);
        }

        @Override
        public OperationContext getOperationCtx() {
            return new OperationContext() {
                @Override
                public String describe() {
                    return opName() + ": " + tableName;
                }
                @Override
                public List<? extends KVStorePrivilege>
                    getRequiredPrivileges() {
                        return SystemPrivilege.dbviewPrivList;
                }
            };
        }
    }

    /**
     * A special context for the case where the table does not exist, but we
     * need to do some check first, e.g., in the case with "IF EXISTS" option.
     */
    private static final class NoTableOpContext
        implements OperationContext {
        private final String phantomTable;
        NoTableOpContext(String phantomTable) {
            this.phantomTable = phantomTable;
        }
        @Override
        public String describe() {
            return "Operation on an non-existing table: " + phantomTable;
        }
        @Override
        public List<? extends KVStorePrivilege>
            getRequiredPrivileges() {
                return SystemPrivilege.dbviewPrivList;
        }
    }
}
