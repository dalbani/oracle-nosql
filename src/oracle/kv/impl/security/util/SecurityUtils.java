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
package oracle.kv.impl.security.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.kv.impl.admin.param.SecurityParams;
import oracle.kv.impl.security.KVStoreUserPrincipal;
import oracle.kv.impl.security.ResourceOwner;
import oracle.kv.impl.security.ssl.KeyStorePasswordSource;
import oracle.kv.impl.util.ConfigUtils;
import oracle.kv.impl.util.FileNames;
import oracle.kv.impl.util.FileUtils;
import oracle.kv.impl.util.SecurityConfigCreator.IOHelper;

/**
 * A collection of security-related utilities.
 */
public final class SecurityUtils {

    public static final String KEY_CERT_FILE = "certFileName";
    private static final String CERT_FILE_DEFAULT = "store.cert";

    public static final String KEY_KEY_ALGORITHM = "keyAlgorithm";
    private static final String KEY_ALGORITHM_DEFAULT = "RSA";

    public static final String KEY_KEY_SIZE = "keySize";
    private static final String KEY_SIZE_DEFAULT = "1024";

    public static final String KEY_DISTINGUISHED_NAME = "distinguishedName";
    private static final String DISTINGUISHED_NAME_DEFAULT = "cn=NoSQL";

    public static final String KEY_KEY_ALIAS = "keyAlias";
    public static final String KEY_ALIAS_DEFAULT = "shared";

    public static final String KEY_VALIDITY = "validity";
    private static final String VALIDITY_DEFAULT = "365";

    /*
     * The strings used by the keystore utility.  Probably subject to
     * localization, etc..
     */
    private static final String KS_PRIVATE_KEY_ENTRY = "PrivateKeyEntry";
    private static final String KS_SECRET_KEY_ENTRY = "SecretKeyEntry";
    private static final String KS_TRUSTED_CERT_ENTRY = "trustedCertEntry";

    private static final String TEMP_CERT_FILE = "temp.cert";

    /*
     * The strings used by the Kerberos utility.
     */
    private static final String KADMIN_DEFAULT = "/usr/kerberos/sbin/kadmin";
    public static final String KRB_CONF_FILE = "/etc/krb5.conf";

    private static final String PRINCIPAL_VALIDITY = "krbPrincValidity";
    private static final String PRINC_VALIDITY_DEFAULT = "365days";

    private static final String KEYSALT_LIST = "krbKeysalt";
    private static final String PRINCIPAL_PWD_EXPIRE = "krbPrincPwdExpire";

    private static final String PRINC_PWD_EXPIRE_DEFAULT = "365days";
    private static final String KEYSALT_LIST_DEFAULT = "des3-cbc-sha1:normal," +
        "aes128-cts-hmac-sha1-96:normal,arcfour-hmac:normal";
    public static final String KERBEROS_AUTH_NAME = "KERBEROS";
    public static final String KRB_NAME_COMPONENT_SEPARATOR_STR = "/";
    public static final String KRB_NAME_REALM_SEPARATOR_STR = "@";

    public static final Properties princDefaultProps = new Properties();

    static {
        princDefaultProps.put(PRINCIPAL_VALIDITY, PRINC_VALIDITY_DEFAULT);
        princDefaultProps.put(KEYSALT_LIST, KEYSALT_LIST_DEFAULT);
        princDefaultProps.put(PRINCIPAL_PWD_EXPIRE, PRINC_PWD_EXPIRE_DEFAULT);
    }

    /* not instantiable */
    private SecurityUtils() {
    }

    /**
     * Given an abstract file, attempt to change permissions so that it is
     * readable only by the owner of the file.
     * @param f a File referencing a file or directory on which permissions are
     * to be changed.
     * @return true if the permissions were successfully changed
     */
    public static boolean makeOwnerAccessOnly(File f)
        throws IOException {

        if (!f.exists()) {
            return false;
        }

        final FileSysUtils.Operations osOps = FileSysUtils.selectOsOperations();
        return osOps.makeOwnerAccessOnly(f);
    }

    /**
     * Given an abstract file, attempt to change permissions so that it is
     * writable only by the owner of the file.
     * @param f a File referencing a file or directory on which permissions are
     * to be changed.
     * @return true if the permissions were successfully changed
     */
    public static boolean makeOwnerOnlyWriteAccess(File f)
        throws IOException {

        if (!f.exists()) {
            return false;
        }

        final FileSysUtils.Operations osOps = FileSysUtils.selectOsOperations();
        return osOps.makeOwnerAccessOnly(f);
    }

    public static boolean passwordsMatch(char[] pwd1, char[] pwd2) {
        if (pwd1 == pwd2) {
            return true;
        }

        if (pwd1 == null || pwd2 == null) {
            return false;
        }

        return Arrays.equals(pwd1, pwd2);
    }

