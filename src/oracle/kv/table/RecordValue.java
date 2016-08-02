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

package oracle.kv.table;

import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * RecordValue extends {@link FieldValue} to represent a multi-valued object
 * that contains a map of string names to fields.  The field values may be
 * simple or complex and allowed fields are defined by the FieldDef definition
 * of the record.
 *
 * @since 3.0
 */
public interface RecordValue extends FieldValue {

    /**
     * Returns the RecordDef that defines the content of this record.
     *
     * @return the RecordDef
     */
    RecordDef getDefinition();

    /**
     * Returns an unmodifiable list of fields that are defined for this
     * record, in definition order.  This list does not depend on the actual
     * values present in this record, and is never empty. Values of the fields,
     * if they are present in this instance, can be obtained using {@link
     * #get}.
     *
     * @return the fields
     *
     * @since 3.0.6
     */
    List<String> getFields();

    /**
     * Returns the value of the named field.
     *
     * @param fieldName the name of the desired field
     *
     * @return the field value if it is available, null if it has not
     * been set
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object
     */
    FieldValue get(String fieldName);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, int value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, long value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, String value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, double value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, float value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, boolean value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, byte[] value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue putFixed(String fieldName, byte[] value);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue putEnum(String fieldName, String value);

    /**
     * Put a null value in the named field, silently overwriting
     * existing values.
     *
     * @param fieldName name of the desired field
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue putNull(String fieldName);

    /**
     * Set the named field, silently overwriting existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param value the value to set
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the type of the field does not match the
     * input type
     */
    RecordValue put(String fieldName, FieldValue value);

    /**
     * Set a RecordValue field, silently overwriting existing values.
     * The returned object is empty of fields and must be further set by the
     * caller.
     *
     * @param fieldName name of the desired field
     *
     * @return an empty instance of RecordValue
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     */
    RecordValue putRecord(String fieldName);

    /**
     * Set a RecordValue field based on map input, silently overwriting
     * existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param map to create value of the desired RecordValue field
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if the map value type doesn't match the
     * field definition of the named field.
     */
    RecordValue putRecord(String fieldName, Map<String, ?> map);

    /**
     * Set a RecordValue field, silently overwriting existing values.
     * The created RecordValue is based on JSON string input. If the
     * {@code exact} parameter is true, the input string must contain an exact
     * match to the Record field definition, including all fields. It must not
     * have additional data. If false, only matching fields will be added and
     * the input may have additional, unrelated data.
     *
     * @param fieldName name of the desired field
     *
     * @param jsonInput a JSON string
     *
     * @param exact set to true for an exact match.  See above
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the jsonInput is malformed
     */
    RecordValue putRecordAsJson(String fieldName,
                                String jsonInput,
                                boolean exact);

    /**
     * Set a RecordValue field, silently overwriting existing values.
     * The created RecordValue is based on JSON stream input. If the
     * {@code exact} parameter is true, the input string must contain an exact
     * match to the Record field definition, including all fields. It must not
     * have additional data. If false, only matching fields will be added and
     * the input may have additional, unrelated data.
     *
     * @param fieldName name of the desired field
     *
     * @param jsonInput a JSON stream input
     *
     * @param exact set to true for an exact match.  See above
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the jsonInput is malformed
     */
    RecordValue putRecordAsJson(String fieldName,
                                InputStream jsonInput,
                                boolean exact);

    /**
     * Set an ArrayValue field, silently overwriting existing values.
     * The returned object is empty of fields and must be further set by the
     * caller.
     *
     * @param fieldName name of the desired field
     *
     * @return an empty instance of ArrayValue
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     */
    ArrayValue putArray(String fieldName);

    /**
     * Set an ArrayValue field based on list input, silently overwriting
     * existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param list to create value of the desired ArrayValue field
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if the list type doesn't match the
     * field definition of the named field.
     */
    RecordValue putArray(String fieldName, Iterable<?> list);

    /**
     * Set an ArrayValue field based on array input, silently overwriting
     * existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param array to create value of the desired ArrayValue field
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if the array type doesn't match the
     * field definition of the named field.
     */
    RecordValue putArray(String fieldName, Object[] array);

