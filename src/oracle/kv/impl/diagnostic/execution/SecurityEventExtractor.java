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

package oracle.kv.impl.diagnostic.execution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import oracle.kv.impl.diagnostic.LogFileInfo;
import oracle.kv.impl.diagnostic.LogFileInfo.LogFileType;
import oracle.kv.impl.diagnostic.LogInfo;

/**
 * The class is to extract all security events in log files.
 *
 */
public class SecurityEventExtractor extends LogExtractor {
    private String TEMP_FILE_SUFFIX = "_securityevent.tmp";
    private String SECURITY_EVENT_PATTERN = "KVAuditInfo";

    /*
     * Add prefix name for the file name of generated file to ensure the
     * file name is unique
     */
    private String prefixName;

    private File resultFile = null;

    public SecurityEventExtractor(String prefixName) {
        /* It is possible that security events exist all log files */
        super(LogFileType.ALL);
        this.prefixName = prefixName;
    }

    /**
     * Get the result security event file
     * @return the security event file
     */
    public File getResultFile() {
        return resultFile;
    }

    @Override
    protected void extract(Map<String, List<LogFileInfo>> logFileInfoMap)
            throws Exception {
        /* return when no log files found */
        if (logFileInfoMap.isEmpty()) {
            resultFile = null;
            return;
        }

        List<LogInfo> securityEventList = new ArrayList<LogInfo>();
        for (Map.Entry<String, List<LogFileInfo>> entry :
                logFileInfoMap.entrySet()) {
            List<LogFileInfo> logFileInfoList = entry.getValue();
            if (!logFileInfoList.isEmpty()) {
                for (LogFileInfo logFileInfo : logFileInfoList) {
                    getSecurityEvent(new File(logFileInfo.getFilePath()),
                            securityEventList);
                }
            }
        }

        BufferedWriter bw = null;
        try {
            resultFile = new File(prefixName + TEMP_FILE_SUFFIX);
            bw = new BufferedWriter(new FileWriter(resultFile));

            /* Sort security event order by time stamp */
            Collections.sort(securityEventList,
                    new LogInfo.LogInfoComparator());

            /*
             * Write all security event into file and remove the duplicate
             * event
             */
            LogInfo currentLog = null;
            for (LogInfo log : securityEventList) {
                if (currentLog == null || !currentLog.equals(log)) {
                    currentLog = log;
                    bw.write(log.toString());
                    bw.newLine();
                }
            }
        } catch (Exception ex) {
            resultFile = null;
            throw ex;
        } finally {
            try {
                if (bw != null)
                    bw.close();
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    /**
     * Extract security event log from a specified log file
     */
    private void getSecurityEvent(File file, List<LogInfo> list) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                /*
                 * When a log contains "KVAuditInfo", it is a security event
                 * log
                 */
                if (line.contains(SECURITY_EVENT_PATTERN)) {
                    list.add(new LogInfo(line));
                }
            }
        } catch (Exception ex) {
        } finally {

            try {
                if (br != null)
                    br.close();
            } catch (Exception ex) {
            }
        }
    }
}
