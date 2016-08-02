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

package oracle.kv.impl.arb;

import java.io.Serializable;

import com.sleepycat.je.rep.ReplicatedEnvironment.State;

import oracle.kv.impl.util.ConfigurableService.ServiceStatus;

/**
 * ArbNodeStatus represents the current status of a running ArbNodeService.  It
 * includes ServiceStatus as well as additional state specific to a ArbNode.
 */
public class ArbNodeStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ServiceStatus status;
    private final State arbState;
    private final long vlsn;
    private final String haHostPort;

    public ArbNodeStatus(ServiceStatus status, long vlsn,
                         State arbiterState, String haHostPort) {
        this.status = status;
        this.vlsn = vlsn;
        this.haHostPort = haHostPort;
        this.arbState = arbiterState;
    }

    public ServiceStatus getServiceStatus() {
        return status;
    }

    public State getArbiterState() {
        return arbState;
    }

    public long getVlsn() {
        return vlsn;
    }

    /**
     * Returns the HA host and port string.
     *
     * @return the HA host and port string or null
     */
    public String getHAHostPort() {
        return haHostPort;
    }

    @Override
    public String toString() {
        return status.toString() ;
    }
}
