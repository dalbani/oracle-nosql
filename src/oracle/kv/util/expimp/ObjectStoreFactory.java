/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
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

package oracle.kv.util.expimp;


/**
 * Factory class used to create instances of ObjectStoreExport and
 * ObjectStoreImport using reflection.
 */
abstract class ObjectStoreFactory {

    static final String factoryClassName =
        "oracle.kv.util.expimp.ObjectStoreFactoryImpl";
    static final String[] cloudStorageClassNames =
        {/* class in oracle.cloud.storage.api.jar */
         "oracle.cloud.storage.CloudStorage",
         /* class in jersey-client.jar */
         "com.sun.jersey.api.client.ClientHandlerException",
         /* class in jersey-core.jar */
         "com.sun.jersey.spi.inject.Errors$Closure",
         /* class in jettison.jar */
         "org.codehaus.jettison.json.JSONException"};

    /**
     * Creates and returns an ObjectStoreFactory if the underlying Oracle
     * storage cloud service class is available, otherwise returns null
     *
     * @return the factory or null
     */
    static ObjectStoreFactory createFactory() {

        /*
         * Check if oracle storage cloud service dependent classes are present.
         */
        for (String className : cloudStorageClassNames) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        try {
            Class<?> factoryClass = Class.forName(factoryClassName);
            return (ObjectStoreFactory) factoryClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception creating" +
                                       " ObjectStoreFactory: " + e.getMessage(),
                                       e);
        }
    }

    abstract AbstractStoreExport createObjectStoreExport(
        String storeName,
        String[] helperHosts,
        String userName,
        String securityFile,
        String containerName,
        String serviceName,
        String objectStoreUserName,
        String objectStorePassword,
        String serviceUrl,
        boolean json);

    abstract AbstractStoreImport createObjectStoreImport(
        String storeName,
        String[] helperHosts,
        String userName,
        String securityFile,
        String containerName,
        String serviceName,
        String objectStoreUserName,
        String objectStorePassword,
        String serviceUrl,
        String status,
        boolean json);
}
