package crypto.test;

import crypto.*;
import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.exceptions.CiphertextException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;
import junit.framework.TestCase;
import supervisor.model.AuthorityManager;
import supervisor.model.Ballot;
import supervisor.model.WebServerTallier;

import java.security.InvalidKeyException;
import java.util.*;

/**
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class BallotCrypterTest extends TestCase {

    private BallotCrypter<ExponentialElGamalCiphertext> ballotCrypter;
    private AdderPublicKey PEK;

    protected void setUp() throws Exception {

        super.setUp();

        try {
            AuthorityManager.newSession(1, 1, 3);
            AuthorityManager.generateAuthorityKeySharePair("1");
            AuthorityManager.generateAuthorityPolynomialValues("1");

            DHExponentialElGamalCryptoType cryptoType = new DHExponentialElGamalCryptoType();

            cryptoType.loadPrivateKeyShares(Collections.singletonList(AuthorityManager.generateRealPrivateKeyShare("1")).toArray(new AdderPrivateKeyShare[1]));
            PEK = AuthorityManager.generatePublicEncryptionKey();
            cryptoType.loadPublicKey(PEK);

            ballotCrypter = new BallotCrypter<>(cryptoType);
            ballotCrypter.loadKeys();
        }
        catch (Exception e) { e.printStackTrace(); }


    }

    public void testEncryptDecrypt() throws InterruptedException {

        Ballot<PlaintextRaceSelection> ballot1 = createPlaintextBallot();
        Ballot<PlaintextRaceSelection> ballot2 = createPlaintextBallot();

        try {

            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> encrypted = ballotCrypter.encrypt(ballot1);
            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> encrypted2 = ballotCrypter.encrypt(ballot2);

            for (int i=0; i<ballot1.getRaceSelections().size(); i++) {

                assertTrue(encrypted.getRaceSelections().get(i).verify(0, 1, PEK));
                assertTrue(encrypted2.getRaceSelections().get(i).verify(0, 1, PEK));

                for (Map.Entry<String, Integer> entry : ballot1.getRaceSelections().get(i).getRaceSelectionsMap().entrySet()) {
                    assertEquals(ballotCrypter.decrypt(encrypted).getRaceSelections().get(i).getRaceSelectionsMap().get(entry.getKey()),
                            ballot1.getRaceSelections().get(i).getRaceSelectionsMap().get(entry.getKey()));
                }
            }

            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> tallied = WebServerTallier.tally("tallied",
                                                                                                          Arrays.asList(encrypted, encrypted2),
                                                                                                          PEK);

            for (int i=0; i<tallied.getRaceSelections().size(); i++) {
                assertTrue(tallied.getRaceSelections().get(i).verify(0,2,PEK));
            }

            Ballot<PlaintextRaceSelection> decrypted = ballotCrypter.decrypt(tallied);

            for (int i=0; i<decrypted.getRaceSelections().size(); i++) {
                for (Map.Entry<String, Integer> entry : decrypted.getRaceSelections().get(i).getRaceSelectionsMap().entrySet()) {
                    assertEquals((int) entry.getValue(), 2 * ballot1.getRaceSelections().get(i).getRaceSelectionsMap().get(entry.getKey()));
                }
            }
        } catch (UninitialisedException | KeyNotLoadedException | CipherException | InvalidKeyException | CiphertextException e) {
            e.printStackTrace(); fail();
        }
    }

    private Ballot<PlaintextRaceSelection> createPlaintextBallot(){
        Random random = new Random();
        List<PlaintextRaceSelection> raceSelectionList = new ArrayList<>();
        Map<String, Integer> voteMap = new HashMap<>();
        Map<String, Integer> voteMap2 = new HashMap<>();

        voteMap.put("Bob",1);
        voteMap.put("Jeff", 0);
        voteMap.put("Jesus", 0);

        voteMap2.putAll(voteMap);
        voteMap2.remove("Bob");
        voteMap2.put("Angela", 1);

        raceSelectionList.add(new PlaintextRaceSelection(voteMap, "Presidential", 1));
        raceSelectionList.add(new PlaintextRaceSelection(voteMap2, "Secondary", 1));

        String bid = Integer.toString(random.nextInt(999999999));

        return new Ballot<>(bid, raceSelectionList, bid);
    }

    public void testLoadKeys(){ /* Check uninitialised */ }

    public void testSetCryptoType(){ /* Check multiple sets */}

}
