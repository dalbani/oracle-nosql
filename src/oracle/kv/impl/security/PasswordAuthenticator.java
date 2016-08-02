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

import java.util.logging.Level;

import oracle.kv.LoginCredentials;
import oracle.kv.PasswordCredentials;
import oracle.kv.impl.security.login.UserLoginCallbackHandler;
import oracle.kv.impl.security.metadata.KVStoreUser;

public abstract class PasswordAuthenticator implements Authenticator {

    @Override
    public boolean authenticate(LoginCredentials creds,
                                UserLoginCallbackHandler handler) {
        if (!(creds instanceof PasswordCredentials)) {
            logMessage(Level.INFO, "Not password credentials, " +
                "credentials type is " + creds.getClass());
            return false;
        }

        final PasswordCredentials pwCreds = (PasswordCredentials) creds;
        final String userName = pwCreds.getUsername();
        final KVStoreUser user = loadUserFromStore(userName);

        if (user == null || !user.verifyPassword(pwCreds.getPassword())) {
            logMessage(Level.INFO, "User password credentials are not valid");
            return false;
        }

        if (user.isPasswordExpired()) {
            logMessage(Level.INFO, "User password credentials are expired");
            throw new PasswordExpiredException(String.format(
                "The password of %s has expired, it is required to " +
                "change the password.", userName));
        }
        return true;
    }

    @Override
    public void resetAuthenticator() {
        throw new UnsupportedOperationException(
            "Password authenticator cannot be reset");
    }

    /**
     * Load KVStoreUser instance of given user name from security metadata.
     */
    public abstract KVStoreUser loadUserFromStore(String userName);

    public abstract void logMessage(Level level, String message);
}
