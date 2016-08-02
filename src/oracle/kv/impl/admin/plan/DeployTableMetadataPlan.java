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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import oracle.kv.KVVersion;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.PlanLocksHeldException;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.ResourceOwner;
import oracle.kv.impl.security.RoleInstance;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.security.metadata.SecurityMetadata;
import oracle.kv.impl.security.util.SecurityUtils;

import com.sleepycat.persist.model.Persistent;

/**
 * A general purpose plan for executing changes to table metadata. By default
 * this plan will lock out other table metadata plans and elasticity plans.
 */
@Persistent
public class DeployTableMetadataPlan extends MetadataPlan<TableMetadata> {
    private static final long serialVersionUID = 1L;

    /** The first version that supports table operations. */
    private static final KVVersion TABLE_SUPPORT_VERSION =
        KVVersion.R3_0; /* R3.0 Q1/2014 */

    /** The first version that supports table authorization. */
    private final static KVVersion TABLE_AUTH_VERSION =
        KVVersion.R3_3; /* R3.3 Q1/2015 */

    DeployTableMetadataPlan(AtomicInteger idGen,
                            String planName,
                            Planner planner) {
        super(idGen, planName, planner);
        /* Ensure all nodes in the store support table operations */
        checkVersion(planner.getAdmin(), TABLE_SUPPORT_VERSION,
                     "Cannot perform plan " + planName + " when not all" +
                     " nodes in the store support table feature.");
    }

    /*
     * No-arg ctor for use by DPL.
     */
    DeployTableMetadataPlan() {
    }

    @Override
    protected Class<TableMetadata> getMetadataClass() {
        return TableMetadata.class;
    }

    @Override
    public MetadataType getMetadataType() {
        return MetadataType.TABLE;
    }

    @Override
    public void preExecutionSave() {
        /* Nothing to save before execution. */
    }

    @Override
    public String getDefaultName() {
        return "Deploy Table Metadata";
    }

    @Override
    public boolean isExclusive() {
      return false;
    }

    @Override
    public void getCatalogLocks() throws PlanLocksHeldException {
        /*
         * Several table metadata plans can't take place at the same time as
         * a partition migration.
         */
        planner.lockElasticity(getId(), getName());
        getPerTaskLocks();
    }

    @Override
    void stripForDisplay() {

    }

    /**
     * Returns the real table name for the table name, making it insensitive to
     * case.  If the table does not exist, return the argument and allow the
     * caller to continue.  This is called for plans where the table may or may
     * not exist.
     */
    String getRealTableName(String tableName) {
        final TableMetadata md = getMetadata();
        if (md != null) {
            TableImpl table = md.getTable(tableName, false);
            if (table != null) {
                return table.getFullName();
            }
        }
        return tableName;
    }

    TableImpl getAndCheckTable(String tableName) {
        final TableMetadata md = getMetadata();
        if (md != null) {
            final TableImpl table = md.getTable(tableName, false);
            if (table != null) {
                return table;
            }
        }
        throw new IllegalCommandException(
            "Table does not exist: " + tableName);
    }

    @Override
    public List<? extends KVStorePrivilege> getRequiredPrivileges() {
        /* Requires SYSDBA for table operations */
        return SystemPrivilege.sysdbaPrivList;
    }

    /*
     * A series of new version table plans since R3.3 with table authorization,
     * since they are set with finer-grain table-level privileges now rather
     * than the blanket SYSDBA privilege.
     */

    /**
     * AddTablePlan requires CREATE_ANY_TABLE privilege.
     */
    static class AddTablePlan extends DeployTableMetadataPlan {
        private static final long serialVersionUID = 1L;

        AddTablePlan(AtomicInteger idGen, String planName, Planner planner) {
            super(idGen, planName, planner);
            checkVersion(planner.getAdmin(), TABLE_AUTH_VERSION,
                         "Cannot add new tables until all nodes in the store" +
                         " have been upgraded to " + TABLE_AUTH_VERSION);
        }
        @Override
        public List<? extends KVStorePrivilege> getRequiredPrivileges() {
            return SystemPrivilege.tableCreatePrivList;
        }
    }

    /**
     * RemoveTablePlan requires DROP_ANY_TABLE privilege.
     */
    static class RemoveTablePlan extends DeployTableMetadataPlan {
        private static final long serialVersionUID = 1L;
        private final ResourceOwner tableOwner;
        private final long tableId;
        private final String tableName;
        private final boolean toRemoveIndex;

        RemoveTablePlan(AtomicInteger idGen, 
                        String planName,
                        String tableName,
                        boolean removeData, 
                        Planner planner) {
            super(idGen, planName, planner);

            final TableImpl table = getAndCheckTable(tableName);
            tableOwner = table.getOwner();
            tableId = table.getId();
            toRemoveIndex = removeData && !table.getIndexes().isEmpty();
            this.tableName = tableName;
        }

        @Override
        public void preExecuteCheck(boolean force, Logger plannerlogger) {
            super.preExecuteCheck(force, plannerlogger);

            /*
             * Checks before each execution to ensure no new privilege defined
             * on this table has been granted, before the drop operation ends
             * successfully.
             */
            checkPrivilegesOnTable();
        }

