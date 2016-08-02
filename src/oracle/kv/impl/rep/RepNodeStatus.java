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

package oracle.kv.impl.rep;

import java.io.Serializable;

import oracle.kv.impl.rep.migration.PartitionMigrationStatus;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.util.PingDisplay;

import com.sleepycat.je.rep.ReplicatedEnvironment.State;
import com.sleepycat.je.rep.ReplicatedEnvironmentStats;
import com.sleepycat.je.rep.impl.networkRestore.NetworkBackupStats;
import com.sleepycat.je.rep.utilint.HostPortPair;

/**
 * RepNodeStatus represents the current status of a running RepNodeService.  It
 * includes ServiceStatus as well as additional state specific to a RepNode.
 */
public class RepNodeStatus implements Serializable, PingDisplay.ServiceInfo {

    private static final long serialVersionUID = 1L;
    private final ServiceStatus status;
    private final State state;
    private final long vlsn;

    /* Since R2 */
    private final String haHostPort;

    /* Since R2 */
    private final PartitionMigrationStatus[] migrationStatus;

    /*
     * The haPort field is present for backward compatibility. If deserialized
     * at an R1 node we still want it to function. The added field, haHostPort,
     * was added for elasticity and is not needed for general operation.
     *
     */
    private final int haPort;

    /**
     * JE HA replication statistics for a master rep node, or null if not the
     * master or otherwise not available.  Added in R3.3.
     */
    private final MasterRepNodeStats masterRepNodeStats;

    /**
     * JE HA network backup statistics for a replica that is performing a
     * network restore, or null if not a replica, not performing a network
     * restore, or the information is otherwise not available.  Added in R3.3.
     */
    private final NetworkBackupStats networkRestoreStats;

    /**
     * JE HA information about whether this is an authoritative master.  Only
     * meaningful if state is non-null and is MASTER.  Added in R3.4.
     */
    private final boolean isAuthoritativeMaster;

    public RepNodeStatus(ServiceStatus status, State state, long vlsn,
                         String haHostPort,
                         PartitionMigrationStatus[] migrationStatus,
                         ReplicatedEnvironmentStats replicatedEnvStats,
                         NetworkBackupStats networkRestoreStats,
                         boolean isAuthoritativeMaster) {
        this.status = status;
        this.state = state;
        this.vlsn = vlsn;
        this.haHostPort = haHostPort;
        this.migrationStatus = migrationStatus;
        haPort = HostPortPair.getPort(haHostPort);
        masterRepNodeStats = MasterRepNodeStats.create(replicatedEnvStats);
        this.networkRestoreStats = networkRestoreStats;
        this.isAuthoritativeMaster =
            isAuthoritativeMaster && (state == State.MASTER);
    }

    @Override
    public ServiceStatus getServiceStatus() {
        return status;
    }

    @Override
    public State getReplicationState() {
        return state;
    }

    public long getVlsn() {
        return vlsn;
    }

    public int getHAPort() {
        return haPort;
    }

    /**
     * Returns the HA host and port string. The returned value may be null
     * if this instance represents a pre-R2 RepNodeService.
     *
     * @return the HA host and port string or null
     */
    public String getHAHostPort() {
        return haHostPort;
    }

    public PartitionMigrationStatus[] getPartitionMigrationStatus() {
        /* For compatibility with R1, return an empty array */
        return (migrationStatus == null) ? new PartitionMigrationStatus[0] :
                                           migrationStatus;
    }

    /**
     * Returns information about JE HA replication statistics associated with a
     * master rep node, or null if this node is not a master or the statistics
     * are otherwise not available.
     *
     * @return the stats or {@code null}
     */
    public MasterRepNodeStats getMasterRepNodeStats() {
        return masterRepNodeStats;
    }

    /**
     * Returns network backup statistics for a replica that is performing a
     * network restore, or null if this node is not a replica, is not
     * performing a network restore, or the statistics are otherwise not
     * available.
     *
     * @return the stats or {@code null}
     */
    public NetworkBackupStats getNetworkRestoreStats() {
        return networkRestoreStats;
    }

    /**
     * Returns whether this node is the authoritative master.  Always returns
     * false if the state shows that the node is not the master.
     */
    @Override
    public boolean getIsAuthoritativeMaster() {
        return isAuthoritativeMaster;
    }

    /**
     * Computes and returns the estimated time in seconds until the node's
     * current network restore operation will complete, or 0 if no network
     * restore operation is known to be underway.
     */
    public long getNetworkRestoreTimeSecs() {
        if (networkRestoreStats != null) {
            final long remainingBytes =
                networkRestoreStats.getExpectedBytes() -
                networkRestoreStats.getTransferredBytes();
            final double transferRate = networkRestoreStats.getTransferRate();
            return (long) Math.ceil(remainingBytes / transferRate);
        }
        return 0;
    }

    @Override
    public String toString() {
        return status + "," + state +
            (((state == State.MASTER) && !isAuthoritativeMaster) ?
             " (non-authoritative)" : "");
    }
}
