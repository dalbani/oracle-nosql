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

package oracle.kv.impl.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.security.auth.DestroyFailedException;

import oracle.kv.FaultException;
import oracle.kv.impl.admin.param.SecurityParams;
import oracle.kv.impl.security.ssl.KeyStorePasswordSource;
import oracle.kv.impl.security.util.SecurityUtils;
import oracle.kv.impl.topo.Topology;

/**
 * A class helping sign or verify a signature for a topology object.
 */
public class TopoSignatureHelper implements SignatureHelper<Topology> {

    private final static String SIG_PRIVATE_KEY_ALIAS_DEFAULT =
        SecurityUtils.KEY_ALIAS_DEFAULT;
    private final static String SIG_PUBLIC_KEY_ALIAS_DEFAULT = "mykey";
    private final static String SIG_ALGORITHM_DEFAULT = "SHA256withRSA";

    /* Properties specified to access keys from kvstore security keystore */
    private final KeyStore keyStore;
    private final KeyStore certStore;
    private final String privKeyAlias;
    private final String certAlias;
    private final KeyStorePasswordSource ksPwdSource;

    private final Signature signature;

    /* Public key can be cached in memory */
    private PublicKey publicKey;

    /**
     * Builds a TopoSignatureHelper from the specified security params.
     */
    public static TopoSignatureHelper
        buildFromSecurityParams(SecurityParams sp) {

        if (sp == null) {
            throw new IllegalArgumentException(
                "Security params must not be null");
        }

        String keyAlias = sp.getKeystoreSigPrivateKeyAlias();
        if (keyAlias == null) {
            keyAlias = SIG_PRIVATE_KEY_ALIAS_DEFAULT;
        }

        String certAlias = sp.getTruststoreSigPublicKeyAlias();
        if (certAlias == null) {
            certAlias = SIG_PUBLIC_KEY_ALIAS_DEFAULT;
        }

        final KeyStorePasswordSource pwdSrc =
            KeyStorePasswordSource.create(sp);
        if (pwdSrc == null) {
            throw new IllegalArgumentException(
                "Unable to create keystore password source");
        }

        final String keyStoreName =
            sp.getConfigDir() + File.separator + sp.getKeystoreFile();
        final String certStoreName =
            sp.getConfigDir() + File.separator + sp.getTruststoreFile();

        final KeyStore keyStore;
        final KeyStore certStore;
        char[] ksPwd = null;

        try {
            ksPwd = pwdSrc.getPassword();
            keyStore = loadStore(keyStoreName, ksPwd, "keystore",
                                 sp.getKeystoreType());
            certStore = loadStore(certStoreName, ksPwd, "truststore",
                                  sp.getTruststoreType());

            String sigAlgorithm = sp.getSignatureAlgorithm();
            if (sigAlgorithm == null || sigAlgorithm.isEmpty()) {
                sigAlgorithm = SIG_ALGORITHM_DEFAULT;
            }

            return new TopoSignatureHelper(sigAlgorithm, keyStore, keyAlias,
                                           certStore, certAlias, pwdSrc);

        } finally {
            SecurityUtils.clearPassword(ksPwd);
        }
    }

    private static KeyStore loadStore(String storeName,
                                      char[] storePassword,
                                      String storeFlavor,
                                      String storeType)
        throws IllegalArgumentException {

        if (storeType == null || storeType.isEmpty()) {
            storeType = KeyStore.getDefaultType();
        }

        final KeyStore ks;
        try {
            ks = KeyStore.getInstance(storeType);
        } catch (KeyStoreException kse) {
            throw new IllegalArgumentException(
                "Unable to find a " + storeFlavor + " instance of type " +
                storeType, kse);
        }

        final FileInputStream fis;
        try {
            fis = new FileInputStream(storeName);
        } catch (FileNotFoundException fnfe) {
            throw new IllegalArgumentException(
                "Unable to locate specified " + storeFlavor + " " + storeName,
                fnfe);
        }

        try {
            ks.load(fis, storePassword);
        } catch (IOException ioe) {
            throw new IllegalArgumentException(
                "Error reading from " + storeFlavor + " file " + storeName,
                ioe);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException(
                "Unable to check " + storeFlavor + " integrity: " + storeName,
                nsae);
        } catch (CertificateException ce) {
            throw new IllegalArgumentException(
                "Not all certificates could be loaded: " + storeName,
                ce);
        } finally {
            try {
                fis.close();
            } catch (IOException ioe) {
                /* ignored */
            }
        }
        return ks;
    }

