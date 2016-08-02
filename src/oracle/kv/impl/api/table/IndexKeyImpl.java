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

import java.util.ArrayList;
import java.util.List;

import oracle.kv.impl.api.table.IndexImpl.IndexField;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;

public class IndexKeyImpl extends RecordValueImpl implements IndexKey {
    private static final long serialVersionUID = 1L;
    final IndexImpl index;

    /**
     * The RecordDef associated with an IndexKeyImpl is that of its table.
     */
    IndexKeyImpl(IndexImpl index) {
        super(index.getTableImpl().getRecordDef());
        this.index = index;
    }

    private IndexKeyImpl(IndexKeyImpl other) {
        super(other);
        this.index = other.index;
    }

    /**
     * Return the Index associated with this key
     */
    @Override
    public Index getIndex() {
        return index;
    }

    @Override
    public IndexKeyImpl clone() {
        return new IndexKeyImpl(this);
    }

    @Override
    FieldDefImpl validateNameAndType(String name,
                                     FieldDef.Type type) {
        if (!index.containsField(name)) {
            throw new IllegalArgumentException
                ("Field is not part of Index: " + name);
        }

        /*
         * Manually do the work of super.validateNameAndType() in order to
         * handle the complexities of map indexes.
         */

        FieldDef ft = getDefinition(name);
        if (ft == null) {
            throw new IllegalArgumentException("No such field in record: " +
                                               name);
        }
        if (ft.getType() != type) {
            if (ft.getType() != FieldDef.Type.MAP ||
                type != FieldDef.Type.STRING) {
                throw new IllegalArgumentException
                    ("Incorrect type for field " +
                     name + ", type is " + type +
                     ", expected " + ft.getType());
            }
        }
        return (FieldDefImpl) ft;
    }

    @Override
    public IndexKey asIndexKey() {
        return this;
    }

