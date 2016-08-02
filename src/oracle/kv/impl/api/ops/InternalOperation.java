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

package oracle.kv.impl.api.ops;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.kv.Operation;
import oracle.kv.UnauthorizedException;
import oracle.kv.impl.api.ops.InternalOperation.Keyspace.KeyAccessChecker;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.measurement.PerfStatType;
import oracle.kv.impl.security.AccessCheckUtils;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.TablePrivilege;
import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.util.FastExternalizable;
import oracle.kv.impl.util.SerialVersion;

import com.sleepycat.je.Transaction;

/**
 * Represents an operation that may be performed on the store.
 */
public abstract class InternalOperation implements FastExternalizable {

    /**
     * An empty, immutable privilege list, used when an operation is requested
     * that does not require authentication.
     */
    static final List<KVStorePrivilege> emptyPrivilegeList =
        Collections.emptyList();

    /**
     * An enumeration listing all available OpCodes of Operations for the
     * data store.
     *
     * WARNING: To avoid breaking serialization compatibility, the order of the
     * values must not be changed and new values must be added at the end.
     */
    public enum OpCode {

        NOP() {

            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new NOP(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {
                return new Result.NOPResult(in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return true;
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.NOP_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.NOP_CUM;
            }
        },

        GET() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new Get(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.GetResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.GetResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.GET_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.GET_CUM;
            }
        },

        MULTI_GET() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiGet(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.IterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.IterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_GET_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_GET_CUM;
            }
        },

        MULTI_GET_KEYS() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiGetKeys(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.KeysIterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.KeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_GET_KEYS_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_GET_KEYS_CUM;
            }
        },

