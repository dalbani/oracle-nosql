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

package oracle.kv.impl.admin.plan;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.param.AdminParams;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.plan.task.ParallelBundle;
import oracle.kv.impl.admin.plan.task.StartAdminV2;
import oracle.kv.impl.admin.plan.task.StartRepNode;
import oracle.kv.impl.admin.plan.task.WaitForAdminState;
import oracle.kv.impl.admin.plan.task.WaitForRepNodeState;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.RepNode;
import oracle.kv.impl.topo.RepNodeId;
import oracle.kv.impl.topo.ResourceId;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;

/**
 * Start the specified services
 */
public class StartServicesPlan extends AbstractPlan {
    private static final long serialVersionUID = 1L;

    StartServicesPlan(AtomicInteger idGen,
                      String name,
                      Planner planner,
                      Topology topology,
                      Set<? extends ResourceId> serviceIds) {
        super(idGen, name, planner);

        final Parameters dbParams = getAdmin().getCurrentParameters();

        final ParallelBundle startTasks = new ParallelBundle();
        final ParallelBundle waitTasks = new ParallelBundle();

        for (ResourceId id : serviceIds) {

            if (id instanceof RepNodeId) {
                final RepNodeId rnId = (RepNodeId)id;
                final RepNode rn = topology.get(rnId);

                if (rn == null) {
                    throw new IllegalCommandException
                        ("There is no RepNode with id " + rnId +
                         ". Please provide the id of an existing RepNode.");
                }

                startTasks.addTask(new StartRepNode(this, rn.getStorageNodeId(),
                                                    rnId, true));
                waitTasks.addTask(new WaitForRepNodeState(this, rnId,
                                                        ServiceStatus.RUNNING));
            } else if (id instanceof AdminId) {
                final AdminId adminId = (AdminId)id;
                final AdminParams adminDbParams = dbParams.get(adminId);

                if (adminDbParams == null) {
                    throw new IllegalCommandException
                        ("There is no Admin with id " + adminId +
                         ". Please provide the id of an existing Admin.");
                }
                final StorageNodeId snId = adminDbParams.getStorageNodeId();
                if (snId == null) {
                    throw new IllegalCommandException
                        ("Storage node not found for Admin with id " + adminId);
                }
                startTasks.addTask(new StartAdminV2(this, snId, adminId, true));
                waitTasks.addTask(new WaitForAdminState(this, snId, adminId,
                                                        ServiceStatus.RUNNING));
            } else {
                throw new IllegalCommandException
                        ("Command not supported for " + id);
            }
        }
        addTask(startTasks);
        addTask(waitTasks);
    }

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    void preExecutionSave() {
        /* Nothing to do. */
    }

    @Override
    public String getDefaultName() {
        return "Start Services";
    }

    @Override
    void stripForDisplay() {
    }

    @Override
    public List<? extends KVStorePrivilege> getRequiredPrivileges() {
        /* Requires SYSOPER */
        return SystemPrivilege.sysoperPrivList;
    }
}
