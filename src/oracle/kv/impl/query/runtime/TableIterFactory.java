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

package oracle.kv.impl.query.runtime;

import oracle.kv.impl.api.table.RecordDefImpl;
import oracle.kv.impl.api.table.RecordValueImpl;

import static oracle.kv.impl.query.QueryException.Location;
import oracle.kv.table.FieldRange;
import oracle.kv.Direction;

/**
 * An interface to abstract client and server side implementations of
 * BaseTableIter. The client side implementation exists primarily for
 * debuggability.
 *
 * The compiler creates an instance T of BaseTableIter. During the open() method
 * on T, TableIterFactory.createTableIter() is called to create an instance TC
 * of either ClientTableIter or ServerTableIter. A ref to TC is stored in T, and
 * the next(), reset(), and close() methods on T are propagated to TC.
 *
 * TBD: should this just pass BaseTableIter and have the constructors
 * get state directly via accessor methods?
 */
public interface TableIterFactory {
    public PlanIter createTableIter(
        RuntimeControlBlock rcb,
        Location loc,
        int statePos,
        int resultReg,
        int[] tupleRegs,
        String tableName,
        String indexName,
        RecordDefImpl typeDefinition,
        Direction dir,
        RecordValueImpl primKeyRecord,
        RecordValueImpl secKeyRecord,
        FieldRange range,
        PlanIter filterIter,
        boolean usesCoveringIndex,
        PlanIter[] pushedExternals);
}
