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

import oracle.kv.impl.util.FastExternalizable;

/**
 * This class holds results of a key iteration over a set of keys.
 * For each key it has 2-3 pieces of information:
 * 1. the key (a byte[])
 * 2. a boolean indicator of whether the key has a non-zero expiration time
 * 3. if an expiration time is present, the actual time (a long)
 *
 * Prior to the implementation of the TTL feature only the key bytes were
 * needed by key scan results. The new state was introduced in release 4.0.
 *
 * @since 4.0
 */
public class ResultKey implements FastExternalizable {

    private final byte[] keyBytes;
    private final long expirationTime;

    public ResultKey(byte[] keyBytes,
                     long expirationTime) {
        this.keyBytes = keyBytes;
        this.expirationTime = expirationTime;
    }

    public ResultKey(byte[] keyBytes) {
        this(keyBytes, 0);
    }

    /**
     * FastExternalizable constructor. This must be compatible with the
     * pre-TTL constructor for KeysIterateResult which had only the key bytes.
     */
    public ResultKey(DataInput in, short serialVersion)
        throws IOException {

        final int keyLen = in.readShort();
        keyBytes = new byte[keyLen];
        in.readFully(keyBytes);
        expirationTime = Result.readExpirationTime(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        out.writeShort(keyBytes.length);
        out.write(keyBytes);
        Result.writeExpirationTime(out, expirationTime, serialVersion);
    }

    public byte[] getKeyBytes() {
        return keyBytes;
    }

    public boolean hasExpirationTime() {
        return (expirationTime != 0);
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
