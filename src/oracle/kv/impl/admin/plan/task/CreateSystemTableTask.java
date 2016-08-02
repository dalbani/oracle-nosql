/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
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

import java.util.List;

import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.plan.AbstractPlan;
import oracle.kv.impl.api.table.FieldMap;
import oracle.kv.impl.api.table.TableBuilder;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.util.SecurityUtils;

/**
 * A task for adding a system table into KVStore. The system table should be
 * added at the end of deploying the KVStore.
 */
public class CreateSystemTableTask extends SingleJobTask {

    private static final long serialVersionUID = 1L;

    private AbstractPlan plan;

    private final String tableName;
    private final String parentName;
    private final List<String> primaryKey;
    private final List<Integer> primaryKeySizes;

    /*
     * The major key is the shard key. Since this is a DPL object, the
     * field cannot be renamed without upgrade issues, so we will
     * maintain the name.
     */
    private final List<String> majorKey;
    private final FieldMap fieldMap;
    private final boolean r2compat;
    private final int schemaId;
    private final String description;

    public CreateSystemTableTask(AbstractPlan plan, TableBuilder tableBuilder) {
        this.plan = plan;
        this.tableName = tableBuilder.getName();
        this.parentName =
                tableBuilder.getParent() == null? null:
                    tableBuilder.getParent().getFullName();
        this.primaryKey = tableBuilder.getPrimaryKey();
        this.primaryKeySizes = tableBuilder.getPrimaryKeySizes();

        /* Note that the major key is the shard key */
        this.majorKey = tableBuilder.getShardKey();

        this.fieldMap = tableBuilder.getFieldMap();
        this.r2compat = tableBuilder.isR2compatible();
        this.schemaId = tableBuilder.getSchemaId();
        this.description = tableBuilder.getDescription();

        final TableMetadata md =
                plan.getAdmin().getMetadata(TableMetadata.class,
                                            MetadataType.TABLE);
        if ((md != null) && md.tableExists(tableName, parentName)) {
            throw new IllegalCommandException
            ("Table already exists: " +
             TableMetadata.makeQualifiedName(tableName, parentName));
        }
    }

    @Override
    public State doWork()
        throws Exception {
        TableMetadata md =
                plan.getAdmin().getMetadata(TableMetadata.class,
                                            MetadataType.TABLE);
        if (md == null) {
            md = new TableMetadata(true);
        }

        /*
         * Don't add the table if it exists. Still do the broadcast, just in
         * case this is a re-execute.
         */
        if (!md.tableExists(tableName, parentName)) {
            // TODO the add table method does not check for dup
            md.addSysTable(tableName,
                           parentName,
                           primaryKey,
                           primaryKeySizes,
                           majorKey,
                           fieldMap,
                           null, /* TTL */
                           r2compat,
                           schemaId,
                           description,
                           SecurityUtils.currentUserAsOwner());
            plan.getAdmin().saveMetadata(md, plan);
        }

        final Admin admin = plan.getAdmin();

        if (!Utils.broadcastMetadataChangesToRNs(plan.getLogger(),
                                                 md,
                                                 admin.getCurrentTopology(),
                                                 toString(),
                                                 admin.getParams()
                                                 .getAdminParams(),
                                                 plan)) {
            return State.INTERRUPTED;
        }

        return State.SUCCEEDED;
    }

    @Override
    public boolean continuePastError() {
        return false;
    }

    @Override
    public String toString() {
       return super.toString() + " create table " + tableName;
    }
}
