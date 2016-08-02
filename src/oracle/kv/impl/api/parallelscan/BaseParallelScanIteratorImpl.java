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

package oracle.kv.impl.api.parallelscan;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.kv.Direction;
import oracle.kv.KVStore;
import oracle.kv.ParallelScanIterator;
import oracle.kv.RequestTimeoutException;
import oracle.kv.StoreIteratorException;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.KVStoreImpl.TaskExecutor;
import oracle.kv.impl.api.Request;
import oracle.kv.impl.api.ops.Result;
import oracle.kv.impl.api.parallelscan.ParallelScanHook.HookType;

/**
 * Implementation of a scatter-gather iterator. The iterator will access the
 * store, possibly in parallel, and sort the values before returning them
 * through the next() method.
 * <p>
 * Theory of Operation
 * <p>
 * When the iterator is created, one {@code Stream} instance is created
 * for each single range, such as a shard or partition. {@code Stream} maintains
 * state required for reading from the store, and the returned data. The
 * instances are kept in a {@code TreeSet} and remain until there are no more
 * records available from the range. How the streams are sorted is described
 * below.
 * <p>
 * When the {@code Stream} is first created it is submitted to the thread pool
 * executor. When run, it will attempt to use makeReadRequest() method to read a
 * block of records from the store. The returned data is placed on the
 * {@code blocks} queue. If there are more records in the store, it will use
 * setResumeKey method to save resume key and if there is room on the queue, the
 * stream re-submits itself to eventually read another block. There is locking
 * which will prevent the stream from being submitted more than once. There is
 * more on threads and reading below.
 * <p>
 * Sorting
 * <p>
 * {@code Stream} implements {@code Comparable}. (Note that {@code Stream} has a
 * natural ordering that is inconsistent with {@code equals}.) When a stream is
 * inserted into the {@code TreeSet} it will sort
 * (using {@code Comparable.compareTo}) on the next element in the stream. If
 * the stream does not have a next element (because the stream has been drained
 * and the read from the store has not yet returned)
 * {@code Comparable.compareTo()} will return -1 causing the stream to sort
 * to the beginning of the set. This means that the first stream in the
 * {@code TreeSet} has the overall next element or is empty waiting on data.
 * See the section below on Direction for an exception to behavior.
 * <p>
 * To get the next overall element the basic steps are as follows:
 * <p>
 * 1. Remove the first stream from the {@code TreeSet} <br>
 * 2. Remove the next element from the stream  <br>
 * 3. Re-insert the stream into the {@code TreeSet}  <br>
 * 4. If the element is not null, return  <br>
 * 5. If the element is null, wait and go back to 1  <br>
 * <p>
 * Removing the element at #2 will cause the stream to update the next
 * element with the element next in line (if any). This will cause the
 * stream to sort based on the new element when re-inserted at #3.
 * <p>
 * There is an optimization at step #5 which will skip the wait if
 * removing an element (#2) resulting in the stream having a non-null
 * next element.
 * <p>
 * Reading
 * <p>
 * Initially, each {@code Stream} is submitted to the executor. As mentioned
 * above the {@code Stream} will read a block and if there is more data
 * available and space in the queue, will re-submit itself to read more data.
 * This will result in reading {@code QUEUE_SIZE} blocks for each range. The
 * {@code ShardStream} is not re-submitted once the queue is filled. If reading
 * from the store results in an end-of-data, a flag is set preventing the stream
 * from being submitted.
 * <p>
 * Once elements are removed from the iterator and a block is removed from the
 * queue, an attempt is made to submit a stream for reading. This happens
 * at step #2 above in {@code Stream.removeNext()}. The method
 * {@code removeNext()} will first attempt to remove an element from the
 * current block. If that block is empty it removes the next block on the
 * queue, making that the current block. At this point the stream is
 * submitted to the executor.
 */
