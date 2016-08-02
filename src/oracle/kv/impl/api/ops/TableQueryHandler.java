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

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Transaction;

import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.api.table.TupleValue;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.runtime.RuntimeControlBlock;
import oracle.kv.impl.query.runtime.server.ServerTableIter.ServerTableIterFactory;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.query.ExecuteOptions;

/**
 * Server handler for {@link TableQuery}.
 */
class TableQueryHandler extends InternalOperationHandler<TableQuery> {

    TableQueryHandler(OperationHandler handler, OpCode opCode) {
        super(handler, opCode, TableQuery.class);
    }

    @Override
    List<? extends KVStorePrivilege> getRequiredPrivileges(TableQuery op) {
        /*
         * Checks the basic privilege for authentication here, and leave the
         * keyspace checking and the table access checking in
         * {@code verifyTableAccess()}.
         */
        return SystemPrivilege.usrviewPrivList;
    }

    @Override
    Result execute(TableQuery op,
                   Transaction txn,
                   PartitionId partitionId) {

        final List<FieldValueImpl> results = new ArrayList<FieldValueImpl>();

        TableMetadata md = getMetadata();

        RuntimeControlBlock rcb = new RuntimeControlBlock(
            op.getNumIterators(),
            op.getNumRegisters(),
            md,
            new ExecuteOptions().setResultsBatchSize(op.getBatchSize()),
            op.getExternalVars(),
            op.getPrimaryResumeKey(),
            op.getSecondaryResumeKey(),
            new ServerTableIterFactory(txn, partitionId, operationHandler),
            null); /* KVStoreImpl not needed and not available */

        executeQueryPlan(op, rcb, results);

        /*
         * Resume key is both input and output parameter for RCB. If set on
         * output there are more keys to be found in this iteration.
         */
        byte[] newPrimaryResumeKey = rcb.getPrimaryResumeKey();
        byte[] newSecondaryResumeKey = rcb.getSecondaryResumeKey();
        boolean more = (newPrimaryResumeKey != null ||
                        newSecondaryResumeKey != null);

        return new Result.QueryResult(getOpCode(),
                                      results,
                                      op.getResultDef(),
                                      more,
                                      newPrimaryResumeKey,
                                      newSecondaryResumeKey);
    }

    /**
     * Returns a TableMetadata instance available on this node.
     */
    private TableMetadata getMetadata() {
        return (TableMetadata) getRepNode().getMetadata(MetadataType.TABLE);
    }

    private void executeQueryPlan(
        TableQuery op,
        RuntimeControlBlock rcb,
        List<FieldValueImpl> results) {

        final PlanIter queryPlan = op.getQueryPlan();
        try {
            queryPlan.open(rcb);

            while (queryPlan.next(rcb)) {
                FieldValueImpl res = rcb.getRegVal(queryPlan.getResultReg());

                if (res.isTuple()) {
                    res = ((TupleValue)res).toRecord();
                }

                results.add(res);
            }
        } catch (QueryException qe) {

            /*
             * For debugging and tracing this can temporarily use level INFO
             */
            getLogger().fine("Query execution failed: " + qe);

            /*
             * Turn this into a wrapped IllegalArgumentException so that it can
             * be passed to the client.
             */
            throw qe.getWrappedIllegalArgument();
        }  catch (QueryStateException qse) {

            /*
             * This exception indicates a bug or problem in the engine. Log
             * it. It will be thrown through to the client side.
             */
            getLogger().warning(qse.toString());

            /*
             * Wrap this exception into one that can be thrown to the client.
             */
            qse.throwClientException();
        } finally {
            queryPlan.close(rcb);
        }
    }
}
