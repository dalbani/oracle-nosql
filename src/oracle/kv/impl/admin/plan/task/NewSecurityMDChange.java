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

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.CommandServiceAPI;
import oracle.kv.impl.admin.param.AdminParams;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.param.StorageNodeParams;
import oracle.kv.impl.admin.plan.SecurityMetadataPlan;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.impl.util.ServiceUtils;

import com.sleepycat.persist.model.Persistent;

/**
 * Send a simple newSecurityMDChange call to the CommandServiceAPI to notify
 * new security metadata change.
 *
 * This task should be executed right after updating security metadata task.
 */
@Persistent
public class NewSecurityMDChange extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private AdminId adminId;
    private SecurityMetadataPlan plan;

    /* For DPL */
    NewSecurityMDChange() {
    }

    public NewSecurityMDChange(SecurityMetadataPlan plan,
                               AdminId adminId) {
        this.plan = plan;
        this.adminId = adminId;
    }

    @Override
    public State doWork()
        throws Exception {

        final Admin admin = plan.getAdmin();
        final Logger logger = plan.getLogger();
        final Parameters parameters = admin.getCurrentParameters();
        final int latestSequenceNum = plan.getMetadata().getSequenceNumber();
        logger.fine("Notify target " +  adminId +
            "security metadata change whose seq number is " + 
            latestSequenceNum);

        try {
            final AdminParams current = parameters.get(adminId);
            final StorageNodeId snid = current.getStorageNodeId();
            final StorageNodeParams snp = parameters.get(snid);
            final CommandServiceAPI cs =
                ServiceUtils.waitForAdmin(snp.getHostname(),
                                          snp.getRegistryPort(),
                                          plan.getLoginManager(),
                                          40,
                                          ServiceStatus.RUNNING);
            cs.newSecurityMDChange();
        } catch (NotBoundException notbound) {
            logger.info(adminId + " cannot be contacted when " +
                "notifying about security metadata change: " + notbound);
            throw notbound;
        } catch (RemoteException e) {
            logger.severe(
                "Attempting to notify " + adminId + " about " +
                "security metadata change: " + e);
            throw e;
        }
        return State.SUCCEEDED;
    }

    @Override
    public String toString() {
       return super.toString() + " cause " + adminId +
           " to apply the latest security metadata change";
    }

    @Override
    public boolean continuePastError() {
        return false;
    }

    /**
     * Returns true if this NewSecurityMDChange will end up call the same
     * admin to notify the security metadata change. Checks that admin Id
     * are the same.
     */
    @Override
    public boolean logicalCompare(Task t) {
        if (this == t) {
            return true;
        }

        if (t == null) {
            return false;
        }

        if (getClass() != t.getClass()) {
            return false;
        }
        NewSecurityMDChange other = (NewSecurityMDChange) t;

        return adminId.equals(other.adminId);
    }
}
