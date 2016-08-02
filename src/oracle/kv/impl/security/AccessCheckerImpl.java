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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import oracle.kv.AuthenticationRequiredException;
import oracle.kv.UnauthorizedException;
import oracle.kv.impl.security.login.LoginToken;
import oracle.kv.impl.security.login.TokenVerifier;
import oracle.kv.impl.security.metadata.KVStoreUser;
import oracle.kv.impl.security.util.BloomFilter;
import oracle.kv.impl.security.util.Cache;
import oracle.kv.impl.security.util.CacheBuilder;
import oracle.kv.impl.security.util.CacheBuilder.CacheConfig;
import oracle.kv.impl.security.util.CacheBuilder.CacheEntry;

/**
 * Standard implementation of AccessChecker.
 */
public class AccessCheckerImpl implements AccessChecker {
    private final TokenVerifier verifier;
    private final RoleResolver roleResolver;
    private volatile Logger logger;

    /* Cache for mapping a user to all his privileges */
    private final Cache<String, PrivilegeEntry> userPrivCache;

    /**
     * Construct a AccessChecker that uses the provided TokenVerifier
     * to validate method calls. If the cache config is set to null, caches will
     * be disabled.
     */
    public AccessCheckerImpl(TokenVerifier verifier,
                             RoleResolver resolver,
                             CacheConfig config,
                             Logger logger) {
        this.verifier = verifier;
        this.roleResolver = resolver;
        this.logger = logger;
        if (config != null) {
            userPrivCache =
                CacheBuilder.build(config);
        } else {
            userPrivCache = null;
        }
    }

    /**
     * Log a message describing an access error.
     * @param msg a general message describing the cause of the error
     * @param execCtx the ExecutionContext that encountered the error
     * @param opCtx the OperationContext that was being attempted
     */
    public void logError(String msg,
                         ExecutionContext execCtx,
                         OperationContext opCtx) {

        AccessCheckUtils.logSecurityError(msg, opCtx.describe(), execCtx,
                                          logger);
    }

    /**
     * Updates the logger used by this instance.  The logger must always be
     * non-null but it may be changed.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Identifies the requestor of an operation.
     * @param context the identifying context provided by the caller.
     *   This is null allowable.
     * @return a Subject object if the identity could be determined,
     *   or else null.
     * @throw SessionAccessException
     */
    @Override
    public Subject identifyRequestor(AuthContext context)
        throws SessionAccessException {

        if (context == null) {
            return null;
        }

        final LoginToken token = context.getLoginToken();

        if (token == null) {
            return null;
        }

        try {
            return verifier.verifyToken(token);
        } catch (SessionAccessException sae) {
            /*
             * rethrow indicating that the access exception applies to the
             * token supplied with the AuthContext.
             */
            throw new SessionAccessException(sae,
                                             false /* isReturnSignal */);
        }
    }

    /**
     * Check the authorization of the requestor against the requirements
     * of the operation.
     */
    @Override
    public void checkAccess(ExecutionContext execCtx, OperationContext opCtx)
        throws AuthenticationRequiredException, UnauthorizedException {

        final List<? extends KVStorePrivilege> requiredPrivileges =
            opCtx.getRequiredPrivileges();

        if (requiredPrivileges.size() == 0) {
            /*
             * subject could be null here, either because token was null
             * or because it couldn't be validated, but since there are
             * no authentication requirements, we don't worry about it
             * here.
             */
            return;
        }

        final Subject subject = execCtx.requestorSubject();

        if (subject == null) {
            final AuthContext secCtx = execCtx.requestorContext();
            if (secCtx == null || secCtx.getLoginToken() == null) {
                logError("Attempt to call method without authentication",
                         execCtx, opCtx);
                throw new AuthenticationRequiredException(
                    "Authentication required for access",
                    false /* isReturnSignal */);
            }

            /* Because we had a token, it must have been invalid */
            logError("Attempt to call method with invalid authentication",
                     execCtx, opCtx);
            throw new AuthenticationRequiredException(
                "Authentication required for access",
                false /* isReturnSignal */);
        }

        /*
         * Checks whether all required privileges are implied by requestor's
         * granted privileges.
         */
        if (!execCtx.hasAllPrivileges(requiredPrivileges)) {
            /* Permission check failed. */
            logError("Insufficient access rights", execCtx, opCtx);
            throw new UnauthorizedException(
                "Insufficient access rights granted");
        }
    }

