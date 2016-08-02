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

import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.runtime.ArithUnaryOpIter;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.TypeManager;

/**
 * Arithmetic unary operations + - function implementation.
 *
 * Note: only - is implemented, + is skipped.
 */
public class FuncArithUnaryOp extends Function {

    FuncArithUnaryOp(FunctionLib.FuncCode code, String name) {
        super(code,
            name,
            TypeManager.ANY_ATOMIC_QSTN() /* params */,
            TypeManager.ANY_ATOMIC_QSTN() /* RetType */);
    }

    @Override
    PlanIter codegen(
        CodeGenerator codegen,
        Expr funcCall,
        PlanIter[] argIters) {

        int resultReg = codegen.allocateResultReg(funcCall);

        assert argIters != null && argIters.length == 1;
        assert (theCode == FunctionLib.FuncCode.OP_ARITH_UNARY );

        return new ArithUnaryOpIter(funcCall, resultReg, theCode, argIters[0]);
    }

    @Override
    ExprType getRetType(ExprFuncCall caller) {

        ExprType.TypeCode typeCode;
        ExprType.Quantifier quantifier = ExprType.Quantifier.ONE;

        assert caller.getNumArgs() == 1;

        ExprType argType = caller.getArg(0).getType();

        switch (argType.getQuantifier()) {
        case ONE:
        case PLUS:
            break;
        case QSTN:
        case STAR:
            quantifier = ExprType.Quantifier.QSTN;
            break;
        default:
            throw new QueryStateException(
                "Unknown Quantifier: " + argType.getQuantifier());
        }

        switch (argType.getCode()) {
        case INT:
            typeCode = ExprType.TypeCode.INT;
            break;
        case LONG:
            typeCode = ExprType.TypeCode.LONG;
            break;
        case FLOAT:
            typeCode = ExprType.TypeCode.FLOAT;
            break;
        case DOUBLE:
            typeCode = ExprType.TypeCode.DOUBLE;
            break;
        case ANY_ATOMIC:
        case ANY:
            typeCode = ExprType.TypeCode.ANY_ATOMIC;
            break;
        default:
            throw new QueryException(
                "Operand in unary arithmetic operation has illegal " +
                "type.\nOperand type :\n" + argType, caller.getLocation());
        }

        return TypeManager.getBuiltinType(typeCode, quantifier);
    }
}
