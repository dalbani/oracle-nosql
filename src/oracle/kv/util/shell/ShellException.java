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

package oracle.kv.util.shell;

import oracle.kv.impl.admin.CommandResult;
import oracle.kv.impl.admin.CommandResult.CommandFails;
import oracle.kv.util.ErrorMessage;

public class ShellException extends Exception {

    private static final long serialVersionUID = 1L;

    /* TODO: should be null? */
    private static final CommandResult DEFAULT_CMDFAILS =
        new CommandFails("Shell command error", ErrorMessage.NOSQL_5100,
                         CommandResult.NO_CLEANUP_JOBS);

    private final CommandResult cmdResult;

    private ShellException(String msg, CommandResult cmdResult) {
        super(msg);
        this.cmdResult = cmdResult;
    }

    public ShellException() {
        this.cmdResult = DEFAULT_CMDFAILS;
    }

    public ShellException(String msg) {
        this(msg, new CommandFails(msg, ErrorMessage.NOSQL_5100,
                                   CommandResult.NO_CLEANUP_JOBS));
    }

    public ShellException(String msg, Throwable cause) {
        super(msg, cause);
        this.cmdResult = new CommandFails(msg, ErrorMessage.NOSQL_5100,
                                          CommandResult.NO_CLEANUP_JOBS);
    }

    public ShellException(String msg,
                          ErrorMessage errorMsg,
                          String[] cleanupJobs) {
        this(msg, new CommandFails(msg, errorMsg, cleanupJobs));
    }

    public ShellException(String msg,
                          Throwable cause,
                          ErrorMessage errorMsg,
                          String[] cleanupJobs) {
        super(msg, cause);
        this.cmdResult = new CommandFails(msg, errorMsg, cleanupJobs);
    }

    public CommandResult getCommandResult() {
        return cmdResult;
    }
}
