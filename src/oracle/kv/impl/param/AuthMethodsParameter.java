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
package oracle.kv.impl.param;

import java.util.HashSet;
import java.util.Set;

import oracle.kv.impl.security.AuthenticatorManager;

import com.sleepycat.persist.model.Persistent;

/**
 * Authentication method parameters that can accept multiple values.
 *
 * Qualified string format is "authMethod1,authMethod2".
 */
@Persistent
public class AuthMethodsParameter extends Parameter {

    private static final long serialVersionUID = 1L;

    private static final String DELIMITER = ",";

    private static final String NONE = "NONE";

    private String[] value;

    /* For DPL */
    public AuthMethodsParameter() {
    }

    public AuthMethodsParameter(String name, String val) {
        super(name);
        parseAuthMethods(val);
    }

    public AuthMethodsParameter(String name, String[] value) {
        super(name);
        this.value = value;
    }

    public String[] asAuthMethods() {
        return value;
    }

    private void parseAuthMethods(String val) {
        String[] splitVal = val.split(DELIMITER);
        Set<String> authMethods = new HashSet<>();
        for (String v : splitVal) {
            final String inputValue = v.trim();
            if (!AuthenticatorManager.isSupported(inputValue) &&
                !NONE.equalsIgnoreCase(inputValue)) {
                throw new IllegalArgumentException(
                    "Unsupported value of authentication method");
            }
            authMethods.add(inputValue.toUpperCase());
        }
        if (authMethods.size() > 1) {
            for (String authMethod : authMethods) {
                if (authMethod.equalsIgnoreCase("NONE")) {
                    throw new IllegalArgumentException(
                        "Cannot set NONE with other auth method");
                }
            }
        }
        value = authMethods.toArray(new String[authMethods.size()]);
    }

    @Override
    public String asString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                sb.append(DELIMITER);
            }
            sb.append(value[i]);
        }
        return sb.toString();
    }

    @Override
    public ParameterState.Type getType() {
        return ParameterState.Type.AUTHMETHODS;
    }
}
