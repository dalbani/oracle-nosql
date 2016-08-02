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

package oracle.kv.impl.api.query;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import oracle.kv.StatementResult;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.RecordValueImpl;
import oracle.kv.impl.api.table.TableAPIImpl;
import oracle.kv.impl.api.table.TupleValue;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.runtime.ClientTableIter.ClientTableIterFactory;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.runtime.RuntimeControlBlock;
import oracle.kv.query.ExecuteOptions;
import oracle.kv.stats.DetailedMetrics;
import oracle.kv.table.FieldValue;
import oracle.kv.table.RecordDef;
import oracle.kv.table.RecordValue;
import oracle.kv.table.TableIterator;


/**
 * Implementation of StatementResult when statement is a query.
 *
 * NOTE: until query work is fully supported StatementResult does not
 * extend Iterable<RecordValue> so it must be added here so that explicit
 * casts in test code can work correctly. Query DML.
 */
public class QueryStatementResultImpl implements StatementResult {

    private QueryResultIterator iterator;
    private boolean closed;

    public QueryStatementResultImpl(
        TableAPIImpl tableAPI,
        ExecuteOptions options,
        PreparedStatementImpl stmt) {

        if (stmt.hasExternalVars()) {
            throw new QueryException(
                "The query contains external variables, none of which " +
                "has been bound. Create a BoundStatement to bind the " +
                "variables");
        }

        init(tableAPI, options, stmt, null);
    }

    public QueryStatementResultImpl(
        TableAPIImpl tableAPI,
        ExecuteOptions options,
        BoundStatementImpl stmt) {

        PreparedStatementImpl ps = stmt.getPreparedStmt();
        FieldValue[] externalVars = ps.getExternalVarsArray(stmt.getVariables());

        init(tableAPI, options, ps, externalVars);
    }

    void init(
        TableAPIImpl tableAPI,
        ExecuteOptions options,
        PreparedStatementImpl ps,
        FieldValue[] externalVars) {

        PlanIter iter = ps.getQueryPlan();
        RecordDef resultDef = ps.getResultDef();

        RuntimeControlBlock rcb = new RuntimeControlBlock(
            ps.getNumIterators(),
            ps.getNumRegisters(),
            tableAPI.getTableMetadata(),
            options, /* ExecuteOptions */
            externalVars,
            null, /* primaryResumeKey */
            null, /* secondaryResumeKey */
            new ClientTableIterFactory(tableAPI),
            tableAPI.getStore());

        this.iterator = new QueryResultIterator(rcb, iter, resultDef);
        closed = false;
    }

    @Override
    public void close() {
        iterator.close();
        closed = true;
    }

    @Override
    public RecordDef getResultDef() {
        if (closed) {
            throw new IllegalStateException("Statement result already closed.");
        }

        return iterator.getResultDef();
    }


    @Override
    public TableIterator<RecordValue> iterator() {
        if (closed) {
            throw new IllegalStateException("Statement result already closed.");
        }

        return iterator;
    }

    @Override
    public int getPlanId() {
        return 0;
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public String getInfoAsJson() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public boolean isDone() {
        return !iterator.hasNext();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public Kind getKind() {
        return Kind.QUERY;
    }

    private class QueryResultIterator implements TableIterator<RecordValue> {

        final private RuntimeControlBlock rcb;
        final private PlanIter rootIter;
        final private RecordDef resultDef;
        private boolean hasNext;

        QueryResultIterator(
            RuntimeControlBlock rcb,
            PlanIter iter,
            RecordDef resultDef) {

            this.rcb = rcb;
            rootIter = iter;
            this.resultDef = resultDef;

            try {
                rootIter.open(rcb);
                hasNext = rootIter.next(rcb);
            } catch (QueryStateException qse) {
                /*
                 * Log the exception if a logger is available.
                 */
                Logger logger = rcb.getStore().getLogger();
                if (logger != null) {
                    logger.warning(qse.toString());
                }
                throw new IllegalStateException(qse.toString());
            } catch (QueryException qe) {
                /* A QueryException thrown at the client; rethrow as IAE */
                throw qe.getIllegalArgument();
            }
        }

        RecordDef getResultDef() {
            return resultDef;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public RecordValue next() {

            if (!hasNext) {
                throw new NoSuchElementException();
            }

            RecordValueImpl record = null;
            try {
                FieldValueImpl resVal = rcb.getRegVal(rootIter.getResultReg());

                if (resVal.isTuple()) {
                    assert(resultDef.equals(resVal.getDefinition()));
                    record = ((TupleValue)resVal).toRecord();
                } else {
                    record = (RecordValueImpl)resVal;
                }
                hasNext = rootIter.next(rcb);
            } catch (QueryStateException qse) {
                /*
                 * Log the exception if a logger is available.
                 */
                Logger logger = rcb.getStore().getLogger();
                if (logger != null) {
                    logger.warning(qse.toString());
                }
                throw new IllegalStateException(qse.toString());
            } catch (QueryException qe) {
                /* A QueryException thrown at the client; rethrow as IAE */
                throw qe.getIllegalArgument();
            }

            return record;
        }

        @Override
        public void close() {
            if (!isClosed()) {
                rootIter.close(rcb);
                hasNext = false;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DetailedMetrics> getPartitionMetrics() {
            if (rcb.getTableIterator() != null) {
                return rcb.getTableIterator().getShardMetrics();
            }
            return Collections.emptyList();
        }

        @Override
        public List<DetailedMetrics> getShardMetrics() {
            if (rcb.getTableIterator() != null) {
                return rcb.getTableIterator().getShardMetrics();
            }
            return Collections.emptyList();
        }

        public boolean isClosed() {
            return rootIter.isClosed(rcb);
        }
    }
}
