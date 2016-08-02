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

/**
 * For internal use only.
 * @hidden
 *
 * Represents a key/value pair.
 *
 * <p>The KeyValue is used as element of input argument EntryStream<KeyValue>
 * for {@link KVStore#put put}, the key and value properties will always
 * be non-null.</p>
 */
public class KeyValue {

    private final Key key;
    private final Value value;

    /**
     * Creates a KeyValue, key and value should be non-null.
     */
    public KeyValue(final Key key, final Value value) {

        if (key == null) {
            throw new IllegalArgumentException("key argument must not be null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value argument must not be " +
            		                           "null");
        }

        this.key = key;
        this.value = value;
    }

    /**
     * Returns the Key part of the KV pair.
     */
    public Key getKey() {
        return key;
    }

    /**
     * Returns the Value part of the KV pair.
     */
    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key.toString() + ' ' + value + ' ';
    }
}
