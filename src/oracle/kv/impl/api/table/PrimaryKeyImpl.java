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

import java.util.List;
import java.util.Map;

import oracle.kv.impl.topo.PartitionId;
import oracle.kv.impl.api.KVStoreImpl;

import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.RecordDef;

/**
 * The RecordDef associated with a PrimaryKey is the RecordDef of the
 * associated table (so it includes non pk fields as well). However,
 * PrimaryKey redefines methods like getFields(), getFieldMapEntry(), etc.
 * so that it hides the non-pk fields.
 */
public class PrimaryKeyImpl extends RowImpl implements PrimaryKey {

    private static final long serialVersionUID = 1L;

    PrimaryKeyImpl(RecordDef field, TableImpl table) {
        super(field, table);
    }

    PrimaryKeyImpl(RecordDef field, TableImpl table,
                   Map<String, FieldValue> fields) {
        super(field, table, fields);
    }

    private PrimaryKeyImpl(PrimaryKeyImpl other) {
        super(other);
    }

    @Override
    public PrimaryKeyImpl clone() {
        return new PrimaryKeyImpl(this);
    }

    @Override
    public PrimaryKey asPrimaryKey() {
        return this;
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }

    @Override
    FieldDefImpl validateNameAndType(String name, FieldDef.Type type) {

        FieldDefImpl def = super.validateNameAndType(name, type);

        if (!table.isKeyComponent(name)) {
            throw new IllegalArgumentException
                ("Field is not part of PrimaryKey: " + name);
        }
        return def;
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other)) {
            return (other != null && other instanceof PrimaryKeyImpl);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Overrides for RecordValueImpl
     */

    /*
     * This is overridden in order to validate the value based on
     * a size constraint if it exists. There is only one type (Integer)
     * that can be constrained so rather than create a method on
     * FieldValueImpl to override, call IntegerValueImpl directly. If
     * this ever changes the validation method can be made part of the
     * interface.
     */
    @Override
    void putField(String name, FieldValue value) {

        if (value.isInteger()) {
            int size = table.getPrimaryKeySize(name);
            if (size != 0) {
                ((IntegerValueImpl) value).validateStorageSize(size);
            }
        }
        valueMap.put(name, value);
    }

    @Override
    int getNumFields() {
        return table.getPrimaryKey().size();
    }

    @Override
    FieldDefImpl getFieldDef(String fieldName) {
        FieldDefImpl def = getDefinition().getField(fieldName);
        if (def != null && table.isKeyComponent(fieldName)) {
            return def;
        }
        return null;
    }

    @Override
    FieldMapEntry getFieldMapEntry(String fieldName) {
        FieldMapEntry fme = getDefinition().getFieldMapEntry(fieldName, false);
        if (fme != null && table.isKeyComponent(fieldName)) {
            return fme;
        }
        return null;
    }

    @Override
    public List<String> getFields() {
        return table.getPrimaryKey();
    }

    @Override
    public List<String> getFieldNamesInternal() {
        return table.getPrimaryKeyInternal();
    }

    @Override
    public int getDataSize() {
        throw new IllegalArgumentException
            ("It is not possible to get data size from a PrimaryKey");
    }

    /**
     * Validate the index key.  Rules:
     * 1. Fields must be in the index
     * 2. Fields must be specified in order.  If a field "to the right"
     * in the index definition is set, all fields to its "left" must also
     * be present.
     */
    @Override
    public void validate() {
        List<String> key = table.getPrimaryKey();
        int numFound = 0;
        for (int i = 0; i < key.size(); i++) {
            String s = key.get(i);
            if (get(s) != null) {
                if (i != numFound) {
                    throw new IllegalArgumentException
                        ("PrimaryKey is missing fields more significant than" +
                         " field: " + s);
                }
                ++numFound;
            }
        }
        if (numFound != size()) {
           throw new IllegalArgumentException
               ("PrimaryKey contains a field that is not part of the key");
        }
    }

    public boolean isComplete() {
        return table.getPrimaryKeySize() == size();
    }

    /*
     * This method works correctly only if the PrimaryKey has been validated
     * to make sure that there are no "gaps" in the key values set already.
     * No validation is needed if the (internal) caller builds the key
     * correctly (with no gaps), as is the case (for example) with the
     *  OptRulePushIndexPreds class in the query compiler.
     */
    public boolean hasShardKey() {
        return table.getShardKeySize() <= size();
    }

    /**
     * Creates a byte[] representation of the key. This may be
     * partial.
     */
    public byte[] createKeyBytes() {
        return TableKey.createKey(getTable(), this, true).getKeyBytes();
    }

    /**
     * If this PrimakyKey contains a complete shard key, get the associated
     * partition id. Otherwise return null.
     */
    public PartitionId getPartitionId(KVStoreImpl store) {

        if (!hasShardKey()) {
            return null;
        }

        TableKey key = TableKey.createKey(table, this, true/*allowPartial*/);

        byte[] binaryKey = store.getKeySerializer().toByteArray(key.getKey());
        return  store.getDispatcher().getPartitionId(binaryKey);
    }
}
