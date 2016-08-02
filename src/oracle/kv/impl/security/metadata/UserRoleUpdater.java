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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.kv.impl.security.metadata.SecurityMDChange.SecurityMDChangeType;
import oracle.kv.impl.security.metadata.SecurityMetadata.SecurityElementType;

/**
 * This is a simple class that tracks SecurityMDListener objects, filter and
 * get user role changes from security metadata changes for notification.
 *
 * This updater is a proxy used to notify changes for listeners registered in
 * SecurityMetadataManager.
 */
public class UserRoleUpdater {

    private final Set<SecurityMDListener> listeners;

    public UserRoleUpdater() {
        listeners =
            Collections.synchronizedSet(new HashSet<SecurityMDListener>());
    }

    public void addListener(SecurityMDListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SecurityMDListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(SecurityMetadata oldMd,
                                List<SecurityMDChange> mdChanges) {
        /*
         * oldMd can be null if updates are made on a newly created security
         * metadata, which is an empty object.
         */
        if (oldMd == null) {
            return;
        }

        /*
         * To avoid holding locks while listener implementation is performing
         * long running task, make copy of listeners, iterate and notify copies
         * about changes.
         */
        final List<SecurityMDListener> listenerCopy;
        synchronized(listeners) {
            listenerCopy = new ArrayList<SecurityMDListener>(listeners);
        }

        for (SecurityMDListener listener : listenerCopy) {
            for (SecurityMDChange change :
                 getRoleChanges(oldMd, mdChanges)) {
                listener.notifyMetadataChange(change);
            }
        }
    }

    /**
     * Iterate given security metadata change list to retrieve user role
     * changes only to notify registered listeners.
     */
    private List<SecurityMDChange>
        getRoleChanges(SecurityMetadata oldMd,
                       List<SecurityMDChange> mdChanges) {

        final List<SecurityMDChange> changes =
            new ArrayList<SecurityMDChange>();
        for (final SecurityMDChange change : mdChanges) {
            final SecurityMDChangeType changeType = change.getChangeType();
            final SecurityElementType elementType = change.getElementType();

            if (changeType == SecurityMDChangeType.UPDATE &&
                elementType == SecurityElementType.KVSTOREUSER) {
                final KVStoreUser newUser = (KVStoreUser)change.getElement();
                final KVStoreUser oldUser = oldMd.getUser(newUser.getName());

                if (!newUser.getGrantedRoles().equals(
                    oldUser.getGrantedRoles())) {
                    changes.add(change);
                }
            } else if (elementType == SecurityElementType.KVSTOREROLE){
                changes.add(change);
            }
        }
        return changes;
    }
}
