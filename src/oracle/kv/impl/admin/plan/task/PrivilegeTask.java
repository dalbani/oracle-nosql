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

import java.util.HashSet;
import java.util.Set;

import oracle.kv.UnauthorizedException;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.plan.SecurityMetadataPlan.PrivilegePlan;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.fault.ClientAccessException;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.AccessCheckUtils;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.KVStorePrivilegeLabel;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.security.metadata.SecurityMetadata;

/**
 * The super class of privilege granting or revocation task.
 */
public class PrivilegeTask extends UpdateMetadata<SecurityMetadata> {

    private static final long serialVersionUID = 1L;

    private static final String ALLPRIVS = "ALL";

    final String roleName;
    final String tableName;

    final Set<KVStorePrivilege> privileges = new HashSet<KVStorePrivilege>();

    public PrivilegeTask(PrivilegePlan plan,
                         String roleName,
                         String tableName,
                         Set<String> privNames) {
        super(plan);

        final SecurityMetadata secMd = plan.getMetadata();
        this.roleName = roleName;
        this.tableName = tableName;

        if ((secMd == null) || (secMd.getRole(roleName) == null)) {
            throw new IllegalCommandException(
                "Role with name " + roleName + " does not exist in store");
        }

        if (secMd.getRole(roleName).readonly()) {
            throw new IllegalCommandException(
                "Cannot grant or revoke privileges to or from a read-only " +
                "role: " + roleName);
        }

        parseToPrivileges(privNames);
    }

    /**
     * Parse and validate string of privilege name to KVStorePrivilege.
     */
    void parseToPrivileges(Set<String> privNames) {
        /* Case of operation for system privileges */
        if (tableName == null) {
            for (String privName : privNames) {
                if (ALLPRIVS.equalsIgnoreCase(privName)) {
                    privileges.addAll(SystemPrivilege.getAllSystemPrivileges());
                    return;
                }
                privileges.add(SystemPrivilege.get(
                    KVStorePrivilegeLabel.valueOf(privName.toUpperCase())));
            }
            return;
        }

        /* Case of operation for table privileges */
        final TableMetadata tableMd =
            plan.getAdmin().getMetadata(TableMetadata.class,
                                        MetadataType.TABLE);
        if (tableMd == null || tableMd.getTable(tableName) == null) {
            throw new IllegalCommandException(
                "Table with name " + tableName + " does not exist");
        }

        final TableImpl table = tableMd.getTable(tableName);
        checkPermission(table);

        for (String privName : privNames) {
            if (ALLPRIVS.equalsIgnoreCase(privName)) {
                privileges.addAll(
                    TablePrivilege.getAllTablePrivileges(table.getId(),
                                                         table.getFullName()));
                return;
            }
            final KVStorePrivilegeLabel privLabel =
                KVStorePrivilegeLabel.valueOf(privName.toUpperCase());
            privileges.add(TablePrivilege.get(privLabel, table.getId(),
                                              table.getFullName()));
        }
    }

    /**
     * Check if current user has enough permission to operation given table
     * privilege granting and revocation.
     */
    private void checkPermission(TableImpl table) {
        final ExecutionContext execCtx = ExecutionContext.getCurrent();
        if (execCtx == null) {
            return;
        }
        if (!AccessCheckUtils.currentUserOwnsResource(table) &&
            !execCtx.hasPrivilege(SystemPrivilege.SYSOPER)) {
               throw new ClientAccessException(
                   new UnauthorizedException(
                       "Insufficient privilege granted to grant or revoke " +
                       "privilege on non-owned tables."));
        }
    }
}
