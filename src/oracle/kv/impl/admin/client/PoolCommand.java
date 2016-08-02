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

package oracle.kv.impl.admin.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.kv.impl.admin.CommandResult;
import oracle.kv.impl.admin.CommandResult.CommandFails;
import oracle.kv.impl.admin.CommandResult.CommandSucceeds;
import oracle.kv.impl.admin.CommandServiceAPI;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.JsonUtils;
import oracle.kv.util.ErrorMessage;
import oracle.kv.util.shell.CommandWithSubs;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellException;

import org.codehaus.jackson.node.ObjectNode;

/*
 * Subcommands of pool
 *   create
 *   remove
 *   join
 */
class PoolCommand extends CommandWithSubs {
    public static final List<? extends SubCommand> subs =
                                       Arrays.asList(new CreatePool(),
                                                     new RemovePool(),
                                                     new JoinPool());
    private static final String POOL_COMMAND_NAME = "pool";

    PoolCommand() {
        super(subs, POOL_COMMAND_NAME, 3, 2);
    }

    @Override
    protected String getCommandOverview() {
        return "Encapsulates commands that manipulates Storage Node pools, " +
               "which are used for" + eol + "resource allocations.";
    }

    static class CreatePool extends SubCommand {

        CreatePool() {
            super("create", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            CommandShell cmd = (CommandShell)shell;
            CommandServiceAPI cs = cmd.getAdmin();
            String poolName = null;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if ("-name".equals(arg)) {
                    poolName = Shell.nextArg(args, i++, this);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }
            if (poolName == null) {
                shell.requiredArg("-name", this);
            }
            String msg = "";
            try {
                List<String> poolNames = cs.getStorageNodePoolNames();
                if (poolNames.indexOf(poolName) >= 0) {
                    msg = "Pool already exists: " + poolName;
                } else {
                    cs.addStorageNodePool(poolName);
                    shell.verboseOutput("Added pool " + poolName);
                }
            } catch (RemoteException re) {
                cmd.noAdmin(re);
            }
            if (shell.getJson()) {
                String operation = POOL_COMMAND_NAME + " " + getCommandName();
                ObjectNode returnValue = JsonUtils.createObjectNode();
                returnValue.put("pool_name", poolName);
                CommandResult cmdResult =
                    new CommandSucceeds(returnValue.toString());
                return Shell.toJsonReport(operation, cmdResult);
            }
            return msg;
        }

        @Override
        protected String getCommandSyntax() {
            return "pool create -name <name> [-json]";
        }

        @Override
        protected String getCommandDescription() {
            return
                "Creates a new Storage Node pool to be used for resource " +
                "distribution" + eolt + "when creating or modifying " +
                "a store.";
        }
    }

    static class RemovePool extends SubCommand {

        RemovePool() {
            super("remove", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            CommandShell cmd = (CommandShell)shell;
            CommandServiceAPI cs = cmd.getAdmin();
            String poolName = null;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if ("-name".equals(arg)) {
                    poolName = Shell.nextArg(args, i++, this);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }
            if (poolName == null) {
                shell.requiredArg("-name", this);
            }
            String msg = "";
            try {
                List<String> poolNames = cs.getStorageNodePoolNames();
                if (poolNames.indexOf(poolName) == -1) {
                    msg = "Pool does not exist: " + poolName;
                } else {
                    cs.removeStorageNodePool(poolName);
                    shell.verboseOutput("Removed pool " + poolName);
                }
            } catch (RemoteException re) {
                cmd.noAdmin(re);
            }
            if (shell.getJson()) {
                String operation = POOL_COMMAND_NAME + " " + getCommandName();
                ObjectNode returnValue = JsonUtils.createObjectNode();
                returnValue.put("pool_name", poolName);
                CommandResult cmdResult =
                    new CommandSucceeds(returnValue.toString());
                return Shell.toJsonReport(operation, cmdResult);
            }
            return msg;
        }

        @Override
        protected String getCommandSyntax() {
            return "pool remove -name <name> [-json]";
        }

        @Override
        protected String getCommandDescription() {
            return "Removes a Storage Node pool.";
        }
    }

    static class JoinPool extends SubCommand {

        JoinPool() {
            super("join", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            CommandShell cmd = (CommandShell)shell;
            CommandServiceAPI cs = cmd.getAdmin();
            String poolName = null;
            List<String> storageNodes = new ArrayList<String>();
            List<StorageNodeId> snids = new ArrayList<StorageNodeId>();
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if ("-name".equals(arg)) {
                    poolName = Shell.nextArg(args, i++, this);
                } else if ("-sn".equals(arg)) {
                    String sn = Shell.nextArg(args, i++, this);
                    try {
                        StorageNodeId snid = StorageNodeId.parse(sn);
                        snids.add(snid);
                    } catch (IllegalArgumentException iae) {
                        invalidArgument(sn);
                    }
                    storageNodes.add(sn);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }
            if (poolName == null) {
                shell.requiredArg("-name", this);
            }
            if (storageNodes.size() == 0) {
                shell.requiredArg("-sn", this);
            }
            try {
                final String operation = POOL_COMMAND_NAME + " " +
                    getCommandName();
                String msg = "";
                List<String> poolNames = cs.getStorageNodePoolNames();
                if (poolNames.indexOf(poolName) == -1) {
                    msg = "Pool does not exist: " + poolName;
                    exitCode = Shell.EXIT_UNKNOWN;
                    if (shell.getJson()) {
                        CommandResult cmdResult =
                            new CommandFails(msg, ErrorMessage.NOSQL_5200,
                                             CommandResult.NO_CLEANUP_JOBS);
                        return Shell.toJsonReport(operation, cmdResult);
                    }
                    return msg;
                }
                Topology t = cs.getTopology();

                /*
                 * If an unknown StorageNode is found, all earlier
                 * additions will still have worked.
                 */
                for (StorageNodeId snid : snids) {
                    if (t.get(snid) == null) {
                        msg = "Storage Node does not exist: " + snid;
                        exitCode = Shell.EXIT_UNKNOWN;
                        if (shell.getJson()) {
                            CommandResult cmdResult =
                                new CommandFails(msg, ErrorMessage.NOSQL_5200,
                                                 CommandResult.NO_CLEANUP_JOBS);
                            return Shell.toJsonReport(operation, cmdResult);
                        }
                        return msg;
                    }
                    cs.addStorageNodeToPool(poolName, snid);
                }

                if (shell.getJson()) {
                    ObjectNode returnValue = JsonUtils.createObjectNode();
                    returnValue.put("pool_name", poolName);
                    CommandSucceeds cmdResult =
                        new CommandSucceeds(returnValue.toString());
                    return Shell.toJsonReport(operation, cmdResult);
                }
                return "Added Storage Node(s) " + snids + " to pool " +
                       poolName;
            } catch (RemoteException re) {
                cmd.noAdmin(re);
            }
            return "";
        }

        @Override
        protected String getCommandSyntax() {
            return "pool join -name <name> [-sn <snX>]* [-json]";
        }

        @Override
        protected String getCommandDescription() {
            return "Adds Storage Nodes to an existing Storage Node pool.";
        }
    }
}
