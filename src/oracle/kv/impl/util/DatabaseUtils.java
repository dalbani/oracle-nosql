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

package oracle.kv.impl.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sleepycat.je.Durability;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockTimeoutException;
import com.sleepycat.je.rep.InsufficientAcksException;
import com.sleepycat.je.rep.InsufficientReplicasException;
import com.sleepycat.je.rep.ReplicaWriteException;
import com.sleepycat.je.rep.ReplicatedEnvironment;
import com.sleepycat.je.rep.ReplicationConfig;
import com.sleepycat.je.rep.UnknownMasterException;
import com.sleepycat.je.rep.impl.RepParams;

/**
 * Collection of utilities for JE Database operations
 */
public class DatabaseUtils {

    /**
     * Prevent instantiation.
     */
    private DatabaseUtils() {
    }

    /**
     * Handles an exception opening a replicated DB. Returns
     * true if the open should be retried otherwise the exception is
     * re-thrown.
     *
     * @param re the exception from the open
     * @param logger a logger
     * @param dbName name of DB that was opened
     * @return true if the open should be retried
     */
    public static boolean handleException(RuntimeException re,
                                          Logger logger,
                                          String dbName) {
        try {
            throw re;
        } catch (ReplicaWriteException rwe) {

            /*
             * Master has not had a chance to create the database as
             * yet, or the current environment (in the replica, or
             * unknown) state is lagging or the node has become a
             * replica. Wait, giving the environment
             * time to catch up and become current.
             */
            logger.log(Level.FINE,
                       "Failed to open database for {0}. {1}",
                       new Object[] {dbName, rwe.getMessage()});
            return true;
        } catch (UnknownMasterException ume) {

            /*
             * Master has not had a chance to create the database as
             * yet, or the current environment (in the replica, or
             * unknown) state is lagging or has become a replica.
             * Wait, giving the environment time to catch up and
             * become current.
             */
            logger.log(Level.FINE,
                       "Failed to open database for {0}. {1}",
                       new Object[] {dbName, ume.getMessage()});
            return true;
        } catch (InsufficientReplicasException ire) {
            logger.log(Level.FINE,
                       "Insufficient replicas when creating " +
                       "database {0}. {1}",
                       new Object[] {dbName, ire.getMessage()});
            return true;
        } catch (InsufficientAcksException iae) {
            logger.log(Level.FINE,
                       "Insufficient acks when creating database {0}. {1}",
                       new Object[] {dbName, iae.getMessage()});
            /*
             * Database has already been created locally, ignore
             * the exception.
             */
            return false;
        } catch (IllegalStateException ise) {
            logger.log(Level.FINE,
                       "Problem accessing database {0}. {1}",
                       new Object[] {dbName, ise.getMessage()});
            return true;
        } catch (LockTimeoutException lte) {
            logger.log(Level.FINE, "Failed to open database for {0}. {1}",
                       new Object[] {dbName, lte.getMessage()});
            return true;
        }
    }

    /*
     * Resets the members of the JE replication group, replacing the group
     * members with the single member associated with the specified
     * environment.  This method does what DbResetRepGroup.reset does, but
     * using the specified configuration properties rather reading the
     * configuration from the environment directory.  Note that the
     * configuration arguments will be modified.
     *
     * @param envDir the node's replicated environment directory
     * @param envConfig the environment configuration
     * @param repConfig the replicated environment configuration
     * @see com.sleepycat.je.rep.util.DbResetRepGroup#reset
     */
    /* TODO: Consider creating a JE entrypoint to do this */
    public static void resetRepGroup(File envDir,
                                     EnvironmentConfig envConfig,
                                     ReplicationConfig repConfig) {
        final Durability durability =
            new Durability(Durability.SyncPolicy.SYNC,
                           Durability.SyncPolicy.SYNC,
                           Durability.ReplicaAckPolicy.NONE);

        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        envConfig.setDurability(durability);
        repConfig.setHelperHosts(repConfig.getNodeHostPort());

        /* Force the re-initialization upon open. */
        repConfig.setConfigParam(RepParams.RESET_REP_GROUP.getName(), "true");

        /* Open the environment, thus replacing the group. */
        final ReplicatedEnvironment repEnv =
            new ReplicatedEnvironment(envDir, repConfig, envConfig);

        repEnv.close();
    }
}
