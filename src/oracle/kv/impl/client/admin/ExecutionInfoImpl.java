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
package oracle.kv.impl.client.admin;

import java.io.Serializable;

import oracle.kv.StatementResult;

/**
 * See ExecutionInfo
 */
public class ExecutionInfoImpl implements ExecutionInfo, Serializable {

    private static final long serialVersionUID = 1L;
    private static final int CURRENT_VERSION = 1;
    private final int planId;
    private final boolean isTerminated;
    private final String info;
    private final String infoAsJson;
    private final boolean isSuccess;
    private final String errorMessage;
    private final boolean isCancelled;
    private final boolean needsCancel;
    private final String results;

    public ExecutionInfoImpl(int planId,
                             boolean isTerminated,
                             String info,
                             String infoAsJson,
                             boolean isSuccess,
                             boolean isCancelled,
                             String errorMessage,
                             boolean needsCancel,
                             String results) {
        this.planId = planId;
        this.isTerminated = isTerminated;
        this.info = info;
        this.infoAsJson = infoAsJson;
        this.isSuccess = isSuccess;
        this.isCancelled = isCancelled;
        this.errorMessage = errorMessage;
        this.needsCancel = needsCancel;
        this.results = results;
    }

    ExecutionInfoImpl(StatementResult pastResult) {
        this.planId = pastResult.getPlanId();
        this.isTerminated = pastResult.isDone();
        this.info = pastResult.getInfo();
        this.infoAsJson = pastResult.getInfoAsJson();
        this.isSuccess = pastResult.isSuccessful();
        this.errorMessage = pastResult.getErrorMessage();
        this.isCancelled = pastResult.isCancelled();

        /*
         * Needs cancel is only set when an ExecutionInfo is generated
         * by the Admin service. This constructor is used when creating new
         * DDLFutures for the proxy server.
         */
        this.needsCancel = false;
        this.results = pastResult.getResult();
    }

    /**
     * Lets the client check that it knows how to parse this ExecutionInfo.
     */
    @Override
        public int getVersion() {
        return CURRENT_VERSION;
    }

   @Override
    public int getPlanId() {
        return planId;
    }

    @Override
    public boolean isTerminated() {
        return isTerminated;
    }

    @Override
    public String getJSONInfo() {
        return infoAsJson;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public String toString() {
        return "ExecutionInfoImpl [planId=" + planId + ", isTerminated="
                + isTerminated + ", info=" + info
                + ", infoAsJson=" + infoAsJson + "]";
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean needsTermination() {
        return needsCancel;
    }

    @Override
    public String getResult() {
        return results;
    }
}
