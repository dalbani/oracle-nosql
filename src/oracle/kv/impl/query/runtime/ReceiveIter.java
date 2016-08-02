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

package oracle.kv.impl.query.runtime;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import oracle.kv.Consistency;
import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.ParallelScanIterator;
import oracle.kv.StoreIteratorConfig;
import oracle.kv.StoreIteratorException;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.Request;
import oracle.kv.impl.api.StoreIteratorParams;
import oracle.kv.impl.api.ops.Result;
import oracle.kv.impl.api.ops.TableQuery;
import oracle.kv.impl.api.parallelscan.ParallelScan.ParallelScanIteratorImpl;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.IndexScan.IndexScanIterator;
import oracle.kv.impl.api.table.PrimaryKeyImpl;
import oracle.kv.impl.api.table.RecordDefImpl;
import oracle.kv.impl.api.table.RecordValueImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TupleValue;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.impl.query.compiler.SortSpec;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.topo.RepGroupId;
import oracle.kv.impl.util.SerializationUtil;
import oracle.kv.query.ExecuteOptions;
import oracle.kv.stats.DetailedMetrics;
import oracle.kv.table.TableIteratorOptions;

/**
 * ReceiveIter are placed at the boundaries between parts of the query that
 * execute on different "machines". Currently, there can be only one ReceiveIter
 * in the whole query plan. It executes at a "client machine" and its child
 * subplan executes at a "server machine". The child subplan may actually be
 * replicated on several server machines (RNs), in which case the ReceiveIter
 * acts as a UNION ALL expr, collecting and propagating the results it receives
 * from its children. Furthermore, the ReceiveIter may perform a merge-sort over
 * its inputs (if the inputs return sorted results).
 *
 * If the ReceiveIter is the root iter, it just propagates to its output the
 * FieldValues (most likely RecordValues) it receives from the RNs. Otherwise,
 * if its input iter produces tuples, the ReceiveIter will recreate these tuples
 * at its output by unnesting into tuples the RecordValues arriving from the RNs.
 *
 */
public class ReceiveIter extends PlanIter {

    public enum DistributionKind {
        SINGLE_PARTITION,
        ALL_PARTITIONS,
        ALL_SHARDS
    }

    private static class ReceiveIterState extends PlanIterState {

        final boolean theRunOnClient;

        final PartitionId thePartitionId;

        ParallelScanIterator<FieldValueImpl> theRemoteResultsIter;

        ReceiveIterState(PartitionId pid, boolean runOnClient) {
            theRunOnClient = runOnClient;
            thePartitionId = pid;
        }

        @Override
        protected void reset(PlanIter iter) {
            super.reset(iter);
            if (theRemoteResultsIter != null) {
                theRemoteResultsIter.close();
                theRemoteResultsIter = null;
            }
        }

        @Override
        protected void close() {
            super.close();
            if (theRemoteResultsIter != null) {
                theRemoteResultsIter.close();
                theRemoteResultsIter = null;
            }
        }
    }

    private final PlanIter theInputIter;

    private final FieldDefImpl theInputType;

    private final int[] theSortFieldPositions;

    private final SortSpec[] theSortSpecs;

    private final int[] theTupleRegs;

    private final DistributionKind theDistributionKind;

    private final RecordValueImpl thePrimaryKey;

    private final String theTableName;

    private final PlanIter[] thePushedExternals;

    private final int theNumRegs;

    private final int theNumIters;

    public ReceiveIter(
        Expr e,
        int resultReg,
        int[] tupleRegs,
        PlanIter input,
        FieldDefImpl inputType,
        int[] sortFieldPositions,
        SortSpec[] sortSpecs,
        DistributionKind distrKind,
        PrimaryKeyImpl primKey,
        PlanIter[] pushedExternals,
        int numRegs,
        int numIters) {

        super(e, resultReg);

        theInputIter = input;
        theInputType = inputType;
        theSortFieldPositions = sortFieldPositions;
        theSortSpecs = sortSpecs;
        theTupleRegs = tupleRegs;

        theDistributionKind = distrKind;

        if (primKey != null) {
            thePrimaryKey = primKey.getDefinition().createRecord();
            thePrimaryKey.copyFrom(primKey);
            theTableName = primKey.getTable().getFullName();
        } else {
            thePrimaryKey = null;
            theTableName = null;
        }

        thePushedExternals = pushedExternals;

        theNumRegs = numRegs;
        theNumIters = numIters;
    }

