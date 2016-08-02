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

import static java.util.Locale.ENGLISH;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import oracle.kv.table.MapValue;
import oracle.kv.impl.api.table.IndexImpl.AnnotatedField;
import oracle.kv.impl.admin.DdlHandler.DdlOperation;
import oracle.kv.impl.admin.SecurityDdlOperation;
import oracle.kv.impl.admin.TableDdlOperation;
import oracle.kv.impl.api.table.ArrayBuilder;
import oracle.kv.impl.api.table.MapBuilder;
import oracle.kv.impl.api.table.RecordBuilder;
import oracle.kv.impl.api.table.TableBuilder;
import oracle.kv.impl.api.table.TableBuilderBase;
import oracle.kv.impl.api.table.TableEvolver;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.security.util.SecurityUtils;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class works with the grammar generated from Table.g by Antlr V4 to
 * implement a parse tree visitor (extends TableBaseListener) to process
 * the results of parsing a DDL statement.
 *
 * Parsing is done by calling one of the static parse() methods on this class.
 * If the parsing is successful the state of the returned TableDdl instance
 * has enough information to further act on the statement.
 *
 * If there was an error in the parsing itself or in processing of the parse
 * tree the returned TableDdl instance will return false from its
 * succeeded() method.  If that method does not return true there is no useful
 * state in the object other than the DdlException returned by
 * getDdlException().  DdlException itself holds semantic errors.  If there is
 * a syntax error it will generate ParseException, which extends DdlException.
 *
 * Example:
 *  TableDdl ddl = TableDdl.parse("some_statement");
 *  if (ddl.succeeded()) {
 *     do_something_about_success();
 *  } else {
 *     String errorMsg = ddl.getErrorMessage();
 *     DdlException exc = ddl.getDdlException();
 *     do_something_about_failure();
 *  }
 *
 * Antlr parses the entire statement into the parse tree before any of the
 * TableListener methods are called.  This means that the implementation can
 * rely on state that is guaranteed by the successful parse.
 *
 * Implementation Notes:
 *
 * The most complicated statement is the CREATE TABLE statement, which has to
 * track field definitions and options, which can be nested. To do this the
 * implementation maintains several stacks:
 * 1.  current TableBuilderBase.  This is the context for the table itself, and
 * any nested complex fields.  Fields are added to the current builder as they
 * are defined.
 * 2.  current field name.  This is the name of the current field being defined.
 * Some contexts do not require names, in which case the stack of names is
 * not 1:1 with the stack of types being defined.  E.g. map and array.
 * 3.  current field type.  This is the current type being defined, which may,
 * or may not have a name in the name stack.
 *
 * The stacks are handled symmetrically in enter/exit methods for the parser
 * rules that correspond to the stacks.
 *
 * As new fields are defined with names, the names are pushed on the name stack
 * in enterField_def(). These are popped by the corresponding exitField_def().
 *
 * As new types are defined their corresponding Field instances are pushed on
 * the stack of Field instances.  These are type-specific so that other rules
 * can be used to add to their state (e.g. min, max, default values, etc).  This
 * stack is maintained by the enter/exit rules for each type.
 *
 * As complex types are encountered they also push new builder instances for
 * the type (e.g. MapBuilder, ArrayBuilder, RecordBuilder).  This stack is
 * maintained in the enter/exit methods for those types.
 *
 * Use of TableMetadata
 *
 * It would be possible to implement the parser and parse tree analysis without
 * access to TableMetadata.  In fact, the current code does not need metadata
 * for create/drop index and drop table.  The rationale for requiring
 * TableMetadata for some operations is just that it simplifies interactions
 * with TableBuilder and related classes in the case of table evolution and
 * child table creation.  This connection could be changed to have the
 * TableMetadata accessed only by callers.
 *
 * If so, it'd have the following implications: o creation of child tables
 * would need to add, and validate parent primary key information after the
 * fact.  o alter table would have to save its individual modifications for
 * application after the parse
 *
 * This may be desirable, but for now, this class uses TableMetadata directly.
 *
 * Usage warnings:
 * The syntax error messages are currently fairly
 * cryptic. oracle.kv.shell.ExecuteCmd implements a getUsage() method which
 * attempts to augment those messages. This should be moved into TableDdl.
 */
public class TableDdl extends TableBaseListener {

    private final TableMetadata metadata;
    private TableImpl table;
    private DdlException ddlException;
    private String errorMessage;

    /*
     * A stack of builder objects, starting with the initial TableBuilder.
     * Complex types (array, map, record) push and pop their own builders on
     * the stack.  This stack is used for table creation and evolution (alter).
     */
    private final Stack<TableBuilderBase> builders =
        new Stack<TableBuilderBase>();

    /*
     * A stack of Field names for the current field being defined.  Some
     * fields do not have names, such as the field type being defined for
     * a map or array.  This is why the stack of field names and stack of
     * fields are not 1:1.
     */
    private final Stack<String> fieldNames = new Stack<String>();

    /*
     * A stack of Field objects which hold information for the current field
     * being defined.  Complex types cause this stack to get deeper than the
     * normal single level.
     */
    private final Stack<Field> fields = new Stack<Field>();

    /*
     * A helper class for handling JSON fragments.
     */
    private final JsonCollector jsonCollector = new JsonCollector();

    /*
     * Set to true if "IF NOT EXISTS" was specified for table or index creation.
     */
    private boolean ifNotExists;

    /*
     * Set to true if "IF EXISTS" was specified for table or index drop.
     */
    private boolean ifExists;

    /**
     * Will be true for a drop table, a drop index, a drop user or a drop role
     * statement.
     */
    private boolean isDrop;

    /**
     * Will be true for an alter table statement.
     */
    private boolean isEvolve;

    /**
     * Will be true for a describe statement.
     */
    private boolean isDescribe;

    /**
     * Will be true for a show statement.
     */
    private boolean isShow;

    /* show/describe as JSON */
    private boolean describeAsJson;
    /* show indexes */
    private boolean showIndexes;
    /* show tables */
    private boolean showTables;

    /**
     * Will be non-null for a table drop or index add/drop statement.
     */
    private String tableName;

    /**
     * Will be non-null for index add or drop statement.
     */
    private String indexName;

    /**
     * Will be non-null if there is a COMMENT on CREATE INDEX
     */
    private String indexComment;

    /**
     * An array of field names shared by index creation and describe.
     *
     * Will be non-null for create index statement and possibly for describe.
     */
    private String[] fieldArray;

    /*
     * An array of field specs for full text index fields.
     * Will be non-null for create text index statement.
     * TBD: maybe fieldArray should be subsumed into this.
     */
    private AnnotatedField[] ftsFieldArray;

    /**
     * A set of names generated internally for use in otherwise unnamed
     * maps and arrays that need them (for Avro schema generation).
     * This set guarantees uniqueness, which is also required by Avro.
     */
    private HashSet<String> generatedNames = new HashSet<String>();

    private DdlOperation ddlOperation;

    private final static String KEYOF_TAG = TableImpl.KEY_TAG;
    private final static String ELEMENTOF_TAG = MapValue.ANONYMOUS;
    private final static String ALL_PRIVS = "ALL";

    enum DDLTimeUnit {
        S() {
            @Override
            TimeUnit getUnit() {
                return TimeUnit.SECONDS;
            }
        },

        M() {
            @Override
            TimeUnit getUnit() {
                return TimeUnit.MINUTES;
            }
        },

        H() {
            @Override
            TimeUnit getUnit() {
                return TimeUnit.HOURS;
            }
        },

        D() {
            @Override
            TimeUnit getUnit() {
                return TimeUnit.DAYS;
            }
        };
        abstract TimeUnit getUnit();
    }

