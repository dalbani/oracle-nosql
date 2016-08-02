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

package oracle.kv.table;

/**
 * FieldDef represents an immutable metadata object used to represent a single
 * data type. A data type defines a set of values, which are said to belong to
 * or be instances of that data type. Data types are either <it>atomic</it> or
 * <it>complex</it>. Atomic types define sets of atomic values, like integers,
 * doubles, strings, etc. Complex types represent complex values, i.e., values
 * that contain other (nested) values, like records, arrays, and maps.
 * <p>
 * All supported data types are represented as instances of FieldDef.
 * Supported types are defined in the enumeration {@link FieldDef.Type Type}.
 * Instances of FieldDef are created when defining the fields for a new
 * {@link Table}, or during query processing.
 * <p>
 * Each instance has these properties:
 *<ul>
 *<li>type -- the kind of the type (one of the values of the
 *    {@link FieldDef.Type} enum.
 *<li>description -- an optional description used when defining the type.
 *</ul>
 *
 * @since 3.0
 */

public interface FieldDef {

    public static enum Type {
        ARRAY,
        BINARY,
        BOOLEAN,
        DOUBLE,
        ENUM,
        FIXED_BINARY,
        FLOAT,
        INTEGER,
        LONG,
        MAP,
        RECORD,
        STRING,
        ANY,
        ANY_RECORD,
        ANY_ATOMIC,
        EMPTY
    }

    /**
     * Perform a deep copy of this FieldDef instance.
     *
     * @return a new instance equal to this
     */
    FieldDef clone();

    /**
     * Returns the description of the field.
     *
     * @return the description of the field if set, otherwise null
     */
    String getDescription();

    /**
     * Returns the type of the field.
     *
     * @return the type of the field
     */
    Type getType();

    /**
     * Returns true if the type of this field matches the parameter.
     *
     * @return true if the type of this field matches the parameter
     */
    boolean isType(FieldDef.Type type);

    /**
     * @return true if this type can participate in a primary key. Only atomic
     * types can be part of a key. Among the atomic types, Boolean, Binary and
     * FixedBinary are not allowed in keys.
     */
    boolean isValidKeyField();

    /**
     * @return true if this type can participate in an index. Only atomic types
     * can be part of an index key. Among the atomic types, Boolean, Binary and
     * FixedBinary are not allowed in index keys.
     */
    boolean isValidIndexField();

    /**
     * Returns true if this is an {@link AnyDef}.
     *
     * @return true if this is an AnyDef, false otherwise
     *
     * @since 4.0
     */
    boolean isAny();

    /**
     * Returns true if this is an {@link AnyRecordDef}.
     *
     * @return true if this is an AnyDef, false otherwise
     *
     * @since 4.0
     */
    boolean isAnyRecord();

    /**
     * Returns true if this is an {@link AnyAtomicDef}.
     *
     * @return true if this is an AnyDef, false otherwise
     *
     * @since 4.0
     */
    boolean isAnyAtomic();

    /**
     * Returns true if this is a {@link BooleanDef}.
     *
     * @return true if this is a BooleanDef, false otherwise
     */
    boolean isBoolean();

    /**
     * Returns true if this is a {@link BinaryDef}.
     *
     * @return true if this is a BinaryDef, false otherwise
     */
    boolean isBinary();

    /**
     * Returns true if this is a {@link DoubleDef}.
     *
     * @return true if this is a DoubleDef, false otherwise
     */
    boolean isDouble();

    /**
     * Returns true if this is an {@link EnumDef}.
     *
     * @return true if this is an EnumDef, false otherwise
     */
    boolean isEnum();

    /**
     * Returns true if this is a {@link FixedBinaryDef}.
     *
     * @return true if this is a FixedBinaryDef, false otherwise
     */
    boolean isFixedBinary();

    /**
     * Returns true if this is a {@link FloatDef}.
     *
     * @return true if this is a FloatDef, false otherwise
     */
    boolean isFloat();

    /**
     * Returns true if this is an {@link IntegerDef}.
     *
     * @return true if this is an IntegerDef, false otherwise
     */
    boolean isInteger();