    /**
     * FastExternalizable constructor.
     */
    public ReceiveIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);

        theNumRegs = in.readInt();
        theNumIters = in.readInt();

        theInputIter = deserializeIter(in, serialVersion);
        theInputType = (FieldDefImpl) deserializeFieldDef(in, serialVersion);
        theSortFieldPositions = deserializeIntArray(in);
        theSortSpecs = deserializeSortSpecs(in, serialVersion);
        theTupleRegs = deserializeIntArray(in);

        short ordinal = in.readShort();
        theDistributionKind = DistributionKind.values()[ordinal];
        thePrimaryKey = (RecordValueImpl)deserializeFieldValue(in, serialVersion);
        theTableName = SerializationUtil.readString(in);
        thePushedExternals = deserializeIters(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        super.writeFastExternal(out, serialVersion);

        out.writeInt(theNumRegs);
        out.writeInt(theNumIters);

        serializeIter(theInputIter, out, serialVersion);
        serializeFieldDef(theInputType, out, serialVersion);
        serializeIntArray(theSortFieldPositions, out);
        serializeSortSpecs(theSortSpecs, out, serialVersion);
        serializeIntArray(theTupleRegs, out);

        out.writeShort(theDistributionKind.ordinal());
        serializeFieldValue(thePrimaryKey, out, serialVersion);
        SerializationUtil.writeString(out, theTableName);
        serializeIters(thePushedExternals, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.RECV;
    }

    @Override
    public int[] getTupleRegs() {
        return theInputIter.getTupleRegs();
    }

    /**
     * This method executes a query on the server side and stores in the
     * iterator state a ParalleScanIterator over the results.
     *
     * At some point a refactor of how parallel scan and index scan work may
     * be necessary to take into consideration these facts:
     *  o a query may be an update or read-only (this can probably be known
     *  ahead of time once the query is prepared). In any case the type of
     *  query and Durability specified will affect routing of the query.
     *  o some iterator params are not relevant (direction, keys, ranges, Depth)
     */
    private void ensureIterator(
        RuntimeControlBlock rcb,
        ReceiveIterState state) {

        if (state.theRemoteResultsIter != null) {
            return;
        }

        KVStoreImpl store = rcb.getStore();
        ExecuteOptions options = rcb.getExecuteOptions();

        final int batchSize = (options != null ?
                               options.getResultsBatchSize() :
                               store.getDefaultChunkSize());

        switch (theDistributionKind) {
        case SINGLE_PARTITION:
            state.theRemoteResultsIter =
                runOnOnePartition(store, rcb, batchSize);
            break;
        case ALL_PARTITIONS:
            state.theRemoteResultsIter =
                runOnAllPartitions(store, rcb, batchSize);
            break;
        case ALL_SHARDS:
            state.theRemoteResultsIter =
                runOnAllShards(store, rcb, batchSize);
            break;
        default:
            throw new QueryStateException(
                "Unknown distribution kind: " + theDistributionKind);
        }

        rcb.setTableIterator(state.theRemoteResultsIter);
    }

    /**
     * Execute the child plan of this ReceiveIter on all partitions
     */
    private ParallelScanIterator<FieldValueImpl> runOnAllPartitions(
        final KVStoreImpl store,
        final RuntimeControlBlock rcb,
        final int batchSize) {

        ExecuteOptions options = rcb.getExecuteOptions();
        StoreIteratorConfig config = new StoreIteratorConfig();

        if (options != null) {
            config.setMaxConcurrentRequests(options.getMaxConcurrentRequests());
        }

        /*
         * Compute the direction to be stored in the BaseParallelScanIterator.
         * Because the actual comparisons among the query results are done by
         * the streams, the BaseParallelScanIterator just needs to know whether
         * sorting is needed or not in order to invoke the comparison method or
         * not. So, we just need to pass UNORDERED or FORWARD.
         */
        Direction dir = (theSortFieldPositions != null ?
                         Direction.FORWARD :
                         Direction.UNORDERED);

        StoreIteratorParams params =
            new StoreIteratorParams(
                dir,
                batchSize,
                null, // key bytes
                null, // key range
                Depth.PARENT_AND_DESCENDANTS,
                rcb.getConsistency(),
                rcb.getTimeout(),
                rcb.getTimeUnit(),
                null); //partitions

        return new ParallelScanIteratorImpl<FieldValueImpl>(
            store, config, params) {

            @Override
            protected QueryPartitionStream createStream(
                RepGroupId groupId,
                int partitionId) {
                return new QueryPartitionStream(groupId, partitionId, null);
            }

            @Override
            protected TableQuery generateGetterOp(byte[] resumeKey) {

                return new TableQuery(theInputIter, theInputType,
                                      DistributionKind.ALL_PARTITIONS,
                                      rcb.getExternalVars(),
                                      theNumIters, theNumRegs,
                                      batchSize, resumeKey,
                                      null); // resumeSecondaryKey
            }

            @Override
            protected void convertResult(
                Result result,
                List<FieldValueImpl> elementList) {

                List<FieldValueImpl> queryResults = result.getQueryResults();

                // TODO: try to avoid this useless loop
                for (FieldValueImpl res : queryResults) {
                    elementList.add(res);
                }
            }

            @Override
            protected int compare(FieldValueImpl one, FieldValueImpl two) {
                throw new QueryStateException("Unexpected call");
            }

            class QueryPartitionStream extends PartitionStream {

                QueryPartitionStream(
                    RepGroupId groupId,
                    int partitionId,
                    byte[] resumePrimaryKey) {

                    super(groupId, partitionId, resumePrimaryKey);
                }

                @Override
                protected int compareInternal(Stream o) {

                    QueryPartitionStream other = (QueryPartitionStream)o;

                    RecordValueImpl rec1 = (RecordValueImpl)
                        currentResultSet.getQueryResults().
                        get(currentResultPos);

                    RecordValueImpl rec2 = (RecordValueImpl)
                        other.currentResultSet.getQueryResults().
                        get(other.currentResultPos);

                    int cmp = compareStreams(rec1, rec2);

                    if (cmp == 0) {
                        return (partitionId < other.partitionId ? -1 : 1);
                    }

                    return cmp;
                }
            }
        };
    }

    /**
     * Execute the child plan of this ReceiveIter on a single partition
     */
    private ParallelScanIterator<FieldValueImpl> runOnOnePartition(
        final KVStoreImpl store,
        final RuntimeControlBlock rcb,
        final int batchSize) {

        final Consistency consistency = rcb.getConsistency();
        final long timeout = rcb.getTimeout();
        final TimeUnit timeUnit = rcb.getTimeUnit();

        ReceiveIterState state = (ReceiveIterState)rcb.getState(theStatePos);
        final PartitionId pid = state.thePartitionId;

        return new ParallelScanIterator<FieldValueImpl>() {

            private List<FieldValueImpl> theResults = null;

            private Iterator<FieldValueImpl> theResultsIter = null;

            private byte[] theResumeKey = null;

            private boolean theMoreRemoteResults = true;

            @Override
            public boolean hasNext() {

                if (theResultsIter != null && theResultsIter.hasNext()) {
                    return true;
                }

                theResultsIter = null;

                if (!theMoreRemoteResults) {
                    return false;
                }

                TableQuery op = new TableQuery(
                    theInputIter,
                    theInputType,
                    DistributionKind.SINGLE_PARTITION,
                    rcb.getExternalVars(),
                    theNumIters,
                    theNumRegs,
                    batchSize,
                    theResumeKey,
                    null); // resumeSecondaryKey

                Request req = store.makeReadRequest(op, pid,
                                                    consistency, timeout,
                                                    timeUnit);

                Result result = store.executeRequest(req);

                theResults = result.getQueryResults();

                theMoreRemoteResults = result.hasMoreElements();

                if (theResults.isEmpty()) {
                    assert(!theMoreRemoteResults);
                    return false;
                }

                theResumeKey = result.getPrimaryResumeKey();
                theResultsIter = theResults.iterator();
                return true;
            }

            @Override
            public FieldValueImpl next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return theResultsIter.next();
            }

            @Override
            public void close() {
                theResultsIter = null;
                theResults = null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<DetailedMetrics> getPartitionMetrics() {
                return Collections.emptyList();
            }

            @Override
            public List<DetailedMetrics> getShardMetrics() {
                return Collections.emptyList();
            }
        };
    }

    /**
     * Execute the child plan of this ReceiveIter on all shards
     * TODO: remove duplicates in result
     */
    private ParallelScanIterator<FieldValueImpl> runOnAllShards(
        final KVStoreImpl store,
        final RuntimeControlBlock rcb,
        @SuppressWarnings("unused")
        final int batchSize) {

        ExecuteOptions options = rcb.getExecuteOptions();

        /*
         * Compute the direction to be stored in the BaseParallelScanIterator.
         * Because the actual comparisons among the query results are done by
         * the streams, the BaseParallelScanIterator just needs to know whether
         * sorting is needed or not in order to invoke the comparison method or
         * not. So, we just need to pass UNORDERED or FORWARD.
         */
        Direction dir = (theSortFieldPositions != null ?
                         Direction.FORWARD :
                         Direction.UNORDERED);

        TableIteratorOptions opts =
            (options != null ?
             options.createTableIteratorOptions(dir) :
             new TableIteratorOptions(dir, null, 0, null));

        return new IndexScanIterator<FieldValueImpl>(store, opts, null) {

            @Override
            protected QueryShardStream createStream(RepGroupId groupId) {
                return new QueryShardStream(groupId, null, null);
            }

            @Override
            protected TableQuery createOp(
                byte[] resumeSecondaryKey,
                byte[] resumePrimaryKey) {

                return new TableQuery(theInputIter, theInputType,
                                      DistributionKind.ALL_SHARDS,
                                      rcb.getExternalVars(),
                                      theNumIters, theNumRegs,
                                      batchSize, resumePrimaryKey,
                                      resumeSecondaryKey);
            }

            @Override
            protected void convertResult(
                Result result,
                List<FieldValueImpl> elementList) {

                List<FieldValueImpl> queryResults = result.getQueryResults();
                for (FieldValueImpl res : queryResults) {
                    elementList.add(res);
                }
            }

            @Override
            protected int compare(FieldValueImpl one, FieldValueImpl two) {
                throw new QueryStateException("Unexpected call");
            }

            class QueryShardStream extends ShardIndexStream {

                QueryShardStream(
                    RepGroupId groupId,
                    byte[] resumeSecondaryKey,
                    byte[] resumePrimaryKey) {

                    super(groupId, resumeSecondaryKey, resumePrimaryKey);
                }

                @Override
                protected int compareInternal(Stream o) {

                    QueryShardStream other = (QueryShardStream)o;

                    RecordValueImpl rec1 = (RecordValueImpl)
                        currentResultSet.getQueryResults().
                        get(currentResultPos);

                    RecordValueImpl rec2 = (RecordValueImpl)
                        other.currentResultSet.getQueryResults().
                        get(other.currentResultPos);

                    int cmp = compareStreams(rec1, rec2);

                    if (cmp == 0) {
                        return getGroupId().compareTo(other.getGroupId());
                    }

                    return cmp;
                }
            }
        };
    }

    @Override
    public void open(RuntimeControlBlock rcb) {

        boolean runOnClient = false;
        String onClient = System.getProperty("test.queryonclient");

        if (onClient != null && !onClient.isEmpty()) {
            runOnClient = true;
            theInputIter.open(rcb);
        }

        PartitionId pid = PartitionId.NULL_ID;

        if (theDistributionKind == DistributionKind.SINGLE_PARTITION) {

            TableImpl table = rcb.getTableMetadata().getTable(theTableName);
            if (table == null) {
                throw new QueryException(
                    "Table does not exist: " + theTableName, getLocation());
            }

            PrimaryKeyImpl primaryKey = table.createPrimaryKey(thePrimaryKey);

            if (thePushedExternals != null &&
                thePushedExternals.length > 0) {

                int size = thePushedExternals.length;

                for (int i = 0; i < size; ++i) {

                    PlanIter iter = thePushedExternals[i];

                    if (iter == null) {
                        continue;
                    }

                    iter.open(rcb);
                    iter.next(rcb);
                    FieldValueImpl val = rcb.getRegVal(iter.getResultReg());
                    iter.close(rcb);

                    String colName = table.getPrimaryKeyColumnName(i);
                    primaryKey.put(colName, val);
                }

                pid = primaryKey.getPartitionId(rcb.getStore());

            } else {
                pid = primaryKey.getPartitionId(rcb.getStore());
            }
        }

        ReceiveIterState state = new ReceiveIterState(pid, runOnClient);

        rcb.setState(theStatePos, state);

        if (theTupleRegs != null) {
            TupleValue tuple = new TupleValue((RecordDefImpl)theInputType,
                                              rcb.getRegisters(),
                                              theTupleRegs);
            rcb.setRegVal(theResultReg, tuple);
        }
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        /*
         * Catch StoreIteratorException and if the cause is a QueryException,
         * throw that instead to provide more information to the caller.
         */
        try {
            ReceiveIterState state =
                (ReceiveIterState)rcb.getState(theStatePos);

            if (state.isDone()) {
                return false;
            }

            if (state.theRunOnClient) {
                return theInputIter.next(rcb);
            }

            ensureIterator(rcb, state);

            boolean more = state.theRemoteResultsIter.hasNext();

            if (!more) {
                state.done();
                return false;
            }

            FieldValueImpl res = state.theRemoteResultsIter.next();

            if (theTupleRegs != null) {
                TupleValue tuple = (TupleValue)rcb.getRegVal(theResultReg);
                tuple.toTuple((RecordValueImpl)res);
            } else {
                rcb.setRegVal(theResultReg, res);
            }

            return true;
        } catch (StoreIteratorException sie) {
            if (sie.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) sie.getCause();
            }
            if (sie.getCause() instanceof QueryException) {
                throw (QueryException) sie.getCause();
            }
            if (sie.getCause() instanceof QueryStateException) {
                throw (QueryStateException) sie.getCause();
            }
            throw sie;
        }
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {
        ReceiveIterState state = (ReceiveIterState)rcb.getState(theStatePos);
        state.reset(this);
        if (state.theRunOnClient) {
            theInputIter.reset(rcb);
        }
    }

    @Override
    public void close(RuntimeControlBlock rcb) {

        ReceiveIterState state = (ReceiveIterState)rcb.getState(theStatePos);
        if (state == null) {
            return;
        }

        state.close();
        if (state.theRunOnClient) {
            theInputIter.close(rcb);
        }
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {

        if (theSortFieldPositions != null) {
            formatter.indent(sb);
            sb.append("Sort Field Positions : ");
            for (int i = 0; i < theSortFieldPositions.length; ++i) {
                sb.append(theSortFieldPositions[i]);
                if (i < theSortFieldPositions.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(",\n");
        }

        formatter.indent(sb);
        sb.append("DistributionKind : ").append(theDistributionKind);
        sb.append(",\n");

        if (thePrimaryKey != null && thePrimaryKey.size() > 0) {
            formatter.indent(sb);
            sb.append("Primary Key :").append(thePrimaryKey);
            sb.append(",\n");
        }

        if (thePushedExternals != null) {
            sb.append("\n");
            formatter.indent(sb);
            sb.append("EXTERNAL KEY EXPRS: " + thePushedExternals.length);

            for (PlanIter iter : thePushedExternals) {

                sb.append("\n");
                if (iter != null) {
                    iter.display(sb, formatter);
                } else {
                    formatter.indent(sb);
                    sb.append("null");
                }
            }
            sb.append(",\n\n");
        }

        formatter.indent(sb);
        sb.append("Number of Registers :").append(theNumRegs);
        sb.append(",\n");
        formatter.indent(sb);
        sb.append("Number of Iterators :").append(theNumIters);
        sb.append(",\n");
        theInputIter.display(sb, formatter);
    }

    int compareStreams(RecordValueImpl rec1, RecordValueImpl rec2) {

        assert(rec1.getDefinition().equals(rec2.getDefinition()));

        for (int i = 0; i < theSortFieldPositions.length; ++i) {
            int pos = theSortFieldPositions[i];
            String fname = rec1.getDefinition().getFieldName(pos);
            FieldValueImpl val1 = rec1.getFieldValue(fname);
            FieldValueImpl val2 = rec2.getFieldValue(fname);

            if (val1 == null || val2 == null) {
                throw new QueryStateException("Unexpected null value");
            }

            int comp;

            if (val1.isNull()) {
                if (val2.isNull()) {
                    comp = 0;
                } else {
                    comp = (theSortSpecs[i].theNullsFirst ? -1 : 1);
                }
            } else if (val2.isNull()) {
                comp = (theSortSpecs[i].theNullsFirst ? 1 : -1);
            } else {
                comp = val1.compareTo(val2);
            }

            if (comp != 0) {
                return (theSortSpecs[i].theIsDesc ? -comp : comp);
            }
        }
        /* they must be equal */
        return 0;
    }
}
