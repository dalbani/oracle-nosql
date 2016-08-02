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

package oracle.kv.hadoop.hive.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import oracle.kv.table.ArrayDef;
import oracle.kv.table.ArrayValue;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.MapDef;
import oracle.kv.table.MapValue;
import oracle.kv.table.RecordDef;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Row;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe.SerDeParameters;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BinaryObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.BooleanObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.DoubleObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.FloatObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.IntObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.LongObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * Concrete implementation of TableSerDeBase that performs deserialization
 * and/or serialization of data loaded into a KVStore via the PrimaryKey
 * based Table API.
 */
public class TableSerDe extends TableSerDeBase {

    private static final String thisClassName = TableSerDe.class.getName();

    private static final Log LOG = LogFactory.getLog(thisClassName);

    /* Implementation-specific methods required by the parent class. */

    /**
     * Verifies that the names and types of the fields in the KV Store
     * table correctly map to the names and types of the Hive table
     * against which the Hive query is to be executed. Note that this
     * method assumes that both the KVStore parameters and the serde
     * parameters have been initialized. If a mismatch is found between
     * KV Store fields and Hive columns, then a SerDeException will be
     * thrown with a descriptive message.
     */
    @Override
    protected void validateParams(Properties tbl) throws SerDeException {

        /* Get KV field names and types for validation. */
        LOG.debug("KV Store Table Name = " + getKvTableName());

        final List<String> fieldNames = getKvFieldNames();
        if (fieldNames == null || fieldNames.size() == 0) {
            final String msg =
                "No fields defined in KV Store table [name=" +
                getKvTableName() + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }
        LOG.debug("KV Store Field Names = " + fieldNames);

        final List<FieldDef.Type> fieldTypes = getKvFieldTypes();
        if (fieldTypes == null || fieldTypes.size() == 0) {
            final String msg =
                "No types defined for fields in KV Store table [name=" +
                getKvTableName() + ", fields=" + fieldNames + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }
        LOG.debug("KV Store Field Types = " + fieldTypes);

        /* Get Hive column names and types for validation. */
        LOG.debug("HIVE Table Name = " + getHiveTableName());

        final SerDeParameters params = getSerdeParams();
        if (params == null) {
            final String msg =
                "No SerDeParameters specified for Hive table [name=" +
                getHiveTableName() + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }

        final List<String> columnNames = params.getColumnNames();
        if (columnNames == null || columnNames.size() == 0) {
            final String msg =
                "No columns defined in Hive table [name=" +
                getHiveTableName() + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }
        LOG.debug("HIVE Column Names = " + columnNames);

        final List<TypeInfo> columnTypes = params.getColumnTypes();
        if (columnTypes == null || columnTypes.size() == 0) {
            final String msg =
                "No types defined for columns in Hive table [name=" +
                getHiveTableName() + ", columns=" + columnNames + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }
        LOG.debug("HIVE Column Types = " + columnTypes);

        /* Number of KV field names must equal number of KV field types. */
        if (fieldNames.size() != fieldTypes.size()) {
            final String msg =
                "For the KV Store table [name=" + getKvTableName() + "], " +
                "the number of field names [" + fieldNames.size() +
                "] != number of field types [" + fieldTypes.size() + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }

        /* Number of Hive column names must equal number of column types. */
        if (columnNames.size() != columnTypes.size()) {
            final String msg =
                "For the created Hive table [name=" + getHiveTableName() +
                "], the number of column names [" + columnNames.size() +
                "] != number of column types [" + columnTypes.size() + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }

        /* Number of KV fields must equal the number of Hive columns. */
        if (fieldNames.size() != columnNames.size()) {
            final String msg =
                "Number of fields [" + fieldNames.size() + "] in the " +
                "KV Store table [name=" + getKvTableName() + "] != " +
                "number of columns [" + columnNames.size() +
                "] specified for the created Hive table [name=" +
                getHiveTableName() + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }

        /* KV field names must equal Hive column names (case insensitive). */
        /*
         * The KV table's top-level field names must match the Hive table's
         * top-level field names.
         *
         * -- NOTE --
         *
         * KV field names (and table and index names), are case INSENSITIVE,
         * but case PRESERVING. For example, if a KV table is created with
         * a field named "MY_TABLE", that field can be referenced using the
         * String "my_table" (or "mY_tAbLE", or "MY_table", etc.); because
         * of case insensitivity. But when the field name is retrieved
         * (via FieldDef.getFields() for example), the name returned for that
         * field will always be "MY_TABLE"; preserving the case used when
         * the field was originally created. Compare this with how Hive
         * handles case in the column names of a Hive table (which correspond
         * to the KV table's top-level fields).
         *
         * When the names of the columns of a Hive table are retrieved using
         * SerDeParameters.getColumnNames(), the names are always returned in
         * LOWER CASE; even if upper case or mixed case was used in the Hive
         * 'CREATE TABLE' command. Because of this, when validating the
         * top-level field names of the KV table against the Hive table's
         * column names, the field names of the KV table are first converted
         * to lower case below.
         */
        for (String fieldName : fieldNames) {
            if (!columnNames.contains(fieldName.toLowerCase())) {
                final String msg =
                    "Field names from the KV Store table [name=" +
                    getKvTableName() + "] does not match the column names " +
                    "from the created Hive table [name=" + getHiveTableName() +
                    "] - " + fieldNames + " != " + columnNames;
                LOG.error(msg);
                throw new SerDeException(new IllegalArgumentException(msg));
            }
        }

        /* KV field types must match Hive column types. */
        final List<FieldDef> fieldDefs = getKvFieldDefs();
        for (int i = 0; i < fieldDefs.size(); i++) {
            if (!TableFieldTypeEnum.kvHiveTypesMatch(
                     fieldDefs.get(i), columnTypes.get(i))) {
                final String msg =
                    "Field types from the KV Store table [name=" +
                    getKvTableName() + "] do not match the column types " +
                    "from the created Hive table [name=" + getHiveTableName() +
                    "] - " + fieldTypes + " != " + columnTypes;
                LOG.error(msg);
                throw new SerDeException(new IllegalArgumentException(msg));
            }
        }
    }

