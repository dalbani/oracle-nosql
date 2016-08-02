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

package oracle.kv.impl.api.ops;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Transaction;

import oracle.kv.impl.api.ops.InternalOperation.OpCode;
import oracle.kv.impl.api.ops.MultiTableOperationHandler.AncestorList;
import oracle.kv.impl.topo.PartitionId;

/**
 * Server handler for {@link IndexKeysIterate}.
 */
public class IndexKeysIterateHandler
        extends IndexOperationHandler<IndexKeysIterate> {

    IndexKeysIterateHandler(OperationHandler handler) {
        super(handler, OpCode.INDEX_KEYS_ITERATE, IndexKeysIterate.class);
    }

    @Override
    Result execute(IndexKeysIterate op,
                   Transaction txn,
                   PartitionId partitionId  /* Not used */) {

        verifyTableAccess(op);

        final AncestorList ancestors =
            new AncestorList(operationHandler,
                             txn,
                             op.getResumePrimaryKey(),
                             op.getTargetTables().getAncestorTableIds());

        final String tableName = getTableName(op);
        IndexScanner scanner =
            new IndexScanner(txn,
                             getSecondaryDatabase(op, tableName),
                             getIndex(op, tableName),
                             op.getIndexRange(),
                             op.getResumeSecondaryKey(),
                             op.getResumePrimaryKey(),
                             true); // key-only, don't fetch data

        final List<ResultIndexKeys> results =
            new ArrayList<ResultIndexKeys>();

        boolean moreElements;

        /*
         * Cannot get the DatabaseEntry key objects from the scanner until it
         * has been initialized.
         */
        try {
            while ((moreElements = scanner.next()) == true) {
                final DatabaseEntry indexKeyEntry = scanner.getIndexKey();
                final DatabaseEntry primaryKeyEntry = scanner.getPrimaryKey();

                /*
                 * Data has not been fetched but the record is locked.
                 * Create the result from the index key and information
                 * available in the index record.
                 */
                List<ResultKey> ancestorKeys =
                    ancestors.addAncestorKeys(primaryKeyEntry);
                if (ancestorKeys != null) {
                    for (ResultKey key : ancestorKeys) {
                        results.add(new ResultIndexKeys
                                    (key.getKeyBytes(), indexKeyEntry.getData(),
                                     key.getExpirationTime()));
                    }
                }
                results.add(new ResultIndexKeys
                            (primaryKeyEntry.getData(),
                             indexKeyEntry.getData(),
                             scanner.getExpirationTime()));

                if (op.getBatchSize() > 0 &&
                    results.size() >= op.getBatchSize()) {
                    break;
                }
            }
            return new Result.IndexKeysIterateResult(getOpCode(), results,
                                                     moreElements);
        } finally {
            scanner.close();
        }
    }
}
