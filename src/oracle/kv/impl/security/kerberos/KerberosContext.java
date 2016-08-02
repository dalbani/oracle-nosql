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

package oracle.kv.impl.security.kerberos;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import oracle.kv.impl.security.kerberos.KerberosConfig.StoreKrbConfiguration;

/**
 * KerberosContext contains the logic to manage the JAAS subject generated via
 * Kerberos login module and operation description. It provides methods for
 * recording the login subject for static access at later point in a
 * Kerberos-related operation. This class only performs JAAS login for
 * server-side.
 */
public class KerberosContext {

    private static KerberosContext currentContext;

    private static StoreKrbConfiguration loginConfig;

    private final Subject subject;

    KerberosContext(Subject subject) {
        this.subject = subject;
    }

    /**
     * Find out current Kerberos context. This method will try to locate a
     * valid subject from calling context that contains Kerberos Ticket to
     * construct current Kerbeors context. If no valid subject found, call
     * {@link #getContext()}} to get a new context via JAAS login.
     * @throws LoginException JAAS login errors while trying to acquire tickets.
     */
    public synchronized static KerberosContext getCurrentContext()
        throws LoginException {

        final AccessControlContext context = AccessController.getContext();
        final Subject subject = Subject.getSubject(context);

        if (subject == null ||
            subject.getPrivateCredentials(KerberosTicket.class).isEmpty()) {
            return getContext();
        }
        return new KerberosContext(subject);
    }

    /**
     * Get a new Kerberos context via JAAS login. If current context has not
     * been initialized, call {@link #createContext()}}} to create a new one.
     * @throws LoginException JAAS login errors while trying to acquire tickets.
     */
    public synchronized static KerberosContext getContext()
        throws LoginException {

        if (currentContext == null) {
            createContext();
        }
        return currentContext;
    }

    /**
     * Create a new context via JAAS login. This method will use underlying
     * Kerberos login module to acquire initial ticket for server node services.
     *
     * @throws LoginException
     */
    public synchronized static void createContext()
        throws LoginException {

        if (loginConfig == null) {
            throw new IllegalStateException("No Kerberos configuration found");
        }
        final String existingConfig =
            System.getProperty(KerberosConfig.getKrb5ConfigName());
        if (existingConfig != null &&
            !existingConfig.equals(loginConfig.getKrbConfig())) {
            throw new IllegalStateException(String.format(
                "Error specifying the location of the Kerberos " +
                "configuration file.  The service security parameters " +
                "specify the file %s, but the %s system property specifies %s",
                loginConfig.getKrbConfig(),
                KerberosConfig.getKrb5ConfigName(),
                existingConfig));
        }
        System.setProperty(KerberosConfig.getKrb5ConfigName(),
                           loginConfig.getKrbConfig());
        Subject subject = new Subject();
        final LoginContext login = new LoginContext(
             StoreKrbConfiguration.STORE_KERBEROS_CONFIG,
             subject, null /* callback handler */, loginConfig);
         login.login();
         currentContext = new KerberosContext(subject);
    }

    public static void setConfiguration(StoreKrbConfiguration conf) {
        loginConfig = conf;
    }

    public synchronized static void resetContext() {
        currentContext = null;
        loginConfig = null;
    }

    public <T> T runWithContext(PrivilegedAction<T> action) {
        return Subject.doAs(subject, action);
    }

    public <T> T runWithContext(PrivilegedExceptionAction<T> action) {
        try {
            return Subject.doAs(subject, action);
        } catch (PrivilegedActionException pae) {
            Throwable cause = pae.getCause();
            if (cause instanceof Error) {
              throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
              throw (RuntimeException) cause;
            } else {
              throw new UndeclaredThrowableException(cause);
            }
        }
    }

    /* For testing */
    Subject getSubject() {
        return subject;
    }
}
