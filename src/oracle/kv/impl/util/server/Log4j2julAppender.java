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

package oracle.kv.impl.util.server;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * This is a Log4J Appender implementation that redirects log4j logging to
 * a java.util.logging.Logger.
 */

public class Log4j2julAppender extends AppenderSkeleton {

    private static Map<Entry<Logger,String>, Log4j2julAppender> appenders =
        new HashMap<>();

    /**
     * Get an appender that is configured for the given logger and prefix.
     * This produces a singleton appender per logger/prefix, to avoid creating
     * the same appender repeatedly.  The prefix is just a string that is
     * prepended to every log message to help with identifying the source.
     * We use Map.Entry as a simple tuple, to combine the logger and prefix into a
     * key for the Map.
     */
    public static synchronized Log4j2julAppender getAppender4Logger
        (Logger r, String prefix) {
        
        Entry<Logger, String> key = new SimpleImmutableEntry<>(r, prefix);
        Log4j2julAppender appender = appenders.get(key);
        if (null == appender) {
            appender = new Log4j2julAppender(r, prefix);
            appenders.put(key, appender);
        }
        return appender;
    }

    private final Logger logger;
    private final String prefix;

    private Log4j2julAppender(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
    }

    @Override
    protected void append(LoggingEvent ev) {
        ThrowableInformation ti = ev.getThrowableInformation();
        if (ti == null) {
            logger.log(mapLevel(ev.getLevel()),
                       prefix + (String)ev.getMessage());
        } else {
            logger.log(mapLevel(ev.getLevel()),
                       prefix + (String)ev.getMessage(),
                       ti.getThrowable());
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    /*
     * Map from a log4j level to a java.util.logging level.
     */
    private java.util.logging.Level mapLevel
        (org.apache.log4j.Level log4jLevel) {

        int val = log4jLevel.toInt();

        switch (val) {

        case org.apache.log4j.Priority.FATAL_INT:
        case org.apache.log4j.Priority.ERROR_INT:
            return java.util.logging.Level.SEVERE;

        case org.apache.log4j.Priority.WARN_INT:
            return java.util.logging.Level.WARNING;

        case org.apache.log4j.Priority.INFO_INT:
            return java.util.logging.Level.INFO;

        case org.apache.log4j.Priority.DEBUG_INT:
            return java.util.logging.Level.FINE;

        case org.apache.log4j.Level.TRACE_INT:
        default:
            return java.util.logging.Level.FINEST;
        }
    }            
}
