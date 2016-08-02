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
import java.util.SortedMap;

import com.sleepycat.je.rep.ReplicatedEnvironmentStats;

/** Stores JE HA replication statistics associated with a master rep node. */
public class MasterRepNodeStats implements Serializable {
    private static final long serialVersionUID = 1;
    private final SortedMap<String, Long> replicaDelayMillisMap;
    private final SortedMap<String, Long> replicaLastCommitTimestampMap;
    private final SortedMap<String, Long> replicaLastCommitVLSNMap;
    private final SortedMap<String, Long> replicaVLSNLagMap;
    private final SortedMap<String, Long> replicaVLSNRateMinuteMap;
    private final long masterLastCommitVLSN;
    private final long masterLastCommitTimestamp;
    private final long masterVLSNRateMinute;

    public static MasterRepNodeStats create(ReplicatedEnvironmentStats stats) {
        return (stats != null) ? new MasterRepNodeStats(stats) : null;
    }

    private MasterRepNodeStats(ReplicatedEnvironmentStats stats) {
        replicaDelayMillisMap = stats.getReplicaDelayMap();
        replicaLastCommitTimestampMap =
            stats.getReplicaLastCommitTimestampMap();
        replicaLastCommitVLSNMap = stats.getReplicaLastCommitVLSNMap();
        replicaVLSNLagMap = stats.getReplicaVLSNLagMap();
        replicaVLSNRateMinuteMap = stats.getReplicaVLSNRateMap();
        masterLastCommitVLSN = stats.getLastCommitVLSN();
        masterLastCommitTimestamp = stats.getLastCommitTimestamp();
        masterVLSNRateMinute = stats.getVLSNRate();
    }

    /** @see ReplicatedEnvironmentStats#getReplicaDelayMap */
    public SortedMap<String, Long> getReplicaDelayMillisMap() {
        return replicaDelayMillisMap;
    }

    /** @see ReplicatedEnvironmentStats#getReplicaLastCommitTimestampMap */
    public SortedMap<String, Long> getReplicaLastCommitTimestampMap() {
        return replicaLastCommitTimestampMap;
    }

    /** @see ReplicatedEnvironmentStats#getReplicaLastCommitVLSNMap */
    public SortedMap<String, Long> getReplicaLastCommitVLSNMap() {
        return replicaLastCommitVLSNMap;
    }

    /** @see ReplicatedEnvironmentStats#getReplicaVLSNLagMap */
    public SortedMap<String, Long> getReplicaVLSNLagMap() {
        return replicaVLSNLagMap;
    }

    /** @see ReplicatedEnvironmentStats#getReplicaVLSNRateMap */
    public SortedMap<String, Long> getReplicaVLSNRateMinuteMap() {
        return replicaVLSNRateMinuteMap;
    }

    /** @see ReplicatedEnvironmentStats#getLastCommitVLSN */
    public long getMasterLastCommitVLSN() {
        return masterLastCommitVLSN;
    }

    /** @see ReplicatedEnvironmentStats#getLastCommitTimestamp */
    public long getMasterLastCommitTimestamp() {
        return masterLastCommitTimestamp;
    }

    /** @see ReplicatedEnvironmentStats#getVLSNRate */
    public double getMasterVLSNRateMinute() {
        return masterVLSNRateMinute;
    }

    /**
     * Computes and returns the estimated rate, in milliseconds per minute
     * that the specified replica is reducing its delay with the master, or
     * null if not known.  Returns a negative value if the replica is getting
     * further behind.
     */
    public Long getReplicaCatchupRate(String replicaName) {

        /* Nothing to do if lag is not known */
        final Long replicaVLSNLag = replicaVLSNLagMap.get(replicaName);
        if (replicaVLSNLag == null) {
            return null;
        }

        /* Not catching up if not behind */
        if (replicaVLSNLag <= 0) {
            return 0L;
        }

        /* Nothing more to do if the delay is not known */
        final Long replicaDelayMillis = replicaDelayMillisMap.get(replicaName);
        if (replicaDelayMillis == null) {
            return null;
        }

        /* Nothing more to do if the replica VLSN rate is not known */
        final Long replicaVLSNPerMinute =
            replicaVLSNRateMinuteMap.get(replicaName);
        if (replicaVLSNPerMinute == null) {
            return null;
        }

        /* Replica catches up this many VLSNs per minute */
        final double replicaVLSNCatchupPerMinute =
            replicaVLSNPerMinute - masterVLSNRateMinute;

        /* Proportion of total delay that replica catches up per minute */
        final double replicaCatchupRatioPerMinute =
            replicaVLSNCatchupPerMinute / replicaVLSNLag;

        /*
         * Return rate in ms per minute that the replica is catching up,
         * rounding the value up
         */
        return (long) Math.ceil(
            replicaDelayMillis * replicaCatchupRatioPerMinute);
    }

    /**
     * Computes and returns the estimated time, in seconds, until the specified
     * replica eliminates its delay with the master, or null if not known.
     * Returns {@link Long#MAX_VALUE} if the replica catchup rate is 0, and
     * returns a negative value whose absolute value is the estimated time in
     * milliseconds until the delay doubles if the replica is falling behind.
     */
    public Long getReplicaCatchupTimeSecs(String replicaName) {

        /* Nothing to do if lag is not known */
        final Long replicaVLSNLag = replicaVLSNLagMap.get(replicaName);
        if (replicaVLSNLag == null) {
            return null;
        }

        /* Caught up if not behind */
        if (replicaVLSNLag <= 0) {
            return 0L;
        }

        /* Nothing more to do if the delay is not known */
        final Long replicaDelayMillis = replicaDelayMillisMap.get(replicaName);
        if (replicaDelayMillis == null) {
            return null;
        }

        /* Nothing more to do if the replica VLSN rate is not known */
        final Long replicaVLSNPerMinute =
            replicaVLSNRateMinuteMap.get(replicaName);
        if (replicaVLSNPerMinute == null) {
            return null;
        }

        /* Replica catches up this many VLSNs per minute */
        final long replicaVLSNCatchupPerMinute =
            replicaVLSNPerMinute - masterVLSNRateMinute;

        /* Not catching up */
        if (replicaVLSNCatchupPerMinute == 0) {
            return Long.MAX_VALUE;
        }

        /*
         * Return time in seconds for the replica to catch up, or a negative
         * value representing the time to double the current delay if the
         * replica is falling behind
         */
        return (long) Math.ceil(
            (60.0 * replicaVLSNLag) / replicaVLSNCatchupPerMinute);
    }
}

