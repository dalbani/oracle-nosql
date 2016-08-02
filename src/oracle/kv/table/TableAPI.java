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

package oracle.kv.table;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.kv.BulkWriteOptions;
import oracle.kv.Consistency;
import oracle.kv.ConsistencyException;
import oracle.kv.Direction;
import oracle.kv.Durability;
import oracle.kv.DurabilityException;
import oracle.kv.EntryStream;
import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.ParallelScanIterator;
import oracle.kv.RequestTimeoutException;
import oracle.kv.Version;

/**
 * TableAPI is a handle for the table interface to an Oracle NoSQL
 * store. Tables are an independent layer implemented on top
 * of the {@link KVStore key/value interface}.  While the two interfaces
 * are not incompatible, in general applications will use one or the other.
 * To create a TableAPI instance use {@link KVStore#getTableAPI getTableAPI()}.
 * <p>
 * The table interface is required to use secondary indexes and supported data
 * types.
 * <p>
 * Tables are similar to tables in a relational database.  They are named and
 * contain a set of strongly typed records, called rows.  Rows in an Oracle
 * NoSQL Database table are analogous to rows in a relational system and each
 * row has one or more named, typed data values.  These fields can be compared
 * to a relational database column.  A single top-level row in a table is
 * contained in a {@link Row} object.  Row is used as return value for TableAPI
 * get operations as well as a key plus value object for TableAPI put
 * operations. All rows in a given table have the same fields.  Tables have a
 * well-defined primary key which comprises one or more of its fields, in
 * order.  Primary key fields must be simple (single-valued) data types.
 * <p>
 * The data types supported in tables are well-defined and include simple
 * single-valued types such as Integer, String, Date, etc., in addition to
 * several complex, multi-valued types -- Array, Map, and Record.  Complex
 * objects allow for creation of arbitrarily complex, nested rows.
 * <p>
 * All operations on this interface include parameters that supply optional
 * arguments to control non-default behavior.  The types of these parameter
 * objects varies depending on whether the operation is a read, update,
 * or a multi-read style operation returning more than one result or an
 * iterator.
 * <p>
 * In order to control, and take advantage of sharding across partitions tables
 * may be defined in a hierarchy.  A top-level table is one without a parent
 * and may be defined in a way such that its primary key spreads the table rows
 * across partitions.  The primary key for this sort of table has a
 * <em>complete</em> shard key but an empty minor key.  Tables with parents
 * always have a primary key with a minor key.  The primary key of a child
 * table comprises the primary key of its immediate parent plus the fields
 * defined in the child table as being part of its primary key.  This means
 * that the fields of a child table implicitly include the primary key fields
 * of all of its ancestors.
 * <p>
 * Some of the methods in this interface include {@link MultiRowOptions} which
 * can be used to cause operations to return not only rows from the target
 * table but from its ancestors and descendant tables as well.  This allows
 * for efficient and transactional mechanisms to return related groups of rows.
 * The MultiRowOptions object is also used to specify value ranges that apply
 * to the operation.
 *
 * Iterators returned by methods of this interface can only be used safely
 * by one thread at a time unless synchronized externally.
 *
 * @since 3.0
 */
public interface TableAPI {

