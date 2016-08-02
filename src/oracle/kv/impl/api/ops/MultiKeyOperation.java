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
import java.io.DataOutput;
import java.io.IOException;

import oracle.kv.Depth;
import oracle.kv.KeyRange;
import oracle.kv.impl.util.UserDataControl;

/**
 * A multi-key operation has a parent key, optional KeyRange and depth.
 */
abstract class MultiKeyOperation extends InternalOperation {

    /**
     * The parent key, or null.
     */
    private final byte[] parentKey;

    /**
     * Sub-key range of traversal, or null.
     */
    private final KeyRange subRange;

    /**
     * Depth of traversal, always non-null.
     */
    private final Depth depth;

    /**
     * Constructs a multi-key operation.
     *
     * For subclasses, allows passing OpCode.
     */
    MultiKeyOperation(OpCode opCode,
                      byte[] parentKey,
                      KeyRange subRange,
                      Depth depth) {
        super(opCode);
        this.parentKey = parentKey;
        this.subRange = subRange;
        this.depth = depth;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     *
     * For subclasses, allows passing OpCode.
     */
    MultiKeyOperation(OpCode opCode, DataInput in, short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);

        final int keyLen = in.readShort();
        if (keyLen < 0) {
            parentKey = null;
        } else {
            parentKey = new byte[keyLen];
            in.readFully(parentKey);
        }

        if (in.readByte() == 0) {
            subRange = null;
        } else {
            subRange = new KeyRange(in, serialVersion);
        }

        final int depthOrdinal = in.readByte();
        if (depthOrdinal == -1) {
            depth = null;
        } else {
            depth = Depth.getDepth(depthOrdinal);
        }
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);

        if (parentKey == null) {
            out.writeShort(-1);
        } else {
            out.writeShort(parentKey.length);
            out.write(parentKey);
        }

        if (subRange == null) {
            out.write(0);
        } else {
            out.write(1);
            subRange.writeFastExternal(out, serialVersion);
        }

        if (depth == null) {
            out.writeByte(-1);
        } else {
            out.writeByte(depth.ordinal());
        }
    }

    byte[] getParentKey() {
        return parentKey;
    }

    KeyRange getSubRange() {
        return subRange;
    }

    Depth getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return super.toString() +
            " parentKey: " + UserDataControl.displayKey(parentKey) +
            " subRange: " + UserDataControl.displayKeyRange(subRange) +
            " depth: " + depth;
    }
}
