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

import java.util.HashMap;
import java.util.Map;

import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.query.compiler.QueryControlBlock;
import oracle.kv.impl.query.compiler.StaticContext.VarInfo;
import oracle.kv.impl.query.runtime.PlanIter;

import oracle.kv.StatementResult;
import oracle.kv.query.BoundStatement;
import oracle.kv.query.PreparedStatement;
import oracle.kv.query.ExecuteOptions;
import oracle.kv.table.RecordDef;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;

/**
 * Implementation of a PreparedStatement. This class contains the query plan,
 * along with enough information to construct a runtime context in which the
 * query can be executed (RuntimeControlBlock).
 *
 * An instance of PreparedStatementImpl is created by CompilerAPI.prepare(),
 * after the query has been compiled.
 */
public class PreparedStatementImpl
    implements PreparedStatement,
               InternalStatement {

    /*
     * The query plan
     */
    private final PlanIter queryPlan;

    /*
     * The type of the result
     */
    private final RecordDef resultDef;

    /*
     * The number of registers required to run the plan
     */
    private final int numRegisters;

    /*
     * The number of iterators in the plan
     */
    private final int numIterators;

    /*
     * externalVars maps the name of each external var declared in the query
     * to the numeric id and data type for that var.
     */
    private final Map<String, VarInfo> externalVars;

    /*
     * Needed for unit testing only
     */
    private QueryControlBlock qcb;

    public PreparedStatementImpl(
        PlanIter queryPlan,
        RecordDef resultDef,
        int numRegisters,
        int numIterators,
        Map<String, VarInfo> externalVars,
        QueryControlBlock qcb) {

        this.queryPlan = queryPlan;
        this.resultDef = resultDef;
        this.numRegisters = numRegisters;
        this.numIterators = numIterators;
        this.externalVars = externalVars;
        this.qcb = qcb;
    }

    @Override
    public RecordDef getResultDef() {
        return resultDef;
    }

    @Override
    public Map<String, FieldDef> getVariableTypes() {
        return getExternalVarsTypes();
    }

    @Override
    public FieldDef getVariableType(String variableName) {
        return getExternalVarType(variableName);
    }

    @Override
    public BoundStatement createBoundStatement() {
        return new BoundStatementImpl(this);
    }

    /*
     * Needed for unit testing only
     */
    public QueryControlBlock getQCB() {
        return qcb;
    }

    public PlanIter getQueryPlan() {
    	return queryPlan;
    }

    public int getNumRegisters() {
        return numRegisters;
    }

    public int getNumIterators() {
        return numIterators;
    }

    public Map<String, FieldDef> getExternalVarsTypes() {

        Map<String, FieldDef> varsMap = new HashMap<String, FieldDef>();

        if (externalVars == null) {
            return varsMap;
        }

        for (Map.Entry<String, VarInfo> entry : externalVars.entrySet()) {
            String varName = entry.getKey();
            VarInfo vi = entry.getValue();
            varsMap.put(varName, vi.getType().getDef());
        }

        return varsMap;
    }

    boolean hasExternalVars() {
        return (externalVars != null && !externalVars.isEmpty());
    }

    public FieldDef getExternalVarType(String name) {

        if (externalVars == null) {
            return null;
        }

        VarInfo vi = externalVars.get(name);
        if (vi != null) {
            return vi.getType().getDef();
        }

        return null;
    }

    /**
     * Convert the map of external vars (maping names to values) to an array
     * with the values only. The array is indexed by an internalid assigned
     * to each external variable. This method also checks that all the external
     * vars declared in the query have been bound.
     */
    public FieldValue[] getExternalVarsArray(Map<String, FieldValue> vars) {

        if (externalVars == null) {
            assert(vars.isEmpty());
            return null;
        }

        int count = 0;
        for (Map.Entry<String, VarInfo> entry : externalVars.entrySet()) {
            String name = entry.getKey();
            ++count;

            if (vars.get(name) == null) {
                throw new IllegalArgumentException(
                    "Variable " + name + " has not been bound");
            }
        }

        FieldValue[] array = new FieldValue[count];

        count = 0;
        for (Map.Entry<String, FieldValue> entry : vars.entrySet()) {
            String name = entry.getKey();
            FieldValue value = entry.getValue();

            VarInfo vi = externalVars.get(name);
            if (vi == null) {
                throw new IllegalStateException(
                    "Variable " + name + " does not appear in query");
            }

            array[vi.getId()] = value;
            ++count;
        }

        assert(count == array.length);
        return array;
    }

    @Override
    public String toString() {
        return queryPlan.display();
    }

    @Override
    public StatementResult executeSync(
        KVStoreImpl store,
        ExecuteOptions options) {

        return new QueryStatementResultImpl(
            store.getTableAPIImpl(), options, this);
    }
}
