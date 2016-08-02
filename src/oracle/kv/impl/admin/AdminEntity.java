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

package oracle.kv.impl.admin;

/**
 *
 * Interface implemented by all admin entity objects. Creates a key for
 * indexing categories of objects persisted in the Admin DB.
 *
 * @param <K> type of AdminEntity indexing key
 */
public interface AdminEntity<K> {

    public static enum EntityType{

        /* 
         * To be added in future: MEMO, PARAM, EVENT, etc 
         * TODO: See the AdminDBCatalog in the unadvertised utility, 
         * oracle.kv.util.internal.AdminDump for a list of the entities stored 
         * in the admin database now, which should be migrated to this class.
         */

        PLAN() {
            @Override
            public String getKey() {
                return "Plan";
            }
        };

        abstract public String getKey();
    }

    /**
     * Returns the indexing key value of this admin entity.
     */
    K getIndexKey();

    /**
     * Returns the type of this admin entity.
     */
    EntityType getEntityType();
}
