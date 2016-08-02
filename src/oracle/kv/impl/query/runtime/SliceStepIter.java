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

import oracle.kv.impl.api.table.ArrayValueImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.impl.query.runtime.PlanIterState.StateEnum;

/**
 *
 */
public class SliceStepIter extends PlanIter {

    static private class SliceStepState extends PlanIterState {

        long theLow;

        long theHigh;

        boolean theHaveNullOrEmptyBound;

        ArrayValueImpl theArray;

        int theElemPos;

        SliceStepState(SliceStepIter iter) {
            init(iter);
        }

        @Override
        protected void reset(PlanIter iter) {
            super.reset(iter);
            init((SliceStepIter)iter);
            theHaveNullOrEmptyBound = false;
        }

        @Override
        protected void close() {
            super.close();
            theArray = null;
        }

        private void init(SliceStepIter iter) {

            theLow = iter.theLowValue;
            theHigh = iter.theHighValue;

            theArray = null;
            theElemPos = 0;
        }
    }

    private final PlanIter theInputIter;

    private final PlanIter theLowIter;

    private final PlanIter theHighIter;

    private final Long theLowValue;

    private final Long theHighValue;

    private final int theCtxItemReg;

    public SliceStepIter(
        Expr e,
        int resultReg,
        PlanIter inputIter,
        PlanIter lowIter,
        PlanIter highIter,
        Long lowValue,
        Long highValue,
        int ctxItemReg) {

        super(e, resultReg);
        theInputIter = inputIter;
        theLowIter = lowIter;
        theHighIter = highIter;

        theLowValue = (lowValue != null ? lowValue : 0);
        theHighValue = (highValue != null ? highValue : Integer.MAX_VALUE);
        theCtxItemReg = ctxItemReg;
    }

    /**
     * FastExternalizable constructor.
     */
    SliceStepIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);
        theCtxItemReg = in.readInt();
        theLowValue = in.readLong();
        theHighValue = in.readLong();
        theInputIter = deserializeIter(in, serialVersion);
        theLowIter = deserializeIter(in, serialVersion);
        theHighIter = deserializeIter(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        super.writeFastExternal(out, serialVersion);
        out.writeInt(theCtxItemReg);
        out.writeLong(theLowValue);
        out.writeLong(theHighValue);
        serializeIter(theInputIter, out, serialVersion);
        serializeIter(theLowIter, out, serialVersion);
        serializeIter(theHighIter, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.SLICE_STEP;
    }

    @Override
    public void open(RuntimeControlBlock rcb) {

        rcb.setState(theStatePos, new SliceStepState(this));
        theInputIter.open(rcb);

        if (theLowIter != null) {
            theLowIter.open(rcb);
        }
        if (theHighIter != null) {
            theHighIter.open(rcb);
        }
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        SliceStepState state = (SliceStepState)rcb.getState(theStatePos);

        if (state.isDone()) {
            return false;
        }

        /*
         * Compute the boundary exprs once here, if they do not depend on the
         * ctx item and they have not been computed already.
         */
        if (theCtxItemReg < 0 && state.isOpen()) {

            state.setState(StateEnum.RUNNING);

            computeBoundaryExprs(rcb, state, false);

            if (state.theHaveNullOrEmptyBound || state.theLow > state.theHigh) {
                state.done();
                return false;
            }
        }

        while (true) {
            /*
             * Get the next context item. It's either the array cached in the
             * state, or if no such array, compute it from the input iter.
             */
            if (state.theArray == null) {

                boolean more = theInputIter.next(rcb);

                if (!more) {
                    state.done();
                    return false;
                }

                int inputReg = theInputIter.getResultReg();
                FieldValueImpl val = rcb.getRegVal(inputReg);

                if (val.isNull()) {
                    rcb.setRegVal(theResultReg, val);
                    return true;
                }

                state.theArray = (ArrayValueImpl)val;

                /*
                 * We have a new ctx item now. If the boundary expr depend on
                 * the ctx item, bind the $$ var and compute the exprs again.
                 */
                if (theCtxItemReg > 0) {
                    computeBoundaryExprs(rcb, state, true);
                }

                state.theElemPos = (int)state.theLow;
            }

            if (state.theHaveNullOrEmptyBound ||
                state.theElemPos > state.theHigh ||
                state.theElemPos >= state.theArray.size()) {
                state.theArray = null;
                continue;
            }

            FieldValueImpl res = state.theArray.getElement(state.theElemPos);
            rcb.setRegVal(theResultReg, res);
            ++state.theElemPos;
            return true;
        }
    }

    private void computeBoundaryExprs(
        RuntimeControlBlock rcb,
        SliceStepState state,
        boolean reset) {

        if (theCtxItemReg > 0) {
            rcb.setRegVal(theCtxItemReg, state.theArray);
        }

        if (theLowIter != null) {

            if (reset) {
                theLowIter.reset(rcb);
            }

            boolean more = theLowIter.next(rcb);

            if (!more) {
                state.theHaveNullOrEmptyBound = true;
            } else {
                FieldValueImpl val = rcb.getRegVal(theLowIter.getResultReg());

                if (val.isNull()) {
                    state.theHaveNullOrEmptyBound = true;
                } else {
                    state.theLow = val.getLong();
                    if (state.theLow < 0) {
                        state.theLow = 0;
                    }
                }
            }
        }

        if (theHighIter != null) {

            if (theHighIter == theLowIter) {
                state.theHigh = state.theLow;
                return;
            }

            if (reset) {
                theHighIter.reset(rcb);
            }

            boolean more = theHighIter.next(rcb);

            if (!more) {
                state.theHaveNullOrEmptyBound = true;
            } else {
                FieldValueImpl val = rcb.getRegVal(theHighIter.getResultReg());

                if (val.isNull()) {
                    state.theHaveNullOrEmptyBound = true;
                } else {
                    state.theHigh = val.getLong();
                }
            }
        }
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {
        theInputIter.reset(rcb);
        if (theLowIter != null) {
            theLowIter.reset(rcb);
        }
        if (theHighIter != null) {
            theHighIter.reset(rcb);
        }
        PlanIterState state = rcb.getState(theStatePos);
        state.reset(this);
    }

    @Override
    public void close(RuntimeControlBlock rcb) {

        PlanIterState state = rcb.getState(theStatePos);
        if (state == null) {
            return;
        }

        theInputIter.close(rcb);
        if (theLowIter != null) {
            theLowIter.close(rcb);
        }
        if (theHighIter != null) {
            theHighIter.close(rcb);
        }

        state.close();
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {

        theInputIter.display(sb, formatter);

        sb.append(",\n");
        if (theLowIter != null) {
            theLowIter.display(sb, formatter);
        } else {
            formatter.indent(sb);
            sb.append("low bound: ").append(theLowValue);
        }

        sb.append(",\n");
        if (theHighIter != null) {
            theHighIter.display(sb, formatter);
        } else {
            formatter.indent(sb);
            sb.append("high bound: ").append(theHighValue);
        }

        if (theCtxItemReg >= 0) {
            sb.append(",\n");
            formatter.indent(sb);
            sb.append("theCtxItemReg : ").append(theCtxItemReg);
        }
    }
}
