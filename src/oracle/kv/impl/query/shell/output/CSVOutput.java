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
import oracle.kv.util.shell.Column.Align;
import oracle.kv.util.shell.Shell;

/**
 * Display records in comma separated values format.
 */
public class CSVOutput extends ColumnOutput {

    private final static char COMMA_DELIMITER = ',';

    public CSVOutput(final Shell shell,
                     final PrintStream output,
                     final StatementResult statementResult,
                     final boolean pagingEnabled,
                     final int pageHeight) {
        super(shell, output, statementResult, pagingEnabled, pageHeight,
              new TableFormat(false /* hasLeftRightBorder */,
                              false /* hasHeaderDelimiter */,
                              false /* hasRowSeparator */,
                              COMMA_DELIMITER /* dataDelimiter */));
    }

    @Override
    void initColumns() {
        final int nColumns = recordDef.getFields().size();
        columns = new Column[nColumns];
        for (int i = 0; i < nColumns; i++) {
            final FieldDef fdef = recordDef.getField(i);
            if (fdef.isComplex()) {
                throw new IllegalArgumentException("The type of field \"" +
                    recordDef.getFieldName(i) + "\" in the result set is " +
                    fdef.getType() + " that can not be displayed in csv " +
                    "format.");
            }
            final Column col = new Column(null, Align.UNALIGNED);
            columns[i] = col;
        }
    }

    @Override
    void appendRecord(long rowIndex, RecordValue recordValue) {

        final int nColumns = columns.length;
        for (int i = 0; i < nColumns; i++) {
            final Column col = columns[i];
            final FieldDef def = recordDef.getField(i);
            final FieldValue val = recordValue.get(i);
            final String value = getStringValue(def, val);
            col.appendData(value);
        }
    }

    @Override
    String getStringValue(final FieldDef fdef, final FieldValue fval) {
        String value = super.getStringValue(fdef, fval);
        if (!fval.isNull() && (fdef.isString() || fdef.isEnum())) {
            value = "\"" + value + "\"";
        }
        return value;
    }

    @Override
    String getNullString() {
        return "";
    }
}
