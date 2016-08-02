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

package oracle.kv.impl.admin.plan;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.plan.task.RemoveUser;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.ResourceOwner;
import oracle.kv.impl.security.metadata.KVStoreUser;
import oracle.kv.impl.security.metadata.SecurityMetadata;
import oracle.kv.table.Table;

/**
 * A new plan for removing users supporting remove their data as well.  For
 * now, if the user being dropped owns any table, an ICE will be thrown to tell
 * users they should drop all the owned tables before dropping the user. In
 * future, an option of CASCADE will be provided to help drop all the owned
 * tables automatically.
 */
public class RemoveUserPlan extends SecurityMetadataPlan {
    private static final long serialVersionUID = 1L;
    private final String userName;

    public RemoveUserPlan(AtomicInteger idGen,
                          String planName,
                          Planner planner,
                          String userName,
                          boolean cascade) {
        super(idGen, planName, planner);

        final SecurityMetadata secMd = getMetadata();
        this.userName = userName;

        if (secMd != null && secMd.getUser(userName) != null) {
            if (cascade) {
                throw new IllegalCommandException(
                    "The CASCADE option is not yet supported in this version");
            }
            ensureNotOwnsTable();

            /* Remove user */
            addTask(new RemoveUser(this, userName));
        }
    }

    @Override
    public void preExecuteCheck(boolean force, Logger plannerlogger) {
        super.preExecuteCheck(force, plannerlogger);
        /*
         * Need to check if any owned table before each execution of this plan,
         * since the table metadata may have been changed before execution.
         */
        ensureNotOwnsTable();
    }

    private void ensureNotOwnsTable() {
        final Set<String> ownedTables = new HashSet<String>();
        getOwnedTables(ownedTables);
        if (!ownedTables.isEmpty()) {
            throw new IllegalCommandException(
                "Cannot drop a user that owns tables: " + ownedTables +
                ". Please retry after dropping all these tables");
        }
    }

    private void getOwnedTables(Set<String> tables) {
        final TableMetadata tableMd =
            planner.getAdmin().getMetadata(TableMetadata.class,
                                           MetadataType.TABLE);
        if (tableMd == null || tableMd.getTables().isEmpty()) {
            return;
        }

        final SecurityMetadata secMd = getMetadata();
        if (secMd == null || secMd.getUser(userName) == null) {
            return;
        }

        final KVStoreUser user = secMd.getUser(userName);
        final ResourceOwner owner =
            new ResourceOwner(user.getElementId(), user.getName());

        for (Table table : tableMd.getTables().values()) {
            addTablesToSet(owner, (TableImpl) table, tables);
        }
    }

    private void addTablesToSet(ResourceOwner owner,
                                TableImpl table,
                                Set<String> tables) {
        if (owner.equals(table.getOwner())) {
            tables.add(table.getFullName());
        }
        for (Table table1 : table.getChildTables().values()) {
            addTablesToSet(owner, (TableImpl) table1, tables);
        }
    }

}
