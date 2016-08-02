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

package oracle.kv.impl.api.table;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import oracle.kv.Depth;
import oracle.kv.Direction;
import oracle.kv.Key;
import oracle.kv.KeyRange;
import oracle.kv.StoreIteratorConfig;
import oracle.kv.ValueVersion;
import oracle.kv.impl.api.KVStoreImpl;
import oracle.kv.impl.api.StoreIteratorParams;
import oracle.kv.impl.api.bulk.BulkMultiGet.BulkGetIterator;
import oracle.kv.impl.api.ops.InternalOperation;
import oracle.kv.impl.api.ops.MultiGetBatchTable;
import oracle.kv.impl.api.ops.MultiGetBatchTableKeys;
import oracle.kv.impl.api.ops.Result;
import oracle.kv.impl.api.ops.ResultKeyValueVersion;
import oracle.kv.table.FieldRange;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;

/**
 * Implementation of the bulk get table iterator and keys iterator, they extend
 * the BulkGetIterator.
 */
class TableMultiGetBatch {

    private final KVStoreImpl store;
    private final TableAPIImpl apiImpl;
    private final StoreIteratorParams params;
    private final StoreIteratorConfig config;
    private final List<Iterator<PrimaryKey>> primaryKeyIterators;
    private final MultiRowOptions getOptions;
    private final FieldRange fieldRange;
    private final AtomicReference<OperationInfo> opInfo;

    TableMultiGetBatch(TableAPIImpl apiImpl,
                       List<Iterator<PrimaryKey>> primaryKeyIterators,
                       MultiRowOptions getOptions,
                       TableIteratorOptions iterateOptions) {

        this.apiImpl = apiImpl;
        this.primaryKeyIterators = primaryKeyIterators;
        this.getOptions = getOptions;
        store = apiImpl.getStore();
        opInfo = new AtomicReference<OperationInfo>(null);

        /* Initialize StoreIteratorConfig object */
        final TableIteratorOptions tio = iterateOptions;
        config = new StoreIteratorConfig();
        if (tio != null) {
            config.setMaxConcurrentRequests(tio.getMaxConcurrentRequests());
        }

        /* Initialize StoreIteratorParams object */
        KeyRange useRange = null;
        if (getOptions != null) {
            fieldRange = getOptions.getFieldRange();
            if (fieldRange != null) {
                useRange = TableAPIImpl.createKeyRange(fieldRange);
            }
        } else {
            fieldRange = null;
        }
        params = new StoreIteratorParams(Direction.UNORDERED,
                                         TableAPIImpl.getBatchSize(tio),
                                         null /*parentKeyBytes*/,
                                         useRange,
                                         Depth.PARENT_AND_DESCENDANTS,
                                         TableAPIImpl.getConsistency(tio),
                                         TableAPIImpl.getTimeout(tio),
                                         TableAPIImpl.getTimeoutUnit(tio));
    }

    /**
     * Creates a table bulk get iterator returning primary keys.
     */
    TableIterator<PrimaryKey> createKeysIterator() {
        final BulkGetIterator<PrimaryKey, PrimaryKey> getIterator =
            new BulkGetIterator<PrimaryKey, PrimaryKey>(store,
                                                        primaryKeyIterators,
                                                        params,
                                                        config) {
                @Override
                public void validateKey(PrimaryKey key) {
                    validatePrimaryKey(key);
                }

                @Override
                public Key getKey(PrimaryKey key) {
                    return ((RowImpl)key).getPrimaryKey(true);
                }

                @Override
                protected InternalOperation generateBulkGetOp
                    (List<byte[]> parentKeys, byte[] resumeKey) {

                    return new MultiGetBatchTableKeys(parentKeys,
                                                      resumeKey,
                                                      getTargetTables(),
                                                      params.getSubRange(),
                                                      params.getBatchSize());
                }

                @Override
                protected void convertResult(Result result,
                                             List<PrimaryKey> elementList) {
                    final List<byte[]> results = result.getKeyList();
                    if (results.size() == 0) {
                        return;
                    }
                    final TableImpl table = getTopTable();
                    for (byte[] bytes: results) {
                        final Key key = keySerializer.fromByteArray(bytes);
                        final PrimaryKey pkey =
                            table.createPrimaryKeyFromKeyBytes(
                                key.toByteArray());
                        if (pkey == null) {
                            throw new IllegalArgumentException("Fail to " +
                            		"convert to primary key");
                        }
                        elementList.add(pkey);
                    }
                }

                @Override
                protected byte[] extractResumeKey
                    (Result result, List<PrimaryKey> elementList) {

                    int cnt = elementList.size();
                    if (cnt == 0) {
                        return null;
                    }
                    final PrimaryKey primaryKey = elementList.get(cnt - 1);
                    final TableImpl table = (TableImpl)primaryKey.getTable();
                    return table.createKey(primaryKey, true).toByteArray();
                }
            };
        return new TableScan.TableIteratorWrapper<PrimaryKey>(getIterator);
    }

