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
package oracle.kv.impl.admin.plan.task;

import java.util.Set;

import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.admin.plan.ChangeAdminParamsPlan;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.StorageNodeId;

/**
 * A task for starting a given Admin. Assumes the node has already been created.
 */
public class StartAdminV2 extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private AbstractPlan plan;
    private StorageNodeId snId;
    private AdminId adminId;
    private boolean continuePastError;

    public StartAdminV2(AbstractPlan plan,
                        StorageNodeId storageNodeId,
                        AdminId adminId,
                        boolean continuePastError) {
        super();
        this.plan = plan;
        this.snId = storageNodeId;
        this.adminId = adminId;
        this.continuePastError = continuePastError;
    }

    @Override
    public State doWork() throws Exception {

        final Set<AdminId> needsAction =
                (plan instanceof ChangeAdminParamsPlan) ?
                       ((ChangeAdminParamsPlan)plan).getNeedsActionSet() : null;

        /*
         * If this is a ChangeAdminParamsPlan we won't perform
         * the action unless the aid is in the needsAction set.
         */
        if ((needsAction == null) || needsAction.contains(adminId)) {
            StartAdmin.start(plan, adminId, snId);
        }
        return State.SUCCEEDED;
    }

    @Override
    public boolean continuePastError() {
        return continuePastError;
    }

    @Override
    public String toString() {
       return super.toString() + " start " + adminId;
    }
}
