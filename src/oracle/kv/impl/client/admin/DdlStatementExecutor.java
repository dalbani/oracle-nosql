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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import oracle.kv.FaultException;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.util.KVThreadFactory;

/**
 * The DdlStatementExecutor manages resources needed for execution of ddl
 * statements. It provides:
 * - a discovery service to locate the admin master
 * - maintains a handle to the ClientAdminService for RMI communication between
 *   the client and Admin node
 * - manages notifications for ddl operation completion by
 *     - providing an Executor to handle tasks to poll for statement completion
 *     - manages notifications of threads that are awaiting statement completion
 *
 * There is a single DdlStatementExecutor per TableAPI.
 *
 * Notification mechanism
 * ----------------------
 * By intent, this class does not automatically maintain a cache of
 * ExecutionFuture instances.  Although DdlFuture, which is the implementation
 * of the ExecutionFuture interface is thread safe, multiple threads may
 * instantiate futures that target the same ddl operation. See the diagram in
 * {@link DdlFuture}.
 *
 * This class does cache the DdlFuture if the future has instigated polling for
 * operation completeness.  Polling is started lazily; it's initiated by a call
 * to DdlFuture.get() or get(time, unit). At that point the DdlFuture calls
 * DdlStatementExecutor to ask for polling to start. DdlStatementExecutor
 * manages the polling requests from multiple futures and ensures that only a
 * single periodic polling task is created per plan. Each DdlFuture that has
 * requested polling registers itself as an interested party for that
 * plan. When plan completion is detected, or an error occurs, the polling task
 * will release all registered DdlFutures.
 *
 * For example:
 *   threadA calls DdlFutureA.get()
 *        DdlStatementExecutor caches DdlFutureA
 *        DdlStatementExecutor creates and submits polling task X
 *        DdlStatementExecutor notes that taskX should notify DdlFutureA
 *   threadB calls DdlFutureB.get()
 *        DdlStatementExecutor caches DdlFutureB
 *        DdlStatementExecutor notes that taskX should notify DdlFutureA and
 *                      DdlFutureB
 * Futures are only cached if the application has called get() and is polling.
 * For synchronization simplicity, cached futures rely on the polling task to
 * free them from the target pool.  If DdlFuture obtains a new status directly,
 * via DdlFuture.updateStatus or isDone(), the new status is applied only to
 * that future instance, and does not affect its own membership in the
 * notification pool, nor the status of any other futures that are focused on
 * the same plan.
 *
 * Synchronization hierarchy:
 * --------------------------
 *  The order of locking is DdlStatementExecutor, followed by DdlFuture.
 */
public class DdlStatementExecutor {

    /* Interface to server, manages refreshing it. */
    private ClientAdminServiceAPI clientAdminService;

    /* Executor for polling tasks */
    private final ScheduledExecutorService completionChecker;

    /* Submitted polling tasks */
    private final Map<Integer, ScheduledFuture<?>> scheduledFutures;

    /*
     * Notification targets are futures that are blocking in get() and
     * are awaiting notification from a polling task.
     */
    private final Map<Integer, Set<DdlFuture>> notificationTargets;

    private final KVStoreImpl store;
    private final Logger logger;

    /*
     * The number of times a status check polling task should retry in the face
     * of remote exceptions.
     */
    private final int maxCheckRetries;

    /* The polling interval for status check tasks. */
    private final long checkIntervalMillis;

    /* TODO: add login manager */
    public DdlStatementExecutor(KVStoreImpl store) {
        this.logger = store.getLogger();
        this.store = store;
        this.scheduledFutures = new HashMap<Integer, ScheduledFuture<?>>();
        this.notificationTargets = new HashMap<Integer, Set<DdlFuture>>();
        this.checkIntervalMillis = store.getCheckIntervalMillis();
        this.maxCheckRetries = store.getMaxCheckRetries();

        completionChecker = new ScheduledThreadPoolExecutor
            (1, new KVThreadFactory("DDLChecks", logger));

        /*
         * Try to connect to an Admin master, in preparation for later DDL, but
         * it's okay if it's not possible. We don't necessarily need the
         * connection. The connection isn't too costly though; RMI will
         * background the connection after a number of seconds and cache the
         * binding, so it's okay to try to set it up now.
         */
        try {
            ensureClientAdminService();
        } catch (Exception ignore) {
            /*
             * The attempt to establish a client/admin connection at this point
             * is just an optimization, so a failure to do so should not
             * matter. It's not really needed until a DDL statement is
             * executed. The connection is checked again at that point, so
             * ignore any exceptions now.
             */
        }
    }

    /**
     * Return the RMI interface for client/admin communication.
     */
    public synchronized ClientAdminServiceAPI getClientAdminService() {
        /* If it's not connected, try to connect now */
        ensureClientAdminService();
        return clientAdminService;
    }

