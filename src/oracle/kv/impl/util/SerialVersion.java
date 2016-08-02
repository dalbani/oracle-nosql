/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
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

import oracle.kv.KVVersion;

/**
 * Defines the previous and current serialization version for services and
 * clients.
 *
 * As features that affect serialized formats are introduced constants
 * representing those features should be added here, associated with the
 * versions in which they are introduced. This creates a centralized location
 * for finding associations of features, serial versions, and release
 * versions. Existing constants (as of release 4.0) are spread throughout the
 * source and can be moved here as time permits.
 *
 * @see oracle.kv.impl.util.registry.VersionedRemote
 */
public class SerialVersion {
    public static final short UNKNOWN = -1;
    private static final KVVersion[] kvVersions = new KVVersion[11];

    /* R1 version */
    public static final short V1 = init(1, KVVersion.R1_2_123);

    /* Introduced at R2 (2.0.23) */
    public static final short V2 = init(2, KVVersion.R2_0_23);

    /* Introduced at R2.1 (2.1.8) */
    public static final short V3 = init(3, KVVersion.R2_1);

    /*
     * Introduced at R3.0 (3.0.5)
     *  - secondary datacenters
     *  - table API
     */
    public static final short V4 = init(4, KVVersion.R3_0);
    public static final short TABLE_API_VERSION = V4;

    /* Introduced at R3.1 (3.1.0) for role-based authorization */
    public static final short V5 = init(5, KVVersion.R3_1);

    /*
     * Introduced at R3.2 (3.2.0):
     * - real-time session update
     * - index key iteration
     */
    public static final short V6 = init(6, KVVersion.R3_2);

    public static final short RESULT_INDEX_ITERATE_VERSION = V6;

    /*
     * Introduced at R3.3 (3.3.0) for secondary Admin type and JSON flag to
     * verifyConfiguration, and password expiration.
     */
    public static final short V7 = init(7, KVVersion.R3_2);

    /*
     * Introduced at R3.4 (3.4.0) for the added replica threshold parameter on
     * plan methods, and the CommandService.getAdminStatus,
     * repairAdminQuorum, and createFailoverPlan methods.
     * Also added MetadataNotFoundException.
     *
     * Added bulk get APIs to Key/Value and Table interface.
     */
    public static final short V8 = init(8, KVVersion.R3_4);
    public static final short BATCH_GET_VERSION = V8;

    /*
     * Introduced at R3.5 (3.5.0) for Admin automation V1 features, including
     * json format output, error code, and Kerberos authentication.
     *
     * Added bulk put APIs to Key/Value and Table interface.
     */
    public static final short V9 = init(9, KVVersion.R3_5);
    public static final short BATCH_PUT_VERSION = V9;

    /*
     * Introduced at R4.0/V10:
     * - new query protocol operations
     * - time to live
     * - Arbiters
     * - Full text search
     */
    public static final short V10 = init(10, KVVersion.R4_0);
    public static final short TTL_SERIAL_VERSION = V10;
    public static final short QUERY_VERSION = V10;

    public static final short CURRENT = 10;

    private static short init(int version, KVVersion kvVersion) {
        kvVersions[version] = kvVersion;
        return (short) version;
    }

    public static KVVersion getKVVersion(short serialVersion) {
        return kvVersions[serialVersion];
    }
}
