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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import oracle.kv.impl.admin.param.SecurityParams;

/**
 * The class manages all implemented authenticator managers, which provides
 * ability to validate and locate authenticator factory of given authentication
 * method.
 */
public class AuthenticatorManager {

    /**
     * The name of the Kerberos authenticator factory. This is available only
     * in the NoSQL DB EE version.
     */
    public static final String KERBEROS_AUTHENTICATOR_FACTORY_CLASS =
        "oracle.kv.impl.security.kerberos.KerberosAuthFactory";

    /**
     * List of system implemented authenticator factories.
     */
    private static final Map<String, String> systemImplementations =
        new HashMap<>();

    static {
        systemImplementations.put(SystemAuthMethod.KERBEROS.name(), 
                                  KERBEROS_AUTHENTICATOR_FACTORY_CLASS);
    }

    /* not instantiable */
    private AuthenticatorManager() {
    }

    /**
     * Attempt to load an AuthenticatorFactory instance of the specified
     * authentication method name. Note that given authentication name is
     * case insensitive and currently only accept values of SystemAuthMethod.
     *
     * @param authMethod the name of the authentication method
     * @return an instance of AuthenticatorManager
     * @throws ClassNotFoundException if the corresponding class of
     *         authentication method cannot be found
     * @throws IllegalAccessException if the class or default constructor
     *         are inaccessible
     * @throws InstantiationException if the class has no default constructor
     *         or is not an instantiable class.
     * @throws ExceptionInInitializerError if the constructor for the class
     *         throws an exception
     * @throws ClassCastException if the class does not implement
     *         AuthenticationFactory.
     */
    public static Authenticator getAuthenticator(String authMethod,
                                                 SecurityParams secParams)
        throws ClassNotFoundException, IllegalAccessException,
               InstantiationException, ClassCastException {

        final Class<? extends AuthenticatorFactory> authClass =
            findAuthenticatorFactory(authMethod);
        final AuthenticatorFactory factory = authClass.newInstance();
        return factory.getAuthenticator(secParams);
    }

    /**
     * Find class name from system implemented authenticator factory list with
     * specified authentication method and load corresponding class.
     *
     * @param authMethod authentication method that is case-insensitive.
     * @return a class of authenticator factory
     *
     * @throws ClassNotFoundException if the corresponding class cannot be found
     * @throws IllegalArgumentException if specified authentication method
     *         cannot be found
     * @throws ClassCastException if found Class object does not represent a
     *         subclass of AuthenticatorFactory
     */
    private static Class<? extends AuthenticatorFactory>
        findAuthenticatorFactory(String authMethod)
        throws ClassNotFoundException, IllegalArgumentException,
               ClassCastException {

        final String className = systemImplementations.get(
            authMethod.toUpperCase(Locale.ENGLISH));

        if (className == null) {
            throw new IllegalArgumentException(
                "The authentication method " + authMethod + " is not found.");
        }

        return Class.forName(className).asSubclass(AuthenticatorFactory.class);
    }

    /**
     * Check if specified authentication method is supported.
     *
     * @param authMethod authentication method that is case-insensitive.
     * @return true if authentication method is supported.
     */
    public static boolean isSupported(String authMethod) {
        if (noneAuthMethod(authMethod)) {
            return true;
        }
        try {
            AuthenticatorManager.findAuthenticatorFactory(authMethod);
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
        return true;
    }

    /**
     * Check if specified authentication method name is valid.
     *
     * @param authMethod authentication method name that is case-insensitive.
     * @return true if the method name is valid
     */
    public static boolean isValidAuthMethod(String authMethod) {
        try {
            Enum.valueOf(SystemAuthMethod.class,
                         authMethod.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException iae) {
            return false;
        }
        return true;
    }

    /**
     * Case-insensitive check if specified authentication method is NONE.
     * 
     * @return true if given value is 'NONE'
     */
    public static boolean noneAuthMethod(String authMethod) {
        return "NONE".equals(authMethod.toUpperCase(Locale.ENGLISH));
    }

    /**
     * System-defined authenticator.
     */
    public static enum SystemAuthMethod {

        KERBEROS;

    }
}
