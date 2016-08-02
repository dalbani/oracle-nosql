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

import oracle.kv.impl.admin.CommandResult;
import oracle.kv.impl.admin.CommandResult.CommandFails;
import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.topo.ResourceId;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.util.ErrorMessage;

/**
 * Monitors the state of a RepNode or ArbNode, blocking until a certain state
 * has been reached.
 */
public class WaitForNodeState extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    /**
     * The node that is to be monitored
     */
    private final ResourceId targetNodeId;

    /**
     * The state the node must be in before finishing this task
     */
    private final ServiceStatus targetState;
    private final AbstractPlan plan;

    /**
     * Creates a task that will block until a given Node has reached
     * a given state.
     *
     * @param desiredState the state to wait for
     */
    public WaitForNodeState(AbstractPlan plan,
                            ResourceId targetNodeId,
                            ServiceStatus desiredState) {
        this.plan = plan;
        this.targetNodeId = targetNodeId;
        this.targetState = desiredState;
    }

    @Override
    public State doWork()
        throws Exception {
        State state =
            Utils.waitForNodeState(plan, targetNodeId, targetState);
            if (state == State.ERROR) {
                final String msg = "RepNode " + targetNodeId +
                    " failed to reach " + targetState;
                final CommandResult taskResult =new CommandFails(
                    msg, ErrorMessage.NOSQL_5400, CommandResult.PLAN_CANCEL);
                setTaskResult(taskResult);
            }
            return state;
    }

    @Override
    public String toString() {
       return super.toString() + " waits for " + targetNodeId + " to reach " +
           targetState + " state";
    }

    @Override
    public boolean continuePastError() {
        return true;
    }
}
