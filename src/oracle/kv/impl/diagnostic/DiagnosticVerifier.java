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

package oracle.kv.impl.diagnostic;

/**
 * Encapsulates a definition and mechanism for verifying arguments or 
 * parameters of NoSQL are valid or not against environment and requirement. 
 * Subclasses of DiagnosticVerifier will define the different types of 
 * DiagnosticVerifier that can be carried out.
 */

public abstract class DiagnosticVerifier {
    
    /* Return immediately when get error message */
    protected boolean returnOnError;
    
    /**
     * Work of verification is done in this method
     */
    public abstract boolean doWork();
    
    public DiagnosticVerifier(boolean returnOnError) {
        this.returnOnError = returnOnError;
    }

    /**
     * Print out error message
     * 
     * @param errorMsg 
     */
    public void printMessage(String errorMsg) {
         if (errorMsg != null) {
             System.err.println(errorMsg);
         }
    }
    
    /**
     * Do verification and return its result
     * 
     * @return result of verification
     */
    public boolean verify() {
        return doWork();
    }
}
