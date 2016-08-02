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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import oracle.kv.impl.util.JsonUtils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * This class provides utilities for interaction with Jackson JSON processing
 * libraries, as well as helpful JSON operations, for creating command result
 * JSON output.
 */
public class CommandJsonUtils extends JsonUtils {
    /*
     * These string constants are used for construction of command result
     * JSON fields.
     */
    public static final String FIELD_OPERATION = "operation";
    public static final String FIELD_RETURN_CODE = "return_code";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_RETURN_VALUE = "return_value";
    public static final String FIELD_CLEANUP_JOB = "cmd_cleanup_job";

    /**
     * Return a command result JSON output based on the operation and
     * CommandResult. A sample JSON output:
     * {
     *   "operation" : "configure",
     *   "return_code" : 5400,
     *   "return_value" : "",
     *   "description" : "Deploy failed: ConnectionIOException ......" ,
     *   "cmd_cleanup_job" : [ "clean-store.kvs -config store.config" ]
     * }
     * @param operation the name set in "operation" JSON field
     * @param result contains the information set in other JSON fields
     * @return return the created JSON output string
     * @throws IOException if Jackson create JSON output string error
     */
    public static String getJsonResultString(String operation,
                                             CommandResult result)
        throws IOException {

        final ObjectNode jsonTop = createObjectNode();
        updateNodeWithResult(jsonTop, operation, result);
        return toJsonString(jsonTop);
    }

    /**
     * Add command result JSON fields to jsonTop node.
     * <p>
     * For example:
     * jsonTop is:
     * {
     *   "name" : <plan_name>,
     *   "id" : <plan_id>,
     *   "state" : <plan_state>
     * }
     * It will be updated as following: 
     * {
     *   "name" : <plan_name>,
     *   "id" : <plan_id>,
     *   "state" : <plan_state>,
     *   "operation" : "plan deploy-admin",
     *   "return_code" : 5400,
     *   "return_value" : "",
     *   "description" : "Deploy failed: ConnectionIOException ......" ,
     *   "cmd_cleanup_job" : [ "clean-store.kvs -config store.config" ]
     * }
     * @param jsonTop the JSON node to be updated
     * @param operation the name set in "operation" JSON field
     * @param result the created JSON output string
     * @throws IOException if Jackson update JSON node error
     */
    public static void updateNodeWithResult(ObjectNode jsonTop,
                                            String operation,
                                            CommandResult result)
        throws IOException {

        if (result == null) {
            return;
        }
        jsonTop.put(FIELD_OPERATION, operation);
        jsonTop.put(FIELD_RETURN_CODE, result.getErrorCode());
        jsonTop.put(FIELD_DESCRIPTION, result.getDescription());
        final String returnValueJsonStr = result.getReturnValue();
        if (returnValueJsonStr != null) {
            final JsonParser valueParser = JsonUtils.createJsonParser(
                new ByteArrayInputStream(returnValueJsonStr.getBytes()));
            JsonNode returnValueNode = valueParser.readValueAsTree();
            jsonTop.put(FIELD_RETURN_VALUE, returnValueNode);
        }
        if (result.getCleanupJobs() != null) {
            ArrayNode cleanupJobNodes = jsonTop.putArray(FIELD_CLEANUP_JOB);
            for(String job: result.getCleanupJobs()) {
                cleanupJobNodes.add(job);
            }
        }
    }

    /**
     * Return the JSON string to present the jsonTop node.
     */
    public static String toJsonString(ObjectNode jsonTop)
        throws IOException {

        final ObjectWriter writer = createWriter(true /* pretty */);
        return writer.writeValueAsString(jsonTop);
    }
}