    /*
     * The body of the class.
     */

    TableDdl() {
        metadata = null;
    }

    /**
     * This is a constructor that simply holds a parse exception that occurred
     * during the parsing operation, before the TableDdl was even created.
     */
    TableDdl(DdlException de) {
        metadata = null;
        setDdlException(de);
    }

    /**
     * This constructor will be used most of the time since many operations
     * require existing metadata.
     */
    TableDdl(TableMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns a table if the parsed statement resulted in a table, otherwise
     * null.
     */
    public TableImpl getTable() {
        return table;
    }

    /**
     * Returns the DdlException if an exception occurred.
     */
    public DdlException getDdlException() {
        return ddlException;
    }

    public void setDdlException(DdlException de) {
        ddlException = de;
        errorMessage = de.getMessage();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean succeeded() {
        return ddlException == null && errorMessage == null;
    }

    /*
     * TODO:
     * Since we directly generate a DdlOperation now, the following
     * flag methods are not used anymore. May be pruned in future.
     */

    /**
     * Returns true if this is a table create
     */
    public boolean isTableCreate() {
        return !isEvolve && table != null && !(isDescribe || isShow);
    }

    /**
     * Returns true if this is a table evolve
     */
    public boolean isTableEvolve() {
        return isEvolve && table != null;
    }

    /**
     * Returns true if this is an index add statement.
     */
    public boolean isIndexAdd() {
        return !isDrop && !isDescribe && !isShow &&
            tableName != null && indexName != null && fieldArray != null;
    }
    
    /**
     * Returns true if this is a text index add statement.
     */
    public boolean isTextIndexAdd() {
        return !isDescribe && ftsFieldArray != null;
    }

    /**
     * Returns true if this is a table drop statement.
     */
    public boolean isTableDrop() {
        return isDrop && tableName != null && indexName == null;
    }

    /**
     * Returns true if this is an index drop statement.
     */
    public boolean isIndexDrop() {
        return isDrop && tableName != null && indexName != null;
    }

    /**
     * Returns true if this is a describe statement.
     */
    public boolean isDescribe() {
        return isDescribe;
    }

    /**
     * Returns true if this is a show statement.
     */
    public boolean isShow() {
        return isShow;
    }

    /**
     * Returns true if this is a describe or show statement with AS JSON
     * specified.
     */
    public boolean isDescribeAsJson() {
        return describeAsJson;
    }

    /**
     * Returns true if this is a show statement with INDEXES specified.
     */
    public boolean isShowIndexes() {
        return showIndexes;
    }

    /**
     * Returns true if this is a show statement with TABLES
     */
    public boolean isShowTables() {
        return showTables;
    }

    /**
     * Returns the value of ifExists for a table or index drop.
     */
    public boolean getIfExists() {
        return ifExists;
    }

    /**
     * Returns the value of ifNotExists for a table or index create.
     */
    public boolean getIfNotExists() {
        return ifNotExists;
    }

    /**
     * Returns whether to remove data as part of a drop table operation.
     * Unconditionally yes at this time.
     */
    public boolean getRemoveData() {
        return true;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getIndexComment() {
        return indexComment;
    }

    public String[] getFieldArray() {
        return fieldArray;
    }

    public AnnotatedField[] getFtsFieldArray() {
    	return ftsFieldArray;
    }
    /**
     * Parse input from stdin, ending with EOF (^D)
     */
    public static TableDdl parse()
        throws DdlException {

        return parse(System.in, null);
    }

    /**
     * Parse String input.
     */
    public static TableDdl parse(String input, TableMetadata meta)
        throws DdlException {

        ANTLRInputStream antlrStream =
            new ANTLRInputStream(input.toCharArray(), input.length());
        return parse(antlrStream, meta);
    }

    /**
     * Parses from an InputStream, ending in EOF (^D)
     */
    public static TableDdl parse(InputStream input, TableMetadata meta)
        throws DdlException {

        try {
            ANTLRInputStream antlrStream = new ANTLRInputStream(input);
            return parse(antlrStream, meta);
        } catch (IOException ioe) {
            throw new DdlException(ioe);
        }
    }

    /**
     * Drive the parsing.
     * TODO: look at using the BailErrorStrategy and setSLL(true) to do faster
     * parsing with a bailout.
     */
    private static TableDdl parse(ANTLRInputStream input,
                                  TableMetadata meta)
        throws DdlException {

        /* creates a buffer of tokens pulled from the lexer */
        TableLexer lexer = new TableLexer(input);

        /* create a parser that feeds off the tokens buffer */
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TableParser parser = new TableParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new DdlErrorListener());

        /*
         * Begins parsing at parse rule
         */
        ParseTree tree = null;
        try {
            tree = parser.parse();
        } catch (RecognitionException re) {
            return new TableDdl(new DdlException(re));
        } catch (DdlException de) {
            return new TableDdl(de);
        }

        ParseTreeWalker walker = new ParseTreeWalker();
        TableDdl ddl = new TableDdl(meta);

        try {
            /*
             * Walks the parse tree, acting on the rules encountered.
             */
            walker.walk(ddl, tree);
        } catch (DdlException de) {
            ddl.setDdlException(de);
        }
        return ddl;
    }

    public DdlOperation getDdlOperation() {
        return ddlOperation;
    }

    /*
     * Implementation of the parser.
     */

    @Override
    public void enterCreate_table_statement
        (@NotNull TableParser.Create_table_statementContext ctx) {

        /* only get the last component of the table path */
        String name = getNamePath(ctx.name_path(), false);
        TableImpl parentTable = getParentTable(ctx.name_path());
        if (ctx.IF_NOT_EXISTS() != null) {
            ifNotExists = true;
        }

        TableParser.Table_defContext table_def = ctx.table_def();

        /*
         * Do validation of single comment here.  The comment itself is added
         * in enterComment.
         */
        if (table_def.comment() != null &&
            table_def.comment().size() > 1) {
            throw new ParseException
                ("Table definition may contain only one comment");
        }

        /*
         * Validate the number of primary keys.
         */
        if (table_def.key_def() == null ||
            table_def.key_def().isEmpty() ||
            table_def.key_def().size() > 1) {
            throw new ParseException
                ("Table definition must contain a single primary " +
                 "key definition");
        }
        builders.push(TableBuilder.createTableBuilder(name, null, parentTable));
    }

    @Override
    public void exitCreate_table_statement
        (@NotNull TableParser.Create_table_statementContext ctx) {
        TableBuilder tbb = (TableBuilder) builders.pop();
        assert tbb != null && builders.empty();
        try {

            /*
             * The only (known) exception thrown from this build is failure to
             * create an Avro schema for the table, which should not be possible
             * if the parsing is correct, but catch it just in case.
             */
            table = tbb.buildTable();
        } catch (Exception e) {
            throw new DdlException
                ("Invalid table state: " + e.getMessage());
        }
        ddlOperation = new TableDdlOperation.CreateTable(table, ifNotExists);
    }

    @Override
    public void enterAlter_table_statement
        (@NotNull TableParser.Alter_table_statementContext ctx) {
        String name = getNamePath(ctx.name_path());
        TableImpl currentTable = getTable(name);
        if (currentTable == null) {
            noTable(name);
        }
        builders.push(TableEvolver.createTableEvolver(currentTable));
        isEvolve = true;
    }

    @Override
    public void exitAlter_table_statement
        (@NotNull TableParser.Alter_table_statementContext ctx) {
        TableEvolver evolver = (TableEvolver) builders.pop();
        table = evolver.evolveTable();
        ddlOperation = new TableDdlOperation.EvolveTable(table);
    }

    /**
     * This is the Primary Key definition, which includes optional
     * specification of the shard key, which makes this a bit more complex
     * than a simple list of ids.
     */
    @Override
    public void enterKey_def
        (@NotNull TableParser.Key_defContext ctx) {

        try {

            /*
             * If it is a simple id list then there is no shard key
             * specified.
             */
            if (ctx.shard_key_def() == null) {
                /*
                 * Handle empty primary key (primary key()).
                 */
                if (ctx.id_list() == null) {
                    throw new ParseException
                        ("PRIMARY KEY must contain a list of fields");
                }

                builders.peek().primaryKey
                    (makeIdArray(ctx.id_list().extended_id()));
                return;
            }

            /*
             * There must be a shard key specified.  Create a list from that,
             * then add the additional primary key fields.
             */
            List<TableParser.Extended_idContext> shardKeyList =
                ctx.shard_key_def().simple_field_list().
                id_list_with_paren().id_list().extended_id();
            builders.peek().shardKey(makeIdArray(shardKeyList));
            List<TableParser.Extended_idContext> pkey =
                new ArrayList<TableParser.Extended_idContext>(shardKeyList);

            /*
             * Handle case where primary key == shard key and the user
             * specified shard(), even though it is redundant.  It's allowed,
             * just not needed.  E.g. create table foo (id integer, primary
             * key (shard(id))).
             */
            if (ctx.id_list() != null) {
                pkey.addAll(ctx.id_list().extended_id());
            }
            builders.peek().primaryKey(makeIdArray(pkey));
        } catch (IllegalArgumentException iae) {
            throw new DdlException(iae.getMessage());
        }
    }

    @Override
    public void enterField_def(@NotNull TableParser.Field_defContext ctx) {
        String name = getNamePath(ctx.name_path());
        fieldNames.push(name);
    }

    @Override
    public void exitField_def(@NotNull TableParser.Field_defContext ctx) {
        fieldNames.pop();
    }

    @Override
    public void enterNot_null(@NotNull TableParser.Not_nullContext ctx) {
        Field field = fields.peek();
        field.setNotNullable();
    }

    /**
     * If this context is inside a field definition, add the comment.  If it
     * is not, add it to the current builder context.
     */
    @Override
    public void enterComment(@NotNull TableParser.CommentContext ctx) {
        if (!fields.isEmpty()) {
            fields.peek().setComment(stripFirstLast(ctx.STRING().getText()));
        } else if (!builders.isEmpty()) {
            builders.peek().setDescription
                (stripFirstLast(ctx.STRING().getText()));
        }
    }

    /*
     * The complex types (array, map, record) do not push onto the fields stack,
     * but instead push onto the builder stack.
     */
    @Override
    public void enterArray(@NotNull TableParser.ArrayContext ctx) {
        builders.push(TableBuilder.createArrayBuilder());
    }

    @Override
    public void exitArray(@NotNull TableParser.ArrayContext ctx) {
        ArrayBuilder arrayBuilder = (ArrayBuilder) builders.pop();
        TableBuilderBase builder = builders.peek();
        String name =
            (builder.isCollectionBuilder() ? null : fieldNames.peek());
        builder.addField(name, arrayBuilder.build());
    }

    @Override
    public void enterMap(@NotNull TableParser.MapContext ctx) {
        builders.push(TableBuilder.createMapBuilder());
    }

    @Override
    public void exitMap(@NotNull TableParser.MapContext ctx) {
        MapBuilder mapBuilder = (MapBuilder) builders.pop();
        TableBuilderBase builder = builders.peek();
        String name =
            (builder.isCollectionBuilder() ? null : fieldNames.peek());
        builder.addField(name, mapBuilder.build());
    }

    @Override
    public void enterRecord(@NotNull TableParser.RecordContext ctx) {
        /*
         * Records require a name in Avro. If this record is an element of a
         * map or array it won't have a name, so a unique name must be
         * generated.
         */
        TableBuilderBase builder = builders.peek();
        String name =
            (builder.isCollectionBuilder() ?
             generateFieldNameInternal("RECORD") : fieldNames.peek());
        builders.push(TableBuilder.createRecordBuilder(name));
    }

    @Override
    public void exitRecord(@NotNull TableParser.RecordContext ctx) {
        RecordBuilder recordBuilder = (RecordBuilder) builders.pop();
        TableBuilderBase builder = builders.peek();
        String name =
            (builder.isCollectionBuilder() ? null : fieldNames.peek());
        builder.addField(name, recordBuilder.build());
    }

    @Override
    public void enterString(@NotNull TableParser.StringContext ctx) {
        fields.push(new StringField(getFieldName(ctx)));
    }

    @Override
    public void exitString(@NotNull TableParser.StringContext ctx) {
        Field field = fields.pop();
        addToBuilder(field);
    }

    @Override
    public void enterString_default
        (@NotNull TableParser.String_defaultContext ctx) {
        Field field = fields.peek();
        String defaultString = stripFirstLast(ctx.STRING().getText());
        field.setDefault(defaultString);
    }


    /**
     * Expressions are generic and are currently only range operations.
     * Type checking of values is done in the Field methods called.
     */
    @Override
    public void enterExpr(@NotNull TableParser.ExprContext ctx) {
        Field field = fields.peek();
        String value = null;

        /*
         * Validate the identifier against the current field.
         */
        if (ctx.extended_id() != null) {
            final String name = ctx.extended_id().getText();
            field.validateExpressionId(name);
        } else if (!ctx.elementof_expr().name_path().isEmpty()) {
            /*
             * allow an empty elementof() expression to get by, but if the
             * name is specified it must match the enclosing map or array
             * builder's name, which is in the fieldNames stack.
             */
            final String name = getNamePath(ctx.elementof_expr().name_path(0));
            TableBuilderBase builder = builders.peek();
            if (!(builder instanceof ArrayBuilder) &&
                !(builder instanceof MapBuilder)) {
                throw new ParseException
                    ("elementof() is only valid inside of an array or map "
                     + "definition");
            }
            if (!name.equalsIgnoreCase(fieldNames.peek())) {
                throw new ParseException
                    ("Invalid identifer in elementof() expression. " +
                     "Expected " + fieldNames.peek() + ", found " + name);
            }
        }

        /*
         * Do some validation
         */
        if (ctx.STRING() != null) {
            if (!(field instanceof StringField)) {
                throw new ParseException
                    ("Type cannot accept quoted strings in expressions: " +
                     field.getType());
            }
            value = stripFirstLast(ctx.STRING().getText());
        } else if (ctx.FLOAT() != null) {
            value = ctx.FLOAT().getText();
        } else if (ctx.INT() != null) {
            value = ctx.INT().getText();
        }
        /* the parser/lexer ensures that one of the above is non-null */
        assert value != null;

        String op = ctx.OP().getText();

        /*
         * The numeric conversions can throw NumberFormatException, catch and
         * rethrow as a parse exception.
         */
        try {
            if (op.equals(">") || op.equals(">=")) {
                field.setMin(value, op.equals(">="));
            } else if (op.equals("<") || op.equals("<=")) {
                field.setMax(value, op.equals("<="));
            } else {
                throw new IllegalStateException
                    ("Unexpected operation: " + op);
            }
        } catch (NumberFormatException nfe) {
            throw new ParseException
                ("Invalid numeric value for type " + field.getType() + ": " +
                 value);
        }
    }

    @Override
    public void enterInt(@NotNull TableParser.IntContext ctx) {
        boolean isLong = ctx.integer_def().LONG_T() != null;
        if (isLong) {
            fields.push(new LongField(getFieldName(ctx)));
        } else {
            fields.push(new IntField(getFieldName(ctx)));
        }
    }

    @Override
    public void exitInt(@NotNull TableParser.IntContext ctx) {

        Field field = fields.pop();
        addToBuilder(field);
    }

    @Override
    public void enterInteger_default
        (@NotNull TableParser.Integer_defaultContext ctx) {
        Field field = fields.peek();
        String defaultString = ctx.INT().getText();
        field.setDefault(defaultString);
    }

    @Override
    public void enterBinary(@NotNull TableParser.BinaryContext ctx) {

        int size = 0;
        if (ctx.binary_def().INT() != null) {
            size = Integer.parseInt(ctx.binary_def().INT().getText());
        }
        fields.push(new BinaryField(getFieldName(ctx), size));
    }

    @Override
    public void exitBinary(@NotNull TableParser.BinaryContext ctx) {
        Field field = fields.pop();
        addToBuilder(field);
    }

    @Override
    public void enterFloat(@NotNull TableParser.FloatContext ctx) {

        boolean isDouble = ctx.float_def().DOUBLE_T() != null;
        if (isDouble) {
            fields.push(new DoubleField(getFieldName(ctx)));
        } else {
            fields.push(new FloatField(getFieldName(ctx)));
        }
    }

    @Override
    public void exitFloat(@NotNull TableParser.FloatContext ctx) {

        Field field = fields.pop();
        addToBuilder(field);
    }

    @Override
    public void enterFloat_default
        (@NotNull TableParser.Float_defaultContext ctx) {
        Field field = fields.peek();
        TerminalNode node = (ctx.FLOAT() != null ? ctx.FLOAT() : ctx.INT());
        String defaultString = node.getText();
        field.setDefault(defaultString);
    }

    @Override
    public void enterBoolean(@NotNull TableParser.BooleanContext ctx) {
        fields.push(new BooleanField(getFieldName(ctx)));
    }

    @Override
    public void exitBoolean(@NotNull TableParser.BooleanContext ctx) {
        Field field = fields.pop();
        addToBuilder(field);
    }

    /**
     * The only constraint handled here is the default value.  Not null is
     * handled in common code.
     */
    @Override
    public void enterBoolean_constraint
        (@NotNull TableParser.Boolean_constraintContext ctx) {
        Field field = fields.peek();
        if (ctx.DEFAULT() != null) {
            String val = ctx.BOOLEAN_VALUE().getText();
            field.setDefault(val);
        }
    }

    @Override
    public void enterEnum(@NotNull TableParser.EnumContext ctx) {
        String[] values =
            makeIdArray(ctx.enum_def().id_list_with_paren().id_list().extended_id());
        fields.push(new EnumField(getFieldName(ctx), values));
    }

    /**
     * The only constraint handled here is the default value.  Not null is
     * handled in common code.
     */
    @Override
    public void enterEnum_constraint
        (@NotNull TableParser.Enum_constraintContext ctx) {
        Field field = fields.peek();
        if (ctx.extended_id() != null) {
            field.setDefault(ctx.extended_id().getText());
        }
    }

    @Override
    public void exitEnum(@NotNull TableParser.EnumContext ctx) {
        Field field = fields.pop();
        addToBuilder(field);
    }

    /*
     * Methods specific to alter table.
     */
    @Override
    public void enterDrop_field_statement
        (@NotNull TableParser.Drop_field_statementContext ctx) {
        TableBuilderBase builder = builders.peek();
        String name = getNamePath(ctx.name_path());
        try {
            builder.removeField(name);
        } catch (IllegalArgumentException iae) {
            throw new DdlException(iae.getMessage(), iae);
        }
    }

    /**
     * In the current TableBuilder/TableEvolver model a new field cannot be
     * added over top of an existing field, so remove the field first so the
     * add later works.  The actual modification is validated in
     * TableImpl.evolve().
     */
    @Override
    public void enterModify_field_statement
        (@NotNull TableParser.Modify_field_statementContext ctx) {

        throw new DdlException("MODIFY is not supported at this time");
    }

    /*
     * Index create/drop, table drop.  These are relatively simple, just setting
     * state.
     */
    @Override
    public void enterCreate_index_statement
        (@NotNull TableParser.Create_index_statementContext ctx) {
        if (ctx.IF_NOT_EXISTS() != null) {
            ifNotExists = true;
        }
        tableName = getNamePath(ctx.name_path());
        indexName = ctx.index_name().extended_id().getText();
        fieldArray = makeNameArray(ctx.complex_field_list().path_list()
                                   .complex_name_path());

        if (ctx.comment() != null) {
            indexComment = stripFirstLast(ctx.comment().STRING().getText());
        }
        table = getTable(tableName);
        ddlOperation = new TableDdlOperation.CreateIndex(
            table, tableName, indexName, fieldArray, indexComment, ifNotExists);
    }

    @Override
    public void enterDrop_index_statement
        (@NotNull TableParser.Drop_index_statementContext ctx) {
        isDrop = true;
        if (ctx.IF_EXISTS() != null) {
            ifExists = true;
        }
        tableName = getNamePath(ctx.name_path());
        indexName = ctx.index_name().extended_id().getText();
        table = getTableSilently(tableName);
        ddlOperation = new TableDdlOperation.DropIndex(
            tableName, table, indexName, ifExists);
    }

    @Override
    public void enterDrop_table_statement
        (@NotNull TableParser.Drop_table_statementContext ctx) {
        isDrop = true;
        if (ctx.IF_EXISTS() != null) {
            ifExists = true;
        }
        tableName = getNamePath(ctx.name_path());
        table = getTableSilently(tableName);
        ddlOperation = new TableDdlOperation.DropTable(tableName, table,
                                                       ifExists,
                                                       getRemoveData());
    }

    @Override
    public void exitCreate_text_index_statement
        (@NotNull TableParser.Create_text_index_statementContext ctx) {
        if (ctx.IF_NOT_EXISTS() != null) {
            ifNotExists = true;
        }
        tableName = getNamePath(ctx.name_path());
        indexName = ctx.index_name().extended_id().getText();
        ftsFieldArray = makeFtsFieldArray(ctx.fts_field_list().fts_path_list()
                                          .fts_path());

        if (ctx.comment() != null) {
            indexComment = stripFirstLast(ctx.comment().STRING().getText());
        }
        table = getTable(tableName);
        ddlOperation = new TableDdlOperation.CreateTextIndex(
            table, tableName, indexName, ftsFieldArray, indexComment, ifNotExists);
    }

    @Override
    public void enterDescribe_statement
        (@NotNull TableParser.Describe_statementContext ctx) {
        if (ctx.name_path() != null) {
            tableName = getNamePath(ctx.name_path());
            if (getTable(tableName) == null) {
                noTable(tableName);
            }
            if (ctx.complex_field_list() != null) {
                fieldArray = makeNameArray(ctx.complex_field_list().path_list()
                                           .complex_name_path());
            }
            if (ctx.index_name() != null) {
                indexName = ctx.index_name().extended_id().getText();
            }
        }
        isDescribe = true;
        describeAsJson = (ctx.AS_JSON() != null);
        ddlOperation = new TableDdlOperation.DescribeTable(
            tableName, indexName, fieldArray, describeAsJson);
    }

    /**
     * Very similar to DESCRIBE, with other options
     * show_statment: SHOW AS_JSON?
     *      (TABLES |
     *      ROLES |
     *      USERS |
     *      ROLE role_name |
     *      USER user_name |
     *      INDEXES ON table_name |
     *      TABLE table_name) ;
     */
    @Override
    public void enterShow_statement
        (@NotNull TableParser.Show_statementContext ctx) {

        /* Try to identify as a Show User or Show Role operation */
        ddlOperation = getShowUserOrRoleOp(ctx);
        if (ddlOperation != null) {
            isShow = true;
            return;
        }

        /* Try to identify as a Show Table or Show Index operation */
        if (ctx.name_path() != null) {
            tableName = getNamePath(ctx.name_path());
            if (getTable(tableName) == null) {
                noTable(tableName);
            }
            if (ctx.INDEXES() != null) {
                showIndexes = true;
            }
        } else {
            /*
             * The grammar does not allow table name and TABLES in the same
             * statement.
             */
            assert ctx.TABLES() != null;
            showTables = true;
        }
        isShow = true;
        describeAsJson = (ctx.AS_JSON() != null);
        ddlOperation =
            new TableDdlOperation.ShowTableOrIndex(tableName,
                                                   showTables,
                                                   showIndexes,
                                                   describeAsJson);
    }

    /*
     * For security related commands
     */

    @Override
    public void exitCreate_user_statement(
        @NotNull TableParser.Create_user_statementContext ctx) {

        final String userName =
            getIdentifierName(ctx.create_user_identified_clause(), "user");
        final boolean isExternal =
            ctx.create_user_identified_clause().IDENTIFIED_EXTERNALLY() !=
                null ? true : false;
        final boolean isAdmin = (ctx.ADMIN() != null);
        final boolean passExpired =
           (ctx.create_user_identified_clause().PASSWORD_EXPIRE() != null);

        final boolean isEnabled =
            ctx.account_lock() != null ?
            !isAccountLocked(ctx.account_lock()) :
            true;

        if (!isExternal) {
            Long pwdLifetimeInMillis =
                ctx.create_user_identified_clause().
                    password_lifetime() == null ? null : resolvePassLifeTime(
                        ctx.create_user_identified_clause().
                            password_lifetime());

            final char[] plainPass =
                resolvePlainPassword(
                    ctx.create_user_identified_clause().identified_clause());

            if (passExpired) {
                pwdLifetimeInMillis = -1L;
            }
            ddlOperation =
                new SecurityDdlOperation.CreateUser(userName,
                                                    isEnabled,
                                                    isAdmin,
                                                    plainPass,
                                                    pwdLifetimeInMillis);
            SecurityUtils.clearPassword(plainPass);
        } else {
            ddlOperation =
                new SecurityDdlOperation.CreateExternalUser(userName,
                                                            isEnabled,
                                                            isAdmin);
        }
    }

    @Override
    public void exitCreate_role_statement(
        @NotNull TableParser.Create_role_statementContext ctx) {
        final String roleName = getIdentifierName(ctx.identifier(), "role");
        ddlOperation = new SecurityDdlOperation.CreateRole(roleName);
    }

    @Override
    public void exitAlter_user_statement(
        @NotNull TableParser.Alter_user_statementContext ctx) {

        final String userName =
            getIdentifierName(ctx.identifier_or_string(), "user");
        boolean retainPassword = false;
        char[] newPass = null;

        final TableParser.Reset_password_clauseContext resetPassCtx =
            ctx.reset_password_clause();
        if (resetPassCtx != null) {
            newPass = resolvePlainPassword(
                resetPassCtx.identified_clause());
            retainPassword =
                (resetPassCtx.RETAIN_CURRENT_PASSWORD() != null);
        }

        final boolean clearRetainedPassword =
            (ctx.CLEAR_RETAINED_PASSWORD() != null);
        final boolean passwordExpire = (ctx.PASSWORD_EXPIRE() != null);

        Long pwdLifetimeInMillis =
            ctx.password_lifetime() == null ?
            null :
            resolvePassLifeTime(ctx.password_lifetime());

        final Boolean isEnabled =
            ctx.account_lock() != null ?
            !isAccountLocked(ctx.account_lock()) :
            null;

        if (passwordExpire) {
            pwdLifetimeInMillis = -1L;
        }
        ddlOperation = new SecurityDdlOperation.AlterUser(
            userName, isEnabled, newPass, retainPassword,
            clearRetainedPassword, pwdLifetimeInMillis);

        SecurityUtils.clearPassword(newPass);
    }

    @Override
    public void exitDrop_user_statement(
        @NotNull TableParser.Drop_user_statementContext ctx) {

        final String userName =
            getIdentifierName(ctx.identifier_or_string(), "user");
        ddlOperation = new SecurityDdlOperation.DropUser(userName,
                                                         false /* cascade */);
    }

    @Override
    public void exitDrop_role_statement(
        @NotNull TableParser.Drop_role_statementContext ctx) {

        final String roleName = getIdentifierName(ctx.identifier(), "role");
        ddlOperation = new SecurityDdlOperation.DropRole(roleName);
    }

    @Override
    public void exitGrant_statement(
        @NotNull TableParser.Grant_statementContext ctx) {

        final Set<String> privSet = new HashSet<String>();
        final List<TableParser.Priv_itemContext> privItemList;
        final String roleName;

        /* The GRANT roles TO user/role case */
        if (ctx.grant_roles() != null) {
            String[] roleNames =
                makeIdArray(ctx.grant_roles().id_list().extended_id());
            final String grantee;
            if (ctx.grant_roles().principal().USER() != null) {
                assert (ctx.grant_roles().principal().ROLE() == null);
                grantee = getIdentifierName(
                    ctx.grant_roles().principal().identifier_or_string(),
                    "user");
                ddlOperation =
                    new SecurityDdlOperation.GrantRoles(grantee, roleNames);
            } else {
                grantee = getIdentifierName(
                    ctx.grant_roles().principal().identifier(), "role");
                ddlOperation = new SecurityDdlOperation.
                    GrantRolesToRole(grantee, roleNames);
            }
            return;
        }

        /* The GRANT system_privilegs TO role case */
        if (ctx.grant_system_privileges() != null) {
            privItemList =
                ctx.grant_system_privileges().sys_priv_list().priv_item();
            getPrivSet(privItemList, privSet);

            roleName = getIdentifierName(
                ctx.grant_system_privileges().identifier(), "role");
            ddlOperation = new SecurityDdlOperation.GrantPrivileges(
                roleName, null /* tableName */, privSet);
            return;
        }

        /* The GRANT object_privilege ON object TO role case */
        if (ctx.grant_object_privileges() != null) {
            if (!ctx.grant_object_privileges().obj_priv_list().
                    ALL().isEmpty()) {
                privSet.add(ALL_PRIVS);
            } else {
                privItemList =
                    ctx.grant_object_privileges().obj_priv_list().priv_item();
                getPrivSet(privItemList, privSet);
            }
            roleName = getIdentifierName(
                ctx.grant_object_privileges().identifier(), "role");
            final String onTable = getNamePath(
                ctx.grant_object_privileges().object().name_path());
            ddlOperation = new SecurityDdlOperation.GrantPrivileges(
                roleName, onTable, privSet);
        }
    }

    @Override
    public void exitRevoke_statement(
        @NotNull TableParser.Revoke_statementContext ctx) {

        final Set<String> privSet = new HashSet<String>();
        final List<TableParser.Priv_itemContext> privItemList;
        final String roleName;

        /* The REVOKE roles FROM user/role case */
        if (ctx.revoke_roles() != null) {
            String[] roleNames =
                makeIdArray(ctx.revoke_roles().id_list().extended_id());
            final String revokee;
            if (ctx.revoke_roles().principal().USER() != null) {
                assert (ctx.revoke_roles().principal().ROLE() == null);
                revokee = getIdentifierName(
                    ctx.revoke_roles().principal().identifier_or_string(),
                    "user");
                ddlOperation =
                    new SecurityDdlOperation.RevokeRoles(revokee, roleNames);
            } else {
                revokee = getIdentifierName(
                    ctx.revoke_roles().principal().identifier(), "role");
                ddlOperation = new SecurityDdlOperation.
                    RevokeRolesFromRole(revokee, roleNames);
            }
            return;
        }

        /* The REVOKE system_privilegs FROM role case */
        if (ctx.revoke_system_privileges() != null) {
            privItemList =
                ctx.revoke_system_privileges().sys_priv_list().priv_item();
            getPrivSet(privItemList, privSet);

            roleName = getIdentifierName(
                ctx.revoke_system_privileges().identifier(), "role");
            ddlOperation = new SecurityDdlOperation.RevokePrivileges(
                roleName, null /* tableName */, privSet);
            return;
        }

        /* The REVOKE object_privilege ON object FROM role case */
        if (ctx.revoke_object_privileges() != null) {
            if (!ctx.revoke_object_privileges().obj_priv_list().
                    ALL().isEmpty()) {
                privSet.add(ALL_PRIVS);
            } else {
                privItemList =
                    ctx.revoke_object_privileges().obj_priv_list().priv_item();
                getPrivSet(privItemList, privSet);
            }
            roleName = getIdentifierName(
                ctx.revoke_object_privileges().identifier(), "role");
            final String onTable = getNamePath(
                ctx.revoke_object_privileges().object().name_path());
            ddlOperation = new SecurityDdlOperation.RevokePrivileges(
                roleName, onTable, privSet);
        }
    }

    /* Callbacks for embedded JSON parsing. */
    @Override
    public void exitJsonAtom(TableParser.JsonAtomContext ctx) {
        jsonCollector.exitJsonAtom(ctx);
    }

    @Override
    public void exitJsonArrayValue(TableParser.JsonArrayValueContext ctx) {
        jsonCollector.exitJsonArrayValue(ctx);
    }

    @Override
    public void exitJsonObjectValue(TableParser.JsonObjectValueContext ctx) {
        jsonCollector.exitJsonObjectValue(ctx);
    }

    @Override
    public void exitJsonPair(TableParser.JsonPairContext ctx) {
        jsonCollector.exitJsonPair(ctx);
    }

    @Override
    public void exitArrayOfJsonValues
        (TableParser.ArrayOfJsonValuesContext ctx) {

        jsonCollector.exitArrayOfJsonValues(ctx);
    }

    @Override
    public void exitEmptyJsonArray(TableParser.EmptyJsonArrayContext ctx) {
        jsonCollector.exitEmptyJsonArray(ctx);
    }

    @Override
    public void exitJsonObject(TableParser.JsonObjectContext ctx) {
        jsonCollector.exitJsonObject(ctx);
    }

    @Override
    public void exitEmptyJsonObject(TableParser.EmptyJsonObjectContext ctx) {
        jsonCollector.exitEmptyJsonObject(ctx);
    }

    @Override
    public void exitJson(TableParser.JsonContext ctx) {
        jsonCollector.exitJson(ctx);
    }

    /*
     * Internal functions and classes
     */

    private static SecurityDdlOperation
        getShowUserOrRoleOp(TableParser.Show_statementContext ctx) {
        final boolean asJson = (ctx.AS_JSON() != null);
        if (ctx.identifier_or_string() != null && ctx.USER() != null) {
            final String name =
                getIdentifierName(ctx.identifier_or_string(), "user");
            return new SecurityDdlOperation.ShowUser(name, asJson);
        }
        if (ctx.identifier() != null && ctx.ROLE() != null) {
            final String name =
                getIdentifierName(ctx.identifier(), "role");
            return new SecurityDdlOperation.ShowRole(name, asJson);
        }
        if (ctx.USERS() != null) {
            return new SecurityDdlOperation.ShowUser(null, asJson);
        } else if (ctx.ROLES() != null) {
            return new SecurityDdlOperation.ShowRole(null, asJson);
        }
        return null;
    }

    private static boolean
        isAccountLocked(TableParser.Account_lockContext ctx) {
        if (ctx.LOCK() != null) {
            assert (ctx.UNLOCK() == null);
            return true;
        }
        return false;
    }

    private static String
        getIdentifierName(TableParser.IdentifierContext ctx, String idType) {
        if (ctx.extended_id() != null) {
            return ctx.extended_id().getText();
        }
        throw new ParseException("Invalid empty name of " + idType);
    }

    private static String
        getIdentifierName(TableParser.Identifier_or_stringContext ctx,
                          String idType) {
        if (ctx.identifier() != null) {
            return getIdentifierName(ctx.identifier(), idType);
        }
        if (ctx.STRING() != null) {
            final String result = stripFirstLast(ctx.STRING().getText());
            if (!result.equals("")) {
                return result;
            }
        }
        throw new ParseException("Invalid empty name of " + idType);
    }

    private static String
        getIdentifierName(TableParser.Create_user_identified_clauseContext ctx,
                          String idType) {
        if (ctx.identified_clause() != null && ctx.identifier() != null) {
            return getIdentifierName(ctx.identifier(), idType);
        }
        if (ctx.IDENTIFIED_EXTERNALLY() != null && ctx.STRING() != null) {
            final String result = stripFirstLast(ctx.STRING().getText());
            if (!result.equals("")) {
                return result;
            }
        }
        throw new ParseException("Invalid empty name of " + idType);
    }

    /*
     * TODO: Will be extended to parse other types of authentication. For now
     * we only parse a password by default.
     */
    private static char[]
        resolvePlainPassword(TableParser.Identified_clauseContext ctx) {

        final String passStr = ctx.by_password().STRING().getText();
        if (passStr.isEmpty() || passStr.length() <= 2) {
            throw new DdlException("Invalid empty password");
        }
        /* Tears down the surrounding '"' */
        final char[] result = new char[passStr.length() - 2];
        passStr.getChars(1, passStr.length() - 1, result, 0);
        return result;
    }

    private static long
        resolvePassLifeTime(TableParser.Password_lifetimeContext ctx) {
        final long timeValue;
        final TimeUnit timeUnit;
        try {
            timeValue = Integer.parseInt(ctx.duration().INT().getText());
            if (timeValue <= 0) {
                throw new DdlException(
                    "Time value must not be zero or negative");
            }
        } catch (NumberFormatException nfe) {
            throw new ParseException("Invalid numeric value for time value");
        }

        timeUnit = convertToTimeUnit(ctx.duration().TIME_UNIT().getText());
        return TimeUnit.MILLISECONDS.convert(timeValue, timeUnit);
    }

    private static TimeUnit convertToTimeUnit(String unitStr) {
        try {
            return TimeUnit.valueOf(
                unitStr.toUpperCase(ENGLISH));
        } catch (IllegalArgumentException iae) {
            try {
                return DDLTimeUnit.valueOf(
                    unitStr.toUpperCase(ENGLISH)).getUnit();
            } catch (IllegalArgumentException iae2) {
                /* Fall through */
            }
        }
        throw new DdlException("Unrecognized time unit");
    }

    /**
     * Use the generatedNames set to generate a unique name based on the
     * prefix, which is unique per-type (record, enum, binary).  Avro
     * requires generated names for some data types that otherwise do not
     * need them in the DDL.
     */
    private String generateFieldNameInternal(String prefix) {
        final String gen = "_gen";
        int num = 0;
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(gen);
        String name = sb.toString();
        while (generatedNames.contains(name)) {
            sb.append(num++);
            name = sb.toString();
        }
        generatedNames.add(name);
        return name;
    }

    /**
     * Returns the full path of the name.
     */
    static private String getNamePath(TableParser.Name_pathContext ctx) {
        return getNamePath(ctx, true);
    }

    /*
     * Returns the name.  If fullName is true then it is a fully
     * qualified (dot notation) name.  If fullName is false, and it is
     * a child name only the last component of the full name is returned,
     * which is the actual child name scoped to its parent.
     */
    static private String getNamePath(TableParser.Name_pathContext ctx,
                                      boolean fullName) {
        if (ctx.extended_id() != null) {
            return ctx.extended_id().getText();
        }
        String path = ctx.NAME_PATH().getText();
        if (fullName) {
            return path;
        }
        return path.substring(path.lastIndexOf('.') + 1);
    }

    /*
     * Returns the full name from a Complex_name_pathContext, which can
     * contain any of these formats:
     * name_path: a[.b]*
     * keyof: KEYOF(target_name_path)
     * elementof: ELEMENTOF(target_name_path)[. name_path]*
     *
     * The latter two get translated into, respectively:
     *   target_name_path.KEYOF_TAG
     *   target_name_path.ELEMENTOF_TAG[. name_path]*
     */
    static private String getComplexNamePath
        (TableParser.Complex_name_pathContext ctx) {

        /*
         * If it's a name_path use the existing method.
         */
        if (ctx.name_path() != null) {
            return getNamePath(ctx.name_path(), true);
        }

        if (ctx.keyof_expr() != null) {
            List<TableParser.Name_pathContext> path =
                new ArrayList<TableParser.Name_pathContext>(1);
            path.add(ctx.keyof_expr().name_path());
            return translatePath(path, KEYOF_TAG);

        }
        if (ctx.elementof_expr() != null) {
            if (ctx.elementof_expr().name_path().isEmpty()) {
                throw new ParseException("Invalid empty elementof() " +
                                         "expression");
            }
            return translatePath(ctx.elementof_expr().name_path(),
                                 ELEMENTOF_TAG);
        }
        /* can't get here */
        throw new IllegalStateException("getComplexNamePath");
    }

    /*
     * Append the tag to the first element of the list.  If there is an
     * additional path, append it.
     */
    static private String translatePath
        (List<TableParser.Name_pathContext> name_paths, String tag) {
        int size = name_paths.size();
        assert size > 0 && size <= 2;

        StringBuilder sb = new StringBuilder();
        sb.append(getNamePath(name_paths.get(0), true));
        sb.append(TableImpl.SEPARATOR);
        sb.append(tag);
        if (size == 2) {
            sb.append(TableImpl.SEPARATOR);
            sb.append(getNamePath(name_paths.get(1), true));
        }
        return sb.toString();
    }

    /**
     * Returns the parent table if the path includes a ".", null otherwise.
     */
    private TableImpl getParentTable(TableParser.Name_pathContext ctx) {
        if (ctx.extended_id() != null) {
            return null;
        }
        String fullPath = ctx.NAME_PATH().getText();
        String parentPath =
            fullPath.substring(0, fullPath.lastIndexOf('.'));
        TableImpl parent = getTable(parentPath);
        if (parent == null) {
            noParentTable(parentPath, fullPath);
        }
        return parent;
    }

    /**
     * Returns the named table if it exists in the table metadata.
     *
     * @return the table if it exists, null if not
     * @throws DdlException if TableMetadata is null
     */
    private TableImpl getTable(String fullName) {
        if (metadata == null) {
            throw new DdlException
                ("TableDdl: cannot get table, no metadata available: " +
                 fullName);
        }
        return metadata.getTable(fullName);
    }

    /**
     * Returns the named table if it exists in table metadata.  Null will be
     * returned if either the table metadata is null, or the table does not
     * exist.
     */
    private TableImpl getTableSilently(String fullName) {
        return metadata == null ? null : metadata.getTable(fullName);
    }

    /**
     * Encapsulates calls to Field.addToBuilder() because it may throw
     * IllegalArgumentException because of invalid table or field state,
     * which needs to be mapped to DdlException.
     */
    private void addToBuilder(Field field) {
        try {
            field.addToBuilder();
        } catch (IllegalArgumentException iae) {
            throw new DdlException(iae.getMessage());
        }
    }

    static private String[] makeIdArray(
        List<TableParser.Extended_idContext> list) {

        String[] ids = new String[list.size()];
        int i = 0;
        for (TableParser.Extended_idContext idCtx : list) {
            ids[i++] = idCtx.getText();
        }
        return ids;
    }

    static private void getPrivSet(List<TableParser.Priv_itemContext> pCtxList,
                                   Set<String> privSet) {
        for (TableParser.Priv_itemContext privItem : pCtxList) {
            if (privItem.ALL_PRIVILEGES() != null) {
                privSet.add(ALL_PRIVS);
            } else {
                privSet.add(getIdentifierName(privItem.identifier(),
                                              "privilege"));
            }
        }
    }

    static private String[] makeNameArray
        (List<TableParser.Complex_name_pathContext> list) {
        String[] names = new String[list.size()];
        int i = 0;
        for (TableParser.Complex_name_pathContext path : list) {
            names[i++] = getComplexNamePath(path);
        }
        return names;
    }
    
    private AnnotatedField[] makeFtsFieldArray
    	(List<TableParser.Fts_pathContext>list) {
    	
    	final AnnotatedField[] fieldspecs =
    			new AnnotatedField[list.size()];
    	
    	int i = 0;
    	for (TableParser.Fts_pathContext pctx: list) {
            TableParser.Complex_name_pathContext path = pctx.complex_name_path();
            String fieldName = getComplexNamePath(path);
            String jsonStr = jsonCollector.get(pctx.json());
            fieldspecs[i++] = new AnnotatedField(fieldName, jsonStr);
    	}
    	return fieldspecs;
    }

    static private String stripFirstLast(String s) {
        return s.substring(1, s.length() - 1);
    }

    static private void noTable(String name) {
        throw new DdlException("Table does not exist: " + name);
    }

    static private void noParentTable(String parentName, String fullName) {
        throw new DdlException
            ("Parent table does not exist (" + parentName +
             ") in table path " + fullName);
    }

    /**
     * Returns the name of the field being defined starting with the context of
     * the type definition.  This may be null in the case of anonymous types,
     * such as fields in an array or map.  It is used to match names in
     * check expressions.  Fields with names will have a parent context of
     * Field_defContext, which has access to the name.
     */
    static private String getFieldName(TableParser.Type_defContext ctx) {
        if (ctx.parent instanceof TableParser.Field_defContext) {
            return getNamePath
                (((TableParser.Field_defContext)ctx.parent).name_path());
        }
        return null;
    }

    /**
     * A set of internal classes to hold the current field being defined and
     * sufficient state to add it to the current builder when exiting the
     * definition in the parse tree walk.
     *
     * This class hierarchy mirrors that of FieldDef plus some extra state that
     * is part of FieldMapEntry.  It would be possible to modify FieldDef and
     * relatives to use them here to avoid duplication if that might be cleaner.
     */
    private abstract class Field {
        boolean notNullable; /* defaults to false */
        String comment;
        final String fieldName;

        /*
         * If there is no field name use the string "_value" to match the
         * syntax required in the DDL itself for anonymous field definitions
         * such as those in arrays and maps.
         */
        static final String NO_NAME = "elementof()";

        Field(String fieldName) {
            this.fieldName = (fieldName != null ? fieldName : NO_NAME);
        }

        void setNotNullable() {
            notNullable = true;
        }

        Boolean getNullable() {
            return notNullable ? false : true;
        }

        void setComment(String comment) {
            this.comment = comment;
        }

        String getComment() {
            return comment;
        }

        /**
         * Generates a field name if necessary.  This is only needed for
         * types that require a name when used in a map or array, which
         * includes FixedBinary and Enum.
         */
        String generateFieldName() {
            return null;
        }

        void addToBuilder() {
            TableBuilderBase builder = builders.peek();
            String name = (builder.isCollectionBuilder() ?
                           generateFieldName() : fieldNames.peek());
            addToBuilder(builder, name);
        }

        /**
         * If the id doesn't match, an incorrect identifier was used in an
         * expression involving this field.
         */
        void validateExpressionId(String id) {
            if (!id.equalsIgnoreCase(fieldName)) {
                throw new ParseException
                    ("Invalid name for identifer in expression.  Expected " +
                     fieldName + ", found " + id);
            }
        }

        /**
         * Sets range values.  The values passed are raw and have not yet been
         * modified based on inclusivity.  That must be done in the
         * implementations.
         */
        @SuppressWarnings("unused")
        void setMin(String minValue, boolean minInclusive) {
            throw new IllegalStateException("Type does not support ranges");
        }

        @SuppressWarnings("unused")
        void setMax(String maxValue, boolean maxInclusive) {
            throw new IllegalStateException("Type does not support ranges");
        }

        @SuppressWarnings("unused")
        void setDefault(String defaultString) {
            throw new IllegalStateException("Type does not support defaults");
        }

        abstract void addToBuilder(TableBuilderBase builder, String name);

        /* used for error messages */
        abstract String getType();
    }

    private class IntField extends Field {
        Integer defaultValue;
        Integer min;
        Integer max;

        IntField(String fieldName) {
            super(fieldName);
        }

        @Override
        void setMin(String minValue, boolean minInclusive) {
            if (minValue != null) {
                int i = Integer.parseInt(minValue);
                min = (minInclusive ? i : i + 1);
            }
        }

        @Override
        void setMax(String maxValue, boolean maxInclusive) {
            if (maxValue != null) {
                int i = Integer.parseInt(maxValue);
                max = (maxInclusive ? i : i - 1);
            }
        }

        @Override
        void setDefault(String defaultString) {
            defaultValue = Integer.parseInt(defaultString);
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            builder.addInteger(name, getComment(), getNullable(),
                               defaultValue,
                               min, max);
        }

        @Override
        String getType() {
            return "INTEGER";
        }
    }

    private class LongField extends Field {
        Long defaultValue;
        Long min;
        Long max;

        LongField(String fieldName) {
            super(fieldName);
        }

        @Override
        void setMin(String minValue, boolean minInclusive) {
            if (minValue != null) {
                long l = Long.parseLong(minValue);
                min = (minInclusive ? l : l + 1);
            }
        }

        @Override
        void setMax(String maxValue, boolean maxInclusive) {
            if (maxValue != null) {
                long l = Long.parseLong(maxValue);
                max = (maxInclusive ? l : l - 1);
            }
        }

        @Override
        void setDefault(String defaultString) {
            defaultValue = Long.parseLong(defaultString);
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            builder.addLong(name, getComment(), getNullable(),
                            defaultValue,
                            min, max);
        }

        @Override
        String getType() {
            return "LONG";
        }
    }

    private class StringField extends Field {
        String defaultValue;
        String min;
        String max;
        Boolean minIncl;
        Boolean maxIncl;

        StringField(String fieldName) {
            super(fieldName);
        }

        @Override
        void setDefault(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        void setMin(String minValue, boolean minInclusive) {
            min = minValue;
            minIncl = minInclusive;
        }

        @Override
        void setMax(String maxValue, boolean maxInclusive) {
            max = maxValue;
            maxIncl = maxInclusive;
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            builder.addString(name, getComment(), getNullable(),
                              defaultValue,
                              min, max,
                              minIncl, maxIncl);
        }

        @Override
        String getType() {
            return "STRING";
        }
    }

    private class BooleanField extends Field {
        Boolean defaultValue;

        BooleanField(String fieldName) {
            super(fieldName);
        }

        @Override
        void setDefault(String defaultValue) {
            this.defaultValue = Boolean.parseBoolean(defaultValue);
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            builder.addBoolean(name, getComment(),
                               getNullable(), defaultValue);
        }

        @Override
        String getType() {
            return "BOOLEAN";
        }
    }

    /**
     * This class handles both Binary and FixedBinary, which differ only in
     * that FixedBinary has a non-zero size field.
     */
    private class BinaryField extends Field {
        int size;

        BinaryField(String fieldName, int size) {
            super(fieldName);
            this.size = size;
        }

        @Override
        String generateFieldName() {
            if (size == 0) {
                return null;
            }
            return generateFieldNameInternal("BINARY");
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            if (size == 0) {
                builder.addBinary(name, getComment(), getNullable());
            } else {
                builder.addFixedBinary(name, size, getComment(), getNullable());
            }
        }

        @Override
        String getType() {
            return "BINARY";
        }
    }

    private class FloatField extends Field {
        Float defaultValue;
        Float min;
        Float max;

        FloatField(String fieldName) {
            super(fieldName);
        }

        @Override
        void setMin(String minValue, boolean minInclusive) {
            if (minValue != null) {
                float f = Float.parseFloat(minValue);
                min = (minInclusive ? f : Math.nextUp(f));
            }
        }

        @Override
        void setMax(String maxValue, boolean maxInclusive) {
            if (maxValue != null) {
                float f = Float.parseFloat(maxValue);
                max = (maxInclusive ? f :
                       Math.nextAfter(f, Float.NEGATIVE_INFINITY));
            }
        }

        @Override
        void setDefault(String defaultString) {
            defaultValue = Float.parseFloat(defaultString);
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            builder.addFloat(name, getComment(), getNullable(),
                             defaultValue,
                             min, max);
        }

        @Override
        String getType() {
            return "FLOAT";
        }
    }

    private class DoubleField extends Field {
        Double defaultValue;
        Double min;
        Double max;

        DoubleField(String fieldName) {
            super(fieldName);
        }

        @Override
        void setMin(String minValue, boolean minInclusive) {
            if (minValue != null) {
                double d = Double.parseDouble(minValue);
                min = (minInclusive ? d : Math.nextUp(d));
            }
        }

        @Override
        void setMax(String maxValue, boolean maxInclusive) {
            if (maxValue != null) {
                double d = Double.parseDouble(maxValue);
                max = (maxInclusive ? d :
                       Math.nextAfter(d, Double.NEGATIVE_INFINITY));
            }
        }

        @Override
        void setDefault(String defaultString) {
            defaultValue = Double.parseDouble(defaultString);
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            builder.addDouble(name, getComment(), getNullable(),
                              defaultValue,
                              min, max);
        }

        @Override
        String getType() {
            return "DOUBLE";
        }
    }

    private class EnumField extends Field {
        String defaultValue;
        String [] values;

        EnumField(String fieldName, String[] values) {
            super(fieldName);
            this.values = values;
        }

        @Override
        void setDefault(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        String generateFieldName() {
            return generateFieldNameInternal("ENUM");
        }

        @Override
        void addToBuilder(TableBuilderBase builder, String name) {
            builder.addEnum(name, values,
                            getComment(), getNullable(), defaultValue);
        }

        @Override
        String getType() {
            return "ENUM";
        }
    }

    /**
     * Handle parser errors.  Consider additional interface methods and
     * some custom notifications (in the grammar) for common errors.  See
     * p. 170 in the book for examples.
     */
    private static class DdlErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line,
                                int charPositionInLine,
                                String msg,
                                RecognitionException re) {
            List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
            Collections.reverse(stack);

            String errorMsg = msg + ", at line " + line + ":" +
                charPositionInLine + // " at " + offendingSymbol +
                "\nrule stack: " + stack;
            throw new ParseException(errorMsg);
        }
    }
}
