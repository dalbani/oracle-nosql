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

package oracle.kv.impl.api.table.query;

import oracle.kv.impl.api.table.query.TableParser.ArrayOfJsonValuesContext;
import oracle.kv.impl.api.table.query.TableParser.EmptyJsonArrayContext;
import oracle.kv.impl.api.table.query.TableParser.EmptyJsonObjectContext;
import oracle.kv.impl.api.table.query.TableParser.JsonArrayValueContext;
import oracle.kv.impl.api.table.query.TableParser.JsonAtomContext;
import oracle.kv.impl.api.table.query.TableParser.JsonContext;
import oracle.kv.impl.api.table.query.TableParser.JsonObjectContext;
import oracle.kv.impl.api.table.query.TableParser.JsonObjectValueContext;
import oracle.kv.impl.api.table.query.TableParser.JsonPairContext;
import oracle.kv.impl.api.table.query.TableParser.JspairContext;
import oracle.kv.impl.api.table.query.TableParser.JsvalueContext;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

/*
 * The JsonCollector is essentially an identity map from parse tree nodes to
 * strings.  After the tree has been walked, the root of every json tree should
 * be associated in this map with a string that is equivalent to the original
 * JSON fragment.
 */
public class JsonCollector extends ParseTreeProperty<String> {

    public JsonCollector() {
    }

    public void exitJsonAtom(JsonAtomContext ctx) {
        put(ctx, ctx.getText());
    }

    public void exitJsonObjectValue(JsonObjectValueContext ctx) {
        put(ctx, get(ctx.jsobject()));
    }

    public void exitJsonArrayValue(JsonArrayValueContext ctx) {
        put(ctx, get(ctx.jsarray()));
    }

    public void exitJsonPair(JsonPairContext ctx) {
        String tag = ctx.STRING().getText();
        JsvalueContext valuectx = ctx.jsvalue();
        String x = String.format("%s : %s", tag, get(valuectx));
        put(ctx, x);
    }

    public void exitArrayOfJsonValues(ArrayOfJsonValuesContext ctx) {
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (JsvalueContext valuectx : ctx.jsvalue()) {
            s.append(get(valuectx)).append(", ");
        }
        dropFinalComma(s);
        s.append("]");
        put(ctx, s.toString());
    }

    public void exitEmptyJsonArray(EmptyJsonArrayContext ctx) {
        put(ctx, "[]");
    }

    public void exitJsonObject(JsonObjectContext ctx) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (JspairContext pairctx : ctx.jspair()) {
            s.append(get(pairctx)).append(", ");
        }
        dropFinalComma(s);
        s.append("}");
        put(ctx, s.toString());
    }

    public void exitEmptyJsonObject(EmptyJsonObjectContext ctx) {
        put(ctx, "{}");
    }
    
    public void exitJson(JsonContext ctx) {
        put(ctx, get(ctx.getChild(0)));
    }

    /*
     * If the final char of s is ',' or if the final 2 chars are ", " then
     * delete them.
     */
    private static void dropFinalComma(StringBuilder s) {
        final int length = s.length();
        if (length >= 1 && s.charAt(length - 1) == ',') {
            s.delete(s.length() - 1, s.length());
        } else if (length >= 2 && s.charAt(length - 1) == ' '
                   && s.charAt(length - 2) == ',') {
            s.delete(s.length() - 2, s.length());
        }
    }

}