public abstract class BaseParallelScanIteratorImpl<K>
    implements ParallelScanIterator<K> {
    /*
     * Use to convert, instead of TimeUnit.toMills(), when you don't want
     * a negative result.
     */
    private static final long NANOS_TO_MILLIS = 1000000L;
    /* Time to wait for data to be returned form the store */
    private static final long WAIT_TIME_MS = 100L;

    /*
     * The default size of the queue of blocks in each stream. Note that the
     * maximum number of blocks maintained by the stream is QUEUE_SIZE + 1.
     * (The queue plus the current block).
     */
    private static final int QUEUE_SIZE = 3;

    protected KVStoreImpl storeImpl;
    protected Logger logger;
    protected long requestTimeoutMs;
    protected Direction itrDirection;
    protected TaskExecutor taskExecutor;
    /*
     * The sorted set of streams. Only streams that have elements or are
     * waiting on reads are in the set. Streams that have exhausted the
     * store are discarded.
     */
    protected TreeSet<Stream> streams;

    /* True if the iterator has been closed */
    protected volatile boolean closed = false;

    /* The exception passed to close(Exception) */
    protected Exception closeException = null;

    /*
     * The next element to be returned from this iterator. This may be null
     * if waiting for reads to complete.
     */
    protected K next = null;

    /*
     * The size of queue of blocks in each stream.
     */
    private int maxResultsBatches = 0;

    /* -- From Iterator -- */

    @Override
    public boolean hasNext() {
        if (isClosed()) {
            return false;
        }

        if (next == null) {
            next = getNext();
        }
        return (next != null);
    }

    @Override
    public K next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final K lastReturned = next;
        next = null;
        return lastReturned;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /* -- From ParallelScanIterator -- */

    @Override
    public void close() {
        close(null);
    }

    /* Sets the max number of result batches */
    protected void setMaxResultsBatches(int maxResultsBatches) {
        this.maxResultsBatches = maxResultsBatches;
    }

    /* Returns the max number of result batches. */
    protected int getMaxResultsBatches() {
        return (maxResultsBatches > 0) ? maxResultsBatches : QUEUE_SIZE ;
    }

    /**
     * Returns true if the iterator is closed, otherwise false. If there
     * was an exception which caused the iterator to be closed a
     * StoreIteratorException is thrown containing the original exception.
     *
     * @return true if the iterator is closed
     */
    private boolean isClosed() {
        if (closed) {
            /*
             * The iterator is closed. If there was an exception during
             * some internal operation, throw it now.
             */
            if (closeException != null) {
                throw new StoreIteratorException(closeException, null);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets the next value in sorted order. If no more values remain or the
     * iterator is canceled, null is returned. This method will block
     * waiting for data from the store.
     *
     * @return the next value in sorted order or null
     */
    private K getNext() {
        final long limitNs = System.nanoTime() +
                             MILLISECONDS.toNanos(requestTimeoutMs);
        while (!isClosed()) {

            /*
             * The first stream in the set will contain the next
             * element in order.
             */
            final Stream stream = streams.pollFirst();

            /* If there are no more streams we are done. */
            if (stream == null) {
                close();
                return null;
            }
            final K entry = stream.removeNext();

            /*
             * Return the stream back to the set where it will sort on the
             * next element.
             */
            if (!stream.isDone()) {
                streams.add(stream);
            }

            /*
             * removeNext() may have waited or thrown an exception so check
             * for closed.
             */
            if (isClosed()) {
                return null;
            }

            if (entry != null) {
                return entry;
            }

            /* The stream is empty, if we have time, wait */
            long waitMs =
                   Math.min((limitNs - System.nanoTime()) / NANOS_TO_MILLIS,
                            WAIT_TIME_MS);
            if (waitMs <= 0) {
                throw new RequestTimeoutException
                    ((int)requestTimeoutMs,
                     ("Operation timed out on shard: " + stream),
                     null, false);
            }
            stream.waitForNext(waitMs);
        }
        return null;    /* Closed */
    }

    /**
     * Close the iterator, recording the specified remote exception. If
     * the reason is not null, the exception is thrown from the hasNext()
     * or next() methods.
     *
     * @param reason the exception causing the close or null
     */
    protected abstract void close(Exception reason);

    /**
     * Compares the two elements. Returns a negative integer, zero, or a
     * positive integer if object one is less than, equal to, or greater
     * than object two.
     *
     * @return a negative integer, zero, or a positive integer
     */
    protected abstract int compare(K one, K two);

    /**
     * Convert the specified result into list of elements.
     * If a RuntimeException is thrown, the iterator will be
     * closed and the exception return to the application.
     *
     * @param result result object
     * @param resultList list to place the converted elements
     */
    protected abstract void convertResult(Result result,
                                          List<K> elementList);

    /**
     * Object that encapsulates the activity around reading records of a single
     * range, such as a shard or partition.
     *
     * Note: this class has a natural ordering that is inconsistent with
     * equals.
     */
    public abstract class Stream implements Comparable<Stream>, Runnable {
        /* The queue of blocks. */
        private final BlockingQueue<List<K>> blocks =
            new LinkedBlockingQueue<List<K>>(getMaxResultsBatches());

        /* The block of values being drained */
        private List<K> currentBlock;

        /* The last element removed, used for sorting */
        private K nextElem = null;

        /* False if nothing left to read */
        private boolean doneReading = false;

        /* True if there are no more values */
        private boolean done = false;

        /* True if this stream is  */
        private boolean active = false;
        /**
         * Remove the next element from this stream and return it. If no
         * elements are available null is returned.
         *
         * @return the next element from this stream or null
         */
        K removeNext() {
            assert !done;

            final K ret = nextElem;
            nextElem = ((currentBlock == null) ||
                    currentBlock.isEmpty()) ? null : currentBlock.remove(0);

            /*
             * If there are no more results in the current block, attempt
             * to get a new block.
             */
            if (nextElem == null) {
                synchronized (this) {
                    currentBlock = blocks.poll();

                    /*
                     * We may have pulled a block off the queue, submit this
                     * stream to get more.
                     */
                    submit();

                    /*
                     * If there are no more blocks and we are done reading
                     * then we are finished.
                     */
                    if (currentBlock == null) {
                        done = doneReading;
                    } else {
                        /* TODO - can this be empty? */
                        nextElem = currentBlock.remove(0);
                    }
                }
            }
            return ret;
        }

        /**
         * Waits up to waitMs for the next element to be available.
         *
         * @param waitMs the max time in ms to wait
         */
        private void waitForNext(long waitMs) {
            /*
             * If the stream was previously empty, but it now has a value,
             * skip the sleep and immediately try again. This can happen
             * when the stream is initially created (priming the pump) and
             * when the reads are not keeping up with removal through the
             * iterator.
             */
            if (nextElem != null) {
                return;
            }
            try {
                synchronized (this) {
                    /* Wait if there is no data left and still reading */
                    if (blocks.isEmpty() && !doneReading) {
                        wait(waitMs);
                    }
                }
            } catch (InterruptedException ex) {
                if (!closed) {
                    logger.log(Level.WARNING, "Unexpected interrupt ", ex);
                }
            }
        }

        /**
         * Returns true if all of the elements have been removed from this
         * stream.
         */
        boolean isDone() {
            return done;
        }

        /**
         * Submit this stream to to request data if there it isn't already
         * submitted, there is more data to read, and there is room for it.
         */
        public synchronized void submit() {
            if (active ||
                doneReading ||
                (blocks.remainingCapacity() == 0)) {
                return;
            }
            active = true;
            try{
                taskExecutor.submit(this);
            } catch (RejectedExecutionException ree) {
                active = false;
                close(ree);
            }
        }

        @Override
        public void run() {
            try {
                assert active;
                assert !doneReading;
                assert blocks.remainingCapacity() > 0;

                final long start = System.nanoTime();
                final int count = readBlock();

                final long end = System.nanoTime();
                final long thisTimeMs = (end - start) / NANOS_TO_MILLIS;

                updateDetailedMetrics(thisTimeMs, count);
            } catch (RuntimeException re) {
                active = false;
                close(re);
            }
        }

        protected boolean hasMoreElements(Result result) {
            return result.hasMoreElements();
        }

        /**
         * Read a block of records from the store. Returns the number of
         * records read.
         */
        private int readBlock() {
            final Request req = makeReadRequest();
            if (req == null) {
                synchronized (this) {
                    /* About to exit, active may be reset in submit() */
                    active = false;
                    doneReading = true;
                    /* Wake up the iterator if it is waiting */
                    notify();
                    return 0;
                }
            }

            assert storeImpl.getParallelScanHook() == null ?
                true :
                storeImpl.getParallelScanHook().
                callback(Thread.currentThread(),
                         HookType.BEFORE_EXECUTE_REQUEST, null);
            final Result result = storeImpl.executeRequest(req);
            final boolean hasMore = hasMoreElements(result);
            int nRecords = result.getNumRecords();

            /*
             * Convert the results into a list of elements ready to be
             * returned from the iterator.
             */
            List<K> elementList = null;
            if (nRecords > 0) {
                elementList = new ArrayList<K>(nRecords);
                convertResult(result, elementList);

                /*
                 * convertResult can filter so get the exact number of
                 * converted results.
                 */
                nRecords = elementList.size();

                setResumeKey(result, elementList);
            }
            synchronized (this) {
                /* About to exit, active may be reset in submit() */
                active = false;
                doneReading = !hasMore;

                if (nRecords == 0) {
                    assert storeImpl.getParallelScanHook() == null ?
                           true :
                           storeImpl.getParallelScanHook().
                           callback(Thread.currentThread(),
                                    HookType.AFTER_PROCESSING_STREAM, null);
                    notify();
                    return 0;
                }
                assert elementList != null;
                blocks.add(elementList);

                /* Wake up the iterator if it is waiting */
                notify();
            }
            submit();
            return nRecords;
        }

        /**
         * Compares this object with the specified object for order for
         * determining the placement of the stream in the TreeSet. See
         * the IndexScan class doc a detailed description of its operation.
         */
        @Override
        public int compareTo(Stream other) {
            /*
             * The same stream is always equal. This is the only time that
             * 0 can be returned.
             */
            if (this == other) {
                return 0;
            }

            /*
             * If unordered, we skip comparing the streams and always sort
             * to the top if we have a next element. If no elements, sort
             * to the bottom. This will keep full streams at the top and
             * empty streams at the bottom.
             */
            if (itrDirection == Direction.UNORDERED) {
                return (nextElem == null) ? 1 : -1;
            }

            /* Forward or reverse */

            /* If we don't have a next, then sort to the top of the set. */
            if (nextElem == null) {
                return -1;
            }

            /* If the other next is null then keep them on top. */
            final K otherNext = other.nextElem;
            if (otherNext == null) {
                return 1;
            }

            /*
             * Finally, compare the elements, inverting the result
             * if necessary.
             */
            final int comp = compare(nextElem, otherNext);

            /*
             * If the different stream elements are equal then there is
             * a duplicate entry, possibly due to partition migration. We
             * currently don't handle is case, so punt.
             */
            if (comp == 0) {
                close(new IllegalStateException("Detected an unexpected " +
                                                "duplicate record"));
            }
            return itrDirection == Direction.FORWARD ? comp : (comp * -1);
        }

        /**
         * Get stream status string.
         */
        public String getStatus() {
            return done + ", " + active + ", " + doneReading + ", "
                   + blocks.size();
        }

        /**
         * Update the metrics for this iterator.
         *
         * @param timeInMs the time spent reading
         * @param recordCount the number of records read
         */
        protected abstract void updateDetailedMetrics(final long timeInMs,
                                                      final long recordCount);

        /**
         * Update resume key for next read request.
         * @param result result object
         * @param elementList the list of elements converted by convert
         */
        protected abstract void setResumeKey(Result result,
                                             List<K> elementList);

        /**
         * Create a request using resume key to get next block of records.
         * The read request may return null for bulk get operations when the
         * keys supplied by the application run out and no further requests to
         * the store are required.
         *
         * @see KVStore#storeIterator(java.util.Iterator, int, oracle.kv.KeyRange, oracle.kv.Depth, oracle.kv.Consistency, long, java.util.concurrent.TimeUnit, oracle.kv.StoreIteratorConfig)
         */
        protected abstract Request makeReadRequest();
    }
}
