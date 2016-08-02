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
import java.util.Arrays;
import java.util.List;

import oracle.kv.impl.admin.CommandServiceAPI;
import oracle.kv.impl.admin.Snapshot;
import oracle.kv.impl.topo.Datacenter;
import oracle.kv.impl.topo.DatacenterId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.util.shell.CommandWithSubs;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellException;
import oracle.kv.util.shell.ShellUsageException;

/*
 * Subcommands of snapshot
 *   create
 *   remove
 */
class SnapshotCommand extends CommandWithSubs {
    private static final
        List<? extends SubCommand> subs =
                       Arrays.asList(new CreateSnapshotSub(),
                                     new RemoveSnapshotSub());

    SnapshotCommand() {
        super(subs, "snapshot", 3, 2);
    }

    @Override
    protected String getCommandOverview() {
        return "The snapshot command encapsulates commands that create and " +
            "delete snapshots," + eol + "which are used for backup and " +
            "restore.";
    }

    static class CreateSnapshotSub extends SnapshotSub {

        CreateSnapshotSub() {
            super("create", 3, true);
        }

        @Override
        protected String getCommandSyntax() {
            return "snapshot create -name <name> [-zn <id> | -znname <name>]";
        }

        @Override
        protected String getCommandDescription() {
            return
                "Creates a new snapshot using the specified name as " +
                "the prefix. If a zone with the specified id or name is " +
                "specified then the command applies to all the SNs " +
                "executing in that zone";
        }
    }

    static class RemoveSnapshotSub extends SnapshotSub {

        RemoveSnapshotSub() {
            super("remove", 3, false);
        }

        @Override
        protected String getCommandSyntax() {
            return "snapshot remove {-name <name> | -all} [-zn <id> |" +
                   " -znname <name>]";
        }

        @Override
        protected String getCommandDescription() {
            return
                "Removes the named snapshot.  If -all is specified " +
                "remove all snapshots. If a zone with the specified id or " +
                "name is specified then the command applies to all the SNs " +
                "executing in that zone";
        }
    }

    abstract static class SnapshotSub extends SubCommand {
        final boolean isCreate;
        /**
         * The total number of snapshot operations. Includes both the success
         * and failure operations. Used for testing purpose.
         */
        private int numOfOperations;
        protected SnapshotSub(String name, int prefixMatchLength,
                              boolean isCreate) {
            super(name, prefixMatchLength);
            this.isCreate = isCreate;
        }

        /**
         * Return the total number of snapshot operations. Used for testing
         * purpose.
         */
        public int getNumOfOperations() {
            return numOfOperations;
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            CommandShell cmd = (CommandShell)shell;
            CommandServiceAPI cs = cmd.getAdmin();
            final String cannotMixMsg = "Use either -zn or -znname";
            String snapName = null;
            String zoneId = null;
            String zoneName = null;
            boolean removeAll = false;
            String zoneInfo = "";

            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if ("-name".equals(arg)) {
                    snapName = Shell.nextArg(args, i++, this);
                } else if ("-all".equals(arg)) {
                    removeAll = true;
                } else if ("-zn".equals(arg)) {
                    zoneId = Shell.nextArg(args, i++, this);
                    if (zoneName != null) {
                        throw new ShellUsageException(cannotMixMsg, this);
                    }
                /* Parse -zname because it was released by accident */
                } else if ("-zname".equals(arg) || "-znname".equals(arg)) {
                    zoneName = Shell.nextArg(args, i++, this);
                    if (zoneId != null) {
                        throw new ShellUsageException(cannotMixMsg, this);
                    }
                }
                else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (snapName == null && !removeAll) {
                shell.requiredArg("-name", this);
            }

            try {
                Snapshot snapshot =
                    new Snapshot(cs, shell.getVerbose(), shell.getOutput());
                String output = "";
                final Topology topology = cs.getTopology();
                DatacenterId dcId = null;
                if (zoneId != null) {
                    dcId = DatacenterId.parse(zoneId);
                    Datacenter dc = topology.get(dcId);
                    if (dc == null) {
                        throw new IllegalArgumentException(
                            "The specified zone id does not exist");
                    }
                    zoneInfo += "zn:[id=" + zoneId + " name=" + dc.getName() +
                                "]";
                }
                if (zoneName != null) {
                    Datacenter zone = topology.getDatacenter(zoneName);
                    if (zone == null) {
                        throw new IllegalArgumentException(
                            "The specified zone name does not exist");
                    }
                    dcId = zone.getResourceId();
                    zoneInfo += "zn:[id=" + dcId.getDatacenterId() +
                                " name=" + zoneName + "]";
                }
                if (isCreate) {
                    if (removeAll) {
                        invalidArgument("-all");
                    }
                    String newSnapName = null;
                    if (dcId != null) {
                        newSnapName = snapshot.createSnapshot(snapName, dcId);
                    } else {
                        newSnapName = snapshot.createSnapshot(snapName);
                    }
                    if (snapshot.succeeded()) {
                        int numSuccess = snapshot.getSuccesses().size();
                        output = "Created snapshot named " + newSnapName +
                            " on " + "all " + numSuccess + " nodes";
                        if (dcId != null) {
                            output += " in zone " + zoneInfo;
                        }
                    } else if (snapshot.getQuorumSucceeded()) {
                        output =
                            "Create snapshot succeeded but not on all nodes" +
                            eol;
                        if (dcId != null) {
                            output = "Create snapshot succeeded but not on " +
                                    "all nodes in zone " + zoneInfo + eol;
                        }
                    }
                } else {
                    if (removeAll) {
                        if (snapName != null) {
                            invalidArgument("-all");
                        }
                        if (dcId != null) {
                            snapshot.removeAllSnapshots(dcId);
                        } else {
                            snapshot.removeAllSnapshots();
                        }
                        if (snapshot.succeeded()) {
                            output = "Removed all snapshots";
                            if (dcId != null) {
                                output += " in zone " + zoneInfo;
                            }
                        }
                    } else {
                        if (dcId != null) {
                            snapshot.removeSnapshot(snapName, dcId);
                        } else {
                            snapshot.removeSnapshot(snapName);
                        }
                        if (snapshot.succeeded()) {
                            output = "Removed snapshot " + snapName;
                            if (dcId != null) {
                                output += " in zone " + zoneInfo;
                            }
                        }
                    }
                }
                numOfOperations = snapshot.getFailures().size()
                        + snapshot.getSuccesses().size();
                return output;
            } catch (RemoteException re) {
                cmd.noAdmin(re);
            } catch (Exception e) {
                shell.handleUnknownException("Snapshot " + snapName +
                                             " failed", e);
            }
            return "";
        }
    }
}
