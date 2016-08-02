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

package oracle.kv.impl.query.types;

import oracle.kv.impl.api.table.ArrayDefImpl;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.MapDefImpl;
import oracle.kv.impl.api.table.RecordDefImpl;
import oracle.kv.table.FieldDef.Type;

/*
 * The ExptType class represents the type of a query expression. It wraps
 * FieldDefImpl, which is the implementation of public interface FieldDef.
 * Furthermore, it adds:
 *
 * - A cardinality indicator ("quantifier") to distinguish between scalar
 *   and non-scalar exprs.
 * - A nullability property to say whether the associated expr can return
 *   a null value.
 * - Representation for some "wildcard" types, which are not part of the
 *   public API (and don't have an associated FieldDef).
 */
public class ExprType implements Cloneable {

    /*
     * WARNING!!
     * Do NOT reorder the enum values in Quantifier. Their ordinal numbers are
     * used as indices in various static matrices defined in TypeManager.
     */
    public static enum Quantifier {
        ONE,   // exactly one value
        QSTN,  // question (?) means zero or one values
        PLUS,  // plus (+) means one or more values
        STAR   // star (*) means zero or more values
    }

    /*
     * WARNING!!
     * Do NOT reorder the enum values in TypeCode. Their ordinal numbers are
     * used as indices in various static matrices defined in TypeManager.
     */
    public static enum TypeCode {

        /* Builtin types */
        EMPTY,

        ANY,
        ANY_ATOMIC,
        ANY_RECORD,

        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        BOOLEAN,
        BINARY,

        /* Non-builtin types */
        FIXED_BINARY,
        ENUM,

        RECORD,
        ARRAY,
        MAP
    }

    /*
     * Builtin types are the ones that can be created statically (because
     * their definition does not depend on any parameters/properties or
     * other types).
     */
    static boolean isBuiltin(TypeCode t) {
        switch (t) {
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case STRING:
        case BOOLEAN:
        case BINARY:
        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
        case EMPTY:
            return true;
        default:
            return false;
        }
    }

    private final TypeCode theTypeCode;

    private Quantifier theQuantifier;

    private final FieldDefImpl theTypeDef;

    private final boolean theIsBuiltin;

    ExprType(FieldDefImpl type, boolean isBuiltin) {
        this(type, Quantifier.ONE, isBuiltin);
    }

    ExprType(FieldDefImpl t, Quantifier q, boolean isBuiltin) {
        theTypeCode = getTypeCodeForDefCode(t.getType());
        theTypeDef = t;
        theQuantifier = q;
        theIsBuiltin = isBuiltin;
    }

    ExprType(ExprType other) {
        assert(!other.theIsBuiltin);
        theTypeCode = other.theTypeCode;
        theTypeDef = other.theTypeDef;
        theQuantifier = other.theQuantifier;
        theIsBuiltin = false;
    }

    @Override
    public ExprType clone() {
        return new ExprType(this);
    }

    public TypeCode getCode() {
        return theTypeCode;
    }

    public Quantifier getQuantifier() {
        return theQuantifier;
    }

    void setQuant(Quantifier q) {
        theQuantifier = q;
    }

    boolean isMultiValued() {
        return (theQuantifier == Quantifier.STAR ||
                theQuantifier == Quantifier.PLUS);
    }

    public FieldDefImpl getDef() {
        return theTypeDef;
    }

    public boolean isBuiltin() {
        return theIsBuiltin;
    }

    public boolean isEmpty() {
        return theTypeCode == TypeCode.EMPTY;
    }

    public boolean isAny() {
        return theTypeCode == TypeCode.ANY;
    }

    public boolean isAnyAtomic() {
        return theTypeCode == TypeCode.ANY_ATOMIC;
    }

    public boolean isComplex() {

        switch (theTypeCode) {
        case ANY_RECORD:
        case RECORD:
        case ARRAY:
        case MAP:
            return true;
        default:
             return false;
        }
    }

    public boolean isAnyRecord() {
        return theTypeCode == TypeCode.ANY_RECORD;
    }

    public boolean isRecord() {
        return (theTypeCode == TypeCode.ANY_RECORD ||
                theTypeCode == TypeCode.RECORD);
    }

    public boolean isArray() {
        return (theTypeCode == TypeCode.ARRAY);
    }

    public boolean isMap() {
        return (theTypeCode == TypeCode.MAP);
    }

    public boolean isPrecise() {
        return (theTypeCode != TypeCode.EMPTY && theTypeDef.isPrecise());
    }

    public boolean isAtomic() {
        return (theTypeCode != TypeCode.EMPTY && theTypeDef.isAtomic());
    }

    public boolean isNumeric() {
        return (theTypeCode != TypeCode.EMPTY && theTypeDef.isNumeric());
    }

    /*
     * For array or map types returns the type of the contained values, if
     * known, otherwise returns ANY_ONE. Should not be used for non-array
     * and non-map types.
     */
    public ExprType getElementType() {
        return getElementType(Quantifier.ONE);
    }

    /*
     * For array or map types returns the type of the contained values,
     * quantified by the given quant. If the array/map type has no schema,
     * [ANY_ONE, quant] is returned.
     *
     * Should not be used for non-array and non-map types.
     */
    public ExprType getElementType(Quantifier quant) {

        FieldDefImpl elemDef;

        switch (theTypeCode) {

        case ARRAY:
            assert(theTypeDef != null);
            ArrayDefImpl arrDef = (ArrayDefImpl)theTypeDef;
            elemDef = (FieldDefImpl)(arrDef.getElement());
            return TypeManager.createType(elemDef, quant);

        case MAP:
            MapDefImpl mapDef = (MapDefImpl)theTypeDef;
            elemDef = (FieldDefImpl)(mapDef.getElement());
            return TypeManager.createType(elemDef, quant);

        default:
            assert(false);
            return null;
        }
    }

