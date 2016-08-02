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

package oracle.kv.impl.api.ops;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Transaction;

import oracle.kv.KeyRange;
import oracle.kv.impl.api.ops.MultiGetBatchExecutor.MultiGetBatchHandler;
import oracle.kv.impl.api.table.TargetTables;
import oracle.kv.impl.topo.PartitionId;

/**
 * This is an intermediate class for a multi-batch-get table operation.
 */
abstract class MultiGetBatchTableOperation<V>
    extends MultiGetTableOperation implements MultiGetBatchHandler<V> {

    private final List<byte[]> keys;
    private final int batchSize;
    private final byte[] resumeKey;

    public MultiGetBatchTableOperation(OpCode opCode,
                                       List<byte[]> parentKeys,
                                       byte[] resumeKey,
                                       TargetTables targetTables,
                                       KeyRange subRange,
                                       int batchSize) {

        super(opCode, parentKeys.get(0), targetTables, subRange);
        this.keys = parentKeys;
        this.resumeKey= resumeKey;
        this.batchSize = batchSize;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    protected MultiGetBatchTableOperation(OpCode opCode,
                                          ObjectInput in,
                                          short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);
        int nkeys = in.readShort();
        if (nkeys > 0) {
            keys = new ArrayList<byte[]>(nkeys);
            for (int i = 0; i < nkeys; i++) {
                int len = in.readShort();
                byte[] key = new byte[len];
                in.readFully(key);
                keys.add(key);
            }
        } else {
            keys = null;
        }
        int len = in.readShort();
        if (len > 0) {
            resumeKey = new byte[len];
            in.readFully(resumeKey);
        } else {
            resumeKey = null;
        }
        batchSize = in.readInt();
    }

    List<byte[]> getParentKeys() {
        return keys;
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
    public void writeFastExternal(ObjectOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);
        if (keys != null) {
            out.writeShort(keys.size());
            for (byte[] key: keys) {
                out.writeShort(key.length);
                out.write(key);
            }
        } else {
            out.writeShort(-1);
        }
        if (resumeKey != null) {
            out.writeShort(resumeKey.length);
            out.write(resumeKey);
        } else {
            out.writeShort(-1);
        }
        out.writeInt(batchSize);
    }

    @Override
    public Result execute(Transaction txn,
                          PartitionId partitionId,
                          OperationHandler operationHandler) {

        verifyTableAccess(operationHandler);

        final MultiGetBatchExecutor<V> executor =
            new MultiGetBatchExecutor<V>(this);
        return executor.execute(txn, partitionId, operationHandler,
                                getParentKeys(), getResumeKey(),
                                getBatchSize());
    }
}
