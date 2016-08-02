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
package oracle.kv.table;

import java.util.List;

import oracle.kv.ContingencyException;

/**
 * Provides information about a failure from the sequence of operations
 * executed by {@link TableAPI#execute(List, WriteOptions)
 * TableAPI.execute(List&lt;TableOperation&gt;, WriteOptions)}
 */
public class TableOpExecutionException extends ContingencyException {

    private static final long serialVersionUID = 1L;

    private final TableOperation failedOperation;
    private final int failedOperationIndex;
    private final TableOperationResult failedOperationResult;

    /**
     * For internal use only.
     * @hidden
     */
    public TableOpExecutionException
        (TableOperation failedOperation,
         int failedOperationIndex,
         TableOperationResult failedOperationResult) {

        super("Failed table operation, type: " + failedOperation.getType() +
              ", operation index in list: " + failedOperationIndex);
        this.failedOperation = failedOperation;
        this.failedOperationIndex = failedOperationIndex;
        this.failedOperationResult = failedOperationResult;
    }

    /**
     * Returns the operation that caused the execution to be aborted.
     * @return the operation that caused the execution to be aborted
     */
    public TableOperation getFailedOperation() {
        return failedOperation;
    }

    /**
     * Returns the result of the operation that caused the execution to be
     * aborted.
     * @return the result of the operation that caused the execution to be
     * aborted
     */
    public TableOperationResult getFailedOperationResult() {
        return failedOperationResult;
    }

    /**
     * Returns the list index of the operation that caused the execution to be
     * aborted.
     * @return the list index of the operation that caused the execution to be
     * aborted
     */
    public int getFailedOperationIndex() {
        return failedOperationIndex;
    }
}
