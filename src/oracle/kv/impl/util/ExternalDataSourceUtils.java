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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import oracle.kv.Consistency;
import oracle.kv.ParamConstant;
import oracle.kv.impl.param.ParameterMap;

import com.sleepycat.je.utilint.PropUtil;

/**
 * Utility class for different external data sources.
 */
public final class ExternalDataSourceUtils {

    /* Check that all parameters in the arguments map are known. */
    public static void checkParams(final ParameterMap params) {
        final Map<String, ParamConstant> allParams =
            ParamConstant.getAllParams();
        for (String paramName : params.keys()) {
            if (!allParams.containsKey(paramName)) {
                throw new IllegalArgumentException
                    (paramName + " is not a recognized parameter.");
            }
        }
    }

    /*
     * Convert a Consistency specification into an oracle.kv.Consistency
     * instance.
     */
    public static Consistency parseConsistency(final String consistencyString) {
        Consistency consistency = null;
        if ("ABSOLUTE".equalsIgnoreCase(consistencyString)) {
            consistency = Consistency.ABSOLUTE;
        } else if ("NONE_REQUIRED_NO_MASTER".equalsIgnoreCase(
            consistencyString)) {
            consistency = Consistency.NONE_REQUIRED_NO_MASTER;
        } else if ("NONE_REQUIRED".equalsIgnoreCase(consistencyString)) {
            consistency = Consistency.NONE_REQUIRED;
        } else if ("TIME".regionMatches
                   (true, 0, consistencyString, 0, 4)) {
            final String consistencyParamName =
                ParamConstant.CONSISTENCY.getName();
            final int firstParenIdx = consistencyString.indexOf("(");
            final int lastParenIdx = consistencyString.indexOf(")");
            if (firstParenIdx < 0 || lastParenIdx < 0 ||
                lastParenIdx < firstParenIdx) {
                throw new ExternalDataSourceException
                    (consistencyParamName + " value of " +
                     consistencyString + " is formatted incorrectly.");
            }
            final String timeParam =
                consistencyString.substring(firstParenIdx + 1, lastParenIdx);
            final String[] lagAndTimeout = timeParam.split(",");
            if (lagAndTimeout.length != 2) {
                throw new ExternalDataSourceException
                    (consistencyParamName + " value of " +
                     consistencyString + " is formatted incorrectly.");
            }

            try {
            final long permissibleLagMSecs =
                PropUtil.parseDuration(lagAndTimeout[0].trim());
            final long timeoutMSecs =
                PropUtil.parseDuration(lagAndTimeout[1].trim());
            consistency =
                new Consistency.Time(permissibleLagMSecs,
                                     TimeUnit.MILLISECONDS,
                                     timeoutMSecs,
                                     TimeUnit.MILLISECONDS);
            } catch (IllegalArgumentException IAE) {
                throw new ExternalDataSourceException
                    (consistencyParamName + " value of " +
                     consistencyString + " is formatted incorrectly.");
            }
        } else {
            throw new ExternalDataSourceException
                ("Unknown consistency specified: " + consistencyString);
        }

        return consistency;
    }

    /* Convert a time + optional timeunit to milliseconds. */
    public static int parseTimeout(final String timeoutValue) {
        return PropUtil.parseDuration(timeoutValue);
    }
}
