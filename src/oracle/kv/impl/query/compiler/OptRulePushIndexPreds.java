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

package oracle.kv.impl.query.compiler;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import oracle.kv.impl.api.table.EnumDefImpl;
import oracle.kv.impl.api.table.FieldDefImpl;
import oracle.kv.impl.api.table.FieldValueImpl;
import oracle.kv.impl.api.table.IndexImpl;
import oracle.kv.impl.api.table.IndexImpl.IndexField;
import oracle.kv.impl.api.table.IndexKeyImpl;
import oracle.kv.impl.api.table.PrimaryKeyImpl;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TablePath;
import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.QueryStateException;
import oracle.kv.impl.query.compiler.Expr.ExprIter;
import oracle.kv.impl.query.compiler.Expr.ExprKind;
import oracle.kv.impl.query.compiler.ExprBaseTable.IndexHint;
import oracle.kv.impl.query.compiler.FunctionLib.FuncCode;
import oracle.kv.impl.query.types.ExprType;
import oracle.kv.impl.query.types.ExprType.Quantifier;
import oracle.kv.table.FieldRange;
import oracle.kv.table.Index;


/**
 * The goal of this optimization rule is convert WHERE predicates into index
 * scan conditions in order to avoing a full table scan.
 *
 * The rule analyses the predicates in a WHERE clause to find, for each index
 * associated with a given table (including the table's primary index), (a) a
 * starting and/or ending key that could be applied to a scan over that index,
 * and (b) predicates that can be evaluated during the index scan from the
 * index columns only, thus filtering the retrieved index keys further.
 *
 * The rule assumes the WHERE-clause expr is in CNF. For each index, it first
 * collects all CNF factors that are "index predicates", i.e., they can be
 * evaluated fully from the index columns only. For example, if the current
 * index is the primary-key index and C1, C2, C3 are the primary-key columns,
 * "C1 > 10" and "C2 = 3 or C3 < 20" are primary index preds. Then, for each
 * index column, in the order that these columns appear in the index (or primary
 * key) declaration, the rule looks-for and processes index preds that are
 * comparison preds (eg, "C1 > 10" is a comparison pred, but "C2 = 3 or C3 < 20"
 * is not). The possible outomes of processing an index pred w.r.t. an index
 * column are listed in the PredicateStatus enum below. The rule stops
 * processing the current index as soon as it finds an index column for which
 * there is no equality pred to be pushed to the index.
 *
 * After the rule has analyzed all indexes, it chooses the "best" index to
 * use among the indexes that had something pushed down to them.
 *
 * TODO: need a "good" heuristic to choose the "best" index, as well as
 * a compiler hint or USE INDEX clause to let the user decide.
 */
class OptRulePushIndexPreds {

    // TODO move this to the Optimizer obj, when we have one
    private RuntimeException theException = null;

    private IndexAnalyzer[] theAnalyzers;

    RuntimeException getException() {
        return theException;
    }

    void apply(ExprSFW sfw) {

        try {
            Expr whereExpr = sfw.getWhereExpr();

            if (whereExpr == null && !sfw.hasSort()) {
                return;
            }

            TableImpl table = sfw.getTable();

            if (table == null) {
                return;
            }

            ExprBaseTable tableExpr = (ExprBaseTable)sfw.getFromExpr();

            IndexHint forceIndexHint = tableExpr.getForceIndexHint();

            Map<String, Index> indexes = table.getIndexes();

            theAnalyzers = new IndexAnalyzer[1+indexes.size()];

            /*
             * Try to push predicates in the primary index
             */
            theAnalyzers[0] = new IndexAnalyzer(sfw, table, null/*index*/);
            IndexAnalyzer primaryAnalyzer = theAnalyzers[0];
            primaryAnalyzer.analyze();

            boolean completePrimaryKey =
                (primaryAnalyzer.thePrimaryKey != null &&
                 primaryAnalyzer.thePrimaryKey.isComplete());

            /* No reason to continue if the WHERE expr is always false */
            if (primaryAnalyzer.theSFW == null) {
                return;
            }

            if (forceIndexHint != null) {

                if (completePrimaryKey) {
                    sfw.removeSort();
                }

                if (sfw.hasSort() &&
                    sfw.getSortingIndex() != forceIndexHint.theIndex) {

                    String hintIndex = (forceIndexHint.theIndex == null ?
                                        "primary" :
                                        forceIndexHint.theIndex.getName());

                    String sortingIndex = (sfw.getSortingIndex() == null ?
                                           "primary" :
                                           sfw.getSortingIndex().getName());

                    throw new QueryException(
                        "Cannot perform order-by because the sorting index " +
                        "is not the same as the one forced via a hint.\n" +
                        "Hint index    : " + hintIndex + "\n" +
                        "Sorting index : " + sortingIndex, sfw.getLocation());
                }

                IndexAnalyzer analyzer =
                    new IndexAnalyzer(sfw, table, forceIndexHint.theIndex);

                analyzer.analyze();

                if (analyzer.theSFW == null) {
                    return;
                }

                analyzer.apply();
                return;
            }

            /*
             * If the query specifies a complete primary key, use the primary
             * index to execute it and remove any order-by.
             */
            if (completePrimaryKey) {
                primaryAnalyzer.apply();
                sfw.removeSort();
                return;
            }

            /*
             * If the query specifies a complete shard key, use the primary
             * index to execute it. In this case, if the query has order-by
             * as well and the sorting index is not the primary one, an error
             * is raised.
             */
            if (primaryAnalyzer.hasShardKey()) {

                if (sfw.hasSort() && sfw.getSortingIndex() != null) {
                    throw new QueryException(
                        "Cannot perform order-by because the query specifies " +
                        "a complete shard key, but the sorting index (" +
                        sfw.getSortingIndex().getName() +
                        ") is a secondary one.", sfw.getLocation());
                }

                primaryAnalyzer.apply();
                return;
            }

            /*
             * If the SFW has sorting, scan the table using the index that
             * sorts the rows in the desired order.
             */
            if (sfw.hasSort()) {

                if (sfw.hasPrimaryIndexBasedSort()) {
                    primaryAnalyzer.apply();
                    return;
                }

                if (sfw.hasSecondaryIndexBasedSort()) {

                    IndexImpl index = sfw.getSortingIndex();

                    IndexAnalyzer analyzer =
                        new IndexAnalyzer(sfw, table, index);

                    analyzer.analyze();

                    if (analyzer.theSFW != null) {
                        analyzer.apply();
                    }

                    return;
                }
            }

            /*
             * Check which of the secondary indexes are applicable for
             * optimizing this query. An index is applicable if the query
             * has any predicates that can be "pushed" to the index.
             */
            boolean alwaysFalse = false;
            int i = 1;
            for (Map.Entry<String, Index> entry : indexes.entrySet()) {

                IndexImpl index = (IndexImpl)entry.getValue();
                theAnalyzers[i] = new IndexAnalyzer(sfw, table, index);

                theAnalyzers[i].analyze();

                if (theAnalyzers[i].theSFW == null) {
                    alwaysFalse = true;
                    break;
                }
                ++i;
            }

            /*
             * Choose the "best" of the applicable indexes.
             */
            if (!alwaysFalse) {
                chooseIndex();
            }

        } catch (RuntimeException e) {
            theException = e;
        }
    }

    /**
     * Choose and apply the "best" index among the applicable ones.
     */
    void chooseIndex() {
        Arrays.sort(theAnalyzers);
        theAnalyzers[0].apply();
    }

    /**
     * The result of processing a particular index predicate w.r.t. some index
     * column C.
     */
    static enum PredicateStatus {

        /*
         * The pred was an equality pred used in creating the start and/or end
         * key for the index scan. Essentially, it was pushed into the index
         * scan and as a result, it should be removed from the WHERE expr.
         */
        PUSHED_EQUAL,

        /*
         * The pred was an =any pred partially pushed into an index start and/or
         * end key for the index scan. Such preds exists only for the multi-key
         * index columns. For example, conider the pred arr[2:5] =any 5, where
         * arr is an array column of a table, and there is an index on that
         * column. The pred arr[] =any 5 can be pushed into the index, to filter
         * out rows that do not contain the number 5 anywhere inside their arr
         * array. But the original pred must be applied to the surviving rows
         * (i.e., it must remain in the WHERE clause), for obvious reasons.
         */
        PUSHED_EQUAL_PARTIAL,

        /*
         * The pred was a range pred used in creating the start and/or end key
         * for the index scan. Essentially, it was pushed into the index scan
         * and as a result, it should be removed from the WHERE expr.
         */
        PUSHED_RANGE,

        /*
         * Same story as PUSHED_EQUAL_PARTIAL, but for the >any, >=any, <any,
         * and <=any operators.
         */
        PUSHED_RANGE_PARTIAL,

        /*
         * The predicate is not pushable to an index start/end condition (w.r.t.
         * any index column, not just C). For example, it may be a pred like
         * "C2 = 3 or C3 < 20". In this case we remove the pred from
         * theIndexPreds so that we won't have to process it again. The pred
         * may still appear in theFilteringPreds.
         */
        SKIP,

        /*
         * The pred is not a comparison pred on C. So keep it to be processed
         * again w.r.t. the following index columns.
         */
        KEEP,

        /*
         * The predicate is always false. This makes the whole WHERE expr always
         * false.
         */
        ALWAYS_FALSE,

        /*
         * The predicate is always true, and as a result it can be removed from
         * the WHERE expr.
         */
        ALWAYS_TRUE
    }

