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
import java.util.Iterator;

import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryException.Location;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.impl.query.runtime.PlanIterState.StateEnum;

/**
 *
 */
public class FilterStepIter extends PlanIter {

    static private class FilterStepState extends PlanIterState {

        FieldValueImpl theCtxItem;

        int theCtxPos;

        Iterator<String> theMapKeysIter;

        String theCtxKey;

        int theBoolValue;

        long theNumValue;

        boolean theComputePredOnce;

        boolean theComputePredPerArray;

        boolean theComputePredPerElem;

        FilterStepState(FilterStepIter iter) {

            theComputePredOnce = (iter.theCtxItemReg < 0 &&
                                  iter.theCtxElemReg < 0 &&
                                  iter.theCtxElemPosReg < 0 &&
                                  iter.theCtxKeyReg < 0);

            theComputePredPerArray = (iter.theCtxItemReg >= 0 &&
                                      iter.theCtxElemReg < 0 &&
                                      iter.theCtxElemPosReg < 0 &&
                                      iter.theCtxKeyReg < 0);

            theComputePredPerElem = (iter.theCtxElemReg >= 0 ||
                                     iter.theCtxElemPosReg >= 0 ||
                                     iter.theCtxKeyReg >= 0);
        }

        @Override
        protected void reset(PlanIter iter) {
            super.reset(iter);
            theCtxItem = null;
            theCtxPos = 0;
            theMapKeysIter = null;
            theBoolValue = -1;
        }

        @Override
        protected void close() {
            super.close();
            theCtxItem = null;
            theMapKeysIter = null;
        }
    }

    private final PlanIter theInputIter;

    private final PlanIter thePredIter;

    private final int theCtxItemReg;

    private final int theCtxElemReg;

    private final int theCtxElemPosReg;

    private final int theCtxKeyReg;

    public FilterStepIter(
        Expr e,
        int resultReg,
        PlanIter inputIter,
        PlanIter predIter,
        int ctxItemReg,
        int ctxElemReg,
        int ctxElemPosReg,
        int ctxKeyReg) {

        super(e, resultReg);
        theInputIter = inputIter;
        thePredIter = predIter;
        theCtxItemReg = ctxItemReg;
        theCtxElemReg = ctxElemReg;
        theCtxElemPosReg = ctxElemPosReg;
        theCtxKeyReg = ctxKeyReg;
    }

    private static void checkCtxItem(FieldValueImpl ctxItem,Location location) {

        if (!ctxItem.isNull() && !ctxItem.isArray() && !ctxItem.isMap()) {
            throw new QueryException(
                "Context item in filter step has invalid type.\n" +
                "Expected an array or map type. Actual type is:\n" +
                ctxItem.getDefinition(), location);
        }
    }

    private void moveToNextElement(FilterStepState state) {

        ++state.theCtxPos;

        if (state.theCtxItem.isMap() && state.theMapKeysIter.hasNext()) {
            state.theCtxKey = state.theMapKeysIter.next();
        }
    }

