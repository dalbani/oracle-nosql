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

import oracle.kv.impl.util.SerializationUtil;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;

/**
 * Methods to serialize and deserialize FieldDefImpl instances. It is assumed
 * that ranges and default values for types that support them need not be
 * included. This is because FieldDef instances are only (currently)
 * serialized when serializing a query plan, which will not contain these.
 *
 * If that assumption changes, then ranges and defaults and perhaps even
 * comments will be added.
 */
public class FieldDefSerialization {

    /*******************************************************************
     *
     * Serialization methods
     *
     *******************************************************************/

    /*
     * Serialize using ObjectOutput
     *
     * type -- byte ordinal
     * atomic types --
     */
    public static void writeFieldDef(FieldDef def,
                                     DataOutput out,
                                     short serialVersion) throws IOException {

            /*
             * The type of the value
             */
            out.writeByte(def.getType().ordinal());

            switch (def.getType()) {
            case INTEGER:
            case LONG:
            case DOUBLE:
            case FLOAT:
            case STRING:
            case BINARY:
            case BOOLEAN:
            case ANY:
            case ANY_ATOMIC:
            case ANY_RECORD:
            case EMPTY:
                break;
            case FIXED_BINARY:
                /*
                 * Write the (fixed) size of the binary. Fixed binary can only
                 * be null or full-sized, so the size of its byte array is the
                 * same as the defined size.
                 */
                int size =((FixedBinaryDefImpl)def).getSize();
                SerializationUtil.writePackedInt(out, size);
                break;
            case ENUM:
                writeEnum(def, out, serialVersion);
                break;
            case RECORD:
                writeRecord((RecordDefImpl)def, out, serialVersion);
                break;
            case MAP:
                writeMap((MapDefImpl)def, out, serialVersion);
                break;
            case ARRAY:
                writeArray((ArrayDefImpl)def, out, serialVersion);
                break;
            }
    }

    /*
     * int -- numFields
     * fields (name, field)
     */
    static void writeRecord(RecordDefImpl def,
                            DataOutput out,
                            short serialVersion) throws IOException {

        SerializationUtil.writeString(out, def.getName());
        SerializationUtil.writePackedInt(out, def.getNumFields());

        for (String fname : def.getFieldsInternal()) {
            FieldDefImpl fdef = def.getField(fname);
            SerializationUtil.writeString(out, fname);
            writeFieldDef(fdef, out, serialVersion);
            boolean nullable = def.isNullable(fname);
            out.writeBoolean(nullable);
            if (!nullable) {
                FieldValue defVal = def.getDefaultValue(fname);
                assert(defVal != null);
                FieldValueSerialization.writeFieldValue(
                    defVal, fdef.isWildcard() /* writeValDef */,
                    out, serialVersion);
            }
        }
    }

    /*
     * A map just has its element.
     */
    static void writeMap(MapDefImpl def,
                         DataOutput out,
                         short serialVersion) throws IOException {

        writeFieldDef(def.getElement(), out, serialVersion);
    }

    /*
     * An array just has its element.
     */
    static void writeArray(ArrayDefImpl def,
                           DataOutput out,
                           short serialVersion) throws IOException {

        writeFieldDef(def.getElement(), out, serialVersion);
    }

    @SuppressWarnings("unused")
    static void writeEnum(FieldDef def, DataOutput out, short serialVersion)
            throws IOException {

        EnumDefImpl enumDef = (EnumDefImpl) def;

        String[] values = enumDef.getValues();
        SerializationUtil.writePackedInt(out, values.length);
        for (String value : values) {
            SerializationUtil.writeString(out, value);
        }
    }

    /*******************************************************************
     *
     * Deserialization methods
     *
     *******************************************************************/

    public static FieldDefImpl readFieldDef(DataInput in,
                                            short serialVersion)
            throws IOException {

        byte ordinal = in.readByte();
        FieldDef.Type type = FieldDef.Type.values()[ordinal];
        switch (type) {
        case INTEGER:
            return FieldDefImpl.integerDef;
        case LONG:
            return FieldDefImpl.longDef;
        case DOUBLE:
            return FieldDefImpl.doubleDef;
        case FLOAT:
            return FieldDefImpl.floatDef;
        case STRING:
            return FieldDefImpl.stringDef;
        case BINARY:
            return FieldDefImpl.binaryDef;
        case BOOLEAN:
            return FieldDefImpl.booleanDef;
        case FIXED_BINARY:
            int size = SerializationUtil.readPackedInt(in);
            return new FixedBinaryDefImpl(size, null);
        case ENUM:
            return readEnum(in, serialVersion);
        case RECORD:
            return readRecord(in, serialVersion);
        case MAP:
            return readMap(in, serialVersion);
        case ARRAY:
            return readArray(in, serialVersion);
        case ANY:
            return FieldDefImpl.anyDef;
        case ANY_ATOMIC:
            return FieldDefImpl.anyAtomicDef;
        case ANY_RECORD:
            return FieldDefImpl.anyRecordDef;
        case EMPTY:
            return FieldDefImpl.emptyDef;
        default:
            throw new IllegalStateException("Unknown type code: " + type);
        }
    }

    static RecordDefImpl readRecord(DataInput in, short serialVersion)
            throws IOException {

        String name = SerializationUtil.readString(in);
        int size = SerializationUtil.readPackedInt(in);

        FieldMap fieldMap = new FieldMap();

        for (int i = 0; i < size; i++) {
            String fname = SerializationUtil.readString(in);
            FieldDefImpl fdef = readFieldDef(in, serialVersion);
            boolean nullable = in.readBoolean();
            FieldValueImpl defVal = null;
            if (!nullable) {
                defVal = (FieldValueImpl)
                    FieldValueSerialization.readFieldValue(
                        (fdef.isWildcard() ? null : fdef),
                        in, serialVersion);
            }
            fieldMap.put(fname, fdef, nullable, defVal);
        }

        if (name == null) {
            return new RecordDefImpl(fieldMap, null/*description*/);
        }

        return new RecordDefImpl(name, fieldMap);
    }

    /*
     * A map just has its element.
     */
    static MapDefImpl readMap(DataInput in, short serialVersion)
            throws IOException {

        return new MapDefImpl(readFieldDef(in, serialVersion));
    }

    /*
     * An array just has its element.
     */
    static ArrayDefImpl readArray(DataInput in, short serialVersion)
            throws IOException {

        return new ArrayDefImpl(readFieldDef(in, serialVersion));
    }

    @SuppressWarnings("unused")
    static EnumDefImpl readEnum(DataInput in, short serialVersion)
            throws IOException {

        int numValues = SerializationUtil.readPackedInt(in);
        String[] values = new String[numValues];
        for (int i = 0; i < numValues; i++) {
            values[i] = SerializationUtil.readString(in);
        }
        return new EnumDefImpl(values, null);
    }
}
