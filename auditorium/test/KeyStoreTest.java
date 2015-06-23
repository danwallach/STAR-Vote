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

import auditorium.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sexpression.ASExpression;
import sexpression.ListExpression;

import java.io.File;

/**
 * Tests the functionality of the Auditorium key store
 *
 * @author Kyle Derr
 */
public class KeyStoreTest {

    private final ASExpression message = new ListExpression( "one", "two" );

    private File file;
    private SimpleKeyStore keystore;

    @Before
    public void build() throws Exception {
        file = new File( "tmp/" );
        file.mkdir();
        Generator.main( "5", "tmp/" );
        keystore = new SimpleKeyStore( "tmp/" );
    }

    @After
    public void tear() throws Exception {
        File[] fileList;
        if((fileList = file.listFiles()) != null) {
            for (File child : fileList)
               child.delete();
        }

        file.delete();
    }

    @Test
    public void loadKey() throws Exception {
        for (int lcv = 0; lcv < 5; lcv++) {
            Certificate cert = keystore.loadCert( Integer.toString( lcv ) );
            Key key = keystore.loadKey( Integer.toString( lcv ) );
            Signature sig = RSACrypto.SINGLETON.sign( message, key );
            RSACrypto.SINGLETON.verify( sig, cert );
        }
    }

    @Test(expected = AuditoriumCryptoException.class)
    public void loadKeyFail1() throws Exception {
        keystore.loadKey( "blah" );
    }

    @Test(expected = AuditoriumCryptoException.class)
    public void loadKeyFail2() throws Exception {
        keystore.loadKey( "5" );
    }

    @Test(expected = AuditoriumCryptoException.class)
    public void loadCertFail1() throws Exception {
        keystore.loadCert( "blah" );
    }

    @Test(expected = AuditoriumCryptoException.class)
    public void loadCertFail2() throws Exception {
        keystore.loadCert( "5" );
    }
}
