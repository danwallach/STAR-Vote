package supervisor.model.test;

import auditorium.IKeyStore;
import com.sun.org.apache.xpath.internal.SourceTree;
import crypto.KeyGenerator;
import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.adder.AdderPublicKeyShare;
import junit.framework.TestCase;
import supervisor.model.AdderKeyManipulator;
import votebox.AuditoriumParams;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPairGenerator;

/**
 * Created by Matthew Kindy II on 6/19/2015.
 */
public class AdderKeyManipulatorTest extends TestCase {

    /**
     * Test case setup
     */
    protected void setUp() throws Exception {

        super.setUp();

    }

    /* Want to test PEK gen and also spew keyshares to files so that
    * we can use them for election run-throughs later */
    public void testProcedure() throws Exception {

        KeyGenerator kg = new KeyGenerator(KeyGenerator.Type.ELGAMAL);
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        AuditoriumParams params = new AuditoriumParams("supervisor.conf");
        IKeyStore keyStore = params.getKeyStore();
        AdderPublicKeyShare pks = keyStore.loadAdderPublicKeyShare();

        /* Set the seed key */
        AdderKeyManipulator.setSeedKey(pks);

        /* Have 3 logins to generate keypairs*/
        AdderPrivateKeyShare a1prks = AdderKeyManipulator.generateAuthorityKeySharePair(1);
        AdderPrivateKeyShare a2prks = AdderKeyManipulator.generateAuthorityKeySharePair(2);
        AdderPrivateKeyShare a3prks = AdderKeyManipulator.generateAuthorityKeySharePair(3);

        AdderKeyManipulator.generateAuthorityPolynomialValues(1);
        AdderKeyManipulator.generateAuthorityPolynomialValues(2);
        AdderKeyManipulator.generateAuthorityPolynomialValues(3);

        a1prks = AdderKeyManipulator.generateRealPrivateKeyShare(1, a1prks);
        a2prks = AdderKeyManipulator.generateRealPrivateKeyShare(2, a2prks);
        a3prks = AdderKeyManipulator.generateRealPrivateKeyShare(3, a3prks);

        AdderPublicKey PEK = AdderKeyManipulator.generatePublicEncryptionKey();





    }
}
