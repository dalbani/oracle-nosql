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

package oracle.kv.impl.query.compiler;

import java.util.ArrayList;

/**
 * There is a single instance of the FunctionLib class, created during static
 * initialization as a static member of the CompilerAPI class. This single
 * instance  creates and stores the Function objs for all builtin functions.
 * It also registers these Function objs in the root static context.
 */
public class FunctionLib {

    /*
     * This enum defines a unique code for each builtin function.
     *
     * WARNING!!
     * The ordinal numbers of the enum are used as indices in theFunctions
     * array, so if you reorder the enum values you MUST also reorder the
     * entries of theFunctions.
     */
    public static enum FuncCode {

        OP_AND,
        OP_OR,

        OP_EQ,
        OP_NEQ,
        OP_GT,
        OP_GE,
        OP_LT,
        OP_LE,

        OP_EQ_ANY,
        OP_NEQ_ANY,
        OP_GT_ANY,
        OP_GE_ANY,
        OP_LT_ANY,
        OP_LE_ANY,

        OP_ADD_SUB,
        OP_MULT_DIV,
        OP_ARITH_UNARY,

        OP_CONCAT,

        FN_SIZE,
        FN_KEYS
    }

    ArrayList<Function> theFunctions;

    FunctionLib(StaticContext sctx) {

        theFunctions = new ArrayList<Function>(20);

        theFunctions.add(new FuncAndOr(FuncCode.OP_AND, "AND"));
        theFunctions.add(new FuncAndOr(FuncCode.OP_OR, "OR"));

        theFunctions.add(new FuncCompOp(FuncCode.OP_EQ, "EQ"));
        theFunctions.add(new FuncCompOp(FuncCode.OP_NEQ, "NEQ"));
        theFunctions.add(new FuncCompOp(FuncCode.OP_GT, "GT"));
        theFunctions.add(new FuncCompOp(FuncCode.OP_GE, "GE"));
        theFunctions.add(new FuncCompOp(FuncCode.OP_LT, "LT"));
        theFunctions.add(new FuncCompOp(FuncCode.OP_LE, "LE"));

        theFunctions.add(new FuncAnyOp(FuncCode.OP_EQ_ANY, "EQ_ANY"));
        theFunctions.add(new FuncAnyOp(FuncCode.OP_NEQ_ANY, "NEQ_ANY"));
        theFunctions.add(new FuncAnyOp(FuncCode.OP_GT_ANY, "GT_ANY"));
        theFunctions.add(new FuncAnyOp(FuncCode.OP_GE_ANY, "GE_ANY"));
        theFunctions.add(new FuncAnyOp(FuncCode.OP_LT_ANY, "LT_ANY"));
        theFunctions.add(new FuncAnyOp(FuncCode.OP_LE_ANY, "LE_ANY"));

        theFunctions.add(new FuncArithOp(FuncCode.OP_ADD_SUB, "+-"));
        theFunctions.add(new FuncArithOp(FuncCode.OP_MULT_DIV, "*/"));
        theFunctions.add(new FuncArithUnaryOp(FuncCode.OP_ARITH_UNARY, "-"));

        theFunctions.add(new FuncConcat());

        theFunctions.add(new FuncSize());
        theFunctions.add(new FuncKeys());

        for (Function func : theFunctions) {
            sctx.addFunction(func);
        }
    }

    Function getFunc(FuncCode c) {
        return theFunctions.get(c.ordinal());
    }
}
