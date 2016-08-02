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

import java.util.logging.Logger;

import oracle.kv.FaultException;
import oracle.kv.impl.api.query.PreparedDdlStatementImpl;
import oracle.kv.impl.api.query.PreparedStatementImpl;
import oracle.kv.impl.api.table.TableAPIImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.query.PreparedStatement;

/*
 * This class drives the compilation of a KVSQL program.
 */
public class CompilerAPI {

    static StaticContext theRootSctx = new StaticContext(null/*parent*/);

    static FunctionLib theFunctionLib = new FunctionLib(theRootSctx);

    static FunctionLib getFuncLib() {
        return theFunctionLib;
    }

    public static PreparedStatement prepare(
        TableAPIImpl tableAPI,
        String queryString) {

        try {
            /* Create an sctx for the query as a child of the roo sctx */
            StaticContext querySctx = new StaticContext(theRootSctx);

            QueryControlBlock qcb = new QueryControlBlock(
                tableAPI, queryString, querySctx);

            qcb.compile();

            if (qcb.succeeded()) {
                return new PreparedStatementImpl(
                    qcb.getQueryPlan(),
                    qcb.getResultDef(),
                    qcb.getNumRegs(),
                    qcb.getNumIterators(),
                    qcb.getInitSctx().getExternalVars(),
                    qcb);
            }

            /*
             * QueryStateException is an internal error in the query compiler
             * or engine (probably a bug). It is a subclass of 
             * IllegalStateException, so it can be passed on directly
             */
            if (qcb.getException() instanceof QueryStateException) {
                Logger logger = tableAPI.getStore().getLogger();
                if (logger != null) {
                    logger.warning(qcb.getException().toString());
                }
                throw qcb.getException();
            }

            /*
             * Pass FaultException directly. May be thrown if, for example,
             * there was a problem with accessing metadata about the tables
             * used in the query.
             */
            if (qcb.getException() instanceof FaultException) {
                throw qcb.getException();
            }

            /*
             * QueryException: semantic or syntactic error with the query.
             */
            if (qcb.getException() instanceof QueryException) {
                throw qcb.getException();
            }

            /*
             * Anything else translate into IllegalArgumentException
             */
            throw new IllegalArgumentException(qcb.getErrorMessage());

        } catch (DdlException ddle) {
            return new PreparedDdlStatementImpl(queryString);
        }
    }

    /**
     * Used by the admin to parse DDL statement.
     * The caller is responsible for determining success or failure by
     * calling QueryControlBlock.succeeded(). On failure there may be
     * an exception which can be obtained using
     * QueryControlBlock.getException().
     */
    public static QueryControlBlock compile(
        String queryString,
        TableMetadata metadata,
        StatementFactory statementFactory) {

        /* Create an sctx for the query as a child of the roo sctx */
        StaticContext querySctx = new StaticContext(theRootSctx);

        QueryControlBlock qcb = new QueryControlBlock(
            metadata, statementFactory, queryString, querySctx);

        qcb.compile();

        return qcb;
    }

    /**
     * Used by unit testing only
     */
    public static QueryControlBlock parse(
        String queryString,
        TableMetadata meta) {

        StaticContext initSctx = new StaticContext(theRootSctx);

        QueryControlBlock qcb = new QueryControlBlock(
            meta, null, /*statementFactory*/ queryString, initSctx);

        qcb.parse();

        /*
         * The caller is responsible for determining success or failure by
         * calling QueryControlBlock.succeeded(). On failure there may be
         * an exception which can be obtained using
         * QueryControlBlock.getException().
         */
        return qcb;
    }
}
