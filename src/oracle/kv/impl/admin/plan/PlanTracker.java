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
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.kv.impl.admin.AdminServiceParams;
import oracle.kv.impl.admin.plan.task.Task;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.KVStoreUserPrincipal;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.util.server.LoggerUtils;
import oracle.kv.impl.util.server.LoggerUtils.SecurityLevel;

/**
 * Logs information about plan execution.
 */
public class PlanTracker implements ExecutionListener {

    private final Logger logger;

    public PlanTracker(AdminServiceParams adminParams) {
        logger = LoggerUtils.getLogger(this.getClass(), adminParams);
    }

    private String header(Plan plan) {
        return plan + ":";
    }

    /**
     * Only log execution state of plans that require SYSOPER and SYSDBA
     * privileges for auditing purpose. 
     */
    private boolean needAuditLogging(Plan plan) {
        final List<? extends KVStorePrivilege> requiredPrivs =
            plan.getRequiredPrivileges();

        return requiredPrivs.contains(SystemPrivilege.SYSOPER) ||
            requiredPrivs.contains(SystemPrivilege.SYSDBA);
    }

    @Override
    public void planStart(Plan plan) {
        int numTasks = plan.getTotalTaskCount();
        logger.log(Level.INFO, "{0} started, {1} tasks",
                   new Object[]{header(plan), numTasks});
    }

    @Override
    public void planEnd(Plan plan) {
        final ExecutionContext exeCtx = ExecutionContext.getCurrent();
        if (exeCtx == null || !needAuditLogging(plan)) {
            logger.log(Level.INFO, "{0} ended, state={1}",
                       new Object[]{header(plan), plan.getState()});
        } else {
            final KVStoreUserPrincipal user =
                ExecutionContext.getSubjectUserPrincipal(
                    exeCtx.requestorSubject());
            logger.log(SecurityLevel.SEC_INFO, "KVAuditInfo [{0}," +
                " owned by {1}, executed by {2} from {3} ended, state={4}]",
                new Object[]{plan,
                (plan.getOwner() == null) ? "" : plan.getOwner(),
                (user == null) ? "" : user.getName(),
                exeCtx.requestorHost(), plan.getState()});
        }
    }

    @Override
    public void taskStart(Plan plan,
                          Task task,
                          int taskNum,
                          int totalTasks) {
        logger.log(Level.INFO, "{0} task {1} [{2}] started",
                   new Object[]{header(plan), taskNum, task});
    }

    @Override
    public void taskEnd(Plan plan,
                        Task task,
                        TaskRun taskRun,
                        int taskNum,
                        int totalTasks) {
        logger.log(Level.INFO, "{0} task {1} [{2}] ended, state={3}",
                   new Object[]{header(plan), taskNum, task, 
                                taskRun.getState()});
    }
}
