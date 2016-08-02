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
import oracle.kv.impl.admin.plan.SecurityMetadataPlan;
import oracle.kv.impl.security.RoleInstance;
import oracle.kv.impl.security.metadata.KVStoreUser;
import oracle.kv.impl.security.metadata.SecurityMetadata;

import com.sleepycat.persist.model.Persistent;

/**
 * Revoke roles from user.
 */
@Persistent
public class RevokeRoles extends UpdateMetadata<SecurityMetadata> {

    private static final long serialVersionUID = 1L;

    private String userName;
    private Set<String> roles;

    public RevokeRoles(SecurityMetadataPlan plan,
                       String userName,
                       Set<String> roles) {
        super(plan);

        final SecurityMetadata secMd = plan.getMetadata();
        this.userName = userName;
        this.roles = roles;

        /* Return null if target user does not exist */
        if ((secMd == null) || (secMd.getUser(userName) == null)) {
            throw new IllegalCommandException(
                "User with name " + userName + " does not exist in store");
        }

        if (roles.contains(RoleInstance.SYSADMIN_NAME)) {
            guardLastSystemUser(secMd);
        }
    }

    /**
     * Guard against revoking sysadmin role from the last enabled admin user.
     */
    private void guardLastSystemUser(final SecurityMetadata secMd) {
        if (secMd == null) {
            return;
        }

        if (secMd.isLastSysadminUser(userName)) {

            throw new IllegalCommandException(
                "Cannot revoke sysadmin role from " +
                "the last enabled admin user " + userName);
        }
    }

    /*
     * No-arg ctor for use by DPL.
     */
    @SuppressWarnings("unused")
    private RevokeRoles() {
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        final SecurityMetadata secMd = plan.getMetadata();

        /* Return null user does not exist */
        if (secMd == null ||
            secMd.getUser(userName) == null) {
            return null;
        }

        final KVStoreUser newCopy = secMd.getUser(userName).clone();
        secMd.updateUser(newCopy.getElementId(), newCopy.revokeRoles(roles));
        plan.getAdmin().saveMetadata(secMd, plan);

        return secMd;
    }

    /**
     * Returns true if this RevokeRoles will end up revoking the same
     * roles to the same user. Checks that user name and role set
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

        RevokeRoles other = (RevokeRoles) t;
        if (!userName.equals(other.userName)) {
            return false; 
        }

        return roles.equals(other.roles);
    }
}
