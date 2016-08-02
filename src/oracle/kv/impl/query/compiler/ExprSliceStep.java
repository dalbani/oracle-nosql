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

import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.ExprType.TypeCode;
import oracle.kv.impl.query.types.TypeManager;


/**
 * Represents a "slicing" step in a path expression. A Slicing step selects
 * elements of arrays based only on the element positions.
 *
 * Syntactically, an slicing step looks like this:
 *
 * input_expr[low_expr? : high_expr?]
 *
 * Notice however that if both low_expr and high_expr are missing, the ":" is
 * actually not allowed, i.e., in this case the range step looks like this:
 *
 * input_expr[]
 *
 * The input_expr is supposed to return a sequence of zero or more complex
 * values. low_expr and high_expr are called the "boundary" exprs. Each is
 * supposed to return at most one value of type Integer.
 *
 * For each value in the input sequence, the step computes zero or more result
 * values. The overall result of the step is the concatenation of the results
 * produced for each input value, in the order of their computation.
 *
 * Let V be the current value the step operates upon.
 *
 * 1. If V is not an array, an error is raised.
 *
 * 2. If V is an array, the boundary exprs are are computed, if present. The
 *    boundary exprs may reference V via the $$ variable. Note that if a
 *    boundary expr does not reference $$, it does not need to be computed for
 *    each V; it can be computed only once, before any of the input values are
 *    processed.
 *
 *    Let L and H be the values returned by the low and high exprs, respectively.
 *    If the low_expr is absent or returns an empty result, L is set to 0. If
 *    the high_expr is absent or returns an empty result, H is set to the size
 *    of the array - 1. If L is < 0, L is set to 0. If H > array_size - 1, H is
 *    set to array_size - 1.
 *
 *    After L and H are computed, the step selects all the elements between
 *    positions L and H. If L > H no elements are selected.
 */
class ExprSliceStep extends Expr {

    private Expr theInput;

    private Expr theLowExpr;

    private Expr theHighExpr;

    private Long theLowValue;

    private Long theHighValue;

    private ExprVar theCtxItemVar;

    private boolean theIsUnarySlice;

    ExprSliceStep(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        Expr input) {

        super(qcb, sctx, ExprKind.SLICE_STEP, location);

        theInput = ExprPromote.create(null, input, TypeManager.ANY_ARRAY_STAR);
        theInput.addParent(this);
    }

    /*
     * This constructor is used when converting a filter step to a single-pos
     * slice step.
     */
    ExprSliceStep(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        Expr input,
        Long pos) {

        super(qcb, sctx, ExprKind.SLICE_STEP, location);
        theIsUnarySlice = true;

        theInput = ExprPromote.create(null, input, TypeManager.ANY_ARRAY_STAR);
        theInput.addParent(this);

        if (pos.intValue() < 0) {
            theLowValue = new Long(0);
            theHighValue = pos;
        } else {
            theLowValue = pos;
            theHighValue = pos;
        }

        checkConst();
    }

    void addCtxVars(ExprVar ctxItemVar) {
        theCtxItemVar = ctxItemVar;
    }

    void addBoundaryExprs(Expr lowExpr, Expr highExpr) {

        theLowExpr = lowExpr;
        theHighExpr = highExpr;

        if (theLowExpr != null) {
            theLowExpr.addParent(this);
        }
        if (theHighExpr != null) {
            theHighExpr.addParent(this);
        }

        if (theLowExpr == null && theHighExpr == null) {
            theCtxItemVar = null;
        }

        checkConst();

        if (!isConst()) {
            if (theLowExpr != null) {
                theLowExpr = ExprPromote.create(
                    this, theLowExpr, TypeManager.LONG_QSTN());
            }

            if (theHighExpr != null) {
                theHighExpr = ExprPromote.create(
                    this, theHighExpr, TypeManager.LONG_QSTN());
            }
        }
    }

    @Override
    int getNumChildren() {

        if (theLowExpr == null && theHighExpr == null) {
            return 1;
        }

        if (theLowExpr == null || theHighExpr == null) {
            return 2;
        }

        return 3;
    }

    @Override
    Expr getInput() {
        return theInput;
    }

    void setInput(Expr newExpr, boolean destroy) {
        theInput.removeParent(this, destroy);
        theInput = newExpr;
        newExpr.addParent(this);
    }

    Expr getLowExpr() {
        return theLowExpr;
    }

    void setLowExpr(Expr newExpr, boolean destroy) {
        theLowExpr.removeParent(this, destroy);
        theLowExpr = newExpr;
        newExpr.addParent(this);
    }

