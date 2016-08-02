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
package oracle.kv.impl.admin;

import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.KVBuiltInRoleResolver;
import oracle.kv.impl.security.RoleInstance;
import oracle.kv.impl.security.RoleResolver;
import oracle.kv.impl.security.metadata.SecurityMetadata;
import oracle.kv.impl.security.util.Cache;
import oracle.kv.impl.security.util.CacheBuilder;
import oracle.kv.impl.security.util.CacheBuilder.CacheConfig;
import oracle.kv.impl.security.util.CacheBuilder.CacheEntry;

/**
 * A role resolver resides on admin
 */
public class AdminRoleResolver implements RoleResolver {

    /* The AdminService being supported */
    private final AdminService adminService;

    /* Role cache */
    private final Cache<String, RoleEntry> roleCache;

    public AdminRoleResolver(AdminService adminService,
                             CacheConfig cacheConfig) {
        this.adminService = adminService;
        this.roleCache = CacheBuilder.build(cacheConfig);
    }

    /**
     * Update by removing the entry if role instance passed in was cached.
     *
     * @return true if role instance was in cache and removed successfully.
     */
    public boolean updateRoleCache(RoleInstance role) {
        return (roleCache.invalidate(role.name()) != null);
    }

    @Override
    public RoleInstance resolve(String roleName) {
        /* Try to resolve it as a built-in role */
        RoleInstance role = KVBuiltInRoleResolver.resolveRole(roleName);

        /* Not a built-in role, resolve it as a user-defined role */
        if (role == null) {
            final RoleEntry entry = roleCache.get(
                RoleInstance.getNormalizedName(roleName));

            if (entry != null) {
                return entry.getRole();
            }

            final SecurityMetadata secMd = adminService.getAdmin().
                getMetadata(SecurityMetadata.class, MetadataType.SECURITY);
            if ((secMd == null) || (secMd.getAllRoles().isEmpty())) {
                return null;
            }
            role = secMd.getRole(roleName);
            if (role != null) {
                roleCache.put(role.name(),
                              new RoleEntry(role));
            }
        }
        return role;
    }

    /**
     * Cache entry for RoleInstance
     */
    private final class RoleEntry extends CacheEntry {

        private final RoleInstance role;

        RoleEntry(final RoleInstance roleInstance) {
            super();
            role = roleInstance;
        }

        RoleInstance getRole() {
            return role;
        }
    }
}
