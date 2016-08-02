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

import oracle.kv.impl.topo.StorageNode;
import oracle.kv.impl.util.FastExternalizable;

/**
 * Encapsulates Kerberos principal instance names for re-authentication of
 * RepNodeLoginManager. Kerberos authentication requires client to provide
 * appropriate service principal name, which including service, instance
 * and realm in NoSQL system. All server nodes must be in the same realm and
 * use the same service name, so only pass instance names to client.
 */
public class KerberosPrincipals implements Serializable, FastExternalizable {

    private static final long serialVersionUID = 1L;

    private final SNKrbInstance[] instanceNames;

    public KerberosPrincipals(final SNKrbInstance[] instanceNames) {
        if ((instanceNames == null) || (instanceNames.length == 0)) {
            this.instanceNames = null;
        } else {
            this.instanceNames = instanceNames;
        }
    }

    /**
     * FastExternalizable constructor.
     */
    public KerberosPrincipals(ObjectInput in, short serialVersion)
        throws IOException {

        final boolean hasInstances = in.readBoolean();
        if (hasInstances) {
            final short len = in.readShort();
            this.instanceNames = new SNKrbInstance[len];
            for (int i = 0; i < len; i++) {
                this.instanceNames[i] =
                    new SNKrbInstance(in, serialVersion);
            }
        } else {
            instanceNames = null;
        }
    }

    public SNKrbInstance[] getSNInstanceNames() {
        return this.instanceNames;
    }

    /**
     * Return instance name of given storage node. If it does not exists, return
     * null.
     */
    public String getInstanceName(final StorageNode sn) {
        for (SNKrbInstance snKrb : getSNInstanceNames()) {
            if (sn.getStorageNodeId().getStorageNodeId() ==
                snKrb.getStorageNodeId()) {
                return snKrb.getInstanceName();
            }
        }
        return null;
    }

    @Override
    public void writeFastExternal(ObjectOutput out, short serialVersion)
        throws IOException {

        if (instanceNames != null && instanceNames.length != 0) {
            out.writeBoolean(true);
            out.writeShort(instanceNames.length);
            for (SNKrbInstance instance : instanceNames) {
                out.writeObject(instance);
            }
        } else {
            out.writeBoolean(false);
        }
    }
}
