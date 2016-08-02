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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import oracle.kv.impl.util.SerializationUtil;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.MapValue;
import oracle.kv.table.RecordValue;

/**
 *
 */
public class FieldValueSerialization {

    private static final int NULL_VALUE = -1;
    private static final int NULL_REFERENCE = -2;

    /*******************************************************************
     *
     * Serialization methods
     *
     *******************************************************************/

    /**
     * If writeDef is true, the deserializer does not have the FieldDef for
     * this value (i.e., the readFieldValue() method will be called with the
     * def param being null). In this case, the serializer must serialize the
     * type as well as the value and the deserializer will read this type first
     * in order to parse the value correctly.
     */
    public static void writeFieldValue(
        FieldValue val,
        boolean writeValDef,
        DataOutput out,
        short serialVersion) throws IOException {

        /*
         * The first byte is always one of:
         *  1. NULL_REFERENCE
         *  2. NULL_VALUE
         *  3. The ordinal of the type
         */

        if (val == null) {
            out.writeByte(NULL_REFERENCE);
            return;
        }

        FieldValueImpl value = (FieldValueImpl) val;
        if (value.isNull()) {
            out.writeByte(NULL_VALUE);
            return;
        }

        /*
         * The following check is valid under the following assumption:
         * RecordValues which are constructed by a record-constructor expr (not
         * yet implemented) will not have ANY_RECORD as their associated type.
         * Notice that a record-constructor expr will probably look like this:
         * "{" name_expr ":" value_expr ("," name_expr ":" value_expr)* "}"
         * If so, this assumption means that a RECORD type must be built on the
         * fly for each RecordValue constructed.
         */
        FieldDefImpl valDef = (FieldDefImpl)val.getDefinition();
        if (valDef.isWildcard()) {
            throw new IllegalStateException(
                "An item cannot have a wildcard type\n" + val);
        }

        /*
         * If the deserializer has schema info, it would be reasonable to skip
         * writing the type of the FieldValue because the type will be available
         * to deserialization. However, given that the first byte needs to be
         * written, and read to handle null values and null references, it makes
         * sense to write the type unconditionally.
         *
         * Notice: this also allows us to construct complex values where a
         * nested value (a field value in a record on an element in an array or
         * map) has a type that is a subtype of the declared type for that value
         * For example, an array of type ARRAY(LONG) may store both integers and
         * longs. However, we need to decide whether to really want to allow
         * this or not (probably not because this would be a backwards-
         * incompatible change) (TODO).
         */
        out.writeByte(val.getType().ordinal());

        switch (val.getType()) {
        case INTEGER:
            SerializationUtil.writePackedInt(out, value.getInt());
            break;
        case LONG:
            SerializationUtil.writePackedLong(out, value.getLong());
            break;
        case DOUBLE:
            out.writeDouble(value.getDouble());
            break;
        case FLOAT:
            out.writeFloat(value.getFloat());
            break;
        case STRING:
            SerializationUtil.writeString(out, value.getString());
            break;
        case BOOLEAN:
            out.writeBoolean(value.getBoolean());
            break;
        case BINARY:
            byte[] bytes = value.getBytes();
            SerializationUtil.writePackedInt(out, bytes.length);
            if (bytes.length > 0) {
                out.write(bytes);
            }
            break;
        case FIXED_BINARY:
            /*
             * Write the (fixed) size of the binary. Fixed binary can only
             * be null or full-sized, so the size of its byte array is the
             * same as the defined size.
             */
            int size =((FixedBinaryDefImpl)valDef).getSize();
            SerializationUtil.writePackedInt(out, size);
            bytes = value.getBytes();
            assert bytes.length == size;
            out.write(bytes);
            break;
        case ENUM:
            if (writeValDef) {
                FieldDef def = value.getDefinition();
                FieldDefSerialization.writeEnum(def, out, serialVersion);
            }
            out.writeShort(value.asEnum().getIndex());
            break;
        case RECORD:
            writeRecord(value, writeValDef, out, serialVersion);
            break;
        case MAP:
            writeMap(value, writeValDef, out, serialVersion);
            break;
        case ARRAY:
            writeArray(value, writeValDef, out, serialVersion);
            break;
        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
            throw new IllegalStateException
                ("ANY* types cannot be materialized as values");
        case EMPTY:
            throw new IllegalStateException
                ("EMPTY type does not contain any values");
        }
    }

