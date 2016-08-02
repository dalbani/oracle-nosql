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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import oracle.kv.impl.api.table.ArrayDefImpl;
import oracle.kv.impl.api.table.DoubleDefImpl;
import oracle.kv.impl.api.table.EnumDefImpl;
import oracle.kv.impl.api.table.FieldDefFactory;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldDefSerialization;
import oracle.kv.impl.api.table.FieldMap;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.FloatDefImpl;
import oracle.kv.impl.api.table.MapDefImpl;
import oracle.kv.impl.api.table.RecordDefImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.ExprType.TypeCode;
import oracle.kv.table.FieldDef;

public class TypeManager {

    /*
     * Then, QUANT_SUBTYPE_MATRIX[q1][q2] is true iff for each possible set
     * S represented by q1, S is also represented by q2. For eaxmple,
     * QUANT_SUBTYPE_MATRIX[?][+] is false because ? represents the empty set,
     * which is not represented by +.
     */
    static final boolean QUANT_SUBTYPE_MATRIX[][] =
    {
        // 1      ?      +      *
        {  true,  true,  true,  true }, // 1
        {  false, true,  false, true }, // ?
        {  false, false, true,  true }, // +
        {  false, false, false, true }  // *
    };

    /*
     * QUANT_UNION_MATRIX[q1][q2] is the "minimum" quantifier that is equally
     * or more permissive than both q1 and q2.
     */
    static final Quantifier QUANT_UNION_MATRIX[][] =
    {
        // 1               ?                +                *
        { Quantifier.ONE,  Quantifier.QSTN, Quantifier.PLUS, Quantifier.STAR }, // 1
        { Quantifier.QSTN, Quantifier.QSTN, Quantifier.STAR, Quantifier.STAR }, // ?
        { Quantifier.PLUS, Quantifier.STAR, Quantifier.PLUS, Quantifier.STAR }, // +
        { Quantifier.STAR, Quantifier.STAR, Quantifier.STAR, Quantifier.STAR }, // *
    };

    /*
     * QUANT_INTERS_MATRIX[q1][q2] is the maximum quantifier that is equally
     * or less permissive than both q1 and q2.
     */
    static final Quantifier QUANT_INTERS_MATRIX[][] =
    {
        // 1              ?                +                *
        { Quantifier.ONE, Quantifier.ONE,  Quantifier.ONE,  Quantifier.ONE  },  // 1
        { Quantifier.ONE, Quantifier.QSTN, Quantifier.ONE , Quantifier.QSTN },  // ?
        { Quantifier.ONE, Quantifier.ONE,  Quantifier.PLUS, Quantifier.PLUS },  // +
        { Quantifier.ONE, Quantifier.QSTN, Quantifier.PLUS, Quantifier.STAR },  // *
    };

    /*
     *
     */
    static final Quantifier QUANT_CONCAT_MATRIX[][] =
    {
        // 1               ?                +                *
        { Quantifier.ONE,  Quantifier.ONE,  Quantifier.PLUS, Quantifier.PLUS }, // 1
        { Quantifier.ONE,  Quantifier.QSTN, Quantifier.PLUS, Quantifier.STAR }, // ?
        { Quantifier.PLUS, Quantifier.PLUS, Quantifier.PLUS, Quantifier.PLUS }, // +
        { Quantifier.PLUS, Quantifier.STAR, Quantifier.PLUS, Quantifier.STAR }, // *
    };