    /**
     * Returns true if this is a {@link LongDef}.
     *
     * @return true if this is a LongDef, false otherwise
     */
    boolean isLong();

    /**
     * Returns true if this is a {@link StringDef}.
     *
     * @return true if this is a StringDef, false otherwise
     */
    boolean isString();

    /**
     * Returns true if this is an {@link ArrayDef}.
     *
     * @return true if this is an ArrayDef, false otherwise
     */
    boolean isArray();

    /**
     * Returns true if this is a {@link MapDef}.
     *
     * @return true if this is a MapDef, false otherwise
     */
    boolean isMap();

    /**
     * Returns true if this is a {@link RecordDef}.
     *
     * @return true if this is a RecordDef, false otherwise
     */
    boolean isRecord();

    /**
     * Returns true if this is an atomic type.
     *
     * @return true if this is an atomic type, false otherwise
     *
     * @since 4.0
     */
    boolean isAtomic();

   /**
     * Returns true if this is a numeric type.
     *
     * @return true if this is a numeric type, false otherwise
     *
     * @since 4.0
     */
    boolean isNumeric();

    /**
     * Returns true if this is a complex type.
     *
     * @return true if this is a complex type, false otherwise
     *
     * @since 4.0
     */
    boolean isComplex();

    /**
     * Casts to AnyDef.
     *
     * @return an AnyDef
     *
     * @throws ClassCastException if this is not an AnyDef
     *
     * @since 4.0
     */
    AnyDef asAny();

    /**
     * Casts to AnyRecordDef.
     *
     * @return an AnyRecordDef
     *
     * @throws ClassCastException if this is not an AnyRecordDef
     *
     * @since 4.0
     */
    AnyRecordDef asAnyRecord();

    /**
     * Casts to AnyAtomicDef.
     *
     * @return an AnyAtomicDef
     *
     * @throws ClassCastException if this is not an AnyAtomicDef
     *
     * @since 4.0
     */
    AnyAtomicDef asAnyAtomic();


    /**
     * Casts to BinaryDef.
     *
     * @return a BinaryDef
     *
     * @throws ClassCastException if this is not a BinaryDef
     */
    BinaryDef asBinary();

    /**
     * Casts to BooleanDef.
     *
     * @return a BooleanDef
     *
     * @throws ClassCastException if this is not a BooleanDef
     */
    BooleanDef asBoolean();

    /**
     * Casts to DoubleDef.
     *
     * @return a DoubleDef
     *
     * @throws ClassCastException if this is not a DoubleDef
     */
    DoubleDef asDouble();

    /**
     * Casts to EnumDef.
     *
     * @return an EnumDef
     *
     * @throws ClassCastException if this is not an EnumDef
     */
    EnumDef asEnum();

    /**
     * Casts to FixedBinaryDef.
     *
     * @return a FixedBinaryDef
     *
     * @throws ClassCastException if this is not a FixedBinaryDef
     *
     */
    FixedBinaryDef asFixedBinary();

    /**
     * Casts to FloatDef.
     *
     * @return a FloatDef
     *
     * @throws ClassCastException if this is not a FloatDef
     */
    FloatDef asFloat();

    /**
     * Casts to IntegerDef.
     *
     * @return an IntegerDef
     *
     * @throws ClassCastException if this is not an IntegerDef
     */
    IntegerDef asInteger();

    /**
     * Casts to LongDef.
     *
     * @return a LongDef
     *
     * @throws ClassCastException if this is not a LongDef
     */
    LongDef asLong();

    /**
     * Casts to StringDef.
     *
     * @return a StringDef
     *
     * @throws ClassCastException if this is not a StringDef
     */
    StringDef asString();

    /**
     * Casts to ArrayDef.
     *
     * @return an ArrayDef
     *
     * @throws ClassCastException if this is not an ArrayDef
     */
    ArrayDef asArray();

    /**
     * Casts to MapDef.
     *
     * @return a MapDef
     *
     * @throws ClassCastException if this is not a MapDef
     */
    MapDef asMap();

