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

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Encapsulates a definition and mechanism for running a step of diagnostic
 * command. Subclasses of DiagnosticTask will define the different types of
 * DiagnosticTask that can be carried out.
 */

public abstract class DiagnosticTask {
    private int totalSubTaskCount = 0;
    private int completedSubTaskCount = 0;

    /* A message queue to store the result messages */
    private Queue<String> messageQueue = new LinkedBlockingQueue<String>();

    public DiagnosticTask() {
        /* Default number of subtask of  DiagnosticTask is 1 */
        this(1);
    }

    public DiagnosticTask(int totalSubTaskCount) {
        this.totalSubTaskCount = totalSubTaskCount;
    }

    public int getTotalSubTaskCount() {
        return totalSubTaskCount;
    }

    public int getCompletedSubTaskCount() {
        return completedSubTaskCount;
    }

    /**
     * Notify to complete a sub task.
     *
     * @param message result message
     */
    public void notifyCompleteSubTask(String message) {
        /* Increment the count of completed sub tasks */
        completedSubTaskCount++;
        /* Add message into message queue */
        messageQueue.add(message);
    }

    public Queue<String> getMessageQueue() {
        return messageQueue;
    }

    protected void setTotalSubTaskCount(int totalSubTaskCount) {
        this.totalSubTaskCount = totalSubTaskCount;
    }

    /**
     * Execute the task.
     * @throws Exception
     */
    public final void execute() throws Exception {
        try {
            doWork();
        } finally {
            /*
             * In the end of execution, all sub-tasks complete, set completed
             * sub tasks as total sub tasks
             */
            completedSubTaskCount = totalSubTaskCount;
        }
    }

    /**
     * Do real work in this method.
     *
     * @throws Exception
     */
    public abstract void doWork() throws Exception;
}
