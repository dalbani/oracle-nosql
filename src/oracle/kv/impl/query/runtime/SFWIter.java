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

import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.NullValueImpl;
import oracle.kv.impl.api.table.RecordDefImpl;
import oracle.kv.impl.api.table.TupleValue;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.impl.util.SerializationUtil;

/**
 * SFWIter evaluates a SELECT-FROM_WHERE query block. For now (in the absence
 * of scalar subqueries), the iterator is always producing a sequence of
 * records. These records are returned unpacked (i.e. as tuples). The names
 * of the tuple columns are statically known (i.e., they don't need to be
 * computed during runtime), and as a result, they are not placed in registers;
 * instead they are available via method calls.
 *
 * theFromIter:
 *
 * theWhereIter:
 *
 * theColumnIters:
 *
 * theColumnNames:
 *
 * theTupleRegs:
 *
 * theTypeDefinition:
 * The type of the data returned by this iterator.
 */
public class SFWIter extends PlanIter {

    private final PlanIter theFromIter;

    private final String theFromVarName;

    private final PlanIter theWhereIter;

    private final PlanIter[] theColumnIters;

    private final String[] theColumnNames;

    private final int[] theTupleRegs;

    private final RecordDefImpl theTypeDefinition;

    public SFWIter(
        Expr e,
        int resultReg,
        int[] tupleRegs,
        PlanIter fromIter,
        String fromVarName,
        PlanIter whereIter,
        PlanIter[] columnIters,
        String[] columnNames) {

        super(e, resultReg);
        theFromIter = fromIter;
        theFromVarName = fromVarName;
        theWhereIter = whereIter;
        theColumnIters = columnIters;
        theColumnNames = columnNames;
        theTupleRegs = tupleRegs;
        theTypeDefinition = (RecordDefImpl) e.getType().getDef();
    }

    /**
     * FastExternalizable constructor.
     */
    SFWIter(DataInput in, short serialVersion) throws IOException {
        super(in, serialVersion);
        theTypeDefinition =
            (RecordDefImpl) deserializeFieldDef(in, serialVersion);
        theTupleRegs = deserializeIntArray(in);
        theColumnNames = deserializeStringArray(in);
        theColumnIters = deserializeIters(in, serialVersion);
        theFromIter = deserializeIter(in, serialVersion);
        theFromVarName = SerializationUtil.readString(in);
        theWhereIter = deserializeIter(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        super.writeFastExternal(out, serialVersion);
        serializeFieldDef(theTypeDefinition, out, serialVersion);
        serializeIntArray(theTupleRegs, out);
        serializeStringArray(theColumnNames, out);
        serializeIters(theColumnIters, out, serialVersion);
        serializeIter(theFromIter, out, serialVersion);
        SerializationUtil.writeString(out, theFromVarName);
        serializeIter(theWhereIter, out, serialVersion);
    }

    @Override
    public PlanIterKind getKind() {
        return PlanIterKind.SFW;
    }

    @Override
    public int[] getTupleRegs() {
        return theTupleRegs;
    }

    public int getNumColumns() {
        return theColumnNames.length;
    }

    public String getColumnName(int i) {
        return theColumnNames[i];
    }

    @Override
    public void open(RuntimeControlBlock rcb) {

        rcb.setState(theStatePos, new PlanIterState());

        TupleValue tuple = new TupleValue(
            theTypeDefinition, rcb.getRegisters(), theTupleRegs);

        rcb.setRegVal(theResultReg, tuple);

        theFromIter.open(rcb);

        if (theWhereIter != null) {
            theWhereIter.open(rcb);
        }

        if (theColumnIters != null) {
            for (PlanIter columnIter : theColumnIters) {
                columnIter.open(rcb);
            }
        }
    }

    @Override
    public boolean next(RuntimeControlBlock rcb) {

        PlanIterState state = rcb.getState(theStatePos);

        //System.out.println("SFW IN-next()");

        if (state.isDone()) {
            //System.out.println("SFW OUT-next(): iter was done");
            return false;
        }

        if (theWhereIter != null) {

            boolean whereValue = true;

            do {
                if (!theFromIter.next(rcb)) {
                    state.done();
                    return false;
                }

                boolean more = theWhereIter.next(rcb);

                if (!more) {
                    whereValue = false;
                } else {
                    FieldValueImpl val =
                        rcb.getRegVal(theWhereIter.getResultReg());

                    whereValue = (val.isNull() ? false : val.getBoolean());
                }

                theWhereIter.reset(rcb);

            } while (whereValue == false);

        } else {
            if (!theFromIter.next(rcb)) {
                //System.out.println("SFW OUT-next(): FROM iter is done");
                state.done();
                return false;
            }
        }

        if (theColumnIters == null) {
            return true;
        }

        for (int i = 0; i < theColumnIters.length; ++i) {

            PlanIter columnIter = theColumnIters[i];
            boolean more = columnIter.next(rcb);

            if (!more) {
                rcb.setRegVal(theTupleRegs[i], NullValueImpl.getInstance());
            }

            FieldValueImpl value = rcb.getRegVal(columnIter.getResultReg());
            if (value.isTuple()) {
                value = ((TupleValue)value).toRecord();
                rcb.setRegVal(theTupleRegs[i], value);
            }

            /* the column iterators need to be reset for the next call to next */
            columnIter.reset(rcb);
        }

        //System.out.println("SFW OUT-next(): got result");
        return true;
    }

    @Override
    public void reset(RuntimeControlBlock rcb) {

        theFromIter.reset(rcb);

        if (theWhereIter != null) {
            theWhereIter.reset(rcb);
        }

        if (theColumnIters != null) {
            for (PlanIter columnIter : theColumnIters) {
                columnIter.reset(rcb);
            }
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

        theFromIter.close(rcb);

        if (theWhereIter != null) {
            theWhereIter.close(rcb);
        }

        if (theColumnIters != null) {
            for (PlanIter columnIter : theColumnIters) {
                columnIter.close(rcb);
            }
        }

        state.close();
    }

    @Override
    protected void displayContent(StringBuilder sb, QueryFormatter formatter) {

        formatter.indent(sb);
        sb.append("FROM:\n");
        theFromIter.display(sb, formatter);
        sb.append(" as " +  theFromVarName + "\n\n");

        if (theWhereIter != null) {
            formatter.indent(sb);
            sb.append("WHERE:\n");
            theWhereIter.display(sb, formatter);
            sb.append("\n\n");
        }

        formatter.indent(sb);
        sb.append("SELECT:\n");

        if (theColumnIters != null) {
            for (int i = 0; i < theColumnIters.length; ++i) {
                theColumnIters[i].display(sb, formatter);
                if (i < theColumnIters.length - 1) {
                    sb.append(",\n");
                }
            }
        } else {
            formatter.indent(sb);
            sb.append("*");
        }

    }
}
