/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2015 Oracle and/or its affiliates.  All rights reserved.
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

package oracle.kv.impl.admin;

import oracle.kv.KVSecurityException;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.api.table.query.TableDdl;
import oracle.kv.impl.fault.ClientAccessException;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.security.AccessChecker;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.OperationContext;
import oracle.kv.impl.security.SessionAccessException;

/**
 * This class encapsulates DDL requests to the admin. The requests are
 * presented as DDL statements, which are strings. The statements are parsed
 * and results handled in this class, which provides success/failure state as
 * well as additional information required to return information to callers,
 * which are remote clients for the most part.
 *
 * This class does not throw any exceptions.  Upon completion a successful
 * operation results in this state:
 * 1. getSuccess() returns true
 * 2. One of the following:
 *  2a. A plan was created and is running.  getPlanId() returns a non-zero value
 *  2b. A statement with "if exists" or "if not exists" was presented and
 *  resulted in a no-op.  getPlanId() returns 0, getResultString() returns null
 *  2c. A statement that returns a String was run.  getResultString() returns a
 *  non-null value, getPlanId() returns 0
 *
 * On failure:
 * 1.  getSuccess() returns false
 * 2.  getErrorMessage() has a non-null value
 * 3.  if the error is transient, in that the operation can be retried,
 * canRetry() returns true.  This will be the case for most plan execution
 * failures.  Failures that occur before plan execution are not transient and
 * if retried will still fail.
 *
 * TODO:
 * o finish describe
 */
public class DdlHandler {

    private final TableDdl tableDdl;
    private final Admin admin;
    private boolean success;
    private String errorString;
    private String resultString;
    private int planId;
    private boolean hasPlan;
    private boolean canRetry;
    private final String statement;
    private final DdlOperationExecutor ddlOpExecutor;

    /**
     * Constructs a DdlHandler and executes the statement.
     */
    DdlHandler(String statement, Admin admin, AccessChecker accessChecker) {
        this.admin = admin;
        this.statement = statement;
        this.ddlOpExecutor = new DdlOperationExecutor(this, accessChecker);
        TableMetadata metadata =
            admin.getMetadata(TableMetadata.class,
                              MetadataType.TABLE);
        /*
         * This parses and executes the statement.
         */
        tableDdl = TableDdl.parse(statement, metadata);
        success = tableDdl.succeeded();

        /*
         * Handle the results of the parse.
         */
        handleResults();
    }

    /**
     * Returns if the operation succeeded or not.
     */
    boolean getSuccess() {
        return success;
    }

    /**
     * Returns an error String if an error occurred (!success), null if not.
     */
    String getErrorMessage() {
        return errorString;
    }

    /**
     * Returns a result string if the operation is synchronous and returns
     * a result, and it was successful, otherwise null.
     */
    String getResultString() {
        return resultString;
    }

    void setResultString(String resultStr) {
        this.resultString = resultStr;
    }

    /**
     * Returns a plan ID if the operation resulted in an executed plan
     * (hasPlan == true), otherwise 0 (undefined).
     */
    int getPlanId() {
        return planId;
    }

    /**
     * Returns whether the operation can be retried.  This value is only valid
     * on errors.
     */
    boolean canRetry() {
        return canRetry;
    }

    /**
     * Returns true if the operation resulted in plan execution.
     */
    boolean hasPlan() {
        return hasPlan;
    }

    /**
     * Returns true if the operation was a describe.
     */
    boolean isDescribe() {
        return tableDdl.isDescribe();
    }

    /**
     * Returns true if the operation was a show
     */
    boolean isShow() {
        return tableDdl.isShow();
    }

    Admin getAdmin() {
        return admin;
    }

    /**
     * Tell the DDLHandler that the statement has finished successfully
     */
    void operationSucceeds() {
        success = true;
    }

    /**
     * Tell the DDLHandler that the statement has failed, and record failure
     * information
     * @param errMsg error message to set in ddlhandler
     */
    void operationFails(String errMsg) {
        success = false;
        errorString = errMsg;
    }

