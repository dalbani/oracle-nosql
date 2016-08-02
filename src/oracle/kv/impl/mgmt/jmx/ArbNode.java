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

package oracle.kv.impl.mgmt.jmx;

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import oracle.kv.impl.admin.param.ArbNodeParams;
import oracle.kv.impl.measurement.ArbiterNodeStats;
import oracle.kv.impl.measurement.ConciseStats;
import oracle.kv.impl.rep.monitor.StatsPacket;
import oracle.kv.impl.topo.ArbNodeId;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.mgmt.jmx.ArbNodeMXBean;

import com.sleepycat.je.rep.arbiter.ArbiterStats;

public class ArbNode
    extends NotificationBroadcasterSupport
    implements ArbNodeMXBean {

    private final ArbNodeId anId;
    private final MBeanServer server;
    private final StorageNode sn;
    private ServiceStatus status;
    private ArbiterStats arbStats;
    private ArbNodeParams parameters;
    private ObjectName oName;
    long notifySequence = 1L;

    static final String
        NOTIFY_AN_STATUS_CHANGE = "oracle.kv.arbnode.status";

    public ArbNode(ArbNodeParams anp, MBeanServer server, StorageNode sn) {
        this.server = server;
        this.anId = anp.getArbNodeId();
        this.sn = sn;
        status = ServiceStatus.UNREACHABLE;

        resetMetrics();

        setParameters(anp);

        register();
    }

    private void resetMetrics() {
        arbStats = null;
    }

    private void register() {

        final StringBuffer buf = new StringBuffer(JmxAgent.DOMAIN);
        buf.append(":type=ArbNode");
        buf.append(",id=");
        buf.append(getArbNodeId());
        try {
            oName = new ObjectName(buf.toString());
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException
                ("Unexpected exception creating JMX ObjectName " +
                 buf.toString(), e);
        }

        try {
            server.registerMBean(this, oName);
        } catch (Exception e) {
            throw new IllegalStateException
                ("Unexpected exception registring MBean " + oName.toString(),
                 e);
        }
    }

    public void unregister() {
        if (oName != null) {
            try {
                server.unregisterMBean(oName);
            } catch (Exception e) {
                throw new IllegalStateException
                    ("Unexpected exception while unregistring MBean " +
                     oName.toString(), e);
            }
        }
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[]
        {
            new MBeanNotificationInfo
                (new String[]{ArbNode.NOTIFY_AN_STATUS_CHANGE},
                 Notification.class.getName(),
                 "Announce a change in this ArbNode's service status"),
        };
    }

    public void setParameters(ArbNodeParams anp) {
        parameters = anp;
    }

    public synchronized void setPerfStats(StatsPacket packet) {

        for (ConciseStats cs : packet.getOtherStats()) {
            if (cs instanceof ArbiterNodeStats) {
                arbStats = ((ArbiterNodeStats)cs).getArbiterStats();
            }
        }
    }

    public synchronized void setServiceStatus(ServiceStatus newStatus) {
        if (status.equals(newStatus)) {
            return;
        }

        final Notification n = new Notification
            (NOTIFY_AN_STATUS_CHANGE, oName, notifySequence++,
             System.currentTimeMillis(),
             "The service status for ArbNode " + getArbNodeId() +
             " changed to " + newStatus.toString() + ".");

        n.setUserData(newStatus.toString());

        sendNotification(n);

        /*
         * Also send it from the StorageNode. A client can observe this event
         * by subscribing ether to the StorageNode or to this ArbNode.
         */
        sn.sendProxyNotification(n);

        status = newStatus;

        /*
         * Whenever there is a service status change, reset the metrics so that
         * we don't report stale information.
         */
        resetMetrics();
    }

    @Override
    public String getArbNodeId() {
        return anId.getFullName();
    }

    @Override
    public String getServiceStatus() {
        return status.toString();
    }

    @Override
    public String getConfigProperties() {
        return parameters.getConfigProperties();
    }

    @Override
    public String getJavaMiscParams() {
        return parameters.getJavaMiscParams();
    }

    @Override
    public String getLoggingConfigProps() {
        return parameters.getLoggingConfigProps();
    }

    @Override
    public boolean getCollectEnvStats() {
        return parameters.getCollectEnvStats();
    }

    @Override
    public int getStatsInterval() {
        return parameters.getStatsInterval() / 1000; /* In seconds. */
    }

    @Override
    public int getHeapMB() {
        return (int) parameters.getMaxHeapMB();
    }

    @Override
    public long getAcks() {
        return arbStats != null ? arbStats.getAcks() : -1;
    }

    @Override
    public String getMaster() {
        return arbStats != null ? arbStats.getMaster() : null;
    }

    @Override
    public long getReplayQueueOverflow() {
        return arbStats != null ? arbStats.getReplayQueueOverflow() : -1;
    }

    @Override
    public String getState() {
        return arbStats != null ? arbStats.getState() : null;
    }

    @Override
    public long getVLSN() {
        return arbStats != null ? arbStats.getVLSN() : -1;
    }
}
