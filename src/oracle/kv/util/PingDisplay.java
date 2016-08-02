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

import static oracle.kv.impl.util.JsonUtils.createObjectNode;
import static oracle.kv.impl.util.JsonUtils.getAsText;
import static oracle.kv.impl.util.JsonUtils.getBoolean;
import static oracle.kv.impl.util.JsonUtils.getLong;
import static oracle.kv.impl.util.JsonUtils.getObject;

import java.util.Collection;

import oracle.kv.KVVersion;
import oracle.kv.impl.admin.AdminStatus;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.rep.MasterRepNodeStats;
import oracle.kv.impl.rep.RepNodeStatus;
import oracle.kv.impl.sna.StorageNodeStatus;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.Datacenter;
import oracle.kv.impl.topo.DatacenterId;
import oracle.kv.impl.topo.RepGroup;
import oracle.kv.impl.topo.RepNode;
import oracle.kv.impl.topo.StorageNode;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.impl.util.FormatUtils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.sleepycat.je.rep.ReplicatedEnvironment.State;

/**
 * This class contains utility methods used by both the Ping and the Verify
 * utilities to display service status information in both JSON and
 * human-readable text format. In general, the methods come in pairs:
 *
 * -XXXtoJSON converts information about components in the store to JSON format
 * -displayXXX takes JSON formatted information and displays it as text
 *
 * In some cases, the XXXtoJSON method do a straightforward translation of
 * information to JSON. In other cases, the XXXtoJSON does some summarizing,
 * aggregation, or other massaging of the service information.
 */
public class PingDisplay {

    /**
     * Information received from a component, or service in the kvstore.
     */
    public interface ServiceInfo {
        /* Starting, running, etc */
        ServiceStatus getServiceStatus();

        /* Master, replica, unknown */
        State getReplicationState();
        boolean getIsAuthoritativeMaster();
    }

    /**
     * Adds overview information about the topology to the JSON node by adding
     * a "topology" field with an object value.
     */
    public static void topologyOverviewToJson(Topology topology,
                                              ObjectNode jsonTop) {
        final ObjectNode on = jsonTop.putObject("topology");
        on.put("storeName", topology.getKVStoreName());
        on.put("sequenceNumber", topology.getSequenceNumber());
        on.put("numPartitions", topology.getPartitionMap().getNPartitions());
        on.put("numStorageNodes", topology.getStorageNodeMap().size());
        on.put("time", System.currentTimeMillis());
        on.put("version", KVVersion.CURRENT_VERSION.getNumericVersionString());
    }

    /**
     * Converts topology information from the JSON node into a human readable
     * string.
     */
    public static String displayTopologyOverview(JsonNode jsonTop) {
        final ObjectNode on = getObject(jsonTop, "topology");
        if (on == null) {
            return "";
        }
        final Long timeValue = getLong(on, "time");
        final String time = (timeValue == null) ? "?" :
            FormatUtils.formatDateAndTime(timeValue);
        return "store " + getAsText(on, "storeName", "?") +
            " based upon topology sequence #" +
            getAsText(on, "sequenceNumber", "?") + "\n" +
            getAsText(on, "numPartitions", "?") + " partitions and " +
            getAsText(on, "numStorageNodes", "?") + " storage nodes\n" +
            "Time: " + time + "   Version: " + getAsText(on, "version", "?");
    }

