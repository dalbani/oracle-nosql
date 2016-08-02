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

package oracle.kv.impl.topo.split;

import com.sleepycat.je.rep.ReplicatedEnvironment;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.kv.Consistency;
import oracle.kv.impl.rep.RepNodeStatus;
import oracle.kv.impl.rep.admin.RepNodeAdminAPI;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.topo.RepGroup;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.RepNode;
import oracle.kv.impl.topo.StorageNode;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.registry.RegistryUtils;

/**
 * An object that represents a portion of a topology which can be operated on
 * in parallel with other splits. A split consists of one or more sets of
 * partitions. In order to minimize contention with other processing the
 * sets of partitions should be processed in order. The partitions within a
 * set can be processed in parallel with each other.
 */
public class TopoSplit {

    /* Split id, for debugging */
    private final int id;

    /* Lists of partition sets */
    private final List<Set<Integer>> partitionSets;

    TopoSplit(int id) {
        this.id = id;
        partitionSets = new ArrayList<Set<Integer>>();
    }

    TopoSplit(int id, Set<Integer> partitionSet) {
        this(id);
        partitionSets.add(partitionSet);
    }

    /**
     * Returns true if there are no partition sets in this split, otherwise
     * false.
     *
     * @return true if there are no partition sets in this split
     */
    public boolean isEmpty() {
        return partitionSets.isEmpty();
    }

    int getId() {
        return id;
    }

    void add(Set<Integer> set) {
        partitionSets.add(set);
    }

    void addAll(List<Set<Integer>> pSets) {
        partitionSets.addAll(pSets);
    }

    int size() {
        return partitionSets.size();
    }

    /**
     * Gets the list of partition sets in this split.
     *
     * @return the list of partition sets
     */
    public List<Set<Integer>> getPartitionSets() {
        return partitionSets;
    }

    Set<Integer> getPartitionSet(int slot) {
        return (slot >= partitionSets.size()) ? null : partitionSets.get(slot);
    }

    /**
     * Gets the set of storage nodes which house the partitions in this split.
     * The set is filtered by the specified consistency.
     *
     * @return the set of storage nodes which house the partitions in this split
     */
    public Set<StorageNode> getSns(Consistency consistency,
                                   Topology topology,
                                   RegistryUtils regUtils) {
        final Set<StorageNode> sns = new HashSet<StorageNode>();

        for (Set<Integer> set : partitionSets) {
            for (Integer i : set) {
                final PartitionId partId = new PartitionId(i);
                final RepGroupId repGroupId = topology.getRepGroupId(partId);

                if (repGroupId == null) {
                    throw new IllegalArgumentException("Topology has not been" +
                                                       " initialized");
                }
                final RepGroup repGroup = topology.get(repGroupId);
                final Collection<RepNode> repNodes = repGroup.getRepNodes();

                for (RepNode rn : repNodes) {
                    RepNodeStatus rnStatus = null;
                    try {
                        final RepNodeAdminAPI rna =
                            regUtils.getRepNodeAdmin(rn.getResourceId());
                        rnStatus = rna.ping();
                    } catch (RemoteException re) {
                        System.err.println("Ping failed for " +
                                           rn.getResourceId() + ": " +
                                           re.getMessage());
                    } catch (NotBoundException e) {
                        System.err.println("No RMI service for RN: " +
                                           rn.getResourceId() +
                                           " message: " + e.getMessage());
                    }

                    if (rnStatus == null) {
                        continue;
                    }

                    final ReplicatedEnvironment.State state =
                                            rnStatus.getReplicationState();
                    if (!state.isActive() ||
                        (consistency == Consistency.NONE_REQUIRED_NO_MASTER &&
                         state.isMaster()) ||
                        (consistency == Consistency.ABSOLUTE &&
                         !state.isMaster())) {
                        continue;
                    }

                    final StorageNodeId snid = rn.getStorageNodeId();
                    final StorageNode sn = topology.get(snid);
                    sns.add(sn);
                }
            }
        }
        return sns;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TopoSplit[");
        sb.append(id);
        sb.append(", ");
        for (Set<Integer> set : partitionSets) {
            sb.append("\n\t");
            sb.append(set);
        }
        sb.append("]");
        return sb.toString();
    }
}
