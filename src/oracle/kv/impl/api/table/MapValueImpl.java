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
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import oracle.kv.table.ArrayValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.MapDef;
import oracle.kv.table.MapValue;
import oracle.kv.table.RecordValue;

import org.apache.avro.Schema;
import org.apache.avro.util.Utf8;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.sleepycat.persist.model.Persistent;

/**
 * MapValueImpl implements the MapValue interface and is a container object
 * that holds a map of FieldValue objects all of the same type.  The getters
 * and setters use the same semantics as Java Map.
 */
@Persistent(version=1)
class MapValueImpl extends ComplexValueImpl implements MapValue {
    private static final long serialVersionUID = 1L;
    private final Map<String, FieldValue> fields;
    private static final StringDefImpl stringDef = new StringDefImpl();

    MapValueImpl(MapDef field) {
        super(field);
        fields = new TreeMap<String, FieldValue>(FieldComparator.instance);
    }

    /* DPL */
    private MapValueImpl() {
        super(null);
        fields = null;
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public FieldValue remove(String fieldName) {
        return fields.remove(fieldName);
    }

    @Override
    public FieldValue get(String fieldName) {
        return fields.get(fieldName);
    }

    @Override
    public MapValue put(String name, int value) {
        validate(name, FieldDef.Type.INTEGER);
        fields.put(name, getElement().createInteger(value));
        return this;
    }

    @Override
    public MapValue put(String name, long value) {
        validate(name, FieldDef.Type.LONG);
        fields.put(name, getElement().createLong(value));
        return this;
    }

    @Override
    public MapValue put(String name, String value) {
        validate(name, FieldDef.Type.STRING);
        fields.put(name, getElement().createString(value));
        return this;
    }

    @Override
    public MapValue put(String name, double value) {
        validate(name, FieldDef.Type.DOUBLE);
        fields.put(name, getElement().createDouble(value));
        return this;
    }

    @Override
    public MapValue put(String name, float value) {
        validate(name, FieldDef.Type.FLOAT);
        fields.put(name, getElement().createFloat(value));
        return this;
    }

    @Override
    public MapValue put(String name, boolean value) {
        validate(name, FieldDef.Type.BOOLEAN);
        fields.put(name, getElement().createBoolean(value));
        return this;
    }

    @Override
    public MapValue put(String name, byte[] value) {
        validate(name, FieldDef.Type.BINARY);
        fields.put(name, getElement().createBinary(value));
        return this;
    }

    @Override
    public MapValue putNull(String name) {
        fields.put(name, NullValueImpl.getInstance());
        return this;
    }

    @Override
    public MapValue putFixed(String name, byte[] value) {
        validate(name, FieldDef.Type.FIXED_BINARY);
        fields.put(name, getElement().createFixedBinary(value));
        return this;
    }

    @Override
    public MapValue putEnum(String name, String value) {
        validate(name, FieldDef.Type.ENUM);
        fields.put(name, getElement().createEnum(value));
        return this;
    }

    /**
     * This version is used internally for index deserialization.  Enums are
     * stored as an integer index into the enumeration values in indexes.
     */
    MapValue putEnum(String name, int index) {
        validate(name, FieldDef.Type.ENUM);
        fields.put(name, ((EnumDefImpl)getElement()).createEnum(index));
        return this;
    }

    @Override
    public MapValue put(String fieldName, FieldValue value) {
        /*
         * TODO: this validation needs to be expanded and shared with
         * ArrayValue at least
         */
        if (!getElement().isType(value.getType())) {
            throw new IllegalArgumentException
                ("Incorrect type for map");
        }
        validateIndexField(fieldName);
        fields.put(fieldName, value);
        return this;
    }

    @Override
    public RecordValueImpl putRecord(String fieldName) {
        validateIndexField(fieldName);
        RecordValue val = getElement().createRecord();
        fields.put(fieldName, val);
        return (RecordValueImpl) val;
    }

    @Override
    public MapValueImpl putMap(String fieldName) {
        validateIndexField(fieldName);
        MapValue val = getElement().createMap();
        fields.put(fieldName, val);
        return (MapValueImpl) val;
    }

    @Override
    public ArrayValueImpl putArray(String fieldName) {
        validateIndexField(fieldName);
        ArrayValue val = getElement().createArray();
        fields.put(fieldName, val);
        return (ArrayValueImpl) val;
    }

    /**
     * Override ComplexValue.getDefinition() to return MapDef
     */
    @Override
    public MapDefImpl getDefinition() {
        return (MapDefImpl) super.getDefinition();
    }

    @Override
    public FieldDef.Type getType() {
        return FieldDef.Type.MAP;
    }

    @Override
    public MapValueImpl clone() {
        MapValueImpl map = new MapValueImpl(getDefinition());
        for (Map.Entry<String, FieldValue> entry : fields.entrySet()) {
            /* Maps can have null entries for key-only index lookups */
            if (entry.getValue().isNull()) {
                map.putNull(entry.getKey());
            } else {
                map.put(entry.getKey(), entry.getValue().clone());
            }
        }
        return map;
    }

    @Override
    public boolean isMap() {
        return true;
    }

    @Override
    public MapValue asMap() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MapValueImpl) {
            MapValueImpl otherValue = (MapValueImpl) other;
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
                for (Map.Entry<String, FieldValue> entry : fields.entrySet()) {
                    if (!entry.getValue().
                        equals(otherValue.get(entry.getKey()))) {
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
        for (Map.Entry<String, FieldValue> entry : fields.entrySet()) {
            code += entry.getKey().hashCode() + entry.getValue().hashCode();
        }
        return code;
    }

    /**
     * FieldDef must match.
     *
     * Compare field values in order of keys.  The algorithm relies on the fact
     * that fields is a SortedMap (TreeMap).  Return as soon as there is a
     * difference. If this object has a field the other does not, return > 0.
     * If this object is missing a field the other has, return < 0.  Compare
     * both keys and values, keys first.
     */
    @Override
    public int compareTo(FieldValue other) {
        if (other instanceof MapValueImpl) {
            MapValueImpl otherImpl = (MapValueImpl) other;
            if (!getDefinition().equals(otherImpl.getDefinition())) {
                throw new IllegalArgumentException
                    ("Cannot compare MapValues with different definitions");
            }
            /* this relies on the maps being sorted */
            assert fields instanceof TreeMap;
            assert otherImpl.fields instanceof TreeMap;

            Iterator<String> keyIter = fields.keySet().iterator();
            Iterator<String> otherIter = otherImpl.fields.keySet().iterator();

            while (keyIter.hasNext() && otherIter.hasNext()) {
                String key = keyIter.next();
                String otherKey = otherIter.next();
                int keyCompare = key.compareTo(otherKey);
                if (keyCompare != 0) {
                    return keyCompare;
                }
                /*
                 * Keys are equal, values must exist.
                 */
                FieldValue val = fields.get(key);
                FieldValue otherVal = otherImpl.fields.get(key);
                int valCompare = val.compareTo(otherVal);
                if (valCompare != 0) {
                    return valCompare;
                }
            }

            /*
             * The object with more keys is greater, otherwise they are equal.
             */
            if (keyIter.hasNext()) {
                return 1;
            } else if (otherIter.hasNext()) {
                return -1;
            }
            return 0;
        }
        throw new ClassCastException
            ("Object is not a MapValue");
    }

    /**
     * Map is represented as ObjectNode.  Jackson does not have a MapNode
     */
    @Override
    public JsonNode toJsonNode() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, FieldValue> entry : fields.entrySet()) {
            node.put(entry.getKey(),
                     ((FieldValueImpl)entry.getValue()).toJsonNode());
        }
        return node;
    }