    public static void clearPassword(char[] pwd) {
        if (pwd != null) {
            for (int i = 0; i < pwd.length; i++) {
                pwd[i] = ' ';
            }
        }
    }

    /**
     * Make a java keystore and an associated trustStore.
     * @param securityDir the directory in which the keystore and truststore
     *    will be created.
     * @param sp a SecurityParams instance containing information regarding
     * the keystore and truststore file names
     * @param keyStorePassword the password with which the keystore and
     * truststore will be secured
     * @param props a set of optional settings that can alter the
     *    keystore creation.
     * @return true if the creation process was successful and false
     *    if an error occurred.
     */
    public static boolean initKeyStore(File securityDir,
                                       SecurityParams sp,
                                       char[] keyStorePassword,
                                       Properties props) {
        if (props == null) {
            props = new Properties();
        }

        final String certFileName = props.getProperty(KEY_CERT_FILE,
                                                      CERT_FILE_DEFAULT);

        final String keyStoreFile =
            new File(securityDir.getPath(), sp.getKeystoreFile()).getPath();
        final String trustStoreFile =
            new File(securityDir.getPath(), sp.getTruststoreFile()).getPath();
        final String certFile =
            new File(securityDir.getPath(), certFileName).getPath();

        try {
            final String keyAlg = props.getProperty(KEY_KEY_ALGORITHM,
                                                    KEY_ALGORITHM_DEFAULT);
            final String keySize = props.getProperty(KEY_KEY_SIZE,
                                                     KEY_SIZE_DEFAULT);
            final String dname = props.getProperty(KEY_DISTINGUISHED_NAME,
                                                   DISTINGUISHED_NAME_DEFAULT);
            final String keyAlias = props.getProperty(KEY_KEY_ALIAS,
                                                      KEY_ALIAS_DEFAULT);
            /*
             * TODO: converting to String here introduces some security risk.
             * Consider changing the keytool invocation to respond directly to
             * the password prompt rather an converting to String and sticking
             * on the command line.  In the meantime, this is a relatively low
             * security risk since it is only used in one-shot setup commands.
             */
            final String keyStorePasswordStr = new String(keyStorePassword);
            final String validityDays = props.getProperty(KEY_VALIDITY,
                                                          VALIDITY_DEFAULT);

            final String[] keyStoreCmds = new String[] {
                "keytool",
                "-genkeypair",
                "-keystore", keyStoreFile,
                "-storepass", keyStorePasswordStr,
                "-keypass", keyStorePasswordStr,
                "-alias", keyAlias,
                "-dname", dname,
                "-keyAlg", keyAlg,
                "-keysize", keySize,
                "-validity", validityDays };
            int result = runCmd(keyStoreCmds);
            if (result != 0) {
                System.err.println(
                    "Error creating keyStore: return code " + result);
                return false;
            }

            final String[] exportCertCmds = new String[] {
                "keytool",
                "-export",
                "-file", certFile,
                "-keystore", keyStoreFile,
                "-storepass", keyStorePasswordStr,
                "-alias", keyAlias };
            result = runCmd(exportCertCmds);

            if (result != 0) {
                System.err.println(
                    "Error exporting certificate: return code " + result);
                return false;
            }

            try {
                /*
                 * We will re-use the keystore password for the truststore
                 */
                final String[] importCertCmds = new String[] {
                    "keytool",
                    "-import",
                    "-file", certFile,
                    "-keystore", trustStoreFile,
                    "-storepass", keyStorePasswordStr,
                    "-noprompt" };
                result = runCmd(importCertCmds);

                if (result != 0) {
                    System.err.println(
                        "Error importing certificate to trustStore: " +
                        "return code " + result);
                    return false;
                }
            } finally {
                /* Delete the cert file when done - we no longer need it */
                new File(certFile).delete();
            }

            makeOwnerOnlyWriteAccess(new File(keyStoreFile));
            makeOwnerOnlyWriteAccess(new File(trustStoreFile));

        } catch (IOException ioe) {
            System.err.println("IO error encountered: " + ioe.getMessage());
            return false;
        }


        return true;
    }

