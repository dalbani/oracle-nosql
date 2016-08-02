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
package oracle.kv.util;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oracle.kv.impl.admin.AdminFaultException;
import oracle.kv.impl.admin.AdminStatus;
import oracle.kv.impl.admin.CommandServiceAPI;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.param.StorageNodeParams;
import oracle.kv.impl.monitor.views.ServiceChange;
import oracle.kv.impl.rep.RepNodeStatus;
import oracle.kv.impl.rep.admin.RepNodeAdminAPI;
import oracle.kv.impl.security.login.LoginManager;
import oracle.kv.impl.sna.StorageNodeAgentAPI;
import oracle.kv.impl.sna.StorageNodeStatus;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.RepGroup;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.RepNode;
import oracle.kv.impl.topo.RepNodeId;
import oracle.kv.impl.topo.ResourceId;
import oracle.kv.impl.topo.StorageNode;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.impl.util.registry.RegistryUtils;
import oracle.kv.util.Ping.Problem;

/**
 * PingCollector visits each service in the store and requests ping status. The
 * collector is created with a topology instance to direct its collection
 * efforts. It is meant for one time use; detected problems are accumulated and
 * are not cleared between calls to
 *
 * - Since admin services are not in the topology, a Parameters instance
 * is used to find admins. The Parameters instance may be null, in which case
 * the collector omits admins.
 *
 * - The collector will use a login manager if one is supplied to contact
 * admin services.
 *
 * The collector is meant to be robust and to continue on past any component
 * failure. If one component of the store can't be reached, the collector
 * saves information about the problem and carries on.
 */
public class PingCollector {

    /*
     * The action to take when the collector visits a storage node
     */
    private interface StorageNodeCallback {
        void nodeCallback(StorageNode sn, StorageNodeStatus status);
    }

    /*
     * The action to take when the collector visits a replication node
     */
    private interface RepNodeCallback {
        void nodeCallback(RepNode rn, RepNodeStatus status);
    }

    /*
     * The action to take when the collector visits an Admin service.
     */
    private interface AdminCallback {
        void nodeCallback(AdminId aId, AdminInfo info);
    }

    /* A struct for packaging information returned from an Admin service. */
    class AdminInfo {
        final StorageNodeId snId;
        AdminStatus adminStatus;

        AdminInfo(StorageNodeId snId) {
            this.snId = snId;
        }
    }

    /* Collections of status information */
    private Map<StorageNode, StorageNodeStatus> snMap;
    private Map<RepNode, RepNodeStatus> rnMap;

    /*
     * AdminMap and monitoredChanges are collected at the same time. AdminMap *
     * holds admin statuses obtained by pinging each Admin service, while *
     * monitoredChanges holds the collection of service changes held by the
     * admin monitoring system.
     */
    private Map<AdminId, AdminInfo> adminMap;
    private Map<ResourceId, ServiceChange> monitoredChanges;

    /* Map of problems encountered when pinging components of the store. */
    private final List<Problem> problems = new ArrayList<Problem>();

    private final Topology topo;
    private final Parameters params;
    private LoginManager adminLoginManager;

    PingCollector(Topology topo,
                  Parameters params,
                  LoginManager adminLoginManager) {

        this.topo = topo;
        this.params = params;
        this.adminLoginManager = adminLoginManager;
    }

    public PingCollector(Topology topo) {
        this(topo, null, null);
    }

    Map<StorageNode, StorageNodeStatus> getSNMap() {
        if (snMap == null) {
            snMap = new HashMap<StorageNode, StorageNodeStatus>();
            forEachStorageNode(new StorageNodeCallback() {
                    @Override
                    public void nodeCallback(StorageNode sn,
                                             StorageNodeStatus status) {
                        snMap.put(sn, status);
                    }
                });
        }
        return snMap;
    }

    Map<RepNode, RepNodeStatus> getRNMap() {
        if (rnMap == null) {
            rnMap = new TreeMap<RepNode, RepNodeStatus>();
            forEachRepNode(new RepNodeCallback() {
                    @Override
                    public void nodeCallback(RepNode rn, RepNodeStatus status) {
                        rnMap.put(rn, status);
                    }
                });
        }
        return rnMap;
    }

