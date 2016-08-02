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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.NullValueImpl;
import oracle.kv.impl.api.table.MapValueImpl;
import oracle.kv.impl.api.table.RecordValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.QueryFormatter;

/**
 *
 */
public class FuncKeysIter extends PlanIter {

    static private class KeysState extends PlanIterState {

        Collection<String> theKeys;

        Iterator<String> theKeysIter;

        KeysState() {
            super();
        }

        @Override
        protected void reset(PlanIter iter) {
            super.reset(iter);
            theKeys = null;
            theKeysIter = null;
        }

        @Override
        protected void close() {
            super.close();
            theKeys = null;
            theKeysIter = null;
        }
    }

    private final PlanIter theInput;

    public FuncKeysIter(Expr e, int resultReg, PlanIter input) {
        super(e, resultReg);
        theInput = input;
    }

    /**
     * FastExternalizable constructor.
     */
    FuncKeysIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);
        theInput = deserializeIter(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        super.writeFastExternal(out, serialVersion);
        serializeIter(theInput, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.FUNC_KEYS;
    }

    @Override
    public void open(RuntimeControlBlock rcb) {
        rcb.setState(theStatePos, new KeysState());
        theInput.open(rcb);
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        KeysState state = (KeysState)rcb.getState(theStatePos);

        if (state.isDone()) {
            return false;
        }

        if (state.theKeys == null) {
            boolean more = theInput.next(rcb);

            if (!more) {
                state.done();
                return false;
            }

            FieldValueImpl item = rcb.getRegVal(theInput.getResultReg());

            if (item.isNull()) {
                rcb.setRegVal(theResultReg, NullValueImpl.getInstance());
                state.done();
                return true;
            }

            if (item.isRecord()) {
                state.theKeys = ((RecordValueImpl)item).getFieldNamesInternal();
                state.theKeysIter = ((List<String>)state.theKeys).iterator();

            } else if (item.isMap()) {
                state.theKeys = ((MapValueImpl)item).getFieldNamesInternal();
                state.theKeysIter = ((Set<String>)state.theKeys).iterator();

            } else {
                throw new QueryException(
                    "Input to the size() function has wrong type\n" +
                    "Expected a record or map item. Actual item type " +
                    " is:\n" + item.getDefinition(), getLocation());
            }
        }

        if (state.theKeysIter.hasNext()) {
            FieldValueImpl res = FieldDefImpl.stringDef.createString(
                state.theKeysIter.next());

            rcb.setRegVal(theResultReg, res);
            return true;
        }

        state.done();
        return false;
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {
        theInput.reset(rcb);
        PlanIterState state = rcb.getState(theStatePos);
        state.reset(this);
    }

    @Override
    public void close(RuntimeControlBlock rcb) {

        PlanIterState state = rcb.getState(theStatePos);
        if (state == null) {
            return;
        }

        theInput.close(rcb);
        state.close();
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {
        theInput.display(sb, formatter);
    }
}
