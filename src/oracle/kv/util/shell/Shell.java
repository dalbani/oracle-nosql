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

package oracle.kv.util.shell;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import oracle.kv.KVSecurityException;
import oracle.kv.impl.admin.CommandJsonUtils;
import oracle.kv.impl.admin.CommandResult;
import oracle.kv.util.ErrorMessage;

/**
 *
 * Simple framework for a command line shell.  See CommandShell.java for a
 * concrete implementation.
 */
public abstract class Shell {
    protected final InputStream input;
    protected final PrintStream output;
    private ShellInputReader inputReader = null;
    protected final CommandHistory history;
    protected int exitCode;
    protected boolean showHidden = false;
    protected boolean showDeprecated = false;
    private VariablesMap shellVariables = null;
    protected Stack<ShellCommand> stCurrentCommands = null;
    protected boolean isSecured = false;
    private final Timer timer;

    public final static String tab = "\t";
    public final static String eol = System.getProperty("line.separator");
    public final static String eolt = eol + tab;

    public static final String INCLUDE_DEPRECATED_FLAG = "-include-deprecated";

    /*
     * These are somewhat standard exit codes from sysexits.h
     */
    public final static int EXIT_OK = 0;
    public final static int EXIT_USAGE = 64; /* usage */
    public final static int EXIT_INPUTERR = 65; /* bad argument */
    public final static int EXIT_UNKNOWN = 1; /* unknown exception */
    public final static int EXIT_NOPERM = 77; /* permission denied */

    /*
     * This variable changes per-command which means that things must be
     * single-threaded, which they are at this time.  The command line
     * parsing consumes any "-verbose" flag and sets this variable for
     * access by commands.
     */
    private boolean verbose = false;

    /*
     * Set to true if the hidden -debug flag is specified, and causes printing
     * of debugging output (stacktraces).  This field also changes per-command,
     * like the verbose field.
     */
    private boolean debug = false;

    /*
     * This variable is toggled by the "verbose" command
     */
    private boolean global_verbose = false;

    /*
     * This variable is set using "timer [on | off]" command, if it is true
     * then the elapsed time of execution of command is measured and printed
     * out.
     */
    private boolean timing = false;

    /*
     * This is used to terminate the interactive loop
     */
    private boolean terminate = false;

    /*
     * This is used to change the output from Shell into json format strings.
     * Once it is set to true, the output of command will also be in JSON
     * format if the command supports JSON format result.
     */
    protected boolean shellJson = false;

    /*
     * This variable changes per-command which turns the output of command
     * execution into json format strings if the command supports JSON format
     * result.
     */
    private boolean json = false;

    /*
     * This is the ending character of command line.
     */
    private final static char LINE_TERMINATOR = ';';

    /*
     * Line continuation character
     */
    private final static char LINE_JOINER = '\\';

    /*
     * These must be implemented by specific shell classes
     */
    public abstract List<? extends ShellCommand> getCommands();

    public abstract String getPrompt();

    public abstract String getUsageHeader();

    public abstract void init();

    public abstract void shutdown();

    /*
     * Concrete implementation
     */
    public Shell(InputStream input, PrintStream output) {
        this.input = input;
        this.output = output;
        history = new CommandHistory();
        stCurrentCommands = new Stack<ShellCommand>();
        shellVariables = new VariablesMap();
        timer = new Timer();
    }

    public String getUsage() {
        String usage = getUsageHeader();
        for (ShellCommand cmd : getCommands()) {
            if ((!showHidden() && cmd.isHidden()) ||
                (!showDeprecated() && cmd.isDeprecated())) {
                continue;
            }

            String help = cmd.getCommandName();
            if (help != null) {
                usage += tab + help + eol;
            }
        }
        return usage;
    }

    public void setShowDeprecated(boolean showDeprecated) {
        this.showDeprecated = showDeprecated;
    }

    public boolean showDeprecated() {
        return showDeprecated;
    }

    public boolean showHidden() {
        return showHidden;
    }

    protected boolean toggleHidden() {
        showHidden = !showHidden;
        return showHidden;
    }

    protected void setHidden(boolean val) {
        showHidden = val;
    }

    public void prompt() {
        String prompt = getPrompt();
        if (prompt != null) {
            output.print(prompt);
        }
    }

    /* Push a current command to stack. */
    public void pushCurrentCommand(ShellCommand command) {
        stCurrentCommands.push(command);
    }

    /* Get the current command on top of stack. */
    public ShellCommand getCurrentCommand() {
        if (stCurrentCommands.size() > 0) {
            return stCurrentCommands.peek();
        }
        return null;
    }

    /* Pop the current command on the top of stack. */
    public void popCurrentCommand() {
        stCurrentCommands.pop();
    }

