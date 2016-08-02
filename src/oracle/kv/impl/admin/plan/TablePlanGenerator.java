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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.kv.KVVersion;
import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.plan.DeployTableMetadataPlan.AddIndexPlan;
import oracle.kv.impl.admin.plan.DeployTableMetadataPlan.AddTablePlan;
import oracle.kv.impl.admin.plan.DeployTableMetadataPlan.EvolveTablePlan;
import oracle.kv.impl.admin.plan.DeployTableMetadataPlan.RemoveIndexPlan;
import oracle.kv.impl.admin.plan.DeployTableMetadataPlan.RemoveTablePlan;
import oracle.kv.impl.admin.plan.task.AddTable;
import oracle.kv.impl.admin.plan.task.CompleteAddIndex;
import oracle.kv.impl.admin.plan.task.EvolveTable;
import oracle.kv.impl.admin.plan.task.ParallelBundle;
import oracle.kv.impl.admin.plan.task.RemoveIndex;
import oracle.kv.impl.admin.plan.task.RemoveTable;
import oracle.kv.impl.admin.plan.task.StartAddIndex;
import oracle.kv.impl.admin.plan.task.StartAddTextIndex;
import oracle.kv.impl.admin.plan.task.UpdateMetadata;
import oracle.kv.impl.admin.plan.task.Utils;
import oracle.kv.impl.admin.plan.task.WaitForAddIndex;
import oracle.kv.impl.admin.plan.task.WaitForRemoveTableData;
import oracle.kv.impl.api.table.FieldMap;
import oracle.kv.impl.api.table.IndexImpl.AnnotatedField;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.param.ParameterMap;
import oracle.kv.impl.param.ParameterState;
import oracle.kv.impl.tif.ElasticsearchHandler;
import oracle.kv.impl.tif.TextIndexFeeder;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.table.Index;
import oracle.kv.table.TimeToLive;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.node.Node;

/**
 * Static utility class for generating plans for secondary indexes.
 *
 * Exception handling note.  This code runs in the context of the admin service
 * and the rule in the admin is that non-fatal runtime exceptions are thrown as
 * IllegalCommandException.  For that reason these methods catch and rethrow
 * exceptions from called methods.
 */
public class TablePlanGenerator {

    private final static KVVersion TABLE_AUTH_VERSION = KVVersion.R3_3;

    /* Prevent construction */
    private TablePlanGenerator() {}

    /**
     * Creates a plan to add a table.
     */
    static DeployTableMetadataPlan
        createAddTablePlan(AtomicInteger idGen,
                           String planName,
                           Planner planner,
                           TableImpl table,
                           String parentName) {

        String tableName = table.getName();

        final String fullPlanName = makeName(planName, tableName, null);
        final DeployTableMetadataPlan plan =
            new AddTablePlan(idGen, fullPlanName, planner);

        tableName = plan.getRealTableName(tableName);
        try {
            plan.addTask(new AddTable(plan,
                                      table,
                                      parentName));
        } catch (IllegalArgumentException iae) {
            throw new IllegalCommandException
                ("Failed to add table: " + iae.getMessage(), iae);
        }
        return plan;
    }

    /**
     * Creates a plan to evolve a table.
     *
     * The table version is the version of the table used as a basis for this
     * evolution.  It is used to verify that only the latest version of a table
     * is evolved.
     */
    static DeployTableMetadataPlan
        createEvolveTablePlan(AtomicInteger idGen,
                              String planName,
                              Planner planner,
                              String tableName,
                              int tableVersion,
                              FieldMap fieldMap,
                              TimeToLive ttl) {
        checkTable(tableName);
        if (fieldMap == null || fieldMap.isEmpty()) {
            throw new IllegalCommandException("Fields cannot be null or empty");
        }

        final String fullPlanName = makeName(planName, tableName, null);
        final DeployTableMetadataPlan plan;
        if (Utils.storeHasVersion(planner.getAdmin(), TABLE_AUTH_VERSION)) {
            plan = new EvolveTablePlan(idGen, fullPlanName, tableName, planner);
        } else {
            plan = new DeployTableMetadataPlan(idGen, fullPlanName, planner);
        }

        tableName = plan.getRealTableName(tableName);
        try {
            plan.addTask(new EvolveTable(plan,
                                         tableName,
                                         tableVersion,
                                         fieldMap,
                                         ttl));
        } catch (IllegalArgumentException iae) {
            throw new IllegalCommandException
                ("Failed to evolve table: " + iae.getMessage(), iae);
        }

        return plan;
    }

