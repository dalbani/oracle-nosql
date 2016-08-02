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
 * A StatementResult provides information about the execution and outcome of a
 * table statement. If obtained via {@link ExecutionFuture#updateStatus}, it can
 * represent information about either a completed or in progress operation. If
 * obtained via {@link ExecutionFuture#get} or {@link KVStore#executeSync}, it
 * represents the final status of a finished operation.
 *
 * @since 3.2
 */
public interface StatementResult {

    /**
     * Returns the administrative plan id for this operation if the statement
     * was a create or remove table, a create or remove index, or an alter
     * index. When using the Admin CLI (runadmin) utility, administrative
     * operations are identified by plan id. The plan id can be used to
     * correlate data definition and administrative statements issued
     * programmatically using the API against operations viewed via the
     * interactive Admin CLI or other monitoring tool.
     * <p>
     * The plan id will be 0 if this statement was not an administrative
     * operation, or did not require execution.
     */
    public int getPlanId();

    /**
     * Returns information about the execution of the statement, in human
     * readable form. If the statement was a data definition command, the
     * information will show the start and end time of the operation and
     * details about server side processing.
     */
    public String getInfo();

    /**
     * Returns the same information as {@link #getInfo}, in JSON format.
     * Several possible formats are returned, depending on the statement. The
     * format of a data definition command which requires server side
     * processing is as follows:
     * <pre>
     * {
     *   "version" : "2",
     *   "planInfo" : {
     *     "id" : 6,
     *     "name" : "CreateIndex:users:LastName",
     *     "isDone" : true,
     *     "state" : "SUCCEEDED",
     *     "start" : "2014-10-29 18:41:12 UTC",
     *     "interrupted" : null,
     *     "end" : "2014-10-29 18:41:12 UTC",
     *     "error" : null,
     *     "executionDetails" : {
     *       "taskCounts" : {
     *         "total" : 3,
     *         "successful" : 3,
     *         "failed" : 0,
     *         "interrupted" : 0,
     *         "incomplete" : 0,
     *         "notStarted" : 0
     *       },
     *       "finished" : [ {
     *         "taskNum" : 1,
     *         "name" : "StartAddIndex:users:LastName",
     *         "state" : "SUCCEEDED",
     *         "start" : "2014-10-29 18:41:12 UTC",
     *         "end" : "2014-10-29 18:41:12 UTC"
     *       }, {
     *         "taskNum" : 2,
     *         "name" : "WaitForAddIndex:users:LastName",
     *         "state" : "SUCCEEDED",
     *         "start" : "2014-10-29 18:41:12 UTC",
     *         "end" : "2014-10-29 18:41:12 UTC"
     *       }, {
     *         "taskNum" : 3,
     *         "name" : "CompleteAddIndex:users:LastName",
     *         "state" : "SUCCEEDED",
     *         "start" : "2014-10-29 18:41:12 UTC",
     *         "end" : "2014-10-29 18:41:12 UTC"
     *       } ],
     *       "running" : [ ],
     *       "pending" : [ ]
     *     }
     *   }
     * }
     * </pre>
     */
    public String getInfoAsJson();

    /**
     * If {@link #isSuccessful} is false, return a description of the
     * problem. Will be null if {@link #isSuccessful} is true.
     */
    public String getErrorMessage();

    /**
     * Returns true if this statement has finished and was successful.
     */
    boolean isSuccessful();

    /**
     * Returns true if the statement completed. This is the equivalent of
     * {@link ExecutionFuture#isDone}
     */
    boolean isDone();

    /**
     * Returns true if the statement had been cancelled. This is the equivalent
     * of {@link ExecutionFuture#isCancelled}
     *
     * @see ExecutionFuture#cancel
     */
    boolean isCancelled();

    /**
     * Returns the output of a DDL statement that generates results, such as
     * SHOW TABLES, SHOW AS JSON TABLES, DESCRIBE TABLE, or DESCRIBE AS JSON
     * TABLE. The output of a DESCRIBE AS JSON TABLES command is:
     * <pre>
     * {
     *     "type" : "table",
     *     "name" : "users",
     *     "comment" : null,
     *     "shardKey" : [ "id" ],
     *     "primaryKey" : [ "id" ],
     *     "fields" : [ {
     *       "name" : "id",
     *       "type" : "INTEGER",
     *       "nullable" : true,
     *       "default" : null
     *     }, {
     *       "name" : "firstName",
     *       "type" : "STRING",
     *       "nullable" : true,
     *       "default" : null
     *     }, {
     *       "name" : "lastName",
     *       "type" : "STRING",
     *       "nullable" : true,
     *       "default" : null
     *     }, {
     *       "name" : "age",
     *       "type" : "INTEGER",
     *       "nullable" : true,
     *       "default" : null
     *     } ],
     *     "indexes" : [ {
     *       "name" : "LastName",
     *       "comment" : null,
     *       "fields" : [ "lastName" ]
     *     } ]
     *   }
     * }
     * </pre>
     * The output of a SHOW AS JSON TABLES command is:
     * <pre>
     * {"tables" : ["users"]}
     * </pre>
     * @since 3.3
     */
    String getResult();

}
