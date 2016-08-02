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

package oracle.kv.impl.api.table;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static oracle.kv.impl.api.table.TableAPIImpl.getBatchSize;
import static oracle.kv.impl.api.table.TableAPIImpl.getConsistency;
import static oracle.kv.impl.api.table.TableAPIImpl.getTimeout;
import static oracle.kv.impl.api.table.TableAPIImpl.getTimeoutUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.Key;
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
import oracle.kv.impl.api.parallelscan.BaseParallelScanIteratorImpl;
import oracle.kv.impl.api.parallelscan.DetailedMetricsImpl;
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
public class IndexScan {

    static final Comparator<byte[]> KEY_BYTES_COMPARATOR =
        new Key.BytesComparator();

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
    static TableIterator<Row> createTableIterator(
        final TableAPIImpl tableAPI,
        final IndexKeyImpl indexKey,
        final MultiRowOptions mro,
        final TableIteratorOptions tio) {

       return createTableIterator(tableAPI, indexKey, mro, tio, null);
    }

    static TableIterator<Row> createTableIterator(
        final TableAPIImpl tableAPI,
        final IndexKeyImpl indexKey,
        final MultiRowOptions mro,
        final TableIteratorOptions tio,
        final Set<RepGroupId> shardSet) {

        final TargetTables targetTables =
            TableAPIImpl.makeTargetTables(indexKey.getTable(), mro);

        final IndexImpl index = (IndexImpl) indexKey.getIndex();
        final TableImpl table = (TableImpl) index.getTable();
        final IndexRange range = new IndexRange(indexKey, mro, tio);

        return new IndexScanIterator<Row>(tableAPI.getStore(), tio, shardSet) {

            @Override
            protected ShardIndexStream createStream(RepGroupId groupId) {
                return new IndexRowScanStream(groupId, null, null);
            }

            @Override
            protected InternalOperation createOp(
                byte[] resumeSecondaryKey,
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
                    rows.add(converted);
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

                final RowImpl fullKey = startingTable.createRowFromKeyBytes(
                    rowResult.getKeyBytes());

                if (fullKey == null) {
                    throw new IllegalStateException
                        ("Unable to deserialize a row from an index result");
                }

                final ValueVersion vv =
                    new ValueVersion(rowResult.getValue(),
                                     rowResult.getVersion());

                RowImpl row =
                    tableAPI.getRowFromValueVersion(
                        vv,
                        fullKey,
                        rowResult.getExpirationTime(),
                        false);
                return row;
            }

            @Override
            protected byte[] extractResumeSecondaryKey(Result result) {

                /*
                 * The resume key is the last index key in the ResultIndexRows
                 * list of index keys.  Because the index key was only added in
                 * release 3.2 the index keys can be null if talking to an older
                 * server.  In that case, back out to extracting the key from
                 * the last Row in the rowList.  NOTE: this will FAIL if the
                 * index includes a multi-key component such as map or array.
                 * That is why new code was introduced in 3.2.
                 */
                byte[] bytes = result.getSecondaryResumeKey();

                /* this will only be null if talking to a pre-3.2 server */
                if (bytes != null || !result.hasMoreElements()) {
                    return bytes;
                }

                /* compatibility code for pre-3.2 servers */
                List<Row> rowList = new ArrayList<Row>();
                convertResult(result, rowList);
                Row lastRow = rowList.get(rowList.size() - 1);
                return index.serializeIndexKey(index.createIndexKey(lastRow));
            }

            @Override
            protected int compare(Row one, Row two) {
                throw new IllegalStateException("Unexpected call");
            }

            /**
             * IndexRowScanStream subclasses ShardIndexStream in order to
             * implement correct ordering of the streams used by an
             * IndexRowScanIterator. Specifically, the problem is that
             * IndexRowScanIterator returns Row objs, and as a result
             * IndexRowScanIterator.compare(), which compares Rows, does not
             * do correct ordering. Instead we must compare index keys. If
             * two index keys (from different shards) are equal, then the
             * associated primary keys are also compared, to make sure that 2
             * streams will never have the same order magnitude (the only way
             * that 2 streams may both return the same index-key, primary-key
             * pair is when both streams retrieve the same row from multiple
             * shards in the event of partition migration.
             */
            class IndexRowScanStream extends ShardIndexStream {

                IndexRowScanStream(
                    RepGroupId groupId,
                    byte[] resumeSecondaryKey,
                    byte[] resumePrimaryKey) {

                    super(groupId, resumeSecondaryKey, resumePrimaryKey);
                }

                @Override
                protected int compareInternal(Stream o) {

                    IndexRowScanStream other = (IndexRowScanStream)o;

                    ResultIndexRows res1 =
                        currentResultSet.getIndexRowList().
                        get(currentResultPos);

                    ResultIndexRows res2 =
                        other.currentResultSet.getIndexRowList().
                        get(other.currentResultPos);

                    byte[] key1 = res1.getIndexKeyBytes();
                    byte[] key2 = res2.getIndexKeyBytes();

                    int cmp = compareUnsignedBytes(key1, key2);

                    if (cmp == 0) {
                        cmp = KEY_BYTES_COMPARATOR.compare(res1.getKeyBytes(),
                                                           res2.getKeyBytes());
                    }

                    return itrDirection == Direction.FORWARD ? cmp : (cmp * -1);
                }

                /**
                 * Compare using a default unsigned byte comparison.
                 *
                 * WARNING!!!!!!!!!!
                 * This is code copied out from JE (see
                 * src/com/sleepycat/je/tree/Key.java). It is extremely
                 * unlikely that JE will modify this code, but if it happens,
                 * the changes should be applied here as well.
                 */
                private int compareUnsignedBytes(byte[] key1, byte[] key2) {
                    return compareUnsignedBytes(key1, 0, key1.length,
                                                key2, 0, key2.length);
                }

                private int compareUnsignedBytes(
                    byte[] key1,
                    int off1,
                    int len1,
                    byte[] key2,
                    int off2,
                    int len2) {

                    int limit = Math.min(len1, len2);

                    for (int i = 0; i < limit; i++) {
                        byte b1 = key1[i + off1];
                        byte b2 = key2[i + off2];
                        if (b1 == b2) {
                            continue;
                        }
                        /*
                         * Remember, bytes are signed, so convert to shorts
                         * so that we effectively do an unsigned byte
                         * comparison.
                         */
                        return (b1 & 0xff) - (b2 & 0xff);
                    }

                    return (len1 - len2);
                }
            }
        };
    }