    /**
     * Merges the trust information from srcSecDir into updateSecDir.
     * @param srcSecDir a File reference to the security directory from which
     *   trust information will be extracted
     * @param updateSecDir a File reference to the security directory into
     *   which trust information will be merged
     * @return true if the merge was successful and false otherwise
     */
    public static boolean mergeTrust(File srcSecDir,
                                     File updateSecDir) {

        /* Get source truststore info */
        final SecurityParams srcSp = loadSecurityParams(srcSecDir);
        final String srcTrustFile =
            new File(srcSecDir, srcSp.getTruststoreFile()).getPath();
        /*
         * TODO: converting to String here introduces some security risk.
         * Consider changing the keytool invocation to respond directly to
         * the password prompt rather an converting to String and sticking
         * on the command line.  In the meantime, this is a relatively low
         * security risk since it is only used in one-shot setup commands.
         */

        final String srcTruststorePwd =
            new String(retrieveKeystorePassword(srcSp));
        final List<KeystoreEntry> stEntries =
            listKeystore(new File(srcTrustFile), srcTruststorePwd);

        /* Get dest truststore info */
        final SecurityParams updateSp = loadSecurityParams(updateSecDir);
        final String updateTrustFile =
            new File(updateSecDir, updateSp.getTruststoreFile()).getPath();
        final String updateTruststorePwd =
            new String(retrieveKeystorePassword(updateSp));
        final List<KeystoreEntry> utEntries =
            listKeystore(new File(updateTrustFile), updateTruststorePwd);

        /*
         * Convert the to-be-updated list to a set of alias names for
         * ease of later access.
         */
        final Set<String> utAliasSet = new HashSet<String>();
        for (KeystoreEntry entry : utEntries) {
            utAliasSet.add(entry.getAlias());
        }

        /* The file to hold the temporary cert */
        final String certFileName = TEMP_CERT_FILE;
        final String certFile =
            new File(srcSecDir.getPath(), certFileName).getPath();

        try {

            for (KeystoreEntry entry : stEntries) {
                final String[] exportCertCmds = new String[] {
                    "keytool",
                    "-export",
                    "-file", certFile,
                    "-keystore", srcTrustFile,
                    "-storepass", srcTruststorePwd,
                    "-alias", entry.getAlias() };
                int result = runCmd(exportCertCmds);

                if (result != 0) {
                    System.err.println(
                        "Error exporting certificate: return code " + result);
                    return false;
                }

                /*
                 * Determine an available alias
                 */
                String alias = entry.getAlias();
                if (utAliasSet.contains(alias)) {
                    int i = 2;
                    while (true) {
                        final String tryAlias = alias + "_" + i;
                        if (!utAliasSet.contains(tryAlias)) {
                            alias = tryAlias;
                            break;
                        }
                        i++;
                    }
                }
                utAliasSet.add(alias);

                final String[] importCertCmds = new String[] {
                    "keytool",
                    "-import",
                    "-file", certFile,
                    "-alias", alias,
                    "-keystore", updateTrustFile,
                    "-storepass", updateTruststorePwd,
                    "-noprompt" };
                result = runCmd(importCertCmds);

                if (result != 0) {
                    System.err.println(
                        "Error importing certificate to trustStore: " +
                        "return code " + result);
                    return false;
                }
            }
        } catch (IOException ioe) {
            System.err.println(
                "Exception " + ioe + " while merging truststore files");
            return false;
        }

        /*
         * Copy the new truststore file to a client.trust file so that the
         * two are consistent.
         */
        final File srcFile = new File(updateTrustFile);
        final File dstFile =
            new File(updateSecDir, FileNames.CLIENT_TRUSTSTORE_FILE);
        try {
            SecurityUtils.copyOwnerWriteFile(srcFile, dstFile);
        } catch (IOException ioe) {
            System.err.println(
                "Exception " + ioe + " while copying " + srcFile +
                " to " + dstFile);
            return false;
        }

        return true;
    }

    public static class KeystoreEntry {
        private final String alias;
        private final EntryType entryType;

        public enum EntryType {
            PRIVATE_KEY,
            SECRET_KEY,
            TRUSTED_CERT,
            OTHER;
        }

        public KeystoreEntry(String alias, EntryType entryType) {
            this.alias = alias;
            this.entryType = entryType;
        }

        String getAlias() {
            return alias;
        }

        EntryType getEntryType() {
            return entryType;
        }
    }

