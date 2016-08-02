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

import oracle.kv.impl.admin.plan.SecurityMetadataPlan;
import oracle.kv.impl.security.metadata.KVStoreUser;
import oracle.kv.impl.security.metadata.KVStoreUser.UserType;
import oracle.kv.impl.security.metadata.SecurityMetadata;

/**
 * Add external user.
 */
public class AddExternalUser extends UpdateMetadata<SecurityMetadata> {

    private static final long serialVersionUID = 1L;

    private final String userName;
    private final boolean isEnabled;
    private final boolean isAdmin;

    public AddExternalUser(SecurityMetadataPlan plan,
                           String userName,
                           boolean isEnabled,
                           boolean isAdmin) {
        super(plan);

        final SecurityMetadata secMd = plan.getMetadata();

        Utils.ensureFirstAdminUser(secMd, isEnabled, isAdmin);

        this.userName = userName;
        this.isAdmin = isAdmin;
        this.isEnabled = isEnabled;

        Utils.checkPreExistingUser(secMd, userName, isEnabled, isAdmin,
                                   null /* no password */);
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

        if (md.getUser(userName) == null) {
            final KVStoreUser newUser =
                KVStoreUser.newInstance(userName, true /* enableRoles */);
            newUser.setEnabled(isEnabled).setAdmin(isAdmin).
                setUserType(UserType.EXTERNAL);

            md.addUser(newUser);
            plan.getAdmin().saveMetadata(md, plan);
        }

        return md;
    }

    /**
     * Returns true if this AddUser will end up creating the same user.
     * Checks that userName, isEnabled, isAdmin are the same.
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

        AddExternalUser other = (AddExternalUser) t;
        if (!userName.equals(other.userName)) {
            return false; 
        }

        if (isEnabled != other.isEnabled || isAdmin != other.isAdmin) {
            return false;
        }

        return true;
    }
}
