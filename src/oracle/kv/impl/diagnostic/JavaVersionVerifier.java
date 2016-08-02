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

import java.util.HashMap;
import java.util.Map;

/**
 * Verify whether the version of the JDK running on this node meets the
 * suggested minimum requirements.
 */

public class JavaVersionVerifier extends DiagnosticVerifier {

    /* Current supported JDK version */
    private final Map<String, JavaVersion> supportedJavaVersionMap =
        new HashMap<String, JavaVersion>();

    /* The java vendor of the installed JDK */
    private String installedJavaVendor;

     /* The java version of the installed JDK */
    private JavaVersion installedJavaVersion;

    /*
     * If non-null, add a suggestion that this override flag (usually -force)
     * can be used to override the Java version requirement.
     */
    private String overrideFlag;

    public JavaVersionVerifier() {
        this(false, null);
    }

    public JavaVersionVerifier(boolean returnOnError, String overrideFlag) {
        super(returnOnError);

        /* Add supported Java vendors and associated Java versions */
        supportedJavaVersionMap.put("Oracle Corporation",
                                  new JavaVersion("1.7.0_51"));
        supportedJavaVersionMap.put("IBM Corporation",
                                  new JavaVersion("1.7.0"));

        /* Get the installed JDK vendor name */
        installedJavaVendor = System.getProperty("java.vendor");

        /* Get the installed JDK version */
        installedJavaVersion =
            new JavaVersion(System.getProperty("java.version"));

        this.overrideFlag = overrideFlag;
    }

    @Override
    public boolean doWork() {

        /* Find the supported java version for the installed java vendor */
        JavaVersion supportedJavaVersion =
            supportedJavaVersionMap.get(installedJavaVendor);

        /* Return false if the installed java vendor is not supported */
        if (supportedJavaVersion == null) {
            printMessage(installedJavaVendor + " JDK is not supported. " +
                         displaySupportedSet());
            return false;
        }

        /* return false when the installed JDK is earlier than the minimum. */
        if (installedJavaVersion.compareTo(supportedJavaVersion) < 0) {
            printMessage(installedJavaVendor + " JDK " +
                         installedJavaVersion +
                         " is not supported. " + displaySupportedSet());
            if (returnOnError) {
                return false;
            }
        }
        return true;
    }

    /**
     * Display the supported versions, so as to create a better error message.
     */
    private String displaySupportedSet() {
        StringBuilder sb = new StringBuilder();
        sb.append("Please use a Java version equal to or newer than the " +
                  "following recommended versions: ");
        boolean first = true;
        for (Map.Entry<String, JavaVersion> e :
             supportedJavaVersionMap.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append(" Java ").append(e.getValue());
            if (first) {
                first = false;
            }
        }

        if (overrideFlag != null) {
            sb.append(". To override this requirement, use ").
                append(overrideFlag).append(".");
        }

        return sb.toString();
    }

    /**
     * Set JDK info including Java vendor and Java version. This method is only
     * used in unit test.
     *
     * @param javaVendor the name of java vendor
     * @param javaVersion the string of java version
     */
    public void setJKDVersionInfo(String javaVendor, String javaVersion) {
        this.installedJavaVendor = javaVendor;
        this.installedJavaVersion = new JavaVersion(javaVersion);
    }

    /**
     * Get the Serialized result of this class and it contains the vendor and
     * version of installed JDK
     */
    @Override
    public String toString() {
        if (installedJavaVendor == null || installedJavaVersion == null) {
            return null;
        }
        return "Vendor: " + installedJavaVendor + ", version: " +
                installedJavaVersion;
    }


    /**
     * Format the string of JDK version and make it can be compared.
     *
     * All version chars are in a-z, A-Z and 0-9. Other chars are the separator
     * of the version string. For example, the current JDK version is 1.7.0_51.
     * The char . and _ are not in a-z, A-Z and 0-9, so they are the separator
     * of the version string. After determining what chars are separators, it is
     * easy to get version numbers:
     * The first version number is 1
     * The second version number is 7
     * The third version number is 0
     * The last version number is 51.
     *
     * Use the same approach to split another compared JDK version and compare
     * the version numbers in int type. Comparison is that first version number
     * compares with the first version number. If they are not equal return
     * comparison result. If the they are equal, then compare the second version
     * numbers. And so on, for each version number of version string.
     */
    private class JavaVersion {
        private String javaVersion = null;
        private String VERSION_SEPARATOR = "[^a-zA-Z0-9]+";

        public JavaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
        }

        /**
         * compare version string of JDK
         *
         * @param comparedVersion compared version of JDK
         *
         * @return the result of comparison. 0 is equal, greater than 0 is
         * greater than compared version, less than 0 is less than compared
         * version.
         */
        public int compareTo(JavaVersion comparedVersion) {
            if (this.javaVersion == null &&
                    comparedVersion.javaVersion != null) {
                return -1;
            } else if (this.javaVersion != null &&
                      comparedVersion.javaVersion == null) {
                return 1;
            } else if (this.javaVersion == null &&
                      comparedVersion.javaVersion == null) {
                return 0;
            }

            String[] versionArray = javaVersion.split(VERSION_SEPARATOR);
            String[] comparedVersionArray =
                    comparedVersion.javaVersion.split(VERSION_SEPARATOR);

            int comparedLength = Math.min(versionArray.length,
                    comparedVersionArray.length);

            for (int i=0; i<comparedLength; i++) {
                if (i == versionArray.length) {
                    return i == comparedVersionArray.length? 0 : -1;
                } else if (i == comparedVersionArray.length) {
                    return 1;
                }

                int numberVersion;
                int compareNumberVersion;
                try {
                    numberVersion = Integer.parseInt(versionArray[i]);
                } catch (Exception e) {
                    numberVersion = Integer.MAX_VALUE;
                }

                try {
                    compareNumberVersion = Integer.parseInt(
                            comparedVersionArray[i]);
                } catch (Exception e) {
                    compareNumberVersion = Integer.MAX_VALUE;
                }

                if (numberVersion > compareNumberVersion) {
                    return 1;
                } else if (numberVersion < compareNumberVersion) {
                    return -1;
                }

                int compareResult = versionArray[i].compareTo(
                        comparedVersionArray[i]);

                if (compareResult != 0) {
                    return compareResult;
                }
            }

            return 0;
        }

        @Override
        public String toString() {
            return javaVersion;
        }
    }
}
