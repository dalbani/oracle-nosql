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

package oracle.kv.impl.arb.admin;

import java.rmi.RemoteException;

import oracle.kv.impl.mgmt.ArbNodeStatusReceiver;
import oracle.kv.impl.param.LoadParameters;
import oracle.kv.impl.security.AuthContext;
import oracle.kv.impl.security.ContextProxy;
import oracle.kv.impl.arb.ArbNodeStatus;
import oracle.kv.impl.security.login.LoginHandle;
import oracle.kv.impl.util.registry.RemoteAPI;

/**
 * The administrative interface to a ArbNode process.
 */
public class ArbNodeAdminAPI extends RemoteAPI {

    /* Null value that will be filled in by proxyRemote */
    private final static AuthContext NULL_CTX = null;

    private final ArbNodeAdmin proxyRemote;

    private ArbNodeAdminAPI(ArbNodeAdmin remote, LoginHandle loginHdl)
        throws RemoteException {

        super(remote);
        this.proxyRemote = ContextProxy.create(remote, loginHdl,
                                               getSerialVersion());
    }

    public static ArbNodeAdminAPI wrap(ArbNodeAdmin remote,
                                       LoginHandle loginHdl)
        throws RemoteException {

        return new ArbNodeAdminAPI(remote, loginHdl);
    }

    /**
     * Notifies the AN that new parameters are available in the storage node
     * configuration file and that these should be reread.
     */
    public void newParameters()
        throws RemoteException {

        proxyRemote.newParameters(NULL_CTX, getSerialVersion());
    }

    /**
     * Notifies the AN that new global parameters are available in the storage
     * node configuration file and that these should be reread.
     */
    public void newGlobalParameters()
        throws RemoteException {

        proxyRemote.newGlobalParameters(NULL_CTX, getSerialVersion());
    }

    /**
     * Returns the in-memory values of the parameters for the AN. Used for
     * configuration verification.
     */
    public LoadParameters getParams()
        throws RemoteException {
        return proxyRemote.getParams(NULL_CTX, getSerialVersion());
    }

    /**
     * Shuts down this ArbNode process cleanly.
     *
     * @param force force the shutdown
     */
    public void shutdown(boolean force)
        throws RemoteException {

        proxyRemote.shutdown(force, NULL_CTX, getSerialVersion());
    }

    /**
     * Returns the <code>ArbNodeStatus</code> associated with the rep node.
     *
     * @return the service status
     */
    public ArbNodeStatus ping()
        throws RemoteException {

        return proxyRemote.ping(NULL_CTX, getSerialVersion());
    }

    /**
     * Returns administrative and configuration information from the
     * arbNode. Meant for diagnostic and debugging support.
     */
    public ArbNodeInfo getInfo()
        throws RemoteException {

        return proxyRemote.getInfo(NULL_CTX, getSerialVersion());
    }

    /**
     * @param groupName
     * @param targetNodeName
     * @param targetHelperHosts
     * @param newNodeHostPort if null entry is removed.
     * @return true if this node's address can be updated in the JE
     * group database, false if there is no current master, and we need to
     * retry.
     * @throws RemoteException
     */
    public boolean updateMemberHAAddress(String groupName,
                                         String targetNodeName,
                                         String targetHelperHosts,
                                         String newNodeHostPort)
        throws RemoteException{

        return proxyRemote.updateMemberHAAddress(groupName,
                                                 targetNodeName,
                                                 targetHelperHosts,
                                                 newNodeHostPort,
                                                 NULL_CTX,
                                                 getSerialVersion());
    }

    /**
     * Install a receiver for ArbNode status updates, for delivering metrics
     * and service change information to the standardized monitoring/management
     * agent.
     */
    public void installStatusReceiver(ArbNodeStatusReceiver receiver)
        throws RemoteException {

        proxyRemote.installStatusReceiver(receiver, NULL_CTX,
                                          getSerialVersion());
    }

}
