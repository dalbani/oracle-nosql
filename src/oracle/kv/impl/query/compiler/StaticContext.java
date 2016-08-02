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


import java.util.HashMap;
import java.util.Map;

import oracle.kv.impl.query.QueryException;
import oracle.kv.impl.query.types.ExprType;

/**
 * StaticContext stores various pieces of info needed during compilation.
 * The reason why we use StaticContext, rather than the QueryControlBlock,
 * is that the relevan info may be scoped. So, multiple StaticContext
 * instances may be created during compilation, each defining a nested
 * scope.
 */
public class StaticContext {

    private final StaticContext theParent;

    private HashMap<String, Function> theFunctions;

    private HashMap<String, ExprVar> theVariables;


    StaticContext(StaticContext parent) {
        theParent = parent;
    }

    boolean isRoot() {
        return theParent == null;
    }

    /*
     * VarInfo is used to store info about an external var. This info is
     * collected, for all external vars, after compilation is done and is
     * then stored in the PreparedStatement. The collected info is needed
     * during query execution, and by extracting it from the ExprVars that
     * represent the external vars during compilation and storing it into
     * the PreparedStatement we can throw away the expressions graph and
     * the static context after query compilation is done.
     */
    public static class VarInfo {

        private final int theId;

        private final ExprType theType;

        VarInfo(ExprVar var) {
            theId = var.getId();
            theType = var.getType();
        }

        public int getId() {
            return theId;
        }

        public ExprType getType() {
            return theType;
        }
    }

    void addVariable(ExprVar var) {

        if (theVariables == null) {
            theVariables = new HashMap<String, ExprVar>();
        }

        if (theVariables.put(var.getName(), var) != null) {
            throw new QueryException(
                "Duplicate declaration for variable " + var.getName(),
                var.getLocation());
        }
    }

    ExprVar findVariable(String name) {
        return findVariable(name, false);
    }

    ExprVar findVariable(String name, boolean local) {

        if (local) {
            if (theVariables == null) {
                return null;
            }
            ExprVar var = theVariables.get(name);
            return var;
        }

        ExprVar var = null;
        StaticContext sctx = this;

        while (var == null && sctx != null) {
           if (sctx.theVariables != null) {
               var = sctx.theVariables.get(name);
           }
            sctx = sctx.theParent;
        }

        return var;
    }

    public Map<String, VarInfo> getExternalVars() {

        Map<String, VarInfo> varsMap = new HashMap<String, VarInfo>();

        if (theVariables == null) {
            return varsMap;
        }

        for (Map.Entry<String, ExprVar> entry : theVariables.entrySet()) {
            String varName = entry.getKey();
            ExprVar var = entry.getValue();
            if (var.isExternal()) {
                varsMap.put(varName, new VarInfo(var));
            }
        }

        return varsMap;
    }

    void addFunction(Function func) {

        if (theFunctions == null) {
            theFunctions = new HashMap<String, Function>();
        }

        if (theFunctions.put(func.getName(), func) != null) {
            throw new QueryException(
                "Duplicate declaration for function " + func.getName(), null);
        }
    }

    Function findFunction(String name, int arity) {
        return findFunction(name, arity, false);
    }

    Function findFunction(String name, int arity, boolean local) {

        Function func = null;

        if (local) {
            if (theFunctions == null) {
                return null;
            }

            func = theFunctions.get(name);

        } else {
            StaticContext sctx = this;

            while (func == null && sctx != null) {
                if (sctx.theFunctions != null) {
                    func = sctx.theFunctions.get(name);
                }
                sctx = sctx.theParent;
            }
        }

        if (func != null && !func.isVariadic() && arity != func.getArity()) {
            return null;
        }

        return func;
    }
}
