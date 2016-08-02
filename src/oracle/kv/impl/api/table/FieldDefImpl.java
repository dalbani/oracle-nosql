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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ListIterator;

import com.sleepycat.persist.model.Persistent;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.util.JsonUtils;
import oracle.kv.table.AnyAtomicDef;
import oracle.kv.table.AnyDef;
import oracle.kv.table.AnyRecordDef;
import oracle.kv.table.ArrayDef;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.BinaryDef;
import oracle.kv.table.BinaryValue;
import oracle.kv.table.BooleanDef;
import oracle.kv.table.BooleanValue;
import oracle.kv.table.DoubleDef;
import oracle.kv.table.DoubleValue;
import oracle.kv.table.EnumDef;
import oracle.kv.table.EnumValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.FixedBinaryDef;
import oracle.kv.table.FixedBinaryValue;
import oracle.kv.table.FloatDef;
import oracle.kv.table.FloatValue;
import oracle.kv.table.IntegerDef;
import oracle.kv.table.IntegerValue;
import oracle.kv.table.LongDef;
import oracle.kv.table.LongValue;
import oracle.kv.table.MapDef;
import oracle.kv.table.MapValue;
import oracle.kv.table.RecordDef;
import oracle.kv.table.RecordValue;
import oracle.kv.table.StringDef;
import oracle.kv.table.StringValue;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import static oracle.kv.impl.api.table.TableJsonUtils.BOOLEAN;
import static oracle.kv.impl.api.table.TableJsonUtils.BYTES;
import static oracle.kv.impl.api.table.TableJsonUtils.DESC;
import static oracle.kv.impl.api.table.TableJsonUtils.DOUBLE;
import static oracle.kv.impl.api.table.TableJsonUtils.FLOAT;
import static oracle.kv.impl.api.table.TableJsonUtils.INT;
import static oracle.kv.impl.api.table.TableJsonUtils.LONG;
import static oracle.kv.impl.api.table.TableJsonUtils.STRING;
import static oracle.kv.impl.api.table.TableJsonUtils.TYPE;

/**
 * Implements FieldDef
 */
