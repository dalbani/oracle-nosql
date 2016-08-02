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

/**
 * This class contains the constants used in diagnostics utility.
 *
 */
public class DiagnosticConstants {

    static final String DEFAULT_WORK_DIR = System.getProperty("user.dir");
    static final String CONFIG_FILE_NAME = "sn-target-list";

    static final String HOST_FLAG = "-host";
    static final String PORT_FLAG = "-port";
    static final String SSH_USER_FLAG = "-sshusername";
    static final String USER_FLAG = "-username";
    static final String SECURITY_FLAG = "-security";
    static final String CONFIG_DIRECTORY_FLAG = "-configdir";
    static final String STORE_FLAG = "-store";
    static final String SN_FLAG = "-sn";
    static final String ROOT_DIR_FLAG = "-rootdir";
    static final String SAVE_DIRECTORY_FLAG = "-savedir";
    static final String NO_COMPRESS_FLAG = "-nocompress";

    static final String NEW_LINE_TAB = "\n\t";
    static final String EMPTY_STRING = "";
    static final String NOT_FOUND_ROOT_MESSAGE =
            "Cannot find root directory: ";
}
