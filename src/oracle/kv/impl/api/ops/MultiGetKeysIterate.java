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

import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.KeyRange;

/**
 * A multi-get-keys-iterate operation.
 */
public class MultiGetKeysIterate extends MultiKeyIterate {

    /**
     * Construct a multi-get-keys-iterate operation.
     */
    public MultiGetKeysIterate(byte[] parentKey,
                               KeyRange subRange,
                               Depth depth,
                               Direction direction,
                               int batchSize,
                               byte[] resumeKey) {
        super(OpCode.MULTI_GET_KEYS_ITERATE, parentKey, subRange, depth,
              direction, batchSize, resumeKey);
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    MultiGetKeysIterate(DataInput in, short serialVersion)
        throws IOException {

        super(OpCode.MULTI_GET_KEYS_ITERATE, in, serialVersion);
    }
}
