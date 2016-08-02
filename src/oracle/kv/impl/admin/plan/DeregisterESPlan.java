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

package oracle.kv.impl.admin.plan;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.param.StorageNodeParams;
import oracle.kv.impl.admin.plan.task.UpdateESConnectionInfo;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;

/**
 * A plan for informing the store of the existence of an Elasticsearch node.
  */
public class DeregisterESPlan extends AbstractPlan {

    private static final long serialVersionUID = 1L;

    public DeregisterESPlan(AtomicInteger idGen,
                            String name,
                            Planner planner) {

        super(idGen, name, planner);

        final Admin admin = getAdmin();
        final Parameters p = admin.getCurrentParameters();
        
        verifyNoTextIndexes(admin);
        
        /*
         *  update the ES connection info list on each SNA.
         */
        int taskCount = 0;
        for (StorageNodeParams snaParams : p.getStorageNodeParams()) {
            if (! ("" == snaParams.getSearchClusterName())) {
                taskCount++;
                addTask(new UpdateESConnectionInfo
                        (this, snaParams.getStorageNodeId(), "", ""));
            }
        }
        if (taskCount == 0) {
            throw new IllegalCommandException
                ("No ES cluster is currently registered with the store.");
        }
    }

    private void verifyNoTextIndexes(Admin admin) {
        final TableMetadata tableMd = admin.getMetadata(TableMetadata.class,
                                                        MetadataType.TABLE);
        if (tableMd == null) {
            return;
        }

        final Set<String> indexnames = tableMd.getTextIndexNames();
        if (indexnames.isEmpty()) {
            return;
        }

        final StringBuilder sb = new StringBuilder
            ("Cannot deregister ES because these text indexes exist:");
        String eol = System.getProperty("line.separator");
        for (String s : indexnames) {
            sb.append(eol);
            sb.append(s);
        }

        throw new IllegalCommandException(sb.toString());
    }

    @Override
    public void preExecutionSave() {
    }

    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    public String getDefaultName() {
        return "Deregister Elasticsearch cluster";
    }

    @Override
	public void stripForDisplay() {
    }

    @Override
    public List<? extends KVStorePrivilege> getRequiredPrivileges() {
        /* Requires SYSOPER */
        return SystemPrivilege.sysoperPrivList;
    }
}
