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

package oracle.kv.impl.admin.plan.task;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import oracle.kv.AuthenticationRequiredException;
import oracle.kv.UnauthorizedException;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.param.GlobalParams;
import oracle.kv.impl.admin.plan.SecurityMetadataPlan;
import oracle.kv.impl.fault.ClientAccessException;
import oracle.kv.impl.security.ExecutionContext;
import oracle.kv.impl.security.KVStoreUserPrincipal;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.security.metadata.KVStoreUser;
import oracle.kv.impl.security.metadata.SecurityMetadata;
import oracle.kv.impl.security.metadata.KVStoreUser.UserType;
import oracle.kv.impl.security.util.SecurityUtils;
import oracle.kv.impl.test.TestStatus;

import com.sleepycat.persist.model.Persistent;

/**
 * Change a user
 * version 0: original
 * version 1: added pwdLifetime
 */
@Persistent(version=1)
public class ChangeUser extends UpdateMetadata<SecurityMetadata> {
    private static final long serialVersionUID = 1L;

    private String userName;
    private Boolean isEnabled;
    private char[] plainPassword;
    private boolean retainPassword;
    private boolean clearRetainedPassword;
    private Long pwdLifetime;

    public ChangeUser(SecurityMetadataPlan plan,
                      String userName,
                      Boolean isEnabled,
                      char[] plainPassword,
                      boolean retainPassword,
                      boolean clearRetainedPassword,
                      Long pwdLifetime) {
        super(plan);

        /* Check if the updated user exists */
        final SecurityMetadata secMd = plan.getMetadata();
        if ((secMd == null) || (secMd.getUser(userName) == null)) {
            throw new IllegalCommandException(
                "User with name " + userName + " does not exist!");
        }

        /* Non-admins can only modify themselves */
        final KVStoreUser modifyUser = secMd.getUser(userName);
        ExecutionContext execCtx = ExecutionContext.getCurrent();
        if (execCtx == null) {
            if (!TestStatus.isActive()) {
                throw new ClientAccessException(
                    new AuthenticationRequiredException(
                        "Authentication required for access",
                        false /* isReturnSignal */));
            }
        } else {
            final Subject reqSubject = execCtx.requestorSubject();
            final KVStoreUserPrincipal reqUser =
                ExecutionContext.getSubjectUserPrincipal(reqSubject);

            if (reqUser != null) {
                if (!modifyUser.getElementId().equals(reqUser.getUserId())) {

                    /* Check if we are admin */
                    if (!execCtx.hasPrivilege(SystemPrivilege.SYSOPER)) {
                        throw new ClientAccessException(
                            new UnauthorizedException(
                                "Admin privilege is required in order to " +
                                "modify other users."));
                    }
                }
            }
        }

        /* Guard against disabling the last admin user */
        if (isEnabled != null &&
            !isEnabled &&
            secMd.isLastSysadminUser(userName)) {

            throw new IllegalCommandException(
                "Cannot disable the last enabled admin user " + userName);
        }

        /* No password related options for external user */
        if (modifyUser.getUserType() == UserType.EXTERNAL &&
            (plainPassword != null || retainPassword || clearRetainedPassword ||
            pwdLifetime != null)) {
            throw new IllegalCommandException(
                "Cannot change the password or lifetime for external " +
                "user");
        }

        /* Could not overwrite a valid retained password */
        if (!clearRetainedPassword && retainPassword &&
            secMd.getUser(userName).retainedPasswordValid()) {
                throw new IllegalCommandException(
                    "Could not retain password: existing retained " +
                    "password should be cleared first.");
        }

        this.userName = userName;
        this.isEnabled = isEnabled;
        this.plainPassword = plainPassword == null ?
                             null :
                             Arrays.copyOf(plainPassword, plainPassword.length);
        this.retainPassword = retainPassword;
        this.clearRetainedPassword = clearRetainedPassword;
        this.pwdLifetime = pwdLifetime;
    }

    /* No-arg ctor for DPL */
    @SuppressWarnings("unused")
    private ChangeUser() {
    }

    @Override
    protected SecurityMetadata updateMetadata() {
        final SecurityMetadata secMd = plan.getMetadata();

        /* Return null if the user does not exist */
        if ((secMd == null) || (secMd.getUser(userName) == null)) {
            return null;
        }

        final KVStoreUser newCopy = secMd.getUser(userName).clone();

        if (isEnabled != null) {
            newCopy.setEnabled(isEnabled);
        }

        /* Check whether the retained password should be cleared */
        if (clearRetainedPassword || !newCopy.retainedPasswordValid()) {
            newCopy.clearRetainedPassword();
        }

        /* Set a new password if required */
        if (plainPassword != null) {
            if (retainPassword) {
                try {
                    newCopy.retainPassword();
                    /*
                     * Let the retained password expire after 24 hours
                     *
                     * TODO: read the life time from configuration
                     */
                    newCopy.getRetainedPassword().setLifetime(
                        TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS));
                } catch (IllegalStateException ise) /* CHECKSTYLE:OFF */ {
                    /*
                     * If the plan is re-executed from a failure recovery, the
                     * password may have been retained successfully in previous
                     * run. We just ignore the ISE and skip the retaining.
                     */
                } /* CHECKSTYLE:ON */
            } else {
                /*
                 * Change password without retaining, clear the afore-set
                 * retained password
                 */
                newCopy.clearRetainedPassword();
            }
            final GlobalParams params =
                plan.getAdmin().getParams().getGlobalParams();
            final long duration = params.getPasswordDefaultLifeTime();
            newCopy.setPassword(((SecurityMetadataPlan) plan).
                makeDefaultHashDigest(plainPassword)).
                setPasswordLifetime(params.getPasswordDefaultLifeTimeUnit().
                                        toMillis(duration));

            /*
             * Wipe out the plain password setting to ensure it does not hang
             * around in in the Java VM memory space.
             */
            SecurityUtils.clearPassword(plainPassword);
        }

        if (pwdLifetime != null) {
            newCopy.setPasswordLifetime(pwdLifetime);
        }
        secMd.updateUser(newCopy.getElementId(), newCopy);
        plan.getAdmin().saveMetadata(secMd, plan);

        return secMd;
    }

    @Override
    public boolean logicalCompare(Task t) {
        if (this == t) {
            return true;
        }

        if (t == null) {
            return false;
        }

        if (getClass() != t.getClass()) {
            return false;
        }

        ChangeUser other = (ChangeUser) t;
        if (!userName.equals(other.userName)) {
            return false; 
        }

        if (isEnabled != other.isEnabled) {
            return false;
        }

        if (!Arrays.equals(plainPassword, other.plainPassword)) {
            return false;
        }

        if (retainPassword != other.retainPassword) {
            return false;
        }

        if (clearRetainedPassword != other.clearRetainedPassword) {
            return false;
        }

        if (pwdLifetime == null) {
            if (other.pwdLifetime != null) {
                return false;
            }
        } else if (!pwdLifetime.equals(other.pwdLifetime)) {
            return false;
        }

        return true;
    }
}