    /**
     * Creates a table iterator returning ordered key pairs.
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
        final IndexImpl index = (IndexImpl) indexKey.getIndex();
        final TableImpl table = (TableImpl) index.getTable();
        final IndexRange range =
            new IndexRange(indexKey, getOptions, iterateOptions);

        return new IndexScanIterator<KeyPair>(apiImpl.getStore(),
                                              iterateOptions,
                                              null) {
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

                    final PrimaryKeyImpl pkey = convertPrimaryKey(res);

                    if (indexKeyImpl != null && pkey != null) {
                        elementList.add(new KeyPair(pkey, indexKeyImpl));
                    } else {
                        elementList.add(null);
                    }
                }
            }

            @Override
            protected int compare(KeyPair one, KeyPair two) {
                return one.compareTo(two);
            }

            private IndexKeyImpl convertIndexKey(byte[] bytes) {
                IndexKeyImpl ikey = index.createIndexKey();
                index.rowFromIndexKey(bytes,
                                      ikey,
                                      false, /*allowPartial*/
                                      false  /*createTableRow*/);
                return ikey;
            }

            private PrimaryKeyImpl convertPrimaryKey(ResultIndexKeys res) {
                /*
                 * If ancestor table returns may be involved, start at the
                 * top level table of this hierarchy.
                 */
                final TableImpl startingTable =
                    targetTables.hasAncestorTables() ?
                    table.getTopLevelTable() : table;
                final PrimaryKeyImpl pkey = startingTable.
                    createPrimaryKeyFromKeyBytes(res.getPrimaryKeyBytes());
                pkey.setExpirationTime(res.getExpirationTime());
                return pkey;
            }
        };
    }

    /**
     * Base class for building shard iterators.
     *
     * @param <K> the type of elements returned by the iterator
     */
    public static abstract class IndexScanIterator<K>
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

        protected final int batchSize;

        /* Per shard metrics provided through ParallelScanIterator */
        private final Map<RepGroupId, DetailedMetricsImpl> shardMetrics =
            new HashMap<RepGroupId, DetailedMetricsImpl>();

        public IndexScanIterator(
            KVStoreImpl store,
            TableIteratorOptions iterateOptions,
            Set<RepGroupId> shardSet) {

            /*
             * BaseParallelScanIterator needs itrDirection to be set in order to
             * sort properly. If not set, index scans default to FORWARD.
             */
            itrDirection = iterateOptions != null ?
                iterateOptions.getDirection() : Direction.FORWARD;
            storeImpl = store;
            consistency = getConsistency(iterateOptions);

            final long timeout = getTimeout(iterateOptions);

            requestTimeoutMs = (timeout == 0) ?
                store.getDefaultRequestTimeoutMs() :
                getTimeoutUnit(iterateOptions).toMillis(timeout);

            if (requestTimeoutMs <= 0) {
                throw new IllegalArgumentException("Timeout must be > 0 ms");
            }

            batchSize = getBatchSize(iterateOptions);
            logger = store.getLogger();

            /* Collect group information from the current topology. */
            final TopologyManager topoManager =
                                    store.getDispatcher().getTopologyManager();
            final Topology topology = topoManager.getTopology();
            Set<RepGroupId> groups;
            if (shardSet == null) {
                groups = topology.getRepGroupIds();
            } else {
                groups = shardSet;
            }
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
                final ShardIndexStream stream = createStream(groupId);
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

        /*
         * Sbclasses override this if they need to use a subclass of
         * ShardIndexStream in their implementation.
         */
        protected ShardIndexStream createStream(RepGroupId groupId) {
            return new ShardIndexStream(groupId, null, null);
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
         * @return a resume secondary key
         */
        protected byte[] extractResumeSecondaryKey(Result result) {
            return result.getSecondaryResumeKey();
        }

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

        /**
         * Reading index records of a single shard.
         */
        protected class ShardIndexStream extends Stream {

            private final RepGroupId groupId;

            private byte[] resumeSecondaryKey;

            private byte[] resumePrimaryKey;

            protected ShardIndexStream(RepGroupId groupId,
                                       byte[] resumeSecondaryKey,
                                       byte[] resumePrimaryKey) {
                this.groupId = groupId;
                this.resumeSecondaryKey = resumeSecondaryKey;
                this.resumePrimaryKey = resumePrimaryKey;
            }

            protected RepGroupId getGroupId() {
                return groupId;
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
            protected void setResumeKey(Result result) {
                resumeSecondaryKey = extractResumeSecondaryKey(result);
                resumePrimaryKey = result.getPrimaryResumeKey();
            }

            @Override
            public String toString() {
                return "ShardStream[" + groupId + ", " + getStatus() + "]";
            }
        }
    }
}