    /**
     * List the entries in a keystore (or truststore) file.
     * @param keystoreFile the keystore file
     * @param storePassword the password for the store
     * @return a list of the keystore entries if successful, or null otherwise
     */
    public static List<KeystoreEntry> listKeystore(File keystoreFile,
                                                   String storePassword) {
        try {
            final String[] keyStoreCmds = new String[] {
                "keytool",
                "-list",
                "-keystore", keystoreFile.getPath(),
                "-storepass", storePassword };

            final List<String> output = new ArrayList<String>();
            final int result = runCmd(keyStoreCmds, output);
            if (result != 0) {
                System.err.println(
                    "Error listing keyStore: return code " + result);
                return null;
            }

            final Pattern keystoreContains =
                Pattern.compile("Your keystore contains ([0-9]+) entr.*");

            /*
             * Entries look like this:
             * shared, Dec 31, 2013, PrivateKeyEntry,
             */
            final Pattern entryPattern =
                Pattern.compile("([^,]+),([^,]+, [0-9]+, )([a-zA-Z]+),.*");

            final List<KeystoreEntry> entries = new ArrayList<KeystoreEntry>();
            boolean startFound = false;
            for (String s : output) {
                if (!startFound) {
                    final Matcher m = keystoreContains.matcher(s);
                    if (m.matches()) {
                        startFound = true;
                    }
                } else {
                    final Matcher m = entryPattern.matcher(s);
                    if (m.matches()) {
                        final String entryTypeStr = m.group(3);
                        final KeystoreEntry.EntryType entryType;
                        if (entryTypeStr.equals(KS_PRIVATE_KEY_ENTRY)) {
                            entryType = KeystoreEntry.EntryType.PRIVATE_KEY;
                        } else if (entryTypeStr.equals(KS_SECRET_KEY_ENTRY)) {
                            entryType = KeystoreEntry.EntryType.SECRET_KEY;
                        } else if (entryTypeStr.equals(KS_TRUSTED_CERT_ENTRY)) {
                            entryType = KeystoreEntry.EntryType.TRUSTED_CERT;
                        }  else {
                            entryType = KeystoreEntry.EntryType.OTHER;
                        }
                        entries.add(new KeystoreEntry(m.group(1), entryType));
                    }
                }
            }
            return entries;
        } catch (IOException ioe) {
            System.err.println("IO error encountered: " + ioe.getMessage());
            return null;
        }
    }

    /**
     * Make a copy of a file where the resulting copy should be writable only
     * by the owner with read privilege determined by system policy
     * (i.e. umask).
     * @param srcFile a file to be copied
     * @param destFile the destination file
     * @throws IOException if an error occurs in the copy process
     */
    public static void copyOwnerWriteFile(File srcFile, File destFile)
        throws IOException {

        FileUtils.copyFile(srcFile, destFile);
        SecurityUtils.makeOwnerOnlyWriteAccess(destFile);
    }

