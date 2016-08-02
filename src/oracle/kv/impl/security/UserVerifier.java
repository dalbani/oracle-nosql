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

import javax.security.auth.Subject;

import oracle.kv.LoginCredentials;
import oracle.kv.impl.security.login.UserLoginCallbackHandler;

/**
 * Interface for login verification.
 */

public interface UserVerifier {

    /**
     * Check the credentials passed in to see if they are valid.  If they
     * are, return a Subject that represents the user.
     *
     * @param creds the login credentials
     * @param handler the user login callback handler, the underlying
     * password authenticator does not need to use this handler, for operations
     * only need password authenticator like renew password can set null
     * as the value of this parameter
     * @return the Subject representing the user, or null if the credentials
     * are not valid
     * @throws PasswordExpiredException if the password has expired and needs
     * to be renewed
     */
    Subject verifyUser(LoginCredentials creds, UserLoginCallbackHandler handler)
        throws PasswordExpiredException;

    /**
     * Check that the user, if any, represented by the specified Subject is
     * valid.  If the Subject has no associated user principal, it is
     * trivially valid.
     *
     * @param subj a Subject representing a user
     * @return a non-null Subject representing the user if the input Subject
     *    still has a valid identity, or null otherwise
     */
    Subject verifyUser(Subject subj);
}
