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

import oracle.kv.impl.admin.plan.Plan;
import oracle.kv.impl.client.admin.ClientAdminService;
import oracle.kv.impl.client.admin.ExecutionInfo;
import oracle.kv.impl.fault.ProcessFaultHandler;
import oracle.kv.impl.security.AccessCheckUtils;
import oracle.kv.impl.security.AuthContext;
import oracle.kv.impl.security.KVStorePrivilegeLabel;
import oracle.kv.impl.security.annotations.SecureAPI;
import oracle.kv.impl.security.annotations.SecureAutoMethod;
import oracle.kv.impl.security.annotations.SecureInternalMethod;
import oracle.kv.impl.util.registry.VersionedRemoteImpl;

import com.sleepycat.je.rep.ReplicatedEnvironment;

/**
 * The server side implementation of the RMI interface which supports the
 * asynchronous execution of DDL statements by the master Admin service.
 */
@SecureAPI
public class ClientAdminServiceImpl
    extends VersionedRemoteImpl implements ClientAdminService {

    /* The service hosting this interface */
    private final AdminService aservice;

    public ClientAdminServiceImpl(AdminService aservice) {
        this.aservice = aservice;
    }

    /**
     * Asynchronously execute the specified DDL statement, and return status
     * about the corresponding plan.
     *
     * The privilege check of statement execution is performed by DdlHandler,
     * after the parsing and before the real execution. That's because we don't
     * know what operation will be done at this stage without statement parsing.
     */
    @Override
    @SecureInternalMethod
    public ExecutionInfo execute(final String statement,
                                 AuthContext authCtx,
                                 final short serialVersion) {
        final Admin admin = aservice.getAdmin();
        return aservice.getFaultHandler().execute
            (new ProcessFaultHandler.SimpleOperation<ExecutionInfo>() {
                @Override
                public ExecutionInfo execute() {
                    return admin.executeStatement(statement, serialVersion);
                }
            });
    }

    /**
     * Get current status for the specified plan.
     */
    @Override
    @SecureInternalMethod
    public ExecutionInfo getExecutionStatus(final int planId,
                                            AuthContext authCtx,
                                            final short serialVersion) {
        final Admin admin = aservice.getAdmin();
        ExecutionInfo info =  aservice.getFaultHandler().execute
            (new ProcessFaultHandler.SimpleOperation<ExecutionInfo>() {
                @Override
                 public ExecutionInfo execute() {
                    final Plan plan = admin.getAndCheckPlan(planId);

                    /* Check that we have the right to examine the plan */
                    AccessCheckUtils.checkPermission
                    (aservice,
                     new AccessCheckUtils.PlanAccessContext
                     (plan, "getExecutionStatus"));

                    return admin.getExecutionStatus(planId, serialVersion);
                }
            });
        return info;
    }

    /**
     * Ask the admin service to stop the specified plan. Interruption may or
     * may not be needed.
     */
    @Override
    @SecureInternalMethod
    public ExecutionInfo interruptAndCancel(final int planId,
                                            AuthContext nullCtx,
                                            final short serialVersion) {

        final Admin admin = aservice.getAdmin();
        ExecutionInfo info =  aservice.getFaultHandler().execute
            (new ProcessFaultHandler.SimpleOperation<ExecutionInfo>() {
                @Override
                public ExecutionInfo execute() {

                    /* Check that we have the right to interrupt this plan */
                    final Plan plan = admin.getAndCheckPlan(planId);
                    AccessCheckUtils.checkPermission
                    (aservice,
                     new AccessCheckUtils.PlanOperationContext
                     (plan, "interruptAndCancel"));

                    Plan.State currentState = admin.getCurrentPlanState(planId);
                    if (currentState.equals(Plan.State.RUNNING)){
                        admin.interruptPlan(planId);
                        currentState = admin.awaitPlan(planId, 0, null);
                    }

                    /*
                     * Possibilities:
                     * -plan is in INTERRUPT or ERROR state, and can be
                     *   cancelled
                     * -plan is already in a terminal state, nothing more to do
                     * -something else unexpected happened.
                     */
                    if (currentState.checkTransition(Plan.State.CANCELED)) {
                        admin.cancelPlan(planId);
                    } else if (!currentState.isTerminal()) {
                        /*
                         * Unexpected -- the plan should have transitioned
                         * into something that was finished, or can be
                         * cancelled.
                         */
                        throw new AdminFaultException
                        (new NonfatalAssertionException
                                ("Cancellation of operation " + planId +
                                "unsuccessful, current state = " +
                                 currentState));
                    }
                    return admin.getExecutionStatus(planId, serialVersion);
                }
            });
        return info;
    }

    /*
     * Only USRVIEW is required here because we want to allow all authenticated
     * users to be able to perform this operation, since it is required by
     * every operation that uses the admin to figure out if the admin is
     * running.
     */
    @Override
    @SecureAutoMethod(privileges = { KVStorePrivilegeLabel.USRVIEW})
    public boolean canHandleDDL(AuthContext authCtx,
                                final short serialVersion) {

        return aservice.getFaultHandler().execute
            (new ProcessFaultHandler.SimpleOperation<Boolean>() {

            @Override
            public Boolean execute() {
                final Admin admin = aservice.getAdmin();
                if (admin == null) {
                    return false; /* indicates unconfigured */
                }

                try {
                    final ReplicatedEnvironment.State repState =
                        admin.getReplicationMode();
                    return repState.isMaster();
                } catch (IllegalStateException iae) {
                    /* State cannot be queried if detached. */
                    return false;
                }
            }
        });
    }

    /*
     * Only USRVIEW is required here because we want to allow all authenticated
     * users to be able to perform this operation, since it is required by
     * every operation that needs to locate the master admin node.
     */
    @Override
    @SecureAutoMethod(privileges = { KVStorePrivilegeLabel.USRVIEW })
    public URI getMasterRmiAddress(AuthContext authCtx, short serialVersion) {

        return aservice.getFaultHandler().execute
            (new ProcessFaultHandler.SimpleOperation<URI>() {

            @Override
            public URI execute() {
                final Admin admin = aservice.getAdmin();
                if (admin == null) {
                    return null;
                }
                return admin.getMasterRmiAddress();
            }
        });
    }
}
