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

import oracle.kv.impl.api.table.PrimaryKeyImpl;
import oracle.kv.impl.api.table.IndexKeyImpl;

import oracle.kv.impl.query.runtime.ReceiveIter.DistributionKind;
import oracle.kv.impl.query.types.ExprType;


/**
 * This expr is used to mark the boundaries between parts of the query that
 * execute on different "machines". The receive expr itself executes at a
 * "client machine" and its child subplan executes at a "server machine".
 * The child subplan may actually be replicated on several server machines,
 * in which case the receive expr acts as a UNION ALL expr, collecting and
 * propagating the results it receives from its children. Furthermore, the
 * receive expr may perform a merge-sort over its inputs (if the inputs
 * return sorted results).
 *
 * Receive exprs are always created as parents of the BaseTable exprs in the
 * exprs graph, After their creation, Receive exprs are pulled-up as far as
 * they can go. All this is done by the Distributer class.
 *
 * theInput:
 * The expr producing the input to this receive expr. 
 */
class ExprReceive extends Expr {

    private Expr theInput;

    private int[] theSortFieldPositions;

    private SortSpec[] theSortSpecs;

    private DistributionKind theDistributionKind;

    private PrimaryKeyImpl thePrimaryKey;

    private ArrayList<Expr> thePushedExternals;

    ExprReceive(QueryControlBlock qcb, StaticContext sctx) {
        super(qcb, sctx, ExprKind.RECEIVE, null);
    }

    @Override
    int getNumChildren() {
        return 1;
    }

    @Override
    Expr getInput() {
        return theInput;
    }

    void setInput(Expr newExpr, boolean destroy) {
        if (theInput != null) {
            theInput.removeParent(this, destroy);
        }
        theInput = newExpr;
        theInput.addParent(this);
        theType = computeType();
        computeDistributionKind();
        setLocation(newExpr.getLocation());
    }

    void computeDistributionKind() {

        if (theInput.getKind() != ExprKind.BASE_TABLE) {
            return;
        }

        ExprBaseTable tableExpr = (ExprBaseTable)theInput;

        thePrimaryKey = tableExpr.getPrimaryKey();
        IndexKeyImpl secKey = tableExpr.getSecondaryKey();

        if (secKey != null) {
           theDistributionKind = DistributionKind.ALL_SHARDS;

        } else if (thePrimaryKey != null && thePrimaryKey.hasShardKey()) {
            theDistributionKind = DistributionKind.SINGLE_PARTITION;

            if (tableExpr.getPushedExternals() != null) {
                thePushedExternals =
                    new ArrayList<Expr>(tableExpr.getPushedExternals());

                /* Remove entries associated with a range, if any */
                if (thePushedExternals != null && tableExpr.getRange() != null) {
                    thePushedExternals.remove(thePushedExternals.size() - 1);
                    thePushedExternals.remove(thePushedExternals.size() - 1);
                }
            }
        } else {
            theDistributionKind = DistributionKind.ALL_PARTITIONS;
        }
    }

    DistributionKind getDistributionKind() {
        return theDistributionKind;
    }

    PrimaryKeyImpl getPrimaryKey() {
        return thePrimaryKey;
    }

    ArrayList<Expr> getPushedExternals() {
        return thePushedExternals;
    }

    void addSort(int[] sortExprPositions, SortSpec[] specs) {
        theSortFieldPositions = sortExprPositions;
        theSortSpecs = specs;
        theType = computeType();
    }

    int[] getSortFieldPositions() {
        return theSortFieldPositions;
    }

    SortSpec[] getSortSpecs() {
        return theSortSpecs;
    }

    @Override
    ExprType computeType() {
        return theInput.getType();
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {

        formatter.indent(sb);
        sb.append("DistributionKind : ").append(theDistributionKind);
        sb.append(",\n");
        if (thePrimaryKey != null) {
            formatter.indent(sb);
            sb.append("PrimaryKey :").append(thePrimaryKey);
            sb.append(",\n");
        }
        if (theSortFieldPositions != null) {
            formatter.indent(sb);
            sb.append("theSortFieldPositions : ").append(theSortFieldPositions);
            sb.append(",\n");
        }
        formatter.indent(sb);
        theInput.display(sb, formatter);
    }
}