    /**
     * Casts to RecordDef.
     *
     * @return a RecordDef
     *
     * @throws ClassCastException if this is not a RecordDef
     */
    RecordDef asRecord();

    /**
     * Creates an empty ArrayValue if this is an {@link ArrayDef}.
     *
     * @return an empty ArrayValue
     *
     * @throws ClassCastException if this is not an ArrayDef object
     */
    ArrayValue createArray();

    /**
     * Creates a BinaryValue instance based on the value
     * if this is a {@link BinaryDef}.
     *
     * @param value the byte array to use for the new value object.  Must not
     * be null.
     *
     * @return a BinaryValue
     *
     * @throws ClassCastException if this is not a BinaryDef
     *
     * @throws IllegalArgumentException if the value is null
     */
    BinaryValue createBinary(byte[] value);

    /**
     * Creates a BooleanValue instance based on the value if this is
     * a {@link BooleanDef}.
     *
     * @param value the value to use
     *
     * @return a BooleanValue
     *
     * @throws ClassCastException if this is not a BooleanDef
     */
    BooleanValue createBoolean(boolean value);

    /**
     * Creates a DoubleValue instance based on the value if this is
     * a {@link DoubleDef}.
     *
     * @param value the value to use
     *
     * @return a DoubleValue
     *
     * @throws ClassCastException if this is not a DoubleDef
     *
     * @throws IllegalArgumentException if the value is not valid for
     * the definition
     */
    DoubleValue createDouble(double value);

    /**
     * Creates an EnumValue instance based on the value if this is
     * an {@link EnumDef}.
     *
     * @param value the value to use
     *
     * @return a EnumValue
     *
     * @throws ClassCastException if this is not an EnumDef
     *
     * @throws IllegalArgumentException if the value is not valid for
     * the definition
     */
    EnumValue createEnum(String value);

    /**
     * Creates a FixedBinaryValue instance based on the value if this is
     * a {@link FixedBinaryDef}.
     *
     * @param value the value to use.  It must not be null.
     *
     * @return a FixedBinaryValue
     *
     * @throws ClassCastException if this is not a FixedBinaryDef
     *
     * @throws IllegalArgumentException if the value is null or not
     * valid for the definition
     */
    FixedBinaryValue createFixedBinary(byte[] value);

    /**
     * Creates a FloatValue instance based on the value if this is
     * {@link FloatDef}.
     *
     * @param value the value to use
     *
     * @return a FloatValue
     *
     * @throws ClassCastException if this is not a FloatDef
     *
     * @throws IllegalArgumentException if the value is not valid for
     * the definition
     */
    FloatValue createFloat(float value);

    /**
     * Creates an IntegerValue instance based on the value if this is
     * an {@link IntegerDef}.
     *
     * @param value the value to use
     *
     * @return a IntegerValue
     *
     * @throws ClassCastException if this is not an IntegerDef
     *
     * @throws IllegalArgumentException if the value is not valid for
     * the definition
     */
    IntegerValue createInteger(int value);

    /**
     * Creates a LongValue instance based on the value if this is
     * a {@link LongDef}.
     *
     * @param value the value to use
     *
     * @return a LongValue
     *
     * @throws ClassCastException if this is not a LongDef
     *
     * @throws IllegalArgumentException if the value is not valid for
     * the definition
     */
    LongValue createLong(long value);

    /**
     * Creates an empty MapValue if this is a {@link MapDef}.
     *
     * @return an empty MapValue
     *
     * @throws ClassCastException if this is not a MapDef
     */
    MapValue createMap();

    /**
     * Creates an empty RecordValue if this is a {@link RecordDef}.
     *
     * @return an empty RecordValue
     *
     * @throws ClassCastException if this is not a RecordDef
     */
    RecordValue createRecord();

    /**
     * Creates a StringValue instance based on the value if this is
     * a {@link StringDef}.
     *
     * @param value the value to use
     *
     * @return a StringValue
     *
     * @throws ClassCastException if this is not a StringDef
     *
     * @throws IllegalArgumentException if the value is not valid for
     * the definition or it is null
     */
    StringValue createString(String value);
}
