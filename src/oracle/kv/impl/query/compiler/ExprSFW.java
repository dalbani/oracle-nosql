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
import java.util.Map;

import oracle.kv.Direction;
import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.api.table.IndexImpl.IndexField;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TablePath;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.impl.query.types.TypeManager;
import oracle.kv.table.Index;

/**
 * Represents a SELECT-FROM-WHERE query block.
 *
 * theFromClause:
 * There is a FromClause for each table expression appearing in the FROM clause
 * of the actual query block. For now, there can be only one table expr, which
 * is actually the name of a KVS table (see ExprBaseTable). But in general,
 * a table expr may be a table function or a nested subquery.
 *
 * theWhereExpr:
 *
 * theFieldNames:
 *
 * theFieldExprs:
 *
 * theIsSelectStar:
 * True if  the SELECT clause is a "select *". False otherwise.
 */
class ExprSFW extends Expr {

    class FromClause
    {
        private final ExprVar theVar;

        private Expr theDomainExpr;

        FromClause(Expr domainExpr, String varName) {
            theDomainExpr = domainExpr;
            theDomainExpr.addParent(ExprSFW.this);
            theVar = new ExprVar(theQCB, theSctx, domainExpr.getLocation(),
                                 varName, this);
        }

        Expr getDomainExpr() {
            return theDomainExpr;
        }

        ExprVar getVar() {
            return theVar;
        }
    }

    private FromClause theFromClause;

    private Expr theWhereExpr;

    private ArrayList<String> theFieldNames;

    private ArrayList<Expr> theFieldExprs;

    private boolean theIsSelectStar = false;

    private ArrayList<Expr> theSortExprs;

    private ArrayList<SortSpec> theSortSpecs;

    private boolean theUsePrimaryIndexForSort = false;

    private IndexImpl theSortingIndex = null;

    ExprSFW(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location) {

        super(qcb, sctx, ExprKind.SFW, location);
    }

    void addFromClause(Expr domainExpr, String varName) {
        theFromClause = this.new FromClause(domainExpr, varName);
    }

    int getNumFromClauses() {
        return 1;
    }

    FromClause getFromClause() {
        return theFromClause;
    }

    Expr getFromExpr() {
        return theFromClause.getDomainExpr();
    }

    void setFromExpr(Expr newExpr, boolean destroy) {
        theFromClause.theDomainExpr.removeParent(this, destroy);
        theFromClause.theDomainExpr = newExpr;
        newExpr.addParent(this);
    }

    ExprVar getFromVar() {
        return theFromClause.getVar();
    }

    TableImpl getTable() {

        if (getFromExpr().getKind() == ExprKind.BASE_TABLE) {
            ExprBaseTable tableExpr = (ExprBaseTable)getFromExpr();
            return tableExpr.getTable();
        }

        return null;
    }

    void addWhereClause(Expr condExpr) {

        assert(theWhereExpr == null);

        theWhereExpr = ExprPromote.create(
            null, condExpr, TypeManager.BOOLEAN_QSTN());

        theWhereExpr.addParent(ExprSFW.this);
    }

    Expr getWhereExpr() {
        return theWhereExpr;
    }

    void setWhereExpr(Expr newExpr, boolean destroy) {
        theWhereExpr.removeParent(this, destroy);
        theWhereExpr = null;
        addWhereClause(newExpr);
    }

    void removeWhereExpr(boolean destroy) {
        theWhereExpr.removeParent(this, destroy);
        theWhereExpr = null;
    }

    void setIsSelectStar() {
        theIsSelectStar = true;
    }

    boolean getIsSelectStar() {
        return theIsSelectStar;
    }

    void addSelectClause(
        ArrayList<String> fieldNames,
        ArrayList<Expr> fieldExprs) {

        assert(fieldNames.size() == fieldExprs.size());
        theFieldNames = fieldNames;
        theFieldExprs = fieldExprs;

        for (int i = 0; i < fieldExprs.size(); ++i) {
            Expr expr = ExprPromote.create(null, fieldExprs.get(i),
                                           TypeManager.ANY_QSTN());
            expr.addParent(this);
            theFieldExprs.set(i, expr);
        }
    }

    Expr getFieldExpr(int i) {
        return theFieldExprs.get(i);
    }

    void setFieldExpr(int i, Expr newExpr, boolean destroy) {
        theFieldExprs.get(i).removeParent(this, destroy);
        theFieldExprs.set(i, newExpr);
        newExpr.addParent(this);
    }

