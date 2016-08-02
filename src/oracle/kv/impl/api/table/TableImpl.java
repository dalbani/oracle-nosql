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

import static oracle.kv.impl.api.table.FieldDefImpl.binaryDef;
import static oracle.kv.impl.api.table.FieldDefImpl.booleanDef;
import static oracle.kv.impl.api.table.FieldDefImpl.doubleDef;
import static oracle.kv.impl.api.table.FieldDefImpl.floatDef;
import static oracle.kv.impl.api.table.FieldDefImpl.integerDef;
import static oracle.kv.impl.api.table.FieldDefImpl.longDef;
import static oracle.kv.impl.api.table.FieldDefImpl.stringDef;
import static oracle.kv.impl.api.table.TableJsonUtils.DESC;
import static oracle.kv.impl.api.table.TableJsonUtils.FIELDS;
import static oracle.kv.impl.api.table.TableJsonUtils.NAME;
import static oracle.kv.impl.api.table.TableJsonUtils.TTL;
import static oracle.kv.impl.api.table.TableJsonUtils.PKEY_SIZES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.ResolvingDecoder;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.sleepycat.util.PackedInteger;

import oracle.kv.Key;
import oracle.kv.Key.BinaryKeyIterator;
import oracle.kv.Value;
import oracle.kv.Value.Format;
import oracle.kv.ValueVersion;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.api.ops.ResultKey;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.metadata.MetadataInfo;
import oracle.kv.impl.security.Ownable;
import oracle.kv.impl.security.ResourceOwner;
import oracle.kv.impl.util.JsonUtils;
import oracle.kv.impl.util.SortableString;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldRange;
import oracle.kv.table.FieldValue;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.MapValue;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.RecordDef;
import oracle.kv.table.RecordValue;
import oracle.kv.table.ReturnRow;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TimeToLive;

/**
 * TableImpl implements Table, which represents a table in Oracle NoSQL
 * Database.  It is an immutable object created from system metadata.
 *
 * Tables are defined in terms of several properties:
 * 1.  a map of {@link FieldDef} instances keyed by a String field name.  This
 * defines the fields (or "columns") of a table.
 * 2.  a list of fields that define the fields that participate in the
 * primary key for the table.  These fields turn into KV Key path
 * components in the store.
 * 3.  a list of fields that is a proper subset of the primary key fields
 * that defines the "shard key" for the table.  The shard key defines the
 * primary key fields that become part of the Key's major path.  The remaining
 * primary key fields become the Key's minor path.
 * 4.  optional indexes, defined in terms of fields in the table.
 * 5.  optional child tables, keyed by table name.  Child tables inherit the
 * table's primary key and shard key.
 * 6.  optional Time-to-Live (TTL) duration. A record may use this TTL as its
 *  expiration when no explicit TTL value is set for the same record.
 *
 * If a table is a child table it also references its parent table.  When a
 * table is created the system generates a unique long to serve as an id for
 * the table.  The serialized form of this id serves a part of the table's
 * primary key to locate it in the store.  An id is used instead of the table
 * name to keep keys small.
 *
 * Tables can be created in {@code r2compat} mode which means that the table
 * name is used for keys instead of the id because the table overlays R2 data.
 * Such tables also write new records in a manner that is compatible with R2 by
 * avoiding adding the table version to the record data.
 *
 * Because a table can evolve the map of fields is maintained as a list of
 * maps of fields, indexed by table "version."  The initial table version
 * is 1 (but index 0).
 *
 * Tables can evolve in limited ways with schema evolution.  The only thing
 * that can be done is to add or remove non-key fields or change fields in
 * a way that does not affect their serialization or change the default TTL.
 * Once r2compat tables have been evolved they are no longer readable by R2
 * key/value code.
 * <p>
 *
 * Tables can carry an optional default expiry duration. Expiry duration can be
 * null or a positive value (including 0) in a particular unit of time. A 0
 * value is semantically equivalent to no expiry being defined at all.
 * <br>
 * For non-zero positive expiry value, the unit of time must be equal or longer
 * than a minimum time unit supported by the system. <br>
 * Currently, minimum unit of time is an hour.
 *
 */
