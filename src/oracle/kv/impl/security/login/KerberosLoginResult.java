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

package oracle.kv.impl.security.login;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * KerberosLoginResult is the result of a Kerberos login. It extends LoginResult
 * adding the mutual authentication token in addition to the LoginToken itself.
 *
 * This class, which includes the mutual authentication token bytes, was
 * introduced in release 3.5.
 *
 * @since 3.5
 */
public class KerberosLoginResult extends LoginResult {

    private static final long serialVersionUID = 1L;

    /*
     * Contains information that the client can use to authenticate the
     * server, when performing mutual authentication, otherwise null.
     */
    private byte[] mutualAuthenToken;

    /**
     * Creates an instance the supplies the mutual authentication token but no
     * login token.
     */
    public KerberosLoginResult(byte[] token) {
        super(null);
        this.mutualAuthenToken = token;
    }
    
    public KerberosLoginResult(LoginToken loginToken, byte[] authenToken) {
        super(loginToken);
        mutualAuthenToken = authenToken;
    }

    /**
     * FastExternalizable constructor.  Must call superclass constructor
     * first to read common elements.
     */
    public KerberosLoginResult(ObjectInput in, short serialVersion)
        throws IOException {

        super(in, serialVersion);

        int tokenLen = in.readShort();
        mutualAuthenToken = new byte[tokenLen];
        in.readFully(mutualAuthenToken);
    }

    /**
     * FastExternalizable writer.  Must call superclass method first to
     * write common elements.
     */
    @Override
    public void writeFastExternal(ObjectOutput out, short serialVersion)
        throws IOException {

        super.writeFastExternal(out, serialVersion);

        out.writeShort(mutualAuthenToken.length);
        out.write(mutualAuthenToken);
    }

    public byte[] getMutualAuthToken() {
        return mutualAuthenToken;
    }
}
