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

import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.QueryFormatter;

/**
 * Iterator to concatenate a number of input sequences
 *
 * Inputs:
 *   0 or more operand iterators
 *
 * Result:
 *   ANY*
 */
public class ConcatIter extends PlanIter {

    private static class ConcatIterState extends PlanIterState {

        int theCurrentInput = 0;

        @Override
        protected void reset(PlanIter iter) {
            super.reset(iter);
            theCurrentInput = 0;
        }

        @Override
        protected void close() {
            super.close();
            theCurrentInput = 0;
        }
    }

    private final PlanIter[] theArgs;

    public ConcatIter(Expr e, int resultReg, PlanIter[] argIters) {
        super(e, resultReg);
        theArgs = argIters;
    }

    /**
     * FastExternalizable constructor.
     */
    public ConcatIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);
        theArgs = deserializeIters(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        super.writeFastExternal(out, serialVersion);
        serializeIters(theArgs, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.CONCAT;
    }

    @Override
    public void open(RuntimeControlBlock rcb) {
        rcb.setState(theStatePos, new ConcatIterState());
        for (PlanIter arg : theArgs) {
            arg.open(rcb);
        }
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        ConcatIterState state = (ConcatIterState)rcb.getState(theStatePos);

        if (theArgs.length == 0 || state.isDone()) {
            return false;
        }

        do {
            PlanIter input = theArgs[state.theCurrentInput];
            if (input.next(rcb)) {
                rcb.setRegVal(theResultReg,
                              rcb.getRegVal(input.getResultReg()));
                return true;
            }

            ++state.theCurrentInput;

        } while (state.theCurrentInput < theArgs.length);

        state.done();
        return false;
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {

        for (PlanIter arg : theArgs) {
            arg.reset(rcb);
        }
        ConcatIterState state = (ConcatIterState)rcb.getState(theStatePos);
        state.reset(this);
    }

    @Override
    public void close(RuntimeControlBlock rcb) {

        ConcatIterState state = (ConcatIterState)rcb.getState(theStatePos);
        if (state == null) {
            return;
        }

        for (PlanIter arg : theArgs) {
            arg.close(rcb);
        }
        state.close();
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {
        for (int i = 0; i < theArgs.length; ++i) {
            theArgs[i].display(sb, formatter);
            if (i < theArgs.length - 1) {
                sb.append(",\n");
            }
        }
    }
}
