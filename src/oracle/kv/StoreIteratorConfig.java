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

package oracle.kv;

import java.util.concurrent.TimeUnit;

/**
 * The configuration object for {@link KVStore#storeIterator(Direction, int,
 * Key, KeyRange, Depth, Consistency, long, TimeUnit, StoreIteratorConfig)}.
 */
public class StoreIteratorConfig {
    private int maxConcurrentRequests;
    private int maxResultsBatches;

    /**
     * Sets the maximum degree of parallelism (in effect the maximum number of
     * client-side threads) to be used when running a parallel store iteration.
     * Setting maxConcurrentRequests to 1 causes the store iteration to be
     * performed using only the current thread. Setting it to 0 lets the KV
     * Client determine the number of threads based on topology information (up
     * to a maximum of the number of available processors as returned by
     * java.lang.Runtime.availableProcessors()). Values less than 0 are
     * reserved for some future use and cause an IllegalArgumentException to be
     * thrown.
     *
     * @param maxConcurrentRequests the maximum number of client-side threads.
     *
     * @return this
     *
     * @throws IllegalArgumentException if a value less than 0 is passed for
     * maxConcurrentRequests.
     */
    public StoreIteratorConfig
        setMaxConcurrentRequests(int maxConcurrentRequests) {

        if (maxConcurrentRequests < 0) {
            throw new IllegalArgumentException
                ("maxConcurrentRequests must be >= 0");
        }
        this.maxConcurrentRequests = maxConcurrentRequests;
        return this;
    }

    /**
     * Returns the maximum number of concurrent requests.
     *
     * @return the maximum number of concurrent requests
     */
    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    /**
     * @deprecated since 3.4, no longer supported.
     *
     * @param maxResultsBatches the maximum number of results sets that can be
     * held on the client side before Replication Node processing pauses.
     *
     * @return this
     */
    @Deprecated
    public StoreIteratorConfig setMaxResultsBatches(int maxResultsBatches) {
        this.maxResultsBatches = maxResultsBatches;
        return this;
    }

    /**
     * @deprecated since 3.4, no longer supported.
     *
     * Returns the value set by setMaxResultsBatches().
     *
     * @return the value set by setMaxResultsBatches()
     */
    @Deprecated
    public int getMaxResultsBatches() {
        return maxResultsBatches;
    }

    @Override
    public String toString() {
        return String.format("maxConcurrentRequests=" + maxConcurrentRequests);
    }
}
