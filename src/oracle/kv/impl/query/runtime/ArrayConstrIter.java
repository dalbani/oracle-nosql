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

import oracle.kv.impl.api.table.ArrayDefImpl;
import oracle.kv.impl.api.table.FieldDefFactory;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.TupleValue;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.table.ArrayDef;
import oracle.kv.table.ArrayValue;

/**
 *
 */
public class ArrayConstrIter extends PlanIter {

    private final PlanIter[] theArgs;
    private final ArrayDef theDef;

    public ArrayConstrIter(
        Expr e,
        int resultReg,
        PlanIter[] args) {

        super(e, resultReg);
        theArgs = args;
        theDef = (ArrayDef) e.getType().getDef();
    }

    /**
     * FastExternalizable constructor.
     */
    ArrayConstrIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);
        theArgs = deserializeIters(in, serialVersion);
        theDef = (ArrayDef) deserializeFieldDef(in, serialVersion);
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
        serializeFieldDef(theDef, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.ARRAY_CONSTR;
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

        int numArgs = theArgs.length;
        int currArg = 0;
        ArrayValue array = null;
        FieldDefImpl targetDef = null;

        while (true) {
            boolean more = theArgs[currArg].next(rcb);

            while (!more && currArg < numArgs - 1) {
                ++currArg;
                more = theArgs[currArg].next(rcb);
            }

            if (more) {

                FieldValueImpl elemValue =
                    rcb.getRegVal(theArgs[currArg].getResultReg());

                if (elemValue.isNull()) {
                    throw new QueryException(
                        "Null values are not allowed in arrays",
                        getLocation());
                }

                FieldDefImpl elemDef = elemValue.getDefinition();
                assert(elemDef.isPrecise());

                if (array != null) {
                    if (!elemDef.equals(targetDef)) {
                        throw new QueryException(
                            "Mixed element types in array constructor",
                            getLocation());
                    }
                } else {
                    targetDef = elemDef;
                    ArrayDefImpl arrayDef =
                        FieldDefFactory.createArrayDef(targetDef);

                    array = arrayDef.createArray();
                }

                if (elemValue.isTuple()) {
                    array.add(((TupleValue)elemValue).toRecord());
                } else {
                    array.add(elemValue);
                }
            } else if (currArg == numArgs - 1) {
                break;
            }
        }

        if (array == null) {
            if (((ArrayDefImpl)theDef).isPrecise()) {
                array = theDef.createArray();
            } else {
                throw new QueryException(
                    "Cannot create empty array because its element " +
                    "type is not known", getLocation());
            }
        }

        rcb.setRegVal(theResultReg, (FieldValueImpl) array);
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