    private TopoSignatureHelper(String sigAlgorithm,
                                KeyStore keyStore,
                                String privKeyAlias,
                                KeyStore certStore,
                                String certAlias,
                                KeyStorePasswordSource ksPwdSource) {
        try {
            signature = Signature.getInstance(sigAlgorithm);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException(
                "Unrecognized signature algorithm: " + sigAlgorithm);
        }

        this.keyStore = keyStore;
        this.privKeyAlias = privKeyAlias;
        this.certStore = certStore;
        this.certAlias = certAlias;
        this.ksPwdSource = ksPwdSource;
    }

    @Override
    public byte[] sign(Topology topo) throws SignatureFaultException {
        final byte[] topoSerialBytes;

        try {
            topoSerialBytes = topo.toByteArrayForSignature();
        } catch (IOException ioe) {
            throw new SignatureFaultException(
                "Failed to get topology bytes",
                ioe);
        }

        synchronized(signature) {
            try {
                signature.initSign(getPrivateKey());
                signature.update(topoSerialBytes);
                return signature.sign();
            } catch (InvalidKeyException ike) {
                throw new SignatureFaultException(
                    "Private key used to generate signature is invalid",
                    ike);
            } catch (KeyAccessException kae) {
                throw new SignatureFaultException(
                    "Failed to access private key",
                    kae);
            } catch (SignatureException se) {
                throw new SignatureFaultException(
                    "Problem while attempting to sign topology",
                    se);
            }
        }
    }

    @Override
    public boolean verify(Topology topo, byte[] sigBytes)
        throws SignatureFaultException {

        final byte[] topoSerialBytes;

        try {
            topoSerialBytes = topo.toByteArrayForSignature();
        } catch (IOException ioe) {
            throw new SignatureFaultException(
                "Failed to get topology bytes",
                ioe);
        }

        synchronized(signature) {
            try {
                signature.initVerify(getPublicKey());
                signature.update(topoSerialBytes);
                return signature.verify(sigBytes);
            } catch (InvalidKeyException ike) {
                throw new SignatureFaultException(
                    "Public key used to verify signature is invalid",
                    ike);
            } catch (KeyAccessException kae) {
                throw new SignatureFaultException(
                    "Failed to access public key",
                    kae);
            } catch (SignatureException se) {
                throw new SignatureFaultException(
                    "Problem while attempting to verify topology",
                    se);
            }
        }
    }

    /**
     * Returns private key
     *
     * @throws KeyAccessException if any issue happened in getting private
     * key
     */
    private PrivateKey getPrivateKey() throws KeyAccessException {
        char[] ksPassword = null;
        PasswordProtection pwdParam = null;

        try {
            /*
             * We read password each time so as not to keep an in-memory
             * copy. This sacrifices efficiency, but is safer.
             */
            ksPassword = ksPwdSource.getPassword();
            pwdParam = new PasswordProtection(ksPassword);

            final PrivateKeyEntry pkEntry =
                (PrivateKeyEntry) keyStore.getEntry(privKeyAlias,
                                                    pwdParam);
            if (pkEntry == null) {
                throw new KeyAccessException(
                    "Could not find private key entry with alias of " +
                    privKeyAlias);
            }
            return pkEntry.getPrivateKey();

        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyAccessException(
                "Unable to recover private key entry from keystore",
                nsae);
        } catch (UnrecoverableEntryException uee) {
            throw new KeyAccessException(
                "Password parameter is invalid or insufficent to " +
                "recover private key entry from keystore",
                uee);
        } catch (KeyStoreException kse) {
            throw new KeyAccessException(
                "Keystore is not loaded or initialized",
                kse);
        } finally {
            SecurityUtils.clearPassword(ksPassword);
            if (pwdParam != null) {
                try {
                    pwdParam.destroy();
                } catch (DestroyFailedException e) {
                    /* Ignore */
                }
            }
        }
    }

    /**
     * Returns public key
     *
     * @throws KeyAccessException if any issue happened in getting public
     * key
     */
    private PublicKey getPublicKey() throws KeyAccessException {
        if (publicKey == null) {
            try {
                final Certificate cert =
                    certStore.getCertificate(certAlias);
                if (cert == null) {
                    throw new KeyAccessException(
                        "Could not find certificate with alias of " +
                        certAlias + " or other");
                }
                publicKey = cert.getPublicKey();
            } catch (KeyStoreException kse) {
                throw new KeyAccessException(
                    "Certificate store is not loaded or initialized",
                    kse);
            }
        }
        return publicKey;
    }

    /*
     * Exception indicating problem encountered while accessing key
     */
    private static class KeyAccessException extends FaultException {

        private static final long serialVersionUID = 1L;

        public KeyAccessException(String msg, Throwable cause) {
            super(msg, cause, false /* isRemote */);
        }

        public KeyAccessException(String msg) {
            super(msg, false /* isRemote */);
        }
    }
}
