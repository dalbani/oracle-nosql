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

import java.util.HashSet;

import oracle.kv.table.FieldRange;
import oracle.kv.table.RecordDef;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.table.TableAPIImpl;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.PrimaryKeyImpl;
import oracle.kv.impl.api.table.IndexKeyImpl;
import oracle.kv.impl.api.table.TableMetadata;

import oracle.kv.impl.query.compiler.parser.KVParser;

import oracle.kv.impl.query.runtime.PlanIter;

import oracle.kv.impl.util.CommonLoggerUtils;

/**
 * The query control block.
 *
 * theTableMeta:
 *
 * theInitSctx:
 * The top-level static context for the query (for now, there is actually no
 * nesting of static contexts (no need for scoping within a query)), so
 * theInitSctx is the only sctx obj used by the query.
 *
 * theException:
 *
 * generatedNames:
 * A set of names generated internally for use in otherwise unnamed maps and
 * arrays that need them (for Avro schema generation). This set guarantees
 * uniqueness, which is also required by Avro.
 */
public class QueryControlBlock {

    private final TableAPIImpl theTableAPI;

    private final String theQueryString;

    private final TableMetadata theTableMeta;

    private final StatementFactory theStatementFactory;

    private final StaticContext theInitSctx;

    private int theInternalVarCounter = 0;

    private int theNumRegs = 0;

    private int theNumPlanIters = 0;

    private RecordDef theResultDef;

    private Expr theRootExpr;

    private PlanIter theRootPlanIter;

    private RuntimeException theException = null;

    private final HashSet<String> generatedNames = new HashSet<String>();

    PrimaryKeyImpl thePushedPrimaryKey;

    IndexKeyImpl thePushedSecondaryKey;

    /* For unit testing only */
    FieldRange thePushedRange;

    QueryControlBlock(
        TableAPIImpl tableAPI,
        String queryString,
        StaticContext sctx) {

        theTableAPI = tableAPI;
        theQueryString = queryString;
        theTableMeta = tableAPI.getTableMetadata();
        theInitSctx = sctx;
        theStatementFactory = null;
    }

    QueryControlBlock(
        TableMetadata metadata,
        StatementFactory statementFactory,
        String queryString,
        StaticContext sctx) {

        theTableAPI = null;
        theQueryString = queryString;
        theTableMeta = metadata;
        theInitSctx = sctx;
        theStatementFactory = statementFactory;
    }

    KVStoreImpl getStore() {
        return theTableAPI.getStore();
    }

    TableMetadata getTableMeta() {
        return theTableMeta;
    }

    public StaticContext getInitSctx() {
        return theInitSctx;
    }

    StatementFactory getStatementFactory() {
        return theStatementFactory;
    }

    public RuntimeException getException() {
        return theException;
    }

    public boolean succeeded() {
        return theException == null;
    }

    public String getErrorMessage() {
        return CommonLoggerUtils.getStackTrace(theException);
    }

    Expr getRootExpr() {
        return theRootExpr;
    }

    void setRootExpr(Expr e) {
        theRootExpr = e;
    }

    public PrimaryKeyImpl getPushedPrimaryKey() {
        return thePushedPrimaryKey;
    }

    public IndexKeyImpl getPushedSecondaryKey() {
        return thePushedSecondaryKey;
    }

    public FieldRange getPushedRange() {
        return thePushedRange;
    }

    public void setPushedPrimaryKey(PrimaryKeyImpl key) {
        thePushedPrimaryKey = key;
    }

    public void setPushedSecondaryKey(IndexKeyImpl key) {
        thePushedSecondaryKey = key;
    }

    public void setPushedRange(FieldRange range) {
        thePushedRange = range;
    }

    /* Will we ever need generic FieldDef as the result? */
    public RecordDef getResultDef() {
        return theResultDef;
    }

    /**
     * The caller is responsible for determining success or failure by
     * calling QueryControlBlock.succeeded(). On failure there may be
     * an exception which can be obtained using
     * QueryControlBlock.getException().
     */
    void compile() {

        KVParser parser = new KVParser();
        parser.parse(theQueryString);

        if (!parser.succeeded()) {
            theException = parser.getParseException();
            return;
        }

        Translator translator = new Translator(this);
        translator.translate(parser.getParseTree());
        theException = translator.getException();

        if (theException != null) {
            return;
        }

        if (translator.isQuery()) {
            theRootExpr = translator.getRootExpr();
            theResultDef = (RecordDef)theRootExpr.getType().getDef();

            OptRulePushIndexPreds rule = new OptRulePushIndexPreds();
            rule.apply((ExprSFW)theRootExpr);
            theException = rule.getException();

            if (theException != null) {
                return;
            }

            Distributer distributer = new Distributer(this);
            distributer.distributeQuery();

            /*
             * Refresh the result def; the optimizer could have infered a more
             * tight type than the translator
             */
            FieldDefImpl resDef = theRootExpr.getType().getDef();
            if (resDef.isRecord()) {
                theResultDef = resDef.asRecord();
            } else {
                assert(resDef.isEmpty());
            }

            CodeGenerator codegen = new CodeGenerator(this);
            codegen.generatePlan(theRootExpr);
            theException = codegen.getException();

            if (theException == null) {
                theRootPlanIter = codegen.getRootIter();
            }
        }

        return;
    }

    void parse() {

        KVParser parser = new KVParser();
        parser.parse(theQueryString);

        if (!parser.succeeded()) {
            theException = parser.getParseException();
            return;
        }

        Translator translator = new Translator(this);
        translator.translate(parser.getParseTree());
        theException = translator.getException();

        if (theException != null) {
            return;
        }

        if (translator.isQuery()) {
            theRootExpr = translator.getRootExpr();

            OptRulePushIndexPreds rule = new OptRulePushIndexPreds();
            rule.apply((ExprSFW)theRootExpr);
            theException = rule.getException();
        }

        return;
    }

    String createInternalVarName(String prefix) {

        if (prefix == null) {
            return "$internVar-" + theInternalVarCounter++;
        }

        return "$" + prefix + "-" + theInternalVarCounter++;
    }

    void incNumRegs(int num) {
        theNumRegs += num;
    }

    public int getNumRegs() {
        return theNumRegs;
    }

    public int incNumPlanIters() {
        return theNumPlanIters++;
    }

    public int getNumIterators() {
        return theNumPlanIters;
    }

    public PlanIter getQueryPlan() {
        return theRootPlanIter;
    }

    public String displayExprTree() {
        return theRootExpr.display();
    }

    public String displayQueryPlan() {
        return theRootPlanIter.display();
    }

    /**
     * Use the generatedNames set to generate a unique name based on the
     * prefix, which is unique per-type (record, enum, binary).  Avro
     * requires generated names for some data types that otherwise do not
     * need them in the DDL.
     */
    String generateFieldName(String prefix) {
        final String gen = "_gen";
        int num = 0;
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(gen);
        String name = sb.toString();
        while (generatedNames.contains(name)) {
            sb.append(num++);
            name = sb.toString();
        }
        generatedNames.add(name);
        return name;
    }
}
