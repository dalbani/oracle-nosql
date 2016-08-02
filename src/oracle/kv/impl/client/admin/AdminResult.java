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

import oracle.kv.StatementResult;

/**
 * AdminResult packages information about a ddl statement that is either
 * currently executing, or has completed. The AdminResult is returned from the
 * ExecutionFuture, so the AdminResult is the vehicle used to convey this
 * information to the API caller. It's basically a reformulation of the
 * ExecutionInfo class, which packages the info delivered from the Admin
 * service to client.
 *
 * In other words,
 *
 *     kvclient <-- ExecutionInfo -- kvstore
 *
 *     application <-- AdminResult/StatementResult -- kvclient
 */
class AdminResult implements StatementResult {

    private final boolean success;
    private final int planId;
    private final String info;
    private final String jsonInfo;
    private final String errorMessage;
    private final boolean isDone;
    private final boolean isCancelled;
    private final String result;

    AdminResult(int planId,
                ExecutionInfo executionInfo,
                boolean isDone,
                boolean isCancelled) {
        this.planId = planId;
        this.isDone = isDone;
        this.isCancelled = isCancelled;
        if (executionInfo == null) {
            if (planId == 0) {

                /*
                 * This is a no-execute operation, nothing more needs to be
                 * done.
                 */
                this.success = true;
                this.info = 
                    "The statement did not require any additional execution";
            } else {
                /*
                 * This operation is in progress, but no execution info is
                 * available because the future was created by the Thrift
                 * proxy.
                 */
                this.success = false;
                this.info = null;
            }
            this.errorMessage = null;
            this.jsonInfo = null;
            this.result = null;
        } else {
            /* We have info from the server. */
            this.success = executionInfo.isSuccess();
            this.info = executionInfo.getInfo();
            this.jsonInfo = executionInfo.getJSONInfo();
            this.errorMessage = executionInfo.getErrorMessage();
            this.result = executionInfo.getResult();
        }
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public int getPlanId() {
        return planId;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public String getInfoAsJson() {
        return jsonInfo;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "AdminResult [success=" + success + ", planId=" + planId
            + ",\ninfo=" + info + ", \njsonInfo=" + jsonInfo
            + ",\nerrorMessage=" + errorMessage + ", isDone=" + isDone
            + ", isCancelled=" + isCancelled + ", result=" + result
            + "]";
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public String getResult() {
        return result;
    }
}
