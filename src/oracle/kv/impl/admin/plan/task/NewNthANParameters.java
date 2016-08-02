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

import java.util.List;

import oracle.kv.impl.admin.param.GlobalParams;
import oracle.kv.impl.admin.param.StorageNodeParams;
import oracle.kv.impl.admin.plan.DeployTopoPlan;
import oracle.kv.impl.arb.admin.ArbNodeAdminAPI;
import oracle.kv.impl.topo.ArbNodeId;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.registry.RegistryUtils;

/**
 * Send a simple newParameters call to the RepNodeAdminAPI to refresh its
 * parameters without a restart. Because the topology is not written until task
 * execution time, this flavor of NewRepNodeParameters must wait until task run
 * time to know the actual RepNodeId to use.
 */
public class NewNthANParameters extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private final int nthAN;
    private final int planShardIdx;
    private final DeployTopoPlan plan;

    /**
     * We don't have an actual RepGroupId and ArbNodeId to use at construction
     * time. Look those ids up in the plan at run time.
     */
    public NewNthANParameters(DeployTopoPlan plan,
                              int planShardIdx,
                              int nthAN) {
        this.plan = plan;
        this.planShardIdx = planShardIdx;
        this.nthAN = nthAN;
    }

    @Override
    public State doWork()
        throws Exception {

        Topology topo = plan.getAdmin().getCurrentTopology();
        RepGroupId rgId = plan.getShardId(planShardIdx);
        List<ArbNodeId> anList = topo.getSortedArbNodeIds(rgId);
        ArbNodeId targetANId = anList.get(nthAN);
        StorageNodeId hostSNId = topo.get(targetANId).getStorageNodeId();
        plan.getLogger().fine("Sending newParameters to " + targetANId);

        GlobalParams gp = plan.getAdmin().getParams().getGlobalParams();
        StorageNodeParams snp = plan.getAdmin().getStorageNodeParams(hostSNId);
        ArbNodeAdminAPI anai =
            RegistryUtils.getArbNodeAdmin(gp.getKVStoreName(),
                                          snp.getHostname(),
                                          snp.getRegistryPort(),
                                          targetANId,
                                          plan.getLoginManager());

        anai.newParameters();
        return State.SUCCEEDED;
    }

    @Override
    public boolean continuePastError() {
        return false;
    }
}
