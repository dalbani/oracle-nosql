package oracle.kv.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import oracle.kv.impl.api.table.ArrayDefImpl;
import oracle.kv.impl.api.table.ArrayValueImpl;
import oracle.kv.impl.api.table.BinaryDefImpl;
import oracle.kv.impl.api.table.ComplexValueImpl;
import oracle.kv.impl.api.table.EnumDefImpl;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FixedBinaryDefImpl;
import oracle.kv.impl.api.table.MapDefImpl;
import oracle.kv.impl.api.table.MapValueImpl;
import oracle.kv.impl.api.table.RecordDefImpl;
import oracle.kv.impl.api.table.RecordValueImpl;
import oracle.kv.impl.api.table.StringDefImpl;
import oracle.kv.impl.api.table.TableJsonUtils;

/**
 * Factory class to create FieldValue instance objects.
 *
 * @since 4.0
 */
public class FieldValueFactory {

    /**
     * <p>Creates a new value from a JSON doc (which is given as a String).</p>
     *
     * If type is RecordDef then:<br/>
     * (a) the JSON doc may have fields that do not appear in the record
     *     schema. Such fields are simply skipped.<br/>
     * (b) the JSON doc may be missing fields that appear in the record's
     *     schema. Such fields will remain unset in the record value. This
     *     means {@link RecordValue#get(String)} will return null and {@link
     *     RecordValue#toJsonString(boolean)} will skip unset fields.<br/>
     *
     * <p>If type is BINARY then the value must be a base64 encoded value.</p>
     *
     * <p>Note: This methods doesn't handle arbitrary JSON, it has to comply to
     * the given type. Also, top level null is not supported.</p>
     *
     * @param type the type definition of the instance.
     * @param jsonString the JSON representation
     * @return the newly created instance
     * @throws IllegalArgumentException for invalid documents
     */
    public static FieldValue createValueFromJson(
        FieldDef type,
        String jsonString) {

        if (jsonString == null) {
            throw new IllegalArgumentException("Not a valid JSON input.");
        }

        int l;
        InputStream jsonInput;

        switch (type.getType()) {
        case ARRAY:
            final ArrayValueImpl arrayValue =
                ((ArrayDefImpl)type).createArray();

            jsonInput =  new ByteArrayInputStream(jsonString.getBytes());
            ComplexValueImpl.createFromJson(arrayValue, jsonInput, false);
            return arrayValue;

        case BINARY:
            l = jsonString.length();

            if (jsonString.charAt(0) != '"' ||
                jsonString.charAt(l - 1) != '"' ||
                l <= 1) {
                throw new IllegalArgumentException("Invalid input for " +
                    "BinaryValue: " + jsonString);
            }
            return ((BinaryDefImpl)type).
                fromString(jsonString.substring(1, l - 1));

        case BOOLEAN:
            if ("true".equals(jsonString.toLowerCase())) {
                return createBoolean(true);
            } else if ("false".equals(jsonString.toLowerCase())) {
                return createBoolean(false);
            }

            throw new IllegalArgumentException("Illegal input for a BooleanValue");

        case DOUBLE:
            try {
                return createDouble(Double.parseDouble(jsonString));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    ("Failed to parse DoubleValue: " + e.getMessage()), e);
            }

        case ENUM:
            l = jsonString.length();

            if (jsonString.charAt(0) != '"' ||
                jsonString.charAt(l - 1) != '"' ||
                l <= 1) {
                throw new IllegalArgumentException("Invalid input for a " +
                    "StringValue: " + jsonString);
            }
            return ((EnumDefImpl)type).
                createEnum(jsonString.substring(1, l - 1));

        case FIXED_BINARY:
            l = jsonString.length();

            if (jsonString.charAt(0) != '"' ||
                jsonString.charAt(l - 1) != '"' ||
                l <= 1) {
                throw new IllegalArgumentException("Invalid input for " +
                    "FixedBinaryValue: " + jsonString);
            }
            return ((FixedBinaryDefImpl)type).
                fromString(jsonString.substring(1, l - 1));

        case FLOAT:
            try {
                return createFloat(Float.parseFloat(jsonString));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    ("Failed to parse FloatValue: " + e.getMessage()), e);
            }

