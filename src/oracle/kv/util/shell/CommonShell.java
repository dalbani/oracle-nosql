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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import oracle.kv.AuthenticationFailureException;
import oracle.kv.Consistency;
import oracle.kv.Durability;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.LoginCredentials;
import oracle.kv.StatementResult;
import oracle.kv.impl.admin.CommandResult;
import oracle.kv.impl.client.DdlJsonFormat;
import oracle.kv.impl.security.PasswordExpiredException;
import oracle.kv.impl.security.util.KVStoreLogin;
import oracle.kv.impl.security.util.KVStoreLogin.CredentialsProvider;
import oracle.kv.impl.util.CommandParser;
import oracle.kv.util.ErrorMessage;
import static oracle.kv.impl.util.CommandParser.CONSISTENCY_FLAG;
import static oracle.kv.impl.util.CommandParser.DURABILITY_FLAG;
import static oracle.kv.impl.util.CommandParser.FROM_FLAG;
import static oracle.kv.impl.util.CommandParser.HOST_FLAG;
import static oracle.kv.impl.util.CommandParser.LAST_FLAG;
import static oracle.kv.impl.util.CommandParser.NAME_FLAG;
import static oracle.kv.impl.util.CommandParser.OFF_FLAG;
import static oracle.kv.impl.util.CommandParser.ON_FLAG;
import static oracle.kv.impl.util.CommandParser.PORT_FLAG;
import static oracle.kv.impl.util.CommandParser.SECURITY_FLAG;
import static oracle.kv.impl.util.CommandParser.TIMEOUT_FLAG;
import static oracle.kv.impl.util.CommandParser.TO_FLAG;
import static oracle.kv.impl.util.CommandParser.USER_FLAG;

/*
 * This class encapsulates some sharable utility commands:
 *  history, timer, page, connect store.
 */
public abstract class CommonShell extends Shell {

    private String kvstoreName = null;
    private String storeHostname = null;
    private int storePort = 0;
    private String storeUser = null;
    private String storeSecurityFile = null;
    private List<String> helperHosts = null;
    private String[] commandToRun;
    private int nextCommandIdx = 0;
    private boolean noprompt = false;
    private boolean noconnect = false;

    private Integer storeTimeout = null;
    private Consistency storeConsistency = null;
    private Durability storeDurability = null;

    private LoginHelper loginHelper = null;
    private KVStore store = null;
    private KVStoreConfig kvstoreConfig = null;
    private int pageHeight = 0;

    /* Default consistency policy used for this connection. */
    final static Consistency CONSISTENCY_DEF = Consistency.NONE_REQUIRED;
    /*
     * By default, use a more aggressive default Durability to make
     * sure that records get into a database.
     */
    final static Durability DURABILITY_DEF = Durability.COMMIT_SYNC;

    /* Default request timeout used for this connection. */
    final static int REQUEST_TIMEOUT_DEF = 5000;

    private static Map<String, Consistency> consistencyMap;
    private static Map<String, Durability> durabilityMap;
    static {
        Map<String, Consistency> consMap = new HashMap<String, Consistency>();
        consMap.put("NONE_REQUIRED", Consistency.NONE_REQUIRED);
        consMap.put("NONE_REQUIRED_NO_MASTER",
            Consistency.NONE_REQUIRED_NO_MASTER);
        consMap.put("ABSOLUTE", Consistency.ABSOLUTE);
        consistencyMap = Collections.unmodifiableMap(consMap);

        Map<String, Durability> durMap = new HashMap<String, Durability>();
        durMap.put("COMMIT_SYNC", Durability.COMMIT_SYNC);
        durMap.put("COMMIT_NO_SYNC", Durability.COMMIT_NO_SYNC);
        durMap.put("COMMIT_WRITE_NO_SYNC", Durability.COMMIT_WRITE_NO_SYNC);
        durabilityMap = Collections.unmodifiableMap(durMap);
    }

    public CommonShell(InputStream input, PrintStream output) {
        super(input, output);
    }

    public KVStore getStore() throws ShellException {
        if (store == null) {
            throw new ShellException("Not Connected.");
        }
        return store;
    }

    public KVStoreConfig getKVStoreConfig() {
        return kvstoreConfig;
    }

    public void setStoreConsistency(Consistency consistency) {
        storeConsistency = consistency;
    }

