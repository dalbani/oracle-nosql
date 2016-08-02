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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import oracle.kv.Key;
import oracle.kv.Operation;
import oracle.kv.OperationFactory;
import oracle.kv.ReturnValueVersion;
import oracle.kv.Value;
import oracle.kv.Version;
import oracle.kv.impl.api.KeySerializer;
import oracle.kv.impl.util.FastExternalizable;

/**
 * An Execute operation performs a sequence of put and delete operations.
 */
public class Execute extends InternalOperation {

    /**
     * The operations to execute.
     */
    private final List<OperationImpl> ops;

    /**
     * Constructs an execute operation.
     */
    public Execute(List<OperationImpl> ops) {
        super(OpCode.EXECUTE);
        this.ops = ops;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    Execute(DataInput in, short serialVersion)
        throws IOException {

        super(OpCode.EXECUTE, in, serialVersion);
        final int opsSize = in.readInt();
        ops = new ArrayList<OperationImpl>(opsSize);
        for (int i = 0; i < opsSize; i += 1) {
            ops.add(new OperationImpl(in, serialVersion));
        }
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);
        out.writeInt(ops.size());
        for (OperationImpl op : ops) {
            op.writeFastExternal(out, serialVersion);
        }
    }

    public List<OperationImpl> getOperations() {
        return ops;
    }

    @Override
    public String toString() {
        return super.toString() + " Ops: " + ops;
    }

    /**
     * Implementation of Operation, the unit of work for the execute() method,
     * and wrapper for the corresponding SingleKeyOperation.
     */
    public static class OperationImpl
        implements Operation, FastExternalizable {

        private final Key key; /* Not serialized. */
        private final boolean abortIfUnsuccessful;
        private final SingleKeyOperation internalOp;

        OperationImpl(Key key,
                      boolean abortIfUnsuccessful,
                      SingleKeyOperation internalOp) {
            this.key = key;
            this.abortIfUnsuccessful = abortIfUnsuccessful;
            this.internalOp = internalOp;
        }

        /**
         * FastExternalizable constructor.  Must call superclass constructor
         * first to read common elements.
         */
        OperationImpl(DataInput in, short serialVersion)
            throws IOException {

            key = null;
            abortIfUnsuccessful = in.readBoolean();
            internalOp = (SingleKeyOperation)
                InternalOperation.readFastExternal(in, serialVersion);
        }

        /**
         * FastExternalizable writer.  Must call superclass method first to
         * write common elements.
         */
        @Override
        public void writeFastExternal(DataOutput out, short serialVersion)
            throws IOException {

            out.writeBoolean(abortIfUnsuccessful);
            internalOp.writeFastExternal(out, serialVersion);
        }

        public SingleKeyOperation getInternalOp() {
            return internalOp;
        }

        /**
         * Because the Key is not serialized, this method will always throw an
         * IllegalStateException on the service-side of the RMI interface.
         * Internally, SingleKeyOperation.getKeyBytes should be called instead
         * of getKey, which is only intended for use by the client.
         */
        @Override
        public Key getKey() {
            if (key == null) {
                throw new IllegalStateException();
            }
            return key;
        }

        @Override
        public Operation.Type getType() {
            return internalOp.getOpCode().getExecuteType();
        }

        @Override
        public boolean getAbortIfUnsuccessful() {
            return abortIfUnsuccessful;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static List<OperationImpl> downcast(List<Operation> ops) {
            /* Downcast: all Operations are OperationImpls. */
            return (List) ops;
        }
    }

    public static class OperationFactoryImpl implements OperationFactory {

        private final KeySerializer keySerializer;

        public OperationFactoryImpl(KeySerializer keySerializer) {
            this.keySerializer = keySerializer;
        }

        @Override
        public Operation createPut(Key key, Value value) {
            return createPut(key, value, null, false);
        }

        @Override
        public Operation createPut(Key key,
                                   Value value,
                                   ReturnValueVersion.Choice prevReturn,
                                   boolean abortIfUnsuccessful) {

            return new OperationImpl
                (key, abortIfUnsuccessful,
                 new Put(keySerializer.toByteArray(key), value,
                         (prevReturn != null) ?
                         prevReturn :
                         ReturnValueVersion.Choice.NONE));
        }

        @Override
        public Operation createPutIfAbsent(Key key, Value value) {
            return createPutIfAbsent(key, value, null, false);
        }

        @Override
        public Operation
            createPutIfAbsent(Key key,
                              Value value,
                              ReturnValueVersion.Choice prevReturn,
                              boolean abortIfUnsuccessful) {
            return new OperationImpl
                (key, abortIfUnsuccessful,
                 new PutIfAbsent(keySerializer.toByteArray(key), value,
                                 (prevReturn != null) ?
                                 prevReturn :
                                 ReturnValueVersion.Choice.NONE));
        }

        @Override
        public Operation createPutIfPresent(Key key, Value value) {
            return createPutIfPresent(key, value, null, false);
        }

        @Override
        public Operation
            createPutIfPresent(Key key,
                               Value value,
                               ReturnValueVersion.Choice prevReturn,
                               boolean abortIfUnsuccessful) {
            return new OperationImpl
                (key, abortIfUnsuccessful,
                 new PutIfPresent(keySerializer.toByteArray(key), value,
                                  (prevReturn != null) ?
                                  prevReturn :
                                  ReturnValueVersion.Choice.NONE));
        }

        @Override
        public Operation createPutIfVersion(Key key,
                                            Value value,
                                            Version version) {
            return createPutIfVersion(key, value, version, null, false);
        }

        @Override
        public Operation
            createPutIfVersion(Key key,
                               Value value,
                               Version version,
                               ReturnValueVersion.Choice prevReturn,
                               boolean abortIfUnsuccessful) {
            return new OperationImpl
                (key, abortIfUnsuccessful,
                 new PutIfVersion(keySerializer.toByteArray(key), value,
                                  (prevReturn != null) ?
                                  prevReturn :
                                  ReturnValueVersion.Choice.NONE,
                                  version));
        }

        @Override
        public Operation createDelete(Key key) {
            return createDelete(key, null, false);
        }

        @Override
        public Operation createDelete(Key key,
                                      ReturnValueVersion.Choice prevReturn,
                                      boolean abortIfUnsuccessful) {
            return new OperationImpl
                (key, abortIfUnsuccessful,
                 new Delete(keySerializer.toByteArray(key),
                            (prevReturn != null) ?
                            prevReturn :
                            ReturnValueVersion.Choice.NONE));
        }

        @Override
        public Operation createDeleteIfVersion(Key key, Version version) {
            return createDeleteIfVersion(key, version, null, false);
        }

        @Override
        public Operation
            createDeleteIfVersion(Key key,
                                  Version version,
                                  ReturnValueVersion.Choice prevReturn,
                                  boolean abortIfUnsuccessful) {
            return new OperationImpl
                (key, abortIfUnsuccessful,
                 new DeleteIfVersion(keySerializer.toByteArray(key),
                                     (prevReturn != null) ?
                                     prevReturn :
                                     ReturnValueVersion.Choice.NONE,
                                     version));
        }
    }
}
