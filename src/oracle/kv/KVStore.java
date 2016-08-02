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

package oracle.kv;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

import oracle.kv.avro.AvroCatalog;
import oracle.kv.lob.KVLargeObject;
import oracle.kv.stats.KVStats;
import oracle.kv.table.TableAPI;

/**
 * KVStore is the handle to a store that is running remotely. To create a
 * connection to a store, request a KVStore instance from
 * {@link KVStoreFactory#getStore KVStoreFactory.getStore}.
 *
 * <p>
 * A handle has thread, memory and network resources associated with it.
 * Consequently, the {@link KVStore#close} method must be invoked to free up
 * the resources when the application is done using the KVStore.
 * <p>
 * To minimize resource allocation and deallocation overheads, it's best to
 * avoid repeated creation and closing of handles to the same store. For
 * example, creating and closing a handle around each KVStore operation, would
 * incur large resource allocation overheads resulting in poor application
 * performance.
 * </p>
 * <p>
 * A handle permits concurrent operations, so a single handle is sufficient to
 * access a KVStore in a multi-threaded application. The creation of multiple
 * handles incurs additional resource overheads without providing any
 * performance benefit.
 * </p>
 * <p>
 * Iterators returned by methods of this interface can only be used safely by
 * one thread at a time unless synchronized externally.
 */
public interface KVStore extends KVLargeObject, Closeable {

