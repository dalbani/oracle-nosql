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

import oracle.kv.impl.api.table.FieldDefSerialization;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.FieldValueSerialization;
import oracle.kv.impl.query.QueryException.Location;
import oracle.kv.impl.query.compiler.Expr;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.compiler.QueryFormatter;
import oracle.kv.impl.query.compiler.SortSpec;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.TypeManager;
import oracle.kv.impl.util.FastExternalizable;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldRange;
import oracle.kv.table.FieldValue;

/**
 * Base class for all query-plan iterators.
 *
 * The query compiler produces the "query execution plan" as a tree of plan
 * iterators. Each plan iterator has an open()-next()-close() interface. To
 * execute a query, the application must first call open() on the root iterator
 * and then call next() a number of times to retrieve the results. Finally,
 * after the application has retrieved all the results, or when it is not
 * interested in retrieving any more results, it must call close() to release
 * any resources held by the iterators.
 *
 * In general, these calls are propagated top-down within the execution plan,
 * and results flow bottom-up. More specifically, each next() call on the root
 * iterator produces one item in the result set of the query. Actually, the
 * same is true for all iterators: each next() call on a plan iterator produces
 * one item in the result set of that iterator. So, in the most general case,
 * the result set of each iterator is a sequence of zero or more items.
 *
 * Iterator state and registers:
 * -----------------------------
 *
 * As mentioned already, each next() call on an iterator produces one result
 * item. However, the result items are not returned by the next() call directly.
 * Instead, each iterator places its current result (the one produced by the
 * current invocation of the next() call) in a "register", where a consumer
 * iterator can pick it up from. A "register" is just an entry in an array of
 * FiledValueImpl instances. This array is created during the creation of the
 * RuntimeControlBlock (RCB). The size of the regs array is determined during
 * code generation (see CodeGenerator class): a number of zero or more registers
 * are reserved for each generated iterator, and the sum of these registers is
 * the size of the regs array. Each iterator knows the positions (reg ids) of
 * the registers that have been reserved for it, and will, in general, provide
 * this info to its parent (consuming) iterator. Of particular interest is the
 * "result reg" of each iterator: the id of the register where the iterator
 * places its result items. All iterators have a result reg. Notice, however,
 * that some iterators may share the same result reg. For example, if an
 * iterator A is just filtering the result of another iterator B, A can use
 * B's result reg as its own result reg as well.
 *
 * Records vs tuples.
 * ------------------
 * Iterators that produce or propagate records have a choice about how the
 * records are returned:
 *
 * (a) As self-described record items, ie, instances of RecordValueImpl.
 *
 * (b) As "tuple" items, i.e., instances of TupleValue. Tuples can be thought
 * of as un-nested records. Their field values are stored in N registers within
 * the RCB array of registers, where N is the number of fields. A TupleValue
 * instance itself stores only the ids of those registers and a pointer to the
 * RCB array of registers. The field names are not stored in the  TupleValue
 * at all; they are stored only in the associated type def.
 *
 * Currently, TupleValue instances are created only by iterators that are
 * original producers of tuples, specifically, the BaseTableIter and the
 * SFWIter. These iterators reserve N + 1 registers in the RCB reg array:
 * 1 reg as the result reg, which will hold a TupleValue, and N regs to hold
 * the field values of each tuple they will generate (these are called the
 * "tuple regs"). Then, during the open() call they create one TupleValue
 * and place a ref to it in the result reg. Finally, during each subsequent
 * next() invocation, they compute the N field values and place them in the
 * N tuple regs.
 *
 * Notice that on each next() call, producers of tuples overwrite the field
 * values of the previous tuple. As a result, consumers of tuples who need to
 * retain a tuple value across next() calls must clone the tuple. Currently,
 * we don't have any iterators that cache any kind of values across next()
 * calls.
 *
 * Data members:
 * -------------
 *
 * theKind:
 * The kind of this iterator (there is one PlanIter subclass for each kind).
 * Roughly speaking, each kind of iterator evaluates a different kind of
 * expression that may appear in a query.
 *
 * theResultReg:
 * The id of the register where this iterator will store each item generated
 * by a next() call.
 *
 * theStatePos:
 * The index within the state array where the state of this iterator is stored.
 */
