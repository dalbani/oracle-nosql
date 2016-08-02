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

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Store all info of log section file, the file contains several log sections.
 * Its format is as follows:
 * line 1     --
 * ...          | -- log section 1
 * line 500   --
 * line 501   --
 * ...          | -- log section 2
 * line 1000  --
 * ...            -- ...
 * line x     --
 * ...          | -- log section z
 * line y     --
 * ...
 *
 * The class store file name of log section file and the first log item of
 * all sections
 */
public class LogSectionFileInfo {
    private String fileName;
    private String filePath;

    /* Store the first log item of all sections */
    private Deque<LogInfo> beginLogInfoList = new LinkedList<LogInfo>();


    public LogSectionFileInfo(File file, List<String> timeStampList) {
        filePath = file.getAbsolutePath();
        fileName = file.getName();

        for (String timeStamp : timeStampList) {
            add(timeStamp);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public LogInfo getFirst() {
        if (beginLogInfoList.isEmpty())
            return null;

        return beginLogInfoList.getFirst();
    }

    public LogInfo pop() {
        if (beginLogInfoList.isEmpty())
            return null;

        return beginLogInfoList.pop();
    }

    private void add(String logInfo) {
        beginLogInfoList.add(new LogInfo(logInfo));
    }

    public boolean isEmpty() {
        return beginLogInfoList.isEmpty();
    }
}
