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

import oracle.kv.impl.util.SortableString;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.IntegerValue;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.IntNode;

import com.sleepycat.persist.model.Persistent;

@Persistent(version=1)
public class IntegerValueImpl extends FieldValueImpl implements IntegerValue {

    private static final long serialVersionUID = 1L;

    protected int value;

    IntegerValueImpl(int value) {
        this.value = value;
    }

    /**
     * This constructor creates IntegerValueImpl from the String format used for
     * sorted keys.
     */
    IntegerValueImpl(String keyValue) {
        this.value = SortableString.intFromSortable(keyValue);
    }

    /* DPL */
    @SuppressWarnings("unused")
    private IntegerValueImpl() {
        value = 0;
    }

    /*
     * Public api methods from Object and FieldValue
     */

    @Override
    public IntegerValueImpl clone() {
        return new IntegerValueImpl(value);
    }

    @Override
    public int hashCode() {
        return ((Integer) value).hashCode();
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof IntegerValueImpl) {
            return value == ((IntegerValueImpl)other).get();
        }
        return false;
    }

    /**
     * Allow comparisons against LongValue to succeed.
     */
    @Override
    public int compareTo(FieldValue other) {

        if (other instanceof IntegerValueImpl) {
            return compare(value, ((IntegerValueImpl)other).get());
        }
        throw new ClassCastException("Object is not comparable to LongValue");
    }

    public static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public FieldDef.Type getType() {
        return FieldDef.Type.INTEGER;
    }

    @Override
    public IntegerDefImpl getDefinition() {
        return FieldDefImpl.integerDef;
    }

    @Override
    public IntegerValue asInteger() {
        return this;
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    /*
     * Public api methods from IntegerValue
     */

    @Override
    public int get() {
        return value;
    }

    /*
     * FieldValueImpl internal api methods
     */

    @Override
    public int getInt() {
        return value;
    }

    @Override
    public long getLong() {
        return value;
    }

    @Override
    public void setInt(int v) {
        value = v;
    }

    @Override
    public int castAsInt() {
        return value;
    }

    @Override
    public long castAsLong() {
        return value;
    }

    @Override
    public float castAsFloat() {
        return value;
    }

    @Override
    public double castAsDouble() {
        return value;
    }

    @Override
    public String castAsString() {
        return Integer.toString(value);
    }

    @Override
    FieldValueImpl getNextValue() {
        if (value == Integer.MAX_VALUE) {
            return null;
        }
        return new IntegerValueImpl(value + 1);
    }

    @Override
    FieldValueImpl getMinimumValue() {
        return new IntegerValueImpl(Integer.MIN_VALUE);
    }

    @Override
    public String formatForKey(FieldDef field, int storageSize) {

        /*
         * Use a schema-defined storage length if available. If not, use
         * the one passed in, which comes from a primary key constraint on
         * this field in the table's primary key.
         */
        int len =
            (field != null ? ((IntegerDefImpl) field).getEncodingLength() : 0);

        /* if len is 0 or the max (5) and storageSize is specified, use it */
        if ((len == 0 || len == 5) && storageSize != 0) {
            len = storageSize;
        }
        return SortableString.toSortable(value, len);
    }

    @Override
    public JsonNode toJsonNode() {
        return new IntNode(value);
    }

    @Override
    public void toStringBuilder(StringBuilder sb) {
        sb.append(toString());
    }

    void validateStorageSize(int size) {
        int requiredSize = SortableString.encodingLength(value);
        if (requiredSize > size) {
            throw new IllegalArgumentException
                ("Integer value is too large for primary key storage size. " +
                 "It requires " + requiredSize + " bytes, and size must be " +
                 "less than or equal to " + size + " bytes");
        }
    }
}
