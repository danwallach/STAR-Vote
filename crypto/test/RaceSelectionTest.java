package crypto.test;

import crypto.*;
import crypto.adder.AdderPublicKey;
import junit.framework.TestCase;
import sexpression.ASEConverter;
import sexpression.ASExpression;
import sexpression.ListExpression;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew Kindy II on 7/9/2015.
 */
public class RaceSelectionTest extends TestCase {

        RaceSelectionCrypto crypto;
        AdderPublicKey PEK;

        protected void setUp() throws Exception {
            File PEKFile = new File(System.getProperty("user.dir"), "PEK.adder.key");
            Path PEKPath = PEKFile.toPath();
            System.out.println(PEKPath.toAbsolutePath());


            try {
                byte[] verbatimPEK = Files.readAllBytes(PEKPath);
                ASExpression PEKASE = ASExpression.makeVerbatim(verbatimPEK);
                PEK = ASEConverter.convertFromASE((ListExpression) PEKASE);
            }
            catch (Exception e) { e.printStackTrace(); throw new RuntimeException("Couldn't use the key file");}

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
                assertTrue(ers.verify(0,1, PEK));

                EncryptedRaceSelection<ExponentialElGamalCiphertext> ers4 = ers.operate(ers3, PEK);
                assertTrue(ers4.verify(0, 3, PEK));
                assertTrue(ers3.verify(0,2, PEK));

                EncryptedRaceSelection<ExponentialElGamalCiphertext> ers5 = ers4.operate(ers3, PEK);
                assertTrue(ers5.verify(0, 5, PEK));
                assertFalse(ers5.verify(1,6, PEK));
                assertFalse(ers5.verify(0, 4, PEK));

            } catch (Exception e) { e.printStackTrace(); }



        }


        public void testSetCryptoType(){ /* Check multiple sets */}

}