    /**
     * Returns a JSON node with overview information about the zone that the
     * caller will add as an array element to the "zoneStatus" field.
     */
    public static ObjectNode zoneOverviewToJson
        (Topology topology,
         Datacenter dc,
         Ping.RepNodeStatusFunction rnStatusFunc) {

        final DatacenterId dcId = dc.getResourceId();
        int online = 0;
        int offline = 0;
        boolean hasReplicas = false;
        Long maxDelay = null;
        Long maxCatchupTime = null;
        final Collection<RepGroup> repGroups =
            topology.getRepGroupMap().getAll();
        for (final RepGroup rg : repGroups) {

            /* Get stats for group master */
            MasterRepNodeStats masterStats = null;
            for (final RepNode rn : rg.getRepNodes()) {
                final RepNodeStatus rnStatus = rnStatusFunc.get(rn);
                if ((rnStatus != null) &&
                    rnStatus.getReplicationState().isMaster()) {
                    masterStats = rnStatus.getMasterRepNodeStats();
                    break;
                }
            }

            for (final RepNode rn : rg.getRepNodes()) {
                final StorageNode sn = topology.get(rn.getStorageNodeId());
                if (!dcId.equals(sn.getDatacenterId())) {
                    continue;
                }
                final RepNodeStatus status = rnStatusFunc.get(rn);
                if ((status == null) ||
                    !status.getReplicationState().isActive()) {
                    offline++;
                    continue;
                }
                online++;
                if (status.getReplicationState().isMaster()) {
                    continue;
                }
                hasReplicas = true;
                Long delay = null;
                Long catchupTime = null;
                final long networkRestoreTime =
                    status.getNetworkRestoreTimeSecs();
                if (networkRestoreTime != 0) {
                    catchupTime = networkRestoreTime;
                } else if (masterStats != null) {
                    final String replicaName = rn.getResourceId().toString();
                    delay = masterStats.getReplicaDelayMillisMap().get(
                        replicaName);
                    catchupTime =
                        masterStats.getReplicaCatchupTimeSecs(replicaName);
                }
                if ((delay != null) &&
                    ((maxDelay == null) || (delay > maxDelay))) {
                    maxDelay = delay;
                }
                if (useForMaxTime(catchupTime, maxCatchupTime)) {
                    maxCatchupTime = catchupTime;
                }
            }
        }
        final ObjectNode on = createObjectNode();
        zoneNameToJson(dc, on);
        final ObjectNode rnStatus = on.putObject("rnSummaryStatus");
        rnStatus.put("online", online);
        rnStatus.put("offline", offline);
        rnStatus.put("hasReplicas", hasReplicas);
        if (maxDelay != null) {
            rnStatus.put("maxDelayMillis", maxDelay);
        }
        if (maxCatchupTime != null) {
            rnStatus.put("maxCatchupTimeSecs", maxCatchupTime);
        }
        return on;
    }

    /**
     * Converts zone overview information from the JSON node into a human
     * readable string.
     */
    public static String displayZoneOverview(JsonNode jsonZone) {
        final ObjectNode jsonRN = getObject(jsonZone, "rnSummaryStatus");
        if (jsonRN == null) {
            return "Zone " + displayZoneName(jsonZone);
        }
        final boolean hasReplicas = getBoolean(jsonRN, "hasReplicas", false);
        final String maxDelay = !hasReplicas ? null :
            getAsText(jsonRN, "maxDelayMillis", "?");
        final Long maxCatchupValue = getLong(jsonRN, "maxCatchupTimeSecs");
        final String maxCatchup = !hasReplicas ? null :
            (maxCatchupValue == null) ? "?" :
            (maxCatchupValue == Long.MAX_VALUE) ? "-" :
            maxCatchupValue.toString();
        return "Zone " + displayZoneName(jsonZone) +
            "   RN Status: online:" + getAsText(jsonRN, "online", "?") +
            " offline:" + getAsText(jsonRN, "offline", "?") +
            ((maxDelay != null) ? " maxDelayMillis:" + maxDelay : "") +
            ((maxCatchup != null) ? " maxCatchupTimeSecs:" + maxCatchup : "");
    }

    /**
     * Adds overview information about shards in the topology to the JSON node
     * by adding a "shardStatus" field with an object value.
     */
    public static void shardOverviewToJson
        (Topology topology,
         Ping.RepNodeStatusFunction rnStatusFunc,
         ObjectNode jsonTop) {

        int totalRF = 0;
        int totalPrimaryRF = 0;
        for (final Datacenter dc : topology.getDatacenterMap().getAll()) {
            final int rf = dc.getRepFactor();
            totalRF += rf;
            if (dc.getDatacenterType().isPrimary()) {
                totalPrimaryRF += rf;
            }
        }
        final int quorum = (totalPrimaryRF / 2) + 1;
        int healthy = 0;
        int writableDegraded = 0;
        int readonly = 0;
        int offline = 0;
        for (RepGroup rg : topology.getRepGroupMap().getAll()) {
            int onlineRNs = 0;
            int onlinePrimaryRNs = 0;
            for (final RepNode rn : rg.getRepNodes()) {
                final StorageNode sn = topology.get(rn.getStorageNodeId());
                final Datacenter dc = topology.get(sn.getDatacenterId());
                final RepNodeStatus status = rnStatusFunc.get(rn);
                if ((status != null) &&
                    status.getReplicationState().isActive()) {
                    onlineRNs++;
                    if (dc.getDatacenterType().isPrimary()) {
                        onlinePrimaryRNs++;
                    }
                }
            }
            if (onlineRNs >= totalRF) {
                healthy++;
            } else if (onlinePrimaryRNs >= quorum) {
                writableDegraded++;
            } else if (onlineRNs > 0) {
                readonly++;
            } else {
                offline++;
            }
        }
        final ObjectNode on = jsonTop.putObject("shardStatus");
        on.put("healthy", healthy);
        on.put("writable-degraded", writableDegraded);
        on.put("read-only", readonly);
        on.put("offline", offline);
    }

