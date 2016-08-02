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

import oracle.kv.impl.api.table.IndexRange;
import oracle.kv.impl.api.table.TargetTables;

/**
 * An index operation identifies the index by name and table and includes
 * start, end, and resume keys.  Rules for keys:
 * <ul>
 * <li>the resume key overrides the start key</li>
 * <li>a null start key is used to operate over the entire index</li>
 * <li>the boolean dontIterate is used to do exact match operations.  If true,
 * then only matching entries are returned.  There may be more than one.</li>
 * </ul>
 *
 * Resume key is tricky because secondary databases may have duplicates so in
 * order to resume properly it's necessary to have both the resume secondary
 * key and the resume primary key.
 *
 * This class is public to make it available to tests.
 */
public class IndexOperation extends InternalOperation {

    /*
     * These members represent the serialized state of the class in the
     * protocol.  These are in order.
     */
    private final String indexName;
    protected final TargetTables targetTables;
    protected final IndexRange range;
    private final byte[] resumeSecondaryKey;
    private final byte[] resumePrimaryKey;
    private final int batchSize;

    /**
     * Constructs an index operation.
     *
     * For subclasses, allows passing OpCode.
     */
    public IndexOperation(OpCode opCode,
                          String indexName,
                          TargetTables targetTables,
                          IndexRange range,
                          byte[] resumeSecondaryKey,
                          byte[] resumePrimaryKey,
                          int batchSize) {
        super(opCode);
        this.indexName = indexName;
        this.targetTables = targetTables;
        this.range = range;
        this.resumeSecondaryKey = resumeSecondaryKey;
        this.resumePrimaryKey = resumePrimaryKey;
        this.batchSize = batchSize;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     *
     * For subclasses, allows passing OpCode.
     */
    IndexOperation(OpCode opCode, DataInput in, short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);
        /* index name */
        indexName = in.readUTF();

        targetTables = new TargetTables(in, serialVersion);

        /* index range */
        range = new IndexRange(in, serialVersion);

        /* resume key */
        int keyLen = in.readShort();
        if (keyLen < 0) {
            resumeSecondaryKey = null;
            resumePrimaryKey = null;
        } else {
            /*
             * Resume keys, if present always have both secondary and
             * primary, in that order.
             */
            resumeSecondaryKey = new byte[keyLen];
            in.readFully(resumeSecondaryKey);
            keyLen = in.readShort();
            resumePrimaryKey = new byte[keyLen];
            in.readFully(resumePrimaryKey);
        }

        /* batch size */
        batchSize = in.readInt();
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);

        out.writeUTF(indexName);

        targetTables.writeFastExternal(out, serialVersion);

        range.writeFastExternal(out, serialVersion);

        if (resumeSecondaryKey == null) {
            out.writeShort(-1);
        } else {
            out.writeShort(resumeSecondaryKey.length);
            out.write(resumeSecondaryKey);
            out.writeShort(resumePrimaryKey.length);
            out.write(resumePrimaryKey);
        }
        out.writeInt(batchSize);
    }

    IndexRange getIndexRange() {
        return range;
    }

    byte[] getResumeSecondaryKey() {
        return resumeSecondaryKey;
    }

    byte[] getResumePrimaryKey() {
        return resumePrimaryKey;
    }

    int getBatchSize() {
        return batchSize;
    }

    String getIndexName() {
        return indexName;
    }

    TargetTables getTargetTables() {
        return targetTables;
    }

    @Override
    public String toString() {
        return super.toString(); //TODO
    }
}