    private static void writeRecord(
        FieldValueImpl val,
        boolean writeValDef,
        DataOutput out,
        short serialVersion) throws IOException {

        RecordDefImpl recordDef = (RecordDefImpl)val.getDefinition();
        RecordValueImpl record = (RecordValueImpl)val;

        if (writeValDef) {
            FieldDefSerialization.writeRecord(recordDef, out, serialVersion);
        }

        for (String fname : recordDef.getFieldsInternal()) {

            FieldValueImpl fval = record.getFieldValue(fname);
            FieldDefImpl fdef = recordDef.getField(fname);

            writeFieldValue(fval, fdef.isWildcard(), out, serialVersion);
        }
    }

    /**
     * Map format:
     *  element type (only if needed)
     *  int -- size
     *  entries:
     *    string -- key
     *    FieldValue -- value
     */
    private static void writeMap(
        FieldValueImpl value,
        boolean writeValDef,
        DataOutput out,
        short serialVersion) throws IOException {

        MapValueImpl map = (MapValueImpl)value.asMap();
        MapDefImpl mapDef = (MapDefImpl)value.getDefinition();
        FieldDefImpl elemDef = (FieldDefImpl)mapDef.getElement();
        boolean wildcard = elemDef.isWildcard();

        if (writeValDef) {
            FieldDefSerialization.writeFieldDef(elemDef, out, serialVersion);
        }

        int size = map.size();
        SerializationUtil.writePackedInt(out, size);

        if (size == 0) {
            return;
        }

        for (Map.Entry<String, FieldValue> entry :
                 map.getFieldsInternal().entrySet()) {

            SerializationUtil.writeString(out, entry.getKey());
            writeFieldValue(entry.getValue(), wildcard, out, serialVersion);
        }
    }

    /**
     * Array format:
     *  element type (only if needed)
     *  int -- size
     *  entries:
     *    FieldValue -- value
     */
    private static void writeArray(
        FieldValueImpl value,
        boolean writeValDef,
        DataOutput out,
        short serialVersion) throws IOException {

        ArrayValueImpl array = (ArrayValueImpl)value.asArray();
        ArrayDefImpl arrayDef = (ArrayDefImpl)value.getDefinition();
        FieldDefImpl elemDef = (FieldDefImpl)arrayDef.getElement();
        boolean wildcard = elemDef.isWildcard();

        if (writeValDef) {
            FieldDefSerialization.writeFieldDef(elemDef, out, serialVersion);
        }

        int size = array.size();
        SerializationUtil.writePackedInt(out, size);

        if (size == 0) {
            return;
        }

        for (int i = 0; i < size; i++) {
            writeFieldValue(array.get(i), wildcard, out, serialVersion);
        }
    }

    /*******************************************************************
     *
     * Deserialization methods
     *
     *******************************************************************/

