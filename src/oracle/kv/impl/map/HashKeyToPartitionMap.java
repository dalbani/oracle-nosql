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

package oracle.kv.impl.map;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import oracle.kv.Key;
import oracle.kv.impl.topo.PartitionId;

/**
 * A hash based implementation used to distribute keys across partitions.
 */
public class HashKeyToPartitionMap implements KeyToPartitionMap {

    private static final long serialVersionUID = 1L;

    final BigInteger nPartitions;

    transient DigestCache digestCache = new DigestCache();

    public HashKeyToPartitionMap(int nPartitions) {
        super();
        this.nPartitions = new BigInteger(Integer.toString(nPartitions));
    }

    @Override
    public int getNPartitions() {
        return nPartitions.intValue();
    }

    @Override
    public PartitionId getPartitionId(byte[] keyBytes) {
        if (digestCache == null) {
            digestCache = new DigestCache();
        }
        /* Clone one for use by this thread. */
        final MessageDigest md = digestCache.get();

        /* Digest Key major path. */
        md.update(keyBytes, 0, Key.getMajorPathLength(keyBytes));

        final BigInteger index = new BigInteger(md.digest()).mod(nPartitions);
        return new PartitionId(index.intValue() + 1);
    }

    /**
     * Implements a per-thread cache using a thread local, to mitigate the cost
     * of calling MessageDigest.getInstance("MD5").
     */
    static class DigestCache extends ThreadLocal<MessageDigest> {

        /** Create the message digest. */
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("MD5 algorithm unavailable");
            }
        }

        /** Reset the message digest before returning. */
        @Override
        public MessageDigest get() {
            final MessageDigest md = super.get();
            md.reset();
            return md;
        }
    }
}
