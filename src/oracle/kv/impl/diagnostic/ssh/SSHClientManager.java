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

package oracle.kv.impl.diagnostic.ssh;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import oracle.kv.impl.diagnostic.SNAInfo;

/**
 * A manager to manage all SSH clients.
 */
public class SSHClientManager {
    private static String AT_SIGN = "@";

    /* Store host@username and SSHClient pairs */
    private static Map<String, SSHClient> clientCache =
            new ConcurrentHashMap<String, SSHClient>();
    /**
     * Get a client according to the info of a SNA
     *
     * @param snaInfo the info of a SNA
     * @return SSHClient
     */
    public static SSHClient getClient(SNAInfo snaInfo) {

        SSHClient client = clientCache.get(getCacheKey(snaInfo.getHost(),
                                                       snaInfo.getSSHUser()));
        if (client == null) {
            /*
             * Instantiate a new SSHClient when do not find a existing client
             * in cache
             */
            client = new SSHClient(snaInfo.getHost(), snaInfo.getSSHUser());
            client.open();
            if (client.isOpen()) {
                clientCache.put(getCacheKey(snaInfo.getHost(),
                                            snaInfo.getSSHUser()), client);
            }
        }
        return client;
    }

    /**
     * Get clients according to the info of SNAs
     *
     * @param snaList the info of SNAs
     * @return Map of SNAInfo and SSHClient pairs
     * @throws Exception
     */
    public static Map<SNAInfo, SSHClient> getClient(List<SNAInfo> snaList)
            throws Exception {
        Map<SNAInfo, SSHClient> clientMap =
                new ConcurrentHashMap<SNAInfo, SSHClient>();
        int numberSSHThread = snaList.size();
        ThreadPoolExecutor threadExecutor =
                new ThreadPoolExecutor(numberSSHThread, numberSSHThread, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        try {
            Map<SNAInfo, Future<SSHClient>> futurnMap =
                    new HashMap<SNAInfo, Future<SSHClient>>();

            /* Get clients in parallel to accelerate the performance */
            for (final SNAInfo snaInfo : snaList) {
                 Callable<SSHClient> snaClientCallable =
                         new Callable<SSHClient>() {

                    @Override
                    public SSHClient call() {
                        SSHClient client = clientCache.
                                get(getCacheKey(snaInfo.getHost(),
                                                snaInfo.getSSHUser()));
                        if (client == null) {
                            /*
                             * Instantiate a new SSHClient when do not find a
                             * existing client in cache
                             */
                            client = new SSHClient(snaInfo.getHost(),
                                                 snaInfo.getSSHUser());

                            /* Open client by authenticated file at first */
                            client.openByAuthenticatedFile();
                        }
                        return client;
                    }
                };
                futurnMap.put(snaInfo,
                              threadExecutor.submit(snaClientCallable));
            }

            /*
             * Check the open status of all clients to validate whether client
             * is open or not
             */
            for (Map.Entry<SNAInfo, Future<SSHClient>> entry :
                    futurnMap.entrySet()) {
                SNAInfo snaInfo = entry.getKey();
                SSHClient client = entry.getValue().get();

                if (client == null) {
                    continue;
                }

                /*
                 * Try to open client by password when the status of client is
                 * not open
                 */
                if (!client.isOpen()  && client.getErrorMessage() == null) {
                    SSHClient existingClient = clientCache.
                            get(getCacheKey(snaInfo.getHost(),
                                            snaInfo.getSSHUser()));

                    if (existingClient == null) {
                        client.openByPassword();
                    } else {
                        client = existingClient;
                    }
                }
                /*
                 * Put the open client into cache, and it can be reuse in the
                 * next time
                 */
                if (client.isOpen()) {
                    clientCache.put(getCacheKey(snaInfo.getHost(),
                                                snaInfo.getSSHUser()), client);
                }

                clientMap.put(snaInfo, client);
            }
        } finally {
            threadExecutor.shutdown();
        }

        return clientMap;
    }

    /**
     * Get key to find SSH Client
     *
     */
    private static String getCacheKey(String host, String username) {
        return username.toLowerCase(Locale.US).trim() + AT_SIGN +
               host.toLowerCase(Locale.US).trim();
    }

    /**
     * Clear all cached clients
     */
    public static void clearClients() {
        for (ConcurrentMap.Entry<String, SSHClient> entry :
                clientCache.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().close();
            }
        }
        clientCache.clear();
    }
}
