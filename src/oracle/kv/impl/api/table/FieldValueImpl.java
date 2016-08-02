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
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ListIterator;

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
import org.apache.avro.generic.GenericData;
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
abstract class FieldValueImpl
    implements FieldValue, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * Return a Jackson JsonNode for the instance.
     */
    public abstract JsonNode toJsonNode();

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

    /**
     * The next two methods convert the value to/from its Avro equivalent
     * for ser-deserialization.  Here are the documented mappings from Java that
     * apply:
     *
     * Schema records are implemented as GenericRecord.
     * Schema arrays are implemented as Collection.
     * Schema maps are implemented as Map.
     * Schema strings are implemented as CharSequence.
     * Schema binary (bytes) are implemented as ByteBuffer.
     * Schema ints are implemented as Integer.
     * Schema longs are implemented as Long.
     * Schema doubles are implemented as Double.
     * Schema floats are implemented as Float.
     * Schema booleans are implemented as Boolean.
     * Schema enums are implemented as GenericEnumSymbol.
     * Schema fixed (FixedBinary) are implemented as GenericFixed.
     *
     * The latter two need special mention because when used in a union, which
     * is the default because most fields are nullable, the appropriate schema
     * needs to be used for them in order for union resolution to work properly
     * in Avro (see GenericData.resolveUnion()).  Because of that these types
     * override toAvroValue().
     */

    /**
     * Convert the value to its Avro equivalent
     */
    @SuppressWarnings("unused")
    Object toAvroValue(Schema schema) {
        switch (getType()) {
        case INTEGER:
            return this.asInteger().get();
        case LONG:
            return this.asLong().get();
        case DOUBLE:
            return this.asDouble().get();
        case FLOAT:
            return this.asFloat().get();
        case STRING:
            return this.asString().get();
        case BINARY:
            return ByteBuffer.wrap(this.asBinary().get());
        case BOOLEAN:
            return this.asBoolean().get();
        case FIXED_BINARY:
        case ENUM:
        case RECORD:
        case ARRAY:
        case MAP:
            throw new IllegalArgumentException
                ("Complex classes must override toAvroValue");
        default:
            throw new IllegalStateException
                ("Unknown type in toAvroValue " + getType());
        }
    }

    /**
     * Construct a FieldValue from an Avro Object returned in a deserialized
     * record.  The type of the Object is type-dependent, as is construction
     * of the value object.
     */
    static FieldValue fromAvroValue(FieldDef def,
                                    Object o,
                                    Schema schema) {
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
            return def.createString(((CharSequence)o).toString());
        case BINARY:
            return def.createBinary(((ByteBuffer)o).array());
        case FIXED_BINARY:
            return def.createFixedBinary(((GenericData.Fixed)o).bytes());
        case BOOLEAN:
            return def.createBoolean((Boolean)o);
        case ENUM:
            return def.createEnum(((GenericData.EnumSymbol)o).toString());
        case RECORD:
            return RecordValueImpl.fromAvroValue(def, o, schema);
        case ARRAY:
            return ArrayValueImpl.fromAvroValue(def, o, schema);
        case MAP:
            return MapValueImpl.fromAvroValue(def, o, schema);
        default:
            throw new IllegalArgumentException
                ("Complex classes must override toAvroValue");
        }
    }

    /**
     * Construct a FieldValue from an Java Object.
     */
    static FieldValue fromJavaObjectValue(FieldDef def,
                                          Object o) {
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

    /**
     *
     * @param sb
     */
    abstract public void toStringBuilder(StringBuilder sb);

    /**
     * Return a String representation of the value suitable for use as part of
     * a primary key.  This method must work for any value that can participate
     * in a primary key.  The key string format may be different than a more
     * "natural" string format and may not be easily human readable.  It is
     * defined so that primary key fields sort and compare correctly and
     * consistently.
     */
    @SuppressWarnings("unused")
    public String formatForKey(FieldDef field) {
        throw new IllegalArgumentException
            ("Key components must be atomic types");
    }

    /**
     * Return the "next" legal value for this type in terms of comparison
     * purposes.  That is value.compareTo(value.getNextValue()) is < 0 and
     * there is no legal value such that value < cantHappen < value.getNextValue().
     *
     * This method is only called for indexable fields and is only
     * implemented for types for which FieldDef.isValidIndexField() returns true.
     */
    public FieldValueImpl getNextValue() {
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
    public FieldValueImpl getMinimumValue() {
        throw new IllegalArgumentException
            ("Type does not implement getMinimumValue: " +
             getClass().getName());
    }

    /**
     * Returns the correct schema for the type if schema is for a union.
     * Returns the schema itself if is not a union.
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
    FieldValueImpl findFieldValue(ListIterator<String> fieldPath,
                                  String mapKey) {
        return null;
    }

    /**
     * Returns the number of non-null field values in a FieldValue instance,
     * including nested values.  This is useful for validating IndexKey and
     * PrimaryKey, for example.  Defaults to 1 for simple types.
     */
    int numValues() {
        return 1;
    }

    /**
     * Defines a method to put fields deep into a nested type, implicitly
     * creating intermediate fields if they do not already exist.  This is
     * only used when deserializing IndexKey instances and is not accessible
     * to users.  The default implementation throws.  This will never be called
     * for simple types and complex types must implement it.
     */
    @SuppressWarnings("unused")
    FieldValue putComplex(ListIterator<String> fieldPath, FieldDef.Type type,
                          Object value) {
        throw new IllegalArgumentException("Complex types must implement");
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
}
