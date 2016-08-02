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

/**
 * A simple class to hold query expression and plan formatting information,
 * such as indent level. A new instance of this class is passed to
 * display() methods.
 *
 * theIndent:
 * The current number of space chars to be printed as indentation when
 * displaying the expression tree or the query execution plan.
 */
public class QueryFormatter {

    private final int theIndentIncrement;

    private int theIndent;

    public QueryFormatter(int increment) {
        theIndentIncrement = increment;
    }

    public QueryFormatter() {
        theIndentIncrement = 2;
    }

    public int getIndent() {
        return theIndent;
    }

    public int getIndentIncrement() {
        return theIndentIncrement;
    }

    public void setIndent(int v) {
        theIndent = v;
    }

    public void incIndent() {
        theIndent += theIndentIncrement;
    }

    public void decIndent() {
        theIndent -= theIndentIncrement;
    }

    public void indent(StringBuilder sb) {
        for (int i = 0; i < theIndent; ++i) {
            sb.append(' ');
        }
    }
}