    void removeField(int i, boolean destroy) {
        theFieldExprs.get(i).removeParent(this, destroy);
        theFieldExprs.remove(i);
        theFieldNames.remove(i);
        theType = computeType();
    }

    void addField(String name, Expr expr) {
        expr = ExprPromote.create(null, expr, TypeManager.ANY_QSTN());
        theFieldExprs.add(expr);
        theFieldNames.add(name);
        expr.addParent(this);
        theType = computeType();
    }

    String getFieldName(int i) {
        return theFieldNames.get(i);
    }

    int getNumFields() {
        return theFieldExprs.size();
    }

    ArrayList<String> getFieldNames() {
        return theFieldNames;
    }

    String[] getFieldNamesArray() {
        String[] arr = new String[theFieldNames.size()];
        return theFieldNames.toArray(arr);
    }

    void addSortClause(
        ArrayList<Expr> sortExprs,
        ArrayList<SortSpec> sortSpecs) {

        theSortExprs = sortExprs;
        theSortSpecs = sortSpecs;

        for (Expr expr : sortExprs) {
            // TODO: allow arrays as well
            expr = ExprPromote.create(null, expr, TypeManager.ANY_ATOMIC_QSTN());
            expr.addParent(this);
        }
    }

    void removeSort() {

        if (!hasSort()) {
            return;
        }

        while (!theSortExprs.isEmpty()) {
            removeSortExpr(0, true);
        }

        theSortExprs = null;
        theSortSpecs = null;
        theSortingIndex = null;
        theUsePrimaryIndexForSort = false;
    }

    boolean hasSort() {
        return (theSortExprs != null && !theSortExprs.isEmpty());
    }

    boolean hasPrimaryIndexBasedSort() {
        return theUsePrimaryIndexForSort;
    }

    boolean hasSecondaryIndexBasedSort() {
        return theSortingIndex != null;
    }

    IndexImpl getSortingIndex() {
        return theSortingIndex;
    }

    int getNumSortExprs() {
        return (theSortExprs == null ? 0 : theSortExprs.size());
    }

    Expr getSortExpr(int i) {
        return theSortExprs.get(i);
    }

    void setSortExpr(int i, Expr newExpr, boolean destroy) {
        theSortExprs.get(i).removeParent(this, destroy);
        theSortExprs.set(i, newExpr);
        newExpr.addParent(this);
    }

    void removeSortExpr(int i, boolean destroy) {
        Expr sortExpr = theSortExprs.remove(i);
        sortExpr.removeParent(this, destroy);
        theSortSpecs.remove(i);
    }

    SortSpec[] getSortSpecs() {
        SortSpec[] arr = new SortSpec[theSortSpecs.size()];
        return theSortSpecs.toArray(arr);
    }