    /*
     * SUBTYPE_MATRIX[sub][sup] == false means that sub is definitely not a
     * subtyoe of sup.
     *
     * SUBTYPE_MATRIX[sub][sup] == true means that sub may be a subtyoe of sup.
     */
    static final boolean SUBTYPE_MATRIX[][] =
    {
        // EMPTY  ANY   AATM   AREC
        // INT    LONG  FLT    DBL    STR    BOOL   BIN  FBIN  ENUM
        // REC    ARR   MAP

        { true,  false, false, false,
          false, false, false, false, false, false, false, false, false,
          false, false, false }, // EMPTY

        { false, true,  false, false,
          false, false, false, false, false, false, false, false, false,
          false, false, false }, // ANY

        { false, true,  true,  false,
          false, false, false, false, false, false, false, false, false,
          false, false, false }, // ATM

        { false, true,  false, true,
          false, false, false, false, false, false, false, false, false,
          false, false, false }, // AREC

        { false, true,  true,  false,
          true,  true,  false, false, false, false, false, false, false,
          false, false, false }, // INT

        { false, true,  true,  false,
          false, true,  false, false, false, false, false, false, false,
          false, false, false }, // LONG

        { false, true,  true,  false,
          false, false, true,  true,  false, false, false, false, false,
          false, false, false }, // FLT

        { false, true,  true,  false,
          false, false, false, true,  false, false, false, false, false,
          false, false, false }, // DBL

        { false, true,  true,  false,
          false, false, false, false, true, false, false, false, false,
          false, false, false }, // STR

        { false, true,  true,  false,
          false, false, false, false, false, true,  false, false, false,
          false, false, false }, // BOOL

        { false, true,  true,  false,
          false, false, false, false, false, false, true,  false, false,
          false, false, false }, // BIN

        { false, true,  true,  false,
          false, false, false, false, false, false, true,  true,  false,
          false, false, false }, // FBIN

        { false, true,  true,  false,
          false, false, false, false, false, false, false, false, true,
          false, false, false }, // ENUM

        // EMPTY  ANY   AATM   AREC
        // INT    LONG  FLT    DBL    STR    BOOL   BIN  FBIN  ENUM
        // REC    ARR   MAP

        { false, true,  false, true,
          false, false, false, false, false, false, false, false, false,
          true,  false, false }, // REC

        { false, true,  false, false,
          false, false, false, false, false, false, false, false, false,
          false, true,  false }, // ARR

        { false, true,  false, false,
          false, false, false, false, false, false, false, false, false,
          false, false, true } // MAP
    };

    /*
     * COMPARISON_MATRIX[t1][t2] == false means that t1 and t2 are definitely
     * not comparable.
     *
     * COMPARISON_MATRIX[t1][t2] == true means that t1 and t2 may be comparable.
     */
    static final boolean COMPARISON_MATRIX[][] =
    {
        // EMPTY ANY    AATM   AREC
        // INT   LONG   FLT    DBL    STR    BOOL   BIN    FBIN   ENUM
        // REC   ARR    MAP

        // EMPTY
        { true,  true,  true,  true,
          true,  true,  true,  true,  true,  true,  true,  true,  true,
          true,  true,  true },

        // ANY
        { true,  true,  true,  true,
          true,  true,  true,  true,  true,  true,  true,  true,  true,
          true,  true,  true },
        // AATM
        { true,  true,  true,  false,
          true,  true,  true,  true,  true,  true,  true,  true,  true,
          false, false, false },
        // AREC
        { true,  true,  false, true,
          false, false, false, false, false, false, false, false, false,
          true,  false, false },

        // INT
        { true,  true,  true,  false,
          true,  true,  true,  true,  false, false, false, false, false,
          false, false, false },
        // LONG
        { true,  true,  true,  false,
          true,  true,  true,  true,  false, false, false, false, false,
          false, false, false },
        // FLT
        { true,  true,  true,  false,
          true,  true,  true,  true,  false, false, false, false, false,
          false, false, false },
        // DBL
        { true,  true,  true,  false,
          true,  true,  true,  true,  false, false, false, false, false,
          false, false, false },
        // STR
        { true,  true,  true,  false,
          false, false, false, false, true,  false, false, false, true,
          false, false, false },
        // BOOL
        { true,  true,  true,  false,
          false, false, false, false, false, true,  false, false, false,
          false, false, false },
        // BIN
        { true,  true,  true,  false,
          false, false, false, false, false, false, true,  true,  false,
          false, false, false },
        // FBIN
        { true,  true,  true,  false,
          false, false, false, false, false, false, true,  true,  false,
          false, false, false },
        // ENUM
        { true,  true,  true,  false,
          false, false, false, false, true,  false, false, false, true,
          false, false, false },

        // EMPTY ANY    AATM   AREC
        // INT   LONG   FLT    DBL    STR    BOOL   BIN    FBIN   ENUM
        // REC   ARR    MAP

        // REC
        { true,  true,  false, true,
          false, false, false, false, false, false, false, false, false,
          true,  false, false },
        // ARR
        { true,  true,  false,  false,
          false, false, false, false, false, false, false, false, false,
          false, true,  false },
        // MAP
        { true,  true,  false,  false,
          false, false, false, false, false, false, false, false, false,
          false, false, true }
    };

