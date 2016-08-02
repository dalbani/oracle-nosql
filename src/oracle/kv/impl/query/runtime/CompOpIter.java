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

package oracle.kv.impl.query.runtime;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import oracle.kv.impl.api.table.ArrayValueImpl;
import oracle.kv.impl.api.table.ComplexValueImpl;
import oracle.kv.impl.api.table.EnumDefImpl;
import oracle.kv.impl.api.table.EnumValueImpl;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.IntegerValueImpl;
import oracle.kv.impl.api.table.LongValueImpl;
import oracle.kv.impl.api.table.MapValueImpl;
import oracle.kv.impl.api.table.NullValueImpl;
import oracle.kv.impl.api.table.RecordValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.QueryException.Location;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.impl.query.types.TypeManager;
import oracle.kv.table.FieldDef.Type;
import oracle.kv.table.FieldValue;

/**
 * Iterator to implement the comparison operators
 *
 * Inputs:
 *   At most one value from the left and right operand iterators
 *
 * Result:
 *   Boolean or NullValue
 */
public class CompOpIter extends PlanIter {

    static public class CompResult {
        int comp;
        boolean haveNull;
    }

    static private class CompIterState extends PlanIterState {

        final CompResult theResult = new CompResult();

        @Override
        protected void reset(PlanIter iter) {
            super.reset(iter);
            theResult.haveNull = false;
        }
    }

    private final FuncCode theCode;

    private final PlanIter theLeftOp;

    private final PlanIter theRightOp;

    public CompOpIter(
        Expr e,
        int resultReg,
        FuncCode code,
        PlanIter[] argIters) {

        super(e, resultReg);
        theCode = code;
        assert(argIters.length == 2);
        theLeftOp = argIters[0];
        theRightOp = argIters[1];
    }


