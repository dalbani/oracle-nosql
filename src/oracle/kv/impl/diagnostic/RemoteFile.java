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

package oracle.kv.impl.diagnostic;

import java.io.File;

/**
 * This class is to save the local path and remote path of a configuration file
 * of SNA.
 */
public class RemoteFile {
    private File file;
    private SNAInfo snaInfo;

    public RemoteFile(File file, SNAInfo snaInfo) {
        this.file = file;
        this.snaInfo = snaInfo;
    }

    /**
     * get local file path of configuration file.
     * @return local file path of configuration file.
     */
    public File getLocalFile() {
        return file;
    }

    public SNAInfo getSNAInfo() {
        return snaInfo;
    }

    @Override
    public String toString() {
        return snaInfo.getIP().getHostName() + ":" +
                ((snaInfo.getRemoteRootdir() == null) ? "" :
                    snaInfo.getRemoteRootdir()) + ":" +
                file.getName();
    }

    private String getRemotePath() {
        return snaInfo.getRemoteRootdir() + ":" + file.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RemoteFile)) {
            return false;
        }
        RemoteFile remoteFile = (RemoteFile)obj;
        if (snaInfo.getIP().equals(remoteFile.snaInfo.getIP()) &&
                getRemotePath().equals(remoteFile.getRemotePath())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +
                ((getRemotePath() == null) ? 0 : getRemotePath().hashCode());
        result = prime * result +
                ((snaInfo.getIP() == null) ? 0 : snaInfo.getIP().hashCode());
        result = prime * result +
                ((file.getName() == null) ? 0 : file.getName().hashCode());
        return result;
    }
}
