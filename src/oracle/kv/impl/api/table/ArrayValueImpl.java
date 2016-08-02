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

package oracle.kv.impl.api.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import oracle.kv.table.ArrayDef;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.MapValue;
import oracle.kv.table.RecordValue;

import org.apache.avro.Schema;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;

import com.sleepycat.persist.model.Persistent;

/**
 * ArrayValueImpl implements the ArrayValue interface to hold an object of
 * type ArrayDef.
 */
@Persistent(version=1)
class ArrayValueImpl extends ComplexValueImpl implements ArrayValue {
    private static final long serialVersionUID = 1L;
    private final ArrayList<FieldValue> array;

    ArrayValueImpl(ArrayDef field) {
        super(field);
        array = new ArrayList<FieldValue>();
    }

    /* DPL */
    private ArrayValueImpl() {
        super(null);
        array = null;
    }

    @Override
    public ArrayDef getDefinition() {
        return (ArrayDef) super.getDefinition();
    }

    @Override
    public FieldValue get(int index) {
        return array.get(index);
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public List<FieldValue> toList() {
        return Collections.unmodifiableList(array);
    }

    @Override
    public ArrayValue add(FieldValue value) {
        validate(value.getType());
        array.add(value);
        return this;
    }

    @Override
    public ArrayValue add(int index, FieldValue value) {
        validate(value.getType());
        array.add(index, value);
        return this;
    }

    @Override
    public ArrayValue set(int index, FieldValue value) {
        validate(value.getType());
        array.set(index, value);
        return this;
    }

    /**
     * set overloads for all simple data types
     */

    /**
     * Integer
     */
    @Override
    public ArrayValue add(int value) {
        validate(FieldDef.Type.INTEGER);
        add(getElement().createInteger(value));
        return this;
    }

    @Override
    public ArrayValue add(int values[]) {
        validate(FieldDef.Type.INTEGER);
        FieldDef def = getElement();
        for (int i : values) {
            add(def.createInteger(i));
        }
        return this;
    }

    @Override
    public ArrayValue add(int index, int value) {
        validate(FieldDef.Type.INTEGER);
        add(index, getElement().createInteger(value));
        return this;
    }

    @Override
    public ArrayValue set(int index, int value) {
        validate(FieldDef.Type.INTEGER);
        set(index, getElement().createInteger(value));
        return this;
    }

    /**
     * Long
     */
    @Override
    public ArrayValue add(long value) {
        validate(FieldDef.Type.LONG);
        add(getElement().createLong(value));
        return this;
    }

    @Override
    public ArrayValue add(long values[]) {
        validate(FieldDef.Type.LONG);
        FieldDef def = getElement();
        for (long l : values) {
            add(def.createLong(l));
        }
        return this;
    }

    @Override
    public ArrayValue add(int index, long value) {
        validate(FieldDef.Type.LONG);
        add(index, getElement().createLong(value));
        return this;
    }

    @Override
    public ArrayValue set(int index, long value) {
        validate(FieldDef.Type.LONG);
        set(index, getElement().createLong(value));
        return this;
    }

    /**
     * String
     */
    @Override
    public ArrayValue add(String value) {
        validate(FieldDef.Type.STRING);
        add(getElement().createString(value));
        return this;
    }

    @Override
    public ArrayValue add(String values[]) {
        validate(FieldDef.Type.STRING);
        FieldDef def = getElement();
        for (String s : values) {
            add(def.createString(s));
        }
        return this;
    }

    @Override
    public ArrayValue add(int index, String value) {
        validate(FieldDef.Type.STRING);
        add(index, getElement().createString(value));
        return this;
    }

    @Override
    public ArrayValue set(int index, String value) {
        validate(FieldDef.Type.STRING);
        set(index, getElement().createString(value));
        return this;
    }

    /**
     * Double
     */
    @Override
    public ArrayValue add(double value) {
        validate(FieldDef.Type.DOUBLE);
        add(getElement().createDouble(value));
        return this;
    }

    @Override
    public ArrayValue add(double values[]) {
        validate(FieldDef.Type.DOUBLE);
        FieldDef def = getElement();
        for (double d : values) {
            add(def.createDouble(d));
        }
        return this;
    }

    @Override
    public ArrayValue add(int index, double value) {
        validate(FieldDef.Type.DOUBLE);
        add(index, getElement().createDouble(value));
        return this;
    }

    @Override
    public ArrayValue set(int index, double value) {
        validate(FieldDef.Type.DOUBLE);
        set(index, getElement().createDouble(value));
        return this;
    }

    /**
     * Float
     */
    @Override
    public ArrayValue add(float value) {
        validate(FieldDef.Type.FLOAT);
        add(getElement().createFloat(value));
        return this;
    }

    @Override
    public ArrayValue add(float values[]) {
        validate(FieldDef.Type.FLOAT);
        FieldDef def = getElement();
        for (float d : values) {
            add(def.createFloat(d));
        }
        return this;
    }

    @Override
    public ArrayValue add(int index, float value) {
        validate(FieldDef.Type.FLOAT);
        add(index, getElement().createFloat(value));
        return this;
    }

    @Override
    public ArrayValue set(int index, float value) {
        validate(FieldDef.Type.FLOAT);
        set(index, getElement().createFloat(value));
        return this;
    }

    /**
     * Boolean
     */
    @Override
    public ArrayValue add(boolean value) {
        validate(FieldDef.Type.BOOLEAN);
        add(getElement().createBoolean(value));
        return this;
    }

    @Override
    public ArrayValue add(boolean values[]) {
        validate(FieldDef.Type.BOOLEAN);
        FieldDef def = getElement();
        for (boolean b : values) {
            add(def.createBoolean(b));
        }
        return this;
    }

    @Override
    public ArrayValue add(int index, boolean value) {
        validate(FieldDef.Type.BOOLEAN);
        add(index, getElement().createBoolean(value));
        return this;
    }

    @Override
    public ArrayValue set(int index, boolean value) {
        validate(FieldDef.Type.BOOLEAN);
        set(index, getElement().createBoolean(value));
        return this;
    }

    /**
     * Binary
     */
    @Override
    public ArrayValue add(byte[] value) {
        validate(FieldDef.Type.BINARY);
        add(getElement().createBinary(value));
        return this;
    }

    @Override
    public ArrayValue add(byte[] values[]) {
        validate(FieldDef.Type.BINARY);
        FieldDef def = getElement();
        for (byte[] b : values) {
            add(def.createBinary(b));
        }
        return this;
    }

    @Override
    public ArrayValue add(int index, byte[] value) {
        validate(FieldDef.Type.BINARY);
        add(index, getElement().createBinary(value));
        return this;
    }

    @Override
    public ArrayValue set(int index, byte[] value) {
        validate(FieldDef.Type.BINARY);
        set(index, getElement().createBinary(value));
        return this;
    }

    /**
     * FixedBinary
     */
    @Override
    public ArrayValue addFixed(byte[] value) {
        validate(FieldDef.Type.FIXED_BINARY);
        add(getElement().createFixedBinary(value));
        return this;
    }

    @Override
    public ArrayValue addFixed(byte[] values[]) {
        validate(FieldDef.Type.FIXED_BINARY);
        FieldDef def = getElement();
        for (byte[] b : values) {
            add(def.createFixedBinary(b));
        }
        return this;
    }

    @Override
    public ArrayValue addFixed(int index, byte[] value) {
        validate(FieldDef.Type.FIXED_BINARY);
        add(index, getElement().createFixedBinary(value));
        return this;
    }

    @Override
    public ArrayValue setFixed(int index, byte[] value) {
        validate(FieldDef.Type.FIXED_BINARY);
        set(index, getElement().createFixedBinary(value));
        return this;
    }

    /**
     * Enum
     */
    @Override
    public ArrayValue addEnum(String value) {
        validate(FieldDef.Type.ENUM);
        add(getElement().createEnum(value));
        return this;
    }

    @Override
    public ArrayValue addEnum(String values[]) {
        validate(FieldDef.Type.ENUM);
        FieldDef def = getElement();
        for (String s : values) {
            add(def.createEnum(s));
        }
        return this;
    }

    @Override
    public ArrayValue addEnum(int index, String value) {
        validate(FieldDef.Type.ENUM);
        add(index, getElement().createEnum(value));
        return this;
    }

    @Override
    public ArrayValue setEnum(int index, String value) {
        validate(FieldDef.Type.ENUM);
        set(index, getElement().createEnum(value));
        return this;
    }

    /**
     * This is used by index deserialization.  The format for enums is an
     * integer.
     */
    ArrayValue addEnum(int value) {
        validate(FieldDef.Type.ENUM);
        add(((EnumDefImpl)getElement()).createEnum(value));
        return this;
    }

    @Override
    public RecordValue setRecord(int index) {
        validate(FieldDef.Type.RECORD);
        RecordValue val = getElement().createRecord();
        array.set(index, val);
        return val;
    }

    @Override
    public RecordValueImpl addRecord() {
        validate(FieldDef.Type.RECORD);
        RecordValue val = getElement().createRecord();
        array.add(val);
        return (RecordValueImpl) val;
    }

    @Override
    public RecordValue addRecord(int index) {
        validate(FieldDef.Type.RECORD);
        RecordValue val = getElement().createRecord();
        array.add(index, val);
        return val;
    }

    @Override
    public MapValue setMap(int index) {
        validate(FieldDef.Type.MAP);
        MapValue val = getElement().createMap();
        array.set(index, val);
        return val;
    }

    @Override
    public MapValueImpl addMap() {
        validate(FieldDef.Type.MAP);
        MapValue val = getElement().createMap();
        array.add(val);
        return (MapValueImpl) val;
    }

    @Override
    public MapValue addMap(int index) {
        validate(FieldDef.Type.MAP);
        MapValue val = getElement().createMap();
        array.add(index, val);
        return val;
    }

    @Override
    public ArrayValue setArray(int index) {
        validate(FieldDef.Type.ARRAY);
        ArrayValue val = getElement().createArray();
        array.set(index, val);
        return val;
    }

    @Override
    public ArrayValueImpl addArray() {
        validate(FieldDef.Type.ARRAY);
        ArrayValue val = getElement().createArray();
        array.add(val);
        return (ArrayValueImpl) val;
    }

    @Override
    public ArrayValue addArray(int index) {
        validate(FieldDef.Type.ARRAY);
        ArrayValue val = getElement().createArray();
        array.add(index, val);
        return val;
    }

    @Override
    public FieldDef.Type getType() {
        return FieldDef.Type.ARRAY;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public ArrayValue asArray() {
        return this;
    }

    /**
     * Increment the value of the array element, not the array.  There
     * can only be one element in this array.
     */
    @Override
    public FieldValueImpl getNextValue() {
        if (size() != 1) {
            throw new IllegalArgumentException
                ("Array values used in ranges must contain only one element");
        }
        ArrayValueImpl newArray = new ArrayValueImpl(getDefinition());
        FieldValueImpl fvi = ((FieldValueImpl)get(0)).getNextValue();
        newArray.add(fvi);
        return newArray;
    }

    @Override
    public FieldValueImpl getMinimumValue() {
        if (size() != 1) {
            throw new IllegalArgumentException
                ("Array values used in ranges must contain only one element");
        }
        ArrayValueImpl newArray = new ArrayValueImpl(getDefinition());
        FieldValueImpl fvi = ((FieldValueImpl)get(0)).getMinimumValue();
        newArray.add(fvi);
        return newArray;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ArrayValueImpl) {
            ArrayValueImpl otherValue = (ArrayValueImpl) other;
            /* maybe avoid some work */
            if (this == otherValue) {
                return true;
            }

            /*
             * detailed comparison
             */
            if (size() == otherValue.size() &&
                getElement().equals(otherValue.getElement()) &&
                getDefinition().equals(otherValue.getDefinition())) {
                for (int i = 0; i < size(); i++) {
                    if (!get(i).equals(otherValue.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = size();
        for (FieldValue val : array) {
            code += val.hashCode();
        }
        return code;
    }

    /**
     * FieldDef must match.
     *
     * Compare field values in array order.  Return as soon as there is a
     * difference. If this object has a field the other does not, return > 0.
     * If this object is missing a field the other has, return < 0.
     */
    @Override
    public int compareTo(FieldValue other) {
        if (other instanceof ArrayValueImpl) {
            ArrayValueImpl otherImpl = (ArrayValueImpl) other;
            if (!getDefinition().equals(otherImpl.getDefinition())) {
                throw new IllegalArgumentException
                    ("Cannot compare ArrayValues with different definitions");
            }
            for (int i = 0; i < size(); i++) {
                FieldValueImpl val = (FieldValueImpl) get(i);
                if (otherImpl.size() < i + 1) {
                    return 1;
                }
                FieldValueImpl otherVal = (FieldValueImpl) otherImpl.get(i);
                if (val != null) {
                    if (otherVal == null) {
                        return 1;
                    }
                    int comp = val.compareTo(otherVal);
                    if (comp != 0) {
                        return comp;
                    }
                } else if (otherVal != null) {
                    return -1;
                }
            }
            /* they must be equal */
            return 0;
        }
        throw new ClassCastException
            ("Object is not an ArrayValue");
    }

    @Override
    public ArrayValueImpl clone() {
        ArrayValueImpl newArray = new ArrayValueImpl(getDefinition());
        for (FieldValue val : array) {
            newArray.add(val.clone());
        }
        return newArray;
    }

    @Override
    public JsonNode toJsonNode() {
        ArrayNode node = JsonNodeFactory.instance.arrayNode();
        for (FieldValue value : array) {
            node.add(((FieldValueImpl)value).toJsonNode());
        }
        return node;
    }

    @Override
    public void toStringBuilder(StringBuilder sb) {
        sb.append('[');
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            FieldValueImpl value = (FieldValueImpl)array.get(i);
            value.toStringBuilder(sb);
        }
        sb.append(']');
    }


    /*
     * Array maps to Collection.  Use ArrayList
     */
    @Override
    Object toAvroValue(Schema schema) {
        Schema valueSchema = getElementSchema(schema);
        ArrayList<Object> values = new ArrayList<Object>(size());
        for (FieldValue value : array) {
            values.add(((FieldValueImpl)value).toAvroValue(valueSchema));

        }
        return values;
    }

    @SuppressWarnings("unchecked")
    static ArrayValueImpl fromAvroValue(FieldDef def,
                                        Object o,
                                        Schema schema) {
        Collection<Object> coll = (Collection<Object>) o;
        ArrayValue array = def.createArray();
        for (Object value : coll) {
            array.add(FieldValueImpl.
                      fromAvroValue(array.getDefinition().getElement(),
                                    value,
                                    getElementSchema(schema)));
        }
        return (ArrayValueImpl)array;
    }

    @SuppressWarnings("unchecked")
    static ArrayValueImpl fromJavaObjectValue(FieldDef def, Object o) {
        Iterable<Object> coll = null;
        if(o instanceof Iterable<?>) {
            coll = (Iterable<Object>) o;
        } else {
            coll = Arrays.asList((Object[]) o);
        }
        ArrayValue array = def.createArray();
        for (Object value : coll) {
            array.add(FieldValueImpl.
                      fromJavaObjectValue(array.getDefinition().getElement(),
                                          value));
        }
        return (ArrayValueImpl)array;
    }

    /**
     * Add JSON fields to the array.
     */
    @Override
    void addJsonFields(JsonParser jp, boolean isIndexKey,
                       String fieldName, boolean exact) {
        try {
            FieldDef element = getElement();
            JsonToken t = jp.getCurrentToken();
            if(t == null) { /* JSON is empty */
                return;
            }
            assert(t == JsonToken.START_ARRAY);
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                /*
                 * Handle null.
                 */
                if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
                    throw new IllegalArgumentException
                        ("Invalid null value in JSON input for array");
                }

                switch (element.getType()) {
                case INTEGER:
                    add(jp.getIntValue());
                    break;
                case LONG:
                    add(jp.getLongValue());
                    break;
                case DOUBLE:
                    add(jp.getDoubleValue());
                    break;
                case FLOAT:
                    add(jp.getFloatValue());
                    break;
                case STRING:
                    add(jp.getText());
                    break;
                case BINARY:
                    add(jp.getBinaryValue());
                    break;
                case FIXED_BINARY:
                    addFixed(jp.getBinaryValue());
                    break;
                case BOOLEAN:
                    add(jp.getBooleanValue());
                    break;
                case ARRAY:
                    ArrayValueImpl array1 = addArray();
                    array1.addJsonFields(jp, isIndexKey, null, exact);
                    break;
                case MAP:
                    MapValueImpl map = addMap();
                    map.addJsonFields(jp, isIndexKey, null, exact);
                    break;
                case RECORD:
                    RecordValueImpl record = addRecord();
                    record.addJsonFields(jp, isIndexKey, null, exact);
                    break;
                case ENUM:
                    addEnum(jp.getText());
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException
                (("Failed to parse JSON input: " + ioe.getMessage()), ioe);
        }
    }

    /*
     * Handle the fact that this field may be nullable and therefore have a
     * Union schema.
     */
    private static Schema getElementSchema(Schema schema) {
        return getUnionSchema(schema, Schema.Type.ARRAY).getElementType();
    }

    /*
     * internals
     */

    private FieldDef getElement() {
        return (getDefinition()).getElement();
    }

    /**
     * This method must expand to do full validation of type, value and
     * constraints if present in the definition.  Right now it just
     * validates the type.
     */
    private void validate(FieldDef.Type type) {
        if (!getElement().isType(type)) {
            throw new IllegalArgumentException
                ("Incorrect type for array");
        }
    }

    @Override
    int numValues() {

        /* always count at least one entry to trigger a validation failure */
        if (array.isEmpty()) {
            return 1;
        }
        int num = 0;
        for (FieldValue v : array) {
            num += ((FieldValueImpl) v).numValues();
        }
        return num;
    }

    /**
     * This is used for index serialization, which means that the array must
     * have one and only one element.
     *
     * Array entries are not named so pass the state directly to the array
     * element, if present.
     */
    @Override
    FieldValueImpl findFieldValue(ListIterator<String> fieldPath,
                                  int arrayIndex) {
        if (arrayIndex == -1 && size() != 1) {
            throw new IllegalArgumentException
                ("Array values used in index keys must contain a single " +
                 "element");
        }

        if (arrayIndex == -1) {
            arrayIndex = 0;
        }
        FieldValueImpl fv = (FieldValueImpl) get(arrayIndex);
        if (fv == null) {
            return null;
        }

        /*
         * TODO:
         * Use this code to consume [] if it is ever to be used to reference
         * the current value.
         *
         * Peek at the current component.  If it is [], consume it,
         * and keep going. This allows operations to target the element
         * itself vs a field contained in the element.
         * String component = fieldPath.next();
         * if (TableImpl.ANONYMOUS.equalsIgnoreCase(component)) {
         *     if (!fieldPath.hasNext()) {
         *         return fv;
         *     }
         * } else {
         *     fieldPath.previous();
         * }
         */

        return fv.findFieldValue(fieldPath, arrayIndex);
    }

    /**
     * Creates a nested complex instance in an array for indexes on a complex
     * field in an array.  If the element type in the array is not complex an
     * exception will be thrown from createComplexValue().  If the array is
     * empty the appropriate type will be created.  If it is not empty its size
     * must be 1 and that element is used.
     */
    @Override
    FieldValueImpl putComplex(ListIterator<String> fieldPath,
                              FieldDef.Type type, Object value)  {
        ComplexValueImpl val = null;
        if (array.isEmpty()) {
            val = createComplexValue(getElement());
            add(val);
        } else if (size() == 1 && (get(0) instanceof ComplexValueImpl)) {
            val = (ComplexValueImpl) get(0);
        } else {
            throw new IllegalArgumentException
                ("Invalid attempt to put a complex element in an array.  " +
                 "Size must be 1 and the element must be complex.  Array " +
                 "contents: " + this);
        }
        val.putComplex(fieldPath, type, value);
        return this;
    }

    /* This method should never be called on an array */
    @Override
    protected void validateIndexField(String fieldName) {
        throw new IllegalStateException
            ("validateIndexField called on an array, field name is " +
             fieldName);
    }

}
