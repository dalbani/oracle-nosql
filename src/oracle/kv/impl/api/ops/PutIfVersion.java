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

import oracle.kv.ReturnValueVersion;
import oracle.kv.Value;
import oracle.kv.Version;
import oracle.kv.table.TimeToLive;

/**
 * Inserts a key/data pair.
 */
public class PutIfVersion extends Put {

    private final Version matchVersion;

    /**
     * Constructs a put-if-version operation.
     */
    public PutIfVersion(byte[] keyBytes,
                        Value value,
                        ReturnValueVersion.Choice prevValChoice,
                        Version matchVersion) {
        this(keyBytes, value, prevValChoice, matchVersion, 0);
    }

    /**
     * Constructs a put-if-version operation with a table id.
     */
    public PutIfVersion(byte[] keyBytes,
                        Value value,
                        ReturnValueVersion.Choice prevValChoice,
                        Version matchVersion,
                        long tableId) {
        this(keyBytes, value, prevValChoice, matchVersion, tableId,
                null, false);
    }

    public PutIfVersion(byte[] keyBytes,
            Value value,
            ReturnValueVersion.Choice prevValChoice,
            Version matchVersion,
            long tableId,
            TimeToLive ttl,
            boolean updateTTL) {
        super(OpCode.PUT_IF_VERSION, keyBytes, value, prevValChoice, tableId,
                ttl, updateTTL);
        this.matchVersion = matchVersion;
    }
    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    PutIfVersion(DataInput in, short serialVersion)
        throws IOException {

        super(OpCode.PUT_IF_VERSION, in, serialVersion);
        matchVersion = Version.createVersion(in, serialVersion);
    }

    Version getMatchVersion() {
        return matchVersion;
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);
        matchVersion.writeFastExternal(out, serialVersion);
    }

    @Override
    public String toString() {
        return super.toString() + " MatchVersion: " + matchVersion;
    }
}
