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

package oracle.kv.impl.client.admin;

import java.net.URI;
import java.rmi.RemoteException;

import oracle.kv.impl.security.AuthContext;
import oracle.kv.impl.util.registry.VersionedRemote;

/**
 * Defines the RMI interface used by the kvclient to asynchronously submit
 * DDL statements, which will be executed by the Admin service.
 */
public interface ClientAdminService extends VersionedRemote {

    /**
     * Ask the master Admin to execute the statement.
     */
    ExecutionInfo execute(String statement,
                          AuthContext authCtx,
                          short serialVersion)
        throws RemoteException;

    /**
     * Get current status for the specified plan.
     */
    ExecutionInfo getExecutionStatus(int planId,
                                     AuthContext authCtx,
                                     short serialVersion)
        throws RemoteException;

    /**
     * Return true if this Admin can handle DDL operations. That currently
     * equates to whether the Admin is a master or not.
     * 
     * @param authCtx
     * @param serialVersion
     * @throws RemoteException
     */
    boolean canHandleDDL(AuthContext authCtx, short serialVersion)
            throws RemoteException;

    /**
     * Return the address of the master Admin. If this Admin doesn't know that,
     * return null.
     */
    URI getMasterRmiAddress(AuthContext authCtx, short serialVersion)
            throws RemoteException;

    /**
     * Start cancellation of a plan. Return the current status.
     */
    ExecutionInfo interruptAndCancel(int planId,
                                     AuthContext nullCtx,
                                     short serialVersion)
            throws RemoteException;
}