        @Override
        public List<? extends KVStorePrivilege> getRequiredPrivileges() {
            final ResourceOwner currentUser =
                    SecurityUtils.currentUserAsOwner();
            if ((currentUser != null) && (currentUser.equals(tableOwner))) {
                /* The owner, checks only USRVIEW to ensure authentication */
                return SystemPrivilege.usrviewPrivList;
            }
            final List<KVStorePrivilege> privsToCheck =
                new ArrayList<KVStorePrivilege>();
            if (toRemoveIndex) {
                privsToCheck.add(new TablePrivilege.DropIndex(tableId));
            }
            privsToCheck.add(SystemPrivilege.DROP_ANY_TABLE);
            return privsToCheck;
        }

        /**
         * Checks whether any privileges defined on the dropping table exists
         * in any role. If yes, the drop will fail. This check prevents any
         * privilege defined on a removed table seen by users.
         * <p>
         * TODO: remove the related table privileges automatically when the
         * table is dropped
         */
        void checkPrivilegesOnTable() {
            final SecurityMetadata secMd = 
                planner.getAdmin().getMetadata(SecurityMetadata.class,
                                               MetadataType.SECURITY);
            if (secMd == null) {
                return;
            }
            final Set<String> involvedRoles = new HashSet<String>();
            for (RoleInstance role : secMd.getAllRoles()) {
                for (KVStorePrivilege priv : role.getPrivileges()) {
                    if ((priv instanceof TablePrivilege) &&
                        ((TablePrivilege) priv).getTableId() == tableId) {
                        involvedRoles.add(role.name());
                    }
                }
            }
            if (!involvedRoles.isEmpty()) {
                throw new IllegalCommandException(
                    "Cannot drop table " + tableName + " since there are " +
                    "privileges defined on it in roles " + involvedRoles +
                    ". Please revoke the privileges explicitly and then try" +
                    " dropping the table again.");
            }
        }
    }

    /**
     * EvolveTablePlan requires EVOLVE_TABLE(tableId) privilege.
     */
    static class EvolveTablePlan extends DeployTableMetadataPlan {
        private static final long serialVersionUID = 1L;
        private final ResourceOwner tableOwner;
        private final long tableId;

        EvolveTablePlan(AtomicInteger idGen,
                        String planName,
                        String tableName,
                        Planner planner) {
            super(idGen, planName, planner);

            final TableImpl table = getAndCheckTable(tableName);
            this.tableOwner = table.getOwner();
            this.tableId = table.getId();
        }

        @Override
        public List<? extends KVStorePrivilege> getRequiredPrivileges() {
            final ResourceOwner currentUser =
                SecurityUtils.currentUserAsOwner();
            if ((currentUser != null) && (currentUser.equals(tableOwner))) {
                return SystemPrivilege.usrviewPrivList;
            }
            return Collections.singletonList(
                new TablePrivilege.EvolveTable(tableId));
        }
    }

    /**
     * AddIndexPlan requires CREATE_INDEX(tableId) privilege.
     */
    static class AddIndexPlan extends DeployTableMetadataPlan {
        private static final long serialVersionUID = 1L;
        private final ResourceOwner tableOwner;
        private final long tableId;

        AddIndexPlan(AtomicInteger idGen,
                     String planName,
                     String tableName,
                     Planner planner) {
            super(idGen, planName, planner);

            final TableImpl table = getAndCheckTable(tableName);
            this.tableOwner = table.getOwner();
            this.tableId = table.getId();
        }

        @Override
        public List<? extends KVStorePrivilege> getRequiredPrivileges() {
            final ResourceOwner currentUser =
                SecurityUtils.currentUserAsOwner();
            if ((currentUser != null) && (currentUser.equals(tableOwner))) {
                return SystemPrivilege.usrviewPrivList;
            }
            return Collections.singletonList(
                new TablePrivilege.CreateIndex(tableId));
        }
    }

    /**
     * RemoveAddIndexPlan requires DROP_INDEX(tableId) privilege.
     */
    static class RemoveIndexPlan extends DeployTableMetadataPlan {
        private static final long serialVersionUID = 1L;
        private final ResourceOwner tableOwner;
        private final long tableId;

        RemoveIndexPlan(AtomicInteger idGen,
                        String planName,
                        String tableName,
                        Planner planner) {
            super(idGen, planName, planner);

            final TableImpl table = getAndCheckTable(tableName);
            this.tableOwner = table.getOwner();
            this.tableId = table.getId();
        }

        @Override
        public List<? extends KVStorePrivilege> getRequiredPrivileges() {
            final ResourceOwner currentUser =
                SecurityUtils.currentUserAsOwner();
            if ((currentUser != null) && (currentUser.equals(tableOwner))) {
                return SystemPrivilege.usrviewPrivList;
            }
            return Collections.singletonList(
                new TablePrivilege.DropIndex(tableId));
        }
    }
}
