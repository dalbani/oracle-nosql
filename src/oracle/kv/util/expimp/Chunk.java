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

package oracle.kv.util.expimp;

/**
 * Files are stored in the export store in chunks of size 1GB. This class
 * manages the chunks for a given file.
 * Oracle object store requires the chunk sequences to be sortable
 * lexicographically and not numerically. This class is responsible for
 * generating sortable lexicographic chunk sequences for a given file.
 *
 * Example: If the file named PERSON has size 4GB, it is stored in the export
 * store in 4 chunks where each chunk size is 1GB.
 * First file segment name is PERSON-abcdefghijkl
 * Second file segment name is PERSON-abcdefghijlk
 * Third file segment name is PERSON-abcdefghikjl
 * Fourth file segment name is PERSON-abcdefghiklj
 * As seen above the chunk sequences are sorted lexicographically
 *
 * Using this strategy the maximum number of segments that can be created for
 * a file is equal to the number of permutations of the string abcdefghijkl
 * = 12! ~ 479M chunks.
 *
 * This class also keeps track of the number of chunks that has been created
 * for a given file.
 */
public class Chunk {
    /*
     * Total number of chunks for a given file
     */
    private Integer numChunks = 0;

    /*
     * Initial chunk sequence for the file segment is abcdefghijkl
     */
    private char[] chunkSequence =
        {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l'};

    /**
     * Return the current chunk sequence for the file
     */
    public String get() {
        StringBuilder builder = new StringBuilder(chunkSequence.length);

        for(Character ch: chunkSequence) {
            builder.append(ch);
        }

        return builder.toString();
    }

    /**
     * Return the number of chunks created for a given file
     */
    public Integer getNumChunks() {
        return numChunks;
    }

    /**
     * Generate the next chunk sequence
     *
     * Algorithm:
     * 1) Find the largest index k such that a[k] < a[k + 1]. If no such index
     *    exists, the permutation is the last permutation.
     * 2) Find the largest index l greater than k such that a[k] < a[l].
     * 3) Swap the value of a[k] with that of a[l].
     * 4) Reverse the sequence from a[k + 1] up to and including the final
     *    element a[n].
     */
    public String next() {
        int index1, tempIndex, index2 = 0;

        /*
         * Find the largest index index1 such that a[index1] < a[index1 + 1]
         */
        for (index1 = chunkSequence.length - 2; index1 >= 0; index1--) {
            if (chunkSequence[index1] < chunkSequence[index1 + 1]) {
                break;
            }
        }

        /*
         * No such index exists. Exhausted all the permutations of the string.
         * Return null;
         */
        if (index1 == -1) {
            return null;
        }

        /*
         * Find the largest index index2 greater than k such that
         * a[index1] < a[index2].
         */
        for (tempIndex = index1 + 1;
             tempIndex < chunkSequence.length; tempIndex++) {
            if (chunkSequence[tempIndex] > chunkSequence[index1]) {
                index2 = tempIndex;
            }
        }

        /*
         * Swap the value of a[index1] with that of a[index2].
         */
        char ch1 = chunkSequence[index1];
        char ch2 = chunkSequence[index2];

        chunkSequence[index1] = ch2;
        chunkSequence[index2] = ch1;

        index1 = index1 + 1;
        index2 = chunkSequence.length - 1;

        /*
         * Reverse the sequence from a[index1 + 1] up to and including the final
         * element of chunkSequence.
         */
        while (index2 > index1) {
            ch1 = chunkSequence[index1];
            ch2 = chunkSequence[index2];
            chunkSequence[index1] = ch2;
            chunkSequence[index2] = ch1;
            index2--;
            index1++;
        }

        numChunks++;
        return get();
    }
}

