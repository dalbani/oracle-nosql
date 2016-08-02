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

package oracle.kv.impl.query.compiler;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

import oracle.kv.impl.api.table.RecordDefImpl;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.compiler.Expr.ExprKind;
import oracle.kv.impl.query.runtime.ArrayConstrIter;
import oracle.kv.impl.query.runtime.BaseTableIter;
import oracle.kv.impl.query.runtime.ConstIter;
import oracle.kv.impl.query.runtime.FieldStepIter;
import oracle.kv.impl.query.runtime.FilterStepIter;
import oracle.kv.impl.query.runtime.PlanIter;
import oracle.kv.impl.query.runtime.PromoteIter;
import oracle.kv.impl.query.runtime.SFWIter;
import oracle.kv.impl.query.runtime.SliceStepIter;
import oracle.kv.impl.query.runtime.VarRefIter;
import oracle.kv.impl.query.runtime.ExternalVarRefIter;
import oracle.kv.impl.query.runtime.ReceiveIter;
import oracle.kv.impl.query.types.ExprType;

/**
 * Walks the expressions tree (actually DAG) and generates the query execution
 * plan (a tree of PlanIter objs) by constructing the appropriate PlanIter for
 * each expression.
 */
public class CodeGenerator extends ExprVisitor {

    private final QueryControlBlock theQCB;

    private final ExprWalker theWalker;

    private final HashMap<Expr, Integer> theResultRegsMap;

    private final HashMap<Expr, int[]> theTupleRegsMap;

    private final Stack<PlanIter> theIters;

    private PlanIter theFromIter = null;

    private PlanIter theRootIter;

    private RuntimeException theException = null;

    CodeGenerator(QueryControlBlock qcb) {
        theQCB = qcb;
        theWalker = new ExprWalker(this, false/*allocateChildrenIter*/);
        theResultRegsMap = new HashMap<Expr, Integer>();
        theTupleRegsMap = new HashMap<Expr, int[]>();
        theIters = new Stack<PlanIter>();
    }

    public RuntimeException getException() {
        return theException;
    }

    public void setException(RuntimeException e) {
        theException = e;
    }

    PlanIter getRootIter() {
        return theRootIter;
    }

    public void generatePlan(Expr expr) {

        try {
            theWalker.walk(expr);
            theRootIter = theIters.pop();
            assert(theIters.isEmpty());
        } catch (RuntimeException e) {
            setException(e);
        }
    }

    int allocateResultReg(Expr e) {

        Integer reg = theResultRegsMap.get(e);

        if (reg == null) {
            reg = new Integer(theQCB.getNumRegs());
            theResultRegsMap.put(e, reg);
            theQCB.incNumRegs(1);
        }

        return reg.intValue();
    }

    void setResultReg(Expr e, int reg) {

        assert(reg >= 0 && reg < theQCB.getNumRegs());

        Integer currReg = theResultRegsMap.get(e);

        if (currReg != null) {
            throw new QueryStateException(
                "Cannot update existing register for expression: " +
                e.display());
        }

        currReg = new Integer(reg);
        theResultRegsMap.put(e, currReg);
    }

    int getResultReg(Expr e) {
        Integer reg = theResultRegsMap.get(e);
        return (reg == null ? -1 : reg.intValue());
    }

    int[] allocateTupleRegs(Expr e, int numRegs) {

        int[] regs = theTupleRegsMap.get(e);

        if (regs == null) {

            regs = new int[numRegs];
            theTupleRegsMap.put(e, regs);

            for (int i = 0; i < numRegs; ++i) {
                regs[i] = theQCB.getNumRegs() + i;
            }

            theQCB.incNumRegs(numRegs);
        }

        return regs;
    }

    void setTupleRegs(Expr e, int[] regs) {

        if (regs == null) {
            return;
        }

        int[] currRegs = theTupleRegsMap.get(e);

        if (currRegs != null) {
            throw new QueryStateException(
                "Cannot update existing tuple registers for expression: " +
                e.display());
        }

        theTupleRegsMap.put(e, regs);
    }

    int[] getTupleRegs(Expr e) {
        return theTupleRegsMap.get(e);
    }

