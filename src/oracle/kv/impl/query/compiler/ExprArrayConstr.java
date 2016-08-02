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
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.TypeManager;

/**
 * Constructs an array containing the values returned by the input exprs.
 * Initially an empty array is created. Then each input expr is computed
 * and its result is appended in the array. The input exprs are computed
 * in the order they appear in the query.
 *
 * The element type of the array is determined by the type of the 1st value
 * of the 1st input expr. If the type of any other value is different then
 * the type of the 1st value, an error is thrown.
 */
class ExprArrayConstr extends Expr {

    private final ArrayList<Expr> theArgs;

    ExprArrayConstr(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        ArrayList<Expr> args) {

        super(qcb, sctx, ExprKind.ARRAY_CONSTR, location);
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

    Expr getArg(int i) {
        return theArgs.get(i);
    }

    void setArg(int i, Expr newExpr, boolean destroy) {
        theArgs.get(i).removeParent(this, destroy);
        theArgs.set(i, newExpr);
        newExpr.addParent(this);
    }

    void removeArg(int i, boolean destroy) {
        if (!theArgs.get(i).getType().isEmpty()) {
            throw new QueryStateException(
                "Cannot remove non-empty input expr from array " +
                "constructor expr");
        }
        theArgs.get(i).removeParent(this, destroy);
        theArgs.remove(i);
    }

    @Override
    ExprType computeType() {

        int numArgs = theArgs.size();

        ExprType elemType = theArgs.get(0).getType();

        if (numArgs == 1) {
            return TypeManager.createArrayType(elemType, Quantifier.ONE);
        }

        for (int i = 1; i < numArgs; ++i) {

            ExprType type = theArgs.get(i).getType();

            for (int j = 0; j < i; ++j) {
                if (!TypeManager.typesIntersect(
                    theArgs.get(j).getType(), type)) {
                    throw new QueryException(
                        "Mixed element types in array constructor",
                        getLocation());
                }
            }

            elemType = TypeManager.getConcatType(elemType, type);
        }

        return TypeManager.createArrayType(elemType, Quantifier.ONE);
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
