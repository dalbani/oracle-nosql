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
import java.util.ArrayList;

import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.FuncAnyOp;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.impl.query.runtime.CompOpIter.CompResult;

/**
 * Iterator to implement the "any" comparison operators (=any, !=any, <any,
 * <=any, >any, and >=any). These operators have existential semantics:
 *
 * Inputs:
 *   Two sequences of zero or more items.
 *
 * Result:
 *   Boolean
 *
 * Note: This implementation assumes no knowledge of the types of the items in
 * the 2 sequences. Each sequence may contain items of any type, including items
 * of different types. As a result, the implementation is not very efficient
 * when both sequences have more than one items, because it simply performs a
 * nested loop over the 2 sequences. Better implementations are possible when
 * we have type info. For example, if all the items in both sequences are of
 * the same type, then <any, <=any, >any, and >=any can be implemented by
 * computing the min and max of each sequence and comparing these min/max
 * values. TODO: implement "specialized" versions of AnyOpIter that make use
 * of the static types of the input sequences.
 */
public class AnyOpIter extends PlanIter {

    static private class AnyOpIterState extends PlanIterState {

        final CompResult theResult = new CompResult();

        @Override
        protected void reset(PlanIter iter) {
            super.reset(iter);
            theResult.haveNull = false;
        }
    }

    private static final int theChunkSize = 10;

    private final FuncCode theAnyCode;

    private final FuncCode theCompCode;

    private final PlanIter theLeftOp;

    private final PlanIter theRightOp;

    public AnyOpIter(
        Expr e,
        int resultReg,
        FuncCode code,
        PlanIter[] argIters) {

        super(e, resultReg);
        theAnyCode = code;
        theCompCode = FuncAnyOp.anyToComp(code);
        assert(argIters.length == 2);
        theLeftOp = argIters[0];
        theRightOp = argIters[1];
    }

