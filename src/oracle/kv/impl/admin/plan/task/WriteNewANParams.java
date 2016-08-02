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
import oracle.kv.impl.admin.param.ArbNodeParams;
import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.param.ParameterMap;
import oracle.kv.impl.security.login.LoginManager;
import oracle.kv.impl.sna.StorageNodeAgentAPI;
import oracle.kv.impl.topo.ArbNodeId;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.registry.RegistryUtils;

/**
 * A task for asking a storage node to write a new configuration file.
 */
public class WriteNewANParams extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private final AbstractPlan plan;
    private final ParameterMap newParams;
    private final StorageNodeId targetSNId;
    private final ArbNodeId anid;
    private final boolean continuePastError;

    public WriteNewANParams(AbstractPlan plan,
                            ParameterMap newParams,
                            ArbNodeId anid,
                            StorageNodeId targetSNId,
                            boolean continuePastError) {
        super();
        this.plan = plan;
        this.newParams = newParams;
        this.anid = anid;
        this.targetSNId = targetSNId;
        this.continuePastError = continuePastError;
    }

    /**
     */
    @Override
    public State doWork()
        throws Exception {

        if (!writeNewANParams(plan, newParams, anid, targetSNId)) {
            return State.ERROR;
        }
        return State.SUCCEEDED;
    }

    public static boolean writeNewANParams(AbstractPlan plan,
                                         ParameterMap newParams,
                                         ArbNodeId anid,
                                         StorageNodeId targetSNId)
        throws Exception {

        Admin admin = plan.getAdmin();
        ArbNodeParams anp = admin.getArbNodeParams(anid);
        ParameterMap anMap = anp.getMap();
        ArbNodeParams newAnp = new ArbNodeParams(newParams);
        newAnp.setArbNodeId(anid);
        ParameterMap diff = anMap.diff(newParams, true);
        plan.getLogger().info("Changing params for " + anid + ": " + diff);

        /*
         * Merge and store the changed rep node params in the admin db before
         * sending them to the SNA.
         */
        anMap.merge(newParams, true);
        admin.updateParams(anp);

        /* Ask the SNA to write a new configuration file. */
        Topology topology = admin.getCurrentTopology();
        LoginManager loginMgr = admin.getLoginManager();
        RegistryUtils registryUtils = new RegistryUtils(topology, loginMgr);

        StorageNodeAgentAPI sna =
            registryUtils.getStorageNodeAgent(targetSNId);
        sna.newArbNodeParameters(anMap);

        return true;
    }

    @Override
    public boolean continuePastError() {
        return continuePastError;
    }

    @Override
    public String toString() {
       return super.toString() +
           " write new " + anid + " parameters into the Admin database: " +
           newParams.showContents();
    }
}