    void removeLowExpr(boolean destroy) {
        theLowExpr.removeParent(this, destroy);
        theLowExpr = null;
    }

    Expr getHighExpr() {
        return theHighExpr;
    }

    void setHighExpr(Expr newExpr, boolean destroy) {
        theHighExpr.removeParent(this, destroy);
        theHighExpr = newExpr;
        newExpr.addParent(this);
    }

    void removeHighExpr(boolean destroy) {
        theHighExpr.removeParent(this, destroy);
        theHighExpr = null;
    }

    Long getLowValue() {
        return theLowValue;
    }

    Long getHighValue() {
        return theHighValue;
    }

    ExprVar getCtxItemVar() {
        return theCtxItemVar;
    }

    boolean hasBounds() {
        return (theLowExpr != null || theHighExpr != null ||
                theLowValue != null || theHighValue != null);
    }

    boolean isConst() {
        return (theLowExpr == null && theHighExpr == null);
    }

    void checkConst() {

        if (isConst()) {
            assert(theCtxItemVar == null);
            return;
        }

        if (theLowValue == null) {
            if (theLowExpr == null) {
                theLowValue = new Long(0);
            } else if (theLowExpr.getKind() == ExprKind.CONST) {
                theLowValue = handleConstExpr(theLowExpr);
                if (theLowValue.longValue() < 0) {
                    theLowValue = new Long(0);
                }
                theLowExpr.removeParent(this, true/*destroy*/);
                theLowExpr = null;
            }
        }

        if (theHighValue == null) {
           if (theHighExpr == null) {
               theHighValue = new Long(Integer.MAX_VALUE);
           } else if (theHighExpr.getKind() == ExprKind.CONST) {
               theHighValue = handleConstExpr(theHighExpr);
               theHighExpr.removeParent(this, true/*destroy*/);
               theHighExpr = null;
           }
        }

        if (theLowValue != null &&
            theHighValue != null &&
            theLowValue.longValue() == theHighValue.longValue()) {

            theIsUnarySlice = true;
        }

        if (theCtxItemVar != null && !theCtxItemVar.hasParents()) {
            theCtxItemVar = null;
        }
    }

    private Long handleConstExpr(Expr expr) {

        TypeCode c = expr.getType().getCode();
        FieldValueImpl value = ((ExprConst)expr).getValue();

        if (c == TypeCode.INT || c == TypeCode.LONG) {
            return new Long(value.getLong());
        }
        throw new QueryException(
            "Boundary const in slice step has invalid type.\n" +
            "Expected long or integer type. Actual type is: \n" +
            expr.getType(), expr.getLocation());
    }

    @Override
    ExprType computeType() {

        ExprType inType = theInput.getType();
        Quantifier inQuant = inType.getQuantifier();
        Quantifier outQuant;

        if (!inType.isArray() && !inType.isAny()  && !inType.isEmpty()) {
            throw new QueryException(
                "Wrong input type for slice step.\n" +
                "Expected array type. Actual type is: \n" + inType,
                getLocation());
        }

        if (inType.isEmpty()) {
            return TypeManager.EMPTY();
        }

        checkConst();

        if (isConst()) {

            if (theLowValue != null && theHighValue != null) {
                if (theLowValue.compareTo(theHighValue) > 0) {
                    return TypeManager.EMPTY();
                }
            }

            if (theHighValue != null && theHighValue.compareTo(0L) < 0) {
                return TypeManager.EMPTY();
            }
        }

        outQuant = Quantifier.STAR;

        if (theIsUnarySlice) {
            outQuant = TypeManager.getUnionQuant(inQuant, Quantifier.QSTN);
        }

        return TypeManager.createType(inType.getElementType(), outQuant);
    }

    @Override
    void display(StringBuilder sb, QueryFormatter formatter) {

        theInput.display(sb, formatter);

        if (isConst()) {
            sb.append("[");
            if (theLowValue != null || theHighValue != null) {
                if (theLowValue == theHighValue) {
                    sb.append(".").append(theLowValue).append(".");
                } else {
                    if (theLowValue != null) {
                        sb.append(theLowValue);
                    }
                    sb.append("..");
                    if (theHighValue != null) {
                        sb.append(theHighValue);
                    }
                }
            }
            sb.append("]");

        } else {
            sb.append(".\n");
            formatter.indent(sb);
            sb.append("[\n");
            if (theLowExpr != null) {
                theLowExpr.display(sb, formatter);
                sb.append("\n");
            }
            formatter.indent(sb);
            sb.append("..\n");
            if (theHighExpr != null) {
                theHighExpr.display(sb, formatter);
            }
            sb.append("]");
        }
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {
    }
}
