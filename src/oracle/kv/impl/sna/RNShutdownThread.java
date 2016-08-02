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

package oracle.kv.impl.sna;

import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.kv.impl.rep.admin.RepNodeAdminAPI;

/**
 * Thread used to shutdown RNs in parallel while ensuring they finish their
 * checkpoints. It attempts a clean shutdown by invoking the RN's shutdown
 * method and if that fails, or the process does not exit in the configured
 * time period, it kills the RN process.
 */
class RNShutdownThread implements Runnable {

    private final StorageNodeAgent sna;
    private final Logger logger;
    private final int serviceWaitMs;
    private final ServiceManager mgr;
    private final boolean stopService;
    private final boolean force;

    RNShutdownThread(StorageNodeAgent sna,
                     ServiceManager mgr,
                     int serviceWaitMs,
                     boolean stopService,
                     boolean force) {
        super();
        this.sna = sna;
        this.serviceWaitMs = serviceWaitMs;
        logger = sna.getLogger();

        this.mgr = mgr;
        this.stopService = stopService;
        this.force = force;
    }

    public ServiceManager getMgr() {
        return mgr;
    }

    @Override
    public void run() {

        try {
            /**
             * Make sure the service won't automatically restart.
             */
            mgr.dontRestart();

            if (mgr.forceOK(force)) {
                mgr.stop();
                return;
            }

            /**
             * Don't try to shut down if it's known to be down already.
             */
            if (!stopService) {
                return;
            }

            if (mgr.isRunning()) {
                final ManagedRepNode mrn = (ManagedRepNode) mgr.getService();
                /**
                 * Get the RN's RMI handle
                 *
                 * NOTE: this timeout is helpful but not critical
                 * so it need not be tuneable.
                 */
                final RepNodeAdminAPI rna = mrn.waitForRepNodeAdmin(sna, 5);

                /**
                 * Try clean shutdown first. If that fails for any reason kill
                 * the process to be sure the service is gone. Give the RN some
                 * time in case it's still running. Stopping it at a random
                 * time can cause problems.
                 */
                if (rna != null) {
                    try {
                        rna.shutdown(force);
                    } catch (RemoteException e) {
                        final Throwable ce = e.getCause();

                        /*
                         * Make special provisions for a request timeout, the
                         * RN could just be in the midst of a long checkpoint:
                         * Don't kill the process right away, but wait for the
                         * configured period.
                         */
                        if (! (ce instanceof SocketTimeoutException)) {
                            /* Kill the process in the handler. */
                            throw e;
                        }

                        logger.warning(String.format(
                            "Socket timed out waiting for %s." +
                            " Message:%s. Wait %,d ms for process exit.",
                            mgr.getService().getServiceName(),
                            ce.getMessage(), serviceWaitMs));
                    }
                }
            }

            /*
             * TODO: replace with a polling or heartbeat mechanism someday for
             * a more explicit sign of forward progress towards a shutdown.
             */
            mgr.waitFor(serviceWaitMs);

        } catch (Exception e) {

            /**
             * Eat the exception but log it and make sure that the service
             * is really stopped.
             */
            logger.log(Level.WARNING, mgr.getService().getServiceName() +
                        ": Exception stopping RepNode", e);
            mgr.stop();
        } finally {
            sna.unbindService(sna.makeRepNodeBindingName
                              (mgr.getService().getServiceName()));
        }
    }
}