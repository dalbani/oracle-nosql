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
import oracle.kv.impl.security.metadata.KVStoreUser;
import oracle.kv.impl.security.metadata.SecurityMetadata;

import com.sleepycat.persist.model.Persistent;

/**
 * Grant roles to user.
 */
@Persistent
public class GrantRoles extends UpdateMetadata<SecurityMetadata> {

    private static final long serialVersionUID = 1L;

    private String grantee;
    private Set<String> roles;

    public GrantRoles(SecurityMetadataPlan plan,
                      String grantee,
                      Set<String> roles) {
        super(plan);

        final SecurityMetadata secMd = plan.getMetadata();

        this.grantee = grantee;
        this.roles = roles;

        /* Return null if grantee does not exist */
        if ((secMd == null) || (secMd.getUser(grantee) == null)) {
            throw new IllegalCommandException(
                "User with name " + grantee + " does not exist in store");
        }
    }

    /*
     * No-arg ctor for use by DPL.
     */
    @SuppressWarnings("unused")
    private GrantRoles() {
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        SecurityMetadata secMd = plan.getMetadata();

        /* Return null if grantee does not exist */
        if (secMd == null ||
            secMd.getUser(grantee) == null) {

            return null;
        }
        final KVStoreUser newCopy = secMd.getUser(grantee).clone();
        secMd.updateUser(newCopy.getElementId(), newCopy.grantRoles(roles));
        plan.getAdmin().saveMetadata(secMd, plan);

        return secMd;
    }

    /**
     * Returns true if this GrantRoles will end up granting the same
     * roles to the same user. Checks that grantee and role set
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

        GrantRoles other = (GrantRoles) t;
        if (!grantee.equalsIgnoreCase(other.grantee)) {
            return false; 
        }

        return roles.equals(other.roles);
    }
}
