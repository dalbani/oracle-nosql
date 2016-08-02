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

import java.util.ArrayList;
import java.util.List;
import oracle.kv.util.shell.Shell;

/**
 * This class is designed to manage all subclass of DianosticTask.java. It
 * calculates the total number of sub-tasks and monitors the progress of
 * execution of all tasks managed by this class.
 */

public class DiagnosticTaskManager {
    private String ON_PROGRESS_STATUS = "progress";
    private String DONE_STATUS = "done";
    private String BACKSPACE_SIGN = "\r";
    private String NEW_LINE_SIGN = "\n";
    private String EMPTY_STRING = "";
    private String BLANKSPACE_SIGN = " ";
    private String POINT_SIGN = ".";

    private String TOTAL_TASKS_PREFIX = "Total: ";
    private String COMPLETED_TASKS_PREFIX = "    Completed: ";
    private String STATUS_PREFIX = "    Status: ";

    private List<DiagnosticTask> taskList = new ArrayList<DiagnosticTask>();
    private Shell shell;
    private int totalTaskCount = 0;
    private int completedTaskCount = 0;
    private int maxLength = 0;
    private int pointsNumber = 0;
    private List<DiagnosticTask> runningTaskList =
            new ArrayList<DiagnosticTask>();

    private String showStr;

    /**
     * DiagnosticManager constructor
     *
     * @param shell
     */
    public DiagnosticTaskManager(Shell shell) {
        this.shell = shell;
    }

    /**
     * Add a task into DiagnosticManager, get the number of sub-tasks of the
     * added task and accumulate it into totalTaskCount.
     *
     * @param task an added DiagnosticTask
     */
    public void addTask(DiagnosticTask task) {
        taskList.add(task);
        totalTaskCount += task.getTotalSubTaskCount();
    }

    /**
     * Execute all DiagnosticTasks in DiagnosticManager and start a
     * TaskProgressMonitor to monitor all progress status of all tasks.
     *
     * @throws Exception
     */
    public void execute() throws Exception {
        TaskProgressMonitor monitor = new TaskProgressMonitor();
        try {
            monitor.start();

            /* New line to divide the sections */
            shell.getOutput().println(EMPTY_STRING);

            for (DiagnosticTask task : taskList) {
                runningTaskList.add(task);
                task.execute();
            }
        } finally {
            /* Wait monitor thread complete and exit */
            monitor.join();
        }
    }

    /**
     * TaskProgressMonitor is designed to monitor the progress status of tasks.
     * And it shows the progress status of tasks on console. The format is as
     * follows:
     * Total: 6    Completed: 4    Status: progress...
     * Total: 6    Completed: 6    Status: done
     *
     */
    private class TaskProgressMonitor extends Thread {
        @Override
        public void run() {
            while (completedTaskCount < totalTaskCount) {
                completedTaskCount = 0;
                /* Remove the previous status string */
                shell.getOutput().print(BACKSPACE_SIGN);

                try {
                    Thread.sleep(1000);
                    for (DiagnosticTask task : runningTaskList) {
                        String blankString = EMPTY_STRING;
                        for (int i = 0; i < maxLength; i++) {
                            /*
                             * Fill with blank space to remove the last
                             * show string
                             */
                            blankString += BLANKSPACE_SIGN;
                        }

                        shell.getOutput().print(blankString + BACKSPACE_SIGN);

                        /*
                         * Get completed sub-task counts of current task
                         * and accumulate it
                         */
                        completedTaskCount += task.getCompletedSubTaskCount();

                        /*
                         * Get result message from MessageQueue of
                         * current task
                         */
                        String message = task.getMessageQueue().poll();
                        while (message != null) {
                            shell.getOutput().println(message);
                            message = task.getMessageQueue().poll();
                        }
                    }
                } catch (InterruptedException ex) {
                }

                /* Compute and generate the latest status string */
                showStr = TOTAL_TASKS_PREFIX + totalTaskCount +
                          COMPLETED_TASKS_PREFIX + completedTaskCount +
                          STATUS_PREFIX;

                if (completedTaskCount < totalTaskCount) {
                    showStr += ON_PROGRESS_STATUS;
                    for (int i = 0; i <= pointsNumber; i++) {
                        showStr += POINT_SIGN;
                    }
                    pointsNumber++;
                    pointsNumber %= 6;
                } else {
                    showStr += DONE_STATUS;
                }

                /* Get max length of show string */
                if (showStr.length() >= maxLength) {
                    maxLength = showStr.length();
                } else {
                    int addNumber = maxLength - showStr.length();
                    for (int i = 0; i < addNumber; i++) {
                        showStr += BLANKSPACE_SIGN;
                    }
                }

                /* Add new line for show string when all tasks are done */
                if (completedTaskCount == totalTaskCount) {
                    showStr += NEW_LINE_SIGN;
                }
                shell.getOutput().print(showStr);
            }
        }
    }

    /**
     * Get progress status. It is only used in test
     * @return progress status
     */
    public String getProgressStatus() {
        return showStr.trim();
    }
}
