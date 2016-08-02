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

package oracle.kv.impl.api.ops;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldDefSerialization;
import oracle.kv.impl.api.table.FieldValueSerialization;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.runtime.ReceiveIter.DistributionKind;
import oracle.kv.table.FieldValue;

/**
 * TableQuery represents and drives the execution of a query subplan at a
 * server site, over one partition or one shard.
 *
 * Instances of TableQuery are created via the parallel-scan infrastructure,
 * when invoked from the open() method of ReceiveIters in the query plan.
 *
 * This class contains the result schema (resultDef) so it can be passed
 * to the result class (Result.QueryResult) and serialized only once for each
 * batch of results. If this were not done, each RecordValue result would have
 * its associated RecordDef serialized (redundantly) along with it.
 */
public class TableQuery extends InternalOperation {

    private static final short queryPlanVersion = 1;

    private final PlanIter queryPlan;

    private final FieldDefImpl resultDef;

    /*
     * Optional Bind Variables. If none exist or are not set this is null.
     * If it would be easier for callers this could be made an empty Map.
     */
    private final FieldValue[] externalVars;

    private final int numIterators;

    private final int numRegisters;

    private final int batchSize;

    private byte[] primaryResumeKey;

    private byte[] secondaryResumeKey;

    public TableQuery(PlanIter queryPlan,
                      FieldDefImpl resultDef,
                      DistributionKind distKind,
                      FieldValue[] externalVars,
                      int numIterators,
                      int numRegisters,
                      int batchSize,
                      byte[] primaryResumeKey,
                      byte[] secondaryResumeKey) {

        /*
         * The distinct OpCodes are primarily for a finer granularity of
         * statistics, allowing the different types of queries to be tallied
         * independently.
         */
        super(distKind == DistributionKind.ALL_PARTITIONS ?
              OpCode.QUERY_MULTI_PARTITION :
              (distKind == DistributionKind.ALL_SHARDS ?
               OpCode.QUERY_MULTI_SHARD :
               OpCode.QUERY_SINGLE_PARTITION));
        this.queryPlan = queryPlan;
        this.resultDef = resultDef;
        this.externalVars = externalVars;
        this.numIterators = numIterators;
        this.numRegisters = numRegisters;
        this.primaryResumeKey = primaryResumeKey;
        this.secondaryResumeKey = secondaryResumeKey;
        this.batchSize = batchSize;
    }

    PlanIter getQueryPlan() {
        return queryPlan;
    }

    FieldDefImpl getResultDef() {
        return resultDef;
    }

    FieldValue[] getExternalVars() {
        return externalVars;
    }

    int getNumIterators() {
        return numIterators;
    }

    int getNumRegisters() {
        return numRegisters;
    }

    int getBatchSize() {
        return batchSize;
    }

    byte[] getPrimaryResumeKey() {
        return primaryResumeKey;
    }

    byte[] getSecondaryResumeKey() {
        return secondaryResumeKey;
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);

        writeQueryPlan(out);
        writeResultDef(resultDef, out, serialVersion);
        writeExternalVars(externalVars, out, serialVersion);
        out.writeInt(numIterators);
        out.writeInt(numRegisters);
        out.writeInt(batchSize);
        if (primaryResumeKey == null) {
            out.writeShort(-1);
        } else {
            out.writeShort(primaryResumeKey.length);
            out.write(primaryResumeKey);
        }
        if (secondaryResumeKey == null) {
            out.writeShort(-1);
        } else {
            out.writeShort(secondaryResumeKey.length);
            out.write(secondaryResumeKey);
        }
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    protected TableQuery(OpCode opCode, DataInput in, short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);

        queryPlan = readQueryPlan(in);
        resultDef = readResultDef(in, serialVersion);
        externalVars = readExternalVars(in, serialVersion);
        numIterators = in.readInt();
        numRegisters = in.readInt();
        batchSize = in.readInt();
        int keyLen = in.readShort();
        if (keyLen < 0) {
            primaryResumeKey = null;
        } else {
            primaryResumeKey = new byte[keyLen];
            in.readFully(primaryResumeKey);
        }
        keyLen = in.readShort();
        if (keyLen < 0) {
            secondaryResumeKey = null;
        } else {
            secondaryResumeKey = new byte[keyLen];
            in.readFully(secondaryResumeKey);
        }
    }

    /**
     * Put the serialized query plan in the stream.
     */
    private void writeQueryPlan(DataOutput out)
        throws IOException {

        out.writeShort(queryPlanVersion);
        PlanIter.serializeIter(queryPlan, out, queryPlanVersion);
    }

    /**
     * Read the serialized query plan from the stream.
     */
    private PlanIter readQueryPlan(DataInput in)
        throws IOException {

        short qpVersion = in.readShort();
        if (qpVersion == queryPlanVersion) {
            return PlanIter.deserializeIter(in, qpVersion);
        }
        throw new IllegalStateException("Unknown query plan version");
    }

    static void writeResultDef(
        FieldDefImpl resultDef,
        DataOutput out,
        short serialVersion)
        throws IOException {

        FieldDefSerialization.writeFieldDef(resultDef, out, serialVersion);
    }

    static FieldDefImpl readResultDef(DataInput in, short serialVersion)
        throws IOException {

        return FieldDefSerialization.readFieldDef(in, serialVersion);
    }

    static void writeExternalVars(
        FieldValue[] vars,
        DataOutput out,
        short serialVersion)
        throws IOException {

        if (vars != null && vars.length > 0) {
            int numVars = vars.length;
            out.writeInt(numVars);
            for (int i = 0; i < numVars; ++i) {
                FieldValueSerialization.writeFieldValue(
                    vars[i], true, out, serialVersion);
            }
        } else {
            out.writeInt(0);
        }
    }

    static FieldValue[] readExternalVars(DataInput in, short serialVersion)
        throws IOException {

        int numVars = in.readInt();
        if (numVars == 0) {
            return null;
        }

        FieldValue[] vars = new FieldValue[numVars];

        for (int i = 0; i < numVars; i++) {
            FieldValue val = FieldValueSerialization.readFieldValue(
                null, in, serialVersion);

            vars[i] = val;
        }
        return vars;
    }
}
