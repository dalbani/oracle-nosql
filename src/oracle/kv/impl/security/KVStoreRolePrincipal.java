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

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

/**
 * Represents a "Role" associated with user.  This exists so that the roles
 * can be attached to subjects as a "Principal".
 */
public final class KVStoreRolePrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Reserved old role field to maintain compatibility during upgrade, only
     * accept the value of KVStoreRoles.
     */
    private final KVStoreRole role;

    /**
     * The name of KVStore role assigned to an identity.
     */
    private String roleName;

    /**
     * The role associated with the public users.
     */
    public static final KVStoreRolePrincipal PUBLIC =
        new KVStoreRolePrincipal(RoleInstance.PUBLIC_NAME);

    /**
     * The role associated with the database administrators.
     */
    public static final KVStoreRolePrincipal DBADMIN =
        new KVStoreRolePrincipal(RoleInstance.DBADMIN_NAME);

    /**
     * The role associated with the store administrators.
     */
    public static final KVStoreRolePrincipal SYSADMIN =
        new KVStoreRolePrincipal(RoleInstance.SYSADMIN_NAME);

    /**
     * The role associated with the read-only users.
     */
    public static final KVStoreRolePrincipal READONLY =
        new KVStoreRolePrincipal(RoleInstance.READONLY_NAME);

    /**
     * The role associated with the write-only users.
     */
    public static final KVStoreRolePrincipal WRITEONLY =
        new KVStoreRolePrincipal(RoleInstance.WRITEONLY_NAME);

    /**
     * The role associated with the read-write users.
     */
    public static final KVStoreRolePrincipal READWRITE =
        new KVStoreRolePrincipal(RoleInstance.READWRITE_NAME);

    /**
     * The role associated with the server itself.
     */
    public static final KVStoreRolePrincipal INTERNAL =
        new KVStoreRolePrincipal(KVStoreRole.INTERNAL);

    /*
     * Reserved old R3 KVStore role principles
     */

    /**
     * The R3 role associated with admin users.
     */
    public static final KVStoreRolePrincipal ADMIN =
        new KVStoreRolePrincipal(KVStoreRole.ADMIN);

    /**
     * The R3 role associated with logged-in users (or internal).
     */
    public static final KVStoreRolePrincipal AUTHENTICATED =
        new KVStoreRolePrincipal(KVStoreRole.AUTHENTICATED);

    /**
     * A map to allow mapping from role name to the corresponding canonical
     * KVStoreRolePrincipal.
     */
    private static final Map<String, KVStoreRolePrincipal> roleMap =
        new HashMap<String, KVStoreRolePrincipal>();

    static {
        roleMap.put(INTERNAL.getName(), INTERNAL);
        roleMap.put(PUBLIC.getName(), PUBLIC);
        roleMap.put(DBADMIN.getName(), DBADMIN);
        roleMap.put(SYSADMIN.getName(), SYSADMIN);
        roleMap.put(READONLY.getName(), READONLY);
        roleMap.put(WRITEONLY.getName(), WRITEONLY);
        roleMap.put(READWRITE.getName(), READWRITE);
        roleMap.put(ADMIN.getName(), ADMIN);
        roleMap.put(AUTHENTICATED.getName(), AUTHENTICATED);
    }

    /**
     * Find the KVStoreRolePrincipal instance corresponding to a role name.
     * The role name will first be resolved as a built-in role and then, if
     * fails, as a user-defined role
     */
    public static KVStoreRolePrincipal get(final String roleName) {
        final KVStoreRolePrincipal rolePrinc =
            roleMap.get(RoleInstance.getNormalizedName(roleName));
        return rolePrinc == null ?
               new KVStoreRolePrincipal(roleName) :
               rolePrinc;
    }

    /**
     * Return an array of names of all KVStoreRoles associated with a Subject.
     * @param subj the subject to consider
     * @return a newly allocated array containing the names assigned roles
     */
    public static String[] getSubjectRoleNames(Subject subj) {
        return ExecutionContext.getSubjectRoles(subj);
    }

    /*
     * Principal interface methods
     */

    /**
     * Check for equality.
     * @see Object#equals
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return roleName.equals(((KVStoreRolePrincipal) other).roleName);
    }

    /**
     * Get the principal name.
     * @see Principal#getName
     */
    @Override
    public String getName() {
        return roleName;
    }

    /**
     * Get the hashCode value for the object.
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return roleName.hashCode();
    }

    /**
     * Get a string representation of this object.
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "KVStoreRolePrincipal(" + roleName + ")";
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        /* Used to read the object created in R3 code */
        if (role != null) {
            if (roleName == null) {

                /*
                 * Role name normalization is performed by role resolver.
                 * TODO: Move all normalization here in future.
                 */
                roleName = role.toString();
            } else if (!role.toString().equalsIgnoreCase(roleName)) {
                throw new IOException(
                    String.format("Role %s and role name %s are mismatched",
                                  role, roleName));
            }
        } else {
            if (roleName == null) {
                throw new IOException(
                    "Unexpected error when read KVStoreRolePrinciple object," +
                    "role and roleName are both null");
            }
        }
    }

    /**
     * Replace R3 role principals with a R3-compatible object before the
     * serialization.
     */
    private Object writeReplace() {

        if (this.role != null) {
            return new KVStoreRolePrincipal(this.role, this.role.toString());
        }
        return this;
    }

    /*
     * non-interface methods
     */

    /**
     * Constructor used for old R3 role principals with normalized role name.
     */
    private KVStoreRolePrincipal(KVStoreRole role) {
        this.role = role;
        this.roleName = RoleInstance.getNormalizedName(role.toString());
    }

    private KVStoreRolePrincipal(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name should not be null");
        }
        this.role = null;
        this.roleName = RoleInstance.getNormalizedName(roleName);
    }

    /**
     * Creating a R3 role principal with R3 role name for backward
     * compatibility.
     */
    private KVStoreRolePrincipal(KVStoreRole role, String roleName) {
        this.role = role;
        this.roleName = roleName;
    }
}
