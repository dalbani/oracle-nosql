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

package oracle.kv.impl.security.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import oracle.kv.impl.util.FastExternalizable;

/**
 * Kerberos principal instance name of a storage node.
 */
public class SNKrbInstance implements Serializable, FastExternalizable {

    private static final long serialVersionUID = 1L;

    private final String instanceName;
    private final int snId;

    public SNKrbInstance(String instanceName, int storageNodeId) {
        this.instanceName = instanceName;
        this.snId = storageNodeId;
    }

    /**
     * FastExternalizable constructor.
     */
    public SNKrbInstance(ObjectInput in,
                         @SuppressWarnings("unused") short serialVersion)
        throws IOException {

        this.instanceName = in.readUTF();
        this.snId = in.readInt();
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public int getStorageNodeId() {
        return snId;
    }

    @Override
    public void writeFastExternal(ObjectOutput out, short serialVersion)
        throws IOException {

        out.writeUTF(instanceName);
        out.writeInt(snId);
    }
}
