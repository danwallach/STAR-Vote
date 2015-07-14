package crypto.test;

import crypto.DHExponentialElGamalCryptoType;
import crypto.adder.AdderPrivateKeyShare;
import junit.framework.TestCase;
import supervisor.model.AuthorityManager;

import java.nio.ByteBuffer;
import java.util.Arrays;
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

    public void testEncryptDecryptSingle() {

        cryptoType = new DHExponentialElGamalCryptoType();

        setUpSingleKeys();

        byte[] ZERO = ByteBuffer.allocate(4).putInt(0).array();
        byte[] ONE = ByteBuffer.allocate(4).putInt(1).array();

        try {
            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(cryptoType.encrypt(ZERO))).getInt(), 0);
            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(cryptoType.encrypt(ONE))).getInt(), 1);
        }
        catch (Exception e) { e.printStackTrace(); fail(); }

    }

    public void testEncryptDecryptMultiple() {

        cryptoType = new DHExponentialElGamalCryptoType();

        setUpMultipleKeys();

        byte[] ZERO = ByteBuffer.allocate(4).putInt(0).array();
        byte[] ONE = ByteBuffer.allocate(4).putInt(1).array();

        try {
            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(cryptoType.encrypt(ZERO))).getInt(), 0);
            assertEquals(ByteBuffer.wrap(cryptoType.decrypt(cryptoType.encrypt(ONE))).getInt(), 1);
        }
        catch (Exception e) { e.printStackTrace(); fail(); }

    }

    private void setUpMultipleKeys() {

         /* Load the seed key */
        try {

            AuthorityManager.generateAuthorityKeySharePair("1");
            AuthorityManager.generateAuthorityKeySharePair("2");

            AuthorityManager.generateAuthorityPolynomialValues("1");
            AuthorityManager.generateAuthorityPolynomialValues("2");

            List<AdderPrivateKeyShare> keyList = Arrays.asList(AuthorityManager.generateRealPrivateKeyShare("1")//,
                                                          /*AuthorityManager.generateRealPrivateKeyShare("2")*/);

            cryptoType.loadPrivateKeyShares(keyList.toArray(new AdderPrivateKeyShare[keyList.size()]));

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
