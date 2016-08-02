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

package oracle.kv.impl.admin.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;

import oracle.kv.impl.admin.CommandServiceAPI;
import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.api.avro.AvroDdl;
import oracle.kv.impl.api.table.ArrayBuilder;
import oracle.kv.impl.api.table.MapBuilder;
import oracle.kv.impl.api.table.RecordBuilder;
import oracle.kv.impl.api.table.TableBuilder;
import oracle.kv.impl.api.table.TableBuilderBase;
import oracle.kv.impl.api.table.TableEvolver;
import oracle.kv.impl.api.table.TableImpl;
import oracle.kv.impl.api.table.TableMetadata;
import oracle.kv.impl.metadata.Metadata.MetadataType;
import oracle.kv.impl.param.ParameterMap;
import oracle.kv.impl.param.ParameterUtils;
import oracle.kv.impl.util.CommandParser;
import oracle.kv.table.FieldDef;
import oracle.kv.table.FieldDef.Type;
import oracle.kv.table.FieldValue;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;
import oracle.kv.table.TableUtils;
import oracle.kv.util.shell.CommandWithSubs;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellCommand;
import oracle.kv.util.shell.ShellException;

import com.sleepycat.je.util.DbCacheSize;

class TableCommand extends CommandWithSubs {
    final static String TABLE_NAME_FLAG = "-name";
    final static String DESC_FLAG = "-desc";
    final static String CREATE_FLAG = "-create";
    final static String EVOLVE_FLAG = "-evolve";
    final static String R2_COMPAT = "-r2-compat";
    final static String ALL_FLAG = "-all";
    final static String ORIGINAL_FLAG = "-original";
    final static String FIELD_NAME_FLAG = "-name";
    final static String FIELD_FLAG = "-field";
    final static String TYPE_FLAG = "-type";
    final static String DEFAULT_FLAG = "-default";
    final static String NOT_NULLABLE_FLAG = "-not-nullable";
    final static String MAX_FLAG = "-max";
    final static String MIN_FLAG = "-min";
    final static String SIZE_FLAG = "-size";
    final static String MAXEXCL_FLAG = "-max-exclusive";
    final static String MINEXCL_FLAG = "-min-exclusive";
    final static String ENUM_VALUE_FLAG = "-enum-values";
    final static String SCHEMA_NAME_FLAG = "-name";

    final static String TABLE_NAME_FLAG_DESC = TABLE_NAME_FLAG + " <name>";
    final static String DESC_FLAG_DESC = DESC_FLAG + " <description>";
    final static String CREATE_FLAG_DESC = CREATE_FLAG;
    final static String EVOLVE_FLAG_DESC = EVOLVE_FLAG;
    final static String R2_COMPAT_DESC = R2_COMPAT;
    final static String ALL_FLAG_DESC = ALL_FLAG;
    final static String ORIGINAL_FLAG_DESC = ORIGINAL_FLAG;
    final static String FIELD_NAME_FLAG_DESC = FIELD_NAME_FLAG + " <name>";
    final static String FIELD_FLAG_DESC = FIELD_FLAG + " <name>";
    final static String TYPE_FLAG_DESC = TYPE_FLAG + " <type>";
    final static String DEFAULT_FLAG_DESC = DEFAULT_FLAG + " <value>";
    final static String NOT_NULLABLE_FLAG_DESC = NOT_NULLABLE_FLAG;
    final static String MAX_FLAG_DESC = MAX_FLAG + " <value>";
    final static String MIN_FLAG_DESC = MIN_FLAG + " <value>";
    final static String SIZE_FLAG_DESC = SIZE_FLAG + " <size>";
    final static String MAXEXCL_FLAG_DESC = MAXEXCL_FLAG;
    final static String MINEXCL_FLAG_DESC = MINEXCL_FLAG;
    final static String ENUM_VALUE_FLAG_DESC =
        ENUM_VALUE_FLAG + " <value[,value[,...]]>";
    final static String SCHEMA_NAME_FLAG_DESC =
        SCHEMA_NAME_FLAG + " <schema-name>";

    private static final
        List<? extends SubCommand> subs =
                       Arrays.asList(new TableClearSub(),
                                     new TableCreateSub(),
                                     new TableEvolveSub(),
                                     new TableListSub(),
                                     new TableSizeSub());

    TableCommand() {
        super(subs, "table", 3, 2);
        overrideJsonFlag = true;
    }

    @Override
    protected String getCommandOverview() {
        return
            "This command has been deprecated in favor of the" + eol +
            "execute <statement> command for most operations." + eol +
            "The table command encapsulates commands for building tables " +
            "for addition or evolution. " + eol +
            "Tables are created and modified in two steps.  " +
            "The table command creates new tables or" + eol +
            "evolves existing tables and saves them in temporary, " +
            "non-persistent storage in the admin" + eol +
            "client.  These tables are kept by name when exiting the create " +
            "and evolve sub-commands. " + eol +
            "The second step is to use the plan command to deploy " +
            "the new and changed tables" + eol +
            "to the store itself.  The temporary list of tables " +
            "can be examined and cleared using" + eol +
            "the list and clear sub-commands.";
    }

    @Override
    protected boolean isDeprecated() {
        return true;
    }

    private static class TableBuilderVariable {
        private final String name;
        private final TableBuilderBase builder;

        TableBuilderVariable(String varName, TableBuilderBase tb) {
            this.name = varName;
            this.builder = tb;
        }

        public String getName() {
            return this.name;
        }

        public TableBuilderBase getTableBuilder() {
            return this.builder;
        }
    }

    static abstract class TableBuildCmdWithSubs extends CommandWithSubs {

        private TableBuilderVariable varBuilder = null;
        TableBuildCmdWithSubs(List<? extends SubCommand> subCommands,
                              String name,
                              int prefixLength,
                              int minArgCount) {
            super(subCommands, name, prefixLength, minArgCount);
            initSubs(subCommands);
        }

        private void initSubs(List<? extends SubCommand> tblSubs) {
            for (SubCommand command: tblSubs) {
                ((TableBuildSubCommand)command).setParentCommand(this);
            }
        }

        public String execute(TableBuilderVariable tbv,
                              String[] args,
                              Shell shell)
            throws ShellException {
            /* Handle the help command. */
            if (isHelpCommand(args[1], shell)) {
                String[] argsEx = new String[args.length - 1];
                argsEx[0] = args[0];
                System.arraycopy(args, 2, argsEx, 1, args.length - 2);
                return getHelp(argsEx, shell);
            }
            varBuilder = tbv;
            return execute(args, shell);
        }

        protected TableBuilderBase getTableBuilder() {
            return varBuilder.getTableBuilder();
        }

        protected String getTableBuilderName() {
            return varBuilder.getName();
        }

        private boolean isHelpCommand(String commandName, Shell shell) {
            ShellCommand cmd = shell.findCommand(commandName);
            return (cmd instanceof Shell.HelpCommand);
        }
    }

    static abstract class TableBuildSubCommand extends SubCommand {
        private CommandWithSubs parentCommand = null;

        protected TableBuildSubCommand(String name, int prefixMatchLength) {
            super(name, prefixMatchLength);
        }

        protected void setParentCommand(CommandWithSubs command) {
            parentCommand = command;
        }

        protected TableBuilderBase getCurrentTableBuilder() {
            return ((TableBuildCmdWithSubs)parentCommand).getTableBuilder();
        }

        protected String getCurrentTableBuilderName() {
            return ((TableBuildCmdWithSubs)parentCommand).getTableBuilderName();
        }
    }

    /* The TableCreate subCommand and its TableBuildSubCommands. */
    static class TableCreateSub extends SubCommand {
        private static final String VAR_TABLEBUILDER = "CurrentTabBuilder";
        private static final TableCreateSubs createSubs = new TableCreateSubs();

        TableCreateSub() {
            super("create", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            TableBuilderVariable tbv =
                (TableBuilderVariable)getVariable(VAR_TABLEBUILDER);
            if (tbv == null) {
                Shell.checkHelp(args, this);
                String tableName = null;
                String desc = null;
                Boolean r2Compat = null;
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];
                    if (TABLE_NAME_FLAG.equals(arg)) {
                        tableName = Shell.nextArg(args, i++, this);
                    } else if (DESC_FLAG.equals(arg)) {
                        desc = Shell.nextArg(args, i++, this);
                    } else if (R2_COMPAT.equals(arg)) {
                        r2Compat = true;
                    } else {
                        shell.unknownArgument(arg, this);
                    }
                }

                if (tableName == null) {
                    shell.requiredArg(TABLE_NAME_FLAG, this);
                }

                /* Parse tableName, split parent name and table id. */
                String parentName = null;
                String[] names = parseName(tableName);
                String tableId = names[0];
                if (names.length == 2) {
                    parentName = names[1];
                }

                /* Check the existence of parentName table. */
                Table tbParent = null;
                if (parentName != null) {
                    tbParent = findTable(parentName, true, shell);
                }

                try {
                    TableBuilderBase tb = TableBuilder.createTableBuilder(
                        tableId, desc, tbParent);
                    if (r2Compat != null) {
                        tb.setR2compat(r2Compat);
                    }
                    tbv = new TableBuilderVariable(tableName, tb);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                ShellCommand cmd = this.clone();
                cmd.addVariable(VAR_TABLEBUILDER, tbv);
                cmd.setPrompt(tableName);
                shell.pushCurrentCommand(cmd);
                return null;
            }
            return createSubs.execute(tbv, args, shell);
        }

