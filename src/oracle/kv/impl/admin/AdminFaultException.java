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

import java.io.IOException;

import oracle.kv.impl.fault.CommandFaultException;
import oracle.kv.impl.fault.InternalFaultException;
import oracle.kv.impl.admin.CommandResult.CommandFails;
import oracle.kv.util.ErrorMessage;

/**
 * Subclass of InternalFaultException used to indicate that the fault
 * originated in the Admin service when satisfying an internal request. Also,
 * it helps to generate a result for admin CLI command execution in the case
 * of any exception happens.
 */
public class AdminFaultException extends InternalFaultException {

    private static final long serialVersionUID = 1L;

    private static final ErrorMessage DEFAULT_ERR_MSG =
        ErrorMessage.NOSQL_5100;

    private /* final */ CommandResult cmdResult;

    public AdminFaultException(Throwable cause) {
        /* uses default command result with error code of 5100 */
        // TODO: should default cmd result be null?
        this(cause, new CommandFails(cause.getMessage(),
                                     DEFAULT_ERR_MSG,
                                     CommandResult.NO_CLEANUP_JOBS));
    }

    public AdminFaultException(Throwable cause,
                               String description,
                               ErrorMessage errorMsg,
                               String[] cleanupJobs) {

        this(cause, new CommandFails(description, errorMsg, cleanupJobs));
    }

    /**
     * Constructs an AdminFaultException with the specified cause as well as
     * the command result describing the cause.
     */
    private AdminFaultException(Throwable cause, CommandResult cmdResult) {
        super(cause);
        this.cmdResult = cmdResult;
    }

    /**
     * Returns the command result describing the cause
     */
    public CommandResult getCommandResult() {
        return cmdResult;
    }

    private void readObject(java.io.ObjectInputStream in)
        throws ClassNotFoundException, IOException {
        in.defaultReadObject();

        if (cmdResult == null) {
            cmdResult = new CommandFails(getMessage(),
                                         DEFAULT_ERR_MSG,
                                         CommandResult.NO_CLEANUP_JOBS);
        }
    }

    /**
     * Wraps a CommandFaultException to an AdminFaultException. If the
     * CommandFaultException has a wrapped cause in it, the cause will be
     * extracted and wrapped to the AdminFaultException so as that old version
     * callers are able to recognize the cause correctly. If the
     * CommandFaultException is an IllegalCommandException or has no cause, it
     * will be wrapped directly.
     * <p>
     * A command result will be generated and set to the new
     * AdminFaultException based on the information included in the
     * CommandFaultException.
     *
     * @param cfe the CommandFaultException to wrap
     * @return an AdminFaultException
     */
    public static AdminFaultException
        wrapCommandFault(CommandFaultException cfe) {

        final CommandResult cmdResult = new CommandFails(cfe);
        if (cfe instanceof IllegalCommandException ||
            cfe.getCause() == null) {
            return new AdminFaultException(cfe, cmdResult);
        }
        return new AdminFaultException(cfe.getCause(), cmdResult);
    }
}