    public Consistency getStoreConsistency() {
        return (storeConsistency != null) ?
                storeConsistency : CONSISTENCY_DEF;
    }

    public static String getConsistencyName(Consistency consistency) {
        if (consistency instanceof Consistency.Time) {
            final Consistency.Time tcons = (Consistency.Time) consistency;
            return tcons.getName() + "[permissibleLag_ms=" +
                tcons.getPermissibleLag(TimeUnit.MILLISECONDS) +
                ", timeout_ms=" + tcons.getTimeout(TimeUnit.MILLISECONDS) +
                "]";
        }
        for (Entry<String, Consistency> entry : consistencyMap.entrySet()) {
            if (consistency.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void setStoreDurability(Durability durability) {
        storeDurability = durability;
    }

    public Durability getStoreDurability() {
        return (storeDurability != null) ?
                storeDurability : DURABILITY_DEF;
    }

    public static String getDurabilityName(Durability durability) {
        for (Entry<String, Durability> entry : durabilityMap.entrySet()) {
            if (durability.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return "Durability[MasterSync=" + durability.getMasterSync() +
            ", ReplicaSync=" + durability.getReplicaSync() +
            ", ReplicaAck= " + durability.getReplicaAck() + "]";
    }

    public void setRequestTimeout(int timeout) {
        storeTimeout = timeout;
    }

    public int getRequestTimeout() {
        return (storeTimeout != null) ?
                storeTimeout.intValue() : REQUEST_TIMEOUT_DEF;
    }

    public void connectStore()
        throws ShellException {

        if (kvstoreName != null) {
            getLoginHelper().updateStoreLogin(storeUser, storeSecurityFile);
            openStore();
        }
    }

    void setPageHeight(int height) {
        pageHeight = height;
    }

    public int getPageHeight() {
        if (pageHeight == 0) {
            final ShellInputReader inputReader = getInput();
            return (inputReader == null) ? -1 : inputReader.getTerminalHeight();
        }
        return pageHeight;
    }

    public boolean isPagingEnabled() {
        return (getPageHeight() >= 0);
    }

    public boolean isNoPrompt() {
        return noprompt;
    }

    public boolean isNoConnect() {
        return noconnect;
    }

    public static Consistency getConsistency(String name) {
        return consistencyMap.get(name.toUpperCase());
    }

    public static Set<String> getConsistencyNames() {
        return consistencyMap.keySet();
    }

    public static Durability getDurability(String name) {
        return durabilityMap.get(name.toUpperCase());
    }

    public static Set<String> getDurabilityNames() {
        return durabilityMap.keySet();
    }

    public void setLoginHelper(LoginHelper loginHelper) {
        this.loginHelper = loginHelper;
    }

    public LoginHelper getLoginHelper() {
        if (loginHelper == null) {
            loginHelper = new LoginHelper();
        }
        return loginHelper;
    }

    public void openStore(String host,
                          int port,
                          String storeName,
                          String user,
                          String securityFile)
        throws ShellException {

        openStore(host, port, storeName, user, securityFile,
                  null /* timeout */, null /* consistency */,
                  null /* durability */);
    }

    public void openStore(String host,
                          int port,
                          String storeName,
                          String user,
                          String securityFile,
                          Integer timeout,
                          Consistency consistency,
                          Durability durability)
        throws ShellException {

        storeHostname = host;
        storePort = port;
        kvstoreName = storeName;
        if (timeout != null) {
            setRequestTimeout(timeout);
        }
        if (consistency != null) {
            setStoreConsistency(consistency);
        }
        if (durability != null) {
            setStoreDurability(durability);
        }

        try {
            getLoginHelper().updateStoreLogin(user, securityFile);
        } catch (IllegalStateException ise) {
            throw new ShellException(ise.getMessage());
        } catch (IllegalArgumentException iae) {
            output.println(iae.getMessage());
        }
        openStore();
    }

    String[] getHostPorts() {
        if (storeHostname != null) {
            return new String[]{storeHostname + ":" + storePort};
        }
        return helperHosts.toArray(new String[helperHosts.size()]);
    }

    /* Open the store. */
    public void openStore()
        throws ShellException {

        final String[] hostports = getHostPorts();
        kvstoreConfig = new KVStoreConfig(kvstoreName, hostports);

        final Durability durability = getStoreDurability();
        kvstoreConfig.setDurability(durability);

        final Consistency consistency = getStoreConsistency();
        kvstoreConfig.setConsistency(consistency);

        final int requestTimeout = getRequestTimeout();
        kvstoreConfig.setRequestTimeout(requestTimeout, TimeUnit.MILLISECONDS);
        /* Make sure socket timeout >= request timeout */
        final long socketTimeout = kvstoreConfig
                .getSocketReadTimeout(TimeUnit.MILLISECONDS);
        if (socketTimeout < requestTimeout) {
            kvstoreConfig.setSocketReadTimeout(requestTimeout,
                TimeUnit.MILLISECONDS);
        }

        try {
            store = getLoginHelper().getAuthenticatedStore(kvstoreConfig);
        } catch (ShellException se) {
            throw se;
        } catch (Exception e) {
            throw new ShellException("Cannot connect to " + kvstoreName +
                " at " + storeHostname + ":" + storePort, e);
        }
    }

    public void closeStore() {
        if (store != null) {
            store.close();
            store = null;
        }
    }

    public abstract class ShellParser extends CommandParser {
        private List<String> reqFlags;

        /* Hidden flags: -noprompt, -noconnect, -hidden. */
        public ShellParser(String[] args,
                           String[] rcArgs,
                           String[] requiredFlags) {
            /*
             * The true argument tells CommandParser that this class will
             * handle all flags, not just those unrecognized.
             */
            super(args, rcArgs, true);
            if (requiredFlags != null) {
                reqFlags = new ArrayList<String>();
                reqFlags.addAll(Arrays.asList(requiredFlags));
            }
        }

        @Override
        protected void verifyArgs() {
            if (!noconnect) {
                if (reqFlags != null && reqFlags.size() > 0) {
                    usage("Missing required argument");
                }
            }
            if ((commandToRun != null) &&
                (nextCommandIdx < commandToRun.length)) {
                usage("Flags may not follow commands");
            }
        }

        /* Abstract function to get the usage of shell. */
        public abstract String getShellUsage();

        @Override
        public void usage(String errorMsg) {
            String error = "";
            if (errorMsg != null) {
                error += errorMsg + eol;
            }
            error += getShellUsage();
            if (argArray != null &&
                Shell.checkArg(argArray, CommandParser.JSON_FLAG)) {
                String operation = "";
                for (String arg: argArray) {
                    operation += arg + " ";
                }
                CommandResult result =
                    new CommandResult.CommandFails(
                        error, ErrorMessage.NOSQL_5100,
                        CommandResult.NO_CLEANUP_JOBS);
                error = Shell.toJsonReport(operation, result);
            } 

            System.err.println(error);
            System.exit(1);
        }

        public boolean checkExtraArg(@SuppressWarnings("unused") String arg) {
            return false;
        }

        @Override
        protected boolean checkArg(String arg) {

            /*
             * In order to allow an embedded command to use the same flags as
             * the shell executable, if the commandToRun is set, just pass them
             * through.
             */
            if (commandToRun == null) {
                checkRequiredFlag(arg);

                if (HOST_FLAG.equals(arg)) {
                    storeHostname = nextArg(arg);
                    return true;
                } else if (PORT_FLAG.equals(arg)) {
                    storePort = Integer.parseInt(nextArg(arg));
                    return true;
                } else if (HELPER_HOSTS_FLAG.equals(arg)) {
                    addHelperHosts(nextArg(arg));
                    return true;
                } else if (STORE_FLAG.equals(arg)) {
                    kvstoreName = nextArg(arg);
                    return true;
                } else if (USER_FLAG.equals(arg)) {
                    storeUser = nextArg(arg);
                    return true;
                } else if (SECURITY_FLAG.equals(arg)) {
                    storeSecurityFile = nextArg(arg);
                    return true;
                } else if (TIMEOUT_FLAG.equals(arg)) {
                    storeTimeout = nextIntArg(arg);
                    if(storeTimeout <= 0) {
                        usage("Flag " + arg + " requires a positive integer");
                    }
                    return true;
                } else if (CONSISTENCY_FLAG.equals(arg)) {
                    final String cname = nextArg(arg);
                    storeConsistency = getConsistency(cname);
                    if (storeConsistency == null) {
                        usage(CommandParser.getConsistencyUsage());
                    }
                    return true;
                } else if (DURABILITY_FLAG.equals(arg)) {
                    final String dname = nextArg(arg);
                    storeDurability = getDurability(dname);
                    if (storeDurability == null) {
                        usage(CommandParser.getDurabilityUsage());
                    }
                    return true;
                } else if (checkExtraArg(arg)) {
                    return true;
                }
            }
            if (NOCONNECT_FLAG.equals(arg)) {
                noconnect = true; /* undocumented option */
                return true;
            }
            if (NOPROMPT_FLAG.equals(arg)) {
                noprompt = true;
                return true;
            }
            if (HIDDEN_FLAG.equals(arg)) {
                showHidden = true;
                /* Some commands take this flag as well */
                if (commandToRun != null) {
                    addToCommand(arg);
                }
                return true;
            }
            if (JSON_FLAG.equals(arg)) {
                shellJson = true;
                /* Some commands take this flag as well */
                if (commandToRun != null) {
                    addToCommand(arg);
                }
                return true;
            }
            if (!isIgnoreUnknownArg()) {
                addToCommand(arg);
            }
            return true;
        }

        private void checkRequiredFlag(String arg) {
            if (reqFlags == null || reqFlags.isEmpty()) {
                return;
            }
            if (reqFlags.contains(arg)) {
                reqFlags.remove(arg);
            }
        }

        private void addHelperHosts(String arg) {
            helperHosts = new ArrayList<String>();
            String[] hostports = arg.split(",");
            helperHosts.addAll(Arrays.asList(hostports));
        }

        /*
         * Add unrecognized args to the commandToRun array.
         */
        private void addToCommand(String arg) {
            if (commandToRun == null) {
                commandToRun = new String[getNRemainingArgs() + 1];
            }
            commandToRun[nextCommandIdx++] = arg;
        }

        @Override
        public String getHostname() {
            return storeHostname;
        }

        @Override
        public int getRegistryPort() {
            return storePort;
        }

        @Override
        public String getStoreName() {
            return kvstoreName;
        }

        @Override
        public String getUserName() {
            return storeUser;
        }

        @Override
        public String getSecurityFile() {
            return storeSecurityFile;
        }
    }

    /**
     * A helper class for client login of store.
     */
    public static class LoginHelper implements CredentialsProvider {
        private final KVStoreLogin storeLogin = new KVStoreLogin();
        private LoginCredentials storeCreds;
        private boolean isSecuredStore;

        /**
         * Updates the login information of store.
         *
         * @param newUser new user name
         * @param newLoginFile new login file path
         */
        void updateStoreLogin(final String newUser,
                              final String newLoginFile) {
            storeLogin.updateLoginInfo(newUser, newLoginFile);
            isSecuredStore = storeLogin.foundSSLTransport();
            storeCreds = null;
        }


        /**
         * Returns a kvstore handler. If the store is secured, authentication
         * will be conducted using login file or via user interaction.
         *
         * @param config the kvconfig specified by user
         * @return kvstore handler
         */
        private KVStore getAuthenticatedStore(final KVStoreConfig config)
            throws ShellException {

            config.setSecurityProperties(storeLogin.getSecurityProperties());

            try {
                if (isSecuredStore && storeCreds == null) {
                    storeCreds = storeLogin.makeShellLoginCredentials();
                }
                return KVStoreFactory.getStore(
                    config, storeCreds,
                    KVStoreLogin.makeReauthenticateHandler(this));
            } catch (AuthenticationFailureException afe) {
                storeCreds = null;
                String errorMsg = "Login failed: ";
                if (afe.getFaultClassName().equals(
                    PasswordExpiredException.class.getName())) {
                    errorMsg += "Password is expired. " +
                                "Please try log in Admin CLI and renew your " +
                                "password following prompts.";
                } else {
                    errorMsg += afe.getMessage();
                }
                throw new ShellException(errorMsg, afe);
            } catch (IOException ioe) {
                throw new ShellException("Failed to get login credentials: " +
                                         ioe.getMessage());
            }
        }

        @Override
        public LoginCredentials getCredentials() {
            return storeCreds;
        }
    }

    /* ConnectStore command */
    public static class ConnectStoreCommand extends ShellCommand {
        static final String NAME = "connect";

        public static final String CONNECT_STORE_COMMAND_DESC =
            "Connects to a KVStore to perform data access functions." +
            eolt + "If the instance is secured, you may need to provide " +
            "login credentials.";

        public static final String CONNECT_STORE_COMMAND_ARGUMENTS =
            CommandParser.getHostUsage() + " " + CommandParser.getPortUsage() +
            " " + NAME_FLAG + " <storeName>" + eolt +
            CommandParser.optional(CommandParser.getTimeoutUsage()) + " " +
            eolt +
            CommandParser.optional(CommandParser.getConsistencyUsage()) +
            eolt +
            CommandParser.optional(CommandParser.getDurabilityUsage()) +
            eolt +
            CommandParser.optional(CommandParser.getUserUsage()) + " " +
            CommandParser.optional(CommandParser.getSecurityUsage());

        public static final String CONNECT_STORE_COMMAND_SYNTAX =
            NAME + " " + CONNECT_STORE_COMMAND_ARGUMENTS;

        public ConnectStoreCommand() {
            super(NAME, 4);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            String hostname = null;
            int port = 0;
            String storeName = null;
            String user = null;
            String security = null;
            Integer timeout = null;
            Consistency consistency = null;
            Durability durability = null;

            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (CommandParser.HOST_FLAG.equals(arg)) {
                    hostname = Shell.nextArg(args, i++, this);
                } else if (PORT_FLAG.equals(arg)) {
                    port = parseInt(Shell.nextArg(args, i++, this));
                    if (port < 1 || port > 65535) {
                        invalidArgument(arg);
                    }
                } else if (NAME_FLAG.equals(arg)) {
                    storeName = Shell.nextArg(args, i++, this);
                } else if (USER_FLAG.equals(arg)) {
                    user = Shell.nextArg(args, i++, this);
                } else if (SECURITY_FLAG.equals(arg)) {
                    security = Shell.nextArg(args, i++, this);
                } else if (TIMEOUT_FLAG.equals(arg)) {
                    timeout = parseInt(Shell.nextArg(args, i++, this));
                    if (timeout <= 0) {
                        invalidArgument(arg);
                    }
                } else if (CONSISTENCY_FLAG.equals(arg)) {
                    final String cname = Shell.nextArg(args, i++, this);
                    consistency = getConsistency(cname);
                    if (consistency == null) {
                        invalidArgument(arg);
                    }
                } else if (DURABILITY_FLAG.equals(arg)) {
                    final String dname = Shell.nextArg(args, i++, this);
                    durability = getDurability(dname);
                    if (durability == null) {
                        invalidArgument(arg);
                    }
                } else {
                    shell.unknownArgument(arg, this);
                }
            }

            if (hostname == null) {
                shell.requiredArg(HOST_FLAG, this);
            }
            if (port == 0) {
                shell.requiredArg(PORT_FLAG, this);
            }
            if (storeName == null) {
                shell.requiredArg(NAME_FLAG, this);
            }

            /* Close current store, open new one.*/
            final CommonShell cshell = (CommonShell) shell;
            cshell.closeStore();
            try {
                cshell.openStore(hostname, port, storeName, user, security,
                                 timeout, consistency, durability);
            } catch (ShellException se) {
                throw new ShellException(
                    se.getMessage() + eol +
                    "Warning: You are no longer connected to a store.");
            }

            String ret = "Connected to " + storeName + " at " + hostname
                + ":" + port + ".";
            if(timeout != null) {
                ret += " Set timeout: " + timeout + "ms.";
            }
            if(consistency != null) {
                ret += " Set consistency: " + consistency + ".";
            }
            if(durability != null) {
                ret += " Set durability: " + durability + ".";
            }
            return ret;
        }

        @Override
        protected String getCommandSyntax() {
            return CONNECT_STORE_COMMAND_SYNTAX;
        }

        @Override
        protected String getCommandDescription() {
            return CONNECT_STORE_COMMAND_DESC;
        }
    }

    /* Time command */
    public static class TimeCommand extends ShellCommand {
        private final static String NAME = "timer";

        final static String SYNTAX = NAME + " " +
            CommandParser.optional(ON_FLAG + " | " + OFF_FLAG);
        final static String DESCRIPTION = "Turns the measurement and display " +
        	"of execution time for commands on or off.";

        public TimeCommand() {
            super(NAME, 5);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            if (args.length > 2) {
                shell.badArgCount(this);
            } else if (args.length > 1) {
                String arg = args[1];
                if (ON_FLAG.equals(arg)) {
                    shell.setTimer(true);
                } else if (OFF_FLAG.equals(arg)) {
                    shell.setTimer(false);
                } else {
                    shell.unknownArgument(arg, this);
                }
            }
            return "Timer is now " + (shell.getTimer() ? "on" : "off");
        }

        @Override
        public String getCommandSyntax() {
            return SYNTAX;
        }

        @Override
        public String getCommandDescription() {
            return DESCRIPTION;
        }
    }

    /* Page command */
    public static class PageCommand extends ShellCommand {
        private final static String NAME = "page";

        final static String SYNTAX = NAME + " " +
            CommandParser.optional(ON_FLAG + " | <n> | " + OFF_FLAG);
        final static String DESCRIPTION =
            "Turns query output paging on or off.  If specified, n is used " +
            "as the page" + eolt + "height. If n is 0, or 'on' is specified " +
            "the default page height is used." + eolt + "'off' turns paging " +
            "off.";

        public PageCommand() {
            super(NAME, 4);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            final CommonShell comShell = (CommonShell)shell;
            int height;
            if (args.length > 2) {
                shell.badArgCount(this);
            } else if (args.length > 1) {
                String arg = args[1];
                if (ON_FLAG.equals(arg)) {
                    comShell.setPageHeight(0);
                } else if (OFF_FLAG.equals(arg)) {
                    comShell.setPageHeight(-1);
                } else {
                    height = parseInt(arg);
                    if (height < 1) {
                        invalidArgument(arg);
                    }
                    comShell.setPageHeight(height);
                }
            }
            if (comShell.isPagingEnabled()) {
                return "Paging mode is now on, height: " +
                    comShell.getPageHeight();
            }
            return "Paging mode is now off";
        }

        @Override
        public String getCommandSyntax() {
            return SYNTAX;
        }

        @Override
        public String getCommandDescription() {
            return DESCRIPTION;
        }
    }

    /* History command */
    public static class HistoryCommand extends ShellCommand {
        final static String NAME = "history";

        final static String SYNTAX = NAME + " " +
            CommandParser.optional(LAST_FLAG + " <n>") + " " +
            CommandParser.optional(FROM_FLAG + " <n>") + " " +
            CommandParser.optional(TO_FLAG + " <n>");

        final static String DESCRIPTION =
            "Displays command history.  By default all history is " +
            "displayed." + eolt + "Optional flags are used to choose " +
            "ranges for display";

        public HistoryCommand() {
            super(NAME, 4);
        }

        @Override
        public String execute(String[] args, Shell shell)
            throws ShellException {

            Shell.checkHelp(args, this);
            Shell.CommandHistory history = shell.getHistory();
            int from = 0;
            int to = history.getSize();
            boolean isLast = false;

            if (args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];
                    if (LAST_FLAG.equals(arg)) {
                        from = parseInt(Shell.nextArg(args, i++, this));
                        isLast = true;
                    } else if (FROM_FLAG.equals(arg)) {
                        from = parseInt(Shell.nextArg(args, i++, this));
                    } else if (TO_FLAG.equals(arg)) {
                        to = parseInt(Shell.nextArg(args, i++, this));
                    } else {
                        shell.unknownArgument(arg, this);
                    }
                }
                if (isLast) {
                    from = history.getSize() - from + 1;
                }
            }
            /*
             * The index of command are shown as 1-based index in
             * the output, so covert it to 0-based index when locating
             * it in CommandHistory list.
             */
            return history.dump(toZeroBasedIndex(from), toZeroBasedIndex(to));
        }

        private int toZeroBasedIndex(int index) {
            return (index > 0) ? (index - 1) : 0;
        }

        @Override
        protected String getCommandSyntax() {
            return SYNTAX;
        }

        @Override
        protected String getCommandDescription() {
            return DESCRIPTION;
        }
    }

    /**
     * Display the result depending on its type and outcome. Operations that
     * ended in error show their error messages and status info, operations
     * like 'describe' and 'show' display their results, and operations that
     * generate plan execution return status info.
     */
    public String displayDDLResults(StatementResult result) {

        if (result.getErrorMessage() != null) {
            return result.getErrorMessage() + "\n" + result.getInfo();
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

    public void start() {
        init();
        if (commandToRun != null) {
            try {
                String result = run(commandToRun[0], commandToRun);
                output.println(result);
            } catch (ShellException se) {
                handleShellException(commandToRun[0], se);
            } catch (Exception e) {
                handleUnknownException(commandToRun[0], e);
            }
        } else {
            loop();
        }
        shutdown();
    }
}
