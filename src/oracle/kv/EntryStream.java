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

/**
 * For internal use only.
 * @hidden
 *
 * The stream interface that supplies the data (Row or Key/Value pair) to be
 * batched and loaded into the store.
 *
 * Any exceptions throw by methods of this interface are handled by the method
 * {@link #catchException}.
 *
 * @param <E> Must be a row, or a KV pair. The term entry is used to cover both
 * cases, in the API and the following javadoc.
 */
public interface EntryStream<E> {

    /**
     * Returns a name to associate with the stream. It's used to identify
     * a specific stream in logs and exception messages.
     *
     * @return the name of the stream. It must not be null.
     */
    String name();

    /**
     * Returns the next entry in the stream. This method is invoked
     * sequentially so that each getNext() operation is allowed to complete
     * before the next call is issues by the loader.
     *
     * The order of entries in the stream impacts duplicate processing.
     * Assuming the key is not already present in the store, at the time the
     * load operation is initiated, the first entry will result in the key
     * being inserted and the second and subsequent duplicate entries will
     * result in the keyExists method being invoked.
     *
     * @return the next entry in the stream or null if at the end of the stream
     */
    E getNext();

    /**
     * Invoked by the loader to indicate that all the entries supplied by the
     * stream have been processed. The callback happens sometime after the
     * <code>getNext()</code> method returns null and all entries supplied by
     * the stream have been written to the store.
     *
     * Applications may choose to use this callback to checkpoint progress
     * during a bulk operation. In case there is a failure, the operation can
     * be resumed by skipping streams that have been completely processed, but
     * reloading all streams that have been partially processed or have not
     * been processed at all. Reloading a partially processed stream will
     * result in {#keyExists} being invoked for entries that were already
     * loaded.
     *
     * The method should not block. It must do minimal processing, delegating
     * any blocking or time-consuming operations to a separate thread and
     * return back to the caller.
     *
     * @see
     */
    void completed();

    /**
     * The method that's invoked when an entry with an existing primary key is
     * found in the store.
     *
     * This method must be re-entrant and should not block. It must do minimal
     * processing, delegating any blocking or time-consuming operations to a
     * separate thread of control and return back to the caller.
     *
     * @param entry the entry associated with the key that was already present.
     */
    void keyExists(E entry);

    /**
     * The method that is invoked when an exception (e.g. Durability,
     * RequestTimeout, etc.) is encountered while trying to add an entry to
     * the store.
     *
     * The method must be re-entrant, since the bulk load operation is run
     * in parallel and multiple concurrent exceptions can be encountered
     * simultaneously.
     *
     * If the method returns normally, the entry is ignored and the bulk
     * load operation continues with the loading of other entries. If the
     * methods throws an exception, the entire bulk load operation is
     * terminated and the exception is rethrown from the <code>put</code>
     * operation that initiated the bulk load.
     *
     * @param exception the exception that was encountered
     *
     * @param entry the entry(Row or KeyValue) associated with the exception
     */
    void catchException(RuntimeException exception, E entry);
}
