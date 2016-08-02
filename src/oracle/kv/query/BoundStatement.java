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

package oracle.kv.query;

import java.util.Map;

import oracle.kv.KVStore;
import oracle.kv.table.FieldValue;
import oracle.kv.table.FieldValueFactory;

/**
 * <p> Represents a {@link Statement} that has been compiled and an execution
 * plan generated for it. A BoundStatement must be used when the Statement is
 * a DML statement that contains external variables. It provides methods to
 * bind the external variables to specific values.</p>
 *
 * <p>To create an instance of a BoundStatement, an instance of a
 * {@link PreparedStatement} must be created first, which is done via the
 * {@link KVStore#prepare(String statement)} method. Next, one or more
 * BoundStatement instances can be created from the same PreparedStatement via
 * the {@link PreparedStatement#createBoundStatement() method}. Creating
 * multiple BoundStatements from the same PreparedStatement allows for the
 * potentially concurrent execution of the PreparedStatement multiple times
 * with different bind values each time.</p>
 *
 * <p>Like a PreparedStatement, a BoundStatement can be executed multiple
 * times via one of the KVStore.execute methods (for example, {@link
 * KVStore#execute(String statement, ExecuteOptions options)} or
 * {@link KVStore#executeSync(Statement statement, ExecuteOptions options) }).
 * All the external variables must be bound before execution is initiated.</p>
 *
 * <p>Objects implementing the PreparedStatement interface are thread-safe
 * and their methods are re-entrant. On the other hand, BoundStatement
 * instances are not thread-safe.</p>
 *
 * <p>{@link FieldValueFactory} to create values for complex types.</p>
 *
 * @since 4.0
 */
public interface BoundStatement
        extends PreparedStatement {

    /**
     * Returns the map of bound variables.
     * @return a map of variables that have been set.
     */
    Map<String, FieldValue> getVariables();

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, FieldValue value);

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, int value);

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, boolean value);

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, double value);

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, float value);

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, long value);

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, String value);

    /**
     * Sets the value of a variable.
     * @param variableName the name of the query variable
     * @param value the value to be set
     * @return the same object
     * @throws IllegalArgumentException if the type of value doesn't match
     * the type of the variable.
     */
    BoundStatement setVariable(String variableName, byte[] value);
}
