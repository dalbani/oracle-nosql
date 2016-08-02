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

import java.util.Set;

/**
 * Defines the interface to provide basic cache functionality.
 *
 * @param <K> type of key object
 * @param <V> type of value object
 */
public interface Cache<K, V> {

    /**
     * Returns the value associated with given key in the cache, or null if
     * no value cached for key. 
     * 
     * @param key
     * @return value
     */
    V get(K key);

    /**
     * Store the key value pair in the cache.
     *
     * If the cache contains the value associated with key, replace the old
     * value by given value 
     * 
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     * Remove cached value for given key.
     * 
     * @param key
     * @return the previously cached value or null
     */
    V invalidate(K key);

    /**
     * Return cache maximum capacity.
     *
     * @return cache maximum capacity
     */
    int getCapacity();

    /**
     * Return a copy of all available values in the cache.
     *
     * @return all values.
     */
    Set<V> getAllValues();

    /**
     * Update entry lifetime with new value in milliseconds.
     * 
     * @param lifeTimeInMillis new lifetime in milliseconds
     */
    void setEntryLifetime(long lifeTimeInMillis);

    /**
     * Stop all background tasks of cache.
     * 
     * @param wait whether wait for the background task finish
     */
    void stop(boolean wait);
}
