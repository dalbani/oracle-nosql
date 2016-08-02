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

package oracle.kv.impl.api.ops;

import java.io.DataInput;
import java.io.IOException;

import oracle.kv.Direction;
import oracle.kv.KeyRange;
import oracle.kv.impl.api.StoreIteratorParams;
import oracle.kv.impl.api.table.TargetTables;

/**
 * Iterate over table rows where the records may or may not reside on
 * the same partition.  Row values are returned which means that the
 * records are fetched from matching keys.
 */
public class TableIterate extends TableIterateOperation {

    public TableIterate(StoreIteratorParams sip,
                        TargetTables targetTables,
                        boolean majorComplete,
                        byte[] resumeKey) {
        super(OpCode.TABLE_ITERATE, sip, targetTables,
              majorComplete, resumeKey);
    }

    /*
     * Internal constructor used by table index population that avoids
     * StoreIteratorParams and defaults direction.
     */
    public TableIterate(byte[] parentKeyBytes,
                        TargetTables targetTables,
                        Direction direction,
                        KeyRange range,
                        boolean majorComplete,
                        int batchSize,
                        byte[] resumeKey) {
        super(OpCode.TABLE_ITERATE, parentKeyBytes, targetTables,
              direction, range, majorComplete, batchSize, resumeKey);
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    protected TableIterate(DataInput in, short serialVersion)
        throws IOException {

        super(OpCode.TABLE_ITERATE, in, serialVersion);
    }
}
