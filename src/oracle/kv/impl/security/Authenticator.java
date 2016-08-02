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

import oracle.kv.LoginCredentials;
import oracle.kv.impl.security.login.UserLoginCallbackHandler;

/**
 * Interface for user authentication.
 */
public interface Authenticator {

    /**
     * Check the login credentials passed in to see if they are valid.
     *
     * @param loginCreds the login credentials
     * @param handler the user login callback handler
     * @return true if login credential is valid
     */
    public boolean authenticate(LoginCredentials loginCreds,
                                UserLoginCallbackHandler handler);

    /**
     * Reset state of authenticator. This method is aim to be called when
     * this type of authenticator is disabled. The authenticator implementations
     * may maintain some authentication information or states. Calling this
     * method is to ensure the authenticator re-enabled won't contains obsolete
     * information or state.
     */
    public void resetAuthenticator();
}
