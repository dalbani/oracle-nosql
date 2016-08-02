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

import java.util.Arrays;

import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.runtime.ArithOpIter;
import oracle.kv.impl.query.runtime.ConstIter;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.ExprType.TypeCode;
import oracle.kv.impl.query.types.TypeManager;

/**
 * Arithmetic operations + - * / function implementation.
 *
 * Note: the last argument must be a constant String that contains the order
 * of the operations.
 */
public class FuncArithOp extends Function {

    FuncArithOp(FunctionLib.FuncCode code, String name) {
        super(code,
              name,
              TypeManager.ANY_ATOMIC_QSTN() /* params */,
              TypeManager.ANY_ATOMIC_QSTN() /* RetType */,
              true /*isVariadic*/);
    }

    @Override
    PlanIter codegen(
        CodeGenerator codegen,
        Expr funcCall,
        PlanIter[] argIters) {

        int resultReg = codegen.allocateResultReg(funcCall);

        // last arg should be a const string
        assert argIters != null && argIters.length >= 3;
        assert argIters[argIters.length - 1] instanceof ConstIter;
        assert ((ConstIter)(argIters[argIters.length - 1])).getValue().isString();

        String ops = ((ConstIter)
                      (argIters[argIters.length - 1])).getValue().castAsString();

        PlanIter[] newArgIters = Arrays.copyOf(argIters, argIters.length - 1);

        return new ArithOpIter(funcCall, resultReg, theCode, newArgIters, ops);
    }

    @Override
    ExprType getRetType(ExprFuncCall caller) {

        TypeCode typeCode = TypeCode.INT;
        Quantifier quantifier = Quantifier.ONE;
        int numArgs = caller.getNumArgs();

        // last argument is the string const with the operations order
        assert caller.getArg(numArgs - 1).getType().getDef().isString();

        for (int i = 0; i < numArgs - 1; i++) {

            ExprType argType = caller.getArg(i).getType();

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

            if (typeCode == TypeCode.ANY_ATOMIC) {
                continue;
            }

            switch (argType.getCode()) {
            case INT:
                break;
            case LONG:
                if (typeCode == TypeCode.INT) {
                    typeCode = TypeCode.LONG;
                }
                break;
            case FLOAT:
                if (typeCode == TypeCode.INT || typeCode == TypeCode.LONG) {
                    typeCode = TypeCode.FLOAT;
                }
                break;
            case DOUBLE:
                typeCode = TypeCode.DOUBLE;
                break;
            case ANY_ATOMIC:
            case ANY:
                typeCode = TypeCode.ANY_ATOMIC;
                break;
            default:
                throw new QueryException(
                    "Operand in arithmetic operation has illegal type\n" +
                    "Operand : " + i + " type :\n" + argType,
                    caller.getLocation());
            }

        }

        return TypeManager.getBuiltinType(typeCode, quantifier);
    }
}
