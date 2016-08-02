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

package oracle.kv;

/**
 * The exception is thrown when an expected piece of metadata is not found.
 * This probably indicates an inconsistency between the client's metadata and
 * that on the server side.  This can happen if, for example, a table is
 * dropped then re-created using the same name.  In this case a client's
 * {@link oracle.kv.table.Table} instance may still represent the old table and it must be
 * refreshed using {@link oracle.kv.table.TableAPI#getTable}.
 *
 * @since 3.4
 */
public class MetadataNotFoundException extends FaultException {

    private static final long serialVersionUID = 1L;

    /**
     * For internal use only.
     * @hidden
     */
    public MetadataNotFoundException(String msg) {
        super(msg, true /*isRemote*/);
    }
}