public abstract class PlanIter implements FastExternalizable {

    private static final int NULL_VALUE = -1;

    /*
     * Add new iterator types to the end of this list. The ordinal is used for
     * serialization of query plans.
     */
    public static enum PlanIterKind {
        CONST,
        VAR_REF,
        EXTERNAL_VAR_REF,
        ARRAY_CONSTR,
        BASE_TABLE,
        COMP_OP,
        ANY_OP,
        AND_OR,
        ARITH_OP,
        ARITH_UNARY_OP,
        PROMOTE,
        FIELD_STEP,
        SLICE_STEP,
        FILTER_STEP,
        SFW,
        FUNC_SIZE,
        FUNC_KEYS,
        RECV,
        CONCAT
    }

    protected final int theResultReg;

    protected final int theStatePos;

    protected final Location theLocation;

    PlanIter(Expr e, int resultReg) {
        theResultReg = resultReg;
        theStatePos = e.getQCB().incNumPlanIters();
        theLocation = e.getLocation();
    }

    protected PlanIter(int statePos, int resultReg, Location location) {
        theResultReg = resultReg;
        theStatePos = statePos;
        theLocation = location;
    }

    /**
     * FastExternalizable constructor.
     */
    @SuppressWarnings("unused")
    protected PlanIter(DataInput in, short serialVersion) throws IOException {
        theResultReg = in.readInt();
        theStatePos = in.readInt();
        theLocation = new Location(in.readInt(), in.readInt(),
                                   in.readInt(), in.readInt());
    }

    /**
     * FastExternalizable writer.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

        // Should we be using SerializationUtil.writePackedInt(out, val) here ????
        out.writeInt(theResultReg);
        out.writeInt(theStatePos);
        out.writeInt(theLocation.getStartLine());
        out.writeInt(theLocation.getStartColumn());
        out.writeInt(theLocation.getEndLine());
        out.writeInt(theLocation.getEndColumn());
    }

    public final int getResultReg() {
        return theResultReg;
    }

    public Location getLocation() {
        return theLocation;
    }

    public abstract PlanIterKind getKind();

    /*
     * Iterators that always return tuples redefine the getTupleRegs() method to
     * return a non-null array of register ids, specifying the registers storing
     * the tuple values.
     *
     * This method is actually needed only by the CodeGenerator to save the
     * allocation of a result reg for some iterators (e.g. ColumnIter,
     * FieldStepIter, PromoteIter).
     */
    public int[] getTupleRegs() {
        return null;
    }

    public boolean producesTuples() {
        return getTupleRegs() != null;
    }

    public abstract void open(RuntimeControlBlock rcb);

    public abstract boolean next(RuntimeControlBlock rcb);

    public abstract void reset(RuntimeControlBlock rcb);

    public abstract void close(RuntimeControlBlock rcb);

    public boolean hasNext(RuntimeControlBlock rcb) {
        return !rcb.getState(theStatePos).isDone();
    }

    public boolean isClosed(RuntimeControlBlock rcb) {
        return rcb.getState(theStatePos).isClosed();
    }

    public final String display() {
        StringBuilder sb = new StringBuilder();
        display(sb, new QueryFormatter());
        return sb.toString();
    }

    /*
     * This method must be overriden by iterators that implement a family of
     * builtin functions (and as a result have a FuncCode data member).
     * Currently, this method is used only in the display method below.
     */
    FuncCode getFuncCode() {
        return null;
    }

    protected void display(StringBuilder sb, QueryFormatter formatter) {

        formatter.indent(sb);

        if (getFuncCode() != null) {
            sb.append(getFuncCode());
        } else {
            sb.append(getKind());
        }

        displayRegs(sb);

        sb.append("\n");
        formatter.indent(sb);
        sb.append("[\n");
        formatter.incIndent();
        displayContent(sb, formatter);
        formatter.decIndent();
        sb.append("\n");
        formatter.indent(sb);
        sb.append("]");
    }