    public static FieldValue readFieldValue(
        FieldDef def,
        DataInput in,
        short serialVersion) throws IOException {

        int ordinal = in.readByte();

        if (ordinal == NULL_REFERENCE) {
            return null;
        }
        if (ordinal == NULL_VALUE) {
            return NullValueImpl.getInstance();
        }

        FieldDef.Type type = FieldDef.Type.values()[ordinal];

        switch (type) {
        case INTEGER: {
            int val = SerializationUtil.readPackedInt(in);
            return FieldDefImpl.integerDef.createInteger(val);
        }
        case LONG: {
            long val = SerializationUtil.readPackedLong(in);
            return FieldDefImpl.longDef.createLong(val);
        }
        case DOUBLE: {
            double val = in.readDouble();
            return FieldDefImpl.doubleDef.createDouble(val);
        }
        case FLOAT: {
            float val = in.readFloat();
            return FieldDefImpl.floatDef.createFloat(val);
        }
        case STRING: {
            String val = SerializationUtil.readString(in);
            return FieldDefImpl.stringDef.createString(val);
        }
        case BOOLEAN: {
            return FieldDefImpl.booleanDef.createBoolean(in.readBoolean());
        }
        case BINARY: {
            int len = SerializationUtil.readPackedInt(in);
            byte[] bytes = new byte[len];
            if (len > 0) {
                in.readFully(bytes);
            }
            return FieldDefImpl.binaryDef.createBinary(bytes);
        }
        case FIXED_BINARY: {
            int len = SerializationUtil.readPackedInt(in);
            byte[] bytes = new byte[len];
            if (len > 0) {
                in.readFully(bytes);
            }
            return new FixedBinaryDefImpl(len, null).createFixedBinary(bytes);
        }
        case ENUM: {
            EnumDefImpl enumDef =
                (def == null ?
                 FieldDefSerialization.readEnum(in, serialVersion) :
                 (EnumDefImpl) def);

            assert(enumDef != null);
            short index = in.readShort();
            return enumDef.createEnum(index);
        }
        case RECORD:
            return readRecord(def, in, serialVersion);
        case MAP:
            return readMap(def, in, serialVersion);
        case ARRAY:
            return readArray(def, in, serialVersion);
        default:
            throw new IllegalStateException("Type not supported: " + type);
        }
    }

    static RecordValue readRecord(
        FieldDef def,
        DataInput in,
        short serialVersion) throws IOException {

        RecordDefImpl recordDef =
            (def == null ?
             FieldDefSerialization.readRecord(in, serialVersion) :
             (RecordDefImpl)def);

        RecordValueImpl record = recordDef.createRecord();

        for (String fname : recordDef.getFieldsInternal()) {

            FieldDefImpl fdef = recordDef.getField(fname);
            if (fdef.isWildcard()) {
                fdef = null;
            }

            FieldValue fval = readFieldValue(fdef, in, serialVersion);

            /*
             * If the field was not present in the original record,
             * readFieldValue returns null.
             */
            if (fval != null) {
                record.put(fname, fval);
            }
        }

        return record;
    }

    static MapValue readMap(
        FieldDef def,
        DataInput in,
        short serialVersion) throws IOException {

        FieldDefImpl elemDef = null;
        MapDefImpl mapDef = null;

        if (def != null) {
            mapDef = (MapDefImpl) def;
            elemDef = (FieldDefImpl)mapDef.getElement();
        } else {
            elemDef = FieldDefSerialization.readFieldDef(in, serialVersion);
            mapDef = new MapDefImpl(elemDef);
        }

        MapValueImpl map = mapDef.createMap();

        boolean wildcard = elemDef.isWildcard();

        if (wildcard) {
            elemDef = null;
        }

        int size = SerializationUtil.readPackedInt(in);

        for (int i = 0; i < size; i++) {
            String fname = SerializationUtil.readString(in);
            FieldValue fval = readFieldValue(elemDef, in, serialVersion);

            assert(fval.isNull() ||
                   elemDef == null ||
                   fval.getType() == elemDef.getType());

            if (fval.isNull()) {
                map.putNull(fname);
            } else {
                map.put(fname, fval);
            }
        }

        return map;
    }

    static ArrayValue readArray(
        FieldDef def,
        DataInput in,
        short serialVersion) throws IOException {

        ArrayDefImpl arrayDef = null;
        FieldDefImpl elemDef = null;

        if (def != null) {
            arrayDef = (ArrayDefImpl) def;
            elemDef = (FieldDefImpl)arrayDef.getElement();
        } else {
            elemDef = FieldDefSerialization.readFieldDef(in, serialVersion);
            arrayDef = new ArrayDefImpl(elemDef);
        }

        ArrayValueImpl array = arrayDef.createArray();

        boolean wildcard = elemDef.isWildcard();

        if (wildcard) {
            elemDef = null;
        }

        int size = SerializationUtil.readPackedInt(in);

        for (int i = 0; i < size; i++) {
            FieldValue fval = readFieldValue(elemDef, in, serialVersion);

            assert(elemDef == null || fval.getType() == elemDef.getType());

            array.add(fval);
        }

        return array;
    }
}
