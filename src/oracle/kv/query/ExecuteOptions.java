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

package oracle.kv.query;

import java.util.concurrent.TimeUnit;

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.Durability;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.table.TableIteratorOptions;

/**
 * Class contains several options for the KVStore.execute() methods.
 * It contains the following execution options: consistency, durability and
 * timeout.
 *
 * @see KVStore#execute(String, ExecuteOptions)
 * @see KVStore#executeSync(String, ExecuteOptions)
 * @see KVStore#executeSync(Statement, ExecuteOptions)
 *
 * @since 4.0
 */
public class ExecuteOptions {

    private Consistency consistency;
    private Durability durability;
    private long timeout;
    private TimeUnit timeoutUnit;
    private int maxConcurrentRequests;
    private int resultsBatchSize;


    public ExecuteOptions() {}

    /**
     * Sets the execution consistency.
     */
    public ExecuteOptions setConsistency(Consistency consistency) {
        this.consistency = consistency;
        return this;
    }

    /**
     * Gets the last set execution consistency.
     */
    public Consistency getConsistency() {
        return consistency;
    }

    /**
     * Sets the execution durability.
     */
    public ExecuteOptions setDurability(Durability durability) {
        this.durability = durability;
        return this;
    }

    /**
     * Gets the last set execution durability.
     */
    public Durability getDurability() {
        return durability;
    }

    /**
     * The {@code timeout} parameter is an upper bound on the time interval for
     * processing the operation.  A best effort is made not to exceed the
     * specified limit. If zero, the {@link KVStoreConfig#getRequestTimeout
     * default request timeout} is used.
     * <p>
     * If {@code timeout} is not 0, the {@code timeoutUnit} parameter must not
     * be {@code null}.
     *
     * @param timeout the timeout value to use
     * @param timeoutUnit the {@link TimeUnit} used by the
     * <code>timeout</code> parameter or null
     */
    public ExecuteOptions setTimeout(long timeout,
                                     TimeUnit timeoutUnit) {

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
     * Gets the timeout, which is an upper bound on the time interval for
     * processing the read or write operations.  A best effort is made not to
     * exceed the specified limit. If zero, the
     * {@link KVStoreConfig#getRequestTimeout default request timeout} is used.
     *
     * @return the timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Gets the unit of the timeout parameter, and may
     * be {@code null} only if {@link #getTimeout} returns zero.
     *
     * @return the timeout unit or null
     */
    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    /**
     * Returns the maximum number of concurrent requests.
     */
    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    /**
     * Sets the maximum number of concurrent requests.
     */
    public ExecuteOptions setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
        return this;
    }

    /**
     * Returns the number of results per request.
     */
    public int getResultsBatchSize() {
        return resultsBatchSize;
    }

    /**
     * Sets the number of results per request.
     */
    public ExecuteOptions setResultsBatchSize(int resultsBatchSize) {
        this.resultsBatchSize = resultsBatchSize;
        return this;
    }

    /**
     * For internal use only.
     * @hidden
     */
    public TableIteratorOptions createTableIteratorOptions(
        Direction direction) {

        return new TableIteratorOptions(direction,
                                        consistency,
                                        timeout,
                                        timeoutUnit,
                                        maxConcurrentRequests,
                                        resultsBatchSize);
    }
}