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

package oracle.kv.impl.tif;


/**
 * Object representing an ES index operation for the text index service. The
 * operation can be either a PUT or DEL, defined in OperationType. For PUT, it
 * carries a JSON document for ES indexing. For both PUT and DEL it carries
 * an encoded primary key that identifies the original record.
 */

class IndexOperation {

    private final String esIndexName;                     /* name of ES index */
    private final String esIndexType;             /* mapping type of ES index */
    private final OperationType operation;           /* operation: PUT or DEL */
    private final String pkPath;                /* encoded PK of the document */
    private final String document;           /* document content, can be null */

    public IndexOperation(String esIndexName,
                          String esIndexType,
                          String pkPath,
                          String document,
                          OperationType operation) {
        this.esIndexName = esIndexName;
        this.esIndexType = esIndexType;
        this.pkPath = pkPath;
        this.document = document;
        this.operation = operation;
    }

    public String getESIndexName() {
        return esIndexName;
    }

    public String getESIndexType() {
        return esIndexType;
    }

    public String getPkPath() {
    	return pkPath;
    }

    public String getDocument() {
        return document;
    }

    public OperationType getOperation() {
        return operation;
    }

    /**
     * Return a number of bytes this index operation represents, for the
     * purpose of calculating this operation's contribution to the size of a
     * bulk request.
     *
     * We take as given that ES uses UTF-8, therefore the number of bytes is
     * equal to the number of chars in the strings.
     *
     * We follow ES's BulkProcessor class's precedent for determining this
     * value, which is simply the length of an IndexRequest's payload "source"
     * field (represented in this class by "document") plus a fudge factor of
     * 50, which is referred to as REQUEST_OVERHEAD.
     *
     * It isn't clear what this REQUEST_OVERHEAD represents.  Possibly it
     * compensates for the lengths of the other fields in the IndexRequest: the
     * index name, type and id.  If that were known to be true then we could be
     * more precise, since we have those fields here in this object.  But then,
     * BulkProcessor also has this information and yet chooses to use a
     * constant fudge factor.
     *
     * This number does not need to be perfectly precise, so I am not
     * going to research this any further!
     */
    private static int FUDGE_FACTOR = 50;
    public int size() {
        final int s = (document == null ? 0 : document.length());
        return s + FUDGE_FACTOR;
    }

    @Override
    public String toString() {
        String fullPath = "/" + esIndexName + "/" + esIndexType +
                          "/" + pkPath;

        return fullPath + ": " + document;
    }

    /* type of the operation to be performed on the search index. */
    public enum OperationType {

        /* put a record to ES index */
        PUT,
        /* delete a record from ES index */
        DEL;

        @Override
        public String toString() {
            switch (this) {
                case PUT:
                    return "PUT";
                case DEL:
                    return "DEL";
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
