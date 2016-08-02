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
import oracle.kv.impl.security.metadata.SecurityMetadata;

/**
 * Remove a user-defined role.
 */
public class RemoveRole extends UpdateMetadata<SecurityMetadata> {

    private static final long serialVersionUID = 1L;

    private final String roleName;

    public RemoveRole(SecurityMetadataPlan plan,
                      String roleName) {
        super(plan);

        /* Check whether the specified role is the system build-in role */
        if (KVBuiltInRoleResolver.resolveRole(roleName) != null) {
            throw new IllegalCommandException(
                "Cannot drop a system built-in role");
        }
        this.roleName = roleName;
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        final SecurityMetadata secMd = plan.getMetadata();

        if (secMd != null && secMd.getRole(roleName) != null) {

            /* The user-defined role exists, so remove the entry from the MD */
            secMd.removeRole(secMd.getRole(roleName).getElementId());
            plan.getAdmin().saveMetadata(secMd, plan);
            return secMd;
        }
        return null;
    }

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

        RemoveRole other = (RemoveRole) t;
        return roleName.equalsIgnoreCase(other.roleName);
    }
}
