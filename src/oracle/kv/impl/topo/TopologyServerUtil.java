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

import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/**
 * A utility class for methods related to {@link Topology} that should only be
 * called on the server side.  This class is not included in the client-side
 * JAR file, which means that the JE EntityStore and Transaction classes are
 * not needed there either.
 */
public class TopologyServerUtil {

    /**
     * Persist a Topology in the environment. The Topology is persisted in the
     * entity store named: <code>TOPOLOGY_STORE_NAME</code> as the value
     * associated with the key: <code>TOPOLOGY_KEY</code>. If the store does
     * not exist, a new store is created to hold the Topology. If one exists,
     * the Topology object in it is updated.
     * <p>
     * Note that the entire Topology, which can be a large object, is stored as
     * a single value. This matches the needs of the typical user of Topology
     * which need access to the entire object in memory.
     *
     * @param topology the Topology
     * @param estore the entity store that holds the Topology
     * @param txn the transaction in progress
     */
    public static void persist(Topology topology,
                               EntityStore estore,
                               Transaction txn) {
        final PrimaryIndex<String, TopologyHolder> ti =
            estore.getPrimaryIndex(String.class, TopologyHolder.class);
        ti.put(txn, new TopologyHolder(topology));
    }

    /**
     * Fetches a previously <code>persisted</code> Topology from the
     * environment. The version number associated with the Topology should be
     * Topology.CURRENT_VERSION, expect when the topology is being upgraded in
     * which case it could be an earlier version. Upgrade code paths need to
     * check the version and tale appropriate action.
     *
     * @param estore the entity store that holds the Topology
     * @param txn the transaction to be used to fetch the Topology. It may be
     * null if the database it's being fetched from is non-transactional as is
     * the case with topology stored at RNs.
     *
     * @see #persist
     */
    public static Topology fetch(EntityStore estore, Transaction txn) {
        final PrimaryIndex<String, TopologyHolder> ti =
            estore.getPrimaryIndex(String.class, TopologyHolder.class);
        final TopologyHolder holder =
            ti.get(txn, TopologyHolder.getKey(), LockMode.READ_UNCOMMITTED);

        return (holder == null) ? null : holder.getTopology();
    }

    public static Topology fetchCommitted(EntityStore estore,
                                          Transaction txn) {
        final PrimaryIndex<String, TopologyHolder> ti =
            estore.getPrimaryIndex(String.class, TopologyHolder.class);
        final TopologyHolder holder =
            ti.get(txn, TopologyHolder.getKey(), LockMode.READ_COMMITTED);

        return (holder == null) ? null : holder.getTopology();
    }
}