public class TableImpl implements Table, MetadataInfo, Ownable,
                                  Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private long id;

    private final TableImpl parent;

    private final TreeMap<String, Index> indexes;

    private final List<String> primaryKey;

    /*
     * If non-null, a list of size constraints on the corresponding Primary Key
     * fields.
     */
    private final List<Integer> primaryKeySizes;

    private final List<String> shardKey;

    private final String description;

    private final Map<String, Table> children;

    private final ArrayList<FieldMap> versions;

    private TimeToLive ttl;

    private TableStatus status;

    /*
     * These next two are true, and non-zero, respectively, if this is
     * an overlay on R2 data with an Avro schema.  r2compat can be true
     * without a schemaId for a key-only table.  It affects the string used
     * as the table's key component (idString, below).
     */
    private final boolean r2compat;
    private final int schemaId;

    private final ResourceOwner owner;

    /* Whether this table is system table */
    private final boolean sysTable;

    /*
     * transient, cached values
     */
    /* Cached Avro Schema for current version, unrelated to schemaId above */
    private transient Schema schema;

    /*
     * The current version of this table instance. It must only be set using
     * its accessor to ensure that associated caches are maintained.
     */
    private transient volatile int version;
    /* The cached record definition associated with the current version above. */
    private transient volatile RecordDef recordDef;
    /* The number of components in a key for this table */
    private transient int numKeyComponents;
    /* The string used to key this table. */
    private transient String idString;

    /**
     * A constant used to designate the key field of a map.  It is here to be
     * more shareable across external classes that need it.
     */
    public static final String KEY_TAG = "_key";

    /**
     * A constant used to designate the element field of a map.  It is here to
     * be more shareable across external classes that need it.
     */
    public static final String ANONYMOUS = MapValue.ANONYMOUS;

    /* Both of these are always used with a left paren and no space */
    private static final String KEYOF = "keyof(";
    static final String ELEMENTOF = "elementof(";

    public enum TableStatus {
        /** Table and its data is being deleted */
        DELETING() {
            @Override
            public boolean isDeleting() {
                return true;
            }
        },

        /** Table is ready for use */
        READY() {
            @Override
            public boolean isReady() {
                return true;
            }
        };

        /**
         * Returns true if this is the {@link #DELETING} type.
         * @return true if this is the {@link #DELETING} type
         */
        public boolean isDeleting() {
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

    /*
     * String separator used to generate a globally unique name for a table.
     */
    public static final String SEPARATOR = ".";
    private static final int MAX_ID_LENGTH = 32;
    private static final int MAX_NAME_LENGTH = 64;
    private static final String SEPARATOR_REGEX = "\\.";
    private static final int INITIAL_TABLE_VERSION = 1;

    /* The prefix of system table names, case insensitive */
    public static final String SYSTEM_TABLE_PREFIX = "SYS$";

    /*
     * Names (field names, enum symbols) must start with an alphabetic
     * character [A-Za-z] followed by alphabetic characters, numeric
     * characters or underscore [A-Za-z0-9_].
     */
    static final String VALID_NAME_CHAR_REGEX = "^[A-Za-z][A-Za-z0-9_]*$";

    /**
     * Creates a TableImpl.
     * @param name the table name (required)
     * @param parent the parent table, or null
     * @param primaryKey the primary key fields (required)
     * @param shardKey the shard key (required)
     * @param fields the field definitions for the table (required)
     * @param r2compat if true create a release 2 compatible table which
     * means using the table name instead of its id in getIdString()
     * @param schemaId if a release 2 schema was used to construct the
     * fields this must be its schema id. It is only meaningful if r2compat
     * is true.
     * @param description a user-provided description of the table, or null
     * @param validate if true validate the fields and state of the table
     * upon construction
     * @param owner the owner of this table
     * @param sysTable if true the table is a system table
     */
    private TableImpl(final String name,
                      final TableImpl parent,
                      final List<String> primaryKey,
                      final List<Integer> primaryKeySizes,
                      final List<String> shardKey,
                      final FieldMap fields,
                      final TimeToLive ttl,
                      boolean r2compat,
                      int schemaId,
                      final String description,
                      boolean validate,
                      ResourceOwner owner,
                      boolean sysTable) {
        this.name = name;
        this.parent = parent;
        this.description = description;
        this.primaryKey = primaryKey;
        this.primaryKeySizes = primaryKeySizes;
        this.shardKey = shardKey;
        this.status = TableStatus.READY;
        this.r2compat = r2compat;
        this.schemaId = schemaId;
        this.sysTable = sysTable;
        children = new TreeMap<String, Table>(FieldComparator.instance);
        indexes = new TreeMap<String, Index>(FieldComparator.instance);
        versions = new ArrayList<FieldMap>();
        versions.add(fields);
        this.ttl = ttl;
        setVersion(INITIAL_TABLE_VERSION);

        if (sysTable) {
            validateSystemTableName(name);
        } else {
            validateComponent(name, true);
        }
        if (validate) {
            validate();
            setSchema(true);
        }
        setIdString();
        this.owner = owner == null ? null : new ResourceOwner(owner);
    }

    /*
     * This constructor is used by clone().  Some fields are copied by
     * reference:
     *  parent
     *  primaryKey, shardKey
     *  indexes (they are immutable)
     */
    private TableImpl(TableImpl t) {
        name = t.name;
        id = t.id;
        version = t.version;
        description = t.description;
        parent = t.parent;
        primaryKey = t.primaryKey;
        primaryKeySizes = t.primaryKeySizes;
        shardKey = t.shardKey;
        status = t.status;
        r2compat = t.r2compat;
        schemaId = t.schemaId;
        owner = t.owner;
        sysTable = t.sysTable;

        children = new TreeMap<String, Table>(FieldComparator.instance);
        for (Table table : t.children.values()) {
            children.put(table.getName(), ((TableImpl)table).clone());
        }

        versions = new ArrayList<FieldMap>(t.versions);
        ttl = t.ttl;
        setVersion(t.version);
        /* this constructor uses the same Comparator as t.indexes */
        indexes = new TreeMap<String, Index>(t.indexes);
        setSchema(true);
        setIdString();
    }

    public static TableImpl createTable(String name,
                                        Table parent,
                                        List<String> primaryKey,
                                        List<Integer> primaryKeySizes,
                                        List<String> shardKey,
                                        FieldMap fields,
                                        boolean r2compat,
                                        int schemaId,
                                        String description,
                                        boolean validate,
                                        ResourceOwner owner,
                                        TimeToLive ttl,
                                        boolean sysTable) {
        return new TableImpl(name, (TableImpl)parent,
                             primaryKey,
                             primaryKeySizes,
                             shardKey, fields, ttl,
                             r2compat,
                             schemaId,
                             description,
                             validate,
                             owner,
                             sysTable);
    }

    /*
     * Needed to deserialize an instance of TableImpl via java deserialization.
     * Specifically, it is needed to initialize transient fields not sent in
     * the serialized object.
     */
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setSchema(false);
        getTableVersion();
        setIdString();
    }

    @Override
    public TableImpl clone() {
        return new TableImpl(this);
    }

    @Override
    public Table getChildTable(String tableName) {
        return children.get(tableName);
    }

    @Override
    public boolean childTableExists(String tableName) {
        return children.containsKey(tableName);
    }

    @Override
    public Table getVersion(int version1) {
        if (versions.size() < version1 || version1 < 0) {
            throw new IllegalArgumentException
                ("Table version " + version1 + " does not exist for table " +
                 getFullName());
        }
        TableImpl newTable = clone();
        newTable.setVersion(version1);
        newTable.setSchema(true);
        return newTable;
    }

    @Override
    public Map<String, Table> getChildTables() {
        return Collections.unmodifiableMap(children);
    }

    @Override
    public Table getParent() {
        return parent;
    }

    public String getAvroSchema(boolean pretty) {
        return generateAvroSchema(version, pretty);
    }

    public Schema getSchema() {
        return schema;
    }

    /**
     * Return the current version of this table.  Each time a table
     * is evolved its version number will increment.  A table starts out at
     * version 1.  Check for 0 because the field is transient and will not be
     * set from a deserialized instance.
     */
    @Override
    public int getTableVersion() {
        if (version == 0) {
            setVersion(versions.size());
        }
        return version;
    }

    @Override
    public Index getIndex(String indexName) {
        return indexes.get(indexName);
    }

    /**
     * Get the secondary Index with the given name.  If no such index exists,
     * return null.  If an index with the given name exists, but it is a Text
     * type index, then the exception is thrown.
     */
    public Index getSecondaryIndex(String indexName) {
        Index i = indexes.get(indexName);
        if (i == null || i.getType() == Index.IndexType.SECONDARY) {
            return i;
        }
        throw new IllegalArgumentException("The index named " + indexName +
                                           " is not a secondary index.");
    }

    /**
     * Get the Text Index with the given name.  If no such index exists, return
     * null.  If an index with the given name exists, but it is not a Text type
     * index, then the exception is thrown.
     */
    public Index getTextIndex(String indexName) {
        Index i = indexes.get(indexName);
        if (i == null || i.getType() == Index.IndexType.TEXT) {
            return i;
        }
        throw new IllegalArgumentException("The index named " + indexName +
                                           " is not a text index.");
    }

    @Override
    public Map<String, Index> getIndexes() {
        return Collections.unmodifiableMap(indexes);
    }

    @Override
    public Map<String, Index> getIndexes(Index.IndexType type) {
    	Map<String, Index> r = new TreeMap<String, Index>();
    	for (Entry<String, Index> entry : indexes.entrySet()) {
            if (entry.getValue().getType() == type) {
                r.put(entry.getKey(), entry.getValue());
            }
    	}
        return r;
    }

    @Override
    public String getName()  {
        return name;
    }

    /**
     * Get a unique string that identifies the table.  This
     * includes the name(s) of any parent tables.
     */
    @Override
    public String getFullName()  {
        StringBuilder sb = new StringBuilder();
        getTableNameInternal(sb);
        return sb.toString();
    }

    public long getId()  {
        return id;
    }

    public String getIdString()  {
        return idString;
    }

    @Override
    public String getDescription()  {
        return description;
    }

    @Override
    public List<String> getFields() {
        return Collections.unmodifiableList(getFieldOrder(version));
    }

    /**
     * Returns the record definitions associated with the table
     */
    RecordDef getRecordDef() {
        return recordDef;
    }

    /**
     * Method used to set the current version associated with the table. It
     * also updates the cached recordDef to ensure that the two always stay
     * in sync.
     */
    private void setVersion(int currentVersion) {
        version = currentVersion;
        recordDef = (version > 0) ?
            new RecordDefImpl(getName(), getFieldMap(version)): null;
    }

    @Override
    public FieldDef getField(String fieldName) {
        FieldMapEntry fme = getFieldMapEntry(fieldName, false);
        if (fme != null) {
            return fme.getField();
        }
        return null;
    }

    @Override
    public boolean isNullable(String fieldName) {

        /* true means throw if the field doesn't exist */
        FieldMapEntry fme = getFieldMapEntry(fieldName, true);
        return fme.isNullable();
    }

    @Override
    public FieldValue getDefaultValue(String fieldName) {

        /* true means throw if the field doesn't exist */
        FieldMapEntry fme = getFieldMapEntry(fieldName, true);
        return fme.getDefaultValue();
    }

    @Override
    public List<String> getPrimaryKey() {
        return Collections.unmodifiableList(primaryKey);
    }


    @Override
    public List<String> getShardKey() {
        return Collections.unmodifiableList(shardKey);
    }

    public List<String> getPrimaryKeyInternal() {
        return primaryKey;
    }

    public int getPrimaryKeySize() {
        return primaryKey.size();
    }

    public List<Integer> getPrimaryKeySizes() {
        return primaryKeySizes;
    }

    public String getPrimaryKeyColumnName(int i) {
        return primaryKey.get(i);
    }

    List<String> getShardKeyInternal() {
        return shardKey;
    }

    public int getShardKeySize() {
        return shardKey.size();
    }

    public RecordDefImpl getRowType() {
        return new RecordDefImpl(getName(), getFieldMap());
    }

    @Override
    public RowImpl createRow() {
        return new RowImpl(recordDef, this);
    }

    @Override
    public RowImpl createRow(RecordValue value) {
        RowImpl row = new RowImpl(recordDef, this);
        populateRecord(row, value);
        return row;
    }

    @Override
    public RowImpl createRowWithDefaults() {
        RowImpl row = new RowImpl(recordDef, this);
        for (Map.Entry<String, FieldMapEntry> entry :
                 getFieldMap().getFields().entrySet()) {
            row.put(entry.getKey(), entry.getValue().getDefaultValue());
        }
        return row;
    }

    @Override
    public PrimaryKeyImpl createPrimaryKey() {
        return new PrimaryKeyImpl(recordDef, this);
    }

    @Override
    public PrimaryKeyImpl createPrimaryKey(RecordValue value) {
        PrimaryKeyImpl key = new PrimaryKeyImpl(recordDef, this);
        populateRecord(key, value);
        return key;
    }

    @Override
    public ReturnRowImpl createReturnRow(ReturnRow.Choice returnChoice) {
        return new ReturnRowImpl(recordDef, this, returnChoice);
    }

    @Override
    public Row createRowFromJson(String jsonInput, boolean exact) {
        return createRowFromJson
            (new ByteArrayInputStream(jsonInput.getBytes()), exact);
    }

    @Override
    public Row createRowFromJson(InputStream jsonInput, boolean exact) {
        RowImpl row = createRow();
        ComplexValueImpl.createFromJson(row, jsonInput, exact);
        return row;
    }

    @Override
    public PrimaryKeyImpl createPrimaryKeyFromJson(String jsonInput,
                                                   boolean exact) {
        return createPrimaryKeyFromJson
            (new ByteArrayInputStream(jsonInput.getBytes()), exact);
    }

    @Override
    public PrimaryKeyImpl createPrimaryKeyFromJson(InputStream jsonInput,
                                                   boolean exact) {
        PrimaryKeyImpl key = createPrimaryKey();
        ComplexValueImpl.createFromJson(key, jsonInput, exact);
        return key;
    }

    @Override
    public FieldRange createFieldRange(String fieldName) {
        FieldDef def = getField(fieldName);
        if (def == null) {
            throw new IllegalArgumentException
                ("Field does not exist in table definition: " + fieldName);
        }
        if (!primaryKey.contains(fieldName)) {
            throw new IllegalArgumentException
                ("Field does not exist in primary key: " + fieldName);
        }
        return new FieldRange(fieldName, def, getPrimaryKeySize(fieldName));
    }

    @Override
    public MultiRowOptions createMultiRowOptions
        (List<String> tableNames, FieldRange fieldRange) {

        if ((tableNames == null || tableNames.isEmpty()) &&
            fieldRange == null) {
            throw new IllegalArgumentException
                ("createMultiRowOptions must have at least one non-null " +
                 "parameter");
        }

        MultiRowOptions mro = null;
        if (fieldRange != null) {
            mro = new MultiRowOptions(fieldRange);
        }

        if (tableNames != null) {
            List<Table> ancestorTables = new ArrayList<Table>();
            List<Table> childTables =  new ArrayList<Table>();
            TableImpl topLevelTable = getTopLevelTable();
            for (String tableName : tableNames) {
                TableImpl t = topLevelTable.findTable(tableName);
                if (t == this) {
                    throw new IllegalArgumentException
                        ("Target table must not appear in included tables list");
                }
                if (isAncestorOf(this, t)) {
                    ancestorTables.add(t);
                } else {
                    assert isAncestorOf(t, this);
                    childTables.add(t);
                }
            }
            if (mro == null) {
                mro = new MultiRowOptions(null, ancestorTables, childTables);
            } else {
                mro.setIncludedParentTables(ancestorTables);
                mro.setIncludedChildTables(childTables);
            }
        }
        return mro;
    }

    /**
     * Returns the size contstraint for the named primary key field, or 0
     * if there is none. This assumes that the field name has already been
     * validated as a primary key field.
     */
    public int getPrimaryKeySize(String keyName) {
        if (primaryKeySizes != null) {
            return primaryKeySizes.get(primaryKey.indexOf(keyName));
        }
        return 0;
    }

    /**
     * Return true if ancestor is an ancestor of this table.   Match on
     * full name only.  Equality isn't needed here.
     */
    public boolean isAncestor(Table ancestor) {
        Table parentTable = getParent();
        String fullName = ancestor.getFullName();
        while (parentTable != null) {
            if (fullName.equals(parentTable.getFullName())) {
                return true;
            }
            parentTable = parentTable.getParent();
        }
        return false;
    }

    /**
     * Return the top-level for this table.
     */
    public TableImpl getTopLevelTable() {
        if (parent != null) {
            return parent.getTopLevelTable();
        }
        return this;
    }

    /**
     * Determine equality.  Use name, parentage, version, field definitions
     * and default TTL.
     */
    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Table) {
            TableImpl otherDef = (TableImpl) other;
            if (getName().equalsIgnoreCase(otherDef.getName()) &&
                idsEqual(otherDef)) {
                if (getParent() != null) {
                    if (!getParent().equals(otherDef.getParent())) {
                        return false;
                    }
                } else if (otherDef.getParent() != null) {
                    return false;
                }
                if (!equalsTTL(ttl, otherDef.ttl)) {
                    return false;
                }
                if (!equalsPKSizes(primaryKeySizes,
                                   otherDef.primaryKeySizes)) {
                    return false;
                }
                return (versionsEqual(otherDef) &&
                        getFieldMap().equals(otherDef.getFieldMap()));
            }
        }
        return false;
    }

    /*
     * Compares ids, matching an id of 0 as ok against any actual versioned id.
     * This allows transient tables to compare correctly to persistent onces
     * when everything but the id matches.
     */
    private boolean idsEqual(TableImpl other) {
        if ((getId() == other.getId()) ||
            (getId() == 0 || other.getId() == 0)) {
            return true;
        }
        return false;
    }

    private static boolean equalsTTL(TimeToLive ttl, TimeToLive ottl) {
        if (ttl != null) {
            return ttl.equals(ottl);
        }
        return (ottl == null);
    }

    private static boolean equalsPKSizes(final List<Integer> pks,
                                         final List<Integer> opks) {
        if (pks != null) {
            if (opks != null && (pks.size() == opks.size())) {
                for (int i = 0; i < pks.size(); i++) {
                    if (pks.get(i) != opks.get(i)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return (opks == null);
    }

    /**
     * Determine equality using only name, fields and keys, ignoring version and
     * other persistent-only state.
     */
    public boolean fieldsEqual(Object other) {
        if (other != null && other instanceof Table) {
            TableImpl otherTable = (TableImpl) other;
            if (getName().equalsIgnoreCase(otherTable.getName())) {
                if (parent != null) {
                    if (!parent.fieldsEqual(otherTable.parent)) {
                        return false;
                    }
                } else if (otherTable.parent != null) {
                    return false;
                }
                /*
                 * Consider the fields equal if these match:
                 *  fields, primary key, shard key
                 */
                return (getFieldMap().equals(otherTable.getFieldMap()) &&
                        primaryKey.equals(otherTable.primaryKey) &&
                        shardKey.equals(otherTable.shardKey));
            }
        }
        return false;
    }

    /**
     * More could be added, but this is enough to uniquely identify tables
     * users have obtained.
     */
    @Override
    public int hashCode() {
        return getFullName().hashCode() + versions.size() +
            getFieldMap().hashCode()
          + (getDefaultTTL() != null ? getDefaultTTL().hashCode() : 0);
    }

    boolean nameEquals(TableImpl other) {
        return getFullName().equals(other.getFullName());
    }

    private boolean versionsEqual(TableImpl other) {
        int thisVersion = (version == 0 ? versions.size() : version);
        int otherVersion = (other.version == 0 ? other.versions.size() :
                            other.version);
        return (thisVersion == otherVersion);
    }

    @Override
    public int numTableVersions() {
        return versions.size();
    }

    public boolean hasChildren() {
        return (children.size() != 0);
    }

    /**
     * Return true if the table is an overlay over Avro key/value records.
     */
    public boolean isR2compatible() {
        return r2compat;
    }

    /**
     * Return the Avro schema ID if this table overlays an R2 table, 0
     * otherwise.
     */
    public int getSchemaId() {
        return schemaId;
    }

    /*
     * This is the only call that sets the table id.  It is called when a table
     * object is created in TableMetadata.
     */
    void setId(long id)  {
        this.id = id;
        setIdString();
    }

    private void setIdString() {
        if (id == 0 || r2compat) {
            idString = name;
        } else {
            idString = createIdString(id);
        }
    }

    /**
     * Creates the string used for table keys.  This is separate so it
     * can be used by test code.
     */
    public static String createIdString(long id) {
        int encodingLength = SortableString.encodingLength(id);
        return SortableString.toSortable(id, encodingLength);
    }

    public FieldMap getFieldMap() {
        return getFieldMap(version);
    }

    /**
     * The number of key components for a unique primary key for this table.
     * This number is used to perform relatively efficient filtering of
     * keys on both client and server side as necessary.
     * NOTE: this could be made persistent but it's easily calculated and
     * cached.
     */
    public int getNumKeyComponents() {
        if (numKeyComponents == 0) {
            calculateNumKeys();
        }
        return numKeyComponents;
    }

    /*
     * This is separate from above so that setting the value is synchronized.
     * The number is:
     * 1.  The size of the primary key plus
     * 2.  One for each table in its hierarchy (including itself) plus
     */
    private synchronized void calculateNumKeys() {
        if (numKeyComponents == 0) {
            int num = primaryKey.size() + 1;
            TableImpl t = this;
            while (t.parent != null) {
                ++num;
                t = t.parent;
            }
            numKeyComponents = num;
        }
    }

    public TableStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(TableStatus newStatus) {
        if ((status != newStatus) && status.isDeleting()) {
            throw new IllegalStateException("Table is being deleted, cannot " +
                                            "change status to " + newStatus);
        }
        status = newStatus;
    }

    Map<String, Table> getMutableChildTables() {
        return children;
    }

    /*
     * See below.  This is used internally and by TableBuilder.
     * TODO: should the accessor methods in this class default to allowing
     * nested paths?  Perhaps so...
     */
    FieldMapEntry getFieldMapEntry(String fieldName,
                                   boolean mustExist) {
        return getFieldMapEntry(fieldName, mustExist, false);
    }

    private FieldMapEntry getFieldMapEntry(String fieldPath,
                                           boolean mustExist,
                                           boolean allowNesting) {
        FieldMap fieldMap = getFieldMap();
        String fieldToUse = fieldPath;

        if (allowNesting) {

            /*
             * Find the containing map and use it, along with the final
             * component of the field name.
             */
            TablePath tablePath = new TablePath(fieldMap, fieldPath);
            if (tablePath.isComplex()) {
                fieldMap = findContainingMap(fieldMap, tablePath, mustExist);
                if (fieldMap == null) {
                    if (!mustExist) {
                        return null;
                    }
                    throw new IllegalArgumentException
                        ("Field does not exist in table definition: " +
                         fieldPath);
                }
                fieldToUse = tablePath.getLastStep();
            }
        }

        FieldMapEntry fme = fieldMap.getFieldMapEntry(fieldToUse);
        if (fme != null) {
            return fme;
        }
        if (mustExist) {
            throw new IllegalArgumentException
                ("Field does not exist in table definition: " + fieldPath);
        }
        return null;
    }

    /**
     * Returns the FieldMap that contains the referenced field.
     * If the field is a simple type the map parameter is returned,
     * otherwise it is the record that references the field.
     */
    static FieldMap findContainingMap(FieldMap map,
                                      TablePath tablePath,
                                      boolean mustExist) {
        if (!tablePath.isComplex()) {
            return map;
        }

        String fieldPathName = tablePath.getPathName();

        String parent = fieldPathName.substring(
            0, fieldPathName.lastIndexOf('.'));

        FieldDef def = findTableField(new TablePath(map, parent));

        /* def can be null if a bad field name is passed */
        if (def instanceof MapDefImpl || def instanceof ArrayDefImpl) {

            /*
             * Try the full name in these cases. If the array or map
             * element is a record the above code won't work.
             */
            def = findTableField(tablePath);
        }
        if (def == null || !(def instanceof RecordDefImpl)) {
            if (mustExist) {
                throw new IllegalArgumentException
                    ("Containing field is not a record: " + fieldPathName);
            }
            return null;
        }
        return ((RecordDefImpl)def).getFieldMap();
    }

    Map<String, Index> getMutableIndexes() {
        return indexes;
    }

    /**
     * If this table has a parent return its fully-qualified name, otherwise
     * null.
     */
    public String getParentName() {
        if (parent != null) {
            return parent.getFullName();
        }
        return null;
    }

    public Key createKey(Row row, boolean allowPartial) {
        setTableVersion(row);
        return TableKey.createKey(this, row, allowPartial).getKey();
    }

    /**
     * Create a Row object with all values for the primary key,
     * extracted from the byte[] array that is the store key.
     *
     * This method, and createPrimaryKeyFromBytes are lenient with
     * respect to failures and return null if they fail to match
     * a table.  This is necessary for mixed access between tables and
     * potentially matching key/value records.
     *
     * This is public so that code in api/ops can use it.
     */
    public RowImpl createRowFromKeyBytes(byte[] keyBytes) {
        return createFromKeyBytes(keyBytes, false);
    }

    /**
     * PrimaryKey version of createRowFromKeyBytes.
     */
    PrimaryKeyImpl createPrimaryKeyFromKeyBytes(byte[] keyBytes) {
        return (PrimaryKeyImpl) createFromKeyBytes(keyBytes, true);
    }

    PrimaryKeyImpl createPrimaryKeyFromResultKey(ResultKey rkey) {
        PrimaryKeyImpl pkey =
            (PrimaryKeyImpl) createFromKeyBytes(rkey.getKeyBytes(), true);
        if (pkey != null) {
            pkey.setExpirationTime(rkey.getExpirationTime());
        }
        return pkey;
    }

    /**
     * PrimaryKey version of createRowFromKeyBytes.
     */
    private RowImpl createFromKeyBytes(byte[] keyBytes,
                                       boolean createPrimaryKey) {
        BinaryKeyIterator keyIter = createBinaryKeyIterator(keyBytes);
        if (keyIter != null) {
            TableImpl targetTable = findTargetTable(keyIter);
            if (targetTable != null) {
                RowImpl row = (createPrimaryKey ?
                               targetTable.createPrimaryKey():
                               targetTable.createRow());
                keyIter.reset();
                if (initRowFromKeyBytes(row, keyIter, targetTable)) {
                    return row;
                }
            }
        }
        return null;
    }

    /**
     * Turn the server-side byte arrays into a Row for index
     * key extraction.
     *
     * If there is a failure of any sort return null.  This method
     * needs to be flexible to work with mixed KV and table access.
     * It also cannot throw an exception or the server would die.
     *
     * The only caller of this method is IndexImpl.extractIndexKey(s).
     * Because empty rows still have a format byte the valueBytes
     * array will always have at least one byte.
     *
     * Note that even such "empty" records need to call
     * initRowFromByteValue() in order to handle schema evolved
     * records that may contain default values not in the record value.
     */
    @SuppressWarnings("deprecation")
    RowImpl createRowFromBytes(byte[] keyBytes,
                               byte[] valueBytes,
                               boolean keyOnly) {
        RowImpl fullKey = createRowFromKeyBytes(keyBytes);
        /*
         * If createRowFromKeyBytes returns null, then the serialized key
         * doesn't match the table's key.  It may, however, return a false
         * positive if the key belongs to a descendent in the parent-child
         * table hierarchy.  Hence the extra test for matching table Ids.
         */
        if (fullKey != null && getId() == fullKey.getTableImpl().getId()) {
            /*
             * The length check is pure paranoia, but doesn't hurt.
             * See header comment above.
             */
            if (keyOnly || valueBytes.length == 0) {
                return fullKey;
            }
            Value.Format format = Value.Format.fromFirstByte(valueBytes[0]);
            if (format == Value.Format.TABLE ||
                (format == Value.Format.AVRO && r2compat)) {
                int offset = 1;
                if (format == Value.Format.AVRO && r2compat) {
                    offset =
                        PackedInteger.getReadSortedIntLength(valueBytes, 0);
                }

                if (initRowFromByteValue(fullKey, valueBytes,
                                         format, offset)) {
                    return fullKey;
                }
            }
        }
        return null;
    }

    private boolean initRowFromKeyBytes(RowImpl row,
                                        BinaryKeyIterator keyIter,
                                        TableImpl targetTable) {
        Iterator<String> pkIter = targetTable.getPrimaryKey().iterator();
        return targetTable.fillInKeyForTable(row, keyIter, pkIter);
    }

    /**
     * Size of the value is the length of the serialized value plus
     * a format byte.
     *
     * TODO: if zero-length empty values are supported, don't add one.
     */
    int getDataSize(Row row) {
        Value value = createValue(row);
        return value.getValue().length + 1;
    }

    int getKeySize(Row row) {
        return createKey(row, true).toByteArray().length;
    }

    /**
     * Serialize the non-key fields into an Avro record.
     * Special cases:
     * 1. NullValue in a nullable field.  Avro wants these to be null entries
     * in the record.  Similarly, on reconstruction (rowFromValue) null Avro
     * record entries turn into NullValue instances in the Row.
     * 2. Default values.  If a field is both optional AND not set in the Row,
     * put its default value into the Avro record.  Required fields are just
     * that -- required.
     */
    @SuppressWarnings("deprecation")
    Value createValue(Row row) {

        setSchema(false);
        if (schema == null) {
            return Value.internalCreateValue(new byte[0], Format.TABLE);
        }
        boolean isAvro = (schemaId != 0 && getTableVersion() == 1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        /*
         * If this is a normal table, write the table/schema version to the
         * stream.
         *
         * If this is a table that overlays R2 (Avro) data and it has not been
         * evolved (which excludes direct KV access) then it must be
         * written using the AVRO Value.Format in order to be readable by
         * a pure key/value application doing mixed access.
         * Evolved R2 table overlays will have a table version > 1.
         */
        if (!isAvro) {
            int writeVersion = getTableVersion();
            outputStream.write(writeVersion);
            setTableVersion(row);
        } else {
            final int size =
                PackedInteger.getWriteSortedIntLength(schemaId);
            final byte[] buf = new byte[size];
            /* Copy in the schema ID. */
            PackedInteger.writeSortedInt(buf, 0, schemaId);
            outputStream.write(buf, 0, size);
            ((RowImpl)row).setTableVersion(1);
        }

        Encoder e = TableJsonUtils.getEncoderFactory().
            binaryEncoder(outputStream, null);

        try {
            writeAvroRecord(e, (RecordValueImpl) row, true);
            e.flush();
            return Value.internalCreateValue
                (outputStream.toByteArray(),
                 isAvro ? Value.Format.AVRO : Value.Format.TABLE);
        } catch (IOException ioe) {
            throw new IllegalCommandException("Failed to serialize Avro: " +
                                              ioe);
        }
    }

    /**
     * Deserialize the record value that is encoded in Avro.
     *
     * Offset is requires because on the client side the byte offset is 0 but
     * on the server side a "raw" database record is used which includes an
     * empty first byte added by the system.
     *
     * There is a special case where the table version cannot be acquired.
     * When a key-only table has a non-key field added (the only evolution
     * that can happen for key-only tables, really), there may be empty
     * records in which case the data array is empty.  In this case
     * there may be schema-evolved fields that need to be defaulted so
     * this method must be called regardless of data length.
     *
     * R2/KV compatibility NOTE:
     * If the table overlays R2 (KV) data, treat it specially because it
     * may not have a table version in the data.  Unevolved R2 overlays
     * will have table version 1 and the data will start with the encoded
     * schema id.  Evolved R2 overlays will have table version > 1 and
     * values may either (1) have the encoded schema id (first byte < 0) or
     * be newly-written values, which will have the table format (1) as
     * the first byte and table version used for write as the second byte.
     *
     * This is public to allow access from the query processor.
     */
    @SuppressWarnings("deprecation")
    public boolean initRowFromByteValue(RowImpl row, byte[] data,
                                 Value.Format format, int offset) {

        /*
         * Decode the serialized data if the record is not key-only
         */
        if (data.length >= (offset + 1)) {
            Schema writerSchema = schema;
            int tableVersion = (format == Value.Format.AVRO ? 1
                                : data[offset]);
            row.setTableVersion(tableVersion);
            /*
             * If table versions don't match get the writer schema unless
             * this table overlays KV records in which case there won't be
             * a valid table version in the first byte.
             */
            if (tableVersion != getTableVersion() &&
                tableVersion > numTableVersions()) {

                /*
                 * Throw TableVersionException so the caller can
                 * get the appropriate metadata and retry or take
                 * other appropriate action.
                 */
                throw new TableVersionException(tableVersion);
            }

            try {
                if (tableVersion != getTableVersion()) {
                    String schemaString =
                        generateAvroSchema(tableVersion, false);
                    writerSchema = new Schema.Parser().parse(schemaString);
                }
                /*
                 * If a "normal" table, or operating on the client side (offset
                 * 0), move the offset past table version byte.
                 */
                if (!(format == Value.Format.AVRO) || offset == 0) {
                    offset += 1;
                }
                Decoder decoder =
                    TableJsonUtils.getDecoderFactory().binaryDecoder
                    (data, offset, (data.length - offset), null);

                SimpleAvroReader reader =
                    new SimpleAvroReader(writerSchema, schema, row);
                reader.read(decoder);
                return true;
            } catch (Exception e) {
                /*
                 * Exception is a big catch-all. It's possible to enumerate
                 * the possibilities, but they all end up returning false.
                 * The reason to do this might be client-side logging but
                 * there's no easy way to get the logger from here.
                 */
                return false;
            }
        }
        fillInDefaultValues(row);
        return true;
    }

    /*
     * The stored record was key-only. In the face of possible schema
     * evolution this does not mean that the returned row should not
     * contain any fields. It's possible that the current (expected)
     * table version has added non-key fields to a previously key-only
     * table. Such fields need to either be initialized with null or
     * default values.
     *
     * Default values for new fields added to non-key-only tables are
     * handled in the decoding code called above (SimpleAvroReader)
     * because default values are returned by the ResolvingDecoder.
     */
    private void fillInDefaultValues(RecordValueImpl row) {
        for (Map.Entry<String, FieldMapEntry> entry :
                 getFields(version).entrySet()) {
            FieldMapEntry fme = entry.getValue();
            String fieldName = entry.getKey();
            if (!isKeyComponent(fieldName)) {
                if (fme.hasDefaultValue()) {
                    row.put(fieldName, fme.getDefaultValue());
                } else if (fme.isNullable()) {
                    row.putNull(fieldName);
                }
            }
        }
    }

    /**
     * Create a Row from the Value.
     */
    @SuppressWarnings("deprecation")
    public RowImpl rowFromValueVersion(ValueVersion vv, RowImpl row) {

        assert row != null;

        row.setVersion(vv.getVersion());

        if (vv.getValue() == null) {
            return row;
        }

        byte[] data = vv.getValue().getValue();

        /*
         * If the value is not the correct format this is a non-table
         * record, skip it silently.  Empty table records will have the
         * TABLE format as well as data.length == 0.  Empty table records
         * (and empty KV records) are not distinguishable so let them pass.
         */
        Value.Format format = vv.getValue().getFormat();
        if ((format != Value.Format.TABLE) &&
            (format != Value.Format.AVRO || !r2compat) &&
            (data.length > 1)) {
            return null;
        }

        /*
         * Do the check for schema after the check for the correct format
         * to filter out non-table rows in the case where the table is key-only
         * and there is a KV key in the key space that doesn't belong to the
         * table. If there is no schema the table is currently key-only, which
         * means that all non-key fields should be null, and there are no
         * default values, so just return.
         */
        if (setSchema(false) == null) {
            return row;
        }

        if (initRowFromByteValue(row, data, format, 0)) {
            return row;
        }
        return null;
    }

    /**
     * Evolve a table by adding a new version associated with a new set of
     * fields or a new TTL.  Evolutionary changes are limited to adding/removing
     * non-key fields.  Evolution is always relative to the latest version.
     *
     * When evolution occurs this method will be called twice.  The first time
     * is on the client side where the changes are made transiently.  The
     * second time is on the server when the metadata is to be updated.  That
     * is where the version check can fail.
     */
    void evolve(FieldMap newFields, TimeToLive newTTL) {
        if (version == 255) {
            throw new IllegalCommandException
                ("Can't evolve the table any further; too many versions");
        }

        validateEvolution(newFields);

        /*
         * it's not legal to evolve a version other than the latest one
         */
        if (version != 0 && (version != versions.size())) {
            throw new IllegalCommandException
                ("Table evolution must be performed on the latest version");
        }
        versions.add(newFields);
        ttl = newTTL;
        if (version != 0) {
            setVersion(version + 1);
        }
        setSchema(true);
    }

    /**
     * Validates a specific field for schema evolution.  It needs to do a few
     * things:
     *  1) validate that the name doesn't exist in the current version of the
     *     table.  See (3) for future exceptions.
     *  2) validate that if the field is being resurrected from an earlier
     *     version of the table that the type and constraints match.
     *  3) future -- allow constraints or other things to change even if the
     *     field exists in the current version.
     */
    void validateFieldAddition(final String fieldName,
                               final FieldMapEntry field) {
        if (findTableField(fieldName) != null) {
            throw new IllegalArgumentException
                ("Cannot add field, " + fieldName + ", it already exists");
        }

        /*
         * Try to find the named field in older table versions and if found,
         * do more validation.  This loop checks the current version as well.
         * This is harmless and the code is simpler this way.
         */
        for (FieldMap map : versions) {
            FieldDef def = findTableField(new TablePath(map, fieldName));
            if (def != null) {

                /*
                 * Insist that the FieldDef instances match.  In the
                 * future this may be more flexible and allow some differences
                 * that are compatible with schema evolution -- e.g. min, max,
                 * default.  Description changes will not be flagged as it's
                 * not used in the equals comparison.
                 */
                if (!def.equals(field.getField())) {
                    throw new IllegalArgumentException
                        ("Cannot add field, " + fieldName +
                         ". A version " +
                         "of the table contains this name and the types do " +
                         "not match, is: " + field.getField().getType() +
                         ", was: " + def.getType());
                }
            }
        }
    }

    /**
     * Does the table have a value or is it key-only?  Key-only tables
     * can avoid some unnecessary work.
     */
    boolean hasValueFields() {
        return schema != null;
    }

    /**
     * Validation of individual evolution steps is performed on the front end
     * when modifying fields. A few additional checks are done here.
     *
     * These operations are not allowed:
     * 1.  change fields in primary key
     * 2.  remove fields that participate in an index
     */
    private void validateEvolution(FieldMap newFields) {

        /*
         * Make sure primary key is intact.  Do this in a loop on primary
         * key fields vs above because it's more efficient.
         */
        for (String fieldName : primaryKey) {
            FieldDef oldDef = getField(fieldName);
            FieldDef newDef = newFields.get(fieldName);
            if (!oldDef.equals(newDef)) {
                throw new IllegalCommandException
                    ("Evolution cannot modify the primary key");
            }
        }

        /*
         * Keys need not be validated because they cannot be modified
         * at this time, but if minor modifications to primary key fields
         * are allowed (description, default value), this should be called
         * for extra safety:
         * validate();
         */

        /*
         * Make sure indexed fields are intact.
         */
        for (Index index : indexes.values()) {
            for (String field : index.getFields()) {

                /*
                 * Use findTableField in order to descend into nested fields.
                 */
                FieldDefImpl def = findTableField(newFields, field);
                if (def == null) {
                    throw new IllegalCommandException
                        ("Evolution cannot remove indexed fields");
                }
                FieldDefImpl origDef = findTableField(field);

                if (!def.equals(origDef)) {
                    throw new IllegalCommandException
                        ("Evolution cannot modify indexed fields");
                }
            }
        }
    }

    /**
     * Create a JSON representation of the table and format
     */
    public String toJsonString(boolean pretty) {
        ObjectWriter writer = JsonUtils.createWriter(pretty);
        ObjectNode o = JsonUtils.createObjectNode();
        o.put("type", "table");
        o.put("name", getName());
        if (getDefaultTTL() != null) {
            o.put(TTL, getDefaultTTL().toString());
        }
        o.put("owner", owner == null ? null : owner.toString());
        if (sysTable) {
            o.put("sysTable", sysTable);
        }
        if (r2compat) {
            o.put("r2compat", r2compat);
        }

        if (description != null) {
            o.put(DESC, description);
        }

        if (parent != null) {
            o.put("parent", parent.getName());
        }
        ArrayNode key = o.putArray("shardKey");
        for (String fieldName : shardKey) {
            key.add(fieldName);
        }
        key = o.putArray("primaryKey");
        for (String fieldName : primaryKey) {
            key.add(fieldName);
        }
        if (primaryKeySizes != null) {
            key = o.putArray(PKEY_SIZES);
            for (int size : primaryKeySizes) {
                key.add(size);
            }
        }

        /*
         * Add child tables as an array of table names.
         */
        if (children.size() != 0) {
            ArrayNode childArray = o.putArray("children");
            for (Map.Entry<String, Table> childEntry : children.entrySet()) {
                childArray.add(childEntry.getKey());
            }
        }

        /*
         * Add the fields.
         */
        getFieldMap().putFields(o);

        /*
         * Add indexes
         */
        if (indexes.size() != 0) {
            ArrayNode indexArray = o.putArray("indexes");
            for (Map.Entry<String, Index> indexEntry : indexes.entrySet()) {
                IndexImpl impl = (IndexImpl) indexEntry.getValue();
                impl.toJsonNode(indexArray.addObject());
            }
        }

        /*
         * Format the JSON into a string
         */
        try {
            return writer.writeValueAsString(o);
        } catch (IOException ioe) {
            return ioe.toString();
        }
    }

    /**
     * Formats the table.  If fields is null format the entire
     * table, otherwise, just use the specified fields.  The field names
     * may be nested (i.e. multi-component dot notation).
     *
     * Returned JSON is pretty-printed unconditionally.
     * TODO:
     *  tabular output
     *
     * @param asJson true if output should be JSON, otherwise tabular.
     * @param fields array of field names to use if not null.
     */
    public String formatTable(boolean asJson, String[] fields) {
        if (asJson) {
            if (fields == null) {
                return toJsonString(true);
            }
            ObjectWriter writer = JsonUtils.createWriter(true);
            ObjectNode o = JsonUtils.createObjectNode();
            ArrayNode array = o.putArray(FIELDS);
            for (String fieldName : fields) {
                ObjectNode fnode = array.addObject();
                FieldMapEntry entry =
                    getFieldMapEntry(fieldName, false, true);
                if (entry == null) {

                    /*
                     * Anonymous fields (map and array elements) are referenced
                     * using "[]".  If so, try getting the field definition
                     * directly.
                     */
                    TablePath tablePath = new TablePath(this, fieldName);

                    if (tablePath.isComplex()) {
                        FieldDefImpl def = findTableField(tablePath);
                        if (def != null) {
                            fnode.put(NAME,
                                      translateToExternalField
                                      (fieldName));
                            def.toJson(fnode);
                            continue;
                        }
                    }
                    throw new IllegalArgumentException
                        ("No such field in table " + getFullName() + ": " +
                         fieldName);
                }
                fnode.put(NAME, translateToExternalField(fieldName));
                entry.toJson(fnode);
            }

            /*
             * Format the JSON into a string
             */
            try {
                return writer.writeValueAsString(o);
            } catch (IOException ioe) {
                throw new IllegalArgumentException
                    ("Failed to serialize table description: " +
                     ioe.getMessage());
            }
        }
        /* TODO: tabular */
        return null;
    }

    /**
     * Add Index objects during construction.  Check for the same indexed
     * fields in a different index name.  Do not allow this.
     */
    public void addIndex(Index index) {
        checkForDuplicateIndex(index);
        indexes.put(index.getName(), index);
    }

    /**
     * Remove an Index.
     */
    public Index removeIndex(String indexName) {
        return indexes.remove(indexName);
    }

    /**
     * Create and return a BinaryKeyIterator based on this table.  If this is
     * a top-level table the first component of the key must match the table
     * id.  If this is a child table it is assumed that the key is well-formed
     * and the parent's primary key is skipped and this child's id must match.
     *
     * If a match is not found null is returned.
     */
    BinaryKeyIterator createBinaryKeyIterator(byte[] key) {
        final BinaryKeyIterator keyIter =
            new BinaryKeyIterator(key);
        if (parent != null) {
            for (int i = 0; i < parent.getNumKeyComponents(); i++) {
                if (keyIter.atEndOfKey()) {
                    return null;
                }
                keyIter.skip();
            }
        }
        if (keyIter.atEndOfKey()) {
            return null;
        }
        final String tableId = keyIter.next();
        if (getIdString().equals(tableId)) {
            return keyIter;
        }
        return null;
    }

    /**
     * Convenience wrapper for createBinaryKeyIterator + findTargetTable.
     */
    public TableImpl findTargetTable(byte[] key) {
        BinaryKeyIterator iter = createBinaryKeyIterator(key);
        if (iter != null) {
            return findTargetTable(iter);
        }
        return null;
    }

    /**
     * Find the target table for this key in this table's hierarchy.
     * The caller has set the BinaryKeyIterator on this table's id
     * in the key and it matches.  At this point, consume key entries
     * until this table's primary key count is done.  The primary key
     * contribution from parent tables must be skipped.
     */
    TableImpl findTargetTable(BinaryKeyIterator keyIter) {
        int numPrimaryKeyComponentsToSkip = primaryKey.size();
        if (parent != null) {
            numPrimaryKeyComponentsToSkip -= parent.primaryKey.size();
        }

        /* Match up the primary keys with the input keys, in number only */
        for (int i = 0; i < numPrimaryKeyComponentsToSkip; i++) {
            /* If the key is short, no match */
            if (keyIter.atEndOfKey()) {
                return null;
            }
            keyIter.skip();
        }

        /* If both are done we have a match */
        if (keyIter.atEndOfKey()) {
            return this;
        }

        /* There is another component, check for a child table */
        final String childId = keyIter.next();
        for (Table table : children.values()) {
            if (((TableImpl)table).getIdString().equals(childId)) {
                return ((TableImpl)table).findTargetTable(keyIter);
            }
        }
        return null;
    }

    /*
     * Internal methods, some for the class, some for the package.
     */

    /**
     * Is the field part of the primary key? This is public for test access.
     */
    public boolean isKeyComponent(String fieldName) {
        for (String component : primaryKey) {
            if (fieldName.equalsIgnoreCase(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is the field in an index on this table?
     */
    boolean isIndexKeyComponent(String fieldName) {
        for (Index index : indexes.values()) {
            if (((IndexImpl)index).containsField(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * List of versions is 0 indexed, actual versions start at 1, so
     * subtract when indexing.  0 means get the default (latest) version.
     */
    private Map<String, FieldMapEntry> getFields(final int version1) {
        return getFieldMap(version1).getFields();
    }

    private List<String> getFieldOrder(final int version1) {
        return getFieldMap(version1).getFieldOrder();
    }

    private FieldMap getFieldMap(final int version1) {
        if (versions.size() < version1 || version1 < 0) {
            throw new IllegalCommandException
                ("Table version " + version1 + " does not exist for table " +
                 name);
        }
        int versionToGet = (version1 == 0) ? versions.size() : version1;
        return versions.get(versionToGet - 1);
    }

    private void throwMissingState(String state) {
        throw new IllegalCommandException
            ("Table is missing state required for construction: " + state);
    }

    /**
     * Validate the parameters, primary key, and shard key.
     * IllegalCommandException is thrown vs IllegalArgumentException because
     * this could be run on the server side and IAE will cause the server to
     * crash.
     */
    private void validate() {

        if (primaryKey.isEmpty()) {
            throwMissingState("primary key");
        }

        if (name == null) {
            throwMissingState("table name");
        }

        FieldMap fields = getFieldMap(0);
        if (fields == null || fields.isEmpty()) {
            throwMissingState("no fields defined");
        }

        /**
         * Primary key for child tables has to have at least one
         * component in addition to parent's key.
         */
        if (parent != null) {
            if (!(primaryKey.size() > parent.primaryKey.size())) {
                throw new IllegalCommandException
                    ("Child table needs a primary key component");
            }
        }

        /**
         * Make sure that the shardKey is a strict subset of primary key
         */
        if (shardKey.size() > primaryKey.size()) {
            throw new IllegalCommandException
                ("Shard key must be a subset of the primary key");
        }
        for (int i = 0; i < shardKey.size(); i++) {
            String pkField = primaryKey.get(i);
            if (pkField == null || !pkField.equals(shardKey.get(i))) {
                throw new IllegalCommandException
                    ("Shard key must be a subset of the primary key");
            }
        }

        /*
         * Validate the primary key fields.  The properties of nullable and
         * default values are not relevant to primary keys, so they are
         * ignored.
         */
        for (int i = 0; i < primaryKey.size(); i++) {
            String pkField = primaryKey.get(i);
            FieldDef field = getField(pkField);
            if (field == null) {
                throw new IllegalCommandException
                    ("Primary key field is not a valid field: " +
                     pkField);
            }
            if (!field.isValidKeyField()) {
                throw new IllegalCommandException
                    ("Field type cannot be part of a primary key: " +
                     field.getType() + ", field name: " + pkField);
            }
            if (primaryKeySizes != null) {
                validateKeyFieldSize(field, primaryKeySizes.get(i));
            }
        }
    }

    private void validateKeyFieldSize(FieldDef field, int size) {
        if (size != 0 && !(field.isInteger())) {
                throw new IllegalCommandException
                    ("Only Integer sizes can be constrained. Invalid type: " +
                     field.getType());
        }

        /* 0 means no restriction */
        if (size != 0) {
            if (size < 1 || size > 5) {
                throw new IllegalCommandException
                    ("Size constraint value on primary key must be between " +
                     "1 and 5. Invalid value: " + size);
            }
        }
    }

    /**
     * Deserialize a record encoded in avro. This API is used by the export
     * utility to deserialize a record using the version of the table that was
     * exported and NOT the latest evolved version of the table.
     *
     * @param writerSchema avro schema used to write the record value
     * @param readerSchema avro schema used to read the record value
     * @param row
     * @param data record in bytes
     * @param offset
     * @param tableVersion version of the table used for export
     */
    public void createExportRowFromValueSchema(Schema writerSchema,
                                               Schema readerSchema,
                                               RowImpl row,
                                               byte[] data,
                                               int offset,
                                               int tableVersion) {

        if (data.length >= (offset + 1)) {
            /*
             * Move the offset past table version byte.
             */
            offset++;

            if (readerSchema == null) {
                readerSchema = schema;
            }

            Decoder decoder = TableJsonUtils.getDecoderFactory().binaryDecoder
                (data, offset, (data.length - offset), null);

            SimpleAvroReader reader =
                new SimpleAvroReader(writerSchema, readerSchema, row);
            try {
                reader.read(decoder);
            } catch (Exception e) {
                /*
                 * Return row without the value portion
                 * Fall through
                 */
            }
            return;
        }
        fillInDefaultValues(row);
    }

    /**
     * Use table schema (primary key) to create a Row record with values from
     * the key parameter (derived from Key). This is used by the import utility
     * to create a Row record using the key field from an external record. The
     * external record might have been created from a different kvstore and
     * hence may have a different table idString than the table in this store.
     * Table idString mismatch will be ignored since the objective is to
     * populate the table with the key values from an external record. If there
     * is a key mismatch (by field type and number of fields), false is returned
     */
    public boolean createImportRowFromKeyBytes(Row keyRecord,
                                               BinaryKeyIterator keyIter,
                                               Iterator<String> pkIter) {

        if (parent != null) {
            if (!(parent).
                    createImportRowFromKeyBytes(keyRecord, keyIter, pkIter)) {
                return false;
            }
        }

        assert !keyIter.atEndOfKey();

        setTableVersion(keyRecord);
        keyIter.next();

        /*
         * Fill in values for primary key components that belong to this
         * table.
         */
        String lastKeyField = primaryKey.get(primaryKey.size() - 1);

        while (pkIter.hasNext()) {

            /*
             * If the table in the kvstore has more key components than the
             * key components in the record being imported return false. The
             * import utility will reject this record
             */
            if (keyIter.atEndOfKey()) {
                return false;
            }

            String field = pkIter.next();
            String val = keyIter.next();
            FieldDefImpl type = (FieldDefImpl)getField(field);

            try {
                keyRecord.put(field,
                    FieldDefImpl.createValueFromKeyString(val, type));
            } catch (Exception e) {
                return false;
            }

            if (field.equals(lastKeyField)) {
                break;
            }
        }

        return true;
    }

    /**
     * Checks if the table has a schema to initialize and if so, the
     * schema is generated from the table metadata.
     *
     * This method is called whenver the schema is used because the serialized
     * form of the Table does not include the Avro schema so it needs
     * regenerating for newly-obtained tables.
     */
    private Schema setSchema(boolean flush) {
        if (schema == null || flush) {
            String schemaString = generateAvroSchema(version, true);
            if (schemaString == null) {
                schema = null;
            } else {
                schema = new Schema.Parser().parse(schemaString);
            }
        }
        return schema;
    }

    /*
     * Constructs the fully-qualified name for this table, including parent
     * tables.  It is a dot-separated format:
     *      parentName.childName.grandChildName
     *
     * Top-level tables have a single component.
     */
    private void getTableNameInternal(StringBuilder sb) {
        if (parent != null) {
            parent.getTableNameInternal(sb);
            sb.append(SEPARATOR);
        }
        sb.append(name);
    }

    /**
     * Use table schema (primary key) to create a Row record with values
     * from the key parameter (derived from Key).  The algorithm is:
     *  1. if table has a parent, call the parent to fill in its portion
     *  2. fill in portion for this table
     * This ensures that the key components are processed in order, making each
     * table responsible for its primary key components, skipping references,
     * which point to fields in ancestor tables.
     *
     * The List<String> from the Key is {TABLE1 pk1 pk2 TABLE2 pk3 pk4 ...}
     * For example a single top-level table with in Integer primary key:
     *   {"Users", "12345"}
     * Or a composite primary key of firstName, lastName:
     *   {"Users", "jane", "doe"}
     * Or a nested Email table, under Users with pk of "address":
     *   {"Users", "12345", "Email", "jane@foo.com"}
     *
     * NOTE: the above example uses table names in place of table ids and
     * string values for the key components.  The actual algorithm iterates
     * the byte[] form of the Key.
     *
     * This method should only be called for Key objects from the store so they
     * are well-formed in terms of the expected layout.  It does have to be
     * defensive in the face of keys that match a table in structure but
     * have values that can't be deserialized correctly.  This can happen
     * if there is mixed access between KV and table applications.  An example
     * is a too-long string that can't be turned into an integer.
     *
     * Unfortunately if the key really isn't supposed to be in the table AND
     * it deserializes without an exception this will succeed.  For this,
     * and other reasons, mixing keyspace for tables and non-tables is
     * not supported.
     *
     * @return true if the key was deserialized in full, false otherwise.
     *
     * This method must not throw exceptions.
     */
    private boolean fillInKeyForTable(Row keyRecord,
                                      BinaryKeyIterator keyIter,
                                      Iterator<String> pkIter) {
        if (parent != null) {
            if (!(parent).fillInKeyForTable(keyRecord, keyIter, pkIter)) {
                return false;
            }
        }
        assert !keyIter.atEndOfKey();

        setTableVersion(keyRecord);
        String keyComponent = keyIter.next();

        if (!keyComponent.equals(getIdString())) {
            return false;
        }

        /*
         * Fill in values for primary key components that belong to this
         * table.
         */
        String lastKeyField = primaryKey.get(primaryKey.size() - 1);

        while (pkIter.hasNext()) {
            assert !keyIter.atEndOfKey();
            String field = pkIter.next();
            String val = keyIter.next();
            FieldDefImpl type = (FieldDefImpl)getField(field);
            try {
                keyRecord.put(
                    field, FieldDefImpl.createValueFromKeyString(val, type));
            } catch (Exception e) {
                return false;
            }
            if (field.equals(lastKeyField)) {
                break;
            }
        }
        return true;
    }

    /**
     * Generate Avro schema from the table schema.
     *
     * Fields that are part of the key are Key components and not part of the
     * generated Avro schema
     *
     * Fields that are not part of the key are serialized via Avro, so they
     * are part of the schema.  Each FieldDef object knows how to generate Avro
     * schema definitions in JSON format using the Jackson interface.
     *
     * TODO: maybe use Avro's schema generation API when it's available
     *
     * NOTE: Avro does not allow duplication of field names for fields of type
     * RECORD, ENUM, and FIXED_BINARY, even if the names have different types
     * (e.g. an enum named foo and fixed_binary named foo).  It doesn't matter
     * where in the schema these fields are declared.  One possible attempt
     * to allow this involved generating unique names when generating the schema
     * in this code that code it calls.  This works, but does not work when
     * serialization/deserializing, where the data-oriented code needs to know
     * the generated names as well. A possible solution is to modify the table
     * metadata to keep both names internally -- one for display and another
     * for serialization/deserialization.  That is a schema change and can be
     * considered for the future.
     *
     * The issue above impacts the ability to easily modularize types that want
     * to (re)use names as well as making it harder to map existing schemas
     * that duplicate names to tables.
     *
     * @param versionToUse the table version to use.  Most callers used the
     * current version.
     *
     * @param pretty set to true to generate a pretty-printed JSON string
     *
     * @return the JSON string representing the schema, or null if there are
     * no serializable fields in the table, in which case this is a key-only
     * table.
     *
     * This is public access so it can be used by export code.
     */
    public String generateAvroSchema(final int versionToUse, boolean pretty) {
        boolean hasSchema = false;
        ObjectWriter writer = JsonUtils.createWriter(pretty);
        ObjectNode sch = JsonUtils.createObjectNode();
        sch.put("type", "record");
        String schemaName = getName();

        /* Replace dollar sign of system table name for Avro compatability */
        if (sysTable) {
            schemaName = schemaName.replace("$", "_");
        }
        sch.put("name", schemaName);
        ArrayNode array = sch.putArray("fields");

        Map<String, FieldMapEntry> mapToUse = getFields(versionToUse);
        for (String fname : getFieldOrder(versionToUse)) {
            FieldMapEntry fme = mapToUse.get(fname);
            if (!isKeyComponent(fname)) {
                hasSchema = true;
                ObjectNode fnode = array.addObject();
                fnode.put("name", fname);
                /*
                 * Add default value and doc (description).
                 */
                fme.createAvroTypeAndDefault(fnode);
                if (fme.getField().getDescription() != null) {
                    fnode.put("doc", fme.getField().getDescription());
                }
            }
        }
        if (!hasSchema) {
            return null;
        }
        try {
            return writer.writeValueAsString(sch);
        } catch (IOException ioe) {
            /* this should not happen */
            throw new IllegalStateException
                ("IO Error writing Avro schema string", ioe);
        }
    }

    @Override
    public String toString() {
        return "Table[" + name + ", " +
            (parent == null ? "-" : parent.getFullName()) + ", " +
            indexes.size() + ", " +
            children.size() + ", " + status + ", " + getTableVersion() + "]";
    }

    /**
     * Finds the named table in this table's hierarchy.
     *
     * @param fullName a fully-qualified table name which must exist in
     * this table's hierarchy.  This means it has at least 2 components.
     *
     * This table (the starting table) must be a top-level table.
     *
     * @throws IllegalArgumentException if any component cannot be found.
     */
    private TableImpl findTable(String fullName) {
        String[] path = parseFullName(fullName);
        if (!path[0].equals(name)) {
            throw new IllegalArgumentException
                ("No such table: " + fullName);
        }
        Table target = this;
        for (int i = 1; i < path.length; i++) {
            target = target.getChildTable(path[i]);
            if (target == null) {
                throw new IllegalArgumentException
                    ("No such table: " + fullName);
            }
        }
        return (TableImpl) target;
    }

    /**
     * Returns true if the target table is an ancestor of the start table.
     * Uses equality of ids, which is cheaper than full table equality.
     *
     * Id equality may not work for transiently constructed tables, but
     * that is not the target for this code.
     */
    private static boolean isAncestorOf(TableImpl start, TableImpl target) {
        TableImpl currentParent = start.parent;
        while (currentParent != null) {
            if (currentParent.id == target.id) {
                return true;
            }
            currentParent = currentParent.parent;
        }
        return false;
    }

    public static void validateComponent(String component, boolean isId) {
        validateComponent(component, isId, false);
    }

    /**
     * Table, field, and index names are constrained to
     * alphanumeric characters plus "_".  They must also start with
     * a letter.  This is necessary for Avro schema, which only applies to
     * field names but it's simpler to enforce the restriction for all strings.
     */
    public static void validateComponent(String comp,
                                         boolean isId,
                                         boolean allowDot) {
        List<String> components =
            (allowDot ?
             new TablePath((FieldMap)null, comp).getSteps() :
             new ArrayList<String>());

        if (!allowDot) {
            components.add(comp);
        }

        for (String component : components) {
            if (!component.matches(VALID_NAME_CHAR_REGEX)) {
                throw new IllegalCommandException
                    ("Table, index and field names may contain only " +
                     "alphanumeric values plus the character \"_\": " + comp);
            }

            if (!Character.isLetter(component.charAt(0)) ||
                (component.charAt(0) == '_')) {
                throw new IllegalCommandException
                    ("Table, index and field names " +
                     "must start with an alphabetic character");
            }

            if (isId && (component.length() > MAX_ID_LENGTH)) {
                throw new IllegalCommandException
                    ("Illegal name: " + comp +
                     ". Table names must be less than or equal to " +
                     MAX_ID_LENGTH + " characters.");
            }
            if (!isId && (component.length() > MAX_NAME_LENGTH)) {
                throw new IllegalCommandException
                    ("Illegal name: " + component +
                     ". Field and index names must be less than or equal to " +
                     MAX_NAME_LENGTH + " characters");
            }
        }
    }

    /**
     * A system table name must start with "SYS$" prefix. The dollar sign of
     * system table prefix name will be replaced with "_" when generate Avro
     * schema. It is neccesary for Avro that the rest of the table name except
     * prefix must be constrainted to alphanumeric characters plus "_".
     */
    public static void validateSystemTableName(String tableName) {
        final String[] nameComps = tableName.split("\\$");
        if (nameComps.length > 2) {
            throw new IllegalArgumentException(
                "System table names must contains only one dollar sign");
        }
        if (nameComps[0].equalsIgnoreCase(SYSTEM_TABLE_PREFIX)) {
            throw new IllegalArgumentException(
                "System table names must start with " + SYSTEM_TABLE_PREFIX);
        }
        validateComponent(nameComps[1], false, false);
    }

    static String[] parseFullName(String fullName) {
        return fullName.split(SEPARATOR_REGEX);
    }

    /*
     * MetadataInfo
     */
    @Override
    public MetadataType getType() {
        return MetadataType.TABLE;
    }

    @Override
    public int getSourceSeqNum() {
        return versions.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Populates the record with the record information from value.  The use
     * of getFields() ensures that the destination record controls the
     * specific fields copied.  This varies for Row and PrimaryKey.  IndexKey
     * does not use this method because it may reference nested fields.
     */
    static void populateRecord(RecordValueImpl record,
                                RecordValue value) {
        if (record instanceof IndexKey) {
            throw new IllegalStateException(
                "Index keys cannot be passed to TableImpl.populateRecord");
        }
        for (String s : record.getFields()) {
            FieldValue v = value.get(s);
            if (v != null) {
                record.put(s, v);
            }
        }
        record.validate();
    }

    /**
     * Checks if a given index is a duplicate of an existing index. Two indices
     * are considered duplicate if all following conditions are true
     *
     * 1. The two indices are of the same type, e.g., both are secondary
     * indices or both are text indices;
     * 2. The two indices share the same field or same set of fields, e.g.,
     * both indices are defined on the same columns.
     *
     * @param index  index to check
     */
    void checkForDuplicateIndex(Index index) {
        for (Map.Entry<String, Index> entry : indexes.entrySet()) {
            final Index existingIndex = entry.getValue();
            if (index.getType().equals(existingIndex.getType()) &&
                index.getFields().equals(existingIndex.getFields())) {
                throw new IllegalCommandException
                    ("Index is a duplicate of an existing index with " +
                     "another name.  Existing index name: " +
                     entry.getKey() + ", new index name: " +
                     index.getName());
            }
        }
    }

    private void setTableVersion(Row row) {
        ((RowImpl)row).setTableVersion(getTableVersion());
    }

    /**
     * See findTableField(TablePath) for semantics.
     * This is internal for now, but public to allow test case access.
     */
    public FieldDefImpl findTableField(String fieldName) {
        return findTableField(getFieldMap(), fieldName);
    }

    static FieldDefImpl findTableField(FieldMap fieldMap, String fieldName) {
        return findTableField(new TablePath(fieldMap, fieldName));
    }

    /**
     * Locates the named field within the table's hierarchy.  The field
     * may be a simple, top-level field, or it may be in dot notation,
     * specifying a field in a nested type (record, map, array of (map|array)).
     * The ultimate field must be an indexable type.  That is checked in the
     * caller.
     *
     * @return the FieldDef for the field or null if the field does not exist.
     */
    static FieldDefImpl findTableField(TablePath field) {

        ListIterator<String> fieldPath = field.iterator();
        assert fieldPath.hasNext();
        FieldDefImpl def =
            (FieldDefImpl) field.getFieldMap().get(fieldPath.next());

        if (def == null || !field.isComplex()) {
            return def;
        }

        /*
         * Call the FieldDef itself to navigate the names.
         */
        assert fieldPath.hasNext();
        return def.findField(fieldPath);
    }

    /**
     * Translate an internal representation to external.  E.g.
     * mapfield._key => keyof(mapfield)
     * mapfield.[] => elementof(mapfield)
     */
    public static String translateToExternalField(String field) {
        if (field.endsWith(KEY_TAG)) {

            /* KEY_TAG is always the last part of the field */
            StringBuilder sb = new StringBuilder();
            String nonKeyPart = /* subtract one for '.' */
                field.substring(0, field.indexOf(KEY_TAG) - 1);
            sb.append(KEYOF).append(nonKeyPart).append(')');
            return sb.toString();
        }
        if (field.contains(ANONYMOUS)) {

            /* ANONYMOUS can have fields after it */
            StringBuilder sb = new StringBuilder();
            int anonIndex = field.indexOf(ANONYMOUS);
            sb.append(ELEMENTOF).append(field.substring(0, anonIndex - 1))
                .append(')');
            /* if there is more path, add it */
            if (field.length() > anonIndex + 1) {
                sb.append(field.substring(anonIndex + 2, field.length()));
            }
            return sb.toString();
        }
        return field;
    }

    /**
     * Translate from an external representation to internal.  E.g.
     * keyof(mapfield) => mapfield._key
     * elementof(mapfield) => mapfield.[]
     *
     * @return the original field if it doesn't contain any keyof() or
     * elementof().  Returns null if the field is illegal.  The caller
     * handles this.
     */
    public static String translateFromExternalField(String field) {
        String lower = field.toLowerCase();
        boolean hasKeyOf = lower.startsWith(KEYOF);
        boolean hasElementOf = lower.startsWith(ELEMENTOF);
        if (hasKeyOf || hasElementOf) {
            StringBuilder sb = new StringBuilder();
            int lp = field.indexOf('(');
            assert lp >= 0; /* KEYOF and ELEMENTOF guarantee this */
            int rp = field.indexOf(')', lp);
            if (rp == -1) {
                return null;
            }

            sb.append(field.substring(lp + 1, rp));
            sb.append(SEPARATOR);
            sb.append(hasKeyOf ? KEY_TAG : ANONYMOUS);
            if (field.length() > rp + 1) {

                /* use the separator from the field */
                sb.append(field.substring(rp + 1, field.length()));
            }
            return sb.toString();
        }
        return field;
    }

    @Override
    public ResourceOwner getOwner() {
        return owner;
    }

    @Override
    public TimeToLive getDefaultTTL() {
        return ttl;
    }

    /**
     * Whether this table is system table, internal use only.
     */
    public boolean isSystemTable() {
        return sysTable;
    }

    /**
     * Whether this table need to be exported, internal use only.
     */
    public boolean dontExport() {
        /*
         * May change in future, currently only system tables don't need to
         * be exported.
         */
        return isSystemTable();
    }

    /**
     * An internal class to do the work of deserializing Avro-encoded table
     * records without creating a tree of objects. The values are decoded
     * directly into the target Row.
     *
     * If the reader and writer schemas are the same this is fast. If they are
     * not the same it's necessary to create a ResolvingDecoder to handle the
     * schema evolution. This is slightly slower, but still a lot faster than
     * creating a tree using a ResolvingDecoder unconditionally, which is what
     * the previous code did.
     *
     * TODO: add the ability to selectively decode, creating sparse Rows
     */
    private static class SimpleAvroReader {
        final private RowImpl row;
        /*
         * The reader (expected) schema. The writer schema does not need to
         * be part of the state.
         */
        final private Schema expected;
        final private ResolvingDecoder resolver; // null if no schema evolution
        final RecordDefImpl recordDef;

        /**
         * @param writer the writer schema used to write the record
         * @param reader the reader schema, which is the one expected by the
         * caller and represents the current state of the table schema
         * @param row the target Row for the data, partially populated, or not
         */
        private SimpleAvroReader(Schema writer, Schema reader, RowImpl row) {
            this.expected = reader;
            this.row = row;
            this.recordDef = row.getDefinition();
            resolver = (writer == reader ? null :
                        getResolvingDecoder(writer, reader));
        }

        /**
         * Construct a resolving decoder to handle schema evolution
         */
        private static ResolvingDecoder getResolvingDecoder(
            Schema actual, Schema expected) {
            try {
                return TableJsonUtils.getDecoderFactory().resolvingDecoder(
                    Schema.applyAliases(actual, expected), expected, null);
            } catch (IOException ioe) {}
            return null;
        }

        private FieldValue read(Decoder in) throws IOException {
            if (resolver != null) {
                return readWithResolver(in);
            }
            readRecord(expected, recordDef, row, in);
            return row;
        }

        private FieldValue readWithResolver(Decoder in)
            throws IOException {

            resolver.configure(in);
            readRecord(expected, recordDef, row, resolver);
            resolver.drain();
            return row;
        }

        private FieldValue read(Schema schema,
                                FieldDefImpl def,
                                Decoder in)
            throws IOException {

            switch (schema.getType()) {

            case RECORD:  return readRecord(schema, def, null, in);
            case ENUM:    return readEnum(def, in);
            case ARRAY:   return readArray(schema, def, in);
            case MAP:     return readMap(schema, def, in);
            case UNION:   return read(
                schema.getTypes().get(in.readIndex()), def, in);
            case FIXED:   return readFixed(schema, def, in);
            case STRING:  return stringDef.createString(in.readString());
            case BYTES:   return readBinary(in);
            case INT:     return integerDef.createInteger(in.readInt());
            case LONG:    return longDef.createLong(in.readLong());
            case FLOAT:   return floatDef.createFloat(in.readFloat());
            case DOUBLE:  return doubleDef.createDouble(in.readDouble());
            case BOOLEAN: return booleanDef.createBoolean(in.readBoolean());
            case NULL:    in.readNull(); return NullValueImpl.getInstance();
            default: throw new IllegalStateException("Unknown type: " +
                                                     schema);
            }
        }

        private RecordValueImpl readRecord(Schema schema,
                                           FieldDefImpl def,
                                           RecordValueImpl record,
                                           Decoder in) throws IOException {
            if (in instanceof ResolvingDecoder) {
                return resolveRecord(def, record, (ResolvingDecoder) in);
            }

            RecordDefImpl rdef = (RecordDefImpl) def;
            if (record == null) {
                record = rdef.createRecord();
            }

            for (Field f : schema.getFields()) {
                record.put(f.name(),
                           read(f.schema(), rdef.getField(f.name()), in));
            }
            return record;
        }

        /**
         * A variant of readRecord that is used for schema evolution
         * between the writer and reader schemas.
         */
        private RecordValueImpl resolveRecord(FieldDefImpl def,
                                              RecordValueImpl record,
                                              ResolvingDecoder in)
            throws IOException {

            RecordDefImpl rdef = (RecordDefImpl) def;
            if (record == null) {
                record = rdef.createRecord();
            }
            for (Field f : in.readFieldOrder()) {
                record.put(f.name(), read(f.schema(),
                                          rdef.getField(f.name()),
                                          in));
            }
            return record;
        }

        private MapValueImpl readMap(Schema schema,
                                     FieldDefImpl def,
                                     Decoder in) throws IOException {

            MapDefImpl mdef = (MapDefImpl) def;
            MapValueImpl map = mdef.createMap();
            Schema valueType = schema.getValueType();
            for (long i = in.readMapStart(); i != 0; i = in.mapNext()) {
                for (long j = 0; j < i; j++) {
                    String key = in.readString();
                    map.put(key, read(valueType,
                                      (FieldDefImpl)mdef.getElement(),
                                      in));
                }
            }
            return map;
        }

        private ArrayValueImpl readArray(Schema schema,
                                         FieldDefImpl def,
                                         Decoder in) throws IOException {

            ArrayDefImpl adef = (ArrayDefImpl) def;
            ArrayValueImpl array = adef.createArray();
            Schema elementType = schema.getElementType();
            for (long i = in.readArrayStart(); i != 0; i = in.arrayNext()) {
                for (long j = 0; j < i; j++) {
                    array.add(read(elementType,
                                   (FieldDefImpl)adef.getElement(),
                                   in));
                }
            }
            return array;
        }

        private FieldValue readEnum(FieldDefImpl def,
                                    Decoder in) throws IOException {

            EnumDefImpl edef = (EnumDefImpl) def;
            return edef.createEnum(in.readEnum());
        }

        private FieldValue readFixed(Schema schema,
                                     FieldDefImpl def,
                                     Decoder in) throws IOException {
            int size = schema.getFixedSize();
            byte[] bytes = new byte[size];
            in.readFixed(bytes, 0, size);
            return def.createFixedBinary(bytes);
        }

        private BinaryValueImpl readBinary(Decoder in)
            throws IOException {
            return binaryDef.createBinary(in.readBytes(null).array());
        }
    }

    /**
     * Below are methods to serialize a Row into an Avro encoding but bypassing
     * the creation of a GenericRecord from the Row. This code uses
     * FieldDefImpl and FieldValueImpl instances as the schema rather than
     * avro schema. This is safe because the table definition is what generates
     * the avro schema in the first place. This code uses the Avro Encoder class,
     * which is responsible for the serialization format.
     */
    private void writeAvro(Encoder encoder,
                           FieldValueImpl fieldValue)
        throws IOException {

        switch (fieldValue.getType()) {
        case INTEGER:
            encoder.writeInt(fieldValue.asInteger().get());
            break;
        case LONG:
            encoder.writeLong(fieldValue.asLong().get());
            break;
        case DOUBLE:
            encoder.writeDouble(fieldValue.asDouble().get());
            break;
        case FLOAT:
            encoder.writeFloat(fieldValue.asFloat().get());
            break;
        case STRING:
            encoder.writeString(fieldValue.asString().get());
            break;
        case BOOLEAN:
            encoder.writeBoolean(fieldValue.asBoolean().get());
            break;
        case BINARY:
            encoder.writeBytes(fieldValue.asBinary().get());
            break;
        case FIXED_BINARY:
            encoder.writeFixed(fieldValue.asFixedBinary().get());
            break;
        case ENUM:
            /*
             * this depends on Avro's indexes on enums being the same as ours
             */
            encoder.writeEnum(fieldValue.asEnum().getIndex());
            break;
        case RECORD:
            RecordValueImpl rval = (RecordValueImpl) fieldValue;
            writeAvroRecord(encoder, rval, false);
            break;
        case MAP:
            writeAvroMap(encoder, (MapValueImpl) fieldValue);
            break;
        case ARRAY:
            writeAvroArray(encoder, (ArrayValueImpl)fieldValue);
            break;
        default:
            throw new IllegalStateException("Unexpected type: " + fieldValue);
        }
    }

    /**
     * Encode/write a record
     * @param encode the Encoder instance responsible for serialization
     * @param record the RecordValueImpl to encode
     * @param isRow true if this is the first call of this method serializing
     * a row. This is needed to filter out primary key components
     */
    private void writeAvroRecord(Encoder encoder,
                                 RecordValueImpl record,
                                 boolean isRow)
        throws IOException {

        /*
         * The complication in this loop is that fields in records may
         * be nullable or not and they may have default values. Not-nullable
         * fields must have default values.
         *
         * Nullable fields are represented in Avro as a union, which is why
         * the writeIndex() calls are necessary to discrimiate the type.
         *
         * Fields must be written in field order because Avro schemas are
         * ordered.
         */
        FieldMap fieldMap = record.getDefinition().getFieldMap();
        for (String fieldName : fieldMap.getFieldOrder()) {
            if (!isRow || !isKeyComponent(fieldName)) {
                FieldValueImpl fv = (FieldValueImpl) record.get(fieldName);
                FieldMapEntry fme = fieldMap.getFieldMapEntry(fieldName);
                if (fv == null || fv.isNull()) {
                    if (fv == null) {
                        fv = fme.getDefaultValue();
                    }
                    if (fv.isNull()) {
                        if (!fme.isNullable()) {
                            throw new IllegalCommandException
                                ("The field can not be null: " + fieldName);
                        }
                        /*
                         * null is always the first choice in the union when
                         * there is no default values
                         */
                        encoder.writeIndex(0);
                        encoder.writeNull();
                        continue;
                    }
                }

                if (fme.isNullable()) {
                    /*
                     * nullable fields with a default value generate schemas
                     * with the default type first in the union.
                     */
                    encoder.writeIndex(fme.hasDefaultValue() ? 0 : 1);
                }
                writeAvro(encoder, fv);
            }
        }
    }

    /**
     * Write a Map
     */
    private void writeAvroMap(Encoder encoder,
                              MapValueImpl mapValue)
        throws IOException {

        encoder.writeMapStart();
        encoder.setItemCount(mapValue.size());
        for (Map.Entry<String, FieldValue> entry :
                 mapValue.getFieldsInternal().entrySet()) {
            encoder.startItem();
            encoder.writeString(entry.getKey());
            writeAvro(encoder, (FieldValueImpl) entry.getValue());
        }
        encoder.writeMapEnd();
    }

    /**
     * Write an Array
     */
    private void writeAvroArray(Encoder encoder,
                                ArrayValueImpl arrayValue)
        throws IOException {

        encoder.writeArrayStart();
        encoder.setItemCount(arrayValue.size());
        for (FieldValue fv : arrayValue.getArrayInternal()) {
            encoder.startItem();
            writeAvro(encoder, (FieldValueImpl) fv);
        }
        encoder.writeArrayEnd();
    }
}
