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

package oracle.kv.impl.api.parallelscan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import oracle.kv.Consistency;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.FaultException;
import oracle.kv.Key;
import oracle.kv.KeyRange;
import oracle.kv.KeyValueVersion;
import oracle.kv.ParallelScanIterator;
import oracle.kv.StoreIteratorConfig;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.KeySerializer;
import oracle.kv.impl.api.Request;
import oracle.kv.impl.api.StoreIteratorParams;
import oracle.kv.impl.api.ops.InternalOperation;
import oracle.kv.impl.api.ops.MultiKeyIterate;
import oracle.kv.impl.api.ops.Result;
import oracle.kv.impl.api.ops.ResultKeyValueVersion;
import oracle.kv.impl.api.ops.StoreIterate;
import oracle.kv.impl.api.ops.StoreKeysIterate;
import oracle.kv.impl.topo.Datacenter;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.topo.TopologyUtil;
import oracle.kv.stats.DetailedMetrics;

import com.sleepycat.je.utilint.PropUtil;

/**
 * Implementation of a scatter-gather storeIterator or storeKeysIterator. The
 * iterator will access the store by partitions.
 * {@code PartitionStream} will use to read a single partition.
 */
public class ParallelScan {

    /* Prevent construction */
    private ParallelScan() {}

    /*
     * The entrypoint to ParallelScan from KVStoreImpl.storeKeysIterate.
     */
    public static ParallelScanIterator<Key>
        createParallelKeyScan(final KVStoreImpl storeImpl,
                              final Direction direction,
                              final int batchSize,
                              final Key parentKey,
                              final KeyRange subRange,
                              final Depth depth,
                              final Consistency consistency,
                              final long timeout,
                              final TimeUnit timeoutUnit,
                              final StoreIteratorConfig storeIteratorConfig)
        throws FaultException {

        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }

        if ((parentKey != null) && (parentKey.getMinorPath().size()) > 0) {
            throw new IllegalArgumentException
                ("Minor path of parentKey must be empty");
        }

        final byte[] parentKeyBytes =
            (parentKey != null) ?
                    storeImpl.getKeySerializer().toByteArray(parentKey) : null;

        /* Prohibit iteration of internal keyspace (//). */
        final KeyRange useRange = storeImpl.getKeySerializer().restrictRange
            (parentKey, subRange);

        final StoreIteratorParams parallelKeyScanSIP =
            new StoreIteratorParams(direction,
                                    batchSize,
                                    parentKeyBytes,
                                    useRange,
                                    depth,
                                    consistency,
                                    timeout,
                                    timeoutUnit);

