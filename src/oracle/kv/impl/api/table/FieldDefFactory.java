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

import oracle.kv.table.FieldDef;

/*
 * A factory to create instances of FieldDefImpl subclasses. Such instances
 * need to be created by the query packages, and instead of making the
 * constructors of the FieldDefImpl subclasses public, we use this factory.
 */
public class FieldDefFactory {

    public static IntegerDefImpl createIntegerDef() {
        return FieldDefImpl.integerDef;
    }

    public static LongDefImpl createLongDef() {
        return FieldDefImpl.longDef;
    }

    public static FloatDefImpl createFloatDef() {
        return FieldDefImpl.floatDef;
    }

    public static DoubleDefImpl createDoubleDef() {
        return FieldDefImpl.doubleDef;
    }

    public static StringDefImpl createStringDef() {
        return FieldDefImpl.stringDef;
    }

    public static EnumDefImpl createEnumDef(String[] values) {
        return new EnumDefImpl(values, null/*descr*/);
    }

    public static BooleanDefImpl createBooleanDef() {
        return FieldDefImpl.booleanDef;
    }

    public static BinaryDefImpl createBinaryDef() {
        return FieldDefImpl.binaryDef;
    }

    public static FixedBinaryDefImpl createFixedBinaryDef(int size) {
        return new FixedBinaryDefImpl(size, null/*descr*/);
    }

    public static RecordDefImpl createRecordDef(
        FieldMap fieldMap,
        String descr) {
        return new RecordDefImpl(fieldMap, descr);
    }

    public static ArrayDefImpl createArrayDef(FieldDefImpl elemType) {
        return new ArrayDefImpl(elemType);
    }

    public static MapDefImpl createMapDef(FieldDefImpl elemType) {
        return new MapDefImpl(elemType);
    }

    public static FieldDefImpl createAtomicTypeDef(FieldDef.Type type) {
        switch (type) {
        case STRING:
            return createStringDef();
        case INTEGER:
            return createIntegerDef();
        case LONG:
            return createLongDef();
        case DOUBLE:
            return createDoubleDef();
        case FLOAT:
            return createFloatDef();
        case BINARY:
            return createBinaryDef();
        case BOOLEAN:
            return createBooleanDef();
        default:
            throw new IllegalArgumentException(
                "Cannot create an atomic field def of type " + type);
        }
    }
}
