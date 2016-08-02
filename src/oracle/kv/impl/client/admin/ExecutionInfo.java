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
package oracle.kv.impl.client.admin;


/**
 * ExecutionInfo describes the information about ddl command execution passed
 * between the Admin service and a client.
 */
public interface ExecutionInfo {

    /**
     * @return a version number describing the format of this planInfo.
     */
    public int getVersion();

    /**
     * @return the id of the target plan, if a plan was used to execute the
     * operation. May be 0 if no plan was required.
     */
    public int getPlanId();

    /**
     * @return true if the operation is no longer running. Encompasses both
     * success and failure cases.
     */
    public boolean isTerminated();

    /**
     * @return a JSON formatted detailed history of plan execution to date.
     * It's a plan history, returned in a structured format so that the client
     * can take actions based on particular plan status, or some aspect of
     * the plan execution. The information is returned as JSON rather than as
     * a Java class to reduce the demands on having Java on the client side.
     */
    public String getJSONInfo();

    /**
     * @return a text version of the plan execution status, formatted for
     * human readability.
     */
    public String getInfo();

    /**
     * @return true if the command succeeded.
     */
    public boolean isSuccess();

    /**
     * If the operation was not successful, there should be a non-null error
     * message.
     */
    public String getErrorMessage();

    /**
     * @return true if the command was cancelled.
     */
    public boolean isCancelled();

    /**
     * Return true if the operation is in such a state that it needs to be
     * explicitly cancelled.
     */
    public boolean needsTermination();

    /**
     * Return results for statements that are essentially read operations,
     * such as show tables and describe table, and read operation DML
     */
    public String getResult();
}
