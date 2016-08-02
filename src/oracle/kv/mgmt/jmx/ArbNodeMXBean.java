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

package oracle.kv.mgmt.jmx;

/**
 * This MBean represents an Arbiter node's operational parameters.
 *
 */
public interface ArbNodeMXBean {

    /**
     * Returns the ArbNodeId number of an Arbiter instance.
     */
    String getArbNodeId();

    /**
     * Returns the reported service status of the Arbiter
     */
    String getServiceStatus();

    /**
     * Returns Non-default BDB-JE configuration properties.
     */
    String getConfigProperties();
    /**
     * Returns a string that is added to the command line when the Replication
     * Node process is started.
     */
    String getJavaMiscParams();
    /**
     * Returns property settings for the Logging subsystem.
     */
    String getLoggingConfigProps();
    /**
     * If true, then the underlying BDB-JE subsystem will dump statistics into
     * a local .stat file.
     */
    boolean getCollectEnvStats();
    /**
     * Returns the collection period for latency statistics, in sec.
     */
    int getStatsInterval();
    /**
     * Returns the size of the Java heap for this Replication Node, in MB.
     */
    int getHeapMB();

    /**
     * Returns the number of transactions acked.
     */
    long getAcks();

    /**
     * Returns the current master.
     */
    String getMaster();

    /**
     * Returns the current node State.
     */
    String getState();

    /*
     * Returns the current acked VLSN
     */
    long getVLSN();

    /*
     * Returns the current replayQueueOverflow value.
     */
    long getReplayQueueOverflow();
}
