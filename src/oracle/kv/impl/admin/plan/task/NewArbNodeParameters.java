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

import java.rmi.RemoteException;
import java.util.logging.Level;

import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.arb.admin.ArbNodeAdminAPI;
import oracle.kv.impl.topo.ArbNodeId;
import oracle.kv.impl.util.registry.RegistryUtils;

/**
 * Send a simple newParameters call to the ArbNodeAdminAPI to refresh its
 * parameters without a restart.
 */
public class NewArbNodeParameters extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private final ArbNodeId targetNodeId;
    private final AbstractPlan plan;

    public NewArbNodeParameters(AbstractPlan plan,
                                ArbNodeId targetNodeId) {
        this.plan = plan;
        this.targetNodeId = targetNodeId;
    }

    @Override
    public State doWork()
        throws Exception {

        plan.getLogger().log(Level.FINE,
                             "Sending newParameters to {0}", targetNodeId);

        try {
            RegistryUtils registry =
                new RegistryUtils(plan.getAdmin().getCurrentTopology(),
                                  plan.getAdmin().getLoginManager());
            ArbNodeAdminAPI anAdmin = registry.getArbNodeAdmin(targetNodeId);
            anAdmin.newParameters();
        } catch (java.rmi.NotBoundException notbound) {
            plan.getLogger().info(targetNodeId +
                        " cannot be contacted when updating its parameters: " +
                        notbound);
            throw notbound;
        } catch (RemoteException e) {
            plan.getLogger().severe
                         ("Attempting to update parameters for targetNodeId:" +
                          e);
            throw e;
        }
        return State.SUCCEEDED;
    }

    @Override
    public String toString() {
        String retval = super.toString() + " cause " + targetNodeId;
        return retval + " to refresh its parameter state without restarting";
    }

    @Override
    public boolean continuePastError() {
        return false;
    }
}
