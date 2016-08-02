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

package oracle.kv.impl.security.kerberos;

import java.io.File;

import oracle.kv.impl.admin.param.SecurityParams;
import oracle.kv.impl.security.Authenticator;
import oracle.kv.impl.security.AuthenticatorFactory;
import oracle.kv.impl.security.kerberos.KerberosAuthenticator;

/**
 * Factory for Kerberos implementation of authenticator.
 */
public class KerberosAuthFactory implements AuthenticatorFactory {

    @Override
    public Authenticator getAuthenticator(SecurityParams secParams)
        throws IllegalArgumentException {

        final File krb5Conf = new File(secParams.getKerberosConfFile());
        if (!krb5Conf.exists()) {
            throw new IllegalArgumentException("Kerberos configuration file " +
                "does not exist");
        }

        final String realm = secParams.getKerberosRealmName();
        if (realm == null || realm.equals("")) {
            throw new IllegalArgumentException("Default realm name " +
                "was not specified");
        }

        final File keytab = new File(secParams.getConfigDir(),
                                     secParams.getKerberosKeytabFile());
        if (!keytab.exists()) {
            throw new IllegalArgumentException("Server keytab file " +
                "does not exist");
        }
        return new KerberosAuthenticator(secParams);
    }
}