    /**
     * Handles the result of the parse.  If it was successful a plan may need
     * to be created and executed.  If so, do that.  If the operation is
     * synchronous then there is nothing to do other than create the
     * resultString.  If the operation failed, the errorString is set.  Note
     * that in a secure kvstore, a security check will be done before the real
     * execution of operations.
     */
    private void handleResults() {

        /*
         * Handle parse and parse tree processing errors
         */
        if (!success) {
            errorString = tableDdl.getErrorMessage();
            return;
        }

        DdlOperation ddlOp = tableDdl.getDdlOperation();
        if (ddlOp == null) {
            throw new IllegalStateException("Problem parsing " + statement +
                                            ": " + tableDdl);
        }
        ddlOpExecutor.execute(ddlOp);
    }

    void approveAndExecute(int planId1) {
        this.planId = planId1;
        approveAndExecute();
    }

    /**
     * Approves and executes the plan.  If execution fails, cancel the plan.
     *
     * A hole exists in plan execution, where concurrent statement execution
     * can cause a spurious exception for the IF NOT EXISTS statement.
     *
     * But the lack of idempotency in table plans causes these
     * problems. Specifically, ddl execution consists of two steps:
     *
     * 1) parsing/metadata checks
     * 2) plan execution
     *
     * When creating a table or index, step 1 complains if the table or index
     * is already in the metadata. But because plan execution isn't fully
     * idempotent, the following interleaving can happen with concurrent
     * statements:
     *
     * Statement A does parsing/metadata check
     * Statement A' does parsing/metadata check
     * Statement A executes plan, completes
     * Statement A' executes plan, but gets error because the plan isn't
     * idempotent -- i.e. the index exists, etc.
     *
     * This is akin to the problem if a index or table creation is cancelled
     * before the index or table becomes READY. In that case, the plan must be
     * re-executed, but because there's been no cleanup, and also because the
     * plan is not idempotent, there's no easy way to make progress.
     */
    void approveAndExecute() {
        assert planId != 0;
        try {
            admin.approvePlan(planId);
            planId = admin.executePlanOrFindMatch(planId);
            hasPlan = true;
        } catch (IllegalCommandException ice) {
            cleanFailedDdlPlan(ice);
            canRetry = false; /* this error not to be tried again */
        } catch (PlanLocksHeldException plhe) {
            cleanFailedDdlPlan(plhe);
            canRetry = true; /* this error can be tried again */
        }
    }

    /* Clean up the failed DDL plan */
    private void cleanFailedDdlPlan(Exception e) {
        /*
         * Plan execution usually fails because there's a running plan.
         * If this happens, cancel the current plan.
         */
        errorString = "Failed to execute plan: " + e.getMessage();
        /* don't let this throw past here */
        try {
            admin.cancelPlan(planId);
        } catch (Exception ignore) {
            /* ignore */
        }
        planId = 0;
        success = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Success: ");
        sb.append(success);
        if (success) {
            if (hasPlan) {
                sb.append(", plan succeeded: ");
                sb.append(planId);
            }
            if (resultString != null) {
                sb.append(", success, result string: ");
                sb.append(resultString);
            }
        } else {
            sb.append(", operation failed: ");
            sb.append(errorString);
        }
        return sb.toString();
    }

    /**
     * Runs a ddl operation.  In a secured kvstore, the permission for the
     * specific ddl operation will be checked first.
     */
    static class DdlOperationExecutor {
        private final AccessChecker accessChecker;
        private final DdlHandler ddlHandler;

        DdlOperationExecutor(DdlHandler ddlHandler,
                             AccessChecker accessChecker) {
            this.accessChecker = accessChecker;
            this.ddlHandler = ddlHandler;
        }

        void execute(DdlOperation ddlOp)
            throws SessionAccessException, ClientAccessException {

            final ExecutionContext exeCtx = ExecutionContext.getCurrent();
            if (exeCtx != null && accessChecker != null) {
                try {
                    accessChecker.checkAccess(exeCtx, ddlOp.getOperationCtx());
                } catch (KVSecurityException kvse) {
                    throw new ClientAccessException(kvse);
                }
            }
            ddlOp.perform(ddlHandler);
        }
    }

    /**
     * A class wrapping a ddl operation with the proper OperationContext which
     * will be checked in a secure kvstore.
     */
    public interface DdlOperation {

        /**
         * Returns the operation context used for security check of this ddl
         * operation.
         */
        OperationContext getOperationCtx();

        /**
         * Performs the operation.  
         */
        void perform(DdlHandler ddlHandler);
    }

    /**
     * @return true if this statement's result string is in JSON format.
     */
    boolean isJson() {
        return tableDdl.isDescribeAsJson();
    }
}
