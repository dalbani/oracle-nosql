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

import oracle.kv.impl.query.compiler.Expr.ExprKind;

/**
 * Distributer is responsible for creating ExprReceive exprs in the exprs graph
 * and placing them at their appropriate position within the graph.
 *
 * It implements an algorithm which traverses the exprs graph looking for 
 * ExprBaseTable nodes. For each ExprBaseTable it finds, it creates an
 * ExprReceive and places it right above the associated ExprBaseTable. It
 * then tries to pull-up the ExprReceive as far as it can go.
 *
 * For now, a query can have at most one ExprBaseTable, which if present, is a
 * child of the single ExprSFW in the query. As a result, the work done by the
 * Distributer is very simple.
 */
class Distributer extends ExprVisitor {

    QueryControlBlock theQCB;

    private final ExprWalker theWalker;

    Distributer(QueryControlBlock qcb) {
        theQCB = qcb;
        theWalker = new ExprWalker(this, false/*allocateChildrenIter*/);
    }

    void distributeQuery() {
        theWalker.walk(theQCB.getRootExpr());
    }

    @Override
    void exit(ExprBaseTable e) {
        ExprReceive recv = new ExprReceive(theQCB, theQCB.getInitSctx());
        e.replace(recv, false);
        recv.setInput(e, false/*destroy*/);
    }

    @Override
    boolean enter(ExprSFW e) {

        theWalker.walk(e.getFromExpr());

        if (e.getFromExpr().getKind() != ExprKind.RECEIVE) {
            return false;
        }

        /*
         * Pull the receive expr above the SFW expr
         */
        ExprReceive rcv = (ExprReceive)e.getFromExpr();

        e.setFromExpr(rcv.getInput(), false/*destroy*/);
        e.replace(rcv, false);
        rcv.setInput(e, false/*destroy*/);

        if (e.getNumSortExprs() == 0) {
            return false;
        }

        /*
         * If the SFW expr has sort do the following:
         * - add the sort expr in the SELECT clause, if not there already.
         * - add to the receive expr the positions of the sort exprs within
         *   the SELECT clause; also add the sort specs
         * - create a new SFW expr on top of the RCV expr, using the RCV expr
         *   as the FROM expr. The new SFW selects a subset of the fields of
         *   the records produces by the RCV; specifically, the fields that
         *   correspond to the fields of the original SFW (before the addition
         *   of the sort exprs).
         */
        int numFields = e.getNumFields();

        int[] sortExprPositions = e.addSortExprsToSelect();

        rcv.addSort(sortExprPositions, e.getSortSpecs());

        ExprSFW sfw = new ExprSFW(theQCB, e.getSctx(), e.getLocation());

        rcv.replace(sfw, false);

        sfw.addFromClause(rcv, theQCB.createInternalVarName("from"));

        ArrayList<Expr> fieldExprs = new ArrayList<Expr>(numFields);
        ArrayList<String> fieldNames = new ArrayList<String>(numFields);

        for (int i = 0; i < numFields; ++i) {
            Expr fieldExpr = new ExprFieldStep(theQCB,
                                               e.getSctx(),
                                               e.getFieldExpr(i).getLocation(),
                                               sfw.getFromVar(),
                                               e.getFieldName(i));
            fieldExprs.add(fieldExpr);
            fieldNames.add(e.getFieldName(i));
        }

        sfw.addSelectClause(fieldNames, fieldExprs);

        return false;
    }
}