    /**
     * Identifies privileges of a specified Subject
     */
    @Override
    public Set<KVStorePrivilege> identifyPrivileges(Subject reqSubj) {
        if (reqSubj == null) {
            return null;
        }

        final KVStoreUserPrincipal user =
            ExecutionContext.getSubjectUserPrincipal(reqSubj);

        /*
         * For subjects from internal login and anonymous login, the user
         * principal could be null. We do not cache in these cases, since both
         * subjects have a limit number of system built-in roles, and thus
         * their privileges can be resolved quickly.
         */
        if (userPrivCache != null && user != null) {
            final PrivilegeEntry privEntry =
                userPrivCache.get(user.getUserId());
            if (privEntry != null) {
                return privEntry.getPrivileges();
            }
        }

        /*
         * No cached subj privileges, try to resolve by recursively traversing
         * the granted role
         */
        final Set<KVStorePrivilege> subjPrivSet =
            new HashSet<KVStorePrivilege>();
        final Set<String> subjRoleSet = new HashSet<String>();

        final Set<KVStoreRolePrincipal> reqRoles =
            reqSubj.getPrincipals(KVStoreRolePrincipal.class);
        for (final KVStoreRolePrincipal princ : reqRoles) {
            final String roleName = princ.getName();
            recursiveGetRolesAndPrivis(roleName, subjRoleSet, subjPrivSet);
        }

        if (userPrivCache != null  && user != null) {
            userPrivCache.put(user.getUserId(),
                              new PrivilegeEntry(user.getUserId(),
                                                 subjPrivSet,
                                                 subjRoleSet));
        }
        return subjPrivSet;
    }

    /**
     * Get role privileges recursively.
     *
     * @param roleName of role that need to get all privileges recursively.
     * @param roleSet contains all leaf roles granted to this role
     * @param priviSet contains all privileges of given role and its granted
     * roles.
     */
    private void recursiveGetRolesAndPrivis(String roleName,
                                            Set<String> roleSet,
                                            Set<KVStorePrivilege> priviSet) {
        final RoleInstance role = roleResolver.resolve(roleName);
        if (role == null) {
            logger.info("Could not resolve role with name of " + roleName);
        } else {
            logger.fine("Role " + roleName + " resolved successfully.");
            priviSet.addAll(role.getPrivileges());
            roleSet.add(roleName);
            for (final String grantedRole : role.getGrantedRoles()) {
                recursiveGetRolesAndPrivis(grantedRole, roleSet, priviSet);
            }
        }
    }

    /**
     * Updates the cache due to role definition change, including the changes
     * of granted roles or privileges of this role.
     *
     * @return true if there are privilege entries contains this role
     * information and removed successfully.
     */
    public boolean updateRoleDefinition(RoleInstance role) {
        /* signal indicate if any entry is removed from the cache*/
        boolean removed = false;
        final Collection<PrivilegeEntry> allPrivEntries =
                userPrivCache.getAllValues();
        for (PrivilegeEntry privEntry : allPrivEntries) {
            if (privEntry.hasRole(role.name())) {
                PrivilegeEntry entry =
                    userPrivCache.invalidate(privEntry.getPrincId());

                if (!removed) {
                    removed = (entry != null);
                }
            }
        }
        return removed;
    }

    /**
     * Updates the cache due to user definition change, which is the changes
     * of granted roles of this user.
     *
     * @return true if there is user in privilege cache and removed
     * successfully.
     */
    public boolean updateUserDefinition(KVStoreUser user) {

        return (userPrivCache.invalidate(user.getElementId()) != null);
    }

    /**
     * Privilege entry for subject-privilege cache.  Besides privileges, each
     * entry also contains a bloom filter of leaf roles the subject contains,
     * in order to ease the user lookup when a role change comes for cache
     * update. Note that using BllomFilter to check role existence would have
     * false positives. However, false positives here could lead to only
     * excessive cache invalidate and update, and thus do not harm the whole
     * correctness.
     */
    public static class PrivilegeEntry extends CacheEntry {
        private final Set<KVStorePrivilege> privsSet;
        private final String princId;
        private final byte[] leafRoleBf;

        public PrivilegeEntry(String princId,
                              Set<KVStorePrivilege> privsSet,
                              Set<String> leafRoles) {
            this.privsSet = privsSet;
            this.princId = princId;

            /* Build flat role bloom filter */
            if (leafRoles == null || leafRoles.isEmpty()) {
                leafRoleBf = null;
            } else {
                final int bfSize = BloomFilter.getByteSize(leafRoles.size());
                leafRoleBf = new byte[bfSize];
                final BloomFilter.HashContext hc =
                    new BloomFilter.HashContext();
                for (String role : leafRoles) {
                    BloomFilter.add(
                        leafRoleBf,
                        RoleInstance.getNormalizedName(role).getBytes(),
                        hc);
                }
            }
        }

        /**
         * Get the id of principal owns this entry, could be a user id.
         */
        String getPrincId() {
            return princId;
        }

        public Set<KVStorePrivilege> getPrivileges() {
            return Collections.unmodifiableSet(privsSet);
        }

        public boolean hasRole(String name) {
            if (leafRoleBf == null) {
                return false;
            }
            return BloomFilter.contains(
                leafRoleBf, RoleInstance.getNormalizedName(name).getBytes());
        }
    }
}
