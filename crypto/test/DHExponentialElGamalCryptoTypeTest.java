package crypto.test;

import crypto.DHExponentialElGamalCryptoType;
import crypto.ExponentialElGamalCiphertext;
import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import junit.framework.TestCase;
import supervisor.model.AuthorityManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Matt Bernhard and Matt Kindy
 */
public class DHExponentialElGamalCryptoTypeTest extends TestCase {

    private DHExponentialElGamalCryptoType cryptoType;

    protected void setUp() throws Exception {
        cryptoType = new DHExponentialElGamalCryptoType();
    }

    public void testEncryptDecryptSingle() throws InterruptedException {

        System.out.println("Testing encryption using a decryption threshold of 1, safety threshold of 1...");
        AuthorityManager.newSession(1,1,2);
        setUpSingleKeys();
        checkEncryptDecrypt();
        Thread.sleep(1000);
    }

    public void testEncryptDecryptMultiple() throws InterruptedException {

        System.out.println("Testing encryption using a decryption threshold of 1, safety threshold of 2...");
        AuthorityManager.newSession(2,1,3);
        setUpMultipleKeys(2, 1);
        checkEncryptDecrypt();
        Thread.sleep(1000);

        System.out.println("Testing encryption using a decryption threshold of 2, safety threshold of 3...");
        AuthorityManager.newSession(3, 2, 4);
        setUpMultipleKeys(3, 2);
        checkEncryptDecrypt();
        Thread.sleep(1000);

        System.out.println("Testing encryption using a decryption threshold of 3, safety threshold of 3...");
        AuthorityManager.newSession(3, 3, 4);
        setUpMultipleKeys(3, 3);
        checkEncryptDecrypt();
        Thread.sleep(1000);

    }

    private void checkEncryptDecrypt() {

        byte[] ZERO = ByteBuffer.allocate(4).putInt(0).array();
        byte[] ONE = ByteBuffer.allocate(4).putInt(1).array();

        try {

            AdderPublicKey PEK = AuthorityManager.generatePublicEncryptionKey();

            ExponentialElGamalCiphertext ZEROct = cryptoType.encrypt(ZERO);
            ExponentialElGamalCiphertext ONEct = cryptoType.encrypt(ONE);
            ExponentialElGamalCiphertext ONEct2 = cryptoType.encrypt(ONE);

            ExponentialElGamalCiphertext TWOct = ONEct.operateIndependent(ONEct2, PEK);
            assertTrue(TWOct.verify(0, 2, PEK));
            TWOct = TWOct.operateIndependent(ZEROct, PEK);
            ExponentialElGamalCiphertext THREEct = TWOct.operateIndependent(ONEct, PEK);

            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(ZEROct)).getInt(), 0);
            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(ONEct)).getInt(), 1);
            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(TWOct)).getInt(), 2);
            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(THREEct)).getInt(), 3);
            assertTrue(ZEROct.verify(0,1,PEK));
            assertTrue(ONEct.verify(0,1,PEK));
            assertTrue(TWOct.verify(0,3,PEK));
            assertTrue(THREEct.verify(0,4, PEK));
        }
        catch (Exception e) { e.printStackTrace(); fail(); }
    }

    private void setUpMultipleKeys(int numGenerate, int numDecrypt) {

         /* Load the seed key */
        try {

            for (int i=0; i<numGenerate; i++) {
                AuthorityManager.generateAuthorityKeySharePair(Integer.toString(i + 1));
            }

            for (int i=0; i<numGenerate; i++) {
                AuthorityManager.generateAuthorityPolynomialValues(Integer.toString(i + 1));
            }

            List<AdderPrivateKeyShare> pksList = new ArrayList<>();

            for (int i=0; i<numGenerate; i++) {
                AdderPrivateKeyShare generated = AuthorityManager.generateRealPrivateKeyShare(Integer.toString(i+1));
                if (i<=numDecrypt) pksList.add(generated);
            }

            cryptoType.loadPrivateKeyShares(pksList.toArray(new AdderPrivateKeyShare[pksList.size()]));
            cryptoType.loadPublicKey(AuthorityManager.generatePublicEncryptionKey());
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void setUpSingleKeys() {

        /* Load the seed key */
        try {

            AuthorityManager.generateAuthorityKeySharePair("1");
            AuthorityManager.generateAuthorityPolynomialValues("1");
            cryptoType.loadPrivateKeyShares(Collections.singletonList(AuthorityManager.generateRealPrivateKeyShare("1")).toArray(new AdderPrivateKeyShare[1]));
            cryptoType.loadPublicKey(AuthorityManager.generatePublicEncryptionKey());
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void testLoadPrivateKey(){ /* Test both implementations with good and bad keys */ }

    public void testLoadPublicKey(){ /* Test both implementations with good and bad keys */ }

    public void testLoadKeys(){ /* Test both implementations with good and bad keys */ }

    public void testExpectedProperties(){ /* Test various decrypt/encrypt setups to see if properties are working */}

}