    /**
     * Creates a table bulk get iterator returning rows.
     */
    TableIterator<Row> createIterator() {
        final BulkGetIterator<PrimaryKey, Row> getIterator =
            new BulkGetIterator<PrimaryKey, Row>(store,
                                                 primaryKeyIterators,
                                                 params,
                                                 config) {
                @Override
                public void validateKey(PrimaryKey key) {
                    validatePrimaryKey(key);
                }

                @Override
                public Key getKey(PrimaryKey key) {
                    return ((RowImpl)key).getPrimaryKey(true);
                }

                @Override
                protected InternalOperation
                    generateBulkGetOp(List<byte[]> parentKeys,
                                      byte[] resumeKey) {

                    return new MultiGetBatchTable(parentKeys,
                                                  resumeKey,
                                                  getTargetTables(),
                                                  params.getSubRange(),
                                                  params.getBatchSize());
                }

                @Override
                protected void convertResult(Result result,
                                             List<Row> elementList) {
                    final List<ResultKeyValueVersion> results =
                        result.getKeyValueVersionList();
                    if (results.size() == 0) {
                        return;
                    }
                    final TableImpl topTable = getTopTable();
                    for (ResultKeyValueVersion entry: results) {
                        final byte[] keyBytes = entry.getKeyBytes();
                        final Key key = keySerializer.fromByteArray(keyBytes);
                        final ValueVersion vv =
                            new ValueVersion(entry.getValue(),
                                             entry.getVersion());
                        RowImpl row =
                            topTable.createRowFromKeyBytes(key.toByteArray());
                        if (row == null) {
                            throw new IllegalArgumentException("Fail to " +
                                "convert to row");
                        }
                        row = apiImpl.getRowFromValueVersion(vv, row, false);
                        elementList.add(row);
                    }
                }

                @Override
                protected byte[] extractResumeKey(Result result,
                                                  List<Row> elementList) {
                    int cnt = elementList.size();
                    if (cnt == 0) {
                        return null;
                    }
                    final Row row = elementList.get(cnt - 1);
                    final TableImpl table = (TableImpl)row.getTable();
                    return table.createKey(row, true).toByteArray();
                }
            };
        return new TableScan.TableIteratorWrapper<Row>(getIterator);
    }

    /**
     * Extract the basic information from the first key including
     * table, targetTables, parentKey fields, topTable.
     *
     * Validate the given primary key:
     *  - If the primary key's table is the target table.
     *  - If the primary key contains all of fields defined for shard key.
     *  - If the field range is not null that the first unspecified field of the
     *    primary key is field in the range.
     */

    private void validatePrimaryKey(PrimaryKey key) {
        if (opInfo.get() == null) {
            synchronized(opInfo) {
                if (opInfo.get() == null) {
                    final OperationInfo ti = new OperationInfo(key, getOptions);
                    opInfo.set(ti);
                }
            }
        }

        /* Check if the primary key's table is target table */
        final TableImpl table = getTarget();
        assert(table != null);
        if (!table.equals(key.getTable())) {
            throw new IllegalArgumentException("The primary key" +
                " is not for table \"" + table.getFullName() +
                "\" but for \"" + key.getTable().getFullName() +
                "\": " + key.toJsonString(false));
        }

        /*
         * Check if the primary key contains all of the fields defined for
         * the shard key.
         */
        for (String keyField : table.getShardKey()) {
            if (key.get(keyField) == null) {
                throw new IllegalArgumentException
                    ("A required field \"" + keyField +
                     "\" is missing from the Primary Key: " +
                     key.toJsonString(false));
            }
        }

        /*
         * Check if the field range is not null that the first unspecified field
         * of the primary key is field in the range.
         */
        if (fieldRange != null) {
            final TableKey tkey = TableKey.createKey(table, key, true);
            tkey.validateFieldOrder(fieldRange);
        }
    }

    private TableImpl getTarget() {
        return opInfo.get().getTarget();
    }

    private TargetTables getTargetTables() {
        return opInfo.get().getTargetTables();
    }

    private TableImpl getTopTable() {
        return opInfo.get().getTopTable();
    }

    /**
     * A class represents the basic related information that extracted from
     * the first primary key.
     */
    private static final class OperationInfo {
        private final TableImpl target;
        private final TableImpl topTable;
        private final TargetTables targetTables;

        OperationInfo(PrimaryKey key, MultiRowOptions opt) {
            target = (TableImpl)key.getTable().clone();
            targetTables = TableAPIImpl.makeTargetTables(target, opt);
            topTable = targetTables.hasAncestorTables() ?
                       target.getTopLevelTable() : target;
            if (opt != null) {
                TableAPIImpl.validateMultiRowOptions(opt, target, false);
            }
        }

        TableImpl getTarget() {
            return target;
        }

        TableImpl getTopTable() {
            return topTable;
        }

        TargetTables getTargetTables() {
            return targetTables;
        }
    }
}
