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

import java.util.Arrays;
import java.util.List;

import oracle.kv.table.ArrayValue;
import oracle.kv.table.FieldValue;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;

/**
 * The Hive ObjectInspector that is used to translate KVStore row fields
 * of type FieldDef.Type.ARRAY to Hive column type LIST.
 */
public class TableArrayObjectInspector extends StandardListObjectInspector {

    /*
     * Note: with respect to the 'get' methods defined by this ObjectInspector,
     * when the Hive infrastructure invokes those methods during a given
     * query, the data Object input to those methods may be an instance of
     * FieldValue (specifically, an ArrayValue) or may be an instance of the
     * corresponding Java class (that is, a List or Object[] representing a
     * Hive LIST type). As a result, each such method must be prepared to
     * handle both cases.
     *
     * With respect to the 'create/set' methods, this class defaults to
     * the 'create/set' methods provided by the StandardListObjectInspector
     * parent class; which always return a Java List or Object[] (representing
     * a Hive LIST type), instead of an ArrayValue.
     */

    TableArrayObjectInspector(ObjectInspector listElementObjectInspector) {

        super(listElementObjectInspector);
    }

    @Override
    public Object getListElement(Object data, int index) {

        if (data == null || index < 0) {
            return null;
        }

        if (data instanceof FieldValue) {
            final ArrayValue arrayValue = ((FieldValue) data).asArray();
            if (arrayValue.size() >= index) {
                return null;
            }
            return arrayValue.get(index);
        } else if (data instanceof List) {
            final List<?> list = (List<?>) data;
            if (index >= list.size()) {
                return null;
            }
            return list.get(index);
        } else {
            final Object[] arr = (Object[]) data;
            if (index >= arr.length) {
                return null;
            }
            return arr[index];
        }
    }

    @Override
    public int getListLength(Object data) {

        if (data == null) {
            return -1;
        }

        if (data instanceof FieldValue) {
            final ArrayValue arrayValue = ((FieldValue) data).asArray();
            return arrayValue.size();
        } else if (data instanceof List) {
            final List<?> list = (List<?>) data;
            return list.size();
        } else {
            final Object[] arr = (Object[]) data;
            return arr.length;
        }
    }

    @Override
    public List<?> getList(Object data) {

        if (data == null) {
            return null;
        }

        if (data instanceof FieldValue) {
            final ArrayValue arrayValue = ((FieldValue) data).asArray();
            return arrayValue.toList();
        } else if (data instanceof List) {
            return (List<?>) data;
        } else {
            return Arrays.asList((Object[]) data);
        }
    }
}
