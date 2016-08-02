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
 * An index iteration that returns keys.  The index is specified by a
 * combination of the indexName and tableName.  The index scan range and
 * additional parameters are specified by the IndexRange.
 *
 * Both primary and secondary keys are required for resumption of batched
 * operations because a single index match may match a large number of
 * primary records. In order to honor batch size constraints a single
 * request/reply operation may need to resume within a set of duplicates.
 *
 * Unless ancestor return values are requested this operation returns only that
 * information which is available in the index itself avoiding extra fetches.
 * When an ancestor table is requested the operation will minimally verify that
 * the ancestor key exists in order to add it to the returned list.  Ancestor
 * keys are returned before the corresponding target table key.  This is true
 * even if the iteration is in reverse order.  In the event of multiple index
 * entries matching the same primary entry and/or ancestor entry, duplicate keys
 * will be returned.
 *
 * The childTables parameter is a list of child tables to return. These are not
 * supported in R3 and will be null.
 *
 * The ancestorTables parameter, if not null, specifies the ancestor tables to
 * return in addition to the target table, which is the table containing the
 * index.
 *
 * The resumeSecondaryKey parameter is used for batching of results and will be
 * null on the first call
 *
 * The resumePrimaryKey is used for batching of results and will be null
 * on the first call
 *
 * The batchSize parameter is the batch size to to use
 */
public class IndexKeysIterate extends IndexOperation {

    /**
     * Constructs an index operation.
     *
     * For subclasses, allows passing OpCode.
     */
    public IndexKeysIterate(String indexName,
                            TargetTables targetTables,
                            IndexRange range,
                            byte[] resumeSecondaryKey,
                            byte[] resumePrimaryKey,
                            int batchSize) {
        super(OpCode.INDEX_KEYS_ITERATE, indexName, targetTables,
              range, resumeSecondaryKey, resumePrimaryKey, batchSize);
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     *
     * For subclasses, allows passing OpCode.
     */
    IndexKeysIterate(DataInput in, short serialVersion)
        throws IOException {

        super(OpCode.INDEX_KEYS_ITERATE, in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);
    }

    @Override
    public String toString() {
        return super.toString(); //TODO
    }
}
