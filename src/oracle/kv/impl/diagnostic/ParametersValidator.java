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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import oracle.kv.impl.param.ParameterUtils;

/**
 * Check parameters are valid or not against environment or requirement.
 * The parameters include network ports, network hosts, directories,
 * memory size and the number of CPUs.
 */

public class ParametersValidator {

    private static final String LOCAL_IP = "127.0.0.1";
    private static final String BROADCAST_IP = "0.0.0.0";

    /**
     * Bind a socket address to check the port in the address is
     * available or not.
     *
     * @param socketAddress
     *
     * @return true when the port in socket address is available; return false
     * when the port is already in use.
     */
    private static boolean bindPort(InetSocketAddress socketAddress) {
        boolean isFree = true;
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(socketAddress);

            ds = new DatagramSocket(socketAddress);
            ds.setReuseAddress(true);
        } catch (IOException e) {
            isFree = false;
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    isFree = false;
                }
            }
        }

        return isFree;
    }

    /**
     * Bind a port to check the port is available or not.
     *
     * @param port
     *
     * @return true when the port is available; return false when the port
     * is already in use.
     */
    private static boolean bindPort(int port) {
        boolean isFree = true;
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);

            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
        } catch (IOException e) {
            isFree = false;
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    isFree = false;
                }
            }
        }

        return isFree;
    }

    /**
     * Check a port is available or not against the current environment.
     *
     * @param parameterName associated name of port
     * @param port value of port
     *
     * @return null when the port is available; return error message when the
     * port is already in use.
     */
    public static String checkPort(String parameterName, int port) {
        try {
            if (bindPort(port) &&
               bindPort(new InetSocketAddress(InetAddress.getLocalHost().
                   getHostName(), port)) &&
               bindPort(new InetSocketAddress(BROADCAST_IP, port)) &&
               bindPort(new InetSocketAddress(LOCAL_IP, port))) {
                return null;
            }
            return "Specified " + parameterName + " " +
                   port + " is already in use";
        } catch (UnknownHostException ex) {
            return ex.toString();
        }
    }

    /**
     * Check a directory exists or not.
     *
     * @param parameterName associated name of directory
     * @param directoryName path of directory
     *
     * @return null when the directory exists; return error message when the
     * directory does not exist.
     */
    public static String checkDirectory(String parameterName,
                                        String directoryName) {
        File file = new File(directoryName);
        if (file.exists() && file.isDirectory()) {
            return null;
        }
        return "Specified " + parameterName + " " +
               directoryName + " does not exist";
    }

    /**
     * Check a file exists or not.
     *
     * @param parameterName associated name of file
     * @param directoryName path of directory contains the file
     * @param fileName name of file
     *
     * @return null when the file exists; return error message when the
     * file does not exist.
     */
    public static String checkFile(String parameterName,
                                    String directoryName,
                                    String fileName) {
        File file = new File(directoryName, fileName);
        if (file.exists() && file.isFile()) {
            return null;
        }
        return "Specified " + parameterName + " " + fileName +
               " does not exist in " + directoryName;
    }

    /**
     * Check a file exists or not.
     *
     * @param parameterName associated name of file
     * @param filePath path of file
     *
     * @return null when the file exists; return error message when the
     * file does not exist.
     */
    public static String checkFile(String parameterName, String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return null;
        }
        return "Specified " + parameterName + " " + filePath +
               " does not exist";
    }

    /**
     * Check a host is reachable or not.
     *
     * @param parameterName associated name of host
     * @param hostname name of host
     *
     * @return null when the host is reachable; return error message when the
     * file is unreachable.
     */
    public static String checkHostname(String parameterName, String hostname) {

        boolean isReachable = true;
        try {
            InetAddress.getByName(hostname);
        } catch (UnknownHostException ex) {
            isReachable = false;
        }
        if (isReachable) {
            return null;
        }
        return "Specified " + parameterName + " " +
               hostname +  " not reachable";
    }

    /**
     * Check a local host is resolvable or not.
     *
     *
     * @return null when the local host is resolvable; return error message
     * when the file is not resolvable.
     */
    public static String checkLocalHostname() {

        boolean isResolvable = true;
        try {
            InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            isResolvable = false;
        }
        if (isResolvable) {
            return null;
        }
        return "The local host name could not be resolved into an address";
    }

    /**
     * Check ports within a range are available or not.
     *
     * @param parameterName associated name of the range of ports
     * @param rangePorts a range of ports
     *
     * @return null when all ports are available; return error message when a
     * or more ports are not available.
     */
    public static String checkRangePorts(String parameterName,
                                         String rangePorts) {
        StringTokenizer tokenizer = new StringTokenizer(rangePorts,
                ParameterUtils.HELPER_HOST_SEPARATOR);
        int firstHAPort = Integer.parseInt(tokenizer.nextToken());
        int secondHAPort = Integer.parseInt(tokenizer.nextToken());
        String retMsg;
        for (int i=firstHAPort; i<=secondHAPort; i++) {
            retMsg = checkPort(parameterName, i);
            if (retMsg != null) {
                return retMsg;
            }
        }
        return null;
    }

    /**
     * Check a number is greater than or equal to 0 or not.
     *
     * @param parameterName associated name of the number
     * @param positiveInteger value of the number
     *
     * @return null when the number is greater than or equal to 0; return
     * error message when the number is less than 0.
     */
    public static String checkPositiveInteger(String parameterName,
                                              int positiveInteger) {
        if (positiveInteger < 0) {
            return positiveInteger + " is invalid; " +
                   parameterName + " must be >= 0";
        }
        return null;

    }



    /**
     * Check the number of ports within a range is greater than or equal to
     * the expected number or not.
     *
     * @param parameterName associated name of the range of ports
     * @param rangePorts a range of ports
     * @param expectedNumberOfPort the expected number of ports
     *
     * @return null when the number of ports within a range greater than or
     * equal to the expected number; return error message when the number
     * of ports within a range is less than the expected number
     */
    public static String checkRangePortsNumber(String parameterName,
                                               String rangePorts,
                                               int expectedNumberOfPort) {
        StringTokenizer tokenizer = new StringTokenizer(rangePorts,
                ParameterUtils.HELPER_HOST_SEPARATOR);
        int firstHAPort = Integer.parseInt(tokenizer.nextToken());
        int secondHAPort = Integer.parseInt(tokenizer.nextToken());

        if (secondHAPort - firstHAPort + 1 < expectedNumberOfPort) {
            return "Specified " + parameterName + " " + rangePorts +
                   " size is less than the number of nodes " +
                   expectedNumberOfPort;
        }
        return null;
    }

    /**
     * Check the number of CPU of computer is greater than or equal to
     * the expected number or not.
     *
     * @param parameterName associated name of the CPU number
     * @param cpuNums the expected number of CPU
     *
     * @return null when the number of CPU of computer is greater than or
     * equal to the expected number; return error message when the number
     * of CPU is less than the expected number
     */
    public static String checkCPUNumber(String parameterName, int cpuNums) {
        OperatingSystemMXBean bean =
                ManagementFactory.getOperatingSystemMXBean();

        if (cpuNums > bean.getAvailableProcessors()) {
            return cpuNums + " is invalid; " + parameterName +" must be <= " +
                   bean.getAvailableProcessors() +
                   "(the number of available processors)";
        }
        return null;
    }

    /**
     * Check the memory size of computer is greater than or equal to
     * the expected number or not.
     *
     * @param parameterName associated name of the memory size
     * @param memSize the expected memory size
     * @param capacity the capacity of a SNA
     *
     * @return null when the memory size of computer is greater than or
     * equal to the expected size; return error message when memory size
     * is less than the expected size
     */
    public static String checkMemorySize(String parameterName,
                                         int memSize, int capacity) {
        OperatingSystemMXBean bean =
                ManagementFactory.getOperatingSystemMXBean();
        final Class<? extends OperatingSystemMXBean> beanClass =
                bean.getClass();
        long mem;
        String intBitsString;
        try {
            final Method m = beanClass.getMethod("getTotalPhysicalMemorySize");
            m.setAccessible(true);
            mem = (Long) m.invoke(bean);

            /*
             * This call will work because, if the above worked, we are likely
             * using a Sun JVM.
             */
            intBitsString = System.getProperty("sun.arch.data.model");
        } catch (Exception e) {
            mem = 0;
            intBitsString = null;
        }
        final int availableMem =
            computeAvailableMemoryMB(capacity, mem, intBitsString);

        if (memSize > availableMem) {
            return memSize + " is invalid; " + parameterName + " must be <= " +
                   availableMem + "(the total available memory)";
        }

        return null;
    }

    /**
     * Compute the amount of memory available in megabytes.
     *
     * @param capacity the number of virtual machines
     * @param mem the total physical memory available in bytes, or 0 if not
     * known
     * @param intBitsString a string representing the number of bits used to
     * represent ints, or null if not known
     */
    static int computeAvailableMemoryMB(int capacity,
                                        long mem,
                                        String intBitsString) {
        if (intBitsString != null) {
            int intBits;
            try {
                intBits = Integer.parseInt(intBitsString);
            } catch (NumberFormatException e) {
                intBits = 0;
            }
            if (intBits == 32) {
                final long maxInt = Integer.MAX_VALUE;
                if ((mem / capacity) > maxInt) {
                    mem = maxInt * capacity;
                }
            }
        }
        return (int) (mem >> 20);
    }
}
