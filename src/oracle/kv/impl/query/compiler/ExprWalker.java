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

import java.util.Iterator;

/**
 * ExprWalker traverses the expression tree in a depth-first fashion, calling
 * the enter() and exit() methods of a given visitor on each node of the tree.
 */
class ExprWalker {

    ExprVisitor theVisitor;
    boolean theAllocateChildrenIter;

    ExprWalker(ExprVisitor visitor, boolean allocateChildrenIter) {
        theVisitor = visitor;
        theAllocateChildrenIter = allocateChildrenIter;
    }

    void walk(Expr e) {

        switch (e.getKind()) {

        case CONST:
            theVisitor.enter((ExprConst)e);
            theVisitor.exit((ExprConst)e);
            break;

       case BASE_TABLE:
            if (theVisitor.enter((ExprBaseTable)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprBaseTable)e);
            break;

        case FUNC_CALL:
            if (theVisitor.enter((ExprFuncCall)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprFuncCall)e);
            break;

        case ARRAY_CONSTR:
            if (theVisitor.enter((ExprArrayConstr)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprArrayConstr)e);
            break;

        case PROMOTE:
            if (theVisitor.enter((ExprPromote)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprPromote)e);
            break;

        case FIELD_STEP:
            if (theVisitor.enter((ExprFieldStep)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprFieldStep)e);
            break;

        case FILTER_STEP:
            if (theVisitor.enter((ExprFilterStep)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprFilterStep)e);
            break;

        case SLICE_STEP:
            if (theVisitor.enter((ExprSliceStep)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprSliceStep)e);
            break;

        case VAR:
            theVisitor.enter((ExprVar)e);
            theVisitor.exit((ExprVar)e);
            break;

        case SFW:
            if (theVisitor.enter((ExprSFW)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprSFW)e);
            break;

        case RECEIVE:
            if (theVisitor.enter((ExprReceive)e)) {
                walkChildren(e);
            }
            theVisitor.exit((ExprReceive)e);
            break;

        default:
            assert(false);
        }
    }

    void walkChildren(Expr e) {

        Iterator<Expr> children = 
            (theAllocateChildrenIter ? e.getChildrenIter() : e.getChildren());

        while (children.hasNext()) {
            Expr child = children.next();
            walk(child);
        }
    }
}
