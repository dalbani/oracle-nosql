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
package oracle.kv.impl.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple role resolver for only those KVStore built-in roles.
 */
public class KVBuiltInRoleResolver implements RoleResolver {

    /* A role map to store all system predefined roles */
    private static final Map<String, RoleInstance> roleMap =
        new HashMap<String, RoleInstance>();

    static {
        roleMap.put(RoleInstance.DBADMIN.name(), RoleInstance.DBADMIN);
        roleMap.put(RoleInstance.SYSADMIN.name(), RoleInstance.SYSADMIN);
        roleMap.put(RoleInstance.INTERNAL.name(), RoleInstance.INTERNAL);
        roleMap.put(RoleInstance.READONLY.name(), RoleInstance.READONLY);
        roleMap.put(RoleInstance.WRITEONLY.name(), RoleInstance.WRITEONLY);
        roleMap.put(RoleInstance.READWRITE.name(), RoleInstance.READWRITE);
        roleMap.put(RoleInstance.PUBLIC.name(), RoleInstance.PUBLIC);

        /* Used for backward compatibility with R3 nodes during upgrade */
        roleMap.put(RoleInstance.ADMIN.name(), RoleInstance.ADMIN);
        roleMap.put(RoleInstance.AUTHENTICATED.name(), RoleInstance.AUTHENTICATED);
    }

    @Override
    public RoleInstance resolve(String roleName) {
        return resolveRole(roleName);
    }

    public static RoleInstance resolveRole(String roleName) {
        return roleMap.get(RoleInstance.getNormalizedName(roleName));
    }

    /* For test purpose */
    Collection<RoleInstance> getAllRoles() {
        return roleMap.values();
    }
}