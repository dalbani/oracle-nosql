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

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;

import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.admin.param.AdminParams;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.plan.ChangeAdminParamsPlan;
import oracle.kv.impl.sna.StorageNodeAgentAPI;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.util.registry.RegistryUtils;

import com.sleepycat.persist.model.Persistent;

/**
 * A task for starting a given Admin. Assumes the node has already been created.
 *
 * NOTE: This class is for backward compatibility only, it has been replaced
 * by StartAdminV2.
 *
 * version 0: original.
 * version 1: Changed inheritance chain.
 */
@Persistent(version=1)
public class StartAdmin extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private ChangeAdminParamsPlan plan;
    private StorageNodeId snId;
    private AdminId adminId;
    private boolean continuePastError;

    /*
     * Unused constructor to clarify that no instances should be created.
     * Callers should use StartAdminV2.
    */
    @SuppressWarnings("unused")
    private StartAdmin(ChangeAdminParamsPlan plan,
                       StorageNodeId storageNodeId,
                       AdminId adminId,
                       boolean continuePastError) {
        throw new AssertionError("Use StartAdminV2");
    }

    /* DPL */
    protected StartAdmin() {
    }

    @Override
    public State doWork()
        throws Exception {

        final Set<AdminId> needsAction = plan.getNeedsActionSet();

        /*
         * We won't perform the action unless the aid is in the needsAction set.
         */
        if (needsAction.contains(adminId)) {
            start(plan, adminId, snId);
        }
        return State.SUCCEEDED;
    }

    /*
     * Starts the specified Admin.
     */
    public static void start(AbstractPlan plan,
                             AdminId adminId,
                             StorageNodeId snId)
        throws RemoteException, NotBoundException {
        /*
         * Update params to indicate that this admin is now enabled,
         * and save the changes.
         */
        Admin admin = plan.getAdmin();
        Parameters parameters = admin.getCurrentParameters();
        AdminParams ap = parameters.get(adminId);
        ap.setDisabled(false);
        admin.updateParams(ap);

        /* Tell the SNA to start the admin. */
        StorageNodeAgentAPI sna =
            RegistryUtils.getStorageNodeAgent(parameters, snId,
                                              plan.getLoginManager());

        sna.startAdmin();
    }

    @Override
    public boolean continuePastError() {
        return continuePastError;
    }

    @Override
    public String toString() {
       return super.toString() +
           " start " + adminId;
    }
}