    /**
     * Creates a plan to remove a table.
     */
    static DeployTableMetadataPlan createRemoveTablePlan(AtomicInteger idGen,
                                                         String planName,
                                                         Planner planner,
                                                         Topology topology,
                                                         String tableName,
                                                         boolean removeData) {
        checkTable(tableName);

        final String fullPlanName = makeName(planName, tableName, null);
        final DeployTableMetadataPlan plan;
        if (Utils.storeHasVersion(planner.getAdmin(), TABLE_AUTH_VERSION)) {
            plan = new RemoveTablePlan(idGen, fullPlanName, tableName,
                                       removeData, planner);
        } else {
            plan = new DeployTableMetadataPlan(idGen, fullPlanName, planner);
        }
        tableName = plan.getRealTableName(tableName);

        /*
         * If we need to remove data, we first mark the table for deletion and
         * broadcast that change. This will trigger the RNs to remove the
         * table data from it's respective shard. The plan will wait for all
         * RNs to finish. Once the data is deleted, the table object can be
         * removed.
         */
        try {
            addRemoveIndexTasks(plan, tableName);
            if (removeData) {
                plan.addTask(new RemoveTable(plan, tableName, true));

                final ParallelBundle bundle = new ParallelBundle();
                for (RepGroupId id : topology.getRepGroupIds()) {
                    bundle.addTask(new WaitForRemoveTableData(plan,
                                                              id,
                                                              tableName));
                }
                plan.addTask(bundle);
            }
            plan.addTask(new RemoveTable(plan, tableName, false));
        } catch (IllegalArgumentException iae) {
            throw new IllegalCommandException
                ("Failed to remove table: " + iae.getMessage(), iae);
        }

        return plan;
    }

    /**
     * Add a task to remove each index defined on the table.  Do this before
     * removing data as indexes are affected and performance would be quite bad
     * otherwise.
     */
    static private void addRemoveIndexTasks(final DeployTableMetadataPlan plan,
                                            final String tableName) {
        final TableMetadata md = plan.getMetadata();
        if (md != null) {
            TableImpl table = md.checkForRemove(tableName, true);
            for (String indexName : table.getIndexes().keySet()) {
                try {
                    plan.addTask(new RemoveIndex(plan, indexName, tableName));
                } catch (IllegalArgumentException iae) {
                    throw new IllegalCommandException
                        ("Failed to remove index: " + iae.getMessage(), iae);
                }
            }
        }
    }

    /**
     * Creates a plan to add an index.
     * This operates in 3 parts
     * 1.  Update metadata to include the new index, which is in state
     *     "Populating". In that state it will be populated and used on
     *      RepNodes but will not appear to users in metadata.
     * 2.  Ask all RepNode masters to populate the index in a parallel bundle
     * 3.  Update the metadata again with the state "Ready" on the index to make
     *     it visible to users.
     */
    static DeployTableMetadataPlan createAddIndexPlan(AtomicInteger idGen,
                                                      String planName,
                                                      Planner planner,
                                                      Topology topology,
                                                      String indexName,
                                                      String tableName,
                                                      String[] indexedFields,
                                                      String description) {
        checkTable(tableName);
        checkIndex(indexName);
        if (indexedFields == null) {    // TODO - check for empty?
            throw new IllegalCommandException("Indexed fields cannot be null");
        }

        final String fullPlanName = makeName(planName, tableName, indexName);
        final DeployTableMetadataPlan plan;
        if (Utils.storeHasVersion(planner.getAdmin(), TABLE_AUTH_VERSION)) {
            plan = new AddIndexPlan(idGen, fullPlanName, tableName, planner);
        } else {
            plan = new DeployTableMetadataPlan(idGen, fullPlanName, planner);
        }
        tableName = plan.getRealTableName(tableName);

        /*
         * Create the index, not-yet-visible
         */
        try {
            plan.addTask(new StartAddIndex(plan,
                                           indexName,
                                           tableName,
                                           indexedFields,
                                           description));

            /*
             * Wait for the added index to be populated. This may take a while.
             */
            final ParallelBundle bundle = new ParallelBundle();
            for (RepGroupId id : topology.getRepGroupIds()) {
                bundle.addTask(new WaitForAddIndex(plan,
                                                   id,
                                                   indexName,
                                                   tableName));
            }
            plan.addTask(bundle);

            /*
             * Complete the job, make the index visible
             */
            plan.addTask(new CompleteAddIndex(plan,
                                              indexName,
                                              tableName));
        } catch (IllegalArgumentException iae) {
            throw new IllegalCommandException
                ("Failed to add index: " + iae.getMessage(), iae);
        }

        return plan;
    }