    @Override
    void exit(ExprReceive e) {

        PlanIter inputIter = theIters.pop();
        int resultReg = inputIter.getResultReg();

        PlanIter[] pushedExternalIters = null;

        if (e.getPushedExternals() != null) {

            ArrayList<Expr> pushedExternalExprs = e.getPushedExternals();
            int size = pushedExternalExprs.size();

            pushedExternalIters = new PlanIter[size];

            for (int i = 0; i < size; ++i) {

                Expr expr = pushedExternalExprs.get(i);

                if (expr == null) {
                    pushedExternalIters[i] = null;
                } else {
                    theWalker.walk(expr);
                    PlanIter iter = theIters.pop();
                    pushedExternalIters[i] = iter;
                }
            }
        }

        /*
         * If the ReceiveIter is the root iter, it just propagates to its
         * output the FieldValues (most likely RecordValues) it receves from
         * the RNs. Otherwise, if its input iter produces tuples, the
         * ReceiveIter will recreate these tuples at its output by unnesting
         * into tuples the RecordValues arriving from the RNs.
         */
        int[] tupleRegs = null;

        if (inputIter.producesTuples() && theQCB.getRootExpr() != e) {
            tupleRegs = inputIter.getTupleRegs();
        }

        PlanIter iter = new ReceiveIter(
            e, resultReg, tupleRegs, inputIter, e.getType().getDef(),
            e.getSortFieldPositions(), e.getSortSpecs(),
            e.getDistributionKind(), e.getPrimaryKey(),
            pushedExternalIters,
            theQCB.getNumRegs(), theQCB.getNumIterators());

        theIters.push(iter);
    }

    @Override
    void exit(ExprConst e) {

        int resultReg = allocateResultReg(e);

        PlanIter constIter = new ConstIter(e, resultReg, e.getValue());
        theIters.push(constIter);
    }

    @Override
    void exit(ExprVar e) {

        String name = e.getName();
        PlanIter varIter;

        if (e.isExternal()) {
            int resultReg = allocateResultReg(e);
            varIter = new ExternalVarRefIter(e, resultReg, e.getId(), name);
        } else {
            /*
             * The registers for a var expr are allocated by the expr that
             * defines the variable.
             */
            int resultReg = getResultReg(e);
            int[] inputTupleRegs = getTupleRegs(e);
            varIter = new VarRefIter(e, resultReg, inputTupleRegs, name);
        }

        theIters.push(varIter);
    }

    @Override
    void exit(ExprArrayConstr e) {

        int numArgs = e.getNumArgs();

        PlanIter argIters[] = new PlanIter[numArgs];

        for (int i = 0; i < e.getNumArgs(); ++i) {
            argIters[numArgs - i - 1] = theIters.pop();
        }

        int resultReg = allocateResultReg(e);

        PlanIter iter = new ArrayConstrIter(e, resultReg, argIters);
        theIters.push(iter);
    }

    @Override
    void exit(ExprFuncCall e) {

        int numArgs = e.getNumArgs();

        PlanIter argIters[] = new PlanIter[numArgs];

        for (int i = 0; i < e.getNumArgs(); ++i) {
            argIters[numArgs - i - 1] = theIters.pop();
        }

        PlanIter iter = e.getFunction().codegen(this, e, argIters);
        theIters.push(iter);
    }

    @Override
    void exit(ExprPromote e) {

        PlanIter inputIter = theIters.pop();
        int resultReg;

        if (inputIter.producesTuples()) {
            resultReg = inputIter.getResultReg();
        } else {
            resultReg = allocateResultReg(e);
        }

        PlanIter promoteIter = new PromoteIter(
            e, resultReg, inputIter, e.getTargetType());

        theIters.push(promoteIter);
    }

    @Override
    boolean enter(ExprFieldStep e) {

        ExprVar ctxItemVar = e.getCtxItemVar();

        if (ctxItemVar != null) {
            allocateResultReg(ctxItemVar);
        }

        return true;
    }