        return new ParallelScanIteratorImpl<Key>(storeImpl,
                                                 storeIteratorConfig,
                                                 parallelKeyScanSIP) {
            @Override
            protected MultiKeyIterate generateGetterOp(byte[] resumeKey) {
                return new StoreKeysIterate
                    (storeIteratorParams.getParentKeyBytes(),
                     storeIteratorParams.getSubRange(),
                     storeIteratorParams.getDepth(),
                     storeIteratorParams.getPartitionDirection(),
                     storeIteratorParams.getBatchSize(),
                     resumeKey);
            }

            @Override
            protected void convertResult(Result result, List<Key> elementList) {
                final List<byte[]> byteKeyResults = result.getKeyList();

                int cnt = byteKeyResults.size();
                if (cnt == 0) {
                    assert (!result.hasMoreElements());
                    return;
                }
                for (int i = 0; i < cnt; i += 1) {
                    final byte[] entry = byteKeyResults.get(i);
                    elementList.add(keySerializer.fromByteArray(entry));
                }
            }

            @Override
            protected byte[] extractResumeKey(Result result,
                                              List<Key> elementList) {
                int cnt = elementList.size();
                if (cnt == 0) {
                    return null;
                }
                return elementList.get(cnt - 1).toByteArray();
            }

            @Override
            protected int compare(Key one, Key two) {
                return one.compareTo(two);
            }
        };
    }

    /*
     * The entrypoint to ParallelScan from KVStoreImpl.storeIterate. The
     * iterator returned via with method will iterate over all of the partitions
     * in the store.
     */
    public static ParallelScanIterator<KeyValueVersion>
        createParallelScan(final KVStoreImpl storeImpl,
                           final Direction direction,
                           final int batchSize,
                           final Key parentKey,
                           final KeyRange subRange,
                           final Depth depth,
                           final Consistency consistency,
                           final long timeout,
                           final TimeUnit timeoutUnit,
                           final StoreIteratorConfig storeIteratorConfig) {
        return createParallelScan(storeImpl,
                                  direction,
                                  batchSize,
                                  parentKey,
                                  subRange,
                                  depth,
                                  consistency,
                                  timeout,
                                  timeoutUnit,
                                  storeIteratorConfig,
                                  null);
    }

    /*
     * The entrypoint to ParallelScan from KVStoreImpl.storeIterate. The
     * iterator returned via with method will iterate over just the partitions
     * in the specified set of partitions.
     */
    public static ParallelScanIterator<KeyValueVersion>
        createParallelScan(final KVStoreImpl storeImpl,
                           final Direction direction,
                           final int batchSize,
                           final Key parentKey,
                           final KeyRange subRange,
                           final Depth depth,
                           final Consistency consistency,
                           final long timeout,
                           final TimeUnit timeoutUnit,
                           final StoreIteratorConfig storeIteratorConfig,
                           final Set<Integer> partitions)
        throws FaultException {

        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }

        if ((parentKey != null) && (parentKey.getMinorPath().size()) > 0) {
            throw new IllegalArgumentException
                ("Minor path of parentKey must be empty");
        }

        final byte[] parentKeyBytes =
            (parentKey != null) ?
            storeImpl.getKeySerializer().toByteArray(parentKey) :
            null;

        /* Prohibit iteration of internal keyspace (//). */
        final KeyRange useRange = storeImpl.getKeySerializer().restrictRange
            (parentKey, subRange);

        final StoreIteratorParams parallelScanSIP =
            new StoreIteratorParams(direction,
                                    batchSize,
                                    parentKeyBytes,
                                    useRange,
                                    depth,
                                    consistency,
                                    timeout,
                                    timeoutUnit,
                                    partitions);

        return new ParallelScanIteratorImpl<KeyValueVersion>
            (storeImpl, storeIteratorConfig, parallelScanSIP) {
            @Override
            protected MultiKeyIterate generateGetterOp(byte[] resumeKey) {
                return new StoreIterate(storeIteratorParams.getParentKeyBytes(),
                                        storeIteratorParams.getSubRange(),
                                        storeIteratorParams.getDepth(),
                                    storeIteratorParams.getPartitionDirection(),
                                        storeIteratorParams.getBatchSize(),
                                        resumeKey);
            }

            @Override
            protected void convertResult(Result result,
                                         List<KeyValueVersion> elementList) {
                final List<ResultKeyValueVersion> byteKeyResults =
                    result.getKeyValueVersionList();

                int cnt = byteKeyResults.size();
                if (cnt == 0) {
                    assert (!result.hasMoreElements());
                    return;
                }
                for (int i = 0; i < cnt; i += 1) {
                    final ResultKeyValueVersion entry = byteKeyResults.get(i);
                    elementList.add(new KeyValueVersion
                        (keySerializer.fromByteArray(entry.getKeyBytes()),
                         entry.getValue(), entry.getVersion()));
                }
            }

            @Override
            protected byte[]
                extractResumeKey(Result result,
                                 List<KeyValueVersion> elementList) {
                int cnt = elementList.size();
                if (cnt == 0) {
                    return null;
                }
                return elementList.get(cnt - 1).getKey().toByteArray();
            }

            @Override
            protected int compare(KeyValueVersion one, KeyValueVersion two) {
                return one.getKey().compareTo(two.getKey());
            }
        };
    }

    /**
     * Base class for parallel scan iterators.
     *
     * Both of the parallel scan methods (storeIterator(...,
     * StoreIteratorConfig) and storeKeysIterator(..., StoreIteratorConfig)
     * return an instance of a ParallelScanIterator (as opposed to plain old
     * Iterator) so that we can eventually use them in try-with-resources
     * constructs.
     */
    public static abstract class ParallelScanIteratorImpl<K> extends
        BaseParallelScanIteratorImpl<K> {

        /* Indexed by partition id. */
        private final Map<Integer, DetailedMetricsImpl> partitionMetrics;
        private final Map<RepGroupId, DetailedMetricsImpl> shardMetrics;

        protected final StoreIteratorParams storeIteratorParams;
        protected final KeySerializer keySerializer;

        public ParallelScanIteratorImpl
            (final KVStoreImpl store,
             final StoreIteratorConfig storeIteratorConfig,
             final StoreIteratorParams storeIteratorParams) {
            this.storeImpl = store;
            this.storeIteratorParams = storeIteratorParams;
            this.itrDirection = storeIteratorParams.getDirection();
            this.keySerializer = store.getKeySerializer();
            this.logger = store.getLogger();
            this.partitionMetrics =
          new HashMap<Integer, DetailedMetricsImpl>(storeImpl.getNPartitions());
            this.shardMetrics = new HashMap<RepGroupId, DetailedMetricsImpl>();
            long timeout = storeIteratorParams.getTimeout();
            requestTimeoutMs = store.getDefaultRequestTimeoutMs();
            if (timeout > 0) {
                requestTimeoutMs = PropUtil.durationToMillis
                    (timeout, storeIteratorParams.getTimeoutUnit());
                if (requestTimeoutMs > store.getReadTimeoutMs()) {
                    String format =
                        "Request timeout parameter: %,d ms exceeds " +
                        "socket read timeout: %,d ms";
                    throw new IllegalArgumentException
                        (String.format(format, requestTimeoutMs,
                                       store.getReadTimeoutMs()));
                }
            }

            createAndSubmitStreams(storeIteratorConfig);
        }

        /**
         * Returns the consistency used for this operation.
         */
        private Consistency getConsistency() {
            return (storeIteratorParams.getConsistency() != null) ?
                    storeIteratorParams.getConsistency() :
                    storeImpl.getDefaultConsistency();
        }

        /*
         * For each partition, create a stream and start reading.
         */
        private void createAndSubmitStreams(
            final StoreIteratorConfig storeIteratorConfig) {

            final Map<RepGroupId, Set<Integer>> partitionsByShard =
                getPartitionTopology(storeIteratorParams.getPartitions());
            int nShards = partitionsByShard.size();
            if (nShards < 1) {
                throw new IllegalStateException
                    ("partitionsByShard has no entries");
            }

            /*
             * Calculate n threads based on topology. The 2x will keep all
             * RNs busy, with a request in transit to/from the RN and a request
             * being processed
             */
            final int RNThreads = 2 *
                ((getConsistency() == Consistency.ABSOLUTE) ?
                 nShards :
                 TopologyUtil.getNumRepNodesForRead
                     (storeImpl.getTopology(),
                      storeImpl.getDispatcher().getReadZoneIds()));

            final int nThreads = storeIteratorConfig.getMaxConcurrentRequests();
            final int useNThreads =
                             (nThreads == 0) ? RNThreads :
                                               Math.min(nThreads, RNThreads);

            taskExecutor = storeImpl.getTaskExecutor(useNThreads);
            streams = new TreeSet<Stream>();
            /*
             * Submit the partition streams in round robin order by shard to
             * achieve poor man's balancing across shards.
             */
            final Map<RepGroupId, List<PartitionStream>> streamsByShard =
                generatePartitionStreams(partitionsByShard);
            final Collection<List<PartitionStream>> streamsByShardColl =
                streamsByShard.values();
            @SuppressWarnings("unchecked")
            final List<PartitionStream>[] streamsByShardArr =
                streamsByShardColl.toArray(new List[0]);
            boolean didSomething;
            do {
                didSomething = false;
                for (int idx = 0; idx < nShards; idx++) {
                    List<PartitionStream> tasks = streamsByShardArr[idx];
                    if(tasks.size() > 0) {
                        PartitionStream task = tasks.get(0);
                        task.submit();
                        streams.add(task);
                        tasks.remove(0);
                        didSomething = true;
                    }
                }
            } while (didSomething);
        }

        private Map<RepGroupId, List<PartitionStream>>
            generatePartitionStreams(final Map<RepGroupId,
                                     Set<Integer>> partitionsByShard) {

            logger.fine("Generating Partition Streams");
            final Map<RepGroupId, List<PartitionStream>> ret =
                new HashMap<RepGroupId, List<PartitionStream>>(
                    partitionsByShard.size());
            for (Map.Entry<RepGroupId, Set<Integer>> ent :
                     partitionsByShard.entrySet()) {
                final RepGroupId rgid = ent.getKey();
                final Set<Integer> parts = ent.getValue();
                for (Integer part : parts) {
                    final PartitionStream pis =
                        new PartitionStream(rgid, part, null);
                    List<PartitionStream> partitionStreams = ret.get(rgid);
                    if (partitionStreams == null) {
                        partitionStreams = new ArrayList<PartitionStream>();
                        ret.put(rgid, partitionStreams);
                    }
                    partitionStreams.add(pis);
                }
            }

            return ret;
        }

        /*
         * Extracts the rep factor of the topology and creates a map of shard to
         * the set of partitions in the shard.
         */
        private Map<RepGroupId, Set<Integer>>
                        getPartitionTopology(final Set<Integer> partitions) {
            final Topology topology =
                storeImpl.getDispatcher().getTopologyManager().getTopology();

            /* Determine Rep Factor. */
            Collection<Datacenter> datacenters =
                topology.getDatacenterMap().getAll();
            if (datacenters.size() < 1) {
                throw new IllegalStateException("No zones in topology?");
            }

            final Map<RepGroupId, Set<Integer>> shardPartitions =
                    new HashMap<RepGroupId, Set<Integer>>();

            /*
             * If the set of partitions was specified, create a map using them
             * and return.
             */
            if (partitions != null) {
                for (Integer i : partitions) {
                    PartitionId partId = new PartitionId(i);
                    RepGroupId rgid = topology.getRepGroupId(partId);
                    if (rgid == null) {
                        throw new IllegalStateException("Partition " + partId +
                                                        " not in topology?");
                    }
                    Set<Integer> parts = shardPartitions.get(rgid);
                    if (parts == null) {
                        parts = new HashSet<Integer>();
                        shardPartitions.put(rgid, parts);
                    }
                    parts.add(i);
                }
                return shardPartitions;
            }

            for (int i = 1; i <= storeImpl.getNPartitions(); i++) {
                PartitionId partId = new PartitionId(i);
                RepGroupId rgid = topology.getRepGroupId(partId);
                if (rgid == null) {
                    throw new IllegalStateException("Partition " + partId +
                                                    " not in topology?");
                }
                Set<Integer> parts = shardPartitions.get(rgid);
                if (parts == null) {
                    parts = new HashSet<Integer>();
                    shardPartitions.put(rgid, parts);
                }
                parts.add(i);
            }

            return shardPartitions;
        }

        /* -- Metrics from ParallelScanIterator -- */

        @Override
        public List<DetailedMetrics> getPartitionMetrics() {
            synchronized (partitionMetrics) {
                List<DetailedMetrics> l =
                    new ArrayList<DetailedMetrics>(partitionMetrics.size());
                l.addAll(partitionMetrics.values());
                return Collections.unmodifiableList(l);
            }
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
         * Returns the generated op for this iterator.
         */
        protected abstract
            InternalOperation generateGetterOp(byte[] resumeKey);

        protected abstract byte[] extractResumeKey
            (Result result, List<K> elementList);

        /**
         * Close the iterator, recording the specified remote exception. If
         * the reason is not null, the exception is thrown from the hasNext()
         * or next() methods.
         *
         * @param reason the exception causing the close or null
         */
        @Override
        protected void close(Exception reason) {
            synchronized (this) {
                if (closed) {
                    return;
                }
                /* Mark this Iterator as terminated */
                closed = true;
                closeException = reason;
            }

            final List<Runnable> unfinishedBusiness =
                taskExecutor.shutdownNow();
            if (!unfinishedBusiness.isEmpty()) {
                logger.log(Level.FINE,
                           "ParallelScan executor didn''t shutdown cleanly. " +
                           "{0} tasks remaining.",
                           unfinishedBusiness.size());
            }
            next = null;
        }

        /**
         * Reading records of a single partition.
         */
        private class PartitionStream extends Stream {
            protected final RepGroupId groupId;
            protected final int partitionId;
            private byte[] resumeKey = null;

            PartitionStream(RepGroupId rgi, int part, byte[] resumeKey) {
                this.groupId = rgi;
                this.partitionId = part;
                this.resumeKey = resumeKey;
            }

            @Override
            protected void updateDetailedMetrics(long timeInMs,
                                                 long recordCount) {
                /* Partition Metrics. */
                final int partIdx = partitionId - 1;
                final String shardName = groupId.toString();
                DetailedMetricsImpl dmi;
                synchronized (partitionMetrics) {
                    dmi = partitionMetrics.get(partIdx);
                    if (dmi != null) {
                        dmi.inc(timeInMs, recordCount);
                    } else {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(partitionId).append(" (").append(shardName).
                            append(")");
                        dmi = new DetailedMetricsImpl(sb.toString(), timeInMs,
                                                      recordCount);
                        partitionMetrics.put(partIdx, dmi);
                    }
                }

                synchronized (shardMetrics) {
                    /* Shard Metrics. */
                    dmi = shardMetrics.get(groupId);
                    if (dmi == null) {
                        dmi = new DetailedMetricsImpl
                            (shardName, timeInMs, recordCount);
                        shardMetrics.put(groupId, dmi);
                        return;
                    }
                }

                dmi.inc(timeInMs, recordCount);
            }

            @Override
            protected Request makeReadRequest() {
                return storeImpl.makeReadRequest(
                    generateGetterOp(resumeKey),
                    new PartitionId(partitionId),
                    storeIteratorParams.getConsistency(),
                    storeIteratorParams.getTimeout(),
                    storeIteratorParams.getTimeoutUnit());
            }

            @Override
            protected void setResumeKey(Result result, List<K> elementList) {
                final boolean hasMore = result.hasMoreElements();
                resumeKey = (hasMore) ?
                    extractResumeKey(result, elementList) :
                    null;
            }

            @Override
            public String toString() {
                return "PartitionStream[" + groupId + ":"+ partitionId + ", "
                       + getStatus() + "]";
            }
        }
    }
}
