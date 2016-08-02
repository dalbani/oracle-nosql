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

package oracle.kv.impl.admin;

import java.io.Serializable;

import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.util.PingDisplay;
import com.sleepycat.je.rep.ReplicatedEnvironment.State;

/**
 * Represents the current status of a running AdminService.  It includes
 * ServiceStatus as well as additional state specific to an Admin.
 */
public class AdminStatus implements Serializable, PingDisplay.ServiceInfo {

    private static final long serialVersionUID = 1L;
    private final ServiceStatus status;
    private final State state;

    /**
     * JE HA information about whether this is an authoritative master.  Only
     * meaningful if state is non-null and is MASTER.
     */
    private final boolean isAuthoritativeMaster;

    public AdminStatus(ServiceStatus status,
                       State state,
                       boolean isAuthoritativeMaster) {
        this.status = status;
        this.state = state;
        this.isAuthoritativeMaster =
            isAuthoritativeMaster && (state == State.MASTER);
    }

    @Override
    public ServiceStatus getServiceStatus() {
        return status;
    }

    @Override
    public State getReplicationState() {
        return state;
    }

    /**
     * Returns whether this node is the authoritative master.  Always returns
     * false if the state shows that the node is not the master.
     */
    @Override
    public boolean getIsAuthoritativeMaster() {
        return isAuthoritativeMaster;
    }

    @Override
    public String toString() {
        return status + "," + state +
            (((state == State.MASTER) && !isAuthoritativeMaster) ?
             " (non-authoritative)" : "");
    }
}