    /*
     * Method to find the index to use for the sort and determine the
     * direction, or throw error if no applicable index.
     */
    void analyseSort() {

        if (theSortExprs == null || theSortExprs.isEmpty()) {
            return;
        }

        TableImpl table = getTable();

        if (table == null) {
            throw new QueryException(
                "Order-by cannot be performed because the order-by " +
                "expressions are not consecutive columns of any index",
                getSortExpr(0).getLocation());
        }

        ExprBaseTable tableExpr = (ExprBaseTable)getFromExpr();

        TablePath epath = new TablePath(table, null/*path*/);
        IndexField ipath = null;
        int i = 0;

        /*
         * Determine the sort direction and store it in the BaseTableExpr
         */
        SortSpec spec = theSortSpecs.get(0);
        boolean desc = spec.theIsDesc;
        Direction direction = (desc ? Direction.REVERSE : Direction.FORWARD);

        for (i = 1; i < theSortSpecs.size(); ++i) {
            spec = theSortSpecs.get(i);
            if (desc != spec.theIsDesc) {
                throw new QueryException(
                    "In the current implementation, all order-by specs " +
                    "must have the same ordering direction",
                    getSortExpr(i).getLocation());
            }
        }

        tableExpr.setDirection(direction);

        /*
         * Check whether the sort exprs are a prefix of the primary key columns.
         */
        List<String> pkColumnNames = table.getPrimaryKeyInternal();

        for (i = 0;
             i < pkColumnNames.size() && i < theSortExprs.size();
             ++i) {

            String name = pkColumnNames.get(i);
            ipath = new IndexField(table, name);
            Expr sortExpr = getSortExpr(i);

            if (ExprUtils.isIndexColumnRef(table,
                                           null/*index*/,
                                           ipath,
                                           sortExpr,
                                           epath) != 1) {
                break;
            }
        }

        if (i == theSortExprs.size()) {
            theUsePrimaryIndexForSort = true;

            int numShardKeys = table.getShardKeySize();

            if (i > numShardKeys) {
                while (theSortExprs.size() > numShardKeys) {
                    removeSortExpr(theSortExprs.size() - 1, true/*destroy*/);
                }
            }

            return;
        }

        /*
         * Check whether the sort exprs are a prefix of the columns of some
         * secondary index.
         */
        Map<String, Index> indexes = table.getIndexes();

        for (Map.Entry<String, Index> entry : indexes.entrySet()) {

            IndexImpl index = (IndexImpl)entry.getValue();
            List<IndexField> indexPaths = index.getIndexFields();

            for (i = 0;
                 i < indexPaths.size() && i < theSortExprs.size();
                 ++i) {

                ipath = indexPaths.get(i);
                Expr sortExpr = getSortExpr(i);

                if (ipath.isMultiKey()) {
                    break;
                }

                if (ExprUtils.isIndexColumnRef(table,
                                               index,
                                               ipath,
                                               sortExpr,
                                               epath) != 1) {
                    break;
                }
            }

            if (i == theSortExprs.size()) {
                theSortingIndex = index;
                return;
            }
        }

        throw new QueryException(
            "Order-by cannot be performed because the order-by " +
            "expressions are not consecutive columns of any index",
            getSortExpr(0).getLocation());
    }

    /*
     * Method to add the sort expr in the SELECT clause, if not there already.
     * The method is called from the Distributer, when a receive expr is pulled
     * above a SFW expr that has sort. The method returns the positions of the
     * sort exprs in the SELECT list. The positions are stored in the receive
     * expr.
     */
    int[] addSortExprsToSelect() {

        int numFieldExprs = theFieldExprs.size();
        int numSortExprs = theSortExprs.size();

        int[] sortPositions = new int[numSortExprs];

        for (int i = 0; i < numSortExprs; ++i) {

            Expr sortExpr = theSortExprs.get(i);

            int j;
            for (j = 0; j < numFieldExprs; ++j) {
                Expr fieldExpr = theFieldExprs.get(j);
                if (ExprUtils.matchExprs(sortExpr, fieldExpr)) {
                    break;
                }
            }

            if (j == numFieldExprs) {
                theFieldExprs.add(sortExpr);
                theFieldNames.add(theQCB.generateFieldName("sort"));
                sortPositions[i] = theFieldExprs.size() - 1;
                theIsSelectStar = false;
            } else {
                sortPositions[i] = j;
                sortExpr.removeParent(this, true/*destroy*/);
            }
        }

        theSortExprs = null;
        theType = computeType();

        return sortPositions;
    }

    @Override
    int getNumChildren() {
        return
            (theWhereExpr != null ? 1 : 0) + 1 +
            theFieldExprs.size() +
            (theSortExprs != null ? theSortExprs.size() : 0);
    }

    /**
     * For now, SFW always returns a record type even if only one column is
     * selected. This is because we don't have scalar subqueries yet, and
     * without them, the result of SFW is always a tuple with column names,
     * according to the SQL semantics.
     */
    @Override
    ExprType computeType() {

        Quantifier q = getFromExpr().getType().getQuantifier();

        return TypeManager.createRecordType(
            theFieldNames, theFieldExprs, q);
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {

        formatter.indent(sb);
        sb.append("FROM:\n");
        theFromClause.getDomainExpr().display(sb, formatter);
        sb.append(" as " + theFromClause.getVar().getName() + "\n\n");

        if (theWhereExpr != null) {
            formatter.indent(sb);
            sb.append("WHERE:\n");
            theWhereExpr.display(sb, formatter);
            sb.append("\n\n");
        }

        formatter.indent(sb);
        sb.append("SELECT:\n");

        for (int i = 0; i < theFieldExprs.size(); ++i) {
            formatter.indent(sb);
            sb.append(theFieldNames.get(i)).append(": \n");
            theFieldExprs.get(i).display(sb, formatter);
            if (i < theFieldExprs.size() - 1) {
                sb.append(",\n");
            }
        }
    }
}
