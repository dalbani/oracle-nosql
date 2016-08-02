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

package oracle.kv.impl.util;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import oracle.kv.KVVersion;
import oracle.kv.impl.diagnostic.DiagnosticCollectCommand;
import oracle.kv.impl.diagnostic.DiagnosticSetupCommand;
import oracle.kv.impl.diagnostic.DiagnosticVerifyCommand;
import oracle.kv.impl.diagnostic.ssh.SSHClientManager;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellCommand;
import oracle.kv.util.shell.ShellException;

/**
 * To implement a new command:
 * 1.  Implement a class that extends ShellCommand.
 * 2.  Add it to the static list, commands, in this class.
 *
 * Commands that have subcommands should extend SubCommand.  See one of the
 * existing classes for example code (e.g. DiagnosticCollectCommand).
 */
public class DiagnosticShell extends Shell {

    public static final String COMMAND_NAME = "diagnostics";
    public static final String COMMAND_DESC =
        "runs the diagnostics command line interface";
    public static final String COMMAND_ARGS = "(setup|collect|" + eol +
            "verify)[args]";

    private boolean noprompt = false;
    private String[] commandToRun;
    private int nextCommandIdx = 0;

    private DiagnosticParser parser;

    static final String prompt = "diagnostics-> ";
    static final String usageHeader =
        "Oracle NoSQL Database Diagnostic Utility Commands:" + eol;
    static final String versionString = " (" +
        KVVersion.CURRENT_VERSION.getNumericVersionString() + ")";

    /*
     * The list of commands available. List setup first, since that's the
     * first required step, then collect.
     */
    private static List<? extends ShellCommand> commands =
                       Arrays.asList(new DiagnosticSetupCommand(),
                                     new DiagnosticCollectCommand(),
                                     new DiagnosticVerifyCommand(),
                                     new Shell.ExitCommand(),
                                     new Shell.HelpCommand()
                                     );

    public DiagnosticShell(InputStream input, PrintStream output) {
        super(input, output);
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
        /* Clear all SSH Clients */
        SSHClientManager.clearClients();
    }

    @Override
    public List<? extends ShellCommand> getCommands() {
        return commands;
    }

    @Override
    public String getPrompt() {
        return noprompt ? null : prompt;
    }

    @Override
    public String getUsageHeader() {
        return usageHeader;
    }

    /**
     * If retry is true, return that, but be sure to reset the value
     */
    @Override
    public boolean doRetry() {
        return false;
    }

    public void start() {
        init();
        if (commandToRun != null) {
            try {
                run(commandToRun[0], commandToRun);
            } catch (ShellException se) {
                handleShellException(commandToRun[0], se);
            } catch (Exception e) {
                handleUnknownException(commandToRun[0], e);
            }
        } else {
            loop();
        }
        shutdown();
    }

    private class DiagnosticParser extends CommandParser {

        private DiagnosticParser(String[] args) {
            /*
             * The true argument tells CommandParser that this class will
             * handle all flags, not just those unrecognized.
             */
            super(args, true);
        }

        @Override
        protected void verifyArgs() {
            if ((commandToRun != null) &&
                (nextCommandIdx < commandToRun.length)) {
                usage("Flags may not follow commands");
            }
        }

        @Override
        public void usage(String errorMsg) {
            if (errorMsg != null) {
                System.err.println(errorMsg);
            }
            System.err.println(KVSTORE_USAGE_PREFIX + COMMAND_NAME + eolt +
                   COMMAND_ARGS);
            System.exit(1);
        }

        @Override
        protected boolean checkArg(String arg) {
            if (NOPROMPT_FLAG.equals(arg)) {
                noprompt = true;
                return true;
            }
            addToCommand(arg);
            return true;
        }

        /*
         * Add unrecognized args to the commandToRun array.
         */
        private void addToCommand(String arg) {
            if (commandToRun == null) {
                commandToRun = new String[getNRemainingArgs() + 1];
            }
            commandToRun[nextCommandIdx++] = arg;
        }
    }


    public void parseArgs(String args[]) {
        parser = new DiagnosticParser(args);
        parser.parseArgs();
    }

    public static void main(String[] args) {
        DiagnosticShell shell = new DiagnosticShell(System.in, System.out);
        shell.parseArgs(args);
        shell.start();
        if (shell.getExitCode() != EXIT_OK) {
            System.exit(shell.getExitCode());
        }
    }
}
