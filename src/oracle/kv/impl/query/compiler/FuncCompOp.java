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

package oracle.kv.impl.query.compiler;

import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.QueryException.Location;
import oracle.kv.impl.query.compiler.Expr.ExprKind;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.runtime.CompOpIter;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.types.TypeManager;
import oracle.kv.table.FieldDef.Type;

/*
 * TODO: check SQL comparison ops
 */
class FuncCompOp extends Function {

    FuncCompOp(FuncCode code, String name) {
        super(
            code,
            name,
            TypeManager.ANY_QSTN(), /* param1 */
            TypeManager.ANY_QSTN(), /* param2 */
            TypeManager.BOOLEAN_QSTN()); /* RetType */
    }

    @Override
    boolean isValueComparison() {
        return true;
    }

    @Override
    Expr normalizeCall(ExprFuncCall fncall) {

        Expr op0 = fncall.getArg(0);
        Expr op1 = fncall.getArg(1);

        if (!TypeManager.areTypesComparable(op0.getType(), op1.getType())) {
            throw new QueryException(
                "Incompatible types for comparison operator: \n" +
                "Type1: " + op0.getType() + "\nType2: " + op1.getType(),
                fncall.getLocation());
        }

        FieldDefImpl op0Def = op0.getType().getDef();
        FieldDefImpl op1Def = op1.getType().getDef();
        Type tc0 = op0Def.getType();
        Type tc1 = op1Def.getType();

        /*
         * Nothing to do if the operands have the same kind of type.
         */
        if (tc0 == tc1) {
            return fncall;
        }

        /*
         * If one of the args is a float or double, promote the other arg to
         * double (the promotion will actually work only if the other arg is
         * an integer or long). Also, if one of the args is an enum, promote
         * the other arg to an enum.
         */
        if (tc0 == Type.DOUBLE) {
            op1 = ExprPromote.create(fncall, op1, TypeManager.DOUBLE_QSTN());
            fncall.setArgInternal(1, op1);

        } else if (tc1 == Type.DOUBLE) {
            op0 = ExprPromote.create(fncall, op0, TypeManager.DOUBLE_QSTN());
            fncall.setArgInternal(0, op0);

        } else if (tc0 == Type.FLOAT) {
            op1 = ExprPromote.create(fncall, op1, TypeManager.FLOAT_QSTN());
            fncall.setArgInternal(1, op1);

        } else if (tc1 == Type.FLOAT) {
            op0 = ExprPromote.create(fncall, op0, TypeManager.FLOAT_QSTN());
            fncall.setArgInternal(0, op0);

        } else if (tc0 == Type.ENUM) {
            op1 = ExprPromote.create(fncall, op1, op0.getType());
            fncall.setArgInternal(1, op1);

        } else if (tc1 == Type.ENUM) {
            op0 = ExprPromote.create(fncall, op0, op1.getType());
            fncall.setArgInternal(0, op0);
        }

        op0Def = op0.getType().getDef();
        op1Def = op1.getType().getDef();
        tc0 = op0Def.getType();
        tc1 = op1Def.getType();

        if (tc0 == tc1) {
            return fncall;
        }

        return handleConstOperand(fncall, theCode);
    }

    /**
     * Handle the case where one of the operands is a const. In this case, we
     * should be able to create a new const that has the same type as the other
     * operand. This is important for turning comparison predicates into index
     * search keys.
     *
     * Note: When this method is called, we know already that the types of
     * the operands are comparable but not equal. Furthermore, the type of the
     * const operand, if any, cannot be one of the ANY types.
     *
     * Note: opCode is given as an explicit paramater because this method is
     * also called from FuncAnyOp, in which case we want to pass one of the
     * normal comparison op codes instead of the any op code that is stored
     * in the Function obj of fncall.
     */
    static Expr handleConstOperand(ExprFuncCall fncall, FuncCode opCode) {

        Expr arg0 = fncall.getArg(0);
        Expr arg1 = fncall.getArg(1);

        Expr varOp = null;
        ExprConst constOp = null;
        int constPos;

        if (arg0.getKind() == ExprKind.CONST) {
            constOp = (ExprConst)arg0;
            constPos = 0;
            varOp = arg1;
            opCode = swapCompOp(opCode);
        } else if (arg1.getKind() == ExprKind.CONST) {
            constOp = (ExprConst)arg1;
            constPos = 1;
            varOp = arg0;
        } else {
            return fncall;
        }

        FieldDefImpl constDef = constOp.getType().getDef();
        FieldDefImpl varDef = varOp.getType().getDef();

        switch (varDef.getType()) {

        case INTEGER:
            if (constDef.getType() == Type.LONG) {
                return longToInt(fncall, constOp, constPos, opCode);
            }
            break;

        case LONG:
            if (constDef.getType() == Type.INTEGER) {
                return intToLong(fncall, constOp, constPos);
            }
            break;

        case FLOAT:
            if (constDef.getType() == Type.DOUBLE) {
                return doubleToFloat(fncall, constOp, constPos, opCode);
            }
            break;

        case DOUBLE:
            if (constDef.getType() == Type.FLOAT) {
                return floatToDouble(fncall, constOp, constPos);
            }
            break;

        case STRING:
        case BOOLEAN:
        case ENUM:
        case ARRAY:
        case MAP:
        case RECORD:
            /* raise an error */
            break;

        case BINARY:
            /* The const may be fixed binary */
        case FIXED_BINARY:
            /* The const may be binary */
        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
            return fncall;

        default:
            assert(false);
            break;
        }

        /* This is sanitiy checking */
        throw new QueryStateException(
           "Unexpected type for constant value in comparison predicate\n" +
           "Expected: " + varDef + "\n" +
           "Found: " + constDef);
    }

