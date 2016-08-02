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

package oracle.kv.impl.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import com.sun.management.GarbageCollectionNotificationInfo;
import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;

public class GCUtils {

    /**
     * Registers listeners for GC events which log GC events. Returns true if
     * the registration was successful, otherwise false. Upon receiving an
     * event the listener will log GC data depending on the type and duration
     * of the operation. Old generation GC operations over oldThresholdMs and
     * young gen. operations over youngThresholdMs are logged at WARNING. All
     * others are logged at FINE.
     *
     * Logging for GC events that are not over the threshold is done through
     * a RateLimitingLogger and the sample period is one minute.
     *
     * This method depends on com.sun classes and their behavior. If running
     * in some other environment the logging may not be available.
     *
     * @param youngThresholdMs the threshold for logging young gen operations
     * @param oldThresholdMs the threshold for logging old gen operations
     * @param logger logger to use
     * @param classloader class loader to check search for com.sun classes
     *
     * @return true if the registration was successful, otherwise false
     */
    public static boolean monitorGC(final int youngThresholdMs,
                                    final int oldThresholdMs,
                                    final Logger logger,
                                    ClassLoader classloader) {
        /* Check on whether the com.sun classes are present */
        try {
            Class.
                 forName("com.sun.management.GarbageCollectionNotificationInfo",
                         false, classloader);
        } catch (ClassNotFoundException cnfe) {
            return false;
        }

        /* Limit the under threshold logging to 1 per min. */
        final RateLimitingLogger<String> rateLimitingLogger =
                   new RateLimitingLogger<String>(60 * 1000, 2, logger);

        final List<GarbageCollectorMXBean> gcBeans =
                ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean gcBean : gcBeans) {
            if (!(gcBean instanceof NotificationEmitter)) {
                continue;
            }
            logger.log(Level.INFO,
                       "Registering GC listener with {0}", gcBean.getName());
            final NotificationEmitter emitter = (NotificationEmitter)gcBean;
            emitter.addNotificationListener(new NotificationListener() {
                @Override
                public void handleNotification(Notification notification,
                                               Object handback) {
                    if (!notification.getType().
                                      equals(GARBAGE_COLLECTION_NOTIFICATION)) {
                        return;
                    }
                    final GarbageCollectionNotificationInfo info =
                            GarbageCollectionNotificationInfo.from(
                                     (CompositeData)notification.getUserData());

                    /*
                     * Adjust the logging level based on the type of GC and how
                     * long the operation took.
                     */
                    Level logLevel = Level.FINE;
                    final long duration = info.getGcInfo().getDuration();

                    String gcType = info.getGcAction();
                    if ("end of minor GC".equals(gcType)) {
                        gcType = "Young Gen GC";

                        if (duration > youngThresholdMs) {
                            logLevel = Level.WARNING;
                        }
                    } else if ("end of major GC".equals(gcType)) {
                        gcType = "Old Gen GC";

                        if (duration > oldThresholdMs) {
                            logLevel = Level.WARNING;
                        }
                    } else {
                        /* Unknown operation */
                        return;
                    }
                    if (!logger.isLoggable(logLevel)) {
                        return;
                    }
                    final StringBuilder sb = new StringBuilder();
                    sb.append(gcType).append(": ").
                       append(info.getGcInfo().getId()).append(" ").
                       append(info.getGcName()).append(" (").
                       append(info.getGcCause()).append(") took ").
                       append(duration).append(" milliseconds");

                    if (logLevel.equals(Level.WARNING)) {
                        logger.log(logLevel, sb.toString());
                    } else {
                        rateLimitingLogger.log(gcType, logLevel, sb.toString());
                    }
                }
            }, null, null);
        }
        return true;
    }
}