    static final ExprType BUILTIN_TYPES[][] =
    {
        {
            new ExprType(FieldDefImpl.emptyDef, Quantifier.QSTN, true)
        },
        {
            new ExprType(FieldDefImpl.anyDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.anyDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.anyDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.anyDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.anyAtomicDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.anyAtomicDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.anyAtomicDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.anyAtomicDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.anyRecordDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.anyRecordDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.anyRecordDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.anyRecordDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.integerDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.integerDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.integerDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.integerDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.longDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.longDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.longDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.longDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.floatDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.floatDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.floatDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.floatDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.doubleDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.doubleDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.doubleDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.doubleDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.stringDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.stringDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.stringDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.stringDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.booleanDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.booleanDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.booleanDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.booleanDef, Quantifier.STAR, true)
        },
        {
            new ExprType(FieldDefImpl.binaryDef, Quantifier.ONE, true),
            new ExprType(FieldDefImpl.binaryDef, Quantifier.QSTN, true),
            new ExprType(FieldDefImpl.binaryDef, Quantifier.PLUS, true),
            new ExprType(FieldDefImpl.binaryDef, Quantifier.STAR, true)
        },
    };

    public static ExprType EMPTY() {
        return BUILTIN_TYPES[TypeCode.EMPTY.ordinal()][0];
    }

