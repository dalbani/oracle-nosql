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
import oracle.kv.impl.query.shell.output.ResultOutputFactory.ResultOutput;
import oracle.kv.table.RecordValue;
import oracle.kv.util.shell.Shell;

/**
 * Display records in JSON format that can be single line or multiple lines.
 */
public class JSONOutput extends ResultOutput {

    private final boolean pretty;

    JSONOutput(final Shell shell,
               final PrintStream output,
               final StatementResult statementResult,
               final boolean pagingEnabled,
               final int pageHeight) {
        this(shell, output, statementResult, pagingEnabled, pageHeight, false);
    }

    JSONOutput(final Shell shell,
               final PrintStream output,
               final StatementResult statementResult,
               final boolean pagingEnabled,
               final int pageHeight,
               final boolean pretty) {
        super(shell, output, statementResult, pagingEnabled, pageHeight);
        this.pretty = pretty;
    }

    @Override
    long outputRecords(long maxLines, boolean enablePaging) {
        long nRows = 0;
        long nLines = 0;
        final StringBuilder sb = new StringBuilder();
        while (resultIterator.hasNext()) {
            final RecordValue recordValue = resultIterator.next();
            final String jsonString = getJsonString(recordValue, pretty);
            sb.append(jsonString);
            sb.append(Shell.eol);
            if (pretty) {
                nLines += countLines(jsonString) + 1;
            } else {
                nLines++;
            }
            nRows++;
            if (nLines >= maxLines) {
                break;
            }
        }
        output(sb.toString());
        return nRows;
    }

    private String getJsonString(final RecordValue recordValue,
                                 final boolean isPretty) {
        String str = recordValue.toJsonString(isPretty);
        if (isPretty) {
            return str + Shell.eol;
        }
        return str;
    }

    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }
}