    /**
     * Creates a plan to remove an index.
     */
    @SuppressWarnings("unused")
    static DeployTableMetadataPlan
        createRemoveIndexPlan(AtomicInteger idGen,
                              String planName,
                              Planner planner,
                              Topology topology,
                              String indexName,
                              String tableName) {
        checkTable(tableName);
        checkIndex(indexName);

        final String fullPlanName = makeName(planName, tableName, indexName);
        final DeployTableMetadataPlan plan;
        if (Utils.storeHasVersion(planner.getAdmin(), TABLE_AUTH_VERSION)) {
            plan = new RemoveIndexPlan(idGen, fullPlanName, tableName, planner);
        } else {
            plan = new DeployTableMetadataPlan(idGen, fullPlanName, planner);
        }

        /*
         * if drop a text index, ensure ES cluster is registered and
         * accessible before generating a plan
         */
        final TableMetadata md = plan.getMetadata();
        final TableImpl tbl =
            (md != null) ? md.getTable(tableName, true) : null;
        final Index idx = (tbl != null) ? tbl.getIndex(indexName) : null;
        if (idx != null && idx.getType().equals(Index.IndexType.TEXT)) {
             /* es cluster must be registered */
            final Admin admin = planner.getAdmin();
            final Parameters p = admin.getCurrentParameters();
            final ParameterMap pm = Utils.verifyAndGetSearchParams(p);
            final String esClusterName = pm.getOrDefault
                (ParameterState.SN_SEARCH_CLUSTER_NAME).asString();
            if (esClusterName.isEmpty()) {
                throw new IllegalStateException(
                    "DROP INDEX failed, index: " + tableName +
                    ":" + indexName + ", no ES cluster registered.");
            }

            /* es cluster must be accessible */
            final String hostName =
                admin.getParams().getStorageNodeParams().getHostname();
            final String esMembers = pm.getOrDefault
                (ParameterState.SN_SEARCH_CLUSTER_MEMBERS).asString();
            if (!isESClusterReachable(esClusterName, esMembers, hostName)) {
                throw new IllegalStateException(
                    "DROP INDEX failed, index: " + tableName +
                    ":" + indexName +
                    ", reason: time out in connecting " +
                    "ES cluster " + esClusterName);
            }
        }

        tableName = plan.getRealTableName(tableName);
        try {
            plan.addTask(new RemoveIndex(plan, indexName, tableName));
        } catch (IllegalArgumentException iae) {
            throw new IllegalCommandException
                ("Failed to remove index: " + iae.getMessage(), iae);
        }

        return plan;
    }

    /**
     * Ensures ES cluster is registered and accessible
     */
    static boolean isESClusterReachable(final String esClusterName,
                                        final String esMembers,
                                        final String hostName) {

        try (final Node clientNode =
                 ElasticsearchHandler.buildESClientNode(esClusterName,
                                                        esMembers,
                                                        hostName)) {

            /* Checks the state of ES cluster, timeout if not reachable */
            ClusterState state =
                ElasticsearchHandler.esClusterState(clientNode.client()
                                                              .admin());

            //TODO: probably should have better check here, e.g, check if
            //status of ES cluster is healthy?
            assert (state != null);

            return true;
        } catch (Exception e) {
            /* unable to access ES cluster */
            return false;
        }
    }