    public static ExprType ANY_ONE() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType ANY_QSTN() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType ANY_PLUS() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType ANY_STAR() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType ANY_ATOMIC_ONE() {
        return BUILTIN_TYPES[TypeCode.ANY_ATOMIC.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType ANY_ATOMIC_QSTN() {
        return BUILTIN_TYPES[TypeCode.ANY_ATOMIC.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType ANY_ATOMIC_PLUS() {
        return BUILTIN_TYPES[TypeCode.ANY_ATOMIC.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType ANY_ATOMIC_STAR() {
        return BUILTIN_TYPES[TypeCode.ANY_ATOMIC.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType ANY_RECORD_ONE() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType ANY_RECORD_QSTN() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType ANY_RECORD_PLUS() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType ANY_RECORD_STAR() {
        return BUILTIN_TYPES[TypeCode.ANY.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType INT_ONE() {
        return BUILTIN_TYPES[TypeCode.INT.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType INT_QSTN() {
        return BUILTIN_TYPES[TypeCode.INT.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType INT_PLUS() {
        return BUILTIN_TYPES[TypeCode.INT.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType INT_STAR() {
        return BUILTIN_TYPES[TypeCode.INT.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType LONG_ONE() {
        return BUILTIN_TYPES[TypeCode.LONG.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType LONG_QSTN() {
        return BUILTIN_TYPES[TypeCode.LONG.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType LONG_PLUS() {
        return BUILTIN_TYPES[TypeCode.LONG.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType LONG_STAR() {
        return BUILTIN_TYPES[TypeCode.LONG.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType FLOAT_ONE() {
        return BUILTIN_TYPES[TypeCode.FLOAT.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType FLOAT_QSTN() {
        return BUILTIN_TYPES[TypeCode.FLOAT.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType FLOAT_PLUS() {
        return BUILTIN_TYPES[TypeCode.FLOAT.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType FLOAT_STAR() {
        return BUILTIN_TYPES[TypeCode.FLOAT.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType DOUBLE_ONE() {
        return BUILTIN_TYPES[TypeCode.DOUBLE.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType DOUBLE_QSTN() {
        return BUILTIN_TYPES[TypeCode.DOUBLE.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType DOUBLE_PLUS() {
        return BUILTIN_TYPES[TypeCode.DOUBLE.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType DOUBLE_STAR() {
        return BUILTIN_TYPES[TypeCode.DOUBLE.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType STRING_ONE() {
        return BUILTIN_TYPES[TypeCode.STRING.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType STRING_QSTN() {
        return BUILTIN_TYPES[TypeCode.STRING.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType STRING_PLUS() {
        return BUILTIN_TYPES[TypeCode.STRING.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType STRING_STAR() {
        return BUILTIN_TYPES[TypeCode.STRING.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType BOOLEAN_ONE() {
        return BUILTIN_TYPES[TypeCode.BOOLEAN.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType BOOLEAN_QSTN() {
        return BUILTIN_TYPES[TypeCode.BOOLEAN.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType BOOLEAN_PLUS() {
        return BUILTIN_TYPES[TypeCode.BOOLEAN.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType BOOLEAN_STAR() {
        return BUILTIN_TYPES[TypeCode.BOOLEAN.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType BINARY_ONE() {
        return BUILTIN_TYPES[TypeCode.BINARY.ordinal()]
                            [Quantifier.ONE.ordinal()];
    }

    public static ExprType BINARY_QSTN() {
        return BUILTIN_TYPES[TypeCode.BINARY.ordinal()]
                            [Quantifier.QSTN.ordinal()];
    }

    public static ExprType BINARY_PLUS() {
        return BUILTIN_TYPES[TypeCode.BINARY.ordinal()]
                            [Quantifier.PLUS.ordinal()];
    }

    public static ExprType BINARY_STAR() {
        return BUILTIN_TYPES[TypeCode.BINARY.ordinal()]
                            [Quantifier.STAR.ordinal()];
    }

    public static ExprType ANY_ARRAY_STAR =
        createArrayType(TypeManager.ANY_ONE(), Quantifier.STAR);

    TypeManager() {
    }

    /*
     * Return the quant that is the intersection of the 2 given quants.
     */
    public static Quantifier getIntersectionQuant(Quantifier q1, Quantifier q2) {
        return QUANT_INTERS_MATRIX[q1.ordinal()][q2.ordinal()];
    }

    /*
     * Return the quant that is the union of the 2 given quants.
     */
    public static Quantifier getUnionQuant(Quantifier q1, Quantifier q2) {
        return QUANT_UNION_MATRIX[q1.ordinal()][q2.ordinal()];
    }

    /*
     * Return the quant that is the concatenation of the 2 given quants.
     */
    public static Quantifier getConcatQuant(Quantifier q1, Quantifier q2) {
        return QUANT_CONCAT_MATRIX[q1.ordinal()][q2.ordinal()];
    }

    static boolean isSubQuant(Quantifier sub, Quantifier sup) {
        return QUANT_SUBTYPE_MATRIX[sub.ordinal()][sup.ordinal()];
    }

    static boolean isSubTypeCode(TypeCode sub, TypeCode sup) {
        return SUBTYPE_MATRIX[sub.ordinal()][sup.ordinal()];
    }

    /*
     * Check whether 2 types intersect. If false is returned, it is certain
     * that the types do not intersect. If true is returned, the types may
     * or may not intersect.
     *
     * TODO: make this method more precise.
     */
    public static boolean typesIntersect(ExprType t1, ExprType t2) {

        TypeCode c1 = t1.getCode();
        TypeCode c2 = t2.getCode();
        Quantifier q1 = t1.getQuantifier();
        Quantifier q2 = t2.getQuantifier();

        if ((q1 == Quantifier.QSTN || q1 == Quantifier.STAR) &&
            (q2 == Quantifier.QSTN || q2 == Quantifier.STAR)) {
            return true;
        }

        if (c1 == TypeCode.EMPTY || c2 == TypeCode.EMPTY) {
            return false;
        }

        if (!isSubTypeCode(c1, c2) && !isSubTypeCode(c2, c1)) {
            return false;
        }

        return true;
    }

    static boolean areTypeCodesComparable(ExprType t1, ExprType t2) {

        return COMPARISON_MATRIX[t1.getCode().ordinal()]
                                [t2.getCode().ordinal()];
    }

    public static boolean areTypesComparable(ExprType t1, ExprType t2) {
        return areTypeCodesComparable(t1, t2);
    }

    /*
     * Create a type T that is the union of the given 2 types.
     * T is a type that includes every instance of both t1 and t2,
     * including the empty sequence if it is included by either t1
     * or t2. Because we don't support union types, T will, in general,
     * also include instances that are neither in T1 nor in T2.
     */
    public static ExprType getUnionType(ExprType t1, ExprType t2) {

        Quantifier q = getUnionQuant(t1.getQuantifier(), t2.getQuantifier());
        return getUnionOrConcatType(t1, t2, q);
    }

    /*
     * Create a type T that is the concatenation of the given 2 types.
     * T is a type that represents the result of concatenating two sequences
     * of types t1 and t2 respectively. Because we don't support union types,
     * T will, in general, also include instances that are neither in T1 nor
     * in T2.
     */
    public static ExprType getConcatType(ExprType t1, ExprType t2) {

        Quantifier q = getConcatQuant(t1.getQuantifier(), t2.getQuantifier());
        return getUnionOrConcatType(t1, t2, q);
    }

    private static ExprType getUnionOrConcatType(
        ExprType t1,
        ExprType t2,
        Quantifier q) {

        FieldDefImpl def1 = t1.getDef();
        FieldDefImpl def2 = t2.getDef();

        FieldDefImpl union = def1.getUnionType(def2);

        return createType(union, q);
    }

    /*
     * Create an ExprType with a given quantifier and the same ItemType as the
     * one of a given type.
     */
    public static ExprType createType(ExprType t, Quantifier q) {

        if (t.getQuantifier() == q) {
            return t;
        }

        if (t.isBuiltin()) {
            return getBuiltinType(t.getCode(), q);
        }

        ExprType res = new ExprType(t);
        res.setQuant(q);
        return res;
    }

    /*
     * Create an ExprType out of a given FieldDef and quantifier.
     */
    public static ExprType createType(FieldDefImpl t, Quantifier q) {

        ExprType type = getBuiltinType(t, q);

        if (type == null) {
            type = new ExprType(t, q, false);
        }

        return type;
    }

    /*
     * Create a record type out of the types and names of its fields, plus a
     * given quantifier.
     *
     * TODO: what about nullability?
     */
    public static ExprType createRecordType(
        List<String> fieldNames,
        List<Expr> fieldExprs,
        Quantifier q) {

        assert(fieldNames.size() == fieldExprs.size());

        FieldMap fieldMap = new FieldMap();

        for (int i = 0; i < fieldNames.size(); ++i) {
            fieldMap.put(
                fieldNames.get(i),
                fieldExprs.get(i).getType().getDef(),
                true/*nullable*/,
                null/*defaultValue*/);
        }

        RecordDefImpl typeDef = FieldDefFactory.createRecordDef(
            fieldMap, null/*descr*/);

        return new ExprType(typeDef, q, false);
    }

    /*
     * Create a reord type to describe the columns of a table
     */
    public static ExprType createTableRecordType(
        TableImpl table,
        Quantifier q) {

        RecordDefImpl rowType = table.getRowType();

        return new ExprType(rowType, q, false);
    }

    public static ExprType createArrayType(
        ExprType elemType,
        Quantifier q) {

        ArrayDefImpl typeDef = FieldDefFactory.createArrayDef(
            elemType.getDef());

        return createType(typeDef, q);
    }

    public static ExprType createMapType(
        ExprType elemType,
        Quantifier q) {

        MapDefImpl typeDef = FieldDefFactory.createMapDef(elemType.getDef());
        return createType(typeDef, q);
    }

    /*
     * Create an expr type based on the type of a value.
     */
    public static ExprType createValueType(FieldValueImpl value) {

        if (value.isNull()) {
            throw new QueryStateException(
                "Null values do not have types!: ");
        }

        FieldDefImpl type;

        switch (value.getType()) {
        case INTEGER:
            return INT_ONE();
        case LONG:
            return LONG_ONE();
        case FLOAT:
            return FLOAT_ONE();
        case DOUBLE:
            return DOUBLE_ONE();
        case STRING:
            return STRING_ONE();
        case ENUM:
            type = value.getDefinition();
            return new ExprType(type, false);
        case BOOLEAN:
            return BOOLEAN_ONE();
        case BINARY:
            return BINARY_ONE();
        case FIXED_BINARY:
            type = value.getDefinition();
            return new ExprType(type, false);
        case RECORD:
        case ARRAY:
        case MAP:
            type = value.getDefinition();
            return new ExprType(type, false);
        default:
            throw new IllegalArgumentException(
                "Unexpected literal type: " + value.getType());
        }
    }

    /*
     * Get one of the builtin types.
     * This is public so that PlanIter can access it for deserialization of
     * ExprType.
     */
    public static ExprType getBuiltinType(TypeCode t, Quantifier q) {

        assert(ExprType.isBuiltin(t));
        if (t == TypeCode.EMPTY) {
            q = Quantifier.QSTN;
        }
        return BUILTIN_TYPES[t.ordinal()][q.ordinal()];
    }

    /*
     * Get one of the builtin types, or null if the given typedef does not
     * correspond to a builtin type.
     */
    private static ExprType getBuiltinType(FieldDefImpl t, Quantifier q) {

        switch (t.getType()) {

        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
            return getBuiltinType(
                ExprType.getTypeCodeForDefCode(t.getType()), q);

        case INTEGER:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case STRING:
        case BOOLEAN:
        case BINARY:
            if (!t.hasMin() && !t.hasMax()) {
                return getBuiltinType(
                    ExprType.getTypeCodeForDefCode(t.getType()), q);
            }

            return null;

        case ENUM:
        case FIXED_BINARY:
        case RECORD:
        case ARRAY:
        case MAP:
            return null;
        default:
            throw new IllegalArgumentException(
                "Unexpected type: " + t.getType());
        }
    }

    /**
     * Promote the given value to a value of the given type, if possible.
     * Return null if promotion is not possible. If the value is already
     * a member of the given type, return the value itself.
     */
    public static FieldValueImpl promote(FieldValueImpl value, ExprType type) {

        if (type.containsValue(value)) {
            return value;
        }

        FieldValueImpl retValue = null;

        FieldDef.Type valueCode = value.getType();
        TypeCode typeCode = type.getCode();

        if (valueCode == FieldDef.Type.INTEGER) {
            if (typeCode == TypeCode.FLOAT) {
                return castIntToFloat(value, type);
            } else if (typeCode == TypeCode.DOUBLE) {
                return castIntToDouble(value, type);
            }
        } else if (valueCode == FieldDef.Type.LONG) {
           if (typeCode == TypeCode.FLOAT) {
               return castLongToFloat(value, type);
            } else if (typeCode == TypeCode.DOUBLE) {
               return castLongToDouble(value, type);
            }
        } else if (valueCode == FieldDef.Type.STRING) {
           if (typeCode == TypeCode.ENUM) {
               return castStringToEnum(value, type);
           }
        }

        return retValue;
    }

    /**
     * Cast an integer value to a float. Return null if the cast is not
     * possible, which is the case when the target type is a FloatRange
     * and the input value (after being cast to float) is not within the
     * allowed range.
     */
    private static FieldValueImpl castIntToFloat(
        FieldValueImpl value,
        ExprType type) {

        assert(type.getDef() != null);
        FloatDefImpl typeDef = (FloatDefImpl)(type.getDef());
        int baseValue = value.getInt();

        try {
            return typeDef.createFloat(baseValue);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Cast an integer value to a double. Return null if the cast is not
     * possible, which is the case when the target type is a DoubleRange
     * and the input value (after being cast to double) is not within the
     * allowed range.
     */
    private static FieldValueImpl castIntToDouble(
        FieldValueImpl value,
        ExprType type) {

        assert(type.getDef() != null);
        DoubleDefImpl typeDef = (DoubleDefImpl)(type.getDef());
        int baseValue = value.getInt();

        try {
            return typeDef.createDouble(baseValue);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Cast a long value to a double. Return null if the cast is not
     * possible, which is the case when the target type is a FloatRange
     * and the input value (after being cast to float) is not within the
     * allowed range.
     */
    private static FieldValueImpl castLongToFloat(
        FieldValueImpl value,
        ExprType type) {

        assert(type.getDef() != null);
        FloatDefImpl typeDef = (FloatDefImpl)(type.getDef());
        long baseValue = value.getLong();

        try {
            return typeDef.createFloat(baseValue);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Cast a long value to a double. Return null if the cast is not
     * possible, which is the case when the target type is a DoubleRange
     * and the input value (after being cast to double) is not within the
     * allowed range.
     */
    private static FieldValueImpl castLongToDouble(
        FieldValueImpl value,
        ExprType type) {

        assert(type.getDef() != null);
        DoubleDefImpl typeDef = (DoubleDefImpl)(type.getDef());
        long baseValue = value.getLong();

        try {
            return typeDef.createDouble(baseValue);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Cast a string value to an Enum. Return null if the cast is not
     * possible, which is the case when the given string value is not
     * string-equal to any of the tokens in the target type.
     */
    private static FieldValueImpl castStringToEnum(
        FieldValueImpl value,
        ExprType type) {

        assert(type.getDef() != null);
        EnumDefImpl typeDef = (EnumDefImpl)(type.getDef());
        String baseValue = value.getString();

        try {
            return typeDef.createEnum(baseValue);
        } catch (Exception e) {
            return null;
        }
    }

    public static void serializeExprType(
        ExprType type,
        DataOutput out,
        short serialVersion) throws IOException {

        out.writeByte(type.getCode().ordinal());
        out.writeByte(type.getQuantifier().ordinal());
        boolean builtin = type.isBuiltin();
        out.writeBoolean(builtin);

        /*
         * Only serialize the FieldDef is this is not a builtin type.
         * Builtin types will never be directly deserialized.
         */
        if (!builtin) {
            FieldDefSerialization.writeFieldDef(type.getDef(),
                                                out, serialVersion);
        }
    }

    /**
     * Read the type and quantifier and builtin boolean. If this is a builtin
     * type, get the (static) type directly from TypeManager.
     */
    public static ExprType deserializeExprType(
        DataInput in,
        short serialVersion) throws IOException {

        int ordinal = in.readByte();
        ExprType.TypeCode typeCode = ExprType.TypeCode.values()[ordinal];
        ordinal = in.readByte();
        ExprType.Quantifier quantifier = ExprType.Quantifier.values()[ordinal];
        boolean builtin = in.readBoolean();
        if (builtin) {
            return getBuiltinType(typeCode, quantifier);
        }
        FieldDefImpl def =
                FieldDefSerialization.readFieldDef(in, serialVersion);
        return new ExprType(def, quantifier, false);
    }
}