    /**
     * IndexAnalyzer analyzes a specific index of the table.
     *
     * theTable:
     * The table whose indexes (including the primary-key index) are to be
     * analyzed.
     *
     * theIndex:
     * The "current" index, i.e., the index that is being analyzed by "this".
     * If theIndex is null, the current index is the primary index.
     *
     * theSFW:
     * The select-from-where expr referencing theTable in its FROM clause.
     *
     * theIndexPreds:
     * Predicates that are referencing index columns only.
     *
     * theFilteringPreds:
     * A subset of theIndexPreds. These preds can be evaluated from the index
     * columns only, without retrieving the full rows from the table. Some of
     * these preds may be pushed into the index scan as start/end conditions,
     * The remaining filtering preds (if any) can be evaluated during the
     * index scan, before retrieving the table rows. 
     *
     * NOTE: in case of non-mullti-key indexes, theFilteringPreds and
     * theIndexPreds are the same set. In case of multi-key indexes, preds
     * that reference the multi-key columns of the index are not used as
     * filtering preds. In some cases, such preds could be used for filtering,
     * but the rules get very complex and we have chosen not to pursue this
     * optimization for now (TODO: implement this optimization).
     *
     * theMapBothPreds:
     * This structure contains info about "MapBoth" preds, which may be pushed
     * to a "MapBoth" index. A MapBoth index is defined here as an index that
     * maps both the keys and the elements of a map, with the keys indexed before
     * all the element paths.
     *
     * This is best explaned by an example.... Consider the following DDL:
     *
     * CREATE TABLE foo (
     *     id INTEGER,
     *     col1 INTEGER,
     *     col2 INTEGER,
     *     map MAP(RECORD(mf1 INTEGER, mf2 INTEGER, mf3 INTEGER)),
     *     primary key (id)
     * )
     *
     * CREATE INDEX mapidx on foo (keyof(map),
     *                             elementof(map).mf1,
     *                             col2,
     *                             elementof(map).mf2,
     *                             elementof(map).mf3)
     *
     * mapidx is a MapBoth index. Let ICi denote the i-th field/column of the
     * index.
     *
     * A "MapBoth" predicate is a value-comparison pred on one of the map-value
     * index paths, for a particular map key. For example: map.key1.mf1 = 3, and
     * map.key10.mf3 > 10 are both MapBoth preds. Notice that a MapBoth pred is
     * equivalent to an existentially-quantified expr with 2 conditions; for
     * example map.key1.mf1 = 3 is equvalent to:
     * [some $key in keys(map) satisfies $key = "key1" and map.$key = 3].
     * A MapBoth pred may be pushed to a MapBoth index as 2 start/stop preds:
     * an equality pred on the map-key field, and an equality or range pred on
     * a map-value field. Here are some example queries (only the WHERE clause
     * shown):
     *
     * Q1. where map.key10.mf1 > 10
     * The pred can be pushed to the index as [IC1 = "key10" and IC2 > 10]
     *
     * Q2. where map.key5.mf1 = 3 and col2 = 20 and map.key5.mf2 = 5.
     * All preds can be pushed as [IC1 = "key5" and IC2 = 3 and IC3 = 20 and
     * IC4 = 5]
     *
     * Q3. where map.key5.mf1 = 3 and map.key5.mf2 = 5.
     * Both preds is pushable, the 2nd as a filtering pred:
     * [IC1 = "key5" and IC2 = 3 and (filtering) IC4 = 5]
     *
     * Q4. where map.key5.mf1 = 3 and (map.key5.mf2 < 5 or map.key5.mf2 > 15)
     * The 2nd should be pushed as a filtering pred, but currently is not. So,
     * only the 1st pred is pushed (TODO). [IC1 = "key5" and IC2 = 3]
     *
     * Q5. where map.key5.mf1 = 3 and col2 = 20 and map.key6.mf2 = 5.
     * The 1st 2 preds can be pushed as [IC1 = "key5" and IC2 = 3 and
     * IC3 = 20]. Alternatively, the 3rd pred can be pushed as
     * [IC1 = "key6" and (filtering) IC4 = 5], but this is probably suboptimal.
     *
     * Q6. where map.key5.mf1 = 3 and col2 = 20 and map.key6.mf1 = 5.
     * We have a choice whether to push the 1st or the 3rd pred. In this case
     * their "value" is the same, so it doesn't matter which. If we choose
     * the 1st, we push [IC1 = "key5" and IC2 = 3 and IC3 = 20].
     *
     * Q7. where map.key5.mf1 > 3 and col2 = 20 and map.key6.mf1 = 5.
     * We have a choice whether to push the 1st or the 3rd pred. But pushing
     * the 3rd pred is probably better, so we push [IC1 = "key6" and IC2 = 5
     * and IC3 = 20].
     *
     * Q8. where map.key5.mf1 = 3 and col2 = 20 and map.key6.mf1 = 5 and
     *           map.key6.mf2 < 30
     * We can push "key5" preds or "Key6" preds, but "key6" is probably better
     * (because it has more pushable preds), so we push [IC1 = "key6" and
     * IC2 = 5 and IC3 = 20 and IC4 < 30].
     *
     * theMapBothPreds is an ArrayList with one entry per index field. For
     * index fields that are not map-value paths, the entry is null. For each
     * map-value path, the entry is another ArrayList containing all the MapBoth
     * preds for that map-value path.
     *
     * theHaveMapBothPreds:
     * Whether there are any MapBoth preds. If true, the index should basically
     * be treated as a simple index, instead of a multi-key index. This is done
     * to some extend, but not completely. As example Q4 shows, non start/stop
     * preds that could be pushed as filtering preds are not currently pushed.
     * Also, the covering index optimization does not apply (TODO). 
     *
     * theMapBothKey:
     * The "best" map key among the one that have MapBoth preds on them.
     *
     * thePushedPreds:
     * A subset of theIndexPreds: the predicates that are actually pushed into
     * the index as start/end conditions. thePushedPreds is maintained so that
     * these preds can be removed from the WHERE clause if the index is actually
     * applied. thePushedPreds does not include partially pushed preds, as
     * those should not be removed from the WHERE clause. It is also used in
     * determining whether the index is a covering one.
     *
     * thePushedExternals:
     * This is used to handle the cases where a pushable pred contains external
     * variables, eg, foo = $x + $y, where foo is an indexed column of theTable.
     * If foo is an integer, we initially create a placeholder FieldValue with
     * value 0, and place it in the IndexKey or PrimaryKey or FieldRange (i.e.,
     * we push the pred foo = 0). thePushedExternals is then used to register
     * the $x + $y expr. thePushedExternals has one entry for each index field
     * on which an equality pred is pushed, and 2 entries for the single index
     * field on which a FieldRange is pushed. The ordering of the entries in
     * thePushedExternals is the same as the declaration order of the associated
     * index fields. If the predicate(s) pushed on an index field do not have
     * any external vars, the associated entry(s) in thePushedExternals is null.
     * If the current index is applied, thePushedExternals will be copied into
     * the associated ExprBaseTable, and during code generation, it will be
     * converted to an array of PalnIters and placed in the BaseTableIter.
     * During BaseTableIter.open(), the PlanIters stored in
     * BaseTableIter.thePushedExternals, will be evaluated and the resulting
     * values will be used to replace the associated placeholders.
     *
     * theHavePushedExternals:
     * Set to true if at least one entry in thePushedExternals is non-null.
     *
     * theIsFilteringPred:
     * A helper boolean that acts as a global var for the recursive
     * isIndexOnlyExpr() method, which determines whether a predicate should
     * be added to theIndexPreds and theFilteringPreds.
     *
     * thePrimaryKey:
     * A potentially partial primary key used as a start/end index condition
     * (together with theRange, if any) for the primary index. It "contains"
     * pushed equality predicates on primary-key columns.
     *
     * theSecondaryKey:
     * A potentially partial secondary key used as a start/end index condition
     * (together with theRange, if any) for a secondary index. It "contains"
     * pushed equality predicates (including partially pushed predicates) on
     * secondary-index columns.
     *
     * theRange:
     *
     * theScore:
     * A crude metric of how effective the index is going to be in optimizing
     * table access. See getScore() method.
     *
     * theScore2:
     * Same as theScore, but without any special treatment for the complete-key
     * case. See getScore() method.
     *
     * theNumEqPredsPushed:
     * The number of equality predicates pushed as start/stop conditions. It
     * includes partially pushed preds. Used to compute theScore and theScore2
     * for each each index in order to choose the "best" applicable index (see
     * getScore() and compareTo() methods).
     */
    static class IndexAnalyzer implements Comparable<IndexAnalyzer> {

        /* 
         * The relative value of each kind of predicate. Used to compute a
         * score for each each index in order to choose the "best" applicable
         * index (see getScore() and compareTo() methods).
         */
        final static int eqValue = 32;
        final static int vrangeValue = 16; // value-range pred
        final static int arangeValue = 8;  // any-range pred
        final static int filterEqValue = 16;
        final static int filterOtherValue = 8;

        final QueryControlBlock theQCB;

        final StaticContext theSctx;

        final TableImpl theTable;

        final IndexImpl theIndex;

        ExprSFW theSFW;

        final ArrayList<Expr> theIndexPreds = new ArrayList<Expr>();
        
        final ArrayList<Expr> theFilteringPreds = new ArrayList<Expr>();

        final ArrayList<Expr> thePushedPreds = new ArrayList<Expr>();

        ArrayList<ArrayList<MapBothPredInfo>> theMapBothPreds;

        Map<String, MapBothKeyInfo> theMapBothKeys;

        MapBothKeyInfo theMapBothKey;

