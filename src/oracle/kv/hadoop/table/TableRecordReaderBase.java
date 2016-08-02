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

package oracle.kv.hadoop.table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import oracle.kv.Consistency;
import oracle.kv.Direction;
import oracle.kv.FaultException;
import oracle.kv.KVSecurityConstants;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.ParamConstant;
import oracle.kv.impl.api.table.TableAPIImpl;
import oracle.kv.impl.security.util.KVStoreLogin;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldRange;
import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableAPI;
import oracle.kv.table.TableIterator;
import oracle.kv.table.TableIteratorOptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * Abstract parent class for RecordReader implementations used to read
 * table rows from an InputSplit. This class provides common, useful,
 * and/or convenient mechanisms that are intended to be shared by
 * subclasses of this class.
 * <p>
 * @since 3.1
 */
abstract class TableRecordReaderBase<K, V> extends RecordReader<K, V> {

    private static final Log LOG =
        LogFactory.getLog("oracle.kv.hadoop.table.TableRecordReaderBase");

    private static final String FILE_SEP =
        System.getProperty("file.separator");
    private static final String USER_SECURITY_DIR =
        System.getProperty("user.dir") + FILE_SEP +
                                         "TABLE_RECORD_READER_SECURITY_DIR";

    private static String[] requiredRangeNames = {"name", "start", "end"};

    private KVStore kvstore;

    /* Iterator input parameters, set in initialize() */
    private PrimaryKey primaryKey;
    private MultiRowOptions rowOpts;
    private TableIteratorOptions itrOpts = null;

    /* List of remaining partitions to read from */
    private List<Set<Integer>> partitionSets;

    /* Initial number of partitions */
    private int startNPartitionSets;

    /* The current iterator */
    private TableIterator<Row> iter;

    protected Row current;

