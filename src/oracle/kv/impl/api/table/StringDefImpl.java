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
import static oracle.kv.impl.api.table.TableJsonUtils.MAX_INCL;
import static oracle.kv.impl.api.table.TableJsonUtils.MIN;
import static oracle.kv.impl.api.table.TableJsonUtils.MIN_INCL;

import oracle.kv.table.StringDef;

/**
 * StringDefImpl implements the StringDef interface.
 */
@Persistent(version=1)
public class StringDefImpl extends FieldDefImpl implements StringDef {

    private static final long serialVersionUID = 1L;

    private String min;
    private String max;
    private Boolean minInclusive;
    private Boolean maxInclusive;

    StringDefImpl(
        String description,
        String min,
        String max,
        Boolean minInclusive,
        Boolean maxInclusive) {

        super(Type.STRING, description);
        this.min = min;
        this.max = max;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        validate();
    }

    StringDefImpl(String description) {
        this(description, null, null, null, null);
    }

    StringDefImpl() {
        super(Type.STRING);
        min = null;
        max = null;
        minInclusive = null;
        maxInclusive = null;
    }

    private StringDefImpl(StringDefImpl impl) {
        super(impl);
        min = impl.min;
        max = impl.max;
        minInclusive = impl.minInclusive;
        maxInclusive = impl.maxInclusive;
    }

    /*
     * Public api methods from Object and FieldDef
     */

    @Override
    public StringDefImpl clone() {
        return new StringDefImpl(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode() +
            (min != null ? min.hashCode() : 0) +
            (max != null ? max.hashCode() : 0);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StringDefImpl) {
            StringDefImpl otherDef = (StringDefImpl) other;
            return (compare(getMin(), otherDef.getMin()) &&
                    compare(getMax(), otherDef.getMax()));
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
    public boolean isString() {
        return true;
    }

    @Override
    public boolean isAtomic() {
        return true;
    }

    @Override
    public StringDef asString() {
        return this;
    }

    @Override
    public StringValueImpl createString(String value) {

        return (hasMin() || hasMax() ?
                new StringRangeValue(value, this) :
                new StringValueImpl(value));
    }

    /*
     * Public api methods from StringDef
     */

    @Override
    public String getMin() {
        return min;
    }

    @Override
    public String getMax() {
        return max;
    }

    @Override
    public boolean isMinInclusive() {
        /* Default value of inclusive is true */
        return (minInclusive != null ? minInclusive : true);
    }

    @Override
    public boolean isMaxInclusive() {
        /* Default value of inclusive is true */
        return (maxInclusive != null ? maxInclusive : true);
    }

    /*
     * FieldDefImpl internal api methods
     */

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

        if (superType.isString() ||
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
            node.put(MIN_INCL, minInclusive);
        }
        if (max != null) {
            node.put(MAX, max);
            node.put(MAX_INCL, maxInclusive);
        }
    }

    @Override
    FieldValueImpl createValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return NullValueImpl.getInstance();
        }
        if (!node.isTextual()) {
            throw new IllegalArgumentException
                ("Default value for type STRING is not a string");
        }
        return createString(node.asText());
    }

    /*
     * local methods
     */

    private void validate() {
        /* Make sure min <= max */
        if (min != null && max != null) {
            if (min.compareTo(max) > 0 ) {
                throw new IllegalArgumentException
                    ("Invalid min or max value");
            }
        }
    }

    void validateValue(String val) {
        if (val == null) {
            throw new IllegalArgumentException
                ("String values cannot be null");
        }
        if ((min != null &&
             ((isMinInclusive() && min.compareTo(val) > 0) ||
              (!isMinInclusive() && min.compareTo(val) >= 0))) ||
            (max != null &&
             ((isMaxInclusive() && max.compareTo(val) < 0) ||
              (!isMaxInclusive() && max.compareTo(val) <= 0)))) {

            StringBuilder sb = new StringBuilder();
            sb.append("Value, ");
            sb.append(val);
            sb.append(", is outside of the allowed range");
            if (min != null && isMinInclusive()) {
                sb.append("[");
            } else {
                sb.append("(");
            }
            if (min != null) {
                sb.append(min);
            } else {
                sb.append("-INF");
            }
            if (max != null) {
                sb.append(max);
            } else {
                sb.append("+INF");
            }
            if (max != null && isMaxInclusive()) {
                sb.append("]");
            } else {
                sb.append(")");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }
}