        case INTEGER:
            try {
                return createInteger(Integer.parseInt(jsonString));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    ("Failed to parse IntegerValue: " + e.getMessage()), e);
            }

        case LONG:
            try {
                return createLong(Long.parseLong(jsonString));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    ("Failed to parse LongValue: " + e.getMessage()), e);
            }

        case MAP:
            final MapValueImpl mapValue = ((MapDefImpl)type).createMap();
            jsonInput =  new ByteArrayInputStream(jsonString.getBytes());
            ComplexValueImpl.createFromJson(mapValue, jsonInput, false);
            return mapValue;

        case RECORD:
            final RecordValueImpl recordValue =
                ((RecordDefImpl)type).createRecord();

            jsonInput =  new ByteArrayInputStream(jsonString.getBytes());
            ComplexValueImpl.createFromJson(recordValue, jsonInput, false);
            return recordValue;

        case STRING:
            l = jsonString.length();

            if (jsonString.charAt(0) != '"' ||
                jsonString.charAt(l - 1) != '"' ||
                l <= 1) {
                throw new IllegalArgumentException("Invalid input for a " +
                    "StringValue: " + jsonString);
            }
            return ((StringDefImpl)type).
                createString(jsonString.substring(1, l - 1));

        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
        case EMPTY:
        default:
            throw new IllegalArgumentException("Type not supported by " +
                "fromJson: " + type);
        }
    }

    /**
     * Creates a new value from a JSON doc (which is given as a Reader).
     * If type is RecordDef then:
     * (a) the JSON doc may have fields that do not appear in the record
     *     schema. Such fields are simply skipped.
     * (b) the JSON doc may be missing fields that appear in the record's
     *     schema. Such fields will remain unset in the record value.
     * If type is BINARY then the value must be a base64 encoded value.
     *
     * Note: This methods doesn't handle arbitrary JSON, it has to comply to
     * the given type. Also, top level null is not supported.
     *
     * @param type the type definition of the instance.
     * @param jsonReader the JSON representation
     * @return the newly created instance
     * @throws IllegalArgumentException for invalid documents
     * @throws IOException for an invalid Reader
     */
    public static FieldValue createValueFromJson(
        FieldDef type,
        Reader jsonReader)
        throws IOException {

        JsonParser jp = null;
        try {
            jp = TableJsonUtils.createJsonParser(jsonReader);

            return fromJson(type, jp);
        } finally {
            if (jp != null) {
                jp.close();
            }
        }
    }

    /**
     * Creates a new value from a JSON doc (which is given as an InputStream).
     * If type is RecordDef then:
     * (a) the JSON doc may have fields that do not appear in the record
     *     schema. Such fields are simply skipped.
     * (b) the JSON doc may be missing fields that appear in the record's
     *     schema. Such fields will remain unset in the record value.
     * If type is BINARY then the value must be a base64 encoded value.
     *
     * Note: This methods doesn't handle arbitrary JSON, it has to comply to
     * the given type. Also, top level null is not supported.
     *
     * @param type the type definition of the instance.
     * @param jsonStream the JSON representation
     * @return the newly created instance
     * @throws IllegalArgumentException for invalid documents
     * @throws IOException for an invalid InputStream
     */
    public static FieldValue createValueFromJson(
        FieldDef type,
        InputStream jsonStream)
        throws IOException {

        JsonParser jp = null;
        try {
            jp = TableJsonUtils.createJsonParser(jsonStream);

            return fromJson(type, jp);
        } finally {
            if (jp != null) {
                jp.close();
            }
        }
    }

    private static FieldValue fromJson(FieldDef type, JsonParser jp)
        throws IOException {

        JsonToken t;
        switch (type.getType()) {
        case ARRAY:
            final ArrayValueImpl arrayValue = ((ArrayDefImpl)type).createArray();
            jp.nextToken();

            arrayValue.addJsonFields(
                jp, (arrayValue instanceof IndexKey), null, false);

            arrayValue.validate();
            return arrayValue;

        case BINARY:
                t = jp.nextToken();
                if (t == JsonToken.VALUE_STRING) {
                    return ((BinaryDefImpl)type).fromString(jp.getText());
                }

                throw new IllegalArgumentException("Invalid input for " +
                    "BinaryValue.");

        case BOOLEAN:
                t = jp.nextToken();
                return createBoolean(jp.getBooleanValue());

        case DOUBLE:
            t = jp.nextToken();
            return createDouble(jp.getDoubleValue());

        case ENUM:
            t = jp.nextToken();
            if (t == JsonToken.VALUE_STRING) {
                return ((EnumDefImpl)type).createEnum(jp.getText());
            }

            throw new IllegalArgumentException("Invalid input for " +
                "StringValue.");

        case FIXED_BINARY:
            t = jp.nextToken();
            if (t == JsonToken.VALUE_STRING) {
                return ((FixedBinaryDefImpl)type).fromString(jp.getText());
            }

            throw new IllegalArgumentException("Invalid input for " +
                "FixedBinaryValue.");

        case FLOAT:
            t = jp.nextToken();
            return createFloat(jp.getFloatValue());

        case INTEGER:
            t = jp.nextToken();
            return createInteger(jp.getIntValue());

        case LONG:
            t = jp.nextToken();
            return createLong(jp.getLongValue());

        case MAP:
            final MapValueImpl mapValue = ((MapDefImpl)type).createMap();
            jp.nextToken();

            mapValue.addJsonFields(
                jp, (mapValue instanceof IndexKey), null, false);

            mapValue.validate();
            return mapValue;

        case RECORD:
            final RecordValueImpl recordValue =
                ((RecordDefImpl)type).createRecord();
            jp.nextToken();

            recordValue.addJsonFields(
                jp, (recordValue instanceof IndexKey), null, false);

            recordValue.validate();
            return recordValue;

        case STRING:
                t = jp.nextToken();
                if (t == JsonToken.VALUE_STRING) {
                    return ((StringDefImpl)type).createString(jp.getText());
                }

                throw new IllegalArgumentException("Invalid input for " +
                    "StringValue.");
        case ANY:
        case ANY_ATOMIC:
        case ANY_RECORD:
        case EMPTY:
        default:
            throw new IllegalArgumentException("Type not supported by " +
                "fromJson: " + type);
        }
    }

    /**
     * Creates a BinaryValue instance from its java representation.
     *
     * @param v the java value
     * @return the newly created instance
     */
    public static BinaryValue createBinary(byte[] v) {
        return FieldDefImpl.binaryDef.createBinary(v);
    }

    /**
     * Creates a BooleanValue instance from its java representation.
     *
     * @param v the java value
     * @return the newly created instance
     */
    public static BooleanValue createBoolean(boolean v) {
        return FieldDefImpl.booleanDef.createBoolean(v);
    }

    /**
     * Creates a DoubleValue instance from its java representation.
     *
     * @param v the java value
     * @return the newly created instance
     */
    public static DoubleValue createDouble(double v) {
        return FieldDefImpl.doubleDef.createDouble(v);
    }

    /**
     * Creates a FloatValue instance from its java representation.
     *
     * @param v the java value
     * @return the newly created instance
     */
    public static FloatValue createFloat(float v) {
        return FieldDefImpl.floatDef.createFloat(v);
    }

    /**
     * Creates a IntegerValue instance from its java representation.
     *
     * @param v the java value
     * @return the newly created instance
     */
    public static IntegerValue createInteger(int v) {
        return FieldDefImpl.integerDef.createInteger(v);
    }

    /**
     * Creates a LongValue instance from its java representation.
     *
     * @param v the java value
     * @return the newly created instance
     */
    public static LongValue createLong(long v) {
        return FieldDefImpl.longDef.createLong(v);
    }

    /**
     * Creates a StringValue instance from its java representation.
     *
     * @param v the java value
     * @return the newly created instance
     */
    public static StringValue createString(String v) {
        return FieldDefImpl.stringDef.createString(v);
    }
}