    @Override
    protected ObjectInspector createObjectInspector() throws SerDeException {

        final List<ObjectInspector> fieldObjInspectors =
            new ArrayList<ObjectInspector>();

        final List<String> hiveColumnNames = getSerdeParams().getColumnNames();

        final List<FieldDef> fieldDefs = getKvFieldDefs();
        for (FieldDef fieldDef : fieldDefs) {
            fieldObjInspectors.add(objectInspector(fieldDef));
        }
        return ObjectInspectorFactory.getStandardStructObjectInspector(
                   hiveColumnNames, fieldObjInspectors);
    }

    @Override
    public Object deserialize(Writable field) throws SerDeException {
        hiveRow.clear();
        final Row kvRow = getKvTable().createRowFromJson(
                                           field.toString(), true);
        LOG.debug("kvRow = " + kvRow);

        /*
         * For each FieldValue of the given Row, return the corresponding
         * "Hive friendly" value; for example, return the Java primitive
         * value referenced by the given FieldValue.
         */
        for (String fieldName : kvRow.getFields()) {

            LOG.debug("fieldName = " + fieldName);

            final FieldValue fieldValue = kvRow.get(fieldName);
            final ObjectInspector oi = objectInspector(fieldValue);

            if (oi instanceof StringObjectInspector) {

                hiveRow.add(
                    ((StringObjectInspector) oi).getPrimitiveJavaObject(
                                                     fieldValue));

            } else if (oi instanceof BinaryObjectInspector) {

                hiveRow.add(
                    ((BinaryObjectInspector) oi).getPrimitiveJavaObject(
                                                     fieldValue));

            } else if (oi instanceof BooleanObjectInspector) {

                hiveRow.add(((BooleanObjectInspector) oi).get(fieldValue));

            } else if (oi instanceof DoubleObjectInspector) {

                hiveRow.add(((DoubleObjectInspector) oi).get(fieldValue));

            } else if (oi instanceof FloatObjectInspector) {

                hiveRow.add(((FloatObjectInspector) oi).get(fieldValue));

            } else if (oi instanceof IntObjectInspector) {

                hiveRow.add(((IntObjectInspector) oi).get(fieldValue));

            } else if (oi instanceof LongObjectInspector) {

                hiveRow.add(((LongObjectInspector) oi).get(fieldValue));

            } else if (oi instanceof TableEnumObjectInspector) {

                hiveRow.add(((TableEnumObjectInspector) oi).
                            getPrimitiveJavaObject(fieldValue));

            } else if (oi instanceof ListObjectInspector) {

                hiveRow.add(((ListObjectInspector) oi).getList(fieldValue));

            } else if (oi instanceof MapObjectInspector) {

                hiveRow.add(((TableMapObjectInspector) oi).getMap(fieldValue));

            } else if (oi instanceof StructObjectInspector) {

                hiveRow.add(((StructObjectInspector) oi).
                            getStructFieldsDataAsList(fieldValue));
            }

        }
        return hiveRow;
    }

