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

import java.net.URI;
import java.util.logging.Level;

import oracle.kv.impl.admin.plan.Plan;
import oracle.kv.impl.security.PasswordRenewer;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.impl.util.ServiceUtils;

/**
 *Provide password renewal on Admin node.
 */
public class AdminPasswordRenewer implements PasswordRenewer {

    /* The AdminService being supported */
    private final AdminService adminService;

    /**
     * Construct an AdminPasswordRenewer supporting the provided AdminService
     * instance.
     */
    public AdminPasswordRenewer(AdminService aService) {
        this.adminService = aService;
    }

    @Override
    public boolean renewPassword(String userName, char[] newPassword) {
        final URI masterURI = adminService.getAdmin().getMasterRmiAddress();
        CommandServiceAPI cs = null;
        int planId = 0;
        try {
            cs = ServiceUtils.waitForAdmin(masterURI.getHost(),
                                           masterURI.getPort(),
                                           adminService.getLoginManager(),
                                           40,
                                           ServiceStatus.RUNNING);

            /* Execute change-user plan to renew user's password */
            planId = cs.createChangeUserPlan("renew-password", userName,
               true /*isEnabled*/, newPassword, false /* retainPassword */,
               false /* clearRetainedPassword */);

            assert planId != 0;
            cs.approvePlan(planId);
            cs.executePlan(planId, false /* force */);
            final Plan.State state = cs.awaitPlan(planId, 0, null);

            /* Ignore other plan execution end state, treat them as failure. */
            if (state == Plan.State.SUCCEEDED) {
                return true;
            }
            cancelPlan(cs, planId);
        } catch (Exception e) {
            logPasswordRenewFailure(userName, e);
            cancelPlan(cs, planId);
        }
        return false;
    }

    /**
     * Log password renewal failure.
     */
    private void logPasswordRenewFailure(String userName, Exception e) {
        if (e == null) {
            return;
        }
        logMsg(Level.SEVERE, "Attempting to change password of user " +
                userName + " failed: " + e);
    }

    private void cancelPlan(CommandServiceAPI cs, int planId) {
        if (cs == null || planId == 0) {
            return;
        }

        try {
            cs.cancelPlan(planId);
        } catch (Exception e) {
            logCancelPlanFailure(planId, e);
        }
    }

    private void logCancelPlanFailure(int planId, Exception e) {
        if (planId != 0 && e != null) {
            return;
        }
        logMsg(Level.INFO, "Attempts to cancel plan of renew password " +
                planId + " failed: " + e.getMessage());
    }

    /**
     * Log a message, if a logger is available.
     */
    private void logMsg(Level level, String msg) {
        if (adminService != null) {
            adminService.getLogger().log(level, msg);
        }
    }
}