@Persistent(version=1)
public abstract class FieldDefImpl implements FieldDef, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    public static final IntegerDefImpl integerDef =  new IntegerDefImpl();
    public static final LongDefImpl longDef =  new LongDefImpl();
    public static final FloatDefImpl floatDef = new FloatDefImpl();
    public static final DoubleDefImpl doubleDef = new DoubleDefImpl();
    public static final StringDefImpl stringDef = new StringDefImpl();
    public static final BooleanDefImpl booleanDef = new BooleanDefImpl();
    public static final BinaryDefImpl binaryDef = new BinaryDefImpl();

    public static final AnyDefImpl anyDef = new AnyDefImpl();
    public static final AnyRecordDefImpl anyRecordDef = new AnyRecordDefImpl();
    public static final AnyAtomicDefImpl anyAtomicDef = new AnyAtomicDefImpl();
    public static final EmptyDefImpl emptyDef = new EmptyDefImpl();

    /*
     * Immutable properties.
     */
    final private Type type;

    private String description;

    /**
     * Convenience constructor.
     */
    FieldDefImpl(Type type) {
        this(type, null);
    }

    FieldDefImpl(Type type, String description) {
        this.type = type;
        this.description = description;
    }

    FieldDefImpl(FieldDefImpl impl) {
        type = impl.type;
        description = impl.description;
    }

    FieldDefImpl() {
        type = null;
        description = null;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public FieldDefImpl clone() {
        try {
            return (FieldDefImpl) super.clone();
        } catch (CloneNotSupportedException ignore) {
        }
        return null;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        throw new IllegalStateException(
            "Classes that implement FieldDefImpl must override equals");
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isType(FieldDef.Type type1) {
        return this.type == type1;
    }

    /**
     * Return true if this type can participate in a primary key.
     * Only simple fields can be part of a key.  Boolean type is not
     * allowed in keys (TODO: is there a valid case for this?).
     */
    @Override
    public boolean isValidKeyField() {
        return false;
    }

    @Override
    public boolean isValidIndexField() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isAny() {
        return false;
    }

    @Override
    public boolean isAnyRecord() {
        return false;
    }

    @Override
    public boolean isAnyAtomic() {
        return false;
    }

    public boolean isWildcard() {
        return (isAny() || isAnyRecord() || isAnyAtomic());
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public boolean isLong() {
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
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isFixedBinary() {
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
    public boolean isEnum() {
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

    @Override
    public AnyDef asAny() {
        throw new ClassCastException
            ("Type is not Any: " + getClass());
    }

    @Override
    public AnyRecordDef asAnyRecord() {
        throw new ClassCastException
            ("Type is not AnyRecord: " + getClass());
    }

    @Override
    public AnyAtomicDef asAnyAtomic() {
        throw new ClassCastException
            ("Type is not AnyAtomic: " + getClass());
    }

    @Override
    public BinaryDef asBinary() {
        throw new ClassCastException
            ("Type is not a Binary: " + getClass());
    }

    @Override
    public FixedBinaryDef asFixedBinary() {
        throw new ClassCastException
            ("Type is not a FixedBinary: " + getClass());
    }

    @Override
    public BooleanDef asBoolean() {
        throw new ClassCastException
            ("Type is not a Boolean: " + getClass());
    }

    @Override
    public DoubleDef asDouble() {
        throw new ClassCastException
            ("Type is not a Double: " + getClass());
    }

    @Override
    public FloatDef asFloat() {
        throw new ClassCastException
            ("Type is not a Float: " + getClass());
    }

    @Override
    public IntegerDef asInteger() {
        throw new ClassCastException
            ("Type is not an Integer: " + getClass());
    }

    @Override
    public LongDef asLong() {
        throw new ClassCastException
            ("Type is not a Long: " + getClass());
    }

    @Override
    public StringDef asString() {
        throw new ClassCastException
            ("Type is not a String: " + getClass());
    }

    @Override
    public EnumDef asEnum() {
        throw new ClassCastException
            ("Type is not an Enum: " + getClass());
    }

    @Override
    public ArrayDef asArray() {
        throw new ClassCastException
            ("Type is not an Array: " + getClass());
    }

    @Override
    public MapDef asMap() {
        throw new ClassCastException
            ("Type is not a Map: " + getClass());
    }

    @Override
    public RecordDef asRecord() {
        throw new ClassCastException
            ("Type is not a Record: " + getClass());
    }

    @Override
    public ArrayValue createArray() {
        throw new ClassCastException
            ("Type is not an Array: " + getClass());
    }

    @Override
    public BinaryValue createBinary(byte[] value) {
        throw new ClassCastException
            ("Type is not a Binary: " + getClass());
    }

    @Override
    public FixedBinaryValue createFixedBinary(byte[] value) {
        throw new ClassCastException
            ("Type is not a FixedBinary: " + getClass());
    }

    @Override
    public BooleanValue createBoolean(boolean value) {
        throw new ClassCastException
            ("Type is not a Boolean: " + getClass());
    }

    @Override
    public DoubleValue createDouble(double value) {
        throw new ClassCastException
            ("Type is not a Double: " + getClass());
    }

    @Override
    public FloatValue createFloat(float value) {
        throw new ClassCastException
            ("Type is not a Float: " + getClass());
    }

    @Override
    public EnumValue createEnum(String value) {
        throw new ClassCastException
            ("Type is not an Enum: " + getClass());
    }

    @Override
    public IntegerValue createInteger(int value) {
        throw new ClassCastException
            ("Type is not an Integer: " + getClass());
    }

    @Override
    public LongValue createLong(long value) {
        throw new ClassCastException
            ("Type is not a Long: " + getClass());
    }

    @Override
    public MapValue createMap() {
        throw new ClassCastException
            ("Type is not a Map: " + getClass());
    }

    @Override
    public RecordValue createRecord() {
        throw new ClassCastException
            ("Type is not a Record: " + getClass());
    }

    @Override
    public StringValue createString(String value) {
        throw new ClassCastException
            ("Type is not a String: " + getClass());
    }

    /*
     * Common utility to compare objects for equals() overrides.  It handles
     * the fact that one or both objects may be null.
     */
    boolean compare(Object o, Object other) {
        if (o != null) {
            return o.equals(other);
        }
        return (other == null);
    }

    public void setDescription(String descr) {
        description = descr;
    }

    /**
     * An internal interface for those fields which have a special encoding
     * length.  By default an invalid value is returned.  This is mostly useful
     * for testing.  It is only used by Integer and Long.
     */
    int getEncodingLength() {
        return -1;
    }

    /*
     * A "precise" type is a type that is fully specified, ie, it is not one of
     * the "any" types and, for complext types, it does not contain any of the
     * "any" types.
     */
    public boolean isPrecise() {
        return true;
    }

    public boolean hasMin() {
        return false;
    }

    public boolean hasMax() {
        return false;
    }

    /**
     * Return whether this is a subtype of a given type.
     */
    @SuppressWarnings("unused")
    public boolean isSubtype(FieldDefImpl superType) {
        throw new IllegalStateException(
            "Classes that implement FieldDefImpl must override isSubtype");
    }

    /**
     * Get the union of this type and the given other type.
     */
    public FieldDefImpl getUnionType(FieldDefImpl other) {

        if (isSubtype(other)) {
            return other;
        }

        if (other.isSubtype(this)) {
            return this;
        }

        Type t1 = getType();
        Type t2 = other.getType();

        if (t1 == t2) {

            if (t1 == Type.RECORD || t2 == Type.ANY_RECORD) {
                return anyRecordDef;
            }

            if (t1 == Type.ARRAY) {
                ArrayDefImpl def1 = (ArrayDefImpl)this;
                ArrayDefImpl def2 = (ArrayDefImpl)other;
                FieldDefImpl edef1 = (FieldDefImpl)def1.getElement();
                FieldDefImpl edef2 = (FieldDefImpl)def2.getElement();

                FieldDefImpl eunion = edef1.getUnionType(edef2);

                return new ArrayDefImpl(eunion);
            }

            if (t1 == Type.MAP) {
                MapDefImpl def1 = (MapDefImpl)this;
                MapDefImpl def2 = (MapDefImpl)other;
                FieldDefImpl edef1 = (FieldDefImpl)def1.getElement();
                FieldDefImpl edef2 = (FieldDefImpl)def2.getElement();

                FieldDefImpl eunion = edef1.getUnionType(edef2);

                return new MapDefImpl(eunion);
            }
        }

        if (isAtomic() && other.isAtomic()) {
            return anyAtomicDef;
        }

        return anyDef;
    }

    /**
     * Returns the FieldDefImpl associated with the names in the iterator.
     *
     * This is used to parse dot notation for navigating fields within complex
     * field types such as Record.  Simple types don't support navigation so the
     * default implementation returns null.  This is used primarily when
     * locating field definitions associated with index fields.
     */
    @SuppressWarnings("unused")
    FieldDefImpl findField(ListIterator<String> fieldPath) {
        return null;
    }

    /**
     * Returns the FieldDef associated with the single field name.  By default
     * this is null, for simple types.  Complex types override this to
     * potentially return non-null FieldDef instances.
     */
    @SuppressWarnings("unused")
    FieldDefImpl findField(String fieldName) {
        return null;
    }

    public String toJsonString() {
        ObjectWriter writer = JsonUtils.createWriter(true);
        ObjectNode o = JsonUtils.createObjectNode();

        toJson(o);

        try {
            return writer.writeValueAsString(o);
        } catch (IOException ioe) {
            throw new IllegalArgumentException(
                "Failed to serialize type description: " +
                ioe.getMessage());
        }
    }

    /**
     * For internal use only.
     *
     * Add this object into Jackson ObjectNode for serialization to
     * a string format.  This implementation works for the common
     * members of FieldDef objects.  Overrides must add state specific
     * to that type.
     * <p>
     * Type is the only state that is absolutely required.  When used in a
     * top-level table or RecordDef the simple types will have names, but when
     * used as the type for an ArrayDef or MapDef only the type is interesting.
     * In those cases the other state is ignored.
     */
    void toJson(ObjectNode node) {
        if (description != null) {
            node.put(DESC, description);
        }
        node.put(TYPE, getType().toString());
    }

    /**
     * Record type must override this in order to return their full definition.
     * This method is used to help generate an Avro schema for a table.
     */
    @SuppressWarnings("unused")
    JsonNode mapTypeToAvro(ObjectNode node) {
        throw new IllegalArgumentException(
            "Type must override mapTypeToAvro: " + getType());
    }

    /**
     * This method returns the JsonNode representing the Avro schema for this
     * type. For simple types it's just a string (TextNode) with
     * the required syntax for Avro.  Complex types and Enumeration override
     * the mapTypeToAvro function to perform the appropriate mapping.
     */
    final JsonNode mapTypeToAvroJsonNode() {
        String textValue = null;
        switch (type) {
        case INTEGER:
            textValue = INT;
            break;
        case LONG:
            textValue = LONG;
            break;
        case STRING:
            textValue = STRING;
            break;
        case BOOLEAN:
            textValue = BOOLEAN;
            break;
        case FLOAT:
            textValue = FLOAT;
            break;
        case DOUBLE:
            textValue = DOUBLE;
            break;
        case BINARY:
            textValue = BYTES;
            break;
        case FIXED_BINARY:
        case ENUM:
        case MAP:
        case RECORD:
        case ARRAY:
            /*
             * The complex types are prepared for a null value in this path.
             * If null, they will allocate the new node.
             */
            return mapTypeToAvro(null);

        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
            throw new IllegalStateException(
                "Wildcard types cannot be mapped to AVRO types: " + type);
        default:
            throw new IllegalStateException
                ("Unknown type in mapTypeToAvroJsonNode: " + type);
        }
        return new TextNode(textValue);
    }

    /**
     * Creates a value instance for the type based on JsonNode input.
     * This is used when constructing a table definition from
     * JSON input or from an Avro schema.
     */
    @SuppressWarnings("unused")
    FieldValueImpl createValue(JsonNode node) {
        return null;
    }

    /*
     * The following 4 methods are used to construct DM values out of strings
     * that are the serialized version of primary key values (see
     * createAtomicFromKey() below).
     */
    @SuppressWarnings("unused")
    IntegerValueImpl createInteger(String value) {
        throw new ClassCastException("Type is not an Integer: " + getClass());
    }

    @SuppressWarnings("unused")
    LongValueImpl createLong(String value) {
        throw new ClassCastException("Type is not a Long: " + getClass());
    }

    @SuppressWarnings("unused")
    DoubleValueImpl createDouble(String value) {
        throw new ClassCastException("Type is not a Double: " + getClass());
    }

    @SuppressWarnings("unused")
    FloatValueImpl createFloat(String value) {
        throw new ClassCastException("Type is not a Float: " + getClass());
    }

    /**
     * Create FieldValue instances from String formats for keys.
     */
    static FieldValueImpl createValueFromKeyString(
        String value,
        FieldDefImpl type) {

        switch (type.getType()) {
        case INTEGER:
            return type.createInteger(value);
        case LONG:
            return type.createLong(value);
        case STRING:
            return (FieldValueImpl)type.createString(value);
        case DOUBLE:
            return type.createDouble(value);
        case FLOAT:
            return type.createFloat(value);
        case ENUM:
            return EnumValueImpl.createFromKey((EnumDefImpl)type, value);
        default:
            throw new IllegalCommandException("Type is not allowed in a key: " +
                                              type.getType());
        }
    }

    /**
     * Create FieldValue instances from Strings that are stored "naturally"
     * for the data type. This is opposed to the String encoding used for
     * key components.
     */
    public static FieldValue createValueFromString(String value,
                                                   final FieldDef def) {

        final InputStream jsonInput;

        switch (def.getType()) {
        case INTEGER:
            return def.createInteger(Integer.parseInt(value));
        case LONG:
            return def.createLong(Long.parseLong(value));
        case STRING:
            return def.createString(value);
        case DOUBLE:
            return def.createDouble(Double.parseDouble(value));
        case FLOAT:
            return def.createFloat(Float.parseFloat(value));
        case BOOLEAN:
            /*
             * Boolean.parseBoolean simply does a case-insensitive comparison
             * to "true" and assigns that value. This means any other string
             * results in false.
             */
            return def.createBoolean(Boolean.parseBoolean(value));
        case ENUM:
            return def.createEnum(value);
        case BINARY:
            return ((BinaryDefImpl)def).fromString(value);
        case FIXED_BINARY:
            return ((FixedBinaryDefImpl)def).fromString(value);
        case RECORD:
            final RecordValueImpl recordValue = (RecordValueImpl)def.createRecord();
            jsonInput =  new ByteArrayInputStream(value.getBytes());
            ComplexValueImpl.createFromJson(recordValue, jsonInput, false);
            return recordValue;
        case ARRAY:
            final ArrayValueImpl arrayValue = (ArrayValueImpl)def.createArray();
            jsonInput =  new ByteArrayInputStream(value.getBytes());
            ComplexValueImpl.createFromJson(arrayValue, jsonInput, false);
            return arrayValue;
        case MAP:
            final MapValueImpl mapValue = (MapValueImpl)def.createMap();
            jsonInput =  new ByteArrayInputStream(value.getBytes());
            ComplexValueImpl.createFromJson(mapValue, jsonInput, false);
            return mapValue;
        default:
            throw new IllegalArgumentException(
                "Type not yet implemented: " + def.getType());
        }
    }

    /**
     * Creates a FieldValue based on the type and this FieldDef.
     * Only atomic types are supported.  This is called from IndexKey
     * deserialization when dealing with putting values into sparsely
     * populated nested types.  Type abstraction is handled here rather
     * than creating per-type overloads.
     *
     * This method is also called in a path where the Object is already a
     * FieldValueImpl instance, so handle that first.  This happens when
     * using a complex path in a FieldRange index lookup.
     *
     * Array and Map are included because they can be indexed directly, which
     * indexes their elements.  In this case the Object will be null.  See
     * code in IndexImpl.rowFromIndexKey() that calls putComplex(), which calls
     * this method.   Record is not because a record itself cannot
     * be the target of an index.
     */
    FieldValue createValue(Object value) {

        if (value instanceof FieldValueImpl) {
            assert(getType() == ((FieldValue) value).getType());
            return (FieldValue) value;
        }

        switch (getType()) {
        case INTEGER:
            return createInteger((Integer) value);
        case STRING:
            return createString((String) value);
        case LONG:
            return createLong((Long) value);
        case DOUBLE:
            return createDouble((Double) value);
        case FLOAT:
            return createFloat((Float) value);
        case ENUM:
            return ((EnumDefImpl) this).createEnum((Integer) value);
        case ARRAY:
            return ((ArrayDefImpl) this).createArray();
        case MAP:
            return ((MapDefImpl) this).createMap();
        case BINARY:
        case BOOLEAN:
        case FIXED_BINARY:
        case RECORD:
        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
        case EMPTY:
            throw new IllegalStateException
                ("Type not supported by createValue: " + getType());
        }
        return null;
    }
}
