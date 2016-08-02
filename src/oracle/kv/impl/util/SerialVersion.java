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

/**
 * Defines the previous and current serialization version for services and
 * clients.
 *
 * @see oracle.kv.impl.util.registry.VersionedRemote
 */
public class SerialVersion {
    public static final short UNKNOWN = -1;

    /* R1 version */
    public static final short V1 = 1;

    /* Introduced at R2 (2.0.23) */
    public static final short V2 = 2;

    /* Introduced at R2.1 (2.1.8) */
    public static final short V3 = 3;

    /* Introduced at R3.0 (3.0.0) for secondary datacenters */
    public static final short V4 = 4;

    /* Introduced at R3.1 (3.1.0) for role-based authorization */
    public static final short V5 = 5;

    /* Introduced at R3.2 (3.2.0) for real-time session update */
    public static final short V6 = 6;

    /*
     * Introduced at R3.3 (3.3.0) for secondary Admin type and JSON flag to
     * verifyConfiguration, and password expiration.
     */
    public static final short V7 = 7;

    /*
     * Introduced at R3.4 (3.4.0) for the added replica threshold parameter on
     * plan methods, and the CommandService.getAdminStatus,
     * repairAdminQuorum, and createFailoverPlan methods.
     * Also added MetadataNotFoundException.
     *
     * Added bulk get APIs to Key/Value and Table interface.
     */
    public static final short V8 = 8;

    /*
     * Introduced at R3.5 (3.5.0) for Admin automation V1 features, including
     * json format output, error code, and Kerberos authentication.
     *
     * Added bulk put APIs to Key/Value and Table interface.
     */
    public static final short V9 = 9;

    public static final short CURRENT = V9;
}
