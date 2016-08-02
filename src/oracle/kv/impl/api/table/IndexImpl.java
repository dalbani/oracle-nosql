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

import static oracle.kv.impl.api.table.TableImpl.ANONYMOUS;
import static oracle.kv.impl.api.table.TableImpl.KEY_TAG;
import static oracle.kv.impl.api.table.TableImpl.SEPARATOR;
import static oracle.kv.impl.api.table.TableJsonUtils.DESC;
import static oracle.kv.impl.api.table.TableJsonUtils.FIELDS;
import static oracle.kv.impl.api.table.TableJsonUtils.NAME;
import static oracle.kv.impl.api.table.TableJsonUtils.TYPE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.JsonNodeFactory;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.util.JsonUtils;
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
 *
 * An index can be viewed as a sorted table of N + 1 columns. Each of the first
 * N columns has an indexable atomic type (one of the numeric types or string
 * or enum). The last column stores serialized primary keys "pointing" to rows
 * in the undelying base table.
 *
 * The rows of the index are computed as follows:
 *
 * - Each index column C (other than the last one) is associated with a path
 *   expr Pc, which when evaluated on a base-table row R produces one or more
 *   indexable atomic values. Let Pc(R) be the *set* of values produced by Pc
 *   on a row R (Pc(R) may produce duplicate values, but the duplicates do not
 *   participate in index creation). If Pc is a path expr that may produce
 *   multiple values from a row, we say that C is a "multi-key" column, and
 *   the whole index is a "multi-key" index.
 *
 * - Each Pc may have at most one step, call it MK, that may produce multiple
 *   values. MK is a [] or _key step whose input is an array or map value from
 *   a row. We denote with MK-Pc the path expr that contains the steps from the
 *   start of Pc up to (and including) MK, and with R-Pc the remaining steps in
 *   Pc.
 *
 * - An index may contain more than one multi-key column, but the path exprs
 *   for all of these columns must all have the same MK-Pc.
 *
 * - Then, conceptually, the index rows are computed by a query like this:
 *
 *   select a.Pc1 as C1, c.R-Pc2 as C2, c.R-Pc3 as C3, primary_key(a) as PK
 *   from A as a, a.MK-Pc as c
 *   order by a.Pc1, c.R-Pc2, c.R-Pc3
 *
 *   In the above query, we assumed the index has 4 columns (N = 3), two of
 *   which (C2 and C3) are multi-key columns sharing the MK-Pc path. If there
 *   are no multi-key columns, the query is simpler:
 *
 *   select a.Pc1 as C1, a.Pc2 as C2, a.Pc3 as C3, primary_key(a) as PK
 *   from A as a,
 *   order by a.Pc1, a.Pc2, a.Pc3
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
     * The stringified path exprs that define the index columns. In the case of
     * map indexes a path expr may contain the special strings TableImpl.KEY_TAG
     * ("_key") and TableImpl.ANONYMOUS ("[]") to distinguish between the 3
     * possible ways of indexing a map: (a) all the keys (using a _key step),
     * (b) all the values (using a [] step), or (c) the value of a specific map
     * entry (using the specific key of the entry we want indexed). In case of
     * array indexes, the "[]" could be used optionally, but we have decided not
     * to allow it, so that there is a single representation of array indexes
     * (there is only one way of indexing arrays).
     */
    private final List<String> fields;

    /* status is used when an index is being populated to indicate readiness */
    private IndexStatus status;

    /*
     * transient version of the index column definitions, materialized as
     * IndexField for efficiency. It is technically final but is not because it
     * needs to be initialized in readObject after deserialization.
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

    /*
     * properties of the index; used by text indexes only; can be null.
     */
    private Map<String, String> properties;

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
    	this(name, table, fields, null, null, description);
    }

    /* Constructor for Full Text Indexes. */
    public IndexImpl(String name, TableImpl table,
                     List<String> fields,
                     Map<String, String> annotations,
                     Map<String, String> properties,
                     String description) {
    	this.name = name;
    	this.table = table;
    	this.fields = translateFields(fields);
    	this.annotations = annotations;
    	this.properties = properties;
    	this.description = description;
    	status = IndexStatus.TRANSIENT;

    	/* validate initializes indexFields as well as isMultiKeyMapIndex */
    	validate();
    	assert indexFields != null;
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

    /**
     * Returns true if this index indexes all the entries (both keys and
     * associated data) of a map, with the key field appearing before any
     * of the data fields. This is info needed by the query optimizer.
     */
    public boolean isMapBothIndex() {

        List<IndexField> ipaths = getIndexFields();
        boolean haveMapKey = false;
        boolean haveMapValue = false;

        if (!isMultiKeyMapIndex) {
            return false;
        }

        for (IndexField ipath : ipaths) {

            if (ipath.isMapKey()) {
                haveMapKey = true;
                if (haveMapValue) {
                    return false;
                }
            } else if (ipath.isMapValue()) {
                haveMapValue = true;
                if (haveMapKey) {
                    break;
                }
            }
        }

        return (haveMapKey && haveMapValue);
    }

    @Override
    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public IndexField getIndexPath(int i) {
        return indexFields.get(i);
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
        if (isTextIndex()) {
            return Collections.unmodifiableMap(annotations);
        }
        return Collections.emptyMap();
    }

    Map<String, String> getAnnotationsInternal() {
    	return annotations;
    }

    public Map<String, String> getProperties() {
        if (properties != null) {
            return properties;
        }
        return Collections.emptyMap();
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
        populateIndexRecord(ikey, (RecordValueImpl) value);
        return ikey;
    }

    /**
     * Create index keys.
     *
     * If the index contains multiKey field like array, keyof(map) and
     * elementof(map), it returns multiple index keys. Otherwise, the returned
     * index key array contains a single index key.
     *
     * Skip the index key if the any of key field value is null, returns null if
     * there is no valid index key returned.
     */
    public IndexKey[] createMultiIndexKeys(RecordValue value) {

        final List<IndexKey> list = new ArrayList<IndexKey>();
        final RecordValueImpl recVal = ((RecordValueImpl)value);
        final Map<IndexField, FieldValue> singleKeyFieldValues =
            new HashMap<IndexField, FieldValue>();
        final List<IndexField> multiKeyFields = new ArrayList<IndexField>();
        IndexField multiKeyField = null;

        for (IndexField field: getIndexFields()) {
            if (field.isMultiKey()) {
                /* Only one multi-key field is allowed for an index. */
                if (multiKeyField == null) {
                    multiKeyField = field.getMultiKeyField();
                }
                multiKeyFields.add(field);
            } else {
                final FieldValue fval = recVal.getComplex(field);
                if (fval == null) {
                    /*
                     * Index key dones't contain null value, so no valid
                     * index keys returned.
                     */
                    return null;
                }
                singleKeyFieldValues.put(field, fval);
            }
        }

        if (!multiKeyFields.isEmpty()) {

            /* Index on multiKey fields */
            final FieldValue multiKeyValue =
                ((RecordValueImpl)value).getComplex(multiKeyField);
            if (multiKeyValue.isNull()) {
                return null;
            }

            if (multiKeyValue.isArray()) {
                /* The multiKey field is a array */
                final int size = multiKeyValue.asArray().size();
                for (int i = 0; i < size; i++) {
                    final IndexKeyImpl idxKey = new IndexKeyImpl(this);
                    boolean hasNullValue = false;
                    for (IndexField field: multiKeyFields) {
                        final String fname = field.getPathName();
                        final FieldValue fval =
                            recVal.findFieldValue(field.iterator(), i);
                        if (fval == null || fval.isNull()) {
                            hasNullValue = true;
                            break;
                        }
                        idxKey.putComplex(fname, fval);
                    }
                    /* Skip the index key if its field value is null */
                    if (!hasNullValue) {
                        list.add(idxKey);
                    }
                }
            } else {
                /* The multiKey field is a keyof(map) or elementof(map) */
                assert(multiKeyValue.isMap());

                final Map<String, FieldValue> mapFields =
                    multiKeyValue.asMap().getFields();

                for (Entry<String, FieldValue> entry : mapFields.entrySet()) {
                    final String key = entry.getKey();
                    final IndexKeyImpl idxKey = new IndexKeyImpl(this);
                    boolean hasNullValue = false;
                    for (IndexField field: multiKeyFields) {
                        final String fname = field.getPathName();
                        FieldValue fval;
                        if (field.isMapKey()) {
                            fval = findIndexField(field).createString(key);
                        } else {
                            assert(field.isMapValue());
                            fval = entry.getValue();
                            if (fval.isComplex()) {
                                fval = recVal.findFieldValue(field.iterator(),
                                                             key);
                            }
                        }
                        if (fval == null || fval.isNull()) {
                            hasNullValue = true;
                            break;
                        }
                        idxKey.putComplex(fname, fval);
                    }
                    /* Skip the index key if its field value is null */
                    if (!hasNullValue) {
                        list.add(idxKey);
                    }
                }
            }
            if (list.isEmpty()) {
                return null;
            }
        } else {
            /* Create a index key on single key field */
            list.add(createIndexKey());
        }

        /* Fill in the simple key field values */
        if (!singleKeyFieldValues.isEmpty()) {
            for (Entry<IndexField, FieldValue> entry :
                 singleKeyFieldValues.entrySet()) {
                for (IndexKey idxKey : list) {
                    ((IndexKeyImpl)idxKey).putComplex(entry.getKey(),
                                                      entry.getValue());
                }
            }
        }
        return list.toArray(new IndexKey[list.size()]);
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
        ComplexValueImpl.createFromJson(key, jsonInput, exact);
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
    public FieldRange createFieldRange(String path) {

        IndexField ifield = new IndexField(table, path);

        FieldDef ifieldDef;

        try {
            ifieldDef = validateIndexField(ifield);
        } catch (IllegalCommandException e) {
            throw new IllegalArgumentException(e);
        }

        if (!isIndexField(ifield)) {
            throw new IllegalArgumentException(
                "Field does not exist in index: " + path);
        }

        return new FieldRange(ifield.getPathName(), ifieldDef, 0);
    }

    /**
     * Populates the IndexKey from the record, handling complex values.
     */
    private void populateIndexRecord(IndexKeyImpl indexKey,
                                     RecordValueImpl value) {
        for (IndexField field : getIndexFields()) {
            FieldValueImpl v = value.getComplex(field);
            if (v != null) {
                indexKey.putComplex(field, v);
            }
        }
        indexKey.validate();
    }

    public int numFields() {
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
    	if (! isTextIndex()) {
            for (IndexField field : getIndexFields()) {
                if (field.isMultiKey()) {
                    return true;
                }
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

    public String getFieldPathName(int i) {
        return fields.get(i);
    }

    /**
     * Returns the list of IndexField objects defining the index.  It is
     * transient, and if not yet initialized, initialize it.
     */
    public List<IndexField> getIndexFields() {
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
                validateIndexField(indexField);
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
        node.put(TYPE, getType().toString().toLowerCase());
        if (description != null) {
            node.put(DESC, description);
        }
        if (isMultiKey()) {
            node.put("multi_key", "true");
        }
        ArrayNode fieldArray = node.putArray(FIELDS);
        for (String s : fields) {
            fieldArray.add(TableImpl.translateToExternalField(s));
        }
        if (annotations != null) {
            putMapAsJson(node, "annotations", annotations);
        }
        if (properties != null) {
            putMapAsJson(node, "properties", properties);
        }
    }

    private static void putMapAsJson(ObjectNode node,
                                     String mapName,
                                     Map<String, String> map) {
        ObjectNode mapNode = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            mapNode.put(entry.getKey(), entry.getValue());
        }
        node.put(mapName, mapNode);
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
            validateIndexField(ifield);

            /* Don't restrict number of multi-key fields for text indexes. */
            if (ifield.isMultiKey() && !isTextIndex()) {
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

    /**
     * Validates the given index path expression (ipath) and returns its data
     * type (which must be one of the indexable atomic types).
     *
     * This call has a side effect of setting the multiKey state in the
     * IndexField so that the lookup need not be done twice.
     */
    private FieldDef validateIndexField(IndexField ipath) {

        StringBuilder sb = new StringBuilder();

        boolean addedStep = false;
        List<String> steps = ipath.getSteps();
        int numSteps = steps.size();

        int stepIdx = 0;
        String step = steps.get(stepIdx);
        sb.append(step);
        FieldDef stepDef = ipath.getFirstDef();

        if (stepDef == null) {
            throw new IllegalCommandException(
                "Invalid index field definition : " + ipath + "\n" +
                "There is no field named " + step);
        }

        while (stepIdx < numSteps) {

            /*
             * TODO: Prevent any path through these types from
             * participating in a text index, until the text index
             * implementation supports them correctly.
             */
            if (isTextIndex() &&
                (stepDef.isBinary() ||
                 stepDef.isFixedBinary() || stepDef.isEnum())) {
                    throw new IllegalCommandException
                        ("Invalid index field definition : " + ipath + "\n" +
                         "Fields of type " + stepDef.getType() +
                         " cannot participate in a FULLTEXT index.");
            }

            if (stepDef.isRecord()) {

                ++stepIdx;
                if (stepIdx >= numSteps) {
                    break;
                }

                step = steps.get(stepIdx);
                stepDef = stepDef.asRecord().getField(step);

                if (stepDef == null) {
                    throw new IllegalCommandException(
                        "Invalid index field definition : " + ipath + "\n" +
                        "There is no field named \"" + step + "\" after " +
                        "path " + sb.toString());
                }

                sb.append(SEPARATOR);
                sb.append(step);

            } else if (stepDef.isArray()) {

                if (ipath.isMultiKey()) {
                    throw new IllegalCommandException(
                        "Invalid index field definition : " + ipath + "\n" +
                        "The definition contains more than one multi-key " +
                        "fields. The second mulit-key field is " + step);
                }

                ipath.setMultiKeyPath(sb.toString());

                /*
                 * If there is no next step or the next step is not [], add
                 * a [] step.
                 */
                if (stepIdx + 1 >= numSteps ||
                    !steps.get(stepIdx + 1).equals(TableImpl.ANONYMOUS)) {
                    ipath.add(stepIdx + 1, TableImpl.ANONYMOUS);
                    ++numSteps;
                    addedStep = true;
                }

                /* Consume the [] step */
                ++stepIdx;
                step = TableImpl.ANONYMOUS;
                stepDef = stepDef.asArray().getElement();
                sb.append(SEPARATOR);
                sb.append(step);

            } else if (stepDef.isMap()) {

                ++stepIdx;
                if (stepIdx >= numSteps) {
                    throw new IllegalCommandException(
                        "Invalid index field definition : " + ipath + "\n" +
                        "Indexes on maps must specify keyof, elementof, " +
                        "or a path to a target field");
                }

                step = steps.get(stepIdx);

                if (step.equals(TableImpl.ANONYMOUS)) {

                    if (ipath.isMultiKey()) {
                        throw new IllegalCommandException(
                            "Invalid index field definition : " + ipath + "\n" +
                            "The definition contains more than one multi-key " +
                            "fields. The second mulit-key field is " + step);
                    }

                    ipath.setMultiKeyPath(sb.toString());
                    ipath.setIsMapValue();
                    isMultiKeyMapIndex = true;

                    /* Consume the [] step */
                    stepDef = stepDef.asMap().getElement();
                    sb.append(SEPARATOR);
                    sb.append(step);

                } else if (step.equals(TableImpl.KEY_TAG)) {

                    if (ipath.isMultiKey()) {
                        throw new IllegalCommandException(
                            "Invalid index field definition : " + ipath + "\n" +
                            "The definition contains more than one multi-key " +
                            "fields. The second mulit-key field is " + step);
                    }

                    ipath.setMultiKeyPath(sb.toString());
                    ipath.setIsMapKey();
                    isMultiKeyMapIndex = true;

                    /* Consume the _key step */
                    stepDef = FieldDefImpl.stringDef;
                    sb.append(SEPARATOR);
                    sb.append(step);

                } else {
                    stepDef = stepDef.asMap().getElement();
                    sb.append(SEPARATOR);
                    sb.append(step);
                }

            } else {

                ++stepIdx;
                if (stepIdx >= numSteps) {
                    break;
                }

                step = steps.get(stepIdx);
                throw new IllegalCommandException(
                    "Invalid index field definition : " + ipath + "\n" +
                    "There is no field named \"" + step + "\" after " +
                    "path " + sb.toString());
            }
        }

        if (!stepDef.isValidIndexField()) {
            throw new IllegalCommandException(
                "Invalid index field definition : " + ipath + "\n" +
                "Cannot index values of type " + stepDef);
        }

        if (addedStep) {
            sb = new StringBuilder();
            for(int i = 0; i < steps.size(); ++i) {
                sb.append(steps.get(i).toLowerCase());
                if (i < steps.size() - 1) {
                    sb.append(SEPARATOR);
                }
            }
            ipath.setPathName(sb.toString());
        }

        return stepDef;
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
    byte[] serializeIndexKey(
        RecordValueImpl record,
        boolean allowPartial,
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
                    throw new IllegalStateException(
                        "Index field not found in table: " + field);
                }
                if (!def.isValidIndexField()) {
                    throw new IllegalStateException(
                        "Index field does not have indexable type: " + field);
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
    byte[] serializeIndexKey(
        RecordValueImpl record,
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

                FieldValue val = record.findFieldValue(
                    field.iterator(), keyString);

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
                FieldValue val = record.findFieldValue(mapField.iterator(), -1);
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
        default:
            throw new IllegalStateException
            ("Type not supported in indexes: " +
                    def.getType());
        }
    }

    /**
     * Deserialize an index key into a RecordValue, which may be a Row or an
     * IndexKey.
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
     * @param rec the RecordValue to use. This may be an IndexKeyImpl or a
     * RowImpl. RecordValueImpl is shared between them.
     * @param partialOK true if not all fields must be in the data stream.
     * @param createTableRow Whether rec is a RowImpl or an IndexKeyImpl.
     */
    public void rowFromIndexKey(byte[] data,
                                RecordValueImpl rec,
                                boolean partialOK,
                                boolean createTableRow) {
        TupleInput input = null;

        assert(!createTableRow || rec instanceof RowImpl);
        assert(createTableRow || rec instanceof IndexKeyImpl);

        try {
            input = new TupleInput(data);

            for (IndexField ifield : getIndexFields()) {

                if (input.available() <= 0) {
                    break;
                }

                FieldDefImpl def = findIndexField(ifield);

                if (def == null) {
                    throw new IllegalStateException
                        ("Could not find index field: " + ifield);
                }

                switch (def.getType()) {
                case INTEGER:
                case STRING:
                case LONG:
                case DOUBLE:
                case FLOAT:
                case ENUM:
                    FieldValue val =
                        def.createValue(FieldValueImpl.readTuple(def, input));
                    rec.putComplex(ifield.iterator(), val, createTableRow);
                    break;

                 default:
                    throw new IllegalStateException
                        ("Type not supported in indexes: " + def.getType());
                }
            }

            if (!partialOK && (rec.numValues() != fields.size())) {
                throw new IllegalStateException
                    ("Missing fields from index data for index " +
                     getName() + ", expected " +
                     fields.size() + ", received " + rec.numValues());
            }
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Returns true if the given path is the same as one of the paths that
     * define the index columns.
     */
    public boolean isIndexField(TablePath path) {

        for (IndexField iField : getIndexFields()) {
            if (iField.equals(path)) {
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
                if (indexField.getPathName().contains(fname)) {
                    return true;
                }

            } else {
                if (indexField.getPathName().equals(fname)) {
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
     * by TablePath.
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
    public static class IndexField extends TablePath {

        /* the path to a multi-key field (map or array) */
        private IndexField multiKeyField;

        private MultiKeyType multiKeyType;

        /* ARRAY is not included because no callers need that information */
        private enum MultiKeyType { NONE, MAPKEY, MAPVALUE }

        public IndexField(TableImpl table, String field) {
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

        public boolean isMultiKey() {
            return multiKeyField != null;
        }

        private void setMultiKeyPath(String path) {
            multiKeyField = new IndexField(getFieldMap(), path);
        }

        public boolean isMapKey() {
            return multiKeyType == MultiKeyType.MAPKEY;
        }

        private void setIsMapKey() {
            multiKeyType = MultiKeyType.MAPKEY;
        }

        public boolean isMapValue() {
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
                    JsonUtils.jsonStringsEqual(annotation, other.annotation));
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
        return table.createRowFromBytes(keyBytes, valueBytes, false);
    }
}
