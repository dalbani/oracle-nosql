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

import static oracle.kv.impl.api.table.TableImpl.ANONYMOUS;
import static oracle.kv.impl.api.table.TableImpl.KEY_TAG;
import static oracle.kv.impl.api.table.TableImpl.SEPARATOR;
import static oracle.kv.impl.api.table.TableJsonUtils.DESC;
import static oracle.kv.impl.api.table.TableJsonUtils.NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldRange;
import oracle.kv.table.FieldValue;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Table;

/**
 * Implementation of the Index interface.  Instances of this class are created
 * and associated with a table when an index is defined.  It contains the index
 * metdata as well as many utility functions used in serializing and
 * deserializing index keys.
 */
public class IndexImpl implements Index, Serializable {

    private static final long serialVersionUID = 1L;

    /* the index name */
    private final String name;
    /* the (optional) index description, user-provided */
    private final String description;
    /* the associated table */
    private final TableImpl table;
    /*
     * a raw list of the fields that define the index.  In the case of map
     * indexes this list may contain the special strings TableImpl.KEY_TAG and
     * TableImpl.ANONYMOUS to help define the indexed field.
     */
    private final List<String> fields;
    /* status is used when an index is being populated to indicate readiness */
    private IndexStatus status;
    /*
     * transient version of the fields, materialized as IndexField for
     * efficiency.  It is technically final but is not because it needs to be
     * initialized in readObject after deserialization.
     */
    private transient List<IndexField> indexFields;
    /*
     * transient indication of whether this is a multiKeyMapIndex.  This is
     * used for serialization/deserialization of map indexes.  It is
     * technically final but is not because it needs to be initialized in
     * readObject after deserialization.
     */
    private transient boolean isMultiKeyMapIndex;
    
    private Map<String, String> annotations;

    public enum IndexStatus {
        /** Index is transient */
        TRANSIENT() {
            @Override
            public boolean isTransient() {
                return true;
            }
        },

        /** Index is being populated */
        POPULATING() {
            @Override
            public boolean isPopulating() {
                return true;
            }
        },

        /** Index is populated and ready for use */
        READY() {
            @Override
            public boolean isReady() {
                return true;
            }
        };

        /**
         * Returns true if this is the {@link #TRANSIENT} type.
         * @return true if this is the {@link #TRANSIENT} type
         */
        public boolean isTransient() {
            return false;
        }

        /**
         * Returns true if this is the {@link #POPULATING} type.
         * @return true if this is the {@link #POPULATING} type
         */
	public boolean isPopulating() {
            return false;
        }

        /**
         * Returns true if this is the {@link #READY} type.
         * @return true if this is the {@link #READY} type
         */
	public boolean isReady() {
            return false;
        }
    }

    public IndexImpl(String name, TableImpl table, List<String> fields,
                     String description) {
    	this.name = name;
    	this.table = table;
    	this.fields = translateFields(fields);
    	this.description = description;
    	annotations = null;
    	status = IndexStatus.TRANSIENT;

    	/* validate initializes indexFields as well as isMultiKeyMapIndex */
    	validate();
    	assert indexFields != null;
    }
        
    /* Constructor for Full Text Indexes. */
    public IndexImpl(String name, TableImpl table,
                     List<String> fields,
                     Map<String, String> annotations,
                     String description) {

        this(name, table, fields, description);
    	this.annotations = annotations;
    }
    
    public static void populateMapFromAnnotatedFields
        (List<AnnotatedField> fields,
         List<String> fieldNames,
         Map<String, String> annotations) {

    	for (AnnotatedField f : fields) {
            String fieldName = f.getFieldName();
            String translatedFieldName =
                TableImpl.translateFromExternalField(fieldName);
            fieldName = (translatedFieldName == null ?
                         fieldName :
                         translatedFieldName);
            fieldNames.add(fieldName);
            annotations.put(fieldName, f.getAnnotation());
    	}
    }
    
    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public String getName()  {
        return name;
    }

    @Override
    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Returns an list of the fields that define a text index.
     * These are in order of declaration which is significant.
     *
     * @return the field names
     */
    public List<AnnotatedField> getFieldsWithAnnotations() {
    	if (! isTextIndex()) {
            throw new IllegalStateException
                ("getFieldsWithAnnotations called on non-text index");
    	}
    	
    	final List<AnnotatedField> fieldsWithAnnotations =
    			new ArrayList<AnnotatedField>(fields.size());
    	
    	for(String field : fields) {
            fieldsWithAnnotations.add
                (new AnnotatedField(field, annotations.get(field)));
    	}
        return fieldsWithAnnotations;
    }
    
