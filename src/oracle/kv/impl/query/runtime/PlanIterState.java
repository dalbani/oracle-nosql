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

import oracle.kv.impl.query.QueryStateException;

public class PlanIterState {

    static enum StateEnum {
        OPEN,
        RUNNING,
        DONE,
        CLOSED
    }

    private StateEnum theState;

    protected PlanIterState() {
        theState = StateEnum.OPEN;
    }

    boolean isOpen() {
        return theState == StateEnum.OPEN;
    }

    boolean isClosed() {
        return theState == StateEnum.CLOSED;
    }

    public boolean isDone() {
        return theState == StateEnum.DONE;
    }

    @SuppressWarnings("unused")
    protected void reset(PlanIter iter) {
        setState(StateEnum.OPEN);
    }

    protected void close() {
        setState(StateEnum.CLOSED);
    }

    public void done() {
        setState(StateEnum.DONE);
    }

    protected void setState(StateEnum v) {
        switch (theState) {
        case RUNNING:
            if (v == StateEnum.RUNNING ||
                v == StateEnum.DONE ||
                v == StateEnum.CLOSED ||
                v == StateEnum.OPEN) {
                theState = v;
                return;
            }
            break;
        case DONE:
            if (v == StateEnum.OPEN || v == StateEnum.CLOSED) {
                theState = v;
                return;
            }
            break;
        case OPEN:
            /*
             * OPEN --> DONE transition is allowed for iterators that are "done"
             * on the 1st next() call after an open() or reset() call. In this
             * case, rather than setting the state to RUNNING on entrance to the
             * next() call and then setting the state again to DONE before
             * returning from the same next() call, we allow a direct transition
             * from OPEN to DONE.
             */
            if (v == StateEnum.RUNNING ||
                v == StateEnum.CLOSED ||
                v == StateEnum.OPEN ||
                v == StateEnum.DONE) {
                theState = v;
                return;
            }
            break;
        case CLOSED:
            break;
        }

        throw new QueryStateException(
            "Wrong state transition for iterator " + getClass() +
            ". Current state: " + theState + " New state: " + v);
    }
}