        MULTI_GET_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiGetIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.IterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.IterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_GET_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_GET_ITERATOR_CUM;
            }
        },

        MULTI_GET_KEYS_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiGetKeysIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.KeysIterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.KeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_GET_KEYS_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_GET_KEYS_ITERATOR_CUM;
            }
        },

        STORE_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new StoreIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.IterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.IterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.STORE_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.STORE_ITERATOR_CUM;
            }
        },

        STORE_KEYS_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new StoreKeysIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.KeysIterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.KeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.STORE_KEYS_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.STORE_KEYS_ITERATOR_CUM;
            }
        },

        PUT() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new Put(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.PutResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.PutResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                return Operation.Type.PUT;
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.PUT_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.PUT_CUM;
            }
        },

        PUT_IF_ABSENT() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new PutIfAbsent(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.PutResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.PutResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                return Operation.Type.PUT_IF_ABSENT;
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.PUT_IF_ABSENT_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.PUT_IF_ABSENT_CUM;
            }
        },

        PUT_IF_PRESENT() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new PutIfPresent(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.PutResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.PutResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                return Operation.Type.PUT_IF_PRESENT;
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.PUT_IF_PRESENT_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.PUT_IF_PRESENT_CUM;
            }
        },

        PUT_IF_VERSION() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new PutIfVersion(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.PutResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.PutResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                return Operation.Type.PUT_IF_VERSION;
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.PUT_IF_VERSION_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.PUT_IF_VERSION_CUM;
            }
        },

        DELETE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new Delete(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.DeleteResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.DeleteResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                return Operation.Type.DELETE;
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.DELETE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.DELETE_CUM;
            }
        },

        DELETE_IF_VERSION() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new DeleteIfVersion(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.DeleteResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.DeleteResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                return Operation.Type.DELETE_IF_VERSION;
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.DELETE_IF_VERSION_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.DELETE_IF_VERSION_CUM;
            }
        },

        MULTI_DELETE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiDelete(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.MultiDeleteResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.MultiDeleteResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_DELETE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_DELETE_CUM;
            }
        },

        EXECUTE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new Execute(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.ExecuteResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.ExecuteResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.EXECUTE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.EXECUTE_CUM;
            }
        },

        MULTI_GET_TABLE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiGetTable(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.IterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.IterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_GET_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_GET_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V4;
            }
        },

        MULTI_GET_TABLE_KEYS() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiGetTableKeys(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.KeysIterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.KeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_GET_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_GET_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V4;
            }
        },

        TABLE_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new TableIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.IterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.IterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.STORE_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.STORE_ITERATOR_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V4;
            }
        },

        TABLE_KEYS_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new TableKeysIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.KeysIterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.KeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.STORE_KEYS_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.STORE_KEYS_ITERATOR_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V4;
            }
        },

        INDEX_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new IndexIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.IndexRowsIterateResult
                    (this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.IndexRowsIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.INDEX_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.INDEX_ITERATOR_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V4;
            }
        },

        INDEX_KEYS_ITERATE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new IndexKeysIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.IndexKeysIterateResult
                    (this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.IndexKeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.INDEX_KEYS_ITERATOR_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.INDEX_KEYS_ITERATOR_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V4;
            }
        },

        MULTI_DELETE_TABLE() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new MultiDeleteTable(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.MultiDeleteResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.MultiDeleteResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not an execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.MULTI_DELETE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.MULTI_DELETE_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V4;
            }
        },

        MULTI_GET_BATCH() {
            @Override
            InternalOperation readOperation(ObjectInput in, short serialVersion)
                throws IOException {

                return new MultiGetBatchIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.BulkGetIterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.BulkGetIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not a execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.EXECUTE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.EXECUTE_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V8;
            }
        },

        MULTI_GET_BATCH_KEYS() {
            @Override
            InternalOperation readOperation(ObjectInput in, short serialVersion)
                throws IOException {

                return new MultiGetBatchKeysIterate(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.BulkGetKeysIterateResult(this, in,
                                                           serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.BulkGetKeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not a execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.EXECUTE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.EXECUTE_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V8;
            }
        },

        MULTI_GET_BATCH_TABLE() {
            @Override
            InternalOperation readOperation(ObjectInput in, short serialVersion)
                throws IOException {

                return new MultiGetBatchTable(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.BulkGetIterateResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.BulkGetIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not a execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.EXECUTE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.EXECUTE_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V8;
            }
        },

        MULTI_GET_BATCH_TABLE_KEYS() {
            @Override
            InternalOperation readOperation(ObjectInput in, short serialVersion)
                throws IOException {

                return new MultiGetBatchTableKeys(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.BulkGetKeysIterateResult(this, in,
                                                           serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.BulkGetKeysIterateResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not a execute op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                return PerfStatType.EXECUTE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                return PerfStatType.EXECUTE_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V8;
            }
        },

        PUT_BATCH() {
            @Override
            InternalOperation readOperation(ObjectInput in,
                                            short serialVersion)
                throws IOException {

                return new PutBatch(in, serialVersion);
            }

            @Override
            public Result readResult(ObjectInput in, short serialVersion)
                throws IOException {

                return new Result.PutBatchResult(this, in, serialVersion);
            }

            @Override
            public boolean checkResultType(Result result) {
                return (result instanceof Result.PutBatchResult);
            }

            @Override
            public Operation.Type getExecuteType() {
                throw new RuntimeException("Not a putBatch op: " + this);
            }

            @Override
            public PerfStatType getIntervalMetric() {
                // TODO: do we want metrics?
                return PerfStatType.EXECUTE_INT;
            }

            @Override
            public PerfStatType getCumulativeMetric() {
                // TODO: do we want metrics?
                return PerfStatType.EXECUTE_CUM;
            }

            @Override
            public short requiredVersion() {
                return SerialVersion.V9;
            }
        };

        abstract InternalOperation readOperation(ObjectInput in,
                                                 short serialVersion)
            throws IOException;

        public abstract Result readResult(ObjectInput in, short serialVersion)
            throws IOException;

        public abstract boolean checkResultType(Result result);

        public abstract Operation.Type getExecuteType();

        public abstract PerfStatType getIntervalMetric();
        public abstract PerfStatType getCumulativeMetric();

        /**
         * This must be overridden by all post-R1 OpCodes
         */
        public short requiredVersion() {
            return SerialVersion.V1;
        }
    }

    private final static OpCode[] OPCODES_BY_ORDINAL;
    static {
        final EnumSet<OpCode> set = EnumSet.allOf(OpCode.class);
        OPCODES_BY_ORDINAL = new OpCode[set.size()];
        for (OpCode op : set) {
            OPCODES_BY_ORDINAL[op.ordinal()] = op;
        }
    }

    public static OpCode getOpCode(int ordinal) {
        if (ordinal < 0 || ordinal >= OPCODES_BY_ORDINAL.length) {
            throw new RuntimeException("unknown opcode: " + ordinal);
        }
        return OPCODES_BY_ORDINAL[ordinal];
    }

    /**
     * All Operations must have an opcode associated with them.
     */
    private final OpCode opCode;

    /**
     * Assigns the opcode to the operation
     *
     * @param opCode
     */
    public InternalOperation(OpCode opCode) {
        this.opCode = opCode;
    }

    /**
     * FastExternalizable constructor.  Subclasses must call this constructor
     * before reading additional elements.
     *
     * The OpCode was read by readFastExternal.
     */
    InternalOperation(OpCode opCode,
                      @SuppressWarnings("unused") ObjectInput in,
                      @SuppressWarnings("unused") short serialVersion) {

        this.opCode = opCode;
    }

    /**
     * FastExternalizable factory for all InternalOperation subclasses.
     */
    public static InternalOperation readFastExternal(ObjectInput in,
                                             short serialVersion)
        throws IOException {

        final OpCode op = getOpCode(in.readUnsignedByte());
        return op.readOperation(in, serialVersion);
    }

    /**
     * FastExternalizable writer.  Subclasses must call this method before
     * writing additional elements.
     */
    @Override
    public void writeFastExternal(ObjectOutput out,
                                  short serialVersion)
        throws IOException {

        out.writeByte(opCode.ordinal());
    }

    /**
     * Execute the operation on the given repNode.
     *
     * @param txn the transaction to use for the operation
     * @param operationHandler the operation handler that implements the
     * operation
     * @return the result of execution
     * @throw UnauthorizedException if an attempt is made to access restricted
     * resources
     */
    public abstract Result execute(Transaction txn,
                                   PartitionId partitionId,
                                   OperationHandler operationHandler)
        throws UnauthorizedException;

    /**
     * Get this operation's opCode.
     *
     * @return the OpCode
     */
    public OpCode getOpCode() {
        return opCode;
    }

    /**
     * Overridden by non-LOB write operations to ensure that the key does
     * not have the LOB suffix currently in effect.
     *
     * @param lobSuffixBytes the byte representation of the LOB suffix in
     * effect
     *
     * @return null if the check passes, or the key bytes if it fails
     */
    public byte[] checkLOBSuffix(byte[] lobSuffixBytes) {
        return null;
    }

    /**
     * Returns a string describing this operation.
     *
     * @return the opcode of this operation
     */
    @Override
    public String toString() {
        return opCode.name();
    }

    /**
     * Checks whether the input key has the server internal key prefix as
     * a prefix.  That is, does the key reference something that is definitely
     * within the server internal keyspace?
     */
    protected boolean isInternalRequestor() {
        final ExecutionContext currentContext = ExecutionContext.getCurrent();
        if (currentContext == null) {
            return true;
        }
        return currentContext.hasPrivilege(SystemPrivilege.INTLOPER);
    }

    /**
     * Common code to throw UnsupportedOperationException when a newer client
     * attempts to perform an operation against a server that does not support
     * it.  There is other common code in Request.writeExternal that does the
     * same thing on a per-operation basis.  This code is called when the
     * operation has conditional parameters that were added in a later version.
     * For example, Get, Put, Delete and their variants added a table id in V4.
     */
    protected void throwTablesRequired(short serialVersion) {
        throw new UnsupportedOperationException
            ("Attempting an operation that is not supported by " +
             "the server version.  Server version is " + serialVersion +
             ", required version is " + SerialVersion.V4 +
             ", operation is " + opCode);
    }

    /**
     * Returns an immutable list of privilege required to execute the
     * operation.  The required privileges depend on the operation and the
     * keyspace it accessing.
     */
    abstract public List<? extends KVStorePrivilege> getRequiredPrivileges();

    /**
     * An common interface for those operations need to access table keyspace,
     * which defining the privileges needed.
     */
    interface PrivilegedTableAccessor {
        /**
         * Returns the needed privileges accessing the table specified by the
         * id.
         *
         * @param tableId table id
         * @return a list of required privileges
         */
        List<? extends KVStorePrivilege> tableAccessPrivileges(long tableId);
    }

    /**
     * A class to help identify the keyspace which operation is accessing.
     */
    static class Keyspace {

        /**
         * The encoding of the prefix of the server-private keyspace(///),
         * which is a subset of the "internal" keyspace (//) used by the client
         * to store the Avro schema.
         */
        private static final byte[] PRIVATE_KEY_PREFIX =
            //new byte[] { Key.BINARY_COMP_DELIM, Key.BINARY_COMP_DELIM };
            new byte[] { 0, 0 };

        /**
         * The encoding of the prefix of the Avro schema keyspace(//sch/),
         * which is used by the client to store the Avro schema.
         */
        private static final byte[] AVRO_SCHEMA_KEY_PREFIX =
            new byte[] { 0, 0x73, 0x63, 0x68 }; /* Keybytes of "//sch" */

        static interface KeyAccessChecker {
            boolean allowAccess(byte[] key);
        }

        static final KeyAccessChecker privateKeyAccessChecker =
            new KeyAccessChecker() {
            @Override
            public boolean allowAccess(byte[] key) {
                return !Keyspace.isPrivateAccess(key);
            }
        };

        static final KeyAccessChecker schemaKeyAccessChecker =
            new KeyAccessChecker() {
            @Override
            public boolean allowAccess(byte[] key) {
                return !Keyspace.isSchemaAccess(key);
            }
        };

        enum KeyspaceType { PRIVATE, SCHEMA, GENERAL }

        static KeyspaceType identifyKeyspace(byte[] key) {
            if (key == null || key.length == 0) {
                throw new IllegalArgumentException(
                    "Key bytes may not be null or empty");
            }

            /* Quick return for non-internal keyspace */
            if (key[0] == 0) {
                if (isSchemaAccess(key)) {
                    return KeyspaceType.SCHEMA;
                }
                if (isPrivateAccess(key)) {
                    return KeyspaceType.PRIVATE;
                }
            }
            /* Other cases are regarded as general keyspace */
            return KeyspaceType.GENERAL;
        }

        /**
         * Checks whether the input key has the Avro schema keyspace as a
         * prefix. That is, does the key matches exactly the "//sch" or has the
         * major component of "//sch"?
         */
        static boolean isSchemaAccess(byte[] key) {
            if (key.length < AVRO_SCHEMA_KEY_PREFIX.length) {
                return false;
            }
            for (int i = 0; i < AVRO_SCHEMA_KEY_PREFIX.length; i++) {
                if (key[i] != AVRO_SCHEMA_KEY_PREFIX[i]) {
                    return false;
                }
            }
            /* Key is exactly the "//sch" */
            if (key.length == AVRO_SCHEMA_KEY_PREFIX.length) {
                return true;
            }
            /* Key has "//sch" as its partial or complete major component */
            final byte endingByte = key[AVRO_SCHEMA_KEY_PREFIX.length];
            return endingByte == (byte) 0xff /* Path delimiter */ ||
                   endingByte == (byte) 0x00; /* Component delimiter */
        }

        /**
         * Checks whether the input key is a prefix of the schema key space.
         * That is, does the key reference something that is may be within the
         * schema key space?
         */
        static boolean mayBeSchemaAccess(byte[] key) {
            if (key.length < AVRO_SCHEMA_KEY_PREFIX.length) {
                for (int i = 0; i < key.length; i++) {
                    if (key[i] != AVRO_SCHEMA_KEY_PREFIX[i]) {
                        return false;
                    }
                }
                return true;
            }
            for (int i = 0; i < AVRO_SCHEMA_KEY_PREFIX.length; i++) {
                if (key[i] != AVRO_SCHEMA_KEY_PREFIX[i]) {
                    return false;
                }
            }
            /* Key is exactly the "//sch" */
            if (key.length == AVRO_SCHEMA_KEY_PREFIX.length) {
                return true;
            }
            /* Key has "//sch" as its partial or complete major component */
            final byte endingByte = key[AVRO_SCHEMA_KEY_PREFIX.length];
            return endingByte == (byte) 0xff /* Path delimiter */ ||
                   endingByte == (byte) 0x00; /* Component delimiter */
        }

        /**
         * Checks whether the input key has the server private keyspace as a
         * prefix.  That is, does the key reference something that is
         * definitely within the server private keyspace?
         */
        static boolean isPrivateAccess(byte[] key) {
            if (key.length < PRIVATE_KEY_PREFIX.length) {
                return false;
            }
            for (int i = 0; i < PRIVATE_KEY_PREFIX.length; i++) {
                if (key[i] != PRIVATE_KEY_PREFIX[i]) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks whether the input key is a prefix of the server private key
         * space.  That is, does the key reference something that is may be
         * within the server private keyspace?
         */
        static boolean mayBePrivateAccess(byte[] key) {
            if (key.length < PRIVATE_KEY_PREFIX.length) {
                for (byte element : key) {
                    if (element != PRIVATE_KEY_PREFIX.length) {
                        return false;
                    }
                }
                return true;
            }
            for (int i = 0; i < PRIVATE_KEY_PREFIX.length; i++) {
                if (key[i] != PRIVATE_KEY_PREFIX.length) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks whether the input key is outside both the server private
         * keyspace and the schema keyspace.
         */
        static boolean isGeneralAccess(byte[] key) {
            return !isPrivateAccess(key) && !isSchemaAccess(key);
        }
    }

    /**
     * A per-key access checker for possible table keyspaces.  Access to a key
     * is allowed if and only if the key falls in a table's keyspace and the
     * current user has access privileges on that table.
     */
    static class TableAccessChecker implements KeyAccessChecker {

        private final PrivilegedTableAccessor tableAccessor;
        final OperationHandler operationHandler;

        /**
         * A set caching the tables which have been verified to be accessible.
         */
        private final Set<Long> accessibleTables = new HashSet<Long>();

        TableAccessChecker(OperationHandler operationHandler,
                           PrivilegedTableAccessor tableAccessor) {
            this.operationHandler = operationHandler;
            this.tableAccessor = tableAccessor;
        }

        @Override
        public boolean allowAccess(byte[] key) {
            if (!Keyspace.isGeneralAccess(key)) {
                return true;
            }

            final TableImpl possibleTable =
                TableOperationHandler.findTableByKeyBytes(operationHandler,
                                                          key);
            /* Not accessing table, returns false */
            if (possibleTable == null) {
                return false;
            }

            return internalCheckTableAccess(possibleTable);
        }

        boolean internalCheckTableAccess(TableImpl table) {
            final ExecutionContext exeCtx = ExecutionContext.getCurrent();
            if (exeCtx == null) {
                return true;
            }

            if (accessibleTables.contains(table.getId())) {
                return true;
            }

            if (!AccessCheckUtils.currentUserOwnsResource(table)) {
                if (!exeCtx.hasAllPrivileges(
                        tableAccessor.tableAccessPrivileges(table.getId()))) {
                    return false;
                }

                /* Ensure at least read privileges on all parent tables. */
                TableImpl parent = (TableImpl) table.getParent();
                while (parent != null) {
                    final long pTableId = parent.getId();
                    /*
                     * One of the parent table is accessible, exits the loop
                     * and returns true according to induction
                     */
                    if (accessibleTables.contains(pTableId)) {
                        break;
                    }

                    final TablePrivilege parentReadPriv =
                        new TablePrivilege.ReadTable(pTableId);
                    if (!exeCtx.hasPrivilege(parentReadPriv) &&
                        !exeCtx.hasAllPrivileges(
                            tableAccessor.tableAccessPrivileges(pTableId))) {
                        return false;
                    }
                    parent = (TableImpl) parent.getParent();
                }
            }

            /* Caches the verified table */
            accessibleTables.add(table.getId());
            return true;
        }
    }
}
