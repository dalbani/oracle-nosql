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

package oracle.kv.hadoop.hive.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.index.IndexPredicateAnalyzer;
import org.apache.hadoop.hive.ql.index.IndexSearchCondition;
import org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc;
import org.apache.hadoop.hive.ql.plan.ExprNodeDesc;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqualOrGreaterThan;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqualOrLessThan;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPGreaterThan;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import oracle.kv.hadoop.table.TableInputSplit;
import oracle.kv.hadoop.table.TableRecordReader;
import oracle.kv.table.FieldDef;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Table;

/**
 * A Hadoop MapReduce version 1 InputFormat class for reading data from an
 * Oracle NoSQL Database when processing a Hive query against data written
 * to that database using the Table API.
 * <p>
 * Note that whereas this class is an instance of a version 1 InputFormat
 * class, in order to exploit and reuse the mechanisms provided by the
 * Hadoop integration classes (in package oracle.kv.hadoop.table), this class
 * also creates and manages an instance of a version 2 InputFormat.
 */
public class TableHiveInputFormat<K, V>
                 implements org.apache.hadoop.mapred.InputFormat<K, V> {

    private static final Log LOG = LogFactory.getLog(
                   "oracle.kv.hadoop.hive.table.TableHiveInputFormat");

    /*
     * Flag indicating whether the splits are generated (by SplitBuilder)
     * based on the store's partitions, or based on the shards (RepGroups)
     * in the store's topology.
     */
    private static boolean splitOnShards = false;

    /*
     * The set of comparison operations that are currently supported for
     * predicate pushdown; '=', '>=', '>', '<=', '<'.
     */
    private static final List<String> COMPARE_OPS = new ArrayList<String>();
    static {
        COMPARE_OPS.add(GenericUDFOPEqual.class.getName());
        COMPARE_OPS.add(GenericUDFOPEqualOrGreaterThan.class.getName());
        COMPARE_OPS.add(GenericUDFOPGreaterThan.class.getName());
        COMPARE_OPS.add(GenericUDFOPEqualOrLessThan.class.getName());
        COMPARE_OPS.add(GenericUDFOPLessThan.class.getName());
    }

    /**
     * Returns the RecordReader for the given InputSplit.
     * <p>
     * Note that the RecordReader that is returned is based on version 1 of
     * MapReduce, but wraps and delegates to a YARN based (MapReduce version2)
     * RecordReader. This is done because the RecordReader provided for
     * Hadoop integration is YARN based, whereas the Hive infrastructure
     * requires a version 1 RecordReader.
     * <p>
     * Additionally, note that when query execution occurs via a MapReduce
     * job, this method is invoked by backend processes running on each
     * DataNode in the Hadoop cluster; where the splits are distributed to
     * each DataNode. When the query is simple enough to be executed by the
     * Hive infrastructure from data in the metastore -- that is, without
     * MapReduce -- this method is invoked by the frontend Hive processes;
     * once for each split. For example, if there are 6 splits and the query
     * is executed via a MapReduce job employing only 3 DataNodes, then each
     * DataNode will invoke this method twice; once for each of 2 splits in
     * the set of splits. On the other hand, if MapReduce is not employed,
     * then the Hive frontend will invoke this method 6 separate times;
     * one per different split. In either case, when this method is
     * invoked, the given Version 1 <code>split</code> has already been
     * populated with a fully populated Version 2 split; and the state of
     * that encapsulated Version 2 split can be exploited to construct the
     * necessary Version 1 RecordReader encapsulating a fully functional
     * Version 2 RecordReader, as required by YARN.
     */
    @Override
    @SuppressWarnings(value = {"unchecked",
                               /*
                                * Ignore the fact that the v2Reader resource
                                * isn't closed -- we want to return it open!
                                */
                               "resource"})
    public RecordReader<K, V> getRecordReader(InputSplit split,
                                              JobConf job,
                                              Reporter reporter)
        throws IOException {

        LOG.debug("split = " + split);

        splitOnShards = ((TableHiveInputSplit) split).getSplitOnShards();

        final TableInputSplit v2Split =
            ((TableHiveInputSplit) split).getV2Split();

        final TableRecordReader v2Reader = new TableRecordReader();
        try {
            v2Reader.initialize(v2Split, new TableTaskAttemptContext(job));
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        /*
         * Must perform an unchecked cast; otherwise an eclipse warning about
         * RecordReader being a raw type that needs to be parameterized will
         * occur. But an eclipse unchecked warning cannot be avoided because
         * of type erasure; so suppress unchecked warnings in this method.
         */
        return (RecordReader<K, V>) (new TableHiveRecordReader(job, v2Reader));
    }

    /**
     * Returns an array containing the input splits for the given job.
     * <p>
     * Implementation Note: when V1V2TableUtil.getInputFormat() is called by
     * this method to retrieve the TableInputFormat instance to use for a given
     * query, only the VERY FIRST call to V1V2TableUtil.getInputFormat() (after
     * the query has been entered on the command line and the input info for
     * the job has been reset) will construct an instance of TableInputFormat;
     * all additional calls -- while that query is executing -- will always
     * return the original instance created by that first call. Note also
     * that in addition to constructing a TableInputFormat instance, that
     * first call to V1V2TableUtil.getInputFormat() also populates the
     * splitMap; which is achieved via a call to getSplits() on the newly
     * created TableInputFormat instance.
     *
     * Since the first call to V1V2TableUtil.getInputFormat() has already
     * called TableInputFormat.getSplits() and placed the retrieved splits
     * in the splitMap, it is no longer necessary to make any additional
     * calls to TableInputFormat.getSplits(). Not only is it not necessary to
     * call TableInputFormat.getSplits(), but such a call should be avoided;
     * to avoid additional, unnecessary remote calls to KVStore. Thus, avoid
     * calls such as, V1V2TableUtil.getInputFormat().getSplits(); since such a
     * call may result in two successive calls to TableInputFormat.getSplits().
     * To avoid this situation, a two step process like the following should
     * be employed to retrieve and return the desired splits:
     * <p>
     * 1. First call V1V2TableUtil.getInputFormat(); which when called
     *    repeatedly, will always return the same instance of
     *    TableInputFormat.
     * 2. Call V1V2TableUtil.getSplitMap(), then retrieve and return the
     *    desired splits from the returned map.
     */
    @Override
    public InputSplit[] getSplits(JobConf job, int numSplits)
        throws IOException {

        V1V2TableUtil.getInputFormat(job, splitOnShards);

        final Set<TableHiveInputSplit> v1SplitKeySet =
            V1V2TableUtil.getSplitMap(job, splitOnShards).keySet();

        return v1SplitKeySet.toArray(
                   new TableHiveInputSplit[v1SplitKeySet.size()]);
    }

    /**
     * Analyzes the components of the given <code>predicate</code> with
     * respect to the columns of the Hive table (retrieved using the given
     * <code>Deserializer</code>) and if the predicate contains a valid
     * <code>PrimaryKey</code> or <code>IndexKey</code> and optional
     * <code>FieldRange</code>, then will use that information to create
     * return an <code>IndexPredicateAnalyzer</code>. The analyzer returned
     * by this method can be used to decompose the predicate into two
     * disjoint sets:
     * <ul>
     *   <li>A set of search conditions that correspond to the valid
     *       <code>PrimaryKey</code> or <code>IndexKey</code> (and optional
     *       <code>FieldRange</code>) found during the analyzis phase,
     *       that can be pushed to the KVStore server (via one of the
     *       <code>TableIterator</code>s; where filtering can be applied
     *       on the backend rather than the Hive frontend.
     *   <li>A set containing the remaining components of the given
     *       <code>predicate</code> (the <em>residual</em> predicate), which
     *       Hive will apply to the results returned after the pushed
     *       search conditions have first been applied on the backend.
     * </ul>
     *
     * If multiple (search conditions, residual predicate) pairs result from
     * the above analysis, then the analyzer that is returned will be the
     * analyzer corresponding to the optimal search conditions.
     * <p>
     * If no (search conditions, residual predicate) pairs result from the
     * above analysis, (that is, it is determined that no combination of
     * components from the given <code>predicate</code> can be pushed to
     * the backend server), then this method returns <code>null</code>;
     * in which case, all filtering should be performed by Hive on the
     * frontend.
     */
    static IndexPredicateAnalyzer sargablePredicateAnalyzer(
                                      final ExprNodeDesc predicate,
                                      final TableSerDe deserializer) {

        if (deserializer == null) {
            return null;
        }

        final Table table = deserializer.getKvTable();
        if (table == null) {
            return null;
        }
        LOG.debug("sargablePredicateAnalyzer: predicate = " + predicate);

        final List<String> hiveColumnNames =
            deserializer.getSerdeParams().getColumnNames();

        final List<List<String>> sargableList = new ArrayList<List<String>>();
        Integer curListIndx = 0;
        Integer indxOfMaxList = -1;
        Integer maxListSize = Integer.MIN_VALUE;

        /*
         * - IndexKeys -
         *
         * For each index, get the corresponding hive column names and
         * determine if the columns corresponding to the fields of that
         * index can be used to form a predicate that can be pushed.
         * That is, determine if those corresponding fields form a valid
         * IndexKey and/or FieldRange (as defined by the Table API).
         *
         * Note that because Hive and KVStore handle case differently column
         * name comparison is performed using lower case, but once a match
         * is found, this method defers to the case used by the actual Hive
         * column name.
         *
         * Also note that it is important that the elements of the curColNames
         * list that is populated below MUST be in the SAME ORDER as the
         * elements of the associated IndexKey before calling validKey().
         */
        final Map<String, List<String>> indexColMap =
            getIndexedColumnMapLower(table);

        for (Map.Entry<String, List<String>> entry : indexColMap.entrySet()) {

            final List<String> curColNames = new ArrayList<String>();
            for (String indexColName : entry.getValue()) {
                for (String hiveColName : hiveColumnNames) {
                    if (indexColName.equals(hiveColName.toLowerCase())) {
                        curColNames.add(hiveColName);
                    }
                }
            }

            /*
             * For the current (ordered) list of index column names, first
             * remove any invalid operations (and corresponding column name)
             * from the given predicate.
             */
            final IndexPredicateAnalyzer curAnalyzer =
                basicPredicateAnalyzer(curColNames);
            final List<IndexSearchCondition> curSearchConditions =
                new ArrayList<IndexSearchCondition>();
            curAnalyzer.analyzePredicate(predicate, curSearchConditions);
            LOG.debug("sargablePredicateAnalyzer: search conditions " +
                      "[indexKey] = " + curSearchConditions);

            /*
             * Validate only those columns referenced in the current
             * search conditions. Remove duplicates, in the SAME ORDER as
             * the fields of the current IndexKey, and with "gaps" inserted.
             */
            final Set<String> searchColSet = new HashSet<String>();
            for (IndexSearchCondition cond : curSearchConditions) {
                /* Remove duplicates. */
                searchColSet.add(
                    cond.getColumnDesc().getColumn().toLowerCase());
            }

            final List<String> curSearchCols = new ArrayList<String>();
            int nAdded = 0;
            for (String curColName : curColNames) {
                if (searchColSet.contains(curColName)) {
                    curSearchCols.add(curColName);
                    nAdded++;
                    if (nAdded == searchColSet.size()) {
                        break;
                    }
                } else {
                    /* Insert a "gap". */
                    curSearchCols.add(null);
                }
            }

            boolean predicateFound = false;
            while (curSearchCols.size() > 0 && !predicateFound) {

                if (validKey(curSearchConditions, curSearchCols)) {

                    sargableList.add(curSearchCols);

                    final Integer curListSize = curSearchCols.size();
                    if (maxListSize < curListSize) {
                        maxListSize = curListSize;
                        indxOfMaxList = curListIndx;
                    }
                    curListIndx++;
                    predicateFound = true;

                } else {

                    /* Remove last element & corresponding search conditions.*/
                    final String invalidCol =
                        curSearchCols.remove(curSearchCols.size() - 1);
                    if (invalidCol != null) {
                        final List<Integer> removeIndxs =
                            new ArrayList<Integer>();
                        for (int i = 0; i < curSearchConditions.size(); i++) {
                            final IndexSearchCondition cond =
                                curSearchConditions.get(i);
                            final String col =
                                cond.getColumnDesc().getColumn().toLowerCase();
                            if (invalidCol.equals(col)) {
                                removeIndxs.add(i);
                            }
                        }

                        /* Remove from end of list for consistent indexes. */
                        for (int i = removeIndxs.size() - 1; i >= 0; i--) {
                            curSearchConditions.remove(
                                                    (int) removeIndxs.get(i));
                        }
                    }

                    /*
                     * Remove null elements from end 'til next non-null
                     * element.
                     *
                     * For example, suppose curSearchCols initially contained
                     * [color, class, model, count, make]. Then, after the last
                     * (non-null) element is removed above, suppose it becomes
                     * [color, class, null, null]; where each null element does
                     * not correspond to any elements in curSearchConditions.
                     * Then, before proceeding, the null elements at the end
                     * are removed to produce a set with non-null last element;
                     * that is, [color, class].
                     */
                    final List<Integer> removeIndxs = new ArrayList<Integer>();
                    for (int i = curSearchCols.size() - 1; i >= 0; i--) {
                        if (curSearchCols.get(i) == null) {
                            removeIndxs.add(i);
                        } else {
                            break;
                        }
                    }
                    for (int i = 0; i < removeIndxs.size(); i++) {
                        curSearchCols.remove((int) removeIndxs.get(i));
                    }
                }
            }
        }

        /*
         * - PrimaryKey -
         *
         * For the table's PrimaryKey, get the corresponding hive column names
         * and determine if the columns corresponding to the fields of that
         * key can be used to form a predicate that can be pushed. That is,
         * determine if those corresponding fields form a valid PrimaryKey
         * and/or FieldRange (as defined by the Table API).
         *
         * Note that because Hive and KVStore handle case differently column
         * name comparison is performed using lower case, but once a match
         * is found, this method defers to the case used by the actual Hive
         * column name.
         *
         * Also note that it is important that the elements of the curColNames
         * list that is populated below MUST be in the SAME ORDER as the
         * elements of the PrimaryKey before calling validKey().
         */
        final List<String> curColNames = new ArrayList<String>();
        for (String primaryColName : getPrimaryColumnsLower(table)) {
            for (String hiveColName : hiveColumnNames) {
                if (primaryColName.equals(hiveColName.toLowerCase())) {
                    curColNames.add(hiveColName);
                }
            }
        }

        /*
         * For the current (ordered) list of primary key column names, first
         * remove any invalid operations (and corresponding column name) from
         * the given predicate.
         */
        final IndexPredicateAnalyzer curAnalyzer =
            basicPredicateAnalyzer(curColNames);
        final List<IndexSearchCondition> curSearchConditions =
            new ArrayList<IndexSearchCondition>();
        curAnalyzer.analyzePredicate(predicate, curSearchConditions);
        LOG.debug("sargablePredicateAnalyzer: search conditions " +
                  "[primaryKey] = " + curSearchConditions);

        /*
         * Validate only those columns referenced in the current
         * search conditions. Remove duplicates, in the SAME ORDER as
         * the fields of the PrimaryKey, and with "gaps" inserted.
         */
        final Set<String> searchColSet = new HashSet<String>();
        for (IndexSearchCondition cond : curSearchConditions) {
            /* Remove duplicates. */
            searchColSet.add(cond.getColumnDesc().getColumn().toLowerCase());
        }

        final List<String> curSearchCols = new ArrayList<String>();
        int nAdded = 0;
        for (String curColName : curColNames) {
            if (searchColSet.contains(curColName)) {
                curSearchCols.add(curColName);
                nAdded++;
                if (nAdded == searchColSet.size()) {
                    break;
                }
            } else {
                /* Insert a "gap". */
                curSearchCols.add(null);
            }
        }

        boolean predicateFound = false;
        while (curSearchCols.size() > 0 && !predicateFound) {

            if (validKey(curSearchConditions, curSearchCols)) {

                sargableList.add(curSearchCols);

                final Integer curListSize = curSearchCols.size();
                if (maxListSize < curListSize) {

                    maxListSize = curListSize;
                    indxOfMaxList = curListIndx;
                }
                curListIndx++;
                predicateFound = true;

            } else {

                /* Remove last element and corresponding search conditions. */
                final String invalidCol =
                    curSearchCols.remove(curSearchCols.size() - 1);
                if (invalidCol != null) {
                    final List<Integer> removeIndxs = new ArrayList<Integer>();
                    for (int i = 0; i < curSearchConditions.size(); i++) {
                        final IndexSearchCondition cond =
                            curSearchConditions.get(i);
                        final String col =
                            cond.getColumnDesc().getColumn().toLowerCase();
                        if (invalidCol.equals(col)) {
                            removeIndxs.add(i);
                        }
                    }

                    /* Remove from end of list to keep indexes consistent. */
                    for (int i = removeIndxs.size() - 1; i >= 0; i--) {
                        curSearchConditions.remove((int) removeIndxs.get(i));
                    }
                }

                /*
                 * Remove null elements from end 'til next non-null element.
                 *
                 * For example, suppose curSearchCols initially contained
                 * [type, make, model, null, color]. Then, after the last
                 * (non-null) element is removed above, suppose it becomes
                 * [type, make, null, null]; where each null element does
                 * not correspond to any elements in curSearchConditions.
                 * Then, before proceeding, the null elements at the end
                 * are removed to produce a set with non-null last element;
                 * that is, [type, make].
                 */
                final List<Integer> removeIndxs = new ArrayList<Integer>();
                for (int i = curSearchCols.size() - 1; i >= 0; i--) {
                    if (curSearchCols.get(i) == null) {
                        removeIndxs.add(i);
                    } else {
                        break;
                    }
                }

                for (int i = 0; i < removeIndxs.size(); i++) {
                    curSearchCols.remove((int) removeIndxs.get(i));
                }
            }
        }

        if (indxOfMaxList >= 0 && sargableList.size() > 0) {
            return basicPredicateAnalyzer(sargableList.get(indxOfMaxList));
        }

        /* No valid keys or field range, so predicate cannot be pushed. */
        return null;
    }

    /**
     * Returns a predicate analyzer that allows all (and only) the valid
     * operations specified in this class; and operates on only the column
     * (field) names in the <code>List</code> input to this method.
     */
    static IndexPredicateAnalyzer basicPredicateAnalyzer(
                final List<String> colNames) {

        final IndexPredicateAnalyzer analyzer = new IndexPredicateAnalyzer();

        for (String compareOp : COMPARE_OPS) {
            analyzer.addComparisonOp(compareOp);
        }

        for (String colName : colNames) {
            analyzer.allowColumnName(colName);
        }
        LOG.debug("allowable columns = " + colNames);
        return analyzer;
    }

    /**
     * Convenience method that returns a <code>List</code> containing all of
     * the column names (fields) that make up the <code>PrimaryKey</code> of
     * the given <code>Table</code>; where each column name is converted to
     * <em>lower case</em>, and is in valid key order (as defined by the
     * Table API).
     */
    static List<String> getPrimaryColumnsLower(final Table table) {
        final List<String> retList = new ArrayList<String>();
        if (table == null) {
            return retList;
        }

        for (String colName : table.getPrimaryKey()) {
            retList.add(colName.toLowerCase());
        }
        return retList;
    }

    /**
     * Convenience method that returns a <code>Map</code> whose key is
     * the name of one of the given <code>Table</code>'s indexes, and
     * corresponding value is a <code>List</code> whose elements are the
     * names of that index's columns (fields); where each such column name
     * is converted to <em>lower case</em>, and is in valid key order
     * (as defined by the Table API).
     */
    static Map<String, List<String>> getIndexedColumnMapLower(
                                         final Table table) {
        final Map<String, List<String>> retMap =
                                        new HashMap<String, List<String>>();
        if (table == null) {
            return retMap;
        }

        for (Index index : table.getIndexes().values()) {
            final List<String> colNames = new ArrayList<String>();
            for (String colName : index.getFields()) {
                colNames.add(colName.toLowerCase());
            }
            retMap.put(index.getName(), colNames);
        }
        return retMap;
    }

    /**
     * Convenience method called by <code>TableStorageHandlerBase</code>
     * to initialize/reset the <code>splitOnShards</code> field of this
     * class to its default value.
     */
    static void resetSplitOnShards() {
        splitOnShards = false;
    }

    /**
     * Convenience method called by <code>TableStorageHandlerBase</code>
     * after it has determined that the given <code>searchConditions</code>
     * are valid and can be pushed to the backend as a predicate. This method
     * determines whether those search conditions are <code>Index</code>
     * based or based on the table's <code>PrimaryKey</code>.
     * <p>
     * If the search conditions are <code>Index</code> based, then the
     * <code>splitOnShards</code> field of this class is set to
     * <code>true</code> to tell the associated <code>TableInputFormat</code>
     * to build splits (in the <code>getSplits</code> method) and iterate
     * based on shards (<code>RepGroup</code>s) and an <code>IndexKey</code>;
     * rather than partition sets and the <code>PrimaryKey</code>.
     */
    static void setSplitOnShards(
                       final List<IndexSearchCondition> searchConditions,
                       final TableSerDe deserializer) {

        splitOnShards = false; /* default */
        if (searchConditions == null || searchConditions.isEmpty()) {
            return;
        }
        if (deserializer == null) {
            return;
        }

        if (indexKeyFromSearchConditionsNoRange(
                searchConditions, deserializer.getKvTable()) != null) {
            splitOnShards = true;
        }
        LOG.debug("splitOnShards = " + splitOnShards);
    }

    /**
     * Assumes the columns in the given search conditions correspond to the
     * fields of a <em>valid</em> key of an <code>Index</code> in the given
     * table, and uses that information to construct and return a partial
     * <code>IndexKey</code> that can be "pushed" to the store for scanning
     * and filtering the associated index in the backend server; or returns
     * <code>null</code> (or an "empty" key) if those search conditions
     * do not satisfy the necessary criteria for constructing the key.
     * The criteria used when constructing the key is as follows:
     *
     * For the set of columns in the search conditions that are associated
     * with the '=' operation, if those columns form a valid key (that is,
     * the first 'N' fields of the index's key, with no "gaps"), then those
     * fields are used to construct the key to return. Additionally, if the
     * search conditions reference the 'N+1st' field of the index's key as
     * a <code>FieldRange</code>, then that field is <em>not</em> included
     * in the returned key; so that the <code>FieldRange</code> can be
     * handled ("pushed") separately.
     */
    static IndexKey indexKeyFromSearchConditionsNoRange(
                       final List<IndexSearchCondition> searchConditions,
                       final Table table) {

        if (searchConditions == null || table == null) {
            return null;
        }

        final Map<String, List<String>> indexColMap =
            getIndexedColumnMapLower(table);

        if (indexColMap == null || indexColMap.isEmpty()) {
            return null; /* Happens when the table contains no indexes. */
        }

        /*
         * To help construct an ordered list of key fields, with "gaps"
         * inserted (see below), retrieve the columns referenced in the
         * given search conditions, with duplicates removed (duplicates
         * can occur when the column is associated with a range).
         */
        final Set<String> searchColSet = new HashSet<String>();
        for (IndexSearchCondition cond : searchConditions) {
            /* Remove duplicates. */
            searchColSet.add(cond.getColumnDesc().getColumn().toLowerCase());
        }

        /*
         * Find the index from which to construct the key. Note that the
         * table may contain multiple indexes with fields corresponding to
         * the columns specified in the search conditions. Thus, the index
         * that produces the optimal key (most fields that satisfy the
         * criteria) should first be found.
         */
        String indexName = null;
        int maxCols = Integer.MIN_VALUE;

        for (Map.Entry<String, List<String>> entry : indexColMap.entrySet()) {

            final String curIndexName = entry.getKey();

            final List<String> curIndexFields = entry.getValue();

            /*
             * From the searchColSet constructed above, create a list
             * containing the names of the fields of the current index
             * in the ORDER required by the index. If a field from the
             * index is not referenced by the search columns, then that
             * is considered a "gap" and null is inserted in the list.
             * If the list contains a "gap", then a valid key cannot be
             * constructed.
             */
            final List<String> orderedSearchCols = new ArrayList<String>();
            int nAdded = 0;
            for (String indexFieldName : curIndexFields) {
                if (searchColSet.contains(indexFieldName)) {
                    orderedSearchCols.add(indexFieldName);
                    nAdded++;
                    if (nAdded == searchColSet.size()) {
                        break;
                    }
                } else {
                    /* Insert a "gap". */
                    orderedSearchCols.add(null);
                }
            }

            /*
             * Select the index having the maximum number of fields referenced
             * in the search conditions that are valid.
             */
            if (validKey(searchConditions, orderedSearchCols)) {
                if (orderedSearchCols.size() > maxCols) {
                    maxCols = orderedSearchCols.size();
                    indexName = curIndexName;
                }
            }
        }

        if (indexName == null) {
            return null;
        }

        /*
         * If there is a column in the search conditions that is associated
         * with a field range, than exclude it from the key construction
         * process; so that the field range can be handled separately.
         */
        final Map<String, IndexSearchCondition> searchCondMap =
                                  new HashMap<String, IndexSearchCondition>();
        for (IndexSearchCondition cond : searchConditions) {
            final String colName =
                             cond.getColumnDesc().getColumn().toLowerCase();
            if ((GenericUDFOPEqual.class.getName()).equals(
                                                    cond.getComparisonOp())) {
                searchCondMap.put(colName, cond);
            }
        }

        /*
         * If the map constructed above is empty, then no columns in the
         * search conditions correspond to the '=' operation. This means
         * that the first field of the index's key is in a FieldRange;
         * which will be handled separately (in the RecordReader). Thus,
         * this method returns an empty IndexKey (so that filtering will
         * be performed on that range, and all remaining fields are
         * 'wildcarded').
         */
        final Index index = table.getIndex(indexName);
        final IndexKey indexKey = index.createIndexKey();

        if (searchCondMap.isEmpty()) {
            return indexKey;
        }

        /*
         * Use the fields from the index identified above that correspond
         * the search condition columns associated with '=' to construct
         * the key to return.
         */
        final List<String> fieldNames = index.getFields();
        for (String fieldName : fieldNames) {

            final IndexSearchCondition searchCond =
                              searchCondMap.get(fieldName.toLowerCase());
            if (searchCond == null) {
                /* null ==> no more elements in searchCondMap. Done. */
                return indexKey;
            }

            populateKey(fieldName, searchCond.getConstantDesc(), indexKey);
        }
        return indexKey;
    }

    /**
     * Convenience/utility method that converts the Hive based value
     * referenced by the <code>constantDesc</code> parameter to the
     * appropriate Java type and places that value in the given
     * <code>RecordValue</code> (representing either a <code>PrimaryKey</code>
     * or an <code>IndexKey</code>) corresponding to the key's given
     * <code>fieldName</code>.
     */
    static void populateKey(final String fieldName,
                            final ExprNodeConstantDesc constantDesc,
                            final RecordValue key) {

        final String typeName = constantDesc.getTypeInfo().getTypeName();
        final Object keyValue = constantDesc.getValue();

        /* Currently supports only the following primitive types . */
        if (serdeConstants.BOOLEAN_TYPE_NAME.equals(typeName)) {
            key.put(fieldName, (Boolean) keyValue);
        } else if (serdeConstants.INT_TYPE_NAME.equals(typeName)) {
            key.put(fieldName, (Integer) keyValue);
        } else if (serdeConstants.BIGINT_TYPE_NAME.equals(typeName)) {
            key.put(fieldName, (Long) keyValue);
        } else if (serdeConstants.FLOAT_TYPE_NAME.equals(typeName)) {
            key.put(fieldName, (Float) keyValue);
        } else if (serdeConstants.DECIMAL_TYPE_NAME.equals(typeName)) {
            key.put(fieldName, (Float) keyValue);
        } else if (serdeConstants.DOUBLE_TYPE_NAME.equals(typeName)) {
            key.put(fieldName, (Double) keyValue);
        } else if (serdeConstants.STRING_TYPE_NAME.equals(typeName)) {

            if (((key.getDefinition()).getField(fieldName)).isType(
                                                         FieldDef.Type.ENUM)) {
                key.putEnum(fieldName, (String) keyValue);
            } else {
                key.put(fieldName, (String) keyValue);
            }

        } else if (serdeConstants.BINARY_TYPE_NAME.equals(typeName)) {
            key.put(fieldName, (byte[]) keyValue);
        }
    }

    /**
     * Determines whether the given <code>searchConditions</code> are
     * valid; where the criteria used to make this determination are
     * as follows:
     * <ul>
     *   <li> The column names (fields) referenced in the given
     *        <code>searchConditions</code> must be associated with the
     *        fields of either the table's <code>PrimaryKey</code> or
     *        the <code>IndexKey</code> of one of the table's indexes;
     *        where the names are either 'all primary' or 'all index',
     *        not a mix.
     *   <li> If the column names are 'all index', then they must belong
     *        to the same index.
     *   <li> There must be no 'missing' fields in those column names.
     *        That is, whether associated with a <code>PrimaryKey</code>
     *        or an <code>IndexKey</code>, the column names must correspond
     *        to the first N fields in the key definition; satisfying
     *        the requirements of a searchable key defined by the Table API.
     *   <li> If a range is specified, that range must be a 'single range'
     *        on the least 'significant' field of the <code>PrimaryKey</code>
     *        or <code>IndexKey</code>; again, as specified by the Table API.
     * </ul>
     * Note that the given <code>searchConditions</code> may reference column
     * names where some of the names correspond to fields of the table's
     * <code>PrimaryKey</code> and others do not, but all of the names
     * correspond to the fields of one of the table's indexes. As a result,
     * index validity is always examined first.
     */
    static boolean searchConditionsValid(
                       final List<IndexSearchCondition> searchConditions,
                       final Table table) {

        if (searchConditions == null || table == null) {
            return false;
        }

        final Set<String> searchCols = new HashSet<String>();
        for (IndexSearchCondition cond : searchConditions) {
            searchCols.add(cond.getColumnDesc().getColumn().toLowerCase());
        }

        /*
         * 1. searchCols must be all primary or all index, not a mix.
         * 2. If all index, then must belong to the same index.
         * 3. There must be no 'missing' fields.
         * 4. If a range is specified, it must be a single range on the
         *    least 'significant' field.
         *
         * Examine the index columns before the primary columns.
         */
        final Map<String, List<String>> indexedColumnMap =
                                            getIndexedColumnMapLower(table);

        if (indexedColumnMap != null && indexedColumnMap.size() > 0) {
            for (String indexName : indexedColumnMap.keySet()) {

                final List<String> kvCols = indexedColumnMap.get(indexName);
                if (kvCols.size() >= searchCols.size()) {
                    /* searchCols must contain the 1st N kvCols to be valid. */
                    boolean validIndex = true;
                    for (int i = 0; i < searchCols.size(); i++) {
                        final String curKvCol = kvCols.get(i);
                        if (!searchCols.contains(curKvCol)) {
                            validIndex = false;
                            break;
                        }
                    }
                    if (validIndex) {
                        return validKey(searchConditions, kvCols);
                    }
                }
            }
        }

        final List<String> primaryColumnNames =
                               getPrimaryColumnsLower(table);

        if (primaryColumnNames != null && !primaryColumnNames.isEmpty() &&
            primaryColumnNames.size() >= searchCols.size()) {

            /* searchCols must contain the 1st N keys to be valid. */
            for (int i = 0; i < searchCols.size(); i++) {
                final String curKvCol = primaryColumnNames.get(i);
                if (!searchCols.contains(curKvCol)) {
                    return false;
                }
            }
            return validKey(searchConditions, primaryColumnNames);
        }
        return false;
    }

    /**
     * Determines whether the given <code>searchColumns</code> represent
     * a valid key (<code>PrimaryKey</code> or <code>IndexKey</code>) with
     * (optional) <code>FieldRange</code>; and whether the associated
     * comparison operations are valid. Note that the order of the elements
     * in the given <code>orderedColumnsLower</code> parameter must be in
     * valid key order; as defined by the Table API.
     */
    private static boolean validKey(
                            final List<IndexSearchCondition> searchConditions,
                            final List<String> orderedColumnsLower) {

        if (searchConditions == null || searchConditions.isEmpty() ||
            orderedColumnsLower == null || orderedColumnsLower.isEmpty()) {

            return false;
        }

        final List<IndexSearchCondition> remainingSearchConditions =
            new ArrayList<IndexSearchCondition>(searchConditions);

        /*
         * Depends on the field elements in orderedColumnsLower being
         * in valid key order (as defined by the Table API).
         */
        for (int i = 0; i < orderedColumnsLower.size() &&
             remainingSearchConditions.size() > 0; i++) {

            final String colName = orderedColumnsLower.get(i);
            if (colName == null) {
                /* Gap in the key. */
                return false;
            }

            final ColumnPredicateInfo colInfo =
                getColumnPredicateInfo(colName, remainingSearchConditions);
            final List<String> colOps = colInfo.getColumnOps();
            final int nColOps = colOps.size();

            /*
             * The field MUST be associated with either 1 op ('=') or 2 ops
             * (is in a FieldRange); otherwise searchConditions are invalid.
             */
            if (nColOps != 1 && nColOps != 2) {

                return false;
            }

            /* If field has only 1 op, then MUST be '='; otherwise invalid. */
            if (nColOps == 1 &&
                !colOps.contains(GenericUDFOPEqual.class.getName())) {

                return false;
            }

            /*
             * If the field has 2 ops, then MUST be in a FieldRange; otherwise
             * invalid. If the field is found to be in a FieldRange, then
             * it also must be the LAST field from the ordered set of key
             * fields that is associated with a comparison op. Because the
             * fields are processed in key order, and because as each such
             * field is processed, the associated ops are removed from the
             * remainingSearchConditions, the FieldRange will be valid only
             * if the 2 ops represent a valid inequality pair, and only if
             * there are NO OTHER fields left in remainingSearchConditions
             * to analyze.
             */
            if (nColOps == 2) {

                /* For valid FieldRange, must contain '<' or '<='. */
                if (!colOps.contains(
                        GenericUDFOPLessThan.class.getName()) &&
                    !colOps.contains(
                        GenericUDFOPEqualOrLessThan.class.getName())) {

                    return false;
                }

                /*
                 * From above, contains '<' or '<='. So check for
                 * '>' or '>='.
                 */
                if (!colOps.contains(
                        GenericUDFOPGreaterThan.class.getName()) &&
                    !colOps.contains(
                        GenericUDFOPEqualOrGreaterThan.class.getName())) {

                    return false;
                }

                /*
                 * From above, contains valid FieldRange inequalities. Next
                 * check that there are NO ADDITIONAL fields to process;
                 * otherwise invalid.
                 */
                if (remainingSearchConditions.size() > 0) {

                    return false;
                }

                /*
                 * Field has 2 ops, is in a valid FieldRange, and there are
                 * no additional fields to process (remainingSearchConditions
                 * is empty); thus, the key is valid.
                 *
                 * Exit the loop and return true.
                 */
            }
        }
        return true;
    }

    /**
     * Convenience method that searches the given list of search conditions
     * for the given column (field) name, and returns an instance of the
     * ColumnPredicateInfo class containing the given column name, along
     * with a list of all of the comparison operations corresponding to that
     * column name.
     * <p>
     * Note that this method modifies the contents of the given search
     * conditions by removing each element that references the given
     * column name. This behavior is intended to provide convenient exit
     * criteria when this method is invoked in a loop; where the loop would
     * exit when the search conditions are empty. Thus, if it is important
     * to maintain the original search conditions, prior to invoking this
     * method, callers should clone the original search conditions to
     * avoid information loss.
     */

    private static ColumnPredicateInfo getColumnPredicateInfo(
        final String curColName,
        final List<IndexSearchCondition> curSearchConditions) {

        final List<String> colOps = new ArrayList<String>();
        final List<Integer> removeIndxs = new ArrayList<Integer>();
        int i = 0;
        for (IndexSearchCondition cond : curSearchConditions) {
            final String searchColName =
                cond.getColumnDesc().getColumn().toLowerCase();

            if (curColName.equals(searchColName)) {
                colOps.add(cond.getComparisonOp());
                removeIndxs.add(i);
            }
            i++;
        }

        /* Remove the search conditions corresponding to the curColName. */
        for (int j = removeIndxs.size() - 1; j >= 0; j--) {
            /*
             * NOTE: must convert what is returned by removeIndxs.get()
             *       to int; otherwise curSearchConditions.remove() will
             *       fail. This is because List defines two remove methods;
             *       remove(Integer) and remove(int). So to remove the
             *       desired element of curSearchConditions, the INDEX
             *       (not the Integer object) that element must be input
             *       must be specified.
             */
            curSearchConditions.remove((int) removeIndxs.get(j));
        }

        return new ColumnPredicateInfo(curColName, colOps);
    }

    /**
     * Local class, intended as a convenient return type data structure, that
     * associates the comparison operation(s) specified in a given predicate
     * with a corresponding column (field) name.
     */
    private static final class ColumnPredicateInfo {
        private final String columnNameLower;
        private final List<String> columnOps;

        ColumnPredicateInfo(
            final String columnName, final List<String> columnOps) {
            this.columnNameLower = (columnName == null ? null :
                                    columnName.toLowerCase());
            this.columnOps = columnOps;
        }

        List<String> getColumnOps() {
            return columnOps;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ColumnPredicateInfo)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            final ColumnPredicateInfo obj1 = this;
            final ColumnPredicateInfo obj2 = (ColumnPredicateInfo) obj;

            if (obj1.columnNameLower == null) {
                if (obj2.columnNameLower != null) {
                    return false;
                }
            } else if (!obj1.columnNameLower.equals(obj2.columnNameLower)) {
                return false;
            }

            if (obj1.columnOps == null) {
                if (obj2.columnOps != null) {
                    return false;
                }
            } else if (!obj1.columnOps.equals(obj2.columnOps)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int pm = 37;
            int hc = 11;
            int hcSum = 0;
            if (columnNameLower != null) {
                hcSum = hcSum + columnNameLower.hashCode();
            }
            if (columnOps != null) {
                hcSum = hcSum + columnOps.hashCode();
            }
            hc = (pm * hc) + hcSum;
            return hc;
        }

        @Override
        public String toString() {
            final StringBuilder buf =
                new StringBuilder(this.getClass().getSimpleName());
            buf.append(": [columnNameLower=");
            if (columnNameLower != null) {
                buf.append(columnNameLower);
            } else {
                buf.append("null");
            }

            buf.append(", columnOps=");
            if (columnOps != null) {
                buf.append(columnOps);
            } else {
                buf.append("null");
            }

            buf.append("]");
            return buf.toString();
        }
    }
}