    /**
     * Asynchronously executes a table statement. Currently, table statements
     * can be used to create or modify tables and indices. The operation is
     * asynchronous and may not be finished when the method returns.
     * <p>
     * An {@link ExecutionFuture} instance is returned which extends
     * {@link java.util.concurrent.Future} and can be used to get information
     * about the status of the operation, or to await completion of the
     * operation.
     * <p>
     * For example:
     * <pre>
     * // Create a table
     * ExecutionFuture future = null;
     * try {
     *     future = tableAPI.execute
     *          ("CREATE TABLE users (" +
     *           "id INTEGER, " +
     *           "firstName STRING, " +
     *           "lastName STRING, " +
     *           "age INTEGER, " +
     *           "PRIMARY KEY (id))");
     * } catch (IllegalArgumentException e) {
     *     System.out.println("The statement is invalid: " + e);
     * } catch (FaultException e) {
     *     System.out.println("There is a transient problem, retry the " +
     *                          "operation: " + e);
     * }
     * // Wait for the operation to finish
     * StatementResult result = future.get()
     * </pre>
     * <p>
     * If the statement is a data definition or administrative operation, and
     * the store is currently executing an operation that is the logical
     * equivalent of the action specified by the statement, the method will
     * return an ExecutionFuture that serves as a handle to that operation,
     * rather than starting a new invocation of the command. The caller can use
     * the ExecutionFuture to await the completion of the operation.
     * <pre>
     *   // process A starts an index creation
     *   ExecutionFuture futureA =
     *       tableAPI.execute("CREATE INDEX age ON users(age)");
     *
     *   // process B starts the same index creation. If the index creation is
     *   // still running in the cluster, futureA and futureB will refer to
     *   // the same operation
     *   ExecutionFuture futureB =
     *       tableAPI.execute("CREATE INDEX age ON users(age)");
     * </pre>
     * <p>
     * Note that, in a secure store, creating and modifying table and index
     * definitions may require a level of system privileges over and beyond
     * that required for reads and writes of table records.
     * <br>
     * See the Data Definition Language for Tables guide in the documentation
     * for information about supported statements.
     * @param statement must follow valid Table syntax.
     * @throws IllegalArgumentException if the statement is not valid
     * @throws FaultException if the statement cannot be completed. This
     * indicates a transient problem with communication to the server or
     * within the server, and the statement can be retried.
     *
     * @since 3.2
     * @deprecated since 3.3 in favor of {@link oracle.kv.KVStore#execute}
     */
    @Deprecated
    oracle.kv.table.ExecutionFuture execute(String statement)
        throws FaultException,
               IllegalArgumentException;

    /**
     * Synchronously execute a table statement. The method will only return
     * when the statement has finished. Has the same semantics as {@link
     * #execute(String)}, but offers synchronous behavior as a convenience.
     * ExecuteSync() is the equivalent of:
     * <pre>
     * ExecutionFuture future = tableAPI.execute( ... );
     * return future.get();
     * </pre>
     * When executeSync() returns, statement execution will have terminated,
     * and the resulting {@link StatementResult} will provide information
     * about the outcome.
     * @param statement must follow valid Table syntax.
     * @throws IllegalArgumentException if the statement is not valid
     * @throws FaultException if the statement cannot be completed. This
     * indicates a transient problem with communication to the server or
     * within the server, and the statement can be retried.
     * @see #execute(String)
     *
     * @since 3.2
     * @deprecated since 3.3 in favor of {@link oracle.kv.KVStore#executeSync}
     */
    @Deprecated
    oracle.kv.table.StatementResult executeSync(String statement)
        throws FaultException,
               IllegalArgumentException;

    /**
     * Gets an instance of a table.  This method can be retried in the event
     * that the specified table is not yet fully initialized.  This call will
     * typically go to a server node to find the requested metadata and/or
     * verify that it is current.
     * <p>
     * This interface will only retrieve top-level tables -- those with no
     * parent table.  Child tables are retrieved using
     * {@link Table#getChildTable}.
     *
     * @param tableName the name of the target table
     *
     * @return the table or null if the table does not exist
     *
     * @throws FaultException if the operation fails to communicate with a
     * server node that has the table metadata
     */
    Table getTable(String tableName) throws FaultException;

    /**
     * Gets all known tables.  Only top-level tables -- those without parent
     * tables -- are returned. Child tables of a parent are retrieved using
     * {@link Table#getChildTables}.
     *
     * @return the map of tables.  If there are no tables and empty map is
     * returned.
     *
     * @throws FaultException if the operation fails to communicate with a
     * server node that has the table metadata
     */
    Map<String, Table> getTables() throws FaultException;