    /**
     * Creates a plan to add a text index.
     */
    static DeployTableMetadataPlan
        createAddTextIndexPlan(AtomicInteger idGen,
                               String planName,
                               Planner planner,
                               String indexName,
                               String tableName,
                               AnnotatedField[] ftsFields,
                               Map<String,String> properties,
                               String description) {

        checkTable(tableName);
        checkIndex(indexName);
        if (ftsFields == null || ftsFields.length == 0) {
            throw new IllegalCommandException
                ("The set of text-indexed fields cannot be null or empty");
        }

        final Admin admin = planner.getAdmin();
        final Parameters p = admin.getCurrentParameters();

        ParameterMap pm = Utils.verifyAndGetSearchParams(p);
        final String esClusterName = pm.getOrDefault
            (ParameterState.SN_SEARCH_CLUSTER_NAME).asString();
        final String esMembers = pm.getOrDefault
            (ParameterState.SN_SEARCH_CLUSTER_MEMBERS).asString();

        if ("".equals(esClusterName)) {
            throw new IllegalCommandException
                ("An Elasticsearch cluster must be registered with the store " +
                 " before a text index can be created.");
        }

        /* This is the hostname of this Admin's host, where this code is
         * running now.
         */
        final String hostName =
            admin.getParams().getStorageNodeParams().getHostname();

        /*
         * If a stale ES index with the target index's name exists,
         * remove it straightaway.  We already know that the Admin's
         * table metadata does not know about it.
         */
        final String esIndexName =
            TextIndexFeeder.deriveESIndexName(p.getGlobalParams()
                                               .getKVStoreName(),
                                              tableName,
                                              indexName);
        try (final Node clientNode =
                 ElasticsearchHandler.buildESClientNode(esClusterName,
                                                        esMembers,
                                                        hostName)) {
            /* delete existent ES index if any */
            ElasticsearchHandler.deleteESIndex(esIndexName,
                                               clientNode.client().admin());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot ensure deletion of " +
                                            "existing ES index before text " +
                                            "index " + indexName +
                                            " on table " + tableName +
                                            " can be created, reason: " +
                                            e.getMessage());
        }

        /* now ready to deploy the plan to create text index */
        final DeployTableMetadataPlan plan =
            new DeployTableMetadataPlan(idGen,
                                        makeName(planName, tableName, indexName),
                                        planner);
        tableName = plan.getRealTableName(tableName);

        /*
         * Create the index, not-yet-visible
         */
        try {
            plan.addTask(new StartAddTextIndex(plan,
                                               indexName,
                                               tableName,
                                               ftsFields,
                                               properties,
                                               description));

             /* TODO: do we want to wait for the index to be ready? */
             plan.addTask(new CompleteAddIndex(plan,
                                               indexName,
                                               tableName));
        } catch (IllegalArgumentException iae) {
            throw new IllegalCommandException
                ("Failed to add index: " + iae.getMessage(), iae);
        }

        return plan;
    }

    public static DeployTableMetadataPlan createBroadcastTableMDPlan
        (AtomicInteger idGen,
         Planner planner) {
        final DeployTableMetadataPlan plan =
            new DeployTableMetadataPlan(idGen, "Broadcast Table MD", planner);

        plan.addTask(new UpdateMetadata<>(plan));
        return plan;
    }

    private static void checkTable(String tableName) {
        if (tableName == null) {
            throw new IllegalCommandException("Table path cannot be null");
        }
    }

    private static void checkIndex(String indexName) {
        if (indexName == null) {
            throw new IllegalCommandException("Index name cannot be null");
        }
    }

    /**
     * Create a plan or task name that puts more information in the log stream.
     */
    public static String makeName(String name, String tableName,
                                  String indexName) {
        return makeName(name, tableName, indexName, null);

    }

    /**
     * Create a task name that puts more information in the log stream.
     */
    public static String makeName(String name, String tableName,
                                  String indexName, String targetName) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":");
        sb.append(tableName);
        if (indexName != null) {
            sb.append(":");
            sb.append(indexName);
        }
        if (targetName != null) {
            sb.append(" on ");
            sb.append(targetName);
        }
        return sb.toString();
    }
}
