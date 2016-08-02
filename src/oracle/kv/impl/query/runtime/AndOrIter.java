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

import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.NullValueImpl;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.compiler.QueryFormatter;

/**
 * Iterator to implement and and or comparison operators
 *
 * Inputs:
 *   1 or more operand iterators
 *
 * Result:
 *   Boolean
 *
 * NOTE: if any of the operand iterators are done then the iterator returns
 * false on next().  Is this the correct behavior, or should it just return a
 * false result and continue until both iterators are exhausted? TODO.
 */
public class AndOrIter extends PlanIter {

    private final FuncCode theCode;

    private final PlanIter[] theArgs;

    public AndOrIter(
        Expr e,
        int resultReg,
        FuncCode code,
        PlanIter[] argIters) {

        super(e, resultReg);
        theCode = code;
        assert(argIters.length >= 2);
        theArgs = argIters;
    }

    /**
     * FastExternalizable constructor.
     */
    public AndOrIter(DataInput in, short serialVersion) throws IOException {

        super(in, serialVersion);
        short ordinal = in.readShort();
        theCode = FuncCode.values()[ordinal];
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
        out.writeShort(theCode.ordinal());
        serializeIters(theArgs, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.AND_OR;
    }

    @Override
    FuncCode getFuncCode() {
        return theCode;
    }

    @Override
    public void open(RuntimeControlBlock rcb) {
        rcb.setState(theStatePos, new PlanIterState());
        for (PlanIter arg : theArgs) {
            arg.open(rcb);
        }
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        PlanIterState state = rcb.getState(theStatePos);

        if (state.isDone()) {
            return false;
        }

        /*
         * If AND, start true, and exit as soon as there is a false result.
         * If OR, start false, and exit as soon as there is a true result.
         */
        assert(theCode == FuncCode.OP_AND || theCode == FuncCode.OP_OR);
        boolean result = (theCode == FuncCode.OP_AND ? true : false);
        boolean haveNull = false;
        FieldValueImpl res;

        for (PlanIter arg : theArgs) {

            boolean more = arg.next(rcb);

            boolean argResult;

            if (!more) {
                argResult = false;
            } else {
                FieldValueImpl argVal = rcb.getRegVal(arg.getResultReg());

                if (argVal.isNull()) {
                    haveNull = true;
                    continue;
                }

                argResult = argVal.getBoolean();
            }

            if (theCode == FuncCode.OP_AND) {
                result &= argResult;
                if (!result) {
                    haveNull = false;
                    break;
                }
            } else {
                result |= argResult;
                if (result) {
                    haveNull = false;
                    break;
                }
            }
        }

        if (haveNull) {
            res = NullValueImpl.getInstance();
        } else {
            res = FieldDefImpl.booleanDef.createBoolean(result);
        }

        rcb.setRegVal(theResultReg, res);
        state.done();
        return true;
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {

        for (PlanIter arg : theArgs) {
            arg.reset(rcb);
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
