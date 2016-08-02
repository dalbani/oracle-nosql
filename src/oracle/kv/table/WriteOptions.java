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

package oracle.kv.table;

import java.util.concurrent.TimeUnit;

import oracle.kv.Durability;
import oracle.kv.KVStoreConfig;

/**
 * WriteOptions is passed to store operations that can update the store to
 * specify non-default behavior relating to durability, timeouts and expiry
 * operations.
 * <p>
 * The default behavior is configured when a store is opened using
 * {@link KVStoreConfig}.
 *
 * @since 3.0
 */
public class WriteOptions implements Cloneable {

    private Durability durability;
    private long timeout;
    private TimeUnit timeoutUnit;
    private boolean updateTTL;

    /**
     * Creates a {@code WriteOptions} with default values.
     * Same as WriteOptions(null, 0, null)
     * @see #WriteOptions(Durability, long, TimeUnit)
     *
     * @since 4.0
     */
    public WriteOptions() {
        this(null, 0, null);
    }

    /**
     * Creates a {@code WriteOptions} with the specified parameters.
     * <p>
     * If {@code durability} is {@code null}, the
     * {@link KVStoreConfig#getDurability default durability} is used.
     * <p>
     * The {@code timeout} parameter is an upper bound on the time interval for
     * processing the operation.  A best effort is made not to exceed the
     * specified limit. If zero, the {@link KVStoreConfig#getRequestTimeout
     * default request timeout} is used.
     * <p>
     * If {@code timeout} is not 0, the {@code timeoutUnit} parameter must not
     * be {@code null}.
     *
     * @param durability the write durability to use
     * @param timeout the timeout value to use
     * @param timeoutUnit the {@link TimeUnit} used by the
     * <code>timeout</code> parameter
     *
     * @throws IllegalArgumentException if timeout is negative
     * @throws IllegalArgumentException if timeout is > 0 and timeoutUnit
     * is null
     */
    public WriteOptions(Durability durability,
                        long timeout,
                        TimeUnit timeoutUnit) {
        setTimeout(timeout, timeoutUnit).
        setDurability(durability);
    }

    /**
     * Sets durability of write operation.
     * @param durability can be null. If {@code null}, the
     * {@link KVStoreConfig#getDurability default durability} will be used.
     * @return this
     *
     * @since 4.0
     */
    public WriteOptions setDurability(Durability durability) {
        this.durability = durability;
        return this;
    }

    /**
     * Sets timeout for write operation.
     *
     * @param timeout the timeout value to use
     * @param timeoutUnit the {@link TimeUnit} used by the
     * <code>timeout</code> parameter
     * @return this
     * @throws IllegalArgumentException if timeout is negative
     * @throws IllegalArgumentException if timeout is > 0 and timeoutUnit
     * is null
     *
     * @since 4.0
     */
    public WriteOptions setTimeout(long timeout, TimeUnit timeoutUnit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be >= 0");
        }
        if ((timeout != 0) && (timeoutUnit == null)) {
            throw new IllegalArgumentException("A non-zero timeout requires " +
                                               "a non-null timeout unit");
        }
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        return this;
    }

    /**
     * Returns the durability associated with the operation.
     *
     * @return the durability or null
     */
    public Durability getDurability() {
        return durability;
    }

    /**
     * Returns the timeout, which is an upper bound on the time interval for
     * processing the operation.  A best effort is made not to exceed the
     * specified limit. If zero, the {@link KVStoreConfig#getRequestTimeout
     * default request timeout} is used.
     *
     * @return the timeout or zero
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Returns the unit of the timeout parameter.
     *
     * @return the {@code TimeUnit} or null if the timeout has not been set.
     *
     * @since 4.0
     */
    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    /**
     * Sets whether absolute expiration time will be modified during update.
     * If false and the operation updates a record, the record's expiration
     * time will not change.
     * <br>
     * If the operation inserts a record, this parameter is ignored and the
     * specified TTL is always applied.
     * <br>
     * By default, this property is false. To update expiration time of an
     * existing record, this flag must be set to true.
     *
     * @param flag set to true if the operation should update an existing
     * record's expiration time.
     *
     * @return this
     *
     * @since 4.0
     */
    public WriteOptions setUpdateTTL(boolean flag) {
        updateTTL = flag;
        return this;
    }

    /**
     * Returns true if the absolute expiration time is to be modified during
     * update operations.
     *
     * @since 4.0
     */
    public boolean getUpdateTTL() {
        return updateTTL;
    }

    /**
     * @since 4.0
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        WriteOptions clone = new WriteOptions();
        clone.setDurability(durability);
        clone.setTimeout(timeout, timeoutUnit);

        return clone;
    }

    /**
     * @since 4.0
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Durability ").append(durability)
            .append(" timeout=").append(timeout)
            .append(timeoutUnit == null ? "" : timeoutUnit)
            .append(" updateTTL=").append(updateTTL);
        return buf.toString();
    }

}
