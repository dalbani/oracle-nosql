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

import java.util.Set;

import oracle.kv.impl.admin.plan.SecurityMetadataPlan.PrivilegePlan;
import oracle.kv.impl.security.RoleInstance;
import oracle.kv.impl.security.metadata.SecurityMetadata;

/**
 * Grant privileges to user-defined role.
 */
public class GrantPrivileges extends PrivilegeTask {

    private static final long serialVersionUID = 1L;

    public GrantPrivileges(PrivilegePlan plan,
                           String roleName,
                           String tableName,
                           Set<String> privNames) {
       super(plan, roleName, tableName, privNames);
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        SecurityMetadata secMd = plan.getMetadata();

        /* Return null if grantee does not exist */
        if (secMd == null || secMd.getRole(roleName) == null) {
            return null;
        }

        final RoleInstance roleCopy = secMd.getRole(roleName).clone();
        secMd.updateRole(roleCopy.getElementId(),
                         roleCopy.grantPrivileges(privileges));
        plan.getAdmin().saveMetadata(secMd, plan);

        return secMd;
    }

    /**
     * Returns true if this GrantPrivilegs will end up granting the same
     * privileges to the same role. Checks that roleName and privilege set
     * are the same.
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

        GrantPrivileges other = (GrantPrivileges) t;
        if (!roleName.equalsIgnoreCase(other.roleName)) {
            return false; 
        }

        return privileges.equals(other.privileges);
    }
}
