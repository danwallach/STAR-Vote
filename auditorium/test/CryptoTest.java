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

package auditorium.test;

import org.junit.Test;

import auditorium.*;
import auditorium.Generator.Keys;

import sexpression.*;

/**
 * Unit test the primitives provided in the Crypto class. These test the output
 * of the generation scripts in /keys/. Make sure you have run them and
 * generated at least 10 key pairs, plus the CA, before you run this test.
 * 
 * @author Kyle Derr
 * 
 */
public class CryptoTest {

    private final Generator gen = new Generator();
    private final ASExpression message = new ListExpression( "One", "Two",
            "Three" );

    @Test
    public void testSignVerify() throws Exception {
        for (int lcv = 0; lcv < 3; lcv++) {
            Keys keys = gen.generateKey( "TEST", "TEST" );
            Cert cert = gen.createCert( keys.getPrivate(), keys.getPublic() );
            Signature sig = RSACrypto.SINGLETON.sign( message, keys.getPrivate() );
            RSACrypto.SINGLETON.verify( sig, cert );
        }
    }

    @Test(expected = AuditoriumCryptoException.class)
    public void testSignVerifyFail() throws Exception {
        for (int lcv = 0; lcv < 3; lcv++) {
            Keys keys = gen.generateKey( "TEST", "TEST" );
            Cert cert = gen.createCert( keys.getPrivate(), keys.getPublic() );
            Signature sig = RSACrypto.SINGLETON.sign( message, keys.getPrivate() );
            byte[] sigBytes = sig.getSigData().getBytesCopy();
            sigBytes[32] = 12;
            sigBytes[1] = -3;
            Signature notSig = new Signature( "TEST", StringExpression.makeString(
                    sigBytes ), keys.getPublic().toASE() );
            RSACrypto.SINGLETON.verify( notSig, cert );
        }
    }
}