    @Override
    void exit(ExprFieldStep e) {

        int fieldPos = -1;
        String fieldName = e.getFieldName();

        PlanIter fieldNameIter = null;
        int ctxItemReg = -1;
        PlanIter inputIter;
        int resultReg;

        /*
         * If we don't have a known field name to select, the field name will be
         * computed in runtime. In this case, if the field name expr references
         * the ctx item, get the register that will hold the value of this var.
         * Furthermore, even if the input iterator produces tuples, we have to
         * allocate a new result reg for the FieldStepIter and "copy" into it
         * the field to select, because if the field to select is different for
         * each input tuple, we cannot update theResultReg of the FieldStepIter
         * to point to the tuple reg that stores the current field to select.
         */
        if (!e.isConst()) {
            fieldNameIter = theIters.pop();
            inputIter = theIters.pop();

            ExprVar ctxItemVar = e.getCtxItemVar();
            ctxItemReg = (ctxItemVar != null ? getResultReg(ctxItemVar) : -1);

            resultReg = allocateResultReg(e);

        } else {
            inputIter = theIters.pop();

            if (inputIter.producesTuples()) {
                ExprType inType = e.getInput().getType();
                RecordDefImpl recDef = (RecordDefImpl)inType.getDef();
                fieldPos = recDef.getFieldPos(fieldName);
                int[] inputTupleRegs = inputIter.getTupleRegs();
                resultReg = inputTupleRegs[fieldPos];
            } else {
                resultReg = allocateResultReg(e);
            }
        }

        PlanIter iter = new FieldStepIter(
            e, resultReg, inputIter, fieldNameIter,
            e.getFieldName(), fieldPos, ctxItemReg);

        theIters.push(iter);
    }

    @Override
    boolean enter(ExprSliceStep e) {

        ExprVar ctxItemVar = e.getCtxItemVar();

        if (ctxItemVar != null) {
            allocateResultReg(ctxItemVar);
        }

        return true;
    }

    @Override
    void exit(ExprSliceStep e) {

        PlanIter inputIter;
        PlanIter lowIter = null;
        PlanIter highIter = null;
        int ctxItemReg = -1;
        int resultReg;

        /*
         * If we don't have known boundaries, the boundaries will be computed
         * in runtime. In this case, if a boundary expr references the ctx item,
         * get the register that will hold the value of this var.
         */
        if (!e.isConst()) {
            highIter = (e.getHighExpr() != null ? theIters.pop() : null);
            lowIter = (e.getLowExpr() != null ? theIters.pop() : null);
            inputIter = theIters.pop();

            ExprVar ctxItemVar = e.getCtxItemVar();
            ctxItemReg = (ctxItemVar != null ? getResultReg(ctxItemVar) : -1);
        } else {
            inputIter = theIters.pop();
        }

        resultReg = allocateResultReg(e);

        PlanIter iter = new SliceStepIter(
            e, resultReg, inputIter, lowIter, highIter,
            e.getLowValue(), e.getHighValue(), ctxItemReg);

        theIters.push(iter);
    }

    @Override
    boolean enter(ExprFilterStep e) {

        ExprVar ctxItemVar = e.getCtxItemVar();
        ExprVar ctxElemVar = e.getCtxElemVar();
        ExprVar ctxElemPosVar = e.getCtxElemPosVar();
        ExprVar ctxKeyVar = e.getCtxKeyVar();

        if (ctxItemVar != null) {
            allocateResultReg(ctxItemVar);
        }

        if (ctxElemVar != null) {
            allocateResultReg(ctxElemVar);
        }

        if (ctxElemPosVar != null) {
            allocateResultReg(ctxElemPosVar);
        }

        if (ctxKeyVar != null) {
            allocateResultReg(ctxKeyVar);
        }

        return true;
    }