    /**
     * Gets the {@code Row} associated with the primary key.
     *
     * @param key the primary key for a table.  It must be a complete primary
     * key, with all fields set.
     *
     * @param readOptions non-default options for the operation or null to
     * get default behavior
     *
     * @return the matching Row, or null if not found
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is not complete
     */
    Row get(PrimaryKey key,
            ReadOptions readOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns the rows associated with a partial primary key in an
     * atomic manner.  Rows are returned in primary key order.  The key used
     * must contain all of the fields defined for the table's shard key.
     *
     * @param key the primary key for the operation.  It may be partial or
     * complete.
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the results.  It may be null.  The table used to construct
     * the {@code PrimaryKey} parameter is always included as a target.
     *
     * @param readOptions non-default options for the operation or null to
     * get default behavior
     *
     * @return a list of matching rows, one for each selected record, or an
     * empty list if no rows are matched
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is malformed or
     * does not contain the required fields
     */
    List<Row> multiGet(PrimaryKey key,
                       MultiRowOptions getOptions,
                       ReadOptions readOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return the rows associated with a partial primary key in an
     * atomic manner.  Keys are returned in primary key order.  The key used
     * must contain all of the fields defined for the table's shard key.
     *
     * @param key the primary key for the operation.  It may be partial or
     * complete
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the results.  It may be null.  The table used to construct
     * the {@code PrimaryKey} parameter is always included as a target.
     *
     * @param readOptions non-default options for the operation or null to
     * get default behavior
     *
     * @return a list of matching keys, one for each selected row, or an
     * empty list if no rows are matched
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is malformed or
     * does not contain the required fields
     */
    List<PrimaryKey> multiGetKeys(PrimaryKey key,
                                  MultiRowOptions getOptions,
                                  ReadOptions readOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an iterator over the rows associated with a partial primary key.
     *
     * @param key the primary key for the operation.  It may be partial or
     * complete shard key.  If the key contains a partial shard key the
     * iteration goes to all partitions in the store.  If the key contains a
     * complete shard key the operation is restricted to the target partition.
     * If the key has no fields set the entire table is matched.
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the results.  It may be null.  The table used to construct
     * the {@code PrimaryKey} parameter is always included as a target.
     *
     * @param iterateOptions the non-default arguments for consistency of the
     * operation and to control the iteration or null to get default behavior.
     * If the primary key contains a complete shard key, the default Direction
     * in {@code TableIteratorOptions} is {@link Direction#FORWARD}. Otherwise,
     * the default Direction in {@code TableIteratorOptions} is
     * {@link Direction#UNORDERED}.
     *
     * @return an iterator over the matching rows, or if none match an empty
     * iterator.  If the primary key contains a complete shard key the
     * methods on TableIterator associated with {@link ParallelScanIterator},
     * such as statistics, will not return meaningful information because the
     * iteration will be single-partition and not parallel.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is malformed or an
     * invalid option is specified, such as iteration order without a complete
     * shard key.
     */
    TableIterator<Row> tableIterator(PrimaryKey key,
                                     MultiRowOptions getOptions,
                                     TableIteratorOptions iterateOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an iterator over the keys associated with a partial primary key.
     *
     * @param key the primary key for the operation.  It may be partial or
     * complete shard key.  If the key contains a partial shard key the
     * iteration goes to all partitions in the store.  If the key contains a
     * complete shard key the operation is restricted to the target partition.
     * If the key has no fields set the entire table is matched.
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the results.  It may be null.  The table used to construct
     * the {@code PrimaryKey} parameter is always included as a target.
     *
     * @param iterateOptions the non-default arguments for consistency of the
     * operation and to control the iteration or null to get default behavior.
     * If the primary key contains a complete shard key, the default Direction
     * in {@code TableIteratorOptions} is {@link Direction#FORWARD}. Otherwise,
     * the default Direction in {@code TableIteratorOptions} is
     * {@link Direction#UNORDERED}.
     *
     * @return an iterator over the primary keys of matching rows, or if none
     * match an empty iterator.  If the primary key contains a complete shard
     * key the methods on TableIterator associated with {@link
     * ParallelScanIterator}, such as statistics, will not return meaningful
     * information because the iteration will be single-partition and not
     * parallel.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is malformed or an
     * invalid option is specified, such as iteration order without a complete
     * shard key.
     */
    TableIterator<PrimaryKey> tableKeysIterator
        (PrimaryKey key,
         MultiRowOptions getOptions,
         TableIteratorOptions iterateOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an iterator over the rows associated with an index key.
     * This method requires an additional database read on the server side
     * to get row information for matching rows.  Ancestor table rows for
     * matching index rows may be returned as well if specified in the
     * {@code getOptions} parameter.  Index operations may not specify the
     * return of child table rows.
     *
     * @param key the index key for the operation.  It may be partial or
     * complete.  If the key has no fields set the entire index is matched.
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the results.  It may be null.  The table on which the
     * index is defined is always included as a target.  Child tables cannot
     * be included for index operations.
     *
     * @param iterateOptions the non-default arguments for consistency of the
     * operation and to control the iteration or null to get default behavior.
     * The default Direction in {@code TableIteratorOptions} is
     * {@link Direction#FORWARD}.
     *
     * @return an iterator over the matching rows, or if none match an empty
     * iterator
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is malformed
     *
     * @throws UnsupportedOperationException if the {@code getOptions}
     * parameter specifies the return of child tables
     */
    TableIterator<Row> tableIterator(IndexKey key,
                                     MultiRowOptions getOptions,
                                     TableIteratorOptions iterateOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return the keys for matching rows associated with an index key.  The
     * iterator returned only references information directly available from
     * the index.  No extra fetch operations are performed.  Ancestor table
     * keys for matching index keys may be returned as well if specified in the
     * {@code getOptions} parameter.  Index operations may not specify the
     * return of child table keys.
     *
     * @param key the index key for the operation.  It may be partial or
     * complete.  If the key has no fields set the entire index is matched.
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the results.  It may be null.  The table on which the
     * index is defined is always included as a target.  Child tables cannot
     * be included for index operations.
     *
     * @param iterateOptions the non-default arguments for consistency of the
     * operation and to control the iteration or null to get default behavior.
     * The default Direction in {@code TableIteratorOptions} is
     * {@link Direction#FORWARD}.
     *
     * @return an iterator over {@code KeyPair} objects, which provide access
     * to both the {@link PrimaryKey} associated with a match but the values
     * in the matching {@link IndexKey} as well without an additional fetch of
     * the Row itself.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is malformed
     *
     * @throws UnsupportedOperationException if the {@code getOptions}
     * parameter specifies the return of child tables
     */
    TableIterator<KeyPair> tableKeysIterator
        (IndexKey key,
         MultiRowOptions getOptions,
         TableIteratorOptions iterateOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an iterator over the rows matching the primary keys supplied by
     * iterator (or the rows in ancestor or descendant tables, or those in a
     * range specified by the MultiRowOptions argument).
     *
     * <p>
     * The result is not transactional and the operation effectively provides
     * read-committed isolation. The implementation batches the fetching of rows
     * in the iterator, to minimize the number of network round trips,
     * while not monopolizing the available bandwidth. Batches are fetched in
     * parallel across multiple Replication Nodes, the degree of parallelism is
     * controlled by the TableIteratorOptions argument.
     * </p>
     *
     * @param primaryKeyIterator it yields a sequence of primary keys, the
     * primary key may be partial or complete, it must contain all of the
     * fields defined for the table's shard key. The iterator implementation
     * need not be thread safe.
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the results. It may be null. The table used to construct the
     * {@code PrimaryKey} parameter is always included as a target.
     *
     * @param iterateOptions the non-default arguments for consistency of the
     * operation and to control the iteration or null to get default behavior.
     * Currently, the Direction in {@code TableIteratorOptions} can only be
     * {@link Direction#UNORDERED}, others are not supported.
     *
     * @return an iterator over the matching rows. If the
     * <code>primaryKeyIterator</code> yields duplicate keys, the row
     * associated with the duplicate keys will be returned at least once and
     * potentially multiple times. The implementation makes an effort to
     * minimize these duplicate values but the exact number of repeated rows is
     * not defined by the implementation, since weeding out such duplicates can
     * be resource intensive.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the supplied key iterator is null or
     * invalid option is specified, such as unsupported iteration order.
     * @since 3.4
     */
    TableIterator<Row> tableIterator(Iterator<PrimaryKey> primaryKeyIterator,
                                     MultiRowOptions getOptions,
                                     TableIteratorOptions iterateOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an iterator over the keys matching the primary keys supplied by
     * iterator (or the rows in ancestor or descendant tables, or those in a
     * range specified by the MultiRowOptions argument).
     *
     * <p>
     * This method is almost identical to {@link #tableIterator(Iterator,
     * MultiRowOptions, TableIteratorOptions)} but differs solely in the type
     * of its return value (PrimaryKeys instead of rows).
     * </p>
     *
     * @see #tableIterator(Iterator, MultiRowOptions, TableIteratorOptions)
     * @since 3.4
     */
    TableIterator<PrimaryKey> tableKeysIterator
        (Iterator<PrimaryKey> primaryKeyIterator,
         MultiRowOptions getOptions,
         TableIteratorOptions iterateOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an iterator over the rows matching the primary keys supplied by
     * iterator (or the rows in ancestor or descendant tables, or those in a
     * range specified by the MultiRowOptions argument).
     *
     * <p>
     * Except for the difference in the type of the first argument:
     * <code>primaryKeyIterators</code>, which is a list of iterators instead of
     * a single iterator, this method is identical to the overloaded {@link
     * #tableIterator(Iterator, MultiRowOptions, TableIteratorOptions)} method.
     * One or more of the iterators in the <code>primaryKeyIterators</code> list
     * may be read in parallel to maximize input throughput.
     * </p>
     *
     * @see #tableIterator(Iterator, MultiRowOptions, TableIteratorOptions)
     * @since 3.4
     */
    TableIterator<Row> tableIterator
        (List<Iterator<PrimaryKey>> primaryKeyIterators,
         MultiRowOptions getOptions,
         TableIteratorOptions iterateOptions)
    throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an iterator over the keys matching the primary keys supplied by
     * iterator (or the rows in ancestor or descendant tables, or those in a
     * range specified by the MultiRowOptions argument).
     *
     * <p>
     * Except for the difference in the type of the first argument:
     * <code>primaryKeyIterators</code>, which is a list of iterators instead of
     * a single iterator, this method is identical to the overloaded {@link
     * #tableKeysIterator(Iterator, MultiRowOptions, TableIteratorOptions)}
     * method. One or more of the iterators in the
     * <code>primaryKeyIterators</code> list may be read in parallel to maximize
     * input throughput.
     * </p>
     *
     * @see #tableKeysIterator(Iterator, MultiRowOptions, TableIteratorOptions)
     * @since 3.4
     */
    TableIterator<PrimaryKey> tableKeysIterator
        (List<Iterator<PrimaryKey>> primaryKeyIterators,
         MultiRowOptions getOptions,
         TableIteratorOptions iterateOptions)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Puts a row into a table.  The row must contain a complete primary
     * key and all required fields.
     *
     * @param row the row to put
     *
     * @param prevRow a {@code ReturnRow} object to contain the previous row
     * value and version associated with the given row, or null if they should
     * not be returned.  If a previous row does not exist, or the {@link
     * ReturnRow.Choice} specifies that they should not be returned, the
     * version in this object is set to null and none of the row's fields are
     * available.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     * See {@code WriteOptions} for more information.
     *
     * @return the version of the new row value
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the <code>row</code> does not have
     * a complete primary key or is otherwise invalid
     */
    Version put(Row row,
                ReturnRow prevRow,
                WriteOptions writeOptions)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Puts a row into a table, but only if the row does not exist.  The row
     * must contain a complete primary key and all required fields.
     *
     * @param row the row to put
     *
     * @param prevRow a {@code ReturnRow} object to contain the previous row
     * value and version associated with the given row, or null if they should
     * not be returned.  If a previous row does not exist, or the {@link
     * ReturnRow.Choice} specifies that they should not be returned, the
     * version in this object is set to null and none of the row's fields are
     * available.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     *
     * @return the version of the new value, or null if an existing value is
     * present and the put is unsuccessful
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the <code>row</code> does not have
     * a complete primary key or is otherwise invalid
     */
    Version putIfAbsent(Row row,
                        ReturnRow prevRow,
                        WriteOptions writeOptions)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Puts a row into a table, but only if the row already exists.  The row
     * must contain a complete primary key and all required fields.
     *
     * @param row the row to put
     *
     * @param prevRow a {@code ReturnRow} object to contain the previous row
     * value and version associated with the given row, or null if they should
     * not be returned.  If a previous row does not exist, or the {@link
     * ReturnRow.Choice} specifies that they should not be returned, the
     * version in this object is set to null and none of the row's fields are
     * available.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     *
     * @return the version of the new value, or null if there is no existing
     * row and the put is unsuccessful
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the {@code Row} does not have
     * a complete primary key or is otherwise invalid.
     */
    Version putIfPresent(Row row,
                         ReturnRow prevRow,
                         WriteOptions writeOptions)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Puts a row, but only if the version of the existing row matches the
     * matchVersion argument. Used when updating a value to ensure that it has
     * not changed since it was last read.  The row must contain a complete
     * primary key and all required fields.
     *
     * @param row the row to put
     *
     * @param matchVersion the version to match
     *
     * @param prevRow a {@code ReturnRow} object to contain the previous row
     * value and version associated with the given row, or null if they should
     * not be returned.  If a previous row does not exist, or the {@link
     * ReturnRow.Choice} specifies that they should not be returned, the
     * version in this object is set to null and none of the row's fields are
     * available.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     *
     * @return the version of the new value, or null if the versions do not
     * match and the put is unsuccessful
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the {@code Row} does not have
     * a complete primary key or is otherwise invalid
     */
    Version putIfVersion(Row row,
                         Version matchVersion,
                         ReturnRow prevRow,
                         WriteOptions writeOptions)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Deletes a row from a table.
     *
     * @param key the primary key for the row to delete
     *
     * @param prevRow a {@code ReturnRow} object to contain the previous row
     * value and version associated with the given row, or null if they should
     * not be returned.  If a previous row does not exist, or the {@link
     * ReturnRow.Choice} specifies that they should not be returned, the
     * version in this object is set to null and none of the row's fields are
     * available.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     *
     * @return true if the row existed and was deleted, false otherwise
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is not complete
     */
    boolean delete(PrimaryKey key,
                   ReturnRow prevRow,
                   WriteOptions writeOptions)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Deletes a row from a table but only if its version matches the one
     * specified in matchVersion.
     *
     * @param key the primary key for the row to delete
     *
     * @param matchVersion the version to match
     *
     * @param prevRow a {@code ReturnRow} object to contain the previous row
     * value and version associated with the given row, or null if they should
     * not be returned.  If a previous row does not exist, or the {@link
     * ReturnRow.Choice} specifies that they should not be returned, or
     * the matchVersion parameter matches the existing value and the delete is
     * successful, the version in this object is set to null and none of the
     * row's fields are available.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     *
     * @return true if the row existed and its version matched matchVersion
     * and was successfully deleted, false otherwise
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is not complete
     */
    boolean deleteIfVersion(PrimaryKey key,
                            Version matchVersion,
                            ReturnRow prevRow,
                            WriteOptions writeOptions)
        throws DurabilityException, RequestTimeoutException, FaultException;


    /**
     * Deletes multiple rows from a table in an atomic operation.  The
     * key used may be partial but must contain all of the fields that are
     * in the shard key.
     *
     * @param key the primary key for the row to delete
     *
     * @param getOptions a {@code MultiRowOptions} object used to control
     * ranges in the operation and whether ancestor and descendant tables are
     * included in the operation. It may be null.  The table used to construct
     * the {@code PrimaryKey} parameter is always included as a target.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     *
     * @return the number of rows deleted from the table
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if the primary key is malformed or does
     * not contain all shard key fields
     */
    int multiDelete(PrimaryKey key,
                    MultiRowOptions getOptions,
                    WriteOptions writeOptions)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Returns a {@code TableOperationFactory} to create operations passed
     * to {@link #execute}.  Not all operations must use the same table but
     * they must all use the same shard portion of the primary key.
     *
     * @return an empty {@code TableOperationFactory}
     */
    TableOperationFactory getTableOperationFactory();

    /**
     * This method provides an efficient and transactional mechanism for
     * executing a sequence of operations associated with tables that share the
     * same <em>shard key</em> portion of their primary keys. The efficiency
     * results from the use of a single network interaction to accomplish the
     * entire sequence of operations.
     * <p>
     * The operations passed to this method are created using an {@link
     * TableOperationFactory}, which is obtained from the {@link
     * #getTableOperationFactory} method.
     * </p>
     * <p>
     * All the {@code operations} specified are executed within the scope of a
     * single transaction that effectively provides serializable isolation.
     * The transaction is started and either committed or aborted by this
     * method.  If the method returns without throwing an exception, then all
     * operations were executed atomically, the transaction was committed, and
     * the returned list contains the result of each operation.
     * </p>
     * <p>
     * If the transaction is aborted for any reason, an exception is thrown.
     * An abort may occur for two reasons:
     * <ol>
     *   <li>An operation or transaction results in an exception that is
     *   considered a fault, such as a durability or consistency error, a
     *   failure due to message delivery or networking error, etc. A {@link
     *   FaultException} is thrown.</li>
     *   <li>An individual operation returns normally but is unsuccessful as
     *   defined by the particular operation (e.g., a delete operation for a
     *   non-existent key) <em>and</em> {@code true} was passed for the {@code
     *   abortIfUnsuccessful} parameter when the operation was created using
     *   the {@link TableOperationFactory}.
     *   A {@link TableOpExecutionException}
     *   is thrown, and the exception contains information about the failed
     *   operation.</li>
     * </ol>
     * </p>
     * <p>
     * Operations are not executed in the sequence they appear the {@code
     * operations} list, but are rather executed in an internally defined
     * sequence that prevents deadlocks.  Additionally, if there are two
     * operations for the same key, their relative order of execution is
     * arbitrary; this should be avoided.
     * </p>
     *
     * @param operations the list of operations to be performed. Note that all
     * operations in the list must specify primary keys with the same
     * complete shard key.
     *
     * @param writeOptions non-default arguments controlling the
     * durability of the operation, or null to get default behavior.
     *
     * @return the sequence of results associated with the operation. There is
     * one entry for each TableOperation in the operations argument list.  The
     * returned list is in the same order as the operations argument list.
     *
     * @throws TableOpExecutionException if an operation is not successful as
     * defined by the particular operation (e.g., a delete operation for a
     * non-existent key) <em>and</em> {@code true} was passed for the {@code
     * abortIfUnsuccessful} parameter when the operation was created using the
     * {@link TableOperationFactory}.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason
     *
     * @throws IllegalArgumentException if operations is null or empty, or not
     * all operations operate on primary keys with the same shard key, or more
     * than one operation has the same primary key, or any of the primary keys
     * are incomplete.
     */
    List<TableOperationResult> execute(List<TableOperation> operations,
                                       WriteOptions writeOptions)
        throws TableOpExecutionException,
               DurabilityException,
               FaultException;

    /**
     * @hidden
     * For internal use only!
     * Obtain a handle onto the asynchronous DDL operation indicated by
     * this plan id.
     * <p>
     * This method is used to support non-Java language bindings, which don't
     * have access to the Java ExecutionFuture. Those non-native Java clients
     * should check first in their operation handle to see if the operation is
     * complete, and only incur the cost of a proxy communication if the
     * operation is not completed.
     * <p>
     * Note also that the planId must be non-zero. This rides on the assumption
     * that DDL statements which have zero planId will also complete
     * synchronously, and that TableeAPI.getFuture() will not be called on
     * their behalf as long as the non-native Java clients check for isDone()
     * appropriately.
     * <p>
     * The ExecutionFuture instance that is returned by this method is a
     * skeleton object, sparsely populated. The proxy should never send it back
     * to the thin client without an additional call to an ExecutionFuture
     * method such as get() or cancel that will actually fill in the instance
     * with appropriate status.
     * @return a sparsely populated Future that corresponds to this planId
     * @throws IllegalStateException if the planId is zero.
     * @deprecated since 3.3 in favor of {@link oracle.kv.KVStore#getFuture}
     */
    @Deprecated
    oracle.kv.table.ExecutionFuture getFuture(int planId)
        throws IllegalArgumentException,
               FaultException;

    /**
     * For internal use only.
     * @hidden
     *
     * Loads rows supplied by special purpose streams into the store. The bulk
     * loading of the entries is optimized to make efficient use of hardware
     * resources. As a result, this operation can achieve much higher
     * throughput when compared with single row put APIs. The sequential
     * semantics of individual streams are the basis for stream granularity
     * resumption in the presence of failures.
     *
     * Entries are supplied to the loader by a list of EntryStream instances.
     * Each stream is read sequentially, that is, each EntryStream.getNext() is
     * allowed to finish before the next operation is issued. The load
     * operation typically reads from these streams in parallel as determined
     * by {@link BulkWriteOptions#getStreamParallelism}.
     *
     * If an entry is associated with a primary key that's already present in
     * the store, the {@link EntryStream#keyExists} method is invoked on it and
     * the entry is not loaded into the store by this method; the
     * {@link EntryStream#keyExists} method may of course choose to do so
     * itself, if the values differ.
     *
     * If the key is absent, a new entry is created in the store, that is, the
     * load operation has putIfAbsent semantics. The putIfAbsent semantics
     * permit restarting a load of a stream that failed for some reason.
     *
     * The collection of streams defines a partial insertion order, with
     * insertion of rows containing the same key within a stream being strictly
     * ordered, but with no ordering constraints being imposed on keys across
     * streams, or for different keys within the same stream.
     *
     * The behavior of the bulk put operation with respect to duplicate entries
     * contained in different streams is thus undefined. If the duplicate
     * entries are just present in a single stream, then the first entry will
     * be inserted (if it's not already present) and the second entry and
     * subsequent entries will result in the invocation of the
     * {@link EntryStream#keyExists} method. If duplicates exist across
     * streams, then the first entry to win the race is inserted and subsequent
     * duplicates will result in {@link EntryStream#keyExists} being invoked on
     * them.
     *
     * Exceptions encountered during the reading of streams result in the put
     * operation being terminated and the first such exception being thrown
     * from the put method.
     *
     * Exceptions encountered when inserting a row into the store result in the
     * {@link EntryStream#catchException} being invoked.
     *
     * @param streams the streams that supply the rows to be inserted. The rows
     * within each stream may be associated with different tables.
     *
     * @param bulkWriteOptions non-default arguments controlling the behavior
     * the bulk write operations
     */
    public void put(List<EntryStream<Row>> streams,
                    BulkWriteOptions bulkWriteOptions);
}
