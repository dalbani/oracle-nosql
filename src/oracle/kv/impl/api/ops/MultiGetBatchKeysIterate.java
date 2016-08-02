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

import java.io.IOException;
import java.io.DataInput;
import java.util.List;

import oracle.kv.Depth;
import oracle.kv.KeyRange;

/**
 * A multi-get-batch-keys iterate operation.
 */
public class MultiGetBatchKeysIterate extends MultiGetBatchIterateOperation {

    /**
     * Construct a multi-get-batch-keys operation.
     *
     * @param parentKeys the batch of parent keys.
     * @param resumeKey is the key after which to resume the iteration of
     * descendants, or null to start at the parent.
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange.
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.
     * @param batchSize the max number of keys to return in one call.
     */
    public MultiGetBatchKeysIterate(List<byte[]> parentKeys,
                                    byte[] resumeKey,
                                    KeyRange subRange,
                                    Depth depth,
                                    int batchSize) {

        super(OpCode.MULTI_GET_BATCH_KEYS, parentKeys, resumeKey, subRange,
              depth, batchSize);
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    protected MultiGetBatchKeysIterate(DataInput in, short serialVersion)
        throws IOException {

        super(OpCode.MULTI_GET_BATCH_KEYS, in, serialVersion);
    }
}