    /**
     * FastExternalizable constructor.
     */
    FilterStepIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);
        theCtxItemReg = in.readInt();
        theCtxElemReg = in.readInt();
        theCtxElemPosReg = in.readInt();
        theCtxKeyReg = in.readInt();
        theInputIter = deserializeIter(in, serialVersion);
        thePredIter = deserializeIter(in, serialVersion);
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
        out.writeInt(theCtxElemReg);
        out.writeInt(theCtxElemPosReg);
        out.writeInt(theCtxKeyReg);
        serializeIter(theInputIter, out, serialVersion);
        serializeIter(thePredIter, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.FILTER_STEP;
    }

    @Override
    public void open(RuntimeControlBlock rcb) {
        rcb.setState(theStatePos, new FilterStepState(this));
        theInputIter.open(rcb);
        if (thePredIter != null) {
            thePredIter.open(rcb);
        }
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        FilterStepState state = (FilterStepState)rcb.getState(theStatePos);

        if (state.isDone()) {
            return false;
        }

        if (state.theComputePredOnce && state.isOpen()) {

            state.setState(StateEnum.RUNNING);

            computePredExpr(rcb, state);

            if (state.theBoolValue == 0) {
                state.done();
                return false;
            }
        }

        FieldValueImpl ctxItem;

        while(true) {

            if (state.theCtxItem == null) {

                /*
                 * Compute the next context item and make sure it is an array
                 * or map, or NULL. If it is NULL, return NULL. If it is an
                 * empty array/map, skip it. Else, initialize iteration over
                 * the ctx item.
                 */
                boolean more = theInputIter.next(rcb);

                if (!more) {
                    state.done();
                    return false;
                }

                ctxItem = rcb.getRegVal(theInputIter.getResultReg());

                checkCtxItem(ctxItem, getLocation());

                if (ctxItem.isNull()) {
                    rcb.setRegVal(theResultReg, ctxItem);
                    return true;
                }

                if (ctxItem.size() == 0) {
                    continue;
                }

                state.theCtxItem = ctxItem;
                state.theCtxPos = 0;

                if (ctxItem.isMap()) {
                    state.theMapKeysIter = ctxItem.getMap().keySet().iterator();
                    state.theCtxKey = state.theMapKeysIter.next();
                }

                /*
                 * Compute the filtering predicate, if not done already. If the
                 * pred value is false, we skip this array/map immediately. We
                 * can also skip this array if the pred value is a number < 0 or
                 * >= size of the array.
                 **/
                if (!state.theComputePredPerElem) {

                    if (state.theComputePredPerArray) {

                        computePredExpr(rcb, state);

                        if (state.theBoolValue == 0) {
                            state.theCtxItem = null;
                            continue;
                        }
                    }

                    if (state.theBoolValue < 0) {

                        if (ctxItem.isMap()) {
                            throw new QueryException(
                                "Cannot use a positional predicate in a " +
                                "filter step over a map context item.",
                                getLocation());
                        }

                        if (state.theNumValue < 0 ||
                            state.theNumValue >= ctxItem.size()) {
                            state.theCtxItem = null;
                            continue;
                        }
                    }
                }
            } else {
                ctxItem = state.theCtxItem;
            }

            /*
             * We have processed all the elements/entries of the current
             * array/map, so proceed with the next one.
             */
            if (state.theCtxPos >= ctxItem.size()) {
                assert(state.theMapKeysIter == null ||
                       !state.theMapKeysIter.hasNext());
                state.theCtxItem = null;
                continue;
            }

            if (state.theComputePredPerElem) {

                computePredExpr(rcb, state);

                if (state.theBoolValue == 0) {
                    moveToNextElement(state);
                    continue;
                }
            } else {
                assert(state.theBoolValue != 0);
            }

            /*
             * We have either a true boolean pred or a positional pred. In the
             * formar case return the current item.
             */
            if (state.theBoolValue == 1) {

                FieldValueImpl elem;

                if (state.theCtxItem.isArray()) {
                    elem = state.theCtxItem.getElement(state.theCtxPos);
                } else {
                    elem = state.theCtxItem.getElement(state.theCtxKey);
                }

                moveToNextElement(state);

                rcb.setRegVal(theResultReg, elem);
                return true;
            }

            /*
             * It's a positional pred. If the pred does not depend on the
             * current element, return the item at the computed position and
             * then proceed with the next array/map. Otherwise, skip/return
             * the current element if its position is equal/not-equal with
             * the computed position.
             */
            if (state.theComputePredPerElem) {

                if (ctxItem.isMap()) {
                    throw new QueryException(
                        "Cannot use a positional predicate in a " +
                        "filter step over a map context item.", getLocation());
                }

                if (state.theNumValue != state.theCtxPos) {
                    moveToNextElement(state);
                    continue;
                }

                moveToNextElement(state);

            } else {
                state.theCtxItem = null;
            }

            FieldValueImpl res = ctxItem.getElement((int)state.theNumValue);
            rcb.setRegVal(theResultReg, res);
            return true;
        }
    }

    void computePredExpr(RuntimeControlBlock rcb, FilterStepState state) {

        if (thePredIter == null) {
            state.theBoolValue = 1;
            return;
        }

        state.theBoolValue = -1;

        thePredIter.reset(rcb);

        if (theCtxItemReg >= 0) {
            rcb.setRegVal(theCtxItemReg, state.theCtxItem);
        }

        if (theCtxElemReg >= 0) {
            if (state.theCtxItem.isArray()) {
                rcb.setRegVal(
                    theCtxElemReg,
                    state.theCtxItem.getElement(state.theCtxPos));
            } else {
                rcb.setRegVal(
                    theCtxElemReg,
                    state.theCtxItem.getElement(state.theCtxKey));
            }
        }

        if (theCtxElemPosReg >= 0) {

            if (state.theCtxItem.isMap()) {
                throw new QueryException(
                    "Cannot reference the $$elementPos context variable " +
                    "in a filter step over a map context item.",
                    getLocation());
            }

            rcb.setRegVal(
                theCtxElemPosReg,
                FieldDefImpl.integerDef.createInteger(state.theCtxPos));
        }

        if (theCtxKeyReg >= 0) {

            if (state.theCtxItem.isArray()) {
                throw new QueryException(
                    "Cannot reference the $$Key context variable " +
                    "in a filter step over an array context item.",
                    getLocation());
            }

            rcb.setRegVal(
                theCtxKeyReg,
                FieldDefImpl.stringDef.createString(state.theCtxKey));
        }

        boolean more = thePredIter.next(rcb);

        if (!more) {
            state.theBoolValue = 0;

        } else {
            FieldValueImpl val = rcb.getRegVal(thePredIter.getResultReg());

            if (val.isNull()) {
                state.theBoolValue = 0;
            } else if (val.isBoolean()) {
                state.theBoolValue = (val.getBoolean() ? 1 : 0);
            } else if (val.isLong() || val.isInteger() ) {
                state.theNumValue = val.getLong();
            } else {
                throw new QueryException(
                    "Predicate expression in filter step has invalid type:\n" +
                    val.getDefinition(), getLocation());
            }
        }
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {
        theInputIter.reset(rcb);
        if (thePredIter != null) {
            thePredIter.reset(rcb);
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
        if (thePredIter != null) {
            thePredIter.close(rcb);
        }

        state.close();
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {

        theInputIter.display(sb, formatter);

        if (thePredIter != null) {
            sb.append(",\n");
            thePredIter.display(sb, formatter);
        }

        if (theCtxItemReg >= 0) {
            sb.append(",\n");
            formatter.indent(sb);
            sb.append("theCtxItemReg : ").append(theCtxItemReg);
        }

        if (theCtxElemReg >= 0) {
            sb.append(",\n");
            formatter.indent(sb);
            sb.append("theCtxElemReg : ").append(theCtxElemReg);
        }

        if (theCtxElemPosReg >= 0) {
            sb.append(",\n");
            formatter.indent(sb);
            sb.append("theCtxElemPosReg : ").append(theCtxElemPosReg);
        }

        if (theCtxKeyReg >= 0) {
            sb.append(",\n");
            formatter.indent(sb);
            sb.append("theCtxKeyReg : ").append(theCtxKeyReg);
        }
    }
}
