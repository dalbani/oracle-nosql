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

import java.util.Map;
import java.util.Set;

import oracle.kv.impl.api.table.IndexImpl.AnnotatedField;
import oracle.kv.impl.api.table.TableImpl;

/**
 * This is an interface that has a number of callbacks implemented, one for each
 * top-level query statement. At this time it's limited to DDL statements, but
 * may be expanded to include DML statements.
 *
 * An instance of this method is passed to query compilation.  It is optional.
 * If a query requires one of these methods and a StatementFactory is not
 * available, as will be the case when compiling on the client side, an
 * exception is thrown, telling the client that the query must be passed to
 * an admin.
 *
 * The interfaces do not return any state. Errors should throw an exception
 * that implements RuntimeException.
 */

public interface StatementFactory {

    public void createTable(TableImpl table,
                            boolean ifNotExists);

    public void dropTable(String tableName,
                          TableImpl table,
                          boolean ifExists,
                          boolean removeData);

    /**
     * Only one of fieldArray or annotatedFields is non-null. The latter is
     * used for full text index creation.  Properties can be null.
     */
    public void createIndex(String tableName,
                            TableImpl table,
                            String indexName,
                            String[] fieldArray,
                            AnnotatedField[] annotatedFields,
                            Map<String,String> properties,
                            String indexComment,
                            boolean ifNotExists);

    public void dropIndex(String tableName,
                          TableImpl table,
                          String indexName,
                          boolean ifExists);

    public void evolveTable(TableImpl table);

    public void describeTable(String tableName,
                              String indexName,
                              String[] fieldArray,
                              boolean describeAsJson);

    public void showTableOrIndex(String tableName,
                                 boolean showTables,
                                 boolean showIndexes,
                                 boolean asJson);

    /*
     * Security methods that read state
     */
    public void showUser(String userName,
                         boolean asJson);

    public void showRole(String role,
                         boolean asJson);

    /*
     * Security methods that modify state
     */
    public void createUser(String userName,
                           boolean isEnabled,
                           boolean isAdmin,
                           final String pass,
                           Long passLifetimeMillis);

    public void createExternalUser(String userName,
                                   boolean isEnabled,
                                   boolean isAdmin);

    public void alterUser(String userName,
                          Boolean isEnabled,
                          final String pass,
                          boolean retainPassword,
                          boolean clearRetainedPassword,
                          Long passLifetimeMillis);

    public void dropUser(String userName);

    public void createRole(String role);

    public void dropRole(String role);

    public void grantRolesToUser(String userName,
                                 String[] roles);

    public void grantRolesToRole(String roleName,
                                 String[] roles);

    public void revokeRolesFromUser(String userName,
                                    String[] roles);

    public void revokeRolesFromRole(String roleName,
                                    String[] roles);

    public void grantPrivileges(String roleName,
                                String tableName,
                                Set<String> privilegeSet);

    public void revokePrivileges(String roleName,
                                 String tableName,
                                 Set<String> privilegeSet);
}
