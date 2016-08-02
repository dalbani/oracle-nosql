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

package oracle.kv.impl.security.util;

import java.util.Random;

/**
 * A Bloom filter implementation reused from
 * {@link com.sleepycat.je.tree.BINDeltaBloomFilter}, whose JE logging methods
 * have been tailored here. As indicated in original doc, both space and
 * computation times are minimized in this implementation, with a potential
 * small loss in accuracy.
 */
public class BloomFilter {

    /*
     * Used to optimize creation of the bloom filter: Lets us avoid repeated
     * (per key) hashing of the key prefix and repeated allocations of the
     * RNG and the hashes array.
     */
    public static class HashContext {

        public int[] hashes;

        public Random rng;

        public long initFNVvalue;

        public HashContext() {
            hashes = new int[BloomFilter.K];
            rng = new Random();
            initFNVvalue = BloomFilter.FNVOffsetBasis;
        }
    }

    /*
     * Params for the Fowler-Noll-Vo (FNV) hash function
     */
    private static final long FNVOffsetBasis = 2166136261L;
    private static final long FNVPrime = 16777619L;

    /*
     * The m/n ratio, where m is the number of bits used by the bloom filter
     * and n is the number of keys in the set represented by the bloom filter.
     */
    private static final int M_N_RATIO = 8;

    /*
     * The number of hash values to generate per key, when a key is added to
     * the filter or when the key's membership is tested.
     */
    private static final int K = 3;

    /*
     * Add the given key to the given bloom filter
     */
    public static void add(byte[] bf, byte[] key, HashContext hc) {

        hash(bf, key, hc);

        for (int idx : hc.hashes) {
            setBit(bf, idx);
        }
    }

    /*
     * Test set membership for the given key
     */
    public static boolean contains(byte[] bf, byte[] key) {

        HashContext hc = new HashContext();

        hash(bf, key, hc);

        for (int idx : hc.hashes) {
            if (!getBit(bf, idx)) {
                return false;
            }
        }

        return true;
    }

    /*
     * Generate K hash values for the given key
     */
    private static void hash(byte[] bf, byte[] key, HashContext hc) {
        assert(hc.hashes.length == K);

        hc.rng.setSeed(hashFNV(key, hc.initFNVvalue));

        int numBits = bf.length * 8;

        if (numBits <= 1024) {
            int hash = hc.rng.nextInt();
            hc.hashes[0] = (hash & 0x000003FF) % numBits;
            hash = hash >> 10;
            hc.hashes[1] = (hash & 0x000003FF) % numBits;
            hash = hash >> 10;
            hc.hashes[2] = (hash & 0x000003FF) % numBits;
        } else {
            hc.hashes[0] = hc.rng.nextInt() % numBits;
            hc.hashes[1] = hc.rng.nextInt() % numBits;
            hc.hashes[2] = hc.rng.nextInt() % numBits;
        }
    }

    /*
     * Fowler-Noll-Vo hash function
     */
    private static long hashFNV(byte[] key, long initValue) {

        long hash = initValue;

        for (byte b : key) {
            hash = (hash * FNVPrime) & 0xFFFFFFFF;
            hash ^= b;
        }

        return hash;
    }


    /*
     * Get the number of bytes needed to store the bitset of a bloom filter
     * for the given number of keys.
     */
    public static int getByteSize(int numKeys) {
        assert(numKeys > 0);
        int nbits = numKeys * M_N_RATIO;
        return (nbits + 7) / 8;
    }

    public static String toString(byte[] bf) {

        StringBuilder sb = new StringBuilder();

        int nbits = bf.length * 8;

        for (int i = 0; i < nbits; ++i) {
            sb.append(getBit(bf, i) ? 1 : 0);
        }
        return sb.toString();
    }

    private static void setBit(byte[] bf, int idx) {
        bf[idx / 8] |= (1 << (idx % 8));
    }

    private static boolean getBit(byte[] bf, int idx) {
        return ( (bf[idx / 8] & (1 << (idx % 8))) != 0 );
    }

}
