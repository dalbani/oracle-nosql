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

import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.je.rep.ReplicationNode;
import oracle.kv.impl.fault.RNUnavailableException;
import oracle.kv.impl.rep.RepNode;
import oracle.kv.impl.topo.RepNodeId;
import oracle.kv.impl.topo.StorageNodeId;

/**
 * Object representing all information from the rep node that host the
 * TextIndexFeeder needed to build subscription configuration.
 */
class HostRepNode {

    private final String tifNodeName;
    private final String rootDirPath;
    private final String storeName;
    private final String host;
    private final int port;
    private final StorageNodeId storageNodeId;
    private final RepNodeId repNodeId;

    private final ReplicatedEnvironment repEnv;

    HostRepNode(String tifNodeName, RepNode hostRN) {
        this.tifNodeName = tifNodeName;

        rootDirPath = hostRN.getStorageNodeParams().getRootDirPath();
        storeName = hostRN.getGlobalParams().getKVStoreName();
        storageNodeId = hostRN.getStorageNodeParams().getStorageNodeId();
        repNodeId = hostRN.getRepNodeId();
        repEnv = hostRN.getEnv(60000);
        if (repEnv == null) {
            throw new RNUnavailableException(
                "Environment of host node " + hostRN.getRepNodeId() +
                " is unavailable during initializing");
        }
        final ReplicationNode node =
            repEnv.getGroup().getMember(repEnv.getNodeName());
        host = node.getHostName();
        port = node.getPort();

    }

    public String getTifNodeName() {
        return tifNodeName;
    }

    public String getRootDirPath() {
        return rootDirPath;
    }

    public String getStoreName() {
        return storeName;
    }

    public StorageNodeId getStorageNodeId() {
        return storageNodeId;
    }

    public RepNodeId getRepNodeId() {
        return repNodeId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ReplicatedEnvironment getRepEnv() {
        return repEnv;
    }

    @Override
    public String toString() {
        return "TIF: " + tifNodeName +
               "\nkv store: " + storeName +
               "\nhost node:port : " + host + ":" + port +
               "\nsn id: " + storageNodeId;
    }
}
