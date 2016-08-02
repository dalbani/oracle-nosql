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

import oracle.kv.Version;
import oracle.kv.impl.util.SerialVersion;

/**
 * This class holds results of an index row iteration over a table.  It extends
 * ResultKeyValueVersion to do this, adding the index key in addition to the
 * row itself.  The index key is needed by the client in order to accurately do
 * chunked iteration using a resume key.  The resume key cannot be constructed
 * from the Row alone in all cases.
 *
 * This class, which includes the index key bytes, was introduced in release
 * 3.2, so it only includes/expected index key bytes if the serialVersion of
 * the "other" side of the connection is at least the version associated with
 * 3.2 (V6).
 *
 * @since 3.2
 */
public class ResultIndexRows extends ResultKeyValueVersion {

    /*
     * Clients and servers with this serial version are prepared for the index
     * key in addition to the ResultKeyValueVersion.  This code was introduced
     * in SerialVersion.V6.
     */
    private static final short RESULT_INDEX_ITERATE_VERSION = SerialVersion.V6;

    /* this class adds the index key to ResultKeyValueVersion */
    private final byte[] indexKeyBytes;

    public ResultIndexRows(byte[] indexKeyBytes,
                           byte[] primaryKeyBytes,
                           byte[] valueBytes,
                           Version version) {
        super(primaryKeyBytes, valueBytes, version);
        this.indexKeyBytes = indexKeyBytes;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor
     * first to read common elements.
     */
    public ResultIndexRows(ObjectInput in, short serialVersion)
        throws IOException {
        super(in, serialVersion);
        if (serialVersion >= RESULT_INDEX_ITERATE_VERSION) {
            int keyLen = in.readShort();
            indexKeyBytes = new byte[keyLen];
            in.readFully(indexKeyBytes);
        } else {
            indexKeyBytes = null;
        }
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(ObjectOutput out, short serialVersion)
        throws IOException {
        super.writeFastExternal(out, serialVersion);
        if (serialVersion >= RESULT_INDEX_ITERATE_VERSION) {
            out.writeShort(indexKeyBytes.length);
            out.write(indexKeyBytes);
        }
    }

    public byte[] getIndexKeyBytes() {
        return indexKeyBytes;
    }
}