    /**
     * Converts overview information about shards from JSON format into a human
     * readable string.
     */
    public static String displayShardOverview(JsonNode jsonTop) {
        final ObjectNode on = getObject(jsonTop, "shardStatus");
        if (on == null) {
            return "";
        }
        return "Shard Status:" +
            " healthy:" + getAsText(on, "healthy", "?") +
            " writable-degraded:" + getAsText(on, "writable-degraded", "?") +
            " read-only:" + getAsText(on, "read-only", "?") +
            " offline:" + getAsText(on, "offline", "?");
    }

    /**
     * Adds overview information about admins to the JSON node by adding a
     * "adminStatus" field with a text value.
     */
    public static void adminOverviewToJson
        (Parameters parameters,
         Ping.AdminStatusFunction adminStatusFunc,
         ObjectNode jsonTop) {

        boolean foundAuthoritativeMaster = false;
        boolean foundOnline = false;
        boolean foundOffline = false;
        for (final AdminId adminId : parameters.getAdminIds()) {
            final AdminStatus adminStatus = adminStatusFunc.get(adminId);
            if ((adminStatus == null) ||
                adminStatus.getServiceStatus() != ServiceStatus.RUNNING) {
                foundOffline = true;
            } else {
                foundOnline = true;
                if (adminStatus.getReplicationState().isMaster() &&
                    adminStatus.getIsAuthoritativeMaster()) {
                    foundAuthoritativeMaster = true;
                }
            }
        }
        final String status =
            !foundOnline ? "offline" :
            !foundAuthoritativeMaster ? "read-only" :
            foundOffline ? "writable-degraded" :
            "healthy";
        jsonTop.put("adminStatus", status);
    }

    /**
     * Converts overview information about admins from the JSON node into a
     * human readable string.
     */
    public static String displayAdminOverview(JsonNode jsonTop) {
        final String adminStatus = getAsText(jsonTop, "adminStatus");
        if (adminStatus == null) {
            return "";
        }
        return "Admin Status: " + adminStatus;
    }

    /**
     * Returns a JSON node with information about the replication node that the
     * caller will add as an array element of the "rnStatus" field within the
     * "snStatus" field.
     */
    public static ObjectNode repNodeToJson(RepNode rn,
                                           RepNodeStatus status,
                                           RepNodeStatus masterStatus,
                                           ServiceStatus expected) {
        final ObjectNode on = createObjectNode();
        on.put("resourceId", rn.getResourceId().toString());
        statusToJson(status, on);
        if (expected != null) {
            on.put("expectedStatus", expected.toString());
        }
        if (status == null) {
            return on;
        }
        on.put("sequenceNumber", status.getVlsn());
        on.put("haPort", status.getHAPort());
        if (status.getReplicationState().isMaster()) {
            return on;
        }
        final long networkRestoreTime = status.getNetworkRestoreTimeSecs();
        if (networkRestoreTime > 0) {
            on.put("networkRestoreUnderway", true);
            on.put("catchupTimeSecs", networkRestoreTime);
            return on;
        }
        on.put("networkRestoreUnderway", false);
        if (masterStatus != null) {
            final MasterRepNodeStats stats =
                masterStatus.getMasterRepNodeStats();
            if (stats != null) {
                final String replicaName = rn.getResourceId().toString();
                final Long delay =
                    stats.getReplicaDelayMillisMap().get(replicaName);
                if (delay != null) {
                    on.put("delayMillis", delay);
                }
                final Long catchupTime =
                    stats.getReplicaCatchupTimeSecs(replicaName);
                if (catchupTime != null) {
                    on.put("catchupTimeSecs", catchupTime);
                }
                final Long catchupRate =
                    stats.getReplicaCatchupRate(replicaName);
                if (catchupRate != null) {
                    on.put("catchupRateMillisPerMinute", catchupRate);
                }
            }
        }
        return on;
    }

    /**
     * Converts replication node information from the JSON node into a human
     * readable string.
     */
    public static String displayRepNode(JsonNode node) {
        String result = "\tRep Node [" +
            getAsText(node, "resourceId", "?") + "]\tStatus: " +
            displayStatus(node);
        final String stopped =
            "UNREACHABLE".equals(getAsText(node, "expectedStatus")) ?
            " (Stopped)" : "";
        if (getAsText(node, "status", "UNREACHABLE").equals("UNREACHABLE")) {
            return result + stopped;
        }

        final Long sequenceNumber = getLong(node, "sequenceNumber");
        result += " sequenceNumber:" +
            ((sequenceNumber != null) ?
             String.format("%,d", sequenceNumber) : "?") +
            " haPort:" + getAsText(node, "haPort", "?");
        if ("MASTER".equals(getAsText(node, "state"))) {
            return result + stopped;
        }

        final Long catchupTimeValue = getLong(node, "catchupTimeSecs");
        final String catchupTime = (catchupTimeValue == null) ? "?" :
            (catchupTimeValue == Long.MAX_VALUE) ? "-" :
            catchupTimeValue.toString();
        final boolean networkRestoreUnderway =
            getBoolean(node, "networkRestoreUnderway", false);
        return result +
            " delayMillis:" + getAsText(node, "delayMillis", "?") +
            " catchupTimeSecs:" + catchupTime +
            (networkRestoreUnderway ? " networkRestoreUnderway" : "") +
            stopped;
    }

