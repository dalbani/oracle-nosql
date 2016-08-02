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
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.model.Persistent;

import oracle.kv.KVSecurityException;
import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.AdminFaultException;
import oracle.kv.impl.admin.CommandResult;
import oracle.kv.impl.admin.NonfatalAssertionException;
import oracle.kv.impl.admin.plan.PlanExecutor.ParallelTaskRunner;
import oracle.kv.impl.fault.CommandFaultException;
import oracle.kv.impl.rep.admin.RepNodeAdminFaultException;
import oracle.kv.impl.security.SessionAccessException;
import oracle.kv.impl.sna.SNAFaultException;
import oracle.kv.util.ErrorMessage;

/**
 * A task that is meant to be executed in a single phase of work.
 */
@Persistent
public abstract class SingleJobTask extends AbstractTask {

    private static final long serialVersionUID = 1L;

    /**
     * Contains the all the logic needed to execute the task, to be done as
     * a single job, or phase.
     */
    public abstract Task.State doWork() throws Exception;

    /**
     * Task execution is started off with a Callable that encompasses
     * all the work of the task.
     */
    @Override
    public Callable<Task.State>
        getFirstJob(int taskNum, ParallelTaskRunner unused) {

        return new Callable<Task.State>() {
            @Override
            public State call() throws Exception {
                try {
                    return doWork();
                } catch (AdminFaultException e) {
                    throw new CommandFaultException(
                        e.getMessage(), e,
                        ErrorMessage.getEnum(
                            e.getCommandResult().getErrorCode()),
                        e.getCommandResult().getCleanupJobs());
                } catch (SNAFaultException | RepNodeAdminFaultException e) {
                    throw new CommandFaultException(
                        e.getMessage(), e, ErrorMessage.NOSQL_5400,
                        CommandResult.PLAN_CANCEL);
                } catch (NotBoundException e) {
                    throw new CommandFaultException(
                        e.getMessage(), e, ErrorMessage.NOSQL_5200,
                        CommandResult.NO_CLEANUP_JOBS);
                } catch (RemoteException | SessionAccessException |
                         RejectedExecutionException | DatabaseException |
                         Admin.DBOperationFailedException e) {
                    throw new CommandFaultException(
                        e.getMessage(), e, ErrorMessage.NOSQL_5400,
                        CommandResult.PLAN_CANCEL);
                } catch (NonfatalAssertionException e) {
                    throw new CommandFaultException(
                        e.getMessage(), e, ErrorMessage.NOSQL_5500,
                        CommandResult.NO_CLEANUP_JOBS);
                } catch (KVSecurityException | IllegalStateException e) {
                    throw new CommandFaultException(
                        e.getMessage(), e, ErrorMessage.NOSQL_5100,
                        CommandResult.NO_CLEANUP_JOBS);
                }
            }
        };
    }
}
