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

package oracle.kv.impl.tif;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkItemResponse.Failure;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.rest.RestStatus;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.api.table.IndexImpl.IndexField;
import oracle.kv.impl.api.table.RowImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableKey;
import oracle.kv.impl.param.ParameterUtils;
import oracle.kv.impl.tif.TransactionAgenda.Commit;
import oracle.kv.impl.util.HostPort;
import oracle.kv.impl.util.JsonUtils;
import oracle.kv.impl.util.server.Log4j2julAppender;
import oracle.kv.table.ArrayDef;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.MapDef;
import oracle.kv.table.MapValue;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Table;

/**
 * Object representing an Elastic Search (ES) handler with all ES related
 * operations including ES index and mapping management and data operations.
 */
public class ElasticsearchHandler {
    
    /*
     * Property names of interest to index creation.
     */
    final static String SHARDS_PROPERTY = "ES_SHARDS";
    final static String REPLICAS_PROPERTY = "ES_REPLICAS";

    private final Logger logger;
    private final Client esClient;
    private final AdminClient esAdminClient;

    private boolean doEnsureCommit;

    ElasticsearchHandler(Client esClient, Logger logger) {
        this.esClient = esClient;
        this.logger = logger;
        esAdminClient = esClient.admin();
        /* by default no ensure commit due to performance reason */
        doEnsureCommit = false;
    }

    /**
     * Closes the ES handler
     */
    void close() {
        esClient.close();
    }

    /**
     * Enables ensure commit
     */
    void enableEnsureCommit() {
        doEnsureCommit = true;
    }

    /**
     * Checks if an ES index exists
     *
     * @param indexName name of ES index
     *
     * @return true if an ES index exists
     */
    boolean existESIndex(String indexName) {
        return (existESIndex(indexName, esAdminClient));
    }

    /**
     * Checks if an ES index mapping exists
     *
     * @param esIndexName  name of ES index
     * @param esIndexType  type of mapping in ES index
     *
     * @return true if a mapping exits
     */
    boolean existESIndexMapping(String esIndexName,
                                String esIndexType) {

        /* if no index, no mapping */
        if (!existESIndex(esIndexName)) {
            return false;
        }

        GetMappingsResponse response =
            esAdminClient.indices().prepareGetMappings(esIndexName).execute()
                         .actionGet();

        /* check if mapping exists in index */
        ImmutableOpenMap<String, MappingMetaData> entry =
            response.getMappings().get(esIndexName);

        return entry.containsKey(esIndexType);
    }

