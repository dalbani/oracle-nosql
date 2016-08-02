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

import oracle.kv.impl.api.table.BooleanValueImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.ExprType.TypeCode;
import oracle.kv.impl.query.types.TypeManager;

/**
 * Represents a "filtering" step in a path expr. In general, a filtering step
 * selects elements of arrays by computing a predicate expression for each
 * element and selecting or rejecting the element depending on whether the
 * predicate result is true or false.
 *
 * Syntactically, a filtering step looks like this:
 *
 * input_expr[pred_expr]
 *
 * A filtering step has 2 operands: an input expr that is supposed to return a
 * sequence of zero or more complex values, and a "predicate" expr that is
 * supposed to return at most one value of type Integer or Boolean
 *
 * For each value in the input sequence, the step computes zero or more result
 * values. The overall result of the step is the concatenation of the results
 * produced for each input value, in the order of their computation.
I*
 * Let V be the current value the step operates upon.
 *
 * 1. If V is not an array value, an error is raised.
 *
 * 2. If V is an array, the step iterates over the array elements and computes
 *    the predicate expr on each element. The predicate expr may reference any
 *    of the following 3 implicitly-declared context variables: $$ is bound to
 *    V (the "context item"), $$elem is the current element in V (the "context
 *    element"), and $$elemPos is the position of the context element within V.
 *    The context element is then either skipped or included in the step result
 *    depending on the result of the predicate expr:
 *
 *    - If the predicate result is a single boolean b, the context element is
 *      skipped/selected if b is false/true respectively.
 *
 *    - If the predicate result is a single long i, the context element is
 *      selected only if its position in the array is equal to i. If i is out
 *      or array bounds, the element is skipped. Notice that in this case,
 *      [pred_expr] is equivalent to [$$elemPos = pred_expr]. Notice also that
 *      if pred_expr does not reference the context element or its position,
 *      and it is known at compile time that it will always return a long (or
 *      subtype), then [pred_expr] is equivalent to the slicing step
 *      [pred_expr:pred_expr].
 *
 *    - If the predicate result is empty, the context element is skipped.
 *
 *    - Error is raised in all other cases.
 *
 * Should we allow negative numbers ???? to count from the end if the array?
 * If i is negative, the element position is counted from the end of the array.
 *
 * TODO: hoist the selector expr (if any) out of the [], if possible. If
 * hoisted and the selector expr has type LONG convert the filtering step
 * to a range step.
 */
class ExprFilterStep extends Expr {

    private Expr theInput;

    private Expr thePredExpr;

    private Object theConstValue;

    private ExprVar theCtxItemVar;

    private ExprVar theCtxElemVar;

    private ExprVar theCtxElemPosVar;

    private ExprVar theCtxKeyVar;

    ExprFilterStep(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        Expr input) {

        super(qcb, sctx, ExprKind.FILTER_STEP, location);
        theInput = input;
        theInput.addParent(this);
    }

    void addCtxVars(
        ExprVar ctxItemVar,
        ExprVar ctxElemVar,
        ExprVar ctxElemPosVar,
        ExprVar ctxKeyVar) {

        theCtxItemVar = ctxItemVar;
        theCtxElemVar = ctxElemVar;
        theCtxElemPosVar = ctxElemPosVar;
        theCtxKeyVar = ctxKeyVar;
    }

    void addPredExpr(Expr pred) {

        assert(thePredExpr == null && pred != null);
        thePredExpr = pred;
        thePredExpr.addParent(this);

        checkConst();

        if (!isConst()) {

            ExprType predType = thePredExpr.getType();

            if (!predType.isAny() &&
                !predType.isAnyAtomic() &&
                !predType.isSubType(TypeManager.LONG_STAR()) &&
                !predType.isSubType(TypeManager.BOOLEAN_STAR())) {
                throw new QueryException(
                    "Predicate expression in filter step has invalid type:\n" +
                    "Expected type is integer, long, or boolean. Actual " +
                    "type is\n" + predType, pred.getLocation());
            }

            thePredExpr = ExprPromote.create(
                this, thePredExpr, TypeManager.ANY_ATOMIC_QSTN());
        }

        removeCtxVars();
    }

