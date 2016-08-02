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

import oracle.kv.table.FieldDef.Type;

import oracle.kv.impl.api.table.FieldDefImpl;

import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.TypeManager;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.runtime.AnyOpIter;


/*
 * TODO: check SQL comparison ops
 */
public class FuncAnyOp extends Function {

    FuncAnyOp(FuncCode code, String name) {
        super(
            code,
            name,
            TypeManager.ANY_STAR(), /* param1 */
            TypeManager.ANY_STAR(), /* param2 */
            TypeManager.BOOLEAN_ONE()); /* RetType */
    }

    @Override
    boolean isAnyComparison() {
        return true;
    }

    @Override
    Expr normalizeCall(ExprFuncCall fncall) {

        Expr op0 = fncall.getArg(0);
        Expr op1 = fncall.getArg(1);

        if (!TypeManager.areTypesComparable(op0.getType(), op1.getType())) {
            return new ExprConst(fncall.getQCB(), fncall.getSctx(),
                                 fncall.getLocation(), false);
        }

        FieldDefImpl op0Def = op0.getType().getDef();
        FieldDefImpl op1Def = op1.getType().getDef();
        Quantifier q0 = op0.getType().getQuantifier();
        Quantifier q1 = op1.getType().getQuantifier();
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
            if (op1Def.isNumeric()) {
                op1 = ExprPromote.create(fncall, op1, TypeManager.DOUBLE_STAR());
                fncall.setArgInternal(1, op1);
            }
        } else if (tc1 == Type.DOUBLE) {
            if (op0Def.isNumeric()) {
                op0 = ExprPromote.create(fncall, op0, TypeManager.DOUBLE_STAR());
                fncall.setArgInternal(0, op0);
            }
        } else if (tc0 == Type.FLOAT) {
            if (op1Def.isNumeric()) {
                op1 = ExprPromote.create(fncall, op1, TypeManager.FLOAT_STAR());
                fncall.setArgInternal(1, op1);
            }
        } else if (tc1 == Type.FLOAT) {
            if (op0Def.isNumeric()) {
                op0 = ExprPromote.create(fncall, op0, TypeManager.FLOAT_STAR());
                fncall.setArgInternal(0, op0);
            }
        } else if (tc0 == Type.ENUM) {
           if (op1Def.isString()) {
               ExprType destType = TypeManager.createType(op0Def, q1);
               op1 = ExprPromote.create(fncall, op1, destType);
               fncall.setArgInternal(1, op1);
           }
        } else if (tc1 == Type.ENUM) {
            if (op0Def.isString()) {
                ExprType destType = TypeManager.createType(op1Def, q0);
                op0 = ExprPromote.create(fncall, op0, destType);
                fncall.setArgInternal(0, op0);
            }
        }

        op0Def = op0.getType().getDef();
        op1Def = op1.getType().getDef();
        tc0 = op0Def.getType();
        tc1 = op1Def.getType();

        if (tc0 == tc1) {
            return fncall;
        }

        return FuncCompOp.handleConstOperand(fncall, anyToComp(theCode));
    }

    public static FuncCode anyToComp(FuncCode op) {

        switch (op) {
        case OP_GT_ANY:
            return FuncCode.OP_GT;
        case OP_GE_ANY:
            return FuncCode.OP_GE;
        case OP_LT_ANY:
            return FuncCode.OP_LT;
        case OP_LE_ANY:
            return FuncCode.OP_LE;
        case OP_EQ_ANY:
            return FuncCode.OP_EQ;
        case OP_NEQ_ANY:
            return FuncCode.OP_NEQ;
        default:
            assert(false);
            return null;
        }
    }

    static FuncCode swapCompOp(FuncCode op) {

        switch (op) {
        case OP_GT_ANY:
            return FuncCode.OP_LT_ANY;
        case OP_GE_ANY:
            return FuncCode.OP_LE_ANY;
        case OP_LT_ANY:
            return FuncCode.OP_GT_ANY;
        case OP_LE_ANY:
            return FuncCode.OP_GE_ANY;
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

        return new AnyOpIter(fncall, resultReg, theCode, argIters);
    }
}