        private static String[] parseName(String tableName) {
            String[] retNames = null;
            int pos = tableName.lastIndexOf(TableImpl.SEPARATOR);
            if (pos > 0) {
                retNames = new String[2];
                retNames[0] = tableName.substring(pos + 1);
                retNames[1] = tableName.substring(0, pos);
            } else {
                retNames = new String[]{tableName};
            }
            return retNames;
        }

        private static class TableCreateSubs extends TableBuildCmdWithSubs {
            private static final
                List<? extends TableBuildSubCommand> tblCreateSubs =
                               Arrays.asList(new TableBuildAddArraySub(),
                                             new TableBuildAddFieldSub(),
                                             new TableBuildRemoveFieldSub(),
                                             new TableBuildAddMapSub(),
                                             new TableBuildAddRecordSub(),
                                             new TableBuildAddSchemaSub(),
                                             new TableBuildCancelSub(),
                                             new TableBuildExitSub(),
                                             new TableBuildShardKeySub(),
                                             new TableBuildPrimaryKeySub(),
                                             new TableBuildSetDescSub(),
                                             new TableBuildShowSub());
            TableCreateSubs() {
                super(tblCreateSubs, "", 0, 2);
            }

            @Override
            public String getCommandOverview() {
                return "Build a new table to be added to the store.";
            }
        }

        @Override
        protected String getCommandSyntax() {
            return "table create " + TABLE_NAME_FLAG_DESC +
                " [" + DESC_FLAG_DESC + "] [" + R2_COMPAT + "]";
        }

        @Override
        protected String getCommandDescription() {
            return "Build a new table to be added to the store.  " +
                "New tables are added using" + eolt +
                "the plan add-table command.  " +
                "The table name is a dot-separated name with" + eolt +
                "the format tableName[.childTableName]+.  Flag " + R2_COMPAT +
                " is used to" + eolt + "create a table on top of " +
                "existing R2 Key/Value pairs.";
        }
    }

    /* The TableEvolve subCommand and its TableBuildSubCommands. */
    static class TableEvolveSub extends SubCommand {
        private static final String VAR_TABLEEVOLVER = "CurrentTabEvolver";
        private static final TableEvolveSubs evolveSubs = new TableEvolveSubs();

        protected TableEvolveSub() {
            super("evolve", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            TableBuilderVariable tbv =
                (TableBuilderVariable)getVariable(VAR_TABLEEVOLVER);
            if (tbv == null) {
                Shell.checkHelp(args, this);
                String tableName = null;
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];
                    if (TABLE_NAME_FLAG.equals(arg)) {
                        tableName = Shell.nextArg(args, i++, this);
                    } else {
                        shell.unknownArgument(arg, this);
                    }
                }
                if (tableName == null) {
                    shell.requiredArg(TABLE_NAME_FLAG, this);
                }

                Table table = findTable(tableName, true, shell);
                tbv = new TableBuilderVariable(
                    tableName, TableEvolver.createTableEvolver(table));
                ShellCommand cmd = this.clone();
                cmd.addVariable(VAR_TABLEEVOLVER, tbv);
                cmd.setPrompt(table.getName());
                shell.pushCurrentCommand(cmd);
                return null;
            }
            return evolveSubs.execute(tbv, args, shell);
        }

        private static class TableEvolveSubs extends TableBuildCmdWithSubs {
            private static final
                List<? extends TableBuildSubCommand> tblEvolveSubs =
                                   Arrays.asList(new TableBuildAddArraySub(),
                                                 new TableBuildAddFieldSub(),
                                                 new TableBuildAddMapSub(),
                                                 new TableBuildAddRecordSub(),
                                                 new TableBuildCancelSub(),
                                                 new TableBuildExitSub(),
                                                 new TableBuildRemoveFieldSub(),
                                                 new TableBuildShowSub());
            TableEvolveSubs() {
                super(tblEvolveSubs, "", 0, 2);
            }

            @Override
            public String getCommandOverview() {
                return "Build table for evolution.";
            }
        }

        @Override
        protected String getCommandSyntax() {
            return "table evolve " + TABLE_NAME_FLAG_DESC;
        }