    /**
     * Get the value associated with the key.
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param key the key used to lookup the key/value pair.
     *
     * @return the value and version associated with the key, or null if no
     * associated value was found.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public ValueVersion get(Key key)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Get the value associated with the key.
     *
     * @param key the key used to lookup the key/value pair.
     *
     * @param consistency determines the consistency associated with the read
     * used to lookup the value.  If null, the {@link
     * KVStoreConfig#getConsistency default consistency} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return the value and version associated with the key, or null if no
     * associated value was found.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public ValueVersion get(Key key,
                            Consistency consistency,
                            long timeout,
                            TimeUnit timeoutUnit)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns the descendant key/value pairs associated with the
     * <code>parentKey</code>. The <code>subRange</code> and the
     * <code>depth</code> arguments can be used to further limit the key/value
     * pairs that are retrieved. The key/value pairs are fetched within the
     * scope of a single transaction that effectively provides serializable
     * isolation.
     *
     * <p>This API should be used with caution since it could result in an
     * OutOfMemoryError, or excessive GC activity, if the results cannot all be
     * held in memory at one time. Consider using the {@link
     * KVStore#multiGetIterator} version instead.</p>
     *
     * <p>This method only allows fetching key/value pairs that are descendants
     * of a parentKey that has a <em>complete</em> major path.  To fetch the
     * descendants of a parentKey with a partial major path, use {@link
     * #storeIterator} instead.</p>
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * fetched.  It must not be null.  The major key path must be complete.
     * The minor key path may be omitted or may be a partial path.
     *
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @return a SortedMap of key-value pairs, one for each key selected, or an
     * empty map if no keys are selected.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public SortedMap<Key, ValueVersion> multiGet(Key parentKey,
                                                 KeyRange subRange,
                                                 Depth depth)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns the descendant key/value pairs associated with the
     * <code>parentKey</code>. The <code>subRange</code> and the
     * <code>depth</code> arguments can be used to further limit the key/value
     * pairs that are retrieved. The key/value pairs are fetched within the
     * scope of a single transaction that effectively provides serializable
     * isolation.
     *
     * <p>This API should be used with caution since it could result in an
     * OutOfMemoryError, or excessive GC activity, if the results cannot all be
     * held in memory at one time. Consider using the {@link
     * KVStore#multiGetIterator} version instead.</p>
     *
     * <p>This method only allows fetching key/value pairs that are descendants
     * of a parentKey that has a <em>complete</em> major path.  To fetch the
     * descendants of a parentKey with a partial major path, use {@link
     * #storeIterator} instead.</p>
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * fetched.  It must not be null.  The major key path must be complete.
     * The minor key path may be omitted or may be a partial path.
     *
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @param consistency determines the read consistency associated with the
     * lookup of the child KV pairs.  If null, the {@link
     * KVStoreConfig#getConsistency default consistency} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return a SortedMap of key-value pairs, one for each key selected, or an
     * empty map if no keys are selected.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public SortedMap<Key, ValueVersion> multiGet(Key parentKey,
                                                 KeyRange subRange,
                                                 Depth depth,
                                                 Consistency consistency,
                                                 long timeout,
                                                 TimeUnit timeoutUnit)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns the descendant keys associated with the <code>parentKey</code>.
     * <p>
     * This method is almost identical to {@link #multiGet(Key, KeyRange,
     * Depth)}. It differs solely in the type of its return value: It returns a
     * SortedSet of keys instead of returning a SortedMap representing
     * key-value pairs.
     * </p>
     *
     * @see #multiGet(Key, KeyRange, Depth)
     */
    public SortedSet<Key> multiGetKeys(Key parentKey,
                                       KeyRange subRange,
                                       Depth depth)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns the descendant keys associated with the <code>parentKey</code>.
     * <p>
     * This method is almost identical to {@link #multiGet(Key, KeyRange,
     * Depth, Consistency, long, TimeUnit)}. It differs solely in the type of
     * its return value: It returns a SortedSet of keys instead of returning a
     * SortedMap representing key-value pairs.
     * </p>
     *
     * @see #multiGet(Key, KeyRange, Depth, Consistency, long, TimeUnit)
     */
    public SortedSet<Key> multiGetKeys(Key parentKey,
                                       KeyRange subRange,
                                       Depth depth,
                                       Consistency consistency,
                                       long timeout,
                                       TimeUnit timeoutUnit)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * The iterator form of {@link #multiGet(Key, KeyRange, Depth)}.
     *
     * <p>The iterator permits an ordered traversal of the descendant key/value
     * pairs associated with the <code>parentKey</code>. It's useful when the
     * result is too large to fit in memory. Note that the result is not
     * transactional and the operation effectively provides read-committed
     * isolation. The implementation batches the fetching of KV pairs in the
     * iterator, to minimize the number of network round trips, while not
     * monopolizing the available bandwidth.</p>
     *
     * <p>This method only allows fetching key/value pairs that are descendants
     * of a parentKey that has a <em>complete</em> major path.  To fetch the
     * descendants of a parentKey with a partial major path, use {@link
     * #storeIterator} instead.</p>
     *
     * <p>The iterator does not support the remove method.</p>
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param direction specifies the order in which records are returned by
     * the iterator.  Only {@link Direction#FORWARD} and {@link
     * Direction#REVERSE} are supported by this method.
     *
     * @param batchSize the suggested number of keys to fetch during each
     * network round trip.  If only the first or last key-value pair is
     * desired, passing a value of one (1) is recommended.  If zero, an
     * internally determined default is used.
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * fetched.  It must not be null.  The major key path must be complete.
     * The minor key path may be omitted or may be a partial path.
     *
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @return an iterator that permits an ordered traversal of the descendant
     * key/value pairs.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Iterator<KeyValueVersion> multiGetIterator(Direction direction,
                                                      int batchSize,
                                                      Key parentKey,
                                                      KeyRange subRange,
                                                      Depth depth)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * The iterator form of {@link #multiGet(Key, KeyRange, Depth, Consistency,
     * long, TimeUnit)}.
     *
     * <p>The iterator permits an ordered traversal of the descendant key/value
     * pairs associated with the <code>parentKey</code>. It's useful when the
     * result is too large to fit in memory. Note that the result is not
     * transactional and the operation effectively provides read-committed
     * isolation. The implementation batches the fetching of KV pairs in the
     * iterator, to minimize the number of network round trips, while not
     * monopolizing the available bandwidth.</p>
     *
     * <p>This method only allows fetching key/value pairs that are descendants
     * of a parentKey that has a <em>complete</em> major path.  To fetch the
     * descendants of a parentKey with a partial major path, use {@link
     * #storeIterator} instead.</p>
     *
     * <p>The iterator does not support the remove method.</p>
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param direction specifies the order in which records are returned by
     * the iterator.  Only {@link Direction#FORWARD} and {@link
     * Direction#REVERSE} are supported by this method.
     *
     * @param batchSize the suggested number of keys to fetch during each
     * network round trip.  If only the first or last key-value pair is
     * desired, passing a value of one (1) is recommended.  If zero, an
     * internally determined default is used.
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * fetched.  It must not be null.  The major key path must be complete.
     * The minor key path may be omitted or may be a partial path.
     *
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @param consistency determines the read consistency associated with the
     * lookup of the child KV pairs.  If null, the {@link
     * KVStoreConfig#getConsistency default consistency} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return an iterator that permits an ordered traversal of the descendant
     * key/value pairs.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Iterator<KeyValueVersion> multiGetIterator(Direction direction,
                                                      int batchSize,
                                                      Key parentKey,
                                                      KeyRange subRange,
                                                      Depth depth,
                                                      Consistency consistency,
                                                      long timeout,
                                                      TimeUnit timeoutUnit)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * The iterator form of {@link #multiGetKeys(Key, KeyRange, Depth)}.
     * <p>
     * This method is almost identical to {@link #multiGetIterator(Direction,
     * int, Key, KeyRange, Depth)}. It differs solely in the type of its return
     * value: it returns an iterator over keys instead of key/value pairs.
     * </p>
     *
     * @see #multiGetIterator(Direction, int, Key, KeyRange, Depth)
     */
    public Iterator<Key> multiGetKeysIterator(Direction direction,
                                              int batchSize,
                                              Key parentKey,
                                              KeyRange subRange,
                                              Depth depth)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * The iterator form of {@link #multiGetKeys(Key, KeyRange, Depth,
     * Consistency, long, TimeUnit)}.
     * <p>
     * This method is almost identical to {@link #multiGetIterator(Direction,
     * int, Key, KeyRange, Depth, Consistency, long, TimeUnit)}. It differs
     * solely in the type of its return value: it returns an iterator over keys
     * instead of key/value pairs.
     * </p>
     *
     * @see #multiGetIterator(Direction, int, Key, KeyRange, Depth,
     * Consistency, long, TimeUnit)
     */
    public Iterator<Key> multiGetKeysIterator(Direction direction,
                                              int batchSize,
                                              Key parentKey,
                                              KeyRange subRange,
                                              Depth depth,
                                              Consistency consistency,
                                              long timeout,
                                              TimeUnit timeoutUnit)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all key/value pairs in unsorted
     * order.
     *
     * <p>Note that the result is not transactional and the operation
     * effectively provides read-committed isolation. The implementation
     * batches the fetching of KV pairs in the iterator, to minimize the number
     * of network round trips, while not monopolizing the available
     * bandwidth.</p>
     *
     * <p>The iterator does not support the remove method.</p>
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param direction specifies the order in which records are returned by
     * the iterator.  Currently only {@link Direction#UNORDERED} is supported
     * by this method.
     *
     * @param batchSize the suggested number of keys to fetch during each
     * network round trip.  If only the first or last key-value pair is
     * desired, passing a value of one (1) is recommended.  If zero, an
     * internally determined default is used.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Iterator<KeyValueVersion> storeIterator(Direction direction,
                                                   int batchSize)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all key/value pairs (or the
     * descendants of a parentKey, or those in a KeyRange) in unsorted order.
     *
     * <p>Note that the result is not transactional and the operation
     * effectively provides read-committed isolation. The implementation
     * batches the fetching of KV pairs in the iterator, to minimize the number
     * of network round trips, while not monopolizing the available
     * bandwidth.</p>
     *
     * <p>This method only allows fetching key/value pairs that are descendants
     * of a parentKey that is null or has a <em>partial</em> major path.  To
     * fetch the descendants of a parentKey with a complete major path, use
     * {@link #multiGetIterator} instead.</p>
     *
     * <p>The iterator does not support the remove method.</p>
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param direction specifies the order in which records are returned by
     * the iterator.  Currently only {@link Direction#UNORDERED} is supported
     * by this method.
     *
     * @param batchSize the suggested number of keys to fetch during each
     * network round trip.  If only the first or last key-value pair is
     * desired, passing a value of one (1) is recommended.  If zero, an
     * internally determined default is used.
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * fetched.  It may be null to fetch all keys in the store.  If non-null,
     * the major key path must be a partial path and the minor key path must be
     * empty.
     *
     * @param subRange further restricts the range under the parentKey to
     * the major path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Iterator<KeyValueVersion> storeIterator(Direction direction,
                                                   int batchSize,
                                                   Key parentKey,
                                                   KeyRange subRange,
                                                   Depth depth)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all key/value pairs (or the
     * descendants of a parentKey, or those in a KeyRange) in unsorted order.
     *
     * <p>Note that the result is not transactional and the operation
     * effectively provides read-committed isolation. The implementation
     * batches the fetching of KV pairs in the iterator, to minimize the number
     * of network round trips, while not monopolizing the available
     * bandwidth.</p>
     *
     * <p>This method only allows fetching key/value pairs that are descendants
     * of a parentKey that is null or has a <em>partial</em> major path.  To
     * fetch the descendants of a parentKey with a complete major path, use
     * {@link #multiGetIterator} instead.</p>
     *
     * <p>The iterator does not support the remove method.</p>
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param direction specifies the order in which records are returned by
     * the iterator.  Currently only {@link Direction#UNORDERED} is supported
     * by this method.
     *
     * @param batchSize the suggested number of keys to fetch during each
     * network round trip.  If only the first or last key-value pair is
     * desired, passing a value of one (1) is recommended.  If zero, an
     * internally determined default is used.
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * fetched.  It may be null to fetch all keys in the store.  If non-null,
     * the major key path must be a partial path and the minor key path must be
     * empty.
     *
     * @param subRange further restricts the range under the parentKey to
     * the major path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @param consistency determines the read consistency associated with the
     * lookup of the child KV pairs.  Version-based consistency may not be used.
     * If null, the {@link KVStoreConfig#getConsistency default consistency} is
     * used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Iterator<KeyValueVersion> storeIterator(Direction direction,
                                                   int batchSize,
                                                   Key parentKey,
                                                   KeyRange subRange,
                                                   Depth depth,
                                                   Consistency consistency,
                                                   long timeout,
                                                   TimeUnit timeoutUnit)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all key/value pairs (or the
     * descendants of a parentKey, or those in a KeyRange). A non-null
     * storeIteratorConfig argument causes a multi-threaded parallel scan on
     * multiple Replication Nodes to be performed.
     *
     * <p>The result is not transactional and the operation effectively
     * provides read-committed isolation. The implementation batches the
     * fetching of KV pairs in the iterator, to minimize the number of network
     * round trips, while not monopolizing the available bandwidth.</p>
     *
     * <p>This method only allows fetching key/value pairs that are descendants
     * of a parentKey that is null or has a <em>partial</em> major path.  To
     * fetch the descendants of a parentKey with a complete major path, use
     * {@link #multiGetIterator} instead.</p>
     *
     * <p>The iterator is not thread safe. It does not support the remove
     * method.</p>
     *
     * <p>The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.</p>
     *
     * @param direction specifies the order in which records are returned by
     * the iterator.
     *
     * @param batchSize the suggested number of keys to fetch during each
     * network round trip. If zero, an internally determined default is used.
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * fetched. It may be null to fetch all keys in the store. If non-null,
     * the major key path must be a partial path and the minor key path must be
     * empty.
     *
     * @param subRange further restricts the range under the parentKey to
     * the major path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @param consistency determines the read consistency associated with the
     * lookup of the child KV pairs.  Version-based consistency may not be used.
     * If null, the {@link KVStoreConfig#getConsistency default consistency} is
     * used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @param storeIteratorConfig specifies the configuration for parallel
     * scanning across multiple Replication Nodes. If this is null, an
     * IllegalArgumentException is thrown.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     *
     * @throws IllegalArgumentException if the storeIteratorConfig argument is
     * null.
     */
    public ParallelScanIterator<KeyValueVersion>
        storeIterator(Direction direction,
                      int batchSize,
                      Key parentKey,
                      KeyRange subRange,
                      Depth depth,
                      Consistency consistency,
                      long timeout,
                      TimeUnit timeoutUnit,
                      StoreIteratorConfig storeIteratorConfig)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all keys in unsorted order.
     * <p>
     * This method is almost identical to {@link #storeIterator(Direction,
     * int)}. It differs solely in the type of its return value: it returns an
     * iterator over keys instead of key/value pairs.
     * </p>
     *
     * @see #storeIterator(Direction, int)
     */
    public Iterator<Key> storeKeysIterator(Direction direction,
                                           int batchSize)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all keys (or the descendants of a
     * parentKey, or those in a KeyRange) in unsorted order.
     * <p>
     * This method is almost identical to {@link #storeIterator(Direction,
     * int, Key, KeyRange, Depth)}. It differs solely in the type of its return
     * value: it returns an iterator over keys instead of key/value pairs.
     * </p>
     *
     * @see #storeIterator(Direction, int)
     */
    public Iterator<Key> storeKeysIterator(Direction direction,
                                           int batchSize,
                                           Key parentKey,
                                           KeyRange subRange,
                                           Depth depth)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all keys (or the descendants of a
     * parentKey, or those in a KeyRange) in unsorted order.
     * <p>
     * This method is almost identical to {@link #storeIterator(Direction, int,
     * Key, KeyRange, Depth, Consistency, long, TimeUnit)}. It differs solely
     * in the type of its return value: it returns an iterator over keys
     * instead of key/value pairs.
     * </p>
     *
     * @see #storeIterator(Direction, int, Key, KeyRange, Depth, Consistency,
     * long, TimeUnit)
     */
    public Iterator<Key> storeKeysIterator(Direction direction,
                                           int batchSize,
                                           Key parentKey,
                                           KeyRange subRange,
                                           Depth depth,
                                           Consistency consistency,
                                           long timeout,
                                           TimeUnit timeoutUnit)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all keys (or the descendants of a
     * parentKey, or those in a KeyRange). A non-null storeIteratorConfig
     * argument causes a multi-threaded parallel scan on multiple Replication
     * Nodes to be performed.
     * <p>
     * This method is almost identical to {@link #storeIterator(Direction, int,
     * Key, KeyRange, Depth, Consistency, long, TimeUnit,
     * StoreIteratorConfig)} but differs solely in the type of its return
     * value (keys instead of key/value pairs).
     * </p>
     *
     * @see #storeIterator(Direction, int, Key, KeyRange, Depth, Consistency,
     * long, TimeUnit, StoreIteratorConfig)
     */
    public ParallelScanIterator<Key>
        storeKeysIterator(Direction direction,
                          int batchSize,
                          Key parentKey,
                          KeyRange subRange,
                          Depth depth,
                          Consistency consistency,
                          long timeout,
                          TimeUnit timeoutUnit,
                          StoreIteratorConfig storeIteratorConfig)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an Iterator which iterates over all key/value pairs matching the
     * keys supplied by iterator (or the descendants of a parentKey, or those
     * in a KeyRange).
     *
     * <p>
     * The result is not transactional and the operation effectively provides
     * read-committed isolation. The implementation batches the fetching of KV
     * pairs in the iterator, to minimize the number of network round trips,
     * while not monopolizing the available bandwidth. Batches are fetched in
     * parallel across multiple Replication Nodes, the degree of parallelism is
     * controlled by the storeIteratorConfig argument.
     *
     * <p>
     * The {@link KVStoreConfig#getConsistency default consistency} and
     * {@link KVStoreConfig#getRequestTimeout default request timeout} are
     * used.
     * </p>
     *
     * @param parentKeyIterator it yields a sequence of parent keys. The major
     * key path must be complete. The minor key path may be omitted or may be a
     * partial path. The iterator implementation need not be thread safe.
     *
     * @param batchSize the suggested number of keys to fetch during each
     * network round trip. If zero, an internally determined default is used.
     *
     * @param subRange further restricts the range under the parentKey to the
     * major path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are returned. If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @param consistency determines the read consistency associated with the
     * lookup of the child KV pairs. Version-based consistency may not be used.
     * If null, the {@link KVStoreConfig#getConsistency default consistency} is
     * used.
     *
     * @param timeout is an upper bound on the time interval for processing
     * each batch. A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @param storeIteratorConfig specifies the configuration for parallel
     * scanning across multiple Replication Nodes. If this is null, an
     * internally determined default is used.
     *
     * @return an iterator that contains an entry for each key that is present
     * in the store and matches the criteria specified by the
     * <code>parentKeyIterator</code>, </code>subRange</code> and
     * <code>depth</code> constraints. If the <code>parentKeyIterator</code>
     * yields duplicate keys, the KeyValueVersion associated with the duplicate
     * keys will be returned at least once and potentially multiple times. The
     * implementation makes an effort to minimize these duplicate values but
     * the exact number of repeated KeyValueVersion instances is not defined by
     * the implementation, since weeding out such duplicates can be resource
     * intensive.
     *
     * @throws ConsistencyException if the specified {@link Consistency} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded during the fetch of a batch of records.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     * @since 3.4
     */
    public ParallelScanIterator<KeyValueVersion>
        storeIterator(Iterator<Key> parentKeyIterator,
                      int batchSize,
                      KeyRange subRange,
                      Depth depth,
                      Consistency consistency,
                      long timeout,
                      TimeUnit timeoutUnit,
                      StoreIteratorConfig storeIteratorConfig)
        throws ConsistencyException, RequestTimeoutException, FaultException;


    /**
     * Return an Iterator which iterates over all keys matching the keys
     * supplied by iterator (or the descendants of a parentKey, or those in a
     * KeyRange).
     *
     * <p>
     * This method is almost identical to {@link #storeIterator(Iterator, int,
     * KeyRange, Depth, Consistency, long, TimeUnit, StoreIteratorConfig)}
     * but differs solely in the type of its return value (keys instead of
     * key/value pairs).
     * </p>
     *
     * @see #storeIterator(Iterator, int, KeyRange, Depth, Consistency,
     * long, TimeUnit, StoreIteratorConfig)
     * @since 3.4
     */
    public ParallelScanIterator<Key>
        storeKeysIterator(Iterator<Key> parentKeyIterator,
                          int batchSize,
                          KeyRange subRange,
                          Depth depth,
                          Consistency consistency,
                          long timeout,
                          TimeUnit timeoutUnit,
                          StoreIteratorConfig storeIteratorConfig)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Returns an Iterator which iterates over all key/value pairs matching the
     * keys supplied by iterators (or the descendants of a parentKey, or those
     * in a KeyRange).
     *
     * <p>
     * Except for the difference in the type of the first argument:
     * <code>parentKeyIterators</code>, which is a list of iterators instead of
     * a single iterator, this method is identical to the overloaded {@link
     * #storeIterator(Iterator, int, KeyRange, Depth, Consistency, long,
     * TimeUnit, StoreIteratorConfig)} method. One or more of the iterators in
     * the <code>parentKeyIterators</code> list may be read in parallel to
     * maximize input throughput.
     * </p>
     *
     * @see #storeIterator(Iterator, int, KeyRange, Depth, Consistency,
     * long, TimeUnit, StoreIteratorConfig)
     * @since 3.4
     */
    public ParallelScanIterator<KeyValueVersion>
        storeIterator(List<Iterator<Key>> parentKeyIterators,
                      int batchSize,
                      KeyRange subRange,
                      Depth depth,
                      Consistency consistency,
                      long timeout,
                      TimeUnit timeoutUnit,
                      StoreIteratorConfig storeIteratorConfig)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Return an Iterator which iterates over all keys matching the keys
     * supplied by iterators (or the descendants of a parentKey, or those in a
     * KeyRange).
     *
     * <p>
     * Except for the difference in the type of the first argument:
     * <code>parentKeyIterators</code>, which is a list of iterators instead of
     * a single iterator, this method is identical to the overloaded
     * {@link #storeKeysIterator(Iterator, int, KeyRange, Depth, Consistency,
     * long, TimeUnit, StoreIteratorConfig)} method. One or more of the
     * iterators in the <code>parentKeyIterators</code> list may be read in
     * parallel to maximize input throughput.
     * </p>
     *
     * @see #storeKeysIterator(Iterator, int, KeyRange, Depth, Consistency, long,
     * TimeUnit, StoreIteratorConfig)
     * @since 3.4
     */
    public ParallelScanIterator<Key>
        storeKeysIterator(List<Iterator<Key>> parentKeyIterators,
                          int batchSize,
                          KeyRange subRange,
                          Depth depth,
                          Consistency consistency,
                          long timeout,
                          TimeUnit timeoutUnit,
                          StoreIteratorConfig storeIteratorConfig)
        throws ConsistencyException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, inserting or overwriting as appropriate.
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was inserted or updated and the (non-null) version of the
     *   new KV pair is returned.  There is no way to distinguish between an
     *   insertion and an update when using this method signature.
     *   <li>
     *   The KV pair was not guaranteed to be inserted or updated successfully,
     *   and one of the exceptions listed below is thrown.
     * </ol>
     *
     * @param key the key part of the key/value pair.
     *
     * @param value the value part of the key/value pair.
     *
     * @return the version of the new value.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version put(Key key,
                       Value value)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, inserting or overwriting as appropriate.
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was inserted or updated and the (non-null) version of the
     *   new KV pair is returned.  The {@code prevValue} parameter may be
     *   specified to determine whether an insertion or update was performed.
     *   If a non-null previous value or version is returned then an update was
     *   performed, otherwise an insertion was performed.  The previous value
     *   or version may also be useful for other application specific reasons.
     *   <li>
     *   The KV pair was not guaranteed to be inserted or updated successfully,
     *   and one of the exceptions listed below is thrown.  The {@code
     *   prevValue} parameter, if specified, is left unmodified.
     * </ol>
     *
     * @param key the key part of the key/value pair
     *
     * @param value the value part of the key/value pair.
     *
     * @param prevValue a {@link ReturnValueVersion} object to contain the
     * previous value and version associated with the given key, or null if
     * they should not be returned.  The version and value in this object are
     * set to null if a previous value does not exist, or the {@link
     * ReturnValueVersion.Choice} specifies that they should not be returned.
     *
     * @param durability the durability associated with the operation. If null,
     * the {@link KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return the version of the new value.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version put(Key key,
                       Value value,
                       ReturnValueVersion prevValue,
                       Durability durability,
                       long timeout,
                       TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, but only if no value for the given key is present.
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was inserted and the (non-null) version of the new KV pair
     *   is returned.
     *   <li>
     *   The KV pair was not inserted because a value was already present with
     *   the given key; null is returned and no exception is thrown.
     *   <li>
     *   The KV pair was not guaranteed to be inserted successfully and one of
     *   the exceptions listed below is thrown.
     * </ol>
     *
     * @param key the key part of the key/value pair.
     *
     * @param value the value part of the key/value pair.
     *
     * @return the version of the new value, or null if an existing value is
     * present and the put is unsuccessful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version putIfAbsent(Key key,
                               Value value)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, but only if no value for the given key is present.
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was inserted and the (non-null) version of the new KV pair
     *   is returned.  The {@code prevValue} parameter, if specified, will
     *   contain a null previous value and version.
     *   <li>
     *   The KV pair was not inserted because a value was already present with
     *   the given key; null is returned and no exception is thrown.  The
     *   {@code prevValue} parameter, if specified, will contain a non-null
     *   previous value and version.  The previous value and version may be
     *   useful for application specific reasons; for example, if an update
     *   will be performed next in this case.
     *   <li>
     *   The KV pair was not guaranteed to be inserted successfully and one of
     *   the exceptions listed below is thrown.  The {@code prevValue}
     *   parameter, if specified, is left unmodified.
     * </ol>
     *
     * @param key the key part of the key/value pair.
     *
     * @param value the value part of the key/value pair.
     *
     * @param prevValue a {@link ReturnValueVersion} object to contain the
     * previous value and version associated with the given key, or null if
     * they should not be returned.  The version and value in this object are
     * set to null if a previous value does not exist, or the {@link
     * ReturnValueVersion.Choice} specifies that they should not be returned.
     *
     * @param durability the durability associated with the operation. If null,
     * the {@link KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return the version of the new value, or null if an existing value is
     * present and the put is unsuccessful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version putIfAbsent(Key key,
                               Value value,
                               ReturnValueVersion prevValue,
                               Durability durability,
                               long timeout,
                               TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, but only if a value for the given key is present.
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was updated and the (non-null) version of the new KV pair
     *   is returned.
     *   <li>
     *   The KV pair was not updated because no existing value was present with
     *   the given key; null is returned and no exception is thrown.
     *   <li>
     *   The KV pair was not guaranteed to be updated successfully and one of
     *   the exceptions listed below is thrown.
     * </ol>
     *
     * @param key the key part of the key/value pair.
     *
     * @param value the value part of the key/value pair.
     *
     * @return the version of the new value, or null if no existing value is
     * present and the put is unsuccessful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version putIfPresent(Key key,
                                Value value)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, but only if a value for the given key is present.
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was updated and the (non-null) version of the new KV pair
     *   is returned.  The {@code prevValue} parameter, if specified, will
     *   contain a non-null previous value and version.  The previous value and
     *   version may be useful for application specific reasons.
     *   <li>
     *   The KV pair was not updated because no existing value was present with
     *   the given key; null is returned and no exception is thrown.  The
     *   {@code prevValue} parameter, if specified, will contain a null
     *   previous value and version.
     *   <li>
     *   The KV pair was not guaranteed to be updated successfully and one of
     *   the exceptions listed below is thrown.  The {@code prevValue}
     *   parameter, if specified, is left unmodified.
     * </ol>
     *
     * @param key the key part of the key/value pair.
     *
     * @param value the value part of the key/value pair.
     *
     * @param prevValue a {@link ReturnValueVersion} object to contain the
     * previous value and version associated with the given key, or null if
     * they should not be returned.  The version and value in this object are
     * set to null if a previous value does not exist, or the {@link
     * ReturnValueVersion.Choice} specifies that they should not be returned.
     *
     * @param durability the durability associated with the operation. If null,
     * the {@link KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return the version of the new value, or null if no existing value is
     * present and the put is unsuccessful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version putIfPresent(Key key,
                                Value value,
                                ReturnValueVersion prevValue,
                                Durability durability,
                                long timeout,
                                TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, but only if the version of the existing value
     * matches the matchVersion argument. Used when updating a value to ensure
     * that it has not changed since it was last read.
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was updated and the (non-null) version of the new KV pair
     *   is returned.
     *   <li>
     *   The KV pair was not updated because no existing value was present with
     *   the given key, or the version of the existing KV pair did not equal
     *   the given {@code matchVersion} parameter; null is returned and no
     *   exception is thrown.
     *   <li>
     *   The KV pair was not guaranteed to be updated successfully and one of
     *   the exceptions listed below is thrown.
     * </ol>
     *
     * @param key the key part of the key/value pair.
     *
     * @param value the value part of the key/value pair.
     *
     * @param matchVersion the version to be matched.
     *
     * @return the version of the new value, or null if the matchVersion
     * parameter does not match the existing value (or no existing value is
     * present) and the put is unsuccessful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version putIfVersion(Key key,
                                Value value,
                                Version matchVersion)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Put a key/value pair, but only if the version of the existing value
     * matches the matchVersion argument. Used when updating a value to ensure
     * that it has not changed since it was last read.
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was updated and the (non-null) version of the new KV pair
     *   is returned.  The {@code prevValue} parameter, if specified, will
     *   contain a non-null previous value and version.  The previous value may
     *   be useful for application specific reasons.
     *   <li>
     *   The KV pair was not updated because no existing value was present with
     *   the given key, or the version of the existing KV pair did not equal
     *   the given {@code matchVersion} parameter; null is returned and no
     *   exception is thrown.  The {@code prevValue} parameter may be specified
     *   to determine whether the failure was due to a missing KV pair or a
     *   version mismatch.  If a null previous value or version is returned
     *   then the KV pair was missing, otherwise a version mismatch occurred.
     *   The previous value or version may also be useful for other application
     *   specific reasons.
     *   <li>
     *   The KV pair was not guaranteed to be updated successfully and one of
     *   the exceptions listed below is thrown.  The {@code prevValue}
     *   parameter, if specified, is left unmodified.
     * </ol>
     *
     * @param key the key part of the key/value pair.
     *
     * @param value the value part of the key/value pair.
     *
     * @param matchVersion the version to be matched.
     *
     * @param prevValue a {@link ReturnValueVersion} object to contain the
     * previous value and version associated with the given key, or null if
     * they should not be returned.  The version and value in this object are
     * set to null if a previous value does not exist, or the {@link
     * ReturnValueVersion.Choice} specifies that they should not be returned,
     * or the matchVersion parameter matches the existing value and the put is
     * successful.
     *
     * @param durability the durability associated with the operation. If null,
     * the {@link KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return the version of the new value, or null if the matchVersion
     * parameter does not match the existing value (or no existing value is
     * present) and the put is unsuccessful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public Version putIfVersion(Key key,
                                Value value,
                                Version matchVersion,
                                ReturnValueVersion prevValue,
                                Durability durability,
                                long timeout,
                                TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Delete the key/value pair associated with the key.
     *
     * <p>Deleting a key/value pair with this method does not automatically
     * delete its children or descendant key/value pairs.  To delete children
     * or descendants, use {@link #multiDelete multiDelete} instead.</p>
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was deleted and true is returned.
     *   <li>
     *   The KV pair was not deleted because no existing value was present with
     *   the given key; false is returned and no exception is thrown.
     *   <li>
     *   The KV pair was not guaranteed to be deleted successfully and one of
     *   the exceptions listed below is thrown.
     * </ol>
     *
     * @param key the key used to lookup the key/value pair.
     *
     * @return true if the delete is successful, or false if no existing value
     * is present.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public boolean delete(Key key)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Delete the key/value pair associated with the key.
     *
     * <p>Deleting a key/value pair with this method does not automatically
     * delete its children or descendant key/value pairs.  To delete children
     * or descendants, use {@link #multiDelete multiDelete} instead.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was deleted and true is returned.  The {@code prevValue}
     *   parameter, if specified, will contain a non-null previous value and
     *   version.  The previous value may be useful for application specific
     *   reasons.
     *   <li>
     *   The KV pair was not deleted because no existing value was present with
     *   the given key; false is returned and no exception is thrown.  The
     *   {@code prevValue} parameter, if specified, will contain a null
     *   previous value and version.
     *   <li>
     *   The KV pair was not guaranteed to be deleted successfully and one of
     *   the exceptions listed below is thrown.  The {@code prevValue}
     *   parameter, if specified, is left unmodified.
     * </ol>
     *
     * @param key the key used to lookup the key/value pair.
     *
     * @param prevValue a {@link ReturnValueVersion} object to contain the
     * previous value and version associated with the given key, or null if
     * they should not be returned.  The version and value in this object are
     * set to null if a previous value does not exist, or the {@link
     * ReturnValueVersion.Choice} specifies that they should not be returned.
     *
     * @param durability the durability associated with the operation. If null,
     * the {@link KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return true if the delete is successful, or false if no existing value
     * is present.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public boolean delete(Key key,
                          ReturnValueVersion prevValue,
                          Durability durability,
                          long timeout,
                          TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Delete a key/value pair, but only if the version of the existing value
     * matches the matchVersion argument. Used when deleting a value to ensure
     * that it has not changed since it was last read.
     *
     * <p>Deleting a key/value pair with this method does not automatically
     * delete its children or descendant key/value pairs.  To delete children
     * or descendants, use {@link #multiDelete multiDelete} instead.</p>
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was deleted and true is returned.
     *   <li>
     *   The KV pair was not deleted because no existing value was present with
     *   the given key, or the version of the existing KV pair did not equal
     *   the given {@code matchVersion} parameter; false is returned and no
     *   exception is thrown.  There is no way to distinguish between a missing
     *   KV pair and a version mismatch when using this method signature.
     *   <li>
     *   The KV pair was not guaranteed to be deleted successfully and one of
     *   the exceptions listed below is thrown.
     * </ol>
     *
     * @param key the key to be used to identify the key/value pair.
     *
     * @param matchVersion the version to be matched.
     *
     * @return true if the deletion is successful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public boolean deleteIfVersion(Key key,
                                   Version matchVersion)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Delete a key/value pair, but only if the version of the existing value
     * matches the matchVersion argument. Used when deleting a value to ensure
     * that it has not changed since it was last read.
     *
     * <p>Deleting a key/value pair with this method does not automatically
     * delete its children or descendant key/value pairs.  To delete children
     * or descendants, use {@link #multiDelete multiDelete} instead.</p>
     *
     * <p>Possible outcomes when calling this method are:
     * <ol>
     *   <li>
     *   The KV pair was deleted and true is returned.  The {@code prevValue}
     *   parameter, if specified, will contain a non-null previous value and
     *   version.  The previous value may be useful for application specific
     *   reasons.
     *   <li>
     *   The KV pair was not deleted because no existing value was present with
     *   the given key, or the version of the existing KV pair did not equal
     *   the given {@code matchVersion} parameter; false is returned and no
     *   exception is thrown.  The {@code prevValue} parameter may be specified
     *   to determine whether the failure was due to a missing KV pair or a
     *   version mismatch.  If a null previous value or version is returned
     *   then the KV pair was missing, otherwise a version mismatch occurred.
     *   The previous value or version may also be useful for other application
     *   specific reasons.
     *   <li>
     *   The KV pair was not guaranteed to be deleted successfully and one of
     *   the exceptions listed below is thrown.  The {@code prevValue}
     *   parameter, if specified, is left unmodified.
     * </ol>
     *
     * @param key the key to be used to identify the key/value pair.
     *
     * @param matchVersion the version to be matched.
     *
     * @param prevValue a {@link ReturnValueVersion} object to contain the
     * previous value and version associated with the given key, or null if
     * they should not be returned.  The version and value in this object are
     * set to null if a previous value does not exist, or the {@link
     * ReturnValueVersion.Choice} specifies that they should not be returned,
     * or the matchVersion parameter matches the existing value and the delete
     * is successful.
     *
     * @param durability the durability associated with the operation. If null,
     * the {@link KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return true if the deletion is successful.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public boolean deleteIfVersion(Key key,
                                   Version matchVersion,
                                   ReturnValueVersion prevValue,
                                   Durability durability,
                                   long timeout,
                                   TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Deletes the descendant Key/Value pairs associated with the
     * <code>parentKey</code>. The <code>subRange</code> and the
     * <code>depth</code> arguments can be used to further limit the key/value
     * pairs that are deleted.
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * deleted.  It must not be null.  The major key path must be complete.
     * The minor key path may be omitted or may be a partial path.
     *
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are deleted.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @return a count of the keys that were deleted.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public int multiDelete(Key parentKey,
                           KeyRange subRange,
                           Depth depth)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * Deletes the descendant Key/Value pairs associated with the
     * <code>parentKey</code>. The <code>subRange</code> and the
     * <code>depth</code> arguments can be used to further limit the key/value
     * pairs that are deleted.
     *
     * @param parentKey the parent key whose "child" KV pairs are to be
     * deleted.  It must not be null.  The major key path must be complete.
     * The minor key path may be omitted or may be a partial path.
     *
     * @param subRange further restricts the range under the parentKey to
     * the minor path components in this subRange. It may be null.
     *
     * @param depth specifies whether the parent and only children or all
     * descendants are deleted.  If null, {@link Depth#PARENT_AND_DESCENDANTS}
     * is implied.
     *
     * @param durability the durability associated with the operation. If null,
     * the {@link KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * operation.  A best effort is made not to exceed the specified limit. If
     * zero, the {@link KVStoreConfig#getRequestTimeout default request
     * timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return a count of the keys that were deleted.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public int multiDelete(Key parentKey,
                           KeyRange subRange,
                           Depth depth,
                           Durability durability,
                           long timeout,
                           TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException;

    /**
     * This method provides an efficient and transactional mechanism for
     * executing a sequence of operations associated with keys that share the
     * same Major Path. The efficiency results from the use of a single network
     * interaction to accomplish the entire sequence of operations.
     * <p>
     * The operations passed to this method are created using an {@link
     * OperationFactory}, which is obtained from the {@link
     * #getOperationFactory} method.
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
     *   the {@link OperationFactory}.  An {@link OperationExecutionException}
     *   is thrown, and the exception contains information about the failed
     *   operation.</li>
     * </ol>
     * </p>
     * <p>
     * Operations are not executed in the sequence they appear in the {@code
     * operations} list, but are rather executed in an internally defined
     * sequence that prevents deadlocks.  Additionally, if there are two
     * operations for the same key, their relative order of execution is
     * arbitrary; this should be avoided.
     * </p>
     *
     * <p>The {@link KVStoreConfig#getDurability default durability} and {@link
     * KVStoreConfig#getRequestTimeout default request timeout} are used.</p>
     *
     * @param operations the list of operations to be performed. Note that all
     * operations in the list must specify keys with the same Major Path.
     *
     * @return the sequence of results associated with the operation. There is
     * one entry for each Operation in the operations argument list.  The
     * returned list is in the same order as the operations argument list.
     *
     * @throws OperationExecutionException if an operation is not successful as
     * defined by the particular operation (e.g., a delete operation for a
     * non-existent key) <em>and</em> {@code true} was passed for the {@code
     * abortIfUnsuccessful} parameter when the operation was created using the
     * {@link OperationFactory}.
     *
     * @throws IllegalArgumentException if operations is null or empty, or not
     * all operations operate on keys with the same Major Path, or more than
     * one operation has the same Key.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public List<OperationResult> execute(List<Operation> operations)
        throws OperationExecutionException,
               DurabilityException,
               FaultException;

    /**
     * This method provides an efficient and transactional mechanism for
     * executing a sequence of operations associated with keys that share the
     * same Major Path. The efficiency results from the use of a single network
     * interaction to accomplish the entire sequence of operations.
     * <p>
     * The operations passed to this method are created using an {@link
     * OperationFactory}, which is obtained from the {@link
     * #getOperationFactory} method.
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
     *   the {@link OperationFactory}.  An {@link OperationExecutionException}
     *   is thrown, and the exception contains information about the failed
     *   operation.</li>
     * </ol>
     * </p>
     * <p>
     * Operations are not executed in the sequence they appear in the {@code
     * operations} list, but are rather executed in an internally defined
     * sequence that prevents deadlocks.  Additionally, if there are two
     * operations for the same key, their relative order of execution is
     * arbitrary; this should be avoided.
     * </p>
     *
     * @param operations the list of operations to be performed. Note that all
     * operations in the list must specify keys with the same Major Path.
     *
     * @param durability the durability associated with the transaction used to
     * execute the operation sequence.  If null, the {@link
     * KVStoreConfig#getDurability default durability} is used.
     *
     * @param timeout is an upper bound on the time interval for processing the
     * set of operations.  A best effort is made not to exceed the specified
     * limit. If zero, the {@link KVStoreConfig#getRequestTimeout default
     * request timeout} is used.
     *
     * @param timeoutUnit is the unit of the timeout parameter, and may be null
     * only if timeout is zero.
     *
     * @return the sequence of results associated with the operation. There is
     * one entry for each Operation in the operations argument list.  The
     * returned list is in the same order as the operations argument list.
     *
     * @throws OperationExecutionException if an operation is not successful as
     * defined by the particular operation (e.g., a delete operation for a
     * non-existent key) <em>and</em> {@code true} was passed for the {@code
     * abortIfUnsuccessful} parameter when the operation was created using the
     * {@link OperationFactory}.
     *
     * @throws IllegalArgumentException if operations is null or empty, or not
     * all operations operate on keys with the same Major Path, or more than
     * one operation has the same Key.
     *
     * @throws DurabilityException if the specified {@link Durability} cannot
     * be satisfied.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     */
    public List<OperationResult> execute(List<Operation> operations,
                                         Durability durability,
                                         long timeout,
                                         TimeUnit timeoutUnit)
        throws OperationExecutionException,
               DurabilityException,
               FaultException;

    /**
     * For internal use only.
     * @hidden
     *
     * Loads Key/Value pairs supplied by special purpose streams into the store.
     * The bulk loading of the entries is optimized to make efficient use of
     * hardware resources. As a result, this operation can achieve much higher
     * throughput when compared with single Key/Value put APIs. The sequential
     * semantics of individual streams are the basis for stream granularity
     * resumption in the presence of failures.
     *
     * Entries are supplied to the loader by a list of EntryStream instances.
     * Each stream is read sequentially, that is, each EntryStream.getNext() is
     * allowed to finish before the next operation is issued. The load
     * operation typically reads from these streams in parallel as determined
     * by {@link BulkWriteOptions#getStreamParallelism}.
     *
     * If an entry is associated with a key that's already present in the store,
     * the {@link EntryStream#keyExists} method is invoked on it and the entry
     * is not loaded into the store by this method; the
     * {@link EntryStream#keyExists} method may of course choose to do so
     * itself, if the values differ.
     *
     * If the key is absent, a new entry is created in the store, that is, the
     * load operation has putIfAbsent semantics. The putIfAbsent semantics
     * permit restarting a load of a stream that failed for some reason.
     *
     * The collection of streams defines a partial insertion order, with
     * insertion of Key/Value pairs containing the same key within a stream
     * being strictly ordered, but with no ordering constraints being imposed on
     * keys across streams, or for different keys within the same stream.
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
     * Exceptions encountered when inserting a Key/Value pair into the store
     * result in the {@link EntryStream#catchException} being invoked.
     *
     * @param streams the streams that supply the Key/Value pairs to be
     * inserted.
     *
     * @param bulkWriteOptions non-default arguments controlling the behavior
     * the bulk write operations
     */
    public void put(List<EntryStream<KeyValue>> streams,
                    BulkWriteOptions bulkWriteOptions);

    /**
     * Returns a factory that is used to creation operations that can be passed
     * to {@link #execute execute}.
     */
    public OperationFactory getOperationFactory();

    /**
     * Close the K/V Store handle and release resources.  If the K/V Store
     * is secure, this also logs out of the store.
     */
    @Override
    public void close();

    /**
     * Returns the statistics related to the K/V Store client.
     *
     * @param clear If set to true, configure the statistics operation to
     * reset statistics after they are returned.
     *
     * @return The K/V Store client side statistics.
     */
    public KVStats getStats(boolean clear);

    /**
     * Returns the catalog of Avro schemas and bindings for this store.  The
     * catalog returned is a cached object and may require refresh.  See
     * {@link AvroCatalog#refreshSchemaCache} for details.
     *
     * @since 2.0
     */
    public AvroCatalog getAvroCatalog();

    /**
     * Returns an instance of the TableAPI interface for the store.
     */
    public TableAPI getTableAPI();

    /**
     * Updates the login credentials for this store handle.
     * This should be called in one of two specific circumstances.
     * <ul>
     * <li>
     * If the application has caught an
     * {@link AuthenticationRequiredException},
     * this method will attempt to re-establish the authentication of this
     * handle against the store, and the credentials provided must be for
     * the store handle's currently logged-in user.
     * </li>
     * <li>
     * If this handle is currently logged out, login credentials for any user
     * may be provided, and the login, if successful, will associate the
     * handle with that user.
     * </li>
     * </ul>
     * @param creds the login credentials to associate with the store handle
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws AuthenticationFailureException if the LoginCredentials
     * do not contain valid credentials.
     *
     * @throws IllegalArgumentException if the LoginCredentials are null
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     *
     * @since 3.0
     */
    public void login(LoginCredentials creds)
        throws RequestTimeoutException, AuthenticationFailureException,
               FaultException, IllegalArgumentException;

    /**
     * Logout the store handle.  After calling this method, the application
     * should not call methods on this handle other than close() before first
     * making a successful call to login().  Calls to other methods will result
     * in AuthenticationRequiredException being thrown.
     *
     * @throws RequestTimeoutException if the request timeout interval was
     * exceeded.
     *
     * @throws FaultException if the operation cannot be completed for any
     * reason.
     *
     * @since 3.0
     */
    public void logout()
        throws RequestTimeoutException, FaultException;

    /**
     * Asynchronously executes a DDL statement. DDL statements can be used to
     * create or modify tables, indices, users and roles. The operation is
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
     *     future = store.execute
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
     *       store.execute("CREATE INDEX age ON users(age)");
     *
     *   // process B starts the same index creation. If the index creation is
     *   // still running in the cluster, futureA and futureB will refer to
     *   // the same operation
     *   ExecutionFuture futureB =
     *       store.execute("CREATE INDEX age ON users(age)");
     * </pre>
     * <p>
     * Note that, in a secure store, creating and modifying table and index
     * definitions may require a level of system privileges over and beyond
     * that required for reads and writes of table records.
     * <br>
     * See the documentation for a full description of the supported statements.
     * @param statement must follow valid Table syntax.
     * @throws IllegalArgumentException if the statement is not valid
     * @throws FaultException if the statement cannot be completed. This
     * indicates a transient problem with communication to the server or
     * within the server, and the statement can be retried.
     * <p>
     * Note that this method supersedes oracle.kv.table.TableAPI.execute.
     * @since 3.3
     */
    ExecutionFuture execute(String statement)
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
     * <p>
     * Note that this method supersedes oracle.kv.table.TableAPI.executeSync
     * @since 3.3
     */
    StatementResult executeSync(String statement)
        throws FaultException,
               IllegalArgumentException;

   /**
     * Obtain a handle onto a previously issued DDL operation, using a
     * serialized version of the ExecutionFuture obtained from
     * {@link ExecutionFuture#toByteArray}. Does not result in any
     * communication with the server. The user must call the appropriate
     * methods on the reconstituted future to get up to date status.
     * For example:
     * <pre>
     * // futureBytes can be saved and used later to recreate an
     * // ExecutionFuture instance
     * ExecutionFuture laterFuture = store.getFuture(futureBytes);
     *
     * // laterFuture doesn't have any status yet. Call ExecutionFuture.get
     * // or updateStatus to communicate with the server and get new
     * // information
     * StatementResult laterResult = laterFuture.get();
     * </pre>
     * @return an ExecutionFuture representing a previously issued DDL
     * operation
     * @throws IllegalArgumentException if futureBytes isn't a valid
     * representation of an ExecutionFuture instance.
     * @since 3.3
     */
    ExecutionFuture getFuture(byte[] futureBytes)
        throws IllegalArgumentException;
}
