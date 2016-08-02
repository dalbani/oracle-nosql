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

import java.util.Iterator;
import java.util.Map;

import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.RecordDef;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TimeToLive;

/**
 * RowImpl is a specialization of RecordValue to represent a single record,
 * or row, in a table.  It is a central object used in most table operations
 * defined in {@link TableAPI}.
 *<p>
 * Row objects are constructed by
 * {@link Table#createRow createRow} or implicitly when returned from table
 * operations.
 */
public class RowImpl extends RecordValueImpl implements Row {

    private static final long serialVersionUID = 1L;

    protected final TableImpl table;

    private Version version;

    private int tableVersion;

    /*
     * A special expiration time of -1 is used to indicate that this value
     * is not valid. This is internal use only and never returned to users.
     */
    private long expirationTime;

    private TimeToLive ttl;

    public RowImpl() {
        table = null;
    }

    RowImpl(RecordDef field, TableImpl table) {
        super(field);
        assert field != null && table != null;
        this.table = table;
        version = null;
        tableVersion = 0;
        expirationTime = -1;
    }

    RowImpl(RecordDef field, TableImpl table, Map<String, FieldValue> fieldMap) {
        super(field, fieldMap);
        assert field != null && table != null;
        this.table = table;
        version = null;
        tableVersion = 0;
        expirationTime = -1;
    }

    RowImpl(RowImpl other) {
        super(other);
        this.table = other.table;
        this.version = other.version;
        this.tableVersion = other.tableVersion;
        this.expirationTime = other.expirationTime;
        this.ttl = other.ttl;
    }

    /**
     * Return the Table associated with this row.
     */
    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public boolean isRow() {
        return true;
    }

    @Override
    public Row asRow() {
        return this;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public int getTableVersion() {
        return tableVersion;
    }

    void setTableVersion(final int tableVersion) {
        this.tableVersion = tableVersion;
    }

    void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public RowImpl clone() {
        return new RowImpl(this);
    }

    @Override
    public PrimaryKey createPrimaryKey() {
        RowImpl impl = clone();
        impl.removeValueFields();
        return new PrimaryKeyImpl(getDefinition(), table,
                                  impl.valueMap);
    }

    public int getDataSize() {
        return table.getDataSize(this);
    }

    public int getKeySize() {
        return table.getKeySize(this);
    }

    /**
     * Return the KVStore Key for the actual key/value record that this
     * object references.
     *
     * @param allowPartial set true if the primary key need not be complete.
     * This is the case for multi- and iterator-based methods.
     */
    public Key getPrimaryKey(boolean allowPartial) {
        return table.createKey(this, allowPartial);
    }

    /* public for query processor */
    public TableImpl getTableImpl() {
        return table;
    }

    /**
     * Return a new Row based on the ValueVersion returned from the store.
     * Make a copy of this object to hold the data.  It is assumed
     * that "this" holds the primary key.
     *
     * @param keyOnly set to true if only primary key fields should be
     * copied/cloned.
     *
     * public for access from api/ops
     */
    public RowImpl rowFromValueVersion(ValueVersion vv, boolean keyOnly) {

        /*
         * Don't use clone to avoid creating a PrimaryKey, in the case
         * that "this" is actually a PrimaryKey.
         */
        RowImpl row = new RowImpl(this);
        if (keyOnly) {
            removeValueFields();
        }
        return table.rowFromValueVersion(vv, row);
    }

    /**
     * Copy the primary key fields from row to this object.
     */
    void copyKeyFields(RowImpl row) {
        for (String keyField : table.getPrimaryKey()) {
            FieldValue val = row.get(keyField);
            if (val != null) {
                put(keyField, val);
            }
        }
    }

    /**
     * Create a Value from a Row
     */
    public Value createValue() {
        return table.createValue(this);
    }

    @Override
    FieldDefImpl validateNameAndType(String name,
                                     FieldDef.Type type) {
        return super.validateNameAndType(name, type);
    }

    /**
     * Compares two Rows taking into consideration that they may come from
     * different tables, which can happen when accessing ancestor and/or child
     * tables in the same scan.  In this case the ancestor should come first,
     * which can be determined by the table ID.  Ancestor table IDs are always
     * smaller than child table IDs.
     */
    @Override
    public int compareTo(FieldValue other) {
        if (other instanceof RowImpl) {
            RowImpl otherImpl = (RowImpl) other;
            if (table.getId() == otherImpl.table.getId()) {
                return super.compareTo(otherImpl);
            }
            return compare(table.getId(), otherImpl.table.getId());
        }
        throw new IllegalArgumentException
            ("Cannot compare Row to another type");
    }

    /* TODO: Replace with Java 7 Long.compareTo */
    private static int compare(long x, long y) {
        return (x < y) ? -1 : (x == y) ? 0 : 1;
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other)) {
            if (other instanceof RowImpl) {
                return table.nameEquals(((RowImpl) other).table);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + table.getFullName().hashCode();
    }

    /*
     * At this time it's difficult, but not imppossible for an application to
     * create a TimeToLive with a negative duration, so check that. It is
     * using the TimeToLive.fromExpirationTime() interface.
     */
    @Override
    public void setTTL(TimeToLive ttl) {
        if (ttl != null && ttl.getValue() < 0) {
            throw new IllegalArgumentException(
                "Row.setTTL() does not support negative time periods");
        }
        this.ttl = ttl;
    }

    @Override
    public TimeToLive getTTL() {
        return ttl;
    }

    /*
     * If the expiration time is not valid, throw.
     */
    @Override
    public long getExpirationTime() {
        if (expirationTime >= 0) {
            return expirationTime;
        }
        throw new IllegalStateException(
            "Row expiration time is not defined for this instance");
    }

    /**
     * Sets expiration time on output. This is used internally by methods that
     * create Row instances retrieved from the server. This method is not used
     * for input. setTTL() is used by users to set a time to live value for a
     * row.
     */
    void setExpirationTime(long t) {
        expirationTime = t;
    }

    /*
     * This is used by internal methods to return the actual TTL value, if
     * set. It has the side effect of clearing expiration time.  Expiration
     * time must be cleared because this method is called on put operations and
     * the expiration time is set as a side effect, so any existing expiration
     * time must be removed.
     */
    TimeToLive getTTLAndClearExpiration() {
        TimeToLive retVal = ttl;
        expirationTime = -1;
        return retVal;
    }

    private void removeValueFields() {
        if (table.hasValueFields()) {
            /* remove non-key fields if present */
            Iterator<Map.Entry<String, FieldValue>> entries =
                valueMap.entrySet().iterator();

            while (entries.hasNext()) {
                Map.Entry<String, FieldValue> entry = entries.next();
                if (!table.isKeyComponent(entry.getKey())) {
                    entries.remove();
                }
            }
        }
    }
}
