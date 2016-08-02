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

package oracle.kv.impl.api.table;


/**
 * EmptyDefImpl represents the "empty" data type; the type that contains no
 * values. EmptyDefImpl cannot be used when defining the schema of a table
 * (i.e., in the CREATE TABLE statement). Instead, it is used to describe
 * the result of a query expression, when the query processor can infer that
 * the result will always be empty.
 */
public class EmptyDefImpl extends FieldDefImpl {

    private static final long serialVersionUID = 1L;

    EmptyDefImpl() {
        super(Type.EMPTY, "");
    }

    private EmptyDefImpl(EmptyDefImpl impl) {
        super(impl);
    }

    /*
     * Public api methods from Object and FieldDef
     */

    @Override
    public EmptyDefImpl clone() {
        return new EmptyDefImpl(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof EmptyDefImpl) {
            return true;
        }
        return false;
    }

    /*
     * FieldDefImpl internal api methods
     */

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isPrecise() {
        return false;
    }

    @Override
    public boolean isSubtype(FieldDefImpl superType) {
        return superType.getType() == Type.EMPTY;
    }
}
