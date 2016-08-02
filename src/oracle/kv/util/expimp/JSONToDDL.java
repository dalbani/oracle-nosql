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

package oracle.kv.util.expimp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.api.table.DDLGenerator;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableJsonUtils;
import oracle.kv.table.FieldDef;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;

/**
 * Utility class used to convert table definitions and index definitions in
 * Json format to Table/Index DDLs.
 */
public class JSONToDDL {

    private final TableAPI tableAPI;
    private AbstractStoreImport storeImport;

    public JSONToDDL(TableAPI tableAPI,
                     AbstractStoreImport storeImport) {

        this.tableAPI = tableAPI;
        this.storeImport = storeImport;
    }

    /**
     * Returns the Table DDLs and Index DDLs
     *
     * @param jsonSchema The table schema definition in json format. It also
     *        contains all the table index definitions.
     * @param tableName
     * @return List of table and its index DDLs. First entry in the list is the
     *         table DDL followed by the index DDLs.
     */
    public List<String> getTableDDLs(String jsonSchema, String tableName) {

        List<String> tableDdls = new ArrayList<String>();

        /*
         * Get the parent table name of this table.
         */
        String pTableName = getParentTableName(tableName);
        storeImport.addTableParent(tableName, pTableName);

        TableImpl pTable = null;
        TableImpl tabImpl = null;

        if (pTableName == null) {

            try {
                tabImpl = TableJsonUtils.fromJsonString(jsonSchema, null);
                storeImport.putTableWriterSchema(tableName,
                    tabImpl.getAvroSchema(false));
            } catch (IllegalCommandException ice) {
                String message = "Unable to resolve table " + tableName +
                    " using the given tableJsonSchema";
                storeImport.logMessage(message, Level.WARNING);
                return null;
            }
        } else {

            /*
             * Check if parent table was already loaded during this import
             */
            pTable = storeImport.getTableImpl(pTableName);

            /*
             * If the parent table was not loaded during this import, check if
             * the table is already present in the kvstore before performing
             * import
             */
            if (pTable == null) {
                pTable = (TableImpl)tableAPI.getTable(pTableName);
            }

            if (pTable != null) {
                try {
                    tabImpl = TableJsonUtils.fromJsonString(jsonSchema, pTable);
                    storeImport.putTableWriterSchema(tableName,
                        tabImpl.getAvroSchema(false));
                } catch (IllegalCommandException ice) {
                    String message = "Unable to resolve the child table " +
                        tableName + " using the given json schema. The " +
                        "parent table keys might be missing in the json " +
                        "schema definition.";
                    storeImport.logMessage(message, Level.WARNING);
                    return null;
                }
            } else {
                String message = "Cannot load child table " + tableName +
                    " before loading the parent tables";
                storeImport.logMessage(message, Level.WARNING);
                return null;
            }
        }

        TableImpl tableImpl = (TableImpl)tableAPI.getTable(tableName);

        /*
         * Check if the table is already present in the target kvstore
         */
        if (tableImpl != null) {

            storeImport.addTableMap(tableName, tableImpl);
            compareKeySchemas(tabImpl, tableImpl);
            return null;
        }

        DDLGenerator ddlGenerator = new DDLGenerator(tabImpl);

        tableDdls.add(ddlGenerator.getDDL());
        tableDdls.addAll(ddlGenerator.getAllIndexDDL());

        return tableDdls;
    }

    public void compareKeySchemas(Table table1, Table table2) {

        List<String> pKeys1 = table1.getPrimaryKey();
        List<String> pKeys2 = table2.getPrimaryKey();

        if (pKeys1.size() != pKeys2.size()) {
            storeImport.addKeyMismatchTable(table1.getFullName());
            return;
        }

        for (int i = 0; i < pKeys1.size(); i++) {
            String fieldName1 = pKeys1.get(i);
            FieldDef fieldDef1 = table1.getField(fieldName1);

            String fieldName2 = pKeys2.get(i);
            FieldDef fieldDef2 = table2.getField(fieldName2);

            if (!fieldDef1.getType().equals(fieldDef2.getType())) {
                storeImport.addKeyMismatchTable(table1.getFullName());
                return;
            }
        }
    }

    /*
     * Given the child table full name, return the parent table name.
     *
     * Example: If child table full name is ABC.DEF.GHI, the method returns
     * ABC.DEF
     */
    String getParentTableName(String tableName) {

        int index = tableName.lastIndexOf(".");

        /*
         * If the table has no parent table, return null
         */
        if (index == -1) {
            return null;
        }

        return tableName.substring(0, index);
    }
}
