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

import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.topo.ArbNodeId;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.RepNodeId;
import oracle.kv.impl.topo.ResourceId;

/**
 * A task for asking a ArbNode to update its helper hosts to include all its
 * peers.
 */
public class UpdateHelperHostV2 extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private final AbstractPlan plan;
    private final ResourceId resId;
    private final RepGroupId rgId;

    public UpdateHelperHostV2(AbstractPlan plan,
                              ResourceId resId,
                              RepGroupId rgId) {

        super();
        this.plan = plan;
        this.resId = resId;
        this.rgId = rgId;
    }

    /**
     */
    @Override
    public State doWork()
        throws Exception {
        if (resId.getType().isRepNode()) {
            Utils.updateHelperHost(plan.getAdmin(),
                                   plan.getAdmin().getCurrentTopology(),
                                   rgId,
                                   (RepNodeId)resId,
                                   plan.getLogger());
        } else {
            Utils.updateHelperHost(plan.getAdmin(),
                                   plan.getAdmin().getCurrentTopology(),
                                   rgId,
                                   (ArbNodeId)resId,
                                   plan.getLogger());
        }
        return State.SUCCEEDED;
    }

    @Override
    public boolean continuePastError() {
        return false;
    }
}