    Map<String, String> getAnnotations() {
    	return Collections.unmodifiableMap(annotations);
    }

    @Override
    public String getDescription()  {
        return description;
    }

    @Override
    public IndexKeyImpl createIndexKey() {
        return new IndexKeyImpl(this);
    }

    @Override
    public IndexKeyImpl createIndexKey(RecordValue value) {
        IndexKeyImpl ikey = new IndexKeyImpl(this);
        TableImpl.populateRecord(ikey, value);
        return ikey;
    }

    @Override
    public IndexKey createIndexKeyFromJson(String jsonInput, boolean exact) {
        return createIndexKeyFromJson
            (new ByteArrayInputStream(jsonInput.getBytes()), exact);
    }

    @Override
    public IndexKey createIndexKeyFromJson(InputStream jsonInput,
                                           boolean exact) {
        IndexKeyImpl key = createIndexKey();
        TableImpl.createFromJson(key, jsonInput, exact);
        return key;
    }

    @Override
    public FieldRange createMapKeyFieldRange(String mapField) {
        StringBuilder sb = new StringBuilder(mapField);
        sb.append(SEPARATOR);
        sb.append(KEY_TAG);
        return createFieldRange(sb.toString());
    }

    @Override
    public FieldRange createMapValueFieldRange(String mapField,
                                               String valueField) {
        StringBuilder sb = new StringBuilder(mapField);
        sb.append(SEPARATOR);
        sb.append(ANONYMOUS);
        if (valueField != null) {
            sb.append(SEPARATOR);
            sb.append(valueField);
        }
        return createFieldRange(sb.toString());
    }

    @Override
    public FieldRange createFieldRange(String fieldName) {
        IndexField field = new IndexField(table, fieldName);
        FieldDef def = findIndexField(field);
        if (def == null) {
            throw new IllegalArgumentException
                ("Field does not exist in table definition: " + fieldName);
        }
        if (!containsField(field)) {
            throw new IllegalArgumentException
                ("Field does not exist in index: " + fieldName);
        }
        return new FieldRange(fieldName, def);
    }

    int numFields() {
        return fields.size();
    }