        boolean theHaveMapBothPreds;

        final ArrayList<Expr> thePushedExternals = new ArrayList<Expr>();

        boolean theHavePushedExternals;

        boolean theIsFilteringPred;

        PrimaryKeyImpl thePrimaryKey;

        IndexKeyImpl theSecondaryKey;

        FieldRange theRange;

        Expr theMinPred;

        Expr theMaxPred;

        FieldValueImpl theMinVal;

        FieldValueImpl theMaxVal;

        Expr theMinConstArg;

        Expr theMaxConstArg;

        boolean thePartialMin;

        boolean thePartialMax;

        boolean theMinInclusive;

        boolean theMaxInclusive;

        FieldDefImpl theRangeDef;

        boolean theIsCovering;

        final boolean theIsPrimary;

        final boolean theIsHintIndex;

        final TablePath thePath;

        int theScore = -1;

        int theScore2 = -1;

        int theNumEqPredsPushed = 0;

        IndexAnalyzer(ExprSFW sfw, TableImpl table, IndexImpl index) {

            theSFW = sfw;
            theQCB = theSFW.getQCB();
            theSctx = theSFW.getSctx();
            theTable = table;
            theIndex = index;
            theIsPrimary = (theIndex == null);

            ExprBaseTable tableExpr = (ExprBaseTable)sfw.getFromExpr();
            theIsHintIndex = tableExpr.isIndexHint(theIndex);

            thePath = new TablePath(table, null/*path*/);
        }

        boolean hasShardKey() {
            return (theIsPrimary &&
                    thePrimaryKey != null &&
                    thePrimaryKey.hasShardKey());
        }

        private int getNumIndexFields() {

            return (theIndex != null ?
                    theIndex.numFields() :
                    theTable.getNumKeyComponents());
        }

        private void resetRange() {
            theRange = null;
            theMinPred = null;
            theMaxPred = null;
            theMinVal = null;
            theMaxVal = null;
            theMinConstArg = null;
            theMaxConstArg = null;
            thePartialMin = false;
            thePartialMax = false;
        }

        private void discardMin(boolean destroy) {
            discardMin(destroy, false);
        }

        private void discardMultiKeyMin(boolean destroy) {
            discardMin(destroy, true);
        }

        private void discardMin(boolean destroy, boolean isMultiKey) {
            if (theMinPred != null) {
                boolean pushed = thePushedPreds.remove(theMinPred);
                if (destroy) {
                    removePred(theMinPred);
                } else if (pushed && !isMultiKey) {
                    theFilteringPreds.add(theMinPred);
                }
            }
            theMinVal = null;
            theMinPred = null;
            thePartialMin = false;
        }

        private void discardMax(boolean destroy) {
            discardMax(destroy, false);
        }

        private void discardMultiKeyMax(boolean destroy) {
            discardMax(destroy, true);
        }

        private void discardMax(boolean destroy, boolean isMultiKey) {
            if (theMaxPred != null) {
                boolean pushed = thePushedPreds.remove(theMaxPred);
                if (destroy) {
                    removePred(theMaxPred);
                } else if (pushed && !isMultiKey) {
                    theFilteringPreds.add(theMinPred);
                }
            }
            theMaxVal = null;
            theMaxPred = null;
            thePartialMax = false;
        }

        private void reset() {
            theIndexPreds.clear();
            thePushedPreds.clear();
            theFilteringPreds.clear();
            if (theMapBothPreds != null) {
                theMapBothPreds.clear();
                theMapBothKeys.clear();
            }
            theMapBothKey = null;
            theHaveMapBothPreds = false;
            resetRange();
            thePrimaryKey = null;
            theSecondaryKey = null;
        }

        /**
         * Remove a pred from the WHERE clause. The pred has either been
         * pushed in the index or is always true/
         */
        private void removePred(Expr pred) {

            Expr whereExpr = theSFW.getWhereExpr();

            if (pred == whereExpr) {
                theSFW.removeWhereExpr(true/*destroy*/);
            } else {
                whereExpr.removeChild(pred, true/*destroy*/);

                if (whereExpr.getNumChildren() == 0) {
                    theSFW.removeWhereExpr(true/*destroy*/);
                }
            }
        }

        /*
         * The whole WHERE expr was found to be always false. Replace the
         * whole SFW expr with an empty expr.
         */
        void processAlwaysFalse(Expr pred) {

            reset();
            Function empty = Function.getFunction(FuncCode.OP_CONCAT);
            Expr emptyExpr = new ExprFuncCall(theQCB, theSctx,
                                              pred.getLocation(),
                                              empty,
                                              new ArrayList<Expr>());
            if (theQCB.getRootExpr() == theSFW) {
                theQCB.setRootExpr(emptyExpr);
            } else {
                theSFW.replace(emptyExpr, true);
            }
            theSFW = null;
        }

        /**
         * Used to sort the IndexAnalyzers in decreasing "value" order, where
         * "value" is a heuristic estimate of how effective the associated
         * index is going to be in optimizing the query.
         */
        @Override
        public int compareTo(IndexAnalyzer other) {

            int numFields1 = getNumIndexFields();
            int numFields2 = other.getNumIndexFields();

            boolean multiKey1 = (theIsPrimary ? false : theIndex.isMultiKey());

            boolean multiKey2 = (other.theIsPrimary ?
                                 false :
                                 other.theIndex.isMultiKey());

            /* Make sure the index scores are computed */
            getScore();
            other.getScore();

            /*
            String name1 = (theIsPrimary ? "primary" : theIndex.getName());
            String name2 = (other.theIsPrimary ? "primary" : other.theIndex.getName());

            System.out.println(
                "Comparing indexes " + name1 + " and " + name2 +
                "\nscore1 = " + score1 + " score2 = " + score2);
            */

            /*
             * If one of the indexes is covering, ....
             */
            if (theIsCovering != other.theIsCovering) {

                if (theIsCovering) {

                    /*
                     * If the other is a preferred index, choose the covering
                     * index if it has at least one eq start/stop condition
                     * or 2 range start/stop conditions.
                     */
                    if (!theIsHintIndex && other.theIsHintIndex) {
                        return (theNumEqPredsPushed > 0 ||
                                thePushedPreds.size() > 1 ?
                                -1 : 1);
                    }

                    /*
                     * If the other index does not have a complete key, choose
                     * the covering index.
                     */
                    if (other.theScore != Integer.MAX_VALUE) {
                        return -1;
                    }

                    /*
                     * The other index has a complete key. Choose the covering
                     * index if its score is >= to the score of the other index
                     * without taking into account the key completeness.
                     */
                    return (theScore >= other.theScore2 ? -1 : 1);
                }

                if (other.theIsCovering) {

                    if (!other.theIsHintIndex && theIsHintIndex) {
                        return (other.theNumEqPredsPushed > 0 ||
                                other.thePushedPreds.size() > 1 ?
                                1 : -1);
                    }

                    if (theScore != Integer.MAX_VALUE) {
                        return 1;
                    }

                    return (other.theScore >= theScore2 ? 1 : -1);
                }
            }

            if (theScore == other.theScore) {

                /*
                 * If none of the indexes has any predicates pushed and one of
                 * them is the primary index, choose that one.
                 */
                if (theScore == 0 && (theIsPrimary || other.theIsPrimary)) {
                    return (theIsPrimary ? -1 : 1);
                }

                /*
                 * If one of the indexes is specified in a hint, choose that
                 * one.
                 */
                if (theIsHintIndex != other.theIsHintIndex) {
                    return (theIsHintIndex ? -1 : 1);
                }

                /*
                 * If one of the indexes is multi-key and other simple, choose
                 * the simple one.
                 */
                if (multiKey1 != multiKey2) {
                    return (multiKey1 ? 1 : -1);
                }

                /*
                 * Choose the index with the smaller number of fields.
                 */
                if (numFields1 != numFields2) {
                    return (numFields1 < numFields2 ? -1 : 1);
                }

                /*
                 * If one of the indexes is the primary index, choose that one.
                 */
                if (theIsPrimary || other.theIsPrimary) {
                    return (theIsPrimary ? -1 : 1);
                }

                /*
                 * TODO ???? Return the one with the smaller key size
                 */

                return 0;
            }

            /*
             * If we have a complete key for one of the indexes, choose that
             * one.
             */
            if (theScore == Integer.MAX_VALUE ||
                other.theScore == Integer.MAX_VALUE) {
                return (theScore == Integer.MAX_VALUE ? -1 : 1);
            }

            /*
             * If one of the indexes is specified in a hint, choose that one.
             */
            if (theIsHintIndex != other.theIsHintIndex) {
                return (theIsHintIndex ? -1 : 1);
            }

            return (theScore > other.theScore ? -1 : 1);
        }

