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

import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.TypeManager;


/**
 * Represents a literal in the query text.
 *
 * For now, literals can be strings and numbers (long or double).
 */
class ExprConst extends Expr {

    private FieldValueImpl theValue;

    ExprConst(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        FieldValueImpl value) {

        super(qcb, sctx, ExprKind.CONST, location);
        setValue(value);
    }

    ExprConst(
        QueryControlBlock qcb,
        StaticContext sctx,
        QueryException.Location location,
        boolean value) {

        super(qcb, sctx, ExprKind.CONST, location);
        setValue(FieldDefImpl.booleanDef.createBoolean(value));
    }

    @Override
    int getNumChildren() {
        return 0;
    }

    FieldValueImpl getValue() {
        return theValue;
    }

    void setValue(FieldValueImpl val) {
        theValue = val;
        setType(TypeManager.createValueType(val));
    }

    @Override
    ExprType computeType() {
        return getTypeInternal();
    }

    @Override
    void display(StringBuilder sb, QueryFormatter formatter) {
        formatter.indent(sb);
        sb.append(theKind);
        sb.append("[");
        displayContent(sb, formatter);
        sb.append("]");
    }

    @Override
    void displayContent(StringBuilder sb, QueryFormatter formatter) {
        theValue.toStringBuilder(sb);
    }
}
