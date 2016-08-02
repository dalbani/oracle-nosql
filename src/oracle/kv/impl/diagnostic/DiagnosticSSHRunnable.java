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

package oracle.kv.impl.diagnostic;

import oracle.kv.impl.diagnostic.ssh.SSHClient;

/**
 * Encapsulates a definition and mechanism for communicating with SSH sever
 * to get info and files. Subclasses of DiagnosticSSHRunnable will define the
 * different types of DiagnosticSSHRunnable that can be carried out.
 */

public abstract class DiagnosticSSHRunnable implements Runnable {

    protected SNAInfo snaInfo;
    protected SSHClient client;

    /* The owner of the thread, the owner starts the thread */
    private final DiagnosticTask owner;

    public DiagnosticSSHRunnable(SNAInfo snaInfo, DiagnosticTask owner,
                                 SSHClient client) {
        this.snaInfo = snaInfo;
        this.owner = owner;
        this.client = client;
    }

    @Override
    public void run() {

        String message = snaInfo.getSNAInfo().trim();
        try {
            /* Call doWork to execute the code in derived classes */
            if (!client.isOpen()) {
                message += DiagnosticConstants.NEW_LINE_TAB +
                        client.getErrorMessage();
            } else {
                message += DiagnosticConstants.NEW_LINE_TAB + doWork();
            }
        } catch (Exception e) {
            /* Convert the exception to a message */
            message = snaInfo.getSNAInfo() +
                    DiagnosticConstants.NEW_LINE_TAB + e;
        }

        /* Notify the sub task is completed */
        owner.notifyCompleteSubTask(message.trim());
    }

    /**
     * The real work to be done is contained in the implementations of this
     * method.
     *
     * @return result message of execution of the thread
     * @throws Exception
     */
    public abstract String doWork() throws Exception;
}
