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
import oracle.kv.table.FieldDef;
import oracle.kv.table.RecordDef;

/**
 * <p>Represents a {@link Statement} that has been compiled and an execution
 * plan generated for it. A PreparedStatement should be used when it is
 * expected that the same statement will be executed multiple times. This way,
 * the cost of compilation is paid only once.</p>
 *
 * <p>An instance of PreparedStatement can be created via the
 * {@link KVStore#prepare(String statement)} method. This instance can then be
 * executed multiple times via one of the KVStore.execute methods (for example,
 * {@link KVStore#execute(String statement, ExecuteOptions options)} or
 * {@link KVStore#executeSync(Statement statement, ExecuteOptions options)}).</p>
 *
 * <p>If the statement is a DML statement that contains external variables,
 * these variables must be bound to specific values before the statement can be
 * executed. To allow for potentially concurrent execution of the same
 * PreparedStatement multiple times with different bind values each time,
 * binding of external variables must be done via one or more instances of
 * the {@link BoundStatement} interface. Such instances are created via the
 * #createBoundStatement() method. It is then the BoundStatement instances
 * that must be executed (i.e., passed as input to the KVStore execute
 * methods) instead of the PreparedStatement instance</p>
 *
 * <p>Objects implementing the PreparedStatement interface are thread-safe
 * and their methods are re-entrant. On the other hand, {@link BoundStatement}
 * instances are not thread-safe.</p>
 *
 * @since 4.0
 */
public interface PreparedStatement extends Statement {

    /**
     * Returns the definition of the result of this Statement.
     */
    RecordDef getResultDef();

    /**
     * Returns the types of the variables.
     */
    Map<String, FieldDef> getVariableTypes();

    /**
     * Returns the type of the given variableName or {@code null} if it
     * doesn't exist.
     */
    FieldDef getVariableType(String variableName);

    /**
     * Creates a new BoundStatement object for this PreparedStatement.
     */
    BoundStatement createBoundStatement();
}
