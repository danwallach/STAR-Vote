package crypto.test;

import crypto.*;
import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.exceptions.CiphertextException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;
import junit.framework.TestCase;
import org.apache.http.auth.AUTH;
import supervisor.model.AuthorityManager;
import supervisor.model.Ballot;
import supervisor.model.WebServerTallier;

import java.security.InvalidKeyException;
import java.util.*;

/**
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class BallotCryptoTest extends TestCase {

    private DHExponentialElGamalCryptoType cryptoType = new DHExponentialElGamalCryptoType();
    private AdderPublicKey PEK;

    protected void setUp() throws Exception {

        try {
            AuthorityManager.newSession(1,1,1);
            AuthorityManager.generateAuthorityKeySharePair("1");
            AuthorityManager.generateAuthorityPolynomialValues("1");
            cryptoType.loadPrivateKeyShares(Collections.singletonList(AuthorityManager.generateRealPrivateKeyShare("1")).toArray(new AdderPrivateKeyShare[1]));
            cryptoType.loadPublicKey(AuthorityManager.generatePublicEncryptionKey());
            PEK = AuthorityManager.generatePublicEncryptionKey();
        }
        catch (Exception e) { e.printStackTrace(); }

        BallotCrypto.setCryptoType(cryptoType);
    }

    public void testEncryptDecrypt() throws InterruptedException {

        Ballot<PlaintextRaceSelection> ballot = createPlaintextBallot();

        try {

            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> encrypted = BallotCrypto.encrypt(ballot);

            for (int i=0; i<ballot.getRaceSelections().size(); i++) {
                for (Map.Entry<String, Integer> entry : ballot.getRaceSelections().get(i).getRaceSelectionsMap().entrySet()) {
                    assertEquals(BallotCrypto.decrypt(encrypted).getRaceSelections().get(i).getRaceSelectionsMap().get(entry.getKey()),
                                 ballot.getRaceSelections().get(i).getRaceSelectionsMap().get(entry.getKey()));
                }
            }

            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> tallied = WebServerTallier.tally("tallied",
                                                                                                          Arrays.asList(encrypted, encrypted),
                                                                                                          PEK);
            Ballot<PlaintextRaceSelection> decrypted = BallotCrypto.decrypt(tallied);

            System.out.println(decrypted.getRaceSelections());

            for (int i=0; i<decrypted.getRaceSelections().size(); i++) {
                for (Map.Entry<String, Integer> entry : decrypted.getRaceSelections().get(i).getRaceSelectionsMap().entrySet()) {
                    assertEquals((int) entry.getValue(), 2 * ballot.getRaceSelections().get(i).getRaceSelectionsMap().get(entry.getKey()));
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
