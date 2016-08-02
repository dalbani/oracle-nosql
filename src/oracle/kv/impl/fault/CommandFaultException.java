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

package oracle.kv.impl.fault;

import oracle.kv.util.ErrorMessage;

/**
 * This class provides a way to specify necessary error information to
 * construct a CommandResult, which will be used to generate a standard NoSQL
 * error report to users. It can be directly used as an exception with the error
 * information, or used to wrap another exception with specified error
 * information. Mainly used on server side.
 * <p>
 * For example, to attach error information to an IllegalStateException, the
 * below code can be followed:
 * ...
 *  } catch (IllegalStateException ise) {
 *      throw new CommandFaultException("Encountered IllegalStateException: " +
 *                                      ise.getMessage(),
 *                                      ise,
 *                                      ErrorMessage.NOSQL_5500,
 *                                      null );
 * }
 */
public class CommandFaultException extends OperationFaultException {

    private static final long serialVersionUID = 1L;

    private final ErrorMessage errorMsg;
    private final String[] cleanupJobs;

    /**
     * Ctor. Message of this exception will be used as the description in a
     * CommandResult.
     */
    public CommandFaultException(String msg,
                                 Throwable fault,
                                 ErrorMessage errorMsg,
                                 String[] cleanupJobs) {
        this(msg, errorMsg, cleanupJobs);
        initCause(fault);
    }

    public CommandFaultException(String msg,
                                 ErrorMessage errorMsg,
                                 String[] cleanupJobs) {
        super(msg);
        this.errorMsg = errorMsg;
        this.cleanupJobs = cleanupJobs;
    }

    public String[] getCleanupJobs() {
        return cleanupJobs;
    }

    public ErrorMessage getErrorMessage() {
        return errorMsg;
    }

    public String getDescription() {
        return getMessage();
    }
}
