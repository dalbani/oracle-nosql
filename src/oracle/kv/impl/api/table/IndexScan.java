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

package oracle.kv.impl.api.table;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static oracle.kv.impl.api.table.TableAPIImpl.getBatchSize;
import static oracle.kv.impl.api.table.TableAPIImpl.getConsistency;
import static oracle.kv.impl.api.table.TableAPIImpl.getTimeout;
import static oracle.kv.impl.api.table.TableAPIImpl.getTimeoutUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import oracle.kv.Consistency;
import oracle.kv.ValueVersion;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.Request;
import oracle.kv.impl.api.TopologyManager;
import oracle.kv.impl.api.TopologyManager.PostUpdateListener;
import oracle.kv.impl.api.ops.IndexIterate;
import oracle.kv.impl.api.ops.IndexKeysIterate;
import oracle.kv.impl.api.ops.InternalOperation;
import oracle.kv.impl.api.ops.Result;
import oracle.kv.impl.api.ops.ResultIndexKeys;
import oracle.kv.impl.api.ops.ResultIndexRows;
import oracle.kv.impl.api.parallelscan.DetailedMetricsImpl;
import oracle.kv.impl.api.parallelscan.BaseParallelScanIteratorImpl;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.stats.DetailedMetrics;
import oracle.kv.table.KeyPair;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.Row;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;

/**
 * Implementation of a scatter-gather iterator for secondary indexes. The
 * iterator will access the store by shards.
 * {@code ShardIndexStream} will use to read a single shard.
 * <p>
 * Discussion of inclusive/exclusive iterations
 * <p>
 * Each request sent to the server side needs a start or resume key and an
 * optional end key. By default these are inclusive.  A {@code FieldRange}
 * object may be included to exercise fine control over start/end values for
 * range queries.  {@code FieldRange} indicates whether the values are inclusive
 * or exclusive.  {@code FieldValue} objects are typed so the
 * inclusive/exclusive state is handled here (on the client side) where they
 * can be controlled per-type rather than on the server where they are simple
 * {@code byte[]}. This means that the start/end/resume keys are always
 * inclusive on the server side.
 */
class IndexScan {

    /* Prevent construction */
    private IndexScan() {}

    /**
     * Creates a table iterator returning ordered rows.
     *
     * @param store
     * @param getOptions
     * @param iterateOptions
     *
     * @return a table iterator
     */
    static TableIterator<Row> createTableIterator
        (final TableAPIImpl apiImpl,
         final IndexKeyImpl indexKey,
         final MultiRowOptions getOptions,
         final TableIteratorOptions iterateOptions) {

        final TargetTables targetTables =
            TableAPIImpl.makeTargetTables(indexKey.getTable(), getOptions);

        return new IndexScanIterator<Row>(apiImpl.getStore(),
                                          indexKey,
                                          getOptions,
                                          iterateOptions) {
            @Override
            protected InternalOperation createOp(byte[] resumeSecondaryKey,
                                                 byte[] resumePrimaryKey) {
                return new IndexIterate(index.getName(),
                                        targetTables,
                                        range,
                                        resumeSecondaryKey,
                                        resumePrimaryKey,
                                        batchSize);
            }

            @Override
            protected void convertResult(Result result, List<Row> rows) {
                final List<ResultIndexRows> indexRowList =
                    result.getIndexRowList();
                for (ResultIndexRows indexRow : indexRowList) {
                    Row converted = convert(indexRow);
                    if (converted != null) {
                        rows.add(converted);
                    }
                }
            }

            /**
             * Converts a single key value into a row.
             */
            private Row convert(ResultIndexRows rowResult) {
                /*
                 * If ancestor table returns may be involved, start at the
                 * top level table of this hierarchy.
                 */
                final TableImpl startingTable =
                    targetTables.hasAncestorTables() ?
                    table.getTopLevelTable() : table;
                final RowImpl fullKey = startingTable.createRowFromKeyBytes
                    (rowResult.getKeyBytes());
                if (fullKey == null) {
                    throw new IllegalStateException
                        ("Unable to deserialize a row from an index result");
                }
                final ValueVersion vv =
                    new ValueVersion(rowResult.getValue(),
                                     rowResult.getVersion());
                return apiImpl.getRowFromValueVersion(vv, fullKey, false);

            }

            @Override
            protected byte[] extractResumeSecondaryKey(Result result,
                                                       List<Row> rowList) {

                /*
                 * The resume key is the last index key in the ResultIndexRows
                 * list of index keys.  Because the index key was only added in
                 * release 3.2 the index keys can be null if talking to an older
                 * server.  In that case, back out to extracting the key from
                 * the last Row in the rowList.  NOTE: this will FAIL if the
                 * index includes a multi-key component such as map or array.
                 * That is why new code was introduced in 3.2.
                 */

                final List<ResultIndexRows> indexRowList =
                    result.getIndexRowList();
                byte[] bytes = indexRowList.get(indexRowList.size() - 1)
                    .getIndexKeyBytes();

                /* this will only be null if talking to a pre-3.2 server */
                if (bytes != null) {
                    return bytes;
                }

                /* compatibility code for pre-3.2 servers */
                Row lastRow = rowList.get(rowList.size() - 1);
                return index.serializeIndexKey(index.createIndexKey(lastRow));
            }

            @Override
            protected byte[] extractResumePrimaryKey(List<Row> rowList) {
                Row row = rowList.get(rowList.size() - 1);
                TableKey key =
                    TableKey.createKey(((RowImpl) row).getTableImpl(),
                                       row, false);
                return key.getKeyBytes();
            }

             /**
             * Compares with a primary and secondary sort, where the
             * primary sort is based on index key and if the index key
             * values are equal, sort by primary key.  Both should never
             * match unless somehow the same row is retrieved from multiple
             * shards in the event of partition migration.
             */
            @Override
            protected int compare(Row one, Row two) {
                final RowImpl oneImpl = (RowImpl) one;
                final RowImpl twoImpl = (RowImpl) two;
                int value = oneImpl.compare(twoImpl,
                                            indexKey.getFieldsInternal());
                if (value == 0) {
                    value = oneImpl.compare(twoImpl,
                                            oneImpl.getTableImpl().
                                            getPrimaryKeyInternal());
                }
                return value;
            }
        };
    }

