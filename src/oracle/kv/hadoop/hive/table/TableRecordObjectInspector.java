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
import java.util.Arrays;
import java.util.List;

import oracle.kv.table.FieldValue;
import oracle.kv.table.RecordValue;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;

/**
 * The Hive ObjectInspector that is used to translate KVStore row fields
 * of type FieldDef.Type.RECORD to Hive column type STRUCT.
 */
public class TableRecordObjectInspector extends StandardStructObjectInspector {

    /*
     * Note: with respect to the 'get' methods defined by this ObjectInspector,
     * when the Hive infrastructure invokes those methods during a given
     * query, the data Object input to those methods may be an instance of
     * FieldValue (specifically, a RecordValue) or may be an instance of the
     * corresponding Java class (that is, a List or Object[] representing a
     * Hive STRUCT type). As a result, each such method must be prepared to
     * handle both cases.
     *
     * With respect to the 'create/set' methods, this class defaults to the
     * 'create/set' methods provided by the StandardStructObjectInspector
     * parent class; which always return a Java List or Object[] (representing
     * a Hive STRUCT type), instead of a RecordValue.
     *
     * Finally, note that the logger used in this class is inherited from
     * the parent class.
     */

    TableRecordObjectInspector() {
        super();
    }

    TableRecordObjectInspector(
        List<String> structFieldNames,
        List<ObjectInspector> structFieldObjectInspectors) {

        super(structFieldNames, structFieldObjectInspectors);
    }

    TableRecordObjectInspector(
        List<String> structFieldNames,
        List<ObjectInspector> structFieldObjectInspectors,
        List<String> structFieldComments) {

        super(
           structFieldNames, structFieldObjectInspectors, structFieldComments);
    }

    @Override
    public StructField getStructFieldRef(String fieldName) {

        if (fieldName == null) {
            return null;
        }
        return super.getStructFieldRef(fieldName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getStructFieldData(Object data, StructField fieldRef) {

        if (data == null || fieldRef == null) {
            return null;
        }

        final MyField f = (MyField) fieldRef;
        final int fieldID = f.getFieldID();
        assert (fieldID >= 0 && fieldID < fields.size());

        if (data instanceof FieldValue) {

            final RecordValue recordValue = ((FieldValue) data).asRecord();
            final int nFields = recordValue.size();

            if (fields.size() != nFields) {
                LOG.warn("Trying to access " + fields.size() +
                         " fields inside a RECORD of " + nFields +
                         " elements: " + recordValue.getFields());
            }

            if (fieldID >= nFields) {
                return null;
            }

            final List<String> recFields = recordValue.getFields();
            int i = 0;
            for (String recFieldName : recFields) {
                if (i == fieldID) {
                    return recordValue.get(recFieldName);
                }
                i++;
            }

        } else if (data instanceof List) {

            final List<Object> list = (List<Object>) data;
            final int nFields = list.size();
            if (fields.size() != nFields) {
                LOG.warn("Trying to access " + fields.size() +
                         " fields inside a LIST of " + nFields +
                         " elements: " + list);
            }

            if (fieldID >= nFields) {
                return null;
            }
            return list.get(fieldID);

        } else {

            final Object[] arr = (Object[]) data;
            final int nFields = arr.length;
            if (fields.size() != nFields) {
                LOG.warn("Trying to access " + fields.size() +
                         " fields inside an ARRAY of " + nFields +
                         " elements: " + Arrays.asList(arr));
            }

            if (fieldID >= nFields) {
                return null;
            }
            return arr[fieldID];
        }
        throw new IllegalArgumentException(
                      "invalid input object: must be Object[], " +
                      "List, or RecordValue");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getStructFieldsDataAsList(Object data) {

        if (data == null) {
            return null;
        }

        if (data instanceof FieldValue) {
            final RecordValue recordValue = ((FieldValue) data).asRecord();
            assert (recordValue.size() == fields.size());
            final List<Object> list = new ArrayList<Object>();
            for (String recFieldName : recordValue.getFields()) {
                list.add(recordValue.get(recFieldName));
            }
            return list;
        } else if (data instanceof List) {
            final List<Object> list = (List<Object>) data;
            assert (list.size() == fields.size());
            return list;
        } else {
            final Object[] arr = (Object[]) data;
            assert (arr.length == fields.size());
            return Arrays.asList(arr);
        }
    }
}
