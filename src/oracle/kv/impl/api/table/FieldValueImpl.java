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

import java.io.IOException;
import java.io.Serializable;
import java.util.ListIterator;
import java.util.Map;
import oracle.kv.impl.util.JsonUtils;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.BinaryValue;
import oracle.kv.table.BooleanValue;
import oracle.kv.table.DoubleValue;
import oracle.kv.table.EnumValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.FixedBinaryValue;
import oracle.kv.table.FloatValue;
import oracle.kv.table.IndexKey;
import oracle.kv.table.IntegerValue;
import oracle.kv.table.LongValue;
import oracle.kv.table.MapValue;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Row;
import oracle.kv.table.StringValue;

import org.apache.avro.Schema;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectWriter;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.persist.model.Persistent;

/**
 * FieldValueImpl represents a value of a single field.  A value may be simple
 * or complex (single-valued vs multi-valued).  FieldValue is the building
 * block of row values in a table.
 *<p>
 * The FieldValueImpl class itself has no state and serves as an abstract base
 * for implementations of FieldValue and its sub-interfaces.
 */
@Persistent(version=1)
public abstract class FieldValueImpl
    implements FieldValue, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @Override
    public FieldValueImpl clone() {
        try {
            return (FieldValueImpl) super.clone();
        } catch (CloneNotSupportedException ignore) {
        }
        return null;
    }

    @Override
    public int compareTo(FieldValue o) {
        throw new IllegalArgumentException
            ("FieldValueImpl objects must implement compareTo");
    }

    @Override
    public abstract FieldDefImpl getDefinition();

    @Override
    public BinaryValue asBinary() {
        throw new ClassCastException
            ("Field is not a Binary: " + getClass());
    }

    @Override
    public BooleanValue asBoolean() {
        throw new ClassCastException
            ("Field is not a Boolean: " + getClass());
    }

    @Override
    public DoubleValue asDouble() {
        throw new ClassCastException
            ("Field is not a Double: " + getClass());
    }

    @Override
    public FloatValue asFloat() {
        throw new ClassCastException
            ("Field is not a Float: " + getClass());
    }

    @Override
    public IntegerValue asInteger() {
        throw new ClassCastException
            ("Field is not an Integer: " + getClass());
    }

    @Override
    public LongValue asLong() {
        throw new ClassCastException
            ("Field is not a Long: " + getClass());
    }

    @Override
    public StringValue asString() {
        throw new ClassCastException
            ("Field is not a String: " + getClass());
    }

    @Override
    public EnumValue asEnum() {
        throw new ClassCastException
            ("Field is not an Enum: " + getClass());
    }

    @Override
    public FixedBinaryValue asFixedBinary() {
        throw new ClassCastException
            ("Field is not a FixedBinary: " + getClass());
    }

    @Override
    public ArrayValue asArray() {
        throw new ClassCastException
            ("Field is not an Array: " + getClass());
    }

    @Override
    public MapValue asMap() {
        throw new ClassCastException
            ("Field is not a Map: " + getClass());
    }

    @Override
    public RecordValue asRecord() {
        throw new ClassCastException
            ("Field is not a Record: " + getClass());
    }

    @Override
    public Row asRow() {
        throw new ClassCastException
            ("Field is not a Row: " + getClass());
    }

    @Override
    public PrimaryKey asPrimaryKey() {
        throw new ClassCastException
            ("Field is not a PrimaryKey: " + getClass());
    }

    @Override
    public IndexKey asIndexKey() {
        throw new ClassCastException
            ("Field is not an IndexKey: " + getClass());
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isFloat() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public boolean isFixedBinary() {
        return false;
    }

    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public boolean isRecord() {
        return false;
    }

    @Override
    public boolean isRow() {
        return false;
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public boolean isIndexKey() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return false;
    }

    /**
     * Subclasses can override this but it will do a pretty good job of output
     */
    @Override
    public String toJsonString(boolean pretty) {
        if (pretty) {
            ObjectWriter writer = JsonUtils.createWriter(pretty);
            try {
                return writer.writeValueAsString(toJsonNode());
            }
            catch (IOException ioe) {
                return ioe.toString();
            }
        }
        StringBuilder sb = new StringBuilder(128);
        toStringBuilder(sb);
        return sb.toString();
    }

    /*
     * Local methods
     */

    public boolean isTuple() {
        return false;
    }

    public int size() {
        throw new ClassCastException(
            "Value is not complex (array, map, or record): " + getClass());
    }

    public Map<String, FieldValue> getMap() {
        throw new ClassCastException(
            "Value is not a record or map: " + getClass());
    }

    public int getInt() {
        throw new ClassCastException(
            "Value is not an integer or subtype: " + getClass());
    }

    public void setInt(@SuppressWarnings("unused")int v) {
        throw new ClassCastException(
            "Value is not an integer or subtype: " + getClass());
    }

    public long getLong() {
        throw new ClassCastException(
            "Value is not a long or subtype: " + getClass());
    }

    public void setLong(@SuppressWarnings("unused")long v) {
        throw new ClassCastException(
            "Value is not a long or subtype: " + getClass());
    }

    public float getFloat() {
        throw new ClassCastException(
            "Value is not a float or subtype: " + getClass());
    }

    public void setFloat(@SuppressWarnings("unused")float v) {
        throw new ClassCastException(
            "Value is not a float or subtype: " + getClass());
    }

    public double getDouble() {
        throw new ClassCastException(
            "Value is not a double or subtype: " + getClass());
    }

    public void setDouble(@SuppressWarnings("unused")double v) {
        throw new ClassCastException(
            "Value is not a double or subtype: " + getClass());
    }

    public String getString() {
        throw new ClassCastException(
            "Value is not a string or subtype: " + getClass());
    }

    public void setString(@SuppressWarnings("unused")String v) {
        throw new ClassCastException(
            "Value is not a String or subtype: " + getClass());
    }

    public String getEnumString() {
        throw new ClassCastException(
            "Value is not an enum or subtype: " + getClass());
    }

    public void setEnum(@SuppressWarnings("unused")String v) {
        throw new ClassCastException(
            "Value is not an enum or subtype: " + getClass());
    }

    public boolean getBoolean() {
        throw new ClassCastException(
            "Value is not a boolean: " + getClass());
    }

    public void setBoolean(@SuppressWarnings("unused")boolean v) {
        throw new ClassCastException(
            "Value is not a boolean or subtype: " + getClass());
    }

    public byte[] getBytes() {
        throw new ClassCastException(
            "Value is not a binary: " + getClass());
    }

    public FieldValueImpl getElement(
        @SuppressWarnings("unused") int index) {
        throw new ClassCastException(
            "Value is not an array: " + getClass());
    }

    public FieldValueImpl getElement(
        @SuppressWarnings("unused") String fieldName) {
        throw new ClassCastException(
            "Value is not a map: " + getClass());
    }

    public FieldValueImpl getFieldValue(
        @SuppressWarnings("unused") String fieldName) {
        throw new ClassCastException(
            "Value is not a record: " + getClass());
    }

    /**
     * Return the "next" legal value for this type in terms of comparison
     * purposes.  That is value.compareTo(value.getNextValue()) is < 0 and
     * there is no legal value such that value < cantHappen < value.getNextValue().
     *
     * This method is only called for indexable fields and is only
     * implemented for types for which FieldDef.isValidIndexField() returns true.
     */
    FieldValueImpl getNextValue() {
        throw new IllegalArgumentException
            ("Type does not implement getNextValue: " +
             getClass().getName());
    }

    /**
     * Return the minimum legal value for this type in terms of comparison
     * purposes such that there is no value V where value.compareTo(V) > 0.
     *
     * This method is only called for indexable fields and is only
     * implemented for types for which FieldDef.isValidIndexField() returns true.
     */
    FieldValueImpl getMinimumValue() {
        throw new IllegalArgumentException
            ("Type does not implement getMinimumValue: " +
             getClass().getName());
    }

    /**
     * Return a Jackson JsonNode for the instance.
     */
    public abstract JsonNode toJsonNode();

    /**
     *
     * @param sb
     */
    abstract public void toStringBuilder(StringBuilder sb);

    /**
     * Construct a FieldValue from an Java Object.
     */
    static FieldValue fromJavaObjectValue(FieldDef def, Object o) {

        switch (def.getType()) {
        case INTEGER:
            return def.createInteger((Integer)o);
        case LONG:
            return def.createLong((Long)o);
        case DOUBLE:
            return def.createDouble((Double)o);
        case FLOAT:
            return def.createFloat((Float)o);
        case STRING:
            return def.createString((String)o);
        case BINARY:
            return def.createBinary((byte[])o);
        case FIXED_BINARY:
            return def.createFixedBinary((byte[])o);
        case BOOLEAN:
            return def.createBoolean((Boolean)o);
        case ENUM:
            return def.createEnum((String)o);
        case RECORD:
            return RecordValueImpl.fromJavaObjectValue(def, o);
        case ARRAY:
            return ArrayValueImpl.fromJavaObjectValue(def, o);
        case MAP:
            return MapValueImpl.fromJavaObjectValue(def, o);
        default:
            throw new IllegalArgumentException
                ("Complex classes must override fromJavaObjectValue");
        }
    }

    /**
     * Return a String representation of the value suitable for use as part of
     * a primary key.  This method must work for any value that can participate
     * in a primary key.  The key string format may be different than a more
     * "natural" string format and may not be easily human readable.  It is
     * defined so that primary key fields sort and compare correctly and
     * consistently.
     */
    @SuppressWarnings("unused")
    public String formatForKey(FieldDef field, int storageSize) {
        throw new IllegalArgumentException
            ("Key components must be atomic types");
    }

    String formatForKey(FieldDef field) {
        return formatForKey(field, 0);
    }

    /**
     * Returns the number of non-null field values in a FieldValue instance,
     * including nested values. This is useful for validating IndexKey and
     * PrimaryKey, for example. Defaults to 1 for simple types.
     */
    int numValues() {
        return 1;
    }

    /**
     * This method is used to populate/update an IndexKey or a table Row. It
     * takes as input a fieldPath and an atomic value. The fieldPath is supposed
     * to be a path that defines an index field. If an IndexKey is populated or
     * updated, the atomic value is the value to search for in that index field.
     * Otherwise, it is a value extracted from the specified field of an index
     * entry.
     *
     * In either case, the method puts the given value deep into "this",
     * implicitly creating intermediate fields/elements along the given
     * fieldPath, if they do not already exist.
     *
     * Note: the method is recursive and applies to FieldValues of different
     * kinds, but the initial, non-recursive, invocation is always on an
     * IndexKey instance or a Row instance. The createTableRow tells whether
     * it's an IndexKey or a Row.
     */
    FieldValue putComplex(
        ListIterator<String> fieldPath,
        FieldValue value,
        boolean createTableRow) {

        FieldDef.Type type = value.getType();

        switch (getType()) {

        case RECORD: {

            if (isTuple()) {
                throw new IllegalStateException(
                    "Cannot add fields to a TupleValue");
            }

            RecordValueImpl rec = (RecordValueImpl)this;

            String fname = fieldPath.next();

            if (!fieldPath.hasNext()) {
                rec.validateNameAndType(fname, type);
                rec.putField(fname, value);

            } else {
                FieldDefImpl def = rec.getFieldDef(fname);
                if (def == null) {
                    throw new IllegalArgumentException(
                        "Cannot find field named " + fname);
                }

                /* The nested field may have been created already. */
                FieldValueImpl val = (FieldValueImpl) rec.get(fname);
                if (val == null) {
                    val = createComplexValue(def);
                }

                rec.putField(fname,
                             val.putComplex(fieldPath, value, createTableRow));
            }
            return this;
        }
        case ARRAY: {
            ArrayValueImpl arr = (ArrayValueImpl)this;
            FieldDefImpl elemDef = arr.getElementDef();

            if (arr.size() > 1) {
                throw new IllegalArgumentException(
                    "Array encountered during construction/update of IndexKey " +
                    "must have at most one element. Array value : \n" + this);
            }

            if (!fieldPath.hasNext()) {

                if (arr.size() == 0) {
                    arr.add(value);
                } else {
                    arr.set(0, value);
                }
                return this;
            }

            String fname = fieldPath.next();

            if (fname.equals(TableImpl.ANONYMOUS)) {
                if (!fieldPath.hasNext()) {
                    if (arr.size() == 0) {
                        arr.add(value);
                    } else {
                        arr.set(0, value);
                    }
                    return this;
                }
            } else {
                fieldPath.previous();
            }

            ComplexValueImpl val = null;

            if (arr.size() == 0) {
                val = createComplexValue(elemDef);
                arr.add(val);

            } else {

                if (!(arr.get(0) instanceof ComplexValueImpl)) {
                    throw new IllegalArgumentException(
                        "Invalid attempt to overwrite an atomic " +
                        "element with a complex element in an array " +
                        "encountered during construction/update of " +
                        "IndexKey. Array value : \n" + this);
                }

                val = (ComplexValueImpl) arr.get(0);
            }

            val.putComplex(fieldPath, value, createTableRow);
            return this;
        }
        case MAP: {
            MapValueImpl map = (MapValueImpl)this;
            FieldDefImpl elemDef = map.getElementDef();

            String fname = fieldPath.next();

            if (MapDefImpl.isMapKeyTag(fname)) {

                assert(value instanceof StringValueImpl);

                map.putNull(((StringValueImpl) value).toString());
                return this;
            }

            if (fname.equals(TableImpl.ANONYMOUS) && createTableRow) {
                String mapKey = map.getMapKey();
                if (mapKey != null) {
                    fname = mapKey;
                }
            }

            /*
             * NOTE: at this point fname may be a normal key string or the
             * special "[]" string.  In the latter case an entry will be
             * created using "[]" as the key string. This is normal for
             * indexes on map values.
             */
            if (!fieldPath.hasNext()) {

                /*
                 * There are no more components.  Put a map entry using fname as
                 * the key and create the value using the specified type and Object.
                 */
                if (type != elemDef.getType()) {
                    throw new IllegalStateException(
                        "Incorrect type for map.  Expected " +
                        elemDef.getType() + ", received " + type);
                }

                map.put(fname, value);

            } else {

                /* Create the field if it's not been created. */
                FieldValueImpl val = (FieldValueImpl) map.get(fname);
                if (val == null ||
                    (createTableRow && val.isNull())) {
                    val = createComplexValue(elemDef);
                }

                /*
                 * Put an entry based on the value just constructed, based
                 * on the iterator, type, and value state.
                 */
                map.put(fname, val.putComplex(fieldPath, value, createTableRow));
            }
            return this;
        }
        default:
            throw new IllegalArgumentException(
                "Cannot put a value in an atomic value");
        }
    }

    static private ComplexValueImpl createComplexValue(FieldDef def) {
        switch (def.getType()) {
        case MAP:
            return (ComplexValueImpl) def.createMap();
        case RECORD:
            return (ComplexValueImpl) def.createRecord();
        case ARRAY:
            return (ComplexValueImpl) def.createArray();
        default:
            throw new IllegalArgumentException(
                "Not a complex type: " + def.getType());
        }
    }

    /**
     * Returns the FieldValue associated with the list of names in the Iterator,
     * starting with this FieldValue and the name at the specified index.
     *
     * This is used to parse dot notation for navigating fields within complex
     * field types such as Record.  Simple types don't support navigation so the
     * default implementation returns null.
     *
     * @param iter
     *
     * @param arrayIndex is >= 0 if there is an array navigation to be done
     */
    FieldValueImpl findFieldValue(ListIterator<String> iter, int arrayIndex) {
        return null;
    }

    /**
     * Returns the FieldValue associated with the list of names in the Iterator,
     * starting with this FieldValue and the name at the specified index.  This
     * method is used primarily by index key serialization.
     *
     * This is used to parse dot notation for navigating fields within complex
     * field types such as Record.  Simple types don't support navigation so the
     * default implementation returns null.
     *
     * @param fieldPath
     *
     * @param mapKey if non-null there may be a map involved and this key is
     * used to get the appropriate entry.
     */
    FieldValueImpl findFieldValue(
        ListIterator<String> fieldPath,
        String mapKey) {

        return null;
    }

    protected static Object readTuple(FieldDef def, TupleInput in) {
        switch (def.getType()) {
        case INTEGER:
            return in.readSortedPackedInt();
        case STRING:
            return in.readString();
        case LONG:
            return in.readSortedPackedLong();
        case DOUBLE:
            return in.readSortedDouble();
        case FLOAT:
            return in.readSortedFloat();
        case ENUM:
            return in.readSortedPackedInt();
        default:
            throw new IllegalStateException
                ("Type not supported in indexes: " +
                 def.getType());
        }
    }

    /**
     * If schema is for a union, returns the union memeber schema that matches
     * the given type. Otherwise, returns the schema itself.
     */
    static Schema getUnionSchema(Schema schema, Schema.Type type) {
        if (schema.getType() == Schema.Type.UNION) {
            for (Schema s : schema.getTypes()) {
                if (s.getType() == type) {
                    return s;
                }
            }
            throw new IllegalArgumentException
                ("Cannot find type in union schema: " + type);
        }
        return schema;
    }

    public int castAsInt() {
        throw new ClassCastException(
            "Value can not be cast to an integer: " + getClass());
    }

    public long castAsLong() {
        throw new ClassCastException(
            "Value can not be cast to a long: " + getClass());
    }

    public float castAsFloat() {
        throw new ClassCastException(
            "Value can not be cast to a float: " + getClass());
    }

    public double castAsDouble() {
        throw new ClassCastException(
            "Value can not be cast to a double: " + getClass());
    }

    public String castAsString() {
        throw new ClassCastException(
            "Value can not be cast to a String: " + getClass());
    }
}
