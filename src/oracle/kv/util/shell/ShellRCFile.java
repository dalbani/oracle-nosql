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
package oracle.kv.util.shell;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class encapsulates the method to read key-value pairs in a section of
 * configuration file, it is used for a shell to read the start-up parameters
 * from a file.
 *
 * All the arguments of a shell can be configured in the rc file in "key=value"
 * format, below lists arguments for sql shell:
 *
 * [kvcli]
 * host=<hostname>
 * port=<port>
 * store=<storeName>
 * admin-host=<adminHost>
 * admin-port=<adminPort>
 * username=<user>
 * security=<security-file-path>
 * admin-username=<adminUser>
 * admin-security=<admin-security-file-path>
 * timeout=<timeout ms>
 * consistency=<NONE_REQUIRED(default) | ABSOLUTE | NONE_REQUIRED_NO_MASTER>
 * durability=<COMMIT_SYNC(default) | COMMIT_NO_SYNC | COMMIT_WRITE_NO_SYNC>
 */
public class ShellRCFile {

    private final static String RCFILE = ".kvclirc";

    /*
     * Returns a array of string that contains key/value pairs in the specified
     * section, the return value of a key/value pair is [-key, value].
     *
     * e.g. section "kvsqlcli" in .kvclirc contains below parameters.
     * [kvsqlcli]
     * helper-hosts=localhost:5000
     * store=kvstore
     * timeout=10000
     *
     * Then return string array is like below:
     *  [-helper-hosts, localhost:5000, -store, kvstore, -timeout, 10000]
     */
    public static String[] readSection(final String section) {
        final String rcFile = System.getProperty("user.home") + "/" + RCFILE;
        final List<String> args = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(rcFile));
            final String sectionName = "[" + section + "]";
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(sectionName)) {
                    break;
                }
            }
            while ((line = br.readLine()) != null) {
                if (line.startsWith("[")) {
                    break;
                }
                String params[] = line.split("=");
                if (params.length == 2) {
                    args.add("-" + params[0]);
                    args.add(params[1]);
                }
            }
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException ignored) {
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        return args.toArray(new String[args.size()]);
    }
}
