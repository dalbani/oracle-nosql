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

/**
 * Index represents an index on a table in Oracle NoSQL Database.  It is an
 * immutable object created from system metadata.  Index is used to examine
 * index metadata and used as a factory for {@link IndexKey} objects used
 * for IndexKey operations in {@link TableAPI}.
 * <p>
 * Indexes are created and managed using the administrative command line
 * interface.
 *
 * @since 3.0
 */
public interface Index {

    /**
     * Returns the Table on which the index is defined.
     *
     * @return the table
     */
    Table getTable();

    /**
     * Returns the name of the index.
     *
     * @return the index name
     */
    String getName();

    /**
     * Returns an unmodifiable list of the field names that define the index.
     * These are in order of declaration which is significant.
     *
     * @return the field names
     */
    List<String> getFields();

    /**
     * Gets the index's description if present, otherwise null.  This is a
     * description of the index that is optionally supplied during
     * definition of the index.
     *
     * @return the description or null
     */
    String getDescription();

    /**
     * Returns the index's IndexType.
     * 
     * @since 3.5
     */
    IndexType getType();

    /**
     * Return an annotation for the given field.  Annotations are used only for
     * Full Text Indexes.  Returns null if there is no annotation.
     * 
     * @since 3.5
     */
    String getAnnotationForField(String fieldName);

    /**
     * Creates an {@code IndexKey} for this index.  The returned key can only
     * hold fields that are part of this. Other fields are rejected if an
     * attempt is made to set them on the returned object.
     *
     * @return an empty index key based on the index
     */
    IndexKey createIndexKey();

    /**
     * Creates an {@code IndexKey} for the index populated relevant fields from
     * the {@code RecordValue} parameter.  Fields that are not part of the
     * index key are silently ignored.
     *
     * @param value a {@code RecordValue} instance
     *
     * @return an {@code IndexKey} containing relevant fields from the value
     */
    IndexKey createIndexKey(RecordValue value);

    /**
     * Creates an {@code IndexKey} based on JSON input.  If the {@code exact}
     * parameter is true the input string must contain an exact match to the
     * index key.  It must not have additional data.  If false, only matching
     * fields will be added and the input may have additional, unrelated data.
     *
     * @param jsonInput a JSON string
     *
     * @param exact set to true for an exact match.  See above
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the input is malformed
     */
    IndexKey createIndexKeyFromJson(String jsonInput,
                                    boolean exact);

    /**
     * Creates an {@code IndexKey} based on JSON input.  If the {@code exact}
     * parameter is true the input string must contain an exact match to the
     * index key.  It must not have additional data.  If false, only matching
     * fields will be added and the input may have additional, unrelated data.
     *
     * @param jsonInput a JSON string
     *
     * @param exact set to true for an exact match.  See above
     *
     * @throws IllegalArgumentException if exact is true and a field is
     * missing or extra.  It will also be thrown if a field type or value is
     * not correct
     *
     * @throws IllegalArgumentException if the input is malformed
     */
    IndexKey createIndexKeyFromJson(InputStream jsonInput,
                                    boolean exact);

    /**
     * Creates a {@code FieldRange} object used to specify a value range for
     * use in a index iteration operation in {@link TableAPI}.
     *
     * @param fieldPath the path to the field from the index
     * to use for the range.  This may be in "dot" notation if the field is
     * nested within an array or record.
     *
     * @throws IllegalArgumentException if the field is not defined in the
     * index
     *
     * @return an empty {@code FieldRange} based on the index
     */
    FieldRange createFieldRange(String fieldPath);

    /**
     * Creates a {@code FieldRange} object used to specify a value range for
     * map keys for use in a index iteration operation over an index that
     * includes a map key as an indexed field.
     *
     * @param pathToMap the path to the map name of the field from the index
     * to use for the range.  This may be in "dot" notation if the map field
     * is nested inside a record.
     *
     * @throws IllegalArgumentException if the field is not defined in the
     * index
     *
     * @return an empty {@code FieldRange} based on the index
     *
     * @since 3.2
     */
    public FieldRange createMapKeyFieldRange(String pathToMap);

    /**
    /**
     * Creates a {@code FieldRange} object used to specify a value range for
     * an indexed value that is either the element of a map or nested inside the
     * element of a map.
     *
     * @param pathToMap the path to the map name of the field from the index
     * to use for the range.  This may be in "dot" notation if the map field
     * is nested inside a record.
     *
     * @param pathToValue the path from the map to the target value for use in
     * the range.  This may be in "dot" notation if the map is map of records
     * and the target field is nested in that record.  It should be null if the
     * target is the map's element itself.
     *
     * @throws IllegalArgumentException if the field is not defined in the
     * index.
     *
     * @return an empty {@code FieldRange} based on the index
     *
     * @since 3.2
     */
    public FieldRange createMapValueFieldRange(String pathToMap,
                                               String pathToValue);

    /**
     * The type of an index.  Currently there are two types: SECONDARY is a
     * regular index defined by mapping fields to keys in a secondary database,
     * while TEXT is a full text index defined via a mapping of fields to a
     * text search engine.
     *
     * @since 3.5
     */
    public enum IndexType {

        SECONDARY, TEXT

    }
}