    /**
     * Returns a JSON node with information about the storage node, by creating
     * an object node that the caller will add as an array element of the
     * "snStatus" field.
     */
    public static ObjectNode storageNodeToJson(Topology topology,
                                               StorageNode sn,
                                               StorageNodeStatus status) {
        final ObjectNode on = createObjectNode();
        on.put("resourceId", sn.getResourceId().toString());
        on.put("hostname", sn.getHostname());
        on.put("registryPort", sn.getRegistryPort());
        zoneNameToJson(topology.get(sn.getDatacenterId()),
                       on.putObject("zone"));
        if (status != null) {
            on.put("serviceStatus", status.getServiceStatus().toString());
            on.put("version", status.getKVVersion().toString());
        } else {
            on.put("serviceStatus", "UNREACHABLE");
        }
        return on;
    }

    /**
     * Converts storage node information from the JSON node into a human
     * readable string.
     */
    public static String displayStorageNode(JsonNode node) {
        final String serviceStatus =
            getAsText(node, "serviceStatus", "UNREACHABLE");
        return "Storage Node [" +
            getAsText(node, "resourceId", "?") + "] on " +
            getAsText(node, "hostname", "?") + ":" +
            getAsText(node, "registryPort", "?") +
            "    Zone: " + displayZoneName(getObject(node, "zone")) +
            " " +
            (!"UNREACHABLE".equals(serviceStatus) ?
             ("   Status: " + serviceStatus +
              "   Ver: " + getAsText(node, "version", "?")) :
             "UNREACHABLE");
    }

    /**
     * Returns a JSON node with information about the admin node that the
     * caller will add as the value of the "adminStatus" field within the value
     * of the "snStatus" field.
     */
    public static ObjectNode adminToJson(AdminId aId,
                                         AdminStatus adminStatus) {
        final ObjectNode on = createObjectNode();
        on.put("resourceId", aId.toString());
        statusToJson(adminStatus, on);
        return on;
    }

    /**
     * Converts admin node information from the JSON node into a human readable
     * string.
     */
    public static String displayAdmin(JsonNode node) {
        return "\tAdmin [" + getAsText(node, "resourceId", "?") +
            "]\t\tStatus: " + displayStatus(node);
    }

    private static String displayStatus(JsonNode node) {
        final String state = getAsText(node, "state");
        final String authoritative =
            ("MASTER".equals(state) &&
             !getBoolean(node, "authoritativeMaster", true)) ?
            " (non-authoritative)" :
            "";
        return getAsText(node, "status", "UNREACHABLE") +
            ((state == null) ? "" : "," + (state + authoritative));
    }

    /**
     * Whether to use the specified time as a maximum time value instead of the
     * maximum provided.  Ignores null values, and uses larger values except
     * that negative values are used in favor of positive ones.
     */
    private static boolean useForMaxTime(Long time, Long maxTime) {
        if (time == null) {
            return false;
        }
        if (maxTime == null) {
            return true;
        }
        if ((time >= 0) && (maxTime < 0)) {
            return false;
        }
        if ((time < 0) && (maxTime >= 0)) {
            return true;
        }
        return time > maxTime;
    }

    private static void zoneNameToJson(Datacenter dc, ObjectNode node) {
        node.put("resourceId", dc.getResourceId().toString());
        node.put("name", dc.getName());
        node.put("type", dc.getDatacenterType().toString());
    }

    private static String displayZoneName(JsonNode node) {
        return "[name=" + getAsText(node, "name", "?") +
            " id=" + getAsText(node, "resourceId", "?") +
            " type=" + getAsText(node, "type", "?") + "]";
    }

    private static void statusToJson(ServiceInfo status, ObjectNode on) {

        if (status == null) {
            on.put("status", "UNREACHABLE");
        } else {
            on.put("status", status.getServiceStatus().toString());
            State replicationState = status.getReplicationState();
            if (replicationState != null) {
                on.put("state", replicationState.toString());
                if (replicationState.isMaster()) {
                    on.put("authoritativeMaster",
                            status.getIsAuthoritativeMaster());
                }
            }
        }
    }
}
