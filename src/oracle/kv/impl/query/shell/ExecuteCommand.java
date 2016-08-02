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
package oracle.kv.impl.query.shell;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import oracle.kv.Consistency;
import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.StatementResult;
import oracle.kv.StatementResult.Kind;
import oracle.kv.query.ExecuteOptions;
import oracle.kv.query.PreparedStatement;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellCommand;
import oracle.kv.util.shell.ShellException;

/* A command to execute SQL statement */
public class ExecuteCommand extends ShellCommand {

    private final static String NAME = "";

    public ExecuteCommand() {
        super(NAME, 0);
    }

    @Override
    public String execute(String[] args, Shell shell)
        throws ShellException {

    	final OnqlShell sqlShell = (OnqlShell)shell;
        final String statement = args[0];
        final KVStore store = sqlShell.getStore();
        final StatementResult result;
        try {
            final PreparedStatement ps = store.prepare(statement);
            result = store.executeSync(ps, getExecuteOptions(shell));
        } catch (IllegalArgumentException iae) {
            String msg = String.format("%s\nUsage:\n\n%s\n",
                                       iae.getMessage(),
                                       OnqlShell.getSQLSyntaxUsage(statement));
            throw new ShellException(msg, iae);
        } catch (FaultException fe) {
            throw new ShellException(fe.getMessage(), fe);
        }
        return displayResults(shell, statement, result);
    }

    private ExecuteOptions getExecuteOptions(Shell shell) {
        final OnqlShell sqlShell = ((OnqlShell)shell);
        final Consistency consistency = sqlShell.getStoreConsistency();
        final long timeout = sqlShell.getRequestTimeout();
        final ExecuteOptions executeOptions = new ExecuteOptions();
        executeOptions.setConsistency(consistency);
        executeOptions.setTimeout(timeout, TimeUnit.MILLISECONDS);
        return executeOptions;
    }

    private String displayResults(final Shell shell,
                                  final String statement,
                                  final StatementResult result)
        throws ShellException {

        final OnqlShell sqlShell = (OnqlShell)shell;
        final Kind kind = result.getKind();
        switch(kind) {
        case DDL:
            return sqlShell.displayDDLResults(result);
        case QUERY: {
            final PrintStream queryOutput = sqlShell.getQueryOutput();
            final boolean isPagingEnabled;
            final boolean isOutputFile = (queryOutput != sqlShell.getOutput());
            if (isOutputFile) {
                isPagingEnabled = false;
                queryOutput.println(createStatementComment(statement));
            } else {
                isPagingEnabled = sqlShell.isPagingEnabled();
            }

            final String ret =
                sqlShell.displayDMLResults(sqlShell.getQueryOutputMode(),
                                           result, isPagingEnabled,
                                           queryOutput);
            if (isOutputFile) {
                try {
                    ((OnqlShell)shell).flushOutput();
                } catch (IOException ignored) {
                }
            }
            return ret;
        }
        default:
            break;
        }
        return null;
    }

    private String createStatementComment(final String statement) {
        final String fmt = "%s%s";
        return String.format(fmt, Shell.COMMENT_MARK, statement.toUpperCase());
    }

    @Override
    public String getCommandSyntax() {
        return null;
    }

    @Override
    public String getCommandDescription() {
        return null;
    }
}