    /**
     *
     * Gets a json string representation of a mapping.
     *
     * @param esIndexName  name of ES index
     * @param esIndexType  type of mapping in ES index
     *
     * @return json string
     */
    String getESIndexMapping(String esIndexName, String esIndexType) {

        /* if no index, no mapping */
        if (!existESIndex(esIndexName)) {
            return null;
        }

        GetMappingsResponse response =
            esAdminClient.indices().prepareGetMappings(esIndexName).execute()
                         .actionGet();

        ImmutableOpenMap<String, MappingMetaData> entry =
            response.getMappings().get(esIndexName);

        MappingMetaData mapping = entry.get(esIndexType);
        if (mapping == null) {
            return null;
        }

        try {
            /*
             * The original mapping source is wrapped in a map keyed by the
             * mapping name a/k/a esIndexType.  We'll strip that outer tag
             * before returning the result.
             */
            Map<String, Object> wrapped =
                JsonUtils.getMapFromJsonStr(mapping.source().string());
            if (wrapped == null) {
                return null;
            }
            @SuppressWarnings("unchecked") /* It is definitely a map */
                Map<String, Object> unwrapped =
                (Map<String, Object>)wrapped.get(esIndexType);
            if (unwrapped == null) {
                return null;
            }
            return XContentFactory.jsonBuilder().value(unwrapped).string();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Creates an ES index with default property, the default number of shards
     * and replicas would be applied by ES.
     *
     * @param esIndexName  name of ES index
     * @throws IllegalStateException
     */
    void createESIndex(String esIndexName) {
        createESIndex(esIndexName, (Map<String,String>)null);
    }

    /**
     * Creates an ES index
     *
     * @param esIndexName  name of ES index
     * @param properties   Map of index properties, can be null
     * @throws IllegalStateException
     */
    void createESIndex(String esIndexName, Map<String,String> properties)
        throws IllegalStateException {
        
        Settings.Builder sb = Settings.settingsBuilder();
        if (properties != null) {
            final String shards = properties.get(SHARDS_PROPERTY);
            final String replicas =  properties.get(REPLICAS_PROPERTY);
            
            if (shards != null) {
                if (Integer.parseInt(shards) < 1) {
                    throw new IllegalStateException
                        ("The " + SHARDS_PROPERTY + " value of " + shards +
                         " is not allowed.");
                }
                sb.put("number_of_shards", shards);
            }
            if (replicas != null) {
                if (Integer.parseInt(replicas) < 0) {
                    throw new IllegalStateException
                        ("The " + REPLICAS_PROPERTY + " value of " + replicas +
                         " is not allowed.");
                }
                sb.put("number_of_replicas", replicas);
            }
        }

        try {
            CreateIndexResponse createResponse =
                esAdminClient.indices().
                create(new CreateIndexRequest(esIndexName, sb.build())).
                actionGet();

            if (!createResponse.isAcknowledged()) {
                throw new IllegalStateException("Fail to create ES index " +
                                                esIndexName);
            }
        } catch (IndexAlreadyExistsException e) {
            /*
             * That is OK; multiple repnodes will all try to create the index
             * at the same time, only one of them can win.
             */
            logger.fine("ES index " + esIndexName + " has already been" +
                        "created");
        }

        ensureCommit();

        logger.info("ES index " + esIndexName + " created");
    }

    /**
     * Deletes an ES index
     *
     * @param esIndexName  name of ES index
     *
     * @throws IllegalStateException
     */
    void deleteESIndex(String esIndexName) throws IllegalStateException {

        if (!deleteESIndex(esIndexName, esAdminClient)) {
            logger.info("nothing to delete, ES index " + esIndexName +
                        " does not exist.");
        }

        ensureCommit();

        logger.info("ES index " + esIndexName + " deleted");
    }

    /**
     * Returns all ES indices corresponding to text indices in the kvstore
     *
     * @param storeName  name of kv store
     *
     * @return  list of all ES index names
     */
    Set<String> getAllESIndexNamesInStore(final String storeName) {

        return getAllESIndexInStoreInternal(storeName, esClient);
    }

    /**
     * Creates an ES index mapping
     *
     * @param esIndexName  name of ES index
     * @param esIndexType  ES index type
     * @param mappingSpec  mapping specification
     *
     * @throws IllegalStateException
     */
    void createESIndexMapping(String esIndexName,
                              String esIndexType,
                              String mappingSpec)
        throws IllegalStateException {

        /* ensure the ES index exists */
        if (!existESIndex(esIndexName)) {
            throw new IllegalStateException("ES Index " + esIndexName + " " +
                                            "does not exist");
        }

        /* ensure no pre-existing conflicting mapping */
        if (existESIndexMapping(esIndexName, esIndexType)) {

            String existingMapping =
                getESIndexMapping(esIndexName, esIndexType);

            if (JsonUtils.jsonStringsEqual(existingMapping, mappingSpec)) {
                return;
            }

            throw new IllegalStateException
                ("Mapping " + esIndexType + " " + "already exists in index " +
                 esIndexName + ", but differs from new mapping." +
                 "\nexisting mapping: " + existingMapping +
                 "\nnew mapping: " + mappingSpec);
        }

        PutMappingResponse putMappingResponse =
            esAdminClient.indices().preparePutMapping(esIndexName)
                         .setType(esIndexType).setSource(mappingSpec)
                         .execute().actionGet();

        if (!putMappingResponse.isAcknowledged()) {
            String msg = "Cannot install ES mapping for ES index " +
                         esIndexName + ", type " + esIndexType;
            logger.info(msg);
            throw new IllegalStateException(msg);
        }

        ensureCommit();

        logger.info("Mapping created for ES index: " + esIndexName +
                    ", index type: " + esIndexType +
                    ", mapping spec: " + mappingSpec);
    }

    /**
     * Fetches an entry from ES index
     *
     * @param esIndexName  name of ES index
     * @param esIndexType  type of index mapping
     * @param key          key of entry to get
     *
     * @return response from ES index
     */
    GetResponse get(String esIndexName, String esIndexType, String key) {

        return esClient.prepareGet(esIndexName, esIndexType, key).execute()
                       .actionGet();

    }

    /**
     * Sends a document to ES for indexing
     *
     * @param document     document to index
     */
    IndexResponse index(IndexOperation document) {

        IndexResponse response =
            esClient.prepareIndex(document.getESIndexName(),
                                  document.getESIndexType(),
                                  document.getPkPath())
                    .setSource(document.getDocument())
                    .execute()
                    .actionGet();

        ensureCommit();

        return response;
    }

    /**
     * Deletes an entry from ES index
     *
     * @param esIndexName  name of ES index
     * @param esIndexType  type of index mapping
     * @param key          key of entry to delete
     */
    DeleteResponse del(String esIndexName, String esIndexType, String key)
        throws IllegalStateException {

        DeleteResponse response =
            esClient.prepareDelete(esIndexName, esIndexType, key)
                    .execute()
                    .actionGet();

        ensureCommit();

        return response;
    }

    /**
     * Send a bulk operation to Elasticsearch
     *
     * @param batch  a batch of operations
     * @return  response from ES cluster
     */
    BulkResponse doBulkOperations(List<TransactionAgenda.Commit> batch) {

        if (batch.size() == 0) {
            return null;
        }
        
        BulkRequestBuilder bulkRequest = esClient.prepareBulk();

        for (Commit commit : batch) {

            /* apply each operation to ES index */
            for (IndexOperation op : commit.getOps()) {
                IndexOperation.OperationType type = op.getOperation();
                if (type == IndexOperation.OperationType.PUT) {
                    bulkRequest.add
                        (esClient.prepareIndex(op.getESIndexName(),
                                               op.getESIndexType(),
                                               op.getPkPath())
                         .setSource(op.getDocument()));
                } else if (type == IndexOperation.OperationType.DEL) {
                    bulkRequest.add
                        (esClient.prepareDelete(op.getESIndexName(),
                                                op.getESIndexType(),
                                                op.getPkPath()));
                } else {
                    throw new IllegalStateException("illegal op to ES index " +
                                                    op.getOperation());
                }
            }
        }
        /* Default timeout is one minute, which seems proper. */
        return bulkRequest.get();
    }

    /* sync ES to ensure commit */
    private void ensureCommit() {
        if (doEnsureCommit) {
            esAdminClient.indices().refresh(new RefreshRequest()).actionGet();
        }
    }

    /*------------------------------------------------------*/
    /* static functions, start with public static functions */
    /*------------------------------------------------------*/

   /**
    * Verify that the given Elasticsearch node exists by connecting to it.
    * Use the transport client to do this rather than the node client, since
    * it is a transient connection used only during configuration.  This
    * method is called in the context of the Admin during plan construction.
    *
    * If the node doesn't exist/the connection fails, throw
    * IllegalCommandException, because the user provided an incorrect address.
    *
    * If the node exists/the connection succeeds, ask the node for a list of
    * its peers, and return that list as a String of hostname:port[,...].
    *
    * If storeName is not null, then we expect that an ES Index corresponding
    * to the store name should NOT exist.  If such does exist, then
    * IllegalCommandException is thrown, unless the forceClear flag is set.
    * If forceClear is true, then the offending ES index will be summarily
    * removed.
    *
    * @param clusterName  name of ES cluster
    * @param transportHp  host and port of ES node to connect
    * @param storeName    name of the NoSQL store, or null as described above
    * @param forceClear   if true, allows deletion of the existing ES index
    * @param logger       caller's logger, if any.  Null is allowed in tests.
    *
    * @return list of discovered ES node and port
    */
    public static String getAllTransports(String clusterName,
                                          HostPort transportHp,
                                          String storeName,
                                          boolean forceClear,
                                          Logger logger) {

        final String errorMsg =
            "Can't connect to an Elasticsearch cluster at ";

        if (null != logger) {
            /*
             * Set up log4j to route messages to our j.u.l-based logging
             * subsystem, so that ES messages are recorded in the Admin log.
             */
            org.apache.log4j.Logger log4jRoot =
                org.apache.log4j.Logger.getRootLogger();
            Log4j2julAppender kvServiceAppender =
                Log4j2julAppender.getAppender4Logger(logger, "[es]");
            log4jRoot.addAppender(kvServiceAppender);
        }


        Settings settings = Settings.settingsBuilder().
            put("cluster.name", clusterName).
                                        put("client.transport.sniff", true).
                                        build();

        try (TransportClient client =
             TransportClient.builder().settings(settings).build()) {

            client.addTransportAddress
                (new InetSocketTransportAddress
                 (InetAddress.getByName(transportHp.hostname()),
                  transportHp.port()));

            List<DiscoveryNode> allNodes = client.connectedNodes();

            if (allNodes.isEmpty()) {
                throw new IllegalCommandException(errorMsg + transportHp +
                                                  " {" + clusterName + "}");
            }

            StringBuilder sb = new StringBuilder();
            for (DiscoveryNode node : allNodes) {
                if (sb.length() != 0) {
                    sb.append(ParameterUtils.HELPER_HOST_SEPARATOR);
                }
                sb.append(node.address().toString());
            }

            /*
             * since each es index corresponds to a text index, we do not
             * know exactly the es index name, but we know all es indices
             * starts with a prefix derived from store name, which can
             * be used to check if there are pre-existing es indices for
             * the particular store
             */
            if (storeName != null) {
                /* fetch list of all indices in ES under the store */
                Set<String> allIndices =
                    getAllESIndexInStoreInternal(storeName, client);

                /* delete each existing ES index if force clear */
                String offendingIndexes = "";
                for (String indexName : allIndices) {
                    if (forceClear) {
                        deleteESIndex(indexName, client.admin());
                    } else {
                        offendingIndexes += "  " + indexName + "\n";
                    }
                }
                if (! "".equals(offendingIndexes)) {
                    throw new IllegalCommandException
                        ("The Elasticsearch cluster \"" + clusterName +
                         "\" already contains indexes\n" +
                         "corresponding to the NoSQL Database " +
                         "store \"" + storeName + "\".\n" +
                         "Here is a list of them:\n" +
                         offendingIndexes +
                         "This situation might occur if you " +
                         "register an ES cluster simultaneously with\n" +
                         "two NoSQL Database stores that have the same " +
                         "store name, which is not allowed;\n" +
                         "or if you have removed a NoSQL store " +
                         "to which the ES cluster was registered\n" +
                         "(which makes the ES indexes orphans), " +
                         "and then created the store again with \n" +
                         "the same name. If the offending indexes " +
                         "are no longer needed, you can remove\n" +
                         "them by re-issuing the plan register-es " +
                         "command with the -force option.");
                }
            }

            return sb.toString();

        } catch (UnknownHostException e) {
            throw new IllegalCommandException(errorMsg + transportHp, e);
        }
    }

    /**
     * Returns state of ES cluster
     *
     * @param esAdminClient The ES Admin client handle
     * @return              state of ES cluster
     */
    public static ClusterState esClusterState(AdminClient esAdminClient) {

        final long timeOutMs = 1000;
        final ClusterStateResponse response =
            esAdminClient.cluster()
                         .state(new ClusterStateRequest())
                         .actionGet(timeOutMs);
        return response.getState();
    }

    /**
     * Static version of existESIndex for use by getAllTransports, when no
     * ElasticsearchHandler object exists.  Called during es registration.
     *
     * @param indexName     The name of the ES index to check
     * @param esAdminClient The ES Admin client handle
     * @return              True if the index exists
     */
    public static boolean existESIndex(String indexName,
                                       AdminClient esAdminClient) {
        IndicesExistsRequest existsRequest =
            new IndicesExistsRequest(indexName);
        IndicesExistsResponse existResponse =
            esAdminClient.indices().exists(existsRequest).actionGet();

        return existResponse.isExists();
    }

    /**
     * Static version of addingESIndex
     *
     * @param esIndexName   name of ES index
     * @param esAdminClient ES Admin client handle
     *
     * @throws IllegalStateException, IndexAlreadyExistsException
     */
    public static void createESIndex(String esIndexName,
                                     AdminClient esAdminClient)
        throws IndexAlreadyExistsException, IllegalStateException {


        CreateIndexResponse createResponse =
            esAdminClient.indices().
                create(new CreateIndexRequest(esIndexName)).actionGet();

        if (!createResponse.isAcknowledged()) {
            throw new IllegalStateException("Fail to create ES index " +
                                            esIndexName);
        }
    }

    /**
     * Static version of deleteEsIndex
     *
     * @param indexName     name of the ES index to remove
     * @param esAdminClient ES Admin client handle
     * @return              True if the index existed and was deleted
     */
    public static boolean deleteESIndex(String indexName,
                                        AdminClient esAdminClient) {
        if (!existESIndex(indexName, esAdminClient)) {
            return false;
        }

        DeleteIndexRequest deleteIndexRequest =
            new DeleteIndexRequest(indexName);

        DeleteIndexResponse deleteIndexResponse =
            esAdminClient.indices().delete(deleteIndexRequest).actionGet();


        if (!deleteIndexResponse.isAcknowledged()) {
            throw new IllegalStateException("Fail to delete ES index " +
                                            indexName);
        }
        return true;
    }

    /**
     * Creates an ES client node
     *
     * @param esClusterName     name of es cluster
     * @param esMembers         es cluster members
     * @param hostName          name of client host node
     * @return                  es client node
     * @throws IllegalStateException
     */
    public static Node buildESClientNode(final String esClusterName,
                                         final String esMembers,
                                         final String hostName)
        throws IllegalStateException {

        /* parameter sanity check */
        if (esClusterName.isEmpty()) {
            throw new IllegalStateException("ES cluster name cannot be null");
        }
        if (esMembers.isEmpty()) {
            throw new IllegalStateException("ES member list cannot be empty");
        }
        if (hostName.isEmpty()) {
            throw new IllegalStateException("Hostname cannot be empty");
        }

        /* Set up ES operational properties based on SNA searchCluster params */
        Settings.Builder sb = Settings.settingsBuilder();
        sb.put("discovery.zen.ping.multicast.enabled", "false");
        sb.put("discovery.zen.ping.unicast.hosts", esMembers);
        sb.put("network.host", hostName);
        sb.put("node.master", false);

        /*
         * It should not be necessary to set a path here, since a client node
         * doesn't store data.  A bug appeared in ES 2.0.0 that requires
         * path.home to be set, even though it is not used.
         * see https://github.com/elastic/elasticsearch/issues/13155
         */
        sb.put("path.home", "/tmp/foobar");

        return NodeBuilder.nodeBuilder().clusterName(esClusterName).data(false)
                          .client(true).settings(sb).node();
    }

    /**
     * Returns all ES indices corresponding to text indices in the kvstore
     *
     * @param storeName  name of kv store
     * @param client     es client
     * @return  list of all ES index names
     */
    static Set<String> getAllESIndexInStoreInternal(final String storeName,
                                                    final Client client) {
        final Set<String> ret = new HashSet<>();
        final String prefix = TextIndexFeeder.deriveESIndexPrefix(storeName);

        /* fetch list of all indices in ES */
        Object[] allESIndices = client.admin().cluster()
                                      .prepareState().execute()
                                      .actionGet()
                                      .getState()
                                      .getMetaData()
                                      .getIndices()
                                      .keys()
                                      .toArray();


        for (Object indexName : allESIndices) {
            final String name = (String)indexName;
            if (name.startsWith(prefix)) {
                ret.add(name);
            }
        }

        return ret;
    }

    /*
     * Creates the JSON to describe an ES type mapping for this index.
     */
    static String constructMapping(IndexImpl index) {

        final Table table = index.getTable();

        try {
            XContentBuilder mapping =
                XContentFactory
                    .jsonBuilder()
                    .startObject()
                    .field("dynamic", "false")
                    /* docs should conform to schema. */
                    .startObject("properties");

            /* The mapping includes a representation of the primary key. */
            mapping.startObject("_pkey")
                .field("enabled", false) /* Do not index the pkey. */
                .startObject("properties")
                .startObject("_table")
                .field("type", "string")
                .endObject();

            for (String keyField : table.getPrimaryKey()) {
                mapping.startObject(keyField).
                    field("type",
                          defaultESTypeFor
                              (table.getField(keyField).getType())).endObject();
            }
            mapping.endObject().endObject(); /* end of properties, _pkey */

            /*
             * We want to preserve the letter case of field names in the ES
             * document type, but the name of the path in IndexField is
             * lower-cased.  The field names in IndexImpl have their original
             * case intact.  So we iterate over the list of String field names
             * and the list of IndexFields in parallel, so that we have the
             * unmolested name in hand when it's needed.
             */
            List<IndexField> indexFields = index.getIndexFields();
            int indexFieldCounter = 0;
            for (String field : index.getFieldsInternal()) {
                IndexField indexField = indexFields.get(indexFieldCounter++);

                /*
                 * We have to parse the mappingSpec string so that it is copied
                 * correctly into the builder.  The mappingSpec cannot be
                 * treated as a string, or it will be quoted in its entirety in
                 * the resulting JSON string.
                 */
                String annotation = index.getAnnotationForField(field);
                annotation = (annotation == null ? "{}" : annotation);
                try (XContentParser parser = XContentType.JSON.xContent().
                     createParser(annotation)) {

                    Map<String, Object> m = parser.map();

                    String mappingFieldName =
                        getMappingFieldName(field);
                    if (null == m.get("type")) {
                        m.put("type", getMappingFieldType(indexField));
                    }

                    mapping.field(mappingFieldName, m);
                }
            }

            mapping.endObject().endObject();

            return mapping.string();

        } catch (IOException e) {
            throw new IllegalStateException
                ("Unable to serialize ES mapping for text index " +
                 index.getName(), e);
        }
    }

    /*
     * Mangle a table field's name so that it works as an ES mapping field
     * name.  In particular, the '.' character is not allowed in mappings,
     * so we substitute '/' for '.'.  Otherwise, the name is used as is,
     * including the perverse coding [] that marks a map value.
     */
    private static String getMappingFieldName(String field) {
        return field.replace('.', '/');
    }

    /*
     * Return the default type for the field represented by the iField.
     */
    private static String getMappingFieldType(IndexField ifield) {

        /* The possibilities are as follows:
         *
         * 1. ifield represents a scalar type.
         *
         * 2. ifield represents an Array
         *    2a. The array contains a scalar type.
         *    2b. The array contains a record and ifield refers to a
         *        scalar type field in the record.
         *
         * 3. ifield represents a Record and refers to a scalar field
         *    in the Record.
         *
         * 4. ifield represents a Map
         *    4a. ifield refers to the Map's string key.
         *    4b. ifield refers to the Map's value.
         *    4c. ifield refers to a specific key name.
         *
         */
              
        FieldDef fdef = ifield.getFirstDef();
        int stepIdx = 0;
        String fieldName = ifield.getSteps().get(stepIdx++);

        switch (fdef.getType()) {
        case STRING:
        case INTEGER:
        case LONG:
        case BOOLEAN:
        case FLOAT:
        case DOUBLE:
            /* case 1 */
            return defaultESTypeFor(fdef.getType());

        case ARRAY:
            final ArrayDef adef = fdef.asArray();
            fdef = adef.getElement();
            if (!fdef.isComplex()) {
                /* case 2a. */
                return defaultESTypeFor(fdef.getType());
            }
            if (!fdef.isRecord()) {
                throw new IllegalStateException
                    ("Array type " + fdef + " not allowed as an index field.");
            }
            /* case 2b. */
            stepIdx++; /* Skip over the ifield placeholder "[]" */ 
            //$FALL-THROUGH$

        case RECORD:
            /* case 3. */
            fieldName = ifield.getSteps().get(stepIdx++);
            fdef = fdef.asRecord().getField(fieldName);
            return defaultESTypeFor(fdef.getType());

        case MAP:
            final MapDef mdef = fdef.asMap();
            fieldName = ifield.getSteps().get(stepIdx++);
            if (TableImpl.KEY_TAG.equals(fieldName)) {
                /* case 4a. Keys are always strings. */
                return defaultESTypeFor(FieldDef.Type.STRING);
            }
            /* case 4b and 4c are the same from a schema standpoint. */
            fdef = mdef.getElement();
            return defaultESTypeFor(fdef.getType());

        default:
            throw new IllegalStateException
                ("Fields of type " + fdef + " aren't allowed as index fields.");
        }            
    }

    /*
     * Returns a put operation containing a JSON document suitable for
     * indexing at an ES index, based on the given RowImpl.
     *
     * @param esIndexName  name of es index to which the put operation is
     *                     created
     * @param esIndexType  es index mapping to which the put operation is
     *                     created
     * @param row          row from which to create a put operation
     * @return  a put index operation to an es index; if null, it means
     *                     that no significant content was found.
     */
    static IndexOperation makePutOperation(IndexImpl index,
                                           String esIndexName,
                                           String esIndexType,
                                           RowImpl row) {

        final Table table = index.getTable();
        assert (table == row.getTable());

        /* The encoded string form of the row's primary key. */
        String pkPath =
            TableKey.createKey(table, row, false).getKey().toString();

        try {
            XContentBuilder document =
                XContentFactory.jsonBuilder().
                    startObject().                             /* root object */
                    startObject("_pkey").        /* nested primary key object */
                    field("_table", table.getFullName());

            for (String keyField : table.getPrimaryKey()) {
                document.field(keyField, row.get(keyField));
            }

            document.endObject();              /* end of primary key object */

            List<IndexField> indexFields = index.getIndexFields();
            int indexFieldCounter = 0;
            boolean contentToIndex = false;
            for (String field : index.getFieldsInternal()) {
                IndexField indexField = indexFields.get(indexFieldCounter++);
                if (addValue(indexField, row,
                             getMappingFieldName(field), document)) {
                    contentToIndex = true;
                }
            }

            if (!contentToIndex) {
                return null;
            }

            document.endObject();             /* end of root object */

            return new IndexOperation(esIndexName,
                                      esIndexType,
                                      pkPath,
                                      document.string(),
                                      IndexOperation.OperationType.PUT);
        } catch (IOException e) {
            throw new IllegalStateException
                ("Unable to serialize ES document for text index " +
                 index.getName(), e);
        }
    }

    /*
     * Add a field to the JSON document using the value implied by indexField
     * and row.  A return value of false indicates that no indexable content
     * was found.
     */
    private static boolean addValue(IndexField indexField,
                                    RowImpl row,
                                    String mappingFieldName,
                                    XContentBuilder document)
        throws IOException {

        FieldDef fdef = indexField.getFirstDef();
        int stepIdx = 0;
        String fieldName = indexField.getSteps().get(stepIdx++);

        /* Emit the field name lazily; if there is nothing to index,
         * don't bother indexing the field at all.
         */
        final DocEmitter emitter = new DocEmitter(mappingFieldName, document);

        switch (fdef.getType()) {

            /* Scalar types are easy. */
        case STRING:
        case INTEGER:
        case LONG:
        case BOOLEAN:
        case FLOAT:
        case DOUBLE:
            emitter.putValue(row.get(fieldName));
            break;

            /* An array can contain either scalars or records.
             * If it's an array of records, one field of the record will
             * be indicated by IndexField.
             */
        case ARRAY:
            final ArrayValue aValue = row.get(fieldName).asArray();
            final ArrayDef adef = fdef.asArray();
            fdef = adef.getElement();
            if (fdef.isComplex()) {
                if (!fdef.isRecord()) {
                    throw new IllegalStateException
                        ("Array type " + fdef +
                         " not allowed as an index field.");
                }
                stepIdx++; /* Skip over the ifield placeholder "[]" */
                fieldName = indexField.getSteps().get(stepIdx++);
            }
            for (FieldValue element : aValue.toList()) {
                if (element.isRecord()) {
                    emitter.putArrayValue(element.asRecord().get(fieldName));
                } else {
                    emitter.putArrayValue(element);
                }
            }
            break;
                    
            /*
             * A record will have one field indicated for indexing.
             */
        case RECORD:
            RecordValue rValue = row.get(fieldName).asRecord();
            fieldName = indexField.getSteps().get(stepIdx++);
            emitter.putValue(rValue.get(fieldName));
            break;

            /*
             * An index field can specify that all keys, all values, or one
             * value corresponding to a given key be included in the index.
             */
        case MAP:
            final MapValue mValue = row.get(fieldName).asMap();
            final Map<String, FieldValue> mFields = mValue.getFields();
            fieldName = indexField.getSteps().get(stepIdx++);
            if (TableImpl.KEY_TAG.equals(fieldName)) {
                /* add all the keys in the map */
                for (String key : mFields.keySet()) {
                    emitter.putArrayString(key);
                }
            } else if (TableImpl.ANONYMOUS.equals(fieldName)) {
                /* add all the values in the map */
                for (Entry<String, FieldValue> entry : mFields.entrySet()) {
                    emitter.putArrayValue(entry.getValue());
                }
            } else {
                emitter.putValue(mFields.get(fieldName));
            }
            break;

        default:
            throw new IllegalStateException
                ("Unexpected type in addValue" + fdef);
        }
        emitter.end();
        return emitter.emitted();
    }

    /*
     * DocEmitter is a helper class for writing fields into an XContentBuilder.
     * It delays writing the field name, so that if it is discovered that there
     * is no content of interest, it can avoid writing anything at all.
     */
     private static class DocEmitter {
        
        private final String fieldName;
        private final XContentBuilder document;
        private boolean emitted;
        private boolean emittingArray;

        DocEmitter(String fieldName, XContentBuilder document) {
            this.fieldName = fieldName;
            this.document = document;
            this.emitted = false;
            this.emittingArray = false;
        }

        private void startEmittingMaybe() throws IOException {
            if (!emitted) {
                document.field(fieldName);
                emitted = true;
            }
        }

        private void startEmittingArrayMaybe() throws IOException {
            startEmittingMaybe();
            if (!emittingArray) {
                document.startArray();
                emittingArray = true;
            }
        }

        void putString(String val) throws IOException {
            if (val == null) {
                return;
            }
            startEmittingMaybe();
            document.value(val);
        }

        void putValue(FieldValue val) throws IOException {
            if (val == null || val.isNull()) {
                return;
            }
            putString(val.toString());
        }

        void putArrayString(String val) throws IOException {
            if (val == null || "".equals(val)) {
                return;
            }
            startEmittingArrayMaybe();
            putString(val);
        }    

        void putArrayValue(FieldValue val) throws IOException {
            if (val == null || val.isNull()) {
                return;
            }
            putArrayString(val.toString());
        }

        void end() throws IOException {
            if (emittingArray) {
                document.endArray();
                emittingArray = false;
            }
        }

        boolean emitted() {
            return emitted;
        }
    }

    /*
     * Returns a delete operation containing a JSON document suitable for
     * indexing at an ES index, based on the given RowImpl.
     *
     * @param esIndexName  name of es index to which the delete operation is
     *                     created
     * @param esIndexType  es index mapping to which the delete operation is
     *                     created
     * @param row         row from which to create a delete operation
     * @return  a delete operation to an es index
     */
    static IndexOperation makeDeleteOperation(IndexImpl index,
                                              String esIndexName,
                                              String esIndexType,
                                              RowImpl row) {

        final Table table = index.getTable();
        assert table == row.getTable();

        /* The encoded string form of the row's primary key. */
        String pkPath =
            TableKey.createKey(table, row, false).getKey().toString();

        return new IndexOperation(esIndexName,
                                  esIndexType,
                                  pkPath,
                                  null,
                                  IndexOperation.OperationType.DEL);
    }

    /*
     * Provides a default translation between NoSQL types and ES types.
     *
     * @param t  field type in NoSQL DB
     * @return ES type translated from field type
     */
    static String defaultESTypeFor(FieldDef.Type t) {
        switch (t) {
            case STRING:
            case INTEGER:
            case LONG:
            case BOOLEAN:
            case FLOAT:
            case DOUBLE:
                return t.toString().toLowerCase();
            case ARRAY:
            case BINARY:
            case FIXED_BINARY:
            case MAP:
            case RECORD:
            case ENUM:
            default:
                throw new IllegalStateException
                    ("Unexpected default type mapping requested for " + t);
        }
    }

    /*
     * Returns true if f represents a retriable failure.  Some ES errors
     * indicate that a there is a problem with the document that was sent to
     * ES.  Such documents will never succeed in being indexed and so should
     * not be retried. The status code is intended for REST request statuses,
     * and not all of the possible values are relevant to the bulk request.  I
     * have chosen to list all possible values in the switch statement anyway;
     * any that seem irrelevant are simply relegated to the "not retriable"
     * category.

     * @param f  A Failure object from a BulkItemResponse.
     * @return   Boolean indication of whether the failure should be re-tried.
     */
    static boolean isRetriable(Failure f) {
        RestStatus status = f.getStatus();
        switch (status) {
        case BAD_GATEWAY:
        case CONFLICT:
        case GATEWAY_TIMEOUT:
        case INSUFFICIENT_STORAGE:
        case INTERNAL_SERVER_ERROR:
        case TOO_MANY_REQUESTS: /* Returned if we try to use a client node that
                                   has been shut down; which would be a bug */
        case SERVICE_UNAVAILABLE: /* Returned if the shard has insufficient
                                     replicas - this is the significant one */

            return true;

        case ACCEPTED:
        case BAD_REQUEST:
        case CONTINUE:
        case CREATED:
        case EXPECTATION_FAILED:
        case FAILED_DEPENDENCY:
        case FOUND:
        case FORBIDDEN:
        case GONE:
        case HTTP_VERSION_NOT_SUPPORTED:
        case LENGTH_REQUIRED:
        case LOCKED:
        case METHOD_NOT_ALLOWED:
        case MOVED_PERMANENTLY:
        case MULTIPLE_CHOICES:
        case MULTI_STATUS:
        case NON_AUTHORITATIVE_INFORMATION:
        case NOT_ACCEPTABLE:
        case NOT_FOUND:
        case NOT_IMPLEMENTED:
        case NOT_MODIFIED:
        case NO_CONTENT:
        case OK:
        case PARTIAL_CONTENT:
        case PAYMENT_REQUIRED:
        case PRECONDITION_FAILED:
        case PROXY_AUTHENTICATION:
        case REQUESTED_RANGE_NOT_SATISFIED:
        case REQUEST_ENTITY_TOO_LARGE:
        case REQUEST_TIMEOUT:
        case REQUEST_URI_TOO_LONG:
        case RESET_CONTENT:
        case SEE_OTHER:
        case SWITCHING_PROTOCOLS:
        case TEMPORARY_REDIRECT:
        case UNAUTHORIZED:
        case UNPROCESSABLE_ENTITY:
        case UNSUPPORTED_MEDIA_TYPE:
        case USE_PROXY:
        default:

            return false;
        }
    }
}