    @Override
    int getNumChildren() {
        return (thePredExpr != null ? 2 : 1);
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

    Expr getPredExpr() {
        return thePredExpr;
    }

    void setPredExpr(Expr newExpr, boolean destroy) {

        thePredExpr.removeParent(this, destroy);
        thePredExpr = newExpr;
        newExpr.addParent(this);

        checkConst();
        removeCtxVars();
    }

    ExprVar getCtxItemVar() {
        return theCtxItemVar;
    }

    ExprVar getCtxElemVar() {
        return theCtxElemVar;
    }

    ExprVar getCtxElemPosVar() {
        return theCtxElemPosVar;
    }

    ExprVar getCtxKeyVar() {
        return theCtxKeyVar;
    }

    Object getConstValue() {
        return theConstValue;
    }

    Long getNumericConst() {
        return (theConstValue instanceof Long ? (Long)theConstValue : null);
    }

    boolean isConst() {
        return thePredExpr == null;
    }

    /**
     * Check whether the pred expr is a const, and if so, store its const value
     * in theConstValue, and throw away the pred expr and the context vars.
     */
    void checkConst() {

        if (isConst()) {
            assert(theCtxItemVar == null);
            assert(theCtxElemVar == null);
            assert(theCtxElemPosVar == null);
            return;
        }

        if (thePredExpr.getKind() == ExprKind.CONST) {

            TypeCode c = thePredExpr.getType().getCode();
            FieldValueImpl value = ((ExprConst)thePredExpr).getValue();

            if (c == TypeCode.INT || c == TypeCode.LONG) {
                theConstValue = new Long(value.getLong());
            } else if (c == TypeCode.BOOLEAN) {
                theConstValue = value;
            } else {
                throw new QueryException(
                    "Predicate expression in filter step has invalid type.\n" +
                    "Expected type is integer, long, or boolean. Actual " +
                    "type is\n" + thePredExpr.getType(),
                    thePredExpr.getLocation());
            }

            thePredExpr.removeParent(this, true/*destroy*/);
            thePredExpr = null;
            theCtxItemVar = null;
            theCtxElemVar = null;
            theCtxElemPosVar = null;
            theCtxKeyVar = null;
        }
    }

    /**
     * Remove the context variables if they are not used anywhere.
     */
    void removeCtxVars() {

        if (theCtxItemVar != null && !theCtxItemVar.hasParents()) {
            theCtxItemVar = null;
        }

        if (theCtxElemVar != null && !theCtxElemVar.hasParents()) {
            theCtxElemVar = null;
        }

        if (theCtxElemPosVar != null) {

            if (!theCtxElemPosVar.hasParents()) {
                theCtxElemPosVar = null;
            } else if (theInput.getType().isMap()) {
                throw new QueryException(
                    "Context variable " + ExprVar.theElementPosVarName +
                    " cannot be used in filter " +
                    "step when the context item is a map",
                    theCtxElemPosVar.getLocation());
            }
        }

        if (theCtxKeyVar != null) {

            if (!theCtxKeyVar.hasParents()) {
                theCtxKeyVar = null;
            } else if (theInput.getType().isArray()) {
                throw new QueryException(
                    "Context variable " + ExprVar.theKeyVarName +
                    " cannot be used in filter " +
                    "step when the context item is an array",
                    theCtxKeyVar.getLocation());
            }
        }
    }

    /**
     * Replace this with a slice step, if possible.
     */
    Expr convertToSliceStep() {

        if (getNumericConst() != null) {

            if (theInput.getType().isMap()) {
                throw new QueryException(
                    "Cannot use a positional predicate in a " +
                    "filter step over a map context item.", getLocation());
            }

            theInput.removeParent(this, false/*deep*/);

            Expr sliceStep = new ExprSliceStep(
                theQCB, theSctx, getLocation(), theInput, getNumericConst());

            replace(sliceStep, true/*destroy*/);
            return sliceStep;
        }

        if (thePredExpr != null && thePredExpr.getType().isNumeric()) {

            if (theInput.getType().isMap()) {
                throw new QueryException(
                    "Cannot use a positional predicate in a " +
                    "filter step over a map context item.",
                    thePredExpr.getLocation());
            }

            // TODO convert to slice step if the predExpr will always return
            // numbers and it does not depend on the context vars. We don't
            // do this yet, because the predExpr would become a common sub-
            // expression, which may create problems.
            return this;
        }

        return this;
    }

    @Override
    public ExprType computeType() {

        ExprType inType = theInput.getType();
        Quantifier outQuant;

        if (!inType.isArray() &&
            !inType.isMap() &&
            !inType.isAny() &&
            !inType.isEmpty()) {
            throw new QueryException(
                "Wrong input type for slice step.\n" +
                "Expected array or map type. Actual type is: \n" + inType,
                getLocation());
        }

        if (inType.isEmpty()) {
            return TypeManager.EMPTY();
        }

        checkConst();

        if (isConst()) {
            if (theConstValue instanceof BooleanValueImpl) {
                boolean val = ((BooleanValueImpl)theConstValue).getBoolean();
                if (val == false) {
                    return TypeManager.EMPTY();
                }
            }
            // TODO convert this to a slice stap
        }

        outQuant = Quantifier.STAR;
        return TypeManager.createType(inType.getElementType(), outQuant);
    }

    @Override
    void display(StringBuilder sb, QueryFormatter formatter) {

        theInput.display(sb, formatter);

        if (isConst()) {
            sb.append("[");
            sb.append(theConstValue);
            sb.append("]");
        } else {
            sb.append(".\n");
            formatter.indent(sb);
            sb.append("[\n");
            formatter.incIndent();
            thePredExpr.display(sb, formatter);
            formatter.decIndent();
            sb.append("\n");
            formatter.indent(sb);
            sb.append("]");
        }
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {
    }
}