    @Override
    void exit(ExprFilterStep e) {

        PlanIter predIter = (e.getPredExpr() != null ? theIters.pop() : null);

        PlanIter inputIter = theIters.pop();

        ExprVar ctxItemVar = e.getCtxItemVar();
        int ctxItemReg = (ctxItemVar != null ? getResultReg(ctxItemVar) : -1);

        ExprVar ctxElemVar = e.getCtxElemVar();
        int ctxElemReg = (ctxElemVar != null ? getResultReg(ctxElemVar) : -1);

        ExprVar ctxElemPosVar = e.getCtxElemPosVar();
        int ctxElemPosReg = (ctxElemPosVar != null ?
                             getResultReg(ctxElemPosVar) : -1);

        ExprVar ctxKeyVar = e.getCtxKeyVar();
        int ctxKeyReg = (ctxKeyVar != null ? getResultReg(ctxKeyVar) : -1);

        int resultReg = allocateResultReg(e);

        PlanIter iter = new FilterStepIter(
            e, resultReg, inputIter, predIter,
            ctxItemReg, ctxElemReg, ctxElemPosReg, ctxKeyReg);

        theIters.push(iter);
    }

    @Override
    boolean enter(ExprBaseTable e) {

        int numTupleRegs =
            ((RecordDefImpl)(e.getType().getDef())).getNumFields();

        int[] tupleRegs = allocateTupleRegs(e, numTupleRegs);
        int resultReg = allocateResultReg(e);

        PlanIter[] pushedExternalIters =  null;

        if (e.getPushedExternals() != null) {

            ArrayList<Expr> pushedExternalExprs = e.getPushedExternals();
            int size = pushedExternalExprs.size();

            pushedExternalIters = new PlanIter[size];

            for (int i = 0; i < size; ++i) {

                Expr expr = pushedExternalExprs.get(i);

                if (expr == null) {
                    pushedExternalIters[i] = null;
                } else {
                    theWalker.walk(expr);
                    PlanIter iter = theIters.pop();
                    pushedExternalIters[i] = iter;
                }
            }
        }

        PlanIter tableIter = new BaseTableIter(
            e, resultReg, tupleRegs, e.getTable(),
            e.getDirection(),
            e.getPrimaryKey(), e.getSecondaryKey(),
            e.getRange(), e.getUsesCoveringIndex(),
            pushedExternalIters);

        theIters.push(tableIter);

        /*
         * Return false so that no codegen is done for theFilteringPred, if
         * any. Codegen for theFilteringPred cannot be done here because no
         * registers have been allocated for the variable associated with
         * this ExprBaseTable. Instead, it will be done by the SFW expr (see
         * below).
         */
        return false;
    }

    @Override
    boolean enter(ExprSFW e) {

        Expr fromExpr = e.getFromExpr();

        theWalker.walk(fromExpr);
        theFromIter = theIters.pop();

        ExprVar fromVar = e.getFromVar();

        setResultReg(fromVar, theFromIter.getResultReg());
        setTupleRegs(fromVar, theFromIter.getTupleRegs());

        /* See comment above, in enter(ExprBaseTable e) */
        if (fromExpr.getKind() == ExprKind.BASE_TABLE) {

            ExprBaseTable tableExpr = (ExprBaseTable)fromExpr;

            if (tableExpr.getFilteringPred() != null) {
                theWalker.walk(tableExpr.getFilteringPred());
                ((BaseTableIter)theFromIter).setFilterIter(theIters.pop());
            }
        }

        PlanIter whereIter = null;
        if (e.getWhereExpr() != null) {
            theWalker.walk(e.getWhereExpr());
            whereIter = theIters.pop();
        }

        int numFields = e.getNumFields();
        PlanIter[] selectIters = new PlanIter[numFields];
        int[] tupleRegs = null;
        int resultReg = allocateResultReg(e);

        tupleRegs = new int[numFields];

        for (int i = 0; i < numFields; ++i) {
            theWalker.walk(e.getFieldExpr(i));
            selectIters[i] = theIters.pop();
            tupleRegs[i] = selectIters[i].getResultReg();
        }

        if (e.getIsSelectStar()) {
            selectIters = null;
        }

        PlanIter sfwIter = new SFWIter(
            e, resultReg, tupleRegs,
            theFromIter, fromVar.getName(), whereIter, selectIters,
            e.getFieldNamesArray());

        theIters.push(sfwIter);
        return false;
    }

    @Override
    void exit(ExprSFW e) {
        theFromIter = null;
   }
}
