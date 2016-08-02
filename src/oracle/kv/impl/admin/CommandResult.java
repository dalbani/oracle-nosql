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

package oracle.kv.impl.admin;

import java.io.Serializable;

import oracle.kv.impl.fault.CommandFaultException;
import oracle.kv.util.ErrorMessage;

/**
 * CommandResult describes the information about a command execution result,
 * which contains a return value, a description of the result, an error code,
 * and cleanup jobs used to remove issues causing execution failure.
 * <p>
 * Note that we need to avoid directly persisting the enum type of ErrorMessage
 * in a CommandResult instance. That is because when being deserialized in
 * old version code, any newly added ErrorMessage value will cause
 * compatibility issue since it cannot be recognized. Persisting the int type
 * error code of the corresponding ErrorMessage can help solve this problem.
 */
public interface CommandResult {

    public static final String[] NO_CLEANUP_JOBS = {};
    public static final String[] STORE_CLEANUP = {"store_clean.kvs"};
    public static final String[] TOPO_REPAIR = {"plan repair-topology"};
    public static final String[] PLAN_CANCEL = {"plan_cancel_retry.kvs"};
    public static final String[] TOPO_PLAN_REPAIR =
        {"plan repair-topology", "plan_cancel_retry.kvs"};

    /**
     * Returned value of command execution, may be null if the command fails
     */
    public String getReturnValue();

    /**
     * Returns a detailed description of this execution
     */
    public String getDescription();

    /**
     * Returns the standard NoSQL error code
     */
    public int getErrorCode();

    /**
     * Returns the suggested cleanup jobs.  The cleanup jobs are in the form
     * of an array of executable commands. If no cleanup job is available, or
     * the command ends successfully, null will be returned.
     */
    public String[] getCleanupJobs();


    public static class CommandFails implements CommandResult, Serializable {

        private static final long serialVersionUID = 1L;

        private final String description;
        private final int errorCode;
        private final String[] cleanupJobs;

        public CommandFails(String description,
                            ErrorMessage errorMsg,
                            String[] cleanupJobs) {
            this.description = description;
            this.errorCode = errorMsg.getValue();
            this.cleanupJobs = cleanupJobs;
        }

        public CommandFails(CommandFaultException cfe) {
            this(cfe.getDescription(), cfe.getErrorMessage(),
                 cfe.getCleanupJobs());
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int getErrorCode() {
            return errorCode;
        }

        @Override
        public String[] getCleanupJobs() {
            return cleanupJobs;
        }

        @Override
        public String getReturnValue() {
            return null;
        }
    }

    public static class CommandSucceeds
        implements CommandResult, Serializable {

        private static final long serialVersionUID = 1L;

        private final static String SUCCESS_MSG =
            "Operation ends successfully";
        private final String returnValue;

        public CommandSucceeds(String returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public String getReturnValue() {
            return returnValue;
        }

        @Override
        public String getDescription() {
            return SUCCESS_MSG;
        }

        @Override
        public int getErrorCode() {
            return ErrorMessage.NOSQL_5000.getValue();
        }

        @Override
        public String[] getCleanupJobs() {
            return null;
        }
    }

    public static class CommandWarns
        implements CommandResult, Serializable {

        private static final long serialVersionUID = 1L;

        private final String description;
        private final String returnValue;

        public CommandWarns(String description, String returnValue) {
            this.description = description;
            this.returnValue = returnValue;
        }

        @Override
        public String getReturnValue() {
            return returnValue;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int getErrorCode() {
            return ErrorMessage.NOSQL_5000.getValue();
        }

        @Override
        public String[] getCleanupJobs() {
            return null;
        }
    }
}
