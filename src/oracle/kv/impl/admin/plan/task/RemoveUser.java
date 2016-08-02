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
import oracle.kv.impl.security.metadata.SecurityMetadata;

import com.sleepycat.persist.model.Persistent;

/**
 * Remove a user
 */
@Persistent
public class RemoveUser extends UpdateMetadata<SecurityMetadata> {

    private static final long serialVersionUID = 1L;

    private String userName;

    public RemoveUser(SecurityMetadataPlan plan,
                      String userName) {
        super(plan);

        final SecurityMetadata secMd = plan.getMetadata();
        this.userName = userName;
        guardLastSysadminUser(secMd);
    }

    @SuppressWarnings("unused")
    private RemoveUser() {
    }

    /**
     * Guard against removing the last enabled admin user.
     */
    private void guardLastSysadminUser(SecurityMetadata secMd) {
        if (secMd == null) {
            return;
        }

        if (secMd.isLastSysadminUser(userName)) {
            throw new IllegalCommandException(
                "Cannot drop the last enabled admin user " + userName);
        }
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        final SecurityMetadata secMd = plan.getMetadata();
        if (secMd != null && secMd.getUser(userName) != null) {
            guardLastSysadminUser(secMd);

            /* The user exists, so remove the entry from the MD */
            secMd.removeUser(secMd.getUser(userName).getElementId());
            plan.getAdmin().saveMetadata(secMd, plan);
        }

        return secMd;
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

        RemoveUser other = (RemoveUser) t;
        return userName.equals(other.userName);
    }
}
