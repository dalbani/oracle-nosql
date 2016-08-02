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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * A base class for programs that have command line arguments.  It implements
 * several common arguments as well as a mechanism for class extension to add
 * more arguments.
 *
 * Usage pattern:
 *   1.  instantiate the object
 *   2.  call parseArgs()
 *
 * These abstract methods must be implemented by extending classes: usage()
 * checkArg() verifyArgs().  See javadoc for details.
 *
 * Arguments supported by this class directly:
 *  host:        -host <hostname> (defaults to "hostname")
 *  port:        -port <port>
 *  storename:   -store <store name>
 *  rootdir:     -root <full_path_to_root_dir>
 *  admin port:  -admin <port>
 *  security file: -security <security file>
 *  username:    -username <username>
 *  usage:       -usage
 *  dns cache ttl: -dns-cachettl <ttl>
 */
public abstract class CommandParser {

    /**
     * Flag strings
     */
    public static final String ROOT_FLAG = "-root";
    public static final String HOST_FLAG = "-host";
    public static final String STORE_FLAG = "-store";
    public static final String PORT_FLAG = "-port";
    public static final String ADMIN_HOST_FLAG = "-admin-host";
    public static final String ADMIN_PORT_FLAG = "-admin-port";
    public static final String ADMIN_FLAG = "-admin";
    public static final String USAGE_FLAG = "-usage";
    public static final String VERBOSE_FLAG = "-verbose";
    public static final String USER_FLAG = "-username";
    public static final String SECURITY_FLAG = "-security";
    public static final String ADMIN_USER_FLAG = "-admin-username";
    public static final String ADMIN_SECURITY_FLAG = "-admin-security";
    public static final String TIMEOUT_FLAG = "-timeout";
    public static final String CONSISTENCY_FLAG = "-consistency";
    public static final String DURABILITY_FLAG = "-durability";
    public static final String HELPER_HOSTS_FLAG = "-helper-hosts";
    public static final String NOCONNECT_FLAG = "-noconnect";
    public static final String NOPROMPT_FLAG = "-noprompt";
    public static final String HIDDEN_FLAG = "-hidden";
    public static final String NAME_FLAG = "-name";
    public static final String LAST_FLAG = "-last";
    public static final String FROM_FLAG = "-from";
    public static final String TO_FLAG = "-to";
    public static final String ON_FLAG = "on";
    public static final String OFF_FLAG = "off";
    public static final String JSON_FLAG = "-json";
    public static final String DNS_CACHETTL_FLAG = "-dns-cachettl";

    public static final String KVSTORE_USAGE_PREFIX =
        "Usage: java -jar KVHOME/lib/kvstore.jar ";

    public static final String KVCLI_USAGE_PREFIX =
        "Usage: java -jar KVHOME/lib/kvcli.jar ";

    protected String rootDir;
    protected String hostname;
    protected String storeName;
    protected String userName;
    protected String securityFile;
    protected String adminUserName;
    protected String adminSecurityFilePath;
    protected int registryPort;
    protected int adminPort;
    protected boolean verbose;
    protected boolean json;
    protected String[] argArray;
    protected int dnsCacheTTL = -1;
    private final boolean dontParse;

    private String[] rcArgs;
    private String[] inputArgs;
    private int argc;
    private boolean ignoreUnknown;

    public CommandParser(String[] args) {
        this(args, false);
    }

    public CommandParser(String[] args, boolean dontParse) {
        this(args, null, dontParse);
    }

    public CommandParser(String[] args, String[] rcArgs, boolean dontParse) {
        this.dontParse = dontParse;
        if (args == null) {
            throw new IllegalArgumentException();
        }
        inputArgs = args;
        this.rcArgs = rcArgs;
        ignoreUnknown = false;
    }

    public void setDefaults(String rootDir,
                            String storeName,
                            String hostname,
                            int registryPort,
                            int adminPort) {
        this.rootDir = rootDir;
        this.storeName = storeName;
        this.hostname = hostname;
        this.registryPort = registryPort;
        this.adminPort = adminPort;
    }