    /**
     * Convert a LONG const expr to an INTGER const expr. The LONG const expr
     * is an operand of a comparison expr, whose the other is an INTEGER-typed
     * expr. Depending on the actual long value and the comp operator, this
     * method may determine that the comparison is always false or always true,
     * in which case it will return a BOOLEAN const expr.
     */
    static Expr longToInt(
        ExprFuncCall compExpr,
        ExprConst longConst,
        int constPos,
        FuncCode opCode) {

        FieldValueImpl constVal = longConst.getValue();
        Location loc = longConst.getLocation();
        QueryControlBlock qcb = compExpr.getQCB();
        StaticContext sctx = compExpr.getSctx();

        if (constVal.getLong() < Integer.MIN_VALUE ||
            constVal.getLong() > Integer.MAX_VALUE) {

            switch(opCode) {
            case OP_EQ:
                return new ExprConst(qcb, sctx, loc, false);

            case OP_NEQ:
                return new ExprConst(qcb, sctx, loc, true);

            case OP_LT:
            case OP_LE:
                if (constVal.getLong() < Integer.MIN_VALUE) {
                    return new ExprConst(qcb, sctx, loc, false);
                }
                return new ExprConst(qcb, sctx, loc, true);

            case OP_GT:
            case OP_GE:
                if (constVal.getLong() < Integer.MIN_VALUE) {
                    return new ExprConst(qcb, sctx, loc, true);
                }
                return new ExprConst(qcb, sctx, loc, false);

            default:
                assert(false);
                return null;
            }
        }

        int v = (int)constVal.getLong();
        constVal = FieldDefImpl.integerDef.createInteger(v);
        ExprConst intExpr = new ExprConst(qcb, sctx, loc, constVal);
        compExpr.setArg(constPos, intExpr, true/*destroy*/);
        return compExpr;
    }

    /**
     * Similar story as the longToInt method above.
     */
    static Expr intToLong(
        ExprFuncCall compExpr,
        ExprConst intConst,
        int constPos) {

        FieldValueImpl constVal = intConst.getValue();
        Location loc = intConst.getLocation();
        QueryControlBlock qcb = compExpr.getQCB();
        StaticContext sctx = compExpr.getSctx();

        long v = constVal.getInt();
        constVal = FieldDefImpl.longDef.createLong(v);
        ExprConst longExpr = new ExprConst(qcb, sctx, loc, constVal);
        compExpr.setArg(constPos, longExpr, true/*destroy*/);
        return compExpr;
    }

    /**
     * Convert a DOUBLE const expr to a FLOAT const expr. The DOUBLE const expr
     * is an operand of a comparison expr, whose the other is a FLOAT-typed
     * expr. Depending on the actual double value and the comp operator, this
     * method may determine that the comparison is always false or always true,
     * in which case it will return a BOOLEAN const expr.
     */
    static Expr doubleToFloat(
        ExprFuncCall compExpr,
        ExprConst doubleConst,
        int constPos,
        FuncCode opCode) {

        FieldValueImpl constVal = doubleConst.getValue();
        Location loc = doubleConst.getLocation();
        QueryControlBlock qcb = compExpr.getQCB();
        StaticContext sctx = compExpr.getSctx();

        if (constVal.getDouble() < Float.MIN_VALUE ||
            constVal.getDouble() > Float.MAX_VALUE) {

            switch(opCode) {
            case OP_EQ:
                return new ExprConst(qcb, sctx, loc, false);

            case OP_NEQ:
                return new ExprConst(qcb, sctx, loc, true);

            case OP_LT:
            case OP_LE:
                if (constVal.getDouble() < Float.MIN_VALUE) {
                    return new ExprConst(qcb, sctx, loc, false);
                }
                return new ExprConst(qcb, sctx, loc, true);

            case OP_GT:
            case OP_GE:
                if (constVal.getDouble() < Float.MIN_VALUE) {
                    return new ExprConst(qcb, sctx, loc, true);
                }
                return new ExprConst(qcb, sctx, loc, false);

            default:
                assert(false);
                return null;
            }
        }

        float floatVal = (float)constVal.getDouble();
        constVal = FieldDefImpl.floatDef.createFloat(floatVal);

        ExprConst floatExpr = new ExprConst(qcb, sctx, loc, constVal);

        compExpr.setArg(constPos, floatExpr, true/*destroy*/);
        return compExpr;
    }

    /**
     * Similar story as the doubleToFloat method above.
     */
    static Expr floatToDouble(
        ExprFuncCall compExpr,
        ExprConst floatConst,
        int constPos) {

        FieldValueImpl constVal = floatConst.getValue();
        Location loc = floatConst.getLocation();
        QueryControlBlock qcb = compExpr.getQCB();
        StaticContext sctx = compExpr.getSctx();

        float v = constVal.getFloat();
        constVal = FieldDefImpl.doubleDef.createDouble(v);
        ExprConst doubleExpr = new ExprConst(qcb, sctx, loc, constVal);
        compExpr.setArg(constPos, doubleExpr, true/*destroy*/);
        return compExpr;
    }

    static FuncCode swapCompOp(FuncCode op) {

        switch (op) {
        case OP_GT:
            return FuncCode.OP_LT;
        case OP_GE:
            return FuncCode.OP_LE;
        case OP_LT:
            return FuncCode.OP_GT;
        case OP_LE:
            return FuncCode.OP_GE;
        default:
            return op;
        }
    }

    @Override
    PlanIter codegen(
        CodeGenerator codegen,
        Expr fncall,
        PlanIter[] argIters) {

        int resultReg = codegen.allocateResultReg(fncall);

        return new CompOpIter(fncall, resultReg, theCode, argIters);
    }
}
