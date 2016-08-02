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

import com.sleepycat.persist.model.Persistent;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import static oracle.kv.impl.api.table.TableJsonUtils.MAX;
import static oracle.kv.impl.api.table.TableJsonUtils.MIN;
import oracle.kv.impl.util.SortableString;

import oracle.kv.table.LongDef;

/**
 * LongDefImpl implements the LongDef interface.
 */
@Persistent(version=1)
public class LongDefImpl extends FieldDefImpl implements LongDef {

    private static final long serialVersionUID = 1L;

    /*
     * min and max are inclusive
     */
    private Long min;
    private Long max;
    private int encodingLength;

    LongDefImpl(String description, Long min, Long max) {
        super(Type.LONG, description);
        this.min = min;
        this.max = max;
        validate();
    }

    LongDefImpl(String description) {
        this(description, null, null);
    }

    LongDefImpl() {
        super(Type.LONG);
        min = null;
        max = null;
        encodingLength = 0;
    }

    private LongDefImpl(LongDefImpl impl) {
        super(impl);
        min = impl.min;
        max = impl.max;
        encodingLength = impl.encodingLength;
    }

    /*
     * Public api methods from Object and FieldDef
     */

    @Override
    public LongDefImpl clone() {
        return new LongDefImpl(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode() +
            (min != null ? min.hashCode() : 0) +
            (max != null ? max.hashCode() : 0);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LongDefImpl) {
            LongDefImpl otherDef = (LongDefImpl) other;
            return (compare(min, otherDef.min) &&
                    compare(max, otherDef.max));
        }
        return false;
    }

    @Override
    public boolean isValidKeyField() {
        return true;
    }

    @Override
    public boolean isValidIndexField() {
        return true;
    }

    @Override
    public boolean isLong() {
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

    @Override
    public LongDef asLong() {
        return this;
    }

    /*
     * Public api methods from LongDef
     */

    @Override
    public Long getMin() {
        return min;
    }

    @Override
    public Long getMax() {
        return max;
    }

    /*
     * FieldDefImpl internal api methods
     */

    @Override
    int getEncodingLength() {
        return encodingLength;
    }

    @Override
    public boolean hasMin() {
        return min != null;
    }

    @Override
    public boolean hasMax() {
        return max != null;
    }

    @Override
    public boolean isSubtype(FieldDefImpl superType) {

        if (superType.isLong() ||
            superType.isAny() ||
            superType.isAnyAtomic()) {
            return true;
        }

        return false;
    }

    @Override
    void toJson(ObjectNode node) {
        super.toJson(node);
        if (min != null) {
            node.put(MIN, min);
        }
        if (max != null) {
            node.put(MAX, max);
        }
    }

    @Override
    public LongValueImpl createLong(long value) {

        return (hasMin() || hasMax() ?
                new LongRangeValue(value, this) :
                new LongValueImpl(value));
    }

    @Override
    LongValueImpl createLong(String value) {

        return (hasMin() || hasMax() ?
                new LongRangeValue(value, this) :
                new LongValueImpl(value));
    }

    @Override
    FieldValueImpl createValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return NullValueImpl.getInstance();
        }
        if (!node.isLong()) {
            throw new IllegalArgumentException
                ("Default value for type LONG is not long");
        }
        return createLong(node.getLongValue());
    }

    /*
     * Local methods
     */

    private void validate() {

        if (min != null && max != null) {
            if (min > max) {
                throw new IllegalArgumentException
                    ("Invalid minimum or maximum value");
            }
        }
        encodingLength = SortableString.encodingLength(min, max);
    }

    /**
     * Validates the value against the range if one exists.
     * min/max are inclusive.
     */
    void validateValue(long val) {

        if ((min != null && val < min) || (max != null && val > max)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Value, ");
            sb.append(val);
            sb.append(", is outside of the allowed range");
            throw new IllegalArgumentException(sb.toString());
        }
    }
}