    Map<AdminId, AdminInfo> getAdminMap() {
        if (adminMap == null) {
            adminMap = new HashMap<AdminId, AdminInfo>();
            monitoredChanges = new HashMap<ResourceId, ServiceChange>();
            forEachAdmin(new AdminCallback() {
                    @Override
                    public void nodeCallback(AdminId aId,
                                             AdminInfo info) {
                        adminMap.put(aId, info);
                    }
                });
        }
        return adminMap;
    }

    Map<ResourceId, ServiceChange> getMonitoredChanges() {
        if (monitoredChanges == null) {
            getAdminMap();
        }
        return monitoredChanges;
    }

    List<Problem> getProblems() {
        return problems;
    }

    /**
     * Collect all statuses, and then return a service status map of all the
     * SNs, RNs, and optionally admins, that make up the topology.
     * @return a map of all node resource ids to their statuses.
     */
    public Map<ResourceId, ServiceStatus> getTopologyStatus() {

        final Map<ResourceId, ServiceStatus> ret =
            new HashMap<ResourceId, ServiceStatus>();

        for (Map.Entry<StorageNode, StorageNodeStatus> e :
                 getSNMap().entrySet()) {
            ServiceStatus status =
                e.getValue() == null ? ServiceStatus.UNREACHABLE:
                e.getValue().getServiceStatus();

            ret.put(e.getKey().getResourceId(), status);
        }

        for (Map.Entry<RepNode, RepNodeStatus> e :
                 getRNMap().entrySet()) {
            ServiceStatus status =
                e.getValue() == null ? ServiceStatus.UNREACHABLE:
                e.getValue().getServiceStatus();

            ret.put(e.getKey().getResourceId(), status);
        }

        for (Map.Entry<AdminId, AdminInfo> e :
                 getAdminMap().entrySet()) {
            ServiceStatus status = null;
            if (e.getValue() == null) {
                status = ServiceStatus.UNREACHABLE;
            } else {
                AdminStatus aStatus = e.getValue().adminStatus;
                status = (aStatus == null) ? ServiceStatus.UNREACHABLE:
                    aStatus.getServiceStatus();
            }
            ret.put(e.getKey(), status);
        }

        return ret;
    }

    /**
     * Collects status for a given replication group and returns the
     * replication node which is the master for the replication group. If the
     * master is not found or there are more than one node that thinks it's
     * master, @code null is returned.
     *
     * @return a replication node or @code null
     */
    public RepNode getMaster(RepGroupId rgId) {

        final List<RepNode> master = new ArrayList<RepNode>();

        forEachRepNodeInShard
            (rgId,
             new RepNodeCallback() {
                 @Override
                 public void nodeCallback(RepNode rn, RepNodeStatus status) {
                     if ((status != null) &&
                         status.getReplicationState().isMaster()) {
                         master.add(rn);
                     }
                 }
             });
        return (master.size() == 1) ? master.get(0) : null;
    }

    /**
     * Find the master of this shard and return its full name and its
     * haport. Returns null if no master found.
     */
    public RNNameHAPort getMasterNamePort(RepGroupId rgId) {

        final List<RNNameHAPort> namePort = new ArrayList<RNNameHAPort>();

        forEachRepNodeInShard
            (rgId, new RepNodeCallback() {
                    @Override
                    public void nodeCallback(RepNode rn, RepNodeStatus status) {
                        if ((status != null) &&
                            status.getReplicationState().isMaster()) {
                            String rnName = rn.getResourceId().getFullName();
                            namePort.add(new RNNameHAPort
                                         (rnName, status.getHAHostPort()));
                        }
                    }
                });
        return (namePort.size() == 1) ? namePort.get(0) : null;
    }

    /* A struct for returning information about a master of a shard */
    public class RNNameHAPort {
        private final String fullName;
        private final String haHostPort;

        RNNameHAPort(String fullName, String haHostPort) {
            this.fullName = fullName;
            this.haHostPort = haHostPort;
        }

        public String getFullName() {
            return fullName;
        }

        public String getHAHostPort() {
            return haHostPort;
        }
    }

