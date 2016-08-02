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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.LogRecord;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * General Export/Import utility class
 */
public class Utilities {

    /**
     * Generate a CRC checksum given the record key and value bytes
     */
    public static long getChecksum(byte[] keyBytes,
                                   byte[] valueBytes) {

        byte[] recordBytes =
            new byte[keyBytes.length + valueBytes.length];

        System.arraycopy(keyBytes, 0, recordBytes,
                         0, keyBytes.length);

        System.arraycopy(valueBytes, 0, recordBytes,
                         keyBytes.length, valueBytes.length);

        Checksum checksum = new CRC32();
        checksum.update(recordBytes, 0, recordBytes.length);

        return checksum.getValue();
    }

    /**
     * Format the LogRecord needed for export and import
     *
     * @param record LogRecord
     */
    public static String format(LogRecord record) {

        Throwable throwable = record.getThrown();
        String throwableException = "";

        if (throwable != null) {
            throwableException = "\n" + throwable.toString();
        }

        Date date=new Date(record.getMillis());
        SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String dateFormat = simpleDateFormat.format(date);

        return dateFormat + " " + record.getLevel().getName() + ": " +
               record.getMessage() + throwableException + "\n";
    }
}
