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

package oracle.kv.impl.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import oracle.kv.impl.admin.param.AdminParams;
import oracle.kv.impl.admin.param.GlobalParams;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.param.SecurityParams;
import oracle.kv.impl.admin.param.StorageNodeParams;
import oracle.kv.impl.param.ParameterMap;
import oracle.kv.impl.security.AccessChecker;
import oracle.kv.impl.security.AccessCheckerImpl;
import oracle.kv.impl.security.Authenticator;
import oracle.kv.impl.security.AuthenticatorManager;
import oracle.kv.impl.security.AuthenticatorManager.SystemAuthMethod;
import oracle.kv.impl.security.RoleInstance;
import oracle.kv.impl.security.RoleResolver;
import oracle.kv.impl.security.login.InternalLoginManager;
import oracle.kv.impl.security.login.LoginUpdater.GlobalParamsUpdater;
import oracle.kv.impl.security.login.LoginUpdater.ServiceParamsUpdater;
import oracle.kv.impl.security.login.ParamTopoResolver;
import oracle.kv.impl.security.login.ParamTopoResolver.ParamsHandle;
import oracle.kv.impl.security.login.TokenResolverImpl;
import oracle.kv.impl.security.login.TokenVerifier;
import oracle.kv.impl.security.metadata.KVStoreUser;
import oracle.kv.impl.security.metadata.SecurityMDChange;
import oracle.kv.impl.security.metadata.SecurityMDUpdater.RoleChangeUpdater;
import oracle.kv.impl.security.metadata.SecurityMDUpdater.UserChangeUpdater;
import oracle.kv.impl.security.util.CacheBuilder.CacheConfig;

/**
 * This is the security management portion of the Admin. It constructs and
 * houses the AccessCheck implementation, etc.
 */