        /**
         * Computes the "score" of an index w.r.t. this query, if not done
         * already.
         *
         * Score is a crude estimate of how effective the index is going to
         * be in optimizing table access. Score is only a relative metric,
         * i.e., it doesn't estimate any real metric (e.g. selectivity), but
         * it is meant to be used only in comparing the relative value of two
         * indexes in order to choose the "best" among all applicable indexes.
         *
         * Score is an integer computed as a weighted sum of the predicates
         * that can be pushed into an index scan (as start/stop conditions or
         * filtering preds).  However, if there is a complete key for an index,
         * that index gets the highest score (Integer.MAX_VALUE).
         */
        private int getScore() {

            if (theScore >= 0) {
                return theScore;
            }

            int numIndexFields;

            theScore = 0;
            theScore2 = 0;

            if (theIndex != null) {
                numIndexFields = theIndex.numFields();
            } else {
                numIndexFields = theTable.getNumKeyComponents();
            }

            theScore += theNumEqPredsPushed * eqValue;

            if (theRange != null) {

                if (theMinPred != null) {
                    Function func = theMinPred.getFunction(null);
                    assert(func != null && func.isComparison());
                    theScore += (func.isAnyComparison() ?
                                 arangeValue : vrangeValue);
                }

                if (theMaxPred != null) {
                    Function func = theMaxPred.getFunction(null);
                    assert(func != null && func.isComparison());
                    theScore += (func.isAnyComparison() ?
                                 arangeValue : vrangeValue);
                }
            }

            for (Expr pred : theFilteringPreds) {
                Function func = pred.getFunction(null);
                if (func != null && func.getCode() == FuncCode.OP_EQ) {
                    theScore += filterEqValue;
                } else {
                    theScore += filterOtherValue;
                }
            }

            theScore2 = theScore;

            if (theNumEqPredsPushed == numIndexFields) {
                theScore = Integer.MAX_VALUE;
                return theScore;
            }

            return theScore;
        }

        /**
         * The index has been chosen among the applicable indexes, so do the
         * actual pred pushdown and remove all the pushed preds from the
         * where clause.
         */
        private void apply() {

            ExprBaseTable tableExpr = (ExprBaseTable)theSFW.getFromExpr();

            if (theRange != null) {
                assert(thePrimaryKey != null || theSecondaryKey != null);
                tableExpr.addRange(theRange);
                theQCB.setPushedRange(theRange);
            }

            if (theIsPrimary) {
                if (thePrimaryKey == null) {
                    thePrimaryKey = theTable.createPrimaryKey();
                }
                tableExpr.addPrimaryKey(thePrimaryKey, theIsCovering);
                theQCB.setPushedPrimaryKey(thePrimaryKey);
            } else {
                if (theSecondaryKey == null) {
                    theSecondaryKey = theIndex.createIndexKey();
                }
                tableExpr.addSecondaryKey(theSecondaryKey, theIsCovering);
                theQCB.setPushedSecondaryKey(theSecondaryKey);
            }

            if (theHavePushedExternals) {
                tableExpr.setPushedExternals(thePushedExternals);
            }

            for (Expr pred : thePushedPreds) {
                removePred(pred);
            }

            if (theFilteringPreds.size() > 1) {

                FunctionLib fnlib = CompilerAPI.getFuncLib();
                Function andFunc = fnlib.getFunc(FuncCode.OP_AND);

                Expr pred = new ExprFuncCall(theQCB, theSctx,
                                             tableExpr.getLocation(),
                                             andFunc,
                                             theFilteringPreds);

                tableExpr.setFilteringPred(pred, false);

                for (Expr pred2 : theFilteringPreds) {
                    removePred(pred2);
                }

            } else if (theFilteringPreds.size() == 1) {
                Expr pred = theFilteringPreds.get(0);
                tableExpr.setFilteringPred(pred, false);
                removePred(pred);
            }
        }

        /**
         * Analyze the WHERE preds w.r.t. the current index.
         *
         * This method will set theSFW to null if it discovers that the whole
         * WHERE expr is always false. If so, it will also replace the whole
         * SFW expr with an empty expr. Callers of this method should check
         * whether theSFW has been set to null.
         */
        private void analyze() {
            if (theIsPrimary) {
                analyzePrimaryIndex();
            } else {
                analyzeSecondaryIndex();
            }
        }

        private void analyzePrimaryIndex() {

            /* Collect theIndexPreds. */
            boolean done = collectIndexPreds();
            if (done) {
                return;
            }

            thePrimaryKey = theTable.createPrimaryKey();

            /*
             * Look-for and process comparison predicates for each primary
             * key column, in the order that these columns appear inside the
             * primary key declaration.
             */
            List<String> pkColumnNames = theTable.getPrimaryKeyInternal();
            IndexField ipath = null;

            for (int i = 0; i < pkColumnNames.size(); ++i) {

                String name = pkColumnNames.get(i);

                ipath = new IndexField(theTable, name);

                done = processIndexColumn(ipath, i);
                if (done) {
                    break;
                }
            }

            createRange(ipath, theTable);

            /* Check if the index is a covering one; */
            checkIsCovering();

            /* clean up this analyzer if nothing got pushed */
            if (theRange == null &&
                theFilteringPreds.isEmpty() &&
                (thePrimaryKey == null || thePrimaryKey.isEmpty())) {
                reset();
            }
        }

        private void analyzeSecondaryIndex() {

            // System.out.println("Processing index " + theIndex.getName());

            if (theIndex.isMapBothIndex()) {

                int numFields = theIndex.numFields();

                if (theMapBothPreds == null) {
                    theMapBothKeys = new HashMap<String, MapBothKeyInfo>();
                    theMapBothPreds =
                        new ArrayList<ArrayList<MapBothPredInfo>>(numFields);
                }

                for (int i = 0; i < numFields; ++i) {
                    theMapBothPreds.add(null);
                }
            }

            /* Collect theIndexPreds and the MapBoth predicates. */
            boolean done = collectIndexPreds();
            if (done) {
                return;
            }

            /* Create the IndexKey into which equality preds will be pushed */
            theSecondaryKey = theIndex.createIndexKey();

            /*
             * Look-for and process comparison predicates for each index
             * column, in the order that these columns appear inside the
             * index declaration.
             */
            List<IndexField> indexPaths = theIndex.getIndexFields();
            IndexField ipath = null;
            boolean processedMultiKeyField = false;

            for (int i = 0; i < indexPaths.size(); ++i) {

                IndexField ipath2 = indexPaths.get(i);

                if (ipath2.isMultiKey()) {
                    if (processedMultiKeyField) {
                        break;
                    }
                    processedMultiKeyField = !theHaveMapBothPreds;
                }

                ipath = ipath2;
                done = processIndexColumn(ipath2, i);
                if (done) {
                    break;
                }
            }

            /* This is here just to eliminate an eclipse warning */
            if (ipath == null) {
                return;
            }

            createRange(ipath, null);

            /* Check if the index is a covering one; */
            checkIsCovering();

            /* clean up this analyzer if nothing got pushed */
            if (theRange == null &&
                theFilteringPreds.isEmpty() &&
                (theSecondaryKey == null || theSecondaryKey.isEmpty())) {
                reset();
            }
        }

        /*
         *
         */
        private void createRange(IndexField ipath, TableImpl table) {

            if (theMinPred != null || theMaxPred != null) {

                int storageSize =
                    (table != null ?
                     table.getPrimaryKeySize(ipath.getPathName()) :
                     0);

                theRange = new FieldRange(
                    ipath.getPathName(),
                    theRangeDef,
                    storageSize);

                if (theMinPred != null) {
                    if (theMinVal == null) {
                        theMinVal = createPlaceHolderValue(theRangeDef);
                        thePushedExternals.add(theMinConstArg);
                        theHavePushedExternals = true;
                        theRange.setStart(theMinVal, theMinInclusive, false);
                    } else {
                        thePushedExternals.add(null);
                        theRange.setStart(theMinVal, theMinInclusive);
                    }

                } else {
                    thePushedExternals.add(null);
                }

                if (theMaxPred != null) {
                    if (theMaxVal == null) {
                        theMaxVal = createPlaceHolderValue(theRangeDef);
                        thePushedExternals.add(theMaxConstArg);
                        theHavePushedExternals = true;
                        theRange.setEnd(theMaxVal, theMaxInclusive, false);
                    } else {
                        thePushedExternals.add(null);
                        theRange.setEnd(theMaxVal, theMaxInclusive);
                    }

                } else {
                    thePushedExternals.add(null);
                }
            }
        }

        /*
         * Check if the index is a covering one.
         */
        void checkIsCovering() {

            int numIndexPreds = (thePushedPreds.size() +
                                 theFilteringPreds.size());
            int numPreds = getNumPreds();

            assert(numIndexPreds <= numPreds);

            /* The index must cover all the predicates in the WHERE clause */
            theIsCovering = (theSFW != null && numIndexPreds == numPreds);

            if (!theIsCovering) {
                return;
            }

            /*
             * The index must cover all the exprs in the SELECT clause.
             * theIsFilteringPred is use here to make sure that an expr
             * does not reference an array/map. In the context of actual
             * predicate exprs, the pred may reference an array/map and
             * still be covered by a multi-key index as a start/stop pred
             * (but not a filtering pred). But in the context of SELECT
             * or ORDERBY exprs, the expr cannot be covered by the index
             * if it references an array/map.
             */
            int numFields = theSFW.getNumFields();

            for (int i = 0; i < numFields; ++i) {
                theIsFilteringPred = true;
                Expr expr = theSFW.getFieldExpr(i);
                if (!isIndexOnlyExpr(expr) || !theIsFilteringPred) {
                    theIsCovering = false;
                    return;
                }
            }

            /* The index must cover all the exprs in the ORDERBY clause */
            int numSortExprs = theSFW.getNumSortExprs();
            
            for (int i = 0; i < numSortExprs; ++i) {
                theIsFilteringPred = true;
                Expr expr = theSFW.getSortExpr(i);
                if (!isIndexOnlyExpr(expr) || !theIsFilteringPred) {
                    theIsCovering = false;
                    return;
                }
            }
        }

