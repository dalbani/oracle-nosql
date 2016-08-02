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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import oracle.kv.impl.api.table.TableImpl.TableField;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.EnumDef;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.MapValue;
import oracle.kv.table.RecordDef;
import oracle.kv.table.RecordValue;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParser.NumberType;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.sleepycat.persist.model.Persistent;

/**
 * RecordValueImpl implements RecordValue and is a multi-valued object that
 * contains a map of string names to fields.  The field values may be simple or
 * complex and allowed fields are defined by the FieldDef definition of the
 * record.
 */
@Persistent(version=1)
class RecordValueImpl extends ComplexValueImpl
    implements RecordValue {
    private static final long serialVersionUID = 1L;
    protected final Map<String, FieldValue> valueMap;

    /*
     * These are used for numeric validation of JSON input.  Construct the
     * values from String to avoid differences in rounding and because the
     * construction is being done from String in the JSON input as well.
     */
    private static final BigDecimal floatMin =
        new BigDecimal(new Float(Float.MIN_VALUE).toString());
    private static final BigDecimal floatMax =
        new BigDecimal(new Float(Float.MAX_VALUE).toString());
    private static final BigDecimal bdNegOne = BigDecimal.ONE.negate();

    RecordValueImpl(RecordDef field) {
        super(field);
        valueMap = new TreeMap<String, FieldValue>(FieldComparator.instance);
    }

    RecordValueImpl(RecordDef field, Map<String, FieldValue> valueMap) {
        super(field);
        if (valueMap == null) {
            throw new IllegalArgumentException
                ("Null valueMap passed to RecordValueImpl");
        }
        this.valueMap = valueMap;
    }

    RecordValueImpl(RecordValueImpl other) {
        super(other.getDefinition());
        valueMap = new TreeMap<String, FieldValue>(FieldComparator.instance);
        copyFields(other);
    }

    /* DPL */
    private RecordValueImpl() {
        super(null);
        valueMap = null;
    }

    @Override
    public FieldValue get(String fieldName) {
        return valueMap.get(fieldName);
    }

    /**
     * Put methods.  All of these silently overwrite any existing state by
     * creating/using new FieldValue objects.  These methods return "this"
     * in order to support chaining of operations.
     */

    @Override
    public RecordValue put(String name, int value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.INTEGER);
        putField(name, def.createInteger(value));
        return this;
    }

    @Override
    public RecordValue put(String name, long value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.LONG);
        putField(name, def.createLong(value));
        return this;
    }

    @Override
    public RecordValue put(String name, String value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.STRING);
        putField(name, def.createString(value));
        return this;
    }

    FieldValue putComplex(TableField tableField,
                          FieldDef.Type type, Object value) {

        if (!tableField.isComplex()) {
            String name = tableField.getFieldName();
            FieldDefImpl def = validateNameAndType(name, type);
            putField(name, def.createValue(type, value));
        } else {
            putComplex(tableField.iterator(), type, value);
        }
        return this;
    }

    @Override
    FieldValueImpl putComplex(ListIterator<String> fieldPath,
                              FieldDef.Type type, Object value) {

        String fname = fieldPath.next();
        if (!fieldPath.hasNext()) {
            FieldDefImpl def = validateNameAndType(fname, type);
            putField(fname, def.createValue(type, value));
        } else {
            FieldDefImpl def = (FieldDefImpl) getDefinition(fname);
            if (def == null) {
                throw new IllegalArgumentException
                    ("Cannot find field named " + fname);
            }

            /*
             * The nested field may have been created already.
             */
            FieldValueImpl val = (FieldValueImpl) get(fname);
            if (val == null) {
                val = createComplexValue(def);
            }
            putField(fname, val.putComplex(fieldPath, type, value));
        }
        return this;
    }

    @Override
    public RecordValue put(String name, double value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.DOUBLE);
        putField(name, def.createDouble(value));
        return this;
    }

    @Override
    public RecordValue put(String name, float value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.FLOAT);
        putField(name, def.createFloat(value));
        return this;
    }

    @Override
    public RecordValue put(String name, boolean value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.BOOLEAN);
        putField(name, def.createBoolean(value));
        return this;
    }

    @Override
    public RecordValue put(String name, byte[] value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.BINARY);
        putField(name, def.createBinary(value));
        return this;
    }

    @Override
    public RecordValue putFixed(String name, byte[] value) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.FIXED_BINARY);
        putField(name, def.createFixedBinary(value));
        return this;
    }

    @Override
    public RecordValue putEnum(String name, String value) {
        EnumDefImpl enumField =
            (EnumDefImpl) validateNameAndType(name, FieldDef.Type.ENUM);
        putField(name, enumField.createEnum(value));
        return this;
    }

    @Override
    public RecordValue putNull(String name) {
        FieldDef ft = getDefinition(name);
        if (ft == null) {
            throw new IllegalArgumentException("No such field in record " +
                                               getDefinition().getName() + ": " +
                                               name);
        }
        if (!getDefinition().isNullable(name)) {
            throw new IllegalArgumentException
                ("Named field is not nullable: " + name);
        }
        putField(name, NullValueImpl.getInstance());
        return this;
    }

    @Override
    public RecordValue put(String name, FieldValue value) {
        if (value.isNull()) {
            return putNull(name);
        }
        validateNameAndType(name, value.getType());
        putField(name, value);
        return this;
    }

    @Override
    public RecordValueImpl putRecord(String name) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.RECORD);
        RecordValue val = def.createRecord();
        putField(name, val);
        return (RecordValueImpl)val;
    }

    @Override
    public RecordValue putRecord(String name, Map<String, ?> map) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.RECORD);
        RecordValue val;
        try {
            val = RecordValueImpl.fromJavaObjectValue(def, map);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("The map value type doesn't "
                + "match the field definition of the named field.",
                                               cce);
        }
        putField(name, val);
        return this;
    }

    @Override
    public RecordValue putRecordAsJson(String name,
                                       String jsonInput,
                                       boolean exact){
        return putRecordAsJson(name,
                               new ByteArrayInputStream(jsonInput.getBytes()),
                               exact);
    }

    @Override
    public RecordValue putRecordAsJson(String name,
                                       InputStream jsonInput,
                                       boolean exact){
        FieldDef def = validateNameAndType(name, FieldDef.Type.RECORD);
        RecordValue record = def.createRecord();
        TableImpl.createFromJson((RecordValueImpl)record, jsonInput, exact);
        putField(name, record);
        return this;
    }

    @Override
    public ArrayValueImpl putArray(String name) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.ARRAY);
        ArrayValue val = def.createArray();
        putField(name, val);
        return (ArrayValueImpl)val;
    }

    @Override
    public RecordValue putArray(String name, Iterable<?> list) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.ARRAY);
        ArrayValue val;
        try {
            val = ArrayValueImpl.fromJavaObjectValue(def, list);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("The list type doesn't match "
                + "the field definition of the named field.",
                                               cce);
        }
        putField(name, val);
        return this;
    }

    @Override
    public RecordValue putArray(String name, Object[] array) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.ARRAY);
        ArrayValue val;
        try {
            val = ArrayValueImpl.fromJavaObjectValue(def, array);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("The array type doesn't + match "
                + "the field definition of the named field.",
                                               cce);
        }
        putField(name, val);
        return this;
    }

    @Override
    public RecordValue putArrayAsJson(String name,
                                      String jsonInput,
                                      boolean exact){
        return putArrayAsJson(name,
                              new ByteArrayInputStream(jsonInput.getBytes()),
                              exact);
    }

    @Override
    public RecordValue putArrayAsJson(String name,
                                      InputStream jsonInput,
                                      boolean exact){
        FieldDef def = validateNameAndType(name, FieldDef.Type.ARRAY);
        ArrayValue array = def.createArray();
        TableImpl.createFromJson((ArrayValueImpl)array, jsonInput, exact);
        putField(name, array);
        return this;
    }

    @Override
    public MapValueImpl putMap(String name) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.MAP);
        MapValue val = def.createMap();
        putField(name, val);
        return (MapValueImpl)val;
    }

    @Override
    public RecordValue putMap(String name, Map<String, ?> map) {
        FieldDef def = validateNameAndType(name, FieldDef.Type.MAP);
        MapValue val;
        try {
            val = MapValueImpl.fromJavaObjectValue(def, map);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("The map value type doesn't "
                + "match the field definition of the named field.",
                                               cce);
        }
        putField(name, val);
        return this;
    }

    @Override
    public RecordValue putMapAsJson(String name,
                                    String jsonInput,
                                    boolean exact){
        return putMapAsJson(name,
                            new ByteArrayInputStream(jsonInput.getBytes()),
                            exact);
    }

    @Override
    public RecordValue putMapAsJson(String name,
                                    InputStream jsonInput,
                                    boolean exact){
        FieldDef def = validateNameAndType(name, FieldDef.Type.MAP);
        MapValue map = def.createMap();
        TableImpl.createFromJson((MapValueImpl)map, jsonInput, exact);
        putField(name, map);
        return this;
    }

    @Override
    public int size() {
        return valueMap.size();
    }

    @Override
    public boolean isEmpty() {
        return valueMap.isEmpty();
    }

    @Override
    public RecordDefImpl getDefinition() {
        return (RecordDefImpl) super.getDefinition();
    }

    @Override
    public FieldDef.Type getType() {
        return FieldDef.Type.RECORD;
    }

    @Override
    public boolean isRecord() {
        return true;
    }

    @Override
    public RecordValue asRecord() {
        return this;
    }

    /**
     * Deep copy
     */
    @Override
    public RecordValueImpl clone() {
        return new RecordValueImpl(this);
    }

    @Override
    public boolean equals(Object other) {
        /* maybe avoid some work */
        if (this == other) {
            return true;
        }
        if (!(other instanceof RecordValueImpl)) {
            return false;
        }
        RecordValueImpl otherValue = (RecordValueImpl) other;

        /*
         * field-by-field comparison
         */
        if (size() == otherValue.size() &&
            getDefinition().equals(otherValue.getDefinition())) {
            for (Map.Entry<String, FieldValue> entry : valueMap.entrySet()) {
                if (!entry.getValue().equals(otherValue.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = size();
        for (Map.Entry<String, FieldValue> entry : valueMap.entrySet()) {
            code += entry.getKey().hashCode() + entry.getValue().hashCode();
        }
        return code;
    }

    /**
     * FieldDef must match for both objects.
     */
    @Override
    public int compareTo(FieldValue other) {
        if (other instanceof RecordValueImpl) {
            RecordValueImpl otherImpl = (RecordValueImpl) other;
            if (!getDefinition().equals(otherImpl.getDefinition())) {
                throw new IllegalArgumentException
                    ("Cannot compare RecordValues with different definitions");
            }
            return compare(otherImpl, getFieldsInternal());
        }
        throw new ClassCastException
            ("Object is not an RecordValue");
    }

    /**
     * This is a standalone method with semantics similar to compareTo from
     * the Comparable interface but it takes a restricted list of fields to
     * compare, in order.  The compareTo interface calls this method.
     *
     * Compare field values in order.  Return as soon as there is a difference.
     * If this object has a field the other does not, return > 0.  If this
     * object is missing a field the other has, return < 0.
     */
    public int compare(RecordValueImpl other, List<String> fieldList) {
        for (String fieldName : fieldList) {
            FieldValueImpl val = (FieldValueImpl) get(fieldName);
            FieldValueImpl otherVal = (FieldValueImpl) other.get(fieldName);
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

    @Override
    public JsonNode toJsonNode() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        /*
         * Add fields in field declaration order.  A little slower but it's
         * what the user expects.  Fields may be missing.  That is allowed.
         */
        for (String fieldName : getFieldsInternal()) {
            FieldValueImpl val = (FieldValueImpl) get(fieldName);
            if (val != null) {
                node.put(fieldName, val.toJsonNode());
            }
        }
        return node;
    }

    @Override
    public void toStringBuilder(StringBuilder sb) {
        sb.append('{');
        int i = 0;
        for (String fieldName : getFieldsInternal()) {
            FieldValueImpl val = (FieldValueImpl)valueMap.get(fieldName);
            if (val != null) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append('\"');
                sb.append(fieldName);
                sb.append('\"');
                sb.append(':');
                val.toStringBuilder(sb);
                i++;
            }
        }
        sb.append('}');
    }

    @Override
    public FieldValue remove(String name) {
        return valueMap.remove(name);
    }

    /**
     * The use of getFieldsInternal() in this method ensures that only fields
     * appropriate for the implementing type (Row, PrimaryKey, IndexKey, etc)
     * are copied. Use of putField() bypasses unnecessary checking of field
     * name and type which is done implicitly by the definition check.
     */
    @Override
    public void copyFrom(RecordValue source) {
        copyFrom(source, false);
    }

    void copyFrom(RecordValue source, boolean ignoreDefinition) {
        if (!ignoreDefinition &&
            !getDefinition().equals(source.getDefinition())) {
            throw new IllegalArgumentException
                ("Definition of source record does not match this object");
        }
        for (String fieldName : getFieldsInternal()) {
            FieldValue val = source.get(fieldName);
            if (val != null) {
                putField(fieldName, val);
            }
        }
    }

    @Override
    public boolean contains(String fieldName) {
        return valueMap.containsKey(fieldName);
    }

    /**
     * Clear the fields.
     * TODO: should this be part of the interface?
     */
    public void clear() {
        valueMap.clear();
    }

    /**
     * Enum is serialized in indexes as an integer representing the value's
     * index in the enumeration declaration.  Deserialize here.
     */
    RecordValue putEnum(String name, int value) {
        EnumDefImpl enumField =
            (EnumDefImpl) validateNameAndType(name, FieldDef.Type.ENUM);
        putField(name, enumField.createEnum(value));
        return this;
    }

    /**
     * Create and set the named field based on its String representation.  The
     * String representation is that returned by
     * {@link Object#toString toString}.
     *
     * @param name name of the desired field
     *
     * @param value the value to set
     *
     * @param type the type to use for coercion from String
     *
     * @return an instance of FieldValue representing the value.
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition.
     */
    public FieldValue put(String name, String value, FieldDef.Type type) {
        FieldDef newField = validateNameAndType(name, type);
        FieldValue val = create(value, newField);
        putField(name, val);
        return val;
    }

    void copyFields(RecordValueImpl from) {
        for (Map.Entry<String, FieldValue> entry : from.valueMap.entrySet()) {
            putField(entry.getKey(), entry.getValue().clone());
        }
    }

    /**
     * Return the FieldDef for the named field in the record.
     */
    FieldDef getDefinition(String name) {
        return getDefinition().getField(name);
    }

    /**
     * Return the number of fields in this record.  This method will be
     * overridden by classes that restrict the number (PrimaryKey, IndexKey).
     */
    int getNumFields() {
        return getDefinition().getNumFields();
    }

    /**
     * Return the field definition for the named field.  Null if it does not
     * exist or is not available in this instance (e.g. PrimaryKey, IndexKey).
     */
    FieldDef getField(String fieldName) {
        return getDefinition().getField(fieldName);
    }

    FieldMapEntry getFieldMapEntry(String fieldName) {
        return getDefinition().getFieldMapEntry(fieldName, false);
    }

    /**
     * Returns an ordered list of fields for this object.  By default it is the
     * field order list from the associated RecordDefImpl object.  Key objects
     * will override this to restrict it to the list they require.
     */
    @Override
    public List<String> getFields() {
        return Collections.unmodifiableList(getFieldsInternal());
    }

    /**
     * Internal method to get fields that does not involve a copy.  This is
     * also overridden by key objects to constrain the list to the fields
     * appropriate to those objects.
     */
    protected List<String> getFieldsInternal() {
        return getDefinition().getFieldsInternal();
    }

    /*
     * Record maps to GenericRecord
     */
    @Override
    Object toAvroValue(Schema schema) {
        Schema recordSchema = getRecordSchema(schema);
        GenericRecord record = new GenericData.Record(recordSchema);
        RecordDefImpl def = getDefinition();
        for (Map.Entry<String, FieldMapEntry> entry :
                 def.getFieldMap().getFields().entrySet()) {
            String fieldName = entry.getKey();
            FieldValueImpl fv = (FieldValueImpl) get(fieldName);
            if (fv == null) {
                fv = entry.getValue().getDefaultValue();
            }
            if (fv.isNull()) {
                record.put(fieldName, null);
            } else {
                Schema s = recordSchema.getField(fieldName).schema();
                record.put(fieldName, fv.toAvroValue(s));
            }
        }
        return record;
    }

    /**
     * Add matching JSON fields to the record.  For each named field do this:
     * 1.  find it in the record definition
     * 2.  if present, add it to the value
     * 3.  if not present ignore, unless exact is true, in which case throw.
     *
     * If exact is true then the input state must match that of the record
     * exactly or IllegalArgumentException is thrown.
     *
     * If exact is false and the JSON input is an empty JSON object (e.g. {})
     * no fields are added and there is no exception thrown.
     */
    @Override
    void addJsonFields(JsonParser jp, boolean isIndexKey,
                       String currentFieldName, boolean exact) {
        int numFields = 0;
        try {
            JsonToken t = null;
            while ((t = jp.nextToken()) != JsonToken.END_OBJECT) {
                if (t == null) {
                    break;
                }
                String fieldName = jp.getCurrentName();
                if (fieldName != null) {

                    /*
                     * getFieldMapEntry() will filter out fields
                     * not relevant to this type (e.g. index key).
                     */
                    FieldMapEntry fme = getFieldMapEntry(fieldName);
                    if (fme == null) {
                        if (exact) {
                            throw new IllegalArgumentException
                                ("Unexpected field in JSON input: " +
                                 fieldName);
                        }
                        /*
                         * An exact match is not required.  Consume the token.
                         * If it is a nested JSON Object or Array, skip it
                         * entirely.
                         */
                        JsonToken token = jp.nextToken();
                        if (token == JsonToken.START_OBJECT) {
                            skipToJsonToken(jp, JsonToken.END_OBJECT);
                        } else if (token == JsonToken.START_ARRAY) {
                            skipToJsonToken(jp, JsonToken.END_ARRAY);
                        }
                        continue;
                    }
                    JsonToken token = jp.nextToken();
                    /*
                     * Handle null.
                     */
                    if (token == JsonToken.VALUE_NULL) {
                        if (fme.isNullable()) {
                            putNull(fieldName);
                            ++numFields;
                            continue;
                        }
                        throw new IllegalArgumentException
                            ("Invalid null value in JSON input for field "
                             + fieldName);
                    }
                    switch (fme.getField().getType()) {
                    case INTEGER:
                        checkNumberType(fieldName, NumberType.INT,
                                        jp.getNumberType(), jp);
                        put(fieldName, jp.getIntValue());
                        break;
                    case LONG:
                        checkNumberType(fieldName, NumberType.LONG,
                                        jp.getNumberType(), jp);
                        put(fieldName, jp.getLongValue());
                        break;
                    case DOUBLE:
                        checkNumberType(fieldName, NumberType.DOUBLE,
                                        jp.getNumberType(), jp);
                        put(fieldName, jp.getDoubleValue());
                        break;
                    case FLOAT:
                        checkNumberType(fieldName, NumberType.FLOAT,
                                        jp.getNumberType(), jp);
                        put(fieldName, jp.getFloatValue());
                        break;
                    case STRING:
                        checkType(fieldName, true, "STRING", jp);
                        put(fieldName, jp.getText());
                        break;
                    case BINARY:
                        checkType(fieldName, true, "BINARY", jp);
                        put(fieldName, jp.getBinaryValue());
                        break;
                    case FIXED_BINARY:
                        checkType(fieldName, true, "BINARY", jp);
                        putFixed(fieldName, jp.getBinaryValue());
                        break;
                    case BOOLEAN:
                        checkType(fieldName, true, "BOOLEAN", jp);
                        put(fieldName, jp.getBooleanValue());
                        break;
                    case ARRAY:
                        checkType(fieldName, false, "ARRAY", jp);
                        ArrayValueImpl array = putArray(fieldName);
                        array.addJsonFields(jp, isIndexKey, fieldName, exact);
                        break;
                    case MAP:
                        checkType(fieldName, false, "MAP", jp);
                        MapValueImpl map = putMap(fieldName);
                        map.addJsonFields(jp, isIndexKey, fieldName, exact);
                        break;
                    case RECORD:
                        checkType(fieldName, false, "RECORD", jp);
                        RecordValueImpl record = putRecord(fieldName);
                        record.addJsonFields(jp, isIndexKey, fieldName, exact);
                        break;
                    case ENUM:
                        checkType(fieldName, true, "ENUM", jp);
                        putEnum(fieldName, jp.getText());
                        break;
                    }
                    ++numFields;
                }
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException
                (("Failed to parse JSON input: " + ioe.getMessage()), ioe);
        }
        if (exact && (getNumFields() != numFields)) {
            throw new IllegalArgumentException
                ("Not enough fields for value in JSON input." +
                 "Found " + numFields + ", expected " + getNumFields());
        }
    }

    private static void checkNumberType(String fieldName, NumberType expected,
                                        NumberType actual, JsonParser jp)
        throws IOException {

        if (actual != expected) {

            /* Jackson infers the type.  Many casts are safe, detect these. */
            switch (actual) {
            case INT:
                /* int can cast to long, float, double */
                return;
            case FLOAT:
            case LONG:
                /* float and long can cast to double */
                if (expected == NumberType.DOUBLE) {
                    return;
                }

                /* long may be able to cast to float */
                if (expected != NumberType.FLOAT) {
                    break;
                }
                //$FALL-THROUGH$
                case DOUBLE:
                /*
                 * Jackson parses into DOUBLE and not FLOAT.  Test whether or
                 * not the cast is legal by comparing values when interpreted
                 * as BigDecimal.  NaN is checked separately as it can't be
                 * turned to BigDecimal.  At this time the Jackson parser does
                 * not handle INF, -INF.
                 */
                if (expected == NumberType.FLOAT) {
                    Double d = jp.getDoubleValue();
                    if (d.isNaN()) {
                        return;
                    }
                    BigDecimal bd = new BigDecimal(jp.getText());
                    if (bd.compareTo(BigDecimal.ZERO) < 0) {
                        bd = bd.multiply(bdNegOne);
                    }
                    assert bd.compareTo(BigDecimal.ZERO) >= 0;
                    int compMin = bd.compareTo(floatMin);
                    int compMax = bd.compareTo(floatMax);
                    if (compMin >= 0 && compMax <=0) {
                        return;
                    }
                }
                break;
            default:
                break;
            }
            throw new IllegalArgumentException
                ("Illegal value for numeric field " + fieldName +
                 ": " + jp.getText() +
                 ". Expected " + expected + ", is " + actual);
        }
    }

    /**
     * This method is called for non-numeric types to do type validation.
     * If the token is numeric or if the token is scalar and shouldn't be,
     * or the token is not scalar and should be, an exception is thrown.
     *
     * @param mustBeScalar indicates whether the type must be a scalar
     * (e.g. string, enum, binary).  If called on a map, array, or record
     * type this will be false, and will catch conditions where a scalar
     * value is incorrectly supplied.
     *
     * Validation of specific type is not done.  In this path mismatched
     * types will be caught later (e.g. enum vs string or map vs record).
     */
    private static void checkType(String fieldName, boolean mustBeScalar,
                                  String type, JsonParser jp)
        throws IOException {

        JsonToken tok = jp.getCurrentToken();
        /*
         * This method is not called for numeric values.  Prevent cast from
         * a number to another type, e.g. String
         */
        if (tok.isScalarValue() != mustBeScalar || tok.isNumeric()) {
            throw new IllegalArgumentException
                ("Illegal value for field " + fieldName +
                 ": " + jp.getText() + ". Expected " + type);
        }
    }

    static RecordValueImpl fromAvroValue(FieldDef definition,
                                         Object obj,
                                         Schema schema) {
        Schema recordSchema = getRecordSchema(schema);
        GenericRecord r = (GenericRecord) obj;
        RecordValue record = definition.createRecord();
        RecordDefImpl defImpl = (RecordDefImpl)definition;
        for (Map.Entry<String, FieldMapEntry> entry :
                 defImpl.getFieldMap().getFields().entrySet()) {
            FieldMapEntry fme = entry.getValue();
            String fieldName = entry.getKey();
            Object o = r.get(fieldName);
            if (o != null) {
                Schema fieldSchema = recordSchema.getField(fieldName).schema();
                record.put(fieldName, FieldValueImpl.
                           fromAvroValue(fme.getField(), o, fieldSchema));
            } else {
                record.put(fieldName, fme.getDefaultValue());
            }
        }
        return (RecordValueImpl)record;
    }

    @SuppressWarnings("unchecked")
    static RecordValueImpl fromJavaObjectValue(FieldDef definition,
                                               Object obj) {
        Map<String, Object> javaMap = (Map<String, Object>) obj;
        RecordValue record = definition.createRecord();
        RecordDefImpl defImpl = (RecordDefImpl) definition;
        for (Map.Entry<String, FieldMapEntry> entry : 
                 defImpl.getFieldMap().getFields().entrySet()) {
            FieldMapEntry fme = entry.getValue();
            String fieldName = entry.getKey();
            Object o = javaMap.get(fieldName);
            if (o != null) {
                record.put(fieldName, FieldValueImpl.
                           fromJavaObjectValue(fme.getField(), o));
            } else {
                record.put(fieldName, fme.getDefaultValue());
            }
        }
        return (RecordValueImpl)record;
    }

    /*
     * Handle the fact that this field may be nullable and therefore have a
     * Union schema.
     */
    private static Schema getRecordSchema(Schema schema) {
        return getUnionSchema(schema, Schema.Type.RECORD);
    }

    /**
     * Internal use
     */
    private void putField(String name, FieldValue value) {
        validateIndexField(name);
        valueMap.put(name, value);
    }

    /**
     * TODO: more validation based on definition.
     */
    FieldDefImpl validateNameAndType(String name,
                                     FieldDef.Type type) {
        FieldDef ft = getDefinition(name);
        if (ft == null) {
            throw new IllegalArgumentException("No such field in record " +
                                               getDefinition().getName() + ": " +
                                               name);
        }
        if (ft.getType() != type) {
            throw new IllegalArgumentException
                ("Incorrect type for field " +
                 name + ", type is " + type +
                 ", expected " + ft.getType());
        }
        return (FieldDefImpl) ft;
    }

    /**
     * Create FieldValue instances from Strings that are stored "naturally"
     * for the data type.  This is opposed to the String encoding used for
     * key components.
     */
    private FieldValue create(String value,
                              final FieldDef field1) {
        switch (field1.getType()) {
        case INTEGER:
            return new IntegerValueImpl(Integer.parseInt(value));
        case LONG:
            return new LongValueImpl(Long.parseLong(value));
        case STRING:
            return new StringValueImpl(value);
        case DOUBLE:
            return new DoubleValueImpl(Double.parseDouble(value));
        case FLOAT:
            return new FloatValueImpl(Float.parseFloat(value));
        case BOOLEAN:
            return new BooleanValueImpl(value);
        case ENUM:
            return new EnumValueImpl((EnumDef) field1, value);
        default:
            throw new IllegalArgumentException("Type not yet implemented: " +
                                               field1.getType());
        }
    }

    FieldValueImpl getComplex(TableImpl.TableField tableField) {
        return findFieldValue(tableField.iterator(), -1);
    }

    @Override
    FieldValueImpl findFieldValue(ListIterator<String> fieldPath,
                                  int arrayIndex) {
        assert fieldPath.hasNext();

        FieldValueImpl fv = (FieldValueImpl) get(fieldPath.next());
        if (fv == null || !fieldPath.hasNext()) {
            return fv;
        }
        return fv.findFieldValue(fieldPath, arrayIndex);
    }

    @Override
    FieldValueImpl findFieldValue(ListIterator<String> fieldPath,
                                  String mapKey) {
        if (!fieldPath.hasNext()) {
            throw new IllegalStateException
                ("Bad call to RecordValueImpl.findFieldValue");
        }

        FieldValueImpl fv = (FieldValueImpl) get(fieldPath.next());
        if (fv == null || !fieldPath.hasNext()) {
            return fv;
        }
        return fv.findFieldValue(fieldPath, mapKey);
    }

    @Override
    int numValues() {
        int num = 0;
        for (FieldValue v : valueMap.values()) {
            int numEntries = ((FieldValueImpl) v).numValues();

            /* count empty complex types as a single entry */
            num += (numEntries == 0 ? 1 : numEntries);
        }
        return num;
    }
}
