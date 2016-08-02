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

package oracle.kv.impl.api.table;

import java.util.List;
import java.util.Map;

import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * TupleValue instances are created internally only during query execution
 * and are never returned to the application.
 *
 * See the "Records vs tuples" section in the javadoc of PlanIter (in
 * impl/query/runtime/PlanIter.java) for more details.
 *
 * This class could have been placed in the query.runtime package. However
 * doing so would require making many of the package-scoped methds of
 * FieldValueImpl public or protected methods. This is the reason we chose
 * to put TupleValue in the api.table package.
 */
public class TupleValue extends FieldValueImpl {

    private static final long serialVersionUID = 1L;

    final FieldValueImpl[] theRegisters;

    final int[] theTupleRegs;

    final RecordDefImpl theDef;

    public TupleValue(RecordDefImpl def, FieldValueImpl[] regs, int[] regIds) {
        super();
        theRegisters = regs;
        theTupleRegs = regIds;
        theDef = def;
    }

    /*
     * Public api methods from Object and FieldValue
     */

    @Override
    public FieldValueImpl clone() {
        throw new IllegalStateException(
            "TupleValue does not implement clone");
    }

    @Override
    public String toString() {
        return toJsonString(false);
    }

    @Override
    public int hashCode() {
        int code = size();
        List<String> fieldNames = getFieldNames();
        for (String name : fieldNames) {
            code += name.hashCode();
        }
        for (int i = 0; i < size(); ++i) {
            code += getFieldValue(i).hashCode();
        }
        return code;
    }

    @Override
    public boolean equals(Object otherObj) {

        if (this == otherObj) {
            return true;
        }

        if (otherObj instanceof RecordValueImpl) {
            RecordValueImpl other = (RecordValueImpl) otherObj;

            /* field-by-field comparison */
            if (size() == other.size() &&
                getDefinition().equals(other.getDefinition())) {

                for (Map.Entry<String, FieldValue> entry :
                     other.valueMap.entrySet()) {

                    FieldValue val1 = entry.getValue();
                    FieldValue val2 = getFieldValue(entry.getKey());
                    if (!val1.equals(val2)) {
                        return false;
                    }
                }

                return true;
            }

            return false;
        }

        if (otherObj instanceof TupleValue) {
            TupleValue other = (TupleValue) otherObj;

            if (size() == other.size() &&
                getDefinition().equals(other.getDefinition())) {

                for (int i = 0; i < size(); ++i) {
                    FieldValue val1 = getFieldValue(i);
                    FieldValue val2 = other.getFieldValue(i);
                    if (!val1.equals(val2)) {
                        return false;
                    }
                }

                return true;
            }

            return false;
        }

        return false;
    }

    @Override
    public int compareTo(FieldValue o) {
        throw new IllegalStateException(
            "TupleValue is not comparable to any other value");
    }

    @Override
    public FieldDef.Type getType() {
        return FieldDef.Type.RECORD;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public RecordDefImpl getDefinition() {
        return theDef;
    }

    /*
     * FieldValueImpl internal api methods
     */

    @Override
    public boolean isTuple() {
        return true;
    }

    @Override
    public FieldValueImpl getFieldValue(String fieldName) {
        int fieldPos = getDefinition().getFieldPos(fieldName);
        return theRegisters[theTupleRegs[fieldPos]];
    }

    @Override
    public JsonNode toJsonNode() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        for (int i = 0; i < theTupleRegs.length; ++i) {
            FieldValueImpl val = getFieldValue(i);
            assert(val != null);
            node.put(getFieldName(i), val.toJsonNode());
        }
        return node;
    }

    @Override
    public void toStringBuilder(StringBuilder sb) {
        sb.append('{');
        for (int i = 0; i < theTupleRegs.length; ++i) {
            FieldValueImpl val = getFieldValue(i);
            assert(val != null);
            if (i > 0) {
                sb.append(',');
            }
            sb.append('\"');
            sb.append(getDefinition().getFieldName(i));
            sb.append('\"');
            sb.append(':');
            val.toStringBuilder(sb);
        }
        sb.append('}');
    }

    @Override
    int numValues() {
        return theTupleRegs.length;
    }

    @Override
    public int size() {
        return theTupleRegs.length;
    }

    /*
     * Local methods
     */

    List<String> getFieldNames() {
        return getDefinition().getFieldsInternal();
    }

    public String getFieldName(int fieldPos) {
        return getDefinition().getFieldName(fieldPos);
    }

    FieldDefImpl getFieldDef(String fieldName) {
        return getDefinition().getField(fieldName);
    }

    public FieldValueImpl getFieldValue(int fieldPos) {
        return theRegisters[theTupleRegs[fieldPos]];
    }

    void putFieldValue(int fieldPos, FieldValueImpl value) {
        theRegisters[theTupleRegs[fieldPos]] = value;
    }

    public RecordValueImpl toRecord() {

        RecordValueImpl rec = getDefinition().createRecord();

        for (int i = 0; i < size(); ++i) {
            rec.put(getFieldName(i), getFieldValue(i));
        }

        return rec;
    }

    public void toTuple(RecordValueImpl rec) {

        assert(theDef.equals(rec.getDefinition()));

        int i = 0;
        for (String s : rec.getFields()) {
            theRegisters[theTupleRegs[i]] = rec.getFieldValue(s);
            ++i;
        }
        /*
          This code will replace the above one if we change the
          implementation of RecordValueImpl.
        for (int i = 0; i < size(); ++i) {
            theRegisters[theTupleRegs[i]] = rec.getFieldValue(i);
        }
        */
    }
}
