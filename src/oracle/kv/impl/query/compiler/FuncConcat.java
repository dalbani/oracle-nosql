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

import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.runtime.ConcatIter;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.TypeManager;

/**
 * Represents the concatanation of the results of an arbitrary number of
 * input exprs. The results from each input may have different data types.
 *
 * For now, this is an internal function, and is only used to represent the
 * "empty" expr: an expr that returns nothing. The empty expr is used, for
 * example, when the compiler that the WHERE condition of a SFW is always
 * false; in this case, the whole SFW expr is replaced with an empty expr.
 */
class FuncConcat extends Function {

    FuncConcat() {
        super(
            FuncCode.OP_CONCAT, "CONCAT",
            TypeManager.ANY_STAR(),
            TypeManager.ANY_STAR() /*retType*/,
            true /*isVariadic*/);
    }


    @Override
    ExprType getRetType(ExprFuncCall caller) {

        int numArgs = caller.getNumArgs();

        if (numArgs == 0) {
            return TypeManager.EMPTY();
        }

        ExprType type = caller.getArg(0).getType();

        for (int i = 1; i < numArgs; ++i) {
            type = TypeManager.getConcatType(type, caller.getArg(i).getType());
        }

        return theReturnType;
    }

    @Override
    PlanIter codegen(
        CodeGenerator codegen,
        Expr caller,
        PlanIter[] argIters) {

        int resultReg = codegen.allocateResultReg(caller);

        return new ConcatIter(caller, resultReg, argIters);
    }
}
