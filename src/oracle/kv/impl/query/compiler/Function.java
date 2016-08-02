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

import java.util.ArrayList;

import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.TypeManager;


/*
 * Base class for representing functions.
 *
 * theCode:
 * Each builtin function has a code that serves as its unique id.
 *
 * theName:
 * The function name. For now, it must be unique among all functions that are
 * visible to a query, i.e., no function overloading (something to reconsider
 * in the future ????).
 *
 * theIsVariadic:
 * Whether the function accepts a variable number of arguments. A variadic
 * function will, in general, have a fixed number N ( >= 0) of "declared"
 * params, followed by a variable number of "undeclared" params. All the
 * undeclared params are assumed to have the same type and this type is equal
 * to the type of the last declared param, or to ANY_STAR, if there are no
 * declared params.
 *
 * theParamTypes:
 * The types of the function's parameters. If the function is variadic,
 * theParamTypes stores the types of the declared params only.
 *
 * theReturnType:
 * The declared return type of the function. Depending on the args used in an
 * actual call of this function, a more tight return type may be deducable.
 * This is done by the getReturnType() method.
 */
public abstract class Function {

    final FuncCode theCode;

    final String theName;

    final boolean theIsVariadic;

    final ArrayList<ExprType> theParamTypes;

    final ExprType theReturnType;


    Function(
        FuncCode code,
        String name,
        ExprType retType) {

        theCode = code;
        theName = name;
        theIsVariadic = false;
        theParamTypes = null;
        theReturnType = retType;
    }

    Function(
        FuncCode code,
        String name,
        ExprType paramType,
        ExprType retType) {
        this(code, name, paramType, retType, false);
    }

    Function(
        FuncCode code,
        String name,
        ExprType paramType,
        ExprType retType,
        boolean isVariadic) {

        theCode = code;
        theName = name;
        theIsVariadic = isVariadic;
        theParamTypes = new ArrayList<ExprType>(1);
        theParamTypes.add(paramType);
        theReturnType = retType;
    }

    @SuppressWarnings("unused")
    Function(
        FuncCode code,
        String name,
        ExprType param1Type,
        ExprType param2Type,
        ExprType retType) {

        theCode = code;
        theName = name;
        theIsVariadic = false;
        theParamTypes = new ArrayList<ExprType>(2);
        theParamTypes.add(param1Type);
        theParamTypes.add(param1Type);
        theReturnType = retType;
    }

    Function(
        FuncCode code,
        String name,
        ArrayList<ExprType> paramTypes,
        ExprType retType) {

        theCode = code;
        theName = name;
        theIsVariadic = false;
        theParamTypes = paramTypes;
        theReturnType = retType;
    }

    final FuncCode getCode() {
        return theCode;
    }

    final String getName() {
        return theName;
    }

    final int getArity() {
        return theParamTypes.size();
    }

    boolean isVariadic() {
        return theIsVariadic;
    }

    boolean isValueComparison() {
        return false;
    }

    boolean isAnyComparison() {
        return false;
    }

    final boolean isComparison() {
        return isValueComparison() || isAnyComparison();
    }


    /*
     *
     */
    ExprType getParamType(int i) {

        if (i < theParamTypes.size()) {
            return theParamTypes.get(i);
        }

        assert(isVariadic());

        if (theParamTypes.isEmpty()) {
            return TypeManager.ANY_STAR();
        }

        return theParamTypes.get(theParamTypes.size() - 1);
    }

    /*
     * This method is redefined by any function that can compute a more
     * specific type based on the argument expressions.
     */
    @SuppressWarnings("unused")
    ExprType getRetType(ExprFuncCall caller) {
        return theReturnType;
    }


    /*
     * Redefined by subclasses that actually need to do further analysis of a
     * function call, based on the argument expressions. Such analysis may
     * determine that the function call must be replaced by another expr, in which
     * case that other expr is created and returned.
     */
    Expr normalizeCall(ExprFuncCall funcCall) {
        return funcCall;
    }

    abstract PlanIter codegen(
        CodeGenerator codegen,
        Expr funcCall,
        PlanIter[] argIters);

    static Function getFunction(FuncCode c) {
        return CompilerAPI.getFuncLib().getFunc(c);
    }
}
