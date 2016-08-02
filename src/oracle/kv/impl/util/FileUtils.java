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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Collection of utilities for file operations
 */
public class FileUtils {

    /**
     * Copy a file
     * @param sourceFile the file to copy from, which must exist
     * @param destFile the file to copy to.  The file is created if it does
     *        not yet exist.
     */
    public static void copyFile(File sourceFile, File destFile)
        throws IOException {

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (final FileInputStream source = new FileInputStream(sourceFile);
             final FileOutputStream dest = new FileOutputStream(destFile)) {
            final FileChannel sourceChannel = source.getChannel();
            dest.getChannel().transferFrom(sourceChannel, 0,
                                           sourceChannel.size());
        }
    }

    /**
     * Write a string to file.
     */
    public static void writeStringToFile(File destFile, String text)
        throws IOException {

        try (final BufferedWriter out =
                 new BufferedWriter(new FileWriter(destFile))) {
            out.write(text);
        }
    }

    /**
     * Write binary data to file.
     */
    public static void writeBytesToFile(final File destFile,
                                        final byte[] bytes)
        throws IOException {

        OutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(destFile));
            output.write(bytes);
        }
        finally {
            if (output != null) {
                output.close();
            }
        }
    }
}