        /*
         * Look-for and process comparison predicates for a given index column.
         * The result of such processing is one of the following (assume C is
         * the column to be processed here):
         *
         * 1. A value is added for C in theKey. This is when a pred like C = 10
         *    is found.
         *
         * 2. A FieldRange is created for C. This is when one or two range
         *    preds are found for C, e.g., 10 < C and C <= 20. In this case,
         *    the rest of the index columns do not need to be processed.
         *
         * 3. It is determined that the whole WHERE expression is always false,
         *    e.g. C = 10 and C < 5. In this case the index analysis is
         *    terminated and the WHERE expr is replaced with a const FALSE expr.
         *
         * 4. None of the above.
         *
         * In cases 2 and 3 no more index columns need to be processed, and
         * the method returns true (done) to the caller to convey this info.
         * Otherwise, the method returns false.
         */
        private boolean processIndexColumn(IndexField ipath, int pos) {

            resetRange();
            boolean pushed = false;
            PredicateStatus status;

            if (theHaveMapBothPreds && ipath.isMultiKey()) {

                pushed = processMapBothIndexField(ipath, pos);

                if (theMinPred != null ||
                    theMaxPred != null ||
                    !pushed) {
                    return true;
                }
                return false;
            }

            for (int i = 0; i < theIndexPreds.size(); ++i) {

                Expr pred = theIndexPreds.get(i);

                status = (ipath.isMultiKey() ?
                          processPredForMultiKeyColumn(pred, ipath) :
                          processPred(pred, ipath));

                switch (status) {
                case PUSHED_EQUAL:
                    /* Case 1 */
                case PUSHED_EQUAL_PARTIAL:
                    /* Case 1 */
                case PUSHED_RANGE:
                case PUSHED_RANGE_PARTIAL:
                    /*
                     * Tentatively, case 2, but must consider all preds to
                     * be find other potential range preds on the current
                     * column and to make sure we don't have case 1 or 3.
                     */
                    if (status != PredicateStatus.PUSHED_EQUAL_PARTIAL &&
                        status != PredicateStatus.PUSHED_RANGE_PARTIAL) {
                        thePushedPreds.add(pred);
                    }
                    theIndexPreds.remove(i);
                    theFilteringPreds.remove(pred);
                    --i;
                    pushed = true;
                    break;

                case SKIP:
                    /* Pred is not pushable to an index start/end condition */
                    theIndexPreds.remove(i);
                    --i;
                    break;
                case KEEP:
                    /* Pred may apply to another index column */
                    break;
                case ALWAYS_TRUE:
                    /* The predicate is always true, so it can be removed */
                    removePred(pred);
                    theIndexPreds.remove(i);
                    theFilteringPreds.remove(pred);
                    --i;
                    break;
                case ALWAYS_FALSE:
                    /* case 3 */
                    processAlwaysFalse(pred);
                    return true;
                default:
                    throw new QueryStateException(
                        "Unexpected PredicateStatus: " + status);
                }

                if (status == PredicateStatus.PUSHED_EQUAL ||
                    status == PredicateStatus.PUSHED_EQUAL_PARTIAL) {
                    break;
                }
            } // for each pred in theIndexPreds

            if (theMinPred != null || theMaxPred != null || /* case 2 */
                !pushed) {                                  /* case 4 */
                return true;
            }

            return false;
        }

        /**
         * Process the given pred w.r.t. the given non-multi-key index column.
         */
        PredicateStatus processPred(Expr pred, TablePath ipath) {

            Function func = pred.getFunction(null);

            if (func == null || !func.isComparison()) {
                return PredicateStatus.SKIP;
            }

            if (func.isAnyComparison()) {
                if (theIsPrimary || !theIndex.isMultiKey()) {
                    return PredicateStatus.SKIP;
                }
                return PredicateStatus.KEEP;
            }

            ExprFuncCall compExpr = (ExprFuncCall)pred;

            Expr newPred = func.normalizeCall(compExpr);

            if (newPred != pred) {
                assert(newPred.getKind() == ExprKind.CONST);
                FieldValueImpl boolVal = ((ExprConst)newPred).getValue();

                if (boolVal.getBoolean()) {
                    return PredicateStatus.ALWAYS_TRUE;
                }
                return PredicateStatus.ALWAYS_FALSE;
            }

            FuncCode op = func.getCode();
            Expr arg0 = compExpr.getArg(0);
            Expr arg1 = compExpr.getArg(1);
            Expr constArg = null;
            ExprType constType;
            FieldValueImpl constVal = null;
            Expr varArg;
            boolean haveExternalVars = false;

            if (ExprUtils.isIndexColumnRef(theTable, theIndex, ipath,
                                           arg0, thePath) == 1) {
                constArg = arg1;
                constType = arg1.getType();
                varArg = arg0;

            } else if (ExprUtils.isIndexColumnRef(theTable, theIndex, ipath,
                                                  arg1, thePath) == 1) {
                constArg = arg0;
                constType = arg0.getType();
                varArg = arg1;
                op = FuncCompOp.swapCompOp(op);

            } else {
                /*
                 * Example: we may have pk column of type int and a pred like
                 * this:* promote(pk, DOUBLE) op double_const
                 * Should we raise an error in this case ????
                 */
                return PredicateStatus.KEEP;
            }

            if (!constType.equals(varArg.getType(), false) ||
                constType.getQuantifier() != Quantifier.ONE) {
                return PredicateStatus.SKIP;
            }

            if (constArg.isConstant()) {
                if (constArg.getKind() == ExprKind.CONST) {
                    constVal = ((ExprConst)constArg).getValue();
                } else {
                    haveExternalVars = true;
                }
            } else {
                return PredicateStatus.SKIP;
            }

            /*
             * We stop processing preds for the current column as soon as
             * we find an equality pred, so we cannot be here and have an
             * equality value already.
             */

            if (op == FuncCode.OP_EQ) {

                FieldValueImpl eqVal = null;

                if (!haveExternalVars) {
                    eqVal = constVal;
                    thePushedExternals.add(null);
                } else {
                    constVal = createPlaceHolderValue(constType.getDef());
                    thePushedExternals.add(constArg);
                    theHavePushedExternals = true;
                }

                if (theIsPrimary) {
                    thePrimaryKey.put(ipath.getPathName(), constVal);
                } else {
                    theSecondaryKey.putComplex(ipath, constVal);
                }
                ++theNumEqPredsPushed;

                if (theMinPred != null) {
                    if (eqVal != null && theMinVal != null) {
                        int cmp = eqVal.compareTo(theMinVal);
                        if (cmp < 0 || (cmp == 0 && !theMinInclusive)) {
                            return PredicateStatus.ALWAYS_FALSE;
                        }
                        discardMin(true);
                    } else {
                        discardMin(false);
                    }
                }

                if (theMaxPred != null) {
                    if (eqVal != null && theMaxVal != null) {
                        int cmp = eqVal.compareTo(theMaxVal);
                        if (cmp > 0 || (cmp == 0 && !theMaxInclusive)) {
                            return PredicateStatus.ALWAYS_FALSE;
                        }
                        discardMax(true);
                    } else {
                        discardMax(false);
                    }
                }

                return PredicateStatus.PUSHED_EQUAL;

            } else if (op == FuncCode.OP_GT || op == FuncCode.OP_GE) {

                /*
                 * If we have another min already, then:
                 * a. If the new min is tighter, discard the old one,
                 * b. Else if the old min is tighter, discard this one.
                 * c. Else, if either this or the other min has external
                 *    vars, use the one without the external vars, or if
                 *    both have external vars, use the other one.
                 */
                if (theMinPred != null) {
                    if (theMinVal != null && constVal != null) {
                        int cmp = theMinVal.compareTo(constVal);
                        if (cmp < 0 || (cmp == 0 && theMinInclusive)) {
                            discardMin(true); // case a
                        } else {
                            return PredicateStatus.ALWAYS_TRUE; // case b
                        }
                    } else if (haveExternalVars) {
                        return PredicateStatus.SKIP; // case c
                    } else {
                        discardMin(false); // case c
                    }
                }

                /*
                 * If we have a max already, then:
                 * a. if either this pred or the max pred have external vars,
                 *    push this pred into index.
                 * b. If the max is < this min, the whole thing is always false, or
                 * c. if min == max, we actually have an equality, or
                 * d. min < max. Push this pred into index
                 */
                if (theMaxPred != null) {
                    if (!haveExternalVars && theMaxVal != null) {
                        int cmp = theMaxVal.compareTo(constVal);
                        if (cmp < 0 ||
                            (cmp == 0 && !theMaxInclusive) ||
                            (cmp == 0 && op == FuncCode.OP_GT)) {
                            return PredicateStatus.ALWAYS_FALSE; // case b
                        }

                        if (cmp == 0) {

                            discardMax(true);

                            if (theIsPrimary) {
                                thePrimaryKey.put(ipath.getPathName(), constVal);
                            } else {
                                theSecondaryKey.putComplex(ipath, constVal);
                            }
                            ++theNumEqPredsPushed;

                            return PredicateStatus.PUSHED_EQUAL; // case c
                        }
                    }

                    // cases a and d is below.
                }

                theMinVal = constVal;
                theMinPred = pred;
                theMinInclusive = (op == FuncCode.OP_GE);
                theRangeDef = constType.getDef();
                if (haveExternalVars) {
                    theMinConstArg = constArg;
                }
                return PredicateStatus.PUSHED_RANGE;

            } else if (op == FuncCode.OP_LT || op == FuncCode.OP_LE) {

                /*
                 * If we have another max already, then:
                 * a. If the new max is tighter, discard the old one,
                 * b. Else old max is tighter, so discard this one.
                 * c. Else, if either this or the other min has external
                 *    vars, use the one without the external vars, or if
                 *    both have external vars, use the other one.
                 */
                if (theMaxPred != null) {

                    if (!haveExternalVars &&
                        theMaxVal != null &&
                        constVal != null) {

                        int cmp = constVal.compareTo(theMaxVal);
                        if (cmp < 0 || (cmp == 0 && theMaxInclusive)) {
                            discardMax(true); // case a
                        } else {
                            return PredicateStatus.ALWAYS_TRUE; // case b
                        }
                    } else if (haveExternalVars) {
                        return PredicateStatus.SKIP; // case c
                    } else {
                        discardMax(false); // case c
                    }
                }

                /*
                 * If we have a min already, then:
                 * a. if either this pred or the min pred have external vars,
                 *    push this pred into index, else
                 * b. If this max is < the min, the whole thing is always false.
                 * c. if min == max, we actually have an equality.
                 * d. min < max. Push this pred into index.
                 */
                if (theMinPred != null) {

                    if (!haveExternalVars &&
                        theMinVal != null &&
                        constVal != null) {

                        int cmp = constVal.compareTo(theMinVal);
                        if (cmp < 0 ||
                            (cmp == 0 && !theMinInclusive) ||
                            (cmp == 0 && op == FuncCode.OP_LT)) {
                            return PredicateStatus.ALWAYS_FALSE; // case b
                        }

                        if (cmp == 0) {
                            discardMin(true);

                            if (theIsPrimary) {
                                thePrimaryKey.put(ipath.getPathName(), constVal);
                            } else {
                                theSecondaryKey.putComplex(ipath, constVal);
                            }
                            ++theNumEqPredsPushed;

                            return PredicateStatus.PUSHED_EQUAL; // case c
                        }
                    }

                    // cases a nad d are below.
                }

                theMaxVal = constVal;
                theMaxPred = pred;
                theMaxInclusive = (op == FuncCode.OP_LE);
                theRangeDef = constType.getDef();
                if (haveExternalVars) {
                    theMaxConstArg = constArg;
                }
                return PredicateStatus.PUSHED_RANGE;

            } else {
                assert(op == FuncCode.OP_NEQ);
                return PredicateStatus.SKIP;
            }
        }

