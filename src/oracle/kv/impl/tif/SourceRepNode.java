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

package oracle.kv.impl.tif;

import java.util.Set;
import java.util.UUID;

import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.fault.RNUnavailableException;
import oracle.kv.impl.metadata.Metadata;
import oracle.kv.impl.rep.PartitionManager;
import oracle.kv.impl.rep.RepNode;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.topo.RepNodeId;

import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.je.rep.ReplicationNode;

/**
 * Object to represent all information from source rep node that are needed by
 * TextIndexFeeder. The source may be either local in which case TIF co-locate
 * with the master, or remote in which case TIF may run on a remote rep node
 * other than the master. In the latter case, all info about the source node
 * need to be populated by querying the remote source.
 */
class SourceRepNode {

    private final boolean remote;
    private final String storeName;
    private final RepNodeId repNodeId;
    private final Set<PartitionId> partitionIdSet;
    private final String groupName;
    private final UUID groupUUID;
    private final String sourceNodeName;
    private final String sourceHost;
    private final int sourcePort;

    /* max concurrent partition transfer source allows */
    private final int concurrentSourceLimit;

    /*
     * it is unclear how to get partition manager and table metadata from
     * remote node, we need be able to map the key to the partition on source
     * node.
     */
    private final PartitionManager partitionManager;
    private final TableMetadata tableMetadata;

    /* sequence number of the topology source RN is using */
    private final long topoSeq;

    /**
     * Constructor used if TIF co-locate with source node and is able to
     * get the RepNode from environment.
     *
     * @param storeName     name of store to stream data from
     * @param sourceRN  source RepNode with which TIF co-locate
     */
    SourceRepNode(String storeName, RepNode sourceRN) {
        remote = false;
        this.storeName = storeName;
        repNodeId = sourceRN.getRepNodeId();
        partitionIdSet = sourceRN.getPartitions();
        partitionManager = sourceRN.getPartitionManager();

        final ReplicatedEnvironment sourceNodeEnv = sourceRN.getEnv(60000);
        if (sourceNodeEnv == null) {
            throw new RNUnavailableException("Source node environment " +
                                             "unavailable while initializing " +
                                             " source rep node");
        }

        groupName = sourceNodeEnv.getGroup().getName();
        groupUUID = sourceNodeEnv.getGroup().getRepGroupImpl().getUUID();
        sourceNodeName = sourceNodeEnv.getNodeName();
        final ReplicationNode node =
            sourceNodeEnv.getGroup().getMember(sourceNodeName);
        sourceHost = node.getHostName();
        sourcePort = node.getPort();
        tableMetadata =
            (TableMetadata) sourceRN.getMetadata(Metadata.MetadataType.TABLE);
        concurrentSourceLimit = sourceRN.getRepNodeParams()
                                        .getConcurrentSourceLimit();
        topoSeq = sourceRN.getTopology().getSequenceNumber();
    }

    /**
     * TODO: in next release!
     * Constructor used if TIF runs remotely on a rep node other the source.
     *
     SourceRepNode(String store, RepNodeAdminAPI remoteRNAPI) {
        remote = true;
        storeName = store;
        repNodeId = null;
        partitionIdSet = null;
        partitionManager = null;
        groupName = null;
        sourceNodeName = null;
        groupUUID = null;
        sourceHost = null;
        sourcePort = 0;
        tableMetadata = null;
        concurrentSourceLimit = 1;
    }
    */

    public boolean isRemote() {
        return remote;
    }

    public String getStoreName() {
        return storeName;
    }

    public RepNodeId getRepNodeId() {
        return repNodeId;
    }

    public Set<PartitionId> getPartitionIdSet() {
        return partitionIdSet;
    }

    public PartitionId getPartitionId(byte[] keyBytes) {
        return partitionManager.getPartitionId(keyBytes);
    }

    public String getGroupName() {
        return groupName;
    }

    public UUID getGroupUUID() {
        return groupUUID;
    }

    public String getSourceNodeName() {
        return sourceNodeName;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public TableMetadata getTableMetadata() {
        return tableMetadata;
    }

    public int getConcurrentSourceLimit() {
        return concurrentSourceLimit;
    }

    public long getTopoSequence() {
        return topoSeq;
    }

    @Override
    public String toString() {
        return  "remote: " + isRemote() +
                "\nfeeder node: " + sourceNodeName +
                "\nhost port: " + sourceHost + ":" + sourcePort +
                "\ngroup name (gid): " + groupName + "(" + groupUUID + ")" +
                "\nnumber of partitions: " + partitionIdSet.size() +
                "\nlist of partitions: " +
                SubscriptionManager.partitionListToString(partitionIdSet) +
                "\ntopology seq#: " + topoSeq;
    }

}