    /**
     * Establish an RMI connection to the admin master. Ensure that the proper
     * credentials are set up.
     *
     * A note about login managers:
     * ----------------------------
     * The RepNodeLoginManager instance stored in the KVStoreImpl is used to
     * validate the secure version of this client->admin connection. This is a
     * convenient implementation, as the KVStoreImpl's login manager already
     * has the right infrastructure to be created and refreshed when new
     * security credentials arrive. All communications between the client and
     * Admin service for a secured RMI interface require a login token. This
     * login token needs to be validated by the Admin service, using a login
     * manager, and in this case, an RN acts as this validator.
     *
     * However, since successfully resolved login tokens are cached by the
     * Admin (as they are by RNs), that Admin only needs to communicate with
     * the RN for validation infrequently. Cached tokens have timeout values,
     * and at timeout, validation will happen again.
     */
    private void ensureClientAdminService() throws FaultException {

        if (clientAdminService != null) {
            try {
                /* See if the service is functional and is the master */
                if (clientAdminService.canHandleDDL()) {
                    return;
                }
            } catch (RemoteException e) {
                logger.fine("Ensuring connection, got " + e);
            }

            /* 
             * Either the RMI service is down, or the admin is  no longer a
             * master. Null out the cached connection so we can find another.
             */
            clientAdminService = null;
        }

        /* Find the SN which holds the admin master */
        logger.info("Establishing RMI connection for admin DDL");

        FindClientAdminService finder = new FindClientAdminService
            (store.getDispatcher().getTopologyManager().getTopology(),
             logger,
             KVStoreImpl.getLoginManager(store));
        clientAdminService = finder.getDDLService();
        if (clientAdminService == null) {
            throw new FaultException(
                    "Couldn't connect to a store Admin service capable of " +
                    "executing an administrative table statement. Contacted " +
                    "nodes " + finder.getTargets(), false);
        }
    }

    /**
     * Schedule a task to start checking status for the specified plan.
     */
    synchronized void startPolling(int planId, DdlFuture ddlFuture) {

        /*
         * If there's already a polling task for this plan, no need to add
         * another one.
         */
        if (!scheduledFutures.containsKey(planId)) {
            ScheduledFuture<?> f =
                completionChecker.scheduleAtFixedRate
                (new DdlCheckTask(planId, this, maxCheckRetries, logger),
                 2000, /* initial delay (2 seconds) */
                 checkIntervalMillis, /* subsequent delays */
                 TimeUnit.MILLISECONDS);
            scheduledFutures.put(planId, f);
        }

        /*
         * Do register this future as an interested party, whether or not
         * we added a new polling task.
         */
        Set<DdlFuture> targets = notificationTargets.get(planId);
        if (targets == null) {
            targets = new HashSet<DdlFuture>();
            notificationTargets.put(planId, targets);
        }
        targets.add(ddlFuture);
    }

    /**
     * Problem seen, but no new status available. Manufacture a status that
     * says that the operation hasn't ended, but that the call to get() needs
     * to fail. There's no valid result.
     */
    void shutdownWaitersDueToError(int planId, Throwable t) {
        ExecutionInfo errorInfo =
            new ExecutionInfoImpl(planId,
                                  false, // isTerminated
                                  null,  // getInfo
                                  null,  // getInfoAsJson
                                  false, // isSuccess,
                                  false, // isCancelled
                                  t.getMessage(),
                                  false, // needsCancel;
                                  null); // result
        updateWaiters(errorInfo, t, true);
    }

    /**
     * New status has come in. Let any futures who have expressed interest
     * know.
     */
    void updateWaiters(ExecutionInfo newInfo) {
        updateWaiters(newInfo, null, false);
    }

    /**
     * New status has come in. Let any futures who have expressed interest
     * know.
     */
    synchronized void updateWaiters(ExecutionInfo newInfo,
                                    Throwable t,
                                    boolean stopPolling) {
        int planId = newInfo.getPlanId();

        /*
         * Update all waiting futures with the new info. Synchronize
         * to make sure that they are all uniformly updated with the
         * same info, and that we don't add a target to the list and
         * miss an update.
         */
        Set<DdlFuture> waiters = notificationTargets.get(planId);
        if (waiters != null) {
            for (DdlFuture waiter : waiters) {
                waiter.applyNewInfo(newInfo, t);
            }
        }

        /* If the operation is completed, terminate the polling task */
        if (stopPolling || newInfo.isTerminated()) {
            ScheduledFuture<?> taskFuture = scheduledFutures.get(planId);
            if (taskFuture != null) {
                logger.fine("Polling task for plan " + planId +
                            " finished, info = " + newInfo);
                taskFuture.cancel(true);
                scheduledFutures.remove(planId);
            }

            notificationTargets.remove(planId);
        }
    }

    /**
     * Called by a scheduled task to report that it had problems, has retried
     * more than the permissible amount of time, and will no longer execute.
     * @param planId
     * @param e
     */
    void taskFailed(int planId, RemoteException e) {
        throw new UnsupportedOperationException(e);
    }
}
