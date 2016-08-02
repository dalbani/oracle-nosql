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

package oracle.kv.hadoop.table;

import java.io.IOException;

import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * A Hadoop InputFormat class for reading data from an Oracle NoSQL Database.
 * Map/reduce keys and values are returned as PrimaryKey and Row objects
 * respectively.
 * <p>
 * For information on the parameters that may be passed to this class,
 * refer to the javadoc for the parent class of this class;
 * <code>TableInputFormatBase</code>.
 * <p>
 * A simple example demonstrating the Oracle NoSQL DB Hadoop
 * oracle.kv.hadoop.table.TableInputFormat class can be found in the
 * KVHOME/example/table/hadoop directory. It demonstrates how, in a MapReduce
 * job, to read records from an Oracle NoSQL Database that were written using
 * Table API.  The javadoc for that program describes the simple Map/Reduce
 * processing as well as how to invoke the program in Hadoop.
 * <p>
 * @since 3.1
 */
public class TableInputFormat extends TableInputFormatBase<PrimaryKey, Row> {

    /**
     * Returns the RecordReader for the given InputSplit.
     */
    @Override
    public RecordReader<PrimaryKey, Row>
        createRecordReader(InputSplit split, TaskAttemptContext context)
        throws IOException, InterruptedException {

        final TableRecordReader ret = new TableRecordReader();
        ret.initialize(split, context);
        return ret;
    }
}
