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

import java.util.concurrent.TimeUnit;

import oracle.kv.Consistency;
import oracle.kv.ParallelScanIterator;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.query.ExecuteOptions;
import oracle.kv.table.FieldValue;

/**
 *
 */
public class RuntimeControlBlock {

    /*
     * The state array contains as many elements as there are PlanIter
     * instances in the query plan to be executed.
     */
    final private PlanIterState[] theIteratorStates;

    /*
     * The register array contains as many elements as required by the
     * instances in the query plan to be executed.
     */
    final private FieldValueImpl[] theRegisters;

    /*
     * TableMetadata is required by some operations to resolve table and index
     * names.
     */
    final private TableMetadata theTableMetadata;

    /*
     * ExecuteOptions are options set by the application and used to control
     * some aspects of database access, such as Consistency, timeouts, batch
     * sizes, etc.
     */
    final private ExecuteOptions theExecuteOptions;

    /*
     * An array storing the values of the extenrnal variables set for the
     * operation. These come from the map in the BoundStatement.
     */
    final FieldValue[] theExternalVars;

    /*
     * TableIterFactory is used to construct a PlanIter instances that knows
     * how to iterate tables. The factory instances are different depending
     * on whether the query is being run on the client or the server side.
     */
    final private TableIterFactory theTableIterFactory;

    /*
     * KVStoreImpl is set on the client side and is used by the dispatch
     * code that sends queries to the server side.
     */
    final private KVStoreImpl theStore;

    /*
     * Resume keys are in/out arguments. They are used as input to resume
     * iteration and are used by server side iteration methods. They are set as
     * output by the same iterators to save the last visited key in the table
     * or index that is being iterated. When there are no further results they
     * will be null on output. There are two keys to handle index iteration
     * which may end a chunk in a sequence of duplicate secondary keys, so it
     * needs the current primary key to accurately resume iteration.
     */
    private byte[] thePrimaryResumeKey;

    private byte[] theSecondaryResumeKey;

    /*
     * The RCB holds the TableIterator for the current remote call from the
     * ReceiveIter if there is one. This is here so that the query results
     * objects can return partition and shard metrics for the distributed
     * query operation.
     */
    private ParallelScanIterator<FieldValueImpl> theTableIterator;

    public RuntimeControlBlock(
        int numIters,
        int numRegs,
        TableMetadata tableMetadata,
        ExecuteOptions executeOptions,
        FieldValue[] externalVars,
        byte[] primaryResumeKey,
        byte[] secondaryResumeKey,
        TableIterFactory tableIterFactory,
        KVStoreImpl store) {

        theIteratorStates = new PlanIterState[numIters];
        theRegisters = new FieldValueImpl[numRegs];
        theTableMetadata = tableMetadata;
        theExecuteOptions = executeOptions;
        theExternalVars = externalVars;
        thePrimaryResumeKey = primaryResumeKey;
        theSecondaryResumeKey = secondaryResumeKey;
        theTableIterFactory = tableIterFactory;
        theStore = store;
    }

    public TableMetadata getTableMetadata() {
        return theTableMetadata;
    }

    public ExecuteOptions getExecuteOptions() {
        return theExecuteOptions;
    }

    Consistency getConsistency() {
        return (theExecuteOptions != null ?
                theExecuteOptions.getConsistency() :
                null);
    }

    long getTimeout() {
        return (theExecuteOptions != null ?
                theExecuteOptions.getTimeout() :
                0);
    }

    TimeUnit getTimeUnit() {
        return (theExecuteOptions != null ?
                theExecuteOptions.getTimeoutUnit() :
                null);
    }

    public void setState(int pos, PlanIterState state) {
        theIteratorStates[pos] = state;
    }

    public PlanIterState getState(int pos) {
        return theIteratorStates[pos];
    }

    public FieldValueImpl[] getRegisters() {
        return theRegisters;
    }

    public FieldValueImpl getRegVal(int regId) {
        return theRegisters[regId];
    }

    public void setRegVal(int regId, FieldValueImpl value) {
        theRegisters[regId] = value;
    }

    FieldValue[] getExternalVars() {
        return theExternalVars;
    }

    FieldValueImpl getExternalVar(int id) {

        if (theExternalVars == null) {
            return null;
        }
        return (FieldValueImpl)theExternalVars[id];
    }

    protected void setTableIterator(ParallelScanIterator<FieldValueImpl> iter) {
        theTableIterator = iter;
    }

    public ParallelScanIterator<FieldValueImpl> getTableIterator() {
        return theTableIterator;
    }

    public byte[] getPrimaryResumeKey() {
        return thePrimaryResumeKey;
    }

    public void setPrimaryResumeKey(byte[] resumeKey) {
        thePrimaryResumeKey = resumeKey;
    }

    public byte[] getSecondaryResumeKey() {
        return theSecondaryResumeKey;
    }

    public void setSecondaryResumeKey(byte[] resumeKey) {
        theSecondaryResumeKey = resumeKey;
    }

    public KVStoreImpl getStore() {
        return theStore;
    }

    TableIterFactory getTableIterFactory() {
        return theTableIterFactory;
    }
}