    /**
     * Called once at initialization.
     * @param split the split that defines the range of records to read
     * @param context the information about the task
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void initialize(InputSplit split, TaskAttemptContext context)
        throws IOException, InterruptedException {

        final TableInputSplit tableInputSplit = (TableInputSplit) split;
        final String tableName = tableInputSplit.getTableName();

        final String kvStoreName = tableInputSplit.getKVStoreName();
        final String[] kvHelperHosts = tableInputSplit.getKVHelperHosts();

        if (kvstore == null) {

            final KVStoreConfig storeConfig =
                new KVStoreConfig(kvStoreName, kvHelperHosts);

            final String kvStoreSecurityFile =
                createLocalKVSecurity(tableInputSplit.getSecurityLogin(),
                                      tableInputSplit.getSecurityTrust());
            storeConfig.setSecurityProperties(
                KVStoreLogin.createSecurityProperties(kvStoreSecurityFile));

            /*
             * If the same Hive CLI session is used to run queries that must
             * connect to different KVStores where one store is non-secure and
             * the other(s) is secure, then since the security information is
             * stored in the state of the splits, if an attempt is made to
             * connect to the non-secure store and that security information
             * is non-null, then a FaultException will be thrown when
             * attempting to connect to the store. This is because that
             * non-null security information will cause the connection
             * mechanism to attempt a secure connection instead of a
             * non-secure connection. To address this, FaultException is
             * caught and the connection attempt is retried with no security
             * information.
             */
            try {
                kvstore = KVStoreFactory.getStore(
                  storeConfig, tableInputSplit.getSecurityCredentials(), null);
            } catch (FaultException e) {
                if (tableInputSplit.getSecurityCredentials() != null) {
                    final KVStoreConfig kvStoreConfigNonSecure =
                        new KVStoreConfig(kvStoreName, kvHelperHosts);
                    kvStoreConfigNonSecure.setSecurityProperties(
                        KVStoreLogin.createSecurityProperties(null));
                    kvstore = KVStoreFactory.getStore(kvStoreConfigNonSecure);
                } else {
                    throw e;
                }
            }
        }

        final TableAPI tableApi = kvstore.getTableAPI();
        final Table table = tableApi.getTable(tableName);
        if (table == null) {
            final String msg =
                "Store does not contain table [name=" + tableName + "]";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
        primaryKey =
            getPrimaryKey(table, tableInputSplit.getPrimaryKeyProperty());

        /* Construct MultiRowOptions */
        rowOpts =
            new MultiRowOptions(
                getFieldRange(table, tableInputSplit.getFieldRangeProperty()));

        /* Construct TableIteratorOptions */
        final Direction direction = tableInputSplit.getDirection();
        final Consistency consistency = tableInputSplit.getConsistency();
        final long timeout = tableInputSplit.getTimeout();
        final TimeUnit timeoutUnit = tableInputSplit.getTimeoutUnit();
        final int maxRequests = tableInputSplit.getMaxRequests();
        final int batchSize = tableInputSplit.getBatchSize();

        if (direction != null && consistency != null && timeoutUnit != null) {
            itrOpts = new TableIteratorOptions(direction, consistency, timeout,
                                               timeoutUnit, maxRequests,
                                               batchSize);
        }
        partitionSets = tableInputSplit.getPartitionSets();
        startNPartitionSets = partitionSets.size();
    }

    /**
     * Read the next row.
     * @return true if a key/value pair was read
     */
    @Override
    public boolean nextKeyValue() {

        try {
            if (iter == null) {
                iter = getNextIterator();
            }

            while (iter != null) {
                if (iter.hasNext()) {
                    current = iter.next();
                    LOG.debug("next row: " + current);
                    return true;
                }
                current = null;
                iter = getNextIterator();
            }
            LOG.debug("all iterations complete: return false");
            return false;

        } catch (Exception e) {
            LOG.error("Exception while iterating table [" + e + "]", e);
            return false;
        }
    }

    private TableIterator<Row> getNextIterator() {
        if (partitionSets.isEmpty()) {
            return null;
        }
        final Set<Integer> partitions = partitionSets.remove(0);
        assert partitions.size() > 0;
        return ((TableAPIImpl) kvstore.getTableAPI()).tableIterator(
                                                          primaryKey,
                                                          rowOpts,
                                                          itrOpts,
                                                          partitions);
    }

    /**
     * The current progress of the record reader through its data.
     *
     * @return a number between 0.0 and 1.0 that is the fraction of the data
     * read
     */
    @Override
    public float getProgress() {
        return (partitionSets == null) ? 0 :
                    (float) (startNPartitionSets - partitionSets.size()) /
                                                   (float) startNPartitionSets;
    }

    /**
     * Close the record reader.
     */
    @Override
    public void close()
        throws IOException {
        LOG.debug("close table iterator and kvstore");
        if (iter != null) {
            iter.close();
        }
        if (kvstore != null) {
            kvstore.close();
        }
    }

    /**
     * Constructs a PrimaryKey for iteration from the given String property.
     * This method assumes that the value of the given prop parameter is a
     * list of name:value pairs in JSON FORMAT like the following:
     * <code>
     *   -Doracle.kv.primaryKey="{\"name\":\"stringVal\",\"name\":floatVal}"
     * </code>
     * For example,
     * <code>
     *   -Doracle.kv.primaryKey="{\"make"\":\"ford\",\"price\":23450.23}"
     * </code>
     * where the list itself is enclosed in un-escaped double quotes and
     * corresponding curly brace; and each field name component -- as well
     * as each STRING type field value component -- is enclosed in ESCAPED
     * double quotes.
     *
     * Note that the double quotes that encapsulate the string values
     * referenced by each field name and each string type field value
     * MUST BE ESCAPED; otherwise, a parsing error will occur. This is
     * because the hadoop command interpreter strips off the double quotes
     * surrounding the name and value components before passing the system
     * property on to the Java VM in which the MapReduce job executes.
     * Escaping the double quotes in the way described above preserves
     * the double quotes so that the value of the system property is in
     * valid JSON format when parsed by this method below.
     */
    private PrimaryKey getPrimaryKey(final Table table, final String prop) {

        PrimaryKey retKey = table.createPrimaryKey();
        if (prop == null) {
            /* Send wildcard if the property is null. */
            return retKey;
        }

        final String warnStr =
            "Invalid JSON in property [" +
            ParamConstant.PRIMARY_KEY.getName() + "=" + prop + "]: " +
            "must be a list of name:value pairs in JSON format having " +
            "the form -D" + ParamConstant.PRIMARY_KEY.getName() + "=" +
            "\"{\\\"fieldName\\\":\\\"stringValue\\\"," +
            "\\\"fieldName\\\":floatValue, ... }\"; where the list " +
            "is enclosed in un-escaped double quotes and curly braces, " +
            "and each fieldName component and each STRING type fieldValue " +
            "component is enclosed in ESCAPED double quotes. ";

        final String proceedStr = "Proceeding with full PrimaryKey wildcard.";
        try {
            retKey = table.createPrimaryKeyFromJson(prop, false);
        } catch (IllegalArgumentException e) {
            LOG.warn(warnStr + proceedStr);
            e.printStackTrace();
        }
        return retKey;
    }

    /**
     * Constructs a FieldRange to use in a table iteration from the given
     * String property. This method assumes that the value of the given
     * rangeFieldProp parameter is a list of name:value pairs in JSON FORMAT
     * like the following:
     * <code>
     *   -Doracle.kv.fieldRange="{\"name\":\"fieldName\",
     *      \"start\":\"startVal\",[\"startInclusive\":true|false],
     *      \"end\"\"endVal\",[\"endInclusive\":true|false]}"
     * </code>
     * For example, if the PrimaryKey is specified using the fields,
     * 'primary-key -field type -field make -field model -field color', then
     * one might specify a field range with the following system properties:
     * <code>
     *   -Doracle.kv.primaryKey="{\"type\":\"truck\"}"
     *   -Doracle.kv.fieldRange="{\"name\":\"make\",
     *      \"start\":\"Chrysler\",\"startInclusive\":true,
     *      \"end\"\"GM\",\"endInclusive\":false}"
     * </code>
     * Note that the list itself is enclosed in un-escaped double quotes and
     * corresponding curly brace, but each name component -- as well as each
     * STRING type value component -- is enclosed in ESCAPED double quotes.
     *
     * Note also that the double quotes that encapsulate the string values
     * referenced by each name and each string type value MUST BE ESCAPED;
     * otherwise, a parsing error will occur. This is because the hadoop
     * command interpreter strips off the double quotes surrounding the name
     * and value components before passing the system property on to the
     * Java VM in which the MapReduce job executes. Escaping the double
     * quotes in the way described above preserves the double quotes so that
     * the value of the system property is in valid JSON format when parsed
     * by this method below.
     */
    private FieldRange getFieldRange(final Table table,
                                     final String rangeFieldProp) {

        FieldRange retRange = null;

        if (rangeFieldProp == null) {
            return retRange;
        }

        final String warnStr =
            "Invalid JSON in property [" +
            ParamConstant.FIELD_RANGE.getName() + "=" + rangeFieldProp +
            "]: must be a list of name:value pairs in JSON format having " +
            "the form -D" + ParamConstant.FIELD_RANGE.getName() + "=" +
            "\"{\\\"name\\\":\\\"fieldName\\\"," +
            "\\\"start\\\":\\\"stringStartVal\\\"|scalarStartVal," +
            "[\\\"startInclusive\\\":true|false]," +
            "\\\"end\\\":\\\"stringEndVal\\\"|scalarEndVal," +
            "[\\\"endInclusive\\\":true|false]}\"; where the list " +
            "is enclosed in un-escaped double quotes and curly braces, " +
            "and each name component and each STRING type value component " +
            "is enclosed in ESCAPED double quotes. ";

        final String proceedStr = "Proceeding with full range of values " +
              "for the PrimaryKey rather than a sub-range ";
        try {
            /* TODO: replace the call to the createFieldRange method from this
             *       class with Table.createFieldRange(rangeFieldProp) once
             *       that method is added to oracle.kv.table.Table; that is,
             *
             *       retRange = table.createFieldRange(rangeFieldProp);
             */
            retRange = createFieldRange(table, rangeFieldProp);
        } catch (IllegalArgumentException e) {
            LOG.warn(warnStr + proceedStr);
            e.printStackTrace();
        }
        return retRange;
    }

    /*
     * NOTE: this method is temporary. Once the createFieldRange method is
     *       added to oracle.kv.table.Table, the getFieldRange method above
     *       should be changed to invoke Table.createFieldRange rather than
     *       this method; and this method can be removed.
     */
    private FieldRange createFieldRange(final Table table,
                                        final String rangeFieldProp) {
        FieldRange retRange = null;

        if (rangeFieldProp == null) {
            return retRange;
        }

        final String missingLeftBraceStr =
                     "invalid JSON format: system property does not begin " +
                     "with left curly brace ['{']";
        final String missingRightBraceStr =
                     "invalid JSON format: system property does not end " +
                     "with right curly brace ['}']";
        if (!rangeFieldProp.startsWith("{")) {
            throw new IllegalArgumentException(missingLeftBraceStr);
        }
        if (!rangeFieldProp.endsWith("}")) {
            throw new IllegalArgumentException(missingRightBraceStr);
        }

        final String dq = "\"";
        final String colon = ":";
        final String missingNameBase = "missing required range field name ";

        /* Verify prop contains all required names in form, "requiredName": */
        for (String requiredName : requiredRangeNames) {
            final String missingName =
                      missingNameBase + "[" + requiredName + "]";

            final int indxOf = rangeFieldProp.indexOf(
                                   dq + requiredName + dq + colon);

            if (indxOf < 0) {
                throw new IllegalArgumentException(missingName);
            }
        }

        /* Build FieldRange from system property inside curly braces. */
        String nameVal = null;
        String startVal = null;
        boolean startInclusive = true;
        String endVal = null;
        boolean endInclusive = true;
        FieldDef.Type fieldType = null;

        final String invalidComponentBase =
                     "invalid JSON format: invalid \"name\":\"value\" pair " +
                     "in system property ";

        /* Strip off the enclosing curly braces from system property. */
        final String prop =
                     rangeFieldProp.substring(1, rangeFieldProp.length() - 1);

        final String[] propComponents = prop.split(",");
        for (String propComponent : propComponents) {

            final String invalidComponent =
                      invalidComponentBase + "[" + propComponent + "]";

            /* Each component must be "name":"value" pair; otherwise errorr. */
            final String[] rangeComponents = propComponent.split(":");
            if (rangeComponents.length != 2) {
                throw new IllegalArgumentException(invalidComponent);
            }

            /*
             * Each name must be encapsulated by double quotes (verified above)
             * and each value of type STRING must be encapsulated by double
             * quotes. Scalar values are not encapsulated by double quotes.
             * Strip off double quotes before constructing the FieldRange.
             */
            final String name = rangeComponents[0].substring(
                                          1, rangeComponents[0].length() - 1);

            /* For values, handle both scalars and strings appropriately. */
            final String val = rangeComponents[1];
            if ("name".equals(name.toLowerCase())) {
                /* The name of the field over which to range is a STRING. */
                if (!(val.startsWith(dq) && val.endsWith(dq))) {
                    throw new IllegalArgumentException(invalidComponent);
                }
                nameVal = val.substring(1, val.length() - 1);
                fieldType = table.getField(nameVal).getType();
            } else if ("start".equals(name.toLowerCase())) {
                /* Test for un-matched double quotes. */
                if (val.startsWith(dq) && !val.endsWith(dq)) {
                    throw new IllegalArgumentException(invalidComponent);
                } else if (!val.startsWith(dq) && val.endsWith(dq)) {
                    throw new IllegalArgumentException(invalidComponent);
                } else if (val.startsWith(dq) && val.endsWith(dq)) {
                    startVal = val.substring(1, val.length() - 1); /* string */
                } else {
                    startVal = val; /* scalar */
                }
            } else if ("startinclusive".equals(name.toLowerCase())) {
                startInclusive = Boolean.parseBoolean(val);
            } else if ("end".equals(name.toLowerCase())) {
                /* Test for un-matched double quotes. */
                if (val.startsWith(dq) && !val.endsWith(dq)) {
                    throw new IllegalArgumentException(invalidComponent);
                } else if (!val.startsWith(dq) && val.endsWith(dq)) {
                    throw new IllegalArgumentException(invalidComponent);
                } else if (val.startsWith(dq) && val.endsWith(dq)) {
                    endVal = val.substring(1, val.length() - 1); /* string */
                } else {
                    endVal = val; /* scalar */
                }
            } else if ("endinclusive".equals(name.toLowerCase())) {
                endInclusive = Boolean.parseBoolean(val);
            }
        }

        if (nameVal == null) {
            throw new IllegalArgumentException(invalidComponentBase);
        }
        if (startVal == null && fieldType == null) {
            throw new IllegalArgumentException(invalidComponentBase);
        }

        /* Verification complete. Construct the FieldRange return value. */

        retRange = table.createFieldRange(nameVal);

        if (FieldDef.Type.STRING.equals(fieldType)) {
            retRange.setStart(startVal, startInclusive);
            retRange.setEnd(endVal, endInclusive);
        } else {
            try {
                if (FieldDef.Type.INTEGER.equals(fieldType)) {
                    retRange.setStart(
                        Integer.parseInt(startVal), startInclusive);
                    retRange.setEnd(Integer.parseInt(endVal), endInclusive);
                } else if (FieldDef.Type.LONG.equals(fieldType)) {
                    retRange.setStart(
                        Long.parseLong(startVal), startInclusive);
                    retRange.setEnd(Long.parseLong(endVal), endInclusive);
                } else if (FieldDef.Type.FLOAT.equals(fieldType)) {
                    retRange.setStart(
                        Float.parseFloat(startVal), startInclusive);
                    retRange.setEnd(Float.parseFloat(endVal), endInclusive);
                } else if (FieldDef.Type.DOUBLE.equals(fieldType)) {
                    retRange.setStart(
                        Double.parseDouble(startVal), startInclusive);
                    retRange.setEnd(Double.parseDouble(endVal), endInclusive);
                } else {
                    throw new IllegalArgumentException(invalidComponentBase);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                              new NumberFormatException(invalidComponentBase));
            }
        }
        return retRange;
    }

    /**
     * Convenience method that retrieves the login configuration and trust
     * credentials as resources from the classpath, and writes that
     * information to corresponding files on the user's local file system.
     * The login file and trust file that are created by this method can
     * then be used when attempting to interact with a secure KVStore.
     * <p>
     * This method returns a string containing the fully qualified path to
     * the login file that is created. The login file will reference the
     * name of the trust file that is created; which must be written to
     * the same directory in which the login file resides.
     */
    private static String createLocalKVSecurity(final String loginFlnm,
                                                final String trustFlnm)
                              throws IOException {

        if (loginFlnm == null) {
            return null;
        }

        if (trustFlnm == null) {
            return null;
        }

        /*
         * If loginFile and/or trustFile is a filename and not an absolute
         * path, then generate local versions of the file.
         */
        final File userSecurityDirFd = new File(USER_SECURITY_DIR);
        if (!userSecurityDirFd.exists()) {
            if (!userSecurityDirFd.mkdirs()) {
                throw new IOException("failed to create " + userSecurityDirFd);
            }
        }

        InputStream loginStream = null;
        InputStream trustStream = null;

        final ClassLoader cl = TableRecordReaderBase.class.getClassLoader();
        if (cl != null) {
            loginStream = cl.getResourceAsStream(loginFlnm);
            trustStream = cl.getResourceAsStream(trustFlnm);
        } else {
            loginStream = ClassLoader.getSystemResourceAsStream(loginFlnm);
            trustStream = ClassLoader.getSystemResourceAsStream(trustFlnm);
        }

        /*
         * Retrieve the login configuration as a resource from the classpath,
         * and write that information to the user's local file system.
         */
        final Properties loginProps = new Properties();
        if (loginStream != null) {
            loginProps.load(loginStream);
        }

        /* Strip off the path of the trust file. */
        final String trustProp =
            loginProps.getProperty(
                KVSecurityConstants.SSL_TRUSTSTORE_FILE_PROPERTY);
        if (trustProp != null) {
            final File trustPropFd = new File(trustProp);
            if (!trustPropFd.exists()) {
                loginProps.setProperty(
                    KVSecurityConstants.SSL_TRUSTSTORE_FILE_PROPERTY,
                    trustPropFd.getName());
            }
        }

        final File loginFd =
            new File(USER_SECURITY_DIR + FILE_SEP + loginFlnm);
        final FileOutputStream loginFos = new FileOutputStream(loginFd);
        loginProps.store(loginFos, null);
        loginFos.close();

        /*
         * Retrieve the trust credentials as a resource from the classpath,
         * and write that information to the user's local file system.
         */
        final File trustFd =
            new File(USER_SECURITY_DIR + FILE_SEP + trustFlnm);
        final FileOutputStream trustFlnmFos =
            new FileOutputStream(trustFd);

        if (trustStream != null) {
            int nextByte = trustStream.read();
            while (nextByte != -1) {
                trustFlnmFos.write(nextByte);
                nextByte = trustStream.read();
            }
        }
        trustFlnmFos.close();

        return loginFd.toString();
    }
}
