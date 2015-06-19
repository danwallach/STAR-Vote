package crypto.test;

import crypto.ByteCrypto;
import crypto.DHExponentialElGamalCryptoType;
import crypto.ICiphertext;
import crypto.ICryptoType;
import crypto.exceptions.BadKeyException;
import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

/**
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class ByteCryptoTest extends TestCase {

    protected void setUp() throws Exception {

    }

    public void testConstruction() {

        /* Construct with specific sample ICryptoType */
        ICryptoType ct = new DHExponentialElGamalCryptoType();
        ByteCrypto bc = new ByteCrypto(ct);

        assertEquals(bc.toString(), "CryptoType: " + ct.toString());
    }

    public void testDecrypt(){

        /* REDUNDANT TEST OF ICRYPTOTYPE */

        /* Take a test crypto type */
        ICryptoType ct = new DHExponentialElGamalCryptoType();


        /* Load the keys */
        String[] filePaths = {"testPublic.adk", "testPrivate.adk"};

        try { ct.loadAllKeys(filePaths); }
        catch (Exception e) { e.printStackTrace(); fail("There was an unexpected exception."); }

        /* Create ICipherText for "0" and 1 */

        try {

            ICiphertext ciphertext0 = ct.encrypt("0".getBytes());

            Integer oneInt = new Integer(1);
            byte[] oneIntBytes = ByteBuffer.allocate(4).putInt(oneInt).array();
            ICiphertext ciphertext1 = ct.encrypt(oneIntBytes);


            /* Decrypt each one */
            ByteCrypto bc = new ByteCrypto(ct);

            /* Check that they match */
            byte[] zero = bc.decrypt(ciphertext0);
            assertEquals("0".getBytes(), zero);

            byte[] one = bc.decrypt(ciphertext1);
            assertEquals(oneIntBytes, one);
        }
        catch (Exception e) { e.printStackTrace(); fail("There was an unexpected exception."); }
    }

    public void testEncrypt(){

        /* REDUNDANT TEST OF ICRYPTOTYPE */

        /* Take a test crypto type */
        ICryptoType ct = new DHExponentialElGamalCryptoType();

        /* Load the keys */


        ByteCrypto bc = new ByteCrypto(ct);

        /* Encrypt "0" and 1 */
        Integer oneInt = 1;
        byte[] oneIntBytes = ByteBuffer.allocate(4).putInt(oneInt).array();

        try {

            ICiphertext ciphertext0 = bc.encrypt("0".getBytes());
            ICiphertext ciphertext1 = bc.encrypt(oneIntBytes);

            /* Decrypt and test each one */
            assertEquals("0".getBytes(), ct.decrypt(ciphertext0));
            assertEquals(oneIntBytes, ct.decrypt(ciphertext1));
        }
        catch (Exception e) { e.printStackTrace(); fail("There was an unexpected exception."); }

    }

    public void testLoadKeys(){

        /* REDUNDANT TEST OF ICRYPTOTYPE */

        ICryptoType ct = new DHExponentialElGamalCryptoType();
        ByteCrypto bc = new ByteCrypto(ct);

        /* Test loading with real keys */
        String[] filePaths = {"testPublic.adk", "testPrivate.adk"};
        String[] fakeFilePaths = {"111111111.adk", "0000000000.adk"};
        String[] badFilePaths = {"fakeTestPublic.adk", "fakeTestPrivate.adk"};

        try { bc.loadKeys(filePaths); }
        catch (Exception e) { e.printStackTrace(); fail("There was an unexpected exception."); }

        /* Test loading with keys that don't exist */
        try { bc.loadKeys(fakeFilePaths); }
        catch (Exception e) { assert(e instanceof FileNotFoundException); }

        /* Test loading with bad keys */
        try { bc.loadKeys(badFilePaths); }
        catch (Exception e) { assert(e instanceof BadKeyException); }

    }

}
