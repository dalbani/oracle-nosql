
/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2015 Oracle and/or its affiliates.  All rights reserved.
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

import oracle.kv.impl.api.table.TablePath;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.IndexImpl;

import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.compiler.Expr.ExprKind;
import oracle.kv.impl.query.compiler.Expr.ExprIter;
import oracle.kv.impl.query.compiler.ExprVar.VarKind;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;

/**
 * Various utility methods used during optimizations
 */
class ExprUtils {

    /**
     * return true if two expressions are identical; otherwise return false
     */
    static boolean matchExprs(Expr expr1, Expr expr2) {

        if (expr1.getKind() != expr2.getKind()) {
            return false;
        }

        if (expr1.getNumChildren() != expr2.getNumChildren()) {
            return false;
        }

        switch (expr1.getKind()) {
        case CONST: {
            ExprConst e1 = (ExprConst)expr1;
            ExprConst e2 = (ExprConst)expr2;
            return e1.getValue().equals(e2.getValue());
        }
        case BASE_TABLE: {
            ExprBaseTable e1 = (ExprBaseTable)expr1;
            ExprBaseTable e2 = (ExprBaseTable)expr2;

            /*
             * For now, there can be only one ExprBaseTable in the query, so
             * just return true. Otherwise, uncomment and finish up the code
             * below (TODO).
             */
            assert(e1 == e2);
            return true;
            /*
            if (e1.getTable() != e2.getTable()) {
                return false;
            }
            if (e1.getPrimaryKey() != null) {
                if (e2.getPrimaryKey() == null) {
                    return false;
                }
                if (!e1.getPrimaryKey().equals(e2.getPrimaryKey())) {
                    return false;
                }
            } else if (e2.getPrimaryKey() != null) {
                return false;
            }
            if (e2.getSecondaryKey() != null) {
                if (e2.getSecondaryKey() == null) {
                    return false;
                }
                if (!e1.getSecondaryKey().equals(e2.getSecondaryKey())) {
                    return false;
                }
            } else if (e2.getSecondaryKey() != null) {
                return false;
            }

            compare range the filtering preds as well.....

            break;
            */
        }
        case VAR: {
            ExprVar e1 = (ExprVar)expr1;
            ExprVar e2 = (ExprVar)expr2;

            if (e1.getVarKind() != e2.getVarKind()) {
                return false;
            }

            if (e1.getVarKind() == VarKind.EXTERNAL) {
                return e1.getId() == e2.getId();
            }

            if (e1.isContext()) {
                return matchExprs(e1.getCtxExpr(), e2.getCtxExpr());
            }

            return matchExprs(e1.getDomainExpr(), e2.getDomainExpr());
        }
        case FUNC_CALL: {
            ExprFuncCall e1 = (ExprFuncCall)expr1;
            ExprFuncCall e2 = (ExprFuncCall)expr2;

            if (e1.getFunction() != e2.getFunction()) {
                return false;
            }

            return matchChildren(e1, e2);
        }
        case PROMOTE: {
            ExprPromote e1 = (ExprPromote)expr1;
            ExprPromote e2 = (ExprPromote)expr2;

            return (e1.getTargetType().equals(e2.getTargetType()) &&
                    matchExprs(e1.getInput(), e2.getInput()));
        }
        case FIELD_STEP: {
            ExprFieldStep e1 = (ExprFieldStep)expr1;
            ExprFieldStep e2 = (ExprFieldStep)expr2;

            if (e1.isConst() != e2.isConst()) {
                return false;
            }

            if (e1.isConst()) {
                return e1.getFieldName().equals(e2.getFieldName());
            }

            return matchChildren(e1, e2);
        }
        case FILTER_STEP: {
            ExprFilterStep e1 = (ExprFilterStep)expr1;
            ExprFilterStep e2 = (ExprFilterStep)expr2;

            if (e1.isConst() != e2.isConst()) {
                return false;
            }

            if (e1.isConst()) {
                return e1.getConstValue().equals(e2.getConstValue());
            }

            return matchChildren(e1, e2);
        }
        case SLICE_STEP: {
            ExprSliceStep e1 = (ExprSliceStep)expr1;
            ExprSliceStep e2 = (ExprSliceStep)expr2;

            if (e1.getLowValue() != null) {
                if (!e1.getLowValue().equals(e2.getLowValue())) {
                    return false;
                }
            } else if (e2.getLowValue() != null) {
                return false;
            }

            if (e1.getHighValue() != null) {
                if (!e1.getHighValue().equals(e2.getHighValue())) {
                    return false;
                }
            } else if (e2.getHighValue() != null) {
                return false;
            }

            return matchChildren(e1, e2);
        }
        case ARRAY_CONSTR: {
            return matchChildren(expr1, expr2);
        }
        case SFW: {
            return matchChildren(expr1, expr2);
        }
        case RECEIVE: {
            throw new QueryStateException(
                "Unexprected expression kind : " + expr1.getKind());
        }
        }

        assert(false); // we should no be here
        return false;
    }

    private static boolean matchChildren(Expr expr1, Expr expr2) {

        ExprIter children1 = expr1.getChildren();
        ExprIter children2 = expr2.getChildren();

        while (children1.hasNext()) {
            assert(children2.hasNext());
            Expr child1 = children1.next();
            Expr child2 = children2.next();

            if (!matchExprs(child1, child2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method checks whether the given expr is a reference to a
     * given column of a given index. It returns one of the following
     * 3 values:
     *
     * 0 : The expr is not a reference to the index column
     * 1 : The expr is an exact reference to the index column
     * 2 : The expr is a partial reference to the index column
     */
    static int isIndexColumnRef(
        TableImpl table,
        IndexImpl index,
        TablePath ipath,
        Expr expr,
        TablePath epath) {

        epath.clear();
        boolean foundMultiKeyStep = false;
        boolean foundElementFilter = false;

        while (true) {

            switch (expr.getKind()) {

            case FUNC_CALL:

                Function keysFunc = expr.getFunction(FuncCode.FN_KEYS);

                if (keysFunc == null || index == null || !index.isMultiKey()) {
                    return 0;
                }

                epath.add(TableImpl.KEY_TAG);
                foundMultiKeyStep = true;

                expr = ((ExprFuncCall)expr).getArg(0);
                break;

            case FIELD_STEP:
                ExprFieldStep stepExpr = (ExprFieldStep)expr;
                String fieldName = stepExpr.getFieldName();

                if (fieldName == null) {
                    return 0;
                }

                epath.add(fieldName);

                if (stepExpr.getInput().getType().isArray()) {
                    epath.add(TableImpl.ANONYMOUS);
                }

                expr = expr.getInput();
                break;

            case SLICE_STEP:
            case FILTER_STEP:
                if (index == null || !index.isMultiKey() || foundMultiKeyStep) {
                    return 0;
                }

                foundMultiKeyStep = true;
                epath.add(TableImpl.ANONYMOUS);

                if (expr.getKind() == ExprKind.SLICE_STEP) {
                    ExprSliceStep step = (ExprSliceStep)expr;
                    foundElementFilter = step.hasBounds();
                } else {
                    ExprFilterStep step = (ExprFilterStep)expr;
                    foundElementFilter = (step.getPredExpr() != null);
                }

                expr = expr.getInput();
                break;

            case VAR:
                ExprVar var = (ExprVar)expr;
                TableImpl table2 = var.getTable();

                if (table2 == null || table2 != table) {
                    return 0;
                }

                epath.reverseSteps();

                if (ipath.equals(epath)) {
                    return (foundElementFilter ? 2 : 1);
                }

                return 0;

            default:
                return 0;
            }
        }
    }
}
