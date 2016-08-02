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

package oracle.kv.impl.topo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for methods related to {@link Topology}.
 */
public class TopologyUtil {

    /**
     * Return a map of replication groups to partitions Ids.
     */
    public static Map<RepGroupId, List<PartitionId>>
        getRGIdPartMap(Topology topology) {

        final Map<RepGroupId, List<PartitionId>> map =
            new HashMap<RepGroupId, List<PartitionId>>();

        for (Partition p : topology.getPartitionMap().getAll()) {
            List<PartitionId> list = map.get(p.getRepGroupId());

            if (list == null) {
                list = new ArrayList<PartitionId>();
                map.put(p.getRepGroupId(), list);
            }

            list.add(p.getResourceId());
        }

        return map;
    }

    /**
     * Returns the number of repNodes can be used for read operations.
     */
    public static int getNumRepNodesForRead(Topology topology,
                                            int[] readZoneIds) {
        final List<Integer> readZoneIdsLst;
        if (readZoneIds != null) {
            readZoneIdsLst = new ArrayList<Integer>(readZoneIds.length);
            for (int id : readZoneIds) {
                readZoneIdsLst.add(id);
            }
        } else {
            readZoneIdsLst = null;
        }

        final Collection<Datacenter> datacenters =
            topology.getDatacenterMap().getAll();
        int num = 0;
        for (Datacenter dc: datacenters) {
            if (readZoneIdsLst != null) {
                final int dcId = dc.getResourceId().getDatacenterId();
                if (!readZoneIdsLst.contains(dcId)) {
                    continue;
                }
            }
            num += dc.getRepFactor();
        }
        final int nShards = topology.getRepGroupMap().size();
        return num * nShards;
    }
}