        @Override
        protected String getCommandDescription() {
            return "Build a table for evolution.  Tables are evolved using " +
                "the plan" + eolt + "evolve-table command.  The table name " +
                "is a dot-separated name with the" + eolt +
                "format tableName[.childTableName]+.";
        }
    }

    /* TableListSub subCommand to show the table(s) building information. */
    static class TableListSub extends SubCommand {

        TableListSub() {
            super("list", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);

            boolean createOnly = false;
            boolean evolveOnly = false;
            String tableName = null;

            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (TABLE_NAME_FLAG.equals(arg)) {
                    tableName = Shell.nextArg(args, i++, this);
                } else if (CREATE_FLAG.equals(arg)) {
                    createOnly = true;
                } else if (EVOLVE_FLAG.equals(arg)) {
                    evolveOnly = true;
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (!createOnly && !evolveOnly) {
                createOnly = evolveOnly = true;
            }

            if (tableName != null) {
                Object obj = shell.getVariable(tableName);
                if (obj instanceof TableBuilder ||
                    obj instanceof TableEvolver) {
                    return getTableBuilderJsonString((TableBuilderBase)obj);
                }
                return "Table " + tableName + " does not exist.";
            }

            StringBuilder sbCreateInfo = new StringBuilder();
            StringBuilder sbEvolveInfo = new StringBuilder();
            boolean verbose = shell.getVerbose();
            Set<Entry<String, Object>> variables = shell.getAllVariables();
            for (Entry<String, Object> entry: variables) {
                Object obj = entry.getValue();
                if (createOnly && (obj instanceof TableBuilder)) {
                    if (verbose) {
                        sbCreateInfo.append(
                            getTableBuilderJsonString((TableBuilder)obj));
                    } else {
                        sbCreateInfo.append(eolt);
                        sbCreateInfo.append(
                            getTableBuilderAbstractInfo((TableBuilder)obj));
                    }
                }
                if (evolveOnly && (obj instanceof TableEvolver)){
                    if (verbose) {
                        sbEvolveInfo.append(
                            getTableBuilderJsonString((TableEvolver)obj));
                    } else {
                        sbEvolveInfo.append(eolt);
                        sbEvolveInfo.append(
                            getTableBuilderAbstractInfo((TableEvolver)obj));
                    }
                }
            }
            if (sbCreateInfo.length() == 0 && sbEvolveInfo.length() == 0) {
                return "No tables available.";
            }

            String createInfo = sbCreateInfo.toString();
            String evolveInfo = sbEvolveInfo.toString();
            if (!verbose) {
                if (createInfo.length() > 0) {
                    createInfo = "Tables to be added: " + createInfo;
                }
                if (evolveInfo.length() > 0) {
                    evolveInfo = "Tables to be evolved: " + evolveInfo;
                    if (createInfo.length() > 0) {
                        evolveInfo = eol + evolveInfo;
                    }
                }
            }
            return (createInfo + evolveInfo);
        }

        private String getTableBuilderJsonString(TableBuilderBase obj) {
            final String CREATE_LABEL = "Add table ";
            final String EVOLVE_LABEL = "Evolve table ";
            String retString = null;

            if (obj instanceof TableBuilder) {
                TableBuilder tb = ((TableBuilder)obj);
                retString = CREATE_LABEL + makeTableFullName(tb) + ": " +
                            eol + tb.toJsonString(true) + eol;
            } else if (obj instanceof TableEvolver) {
                TableEvolver te = (TableEvolver)obj;
                retString = EVOLVE_LABEL + makeTableFullName(te) + ": "  +
                            eol + te.toJsonString(true) + eol;
            }
            return retString;
        }

        private String getTableBuilderAbstractInfo(TableBuilderBase obj) {
            String retString = null;
            if (obj instanceof TableBuilder) {
                TableBuilder tb = ((TableBuilder)obj);
                retString = makeTableFullName(tb);
                String desc = tb.getDescription();
                if (desc != null && desc.length() > 0) {
                    retString += " -- " + desc;
                }
            } else if (obj instanceof TableEvolver) {
                TableEvolver te = (TableEvolver)obj;
                retString = makeTableFullName(te);
                String desc = te.getTable().getDescription();
                if (desc != null && desc.length() > 0) {
                    retString += " -- " + desc;
                }
            }
            return retString;
        }

        @Override
        protected String getCommandSyntax() {
            return "table list [" + TABLE_NAME_FLAG_DESC + "] " +
                    "["+ CREATE_FLAG_DESC +" | " + EVOLVE_FLAG_DESC + "]";
        }

        @Override
        protected String getCommandDescription() {
            return "Lists all the tables built but not yet created or " +
                "evolved.  Flag " + TableCommand.TABLE_NAME_FLAG + eolt +
                "is used to show the details of the named table.  The table " +
                "name is" + eolt + "a dot-separated name with the format " +
                "tableName[.childTableName]+." + eolt + "Use flag " +
                CREATE_FLAG + " or " + EVOLVE_FLAG + " to show the tables " +
                "for addition or evolution.";
        }
    }

    /* TableClearSub subCommand to clear building information. */
    static class TableClearSub extends SubCommand {
        TableClearSub() {
            super("clear", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);

            String tableName = null;
            boolean removeAll = false;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (TABLE_NAME_FLAG.equals(arg)) {
                    tableName = Shell.nextArg(args, i++, this);
                } else if (ALL_FLAG.equals(arg)) {
                    removeAll = true;
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (tableName == null && !removeAll) {
                shell.requiredArg(TABLE_NAME_FLAG + "|" + ALL_FLAG, this);
            }

            if (tableName != null) {
                Object obj = shell.getVariable(tableName);
                if (obj != null) {
                    if (obj instanceof TableBuilderBase ) {
                        shell.removeVariable(tableName);
                        return "Table " +
                               makeTableFullName((TableBuilderBase)obj) +
                               " cleared.";
                    }
                }
                return "Table " + tableName + " does not exist.";
            }

            int count = 0;
            Set<Entry<String, Object>> vars = shell.getAllVariables();
            String tinfo = "";
            for (Iterator<Entry<String, Object>> iterator = vars.iterator();
                 iterator.hasNext(); ) {

                Entry<String, Object> entry = iterator.next();
                Object obj = entry.getValue();
                if (obj instanceof TableBuilderBase) {
                    if (tinfo.length() > 0) {
                        tinfo += ", ";
                    }
                    tinfo += makeTableFullName((TableBuilderBase)obj);
                    iterator.remove();
                    count++;
                }
            }
            if (count == 0) {
                return "No available table.";
            }
            return count + ((count > 1)?" tables":" table") + " cleared: " +
                   tinfo + ".";
        }

        @Override
        protected String getCommandSyntax() {
            return "table clear [" + TABLE_NAME_FLAG_DESC + " | " +
                    ALL_FLAG_DESC + "]";
        }

        @Override
        protected String getCommandDescription() {
            return "Clear the tables built but not yet created or evolved. " +
                "Use flag " + TABLE_NAME_FLAG + eolt + "to specify the table " +
                "to be clear.  The table name is a dot-separated name" + eolt +
                "with the format tableName[.childTableName]+.  Flag " +
                ALL_FLAG + " is used to clear" + eolt + "all the tables.";
        }
    }

    /* TableBuildSubCommand: add-field */
    static class TableBuildAddFieldSub extends TableBuildSubCommand {

        private final boolean inCollectionBuilder;

        protected TableBuildAddFieldSub() {
            this(false);
        }

        protected TableBuildAddFieldSub(boolean inCollectionBuilder) {
            super("add-field", 5);
            this.inCollectionBuilder = inCollectionBuilder;
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {
            Shell.checkHelp(args, this);

            String fname = null;
            FieldDef.Type type = null;
            String defVal = null;
            Boolean nullable = null;
            String max = null;
            String min = null;
            Boolean maxExcl = null;
            Boolean minExcl = null;
            String size = null;
            String desc = null;
            String[] values = null;
            TableBuilderBase tb = getCurrentTableBuilder();

            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (FIELD_NAME_FLAG.equals(arg)) {
                    fname = Shell.nextArg(args, i++, this);
                } else if (TYPE_FLAG.equals(arg)) {
                    String ftype = Shell.nextArg(args, i++, this);
                    type = getType(ftype);
                    if (type == null) {
                        invalidArgument(ftype);
                    }
                } else if (DESC_FLAG.equals(arg)) {
                    desc = Shell.nextArg(args, i++, this);
                } else if (DEFAULT_FLAG.equals(arg)) {
                    defVal = Shell.nextArg(args, i++, this);
                } else if (NOT_NULLABLE_FLAG.equals(arg)) {
                    nullable = false;
                } else if (MAX_FLAG.equals(arg)) {
                    max = Shell.nextArg(args, i++, this);
                } else if (MIN_FLAG.equals(arg)) {
                    min = Shell.nextArg(args, i++, this);
                } else if (MAXEXCL_FLAG.equals(arg)) {
                    maxExcl = true;
                } else if (MINEXCL_FLAG.equals(arg)) {
                    minExcl = true;
                } else if (SIZE_FLAG.equals(arg)) {
                    size = Shell.nextArg(args, i++, this);
                } else if (ENUM_VALUE_FLAG.equals(arg)) {
                    String str = Shell.nextArg(args, i++, this);
                    values = str.trim().split("\\,");
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (type == null) {
                shell.requiredArg(TYPE_FLAG, this);
            }

            /**
             * Only the element field of Map and Array with primitive types
             * does not require a name.
             */
            if (typeRequiresName(type)) {
                if (fname == null) {
                    shell.requiredArg(FIELD_NAME_FLAG, this);
                }
            }

            /* ENUM type requires ENUM values.*/
            if (type == Type.ENUM && values == null) {
                shell.requiredArg(ENUM_VALUE_FLAG, this);
            }
            /* FIXED_BINARY type requires a size.*/
            if (type == Type.FIXED_BINARY && size == null) {
                shell.requiredArg(SIZE_FLAG, this);
            }

            /* check if the property can be used for the data type. */
            if (min != null || max != null) {
                validateProperty(tb, type, MIN_FLAG);
            }
            if (nullable != null) {
                validateProperty(tb, type, NOT_NULLABLE_FLAG);
            }
            if (defVal != null) {
                validateProperty(tb, type, DEFAULT_FLAG);
            }
            if (minExcl != null || maxExcl != null) {
                validateProperty(tb, type, MINEXCL_FLAG);
            }
            if (size != null) {
                validateProperty(tb, type, SIZE_FLAG);
            }
            if (values != null) {
                validateProperty(tb, type, ENUM_VALUE_FLAG);
            }

            addField(shell, tb, fname, type, defVal,
                     nullable, min, max, minExcl, maxExcl, size,
                     desc, values);
            return null;
        }

        private void validateProperty(TableBuilderBase tabBuilder, Type type,
                                      String propFlag)
            throws ShellException{

            if (propFlag.equals(MIN_FLAG)) {
                if (type == Type.BOOLEAN || type == Type.BINARY ||
                    type == Type.ENUM || type == Type.FIXED_BINARY) {
                    invalidArgument
                        (MIN_FLAG + " and " + MAX_FLAG +
                         " is not allowed for type " + type);
                }
            } else if (propFlag.equals(DEFAULT_FLAG) ||
                       propFlag.equals(NOT_NULLABLE_FLAG)) {
                if (tabBuilder.isCollectionBuilder() ||
                    (type == Type.BINARY || type == Type.FIXED_BINARY)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(propFlag);
                    sb.append(" is not allowed for ");
                    if (tabBuilder.isCollectionBuilder()) {
                        sb.append("the type field of ");
                        sb.append(tabBuilder.getBuilderType());
                    } else {
                        sb.append("type ");
                        sb.append(type);
                    }
                    invalidArgument(sb.toString());
                }
            } else if (propFlag.equals(MINEXCL_FLAG)) {
                if (type != Type.STRING){
                    invalidArgument
                        (MINEXCL_FLAG + " and " + MAXEXCL_FLAG +
                         " can only be used with " + Type.STRING);
                }
            } else if (propFlag.equals(SIZE_FLAG)) {
                if (type != Type.FIXED_BINARY) {
                    invalidArgument(SIZE_FLAG +
                        " can only be used with " + Type.FIXED_BINARY);
                }
            } else if (propFlag.equals(ENUM_VALUE_FLAG)) {
                if (type != Type.ENUM) {
                    invalidArgument(ENUM_VALUE_FLAG +
                        " can only be used with " + Type.ENUM);
                }
            }
        }

        private FieldDef.Type getType(String type) {
            try {
                return FieldDef.Type.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        private void addField(Shell shell, TableBuilderBase tabBuilder,
                              String fname, FieldDef.Type type, String defVal,
                              Boolean nullable,
                              String min, String max, Boolean minExcl,
                              Boolean maxExcl, String size, String desc,
                              String[] values)
            throws ShellException {

            switch (type) {
            case INTEGER: {
                Integer idef = null;
                Integer imax = null;
                Integer imin = null;

                if (defVal != null) {
                    idef = getValidIntVal(defVal);
                }
                if (min != null) {
                    imin = getValidIntVal(min);
                }
                if (max != null) {
                    imax = getValidIntVal(max);
                }
                try {
                    tabBuilder.addInteger(fname, desc, nullable,
                                          idef, imin, imax);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            }
            case LONG: {
                Long ldef = null;
                Long lmax = null;
                Long lmin = null;
                if (defVal != null) {
                    ldef = getValidLongVal(defVal);
                }
                if (min != null) {
                    lmin = getValidLongVal(min);
                }
                if (max != null) {
                    lmax = getValidLongVal(max);
                }
                try {
                    tabBuilder.addLong(fname, desc, nullable,
                                       ldef, lmin, lmax);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            }
            case DOUBLE:{
                Double ddef = null;
                Double dmax = null;
                Double dmin = null;
                if (defVal != null) {
                    ddef = getValidDoubleVal(defVal);
                }
                if (min != null) {
                    dmin = getValidDoubleVal(min);
                }
                if (max != null) {
                    dmax = getValidDoubleVal(max);
                }
                try {
                    tabBuilder.addDouble(fname, desc, nullable,
                                         ddef, dmin, dmax);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            }
            case FLOAT:{
                Float fdef = null;
                Float fmax = null;
                Float fmin = null;
                if (defVal != null) {
                    fdef = getValidFloatVal(defVal);
                }
                if (min != null) {
                    fmin = getValidFloatVal(min);
                }
                if (max != null) {
                    fmax = getValidFloatVal(max);
                }
                try {
                    tabBuilder.addFloat(fname, desc, nullable,
                                        fdef, fmin, fmax);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            }
            case STRING:
                try {
                    tabBuilder.addString(fname, desc,
                                         nullable, defVal, min, max,
                                         (minExcl != null)?(!minExcl):null,
                                         (maxExcl != null)?(!maxExcl):null);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            case BOOLEAN: {
                boolean bdef = Boolean.valueOf(defVal);
                try {
                    tabBuilder.addBoolean(fname, desc, nullable, bdef);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            }
            case BINARY:
                try {
                    tabBuilder.addBinary(fname, desc);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            case FIXED_BINARY:
                int isize = 0;
                if (size != null) {
                    isize = getValidIntVal(size);
                } else {
                    throw new ShellException("FIXED_BINARY requires a size");
                }

                try {
                    tabBuilder.addFixedBinary(fname, isize, desc);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            case ENUM:
                try {
                    tabBuilder.addEnum(fname, values, desc, nullable, defVal);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException ice) {
                    throw new ShellException(ice.getMessage(), ice);
                }
                break;
            case MAP:
            case ARRAY:
            case RECORD:
                Object obj = getCurrentCmdVariable(shell, fname);
                try {
                    if (tabBuilder.isCollectionBuilder()) {
                        /* Ignore element name of Map and Array. */
                        tabBuilder.addField((FieldDef)obj);
                    } else {
                        tabBuilder.addField(fname, (FieldDef)obj);
                    }
                } catch (IllegalArgumentException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                } catch (IllegalCommandException iae) {
                    throw new ShellException(iae.getMessage(), iae);
                }
                removeCurrentCmdVariable(shell, fname);
                break;
            default:
                throw new ShellException("Unsupported datatype: " + type);
            }
        }

        private Integer getValidIntVal(String val)
            throws ShellException {

            try {
                return Integer.valueOf(val);
            } catch (NumberFormatException nfe) {
                invalidArgument(val + ", not a valid integer number.");
            }
            return null;
        }

        private Long getValidLongVal(String val)
            throws ShellException {

            try {
                return Long.valueOf(val);
            } catch (NumberFormatException nfe) {
                invalidArgument(val + ", not a valid long number.");
            }
            return null;
        }

        private Double getValidDoubleVal(String val)
            throws ShellException {

            try {
                return Double.valueOf(val);
            } catch (NumberFormatException nfe) {
                invalidArgument(val + ", not a valid double.");
            }
            return null;
        }

        private Float getValidFloatVal(String val)
            throws ShellException {

            try {
                return Float.valueOf(val);
            } catch (NumberFormatException nfe) {
                invalidArgument(val + ", not a valid float.");
            }
            return null;
        }

        private boolean typeRequiresName(Type type) {
            if (isNameOptional()) {
                return (type == Type.ENUM ||
                        type == Type.FIXED_BINARY ||
                        type == Type.RECORD);
            }
            return true;
        }

        private boolean isNameOptional() {
            return inCollectionBuilder;
        }

        private static void removeCurrentCmdVariable(Shell shell,
                                                     String varName) {
            shell.getCurrentCommand().removeVariable(varName);
        }

        @Override
        protected String getCommandSyntax() {
            String nameDesc = isNameOptional() ?
                    CommandParser.optional(FIELD_NAME_FLAG_DESC):
                    FIELD_NAME_FLAG_DESC;
            return "add-field " + TYPE_FLAG_DESC + " " + nameDesc +
                " ["+ NOT_NULLABLE_FLAG_DESC + "] " +
                eolt +
                "[" + DEFAULT_FLAG_DESC + "] " +
                "[" + MAX_FLAG_DESC + "] " +
                "[" + MIN_FLAG_DESC + "] " +
                eolt +
                "[" + MAXEXCL_FLAG_DESC + "] " +
                "[" + MINEXCL_FLAG_DESC + "] " +
                "[" + DESC_FLAG_DESC + "] " +
                eolt +
                "[" + SIZE_FLAG_DESC + "] " +
                "[" + ENUM_VALUE_FLAG_DESC + "]" +
                eolt +
                "<type>: INTEGER, LONG, DOUBLE, FLOAT, STRING, BOOLEAN," +
                " BINARY," + eolt + "FIXED_BINARY, ENUM.";
        }

        @Override
        protected String getCommandDescription() {
            return "Add a field. Ranges are inclusive with the exception of " +
                eolt + "String, which will be set to exclusive.";
        }
    }

    static abstract class ComplexFieldBuilderSub
        extends TableBuildSubCommand {

        private static final String VAR_BUILDER = "CurrentFieldBuilder";
        boolean inCollectionBuilder;

        protected ComplexFieldBuilderSub(String name,
                                         int prefixMatchLength) {
            this(name, prefixMatchLength, false);
        }

        protected ComplexFieldBuilderSub(String name,
                                         int prefixMatchLength,
                                         boolean inCollectionBuilder) {
            super(name, prefixMatchLength);
            this.inCollectionBuilder = inCollectionBuilder;
        }

        abstract TableBuildCmdWithSubs getFieldBuilderSubs();
        abstract TableBuilderBase createFieldBuilder(String fname, String desc);

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            TableBuilderVariable tbv =
                    (TableBuilderVariable)getVariable(VAR_BUILDER);
            if (tbv == null) {
                Shell.checkHelp(args, this);
                String fname = null;
                String desc = null;
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];
                    if (FIELD_NAME_FLAG.equals(arg)) {
                        fname = Shell.nextArg(args, i++, this);
                    } else if (DESC_FLAG.equals(arg)) {
                        desc = Shell.nextArg(args, i++, this);
                    } else {
                        shell.unknownArgument(arg, this);
                    }
                }
                if (isNameRequired() && fname == null) {
                    shell.requiredArg(FIELD_NAME_FLAG, this);
                }
                String varName = (fname == null) ? "element" : fname;
                tbv = new TableBuilderVariable(varName,
                                               createFieldBuilder(fname, desc));
                ShellCommand cmd = this.clone();
                cmd.addVariable(VAR_BUILDER, tbv);
                cmd.setPrompt(varName);
                shell.pushCurrentCommand(cmd);
                return null;
            }
            return getFieldBuilderSubs().execute(tbv, args, shell);
        }

        boolean isNameRequired() {
            return true;
        }

        String getCommonUsage() {
            return (isNameRequired() ?
                        FIELD_NAME_FLAG_DESC:
                        CommandParser.optional(FIELD_NAME_FLAG_DESC)) + " " +
                    CommandParser.optional(DESC_FLAG_DESC);
        }
    }

    /* TableBuildSubCommand: add-map-field */
    static class TableBuildAddMapSub extends ComplexFieldBuilderSub {

        private static TableBuildCmdWithSubs builderSubs = new MapBuilderSubs();

        TableBuildAddMapSub() {
            this(false);
        }

        TableBuildAddMapSub(boolean nestedCommand) {
            super("add-map-field", 5, nestedCommand);
        }

        @Override
        TableBuildCmdWithSubs getFieldBuilderSubs() {
            return builderSubs;
        }

        @Override
        TableBuilderBase createFieldBuilder(String fname, String desc) {
            return TableBuilder.createMapBuilder(desc);
        }

        @Override
        boolean isNameRequired() {
            return !inCollectionBuilder;
        }

        private static class MapBuilderSubs extends TableBuildCmdWithSubs {
            private static final
                List<? extends SubCommand> mapBuilderSubs =
                               Arrays.asList(new TableBuildAddArraySub(true),
                                             new TableBuildAddFieldSub(true),
                                             new TableBuildAddMapSub(true),
                                             new TableBuildAddRecordSub(),
                                             new TableBuildCancelSub(),
                                             new TableBuildExitSub(),
                                             new TableBuildSetDescSub(),
                                             new TableBuildShowSub());
            MapBuilderSubs() {
                super(mapBuilderSubs, "", 0, 2);
            }

            @Override
            public String getCommandOverview() {
                return "Build a map field.";
            }
        }

        @Override
        protected String getCommandSyntax() {
            return "add-map-field " + getCommonUsage();
        }

        @Override
        protected String getCommandDescription() {
            return "Build a map field.";
        }
    }

    /* TableBuildSubCommand: add-array-field */
    static class TableBuildAddArraySub extends ComplexFieldBuilderSub {

        private static TableBuildCmdWithSubs builderSubs =
            new ArrayBuilderSubs();

        TableBuildAddArraySub() {
            this(false);
        }

        TableBuildAddArraySub(boolean inCollectionBuilder) {
            super("add-array-field", 5, inCollectionBuilder);
        }

        @Override
        TableBuildCmdWithSubs getFieldBuilderSubs() {
            return builderSubs;
        }

        @Override
        TableBuilderBase createFieldBuilder(String fname, String desc) {
            return TableBuilder.createArrayBuilder(desc);
        }

        private static class ArrayBuilderSubs extends TableBuildCmdWithSubs {
            private static final
                List<? extends SubCommand> arraybuilderSubs =
                               Arrays.asList(new TableBuildAddArraySub(true),
                                             new TableBuildAddFieldSub(true),
                                             new TableBuildAddMapSub(true),
                                             new TableBuildAddRecordSub(),
                                             new TableBuildCancelSub(),
                                             new TableBuildExitSub(),
                                             new TableBuildSetDescSub(),
                                             new TableBuildShowSub());
            ArrayBuilderSubs() {
                super(arraybuilderSubs, "", 0, 2);
            }

            @Override
            public String getCommandOverview() {
                return "Build a array field.";
            }
        }

        @Override
        boolean isNameRequired() {
            return !inCollectionBuilder;
        }

        @Override
        protected String getCommandSyntax() {
            return "add-array-field " + getCommonUsage();
        }

        @Override
        protected String getCommandDescription() {
            return "Build a array field.";
        }
    }

    /* TableBuildSubCommand: add-record-field */
    static class TableBuildAddRecordSub extends ComplexFieldBuilderSub {

        private static TableBuildCmdWithSubs builderSubs =
            new RecordBuilderSubs();

        TableBuildAddRecordSub() {
            super("add-record-field", 5);
        }

        @Override
        TableBuildCmdWithSubs getFieldBuilderSubs() {
            return builderSubs;
        }

        @Override
        TableBuilderBase createFieldBuilder(String fname, String desc) {
            return TableBuilder.createRecordBuilder(fname, desc);
        }

        private static class RecordBuilderSubs extends TableBuildCmdWithSubs {
            private static final
                List<? extends SubCommand> recordBuilderSubs =
                               Arrays.asList(new TableBuildAddArraySub(),
                                             new TableBuildAddFieldSub(),
                                             new TableBuildAddMapSub(),
                                             new TableBuildAddRecordSub(),
                                             new TableBuildCancelSub(),
                                             new TableBuildExitSub(),
                                             new TableBuildSetDescSub(),
                                             new TableBuildShowSub());
            RecordBuilderSubs() {
                super(recordBuilderSubs, "", 0, 2);
            }

            @Override
            public String getCommandOverview() {
                return "Build a record field.";
            }
        }

        @Override
        protected String getCommandSyntax() {
            return "add-record-field " + getCommonUsage();
        }

        @Override
        protected String getCommandDescription() {
            return "Build a record field.";
        }
    }

    /* TableBuildSubCommand: remove-field */
    static class TableBuildRemoveFieldSub
        extends TableBuildSubCommand {

        protected TableBuildRemoveFieldSub() {
            super("remove-field", 4);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            String field = null;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (FIELD_NAME_FLAG.equals(arg)) {
                    field = Shell.nextArg(args, i++, this);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (field == null) {
                shell.requiredArg(FIELD_NAME_FLAG, this);
            }
            try {
                getCurrentTableBuilder().removeField(field);
            } catch (IllegalArgumentException iae) {
                throw new ShellException(iae.getMessage());
            }
            return null;
        }

        @Override
        protected String getCommandSyntax() {
            return "remove-field " + FIELD_NAME_FLAG_DESC;
        }

        @Override
        protected String getCommandDescription() {
            return "Remove a field.";
        }
    }

    /* TableBuildSubCommand: add-schema */
    static class TableBuildAddSchemaSub
        extends TableBuildSubCommand {

        protected TableBuildAddSchemaSub() {
            super("add-schema", 5);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            String schemaName = null;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (SCHEMA_NAME_FLAG.equals(arg)) {
                    schemaName = Shell.nextArg(args, i++, this);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }
            if (schemaName == null) {
                shell.requiredArg(SCHEMA_NAME_FLAG, this);
            }

            final CommandShell cmd = (CommandShell) shell;
            final CommandServiceAPI cs = cmd.getAdmin();
            final AvroDdl.SchemaDetails schema =
                getSchemaDetails(cs, schemaName, cmd);
            TableBuilder tb = (TableBuilder)getCurrentTableBuilder();
            try {
                tb.setSchemaId(schema.getId());
                tb.addSchema(schema.getText());
            } catch (IllegalArgumentException iae) {
                throw new ShellException(iae.getMessage(), iae);
            } catch (IllegalCommandException ice) {
                throw new ShellException(ice.getMessage(), ice);
            }
            return null;
        }

        private AvroDdl.SchemaDetails getSchemaDetails(CommandServiceAPI cs,
                                                       String schemaName,
                                                       CommandShell shell)
            throws ShellException {

            try {
                final SortedMap<String, AvroDdl.SchemaSummary> map =
                    cs.getSchemaSummaries(false /*includeDisabled*/);

                /* Get the the first active schema with the given name */
                AvroDdl.SchemaSummary summary = map.get(schemaName);
                if (summary != null) {
                    return cs.getSchemaDetails(summary.getId());
                }
            } catch (RemoteException re) {
                shell.noAdmin(re);
            }
            throw new ShellException("Schema does not exist or is disabled: " +
                                     schemaName);
        }

        @Override
        protected String getCommandSyntax() {
            return "add-schema " + SCHEMA_NAME_FLAG_DESC;
        }

        @Override
        protected String getCommandDescription() {
            return "Build a table from Avro schema.";
        }
    }

    /* TableBuildSubCommand: primary-key */
    static class TableBuildPrimaryKeySub extends TableBuildSubCommand {

        protected TableBuildPrimaryKeySub() {
            super("primary-key", 4);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            List<String> fields = new ArrayList<String>();
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (FIELD_FLAG.equals(arg)) {
                    fields.add(Shell.nextArg(args, i++, this));
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (fields.size() == 0) {
                shell.requiredArg(FIELD_FLAG, this);
            }
            TableBuilderBase tb = getCurrentTableBuilder();
            try {
                tb.primaryKey(fields.toArray(new String[fields.size()]));
            } catch (IllegalArgumentException iae) {
                throw new ShellException(iae.getMessage(), iae);
            }
            return null;
        }

        @Override
        protected String getCommandSyntax() {
            return "primary-key " + FIELD_FLAG_DESC +
                   " [" + FIELD_FLAG_DESC + "]+";
        }

        @Override
        protected String getCommandDescription() {
            return "Set primary key.";
        }
    }

    /* TableBuildSubCommand: shard-key */
    static class TableBuildShardKeySub extends TableBuildSubCommand {

        protected TableBuildShardKeySub() {
            super("shard-key", 4);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            List<String> fields = new ArrayList<String>();
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (FIELD_FLAG.equals(arg)) {
                    fields.add(Shell.nextArg(args, i++, this));
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (fields.size() == 0) {
                shell.requiredArg(FIELD_FLAG, this);
            }
            TableBuilderBase tb = getCurrentTableBuilder();
            try {
                tb.shardKey(fields.toArray(new String[fields.size()]));
            } catch (IllegalArgumentException iae) {
                throw new ShellException(iae.getMessage(), iae);
            }
            return null;
        }

        @Override
        protected String getCommandSyntax() {
            return "shard-key " +  FIELD_FLAG_DESC +
                   " [" + FIELD_FLAG_DESC + "]+";
        }

        @Override
        protected String getCommandDescription() {
            return "Set shard key.";
        }
    }

    /* TableBuildSubCommand: set-description */
    static class TableBuildSetDescSub extends TableBuildSubCommand {

        protected TableBuildSetDescSub() {
            super("set-description", 5);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            String description = null;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (DESC_FLAG.equals(arg)) {
                    description = Shell.nextArg(args, i++, this);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (description == null) {
                shell.requiredArg(DESC_FLAG, this);
            }
            getCurrentTableBuilder().setDescription(description);
            return null;
        }

        @Override
        protected String getCommandSyntax() {
            return "set-description " + DESC_FLAG_DESC;
        }

        @Override
        protected String getCommandDescription() {
            return "Set description for the table.";
        }
    }

    /* TableBuildSubCommand: show */
    static class TableBuildShowSub extends TableBuildSubCommand {

        protected TableBuildShowSub() {
            super("show", 2);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);

            boolean showOriginal = false;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (ORIGINAL_FLAG.equals(arg)) {
                    showOriginal = true;
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            TableBuilderBase tb = getCurrentTableBuilder();
            try {
                if (tb instanceof TableBuilder) {
                    return ((TableBuilder)tb).toJsonString(true);
                } else if (tb instanceof TableEvolver) {
                    if (showOriginal) {
                        return ((TableEvolver)tb).getTable().toJsonString(true);
                    }
                    return ((TableEvolver)tb).toJsonString(true);
                } else if (tb instanceof MapBuilder) {
                    return ((MapBuilder)tb).toJsonString(true);
                } else if (tb instanceof ArrayBuilder) {
                    return ((ArrayBuilder)tb).toJsonString(true);
                } else if (tb instanceof RecordBuilder) {
                    return ((RecordBuilder)tb).toJsonString(true);
                }
            } catch (IllegalCommandException ice) {
                throw new ShellException("Could not create JSON " +
                        "representation:" + eolt + ice.getMessage(), ice);
            } catch (IllegalArgumentException iae) {
                throw new ShellException("Could not create JSON " +
                        "representation:" + eolt + iae.getMessage(), iae);
            }
            return "";
        }

        @Override
        protected String getCommandSyntax() {
            return "show [" + ORIGINAL_FLAG_DESC + "]";
        }

        @Override
        protected String getCommandDescription() {
            return "Display the table information, if building a table for " +
                "evolution, " + eolt + "use " + ORIGINAL_FLAG_DESC +
                " flag to show the original table information, the flag " +
                eolt + "will be ignored for building table for addition.";
        }
    }

    /* TableBuildSubCommand: exit */
    static class TableBuildExitSub extends TableBuildSubCommand {

        protected TableBuildExitSub() {
            super("exit", 2);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            if (args.length != 1) {
                shell.badArgCount(this);
            }

            TableBuilderBase tb = getCurrentTableBuilder();
            String retString = null;
            if (tb instanceof TableBuilder) {
                retString = procAddTable((TableBuilder) tb, shell);
            } else if (tb instanceof TableEvolver) {
                retString = procEvolveTable((TableEvolver) tb, shell);
            } else {
                retString = procFieldBuilder(tb, getCurrentTableBuilderName(),
                                             shell);
            }
            return retString;
        }

        private String procAddTable(TableBuilder tbr, Shell shell)
            throws ShellException {

            try {
                tbr.validate();
            } catch (IllegalArgumentException iae) {
                throw new ShellException(iae.getMessage(), iae);
            } catch (IllegalCommandException ice) {
                throw new ShellException(ice.getMessage(), ice);
            }

            shell.addVariable(makeTableFullName(tbr), tbr);
            shell.popCurrentCommand();
            return "Table " + makeTableFullName(tbr) + " built.";
        }

        private String procEvolveTable(TableEvolver te, Shell shell)
            throws ShellException {

            try {
                te.evolveTable();
            } catch (IllegalCommandException ice) {
                throw new ShellException(ice.getMessage(), ice);
            }

            shell.addVariable(makeTableFullName(te), te);
            shell.popCurrentCommand();
            return "Table " + makeTableFullName(te) + " built.";
        }

        private String procFieldBuilder(TableBuilderBase tb,
                                        String varName, Shell shell)
            throws ShellException {

            FieldDef fieldDef = null;
            try {
                fieldDef = tb.build();
            } catch (IllegalArgumentException iae) {
                throw new ShellException(iae.getMessage(), iae);
            }
            shell.popCurrentCommand();

            /* Add an variable FieldDef to current command. */
            ShellCommand cmd = shell.getCurrentCommand();
            cmd.addVariable(varName, fieldDef);

            String ftype = getComplexFieldBuilderType(tb);
            if (ftype == null) {
                throw new ShellException("Unsupported field type.");
            }
            /*Run add-field command to add the field to tableBuilder. */
            shell.runLine("add-field -name " + varName + " -type " + ftype);
            return null;
        }

        private String getComplexFieldBuilderType(TableBuilderBase tb) {
            if (tb instanceof MapBuilder) {
                return "map";
            } else if (tb instanceof ArrayBuilder) {
                return "array";
            } else if (tb instanceof RecordBuilder) {
                return "record";
            }
            return null;
        }
        @Override
        protected String getCommandDescription() {
            return "Exit the building operation, saving the table(or field) " +
                    eolt + "for addition(or evolution) to the store.";
        }
    }

    /* TableBuildSubCommand: cancel */
    static class TableBuildCancelSub extends TableBuildSubCommand {

        protected TableBuildCancelSub() {
            super("cancel", 4);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            if (args.length != 1) {
                shell.badArgCount(this);
            }
            shell.popCurrentCommand();
            return null;
        }

        @Override
        protected String getCommandDescription() {
            return "Exit the building operation, canceling the table(or field)"+
                    eolt + "under construction.";
        }
    }

    /* Table size command */
    static class TableSizeSub extends SubCommand {
        static final String COMMAND_NAME = "size";

        static final String NROWS_FLAG = "-rows";
        static final String NROWS_FLAG_DESC = NROWS_FLAG + " <num>";
        static final String JSON_FLAG = "-json";
        static final String JSON_FLAG_DESC = JSON_FLAG + " <string>";
        static final String PRIMARYKEY_FLAG = "-primarykey";
        static final String PRIMARYKEY_FLAG_DESC = PRIMARYKEY_FLAG;
        static final String INDEX_FLAG = "-index";
        static final String INDEX_FLAG_DESC = INDEX_FLAG + " <name>";
        static final String KEY_PREFIX_FLAG = "-keyprefix";
        static final String KEY_PREFIX_FLAG_DESC = KEY_PREFIX_FLAG + " <size>";

        static final String COMMAND_SYNTAX =
            "table " + COMMAND_NAME + " " + TABLE_NAME_FLAG_DESC +
            " " + JSON_FLAG_DESC + eolt + " [" + NROWS_FLAG_DESC +
            " [[" + PRIMARYKEY_FLAG_DESC + " | " + INDEX_FLAG_DESC  + "] " +
            KEY_PREFIX_FLAG_DESC +  "]+]";

        static final String COMMAND_DESCRIPTION  =
            "Calculates key and data sizes for the specified table using the " +
            "row" + eolt + "input, optionally estimating the NoSQL DB cache " +
            "size required for a" + eolt + "specified number of rows of the " +
            "same format.  Running this command on" + eolt + "multiple sample" +
            " rows can help determine the necessary cache size for" + eolt +
            "desired store performance." + eolt +
            JSON_FLAG + " specifies a sample row used for the " +
            "calculation." + eolt +
            NROWS_FLAG + " specifies the number of rows to use for the cache " +
            "size calculation." + eolt +
            INDEX_FLAG + " or " + PRIMARYKEY_FLAG + " and " + KEY_PREFIX_FLAG +
            " are used to specify the expected" + eolt +
            "commonality of index keys in terms of number of bytes.";

        protected TableSizeSub() {
            super(COMMAND_NAME, 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            String tableName = null;
            String jsonString = null;
            long nRows = 0;
            boolean calcCacheSize = false;
            Map<String, Integer> keyPrefixSizes =
                new HashMap<String, Integer>();
            int primarykeyPrefix = 0;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (TABLE_NAME_FLAG.equals(arg)) {
                    tableName = Shell.nextArg(args, i++, this);
                } else if (NROWS_FLAG.equals(arg)) {
                    nRows = parseLongArg(Shell.nextArg(args, i++, this));
                } else if (JSON_FLAG.equals(arg)) {
                    jsonString = Shell.nextArg(args, i++, this);
                } else if (PRIMARYKEY_FLAG.equals(arg) ||
                           INDEX_FLAG.equals(arg)) {
                    String idxName = null;
                    if (INDEX_FLAG.equals(arg)) {
                        idxName = Shell.nextArg(args, i++, this);
                    }
                    if (++i < args.length) {
                        arg = args[i];
                        if (KEY_PREFIX_FLAG.equals(arg)) {
                            int size = parseIntArg(
                                Shell.nextArg(args, i++, this));
                            if (idxName == null) {
                                primarykeyPrefix = size;
                            } else {
                                keyPrefixSizes.put(idxName, size);
                            }
                        } else {
                            invalidArgument(arg + ", " + KEY_PREFIX_FLAG +
                                            " is reqired");
                        }
                    } else {
                        shell.requiredArg(KEY_PREFIX_FLAG, this);
                    }
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (tableName == null) {
                shell.requiredArg(TABLE_NAME_FLAG, this);
            }
            if (jsonString == null) {
                shell.requiredArg(JSON_FLAG, this);
            }

            if (nRows > 0) {
                calcCacheSize = true;
            }
            final Table table = findTable(tableName, true, shell);
            if (!keyPrefixSizes.isEmpty()) {
                for (Entry<String, Integer> entry: keyPrefixSizes.entrySet()) {
                    String idxName = entry.getKey();
                    if (table.getIndex(entry.getKey()) == null) {
                        throw new ShellException("Index does not exist: " +
                            idxName + " on table: " + tableName);
                    }
                }
            }

            Row row = null;
            try {
                row = table.createRowFromJson(jsonString, false);
            } catch (IllegalArgumentException iae) {
                throw new ShellException(iae.getMessage(), iae);
            }
            /* Check if the primary key fields are not null. */
            validatePrimaryKeyFields(row);

            /* Calculate the primary key sizes, data size and index key sizes.*/
            int primaryKeySize = TableUtils.getKeySize(row);
            int dataSize = TableUtils.getDataSize(row);
            Map<String, Integer> indexKeySizes = calcIndexesKeySize(row);

            /* Calculate the DbCacheSize. */
            Map<String, Properties> cacheInfoMap = null;
            if (calcCacheSize) {
                String[] jeParams = getJEConfigParams();
                cacheInfoMap = calcDbCacheSize((TableImpl)table, nRows,
                                               primaryKeySize, dataSize,
                                               primarykeyPrefix, indexKeySizes,
                                               keyPrefixSizes, jeParams);
            }

            /* Generate the output information. */
            StringBuilder sb = new StringBuilder();
            sb.append(eol);
            sb.append(genKeySizesInfo(primaryKeySize, dataSize, indexKeySizes));
            if (cacheInfoMap != null) {
                sb.append(eol);
                sb.append(genCacheSizeInfo(cacheInfoMap));
            }
            return sb.toString();
        }

        private void validatePrimaryKeyFields(Row row)
            throws ShellException {

            List<String> pkFields = row.createPrimaryKey().getFields();
            for (String field: pkFields) {
                FieldValue fv = row.get(field);
                if (fv == null || fv.isNull()) {
                    throw new ShellException("Primary key field cannot " +
                                             "be null: " + field);
                }
            }
        }

        private long parseLongArg(String arg)
            throws ShellException {

            long value = parseLong(arg);
            if (value <= 0) {
                invalidArgument(arg);
            }
            return value;
        }

        private int parseIntArg(String arg)
            throws ShellException {

            int value = parseInt(arg);
            if (value <= 0) {
                invalidArgument(arg);
            }
            return value;
        }

        private Map<String, Integer> calcIndexesKeySize(Row row)
            throws ShellException {

            Map<String, Index> indexes =
                row.getTable().getIndexes(Index.IndexType.SECONDARY);
            if (indexes == null || indexes.isEmpty()) {
                return null;
            }
            Map<String, Integer> sizeMap = new LinkedHashMap<String, Integer>();
            for (Entry<String, Index> entry: indexes.entrySet()) {
                IndexKey key;
                try {
                    key = entry.getValue().createIndexKey(row);
                } catch (IllegalArgumentException iae) {
                    throw new ShellException("Failed to create index key " +
                                             "for " + entry.getKey() + ": " +
                                             iae.getMessage(), iae);
                }
                sizeMap.put(entry.getKey(), TableUtils.getKeySize(key));
            }
            return sizeMap;
        }

        private Map<String, Properties> calcDbCacheSize(TableImpl table,
                                  long nRows, int primaryKeySize,
                                  int dataSize, int primaryKeyPrefix,
                                  Map<String, Integer> indexKeySizes,
                                  Map<String, Integer> indexKeyPrefixSizes,
                                  String[] jeParams)
            throws ShellException {

            Map<String, Properties> map =
                new LinkedHashMap<String, Properties>();

            /**
             * Calculate the DbCacheSize for primary db:
             * Primary db key prefix = length of table id string + 1.
             *  - table id: the common key prefix of a table.
             *  - Additional 1 byte: key component delimiter that system added.
             */
            primaryKeyPrefix += table.getIdString().length() + 1;
            Properties props = runDbCacheSize(nRows, primaryKeySize, dataSize,
                                              primaryKeyPrefix, jeParams, null);
            map.put("Table", props);
            if (indexKeySizes == null) {
                return map;
            }

            /* Calculate the DbCacheSize for indexes. */
            for (Entry<String, Index> entry:
                table.getIndexes(Index.IndexType.SECONDARY).entrySet()) {
            	
                final String indexName = entry.getKey();
                final int keySize = indexKeySizes.get(indexName);
                int keyPrefixSize = 0;
                if (indexKeyPrefixSizes != null &&
                    indexKeyPrefixSizes.containsKey(indexName)) {
                    keyPrefixSize = indexKeyPrefixSizes.get(indexName);
                }
                props = runDbCacheSize(nRows, keySize, primaryKeySize,
                                       keyPrefixSize, jeParams,
                                       new String[] {"-duplicates"});
                map.put(indexName, props);
            }
            return map;
        }

        private Properties runDbCacheSize(long nRows, int keySize,
                                          int dataSize, int keyPrefixSize,
                                          String[] jeParams,
                                          String[] otherParams)
            throws ShellException {

            String[] args = getDbCacheSizeArgs(keySize, dataSize,
                                               keyPrefixSize, nRows,
                                               jeParams, otherParams);
            /**
             * Currently, just call DbCacheSize.main() to perform
             * calculation on required cache size, then parse the output
             * PrintStream to get the result.
             */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(ps);
            try {
                DbCacheSize.main(args);
            } catch (Throwable e) {
                throw new ShellException(e.getMessage());
            }
            String ret = baos.toString();
            System.out.flush();
            System.setOut(old);
            Properties props = null;
            if (ret != null) {
                props = new Properties();
                try {
                    props.load(new StringReader(ret));
                } catch (IOException e) {
                    throw new ShellException(e.getMessage(), e);
                }
            }
            return props;
        }

        /* Get the arguments for DbCacheSize. */
        private String[] getDbCacheSizeArgs(int keySize, int dataSize,
                                            int keyPerfixsize, long nRows,
                                            String[] jeParams,
                                            String[] otherParams) {

            String[] basicArgs = new String[] {
                "-records", String.valueOf(nRows),
                "-key", String.valueOf(keySize),
                "-data", String.valueOf(dataSize),
                "-keyprefix", String.valueOf(keyPerfixsize),
                "-outputproperties",
                "-replicated"
            };
            int nArgs = basicArgs.length;
            if (jeParams != null) {
                nArgs += jeParams.length;
            }
            if (otherParams != null) {
                nArgs += otherParams.length;
            }

            String[] args = null;
            if (nArgs > basicArgs.length) {
                args = new String[nArgs];
                int i = 0;
                System.arraycopy(basicArgs, 0, args, i, basicArgs.length);
                i += basicArgs.length;
                if (jeParams != null) {
                    System.arraycopy(jeParams, 0, args, i, jeParams.length);
                    i += jeParams.length;
                }
                if (otherParams != null) {
                    System.arraycopy(otherParams, 0, args, i,
                                     otherParams.length);
                }
            } else {
                args = basicArgs;
            }
            return args;
        }

        /* Get all JE configuration parameters used in kvstore. */
        private String[] getJEConfigParams() {
            ParameterUtils pu = new ParameterUtils(new ParameterMap());
            Properties props = pu.getRNRepEnvConfig().getProps();
            if (props == null || props.isEmpty()) {
                return null;
            }
            String[] jeParams = new String[props.size() * 2];
            Enumeration<?> e = props.propertyNames();
            int i = 0;
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                jeParams[i++] = "-" + key;
                jeParams[i++] = props.getProperty(key);
            }
            return jeParams;
        }

        /* Generate the output of key average sizes information. */
        private String genKeySizesInfo(int keySize, int dataSize,
                                       Map<String, Integer> indexSizes) {

            String title = "Key and Data Size";
            Map<String, Integer> map = new LinkedHashMap<String, Integer>();
            map.put("Primary Key", keySize);
            map.put("Data", dataSize);
            if (indexSizes != null) {
                for (Entry<String, Integer> entry: indexSizes.entrySet()) {
                    map.put("Index Key of " + entry.getKey(), entry.getValue());
                }
            }
            return formatKeySizesInfo(title, map);
        }

        /* Format the output for sizes information. */
        private String formatKeySizesInfo(String title,
                                          Map<String, Integer> sizes) {

            StringBuilder sb = new StringBuilder();
            sb.append(formatTableTitle(title));
            sb.append(formatKeySizesTable(sizes));
            return sb.toString();
        }

        private String formatKeySizesTable(Map<String, Integer> sizes) {
            Column colName = new Column("Name");
            Column colSize = new Column("Number of Bytes");
            colName.appendGroupDelimiter();
            colSize.appendGroupDelimiter();
            for (Entry<String, Integer> entry: sizes.entrySet()) {
                /* Object name */
                colName.appendData(entry.getKey());
                /* Number of bytes */
                colSize.appendData(entry.getValue().toString(),
                                      Column.Align.CENTER);
            }
            return formatColumnsOutput(new Column[]{colName, colSize}, "  ");
        }

        /* Generate the output of DbCacheSize information. */
        private String genCacheSizeInfo(Map<String, Properties> cacheSizesMap)
            throws ShellException {

            final String titleCacheOverhead = "Environment Cache Overhead";
            final String titleCacheSize = "Database Cache Sizes";
            final String footCacheSize =
                "For further information see the DbCacheSize javadoc.";
            final String propNames[] = new String[] {
                "internalNodes",
                "internalNodesAndVersions",
                "allNodes"
            };
            final String descriptions[] = new String [] {
                "Internal nodes only",
                "Internal nodes and record versions",
                "Internal nodes and leaf nodes"
            };
            final String columnNames[] = new String[] {
                "Name", "Number of Bytes", "Description"
            };
            final String propOverhead = "overhead";
            final String totalName = "Total";
            final NumberFormat INT_FORMAT = NumberFormat.getIntegerInstance();

            int nValues = propNames.length;
            long sum[] = new long[nValues];
            long cacheOverhead = 0;
            Map<String, LinkedHashMap<String, String>> result =
                new LinkedHashMap<String, LinkedHashMap<String, String>>();
            for (Entry<String, Properties> info: cacheSizesMap.entrySet()) {
                Properties props = info.getValue();
                if (cacheOverhead == 0) {
                    cacheOverhead = getPropLongValue(props, propOverhead);
                }
                LinkedHashMap<String, String> map =
                    new LinkedHashMap<String, String>(nValues);
                long value[] = new long[nValues];
                for (int i = 0; i < value.length; i++) {
                    value[i] = getPropLongValue(props, propNames[i]);
                    sum[i] += value[i];
                    map.put(descriptions[i], INT_FORMAT.format(value[i]));
                }
                result.put(info.getKey(), map);
            }
            /* Added sum information to result. */
            LinkedHashMap<String, String> map =
                new LinkedHashMap<String, String>(nValues);
            for (int i = 0; i < nValues; i++) {
                map.put(descriptions[i], INT_FORMAT.format(sum[i]));
            }
            result.put(totalName, map);

            /* Generate cache overhead information */
            String output = formatCacheOverHeadInfo(titleCacheOverhead,
                String.format("%s minimum bytes",
                              INT_FORMAT.format(cacheOverhead)));

            /* Generate cache size information */
            output += formatCacheSizeInfo(titleCacheSize, columnNames,
                                          result, footCacheSize);
            return output;
        }

        private long getPropLongValue(Properties props, String propName)
            throws ShellException {

            String value = props.getProperty(propName);
            if (value == null) {
                return 0;
            }
            try {
                return Long.parseLong(props.getProperty(propName));
            } catch (NumberFormatException nfe) {
                throw new ShellException("Value of property '" + propName +
                    "' is not long value: " + value);
            }
        }

        /* Format cache overhead information. */
        private String formatCacheOverHeadInfo(String title, String subHead) {

            StringBuilder sb = new StringBuilder();
            sb.append(formatTableTitle(title));
            if (subHead != null) {
                sb.append(subHead);
                sb.append(eol);
                sb.append(eol);
            }
            return sb.toString();
        }

        /* Format cache sizes information. */
        private String formatCacheSizeInfo(String title, String columnNames[],
                Map<String, LinkedHashMap<String, String>> result,
                String foot) {

            StringBuilder sb = new StringBuilder();
            sb.append(formatTableTitle(title));
            if (result != null) {
                sb.append(formatCacheSizeTable(columnNames, result));
            }
            if (foot != null) {
                sb.append(eol);
                sb.append(foot);
                sb.append(eol);
            }
            return sb.toString();
        }

        private String formatCacheSizeTable(String[] headers,
                Map<String, LinkedHashMap<String, String>> result) {

            Column[] columns = new Column[headers.length];
            for (int i = 0; i < columns.length ; i++) {
                columns[i] = new Column(headers[i]);
                columns[i].appendGroupDelimiter();
            }

            int i = 0;
            for (Entry<String, LinkedHashMap<String, String>> entry:
                 result.entrySet()) {

                String key = entry.getKey();
                HashMap<String, String> map = entry.getValue();
                boolean appendDelimiter = false;
                if (++i < result.size()) {
                    appendDelimiter = true;
                }

                /* Object name */
                Column column = columns[0];
                for (int j = 0; j < map.size(); j++) {
                    if (j == map.size()/2) {
                        column.appendData(key);
                    } else {
                        column.appendData("");
                    }
                }
                if (appendDelimiter) {
                    column.appendGroupDelimiter();
                }

                /* Number of bytes */
                column = columns[1];
                for (String text: map.values()) {
                    column.appendData(text, Column.Align.RIGHT);
                }
                if (appendDelimiter) {
                    column.appendGroupDelimiter();
                }

                /* Description */
                column = columns[2];
                for (String text: map.keySet()) {
                    column.appendData(text);
                }
                if (appendDelimiter) {
                    column.appendGroupDelimiter();
                }
            }
            return formatColumnsOutput(columns, "  ");
        }

        private String formatTableTitle(String title) {
            final String appending = "===";
            return String.format("%s %s %s" + eol + eol,
                                 appending, title, appending);
        }

        private String formatColumnsOutput(Column columns[],
                                           final String separator) {
            StringBuilder sb = new StringBuilder();
            int nRows = columns[0].getSize();
            for (int i = 0; i < nRows; i++) {
                for (Column col: columns) {
                    sb.append(col.getFormatedText(i));
                    sb.append(separator);
                }
                sb.append(eol);
            }
            return sb.toString();
        }

        /**
         * Class Column is to store elements of a column in table.
         * For each cell in a column, it contains the text and alignment
         * information.
         */
        private static class Column{
            private final static char GROURP_SEP = '-';
            private final List<Cell> cells;
            private int width;
            private final char separator;

            static enum Align{
                LEFT,
                RIGHT,
                CENTER
            }
            static class Cell {
                static final Align ALIGN_DEF = Align.LEFT;
                private final String text;
                private final Align align;
                private final boolean isGroupSep;

                Cell(String text, Align align) {
                    this(text, align, false);
                }

                Cell(String text, Align align, boolean isGroupSep) {
                    this.text = text;
                    this.align = ((align == null)? ALIGN_DEF : align);
                    this.isGroupSep = isGroupSep;
                }

                String getText() {
                    return text;
                }

                Align getAlign() {
                    return align;
                }

                boolean isGroupSepartor() {
                    return isGroupSep;
                }
            }

            Column(String title) {
                this(title, GROURP_SEP);
            }

            Column(String title, char groupSeparator) {
                cells = new ArrayList<Cell>();
                width = 0;
                appendData(title, Align.CENTER);
                separator = groupSeparator;
            }

            void appendData(String text) {
                appendData(text, null);
            }

            void appendData(String text, Align align) {
                if (text == null) {
                    text = "";
                }
                cells.add(new Cell(text, align));
                if (text.length() > width) {
                    width = text.length();
                }
            }

            void appendGroupDelimiter() {
                cells.add(new Cell("", null, true));
            }

            int getSize() {
                return cells.size();
            }

            String getFormatedText(int index) {
                if (index < 0 || index >= cells.size()) {
                    return "";
                }

                Cell cell = cells.get(index);
                String text = cell.getText();
                switch (cell.getAlign()) {
                case LEFT:
                    text = padRight(text, width);
                    break;
                case RIGHT:
                    text = padLeft(text, width);
                    break;
                case CENTER:
                    int padding = width - text.length();
                    if (padding <= 0) {
                        return text;
                    }
                    int left = padding / 2 + text.length();
                    text = padRight(padLeft(text, left), width);
                    break;
                default:
                    break;
                }
                if (cell.isGroupSepartor()) {
                    return text.replace(' ', separator);
                }
                return text;
            }

            private String padLeft(String s, int n) {
                return String.format("%1$" + n + "s", s);
            }

            private String padRight(String s, int n) {
                return String.format("%1$-" + n + "s", s);
            }
        }

        @Override
        protected String getCommandSyntax() {
            return COMMAND_SYNTAX;
        }

        @Override
        protected String getCommandDescription() {
            return COMMAND_DESCRIPTION;
        }
    }

    private static Table findTable(String tableName,
                                   boolean mustExist,
                                   Shell shell)
        throws ShellException {

        final CommandShell cmd = (CommandShell) shell;
        final CommandServiceAPI cs = cmd.getAdmin();
        TableMetadata meta = null;
        try {
            meta = cs.getMetadata(TableMetadata.class,
                                  MetadataType.TABLE);
            if (meta != null) {
                return meta.getTable(tableName, mustExist);
            }
        } catch (RemoteException re) {
            cmd.noAdmin(re);
        } catch (IllegalStateException ise) {
            throw new ShellException(ise.getMessage(), ise);
        } catch (IllegalArgumentException iae) {
            throw new ShellException(iae.getMessage(), iae);
        }
        if (mustExist) {
            throw new ShellException("Table does not exist: " +
                     makeTableFullName(null, tableName));
        }
        return null;
    }

    private static Object getCurrentCmdVariable(Shell shell, String varName) {
        return shell.getCurrentCommand().getVariable(varName);
    }

    private static String makeTableFullName(TableBuilderBase tb) {
        String tableName = null;
        String parentName = null;

        if (tb instanceof TableBuilder) {
            tableName = ((TableBuilder)tb).getName();
            if (((TableBuilder)tb).getParent() != null) {
                parentName = ((TableBuilder)tb).getParent().getFullName();
            }
            return makeTableFullName(tableName, parentName);
        } else if (tb instanceof TableEvolver) {
            return ((TableEvolver)tb).getTable().getFullName();
        }
        return null;
    }

    private static String makeTableFullName(String name,
                                            String tableName) {
        return TableMetadata.makeQualifiedName(name, tableName);
    }
}
