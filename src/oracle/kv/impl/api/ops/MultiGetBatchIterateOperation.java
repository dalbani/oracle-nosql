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
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;

import oracle.kv.Depth;
import oracle.kv.KeyRange;

import oracle.kv.impl.util.UserDataControl;

/**
 * This is an intermediate class for multi-get-batch iterate operation.
 */
abstract class MultiGetBatchIterateOperation extends MultiKeyOperation {

    private final List<byte[]> parentKeys;
    private final int batchSize;
    private final byte[] resumeKey;

    public MultiGetBatchIterateOperation(OpCode opCode,
                                         List<byte[]> parentKeys,
                                         byte[] resumekey,
                                         KeyRange subRange,
                                         Depth depth,
                                         int batchSize) {

        super(opCode, parentKeys.get(0), subRange, depth);

        this.parentKeys = parentKeys;
        this.resumeKey = resumekey;
        this.batchSize = batchSize;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    protected MultiGetBatchIterateOperation(OpCode opCode,
                                            DataInput in,
                                            short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);
        int nkeys = in.readShort();
        if (nkeys > 0) {
            parentKeys = new ArrayList<byte[]>(nkeys);
            for (int i = 0; i < nkeys; i++) {
                int len = in.readShort();
                byte[] key = new byte[len];
                in.readFully(key);
                parentKeys.add(key);
            }
        } else {
            parentKeys = null;
        }
        final int len = in.readShort();
        if (len > 0) {
            resumeKey = new byte[len];
            in.readFully(resumeKey);
        } else {
            resumeKey = null;
        }
        batchSize = in.readInt();
    }

    List<byte[]> getParentKeys() {
        return parentKeys;
    }

    int getBatchSize() {
        return batchSize;
    }

    byte[] getResumeKey() {
        return resumeKey;
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);
        if (parentKeys != null) {
            out.writeShort(parentKeys.size());
            for (byte[] key: parentKeys) {
                out.writeShort(key.length);
                out.write(key);
            }
        } else {
            out.writeShort(-1);
        }
        if (resumeKey != null && resumeKey.length > 0) {
            out.writeShort(resumeKey.length);
            out.write(resumeKey);
        } else {
            out.writeShort(-1);
        }
        out.writeInt(batchSize);
    }

    @Override
    public String toString() {
        return "parentKeys: " + parentKeys.size() +
            " resumeKey: " + UserDataControl.displayKey(resumeKey) +
            " subRange: " + UserDataControl.displayKeyRange(getSubRange()) +
            " depth: " + getDepth();
    }
}
