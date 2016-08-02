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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Collections;

/**
 * This class encapsulates methods used to help parse and navigate paths
 * to nested fields in metadata, although it works with both simple and
 * complex paths.
 *
 * Simple fields (e.g. "name") have a single component. Fields that navigate
 * into nested fields (e.g. "address.city") have multiple components. The
 * state maintained by TableFieldPath includes:
 *
 * This class is also used by the query compiler. In the compiler, a path
 * expression is represented as multiple step expressions. This class is used
 * to collect information from each step expr and thus provide an alternative
 * representation of a path expr in a single java object. This is done when
 * we want to push secondary-index predicates from a WHERE clause down to a
 * scan over the index. The definition of a secondary index column is
 * represented as a TablePath, and we want to create another TablePath instance
 * from the compiler path expr in order to "match" the two.
 *
 * fieldMap:
 * The FieldMap of the containing object that provides context for navigations.
 * In most cases it will be the FieldMap associated with a TableImpl. In some
 * cases it is the FieldMap of a RecordValueImpl.
 *
 * pathName:
 * The full string name or path. It will be null when "this" represents a
 * compiler path expr.
 *
 * steps:
 * A parsed List of steps/components of the path. Simple fields will have a
 * single entry. Complex fields, more than one.
 *
 * isComplex:
 * True if this is a complex path.
 */
public class TablePath {
    
    final private FieldMap fieldMap;

    private String pathName;

    private List<String> steps;

    private boolean isComplex;

    public TablePath(TableImpl table, String path) {
        this(table.getFieldMap(), path);
    }

    protected TablePath(FieldMap fieldMap, String path) {

        this.fieldMap = fieldMap;

        if (path != null) {
            pathName = path.toLowerCase();
            steps = parsePathName(path);
        } else {
            steps = new ArrayList<String>();
        }
        
        isComplex = (steps.size() > 1);
    }

    @Override
    public String toString() {
        return pathName;
    }

    @Override
    public int hashCode() {
        return pathName.hashCode();
    }

    /*
     * Comparing the steps in order is sufficient to distinguish TablePath
     * instances. Comparisons are never made across tables, and all paths
     * within the same table are unique.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof TablePath)) {
            return false;
        }

        TablePath other = (TablePath)o;

        if (steps.size() != other.steps.size()) {
            return false;
        }

        for (int i = 0; i < steps.size(); ++i) {
            if (!steps.get(i).equalsIgnoreCase(other.steps.get(i))) {
                return false;
            }
        }
        
        return true;
    }

    public final void clear() {
        pathName = null;
        steps.clear();
        isComplex = false;
    }
 
    final FieldMap getFieldMap() {
        return fieldMap;
    }

    public final boolean isComplex() {
        return isComplex;
    }

    public final String getPathName() {
        return pathName;
    }

    public final void setPathName(String v) {
        pathName = v;
    }

    public int numSteps() {
        return steps.size();
    }

    public final List<String> getSteps() {
        return steps;
    }

    public final String getStep(int i) {
        return steps.get(i);
    }

    public final void add(String step) {
        steps.add(step);
        if (steps.size() > 1) {
            isComplex = true;
        }
    }

    public final void add(int pos, String step) {
        steps.add(pos, step);
        if (steps.size() > 1) {
            isComplex = true;
        }
    }

    public void reverseSteps() {
        Collections.reverse(steps);
    }

    ListIterator<String> iterator() {
        return steps.listIterator();
    }

    public final String getLastStep() {
        return steps.get(steps.size() - 1);
    }

    /**
     * Returns the FieldDef associated with the first (and maybe only)
     * component of the field.
     */
    public FieldDefImpl getFirstDef() {
        return (FieldDefImpl) fieldMap.get(steps.get(0));
    }

    /**
     * Returns a list of field names components in a complex field name,
     * or a single name if the field name is not complex (this is for
     * simplicity in use).
     */
    public static List<String> parsePathName(String pathname) {

        List<String> list = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        for (char ch : pathname.toCharArray()) {
            if (ch == '.') {
                if (sb.length() == 0) {
                    throw new IllegalArgumentException(
                        "Malformed field name: " + pathname);
                }
                list.add(sb.toString());
                sb.delete(0, sb.length());
            } else {
                sb.append(ch);
            }
        }

        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list;
    }

    /**
     * Constructs a single dot-separated string field path from one or
     * more components.
     */
    public static String createPathName(Iterator<String> iter) {

        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            String current = iter.next();
            sb.append(current);
            if (iter.hasNext()) {
                sb.append(TableImpl.SEPARATOR);
            }
        }
        return sb.toString();
    }
}
