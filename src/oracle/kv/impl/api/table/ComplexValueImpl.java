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

import java.io.IOException;

import oracle.kv.table.FieldDef;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.sleepycat.persist.model.Persistent;

/**
 * ComplexValueImpl is an intermediate abstract implementation class used to
 * factor out common state and code from complex types such as Array, Map,
 * Record, Row, etc.  It introduces a single function to get the field
 * definition ({@link FieldDef}) for the object.
 * <p>
 * The field definition ({@link FieldDef}) is table metadata that defines the
 * types and constraints in a table row.  It is required by ComplexValue
 * instances to define the shape of the values they hold.  It is used to
 * validate type and enforce constraints for values added to a ComplexValue.
 */

@Persistent(version=1)
abstract class ComplexValueImpl extends FieldValueImpl {
    private static final long serialVersionUID = 1L;

    /*
     * Index is set if this instance participates in an IndexKey.  It is used
     * for validation of field creation to catch attempts to assign fields
     * to a map, record, or array that are not part of the index.
     */
    transient protected IndexImpl indexImpl;
    final private FieldDef fieldDef;

    ComplexValueImpl(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    /* DPL */
    @SuppressWarnings("unused")
    private ComplexValueImpl() {
        fieldDef = null;
    }

    protected void setIndex(IndexImpl indexImpl) {
        this.indexImpl = indexImpl;
    }

    /**
     * Validates the field name as part of the index.  This is called when
     * fields are put into Map and Record types when constructing an IndexKey.
     * For records the field name must exist in the index.  For maps the
     * field name must exist if the index is not multi-key; otherwise the field
     * name is just a map key and not relevant to the index itself.
     *
     * This method isn't called when adding fields to an array so the check
     * for a multi-key index is map-only.
     */
    protected void validateIndexField(String fieldName) {
        if (indexImpl != null && !indexImpl.containsField(fieldName)) {
            if (!indexImpl.isMultiKey()) {
                throw new IllegalArgumentException
                    ("Field is not part of the index: " + fieldName);
            }
        }
    }

    /**
     * Return the definition of this field
     */
    public FieldDef getDefinition() {
        return fieldDef;
    }

    static protected ComplexValueImpl createComplexValue(FieldDef def) {
        switch (def.getType()) {
        case MAP:
            return (ComplexValueImpl) def.createMap();
        case RECORD:
            return (ComplexValueImpl) def.createRecord();
        case ARRAY:
            return (ComplexValueImpl) def.createArray();
        default:
            throw new IllegalArgumentException
                ("Not a complex type: " + def.getType());
        }
    }

    /**
     * Provides a common method for the string value of the complex types.
     */
    @Override
    public String toString() {
        return toJsonString(false);
    }

    /**
     * Add JSON fields from the JsonParser to this object.
     * @param jp the parser
     * @param isIndexKey true if the containing object is an IndexKey.
     * This is used to handle situations that are conditional and are not
     * caught by a RecordValueImpl.validate() call.
     * @param currentFieldName the current field name, which is the last
     * field name extracted from the parser.  This is only non-null when
     * addJsonFields is called from RecordValueImpl, which knows field
     * names.
     * @param exact true if the JSON needs have all fields present.
     */
    abstract void addJsonFields(JsonParser jp, boolean isIndexKey,
                                String currentFieldName, boolean exact);

    /**
     * Validate the value of the object.  By default there is not validation.
     * Subclasses may implement this.
     */
    void validate() {
    }

    /**
     * A utility method for use by subclasses to skip JSON input
     * when an exact match is not required.  This function finds a matching
     * end of array or object token.  It will recurse in the event a
     * nested array or object is detected.
     */
    static void skipToJsonToken(JsonParser jp, JsonToken skipTo) {
        try {
            JsonToken token = jp.nextToken();
            while (token != skipTo) {
                if (token == JsonToken.START_OBJECT) {
                    skipToJsonToken(jp, JsonToken.END_OBJECT);
                } else if (token == JsonToken.START_ARRAY) {
                    skipToJsonToken(jp, JsonToken.END_ARRAY);
                }
                token = jp.nextToken();
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException
                (("Failed to parse JSON input: " + ioe.getMessage()), ioe);
        }
    }
}
