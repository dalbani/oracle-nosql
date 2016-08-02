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

package oracle.kv.impl.security.metadata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import oracle.kv.impl.security.metadata.SecurityMetadata.SecurityElementType;

/**
 * A class to help update user and role information maintained in security
 * components once there are changes of user and role definition.
 */
public class SecurityMDUpdater {

    private final Set<UserChangeUpdater> userUpdaters =
        Collections.synchronizedSet(new HashSet<UserChangeUpdater>());

    private final Set<RoleChangeUpdater> roleUpdaters =
        Collections.synchronizedSet(new HashSet<RoleChangeUpdater>());

    public void addUserChangeUpdaters(UserChangeUpdater... updaters) {
        for (final UserChangeUpdater updater : updaters) {
            userUpdaters.add(updater);
        }
    }

    public void addRoleChangeUpdaters(RoleChangeUpdater... updaters) {
        for (final RoleChangeUpdater updater : updaters) {
            roleUpdaters.add(updater);
        }
    }

    /**
     * User definition change listener. Registered user definition updaters will
     * be notified of the changes.
     */
    public class UserChangeListener implements SecurityMDListener {

        @Override
        public void notifyMetadataChange(SecurityMDChange mdChange) {
            if (mdChange.getElementType() != SecurityElementType.KVSTOREUSER) {
                return;
            }

            for (final UserChangeUpdater userUpdater : userUpdaters) {
                userUpdater.newUserDefinition(mdChange);
            }
        }
    }

    /**
     * Role definition change listener. Registered role definition updaters will
     * be notified of the changes.
     */
    public class RoleChangeListener implements SecurityMDListener {

        @Override
        public void notifyMetadataChange(SecurityMDChange mdChange) {
            if (mdChange.getElementType() != SecurityElementType.KVSTOREROLE) {
                return;
            }

            for (final RoleChangeUpdater roleUpdater : roleUpdaters) {
                roleUpdater.newRoleDefinition(mdChange);
            }
        }
    }

    /**
     * Updater interface for user definition change
     * 
     */
    public interface UserChangeUpdater {
        /**
         * Notifies a new user definition change.
         *
         * @param mdChange the security metadata change which should contains
         * the new KVStoreUser instance
         */
        void newUserDefinition(SecurityMDChange mdChange);
    }

    /**
     * Updater interface for role definition change
     */
    public interface RoleChangeUpdater {
        /**
         * Notifies a new role definition change.
         *
         * @param mdChange the security metadata change which should contains
         * the new RoleInstance instance
         */
        void newRoleDefinition(SecurityMDChange mdChange);
    }
}
