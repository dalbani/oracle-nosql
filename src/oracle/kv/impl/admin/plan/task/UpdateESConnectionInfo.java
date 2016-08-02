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

package oracle.kv.impl.admin.plan.task;

import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.param.StorageNodeParams;
import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.sna.StorageNodeAgentAPI;
import oracle.kv.impl.test.TestHook;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.util.registry.RegistryUtils;

/**
 * A task for creating and starting an instance of an Admin service on a
 * StorageNode.
 */
public class UpdateESConnectionInfo extends SingleJobTask {
    private static final long serialVersionUID = 1L;

    /* Hook to inject failures at different points in task execution */
    public static TestHook<Integer> FAULT_HOOK;

    private final AbstractPlan plan;
    private final StorageNodeId snid;
    private final String clusterName;
    private final String allTransports;

    public UpdateESConnectionInfo(AbstractPlan plan,
                                  StorageNodeId snid,
                                  String clusterName,
                                  String allTransports) {
        super();
        this.plan = plan;
        this.snid = snid;
        this.clusterName = clusterName;
        this.allTransports = allTransports;
    }

    @Override
    public State doWork()
        throws Exception {

        Admin admin = plan.getAdmin();
        Parameters p = admin.getCurrentParameters();
        StorageNodeParams snp = p.get(snid);
        snp.setSearchClusterMembers(allTransports);
        snp.setSearchClusterName(clusterName);
        admin.updateParams(snp, null);

        plan.getLogger().info("Changed searchClusterMembers for " + snid +
                              " to " + allTransports);

        /* Tell the SNA about it. */
        StorageNodeAgentAPI sna =
            RegistryUtils.getStorageNodeAgent(p, snid,
                                              plan.getLoginManager());
        sna.newStorageNodeParameters(snp.getMap());

        return State.SUCCEEDED;
    }

    @Override
    public boolean continuePastError() {
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " update clusterMembers for " + snid;
    }
}