    /**
     * Set a ArrayValue field, silently overwriting existing values.
     * The created ArrayValue is based on JSON string input. If the
     * {@code exact} parameter is true, the input string must contain an exact
     * match to all the nested Record definition in Array field, including all
     * fields. It must not have additional data. If false, only matching fields
     * will be added and the input may have additional, unrelated data.
     *
     * @param fieldName name of the desired field
     *
     * @param jsonInput a JSON string
     *
     * @param exact set to true for an exact match.  See above
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the jsonInput is malformed
     */
    RecordValue putArrayAsJson(String fieldName,
                               String jsonInput,
                               boolean exact);

    /**
     * Set a ArrayValue field, silently overwriting existing values.
     * The created ArrayValue is based on JSON stream input. If the
     * {@code exact} parameter is true, the input string must contain an exact
     * match to all the nested Record definition in Array field, including all
     * fields. It must not have additional data. If false, only matching fields
     * will be added and the input may have additional, unrelated data.
     *
     * @param fieldName name of the desired field
     *
     * @param jsonInput a JSON stream input
     *
     * @param exact set to true for an exact match.  See above
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the jsonInput is malformed
     */
    RecordValue putArrayAsJson(String fieldName,
                               InputStream jsonInput,
                               boolean exact);

    /**
     * Set a MapValue field, silently overwriting existing values.
     * The returned object is empty of fields and must be further set by the
     * caller.
     *
     * @param fieldName name of the desired field
     *
     * @return an empty instance of MapValue
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     */
    MapValue putMap(String fieldName);

    /**
     * Set a MapValue field based on map input, silently overwriting
     * existing values.
     *
     * @param fieldName name of the desired field
     *
     * @param map to create value of the desired MapValue field
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if the map value type doesn't match the
     * field definition of the named field.
     */
    RecordValue putMap(String fieldName, Map<String, ?> map);

    /**
     * Set a MapValue field, silently overwriting existing values.
     * The created MapValue is based on JSON string input. If the {@code exact}
     * parameter is true, the input string must contain an exact match to all
     * the nested Record definition in Map field, including all fields. It
     * must not have additional data. If false, only matching fields will be
     * added and the input may have additional, unrelated data.
     *
     * @param fieldName name of the desired field
     *
     * @param jsonInput a JSON string
     *
     * @param exact set to true for an exact match.  See above
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the jsonInput is malformed
     */
    RecordValue putMapAsJson(String fieldName,
                             String jsonInput,
                             boolean exact);

    /**
     * Set a MapValue field, silently overwriting existing values.
     * The created MapValue is based on JSON stream input. If the {@code exact}
     * parameter is true, the input string must contain an exact match to all
     * the nested Record definition in Map field, including all fields. It
     * must not have additional data. If false, only matching fields will be
     * added and the input may have additional, unrelated data.
     *
     * @param fieldName name of the desired field
     *
     * @param jsonInput a JSON stream input
     *
     * @param exact set to true for an exact match.  See above
     *
     * @return this
     *
     * @throws IllegalArgumentException if the named field does not exist in
     * the definition of the object or the definition of the field does not
     * match the input definition
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the jsonInput is malformed
     */
    RecordValue putMapAsJson(String fieldName,
                             InputStream jsonInput,
                             boolean exact);

    /**
     * Returns the number of fields in the record.  Only top-level fields are
     * counted.
     *
     * @return the number of fields
     */
    int size();

    /**
     * Returns true if there are no fields in the record, false otherwise.
     *
     * @return true if there are no fields in the record, false otherwise
     */
    boolean isEmpty();

    /**
     * Remove the named field if it exists.
     *
     * @param fieldName the name of the field to remove
     *
     * @return the FieldValue if it existed, otherwise null
     */
    FieldValue remove(String fieldName);

    /**
     * Copies the fields from another RecordValue instance, overwriting
     * fields in this object with the same name.
     *
     * @param source the source RecordValue from which to copy
     *
     * @throws IllegalArgumentException if the {@link RecordDef} of source
     * does not match that of this instance.
     */
    void copyFrom(RecordValue source);

    /**
     * Returns true if the record contains the named field.
     *
     * @param fieldName the name of the field
     *
     * @return true if the field exists in the record, otherwise null
     */
    boolean contains(String fieldName);

    /**
     * Returns a deep copy of this object.
     *
     * @return a deep copy of this object
     */
    @Override
    public RecordValue clone();

    /**
     * Returns a String representation of the value.  The value is returned
     * is a JSON string, and is the same as that returned by
     * {@link FieldValue#toJsonString}.
     *
     * @return a String representation of the value
     */
    @Override
    public String toString();
}
