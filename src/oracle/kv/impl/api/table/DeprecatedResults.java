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

package oracle.kv.impl.api.table;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import oracle.kv.FaultException;

/**
 * This class contains wrapper classes meant solely to support the API change
 * to move TableAPI.{execute, executeSync} to a higher level package, and their
 * new home in KVStore.{execute, executeSync}.
 *
 * The TableAPI methods were changed to merely dispatch to the new KVStore
 * methods, in order to avoid duplication of the underlying implementation.
 * Since the result classes, ExecutionFuture and StatementResult also moved
 * from oracle.kv.table to oracle.kv, these wrapper classes translate the new
 * result classes to the old result classes.  This class can be removed when
 * the TableAPI.execute* methods are removed.
 */
@Deprecated
class DeprecatedResults {

    /**
     * Convert an oracle.kv.ExecutionFuture into an oracle.kv.ExecutionFuture.
     */
    static class ExecutionFutureWrapper
        implements oracle.kv.table.ExecutionFuture {

        /** The new methods return this kind of ExecutionFuture */
        final private oracle.kv.ExecutionFuture storeFuture;

        ExecutionFutureWrapper(oracle.kv.ExecutionFuture storeFuture) {
            this.storeFuture = storeFuture;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return storeFuture.cancel(mayInterruptIfRunning);
        }

        @Override
        public oracle.kv.table.StatementResult get()
            throws CancellationException,
                   ExecutionException, 
                   InterruptedException {
            return new StatementResultWrapper(storeFuture.get());
        }

        @Override
        public oracle.kv.table.StatementResult get(long timeout, TimeUnit unit)
            throws InterruptedException, 
                   TimeoutException,
                   ExecutionException {
            return new StatementResultWrapper(storeFuture.get(timeout, unit));
        }

        @Override
        public boolean isCancelled() {
            return storeFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return storeFuture.isDone();
        }

        @Override
        public oracle.kv.table.StatementResult updateStatus() 
            throws FaultException {
            return new StatementResultWrapper(storeFuture.updateStatus());
        }

        @Override
        public oracle.kv.table.StatementResult getLastStatus() {
            return new StatementResultWrapper(storeFuture.getLastStatus());
        }

        @Override
        public String getStatement() {
            return storeFuture.getStatement();
        }
    }

    /**
     * Convert an oracle.kv.StatementResult into an oracle.kv.StatementResult.
     */
    static class StatementResultWrapper
        implements oracle.kv.table.StatementResult {

        /** The new methods return this kind of result */
        private final oracle.kv.StatementResult storeResult;

        StatementResultWrapper(oracle.kv.StatementResult storeResult) {
            this.storeResult = storeResult;
        }

        @Override
        public int getPlanId() {
            return storeResult.getPlanId();
        }

        @Override
        public String getInfo() {
            return storeResult.getInfo();
        }

        @Override
        public String getInfoAsJson() {
            return storeResult.getInfoAsJson();
        }

        @Override
        public String getErrorMessage() {
            return storeResult.getErrorMessage();
        }

        @Override
        public boolean isSuccessful() {
            return storeResult.isSuccessful();
        }

        @Override
        public boolean isDone() {
            return storeResult.isDone();
        }

        @Override
        public boolean isCancelled() {
            return storeResult.isCancelled();
        }
    }
}
