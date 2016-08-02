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

package oracle.kv.impl.api.query;

import java.util.Map;

import oracle.kv.impl.api.KVStoreImpl;

import oracle.kv.StatementResult;
import oracle.kv.query.BoundStatement;
import oracle.kv.query.PreparedStatement;
import oracle.kv.query.ExecuteOptions;
import oracle.kv.table.FieldDef;
import oracle.kv.table.RecordDef;

/**
 * Encapsulates a "prepared" DDL query, which is just the original query string.
 * That is, DDL queries are never prepared locally (at the client or query
 * coordinator); they are always routed to an admin.
 */
public class PreparedDdlStatementImpl
    implements PreparedStatement,
               InternalStatement {

    private final String query;

    public PreparedDdlStatementImpl(String query) {
        this.query = query;
    }

    /*
     * DDL operations do not (at this time) have metadata.  This may change for
     * some of them (e.g. show, describe, etc) - those which have logical
     * tabular views.
     */
    @Override
    public RecordDef getResultDef() {
        return null;
    }

    @Override
    public Map<String, FieldDef> getVariableTypes() {
        throw new IllegalArgumentException("Cannot bind a DDL query");
    }

    @Override
    public FieldDef getVariableType(String variableName) {
        throw new IllegalArgumentException("Cannot bind a DDL query");
    }

    @Override
    public BoundStatement createBoundStatement() {
        throw new IllegalArgumentException("Cannot bind a DDL query");
    }

    @Override
    public String toString() {
        return query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public StatementResult executeSync(
        KVStoreImpl store,
        ExecuteOptions options) {

        /* DDL queries are routed to an admin */
        return store.executeSync(query);
    }
}
