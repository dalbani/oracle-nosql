/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
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

package oracle.kv.impl.rep;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;


/*
 * Struct to package together the information needed to schedule the
 * executor. Packaged this way rather than making these fields be class
 * members for easier unit testing.
 */
public class ScheduleStart {
    private int delay;
    private int interval;
    private TimeUnit unit;

    public TimeUnit getTimeUnit() {
        return unit;
    }

    public int getDelay() {
        return delay;
    }

    public int getInterval() {
        return interval;
    }

    public static ScheduleStart calculateStart(Calendar now,
                                       int configuredInterval,
                                       TimeUnit configuredUnit) {

        ScheduleStart start = new ScheduleStart();

        start.unit = configuredUnit;
        start.interval = configuredInterval;

        /*
         * It's easier to calculate a sync'ed up start if the intervals are
         * promoted to their maximum unit.
         */
         switch (configuredUnit) {
         case HOURS:
             /* Round interval up to days. */
             if (configuredInterval > 24) {
                 start.unit = TimeUnit.DAYS;
                 start.interval = configuredInterval/24;
                 if (configuredInterval%24 > 0) {
                     start.interval++;
                 }
             }
             break;

         case MINUTES:
             /* Round interval up to hours. */
             if (configuredInterval > 60) {
                 start.unit = TimeUnit.HOURS;
                 start.interval = configuredInterval/60;
                 if (configuredInterval%60 > 0) {
                     start.interval++;
                 }
             }
             break;

         case SECONDS:
             /* Round interval up to minutes. */
             if (configuredInterval > 60) {
                 start.unit = TimeUnit.MINUTES;
                 start.interval = configuredInterval/60;
                 if (configuredInterval%60 > 0) {
                     start.interval++;
                 }
             }
             break;

         default:
             break;
         }

         /*
          * Calculate delayToNext to see if it's possible to sync up all the rep
          * nodes.  This works fine as long as the unit is an even factor of the
          * next largest granularity unit.
          *
          * i.e. if the interval is 5, 10, 15, 20, 30 minutes, we can figure out
          * a good point in the hour to start. But if the interval doesn't
          * divide into an the next unit evenly, it's not worth doing, so skip
          * it.
          */
         switch (start.unit) {
         case HOURS:
             nextStart(start, now, Calendar.HOUR, 24);
             break;

         case  MINUTES:
             nextStart(start, now, Calendar.MINUTE, 60);
             break;

         case SECONDS:
             nextStart(start, now, Calendar.SECOND, 60);
             break;

         default:
             break;
         }

         return start;
    }

    private static void nextStart(ScheduleStart start,
                                  Calendar now,
                                  int calField,
                                  int numTotalUnits) {

        if ((numTotalUnits % start.interval) != 0) {
            start.delay = 0;
            return;
        }

        int nowUnits = now.get(calField);
        int nextStartUnit = (nowUnits / start.interval) + 1;
        start.delay = (nextStartUnit * start.interval) - nowUnits;
    }
}
