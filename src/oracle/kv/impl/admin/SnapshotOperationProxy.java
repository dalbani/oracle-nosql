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

package oracle.kv.impl.admin;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import oracle.kv.impl.admin.Snapshot.SnapResult;
import oracle.kv.impl.admin.Snapshot.SnapshotOperation;
import oracle.kv.impl.fault.InternalFaultException;
import oracle.kv.impl.sna.StorageNodeAgentAPI;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.RepNodeId;
import oracle.kv.impl.topo.ResourceId;
import oracle.kv.impl.topo.StorageNode;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.registry.RegistryUtils;

/**
 * When security is enabled clients cannot directly invoke methods on the SNA
 * because SNA methods are internal used only in a secure configuration. This
 * class act as a proxy in this case. It is designed to reside on an internal
 * KVStore component (e.g., Admin), and helps to perform snapshot operations on
 * SNA with the component's InternalLoginManager.
 */
public class SnapshotOperationProxy {

    private final Topology topo;
    private final RegistryUtils ru;
    private final Logger logger;

    public SnapshotOperationProxy(final Admin admin) {
        assert admin != null;

        this.topo = admin.getCurrentTopology();
        this.ru = new RegistryUtils(topo, admin.getLoginManager());
        this.logger = admin.getLogger();
    }

    /**
     * Returns a specific Storage Node Agent.
     */
    private StorageNodeAgentAPI getStorageNodeAgent(StorageNodeId snid) {
        Exception e = null;
        logger.fine("Trying to contact storage node " + snid +
                    " for snapshot operation");
        try {
            return ru.getStorageNodeAgent(snid);
        } catch (ConnectException ce) {
            e = ce;
        } catch (RemoteException re) {
            e = re;
        } catch (NotBoundException nbe) {
            e = nbe;
        }
        final String errMsg =
            "Cannot contact storage node " + snid + ": " + e.getMessage();
        logger.fine(errMsg);
        throw new SNAUnavailableException(errMsg);
    }

    /**
     * Returns any Storage Node Agent.
     */
    private StorageNodeAgentAPI getStorageNodeAgent() {
        SNAUnavailableException e = null;
        final List<StorageNode> storageNodes = topo.getSortedStorageNodes();
        StorageNodeAgentAPI snai = null;
        for (StorageNode sn : storageNodes) {
            try {
                snai = getStorageNodeAgent(sn.getResourceId());
                logger.info("Snapshot operation using storage node: " +
                            sn.getResourceId());
                break;
            } catch (SNAUnavailableException snaue) {
                e = snaue;
            }
        }
        if (snai == null && e != null) {
            throw e;
        }
        return snai;
    }

    /**
     * Returns an array of names of snapshots. If no storage node id is
     * specified, this will choose an arbitrary storage node and ask it for its
     * list under the assumption that each SN should have the same snapshots.
     * Try all storage nodes until an available one is found.
     *
     * @param snid id of the storage node. If null, an arbitrary storage node
     * will be chosen.
     * @return an array of snapshot names
     */
    public String[] listSnapshots(StorageNodeId snid) {
        /*
         * If storage node is chosen arbitrarily, its id will have been logged
         * in getStorageNodeAgent() before, so we can make it empty here.
         */
        final String snStr = (snid != null) ? snid.toString() : "";
        logger.info("List snapshots from storage node " + snStr);

        try {
            /* Get an arbitrary storage node if snid is null */
            final StorageNodeAgentAPI snai = (snid == null) ?
                                             getStorageNodeAgent() :
                                             getStorageNodeAgent(snid);
            return snai.listSnapshots();
        } catch (Exception e) {
            logger.info("Failed to list shapshots from storage node " + snStr +
                        ": " + e);
            throw new IllegalCommandException(
                "Cannot list snapshots from storage node " + snStr + ": " +
                e.getMessage(), e);
        }
    }

    /**
     * Helps to call the snapshot methods on SNA according to specified
     * operation types and storage node id. The result of operation will be
     * encapsulated and returned as a {@link SnapResult} instance.
     *
     * @param sop snapshot operation
     * @param snid id of storage node on which the snapshot will be done
     * @param rid the resource that is the snapshot target, a RepNodeId or
     * AdminId
     * @param sname name of snapshot
     * @return operation result as a SnapResult instance
     */
    public SnapResult executeSnapshotOp(SnapshotOperation sop,
                                        StorageNodeId snid,
                                        ResourceId rid,
                                        String sname) {
        try {
            final StorageNodeAgentAPI snai = getStorageNodeAgent(snid);
            if (sop == SnapshotOperation.CREATE) {
                if (rid instanceof RepNodeId) {
                    snai.createSnapshot((RepNodeId) rid, sname);
                } else {
                    snai.createSnapshot((AdminId) rid, sname);
                }
            } else if (sop == SnapshotOperation.REMOVE) {
                if (rid instanceof RepNodeId) {
                    snai.removeSnapshot((RepNodeId) rid, sname);
                } else {
                    snai.removeSnapshot((AdminId) rid, sname);
                }
            } else {
                if (rid instanceof RepNodeId) {
                    snai.removeAllSnapshots((RepNodeId) rid);
                } else {
                    snai.removeAllSnapshots((AdminId) rid);
                }
            }
            logger.info("Snapshot operation " + sop + " of " + sname + " on " +
                        snid + " for " + rid + " succeeded.");
            return new SnapResult(sop, true, rid, null, "Succeeded");
        } catch (Exception e) {
            logger.info("Snapshot operation " + sop + " of " + sname +
                        " failed on " + snid + " for " + rid + ": " +
                        e.getMessage());

            /*
             * The exception may not be serializable, so we wrap it in a
             * SnapshotFaultException.
             */
            return new SnapResult(sop, false, rid,
                                  new SnapshotFaultException(e), "Failed");
        }
    }

    /**
     * Signals that a StorageNodeAgent is unavailable. This extends
     * {@link NonfatalAsertionException} because it will not crash the Admin.
     */
    private static class SNAUnavailableException
        extends NonfatalAssertionException{
        private static final long serialVersionUID = 1L;

        private SNAUnavailableException(String msg) {
            super(msg);
        }
    }

    /**
     * Subclass of InternalFaultException used to indicate that the fault
     * originated in calling the snapshot operation on SNA.
     */
    private static class SnapshotFaultException
        extends InternalFaultException {
        private static final long serialVersionUID = 1L;

        private SnapshotFaultException(Throwable cause) {
            super(cause);
        }
    }
}
