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

package oracle.kv.impl.api.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.kv.StatementResult;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.MapValueImpl;
import oracle.kv.query.BoundStatement;
import oracle.kv.query.ExecuteOptions;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.RecordDef;
import oracle.kv.table.RecordValue;

/**
 * Implementation of BoundStatement
 */
public class BoundStatementImpl
    implements BoundStatement,
               InternalStatement {

    private final PreparedStatementImpl preparedStatement;

    private final Map<String, FieldValue> bindVariables;

    BoundStatementImpl(PreparedStatementImpl preparedStatement) {
        this.preparedStatement = preparedStatement;
        bindVariables = new HashMap<String, FieldValue>();
    }

    PreparedStatementImpl getPreparedStmt() {
        return preparedStatement;
    }

    @Override
    public RecordDef getResultDef() {
        return preparedStatement.getResultDef();
    }

    @Override
    public Map<String, FieldDef> getVariableTypes() {
        return preparedStatement.getExternalVarsTypes();
    }

    @Override
    public FieldDef getVariableType(String name) {
        return preparedStatement.getExternalVarType(name);
    }

    @Override
    public Map<String, FieldValue> getVariables() {
        return bindVariables;
    }

    @Override
    public BoundStatement createBoundStatement() {
        return preparedStatement.createBoundStatement();
    }

    @Override
    public BoundStatement setVariable(String name, FieldValue value) {
        validate(name, value);
        bindVariables.put(name, value);
        return this;
    }

    @Override
    public BoundStatement setVariable(String name, int value) {
        FieldValue val = FieldDefImpl.integerDef.createInteger(value);
        setVariable(name, val);
        return this;
    }

    @Override
    public BoundStatement setVariable(String name, boolean value) {
        FieldValue val = FieldDefImpl.booleanDef.createBoolean(value);
        setVariable(name, val);
        return this;
    }

    @Override
    public BoundStatement setVariable(String name, double value) {
        FieldValue val = FieldDefImpl.doubleDef.createDouble(value);
        setVariable(name, val);
        return this;
    }

    @Override
    public BoundStatement setVariable(String name, float value) {
        FieldValue val = FieldDefImpl.floatDef.createFloat(value);
        setVariable(name, val);
        return this;
    }

    @Override
    public BoundStatement setVariable(String name, long value) {
        FieldValue val = FieldDefImpl.longDef.createLong(value);
        setVariable(name, val);
        return this;
    }

    @Override
    public BoundStatement setVariable(String name, String value) {
        FieldValue val = FieldDefImpl.stringDef.createString(value);
        setVariable(name, val);
        return this;
    }

    @Override
    public BoundStatement setVariable(String name, byte[] value) {
        FieldValue val = FieldDefImpl.binaryDef.createBinary(value);
        setVariable(name, val);
        return this;
    }


    private void validate(String varName, FieldValue value) {

        FieldDefImpl def = (FieldDefImpl)getVariableType(varName);

        if (def == null) {
            throw new IllegalArgumentException(
                "Variable " + varName + " has not been declared in the query");
        }

        if (!((FieldDefImpl)value.getDefinition()).isSubtype(def)) {
            throw new IllegalArgumentException(
                "Variable " + varName + " does not have an expected type. " +
                "Expected " + def.getType() + " or subtype, got " +
                value.getType());
        }

        checkRecordsContainAllFields(varName, value);
    }

    /*
     * Check if record values have all the fields defined in the type.
     */
    private void checkRecordsContainAllFields(
        String varName,
        FieldValue value) {

        if (value.isNull() ) {
            return;
        }

        FieldDef def = value.getDefinition();

        if (def.isRecord()) {
            RecordValue rec = value.asRecord();
            List<String> typeFieldNames = def.asRecord().getFields();

            /*
             * The various RecordValue.put() methods forbid adding a field
             * whose name or type does not comform to the record def.
             */
            assert(rec.size() <= typeFieldNames.size());

            for (String fname : typeFieldNames) {

                FieldValue fval = rec.get(fname);

                if (fval == null) {
                    throw new IllegalArgumentException(
                        "Value for variable " + varName +
                            " not conforming to type definition: there is no" +
                            " value for field: '" + fname + "'.");
                }

                checkRecordsContainAllFields(varName, fval);
            }
        } else if (def.isArray()) {
            for (FieldValue v : value.asArray().toList()) {
                checkRecordsContainAllFields(varName, v);
            }
        } else if (def.isMap()) {
            for (FieldValue v :
                ((MapValueImpl)value.asMap()).getFieldsInternal().values()) {
                checkRecordsContainAllFields(varName, v);
            }
        }
    }

    @Override
    public StatementResult executeSync(
        KVStoreImpl store,
        ExecuteOptions options) {
        return new QueryStatementResultImpl(store.getTableAPIImpl(), options, this);
    }
}
