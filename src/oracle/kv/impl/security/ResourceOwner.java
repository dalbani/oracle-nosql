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

import java.io.Serializable;

import oracle.kv.impl.util.ObjectUtil;

/**
 * A simple structure recording the owner of an resource in KVStore security
 * systems, including plan, table, and keyspace in future. General, an owner of
 * a resource is a KVStoreUser. Here only the id and the user name are recorded
 * for simplicity.
 */
public class ResourceOwner implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;

    public ResourceOwner(String id, String name) {
        ObjectUtil.checkNull("id", id);
        ObjectUtil.checkNull("name", name);
        this.id = id;
        this.name = name;
    }

    /* Copy ctor */
    public ResourceOwner(ResourceOwner other) {
        ObjectUtil.checkNull("Other owner", other);
        this.id = other.id;
        this.name = other.name;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s(id:%s)", name, id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceOwner)) {
            return false;
        }
        final ResourceOwner otherOwner = (ResourceOwner) other;
        return id.equals(otherOwner.id) && name.equals(otherOwner.name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17 * prime + id.hashCode();
        result = result * prime + name.hashCode();
        return result;
    }
}