        /**
         * Process the given pred w.r.t. the given multi-key index column.
         */
        private PredicateStatus processPredForMultiKeyColumn(
            Expr pred,
            TablePath ipath) {

            assert(theIndex != null && theIndex.isMultiKey());

            Function func = pred.getFunction(null);
            FuncCode op = (func != null ? func.getCode() : null);

            if (func == null ||
                !func.isAnyComparison() ||
                op == FuncCode.OP_NEQ_ANY) {
                // TODO: consider handling value comparisons as well
                return PredicateStatus.SKIP;
            }

            ExprFuncCall compExpr = (ExprFuncCall)pred;

            Expr newPred = func.normalizeCall(compExpr);

            if (newPred != pred) {
                assert(newPred.getKind() == ExprKind.CONST);
                FieldValueImpl boolVal = ((ExprConst)newPred).getValue();

                if (boolVal.getBoolean()) {
                    return PredicateStatus.ALWAYS_TRUE;
                }
                return PredicateStatus.ALWAYS_FALSE;
            }

            Expr arg0 = compExpr.getArg(0);
            Expr arg1 = compExpr.getArg(1);
            Expr constArg = null;
            ExprType constType;
            FieldValueImpl constVal = null;
            Expr varArg;
            boolean haveExternalVars = false;

            int ic = ExprUtils.isIndexColumnRef(theTable, theIndex, ipath,
                                                arg0, thePath);
            if (ic != 0) {
                constArg = arg1;
                constType = arg1.getType();
                varArg = arg0;
            } else {
                ic = ExprUtils.isIndexColumnRef(theTable, theIndex, ipath,
                                                arg1, thePath);
                if (ic != 0) {
                    constArg = arg0;
                    constType = arg0.getType();
                    varArg = arg1;
                    op = FuncAnyOp.swapCompOp(op);
                } else {
                    return PredicateStatus.KEEP;
                }
            }

            if (!constType.equals(varArg.getType(), false) ||
                constType.getQuantifier() != Quantifier.ONE) {
                return PredicateStatus.SKIP;
            }

            if (constArg.isConstant()) {
                if (constArg.getKind() == ExprKind.CONST) {
                    constVal = ((ExprConst)constArg).getValue();
                } else {
                    haveExternalVars = true;
                }
            } else {
                return PredicateStatus.SKIP;
            }

            if (op == FuncCode.OP_EQ_ANY) {

                FieldValueImpl eqVal = null;

                if (!haveExternalVars) {
                    eqVal = constVal;
                    thePushedExternals.add(null);
                } else {
                    constVal = createPlaceHolderValue(constType.getDef());
                    thePushedExternals.add(constArg);
                    theHavePushedExternals = true;
                }

                theSecondaryKey.putComplex(ipath, constVal);
                ++theNumEqPredsPushed;

                if (theMinPred != null) {
                    if (eqVal != null && theMinVal != null) {
                        // theMinVal <any x and x =any eqVal
                        int cmp = eqVal.compareTo(theMinVal);
                        if (cmp < 0 || (cmp == 0 && !theMinInclusive)) {
                            discardMultiKeyMin(false);
                        } else {
                            discardMultiKeyMin(!thePartialMin);
                        }
                    } else {
                        discardMultiKeyMin(false);
                    }
                }

                if (theMaxPred != null) {
                    if (eqVal != null && theMaxVal != null) {
                        int cmp = eqVal.compareTo(theMaxVal);
                        if (cmp > 0 || (cmp == 0 && !theMaxInclusive)) {
                            discardMultiKeyMax(false);
                        }
                        discardMultiKeyMax(!thePartialMax);
                    } else {
                        discardMultiKeyMax(false);
                    }
                }

                return (ic == 1 ?
                        PredicateStatus.PUSHED_EQUAL :
                        PredicateStatus.PUSHED_EQUAL_PARTIAL);

            } else if (op == FuncCode.OP_GT_ANY || op == FuncCode.OP_GE_ANY) {

                /*
                 * If we have another min already, then:
                 * a. If the new min is tighter, discard the old one,
                 * b. Else if the old min is tighter, so discard this one.
                 * c. Else, if either this or the other min has external
                 *    vars:
                 * c1: use the non-partially-pushed one, or if they are both
                 *     partially pushed,
                 * c2: use the one without the external vars, or if both have
                 *     external vars,
                 * c3: use the other one.
                 *
                 * However, in general, do not discard a pred if the other one
                 * is only partially pushed.
                 */
                if (theMinPred != null) {
                    if (theMinVal != null && constVal != null) {
                        int cmp = theMinVal.compareTo(constVal);
                        if (cmp < 0 || (cmp == 0 && theMinInclusive)) {
                            discardMultiKeyMin(!thePartialMin); // case a
                        } else {
                            return (thePartialMin ?
                                    PredicateStatus.SKIP :
                                    PredicateStatus.ALWAYS_TRUE); // case b
                        }
                    } else if (thePartialMin && ic != 2) {
                        discardMultiKeyMin(false); // case c1
                    } else if (ic == 2) {
                        return PredicateStatus.SKIP; // case c1
                    } else if (haveExternalVars) {
                        return PredicateStatus.SKIP; // case c2 or c3
                    } else {
                        discardMultiKeyMin(false); // case c2
                    }
                }

                /*
                 * We can push only one pred, and we have already pushed an
                 * upper-bound pred, so skip this lower-bound pred.
                 */
                if (theMaxPred != null) {
                    return PredicateStatus.SKIP;
                }

                theMinVal = constVal;
                theMinPred = pred;
                thePartialMin = (ic == 2);
                theMinInclusive = (op == FuncCode.OP_GE_ANY);
                theRangeDef = constType.getDef();
                if (haveExternalVars) {
                    theMinConstArg = constArg;
                }
                return (ic == 1 ?
                        PredicateStatus.PUSHED_RANGE :
                        PredicateStatus.PUSHED_RANGE_PARTIAL);

            } else if (op == FuncCode.OP_LT_ANY || op == FuncCode.OP_LE_ANY) {

                /*
                 * We can push only one pred, and we have already pushed a
                 * lower-bound pred, so skip this upper-bound pred.
                 */
                if (theMinPred != null) {
                    return PredicateStatus.SKIP;
                }

                /*
                 * If we have another max already, then:
                 * a. If the new max is tighter, discard the old one,
                 * b. Else the old max is tighter, so discard this one.
                 * However do not discard a pred if the other one is only
                 * partially pushed.
                 */
                if (theMaxPred != null) {
                    if (theMaxVal != null && constVal != null) {
                        int cmp = constVal.compareTo(theMaxVal);
                        if (cmp < 0 || (cmp == 0 && theMaxInclusive)) {
                            discardMultiKeyMax(!thePartialMax); // case a
                        } else {
                            // case b
                            return (thePartialMax ?
                                    PredicateStatus.SKIP :
                                    PredicateStatus.ALWAYS_TRUE);
                        }
                    } else if (thePartialMax && ic != 2) {
                        discardMultiKeyMax(false); // case c1
                    } else if (ic == 2) {
                        return PredicateStatus.SKIP; // case c1
                    } else if (haveExternalVars) {
                        return PredicateStatus.SKIP; // case c2 or c3
                    } else {
                        discardMultiKeyMax(false); // case c2
                    }
                }

                theMaxVal = constVal;
                theMaxPred = pred;
                thePartialMax = (ic == 2);
                theMaxInclusive = (op == FuncCode.OP_LE_ANY);
                theRangeDef = constType.getDef();
                if (haveExternalVars) {
                    theMaxConstArg = constArg;
                }
                return (ic == 1 ?
                        PredicateStatus.PUSHED_RANGE :
                        PredicateStatus.PUSHED_RANGE_PARTIAL);

            } else {
                assert(false);
                return PredicateStatus.SKIP;
            }
        }

