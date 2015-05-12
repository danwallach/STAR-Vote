/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package auditorium;

import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.adder.AdderPublicKeyShare;

/**
 * The auditorium integrity layer interfaces with an instance of this type in
 * order to access keys that are stored as files on the disk.
 * 
 * @author Kyle Derr
 */
public interface IKeyStore {

    /**
     * Load the public encryption key (the key for the entire election)
     * @return
     * @throws AuditoriumCryptoException
     */
    public AdderPublicKey loadPEK() throws AuditoriumCryptoException;

    /**
     * Load the private key associated with a given ID.
     * 
     * @return This method returns the private key of the ID that was asked for.
     * @throws AuditoriumCryptoException Thrown if the file can't be found or if it isn't in the correct format.
     */
    Key loadKey(String nodeID) throws AuditoriumCryptoException;

    /**
     * Load a PEM certificate from a file.
     * 
     * @param nodeID            Load this node's certificate.
     * @return                  This method returns the certificate that was loaded from the given file.
     */
    Certificate loadCert(String nodeID) throws AuditoriumCryptoException;
    
    /**
     * Load the adder public key associated with the given ID.
     */
    AdderPublicKeyShare loadAdderPublicKeyShare() throws RuntimeException;

    /**
     * Load the adder private key associated with the given ID.
     */
    AdderPrivateKeyShare loadAdderPrivateKey() throws RuntimeException;
}
