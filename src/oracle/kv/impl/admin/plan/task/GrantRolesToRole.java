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

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.plan.SecurityMetadataPlan.RolePlan;
import oracle.kv.impl.security.RoleInstance;
import oracle.kv.impl.security.metadata.SecurityMetadata;

public class GrantRolesToRole extends UpdateMetadata<SecurityMetadata> {


    private static final long serialVersionUID = 1L;
    private final String grantee;
    private final Set<String> roles;

    public GrantRolesToRole(RolePlan plan,
                            String grantee,
                            Set<String> roles) {
        super(plan);

        final SecurityMetadata secMd = plan.getMetadata();
        if (secMd == null || secMd.getRole(grantee) == null) {
            throw new IllegalCommandException(
                "Role with name " + grantee + " does not exist in store");
        }

        this.grantee = grantee;
        this.roles = roles;

        /* Detect circular role grant */
        for (String roleName : roles) {
            checkCircularGrants(roleName, secMd);
        }
    }

    private void checkCircularGrants(String roleName,
                                     SecurityMetadata secMd) {
        if (roleName.equalsIgnoreCase(grantee)) {
            throw new IllegalCommandException(
                "Could not complete grant, circular role grant detected");
        }
        final RoleInstance role = secMd.getRole(roleName);

        /* no need to check if role is system build-in role */
        if (role == null) {
            return;
        }

        for (String subRole : role.getGrantedRoles()) {
            checkCircularGrants(subRole, secMd);
        }
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        final SecurityMetadata secMd = plan.getMetadata();

        /* Return null if role does not exist */
        if (secMd == null || secMd.getRole(grantee) == null) {
            return null;
        }
        final RoleInstance newCopy = secMd.getRole(grantee).clone();
        secMd.updateRole(
            newCopy.getElementId(), newCopy.grantRoles(roles));
        plan.getAdmin().saveMetadata(secMd, plan);

        return secMd;
    }

    /**
     * Returns true if this GrantRolesToRole will end up granting the same
     * roles to the same role. Checks that grantee and role set
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

        GrantRolesToRole other = (GrantRolesToRole) t;
        if (!grantee.equalsIgnoreCase(other.grantee)) {
            return false; 
        }

        return roles.equals(other.roles);
    }
}
