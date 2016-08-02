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

package oracle.kv.impl.tif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;

import com.sleepycat.je.utilint.VLSN;

/**
 * Object maintaining a list of open transactions to keep track of
 * operations to be performed to ES index. When a transaction is
 * committed or aborted, the transaction will be applied to ES index
 * as a whole or aborted. For each key transferred from the Partition
 * Migration Service, agenda would commit each key to ES index as an
 * single index operation. During commit, client-defined commit callback is
 * fired for each transaction or key. The agenda remembers VLSN of the
 * last transaction committed to ES index.
 *
 * TODO: enforce a size limit of the transactions Map.
 */
class TransactionAgenda {

    /* logger */
    private final Logger logger;
    /* list of open transactions indexed by transaction ID */
    private final Map<Long, Transaction> transactions;
    /* ElasticSearch handler */
    private final ElasticsearchHandler esHandler;

    /* VLSN of last txn committed to ES */
    private VLSN lastCommittedVLSN;

    /* statistics */
    private AtomicLong numOpenTXNs;
    private AtomicLong numCommittedKeys;
    private AtomicLong numCommittedTXNs;
    private AtomicLong numAbortedTXNs;
    private AtomicLong maxNumOpenTXNs;
    private long numberOfBatches;
    private long sumOfBatchSizes;
    private long sumOfBatchOperations;
    private long cumulativeTimeInBatchRequest;
    private long statSampleStartTime;

    /* commit call back */
    private TransactionPostCommitCallback clientCallback;

    /* A Timer for periodic commit flushing. */
    private final Timer pendingCommitTimer;
    /*
     * The commit queue, used to allow commits to pile up so they can be
     * indexed as a batch.
     */
    private final CommitQueue pendingCommits;
    /* A label used to identify this TransactionAgenda in log messages. */
    private String name;

    /* TODO: all uses of these constants should be overridable by parameters */
    private static final int COMMIT_PERIOD_MS = 5000;
    private static final int MAX_PENDING_COMMIT_SIZE = 5 *1024 * 1024;
    private static final int COMMIT_QUEUE_CAPACITY = 20 * 1024 * 1024;
    private static final int STAT_SAMPLE_PERIOD_MS = 5 * 60 * 1000;