    @Override
    public Writable serialize(Object obj, ObjectInspector objectInspector)
                        throws SerDeException {

        LOG.debug("obj = " + obj + ", objectInspector = " +
                  objectInspector.getClass().getSimpleName());

        final StructObjectInspector structInspector =
            (StructObjectInspector) objectInspector;

        final List<? extends StructField> structFields =
            structInspector.getAllStructFieldRefs();

        final List<String> hiveColumnNames = getSerdeParams().getColumnNames();

        if (structFields.size() != hiveColumnNames.size()) {
            final String msg =
                "Number of Hive columns to serialize " + structFields.size() +
                "] does not equal number of columns [" +
                hiveColumnNames.size() + "] specified in the created Hive " +
                "table [name=" + getHiveTableName() + "]";
            LOG.error(msg);
            throw new SerDeException(new IllegalArgumentException(msg));
        }

        kvMapWritable.clear();

        for (int i = 0; i < structFields.size(); i++) {

            final StructField structField = structFields.get(i);
            final String hiveColumnName = hiveColumnNames.get(i);

            if (structField != null) {

                /* Currently assume field is Hive primitive type. */

                final AbstractPrimitiveObjectInspector fieldObjInspector =
                    (AbstractPrimitiveObjectInspector)
                                       structField.getFieldObjectInspector();

                final Object fieldData =
                    structInspector.getStructFieldData(obj, structField);

                Writable fieldValue =
                    (Writable) fieldObjInspector.getPrimitiveWritableObject(
                                                     fieldData);
                if (fieldValue == null) {
                    if (PrimitiveCategory.STRING.equals(
                            fieldObjInspector.getPrimitiveCategory())) {

                        fieldValue = NullWritable.get();
                    } else {
                        fieldValue = new IntWritable(0);
                    }
                }

                kvMapWritable.put(new Text(hiveColumnName), fieldValue);
            }
        }
        return kvMapWritable;
    }

