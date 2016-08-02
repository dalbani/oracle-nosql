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
package oracle.kv.shell;

import java.rmi.RemoteException;
import java.util.StringTokenizer;

import oracle.kv.FaultException;
import oracle.kv.StatementResult;
import oracle.kv.impl.admin.client.CommandShell;
import oracle.kv.impl.client.DdlJsonFormat;
import oracle.kv.util.shell.Shell;
import oracle.kv.util.shell.ShellCommand;
import oracle.kv.util.shell.ShellException;
import oracle.kv.util.shell.ShellUsageException;

/**
 * Implements the 'execute <ddl statement>' command.
 */
public class ExecuteCommand extends ShellCommand {

    private final static String GRANT_USAGE =
        "To grant roles to a user or a role, use the syntax of:" + eol +
        "GRANT role_name (,role_name)* TO (USER user_name | " +
        "ROLE role_name)" + eol + eol +
        "To grant system privileges to a role, use the syntax of:" + eol +
        "GRANT (system_privilege | ALL PRIVILEGES) " +
        "(,(system_privilege | ALL PRIVILEGES))* TO role_name" + eol + eol +
        "To grant object privileges to a role, use the syntax of:" + eol +
        "GRANT (object_privilege | ALL [PRIVILEGES]) " +
        "(,(object_privilege | ALL [PRIVILEGES]))* ON object TO role_name";

    private final static String REVOKE_USAGE =
        "To revoke roles from a user or a role, use the syntax of:" + eol +
        "REVOKE role_name (,role_name)* FROM (USER user_name | " +
        "ROLE role_name)" + eol + eol +
        "To revoke system privileges from a role, use the syntax of:" + eol +
        "REVOKE (system_privilege | ALL PRIVILEGES) " +
        "(,(system_privilege | ALL PRIVILEGES))* FROM role_name" + eol + eol +
        "To revoke object privileges from a role, use the syntax of:" + eol +
        "REVOKE (object_privilege | ALL [PRIVILEGES]) " +
        "(,(object_privilege | ALL [PRIVILEGES]))* ON object FROM role_name";

    /**
     * Prefix matching of 4 characters means 'execute' and 'exec' are supported.
     */
    public ExecuteCommand() {
        super("execute", 4);
    }

    /**
     * Usage: execute <ddl statement>
     */
    @Override
    public String execute(String[] args, Shell shell)
        throws ShellException {

        if (args.length != 2) {
            shell.badArgCount(this);
        }

        final String ddlStatement = args[1];

        /* Statement is empty */
        if (ddlStatement.length() == 0) {
            throw new ShellUsageException("Empty statement",  this);
        }

        CommandShell cmd = (CommandShell) shell;
        StatementResult result = null;
        try {
           result = cmd.getStore().executeSync(ddlStatement);
           return displayResults(result);
        } catch (IllegalArgumentException iae) {
            final StringBuilder sb = new StringBuilder();
            sb.append(iae.getMessage());
            sb.append("\nUsage:\n\n");
            sb.append(getUsage(ddlStatement)).append("\n");
            return sb.toString();
        } catch (FaultException e) {
            if (e.getCause() != null &&
                e.getCause().getClass().equals(RemoteException.class)) {
                RemoteException re = (RemoteException) e.getCause();
                cmd.noAdmin(re);
                return "failed";
            }
            return e.getMessage();
        }
    }

