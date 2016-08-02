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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * A bean class to store log item of kvstore.
 */

public class LogInfo {
    private String BLANKSPACE_SEPARATOR = " ";

    private String logItem;
    private String timestampString;
    private Date logTimestamp;

    /* Date format of log item time stamp */
    private SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    public LogInfo(String logItem) {
        this.logItem = logItem;
        try {
            parse();
        } catch (ParseException ex) {
            /*
             * Almost log items have time stamp, but some lines maybe do not
             * contain time stamp, it causes n ParseException. The exception is
             * expected and harmless
             */
        }
    }

    /**
     * Extract time stamp string from log item and convert it to Date type
     */
    private void parse() throws ParseException {
        String[] dateStr = logItem.split(BLANKSPACE_SEPARATOR);
        /*
         * A time stamp string has 3 parts, so a log time should be split as
         * at least 3 parts
         */
        if (dateStr.length >= 3) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(dateStr[2]));
            logTimestamp = dateFormat.parse(dateStr[0] + BLANKSPACE_SEPARATOR +
                                            dateStr[1]);

            timestampString = dateStr[0] + BLANKSPACE_SEPARATOR + dateStr[1] +
                              BLANKSPACE_SEPARATOR + dateStr[2];
        }
    }

    public Date getTimestamp() {
        return logTimestamp;
    }

    public String getTimestampString() {
        return timestampString;
    }

    @Override
    public String toString() {
        return logItem;
    }

    /**
     * Compare whether two LogInfos are equal
     *
     * @param logInfo compared LogInfo
     */
    public boolean equals(LogInfo logInfo) {
        return this.logItem.equals(logInfo.logItem);
    }

    /**
     * LogInfo Comparator is to indicate that LogInfos are ordered
     * by its time stamp
     */
    public static class LogInfoComparator implements
            Comparator<LogInfo> {

        @Override
        public int compare(LogInfo o1, LogInfo o2) {
            if (o1.getTimestamp().after(o2.getTimestamp())) {
                return 1;
            } else if (o1.getTimestamp().before(o2.getTimestamp())) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
