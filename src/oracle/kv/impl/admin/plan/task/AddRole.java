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

package oracle.kv.impl.admin.plan.task;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.plan.SecurityMetadataPlan;
import oracle.kv.impl.security.KVBuiltInRoleResolver;
import oracle.kv.impl.security.KVStorePrivilegeLabel;
import oracle.kv.impl.security.RoleInstance;
import oracle.kv.impl.security.metadata.SecurityMetadata;

/**
 * Add a user-defined role
 */
public class AddRole extends UpdateMetadata<SecurityMetadata> {

    private static final long serialVersionUID = 1L;

    private final String roleName;

    public AddRole(SecurityMetadataPlan plan, String roleName) {
        super(plan);

        final SecurityMetadata secMd = plan.getMetadata();
        this.roleName = roleName;

        if (secMd != null && secMd.getRole(roleName) == null) {
            /*
             * Do not allow define role with duplicate name of system
             * built-in roles and privileges
             */
            if (KVBuiltInRoleResolver.resolveRole(roleName) != null) {
                throw new IllegalCommandException(
                    "Role with name " + roleName + 
                    " is system-builtin role.");
            }
            try {
                KVStorePrivilegeLabel.valueOf(roleName.toUpperCase());
                throw new IllegalCommandException(
                    "Name of " + roleName + 
                    " already exists as a privilege");
            } catch (IllegalArgumentException iae) {
                /* Normal case, the name should not be a priv label name */
            }
        }
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        SecurityMetadata md = plan.getMetadata();
        if (md == null) {
            final String storeName =
                    plan.getAdmin().getParams().getGlobalParams().
                    getKVStoreName();
            md = new SecurityMetadata(storeName);
        }

        if (md.getRole(roleName) == null) {
            final RoleInstance newRole = new RoleInstance(roleName);
            md.addRole(newRole);
            plan.getAdmin().saveMetadata(md, plan);
        }
        return md;
    }

    /**
     * Returns true if this AddRole will end up creating the same role.
     * Checks that roleName are the same.
     */
    @Override
    public boolean logicalCompare(Task t) {
        if (this == t) {
            return true;
        }

        if (t == null) {
            return false;
        }

        if (getClass() != t.getClass()) {
            return false;
        }

        AddRole other = (AddRole) t;
        return roleName.equalsIgnoreCase(other.roleName);
    }
}