    public String getHostname() {
        return hostname;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getUserName() {
        return userName;
    }

    public String getSecurityFile() {
        return securityFile;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public String getAdminSecurityFile() {
        return adminSecurityFilePath;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public boolean getJson() {
        return json;
    }

    public int getDnsCacheTTL() {
         return dnsCacheTTL;
    }

    public void verbose(String msg) {
        if (verbose) {
            System.err.println(msg);
        }
    }

    public String nextArg(String arg) {
        if (argc >= argArray.length) {
            usage("Flag " + arg + " requires an argument");
        }
        return argArray[argc++];
    }

    public int nextIntArg(String arg) {
        String next = nextArg(arg);
        try {
            return Integer.parseInt(next);
        } catch (NumberFormatException e) {
            usage("Flag " + arg + " requires an integer argument");
            return 0; // for compiler, usage always ends abnormally
        }
    }

    /**
     * Returns the next argument as a long, calling the usage method if the
     * value is illegal.
     *
     * @param arg the flag associated with the next argument
     * @return the next argument as a long
     */
    public long nextLongArg(final String arg) {
        final String next = nextArg(arg);
        try {
            return Long.parseLong(next);
        } catch (final NumberFormatException e) {
            usage("Flag " + arg + " requires a long argument");
            return 0; // for compiler, usage always ends abnormally
        }
    }

    /**
     * Returns the next argument as a constant of the specified enum type.
     * Converts the value to uppercase for parsing by the enum's valueOf
     * method.  Calls the usage method if the value is illegal.
     *
     * @param <E> the enum type
     * @param arg the flag associated with the next argument
     * @param enumClass the enum class
     * @return a constant of the specified enum type
     */
    public <E extends Enum<E>> E nextEnumArg(final String arg,
                                             final Class<E> enumClass) {
        final String next = nextArg(arg);
        try {
            return E.valueOf(enumClass, next.toUpperCase());
        } catch (final IllegalArgumentException e) {
            usage("Flag " + arg + " requires a constant of enum type " +
                  enumClass.getName() + ", one of: " +
                  Arrays.toString(enumClass.getEnumConstants()));
            return null; // for compiler, usage always ends abnormally
        }
    }

    /**
     * Returns the next argument as a set of constants of the specified enum
     * type.  Looks for multiple values separated by commas, and converts
     * values to uppercase for parsing by the enum's valueOf method.  Calls the
     * usage method if any value is illegal.
     *
     * @param <E> the enum type
     * @param arg the flag associated with the next argument
     * @param enumClass the enum class
     * @return a set of constants of the specified enum type
     */
    public <E extends Enum<E>> EnumSet<E> nextEnumSetArg(
        final String arg, final Class<E> enumClass) {

        final EnumSet<E> set = EnumSet.noneOf(enumClass);
        for (final String value : nextArg(arg).split(",")) {
            try {
                set.add(Enum.valueOf(enumClass, value.toUpperCase()));
            } catch (final IllegalArgumentException e) {
                usage("Flag " + arg +
                      " requires one or more constants of enum type " +
                      enumClass.getName() + ": " +
                      Arrays.toString(enumClass.getEnumConstants()));
            }
        }
        return set;
    }

    protected int getNRemainingArgs() {
        return argArray.length - argc;
    }

    protected void missingArg(String arg) {
        usage("Flag " + arg + " is required");
    }

    private void unknownArg(String arg) {
        usage("Unknown argument: " + arg);
    }

    public boolean isIgnoreUnknownArg() {
        return ignoreUnknown;
    }

    public void parseArgs() {
        if (rcArgs != null) {
            argArray = rcArgs;
            ignoreUnknown = true;
            doParseArgs();
        }
        argArray = inputArgs;
        ignoreUnknown = false;
        doParseArgs();
        verifyArgs();
    }

    private void doParseArgs() {
        int nArgs = argArray.length;
        argc = 0;
        while (argc < nArgs) {
            String thisArg = argArray[argc++];

            /* DNS settings. */
            if (thisArg.equals(DNS_CACHETTL_FLAG)) {
                dnsCacheTTL = Integer.parseInt(nextArg(thisArg));
                java.security.Security.setProperty
                    ("networkaddress.cache.ttl" ,
                     Integer.toString(dnsCacheTTL));
                java.security.Security.setProperty
                    ("networkaddress.cache.negative.ttl" ,
                     Integer.toString(dnsCacheTTL));
                continue;
            }

            if (dontParse) {
                if (!checkArg(thisArg)) {
                    if (!isIgnoreUnknownArg()) {
                        unknownArg(thisArg);
                    }
                }
                continue;
            }
            if (thisArg.equals(ROOT_FLAG)) {
                rootDir = nextArg(thisArg);
            } else if (thisArg.equals(USAGE_FLAG)) {
                usage(null);
            } else if (thisArg.equals(VERBOSE_FLAG)) {
                verbose = true;
            } else if (thisArg.equals(HOST_FLAG)) {
                hostname = nextArg(thisArg);
            } else if (thisArg.equals(PORT_FLAG)) {
                registryPort = Integer.parseInt(nextArg(thisArg));
            } else if (thisArg.equals(ADMIN_FLAG)) {
                adminPort = Integer.parseInt(nextArg(thisArg));
            } else if (thisArg.equals(STORE_FLAG)) {
                storeName = nextArg(thisArg);
            } else if (thisArg.equals(USER_FLAG)) {
                userName = nextArg(thisArg);
            } else if (thisArg.equals(SECURITY_FLAG)) {
                securityFile = nextArg(thisArg);
            } else if (thisArg.equals(ADMIN_USER_FLAG)) {
                adminUserName = nextArg(thisArg);
            } else if (thisArg.equals(ADMIN_SECURITY_FLAG)) {
                adminSecurityFilePath = nextArg(thisArg);
            } else if (thisArg.equals(JSON_FLAG)) {
                json = true;
            } else if (!checkArg(thisArg)) {
                if (!isIgnoreUnknownArg()) {
                    unknownArg(thisArg);
                }
            }
        }
    }

    /**
     * Methods for implementing classes.
     */
    public static String optional(String msg) {
        return "[" + msg + "]";
    }

    public static String getUsage() {
        return optional(USAGE_FLAG);
    }

    public static String getRootUsage() {
        return ROOT_FLAG + " <rootDirectory>";
    }

    public static String getHostUsage() {
        return HOST_FLAG + " <hostname>";
    }

    public static String getStoreUsage() {
        return STORE_FLAG + " <storeName>";
    }

    public static String getPortUsage() {
        return PORT_FLAG + " <port>";
    }

    public static String getAdminUsage() {
        return ADMIN_FLAG + " <adminPort>";
    }

    public static String getAdminHostUsage() {
        return ADMIN_HOST_FLAG + " <adminHost>";
    }

    public static String getUserUsage() {
        return USER_FLAG + " <user>";
    }

    public static String getAdminPortUsage() {
        return ADMIN_PORT_FLAG + " <adminPort>";
    }

    public static String getSecurityUsage() {
        return SECURITY_FLAG + " <security-file-path>";
    }

    public static String getAdminUserUsage() {
        return ADMIN_USER_FLAG + " <adminUser>";
    }

    public static String getAdminSecurityUsage() {
        return ADMIN_SECURITY_FLAG + " <admin-security-file-path>";
    }

    public static String getTimeoutUsage() {
        return TIMEOUT_FLAG + " <timeout ms>";
    }

    public static String getConsistencyUsage() {
        return CONSISTENCY_FLAG +
            " <NONE_REQUIRED(default) | ABSOLUTE | NONE_REQUIRED_NO_MASTER>";
    }

    public static String getDurabilityUsage() {
        return DURABILITY_FLAG +
            " <COMMIT_SYNC(default) | COMMIT_NO_SYNC | COMMIT_WRITE_NO_SYNC>";
    }

    public static String getHelperHostUsage() {
        return HELPER_HOSTS_FLAG + " <host:port[,host:port]*>";
    }


    public static String getDnsCacheTTLUsage() {
         return DNS_CACHETTL_FLAG + " <time in sec>";
    }

    /**
     * Used to extend the base arguments to additional ones.  Returns true if
     * the argument is expected and valid.  Calls usage() if the arg is
     * recognized but invalid.  Returns false if arg is not recognized.
     */
    protected abstract boolean checkArg(String arg);

    /**
     * Called after parsing all args.  Calls usage() if all required arguments
     * are not set.
     */
    protected abstract void verifyArgs();

    /**
     * Prints optional error message, prints usage, then throws an
     * IllegalArgumentException or calls System.exit.  Must not return
     * normally.
     */
    public abstract void usage(String errorMsg);
}