        /**
         * Collect theIndexPreds and theMapBothPreds. Return true (done)
         * if no preds were collected; otherwise return false.
         */
        boolean collectIndexPreds() {

            Expr whereExpr = theSFW.getWhereExpr();

            if (whereExpr == null) {
                return true;
            }

            Function andOp = whereExpr.getFunction(FuncCode.OP_AND);

            /*
             * Assume optimistically that the next pred to be checked is a
             * filtering pred.
             */
            theIsFilteringPred = true;

            if (andOp != null) {
                ExprIter children = whereExpr.getChildren();

                while (children.hasNext()) {
                    Expr child = children.next();

                    if (isIndexOnlyExpr(child)) {
                        theIndexPreds.add(child);
                        if (theIsFilteringPred) {
                            theFilteringPreds.add(child);
                        }
                    } else if (theMapBothPreds != null) {
                        collectMapBothPred(child);
                    }
                    theIsFilteringPred = true;
                }
            } else {
                if (isIndexOnlyExpr(whereExpr)) {
                    theIndexPreds.add(whereExpr);
                    if (theIsFilteringPred) {
                        theFilteringPreds.add(whereExpr);
                    }
                } else if (theMapBothPreds != null) {
                    collectMapBothPred(whereExpr);
                }
            }

            if (theHaveMapBothPreds) {
                chooseMapBothKey();
            }

            if (theIndexPreds.isEmpty() && !theHaveMapBothPreds) {
                return true;
            }

            return false;
        }

        /**
         * This method checks whether the given expr is a expr that references
         * columns of the current index only (which may be the primary index,
         * if theIndex is null).
         */
        private boolean isIndexOnlyExpr(Expr expr) {

            if (expr.isStepExpr()) {

                thePath.clear();
                boolean foundMultiKeyStep = false;

                 do {
                    if (expr.getKind() == ExprKind.FIELD_STEP) {

                        ExprFieldStep stepExpr = (ExprFieldStep)expr;
                        String fieldName = stepExpr.getFieldName();
                        Expr fieldNameExpr = stepExpr.getFieldNameExpr();
                        Expr inputExpr = stepExpr.getInput();

                        if (fieldName == null) {
                            if (!isIndexOnlyExpr(fieldNameExpr)) {
                                return false;
                            }
                            if (!isIndexOnlyExpr(inputExpr)) {
                                return false;
                            }
                            return true;
                        }

                        thePath.add(fieldName);

                        if (inputExpr.getType().isArray()) {
                            thePath.add(TableImpl.ANONYMOUS);
                            foundMultiKeyStep = true;
                        }

                    } else if (expr.getKind() == ExprKind.FILTER_STEP) {

                        ExprFilterStep stepExpr = (ExprFilterStep)expr;

                        if (stepExpr.getPredExpr() != null) {
                            if (!isIndexOnlyExpr(stepExpr.getPredExpr())) {
                                return false;
                            }
                        }

                        thePath.add(TableImpl.ANONYMOUS);
                        foundMultiKeyStep = true;

                    } else if (expr.getKind() == ExprKind.SLICE_STEP) {

                        ExprSliceStep stepExpr = (ExprSliceStep)expr;

                        if (stepExpr.getLowExpr() != null) {
                            if (!isIndexOnlyExpr(stepExpr.getLowExpr())) {
                                return false;
                            }
                        }
                        if (stepExpr.getHighExpr() != null) {
                            if (!isIndexOnlyExpr(stepExpr.getHighExpr())) {
                                return false;
                            }
                        }

                        thePath.add(TableImpl.ANONYMOUS);
                        foundMultiKeyStep = true;

                    } else if (expr.getKind() == ExprKind.FUNC_CALL) {

                        Function func = ((ExprFuncCall)expr).getFunction();

                        assert(func.getCode() == FuncCode.FN_KEYS);
                        thePath.add(TableImpl.KEY_TAG);
                        foundMultiKeyStep = true;
                    }

                    expr = expr.getInput();

                } while (expr.isStepExpr());

                if (expr.getKind() == ExprKind.VAR) {

                    ExprVar var = (ExprVar)expr;

                    if (var.isExternal() || var.isContext()) {
                        return true;
                    }

                    TableImpl table = var.getTable();

                    if (table == null) {
                        /*
                         * Cannot happen because the var is neither an external
                         * nor a context var. So, for now, the only other kind
                         * of var we have is "table" var.
                         */
                        throw new QueryStateException(
                            "Reached a unexpected non-table var: " +
                            expr.display());
                    }

                    if (table != theTable) {
                        return false;
                    }

                    if (!thePath.isComplex() &&
                        theTable.isKeyComponent(thePath.getLastStep())) {

                        return true;

                    } else if (!theIsPrimary) {

                        thePath.reverseSteps();

                        if (theIndex.isIndexField(thePath)) {
                            if (foundMultiKeyStep) {
                                theIsFilteringPred = false;
                            }
                            return true;
                        }
                    }

                    return false;
                }

                return isIndexOnlyExpr(expr);

            } else if (expr.getKind() == ExprKind.VAR) {

                ExprVar var = (ExprVar)expr;

                if (var.isExternal() || var.isContext()) {
                    return true;
                }

                /*
                 * It must be a table var that is accessed "directly", i.e.
                 * not via a path expr.
                 */
                return false;

            } else {
                ExprIter children = expr.getChildren();

                while (children.hasNext()) {
                    Expr child = children.next();
                    if (!isIndexOnlyExpr(child)) {
                        children.reset();
                        return false;
                    }
                }

                return true;
            }
        }

        private static class MapBothPredInfo {

            Expr thePred;
            FuncCode theCompOp;
            Expr theConstExpr;
            String theMapKey;

            MapBothPredInfo(
                Expr pred,
                FuncCode op,
                Expr constExpr,
                String mapKey) {

                thePred = pred;
                theCompOp = op;
                theConstExpr = constExpr;
                theMapKey = mapKey;
            }

            boolean isEq() {
                return theCompOp == FuncCode.OP_EQ;
            }

            boolean isMin() {
                return (theCompOp == FuncCode.OP_GT ||
                        theCompOp == FuncCode.OP_GE);
            }

            boolean isMax() {
                return (theCompOp == FuncCode.OP_LT ||
                        theCompOp == FuncCode.OP_LE);
            }
        }
    
        private static class MapBothKeyInfo {
            String theKey;
            int theScore;

            MapBothKeyInfo(String key) {
                theKey = key;
            }
        }

        /*
         * Check if a given pred P is a MapBoth pred, and if so, register info
         * about it in theMapBothPreds. However, P is not registered if we have
         * seen already another MapBoth pred P' for the same index path and map
         * key such that P' is an equality or P' and P are the same kind of
         * range pred (min or max). As a result, for each index path and map
         * key, there are at most 2 MapBoth preds: one eq pred, or one range
         * pred, or a min and a max range preds.
         */
        void collectMapBothPred(Expr pred) {

            /*
             * Check whether the pred is a value-comparison pred, the comp op
             * is not !=, and one of the operands is a constant expr.
             */
            Function func = pred.getFunction(null);
            FuncCode op = (func != null ? func.getCode() : null);

            if (func == null ||
                !func.isValueComparison() ||
                op == FuncCode.OP_NEQ) {
                return;
            }

            ExprFuncCall compExpr = (ExprFuncCall)pred;
            Expr arg0 = compExpr.getArg(0);
            Expr arg1 = compExpr.getArg(1);
            Expr constArg = null;
            ExprType constType;
            Expr varArg;

            if (arg0.isConstant()) {
                constArg = arg0;
                constType = arg0.getType();
                varArg = arg1;
                op = FuncCompOp.swapCompOp(op);
            } else if (arg1.isConstant()) {
                constArg = arg1;
                constType = arg1.getType();
                varArg = arg0;
            } else {
                return;
            }

            if (!constType.equals(varArg.getType(), false) ||
                constType.getQuantifier() != Quantifier.ONE) {
                return;
            }

            /*
             * Check whether the non-constant operand is a simple path expr,
             * and if so, collect the path expr into a TablePath.
             */
            Expr expr = varArg;
            thePath.clear();
            boolean done = false;

            while (!done) {

                switch (expr.getKind()) {

                case FIELD_STEP:
                    ExprFieldStep stepExpr = (ExprFieldStep)expr;
                    String fieldName = stepExpr.getFieldName();

                    if (fieldName == null) {
                        return;
                    }

                    thePath.add(fieldName);

                    if (stepExpr.getInput().getType().isArray()) { 
                        thePath.add(TableImpl.ANONYMOUS);
                    }

                    expr = expr.getInput();
                    break;

                case VAR:
                    ExprVar var = (ExprVar)expr;
                    TableImpl table = var.getTable();

                    if (table == null || table != theTable) {
                        return;
                    }

                    done = true;
                    break;
                    
                default:
                    return;
                }
            }

            /*
             * Try to match the path expr with one of the index paths.
             */
            thePath.reverseSteps();
            int numFields = theIndex.numFields();
            int ipathIdx;
            String mapKey = null;

            for (ipathIdx = 0; ipathIdx < numFields; ++ipathIdx) {

                IndexField ipath = theIndex.getIndexPath(ipathIdx);

                if (!ipath.isMapValue() ||
                    ipath.numSteps() != thePath.numSteps()) {
                    continue;
                }

                for (int i = 0; i < ipath.numSteps(); ++i) {

                    String step = ipath.getStep(i);

                    if (step.equalsIgnoreCase(thePath.getStep(i))) {
                        continue;
                    }

                    if (step.equals(TableImpl.ANONYMOUS)) {
                        mapKey = thePath.getStep(i);
                        continue;
                    }

                    mapKey = null;
                    break;
                }
            
                if (mapKey != null) {
                    break; // P is a MapBoth pred.
                }
            }

            if (ipathIdx == numFields) {
                return; // P is not a MapBoth pred.
            }

            /*
             * P is a MapBoth pred. Register it, unless there is already another
             * pred P' as described in the header javadoc of this method. TODO:
             * check if P and/or P' are always true or always false. 
             */
            ArrayList<MapBothPredInfo> preds = theMapBothPreds.get(ipathIdx);

            if (preds == null) {
                preds = new ArrayList<MapBothPredInfo>();
                theMapBothPreds.set(ipathIdx, preds);
            }

            MapBothPredInfo predInfo = 
                new MapBothPredInfo(pred, op, constArg, mapKey);

            for (int i = 0; i < preds.size(); ++i) {

                MapBothPredInfo currPredInfo = preds.get(i);

                if (!currPredInfo.theMapKey.equals(mapKey)) {
                    continue;
                }

                if (currPredInfo.isEq() ||
                    (currPredInfo.isMin() && predInfo.isMin()) ||
                    (currPredInfo.isMax() && predInfo.isMax())) {
                    return;
                }

                if (predInfo.isEq()) {
                    preds.remove(i);
                    --i;
                }
            }
        
            preds.add(predInfo);

            MapBothKeyInfo mki = theMapBothKeys.get(mapKey);
            if (mki == null) {
                mki = new MapBothKeyInfo(mapKey);
                theMapBothKeys.put(mapKey, mki);
            }

            theHaveMapBothPreds = true;

            /*
            System.out.println("Found MapBoth pred:\n" +
                               predInfo.thePred.display());
            */
        }

