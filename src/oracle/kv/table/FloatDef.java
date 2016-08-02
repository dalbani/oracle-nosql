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

package oracle.kv.table;

/**
 * FloatDef is an extension of {@link FieldDef} to encapsulate the Float type.
 * It adds a minimum and maximum value range and a default value.
 * Minimum and maximum are inclusive.
 *
 * @since 3.0
 */
public interface FloatDef extends FieldDef {

    /**
     * @return the minimum value for the instance if defined, otherwise null
     *
     * @deprecated as of release 4.0 it is no longer possible to specify
     * ranges on Float types. A storage size argument can be specified on
     * a Float type when used in a primary key.
     */
    @Deprecated
    Float getMin();

    /**
     * @return the maximum value for the instance if defined, otherwise null
     *
     * @deprecated as of release 4.0 it is no longer possible to specify
     * ranges on Float types. A storage size argument can be specified on
     * a Float type when used in a primary key.
     */
    @Deprecated
    Float getMax();

    /**
     * @return a deep copy of this object
     */
    @Override
    public FloatDef clone();
}
