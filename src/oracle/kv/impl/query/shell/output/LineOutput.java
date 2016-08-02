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
package oracle.kv.impl.query.shell.output;

import java.io.PrintStream;

import oracle.kv.StatementResult;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldValue;
import oracle.kv.table.RecordValue;
import oracle.kv.util.shell.Column;
import oracle.kv.util.shell.Shell;

/**
 * The class is used to display records in line mode, one value per line.
 *
 * e.g. Table users contains 3 fields:
 *  - id INTEGER
 *  - name STRING
 *  - age INTEGER
 *
 * onql-> select * from users;
 * > Row 1
 * +------+------------+
 * | id   | 1          |
 * | name | Jack Smith |
 * | age  | 20         |
 * +------+------------+
 *
 * > Row 2
 * +------+------------+
 * | id   | 2          |
 * | name | Tom White  |
 * | age  | 30         |
 * +------+------------+
 *
 * > Row #
 * ...
 */

public class LineOutput extends ColumnOutput {

    /* The column to store field names */
    private Column colLabel;

    /* The column to store field values */
    private Column colData;

    LineOutput(final Shell shell,
               final PrintStream output,
               final StatementResult statementResult,
               final boolean pagingEnabled,
               final int pageHeight) {
        super(shell, output, statementResult, pagingEnabled, pageHeight,
              new TableFormat(true  /* hasLeftRightBorder */,
                              false /* hasHeaderDelimiter */,
                              true  /* hasRowSeparator */)
            );
    }

    /**
     * Initializes the label column(field names) and data column(field values).
     */
    @Override
    void initColumns() {
        columns = new Column[2];
        boolean isCompositeColumn = false;
        for(String fieldName : recordDef.getFields()) {
            final FieldDef fdef = recordDef.getField(fieldName);
            if (useCompositeColumn(fdef)) {
                isCompositeColumn = true;
                break;
            }
        }

        colLabel = new Column();
        if (isCompositeColumn) {
            colData = new CompositeColumn();
        } else {
            colData = new Column();
        }
        columns[0] = colLabel;
        columns[1] = colData;
    }

    @Override
    void appendRecord(long index, RecordValue recordValue) {

        final int nColumnCount = recordDef.getFields().size();
        final long iRow = getNumRecords() + index;
        appendRowLabel(iRow);
        for (int i = 0; i < nColumnCount; i++) {
            final String label = recordDef.getFieldName(i);
            final FieldDef def = recordDef.getField(i);
            final FieldValue val = recordValue.get(i);
            colLabel.appendData(label);
            appendValue(colData, def, val);
            finishingRow(Math.max(colLabel.getHeight(), colData.getHeight()));
        }
    }

    private void appendRowLabel(long i) {
        colLabel.appendTitle("\n > Row " + i);
        colData.appendTitle("");
    }

    @Override
    void resetColumns(boolean pagingEnabled) {
        for (Column column : columns) {
            column.reset(false, false);
        }
    }
}
