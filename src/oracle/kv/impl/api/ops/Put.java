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

import static oracle.kv.impl.util.SerialVersion.TABLE_API_VERSION;
import static oracle.kv.impl.util.SerialVersion.TTL_SERIAL_VERSION;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import oracle.kv.ReturnValueVersion;
import oracle.kv.Value;
import oracle.kv.impl.api.lob.KVLargeObjectImpl;
import oracle.kv.table.TimeToLive;

/**
 * A Put operation puts a value in the KV Store.
 * <br>
 * The operation is transmitted over the wire. Options related to operation
 * such as table identifier or TTL parameters are parts of wire format.
 */
public class Put extends SingleKeyOperation {

    /**
     * The value to write
     */
    private final RequestValue requestValue;

    /**
     * Whether to return previous value/version.
     */
    private final ReturnValueVersion.Choice prevValChoice;

    /**
     * Table operations include the table id.  0 means no table.
     */
    private final long tableId;

    private TimeToLive ttl;
    private boolean updateTTL;

    /**
     * Constructs a put operation.
     */
    public Put(byte[] keyBytes,
               Value value,
               ReturnValueVersion.Choice prevValChoice) {
        this(OpCode.PUT, keyBytes, value, prevValChoice, 0);
    }

    /**
     * Constructs a put operation with a table id.
     */
    public Put(byte[] keyBytes,
               Value value,
               ReturnValueVersion.Choice prevValChoice,
               long tableId) {
        this(OpCode.PUT, keyBytes, value, prevValChoice, tableId);
    }

    /**
     * Constructs a put operation with a table id and TTL-related arguments.
     */
    public Put(byte[] keyBytes,
            Value value,
            ReturnValueVersion.Choice prevValChoice,
            long tableId, TimeToLive ttl, boolean updateTTL) {
     this(OpCode.PUT, keyBytes, value, prevValChoice, tableId,
          ttl, updateTTL);
    }

    /**
     * For subclasses, allows passing OpCode.
     */
    Put(OpCode opCode,
        byte[] keyBytes,
        Value value,
        ReturnValueVersion.Choice prevValChoice,
        long tableId) {

        super(opCode, keyBytes);
        this.requestValue = new RequestValue(value);
        this.prevValChoice = prevValChoice;
        this.tableId = tableId;
    }

    Put(OpCode opCode,
        byte[] keyBytes,
        Value value,
        ReturnValueVersion.Choice prevValChoice,
        long tableId,
        TimeToLive ttl, boolean updateTTL) {

        this(opCode, keyBytes, value, prevValChoice, tableId);
        this.ttl = ttl;
        this.updateTTL = updateTTL;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor first
     * to read common elements.
     */
    Put(DataInput in, short serialVersion)
        throws IOException {
        this(OpCode.PUT, in, serialVersion);
    }

    /**
     * For subclasses, allows passing OpCode.
     */
    Put(OpCode opCode, DataInput in, short serialVersion)
        throws IOException {

        super(opCode, in, serialVersion);

        requestValue = new RequestValue(in, serialVersion);
        assert requestValue.getBytes() != null;
        prevValChoice = ReturnValueVersion.getChoice(in.readUnsignedByte());
        if (serialVersion >= TABLE_API_VERSION) {

            /*
             * Read table id.  If there is no table the value is 0.
             */
            tableId = in.readLong();
        } else {
            tableId = 0;
        }
        if (serialVersion >= TTL_SERIAL_VERSION) {
            int ttlVal = in.readInt();
            TimeUnit unit = ttlVal != 0 ? TimeUnit.values()[in.readByte()] :
                             TimeUnit.DAYS;
            ttl = TimeToLive.createTimeToLive(ttlVal, unit);
            updateTTL = in.readBoolean();
        }
    }

    public void setTTLOptions(TimeToLive ttl, boolean updateTTL) {
        this.ttl = ttl;
        this.updateTTL = updateTTL;
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to write
     * common elements.
     */
    @Override
    public void writeFastExternal(DataOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);
        requestValue.writeFastExternal(out, serialVersion);
        out.writeByte(prevValChoice.ordinal());
        if (serialVersion >= TABLE_API_VERSION) {

            /*
             * Write the table id.  If this is not a table operation the
             * id will be 0.
             */
            out.writeLong(tableId);
        } else if (tableId != 0) {
            throwVersionRequired(serialVersion, TABLE_API_VERSION);
        }
        if (serialVersion >= TTL_SERIAL_VERSION) {
            int ttlVal = 0;
            if (ttl != null) {
                ttlVal = (int) ttl.getValue();
            }
            out.writeInt(ttlVal);
            if (ttlVal != 0) {
                out.writeByte(ttl.getUnit().ordinal());
            }
            out.writeBoolean(updateTTL);
        } else if (ttl != null && ttl.getValue() != 0) {
            /*
             * Throw an exception so that TTL information is not
             * transparently dropped when writing to older servers.
             */
            throwVersionRequired(serialVersion, TTL_SERIAL_VERSION);
        }
   }

    /**
     * Gets the value to be put
     */
    public byte[] getValueBytes() {
        return requestValue.getBytes();
    }

    public ReturnValueVersion.Choice getReturnValueVersionChoice() {
        return prevValChoice;
    }

    /**
     * Returns the tableId, which is 0 if this is not a table operation.
     */
    @Override
    long getTableId() {
        return tableId;
    }

    /**
     * Returns expiry duration
     */
    TimeToLive getTTL() {
        return ttl;
    }

    /**
     * Returns whether to update expiry
     */
    boolean getUpdateTTL() {
        return updateTTL;
    }

    @Override
    public byte[] checkLOBSuffix(byte[] lobSuffixBytes) {
        return KVLargeObjectImpl.hasLOBSuffix(getKeyBytes(), lobSuffixBytes) ?
            getKeyBytes() :
            null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if (tableId != 0) {
            sb.append(" Table Id ");
            sb.append(tableId);
        }
        sb.append(" Value: ");
        sb.append(requestValue);

        return sb.toString();
    }
}
