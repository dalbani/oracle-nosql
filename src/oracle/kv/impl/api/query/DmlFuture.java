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

package oracle.kv.impl.api.query;

import java.util.concurrent.TimeUnit;

import oracle.kv.ExecutionFuture;
import oracle.kv.StatementResult;

/**
 * An implementation of ExecutionFuture that encapsulates a DML query.  At
 * this time such queries are always synchronous.
 *
 * @since 4.0
 */
public class DmlFuture implements ExecutionFuture {

    private final StatementResult result;

    public DmlFuture(final StatementResult result) {
        this.result = result;
    }

    /**
     * Cancel always fails -- the query is done.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public StatementResult get() {
        return result;
    }

    @Override
    public StatementResult get(long timeout, TimeUnit unit) {
        return result;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public StatementResult updateStatus() {
        return result;
    }

    @Override
    public StatementResult getLastStatus() {
        return result;
    }

    @Override
    public String getStatement() {

        /*
         * TODO: Should this get stored? if so, some work on PreparedStatement
         * is necessary.
         */
        return null;
    }

    @Override
    public byte[] toByteArray() {

        /* TODO: maybe create a serialization of this */
        throw new IllegalArgumentException(
            "Cannot create a byte array from a query that is not DDL");
    }
}
