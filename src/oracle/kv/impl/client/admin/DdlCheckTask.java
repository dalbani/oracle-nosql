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
import java.util.logging.Logger;

/**
 * DDLCheckTask contacts the Admin service to obtain new status for a given
 * plan. The new status is sent on to the DdlStatementExecutor to update
 * any waiting targets.
 */
class DdlCheckTask implements Runnable {

    private final Logger logger;

    private final int planId;
    private final DdlStatementExecutor statementExec;

    private final int maxRetries;
    private int numRetries;

    DdlCheckTask(int planId,
                 DdlStatementExecutor statementExec,
                 int maxRetries,
                 Logger logger) {
        this.planId = planId;
        this.statementExec = statementExec;
        this.maxRetries = maxRetries;
        this.logger = logger;
    }

    @Override
    public void run() {

        /* Call to the server side to get up to date status. */
        try {
            ExecutionInfo newInfo = statementExec.getClientAdminService().
                getExecutionStatus(planId);
            newInfo = DdlFuture.checkForNeedsCancel(newInfo, statementExec,
                                                    planId);
            statementExec.updateWaiters(newInfo);
        } catch (RemoteException e) {
            numRetries++;
            logger.fine("Got " + e + ", " + numRetries + "th retry" +
                        " maxRetries = "  + maxRetries);
            if (numRetries > maxRetries) {
                statementExec.shutdownWaitersDueToError(planId, e);
            }
        } catch (Throwable t) {
            logger.info("DDL polling task for plan " + planId + 
                        " shut down due to " + t);
            statementExec.shutdownWaitersDueToError(planId, t);
        }
    }
}
