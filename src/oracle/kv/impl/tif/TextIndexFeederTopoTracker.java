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

package oracle.kv.impl.tif;

import oracle.kv.impl.api.TopologyManager;
import oracle.kv.impl.fault.OperationFaultException;
import oracle.kv.impl.rep.RepNode;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.topo.PartitionMap;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.RepNodeId;
import oracle.kv.impl.topo.Topology;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Object to track the topology changes and make the text index feeder (TIF)
 * acts accordingly to the new topology.
 */
public class TextIndexFeederTopoTracker
    implements TopologyManager.PostUpdateListener {

    private final RepNode repNode;
    private final TextIndexFeeder tif;
    private final Logger logger;

    public TextIndexFeederTopoTracker(RepNode repNode,
                                      TextIndexFeeder tif,
                                      Logger logger) {
        this.repNode = repNode;
        this.tif = tif;
        this.logger = logger;
    }

    @Override
    public boolean postUpdate(Topology topology) {

        if (tif == null) {
            logger.log(Level.FINE,
                       "TIF unavailable, skip topology update with seq# {0}",
                       topology.getSequenceNumber());
            /* Keeps this listener */
            return false;
        }

        final long newTopoSeq = topology.getSequenceNumber();
        final long currentTopoSeq = tif.getSubManager().getCurrentTopologySeq();

        /* skip update if not newer */
        if (newTopoSeq <= currentTopoSeq) {
            logger.log(Level.FINE,
                       "Ignore this update because new topology (seq: {0}) " +
                       "is not newer than current (seq: {1})",
                       new Object[]{newTopoSeq, currentTopoSeq});
            /* Keeps this listener */
            return false;
        }

        /* validate env since we may need to schedule new partition reader */
        if (repNode.getEnv(1) == null) {
            throw new OperationFaultException("Could not obtain env handle");
        }

        /* compute incoming and outbound partitions */
        final RepNodeId repNodeId = repNode.getRepNodeId();
        final RepGroupId repGroupId = topology.get(repNodeId).getRepGroupId();

        final Set<PartitionId> outPids =
            processOutgoingPartitions(repGroupId, topology);
        final Set<PartitionId> inPids =
            processIncomingPartitions(repGroupId, topology);

        tif.getSubManager().setCurrentTopologySeq(newTopoSeq);

        if (!outPids.isEmpty() || !inPids.isEmpty()) {
            logger.log(Level.INFO,
                       "TopologyTracker for TIF on RN {0} finished processing" +
                       " topology update seq # {1}.\nOutgoing partitions: " +
                       "{2}\nIncoming partitions: {3}",
                       new Object[]{repNodeId.getFullName(), newTopoSeq,
                           SubscriptionManager.partitionListToString(outPids),
                           SubscriptionManager.partitionListToString(inPids)});
        } else {
            logger.log(Level.FINE,
                       "TopologyTracker for TIF on RN {0} finished processing" +
                       " topology update seq # {1}, while no partition leaves" +
                       " or joins the group",
                       new Object[]{repNodeId.getFullName(), newTopoSeq});
        }

        /* Keeps this listener */
        return false;
    }

    /* Processes outgoing partitions due to topology change */
    private Set<PartitionId> processOutgoingPartitions(RepGroupId repGroupId,
                                                       Topology topo) {

        final long topoSeq = topo.getSequenceNumber();
        final Set<PartitionId> outgoingParts = new HashSet<>();
        final PartitionMap newPartMap = topo.getPartitionMap();

        for (PartitionId pid : tif.getSubManager().getManagedPartitions()) {

            if (!repGroupId.equals(newPartMap.getRepGroupId(pid))) {
                tif.removePartition(pid);
                outgoingParts.add(pid);
            }
        }

        logger.log(Level.FINE,
                   "Under topology seq# {0} all outgoing partitions have been" +
                   " processed: {1}",
                   new Object[]{topoSeq,
                       SubscriptionManager
                           .partitionListToString(outgoingParts)});

        return outgoingParts;
    }

    /* Processes incoming partitions due to topology change */
    private Set<PartitionId>  processIncomingPartitions(RepGroupId repGroupId,
                                                        Topology topo) {

        final long topoSeq = topo.getSequenceNumber();
        final Set<PartitionId> incomingParts = new HashSet<>();
        final Set<PartitionId> partitions = getPartitions(repGroupId, topo);

        for (PartitionId pid : partitions) {
            if (!tif.isManangedPartition(pid)) {
                tif.addPartition(pid);
                incomingParts.add(pid);
            } else {
                logger.log(Level.FINE,
                           "Existing partition {0} owned by rep group {1}, " +
                           "just ignore.",
                           new Object[]{pid, repGroupId});
            }
        }

        logger.log(Level.FINE,
                   "Under topology seq# {0} all incoming partitions " +
                   "processed: {1}",
                   new Object[]{topoSeq,
                       SubscriptionManager
                           .partitionListToString(incomingParts)});

        return incomingParts;
    }

    /* Gets list of partitions belongs to the rep group in topology */
    private Set<PartitionId> getPartitions(RepGroupId repGroupId,
                                           Topology topo) {

        final Set<PartitionId> ret = new HashSet<>();

        /* get all partitions belongs to my group */
        for (PartitionId pid : topo.getPartitionMap().getAllIds()) {
            if (repGroupId.equals(topo.getRepGroupId(pid))) {
                ret.add(pid);
            }
        }

        logger.log(Level.FINE,
                   "Under topology seq# {0}, all partitions owned by " +
                   "replication group {1}: {2}.",
                   new Object[]{topo.getSequenceNumber(), repGroupId,
                       SubscriptionManager.partitionListToString(ret)});
        return ret;
    }
}

