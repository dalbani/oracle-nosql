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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;

/**
 * Callback handler implementation that passed to underlying user login
 * authenticators. Currently only support LoggingCallback and
 * LoginResultCallback.
 */
public class UserLoginCallbackHandler {

    /* Login result produced by user authenticator */
    private LoginResult loginResult;

    private Logger logger;

    public UserLoginCallbackHandler(Logger logger) {
        this.loginResult = null;
        this.logger = logger;
    }

    public void handle(Callback callback) {
        if (callback instanceof LoginResultCallback) {
            final LoginResultCallback ck = (LoginResultCallback) callback;
            loginResult = ck.getLoginResult();
        } else if (callback instanceof LoggingCallback) {
            final LoggingCallback ck = (LoggingCallback) callback;
            logger.log(ck.getLevel(), ck.getMessage());
        } else {
            logger.warning("Handle unsupported callback: " +
                           callback.getClass().getName());
        }
    }

    /**
     * Get login result. May return null if underlying authenticator does not
     * produce a login result.
     */
    public LoginResult getLoginResult() {
        return loginResult;
    }

    /**
     * Login result callback. It is used for underlying authenticator to
     * rebuild the login results.
     */
    public interface LoginResultCallback extends Callback {
        public LoginResult getLoginResult();
    }

    /**
     * Logging callback. It is used for underlying authenticator logging.
     */
    public static class LoggingCallback implements Callback {

        private Level level;

        private String message;

        public LoggingCallback() {
            level = null;
            message = null;
        }

        public LoggingCallback(Level level, String message) {
            this.level = level;
            this.message = message;
        }

        public LoggingCallback setLevel(Level level) {
            this.level = level;
            return this;
        }

        public LoggingCallback setMessage(String message) {
            this.message = message;
            return this;
        }

        public Level getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }
    }
}
