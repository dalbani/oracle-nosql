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

package oracle.kv.impl.api;

import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.Version;

/**
 * Adds an expiration time to KeyValueVersion, making its
 * getExpirationTime() interface return a valid value.
 */
public class KeyValueVersionInternal extends KeyValueVersion {

    private final long expirationTime;

    /**
     * Creates a KeyValueVersion with non-null properties, extending it
     * to include an expiration time.
     */
    public KeyValueVersionInternal(final Key key,
                                   final Value value,
                                   final Version version,
                                   final long expirationTime) {
        super(key, value, version);
        this.expirationTime = expirationTime;
    }

    /**
     * Creates a KeyValueVersion with non-null properties for key and value,
     * extending it to include an expiration time.
     */
    public KeyValueVersionInternal(final Key key,
                                   final Value value,
                                   final long expirationTime) {
        super(key, value);
        this.expirationTime = expirationTime;
    }

    @Override
    public long getExpirationTime() {
        return expirationTime;
    }

    @Override
    public String toString() {
        return super.toString() + ' ' + expirationTime;
    }
}
