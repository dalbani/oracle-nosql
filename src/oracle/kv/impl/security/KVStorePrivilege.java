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
package oracle.kv.impl.security;

import java.io.Serializable;

/**
 * Abstract implementation of privileges within KVStore security system. For
 * each KVStore privilege label (see {@link KVStorePrivilegeLabel}), we can
 * create an instance describing its detailed information, which includes the
 * implied privileges, specific resource subject to access control and so
 * forth.
 */
public abstract class KVStorePrivilege implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Three categories of privileges in KVStore: system privilege, object
     * privilege and table privilege. System privileges apply to all operations
     * of a certain sort, while object privileges are limited to particular
     * objects. Especially, table privileges are applicable for tables only.
     * <p>
     * From R3.2, the OBJECT type becomes obsolete, and will be replace by more
     * specific object types like TABLE.
     */
    public static enum PrivilegeType { SYSTEM, OBJECT, TABLE }

    /* The privilege label associated with this privilege instance. */
    private final KVStorePrivilegeLabel privLabel;

    /*
     * TODO:
     * Keep associated resource information for object privileges in future.
     */

    /**
     * Constructs a privilege instance using the specified label.
     *
     * @param priviLabel KVStore privilege label
     */
    KVStorePrivilege(KVStorePrivilegeLabel privLabel) {
        this.privLabel = privLabel;
    }

    /**
     * Checks for equality.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return privLabel.equals(
            ((KVStorePrivilege) other).privLabel);
    }

    /**
     * Gets the hashCode value for the object
     */
    @Override
    public int hashCode() {
        return privLabel.hashCode();
    }

    /**
     * Gets a string representation of this privilege
     */
    @Override
    public String toString() {
        return privLabel.toString();
    }

    /**
     * Returns the label associated with this privilege
     */
    public KVStorePrivilegeLabel getLabel() {
        return privLabel;
    }

    /**
     * Returns the type of this privilege
     */
    public PrivilegeType getType() {
        return privLabel.getType();
    }

    /**
     * Returns the privileges "implying" this privilege.  This must be
     * implemented by subclasses of privilege, as they are the only ones that
     * can impose semantics on a privilege object.
     *
     * @return an array of privileges implying this privilege. If no privilege
     * implies this one, an empty array will be returned.
     */
    public abstract KVStorePrivilege[] implyingPrivileges();
}
