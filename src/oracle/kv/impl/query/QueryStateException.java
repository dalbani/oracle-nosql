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

package oracle.kv.impl.query;

import java.io.PrintWriter;
import java.io.StringWriter;

import oracle.kv.impl.fault.WrappedClientException;

/**
 * An internal class that encapsulates illegal states in the query engine.
 * The query engine operates inside clients and servers and cannot safely
 * throw IllegalStateException as that can crash the server. This exception is
 * used to indicate problems in the engine that are most likely query engine
 * bugs but are not otherwise fatal to the system.
 *
 * On the server side this exception is caught and logged, then passed to
 * the client as a WrappedClientException, where it will be caught as
 * a simple IllegalStateException, and logged there as well, if possible.
 */
public class QueryStateException extends IllegalStateException {

    private static final long serialVersionUID = 1L;
    private final String stackTrace;

    public QueryStateException(String message) {
        super("Unexpected state in query engine:\n" + message);

        final StringWriter sw = new StringWriter(500);
        new RuntimeException().printStackTrace(new PrintWriter(sw));
        stackTrace = sw.toString();
    }

    /**
     * Wrap this exception so it can be passed to the client.
     */
    public void throwClientException() {
        throw new WrappedClientException(this);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(1000);
        sb.append(super.toString());
        sb.append("\nStack trace: ");
        sb.append(stackTrace);
        return sb.toString();
    }
}
