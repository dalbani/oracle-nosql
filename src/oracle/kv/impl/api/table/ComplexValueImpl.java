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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.sleepycat.persist.model.Persistent;
import oracle.kv.table.FieldDef;
import oracle.kv.table.IndexKey;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

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
public abstract class ComplexValueImpl extends FieldValueImpl {

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

    /*
     * Public api methods from Object and FieldValue
     */

    /**
     * Provides a common method for the string value of the complex types.
     */
    @Override
    public String toString() {
        return toJsonString(false);
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    /*
     * FieldValueImpl internal api methods
     */

    @Override
    public FieldDefImpl getDefinition() {
        return (FieldDefImpl)fieldDef;
    }

    /*
     * Local methods
     */

    protected void setIndex(IndexImpl indexImpl) {
        this.indexImpl = indexImpl;
    }

    /**
     * Validate the value of the object.  By default there is not validation.
     * Subclasses may implement this.
     */
    public void validate() {
    }

    /**
     * Validates the field name as part of the index. This is called during
     * IndexKey construction by the app, when nested fields are put into Map
     * and Record values that participate in the IndexKey. For records the
     * field name must exist in the index definition. For maps the field name
     * must exist if the index is not multi-key; otherwise the field name is
     * just a map key and not relevant to the index itself.
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
     * Populate the given complex value from a JSON doc (which is given as
     * a reader). If exact is true, the json doc must match exactly to the
     * schema of the complex value. Otherwise, if this is a RecordValue, then
     * (a) the JSON doc may have fields that do not appear in the record
     *     schema. Such fields are simply skipped.
     * (b) the JSON doc may be missing fields that appear in the record's
     *     schema. Such fields will remain unset in the record value.
     */
    public static void createFromJson(
        ComplexValueImpl complexValue,
        Reader jsonInput,
        boolean exact) {

        JsonParser jp = null;

        try {
            jp = TableJsonUtils.createJsonParser(jsonInput);
            /*move to START_OBJECT or START_ARRAY*/
            jp.nextToken();

            complexValue.addJsonFields(
                jp, (complexValue instanceof IndexKey), null, exact);

            complexValue.validate();

        } catch (IOException ioe) {
            throw new IllegalArgumentException(
                ("Failed to parse JSON input: " + ioe.getMessage()), ioe);
        } finally {
            if (jp != null) {
                try {
                    jp.close();
                } catch (IOException ignored) {
                    /* ignore failures on close */
                }
            }
        }
    }

    /**
     * Populate the given complex value from a JSON doc (which is given as an
     * input stream). If exact is true, the json doc must match exactly to the
     * schema of the complex value. Otherwise, if this is a RecordValue, then
     * (a) the JSON doc may have fields that do not appear in the record
     *     schema. Such fields are simply skipped.
     * (b) the JSON doc may be missing fields that appear in the record's
     *     schema. Such fields will remain unset in the record value.
     */
    public static void createFromJson(
        ComplexValueImpl complexValue,
        InputStream jsonInput,
        boolean exact) {

        JsonParser jp = null;

        try {
            jp = TableJsonUtils.createJsonParser(jsonInput);
            /*move to START_OBJECT or START_ARRAY*/
            jp.nextToken();

            complexValue.addJsonFields(
                jp, (complexValue instanceof IndexKey), null, exact);

            complexValue.validate();

        } catch (IOException ioe) {
            throw new IllegalArgumentException(
                ("Failed to parse JSON input: " + ioe.getMessage()), ioe);
        } finally {
            if (jp != null) {
                try {
                    jp.close();
                } catch (IOException ignored) {
                    /* ignore failures on close */
                }
            }
        }
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
    abstract void addJsonFields(
        JsonParser jp,
        boolean isIndexKey,
        String currentFieldName,
        boolean exact);

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
