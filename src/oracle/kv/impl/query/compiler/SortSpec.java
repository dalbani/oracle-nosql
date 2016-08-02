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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import oracle.kv.impl.util.FastExternalizable;

/**
 * The order-by clause, for each sort expression allows for an optional
 * "sort spec", which specifies the relative order of NULLs (less than or
 * greater than all other values) and whether the values returned by the
 * sort expr should be sorted in ascending or descending order.
 *
 * The SortSpec class stores these two pieces of information.
 */
public class SortSpec implements FastExternalizable {

    public final boolean theIsDesc;

    public final boolean theNullsFirst;

    SortSpec(boolean isDesc, boolean nullsFirst) {
        theIsDesc = isDesc;
        theNullsFirst = nullsFirst;
    }

    public SortSpec(
        DataInput in,
        @SuppressWarnings("unused") short serialVersion) throws IOException {
        theIsDesc = in.readBoolean();
        theNullsFirst = in.readBoolean();
    }

    @Override
    public void writeFastExternal(
        DataOutput out,
        short serialVersion)  throws IOException {
        out.writeBoolean(theIsDesc);
        out.writeBoolean(theNullsFirst);
    }
}
