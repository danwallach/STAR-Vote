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

import sexpression.ASExpression;
import sexpression.StringExpression;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Crypto primitives used in auditorium are wrapped here.
 * 
 * @author Kyle Derr
 */
public class RSACrypto {

    /** Since we'll only really need one of these, we use the singleton pattern */
    public static final RSACrypto SINGLETON = new RSACrypto();

    /** Private constructor for singleton */
    private RSACrypto() {}

    /**
     * Create an RSA digital signature.
     * 
     * @param data      Sign this expression's verbatim form.
     * @param key       Use this key to create the signature.
     * @return          The signature data.
     *
     * @throws AuditoriumCryptoException Thrown if there is a problem with signing the data
     */
    public Signature sign(ASExpression data, Key key) throws AuditoriumCryptoException {
        try {
            /* Build a key using RSA */
            KeyFactory factory = KeyFactory.getInstance("RSA");
            java.security.Signature sig = java.security.Signature.getInstance("SHA1withRSA");
            PrivateKey privatekey = factory.generatePrivate(new RSAPrivateKeySpec(key.getMod(), key.getKey()));

            /* Initialize the signer */
            sig.initSign(privatekey);
            sig.update(data.toVerbatim());

            /* Return a new Signature object with the signed data and signature from the key*/
            return new Signature(key.getId(), StringExpression.makeString(sig.sign()), data);
        }
        catch (Exception e) {
            throw new AuditoriumCryptoException("sign", e);
        }
    }

    /**
     * Verify that an RSA digital signature (possibly created by the sign
     * function) came from a particular host.
     * 
     * @param signature         The digital signature, itself.
     * @param host              The certificate of the host that supposedly signed the message.
     *
     * @throws AuditoriumCryptoException Thrown if there is a problem with the verification process
     */
    public void verify(Signature signature, Certificate host) throws AuditoriumCryptoException {
        try {
            /* Get an RSA key */
            KeyFactory factory = KeyFactory.getInstance("RSA");
            java.security.Signature sig = java.security.Signature.getInstance("SHA1withRSA");
            PublicKey publickey = factory.generatePublic(new RSAPublicKeySpec(host.getKey().getMod(), host.getKey().getKey()));

            /* Initialize the key */
            sig.initVerify(publickey);
            sig.update(signature.getPayload().toVerbatim());

            /* Verify the provided signature */
            if (!sig.verify(signature.getSigData().getBytesCopy()))
                throw new AuditoriumCryptoException("verify signature", new Exception("Verification failure: " + signature + " not signed by " + host));
        }
        catch (Exception e) {
            throw new AuditoriumCryptoException("verify signature", e);
        }
    }
}
