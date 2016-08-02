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

package oracle.kv.impl.api.table;

import java.util.Arrays;

import oracle.kv.table.BinaryValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import com.sleepycat.persist.model.Persistent;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.BinaryNode;

@Persistent(version=1)
public class BinaryValueImpl extends FieldValueImpl implements BinaryValue {

    private static final long serialVersionUID = 1L;

    final private byte[] value;

    BinaryValueImpl(byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException
                ("Binary values cannot be null");
        }
        this.value = value;
    }

    /* DPL */
    @SuppressWarnings("unused")
    private BinaryValueImpl() {
        value = null;
    }

    /*
     * Public api methods from Object and FieldValue
     */

    @Override
    public BinaryValueImpl clone() {
        return new BinaryValueImpl(value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BinaryValueImpl) {
            return Arrays.equals(value, ((BinaryValueImpl)other).get());
        }
        return false;
    }

    /**
     * Returns 0 if the two values are equal in terms of length and byte
     * content, otherwise it returns -1.
     */
    @Override
    public int compareTo(FieldValue otherValue) {
        return (equals(otherValue) ? 0 : -1);
    }

   @Override
    public String toString() {
        return Base64Variants.getDefaultVariant().encode(value, false);
    }

    @Override
    public FieldDef.Type getType() {
        return FieldDef.Type.BINARY;
    }

    @Override
    public BinaryDefImpl getDefinition() {
        return FieldDefImpl.binaryDef;
    }

    @Override
    public BinaryValue asBinary() {
        return this;
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    /*
     * Public api methods from BinaryValue
     */

    @Override
    public byte[] get() {
        return value;
    }

    @Override
    public JsonNode toJsonNode() {
        return new BinaryNode(value);
    }

    @Override
    public void toStringBuilder(StringBuilder sb) {
        sb.append(Base64Variants.getDefaultVariant().encode(value, true));
    }

    /*
     * Methods from FieldValueImpl
     */

    @Override
    public byte[] getBytes() {
        return value;
    }

    /*
     * local methods
     */

    /**
     * This is directly from JE's com.sleepycat.je.tree.Key class and is the
     * default byte comparator for JE's btree.
     *
     * Compare using a default unsigned byte comparison.
     */
    public static int compareUnsignedBytes(byte[] key1,
                                           int off1,
                                           int len1,
                                           byte[] key2,
                                           int off2,
                                           int len2) {
        int limit = Math.min(len1, len2);

        for (int i = 0; i < limit; i++) {
            byte b1 = key1[i + off1];
            byte b2 = key2[i + off2];
            if (b1 == b2) {
                continue;
            }
            /*
             * Remember, bytes are signed, so convert to shorts so that we
             * effectively do an unsigned byte comparison.
             */
            return (b1 & 0xff) - (b2 & 0xff);
        }

        return (len1 - len2);
    }

    public static BinaryValueImpl create(byte[] value) {
        return new BinaryValueImpl(value);
    }
}
