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
package oracle.kv.impl.security;

import oracle.kv.impl.security.KVStorePrivilege.PrivilegeType;
import static oracle.kv.impl.security.KVStorePrivilege.PrivilegeType.SYSTEM;
import static oracle.kv.impl.security.KVStorePrivilege.PrivilegeType.TABLE;

/**
 * A set of labels denoting system-defined privileges within the KVStore
 * security system. The privilege labels are mainly used in annotations of RMI
 * to denote the required privileges.
 */
public enum KVStorePrivilegeLabel {

    /*
     * Privileges for data access
     */

    /**
     * Get/iterate keys and values in entire store including any tables
     */
    READ_ANY() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Put/delete values in entire store including any tables
     */
    WRITE_ANY() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for administrative tasks
     */

    /**
     * Privilege to perform ONDB database management
     */
    SYSDBA() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privilege to view/show system information, configuration and metadata
     */
    SYSVIEW() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privilege to query data object information
     */
    DBVIEW() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privilege to query users' own information
     */
    USRVIEW() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privilege to perform ONDB administrative tasks
     */
    SYSOPER() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privilege for KVStore component to access internal services and internal
     * keyspace.
     */
    INTLOPER() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for reading Avro schemas.
     */
    READ_ANY_SCHEMA() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for adding, removing and updating Avro schemas
     */
    WRITE_ANY_SCHEMA() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for creating any table in kvstore
     */
    CREATE_ANY_TABLE() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for dropping any table in kvstore
     */
    DROP_ANY_TABLE() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for evolving any table in kvstore
     */
    EVOLVE_ANY_TABLE() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for creating index on any table in kvstore
     */
    CREATE_ANY_INDEX() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for dropping index on any table in kvstore
     */
    DROP_ANY_INDEX() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for reading data in any table in kvstore
     */
    READ_ANY_TABLE() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for deleting data in any table in kvstore
     */
    DELETE_ANY_TABLE() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privileges for inserting data in any table in kvstore
     */
    INSERT_ANY_TABLE() {

        @Override
        public PrivilegeType getType() {
            return SYSTEM;
        }
    },

    /**
     * Privilege for getting/iterating key-values from a specific table
     */
    READ_TABLE() {

        @Override
        public PrivilegeType getType() {
            return TABLE;
        }
    },

    /**
     * Privilege for deleting key-values in a specific tables
     */
    DELETE_TABLE() {

        @Override
        public PrivilegeType getType() {
            return TABLE;
        }
    },

    /**
     * Privilege for putting key-values in a specific tables
     */
    INSERT_TABLE() {

        @Override
        public PrivilegeType getType() {
            return TABLE;
        }
    },

    /**
     * Privilege for evolving a specific tables
     */
    EVOLVE_TABLE() {

        @Override
        public PrivilegeType getType() {
            return TABLE;
        }
    },

    /**
     * Privilege for creating index on a specific tables
     */
    CREATE_INDEX() {

        @Override
        public PrivilegeType getType() {
            return TABLE;
        }
    },

    /**
     * Privilege for dropping index on a specific tables
     */
    DROP_INDEX() {

        @Override
        public PrivilegeType getType() {
            return TABLE;
        }
    };

    abstract public PrivilegeType getType();
}