    /**
     * Creates and returns the appropriate ObjectInspector based on the
     * given FieldValue. Supports deserialization; when only a FieldValue,
     * not a FieldDef is available. Note: not all FieldValue types provide
     * a getDefinition method.
     */
    private ObjectInspector objectInspector(final FieldValue fieldValue) {

        if (fieldValue == null || fieldValue.isNull()) {
            return null;
        }

        final FieldDef.Type fieldType = fieldValue.getType();

        LOG.debug("fieldValue = " + fieldValue + ", fieldType = " + fieldType);

        switch (fieldType) {
        case STRING:
            return PrimitiveObjectInspectorFactory.javaStringObjectInspector;

        case BOOLEAN:
            return new TableBooleanObjectInspector();

        case INTEGER:
            return new TableIntObjectInspector();

        case LONG:
            return new TableLongObjectInspector();

        case FLOAT:
            return new TableFloatObjectInspector();

        case DOUBLE:
            return new TableDoubleObjectInspector();

        case BINARY:
        case FIXED_BINARY:
            return new TableBinaryObjectInspector();

        case ENUM:
            return new TableEnumObjectInspector();

        case MAP:

            final Collection<FieldValue> mapValues =
                (((MapValue) fieldValue).getFields()).values();
            final FieldValue anyMapValue = mapValues.iterator().next();
            return new TableMapObjectInspector(
                    PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                    objectInspector(anyMapValue));

        case ARRAY:

            final FieldValue firstElementValue =
                ((ArrayValue) fieldValue).get(0);
            final ObjectInspector listElementObjectInspector =
                                      objectInspector(firstElementValue);
            return new TableArrayObjectInspector(listElementObjectInspector);

        case RECORD:

            final List<String> structFieldNames =
                ((RecordValue) fieldValue).getFields();

            final List<ObjectInspector> structFieldObjectInspectors =
                new ArrayList<ObjectInspector>();

            for (String structFieldName : structFieldNames) {

                final FieldValue structFieldValue =
                    ((RecordValue) fieldValue).get(structFieldName);
                structFieldObjectInspectors.add(
                    objectInspector(structFieldValue));
            }
            return new TableRecordObjectInspector(
                            structFieldNames, structFieldObjectInspectors);

        default:
            return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        }

    }

    /**
     * Creates and returns the appropriate ObjectInspector based on the
     * given FieldDef. Used on the frontend, by the creatObjectInspector
     * method, during initialization; when all FieldDefs are available.
     */
    private ObjectInspector objectInspector(FieldDef fieldDef) {

        if (fieldDef == null) {
            return null;
        }

        final FieldDef.Type fieldType = fieldDef.getType();

        LOG.debug("fieldDef = " + fieldDef.getClass().getSimpleName() +
                  ", fieldType = " + fieldType);

        switch(fieldType) {
        case STRING:
            return PrimitiveObjectInspectorFactory.javaStringObjectInspector;

        case BOOLEAN:
            return new TableBooleanObjectInspector();

        case INTEGER:
            return new TableIntObjectInspector();

        case LONG:
            return new TableLongObjectInspector();

        case FLOAT:
            return new TableFloatObjectInspector();

        case DOUBLE:
            return new TableDoubleObjectInspector();

        case BINARY:
        case FIXED_BINARY:
            return new TableBinaryObjectInspector();

        case ENUM:
            return new TableEnumObjectInspector();

        case MAP:

            final FieldDef mapElementDef = ((MapDef) fieldDef).getElement();
            return new TableMapObjectInspector(
                    PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                    objectInspector(mapElementDef));

        case ARRAY:

            final ArrayDef arrayDef = (ArrayDef) fieldDef;
            final FieldDef arrayElementDef = arrayDef.getElement();

            final ObjectInspector listElementObjectInspector =
                                      objectInspector(arrayElementDef);
            return new TableArrayObjectInspector(listElementObjectInspector);

        case RECORD:

            final RecordDef recordDef = (RecordDef) fieldDef;
            final List<String> structFieldNames = recordDef.getFields();

            final List<ObjectInspector> structFieldObjectInspectors =
                new ArrayList<ObjectInspector>();

            for (String structFieldName : structFieldNames) {

                final FieldDef structFieldDef =
                    recordDef.getField(structFieldName);
                structFieldObjectInspectors.add(
                    objectInspector(structFieldDef));
            }
            return new TableRecordObjectInspector(
                           structFieldNames, structFieldObjectInspectors);

        default:
           return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        }
    }
}