    final void displayRegs(StringBuilder sb) {
        sb.append("(");
        sb.append("[").append(theResultReg).append("]");
        int[] tupleRegs = getTupleRegs();
        if (tupleRegs != null) {
            sb.append(", ");
            for (int i = 0; i < tupleRegs.length; ++i) {
                sb.append(tupleRegs[i]);
                if (i < tupleRegs.length - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(")");
    }

    protected abstract void displayContent(
        StringBuilder sb,
        QueryFormatter formatter);

    /*
     * Utility methods
     */

    /*
     * A null array writes a 0 length.
     */
    static void serializeIters(
        PlanIter[] iters,
        DataOutput out,
        short serialVersion) throws IOException {

        if (iters == null) {
            out.writeInt(0);
        } else {
            out.writeInt(iters.length);
            for (PlanIter iter : iters) {
                serializeIter(iter, out, serialVersion);
            }
        }
    }

    /*
     * A zero length returns a null array
     */
    static PlanIter[] deserializeIters(DataInput in, short serialVersion)
            throws IOException {

        int numArgs = in.readInt();
        PlanIter[] iters = new PlanIter[numArgs];
        if (numArgs > 0) {
            for (int i = 0; i < numArgs; i++) {
                iters[i] = deserializeIter(in, serialVersion);
            }
        }
        return iters;
    }

    /**
     * PlanIter always has a leading short indicating the type of the iterator.
     * This is used by the deserialization code to determine which constructor
     * to call on deserialization. A value of NULL_VALUE indicates that there is
     * no iterator at all in this space in the stream.
     */
    public static void serializeIter(
        PlanIter iter,
        DataOutput out,
        short serialVersion) throws IOException {

        if (iter == null) {
            out.writeByte(NULL_VALUE);
            return;
        }

        out.writeByte(iter.getKind().ordinal());
        iter.writeFastExternal(out, serialVersion);
    }

    /**
     * See comments on serializeIter about layout.
     */
    public static PlanIter deserializeIter(DataInput in, short serialVersion)
            throws IOException {

        int iterType = in.readByte();
        if (iterType == NULL_VALUE) {
            return null;
        }
        PlanIter iter = null;

        PlanIterKind kind = PlanIterKind.values()[iterType];
        switch (kind) {
        case CONST:
            iter = new ConstIter(in, serialVersion);
            break;
        case VAR_REF:
            iter = new VarRefIter(in, serialVersion);
            break;
        case EXTERNAL_VAR_REF:
            iter = new ExternalVarRefIter(in, serialVersion);
            break;
        case ARRAY_CONSTR:
            iter = new ArrayConstrIter(in, serialVersion);
            break;
        case BASE_TABLE:
            iter = new BaseTableIter(in, serialVersion);
            break;
        case COMP_OP:
            iter = new CompOpIter(in, serialVersion);
            break;
        case ANY_OP:
            iter = new AnyOpIter(in, serialVersion);
            break;
        case AND_OR:
            iter = new AndOrIter(in, serialVersion);
            break;
        case PROMOTE:
            iter = new PromoteIter(in, serialVersion);
            break;
        case FIELD_STEP:
            iter = new FieldStepIter(in, serialVersion);
            break;
        case SLICE_STEP:
            iter = new SliceStepIter(in, serialVersion);
            break;
        case FILTER_STEP:
            iter = new FilterStepIter(in, serialVersion);
            break;
        case SFW:
            iter = new SFWIter(in, serialVersion);
            break;
        case FUNC_SIZE:
            iter = new FuncSizeIter(in, serialVersion);
            break;
        case FUNC_KEYS:
            iter = new FuncKeysIter(in, serialVersion);
            break;
        case ARITH_OP:
            iter = new ArithOpIter(in, serialVersion);
            break;
        case ARITH_UNARY_OP:
            iter = new ArithUnaryOpIter(in, serialVersion);
            break;
        case CONCAT:
            iter = new ConcatIter(in, serialVersion);
            break;
        case RECV:
            iter = new ReceiveIter(in, serialVersion);
            break;
        }
        return iter;
    }

    void serializeByteArray(byte[] array, DataOutput out) throws IOException {

        if (array == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(array.length);
        for (byte element : array) {
            out.writeByte(element);
        }
    }

    byte[] deserializeByteArray(DataInput in) throws IOException {

        int len = in.readInt();
        if (len > 0) {
            byte[] array = new byte[len];
            for (int i = 0; i < len; i++) {
                array[i] = in.readByte();
            }
            return array;
        }
        return null;
    }

    static void serializeIntArray(int[] array, DataOutput out)
            throws IOException {

        if (array == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(array.length);
        for (int element : array) {
            out.writeInt(element);
        }
    }

    static int[] deserializeIntArray(DataInput in) throws IOException {

        int len = in.readInt();
        if (len > 0) {
            int[] intArray = new int[len];
            for (int i = 0; i < len; i++) {
                intArray[i] = in.readInt();
            }
            return intArray;
        }
        return null;
    }

    static void serializeStringArray(String[] array, DataOutput out)
            throws IOException {

        if (array == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(array.length);
        for (String element : array) {
            out.writeUTF(element);
        }
    }

    static String[] deserializeStringArray(DataInput in) throws IOException {

        int len = in.readInt();
        if (len > 0) {
            String[] array = new String[len];
            for (int i = 0; i < len; i++) {
                array[i] = in.readUTF();
            }
            return array;
        }
        return null;
    }

    static void serializeFieldRange(
        FieldRange range,
        DataOutput out,
        short serialVersion) throws IOException {

        if (range != null) {
            out.writeBoolean(true);
            range.writeFastExternal(out, serialVersion);
        } else {
            out.writeBoolean(false);
        }
    }

    static FieldRange deserializeFieldRange(
        DataInput in,
        short serialVersion) throws IOException {

        boolean hasRange = in.readBoolean();
        if (hasRange) {
            return new FieldRange(in, serialVersion);
        }
        return null;
    }

    public static void serializeFieldDef(
        FieldDef def,
        DataOutput out,
        short serialVersion) throws IOException {

        FieldDefSerialization.writeFieldDef(def, out, serialVersion);
    }

    public static FieldDef deserializeFieldDef(
        DataInput in,
        short serialVersion) throws IOException {

        return FieldDefSerialization.readFieldDef(in, serialVersion);
    }

    static void serializeFieldValue(
        FieldValue value,
        DataOutput out,
        short serialVersion) throws IOException {

        FieldValueSerialization.writeFieldValue(
            value, true, out, serialVersion);
    }

    static FieldValueImpl deserializeFieldValue(
        DataInput in,
        short serialVersion) throws IOException {

        return (FieldValueImpl)FieldValueSerialization.readFieldValue(
            null, in, serialVersion);
    }

    static void serializeExprType(
        ExprType type,
        DataOutput out,
        short serialVersion) throws IOException {

        if (type != null) {
            out.writeBoolean(true);
            TypeManager.serializeExprType(type, out, serialVersion);
        } else {
            out.writeBoolean(false);
        }
    }

    /**
     * Read the type and quantifier and builtin boolean. If this is a builtin
     * type, get the (static) type directly from TypeManager.
     */
    static ExprType deserializeExprType(DataInput in, short serialVersion)
            throws IOException {

        boolean hasType = in.readBoolean();
        if (hasType) {
            return TypeManager.deserializeExprType(in, serialVersion);
        }
        return null;
    }

    static void serializeSortSpecs(
        SortSpec[] specs,
        DataOutput out,
        short serialVersion) throws IOException {

        if (specs == null) {
            out.writeShort(0);
            return;
        }

        out.writeShort(specs.length);
        for (SortSpec spec : specs) {
            spec.writeFastExternal(out, serialVersion);
        }
    }

    static SortSpec[] deserializeSortSpecs(DataInput in, short serialVersion)
            throws IOException {

        int num = in.readShort();

        if (num == 0) {
            return null;
        }

        SortSpec[] specs = new SortSpec[num];

        for (int i = 0; i < num; ++i) {
            specs[i] = new SortSpec(in, serialVersion);
        }

        return specs;
    }

}
