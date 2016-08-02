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

package oracle.kv.util.shell;

import java.util.List;

import oracle.kv.impl.util.CommandParser;
import oracle.kv.util.shell.CommandWithSubs;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellException;

import static oracle.kv.impl.util.CommandParser.COMMAND_FLAG;
import static oracle.kv.impl.util.CommandParser.LAST_FLAG;

/*
 * A base class for show commands and the sub commands.
 */
public class ShowCommandBase extends CommandWithSubs {

    final static String COMMAND = "show";

    final static String OVERVIEW = "Encapsulates commands that display the " +
        "state of the store and its components.";

    public ShowCommandBase(List<? extends SubCommand> subs) {
        super(subs, COMMAND, 2, 2);
    }

    @Override
    protected String getCommandOverview() {
        return OVERVIEW;
    }

    public static final class ShowFaults extends SubCommand {

        final static String NAME = "faults";
        private final static String COMMAND_DESC = COMMAND_FLAG + " <index>";

        final static String SYNTAX = COMMAND + " " + NAME + " " +
            CommandParser.optional(LAST_FLAG) + " " +
            CommandParser.optional(COMMAND_DESC);

        final static String DESCRIPTION =
            "Displays faulting commands.  By default all available " +
            "faulting commands" + eolt + "are displayed.  Individual " +
            "fault details can be displayed using the" + eolt +
            LAST_FLAG + " and " + COMMAND_FLAG + " flags.";

        public ShowFaults() {
            super(NAME, 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            final Shell.CommandHistory history = shell.getHistory();
            final int from = 0;
            final int to = history.getSize();

            if (args.length > 1) {
                final String arg = args[1];
                if (LAST_FLAG.equals(arg)) {
                    return history.dumpLastFault();
                } else if (COMMAND_FLAG.equals(arg)) {
                    final String faultString = Shell.nextArg(args, 1, this);
                    int fault;
                    try {
                        fault = Integer.parseInt(faultString);
                        /*
                         * The index of command are shown as 1-based index in
                         * output, so covert it to 0-based index when locating
                         * it in CommandHistory list.
                         */
                        int idxFault = toZeroBasedIndex(fault);
                        if (idxFault < 0 || idxFault >= history.getSize()) {
                            return "Index out of range: " + fault + "" + eolt +
                                   getBriefHelp();
                        }
                        if (history.commandFaulted(idxFault)) {
                            return history.dumpCommand(idxFault, true);
                        }
                        return "Command " + fault + " did not fault";
                    } catch (IllegalArgumentException e) {
                        invalidArgument(faultString);
                    }
                } else {
                    shell.unknownArgument(arg, this);
                }
            }
            return history.dumpFaultingCommands(from, to);
        }

        private int toZeroBasedIndex(int index) {
            return (index > 0) ? (index - 1) : 0;
        }

        @Override
        protected String getCommandSyntax() {
            return SYNTAX;
        }

        @Override
        protected String getCommandDescription() {
            return DESCRIPTION;
        }
    }
}
