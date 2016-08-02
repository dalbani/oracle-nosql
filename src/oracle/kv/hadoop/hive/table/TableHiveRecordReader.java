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

package oracle.kv.hadoop.hive.table;

import java.io.IOException;

import oracle.kv.hadoop.table.TableRecordReader;
import oracle.kv.table.Row;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.RecordReader;

/**
 * Concrete implementation of RecordReader; used to read PrimaryKey/Row pairs
 * from an InputSplit.
 * <p>
 * Note that this RecordReader is based on version 1 of MapReduce (as
 * required by the Hive infrastructure), but wraps and delegates to a YARN
 * based (MapReduce version 2) RecordReader. This is done because the
 * RecordReader provided by Oracle NoSQL Database to support Hadoop
 * integration is YARN based, and this class wishes to exploit and reuse
 * the functionality already provided by the YARN based RecordReader class.
 */
public class TableHiveRecordReader implements RecordReader<Text, Text> {

    private static final Log LOG =
        LogFactory.getLog("oracle.kv.hadoop.hive.table.TableHiveRecordReader");

    private TableRecordReader v2RecordReader;

    public TableHiveRecordReader(TableRecordReader v2RecordReader) {
        this.v2RecordReader = v2RecordReader;
    }

    @Override
    public void close() throws IOException {
        /* Close and null out for GC */
        if (v2RecordReader != null) {
            v2RecordReader.close();
            v2RecordReader = null;
        }
        V1V2TableUtil.resetInputJobInfoForNewQuery();
    }

    @Override
    public long getPos() throws IOException {
        return 0L;
    }

    @Override
    public float getProgress() {
        return v2RecordReader.getProgress();
    }

    @Override
    public boolean next(Text key, Text value) {

        if (key == null || value == null) {
            return false;
        }
        boolean ret = false;
        try {
            key.clear();
            value.clear();
            ret = v2RecordReader.nextKeyValue();
            if (ret) {
                final Row curRow = v2RecordReader.getCurrentValue();
                assert curRow != null;
                key.set(curRow.createPrimaryKey().toString());
                value.set(curRow.toString());
            }
        } catch (Exception e) {
            LOG.error("TableHiveRecordReader " + this + " caught: " + e, e);
        }
        return ret;
    }

    /**
     * Get the current key.
     *
     * @return the current key or null if there is no current key
     */
    @Override
    public Text createKey() {
        return new Text();
    }

    /**
     * Get the current value.
     * @return the object that was read
     */
    @Override
    public Text createValue() {
        return new Text();
    }
}
