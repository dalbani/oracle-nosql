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

package oracle.kv.impl.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Atomic integer which you can wait on for specified values.
 */
public class WaitableCounter extends AtomicInteger {
    private static final long serialVersionUID = 1L;

    /**
     * Returns true if the counter is at the specified value. If the counter
     * is not at the value, this method will wait for the specified timeout,
     * checking the counter each check period. It will return false if the
     * timeout has been reached and the value was not found.
     *
     * Note that this method returns true if the counter was found to be
     * at the value. The counter may have been changed upon return.
     *
     * @param value the value to check and wait for
     * @param checkPeriodMs time, in milliseconds, between checks
     * @param timeoutMs total time to wait, in milliseconds
     * @return true if the counter was found to be at the value
     */
    public boolean await(final int value, int checkPeriodMs, int timeoutMs) {
        return
            new PollCondition(checkPeriodMs, timeoutMs) {
                @Override
                protected boolean condition() {
                    return get() == value;
                }
            }.await();
    }

    /**
     * Returns true if the counter is at zero. This method is equivalent to
     * calling await(0, checkPeriodMs, timeoutMs)
     *
     * @param checkPeriodMs time, in milliseconds, between checks
     * @param timeoutMs total time to wait, in milliseconds
     * @return true if the counter was found to be at 0
     */
    public boolean awaitZero(int checkPeriodMs, int timeoutMs) {
        return await(0, checkPeriodMs, timeoutMs);
    }
}
