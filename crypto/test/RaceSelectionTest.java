package crypto.test;

import crypto.*;
import crypto.adder.AdderPublicKey;
import junit.framework.TestCase;
import supervisor.model.AuthorityManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew Kindy II on 7/9/2015.
 */
public class RaceSelectionTest extends TestCase {

        RaceSelectionCrypto crypto;
        AdderPublicKey PEK;

        protected void setUp() throws Exception {

            AuthorityManager.SESSION.newSession(1, 1, 2);
            AuthorityManager.SESSION.generateAuthorityKeySharePair("1");
            AuthorityManager.SESSION.generateAuthorityPolynomialValues("1");
            AuthorityManager.SESSION.generateRealPrivateKeyShare("1");
            PEK = AuthorityManager.SESSION.generatePublicEncryptionKey();

            DHExponentialElGamalCryptoType cryptoType = new DHExponentialElGamalCryptoType();
            cryptoType.loadPublicKey(PEK);

            /* Initialise RaceSelectionCrypto */
            crypto = new RaceSelectionCrypto(cryptoType);

        }

        public void testVerification(){

            Map<String,Integer> voteMap = new HashMap<>();

            voteMap.put("Candidate1",1);
            voteMap.put("Candidate2",0);
            voteMap.put("Candidate3",0);

            PlaintextRaceSelection prs = new PlaintextRaceSelection(voteMap,"thisRace",1);

            try {
                EncryptedRaceSelection<ExponentialElGamalCiphertext> ers = crypto.encrypt(prs);
                EncryptedRaceSelection<ExponentialElGamalCiphertext> ers2 = crypto.encrypt(prs);

                assertTrue(ers.verify(0, 1, PEK));
                assertTrue(ers2.verify(0, 1, PEK));

                EncryptedRaceSelection<ExponentialElGamalCiphertext> ers3 = ers.operate(ers2,PEK);
                assertTrue(ers3.verify(0, 2, PEK));
                assertTrue(ers.verify(0, 1, PEK));

                EncryptedRaceSelection<ExponentialElGamalCiphertext> ers4 = ers.operate(ers3, PEK);
                assertTrue(ers4.verify(0, 3, PEK));
                assertTrue(ers3.verify(0, 2, PEK));

                EncryptedRaceSelection<ExponentialElGamalCiphertext> ers5 = ers4.operate(ers3, PEK);
                assertTrue(ers5.verify(0, 5, PEK));
                assertFalse(ers5.verify(1, 6, PEK));
                assertFalse(ers5.verify(0, 4, PEK));

            } catch (Exception e) { e.printStackTrace(); }



        }


        public void testSetCryptoType(){ /* Check multiple sets */}

}
