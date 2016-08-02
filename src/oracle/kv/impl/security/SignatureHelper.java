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

/**
 * A common interface implemented by object signature signer and verifier.
 *
 * @param <T> type of object to be performed with signature operations
 */
public interface SignatureHelper<T> {

    /**
     * Returns an array of bytes representing the signature of an object
     *
     * @param object
     * @throws SignatureFaultException if any issue happens in generating the
     * signature
     */
    byte[] sign(T object) throws SignatureFaultException;

    /**
     * Verifies the integrity of the specified object using the given
     * signature.
     *
     * @param object
     * @param sigBytes signature to be verified
     * @return true if the signature was verified, false if not.
     * @throws SignatureFaultException f a configuration problem prevented
     * signature verification from being performed
     */
    boolean verify(T object, byte[] sigBytes) throws SignatureFaultException;
}
