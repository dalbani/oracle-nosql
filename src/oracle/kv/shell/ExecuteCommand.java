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
package oracle.kv.shell;

import java.rmi.RemoteException;

import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.StatementResult;
import oracle.kv.StatementResult.Kind;
import oracle.kv.impl.admin.client.CommandShell;
import oracle.kv.impl.query.shell.OnqlShell.OutputMode;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellCommand;
import oracle.kv.util.shell.ShellException;
import oracle.kv.util.shell.ShellUsageException;

/**
 * Implements the 'execute <ddl statement>' command.
 */
public class ExecuteCommand extends ShellCommand {

    /**
     * Prefix matching of 4 characters means 'execute' and 'exec' are supported.
     */
    public ExecuteCommand() {
        super("execute", 4);
    }

    /**
     * Usage: execute <ddl statement>
     */
    @Override
    public String execute(String[] args, Shell shell)
        throws ShellException {

        if (args.length != 2) {
            shell.badArgCount(this);
        }

        final String statement = args[1];

        /* Statement is empty */
        if (statement.length() == 0) {
            throw new ShellUsageException("Empty statement",  this);
        }

        CommandShell cmd = (CommandShell) shell;
        try {
            final KVStore store = cmd.getStore();
            final StatementResult result = store.executeSync(statement);
            return displayResults(shell, result);
        } catch (IllegalArgumentException iae) {
            String msg = String.format("%s\nUsage:\n\n%s\n",
                iae.getMessage(), CommandShell.getSQLSyntaxUsage(statement));
            throw new ShellException(msg, iae);
        } catch (FaultException fe) {
            if (fe.getCause() != null &&
                fe.getCause().getClass().equals(RemoteException.class)) {
                RemoteException re = (RemoteException) fe.getCause();
                cmd.noAdmin(re);
                return "failed";
            }
            throw new ShellException(fe.getMessage(), fe);
        }
    }

    private String displayResults(final Shell shell,
                                  final StatementResult result)
        throws ShellException {

        final CommandShell cmdShell = (CommandShell)shell;
        final Kind kind = result.getKind();
        switch(kind) {
        case DDL:
            return cmdShell.displayDDLResults(result);
        case QUERY:
            return cmdShell.displayDMLResults(OutputMode.COLUMN, result);
        default:
            break;
        }
        return null;
    }

    @Override
    protected String getCommandSyntax() {
        return name + " <statement>";
    }

    @Override
    protected String getCommandDescription() {
        return "Executes the specified statement synchronously. The statement"+
            eolt + "must be enclosed in single or double quotes.";
    }
}
