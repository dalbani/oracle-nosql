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
import java.util.List;

import oracle.kv.Direction;
import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.api.table.IndexKeyImpl;
import oracle.kv.impl.api.table.PrimaryKeyImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryException.Location;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.TypeManager;
import oracle.kv.table.FieldRange;


/**
 * ExprBaseTable is an internal expression representing a KVS table appearing
 * in a FROM clause of a query. Evaluation of this expression returns the rows
 * of the associated table. The rows are returned as tuples, i.e., if a the
 * rows of the table consist of N fields, the expression returns N values per
 * row.
 */
class ExprBaseTable extends Expr {

    static class IndexHint {

        IndexImpl theIndex; // null means the primary index
        boolean theForce;

        IndexHint(IndexImpl index, boolean force) {
            theIndex = index;
            theForce = force;
        }
    }

    final private TableImpl theTable;

    private Direction theDirection = Direction.FORWARD;

    private PrimaryKeyImpl thePrimaryKey;

    private IndexKeyImpl theSecondaryKey;

    private FieldRange theRange;

    private Expr theFilteringPred;

    private boolean  theUsesCoveringIndex;

    private ArrayList<Expr> thePushedExternals;

    private List<IndexHint> theIndexHints = null;

    private IndexHint theForceIndexHint = null;

    ExprBaseTable(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        TableImpl table) {

        super(qcb, sctx, ExprKind.BASE_TABLE, location);

        theTable = table;

        ExprType exprType = TypeManager.createTableRecordType(
            table, Quantifier.STAR);

        setType(exprType);
    }

    TableImpl getTable() {
        return theTable;
    }

    @Override
    int getNumChildren() {
        return (theFilteringPred != null ? 1 : 0);
    }

    Direction getDirection() {
        return theDirection;
    }

    void setDirection(Direction dir) {
        theDirection = dir;
    }

     PrimaryKeyImpl getPrimaryKey() {
        return thePrimaryKey;
    }

    void addPrimaryKey(PrimaryKeyImpl key, boolean isCoveringIndex) {
        thePrimaryKey = key;
        theUsesCoveringIndex = isCoveringIndex;
    }

     IndexKeyImpl getSecondaryKey() {
        return theSecondaryKey;
    }

    void addSecondaryKey(IndexKeyImpl key, boolean isCoveringIndex) {
        theSecondaryKey = key;
        theUsesCoveringIndex = isCoveringIndex;
    }

    FieldRange getRange() {
        return theRange;
    }

    void addRange(FieldRange range) {
        theRange = range;
    }

    Expr getFilteringPred() {
        return theFilteringPred;
    }

    void setFilteringPred(Expr pred, boolean destroy) {
        if (theFilteringPred != null) {
            theFilteringPred.removeParent(this, destroy);
        }
        pred = ExprPromote.create(null, pred, TypeManager.BOOLEAN_QSTN());
        theFilteringPred = pred;
        theFilteringPred.addParent(this);
    }

    void removeFilteringPred(boolean destroy) {
        theFilteringPred.removeParent(this, destroy);
        theFilteringPred = null;

    }

    boolean getUsesCoveringIndex() {
        return theUsesCoveringIndex;
    }

    void setPushedExternals(ArrayList<Expr> v) {
        assert(thePushedExternals == null);
        thePushedExternals = v;
    }

    ArrayList<Expr> getPushedExternals() {
        return thePushedExternals;
    }

    /**
     * If index is null, we are checking whether the ptimary index
     * has been specified in a hint/
     */
    boolean isIndexHint(IndexImpl index) {

        if (theIndexHints == null) {
            return false;
        }

        for (IndexHint hint : theIndexHints) {
            if (hint.theIndex == index) {
                return true;
            }
        }

        return false;
    }

    IndexHint getForceIndexHint() {
        return theForceIndexHint;
    }

    /**
     * If index is null, it means the hint is about the primary index
     */
    void addIndexHint(IndexImpl index, boolean force, Location loc) {

        if (theIndexHints == null) {
            theIndexHints = new ArrayList<IndexHint>();
        }

        IndexHint hint = new IndexHint(index, force);

        if ( !containsHint(theIndexHints, hint) ) {
            theIndexHints.add(hint);
        }

        if (force) {
            if (theForceIndexHint != null) {
                throw new QueryException(
                    "Cannot have more than one FORCE_INDEX hints", loc);
            }

            theForceIndexHint = hint;
        }
    }

    private static boolean containsHint(List<IndexHint> indexHints,
        IndexHint hint) {
        for ( IndexHint h : indexHints) {
            if ( h.theIndex.getName().equals(hint.theIndex.getName()) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    ExprType computeType() {
        return getTypeInternal();
    }

    @Override
    void display(StringBuilder sb, QueryFormatter formatter) {

        formatter.indent(sb);
        sb.append("TABLE");
        displayContent(sb, formatter);
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {

        if (thePrimaryKey != null || theRange != null) {
            sb.append("\n");
            formatter.indent(sb);
            sb.append("[\n");

            formatter.incIndent();
            formatter.indent(sb);
            sb.append(theTable.getName());

            if (thePrimaryKey != null) {
                sb.append("\n");
                formatter.indent(sb);
                sb.append("KEY: ");
                sb.append(thePrimaryKey);
            }

            if (theRange != null) {
                sb.append("\n");
                formatter.indent(sb);
                sb.append("RANGE: ");
                sb.append(theRange);
            }

            if (thePushedExternals != null) {
                sb.append("\n");
                formatter.indent(sb);
                sb.append("PUSHED EXTERNAL EXPRS: ");
                for (Expr expr : thePushedExternals) {
                    sb.append("\n");
                    if (expr == null) {
                        formatter.indent(sb);
                        sb.append("null");
                    } else {
                        expr.display(sb, formatter);
                    }
                }
            }

            formatter.decIndent();
            sb.append("\n");
            formatter.indent(sb);
            sb.append("]");

        } else {
            sb.append("[");
            sb.append(theTable.getName());
            sb.append("]");
        }
    }
}