    /**
     * Return a map of replication node status for each RN in the shard. Note
     * that the status value may be null if the RN is not responsive.
     */
    public Map<RepNodeId, RepNodeStatus> getRepNodeStatus(RepGroupId rgId) {

        /* Get status for each node in the shard */
        final Map<RepNodeId, RepNodeStatus> statusMap =
            new HashMap<RepNodeId, RepNodeStatus>();

        forEachRepNodeInShard
            (rgId, new RepNodeCallback() {
                    @Override
                    public void nodeCallback(RepNode rn, RepNodeStatus status) {
                        statusMap.put(rn.getResourceId(), status);
                    }
                });
        return statusMap;
    }

    private void forEachStorageNode(StorageNodeCallback callback) {

        /* LoginManager not needed for ping */
        RegistryUtils regUtils = new RegistryUtils(topo,
                                                   (LoginManager) null);

        for (StorageNode sn : topo.getStorageNodeMap().getAll()) {
            StorageNodeStatus status = null;
            try {
                StorageNodeAgentAPI sna =
                    regUtils.getStorageNodeAgent(sn.getResourceId());
                status = sna.ping();
            } catch (RemoteException re) {
                problems.add(new Problem(sn.getResourceId(), sn.getHostname(),
                                         sn.getRegistryPort(),
                                         "Can't call ping for SN: ", re));
            } catch (NotBoundException e) {
                problems.add(new Problem(sn.getResourceId(), sn.getHostname(),
                                         sn.getRegistryPort(),
                                         "No RMI service for SN", e));
            }
            callback.nodeCallback(sn, status);
        }
    }

    private void forEachRepNode(RepNodeCallback callback) {

        for (RepGroup rg : topo.getRepGroupMap().getAll()) {
            forEachRepNodeInShard(rg.getResourceId(), callback);
        }
    }

    private void forEachRepNodeInShard(RepGroupId rgId,
                                       RepNodeCallback callback) {

        final RepGroup group = topo.get(rgId);

        if (group == null) {
            return;
        }

        /* LoginManager not needed for ping */
        final RegistryUtils regUtils = new RegistryUtils(topo,
                                                         (LoginManager) null);

        for (RepNode rn : group.getRepNodes()) {
            RepNodeStatus status = null;
            StorageNode sn = topo.get(rn.getStorageNodeId());
            try {
                RepNodeAdminAPI rna =
                    regUtils.getRepNodeAdmin(rn.getResourceId());
                status = rna.ping();
            } catch (RemoteException re) {
                problems.add(new Problem(rn.getResourceId(),
                                         sn.getHostname(),
                                         sn.getRegistryPort(),
                                         "Can't call ping for RN:" + re));
            } catch (NotBoundException e) {
                problems.add(new Problem(rn.getResourceId(),
                                         sn.getHostname(),
                                         sn.getRegistryPort(),
                                         "No RMI service for RN: " + e));
            }
            callback.nodeCallback(rn, status);
        }
    }

    private void forEachAdmin(AdminCallback callback) {
        if (params == null) {
            return;
        }
        for (final AdminId aId : params.getAdminIds()) {
            final StorageNodeId snId = params.get(aId).getStorageNodeId();
            final StorageNodeParams snp = params.get(snId);
            String hostname = snp.getHostname();
            int port = snp.getRegistryPort();
            final AdminInfo info = new AdminInfo(snId);
            try {
                final CommandServiceAPI admin =
                    RegistryUtils.getAdmin(hostname, port, adminLoginManager);
                info.adminStatus = admin.getAdminStatus();

                /*
                 * Ask the Admin master for the latest service status map,
                 * which is maintained as part of the Admin's monitoring
                 * infrastructure.
                 */
                if (info.adminStatus.getIsAuthoritativeMaster()) {
                    Map<ResourceId, ServiceChange> monitorMap =
                        admin.getStatusMap();
                    monitoredChanges.putAll(monitorMap);
                }
            } catch (AdminFaultException afe) {
                /*
                 * Note that admin.getAdminStatus() may throw an AFE wrapping a
                 * SAE in case of network issues, which is caused by RE or NBE.
                 */
                problems.add(new Problem(aId, hostname, port,
                                         "Can't get status for Admin:", afe));
            } catch (RemoteException re) {
                problems.add(new Problem(aId, hostname, port,
                                         "Can't get status for Admin:", re));
            } catch (NotBoundException e) {
                problems.add(new Problem(aId, hostname, port,
                                         "No RMI Service for Admin:", e));
            }
            callback.nodeCallback(aId, info);
        }
    }
}