    /**
     * Returns true if the index comprises only fields from the table's primary
     * key.  Nested types can't be key components so there is no need to handle
     * a complex path.
     */
    public boolean isKeyOnly() {
        for (String field : fields) {
            if (!table.isKeyComponent(field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if this index has multiple keys perrecord.  This can happen
     * if there is an array or map in the index.  An index can only contain one
     * array or map.
     */
    public boolean isMultiKey() {
        for (IndexField field : getIndexFields()) {
            if (field.isMultiKey()) {
                return true;
            }
        }
        return false;
    }

    public IndexStatus getStatus() {
        return status;
    }

    public void setStatus(IndexStatus status) {
        this.status = status;
    }

    public TableImpl getTableImpl() {
        return table;
    }

    public List<String> getFieldsInternal() {
        return fields;
    }

    /**
     * Returns the list of IndexField objects defining the index.  It is
     * transient, and if not yet initialized, initialize it.
     */
    List<IndexField> getIndexFields() {
    	if (isTextIndex()) {
            throw new IllegalStateException
                ("getIndexFields called on a text index");
    	}
        if (indexFields == null) {
            initIndexFields();
        }
        return indexFields;
    }

    /**
     * Initializes the transient list of index fields.  This is used when
     * the IndexImpl was constructed via deserialization and the constructor
     * and validate() were not called.
     *
     * TODO: figure out how to do transient initialization in the
     * deserialization case.  It is not as simple as implementing readObject()
     * because an intact Table is required.  Calling validate() from TableImpl's
     * readObject() does not work either (generates an NPE).
     */
    private void initIndexFields() {
        if (indexFields == null) {
            List<IndexField> list = new ArrayList<IndexField>(fields.size());
            for (String field : fields) {
                IndexField indexField = new IndexField(table, field);

                /* this sets the multiKey state of the IndexField */
                isMultiKey(indexField);
                list.add(indexField);
            }
            indexFields = list;
        }
    }

    /**
     * If there's a multi-key field in the index return a new IndexField
     * based on the the path to the complex instance.
     */
    private IndexField findMultiKeyField() {
        for (IndexField field : getIndexFields()) {
            if (field.isMultiKey()) {
                return field.getMultiKeyField();
            }
        }

        throw new IllegalStateException
            ("Could not find any multiKeyField in index " + name);
    }

    private boolean isMultiKeyMapIndex() {
        return isMultiKeyMapIndex;
    }

    /**
     * Returns true if the (complex) fieldName contains a reference to a
     * field that has multiple keys anywhere in its path, false otherwise.
     * MultiKey fields include arrays and most map indexes.
     *
     * This call has a side effect of setting the multiKey state in the
     * IndexField so that the lookup need not be done twice.
     */
    private boolean isMultiKey(IndexField field) {
        StringBuilder sb = new StringBuilder();
        List<String> components = field.getComponents();
        int compIndex = 0;

        FieldDefImpl def = field.getFirstDef();
        sb.append(components.get(compIndex++));
        if (checkMultiKey(field, def, components, compIndex, sb)) {
                return true;
        }
        sb.append(SEPARATOR);
        while (compIndex < components.size()) {
            String current = components.get(compIndex++);
            sb.append(current);
            def = def.findField(current);
            assert def != null;
            if (checkMultiKey(field, def, components, compIndex, sb)) {
                return true;
            }
            sb.append(SEPARATOR);
        }
        return false;
    }

    /**
     * Determine if the current FieldDef is a multiKey field, and if so,
     * set the field's path in the IndexField and return true.
     *
     * All arrays are multiKey, as well as some map indexes.
     * There are 3 types of map index:
     * 1.  value-only (not multi-key).  mapField.key[.path], where "key" is
     * not part of the map's schema.
     * 2.  key-only (multi-key).  Specified by mapField._key
     * 3.  key + value (multi-key).  Specified by mapField.[].
     *
     * @param field the IndexField being checked
     * @param def the current FieldDef in the field
     * @param components the list of string components in the field
     * @param compIndex the index in components of the *next* field name
     * in the IndexField path
     * @param sb a StringBuilder holding the path to the current component
     *
     * @return true if the field is multi-key.
     */
    private boolean checkMultiKey(IndexField field,
                                  FieldDefImpl def,
                                  List<String> components,
                                  int compIndex,
                                  StringBuilder sb) {
        assert sb != null;
        assert compIndex > 0;
        if (def.isArray()) {
            field.setMultiKeyPath(sb.toString());
            return true;
        }

        if (def.isMap()) {
            FieldDefImpl elementDef =
                (FieldDefImpl) ((MapDefImpl)def).getElement();

            if (compIndex == components.size()) {
                throw new IllegalCommandException
                    ("Indexes on maps must specify _key, [], or a path " +
                     "to the target field");
            }

            /*
             * Get the next component and keep processing.
             */
            String next = components.get(compIndex);

            /*
             * index on key
             */
            if (MapDefImpl.isMapKeyTag(next)) {
                field.setMultiKeyPath(sb.toString());
                field.setIsMapKey();
                isMultiKeyMapIndex = true;
                return true;
            }

            /*
             * index on value
             */
            if (MapDefImpl.isMapValueTag(next)) {
                if (elementDef.isMap() || elementDef.isArray()) {
                    throw new IllegalCommandException
                        ("Indexes are not allowed on a map " +
                         "containing a map or array");
                }
                field.setMultiKeyPath(sb.toString());
                field.setIsMapValue();
                isMultiKeyMapIndex = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts an index key from the key and data for this
     * index.  The key has already matched this index.
     *
     * @param key the key bytes
     *
     * @param data the row's data bytes
     *
     * @param keyOnly true if the index only uses key fields.  This
     * optimizes deserialization.
     *
     * @return the byte[] serialization of an index key or null if there
     * is no entry associated with the row, or the row does not match a
     * table record.
     *
     * While not likely it is possible that the record is not actually  a
     * table record and the key pattern happens to match.  Such records
     * will fail to be deserialized and throw an exception.  Rather than
     * treating this as an error, silently ignore it.
     *
     * TODO: maybe make this faster.  Right now it turns the key and data
     * into a Row and extracts from that object which is a relatively
     * expensive operation, including full Avro deserialization.
     */
    public byte[] extractIndexKey(byte[] key,
                                  byte[] data,
                                  boolean keyOnly) {
        RowImpl row = table.createRowFromBytes(key, data, keyOnly);
        if (row != null) {
            return serializeIndexKey(row, false, 0);
        }
        return null;
    }

    /**
     * Extracts multiple index keys from a single record.  This is used if
     * one of the indexed fields is an array.  Only one array is allowed
     * in an index.
     *
     * @param key the key bytes
     *
     * @param data the row's data bytes
     *
     * @param keyOnly true if the index only uses key fields.  This
     * optimizes deserialization.
     *
     * @return a List of byte[] serializations of index keys or null if there
     * is no entry associated with the row, or the row does not match a
     * table record.  This list may contain duplicate values.  The caller is
     * responsible for handling duplicates (and it does).
     *
     * While not likely it is possible that the record is not actually  a
     * table record and the key pattern happens to match.  Such records
     * will fail to be deserialized and throw an exception.  Rather than
     * treating this as an error, silently ignore it.
     *
     * TODO: can this be done without reserializing to Row?  It'd be
     * faster but more complex.
     *
     * 1.  Deserialize to RowImpl
     * 2.  Find the map or array value and get its size
     * 3.  for each map or array entry, serialize a key using that entry
     */
    public List<byte[]> extractIndexKeys(byte[] key,
                                         byte[] data,
                                         boolean keyOnly) {

        RowImpl row = table.createRowFromBytes(key, data, keyOnly);
        if (row != null) {
            IndexField indexField = findMultiKeyField();

            FieldValueImpl val = row.getComplex(indexField);
            if (val == null || val.isNull()) {
                return null;
            }

            if (val.isMap()) {
                MapValueImpl mapVal = (MapValueImpl) val;
                ArrayList<byte[]> returnList =
                    new ArrayList<byte[]>(mapVal.size());
                Map<String, FieldValue> map = mapVal.getFieldsInternal();
                for (String mapKey : map.keySet()) {
                    byte[] serKey = serializeIndexKey(row, false, mapKey, true);
                    if (serKey != null) {
                        returnList.add(serKey);
                    }
                }
                return returnList;
            }

            assert val.isArray();
            ArrayValueImpl fv = (ArrayValueImpl) val;

            int arraySize = fv.size();
            ArrayList<byte[]> returnList = new ArrayList<byte[]>(arraySize);
            for (int i = 0; i < arraySize; i++) {
                byte[] serKey = serializeIndexKey(row, false, i);

                /*
                 * It should not be possible for this to be null because
                 * it is not possible to add null values to arrays, but
                 * a bit of paranoia cannot hurt.
                 */
                if (serKey != null) {
                    returnList.add(serKey);
                }
            }
            return returnList;
        }
        return null;
    }

    public void toJsonNode(ObjectNode node) {
        node.put(NAME, name);
        node.put(DESC, description);
        if (isMultiKey()) {
            node.put("multi_key", "true");
        }
        ArrayNode fieldArray = node.putArray("fields");
        for (String s : fields) {
            fieldArray.add(TableImpl.translateToExternalField(s));
        }
    }

    /**
     * Validate that the name, fields, and types of the index match
     * the table.  This also initializes the (transient) list of index fields in
     * indexFields, so that member must not be used in validate() itself.
     *
     * This method must only be called from the constructor.  It is not
     * synchronized and changes internal state.
     */
    private void validate() {
        TableImpl.validateComponent(name, false);
        IndexField multiKeyField = null;
        if (fields.isEmpty()) {
            throw new IllegalCommandException
                ("Index requires at least one field");
        }

        assert indexFields == null;

        indexFields = new ArrayList<IndexField>(fields.size());

        for (String field : fields) {
            if (field == null || field.length() == 0) {
                throw new IllegalCommandException
                    ("Invalid (null or empty) index field name");
            }
            IndexField ifield = new IndexField(table, field);

            /*
             * This call handles indexes into complex, nested types.
             */
            FieldDefImpl def = findIndexField(ifield);
            if (def == null) {
                throw new IllegalCommandException
                    ("Index field not found in table: " + field);
            }
            if (!def.isValidIndexField()) {
                throw new IllegalCommandException
                    ("Field type is not valid in an index: " +
                     def.getType() + ", field name: " + field);
            }

            /*
             * The check for multiKey needs to consider all fields as well as
             * fields that reference into complex types.  A multiKey field may
             * occur at any point in the navigation path (first, interior, leaf).
             *
             * The call to isMultiKey() will set the multiKey state in
             * the IndexField.
             *
             * Allow more than one multiKey field in a single index IFF they are
             * in the same object (map or array).
             */
            boolean fieldIsMultiKey = isMultiKey(ifield);
            if (fieldIsMultiKey) {
                IndexField mkey = ifield.getMultiKeyField();
                if (multiKeyField != null && !mkey.equals(multiKeyField)) {
                    throw new IllegalCommandException
                        ("Indexes may contain only one multiKey field");
                }
                multiKeyField = mkey;
            }
            if (indexFields.contains(ifield)) {
                throw new IllegalCommandException
                    ("Index already contains the field: " + field);
            }
            indexFields.add(ifield);
        }
        assert fields.size() == indexFields.size();
        table.checkForDuplicateIndex(this);
    }

    @Override
    public String toString() {
        return "Index[" + name + ", " + table.getId() + ", " + status + "]";
    }

    /**
     * Serialize the index fields from the RecordValueImpl argument.
     * Fields are extracted in index order.  It is assumed that the caller has
     * validated the record and that if it is an IndexKey that user-provided
     * fields are correct and in order.  This method is used if there may be
     * an array in the index.
     *
     * @param record the record to extract.  This may be an IndexKeyImpl or
     * RowImpl.  In both cases the caller can vouch for the validity of the
     * object.
     *
     * @param allowPartial if true then partial keys can be serialized.  This is
     * the case for client-based keys.  If false, partial keys result in
     * returning null.  This is the server side key extraction path.
     *
     * @param arrayIndex will be 0 if not doing an array lookup, or if the
     * desired array index is actually 0.  For known array lookups it may be
     * >0.
     *
     * @return the serialized index key or null if the record cannot
     * be serialized.
     *
     * These are conditions that will cause serialization to fail:
     * 1.  The record has a null values in one of the index keys
     * 2.  An index key field contains a map and the record does not
     * have a value for the indexed map key value
     *
     * TODO: consider sharing more code with the other serializeIndexKey()
     * method.
     */
    byte[] serializeIndexKey(RecordValueImpl record, boolean allowPartial,
                             int arrayIndex) {
        if (isMultiKeyMapIndex()) {
            throw new IllegalStateException("Wrong serializer for map index");
        }
        TupleOutput out = null;
        try {
            out = new TupleOutput();
            for (IndexField field : getIndexFields()) {
                FieldValue val =
                    record.findFieldValue(field.iterator(), arrayIndex);
                FieldDefImpl def = findIndexField(field);
                if (def == null) {
                    throw new IllegalStateException
                        ("Index field not found in table: " + field);
                }

                /*
                 * If the target field is an array use its type, which must be
                 * simple, and indexable.
                 */
                if (val != null) {
                    if (def.isArray()) {
                        def = (FieldDefImpl) ((ArrayDefImpl)def).getElement();
                        val = ((ArrayValueImpl)val).get(arrayIndex);
                    } else if (def.isMap()) {
                        String mapKey = ((MapValueImpl)val).getMapKey();
                        /*
                         * Call the serialize method that takes a mapKey.
                         * This is a single-key map index inside an array.
                         * This method restarts serialization from the start,
                         * so return the result.
                         */
                        return serializeIndexKey(record, allowPartial,
                                                 mapKey, false);
                    }
                }

                /*
                 * Failed to find a value, this is a partial key.
                 */
                if (val == null) {

                    /* If the key must be fully present, fail */
                    if (!allowPartial) {
                        return null;
                    }

                    /* A partial key, done with fields */
                    break;
                }

                /*
                 * If any values are null it is not possible to serialize the
                 * index key, even partially.  Null values cannot be indexed
                 * so this row has no entry for this index.
                 */
                if (val.isNull()) {
                    return null;
                }

                serializeValue(out, val, def);
            }
            return (out.size() != 0 ? out.toByteArray() : null);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Serialize the index fields from the RecordValueImpl argument.
     * Fields are extracted in index order.  It is assumed that the caller has
     * validated the record and that if it is an IndexKey that user-provided
     * fields are correct and in order. This method is used if there may be
     * a map in the index.
     *
     * @param record the record to extract.  This may be an IndexKeyImpl or
     * RowImpl.  In both cases the caller can vouch for the validity of the
     * object.
     *
     * @param allowPartial if true then partial keys can be serialized.  This is
     * the case for client-based keys.  If false, partial keys result in
     * returning null.  This is the server side key extraction path.
     *
     * @param mapKey will be null if not doing a map lookup.
     *
     * @return the serialized index key or null if the record cannot
     * be serialized.
     *
     * These are conditions that will cause serialization to fail:
     * 1.  The record has a null values in one of the index keys
     * 2.  An index key field contains a map and the record does not
     * have a value for the indexed map key value
     *
     * TODO: consider sharing more code with the other serializeIndexKey()
     * method.
     *
     * This method is package protected vs private because it's used by test
     * code.
     */
    /* private */ byte[] serializeIndexKey(RecordValueImpl record,
                                           boolean allowPartial,
                                           String mapKey,
                                           boolean extracting) {
        assert isMultiKeyMapIndex();
        TupleOutput out = null;
        try {
            out = new TupleOutput();
            for (IndexField field : getIndexFields()) {

                /*
                 * findField handles the special map fields of "_key" and
                 * "[]" and returns the correct information in both
                 * cases.  See MapValueImpl.findFieldValue().
                 */
                String keyString = (extracting || !field.isMapValue()) ?
                    mapKey : null;
                FieldValue val = record.findFieldValue(field.iterator(),
                                                       keyString);
                FieldDefImpl def = findIndexField(field);
                if (def == null) {
                    throw new IllegalStateException
                        ("Could not find index field: " + field);
                }

                /*
                 * Failed to find a value, this is a partial key.
                 */
                if (val == null) {

                    /* If the key must be fully present, fail */
                    if (!allowPartial) {
                        return null;
                    }

                    /* A partial key, done with fields */
                    break;
                }

                /*
                 * If any values are null it is not possible to serialize the
                 * index key, even partially.  Null values cannot be indexed
                 * so this row has no entry for this index.
                 */
                if (val.isNull()) {
                    return null;
                }

                serializeValue(out, val, def);
            }
            return (out.size() != 0 ? out.toByteArray() : null);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This is the version used by most client-based callers.  In this case
     * the key may be partially specified.
     *
     * @return the serialized index key or null if the record cannot
     * be serialized (e.g. it has null values).
     */
    public byte[] serializeIndexKey(IndexKeyImpl record) {
        if (isMultiKeyMapIndex()) {
            String mapKey = findMapKey(record);
            return serializeIndexKey(record, true, mapKey, false);
        }
        return serializeIndexKey(record, true, 0);
    }

    /**
     * This method is called on IndexKeyImpl objects that are used for an
     * index with a multi-key map component.  This method finds that map
     * field and then calls it to return the string that represents a
     * key in the map.  There can be only one.  The map itself may contain
     * 1 or 2 keys.  If 1 then it is returned.  If 2 then one of them should
     * be the special "[]" field.  MapValueImpl.getMapKey() skips that one
     * and returns any other key.
     *
     * To summarize: A result will be returned iff there is a map and it has
     * either a single key, or two keys, one of which is [].  Anything else
     * causes a null return.
     */
    private String findMapKey(IndexKeyImpl record) {
        for (IndexField field : getIndexFields()) {
            IndexField mapField = field.getMultiKeyField();
            if (mapField != null) {
                FieldValue val =
                    record.findFieldValue(mapField.iterator(), -1);
                if (val != null) {
                    if (!val.isMap()) {
                        throw new IllegalStateException
                            ("Multi-key value in index must be a map");
                    }
                    return ((MapValueImpl)val).getMapKey();
                }
                return null;
            }
        }
        return null;
    }

    static TupleInput serializeValue(FieldDef def, FieldValue value) {
        TupleOutput output = new TupleOutput();
        serializeValue(output, value, def);
        return new TupleInput(output);
    }

    private static void serializeValue(TupleOutput out, FieldValue val,
                                       FieldDef def) {

        switch (def.getType()) {
        case INTEGER:
            out.writeSortedPackedInt(val.asInteger().get());
            break;
        case STRING:
            out.writeString(val.asString().get());
            break;
        case LONG:
            out.writeSortedPackedLong(val.asLong().get());
            break;
        case DOUBLE:
            out.writeSortedDouble(val.asDouble().get());
            break;
        case FLOAT:
            out.writeSortedFloat(val.asFloat().get());
            break;
        case ENUM:
            /* enumerations are sorted by declaration order */
            out.writeSortedPackedInt(val.asEnum().getIndex());
            break;
        case MAP:
        case ARRAY:
        case BINARY:
        case BOOLEAN:
        case FIXED_BINARY:
        case RECORD:
            throw new IllegalStateException
                ("Type not supported in indexes: " +
                 def.getType());
        }
    }

    /**
     * Deserialize an index key into IndexKey.  The caller will also have
     * access to the primary key bytes which can be turned into a PrimaryKey
     * and combined with the IndexKey for the returned KeyPair.
     *
     * Arrays -- if there is an array index the index key returned will
     * be the serialized value of a single array entry and not the array
     * itself. This value needs to be deserialized back into a single-value
     * array.
     *
     * Maps -- if there is a map index the index key returned will
     * be the serialized value of a single map entry.  It may be key-only or
     * it may be key + value. In both cases the map and the appropriate key
     * need to be created.
     *
     * @param data the bytes
     * @param partialOK true if not all fields must be in the data stream.
     */
    public IndexKeyImpl rowFromIndexKey(byte[] data, boolean partialOK) {
        IndexKeyImpl ikey = createIndexKey();
        TupleInput input = null;

        try {
            input = new TupleInput(data);
            for (IndexField field : getIndexFields()) {
                if (input.available() <= 0) {
                    break;
                }

                IndexField mapField = (field.isMapKey() ?
                                       field.getMultiKeyField() :
                                       null);

                FieldDef def = (mapField != null ?
                                findIndexField(mapField) :
                                findIndexField(field));
                if (def == null) {
                    throw new IllegalStateException
                        ("Could not find index field: " + field);
                }
                switch (def.getType()) {
                case INTEGER:
                case STRING:
                case LONG:
                case DOUBLE:
                case FLOAT:
                case ENUM:
                    ikey.putComplex(field, def.getType(),
                                    FieldValueImpl.readTuple(def, input));
                    break;
                case MAP:

                    /* the data is not used by the map constructor */
                    assert mapField != null;
                    ikey.putComplex(mapField, FieldDef.Type.MAP, null);
                    MapValueImpl map = (MapValueImpl) ikey.getComplex(mapField);
                    handleMapKey(map, input, field);
                    break;
                case ARRAY:
                    /* the data is not used by the array constructor */
                    ikey.putComplex(field, FieldDef.Type.ARRAY, null);
                    ArrayValueImpl array =
                        (ArrayValueImpl) ikey.getComplex(field);
                    readArrayElement(array, input);
                    break;
                case BINARY:
                case BOOLEAN:
                case FIXED_BINARY:
                case RECORD:
                    throw new IllegalStateException
                        ("Type not supported in indexes: " +
                         def.getType());
                }
            }
            if (!partialOK && (ikey.numValues() != fields.size())) {
                throw new IllegalStateException
                    ("Missing fields from index data for index " +
                     getName() + ", expected " +
                     fields.size() + ", received " + ikey.numValues());
            }
            return ikey;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    private void readArrayElement(ArrayValueImpl array,
                                  TupleInput input) {
        switch (array.getDefinition().getElement().getType()) {
        case INTEGER:
            array.add(input.readSortedPackedInt());
            break;
        case STRING:
            array.add(input.readString());
            break;
        case LONG:
            array.add(input.readSortedPackedLong());
            break;
        case DOUBLE:
            array.add(input.readSortedDouble());
            break;
        case FLOAT:
            array.add(input.readSortedFloat());
            break;
        case ENUM:
            array.addEnum(input.readSortedPackedInt());
            break;
        default:
            throw new IllegalStateException("Type not supported in indexes: ");
        }
    }

    /**
     * Reads key from a serialized index key.  Value fields don't follow
     * this path.
     */
    private void handleMapKey(MapValueImpl map,
                              TupleInput input,
                              IndexField field) {

        /*
         * A map key field
         */
        if (!field.isMapKey()) {
            throw new IllegalStateException
                ("Field should have been a map key field: " + field);
        }
        String mapKey = input.readString();
        map.putNull(mapKey);
    }

    /**
     * Does a direct comparison of the IndexField to the existing fields to
     * look for duplicates.
     */
    boolean containsField(IndexField indexField) {
        for (IndexField iField : getIndexFields()) {
            if (iField.equals(indexField)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the index contains the *single* named field.
     * For simple types this is a simple contains operation.
     *
     * For complex types this needs to validate for a put of a complex
     * type that *may* contain an indexed field.
     * Validation of such fields must be done later.
     *
     * In the case of a nested field name with dot-separated names,
     * this code simply checks that fieldName is one of the components of
     * the complex field (using String.contains()).
     */
    boolean containsField(String fieldName) {
        String fname = fieldName.toLowerCase();

        for (IndexField indexField : getIndexFields()) {
            if (indexField.isComplex()) {
                if (indexField.getFieldName().contains(fname)) {
                    return true;
                }

            } else {
                if (indexField.getFieldName().equals(fname)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Translate external representation to internal.  E.g.:
     * keyof(mapfield) => mapfield._key
     * elementof(mapfield) => mapfield.[]
     *
     * Keywords (keyof, elementof) are case-insensitive.
     * NOTE: the values of the keyof and elementof strings are tied to what is
     * supported by the DDL/DML.  See Table.g for changes.
     *
     * This could be optimized for the case where there's nothing to do, but
     * this isn't a high performance path, so unconditionally copy the list.
     *
     * If there is a failure to parse the original list is returned, allowing
     * errors to be handled in validation of fields.
     */
    public static List<String> translateFields(List<String> fieldList) {
        ArrayList<String> newList = new ArrayList<String>(fieldList.size());

        for (String field : fieldList) {
            /*
             * it is possible for the field to be null, at least in test
             * cases.  If so, return the original list.
             */
            if (field == null) {
                return fieldList;
            }

            String newField = TableImpl.translateFromExternalField(field);
            /*
             * A null return means that the format of the field string is
             * not legal.  Return the original list and let the inevitable
             * failure happen (no such field) on the untranslated list.
             */
            if (newField == null) {
                return fieldList;
            }
            newList.add(newField);
        }
        return newList;
    }

    /**
     * When called internally using an already-validated IndexImpl and a field
     * that is known to exist, this method cannot fail to return an object.
     * When called during validation or with a field name passed from a user
     * (e.g. createFieldRange()) it can return null.
     */
    FieldDefImpl findIndexField(IndexField field) {
        return TableImpl.findTableField(field);
    }

    IndexField createIndexField(String fieldName) {
        return new IndexField(table, fieldName);
    }

    /**
     * Encapsulates a single field in an index, which may be simple or
     * complex.  Simple fields (e.g. "name") have a single component. Fields
     * that navigate into nested fields (e.g. "address.city") have multiple
     * components.  The state of whether a field is simple or complex is kept
     * by TableField.
     *
     * IndexField adds this state:
     *   multiKeyField -- if this field results in a multi-key index this holds
     *     the portion of the field's path that leads to the FieldValue that
     *     makes it multi-key -- an array or map.  This is used as a cache to
     *     make navigation to that field easier.
     *   multiKeyType -- if multiKeyPath is set, this indicates if the field
     *     is a map key or map value field.
     * Arrays don't need additional state.
     *
     * Field names are case-insensitive, so strings are stored lower-case to
     * simplify case-insensitive comparisons.
     */
    static class IndexField extends TableImpl.TableField {
        /* the path to a multi-key field (map or array) */
        private IndexField multiKeyField;
        private MultiKeyType multiKeyType;

        /* ARRAY is not included because no callers need that information */
        private enum MultiKeyType { NONE, MAPKEY, MAPVALUE }

        private IndexField(TableImpl table, String field) {
            super(table, field);
            multiKeyType = MultiKeyType.NONE;
        }

        private IndexField(FieldMap fieldMap, String field) {
            super(fieldMap, field);
            multiKeyType = MultiKeyType.NONE;
        }

        IndexField getMultiKeyField() {
            return multiKeyField;
        }

        private boolean isMultiKey() {
            return multiKeyField != null;
        }

        private void setMultiKeyPath(String path) {
            multiKeyField = new IndexField(getFieldMap(), path);
        }

        boolean isMapKey() {
            return multiKeyType == MultiKeyType.MAPKEY;
        }

        private void setIsMapKey() {
            multiKeyType = MultiKeyType.MAPKEY;
        }

        boolean isMapValue() {
            return multiKeyType == MultiKeyType.MAPVALUE;
        }

        private void setIsMapValue() {
            multiKeyType = MultiKeyType.MAPVALUE;
        }
    }

    @Override
    public Index.IndexType getType() {
        if (annotations == null) {
            return Index.IndexType.SECONDARY;
        }
        return Index.IndexType.TEXT;
    }

    private boolean isTextIndex() {
        return getType() == Index.IndexType.TEXT;
    }

    /**
     * This lightweight class stores an index field, along with
     * an annotation.  Not all index types require annotations;
     * It is used for the mapping specifier in full-text indexes.
     */
    public static class AnnotatedField implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String fieldName;

        private final String annotation;

        public AnnotatedField(String fieldName, String annotation) {
            assert(fieldName != null);
            this.fieldName = fieldName;
            this.annotation = annotation;
        }

        /**
         * The name of the indexed field.
         */
        public String getFieldName() {
            return fieldName;
        }

        /**
         *  The field's annotation.  In Text indexes, this is the ES mapping
         *  specification, which is a JSON string and may be null.
         */
        public String getAnnotation() {
            return annotation;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            AnnotatedField other = (AnnotatedField) obj;

            if (! fieldName.equals(other.fieldName)) {
                return false;
            }

            return (annotation == null ?
                    other.annotation == null :
                    annotation.equals(other.annotation));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + fieldName.hashCode();
            if (annotation != null) {
                result = prime * result + annotation.hashCode();
            }
            return result;
        }
    }

    @Override
    public String getAnnotationForField(String fieldName) {
        if (isTextIndex() == false) {
            return null;
        }
        return annotations.get(fieldName);
    }
	
    public RowImpl deserializeRow(byte[] keyBytes, byte[] valueBytes) {
        TableImpl topTable = table.getTopLevelTable(); 
        return topTable.createRowFromBytes(keyBytes, valueBytes, false);
    }
}
