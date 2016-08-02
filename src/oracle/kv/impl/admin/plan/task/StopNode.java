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

import oracle.kv.impl.admin.PlanLocksHeldException;
import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.admin.plan.Planner;
import oracle.kv.impl.topo.ArbNodeId;
import oracle.kv.impl.topo.RepNodeId;
import oracle.kv.impl.topo.ResourceId;
import oracle.kv.impl.topo.StorageNodeId;

/**
 * A task for stopping a given RepNode or ArbNode
 */
public class StopNode extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private final AbstractPlan plan;
    private final StorageNodeId snId;
    private final ResourceId resId;
    private final boolean continuePastError;

    /**
     * We expect that the target Node exists before StopNode is
     * executed.
     * @param continuePastError if true, if this task fails, the plan
     * will stop.
     */
    public StopNode(AbstractPlan plan,
                    StorageNodeId snId,
                    ResourceId resId,
                    boolean continuePastError) {
        super();
        this.plan = plan;
        this.snId = snId;
        this.resId = resId;
        this.continuePastError = continuePastError;
    }

    @Override
    public State doWork()
        throws Exception {

        if (resId.getType().isRepNode()) {
            // TODO - Survey usages of this task to see if it should wait for
            // nodes to be consistent, stopRN(..., true).
            Utils.stopRN(plan, snId, (RepNodeId)resId, false);
        } else {
            Utils.stopAN(plan, snId, (ArbNodeId)resId);
        }

        return State.SUCCEEDED;
    }

    @Override
    public boolean continuePastError() {
        return continuePastError;
    }

    @Override
    public String getName() {
        return super.getName() + " " + resId;
    }

    @Override
    public String toString() {
        return super.toString() + " " + resId;
    }

    @Override
    public void lockTopoComponents(Planner planner)
        throws PlanLocksHeldException {
        planner.lock(plan.getId(), plan.getName(), resId);
    }
}
