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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oracle.kv.impl.diagnostic.LogFileInfo;
import oracle.kv.impl.diagnostic.LogFileInfo.LogFileType;

/**
 * This class is to extract log items from log item. The subclass of
 * LogExtractor can be carried out.
 *
 */
public abstract class LogExtractor {
    private String LOG_FOLDER_NAME = "log";
    private String LOG_FILE_NAME_PATTERN = "[0-9]+\\_[0-9]+\\.log$";
    protected String INFO_SEPEARATOR = ";";

    private LogFileType logFileType;

    /* Do real extracting work */
    protected abstract void extract(Map<String,
            List<LogFileInfo>> logFileInfoMap) throws Exception;

    public LogExtractor(LogFileType logFileType) {
        this.logFileType = logFileType;
    }

    /**
     * execute extracting
     * @param rootDirectory the root directory of a SNA
     * @throws Exception
     */
    public void execute(String rootDirectory) throws Exception {
        File logDirectory = getLogDirectory(rootDirectory);
        Map<String, List<LogFileInfo>> logFileInfoMap =
                iterateLogFile(logDirectory);
        extract(logFileInfoMap);
    }

    /**
     * Get directory which contains log files of kvstore
     */
    private File getLogDirectory(String directory) {
        File f = new File(directory);

        if (!f.exists()) {
            return null;
        }
        /*
         * Add file name filter to ensure the return of listFiles are
         * directory.
         */
        File[] files = f.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file,String name)
                {
                  return new File(file.getAbsoluteFile(), name)
                          .isDirectory();
                }
            });
        for (File file : files) {
            /*
             * return the path when find the target directory
             */
            if (file.getName().equals(LOG_FOLDER_NAME)) {
                return file;
            } else if (file.isDirectory()) {
                File logFolder = getLogDirectory(file.getAbsolutePath());
                if (logFolder != null)
                    return logFolder;
            }
        }
        /* Can not find the target directory, return null */
        return null;
    }

    /**
     * Iterate a type of log files in log directory
     */
    private Map<String, List<LogFileInfo>> iterateLogFile(File logDirectory) {
        Map<String, List<LogFileInfo>> logFileMap =
                new HashMap<String, List<LogFileInfo>>();
        if (logDirectory != null) {

            /* Get a type of log file within log directory */
            File[] logFiles = logDirectory.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String name) {

                            Pattern p = Pattern.compile(logFileType +
                                                        LOG_FILE_NAME_PATTERN);
                            Matcher matcher = p.matcher(name);
                            return matcher.find();
                        }
                });
            for (File f : logFiles) {
                /*
                 * Store log file into in an ArrayList, and store the ArrayList
                 * with the name in a map
                 */
                LogFileInfo logFileInfo = new LogFileInfo(f, logFileType);
                List<LogFileInfo> logFileInfoList =
                        logFileMap.get(logFileInfo.getNodeName());

                /*
                 * Create a new ArrayList when do not find ArrayList with
                 * specified name in the map, and store the new ArrayList
                 * in the map
                 */
                if (logFileInfoList == null) {
                    logFileInfoList = new ArrayList<LogFileInfo>();
                    logFileMap.put(logFileInfo.getNodeName(), logFileInfoList);
                }

                /* Add the log file info into ArrayList */
                logFileInfoList.add(logFileInfo);
            }
        }
        return logFileMap;
    }
}