public class AdminSecurity implements GlobalParamsUpdater,
                                      ServiceParamsUpdater,
                                      UserChangeUpdater,
                                      RoleChangeUpdater {

    private final AdminService adminService;
    private final AccessCheckerImpl accessChecker;
    private final TokenResolverImpl tokenResolver;
    private final AdminParamsHandle paramsHandle;
    private final ParamTopoResolver topoResolver;
    private final TokenVerifier tokenVerifier;
    private Logger logger;
    /* not final because it can change when configured */
    private InternalLoginManager loginMgr;
    private final AdminRoleResolver roleResolver;
    private final Map<String, Authenticator> authenticators;

    /*
     * On admin, since the heap size is small, we set a small cache size and
     * small timeout value so that the cache will not occupy too much space.
     */
    private static final int CHECKER_SUBJECT_CACHE_SIZE = 50;
    private static final long CHECKER_SUBJECT_CACHE_TIMEOUT =
        TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);

    /**
     * Constructor
     */
    public AdminSecurity(AdminService adminService, Logger logger) {

        this.logger = logger;
        this.adminService = adminService;
        final AdminServiceParams params = adminService.getParams();
        final SecurityParams secParams = params.getSecurityParams();
        final String storeName = params.getGlobalParams().getKVStoreName();

        if (secParams.isSecure()) {
            final StorageNodeParams snParams = params.getStorageNodeParams();
            final String hostname = snParams.getHostname();
            final int registryPort = snParams.getRegistryPort();

            this.paramsHandle = new AdminParamsHandle();
            this.topoResolver = new ParamTopoResolver(paramsHandle, logger);
            this.loginMgr = new InternalLoginManager(topoResolver);
            this.tokenResolver = new TokenResolverImpl(hostname, registryPort,
                                                       storeName, topoResolver,
                                                       loginMgr, logger);
            /* TODO: To be configured via parameters */
            final int roleCacheSize = 100;
            final long roleCacheEntryLifetime =
                TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);

            final CacheConfig roleCacheConfig =
                new CacheConfig().capacity(roleCacheSize).
                                  entryLifetime(roleCacheEntryLifetime);
            this.roleResolver =
                new AdminRoleResolver(adminService, roleCacheConfig);

            final AdminParams ap = params.getAdminParams();
            final int tokenCacheCapacity = ap.getLoginCacheSize();

            final GlobalParams gp = params.getGlobalParams();
            final long tokenCacheEntryLifetime =
                gp.getLoginCacheTimeoutUnit().toMillis(
                    gp.getLoginCacheTimeout());
            final CacheConfig tokenCacheConfig =
                new CacheConfig().capacity(tokenCacheCapacity).
                                  entryLifetime(tokenCacheEntryLifetime);
            this.tokenVerifier =
                new TokenVerifier(tokenCacheConfig, tokenResolver);
            final CacheConfig subjectCacheConfig =
                new CacheConfig().capacity(CHECKER_SUBJECT_CACHE_SIZE).
                                  entryLifetime(CHECKER_SUBJECT_CACHE_TIMEOUT);
            this.accessChecker =
                new AccessCheckerImpl(tokenVerifier, roleResolver,
                                      subjectCacheConfig, logger);
            this.authenticators = new HashMap<>();

            /* Initialize all supported system authenticators */
            for (final SystemAuthMethod sysAuth : SystemAuthMethod.values()) {
                final Authenticator authenticator =
                    createAuthenticator(sysAuth, secParams);

                if (authenticator != null) {
                    logger.info("AdminSecurity: " + sysAuth +
                                " authenticator is initialized");
                    authenticators.put(sysAuth.name(), authenticator);
                }
            }
        } else {
            paramsHandle = null;
            topoResolver = null;
            tokenResolver = null;
            accessChecker = null;
            loginMgr = null;
            tokenVerifier = null;
            roleResolver = null;
            authenticators = null;
        }
    }

    /**
     * For access by AdminService when a configure() operation is performed
     */
    void configure(String storeName) {
        if (loginMgr == null) {
            return;
        }
        loginMgr.logout();
        loginMgr = new InternalLoginManager(topoResolver);
        logger = adminService.getLogger();
        topoResolver.setLogger(logger);
        tokenResolver.setLogger(logger);
        tokenResolver.setStoreName(storeName);
        accessChecker.setLogger(logger);
    }

    public AccessChecker getAccessChecker() {
        return accessChecker;
    }

    public InternalLoginManager getLoginManager() {
        return loginMgr;
    }

    RoleResolver getRoleResolver() {
        return roleResolver;
    }

    Map<String, Authenticator> getAuthenticators() {
        return authenticators;
    }

    private class AdminParamsHandle implements ParamsHandle {
        @Override
        public Parameters getParameters() {

            Admin admin = adminService.getAdmin();
            if (admin == null) {
                return null;
            }

            return admin.getCurrentParameters();
        }
    }

    @Override
    public void newServiceParameters(ParameterMap map) {
        if (tokenVerifier == null) {
            return;
        }
        final AdminParams ap = new AdminParams(map);
        final int newCapacity = ap.getLoginCacheSize();

        /* Update the loginCacheSize if a new value is specified */
        if (tokenVerifier.updateLoginCacheSize(newCapacity)) {
            logger.info(String.format(
                "AdminSecurity: loginCacheSize has been updated to %d",
                newCapacity));
        }
    }

    @Override
    public void newGlobalParameters(ParameterMap map) {
        if (tokenVerifier == null) {
            return;
        }

        final GlobalParams gp = new GlobalParams(map);
        final long newLifeTime =
            gp.getLoginCacheTimeoutUnit().toMillis(gp.getLoginCacheTimeout());

        /* Update the loginCacheTimeout if a new value is specified */
        if (tokenVerifier.updateLoginCacheTimeout(newLifeTime)) {
            logger.info(String.format(
                "AdminSecurity: loginCacheTimeout has been updated to %d ms",
                newLifeTime));
        }

        if (authenticators == null) {
            return;
        }
        final String[] enabledAuthMethods = gp.getUserExternalAuthMethods();

        for (Map.Entry<String, Authenticator> entry :
                authenticators.entrySet()) {
            final String authenName = entry.getKey();
            boolean enabled = false;
            for (String authMethod : enabledAuthMethods) {
                if (authenName.equals(authMethod)) {
                    enabled = true;
                    break;
                }
            }
            if (!enabled) {
                final Authenticator authen = entry.getValue();
                logger.info("AdminSecurity: disable authenticator " +
                            authenName);
                authen.resetAuthenticator();
            }
        }
    }

    @Override
    public void newRoleDefinition(SecurityMDChange mdChange) {
        if (!(mdChange.getElement() instanceof RoleInstance)) {
            throw new AssertionError();
        }
        final RoleInstance role = (RoleInstance) mdChange.getElement();

        if (roleResolver == null) {
            return;
        }

        if (roleResolver.updateRoleCache(role)) {
            logger.fine(String.format("AdminSecurity: update role %s " +
                "instance in role cache", role.name()));
        }
        if (accessChecker.updateRoleDefinition(role)) {
            logger.fine(String.format("AdminSecurity: update role %s " +
                "definition in access checker privilege cache", role.name()));
        }
    }

    @Override
    public void newUserDefinition(SecurityMDChange mdChange) {
        /* A guard */
        if (!(mdChange.getElement() instanceof KVStoreUser)) {
            throw new AssertionError();
        }
        final KVStoreUser user = (KVStoreUser)mdChange.getElement();

        if (tokenVerifier == null) {
            return;
        }

        if (tokenVerifier.updateLoginCacheSessions(user)) {
            logger.info(String.format(
                "AdminSecurity: update sessions in login cache with " +
                "metadata %d", mdChange.getSeqNum()));
        }

        if (accessChecker.updateUserDefinition(user)) {
            logger.fine(String.format("AdminSecurity: update user %s " +
                "definition in access checker privilege cache",
                user.getName()));
        }
    }

    private Authenticator createAuthenticator(SystemAuthMethod authMethod,
                                              SecurityParams secParams) {
        try {
            return AuthenticatorManager.getAuthenticator(authMethod.name(),
                                                         secParams);
        } catch (Exception e) {
            logger.warning("AdminSecurity: initialize authenticator " +
                           authMethod + ", error " + e.getMessage());
            return null;
        }
    }
}
