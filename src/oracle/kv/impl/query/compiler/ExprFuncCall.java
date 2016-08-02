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

import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.types.ExprType;


/*
 * ExprFuncCall represents a function call to a first-order function, i.e., a
 * function that does not take as input another function and does not return
 * another function as a result.
 */
class ExprFuncCall extends Expr {

    private final ArrayList<Expr> theArgs;

    private final Function theFunction;

    ExprFuncCall(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        Function f,
        ArrayList<Expr> args) {

        super(qcb, sctx, ExprKind.FUNC_CALL, location);
        theFunction = f;
        theArgs = args;

        for (Expr arg : args) {
            arg.addParent(this);
        }
    }

    @Override
    int getNumChildren() {
        return theArgs.size();
    }

    int getNumArgs() {
        return theArgs.size();
    }

    @Override
    Expr getInput() {
        if (getNumArgs() != 1) {
            throw new ClassCastException(
                "Expression does not have a single input: " + getClass());
        }
        return getArg(0);
    }

    Expr getArg(int i) {
        return theArgs.get(i);
    }

    void setArg(int i, Expr newExpr, boolean destroy) {
        theArgs.get(i).removeParent(this, destroy);
        theArgs.set(i, newExpr);
        newExpr.addParent(this);
    }

    void removeArg(int i, boolean destroy) {
        if (!theFunction.isVariadic()) {
            throw new QueryStateException(
                "Cannot remove argument from a non-variadic function");
        }
        theArgs.get(i).removeParent(this, destroy);
        theArgs.remove(i);
    }

    void setArgInternal(int i, Expr arg) {
        theArgs.set(i, arg);
    }

    Function getFunction() {
        return theFunction;
    }

    @Override
    ExprType computeType() {
        return theFunction.getRetType(this);
    }

    @Override
    void display(StringBuilder sb, QueryFormatter formatter) {

        formatter.indent(sb);
        sb.append(theFunction.getName());

        sb.append("\n");
        formatter.indent(sb);
        sb.append("[\n");
        formatter.incIndent();
        displayContent(sb, formatter);
        formatter.decIndent();
        sb.append("\n");
        formatter.indent(sb);
        sb.append("]");
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {
        for (int i = 0; i < theArgs.size(); ++i) {
            theArgs.get(i).display(sb, formatter);
            if (i < theArgs.size() - 1) {
                sb.append(",\n");
            }
        }
    }
}