    /*
     * For a record type returns the type of the given field, quantified by
     * the given quant. If the record type has no schema, [ANY_ONE, quant]
     * is returned. If the record type has schema and the schema does not
     * contain any field with the given name, null is returned.
     *
     * Should not be used for non-record types.
     */
    public ExprType getFieldType(String fieldName, Quantifier quant) {

       switch (theTypeCode) {

       case ANY_RECORD:
           return TypeManager.createType(TypeManager.ANY_ONE(), quant);

       case RECORD:
           RecordDefImpl recDef = (RecordDefImpl)getDef();
           FieldDefImpl fieldDef = (recDef.getField(fieldName));

           if (fieldDef == null) {
               return null;
           }

           return TypeManager.createType(fieldDef, quant);

       default:
           assert(false);
           return null;
       }
    }

    /*
     * Create a type with the same ItemType as this, and a quant of ONE.
     */
    public ExprType getItemType() {
        return TypeManager.createType(this, Quantifier.ONE);
    }

    /*
     *
     */
    @Override
    public boolean equals(Object o) {
        return equals(o, true);
    }

    public boolean equals(Object o, boolean compareQuants) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof ExprType)) {
            return false;
        }

        ExprType other = (ExprType)o;

        if (compareQuants && theQuantifier != other.theQuantifier) {
            return false;
        }

        if (theTypeCode != other.theTypeCode) {
            return false;
        }

        if (theTypeCode == TypeCode.EMPTY ||
            other.theTypeCode == TypeCode.EMPTY) {
            return false;
        }

        return theTypeDef.equals(other.theTypeDef);
    }

    @Override
    public int hashCode() {
        return theQuantifier.hashCode() + theTypeCode.hashCode() +
            (theTypeDef != null ? theTypeDef.hashCode() : 0);
    }

    /*
     * Return true iff this is a subtype of the given type (superType).
     */
    public boolean isSubType(ExprType superType) {
        return isSubType(superType, true);
    }

    public boolean isSubType(ExprType superType, boolean compareQuants) {

        if (this == superType) {
            return true;
        }

        if (compareQuants &&
            !TypeManager.isSubQuant(theQuantifier, superType.theQuantifier)) {
            return false;
        }

        if (theTypeCode == TypeCode.EMPTY) {
            return true;
        }

        if (!TypeManager.isSubTypeCode(theTypeCode, superType.theTypeCode)) {
            return false;
        }

        switch (superType.theTypeCode) {
        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
        case BOOLEAN:
        case BINARY:
            return true;

        /*
         * The typecodes indicate that sub may be a subtype of sup, but the
         * types may have additional properties that must be checked.
         */
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case STRING:
        case ENUM:
        case FIXED_BINARY:
        case RECORD:
        case ARRAY:
        case MAP:
            return theTypeDef.isSubtype(superType.theTypeDef);

        default:
            assert(false);
            return false;
        }
    }

    /*
     * Return true iff the given value is an instance of of this type
     */
    public boolean containsValue(FieldValueImpl value) {

        TypeCode valueCode = getTypeCodeForDefCode(value.getType());

        if (!TypeManager.isSubTypeCode(valueCode, theTypeCode)) {
            return false;
        }

        switch (theTypeCode) {

        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
        case BOOLEAN:
        case BINARY:
            return true;

        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case STRING:
        case ENUM:
        case FIXED_BINARY:
        case RECORD:
        case ARRAY:
        case MAP:
            FieldDefImpl valDef = value.getDefinition();
            assert(valDef != null);
            return valDef.isSubtype(theTypeDef);

        default:
            assert(false);
            return false;
        }
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append(getDef());

        switch (theQuantifier) {
        case ONE:
            break;
        case QSTN:
            sb.append("?");
            break;
        case PLUS:
            sb.append("+");
            break;
        case STAR:
            sb.append("*");
            break;
        }

        return sb.toString();
    }

    static TypeCode getTypeCodeForDefCode(Type t) {

        switch (t) {
        case EMPTY:
            return TypeCode.EMPTY;
        case ANY:
            return TypeCode.ANY;
        case ANY_ATOMIC:
            return TypeCode.ANY_ATOMIC;
        case ANY_RECORD:
            return TypeCode.ANY_RECORD;
        case INTEGER:
            return TypeCode.INT;
        case LONG:
            return TypeCode.LONG;
        case FLOAT:
            return TypeCode.FLOAT;
        case DOUBLE:
            return TypeCode.DOUBLE;
        case STRING:
            return TypeCode.STRING;
        case ENUM:
            return TypeCode.ENUM;
        case BOOLEAN:
            return TypeCode.BOOLEAN;
        case BINARY:
            return TypeCode.BINARY;
        case FIXED_BINARY:
            return TypeCode.FIXED_BINARY;
        case RECORD:
            return TypeCode.RECORD;
        case ARRAY:
            return TypeCode.ARRAY;
        case MAP:
            return TypeCode.MAP;
        default:
            throw new IllegalArgumentException(
                "Unexpected type: " + t);
        }
    }
}