        /**
         *
         */
        void chooseMapBothKey() {

            if (!theHaveMapBothPreds) {
                return;
            }

            int numFields = theIndex.numFields();

            /*
             * For each map key, calculate its score.
             */
            for (MapBothKeyInfo mki : theMapBothKeys.values()) {

                String mapKey = mki.theKey;
                boolean useFilteringPreds = false;
                boolean eqPredPushed = false;

                for (int i = 0; i < numFields; ++i) {

                    ArrayList<MapBothPredInfo> preds = theMapBothPreds.get(i);
                
                    if (preds == null) {
                        continue; // not a MapValue index path
                    }

                    /*
                     * Count the preds (if any) that are pushable to the current
                     * MapValue path for the current map key
                     */
                    for (MapBothPredInfo pred : preds) {
                            
                        if (!pred.theMapKey.equals(mapKey)) {
                            continue;
                        }

                        if (useFilteringPreds) {
                            if (pred.isEq()) {
                                mki.theScore += filterEqValue;
                            } else {
                                mki.theScore += filterOtherValue;
                            }
                        } else {
                            if (pred.isEq()) {
                                mki.theScore += eqValue;
                                eqPredPushed = true;
                            } else {
                                mki.theScore += vrangeValue;
                            }
                        }
                    }

                    if (!eqPredPushed) {
                        useFilteringPreds = true;
                    }
                }
            }

            /*
             * Now choose the "best" map key
             */
            for (MapBothKeyInfo mki : theMapBothKeys.values()) {
                if (theMapBothKey == null) {
                    theMapBothKey = mki;
                } else if (mki.theScore > theMapBothKey.theScore) {
                    theMapBothKey = mki;
                }
            }

            /*
             * Throw away all MapBoth preds that are not on the "best" map key.
             * Also add all the remaining MapBoth preds to theFilteringPreds.
             */
            for (int i = 0; i < numFields; ++i) {
                
                ArrayList<MapBothPredInfo> preds = theMapBothPreds.get(i);

                if (preds == null) {
                    continue;
                }

                for (int j = 0; j < preds.size(); ++j) {

                    MapBothPredInfo predInfo = preds.get(j);
                    
                    if (predInfo.theMapKey.equals(theMapBothKey.theKey)) {
                        theFilteringPreds.add(predInfo.thePred);
                    } else {
                        preds.remove(j);
                        --j;
                    }
                }
            }

            /*
            System.out.println(
                "Best MapBoth key : " + theMapBothKey.theKey +
                " theNumEqPreds = " + theMapBothKey.theNumEqPreds +
                " theNumRangePreds = " + theMapBothKey.theNumRangePreds);
            */
        }

        boolean processMapBothIndexField(IndexField ipath, int pos) {

            if (ipath.isMapKey()) {

                FieldValueImpl keyVal;
                keyVal = FieldDefImpl.stringDef.createString(theMapBothKey.theKey);

                theSecondaryKey.putComplex(ipath, keyVal);
                thePushedExternals.add(null);
                ++theNumEqPredsPushed;
                return true;
            }

            ArrayList<MapBothPredInfo> preds = theMapBothPreds.get(pos);

            if (preds == null) {
                return false;
            }

            boolean pushed = false;

            for (MapBothPredInfo predInfo : preds) {

                assert(predInfo.theMapKey.equals(theMapBothKey.theKey));

                pushed = true;

                Expr pred = predInfo.thePred;
                FuncCode op = predInfo.theCompOp;
                FieldValueImpl constVal = null;
                ExprType constType = predInfo.theConstExpr.getType();
                boolean haveExternalVars = false;

                if (predInfo.theConstExpr.getKind() == ExprKind.CONST) {
                    constVal = ((ExprConst)predInfo.theConstExpr).getValue();
                } else {
                    haveExternalVars = true;
                }

                if (predInfo.isEq()) {

                    if (!haveExternalVars) {
                        thePushedExternals.add(null);
                    } else {
                        constVal = createPlaceHolderValue(constType.getDef());
                        thePushedExternals.add(predInfo.theConstExpr);
                        theHavePushedExternals = true;
                    }

                    theSecondaryKey.putComplex(ipath, constVal);
                    thePushedPreds.add(predInfo.thePred);
                    theFilteringPreds.remove(pred);
                    ++theNumEqPredsPushed;

                    return true;

                } else if (predInfo.isMin()) {

                    if (theMaxPred != null) {
                        if (!haveExternalVars && theMaxVal != null) {
                            int cmp = theMaxVal.compareTo(constVal);
                            if (cmp < 0 ||
                                (cmp == 0 && !theMaxInclusive) ||
                                (cmp == 0 && op == FuncCode.OP_GT)) {
                                processAlwaysFalse(pred);
                                return false;
                            }

                            if (cmp == 0) {
                                discardMax(true);
                                theSecondaryKey.putComplex(ipath, constVal);
                                ++theNumEqPredsPushed;
                                thePushedPreds.add(pred);
                                theFilteringPreds.remove(pred);
                                return true;
                            }
                        }
                    }

                    theMinVal = constVal;
                    theMinPred = pred;
                    theMinInclusive = (op == FuncCode.OP_GE);
                    theRangeDef = constType.getDef();
                    if (haveExternalVars) {
                        theMinConstArg = predInfo.theConstExpr;
                    }
                    thePushedPreds.add(pred);
                    theFilteringPreds.remove(pred);

                } else {
                    assert(predInfo.isMax());

                    if (theMinPred != null) {

                        if (theMinVal != null && constVal != null) {

                            int cmp = constVal.compareTo(theMinVal);
                            if (cmp < 0 ||
                                (cmp == 0 && !theMinInclusive) ||
                                (cmp == 0 && op == FuncCode.OP_LT)) {
                                processAlwaysFalse(pred);
                                return false;
                            }

                            if (cmp == 0) {
                                discardMin(true);
                                theSecondaryKey.putComplex(ipath, constVal);
                                ++theNumEqPredsPushed;
                                thePushedPreds.add(pred);
                                theFilteringPreds.remove(pred);
                                return true;
                            }
                        }
                    }

                    theMaxVal = constVal;
                    theMaxPred = pred;
                    theMaxInclusive = (op == FuncCode.OP_LE);
                    theRangeDef = constType.getDef();
                    if (haveExternalVars) {
                        theMaxConstArg = predInfo.theConstExpr;
                    }
                    thePushedPreds.add(pred);
                    theFilteringPreds.remove(pred);
                }
            }

            return pushed;
        }

        FieldValueImpl createPlaceHolderValue(FieldDefImpl type) {

            switch (type.getType()) {
            case INTEGER:
                return FieldDefImpl.integerDef.createInteger(0);
            case LONG:
                return FieldDefImpl.longDef.createLong(0);
            case FLOAT:
                return FieldDefImpl.floatDef.createFloat(0.0F);
            case DOUBLE:
                return FieldDefImpl.doubleDef.createDouble(0.0);
            case STRING:
                return FieldDefImpl.stringDef.createString("");
            case ENUM:
                return ((EnumDefImpl)type).createEnum(1);
            default:
                throw new QueryStateException(
                    "Unexpected type for index key: " + type);
            }
        }

        int getNumPreds() {

            if (theSFW == null) {
                return 0;
            }

            Expr whereExpr = theSFW.getWhereExpr();

            if (whereExpr == null) {
                return 0;
            }

            Function andOp = whereExpr.getFunction(FuncCode.OP_AND);

            if (andOp != null) {
                return whereExpr.getNumChildren();
            }
            return 1;
        }
    }
}