    @Override
    public boolean isIndexKey() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other)) {
            return other instanceof IndexKeyImpl;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    int getNumFields() {
        return index.getFieldsInternal().size();
    }

    @Override
    FieldDef getField(String fieldName) {
        FieldDef def = getDefinition().getField(fieldName);
        if (def != null && index.containsField(fieldName)) {
            return def;
        }
        return null;
    }

    @Override
    FieldMapEntry getFieldMapEntry(String fieldName) {
        FieldMapEntry fme = getDefinition().getFieldMapEntry(fieldName, false);
        if (fme != null && index.containsField(fieldName)) {
            return fme;
        }
        return null;
    }

    @Override
    public List<String> getFields() {
        return index.getFields();
    }

    /**
     * Gets the fields that are associated with the index.  This method is
     * called by copy methods and other functions that only expect the top-level
     * fields and not any nested fields, so in the case of a complex field,
     * only return the first component.
     */
    @Override
    protected List<String> getFieldsInternal() {
        List<IndexField> indexFields = index.getIndexFields();
        List<String> list = new ArrayList<String>(indexFields.size());
        for (IndexField indexField : indexFields)  {
            list.add(indexField.getComponents().get(0));
        }
        return list;
    }

    /**
     * IndexKey cannot contain null values
     */
    @Override
    public IndexKey putNull(String name) {
        throw new IllegalArgumentException
            ("IndexKey may not contain null values");
    }

    /**
     * Creates a record with its index field set for validation.
     */
    @Override
    public RecordValueImpl putRecord(String fieldName) {
        RecordValueImpl record = super.putRecord(fieldName);
        record.setIndex(index);
        return record;
    }

    /**
     * Creates a map with its index field set for validation.
     */
    @Override
    public MapValueImpl putMap(String fieldName) {
        MapValueImpl map = super.putMap(fieldName);
        map.setIndex(index);
        return map;
    }

    /**
     * Creates an array with its index field set for validation.
     */
    @Override
    public ArrayValueImpl putArray(String fieldName) {
        ArrayValueImpl array = super.putArray(fieldName);
        array.setIndex(index);
        return array;
    }

    public int getKeySize() {
        return index.serializeIndexKey(this).length;
    }

    TableImpl getTable() {
        return index.getTableImpl();
    }

    FieldValue putComplex(String fieldName, FieldDef.Type type,
                          FieldValue value) {
        return putComplex(index.createIndexField(fieldName), type, value);
    }

    /**
     * Puts a potentially nested value into an index key.  This exists for the
     * CLI to allow it to put nested values by name vs via JSON.
     */
    public FieldValue putComplexField(String fieldName, FieldValue value) {
        return putComplex(fieldName, value.getType(), value);
    }

    /**
     * Finds a potentially nested FieldDef by name.  This exists for the CLI to
     * allow it to put nested values by name vs via JSON.
     */
    public FieldDefImpl findFieldDefinition(String fieldName) {
        FieldDefImpl def =
            index.findIndexField(index.createIndexField(fieldName));
        if (def == null) {
            throw new IllegalArgumentException
                ("Field does not exist in the index: " + fieldName);
        }
        return def;
    }

    /**
     * Validate the index key.  Rules:
     * 1. Fields must be in the index
     * 2. Fields must be specified in order.  If a field "to the right"
     * in the index definition is set, all fields to its "left" must also
     * be present.
     *
     * If the field is in a nested type this code will fetch the actual "leaf"
     * FieldValue and validate.
     */
    @Override
    void validate() {
        int numFound = 0;
        int i = 0;
        for (IndexField field : index.getIndexFields()) {
            FieldValueImpl val = getComplex(field);
            if (val != null) {
                if (i != numFound) {
                    throw new IllegalArgumentException
                        ("IndexKey is missing fields more significant than" +
                         " field: " + field);
                }

                if (val.isNull()) {
                    throw new IllegalArgumentException
                        ("Field value is null, which is invalid for " +
                         "IndexKey: " + field);
                }

                /*
                 * If the field is an array it must have a single entry.
                 */
                if (val.isArray() && val.asArray().size() != 1) {
                    throw new IllegalArgumentException
                        ("Arrays used in index keys must contain a single " +
                         "element: " + this);
                }
                ++numFound;
            }
            ++i;
        }

        /*
         * numValues() counts all values in the record that are set, descending
         * into complex types as well.  This ensures that there are no extra
         * fields in an IndexKey that do not belong.  Unfortunately this check
         * won't occur until after the IndexKey has been created.  Bad top-level
         * fields are validated when they are set, but bad nested fields aren't
         * found until use of the IndexKey.
         */
        if (numFound != numValues()) {
            throw new IllegalArgumentException
                ("IndexKey contains a field that is not part of the Index");
        }
    }

    /**
     * Index key field lists may be nested so make this a special case vs using
     * RecordValueImpl.compareTo.
     * TODO: re-combine them?
     */
    @Override
    public int compareTo(FieldValue other) {
        if (other instanceof IndexKeyImpl) {
            IndexKeyImpl otherImpl = (IndexKeyImpl) other;
            if (!getDefinition().equals(otherImpl.getDefinition())) {
                throw new IllegalArgumentException
                    ("Cannot compare IndexKeys with different definitions");
            }
            return compare(otherImpl);
        }
        throw new ClassCastException
            ("Object is not an RecordValue");
    }

    private int compare(IndexKeyImpl other) {
        FieldMap fieldMap = getDefinition().getFieldMap();
        for (String fieldName : index.getFieldsInternal()) {
            FieldValueImpl val =
                getComplex(new TableImpl.TableField(fieldMap, fieldName));
            FieldValueImpl otherVal =
                other.getComplex(new TableImpl.TableField(fieldMap, fieldName));
            if (val != null) {
                if (otherVal == null) {
                    return 1;
                }
                int comp = val.compareTo(otherVal);
                if (comp != 0) {
                    return comp;
                }
            } else if (otherVal != null) {
                return -1;
            }
        }
        /* they must be equal */
        return 0;
    }

    public IndexImpl getIndexImpl() {
        return index;
    }

    /**
     * Return true if all fields in the index are specified.
     */
    boolean isComplete() {
        return (size() == getNumFields());
    }

    /**
     * This function behaves like adding "one" to the entire index key.  That
     * is, it increments the least significant field but if that field "wraps"
     * in that it's already at its maximum in terms of data type, such as
     * Integer.MAX_VALUE then increment the next more significant field and
     * set that field to its minimum value.
     *
     * If the value would wrap and there are no more significant fields then
     * return false, indicating that the "next" value is actually the end
     * of the index, period.
     *
     * This code is used to implement inclusive/exclusive semantics.
     *
     * Indexes that include a map key as a field are slightly more complicated.
     * In this case the key needs to be incremented and put back into the map.
     * In order to avoid multiple keys in the map the original key must be
     * removed from the map.
     */
    public boolean incrementIndexKey() {
        FieldValue[] values = new FieldValue[index.numFields()];
        int fieldIndex = 0;
        List<IndexField> indexFields = index.getIndexFields();
        for (IndexField field : indexFields) {
            values[fieldIndex] = getComplex(field);
            if (values[fieldIndex] == null || values[fieldIndex].isNull()) {
                break;
            }
            fieldIndex++;
        }

        /*
         * At least one field must exist.  Assert that and move back to the
         * target field.
         */
        assert fieldIndex > 0;
        --fieldIndex;

        /*
         * Increment and reset.  If the current field returns null, indicating
         * that it will wrap its value, set it to its minimum value and move to
         * the next more significant field.  If there are none, return false
         * indicating that there are no larger keys in the index that match the
         * key.
         */
        FieldValueImpl fvi = ((FieldValueImpl)values[fieldIndex]).getNextValue();
        while (fvi == null) {
            fvi = ((FieldValueImpl)values[fieldIndex]).getMinimumValue();

            /*
             * If the field is a map key, remove the existing key.
             */
            IndexField ifield = indexFields.get(fieldIndex);
            if (ifield.isMapKey()) {
                clearMap(ifield);
            }
            putComplex(indexFields.get(fieldIndex), fvi.getType(), fvi);

            /*
             * Move to next more significant field if it exists
             */
            --fieldIndex;
            if (fieldIndex >= 0) {
                fvi = ((FieldValueImpl)values[fieldIndex]).getNextValue();
            } else {
                /*
                 * Failed to increment
                 */
                return false;
            }
        }
        assert fvi != null && fieldIndex >= 0;


        /*
         * If the field is a map key, remove the existing key.
         */
        IndexField ifield = indexFields.get(fieldIndex);
        if (ifield.isMapKey()) {
            clearMap(ifield);
        }

        putComplex(indexFields.get(fieldIndex), fvi.getType(), fvi);
        return true;
    }

    /**
     * Clears the map that is referenced by the multiKeyPath() in IndexField.
     */
    private void clearMap(IndexField indexField) {
        FieldValue fv = getComplex(indexField.getMultiKeyField());
        if (fv == null || !fv.isMap()) {
            throw new IllegalStateException
                ("Did not find a map in IndexKey in path: " + indexField);
        }
        ((MapValueImpl)fv).clearMap();
    }
}