    /**
     * Creates a table iterator returning ordered key pairs.
     *
     * @param store
     * @param indexKey
     * @param indexRange
     * @param batchSize
     * @param consistency
     * @param timeout
     * @param timeoutUnit
     *
     * @return a table iterator
     */
    static TableIterator<KeyPair>
        createTableKeysIterator(final TableAPIImpl apiImpl,
                                final IndexKeyImpl indexKey,
                                final MultiRowOptions getOptions,
                                final TableIteratorOptions iterateOptions) {

        final TargetTables targetTables =
            TableAPIImpl.makeTargetTables(indexKey.getTable(), getOptions);

        return new IndexScanIterator<KeyPair>(apiImpl.getStore(),
                                              indexKey,
                                              getOptions,
                                              iterateOptions) {
            @Override
            protected InternalOperation createOp(byte[] resumeSecondaryKey,
                                                 byte[] resumePrimaryKey) {
                return new IndexKeysIterate(index.getName(),
                                            targetTables,
                                            range,
                                            resumeSecondaryKey,
                                            resumePrimaryKey,
                                            batchSize);
            }

            /**
             * Convert the results to KeyPair instances.  Note that in the
             * case where ancestor and/or child table returns are requested
             * the IndexKey returned is based on the the index and the table
             * containing the index, but the PrimaryKey returned may be from
             * a different, ancestor or child table.
             */
            @Override
            protected void convertResult(Result result,
                                         List<KeyPair> elementList) {
                final List<ResultIndexKeys> results =
                    result.getIndexKeyList();
                for (ResultIndexKeys res : results) {
                    final IndexKeyImpl indexKeyImpl =
                        convertIndexKey(res.getIndexKeyBytes());
                    if (indexKeyImpl != null) {
                        final PrimaryKeyImpl pkey =
                            convertPrimaryKey(res.getPrimaryKeyBytes());
                        if (pkey != null) {
                            elementList.add(new KeyPair(pkey, indexKeyImpl));
                        }
                    }
                }
            }

            @Override
            protected byte[] extractResumeSecondaryKey
                (Result result, List<KeyPair> elementList) {

                /*
                 * The resumeKey is the byte[] value of the IndexKey from
                 * the last Row returned.
                 */
                IndexKeyImpl ikey = (IndexKeyImpl)
                    elementList.get(elementList.size() - 1).getIndexKey();
                return index.serializeIndexKey(ikey);
            }

            @Override
            protected byte[] extractResumePrimaryKey
                (List<KeyPair> elementList) {

                PrimaryKeyImpl pkey = (PrimaryKeyImpl)
                    elementList.get(elementList.size() - 1).getPrimaryKey();
                TableKey key =
                    TableKey.createKey(pkey.getTableImpl(), pkey, false);
                return key.getKeyBytes();
            }

            @Override
            protected int compare(KeyPair one, KeyPair two) {
                return one.compareTo(two);
            }

            private IndexKeyImpl convertIndexKey(byte[] bytes) {
                return index.rowFromIndexKey(bytes, false);
            }

            private PrimaryKeyImpl convertPrimaryKey(byte[] bytes) {
                /*
                 * If ancestor table returns may be involved, start at the
                 * top level table of this hierarchy.
                 */
                final TableImpl startingTable =
                    targetTables.hasAncestorTables() ?
                    table.getTopLevelTable() : table;
                return startingTable.createPrimaryKeyFromKeyBytes(bytes);
            }
        };
    }