    TransactionAgenda(final ElasticsearchHandler esHandler,
                      final Logger logger,
                      final String name) {
        /* by default no client defined post commit */
        clientCallback = null;
        this.logger = logger;
        this.esHandler = esHandler;
        this.name = name;
        lastCommittedVLSN = VLSN.NULL_VLSN;
        transactions = new HashMap<>();
        numOpenTXNs = new AtomicLong(0);
        numCommittedTXNs = new AtomicLong(0);
        numAbortedTXNs = new AtomicLong(0);
        maxNumOpenTXNs = new AtomicLong(0);
        numCommittedKeys = new AtomicLong(0);

        pendingCommits = new CommitQueue(COMMIT_QUEUE_CAPACITY);

        pendingCommitTimer = new Timer(true);
        pendingCommitTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        /*
                         * Flush pending commits periodically, regardless of
                         * the size of the commit queue.
                         */
                        flushPendingCommits();
                    } catch (Exception e) {
                        logger.log(Level.WARNING,
                                   lm("Periodic flush to ES threw exception"),
                                   e);
                    }
                }
            }, COMMIT_PERIOD_MS, COMMIT_PERIOD_MS);

        statSampleStartTime = System.currentTimeMillis();
    }

    /**
     * Stop the timer threads so that this Agenda doesn't become a zombie.
     */
    public void stop() {
        logger.info(lm("stopped. final " +
                       getStat(System.currentTimeMillis())));
        pendingCommitTimer.cancel();
    }

    /**
     * Set client defined post commit callback
     * @param c client defined post commit callback
     */
    synchronized public void setPostCommitCbk(TransactionPostCommitCallback c) {
        clientCallback = c;
    }

    /**
     * Get the VLSN of last committed transaction to ES index
     *
     * @return minimal VLSN
     */
    public VLSN getLastCommittedVLSN() {
        return lastCommittedVLSN;
    }

    /**
     * Add an operation to agenda. Create an open txn if it is the first
     * operation in this txn.
     *
     * @param txnid id of txn
     * @param op    index operation
     */
    synchronized public void addOp(long txnid, IndexOperation op) {
        if (transactions.containsKey(txnid)) {
            transactions.get(txnid).addOp(op);
        } else {
            Transaction txn = new Transaction(txnid);
            txn.addOp(op);
            transactions.put(txnid, txn);
            numOpenTXNs.getAndIncrement();

            /* update statistics */
            if (maxNumOpenTXNs.get() < numOpenTXNs.get()) {
                maxNumOpenTXNs.getAndSet(numOpenTXNs.get());
            }
        }
    }

    /**
     * Abort a txn from agenda
     *
     * @param txnid id of transaction to abort
     */
    synchronized public void abort(long txnid) {
        transactions.remove(txnid);
        numOpenTXNs.decrementAndGet();
        numAbortedTXNs.incrementAndGet();
    }

    /**
     * Commit an open txn from agenda
     *
     * @param txnid  id of transaction to commit
     */
    public void commit(long txnid, VLSN commitVLSN) {

        Transaction txn = null;
        synchronized(this) {
            txn = transactions.get(txnid);
        }

        /* return if nothing to commit or txn does not exist */

        if (txn == null) {
            logger.finest(lm("Commit a non-existent txn id " + txnid));
            return;
        }

        pendingCommits.add(txn, commitVLSN);

        flushMaybe();
    }

    /**
     * Commit a copy operation from partition transfer
     *
     * @param op   operation to be sent to ES index
     */
    public void commit(IndexOperation op) {

        pendingCommits.add(op);
        flushMaybe();
    }

    /*
     * Trigger a flush if the pent up commits are big enough.
     */
    private void flushMaybe() {
        if (pendingCommits.getSize() >= MAX_PENDING_COMMIT_SIZE) {
            flushPendingCommits();
        }
    }

    /*
     * Periodically log stats; this is a temporary measure until we integrate
     * stats with the system-wide stats collection facility.
     */
    private void logStatsMaybe() {
        long now = System.currentTimeMillis();
        if (now - statSampleStartTime >= STAT_SAMPLE_PERIOD_MS) {
            logger.info(lm(getStat(now)));
            statSampleStartTime = now;
            resetBatchStats();
        }
    }

    /*
     * Grab a batch of commits from the commit queue, and send them
     * as a bulk operation to Elasticsearch.  Handle errors.
     */
    private void flushPendingCommits() {

        List<Commit> batch =
            pendingCommits.claimBatch(MAX_PENDING_COMMIT_SIZE);

        /*
         * A null return from claimBatch means that either there is nothing to
         * do, or a batch is already in progress.  Only one bulk operations can
         * be in progress at a time, per TransactionAgenda.
         */
        if (batch == null) {
            return;
        }

        /* At this point we are single-threaded in this path by virtue of the
         * single-batch-at-a-time rule.
         */

        logStatsMaybe();

        try {
            BulkResponse br = esHandler.doBulkOperations(batch);
            cumulativeTimeInBatchRequest += br.getTookInMillis();
            /*
             * If the batch failed with retriable errors, then we can drop the
             * claim on pendingCommits and let the next flush re-try it.  If
             * there were some successful operations in the batch, they are
             * idempotent and it will not hurt to do them again.  If all of the
             * errors are not retriable, we'll just give up on them.  Perhaps
             * in the future we'll have a means of getting a message back to
             * the user about such failures.
             */
            if (br.hasFailures()) {
                logger.warning(lm("Bulk request failed"));
                for (BulkItemResponse item : br.getItems()) {
                    if (item.isFailed()) {

                        boolean retriable =
                            ElasticsearchHandler.isRetriable(item.getFailure());

                        logger.log
                            (Level.WARNING,
                             lm(" item {0}. {1}. VLSN: {2}. STATUS: {3} - {4}"),
                             new Object[]
                                {item.getItemId(),
                                 item.getFailureMessage(),
                                 batch.get(item.getItemId()).getCommitVLSN(),
                                 item.getFailure().getStatus(),
                                 (retriable ? "will" : "won't") + " retry"});
                        if (retriable) {
                            pendingCommits.cancelBatch();
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            /* An exception means that the ES client could not contact a node
             * of the cluster.
             */
            logger.log
                (Level.WARNING, lm("Bulk request failed, will re-try."), e);
            pendingCommits.cancelBatch(); /* Re-try next time around */
            return;
        }

        pendingCommits.confirmBatch(); /* Remove the batch, it's done */

        for (Commit commit : batch) {
            final Transaction txn = commit.getTxn();

            if (txn == null) {
                /* The Commit represents a single COPY transfer operation. */
                if (clientCallback != null) {
                    /* call post commit callback -- used for testing */
                    clientCallback.postCommit
                        (commit.getOps().iterator().next());
                }
                numCommittedKeys.getAndIncrement();
            } else {
                /* The Commit represents a transaction. */
                final VLSN commitVLSN = commit.getCommitVLSN();

                if (clientCallback != null) {
                    clientCallback.postCommit(txn, commitVLSN);
                }
                /* clean up and update statistics */
                synchronized(this) {
                    transactions.remove(txn.getTxnId());
                }
                lastCommittedVLSN = commitVLSN;
                numOpenTXNs.decrementAndGet();
                numCommittedTXNs.incrementAndGet();
            }
        }
    }

    private String getStat(long now) {
        String s = "txn agenda stats [" +
            "open txns " + numOpenTXNs +
            ", committed txns " + numCommittedTXNs +
            ", aborted txns " + numAbortedTXNs +
            ", max open txns " + maxNumOpenTXNs +
            ", committed keys " + numCommittedKeys +
            ", last committed VLSN " + lastCommittedVLSN;
        /* Avoid division by zero! */
        if (numberOfBatches > 0) {
            s += ", avg batch size " + sumOfBatchSizes/numberOfBatches +
                ", avg ops per batch " + sumOfBatchOperations/numberOfBatches +
                ", avg ms per batch " +
                cumulativeTimeInBatchRequest/numberOfBatches +
                ", avg bytes per sec " +
                sumOfBatchSizes * 1000/(now - statSampleStartTime);
        }

        return s + "]";
    }

    private void resetBatchStats() {
        sumOfBatchSizes = 0;
        numberOfBatches = 0;
        sumOfBatchOperations = 0;
        cumulativeTimeInBatchRequest = 0;
    }

    /*
     * Preprocess a log message string to include the name of this
     * TransactionAgenda instance.  TODO: future grand redesign of the logging
     * system will no doubt override this localized band-aid...
     */
    private String lm(String s) {
        return "[tif][" + name + "] " + s;
    }

    /**
     * Object to represent an open transaction in agenda
     */
    public static class Transaction {

        private final long txnId;
        private final List<IndexOperation> ops;

        Transaction(long id) {
            txnId = id;
            ops = new ArrayList<>();
        }

        public long getTxnId() {
            return txnId;
        }

        public List<IndexOperation> getOps() {
            return ops;
        }

        public void addOp(IndexOperation op) {
            assert (op != null);
            ops.add(op);
        }

        public int size() {
            int totalSize = 0;
            for (IndexOperation op : ops) {
                totalSize += op.size();
            }
            return totalSize;
        }

        @Override
        public String toString() {
            String opList = "op list: ";
            for (IndexOperation op : ops) {
                opList += op.getOperation().toString() + "(key " + op
                    .getPkPath() + ") ";
            }
            return "Transaction with id " + getTxnId() + ", " +
                   ", total # ops " + ops.size() +
                   ", ops list: " + opList;
        }
    }

    /*
     * This class represents a transaction commit, or a singleton transfer COPY
     * operation.
     */
    abstract static class Commit {
        private final int size;

        Commit(int size) {
            this.size = size;
        }

        int size() {
            return size;
        }

        abstract Transaction getTxn();
        abstract VLSN getCommitVLSN();
        abstract List<IndexOperation> getOps();
    }

    private static class TransactionCommit extends Commit {
        private final Transaction txn;
        private final VLSN commitVLSN;

        TransactionCommit(Transaction txn, VLSN commitVLSN) {
            super(txn.size());
            this.txn = txn;
            this.commitVLSN = commitVLSN;
        }

        @Override
        List<IndexOperation> getOps() {
            return txn.getOps();
        }

        @Override
        Transaction getTxn() {
            return txn;
        }

        @Override
        VLSN getCommitVLSN() {
            return commitVLSN;
        }
    }

    private static class TransferCommit extends Commit {
        private final List<IndexOperation> singletonOperation;

        TransferCommit(IndexOperation op) {
            super(op.size());
            singletonOperation = Collections.singletonList(op);
        }

        @Override
        List<IndexOperation> getOps() {
            return singletonOperation;
        }

        @Override
        Transaction getTxn() {
            return null;
        }

        @Override
        VLSN getCommitVLSN() {
            return null;
        }
    }

    /*
     * This class keeps track of commits that have been received and processed
     * by the TIF but which have not yet been sent to Elasticsearch.
     *
     * One feature of CommitQueue that distinguishes it from a standard library
     * Queue is that it keeps track of the aggregate size of the pending
     * indexing operations, as opposed to their number.
     *
     * When a consumer of the queue is ready to process a batch, it calls
     * claimBatch specifying the size of the batch it is willing to process.
     * This size is not the number of items but their aggregate size.
     *
     * To maintain commit order, only a single batch can be outstanding at a
     * time.  This also serves to throttle the load on the Elasticsearch
     * cluster.
     */
    private class CommitQueue {
        private final List<Commit> commitsList;
        private final int capacity;
        private int commitsSize;
        private int claimIndex; /* When a claim is outstanding, this int is the
                                   commitsList index of the last commit
                                   included in the batch.  When no claim is
                                   outstanding, its value is -1.  A non-zero
                                   value prevents the claiming of a batch,
                                   because only a single batch may be claimed
                                   at a time. */
        /*
         * Capacity is the maximum aggregate size of elements that can be
         * stored in this queue.  If that number is exceeded, then calls to
         * add() will wait until the number has fallen below the capacity.
         */
        CommitQueue(int capacity) {
            this.capacity = capacity;
            this.commitsList = new ArrayList<>();
            this.commitsSize = 0;
            this.claimIndex = -1;
        }

        /*
         * Add a comitted transaction to the queue.
         */
        synchronized int add(Transaction txn, VLSN vlsn) {
            waitIfAtCapacity();
            commitsList.add(new TransactionCommit(txn, vlsn));
            commitsSize += txn.size();
            return commitsSize;
        }

        /*
         * Add a transfer copy operation to the queue.  These operations are
         * singletons and are non-transactional.
         */
        synchronized int add(IndexOperation op) {
            waitIfAtCapacity();
            commitsList.add(new TransferCommit(op));
            commitsSize += op.size();
            return commitsSize;
        }

        private void waitIfAtCapacity() {
            int nwaits = 0;
            while (commitsSize >= capacity) {
                try {
                    if (nwaits++ % 1000 == 0) {
                        logger.warning
                            (lm("Waiting because CommitQueue is at capacity"));
                    }
                    wait();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING,
                               lm("Unexpected interruption of wait()"), e);
                }
            }
        }

        /*
         * When the consumer of the queue is ready to process some number of
         * enqueued operations, it calls this method to acquire a batch to
         * process.  maxSize determines the number of elements in the batch,
         * based on the total sizes of their component operations.
         * Only a single batch is allowed to be outstanding at a time.
         */
        synchronized List<Commit> claimBatch(int maxSize) {
            /*
             * If there's nothing to do, or if there's already a batch in
             * progress, skip this claim by returning null.
             */
            if (claimIndex >= 0 || commitsList.size() == 0) {
                logger.fine(lm("claimBatch rejected; claimIndex " +
                               claimIndex + " commits " + commitsList.size()));
                return null;
            }

            List<Commit> batch = new ArrayList<>();
            int batchSize = 0;

            for (Commit c : commitsList) {
                batch.add(c);
                claimIndex++;
                batchSize += c.size();
                if (batchSize >= maxSize) {
                    break;
                }
            }
            logger.fine(lm("Claimed batch: " + claimIndex +
                           " for " + batchSize + " bytes, first vlsn " +
                           batch.get(0).getCommitVLSN()));
            numberOfBatches++;
            sumOfBatchSizes += batchSize;
            sumOfBatchOperations += batch.size();
            return batch;
        }

        /*
         * If a batch has been claimed but cannot be processed, this method
         * will cancel the claim.  A later claim will produce the same batch,
         * given the same maxSize argument.
         */
        synchronized void cancelBatch() {
            claimIndex = -1;
        }

        /*
         * Confirming the batch means that the batch has been processed and we
         * can forget about it.
         */
        synchronized void confirmBatch() {
            if (claimIndex < 0) {
                return;
            }

            while (claimIndex >= 0) {
                Commit c = commitsList.remove(0);
                commitsSize -= c.size();
                claimIndex--;
            }
            notifyAll(); /* Wake up waiters in waitIfAtCapacity */
        }

        int getSize() {
            return commitsSize;
        }
    }
}