    /**
     * This is just a stopgap because currently, the ddl parser emits fairly
     * cryptic error messages. Since generating sensible error messages
     * is not a trivial task, this method merely supplies usage information
     * by applying a simple guess as to what the statement is.
     *
     * TODO: this should come from the TableDDL.java.
     */
    private String getUsage(String ddlStatement) {

        final StringTokenizer t = new StringTokenizer(ddlStatement);
        final String keyword = t.nextToken();

        if (keyword.equalsIgnoreCase("create")) {
            String secondWord = null;
            if (t.hasMoreTokens()) {
               secondWord = t.nextToken();
            }

            if ("index".equalsIgnoreCase(secondWord)) {
                return "CREATE INDEX [IF NOT EXISTS]\n" +
                     "     index_name on table_name (fieldName [,fieldName]*)";
            } else if ("user".equalsIgnoreCase(secondWord)) {
                return "CREATE USER" +
                    " (user_name IDENTIFIED (BY password | EXTERNALLY) \n" +
                    "  [PASSWORD EXPIRE] [PASSWORD LIFETIME duration] \n" +
                    "  [ACCOUNT LOCK|UNLOCK] [ADMIN]";
            } else if ("role".equalsIgnoreCase(secondWord)) {
                return "CREATE ROLE role_name";
            }

            return "CREATE TABLE [IF NOT EXISTS] table_name (\n" +
                    "  (table_definition (,table_definition)*),\n" +
                    "   primary_key_definition";
        } else if (keyword.equalsIgnoreCase("drop")) {
            String secondWord = null;
            if (t.hasMoreTokens()) {
               secondWord = t.nextToken();
            }

            if ("index".equalsIgnoreCase(secondWord)) {
                return "DROP INDEX [IF EXISTS] index_name ON table_name";
            } else if ("user".equalsIgnoreCase(secondWord)) {
                return "DROP USER user_name";
            } else if ("role".equalsIgnoreCase(secondWord)) {
                return "DROP ROLE role_name";
            }

            return "DROP TABLE [IF EXISTS] table_name";
        } else if (keyword.equalsIgnoreCase("alter")) {
            String secondWord = null;
            if (t.hasMoreTokens()) {
               secondWord = t.nextToken();
            }

            if ("user".equalsIgnoreCase(secondWord)) {
                return "ALTER USER user_name\n" +
                    "  [IDENTIFIED BY password [RETAIN CURRENT PASSWORD]]\n" +
                    "  [CLEAR RETAINED PASSWORD] [PASSWORD EXPIRE]\n" +
                    "  [PASSWORD LIFETIME duration] [ACCOUNT UNLOCK|LOCK]";
            }

            return "ALTER TABLE table_name (\n" +
                   "  ADD field_name field_type\n" +
                   "| DROP field_name)";
        } else if (keyword.equalsIgnoreCase("grant")) {
            return GRANT_USAGE;
        } else if (keyword.equalsIgnoreCase("revoke")) {
            return REVOKE_USAGE;
        } else if (keyword.equalsIgnoreCase("show")) {
            return "SHOW [AS JSON]\n" +
                   "  TABLES\n" +
                   "  USERS\n" +
                   "  ROLES\n" +
                   "  | TABLE table_name\n" +
                   "  | USER user_name\n" +
                   "  | ROLE role_name\n" +
                   "  | INDEXES ON table_name";
        } else if (keyword.equalsIgnoreCase("describe") ||
                   keyword.equalsIgnoreCase("desc")) {
            return "(DESCRIBE|DESC) [AS JSON]\n" +
                    "  TABLE table_name (field_name (,field_name)*)?\n" +
                    "  | INDEX index_name ON table_name";
        } else {
            return "Unknown statement";
        }
    }

    /**
     * Display the result depending on its type and outcome. Operations that
     * ended in error show their error messages and status info, operations
     * like 'describe' and 'show' display their results, and operations that
     * generate plan execution return status info.
     */
    private String displayResults(StatementResult result) {
        if (result.getErrorMessage() != null) {
            return result.getErrorMessage() + "\n" + result.getInfo();
        }

        if (!result.isDone()) {
            return "Statement did not complete.";
        }

        /*
         * Statement was successful, output for describes, shows, and noop
         * should be displayed.
         */
        if (result.isSuccessful()) {
            if (result.getResult() != null) {
                return result.getResult();
            }
            if (result.getInfo().equals(DdlJsonFormat.NOOP_STATUS)) {
                return result.getInfo();
            }
            return "Statement completed successfully";
        }

        /* 
         * For now, all info is returned as unstructured text. In the future,
         * when the execute option can specify json or not, we may return
         * status info in json form.
         */
        return "Statement did not complete successfully:\n" +
            result.getInfo();
    }

    @Override
    protected String getCommandSyntax() {
        return name + " <statement>";
    }

    @Override
    protected String getCommandDescription() {
        return "Executes the specified statement synchronously. The statement"+
            eolt + "must be enclosed in single or double quotes.";
    }
}