    /**
     * TupleSerialization constructor.
     */
    AnyOpIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);

        short ordinal = in.readShort();
        theAnyCode = FuncCode.values()[ordinal];
        theCompCode = FuncAnyOp.anyToComp(theAnyCode);
        theLeftOp = deserializeIter(in, serialVersion);
        theRightOp = deserializeIter(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        super.writeFastExternal(out, serialVersion);
        out.writeShort(theAnyCode.ordinal());
        serializeIter(theLeftOp, out, serialVersion);
        serializeIter(theRightOp, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.ANY_OP;
    }

    @Override
    FuncCode getFuncCode() {
        return theAnyCode;
    }


    @Override
    public void open(RuntimeControlBlock rcb) {
        rcb.setState(theStatePos, new AnyOpIterState());
        theLeftOp.open(rcb);
        theRightOp.open(rcb);
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        AnyOpIterState state = (AnyOpIterState)rcb.getState(theStatePos);

        if (state.isDone()) {
            return false;
        }

        FieldValueImpl lv1;
        FieldValueImpl lv2;
        FieldValueImpl rv1;
        FieldValueImpl rv2;
        boolean more;

        /*
         * Optimize for the case where at least one of the input sequences
         * contains at most one item.
         */

        more = theLeftOp.next(rcb);
        if (!more) {
            done(rcb, state, false);
            return true;
        }

        more = theRightOp.next(rcb);
        if (!more) {
            done(rcb, state, false);
            return true;
        }

        /*
         * Both the left and the right side have at least 1 item.
         * Compare the 1st items from each side now.
         * */
        lv1 = rcb.getRegVal(theLeftOp.getResultReg());
        rv1 = rcb.getRegVal(theRightOp.getResultReg());

        if (compare(lv1, rv1, state.theResult)) {
            done(rcb, state, true);
            return true;
        }

        more = theLeftOp.next(rcb);

        if (!more) {

            /* Left size has exactly one item */
            while (theRightOp.next(rcb)) {

                rv2 = rcb.getRegVal(theRightOp.getResultReg());

                if (compare(lv1, rv2, state.theResult)) {
                    done(rcb, state, true);
                    return true;
                }
            }

            done(rcb, state, false);
            return true;
        }

        /* Left size has more than one item */
        lv2 = rcb.getRegVal(theLeftOp.getResultReg());

        more = theRightOp.next(rcb);

        if (!more) {

            /* Right size has exactly one item */
           if (compare(lv2, rv1, state.theResult)) {
               done(rcb, state, true);
               return true;
           }

           while (theLeftOp.next(rcb)) {

                lv2 = rcb.getRegVal(theLeftOp.getResultReg());

                if (compare(lv2, rv1, state.theResult)) {
                    done(rcb, state, true);
                    return true;
                }
           }

           done(rcb, state, false);
           return true;
        }

        rv2 = rcb.getRegVal(theRightOp.getResultReg());

        /*
         * Handle the general case. Compute a chunk of items from the left
         * side, and then compare these items with all of the items from
         * the right side. Repeat until a left item compares true with a
         * right item or until there are no more left items. The right-side
         * subplan is reset at the end of each left chunk (i.e., the right-
         * side subplan is re-computed fully for each left chunk).
         *
         * TODO consider chunking the right side as well
         */

        /*
         * lv2 and rv2 have not yet been compared with each other and with
         * lv1/rv1. Do these comparisons now, before handling the reset of
         * the sequences.
         */
        if (compare(lv1, rv2, state.theResult) ||
            compare(lv2, rv1, state.theResult) ||
            compare(lv2, rv2, state.theResult)) {
            done(rcb, state, true);
            return true;
        }

        ArrayList<FieldValueImpl> lchunk =
            new ArrayList<FieldValueImpl>(theChunkSize);

        while (true) {

            while (lchunk.size() < theChunkSize && theLeftOp.next(rcb)) {
                lv1 = rcb.getRegVal(theLeftOp.getResultReg());
                lchunk.add(lv1);
            }

            while (theRightOp.next(rcb)) {

                rv1 = rcb.getRegVal(theRightOp.getResultReg());

                for (int i = 0; i < lchunk.size(); ++i) {

                    lv1 = lchunk.get(i);

                    if (compare(lv1, rv1, state.theResult)) {
                        done(rcb, state, true);
                        return true;
                    }
                }
            }

            if (lchunk.size() == theChunkSize) {
                theRightOp.reset(rcb);
                lchunk.clear();
                continue;
            }

            break;
        }

        done(rcb, state, false);
        return true;
    }

    private void done(
        RuntimeControlBlock rcb,
        AnyOpIterState state,
        boolean result) {

        FieldValueImpl res = FieldDefImpl.booleanDef.createBoolean(result);
        rcb.setRegVal(theResultReg, res);
        state.done();
    }

    private boolean compare(
        FieldValueImpl v0,
        FieldValueImpl v1,
        CompResult res) {

        try {
            CompOpIter.compare(v0, v1, theCompCode, res, getLocation());
        } catch (QueryException e) {
            return false;
        }

        if (res.haveNull) {
            return false;
        }

        int comp = res.comp;

        switch (theCompCode) {
        case OP_EQ:
            return (comp == 0);
        case OP_NEQ:
            return (comp != 0);
        case OP_GT:
            return (comp > 0);
        case OP_GE:
            return (comp >= 0);
        case OP_LT:
            return (comp < 0);
        case OP_LE:
            return (comp <= 0);
        default:
            assert(false);
            return false;
        }
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {
        theLeftOp.reset(rcb);
        theRightOp.reset(rcb);
        PlanIterState state = rcb.getState(theStatePos);
        state.reset(this);
    }

    @Override
    public void close(RuntimeControlBlock rcb) {
        theLeftOp.close(rcb);
        theRightOp.close(rcb);
        PlanIterState state = rcb.getState(theStatePos);
        state.close();
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {
        theLeftOp.display(sb, formatter);
        sb.append(",\n");
        theRightOp.display(sb, formatter);
    }
}
