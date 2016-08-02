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
 * Represents a "dot" step in a path expr. In general, a field step will select
 * the value of a field from one or more input records.
 *
 * Syntactically, an field step looks like this:
 *
 * input_expr.name_expr
 *
 * A field step has 2 operands: an input expr that is supposed to return a
 * sequence of zero or more complex values, and a "name" expr, that is
 * supposed to return at most one string. We refer to the string returned
 * by the name expr as the "key name".
 *
 * For each value in the input sequence, the step computes zero or more result
 * values. The overall result of the step is the concatenation of the results
 * produced for each input value, in the order of their computation.
 *
 * Let V be the current value that the field step operates upon.
 *
 * 1. If V is not a complex value, an error is raised.
 *
 * 2. If V is a record, the name expr is computed. The name expr may reference
 *    V via the $$ variable. Note that if the name expr does not reference $$,
 *    it does not need to be computed for each V; it can be computed only once,
 *    before any of the input values are processed.
 *
 *    If the name expr returns an empty result (no key name), V is skipped. If
 *    the name expr returns more than 1 result, or a non-string result, an error
 *    is raised. Otherwise, let K be the key name computed by the name expr.
 *    Then, if V contains a field whose name is equal to K, the value of that
 *    field is selected. Otherwise, an error is raised.
 *
 * 3. If V is a map. the name expr is computed. As in case 2, if there is no
 *    key name of the name expr returns more than 1 result, or a non-string
 *    result, an error is raised. Otherwise, let K be the key name computed
 *    by the name expr. Contrary to case 2, no error is raised if V does not
 *    contain an entry with key equal to K; instead V is skipped. Otherwise,
 *    the value of the entry whose key is equal to K is selected.
 *
 * 4  If V is an array, the field step is applied recursively to each element
 *    of the array.
 *
 * Should we allow theFieldNameExpr to return multiple strings ????
 * Maybe have another kind of step (using the { expr } syntax, where expr
 * produces any number of strings and the step selects matching pairs and
 * constructs a new record out of the selected pairs.
 *
 * Should we define $$pos and allow its use in the name expr ???? It would be
 * defined as the number of records or maps processed so far.
 */
class ExprFieldStep extends Expr {

    private Expr theInput;

    private Expr theFieldNameExpr;

    private String theFieldName;

    private ExprVar theCtxItemVar;

    ExprFieldStep(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        Expr input) {

        super(qcb, sctx, ExprKind.FIELD_STEP, location);
        theInput = input;

        theInput.addParent(this);

        ExprType inType = input.getType();
        if (!inType.isComplex() && !inType.isAny()) {
            throw new QueryException(
                "Wrong input type for path step " +
                "(must be a record, array, or map type): " + inType, location);
        }
    }

    ExprFieldStep(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        Expr input,
        String fieldName) {

        this(qcb, sctx, location, input);
        theFieldName = fieldName;
    }

    void addCtxVars(ExprVar ctxItemVar) {
        theCtxItemVar = ctxItemVar;
    }

    void addFieldNameExpr(String fieldName, Expr fieldNameExpr) {

        theFieldName = fieldName;
        theFieldNameExpr = fieldNameExpr;
        if (theFieldNameExpr != null) {
            theFieldNameExpr.addParent(this);
        }

        checkConst();

        if (!isConst()) {
            theFieldNameExpr = ExprPromote.create(
                this, theFieldNameExpr, TypeManager.STRING_QSTN());
        }
    }

    @Override
    int getNumChildren() {
        return (theFieldNameExpr != null ? 2 : 1);
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

    Expr getFieldNameExpr() {
        return theFieldNameExpr;
    }

    void setFieldNameExpr(Expr newExpr, boolean destroy) {
        theFieldNameExpr.removeParent(this, destroy);
        theFieldNameExpr = newExpr;
        newExpr.addParent(this);
    }

    String getFieldName() {
        return theFieldName;
    }

    ExprVar getCtxItemVar() {
        return theCtxItemVar;
    }

    public boolean isConst() {
        return theFieldName != null;
    }

    public void checkConst() {

        if (isConst()) {
            assert(theFieldNameExpr == null);
            assert(theCtxItemVar == null);
            return;
        }

        if (theFieldNameExpr.getKind() == ExprKind.CONST &&
            theFieldNameExpr.getType().getCode() == TypeCode.STRING) {

            FieldValueImpl value = ((ExprConst)theFieldNameExpr).getValue();
            theFieldName = value.getString();
            theFieldNameExpr.removeParent(this, true/*destroy*/);
            theFieldNameExpr = null;
            theCtxItemVar = null;
        }

        if (theCtxItemVar != null && !theCtxItemVar.hasParents()) {
            theCtxItemVar = null;
        }
    }

    @Override
    public ExprType computeType() {
        return computeType(theInput.getType());
    }

    private ExprType computeType(ExprType inType) {

        Quantifier inQuant = inType.getQuantifier();
        Quantifier outQuant;
        ExprType outType;
        ExprType anyOne = TypeManager.ANY_ONE();

        if (!inType.isComplex() && !inType.isAny()) {
            throw new QueryException(
                "Wrong input type for path step " +
                "(must be a record, array, or map type): " + inType,
                getLocation());
        }

        checkConst();

        if (inType.isRecord()) {

            if (isConst()) {
                outType = inType.getFieldType(theFieldName, inQuant);
                if (outType == null) {
                    throw new QueryException(
                        "There is no field named " + theFieldName +
                        " in type " + inType, getLocation());
                }
            } else {
                outType = TypeManager.createType(anyOne, inQuant);
            }

        } else if (inType.isMap()) {
            outQuant = TypeManager.getUnionQuant(inQuant, Quantifier.QSTN);
            outType = inType.getElementType(outQuant);

        } else if (inType.isArray()) {
            inType = inType.getElementType(Quantifier.STAR);
            outType = computeType(inType);

        } else {
            outType = TypeManager.ANY_STAR();
        }

        return outType;
    }

    @Override
    void display(StringBuilder sb, QueryFormatter formatter) {

        if (isConst()) {
            theInput.display(sb, formatter);
            sb.append(".").append(theFieldName);
        } else {
            super.display(sb, formatter);
        }
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {
        theInput.display(sb, formatter);
        sb.append(".\n");
        theFieldNameExpr.display(sb, formatter);
    }
}