    /**
     * FastExternalizable constructor.
     */
    CompOpIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);
        short ordinal = in.readShort();
        theCode = FuncCode.values()[ordinal];
        theLeftOp = deserializeIter(in, serialVersion);
        theRightOp = deserializeIter(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        super.writeFastExternal(out, serialVersion);
        out.writeShort(theCode.ordinal());
        serializeIter(theLeftOp, out, serialVersion);
        serializeIter(theRightOp, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.COMP_OP;
    }

    @Override
    FuncCode getFuncCode() {
        return theCode;
    }

    @Override
    public void open(RuntimeControlBlock rcb) {
        rcb.setState(theStatePos, new CompIterState());
        theLeftOp.open(rcb);
        theRightOp.open(rcb);
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        CompIterState state = (CompIterState)rcb.getState(theStatePos);

        if (state.isDone()) {
            return false;
        }

        boolean leftOpNext = theLeftOp.next(rcb);

        if (!leftOpNext) {
            state.done();
            return false;
        }

        boolean rightOpNext = theRightOp.next(rcb);

        if (!rightOpNext) {
            state.done();
            return false;
        }

        FieldValueImpl leftValue = rcb.getRegVal(theLeftOp.getResultReg());
        FieldValueImpl rightValue = rcb.getRegVal(theRightOp.getResultReg());

        assert(leftValue != null && rightValue != null);

        compare(leftValue, rightValue, theCode, state.theResult, getLocation());

        if (state.theResult.haveNull) {
            rcb.setRegVal(theResultReg, NullValueImpl.getInstance());
            state.done();
            return true;
        }

        int comp = state.theResult.comp;
        boolean result;

        switch (theCode) {
        case OP_EQ:
            result = (comp == 0);
            break;
        case OP_NEQ:
            result = (comp != 0);
            break;
        case OP_GT:
            result = (comp > 0);
            break;
        case OP_GE:
            result = (comp >= 0);
            break;
        case OP_LT:
            result = (comp < 0);
            break;
        case OP_LE:
            result = (comp <= 0);
            break;
        default:
            throw new QueryStateException(
                "Invalid operation code: " + theCode);
        }

        FieldValueImpl res = FieldDefImpl.booleanDef.createBoolean(result);
        rcb.setRegVal(theResultReg, res);

        state.done();
        return true;
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {
        theLeftOp.reset(rcb);
        theRightOp.reset(rcb);
        PlanIterState state = rcb.getState(theStatePos);
        state.reset(this);
    }

    @Override
    public void close(RuntimeControlBlock rcb) {

        PlanIterState state = rcb.getState(theStatePos);
        if (state == null) {
            return;
        }

        theLeftOp.close(rcb);
        theRightOp.close(rcb);
        state.close();
    }

    /**
     * Compare 2 values for the order-relation specified by the given opCode.
     * If the values are complex, the method will, in general, call itself
     * recursivelly on the contained values.
     *
     * The method retuns 2 pieces of info (inside the "res" out param):
     *
     * a. Whether either v0 or v1 is NULL.
     *
     * b1. If a is not true and the operator is = or !=, an integer which is
     *     equal to 0 if v0 == v1, and non-0 if v0 != v1.
     *
     * b2. If a is not true and the operator is >, >=, <, or <=, an integer
     *     which is equal to 0 if v0 == v1, greater than 0 if v0 > v1, and
     *     less than zero if v0 < v1.
     */
    static void compare(
        FieldValueImpl v0,
        FieldValueImpl v1,
        FuncCode opCode,
        CompResult res,
        Location location) {

        if (v0.isNull() || v1.isNull()) {
            res.haveNull = true;
            return;
        }

        Type tc0 = v0.getType();
        Type tc1 = v1.getType();

        try {
            switch (tc0) {

            case INTEGER: {
                switch (tc1) {
                case INTEGER:
                    res.comp = IntegerValueImpl.compare(v0.getInt(),
                        v1.getInt());
                    return;
                case LONG:
                    res.comp = LongValueImpl.compare(v0.getLong(),
                        v1.getLong());
                    return;
                case FLOAT:
                    res.comp = Float.compare(v0.getInt(), v1.getFloat());
                    return;
                case DOUBLE:
                    res.comp = Double.compare(v0.getInt(), v1.getDouble());
                    return;
                default:
                    break;
                }
                break;
            }
            case LONG: {
                switch (tc1) {
                case INTEGER:
                    res.comp = LongValueImpl.compare(v0.getLong(), v1.getLong());
                    return;
                case LONG:
                    res.comp = LongValueImpl.compare(v0.getLong(),
                        v1.getLong());
                    return;
                case FLOAT:
                    res.comp = Float.compare(v0.getLong(), v1.getFloat());
                    return;
                case DOUBLE:
                    res.comp = Double.compare(v0.getLong(), v1.getDouble());
                    return;
                default:
                    break;
                }
                break;
            }
            case FLOAT: {
                switch (tc1) {
                case INTEGER:
                    res.comp = Float.compare(v0.getFloat(), v1.getInt());
                    return;
                case LONG:
                    res.comp = Float.compare(v0.getFloat(), v1.getLong());
                    return;
                case FLOAT:
                    res.comp = Float.compare(v0.getFloat(), v1.getFloat());
                    return;
                case DOUBLE:
                    res.comp = Double.compare(v0.getDouble(), v1.getDouble());
                    return;
                default:
                    break;
                }
                break;
            }
            case DOUBLE: {
                switch (tc1) {
                case INTEGER:
                    res.comp = Double.compare(v0.getDouble(), v1.getInt());
                    return;
                case LONG:
                    res.comp = Double.compare(v0.getDouble(), v1.getLong());
                    return;
                case FLOAT:
                    res.comp = Double.compare(v0.getDouble(), v1.getDouble());
                    return;
                case DOUBLE:
                    res.comp = Double.compare(v0.getDouble(), v1.getDouble());
                    return;
                default:
                    break;
                }
                break;
            }
            case STRING: {
                switch (tc1) {
                case STRING:
                    res.comp = v0.getString().compareTo(v1.getString());
                    return;
                case ENUM:
                    // TODO: optimize this
                    FieldValueImpl enumVal = TypeManager.promote(
                        v0, TypeManager.createValueType(v1));

                    if (enumVal == null) {
                        break;
                    }

                    res.comp = compareEnums(enumVal, v1);
                    return;
                default:
                    break;
                }
                break;
            }
            case ENUM: {
                switch (tc1) {
                case STRING:
                    FieldValueImpl enumVal = TypeManager.promote(
                        v1, TypeManager.createValueType(v0));

                    if (enumVal == null) {
                        break;
                    }

                    res.comp = compareEnums(v0, enumVal);
                    return;
                case ENUM:
                    res.comp = v0.compareTo(v1);
                    return;
                default:
                    break;
                }
                break;
            }
            case BOOLEAN: {
                res.comp = v0.compareTo(v1);
                return;
            }
            case BINARY:
            case FIXED_BINARY: {
                if (tc1 != Type.BINARY && tc1 != Type.FIXED_BINARY) {
                    break;
                }

                if (opCode != FuncCode.OP_EQ && opCode != FuncCode.OP_NEQ) {
                    throw new QueryException(
                        "Invalid comparison operator between two binary " +
                        "values: " + opCode +
                        "\nBinaries can be compared for equality only",
                        location);
                }

                res.comp =
                    (Arrays.equals(v0.getBytes(), v1.getBytes()) ? 0 : 1);
                return;
            }
            case RECORD: {
                if (tc1 != Type.RECORD) {
                    break;
                }

                if (opCode != FuncCode.OP_EQ && opCode != FuncCode.OP_NEQ) {
                    throw new QueryException(
                        "Invalid comparison operator between two record " +
                        "values: " + opCode +
                        "\nRecords can be compared for equality only",
                        location);
                }

                RecordValueImpl r0 = (RecordValueImpl)v0;
                RecordValueImpl r1 = (RecordValueImpl)v1;
                compareRecordsOrMaps(r0, r1, opCode, res, location);
                return;
            }
            case MAP: {
                if (tc1 != Type.MAP) {
                    break;
                }

                if (opCode != FuncCode.OP_EQ && opCode != FuncCode.OP_NEQ) {
                    throw new QueryException(
                        "Invalid comparison operator between two map " +
                        "values: " + opCode +
                        "\nMaps can be compared for equality only", location);
                }

                MapValueImpl m0 = (MapValueImpl)v0;
                MapValueImpl m1 = (MapValueImpl)v1;
                compareRecordsOrMaps(m0, m1, opCode, res, location);
                return;
            }
            case ARRAY: {
                if (tc1 != Type.ARRAY) {
                    break;
                }

                ArrayValueImpl a0 = (ArrayValueImpl)v0;
                ArrayValueImpl a1 = (ArrayValueImpl)v1;

                if (opCode == FuncCode.OP_EQ || opCode == FuncCode.OP_NEQ) {
                    if (a0.size() != a1.size()) {
                        res.comp = 1;
                        return;
                    }
                }

                int minSize = Math.min(a0.size(), a1.size());

                for (int i = 0; i < minSize; ++i) {

                    FieldValueImpl elem0 = a0.getElement(i);
                    FieldValueImpl elem1 = a1.getElement(i);
                    // can either elem0 or elem1 ever be null ????
                    assert(elem0 != null);
                    assert(elem1 != null);

                    compare(elem0, elem1, opCode, res, location);

                    if (res.comp != 0 || res.haveNull) {
                        return;
                    }
                }

                if (a0.size() != minSize) {
                    res.comp = 1;
                    return;
                } else if (a1.size() != minSize) {
                    res.comp = -1;
                    return;
                } else {
                    res.comp = 0;
                    return;
                }
            }
            default:
                throw new QueryStateException(
                    "Unexpected operand type in comparison operator: " + tc0);
            }
        } catch (ClassCastException e) {
        }

        throw new QueryException(
            "Cannot compare value " + v0 + "\nof type :\n" +
            v0.getDefinition() + "\nwith value " + v1 + "\nof type :\n" +
            v1.getDefinition(), location);
    }

    /**
     * Comapre 2 records or 2 maps for equality
     */
    static <T extends ComplexValueImpl> void compareRecordsOrMaps(
        T v0,
        T v1,
        FuncCode opCode,
        CompResult res,
        Location location) {

        if (v0.size() != v1.size()) {
            res.comp = 1;
            return;
        }

        for (Map.Entry<String, FieldValue> e0 : v0.getMap().entrySet()) {

            String k0 = e0.getKey();
            FieldValueImpl fv0 = (FieldValueImpl)e0.getValue();
            FieldValueImpl fv1 = v1.getFieldValue(k0);

            if (fv1 == null) {
                res.comp = 1;
                return;
            }

            compare(fv0, fv1, opCode, res, location);

            if (res.comp != 0 || res.haveNull) {
                return;
            }
        }

        res.comp = 0;
        return;
    }

    static int compareEnums(FieldValueImpl v0, FieldValueImpl v1) {

        EnumValueImpl e0 = (EnumValueImpl)v0;
        EnumValueImpl e1 = (EnumValueImpl)v1;
        EnumDefImpl def0 = e0.getDefinition();
        EnumDefImpl def1 = e1.getDefinition();

        if (def0.valuesEqual(def1)) {
            int idx0 = e0.getIndex();
            int idx1 = e1.getIndex();
            return ((Integer)idx0).compareTo(idx1);
        }

        throw new ClassCastException("Enums do not have the same type");
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {
        theLeftOp.display(sb, formatter);
        sb.append(",\n");
        theRightOp.display(sb, formatter);
    }
}