    /* Return the customized propmt string of the current command. */
    public String getCurrentCommandPropmt() {
        ShellCommand command = getCurrentCommand();
        if (command == null) {
            return null;
        }

        Iterator<ShellCommand> it = stCurrentCommands.iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            ShellCommand cmd = it.next();
            if (cmd.getPrompt() != null) {
                if (sb.length() > 0) {
                    sb.append(".");
                }
                sb.append(cmd.getPrompt());
            }
        }
        if (sb.length() > 0) {
            sb.append("-> ");
        }
        return sb.toString();
    }

    /* Store a variable. */
    public void addVariable(String name, Object value) {
        shellVariables.add(name, value);
    }

    /* Get the value of a variable. */
    public Object getVariable(String name) {
        return shellVariables.get(name);
    }

    /* Get all variables. */
    public Set<Entry<String, Object>> getAllVariables() {
        return shellVariables.getAll();
    }

    /* Remove a variable. */
    public void removeVariable(String name) {
        shellVariables.remove(name);
    }

    /* Remove all variables. */
    public void removeAllVariables() {
        shellVariables.reset();
    }

    public boolean doRetry() {
        return false;
    }

    public boolean handleShellException(String line, ShellException se) {
        /* do one retry */
        if (doRetry()) {
            return true;
        }

        final CommandResult cmdResult = se.getCommandResult();
        if (se instanceof ShellHelpException) {
            history.add(line, null);
            displayResultReport(line, cmdResult,
                ((ShellHelpException) se).getVerboseHelpMessage());
            exitCode = EXIT_USAGE;
            return false;
        } else if (se instanceof ShellUsageException) {
            history.add(line, null);
            ShellUsageException sue = (ShellUsageException) se;
            displayResultReport(line, cmdResult,
                                sue.getMessage() +
                                System.getProperty("line.separator") +
                                sue.getVerboseHelpMessage());
            exitCode = EXIT_USAGE;
            return false;
        } else if (se instanceof ShellArgumentException) {
            history.add(line, null);
            displayResultReport(line, cmdResult, se.getMessage());
            exitCode = EXIT_INPUTERR;
            return false;
        }

        history.add(line, se);
        String message = "Error handling command " + line + ": " +
                         se.getMessage();
        exitCode = EXIT_UNKNOWN;
        displayResultReport(line, cmdResult, message);
        if (debug) {
            se.printStackTrace(output);
        }
        return false;
    }

    public void handleUnknownException(String line, Exception e) {
        history.add(line, e);
        exitCode = EXIT_UNKNOWN;
        final CommandResult cmdResult =
            new CommandResult.CommandFails(e.getMessage(),
                                           ErrorMessage.NOSQL_5500,
                                           CommandResult.NO_CLEANUP_JOBS);
        displayResultReport(line, cmdResult,
                            "Unknown Exception: " + e.getClass());
        if (debug) {
            e.printStackTrace(output);
        }
    }

    /**
     * General handler of KVSecurityException. The default behavior is to log
     * the command and output error messages.
     *
     * @param line command line
     * @param kvse instance of AuthenticationRequiredException
     * @return true only if a retry is intentional
     */
    public boolean
        handleKVSecurityException(String line,
                                  KVSecurityException kvse) {
        history.add(line, kvse);
        final CommandResult cmdResult =
                new CommandResult.CommandFails(kvse.getMessage(),
                                               ErrorMessage.NOSQL_5100,
                                               CommandResult.NO_CLEANUP_JOBS);
        displayResultReport(line, cmdResult,
                            "Error handling command " + line + ": " +
                                kvse.getMessage());
        if (debug) {
            kvse.printStackTrace(output);
        }
        exitCode = EXIT_NOPERM;
        return false;
    }

    public void verboseOutput(String msg) {
        if (verbose || global_verbose) {
            output.println(msg);
        }
    }

    public void setTerminate() {
        terminate = true;
    }

    public boolean getTerminate() {
        return terminate;
    }

    /*
     * The primary loop that reads lines and dispatches them to the appropriate
     * command.
     */
    public void loop() {
        try {
            /* initialize input reader */
            inputReader = new ShellInputReader(this);
            inputReader.setDefaultPrompt(getPrompt());
            final CommandLinesParser clp = new CommandLinesParser(this);

            LoopUntilTerminate:
            while (!terminate) {
                final String promptDef = inputReader.getDefaultPrompt();
                boolean multiLineInput = false;

                do {
                    String prompt = getCurrentCommandPropmt();
                    if (multiLineInput) {
                        final int len = (prompt != null) ?
                            prompt.length() : promptDef.length();
                        prompt = String.format("%" + len + "s", "-> ");
                    }

                    final String line;
                    try {
                        line = inputReader.readLine(prompt);
                    } catch (IOException ioe) {
                        echo("Exception reading input: " + ioe + Shell.eol);
                        continue;
                    }

                    /*
                     * If read empty line (enter Ctrl-D), then terminate the
                     * loop or multi-line input mode.
                     */
                    if (line == null) {
                        if (multiLineInput) {
                            clp.reset();
                            output.println();
                            break;
                        }
                        break LoopUntilTerminate;
                    }

                    try {
                        clp.appendLine(line);
                    } catch (Exception e) {
                        final String[] commands = clp.getCommands();
                        assert(commands.length == 1);
                        handleExecuteException(commands[0], e);
                        clp.reset();
                        break;
                    }
                    if (!multiLineInput) {
                        multiLineInput = true;
                    }
                } while (!clp.complete());

                /* Execute command(s) */
                String[] commands = clp.getCommands();
                if (commands != null) {
                    for (String command: commands) {
                        execute(command);
                    }
                }
                clp.reset();
            }
        } finally {
            inputReader.shutdown();
            shutdown();
        }
    }

    public void println(String msg) {
        output.println(msg);
    }

    /*
     * Encapsulates runLine in try/catch blocks for calls from external tools
     * that construct Shell directly.  This is also used by loop().  This
     * function trims leading/trailing white space from the line.
     *
     * @param line The input command line to execute
     */
    public void execute(String line) {
        line = line.trim();
        if (line.length() == 0) {
            return;
        }
        try {
            runLine(line);
        } catch (Exception e) {
            handleExecuteException(line, e);
        }
    }

    private void handleExecuteException(String command, Exception e) {
        try {
            if (e instanceof KVSecurityException) {
                final KVSecurityException kvse = (KVSecurityException)e;
                /* Returns true to give a chance to retry the command once. */
                if (handleKVSecurityException(command, kvse)) {
                    runLine(command);
                }
            } else {
                throw e;
            }
        } catch (ShellException se) {
            /* Returns true if a retry is in order */
            if (handleShellException(command, (ShellException)e)) {
                execute(command);
            }
        } catch (Exception ex) {
            handleUnknownException(command, ex);
        }
    }

    public ShellCommand findCommand(String commandName) {
        for (ShellCommand command : getCommands()) {
            if (command.matches(commandName)) {
                return command;
            }
        }
        return null;
    }

    /*
     * Extract the named flag.  The flag must exist in the args
     */
    public static String[] extractArg(String[] args, String arg) {
        String[] retArgs = new String[args.length - 1];
        int i = 0;
        for (String s : args) {
            if (! arg.equals(s)) {
                retArgs[i++] = s;
            }
        }
        return retArgs;
    }

    private String[] checkVerboseAndDebug(String[] args) {
        verbose = false;
        debug = false;
        String[] retArgs = args;
        if (checkArg(args, "-verbose")) {
            verbose = true;
            retArgs = extractArg(args, "-verbose");
        }
        if (checkArg(args, "-debug")) {
            debug = true;
            retArgs = extractArg(args, "-debug");
        }
        return retArgs;
    }

    private String[] checkJson(String[] args) {
        json = false;
        String[] retArgs = args;
        if (checkArg(args, "-json")) {
            json = true;
            retArgs = extractArg(args, "-json");
        }
        return retArgs;
    }

    /*
     * Parse a single line.  Treat "#" as comments
     */
    public String[] parseLine(String line) {
        return parseLine(line, false);
    }

    /*
     * Parse a single line.  Treat "#" as comments
     *
     * @param line The input line to parse
     * @param checkQuotesMatch Indicates if check the quotes occur in pair.
     *        If the quote character is checked to not occur in pair, then
     *        throw an exception.
     *
     * @return the array of string tokens
     */
    private String[] parseLine(String line, boolean checkQuotesMatch) {
        int tokenType;
        List<String> words = new ArrayList<String>();
        StreamTokenizer st = new StreamTokenizer(new StringReader(line));

        st.resetSyntax();
        st.whitespaceChars(0, ' ');
        st.wordChars('!', 255);
        st.quoteChar('"');
        st.quoteChar('\'');
        st.commentChar('#');

        while (true) {
            try {
                tokenType = st.nextToken();
                if (tokenType == StreamTokenizer.TT_WORD) {
                    words.add(st.sval);
                } else if (tokenType == '\'' || tokenType == '"') {
                    String sVal = st.sval;
                    words.add(sVal);
                    if (words.size() > 1 && checkQuotesMatch) {
                        /*
                         * Check if the quotes occurred in pair, 2 kinds of
                         * mismatch cases are:
                         *  1. command -arg "
                         *  2. command -arg "xxx
                         *
                         * Way to check for above cases:
                         * The next token is EOF of line, the body of quoted
                         * string value is empty string or the body of quoted
                         * string is not empty but the end character of line
                         * is not quote character.
                         */
                        if (st.nextToken() == StreamTokenizer.TT_EOF) {
                            String quote = String.valueOf((char)tokenType);
                            if (sVal.length() == 0 || !line.endsWith(quote)) {
                                throw new RuntimeException("Except to found " +
                                            quote + " after " + st.sval +
                                            ", but not found");
                            }
                        }
                        /*
                         * Push back the current token to continue
                         * the parse work.
                         */
                        st.pushBack();
                    }
                } else if (tokenType == StreamTokenizer.TT_NUMBER) {
                    echo("Unexpected numeric token!" + eol);
                } else {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
        return words.toArray(new String[words.size()]);
    }

    /**
     * An exception class that encapsulates command line parse error that
     * thrown by parseLine. Use locally.
     */
    private class ParseLineException extends ShellException  {
        private static final long serialVersionUID = 1L;
        public ParseLineException(String msg) {
            super(msg);
        }
    }

    public void runLine(String line)
        throws ShellException {

        runLine(line, false);
    }

    private void runLine(String line, boolean checkQuotesMatch)
        throws ShellException {

        exitCode = EXIT_OK;
        if (line.length() > 0 && !line.startsWith("#")) {
            String[] splitArgs;
            try {
                splitArgs = parseLine(line, checkQuotesMatch);
            } catch (RuntimeException re) {
                throw new ParseLineException(re.getMessage());
            }
            String commandName = splitArgs[0];
            final boolean timerEnabled = getTimer();
            if (timerEnabled) {
               timer.begin();
            }
            String result = run(commandName, splitArgs);
            if (result != null) {
                output.println(result);
            }
            if (timerEnabled) {
                timer.end();
                output.println(timer.toString());
            }
            history.add(line, null);
        }
    }

    public String run(String commandName, String[] args)
        throws ShellException {

        ShellCommand command = null;
        String[] cmdArgs = null;

        command = getCurrentCommand();
        if (command != null) {
            cmdArgs = new String[args.length + 1];
            cmdArgs[0] = command.getCommandName();
            System.arraycopy(args, 0, cmdArgs, 1, args.length);
        } else {
            command = findCommand(commandName);
            cmdArgs = args;
        }

        if (command != null) {
            cmdArgs = checkVerboseAndDebug(cmdArgs);
            if (!command.overrideJsonFlag()) {
                cmdArgs = checkJson(cmdArgs);
            }
            final String result = command.execute(cmdArgs, this);
            exitCode = command.getExitCode();
            return result;
        }

        throw new ShellArgumentException(
            "Could not find command: " + commandName + eol + getUsage());
    }

    public boolean getVerbose() {
        return (verbose || global_verbose);
    }

    /** Returns whether to print debugging output. */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Returns whether to print result in JSON format
     */
    public boolean getJson() {
        return (json || shellJson);
    }

    public PrintStream getOutput() {
        return output;
    }

    public ShellInputReader getInput() {
        return inputReader;
    }

    public boolean toggleVerbose() {
        global_verbose = !global_verbose;
        return global_verbose;
    }

    public void setVerbose(boolean val) {
        global_verbose = val;
    }

    /* Enable or disable time measurement */
    public void setTimer(boolean val) {
        timing = val;
    }

    /* Returns whether time measurement is enabled or not. */
    public boolean getTimer() {
        return timing;
    }

    public int getExitCode() {
        return exitCode;
    }

    public CommandHistory getHistory() {
        return history;
    }

    public static String nextArg(String[] args, int index, ShellCommand cmd)
        throws ShellException {

        if (++index < args.length) {
            return args[index];
        }
        throw new ShellUsageException
            ("Flag " + args[index-1] + " requires an argument", cmd, true);
    }

    public void unknownArgument(String arg, ShellCommand command)
        throws ShellException {

        String msg = "Unknown argument: " + arg;
        throw new ShellUsageException(msg, command);
    }

    public void badArgCount(ShellCommand command)
        throws ShellException {

        String msg = "Incorrect number of arguments for command: " +
            command.getCommandName();
        throw new ShellUsageException(msg, command, true);
    }

    public void badArgUsage(String arg, String info, ShellCommand command)
        throws ShellException {

        String msg = "Invalid usage of the " + arg +
            " argument to the command: " + command.getCommandName();
        if (info != null && !info.isEmpty()) {
            msg = msg + " - " + info;
        }
        throw new ShellUsageException(msg, command);
    }

    public void requiredArg(String arg, ShellCommand command)
        throws ShellException {

        String msg = "Missing required argument" +
            ((arg != null) ? " (" + arg + ")" : "")
            + " for command: " +
            command.getCommandName();
        throw new ShellUsageException(msg, command, true);
    }

    /**
     * Displays the command execution result report.  If json flag is specified,
     * a json format report will be displayed. Otherwise, the non-json
     * description will be displayed.
     *
     * @param command command line
     * @param cmdResult command execution result
     * @param nonJsonDesc non-json description
     */
    public void displayResultReport(String command,
                                    CommandResult cmdResult,
                                    String nonJsonDesc) {
        if (!getJson()) {
            output.println(nonJsonDesc);
            return;
        }
        output.println(toJsonReport(command, cmdResult));
    }

    public static String makeWhiteSpace(int indent) {
        String ret = "";
        for (int i = 0; i < indent; i++) {
            ret += " ";
        }
        return ret;
    }

    /*
     * Look for -help or ? or -? in a command line.  This method makes it easy
     * for commands to accept -help or related flags later in the command line
     * and interpret them as help requests.
     */
    public static void checkHelp(String[] args, ShellCommand command)
        throws ShellException {

        for (String s : args) {
            String sl = s.toLowerCase();
            if (sl.equals("-help") ||
                sl.equals("help") ||
                sl.equals("?") ||
                sl.equals("-?")) {
                throw new ShellHelpException(command);
            }
        }
    }

    /*
     * Return true if the named argument is in the command array.
     * The arg parameter is expected to be in lower case.
     */
    public static boolean checkArg(String[] args, String arg) {
        for (String s : args) {
            String sl = s.toLowerCase();
            if (sl.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Return the item following the specified flag, e.g. the caller may be
     * looking for the argument to a -name flag.  Return null if it does not
     * exist.
     */
    public static String getArg(String[] args, String arg) {
        boolean returnNext = false;
        for (String s : args) {
            if (returnNext) {
                return s;
            }
            String sl = s.toLowerCase();
            if (sl.equals(arg)) {
                returnNext = true;
            }
        }
        return null;
    }

    public static boolean matches(String inputName,
                                  String commandName) {
        return matches(inputName, commandName, 0);
    }

    public static boolean matches(String inputName,
                                  String commandName,
                                  int prefixMatchLength) {

        if (inputName.length() < prefixMatchLength) {
            return false;
        }

        if (prefixMatchLength > 0) {
            String match = inputName.toLowerCase();
            return (commandName.toLowerCase().startsWith(match));
        }

        /* Use the entire string for comparison */
        return commandName.toLowerCase().equals(inputName.toLowerCase());
    }

    public static String toJsonReport(String command,
                                      CommandResult cmdResult) {

        try {
            return CommandJsonUtils.getJsonResultString(command, cmdResult);
        } catch (IOException e) {
            /*
             * When hit IOException while interact with Jackson JSON processing,
             * return this constant JSON string to represent this internal error
             */
            return "{" + Shell.eolt +
                "\"operation\" : \"create json output\"," + Shell.eolt +
                "\"return_code\" : 5500," + Shell.eolt +
                "\"description\" : " +
                "\"IOException in generating JSON format result: " +
                e.getMessage() + "\"," + Shell.eolt +
                "\"cmd_cleanup_job\" : []" + Shell.eolt +
                "}";

        }
    }

    /**
     * Output status information during command execution in non-json mode. It
     * differs from verboseOutput() in that the message will be output even in
     * non-verbose mode.
     */
    public void echo(String msg) {
        if (!getJson()) {
            output.print(msg);
        }
    }

    public static class LoadCommand extends ShellCommand {

        public LoadCommand() {
            super("load", 3);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            String path = null;
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if ("-file".equals(arg)) {
                    path = Shell.nextArg(args, i++, this);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }
            if (path == null) {
                shell.requiredArg("-file", this);
            }

            FileReader fr = null;
            BufferedReader br = null;
            String retString = "";
            try {
                final CommandLinesParser clp = new CommandLinesParser(shell);
                fr = new FileReader(path);
                br = new BufferedReader(fr);
                String line;

                LoopReadLine:
                while ((line = br.readLine()) != null &&
                       !shell.getTerminate() &&
                       shell.getExitCode() == Shell.EXIT_OK) {
                    try {
                        clp.appendLine(line);
                    } catch (Exception e) {
                        final String[] commands = clp.getCommands();
                        assert(commands.length == 1);
                        final String msg =
                            handleExecuteException(shell, commands[0], e);
                        /*
                         * If the returned 'msg' is not null, then the exception
                         * is handled and stop execution. Otherwise, continue
                         * to read next line and execute it.
                         */
                        if (msg != null) {
                            retString = msg;
                            break LoopReadLine;
                        }
                        clp.reset();
                        continue;
                    }

                    if (!clp.complete()) {
                        continue;
                    }

                    /* Execute commands if parsing is done. */
                    final String[] commands = clp.getCommands();
                    if (commands != null) {
                        for (String cmd: commands) {
                            cmd = cmd.trim();
                            try {
                                shell.runLine(cmd);
                            } catch (Exception e) {
                                final String msg =
                                    handleExecuteException(shell, cmd, e);
                                if (msg != null) {
                                    retString = msg;
                                    break LoopReadLine;
                                }
                            }
                        }
                    }
                    clp.reset();
                }
                exitCode = shell.getExitCode();
            } catch (IOException ioe) {
                exitCode = Shell.EXIT_INPUTERR;
                final String msg = "Failed to load file: " + path;
                if(!shell.getJson()) {
                    return msg;
                }
                CommandResult cmdResult = new CommandResult.CommandFails(
                    msg, ErrorMessage.NOSQL_5100,
                    CommandResult.NO_CLEANUP_JOBS);
                return Shell.toJsonReport(getCommandName(), cmdResult);
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException ignored) /* CHECKSTYLE:OFF */ {
                    } /* CHECKSTYLE:ON */
                }
            }
            if (shell.getJson()) {
                /* If JSON enable, don't output LoadCommand status. */
                return "";
            }
            return retString;
        }

        /**
         * Handle the execution exception, returns not-null string if the
         * exception is handled.
         *  - Returns the error message if the exception is ShellException and
         *    handled, otherwise return null (retry case).
         *  - For other unknown exception, returns a empty string
         */
        private String handleExecuteException(Shell shell,
                                              String command,
                                              Exception e) {
            if (e instanceof ShellException) {
                /* stop execution if false is returned */
                if (!shell.handleShellException(command, (ShellException)e)) {
                    return "Script error in line \"" + command +
                            "\", ending execution";
                }
                return null;
            }
            /* Unknown exceptions will terminate the script */
            shell.handleUnknownException(command, e);
            return "";
        }

        @Override
        protected String getCommandSyntax() {
            return "load -file <path to file>";
        }

        @Override
        protected String getCommandDescription() {
            return
                "Load the named file and interpret its contents as a script " +
                "of commands" + eolt + "to be executed.  If any command in " +
                "the script fails execution will end.";
        }
    }

    public static class HelpCommand extends ShellCommand {

        public HelpCommand() {
            super("help", 2);
        }

        @Override
        protected boolean matches(String commandName) {
            return ("?".equals(commandName) ||
                    super.matches(commandName));
        }

        public String[] checkForDeprecatedFlag(String[] args, Shell shell) {
            String[] retArgs = args;
            shell.setShowDeprecated(false);
            if (checkArg(args, INCLUDE_DEPRECATED_FLAG)) {
                shell.setShowDeprecated(true);
                retArgs = extractArg(args, INCLUDE_DEPRECATED_FLAG);
            }
            return retArgs;
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            args = checkForDeprecatedFlag(args, shell);

            if (args.length == 1) {
                return shell.getUsage();
            }

            /* per-command help */
            String commandName = args[1];
            ShellCommand command = shell.findCommand(commandName);
            if (command != null) {
                return(command.getHelp(Arrays.copyOfRange(args, 1, args.length),
                                       shell));
            }
            return("Could not find command: " + commandName +
                   eol + shell.getUsage());
        }

        @Override
        protected String getCommandSyntax() {
            return "help [command [sub-command]] [-include-deprecated]";
        }

        @Override
        protected String getCommandDescription() {
            return "Print help messages.  With no arguments the top-level shell"
                    + " commands" + eolt + "are listed.  With additional "
                    + "commands and sub-commands, additional" + eolt
                    + "detail is provided. " + "Will list only those commands "
                    + "that are not" + eolt + "deprecated. To list "
                    + "the deprecated commands as well, use the"
                    + eolt + INCLUDE_DEPRECATED_FLAG + " flag.";
        }
    }

    public static class ExitCommand extends ShellCommand {

        public ExitCommand() {
            super("exit", 2);
        }

        @Override
        protected boolean matches(String commandName) {
            return super.matches(commandName) ||
                   Shell.matches(commandName, "quit", 2);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            shell.setTerminate();
            return "";
        }

        @Override
        protected String getCommandSyntax() {
            return "exit | quit";
        }

        @Override
        protected String getCommandDescription() {
            return "Exit the interactive command shell.";
        }
    }

    /*
     * Maintain command history.
     *
     * TODO: limit the size of the list -- requires a circular buffer.
     */
    public class CommandHistory {
        private final List<CommandHistoryElement> history1 =
            new ArrayList<CommandHistoryElement>(100);

        /**
         * Add a command to the history
         *
         * @param command the command to add
         * @param e Exception encountered on command if any, otherwise null
         */
        public void add(String command, Exception e) {
            history1.add(new CommandHistoryElement(command, e));
        }

        /**
         * Gets the specified element in the history
         *
         * @param which the offset in the history array
         * @return the specified command
         */
        public CommandHistoryElement get(int which) {
            if (history1.size() > which) {
                return history1.get(which);
            }
            output.println("No such command in history at offset " + which);
            return null;
        }

        public int getSize() {
            return history1.size();
        }

        /**
         * Dumps the current history
         */
        public String dump(int from, int to) {
            from = Math.min(from, history1.size());
            to = Math.min(to, history1.size() - 1);
            String hist = "";
            for (int i = from; i <= to; i++) {
                hist += dumpCommand(i, false /* withFault */);
            }
            return hist;
        }

        /*
         * Caller verifies range
         */
        public boolean commandFaulted(int command) {
            CommandHistoryElement cmd = history1.get(command);
            return (cmd.getException() != null);
        }

        /*
         * Caller verifies range
         */
        public String dumpCommand(int command, boolean withFault) {
            CommandHistoryElement cmd = history1.get(command);
            String res = "";
            res = cmd.getCommand();
            if (withFault && cmd.getException() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                cmd.getException().printStackTrace(new PrintWriter(b, true));
                res += eolt + b.toString();
            }
            /*
             * The index of command are shown as 1-based index in output,
             * it is to be consistent with that in Jline command history,
             * then user can rerun a single command in history using !n,
             * n is 1-based index of command in the history.
             */
            return ((command + 1) + " " + res + eol);
        }

        public String dumpFaultingCommands(int from, int to) {
            from = Math.min(from, history1.size());
            to = Math.min(to, history1.size() - 1);
            String hist = "";
            for (int i = from; i <= to; i++) {
                CommandHistoryElement cmd = history1.get(i);
                Exception e = cmd.getException();
                if (e != null) {
                    String res = "";
                    res = cmd.getCommand();
                    /*
                     * The index of command are shown as 1-based index in
                     * output, see details in above dumpCommand() method.
                     */
                    hist += ((i + 1) + " " + res + ": " + e.getClass() + eol);
                }
            }
            return hist;
        }

        public Exception getLastException() {
            for (int i = history1.size() - 1; i >= 0; i--) {
                CommandHistoryElement cmd = history1.get(i);
                if (cmd.getException() != null) {
                    return cmd.getException();
                }
            }
            return null;
        }

        public String dumpLastFault() {
            for (int i = history1.size() - 1; i >= 0; i--) {
                CommandHistoryElement cmd = history1.get(i);
                if (cmd.getException() != null) {
                    return dumpCommand(i, true);
                }
            }
            return "";
        }

        public void clear() {
            history1.clear();
        }
    }

    class CommandHistoryElement {
        String command;
        Exception exception;

        public CommandHistoryElement(String command, Exception exception) {
            this.command = command;
            this.exception = exception;
        }

        public String getCommand() {
            return command;
        }

        public Exception getException() {
            return exception;
        }
    }

    public static class CommandComparator implements Comparator<ShellCommand> {

        @Override
        public int compare(ShellCommand o1, ShellCommand o2) {
            return o1.getCommandName().compareTo(o2.getCommandName());
        }
    }

    /*
     * Maintain a HashMap to store variables.
     */
    public static class VariablesMap implements Cloneable {
        private final HashMap<String, Object> variablesMap =
            new HashMap<String, Object>();

        public void add(String name, Object value) {
            variablesMap.put(name, value);
        }

        public Object get(String name) {
            return variablesMap.get(name);
        }

        public Set<Entry<String, Object>> getAll() {
            return variablesMap.entrySet();
        }

        public void remove(String name) {
            if (variablesMap.containsKey(name)) {
                variablesMap.remove(name);
            }
        }

        public int size() {
            return variablesMap.size();
        }

        public void reset() {
            variablesMap.clear();
        }

        @Override
        public VariablesMap clone() {
            VariablesMap map = new VariablesMap();
            for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
                map.add(entry.getKey(), entry.getValue());
            }
            return map;
        }

        @Override
        public String toString() {
            String retString = "";
            for (Map.Entry<String, Object> entry: variablesMap.entrySet()) {
                retString += Shell.tab + entry.getKey() + ": " +
                             entry.getValue() + Shell.eol;
            }
            return retString;
         }
     }

     /**
      * A class used to parse input line(s) to command(s), it can deal with
      * below command style:
      *  - Single line command
      *  - Multi-line command joined by backslash '\'
      *  - Multi-line command with semicolon as terminator
      *  - Multiple commands with semicolon as command terminator
      *
      * 4 methods provided:
      *  1) appendLine(String line): Stores the new line to internal string
      *     buffer, check if command(s) are complete.
      *  2) complete(): Returns true if the input line(s) contains command(s)
      *     which are all complete, otherwise return false.
      *  3) getCommands(): Returns a array of parsed commands.
      *  4) reset(): Resets the CommandLinesParser to initial state.
      *
      * Generally, it can be used like below:
      *
      *     CommandLinesParser clp = new CommandLinesParser(shell);
      *     do {
      *         ...
      *         clp.appendLine(newLine);
      *     } while(!clp.complete())
      *
      *     String[] commands = clp.getCommands();
      *     ...
      *     clp.reset();
      */
    private static class CommandLinesParser {
        /* Help command */
        private static String HELP_COMMAND = "?";
        private static enum ParseState {
            SINGLE_LINE,
            MULTI_LINE_CONT,
            MULTI_LINE_TERM,
            PARSE_DONE_EXECUTED,
            PARSE_DONE,
        }
        private final Shell shell;
        private final StringBuilder sb;
        private ParseState state;

        CommandLinesParser(Shell shell) {
            this.shell = shell;
            sb = new StringBuilder();
            state = ParseState.SINGLE_LINE;
        }

        /*
         * Append the new line to internal string buffer, check if the
         * command line string matches any of below styles:
         *  - Single line command
         *  - Multi-line command joined by backslash '\'
         *  - Multi-line command with semicolon as terminator
         *  - Multiple commands with semicolon as command terminator
         *
         *  The checkComplete() method may be called to check if the command is
         *  complete, internally the command is executed, so the exception
         *  may be thrown out if the execution failed, the outer caller should
         *  handle the exception.
         */
        void appendLine(final String line)
            throws Exception {

            String command = line.trim();
            if (command.length() == 0) {
                if (state == ParseState.SINGLE_LINE) {
                    state = ParseState.PARSE_DONE_EXECUTED;
                }
                return;
            }

            /* Read "?", then terminate the parsing  */
            if (command.equalsIgnoreCase(HELP_COMMAND)) {
                sb.append(" -help");
                state = ParseState.PARSE_DONE;
                return;
            }
            final char ending = command.charAt(command.length() - 1);
            final boolean endWithCont = (ending == LINE_JOINER);
            final boolean endWithTerm = (ending == LINE_TERMINATOR);

            /* Append command to string buffer */
            if (endWithCont) {
                command = command.substring(0, command.length() - 1);
                if (!command.endsWith(" ")) {
                    command = command + " ";
                }
            }
            if (state == ParseState.MULTI_LINE_TERM) {
                sb.append(" ");
            }
            sb.append(command);

            /*
             * If the input line is ended with the line terminator , then
             * terminates the parsing.
             */
            if (endWithTerm) {
                state = ParseState.PARSE_DONE;
                return;
            }

            String cmdToExecute = null;
            switch (state) {
            case SINGLE_LINE:
                if (endWithCont) {
                    state = ParseState.MULTI_LINE_CONT;
                } else {
                    /*
                     * Parses the command line string, if it is single command,
                     * then need checking its completeness by executing it.
                     */
                    final String[] commands = parseCommandLines(sb.toString());
                    if (commands.length > 1) {
                        state = ParseState.MULTI_LINE_TERM;
                    } else {
                        cmdToExecute = commands[0];
                    }
                }
                break;
            case MULTI_LINE_CONT:
                if (!endWithCont) {
                    /*
                     * Parses the command line string, if it is single command,
                     * then need checking its completeness by executing it.
                     */
                    final String[] commands = parseCommandLines(sb.toString());
                    if (commands.length > 1) {
                        state = ParseState.MULTI_LINE_TERM;
                    } else {
                        cmdToExecute = commands[0];
                    }
                }
                break;
            default:
                break;
            }

            /* Check if the command is complete or not. */
            if (cmdToExecute != null) {
                try {
                    final boolean isCompleted = checkCompleted(cmdToExecute);
                    if (!isCompleted) {
                        state = ParseState.MULTI_LINE_TERM;
                    } else {
                        state = ParseState.PARSE_DONE_EXECUTED;
                    }
                } catch (Exception e) {
                    /* Throw the exception if execution of command failed. */
                    state = ParseState.PARSE_DONE;
                    throw e;
                }
            }
        }

        /* Returns true if the parsing state is done. */
        boolean complete() {
            return (state == ParseState.PARSE_DONE ||
                    state == ParseState.PARSE_DONE_EXECUTED);
        }

        /*
         * Returns the array of commands if parsing state is PARSE_DONE.
         * For none-complete states like SINGLE_LINE, MULTI_LINE_TERM and
         * MULTI_LINE_CONT, returns null. If state is PARSE_DONE_EXECUTED,
         * the command was executed so return null as well.
         */
        String[] getCommands() {
            if (state == ParseState.PARSE_DONE) {
                return parseCommandLines(sb.toString());
            }
            return null;
        }

        /* Resets the state and string buffer to initial value. */
        void reset() {
            state = ParseState.SINGLE_LINE;
            sb.setLength(0);
        }

        /*
         * Check if the input command is complete by executing the command,
         * the command is regarded as incomplete if caught below 2 exceptions:
         *   1) ParseLineException
         *   2) ShellUsageException and ShellUsageException.requireArgument()
         *      returns true.
         *
         * If execution failed, then throw exception.
         */
        private boolean checkCompleted(String command)
            throws Exception {

            try {
                shell.runLine(command, true);
                return true;
            } catch (ShellException se) {
                if (se instanceof ParseLineException) {
                    return false;
                }
                if ((se instanceof ShellUsageException) &&
                    ((ShellUsageException) se).requireArgument()) {
                    return false;
                }
                throw se;
            } catch (Exception e) {
                throw e;
            }
        }

        /*
         * Split a single line into multiple command lines on the delimiter
         * semicolon ; but ignore the semicolon in single/double quotes:
         *
         * e.g.
         *      cmd1; cmd2 "xxx; yyy"; cmd3 'xxx; yyy'
         *  =>
         *      cmd1
         *      cmd2 "xxx; yyy"
         *      cmd3 'xxx; yyy'
         */
        private String[] parseCommandLines(String line) {
            String pattern = ";(?=(?:[^'\"]|\"[^\"]*\"|'[^']*')*$)";
            return line.trim().replaceAll(";+$", "").split(pattern);
        }
    }

    private static class Timer {
        private long time;

        Timer() {
            time = 0;
        }

        void begin() {
            time = getWallClockTime();
        }

        void end() {
            time = getWallClockTime() - time;
        }

        private long getWallClockTime() {
            return System.currentTimeMillis();
        }

        @Override
        public String toString() {
            final String fmt = "\nTime: %,dsec %dms";
            long sec = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
            long ms =
                time - TimeUnit.MILLISECONDS.convert(sec, TimeUnit.SECONDS);
            return String.format(fmt, sec, ms);
        }
    }
}