    @Override
    public void toStringBuilder(StringBuilder sb) {
        sb.append('{');
        int i = 0;
        for (Map.Entry<String, FieldValue> entry : fields.entrySet()) {
            String key = entry.getKey();
            FieldValueImpl val = (FieldValueImpl)entry.getValue();
            if (val != null) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append('\"');
                sb.append(key);
                sb.append('\"');
                sb.append(':');
                val.toStringBuilder(sb);
                i++;
            }
        }
        sb.append('}');
    }

    @Override
    public Map<String, FieldValue> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    Map<String, FieldValue> getFieldsInternal() {
        return fields;
    }

    /**
     * This method operates on a MapValueImpl inside an IndexKey.  Such
     * instances are constrained to only contain map entries that are valid
     * for use in an index key and index scan.  An index involving a map has
     * several permutations:
     * 1.  index on the map's key (map._key)
     * 2.  index on the map's value (map.[]
     * 3.  index on both key and value (map._key, map.[])
     *
     * When there's an index on the key the user creates an IndexKey and sets
     * the desired key string to null -- map.putNull("keyToScan").
     * When there's an index on the value the user creates a special entry
     * with the key "[]" which contains the desired value
     * -- map.put("[]", desiredValue).  The "[]" string is the constant,
     * MapValue.ANONYMOUS.
     *
     * This method is simply trying to find a "key" to use for an index
     * scan.  It may or may not be present.  If the map has only one
     * entry it returns that entry.  If it has 2 then it returns the one that
     * is not equal to [], if present.  If it has more than 2 then it's not
     * valid as an index key and null is returned.  Error states are handled by
     * callers.  These are generally an invalid IndexKey or failure to find
     * matching values in the index scan.
     */
    String getMapKey() {
        Set<String> mapKeys = fields.keySet();
        if (mapKeys.size() == 1) {
            for (String mapKey : mapKeys) {
                return mapKey;
            }
        }

        /* if there are 2 entries, skip "[]" */
        if (mapKeys.size() == 2) {
            Iterator<String> keyIter = mapKeys.iterator();
            while (keyIter.hasNext()) {
                String keyVal = keyIter.next();
                if (MapDefImpl.isMapValueTag(keyVal)) {
                    continue;
                }
                return keyVal;
            }
        }

        return null;
    }

    /**
     * The Avro Generic interface for maps takes a Java Map<String, Object> but
     * the Object must cast to the appropriate type based on Avro's mappings
     * of Java objects to Avro values.
     */
    @Override
    Object toAvroValue(Schema schema) {
        Schema valueSchema = getValueSchema(schema);
        Map<String, Object> newMap =
            new TreeMap<String, Object>(FieldComparator.instance);
        for (Map.Entry<String, FieldValue> entry :
                 getFieldsInternal().entrySet()) {
            newMap.put(entry.getKey(),
                       ((FieldValueImpl)entry.getValue()).
                       toAvroValue(valueSchema));
        }
        return newMap;
    }

    /*
     * Handle the fact that this field may be nullable and therefore have a
     * Union schema.
     */
    private static Schema getValueSchema(Schema schema) {
        return getUnionSchema(schema, Schema.Type.MAP).getValueType();
    }

    @SuppressWarnings("unchecked")
    static MapValueImpl fromAvroValue(FieldDef def,
                                      Object o,
                                      Schema schema) {
        Map<Utf8, Object> avroMap = (Map<Utf8, Object>) o;
        MapValue map = def.createMap();
        for (Map.Entry<Utf8, Object> entry : avroMap.entrySet()) {
            String key = entry.getKey().toString();
            map.put(key, FieldValueImpl.
                    fromAvroValue(map.getDefinition().getElement(),
                                  entry.getValue(),
                                  getValueSchema(schema)));
        }
        return (MapValueImpl)map;
    }

    @SuppressWarnings("unchecked")
    static MapValueImpl fromJavaObjectValue(FieldDef def, Object o) {
        Map<String, Object> javaMap = (Map<String, Object>) o;
        MapValue map = def.createMap();
        for (Map.Entry<String, Object> entry : javaMap.entrySet()) {
            String key = entry.getKey().toString();
            map.put(key, FieldValueImpl.
                    fromJavaObjectValue(map.getDefinition().getElement(),
                                        entry.getValue()));
        }
        return (MapValueImpl)map;
    }

    /**
     * Add JSON fields to the map.
     */
    @Override
    void addJsonFields(JsonParser jp, boolean isIndexKey,
                       String currentFieldName, boolean exact) {
        try {
            FieldDef element = getElement();
            JsonToken t = jp.getCurrentToken();
            if(t == null) { /* JSON is empty */
                return;
            }
            assert(t == JsonToken.START_OBJECT);
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jp.getCurrentName();
                JsonToken token = jp.nextToken();

                if (isIndexKey) {
                    String lower = fieldname.toLowerCase();
                    if (lower.startsWith(TableImpl.ELEMENTOF)) {
                        /*
                         * Translate elementof(mapFieldName) to ANONYMOUS to
                         * allow hiding of the ANONYMOUS string in JSON input
                         * for index keys.
                         */
                        validateElementOfString(lower, currentFieldName);
                        fieldname = MapValue.ANONYMOUS;
                    }
                }

                /*
                 * Handle null.  If used in a Row it is illegal; however,
                 * it is legal in an index key.
                 */
                if (token == JsonToken.VALUE_NULL) {
                    if (isIndexKey) {
                        putNull(fieldname);
                        continue;
                    }
                    throw new IllegalArgumentException
                        ("Invalid null value in JSON input for field "
                         + fieldname);
                }

                switch (element.getType()) {
                case INTEGER:
                    put(fieldname, jp.getIntValue());
                    break;
                case LONG:
                    put(fieldname, jp.getLongValue());
                    break;
                case DOUBLE:
                    put(fieldname, jp.getDoubleValue());
                    break;
                case FLOAT:
                    put(fieldname, jp.getFloatValue());
                    break;
                case STRING:
                    put(fieldname, jp.getText());
                    break;
                case BINARY:
                    put(fieldname, jp.getBinaryValue());
                    break;
                case FIXED_BINARY:
                    putFixed(fieldname, jp.getBinaryValue());
                    break;
                case BOOLEAN:
                    put(fieldname, jp.getBooleanValue());
                    break;
                case ARRAY:
                    /*
                     * current token is '[', then array elements
                     * TODO: need to have a full-on switch for adding
                     * array elements of the right type.
                     */
                    ArrayValueImpl array = putArray(fieldname);
                    array.addJsonFields(jp, isIndexKey, null, exact);
                    break;
                case MAP:
                    MapValueImpl map = putMap(fieldname);
                    map.addJsonFields(jp, isIndexKey, null, exact);
                    break;
                case RECORD:
                    RecordValueImpl record = putRecord(fieldname);
                    record.addJsonFields(jp, isIndexKey, null, exact);
                    break;
                case ENUM:
                    putEnum(fieldname, jp.getText());
                    break;
                }
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException
                (("Failed to parse JSON input: " + ioe.getMessage()), ioe);
        }
    }

    /**
     * Validates that the format of the elementOfString conforms to
     * "elementof(mapFieldName)" without any trailing data.
     */
    private static void validateElementOfString(String elementOfString,
                                                String currentFieldName) {
        if (currentFieldName != null && elementOfString
            .endsWith(currentFieldName.toLowerCase() + ")")) {
                return;
        }
        throw new IllegalArgumentException
            ("Invalid use of elementof(mapFieldPath) when constructing an " +
             "IndexKey from JSON.  The mapFieldPath must contain the path " +
             "to a map field and there can be no text after the expression: " +
             elementOfString);
    }

    /**
     * Clears the map.
     */
    void clearMap() {
        fields.clear();
    }

    /*
     * internals
     */
    private FieldDef getElement() {
        return getDefinition().getElement();
    }

    private void validate(String name, FieldDef.Type type) {
        if (!getElement().isType(type)) {
            throw new IllegalArgumentException
                ("Incorrect type for map");
        }
        validateIndexField(name);
    }

    @Override
    FieldValueImpl findFieldValue(ListIterator<String> fieldPath,
                                  int arrayIndex) {
        assert fieldPath.hasNext();

        String next = fieldPath.next();

        /*
         * This method is called when validating index keys.  If the key
         * is the target, just validate that there's a single entry
         * and return the value based on that key.
         */
        if (MapDefImpl.isMapKeyTag(next)) {

            /*
             * this call path comes from IndexImpl.findMapKey().
             */
            if (size() != 1) {

                /*
                 * If the map has 2 entries and the other is a "[]" entry
                 * then this map is in an index  with both key and value
                 * defined.
                 */
                if (size() != 2 || !fields.containsKey(ANONYMOUS)) {
                    throw new IllegalArgumentException
                        ("Index keys for maps can contain only one entry");
                }
            }

            /*
             * Never return the key matching "[]" as it's not useful, so
             * if it is present, skip it.
             */
            Iterator<String> keyIter = fields.keySet().iterator();
            while (keyIter.hasNext()) {
                String keyVal = keyIter.next();
                if (MapDefImpl.isMapValueTag(keyVal)) {
                    continue;
                }
                return stringDef.createString(keyVal);
            }
            return null;
        }

        FieldValueImpl fv = (FieldValueImpl) get(next);
        if (fv == null || !fieldPath.hasNext()) {
            return fv;
        }
        return fv.findFieldValue(fieldPath, arrayIndex);
    }

    /**
     * This method is used to find fields mostly during index serialization,
     * and has to handle the special field components "_key" and "[]"
     * and return the correct information for each.
     *
     * It is used both in serializing index keys extracted on the server side
     * and in serializing index keys used in the client for index scan operations.
     * In the former case mapKey will always be non-null because it's extracting
     * an entry for each map entry. In addition the mapKey will never be "_key"
     * or "[]" because they are not valid map key strings.
     *
     * In the latter case the map may have null entries for the "_key" index
     * component and it may have entries using the "[]" key for value
     * index components.
     *
     * If the index field component is "_key" then a StringValue based on
     * the mapKey is returned.
     *
     * If the index field component is "[]" then mapKey may be null, in
     * which case "[]" is used as the key.
     */
    @Override
    FieldValueImpl findFieldValue(ListIterator<String> fieldPath,
                                  String mapKey) {
        assert fieldPath.hasNext();
        String next = fieldPath.next();

        /*
         * If the field is the _key, return a string value based on the map key.
         */
        if (MapDefImpl.isMapKeyTag(next)) {
            return stringDef.createString(mapKey);
        }

        /*
         * If the key is null and the component is [], use "[]" as the
         * key.  This will happen in IndexKeys and FieldRanges, never when
         * extracting index keys on the server.
         */
        if (MapDefImpl.isMapValueTag(next) && (mapKey == null)) {
                mapKey = next;
        }

        FieldValueImpl fv = (FieldValueImpl) get(mapKey);

        /*
         * Null values are never serializable, they are equivalent to null.
         */
        if (fv != null && fv.isNull()) {
                fv = null;
        }

        if (fv == null || !fieldPath.hasNext()) {
            return fv;
        }
        return fv.findFieldValue(fieldPath, mapKey);
    }

    @Override
    int numValues() {

        /* always count at least one entry to trigger a validation failure */
        if (fields.isEmpty()) {
            return 1;
        }
        int num = 0;
        for (FieldValue v : fields.values()) {
            num += ((FieldValueImpl) v).numValues();
        }
        return num;
    }

    /**
     * See FieldValueImpl.putComplex() for more information.
     *
     * Used for construction of nested values, creating intermediate fields as
     * required.  Used primarily for deserializing index keys where the Object
     * values are being read from the serialized (byte[]) index key.
     *
     * There are several cases that are handled in this method:
     * 1.  put a map key.  In this case the current iterator component will be
     *   "_key" and the value will be a StringValueImpl.  Put a null value using
     *   the StringValue as the key.
     * 2.  put of a field in the field's element

     * key string or it may be the field name of a record, if the map is a map
     * of records.  These two cases are distinguished by knowing that if the
     * the map field is a record, and that record has a field with the next name
     * in the field's path, then the value Object is part of the record and not
     * the map.
     *
     * @param fieldPath the iterator over the path used to locate the target
     * field
     * @param type the type of the Object
     * @param value the value to be used. This may be an atomic type such as
     *              Integer or String, but it can also be FieldValue or null,
     *              as some types (Map and Array) don't use the value in
     *              construction.
     */
    @Override
    FieldValueImpl putComplex(ListIterator<String> fieldPath,
                              FieldDef.Type type, Object value) {
        FieldDefImpl def = (FieldDefImpl) getElement();
        String fname = fieldPath.next();
        if (MapDefImpl.isMapKeyTag(fname) &&
            (value instanceof StringValueImpl)) {
            putNull(((StringValueImpl) value).toString());
            return this;
        }

        /*
         * NOTE: at this point fname may be a normal key string or the
         * special "[]" string.  In the latter case an entry will be
         * created using "[]" as the key string.  This is normal for
         * indexes on map values.
         */
        if (!fieldPath.hasNext()) {

            /*
             * There are no more components.  Put a map entry using fname as
             * the key and create the value using the specified type and Object.
             */
            if (type != def.getType()) {
                throw new IllegalStateException
                    ("Incorrect type for map.  Expected " +
                     def.getType() + ", received " + type);
            }
            put(fname, def.createValue(type, value));
        } else {

            /*
             * Create the field if it's not been created.
             */
            FieldValueImpl val = (FieldValueImpl) get(fname);
            if (val == null) {
                val = createComplexValue(def);
            }

            /*
             * Put an entry based on the value just constructed, based
             * on the iterator, type, and value state.
             */
            put(fname, val.putComplex(fieldPath, type, value));
        }
        return this;
    }

    @Override
    public FieldValueImpl getMinimumValue() {
        throw new IllegalArgumentException
            ("Type does not implement getMinimumValue: " +
             getClass().getName());
    }
}