    /**
     * Base class for building index iterators.
     *
     * @param <K> the type of elements returned by the iterator
     */
    private static abstract class IndexScanIterator<K>
        extends BaseParallelScanIteratorImpl<K>
        implements TableIterator<K>,
                   PostUpdateListener {

        private final Consistency consistency;
        /*
         * The number of shards when the iterator was created. If this changes
         * we must abort the operation as data may have been missed between
         * the point that the new shard came online and when we noticed it.
         */
        private final int nGroups;

        /*
         * The hash code of the partition map when the iterator was created.
         * If the location of any partition changes we must abort the operation,
         * otherwise data may be lost or duplicate values can be returned.
         * The hash code is used as a poor man's check to see if the partitions
         * have changed location. We could copy the map and check each
         * partition's location but that could be costly when there are 1000s
         * of partitions. Note the only reason that the map should change is
         * due to a change in the group.
         */
        private final int partitionMapHashCode;

        protected final IndexRange range;
        protected final int batchSize;
        protected final IndexImpl index;
        protected final TableImpl table;

        /* Per shard metrics provided through ParallelScanIterator */
        private final Map<RepGroupId, DetailedMetricsImpl> shardMetrics =
                                new HashMap<RepGroupId, DetailedMetricsImpl>();

        private IndexScanIterator(KVStoreImpl store,
                                  IndexKeyImpl indexKey,
                                  MultiRowOptions getOptions,
                                  TableIteratorOptions iterateOptions) {
            storeImpl = store;
            range = new IndexRange(indexKey, getOptions, iterateOptions);
            itrDirection = range.getDirection();
            consistency = getConsistency(iterateOptions);
            final long timeout = getTimeout(iterateOptions);
            requestTimeoutMs = (timeout == 0) ?
                store.getDefaultRequestTimeoutMs() :
                getTimeoutUnit(iterateOptions).toMillis(timeout);
            if (requestTimeoutMs <= 0) {
                throw new IllegalArgumentException("Timeout must be > 0 ms");
            }
            batchSize = getBatchSize(iterateOptions);
            index = indexKey.getIndexImpl();
            table = index.getTableImpl();
            logger = store.getLogger();

            /* Collect group information from the current topology. */
            final TopologyManager topoManager =
                                    store.getDispatcher().getTopologyManager();
            final Topology topology = topoManager.getTopology();
            final Set<RepGroupId> groups = topology.getRepGroupIds();
            nGroups = groups.size();
            if (nGroups == 0) {
                throw new IllegalStateException("Store not yet initialized");
            }
            partitionMapHashCode = topology.getPartitionMap().hashCode();

            /*
             * The 2x will keep all RNs busy, with a request in transit to/from
             * the RN and a request being processed
             */
            taskExecutor = store.getTaskExecutor(nGroups * 2);

            streams = new TreeSet<Stream>();
            /* For each shard, create a stream and start reading */
            for (RepGroupId groupId : groups) {
                final ShardIndexStream stream =
                    new ShardIndexStream(groupId, null, null);
                streams.add(stream);
                stream.submit();
            }

            /*
             * Register a listener to detect changes in the groups (shards).
             * We register the lister weakly so that the listener will be
             * GCed in the event that the application does not close the
             * iterator.
             */
            topoManager.addPostUpdateListener(this, true);
        }

        /* -- Metrics from ParallelScanIterator -- */

        @Override
        public List<DetailedMetrics> getPartitionMetrics() {
            return Collections.emptyList();
        }

        @Override
        public List<DetailedMetrics> getShardMetrics() {
            synchronized (shardMetrics) {
                final ArrayList<DetailedMetrics> ret =
                    new ArrayList<DetailedMetrics>(shardMetrics.size());
                ret.addAll(shardMetrics.values());
                return ret;
            }
        }

        /**
         * Create an operation using the specified resume key. The resume key
         * parameters may be null.
         *
         * @param resumeSecondaryKey a resume key or null
         * @param resumePrimaryKey a resume key or null
         * @return an operation
         */
        protected abstract InternalOperation createOp
            (byte[] resumeSecondaryKey, byte[] resumePrimaryKey);

        /**
         * Returns a resume secondary key based on the specified element.
         *
         * @param result result object
         * @param elementList the list of elements converted by convert.
         * @return a resume secondary key
         */
        protected abstract byte[]
            extractResumeSecondaryKey(Result result, List<K> elementList);

        /**
         * Returns a resume primary key based on the specified element.
         *
         * @param elementList the list of elements converted by convert.
         * @return a resume primary key
         */
        protected abstract byte[]
            extractResumePrimaryKey(List<K> elementList);

        @Override
        protected void close(Exception reason) {
            close(reason, true);
        }

        /**
         * Close the iterator, recording the specified remote exception. If
         * the reason is not null, the exception is thrown from the hasNext()
         * or next() methods.
         *
         * @param reason the exception causing the close or null
         * @param remove if true remove the topo listener
         */
        private void close(Exception reason, boolean remove) {
            synchronized (this) {
                if (closed) {
                    return;
                }
                /* Mark this Iterator as terminated */
                closed = true;
                closeException = reason;
            }

            if (remove) {
                storeImpl.getDispatcher().getTopologyManager().
                                            removePostUpdateListener(this);
            }

            final List<Runnable> unfinishedBusiness =
                taskExecutor.shutdownNow();
            if (!unfinishedBusiness.isEmpty()) {
                logger.log(Level.FINE,
                           "IndexScan executor didn''t shutdown cleanly. " +
                           "{0} tasks remaining.",
                           unfinishedBusiness.size());
            }
            next = null;
        }

        /* -- From PostUpdateListener -- */

        /*
         * Checks to see if something in the new topology has changed which
         * would invalidate the iteration. In this case if a partition moves
         * we can no longer trust the results. We check for partitions moving
         * by a change in the number of shards or a change in the partition
         * map. If a change is detected the iterator is closed with a
         * UnsupportedOperationException describing the issue.
         */
        @Override
        public boolean postUpdate(Topology topology) {
            if (closed) {
                return true;
            }

            final int newGroupSize = topology.getRepGroupIds().size();

            /*
             * If the number of groups have changed this iterator needs to be
             * closed. The RE will be reported back to the application from
             * hasNext() or next().
             */
            if (nGroups > newGroupSize) {
                close(new UnsupportedOperationException("The number of shards "+
                                         "has decreased during the iteration"),
                      false);
            }

            /*
             * The number of groups has increased.
             */
            if (nGroups < newGroupSize) {
                close(new UnsupportedOperationException("The number of shards "+
                                         "has increased during the iteration"),
                      false);
            }

            /*
             * Check to see if the partition locations have changed (see
             * comment for partitionMapHashCode).
             */
            if (partitionMapHashCode != topology.getPartitionMap().hashCode()) {
                close(new UnsupportedOperationException("The location of " +
                                         "one or more partitions has changed " +
                                         "during the iteration"),
                          false);
            }
            return closed;
        }

        @Override
        public String toString() {
            return "IndexScanIterator[" + index.getName() +
                ", " + itrDirection + "]";
        }

        /**
         * Reading index records of a single shard.
         */
        private class ShardIndexStream extends Stream {
            private final RepGroupId groupId;
            private byte[] resumeSecondaryKey;
            private byte[] resumePrimaryKey;

            ShardIndexStream(RepGroupId groupId,
                             byte[] resumeSecondaryKey,
                             byte[] resumePrimaryKey) {
                this.groupId = groupId;
                this.resumeSecondaryKey = resumeSecondaryKey;
                this.resumePrimaryKey = resumePrimaryKey;
            }

            @Override
            protected void updateDetailedMetrics(long timeInMs,
                                                 long recordCount) {
                DetailedMetricsImpl dmi;
                synchronized (shardMetrics) {

                    dmi = shardMetrics.get(groupId);
                    if (dmi == null) {
                        dmi = new DetailedMetricsImpl(groupId.toString(),
                                                      timeInMs, recordCount);
                        shardMetrics.put(groupId, dmi);
                        return;
                    }
                }
                dmi.inc(timeInMs, recordCount);
            }

            @Override
            protected Request makeReadRequest() {
                return storeImpl.makeReadRequest(
                    createOp(resumeSecondaryKey, resumePrimaryKey),
                    groupId,
                    consistency,
                    requestTimeoutMs,
                    MILLISECONDS);
            }

            @Override
            protected void setResumeKey(Result result, List<K> elementList) {
                final boolean hasMore = result.hasMoreElements();
                resumeSecondaryKey = (hasMore) ?
                    extractResumeSecondaryKey(result, elementList) :
                    null;
                resumePrimaryKey = (hasMore) ?
                    extractResumePrimaryKey(elementList) :
                    null;
            }

            @Override
            public String toString() {
                return "ShardStream[" + groupId + ", " + getStatus() + "]";
            }
        }
    }
}