    /**
     * Create store service principal and extract keytab file.
     *
     * @param securityDir the directory in which the keytab will be created.
     * @param sp a SecurityParams instance containing information regarding
     *        the store service principal and keytab file names
     * @param kadminSetting kadmin settings
     * @param props a set of optional settings that can alter the
     *        principal creation, or null
     * @param ioHelper I/O helper class used to read kadmin password
     * @return true if the generation process was successful and false
     *         if an error occurred.
     */
    public static boolean generateKeyTabFile(File securityDir,
                                             SecurityParams sp,
                                             KadminSetting kadminSetting,
                                             Properties props,
                                             IOHelper ioHelper) {
        if (props == null) {
            props = new Properties();
        }

        final String keytabFile = new File(
            securityDir.getPath(), sp.getKerberosKeytabFile()).getPath();

        try {
            final String princName = sp.getKerberosServiceName();
            final String validityDays = props.getProperty(PRINCIPAL_VALIDITY);
            final String keysaltList = props.getProperty(KEYSALT_LIST);
            final String pwdExpire = props.getProperty(PRINCIPAL_PWD_EXPIRE);
            final String instance = sp.getKerberosInstanceName();
            final String realm = sp.getKerberosRealmName();
            final String principal = (instance != null) ?
                                     princName + "/" + instance :
                                     princName;

            final List<String> kadminCmdsList =
                generateKadminCmds(kadminSetting, realm);

            /* Add store service principal */
            final String addPrincCmds = "add_principal" +
                " -expire " + validityDays +
                " -pwexpire " + pwdExpire +
                " -randkey " + "\"" + principal + "\"";
            kadminCmdsList.add(addPrincCmds);

            System.out.println("Adding principal " + principal);
            int result = runKadminCmd(kadminSetting, ioHelper, kadminCmdsList);
            if (result != 0) {
                System.err.println(
                    "Error adding service principal: return code " + result);
                return false;
            }
            kadminCmdsList.remove(kadminCmdsList.size() - 1);

            /* Extract keytab of service principal */
            System.out.println("Extracting keytab " + keytabFile);
            final String extractKeytabCmds = "ktadd" +
                " -k " + keytabFile +
                " -e " + keysaltList +
                " " + "\"" + principal + "\"";
            kadminCmdsList.add(extractKeytabCmds);
            result = runKadminCmd(kadminSetting, ioHelper, kadminCmdsList);
            if (result != 0) {
                System.err.println(
                    "Error extracting keytab file: return code " + result);
                return false;
            }

            makeOwnerOnlyWriteAccess(new File(keytabFile));
        } catch (IOException ioe) {
            System.err.println("IO error encountered: " + ioe.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Renew keytab file in given security directory.
     *
     * @param secDir security directory
     * @param keysaltList keysalt list used to generate new keytab file
     * @param kadminSetting settings for connecting kadmin
     * @param ioHelper I/O helper class to read kadmin password
     * @return true if the renew process was successful and false
     *         if an error occurred.
     */
    public static boolean renewKeytab(File secDir,
                                      String keysaltList,
                                      KadminSetting kadminSetting,
                                      IOHelper ioHelper) {
        final SecurityParams sp = loadSecurityParams(secDir);
        final File keytabFile = new File(secDir, sp.getKerberosKeytabFile());
        if (!keytabFile.exists()) {
            System.err.println("keytab " + keytabFile + " does not exist");
            return false;
        }
        final String principal = SecurityUtils.getCanonicalPrincName(sp);
        File tmpKeytab = null;
        try {
            final List<String> kadminCmdsList =
                generateKadminCmds(kadminSetting, sp.getKerberosRealmName());
            if (keysaltList == null) {
                keysaltList = KEYSALT_LIST_DEFAULT;
            }

            /*
             * Create a temporary file to store the new keys of principal,
             * so that old key can be reserved in case of extracting keytab
             * error.
             *
             * Using createTempFile method to create temporary file then
             * remove it and only keep the file name, since ktadd command
             * cannot store keys to a file generated by Java.
             */
            tmpKeytab = File.createTempFile("tmp", ".keytab");
            if (!tmpKeytab.delete()) {
                System.err.println("Error generating a temporary keytab file");
                return false;
            }
            final String extractKeytabCmds = "ktadd" +
                " -k " + tmpKeytab.getAbsolutePath() +
                " -e " + keysaltList +
                " " + principal;
            kadminCmdsList.add(extractKeytabCmds);
            final int result =
                runKadminCmd(kadminSetting, ioHelper, kadminCmdsList);
            if (result != 0) {
                System.err.println(
                    "Error extracting keytab file: return code " + result);
                return false;
            }
            if (!keytabFile.delete()) {
                System.err.println("Old keytab " + keytabFile +
                                   " cannot be deleted");
                return false;
            }
            if (!tmpKeytab.renameTo(keytabFile)) {
                System.err.println("keytab " + tmpKeytab +
                                   " cannot be renamed as " + keytabFile);
                return false;
            }
            makeOwnerOnlyWriteAccess(keytabFile);
        } catch (IOException ioe) {
            System.err.println("IO error encountered: " + ioe.getMessage());
            return false;
        } finally {
            if (tmpKeytab != null &&
                tmpKeytab.exists() &&
                !tmpKeytab.delete()) {
                System.err.println("Temporary keytab " + tmpKeytab +
                                   " cannot be deleted");
                return false;
            }
        }
        return true;
    }

    /**
     * Check given authentication method name is Kerberos.
     */
    public static boolean isKerberos(String authMethod) {
        if (authMethod == null) {
            return false;
        }
        return authMethod.equalsIgnoreCase(KERBEROS_AUTH_NAME);
    }

    /**
     * Check given authentication methods in format "authMethod1,authMethod2"
     * contains Kerberos.
     */
    public static boolean hasKerberos(String authMethods) {
        if (authMethods == null) {
            return false;
        }

        for (String method : authMethods.split(",")) {
            if (isKerberos(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check given authentication methods array contains element of Kerberos.
     */
    public static boolean hasKerberos(String[] authMethods) {
        if (authMethods == null) {
            return false;
        }

        for (String method : authMethods) {
            if (isKerberos(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return canonical Kerberos service principal.
     */
    public static String getCanonicalPrincName(SecurityParams secParams) {
        final StringBuilder sb = new StringBuilder();
        sb.append(secParams.getKerberosServiceName());

        String instanceName = secParams.getKerberosInstanceName();
        if (instanceName != null && !instanceName.equals("")) {
            sb.append(KRB_NAME_COMPONENT_SEPARATOR_STR);
            sb.append(secParams.getKerberosInstanceName());
        }

        if (!secParams.getKerberosRealmName().equals("")) {
            sb.append(KRB_NAME_REALM_SEPARATOR_STR);
            sb.append(secParams.getKerberosRealmName());
        }
        return sb.toString();
    }

    private static List<String> generateKadminCmds(KadminSetting kadminSetting,
                                                   String defaultRealm) {
        final List<String> kadminCmdsList = new ArrayList<String>();
        kadminCmdsList.add(kadminSetting.getKrbAdminPath());
        kadminCmdsList.add("-r");
        kadminCmdsList.add(defaultRealm);

        if (kadminSetting.useKeytab()) {
            kadminCmdsList.add("-k");
            kadminCmdsList.add("-t");
            kadminCmdsList.add(kadminSetting.getKrbAdminKeytab());
            System.out.println(String.format(
                "Login Kerberos admin via keytab %s with %s",
                kadminSetting.getKrbAdminKeytab(),
                kadminSetting.getKrbAdminPrinc()));
        } else if (kadminSetting.useCcache()) {
            kadminCmdsList.add("-c");
            kadminCmdsList.add(kadminSetting.getKrbAdminCcache());
            System.out.println(String.format(
                "Login Kerberos admin via credential cache %s with %s",
                kadminSetting.getKrbAdminCcache(),
                kadminSetting.getKrbAdminPrinc()));
        }

        if (kadminSetting.getKrbAdminPrinc() != null) {
            kadminCmdsList.add("-p");
            kadminCmdsList.add(kadminSetting.getKrbAdminPrinc());
        }
        kadminCmdsList.add("-q");
        return kadminCmdsList;
    }

    /**
     * The class maintain Kerberos V5 configuration information that retrieved 
     * from user specified configuration files. Copy the approach used by the
     * Java internal Kerberos parsing code to retrieve and validate default
     * realm and kdc, which is required for NoSQL Kerberos authentication.
     * The rest of configuration parameters are validated by Java Kerberos
     * login module when performing the actual authentication.
     */
    public static class Krb5Config {
        private File configFile;
        private String defaultRealm;
        private String realmKdc;

        public Krb5Config(File krb5ConfFile) {
            configFile = krb5ConfFile;
        }

        /**
         * Parse krb5 configuration file and identify default realm and
         * corresponding kdc.
         */
        public void parseConfigFile() throws IOException {
            final List<String> lines = loadConfigFile();
            final Map<String, String> kdcs = new HashMap<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                /* Find default realm from libdefaults */
                if (line.equalsIgnoreCase("[libdefaults]")) {
                    for (int count = i + 1; count < lines.size(); count++) {
                        line = lines.get(count).trim();

                        final int equalsPos = line.indexOf('=');
                        if (equalsPos > 0) {
                            final String key =
                                line.substring(0, equalsPos).trim();

                            if (key.equalsIgnoreCase("default_realm")) {
                                defaultRealm =
                                    trimmed(line.substring(equalsPos + 1));
                            }
                        }
                        if (lines.get(count).startsWith("[")) {
                            i = count - 1;
                            break;
                        }
                    }
                } else if (line.equalsIgnoreCase("[realms]")) {
                    /* Parse all realms and cache their corresponding kdc */
                    String realm = "";

                    for (int count = i + 1; count < lines.size(); count++) {
                        line = lines.get(count).trim();
                        if (line.endsWith("{")) {
                            final int equalsPos = line.indexOf('=');
                            if (equalsPos > 0) {
                                realm = line.substring(0, equalsPos).trim();
                            }
                        } else if (!line.startsWith("}")) {
                            final int equalsPos = line.indexOf('=');
                            if (equalsPos > 0) {
                                final String key =
                                    line.substring(0, equalsPos).trim();

                                if (key.equalsIgnoreCase("kdc") &&
                                    !realm.equals("")) {
                                    /*
                                     * User can specify multiple realms in
                                     * the configuration file, cache them
                                     * firstly and find the default kdc
                                     * later.
                                     */
                                    kdcs.put(realm, trimmed(
                                        line.substring(equalsPos + 1)));
                                }
                            }
                        }

                        if (lines.get(count).startsWith("[")) {
                            i = count - 1;
                            break;
                        }
                    }
                }
            }

            if (defaultRealm != null) {
                realmKdc = kdcs.get(defaultRealm);
            }
        }

        public String getDefaultRealm() {
            return defaultRealm;
        }

        public String getKdc() {
            return realmKdc;
        }

        public String getConfigFilePath() {
            return configFile.getAbsolutePath();
        }

        private String trimmed(String s) {
            s = s.trim();
            if (s.charAt(0) == '"' && s.charAt(s.length()-1) == '"' ||
                s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\'') {
                s = s.substring(1, s.length()-1).trim();
            }
            return s;
        }

        private List<String> loadConfigFile() throws IOException {
            final List<String> lines = new ArrayList<String>();

            try (final BufferedReader br = new BufferedReader(
                     new InputStreamReader(new FileInputStream(configFile)))) {
                String line;

                /*
                 * Cache previous line, used to resolve the case that Kerberos
                 * configuration file accepts and convert to standard format.
                 *  EXAMPLE.COM =
                 *  {
                 *      kdc = kerberos.example.com
                 *  }
                 */
                String previous = null;

                while ((line = br.readLine()) != null) {
                    /* Ignore comments and blank lines */
                    if (!(line.startsWith("#") || line.trim().isEmpty())) {
                        String current = line.trim();

                        if (current.startsWith("{")) {
                            if (previous == null) {
                                throw new IOException(
                                     "Config file should not start with \"{\"");
                            }
                            previous += " " + current;
                        } else {
                            if (previous != null) {
                                lines.add(previous);
                            }
                            previous = current;
                        }
                    }
                }

                if (previous != null) {
                    lines.add(previous);
                }
                return lines;
            }
        }
    }

    /**
     * The class maintains the setting used to connecting kadmin utility.
     */
    public static class KadminSetting {
        private String krbAdminPath = KADMIN_DEFAULT;
        private String krbAdminPrinc;
        private String krbAdminKeytab;
        private String krbAdminCcache;

        public KadminSetting setKrbAdminPath(String kadminPath) {
            this.krbAdminPath = kadminPath;
            return this;
        }

        public String getKrbAdminPath() {
            return krbAdminPath;
        }

        public KadminSetting setKrbAdminPrinc(String kadminPrinc) {
            this.krbAdminPrinc = kadminPrinc;
            return this;
        }

        public String getKrbAdminPrinc() {
            return krbAdminPrinc;
        }

        public KadminSetting setKrbAdminKeytab(String adminKeytab) {
            this.krbAdminKeytab = adminKeytab;
            return this;
        }

        public String getKrbAdminKeytab() {
            return krbAdminKeytab;
        }

        public KadminSetting setKrbAdminCcache(String adminCcache) {
            this.krbAdminCcache = adminCcache;
            return this;
        }

        public String getKrbAdminCcache() {
            return krbAdminCcache;
        }

        /**
         * Validate if given kadmin settings are appropriate.
         *
         * @throws IllegalArgumentException
         */
        public void validateKadminSetting()
            throws IllegalArgumentException {
            /*
             * Check if user specified admin keytab and credential cache at the
             * same time 
             */
            if (krbAdminKeytab != null) {
                if (krbAdminCcache != null) {
                    throw new IllegalArgumentException(
                        "cannot use admin ketyab and credential cache together");
                }

                if (krbAdminPrinc == null) {
                    throw new IllegalArgumentException(
                        "must specify admin principal when using keytab file");
                }

                if (!new File(krbAdminKeytab).exists()) {
                    throw new IllegalArgumentException(
                        "keytab file " + krbAdminKeytab + " does not exist");
                }
            }

            /* check if kadmin ccache exists */
            if (krbAdminCcache != null && !new File(krbAdminCcache).exists()) {
                throw new IllegalArgumentException(
                    "credential cache " + krbAdminCcache + " does not exist");
            }

            /* Must specify principal if use password */
            if (krbAdminKeytab == null &&
                krbAdminCcache == null &&
                krbAdminPrinc == null) {
                throw new IllegalArgumentException("use kadmin with password " +
                    "must specify principal name");
            }
        }

        /**
         * Whether use keytab to connect kadmin utility.
         */
        public boolean useKeytab() {
            return krbAdminKeytab != null &&
                   krbAdminPrinc != null &&
                   krbAdminCcache == null;
        }

        /**
         * Whether use credential cache to connect kadmin utility.
         */
        public boolean useCcache() {
            return krbAdminCcache != null &&
                   krbAdminKeytab == null;
        }

        /**
         * Whether prompt password to connect kadmin utility.
         */
        public boolean promptPwd() {
            return krbAdminCcache == null && krbAdminKeytab == null;
        }
    }

    /**
     * Run a command in a subshell.
     * @param args an array of command-line arguments, in the format expected
     * by Runtime.exec(String[]).
     * @return the process exit code
     * @throw IOException if an IO error occurs during the exec process
     */
    static int runCmd(String[] args)
        throws IOException {
        final Process proc = Runtime.getRuntime().exec(args);

        boolean done = false;
        int returnCode = 0;
        while (!done) {
            try {
                returnCode = proc.waitFor();
                done = true;
            } catch (InterruptedException e) /* CHECKSTYLE:OFF */ {
            } /* CHECKSTYLE:ON */
        }
        return returnCode;
    }

    /**
     * Run kadmin-related commands. If users do not specify keytab or credential
     * cache for kadmin user, prompt for admin user password.
     *
     * @param setting settings for connecting kadmin utility
     * @param ioHelper I/O helper may be used for password prompt
     * @param args an list of command line arguments.
     * @return the process exit code
     * @throws IOException if an IO error occurs during the process execution.
     */
    private static int runKadminCmd(KadminSetting setting,
                                    IOHelper ioHelper,
                                    List<String> args)
        throws IOException {

        final List<String> output = new ArrayList<>();
        final ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        char[] pwd = null;

        final Process proc = pb.start();
        final BufferedReader br =
            new BufferedReader(new InputStreamReader(proc.getInputStream()));

        if (setting.promptPwd()) {
            final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(proc.getOutputStream()));

            pwd = ioHelper.readPassword(
                "Password for " + setting.getKrbAdminPrinc() + ": ");
            if (pwd == null) {
                System.err.println("Failed to acquire kadmin password");
            }
            writer.write(pwd);
            SecurityUtils.clearPassword(pwd);
            writer.write("\n");
            writer.flush();
        }

        /* Read lines of input */
        boolean done = false;
        while (!done) {
            final String s = br.readLine();
            if (s == null) {
                done = true;
            } else {
                output.add(s);
            }
        }

        /* Then get exit code */
        done = false;
        int returnCode = 0;
        while (!done) {
            try {
                returnCode = proc.waitFor();
                done = true;
            } catch (InterruptedException e) /* CHECKSTYLE:OFF */ {
            } /* CHECKSTYLE:ON */
        }

        /* Output kadmin error and std output for easier debugging */
        for (String s : output) {
            System.err.println(s);
        }
        return returnCode;
    }

    private static int runCmd(String[] args, List<String> output)
        throws IOException {

        final Process proc = Runtime.getRuntime().exec(args);
        final BufferedReader br =
            new BufferedReader(new InputStreamReader(proc.getInputStream()));

        /* Read lines of input */
        boolean done = false;
        while (!done) {
            final String s = br.readLine();
            if (s == null) {
                done = true;
            } else {
                output.add(s);
            }
        }

        /* Then get exit code */
        done = false;
        int returnCode = 0;
        while (!done) {
            try {
                returnCode = proc.waitFor();
                done = true;
            } catch (InterruptedException e) /* CHECKSTYLE:OFF */ {
            } /* CHECKSTYLE:ON */
        }

        return returnCode;
    }

    /**
     * Report whether the host parameter is an address that is local to this
     * machine. If the host is a name rather than a literal address, all
     * resolutions of the name must be local in order for the host to be
     * considered local.
     *
     * @param host either an IP address literal or host name
     * @return true it the host represents a local address
     * @throws SocketException if an IO exception occurs
     */
    public static boolean isLocalHost(String host)
        throws SocketException {

        try {
            boolean anyLocal = false;
            for (InetAddress hostAddr : InetAddress.getAllByName(host)) {
                if (isLocalAddress(hostAddr)) {
                    anyLocal = true;
                } else {
                    return false;
                }
            }
            return anyLocal;
        } catch (UnknownHostException uhe) {
            return false;
        }
    }

    /**
     * Determine whether the address portion of the InetAddress (host name is
     * ignored) is an address that is local to this machine.
     */
    private static boolean isLocalAddress(InetAddress address)
        throws SocketException {

        final Enumeration<NetworkInterface> netIfs =
            NetworkInterface.getNetworkInterfaces();
        while (netIfs.hasMoreElements()) {
            final NetworkInterface netIf = netIfs.nextElement();
            if (isLocalAddress(netIf, address)) {
                return true;
            }

            final Enumeration<NetworkInterface> subIfs =
                netIf.getSubInterfaces();
            while (subIfs.hasMoreElements()) {
                if (isLocalAddress(subIfs.nextElement(), address)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine whether the address portion of the InetAddress (host name is
     * ignored) is an address that is local to a network interface.
     */
    private static boolean isLocalAddress(NetworkInterface netIf,
                                          InetAddress address) {

        final Enumeration<InetAddress> addrs = netIf.getInetAddresses();
        while (addrs.hasMoreElements()) {
            final InetAddress addr = addrs.nextElement();
            if (addr.equals(address)) {
                return true;
            }
        }
        return false;
    }

    public static SecurityParams loadSecurityParams(File secDir) {
        final File secFile = new File(secDir, FileNames.SECURITY_CONFIG_FILE);
        return ConfigUtils.getSecurityParams(secFile);
    }

    private static char[] retrieveKeystorePassword(SecurityParams sp) {
        final KeyStorePasswordSource pwdSrc = KeyStorePasswordSource.create(sp);
        return (pwdSrc == null) ? null : pwdSrc.getPassword();
    }

    /**
     * Constructs a resource owner from KVStore user in current context.  Null
     * will be return if we could not detect a user principal in current
     * execution context, or the current execution context is null.
     */
    public static ResourceOwner currentUserAsOwner() {
        final KVStoreUserPrincipal currentUserPrinc =
            KVStoreUserPrincipal.getCurrentUser();
        if (currentUserPrinc == null) {
            return null;
        }
        return new ResourceOwner(currentUserPrinc.getUserId(),
                                 currentUserPrinc.getName());
    }

    /**
     * Return the default Kerberos principal configuration properties. These
     * properties are used whiling adding principal and extracting keytab.
     */
    public static Properties getDefaultKrbPrincipalProperties() {
        return (Properties) princDefaultProps.clone();
    }
}
