package crypto;

import crypto.exceptions.BadKeyException;
import crypto.exceptions.CiphertextException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

/**
 * A crypto class used as a black box operating over Votes performing
 * cryptographic functions. Behaviour depends on a specified cryptographic
 * protocol contained within the byteEncrypter field which is set upon construction of
 * BallotCrypto.
 *
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class RaceSelectionCrypto {


    private ByteCrypto byteCrypter;

    public RaceSelectionCrypto(ICryptoType cryptoType) {
        byteCrypter = new ByteCrypto(cryptoType);
    }

    public PlaintextRaceSelection decrypt(EncryptedRaceSelection raceSelection) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        /* Get the map from the vote */
        Map<String, AHomomorphicCiphertext> cipherMap = raceSelection.getRaceSelectionsMap();
        Map<String, Integer> raceSelectionMap = new HashMap<>();

        /* Cycle over each of the candidates in the cipherMap */
        for(Map.Entry<String, AHomomorphicCiphertext> cur : cipherMap.entrySet()) {

            AHomomorphicCiphertext encryptedChoice = cur.getValue();
            String candidate = cur.getKey();

            /* Encrypt each choice and put into a ciphertext */
            Integer decryptedChoice = ByteBuffer.wrap(byteCrypter.decrypt(encryptedChoice)).getInt();

            /* Put the ciphertexts into a map */
            raceSelectionMap.put(candidate, decryptedChoice);
        }

        /* Pull out parts from AHomomorphicCiphertext in vote to pass to byteCrypter in order to contstruct new PlaintextVote */
        return new PlaintextRaceSelection(raceSelectionMap, raceSelection.getTitle(), raceSelection.size);
    }

    public EncryptedRaceSelection encrypt(PlaintextRaceSelection vote) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        /* Get the map from the PlaintextVote */
        Map<String, Integer> voteMap = vote.getRaceSelectionsMap();
        Map<String, AHomomorphicCiphertext> cipherMap = new HashMap<>();

        /* Cycle over each of the candidates in the voteMap */
        for(Map.Entry<String, Integer> cur : voteMap.entrySet()) {

            int choice = cur.getValue();
            String candidate = cur.getKey();

            /* Encrypt each choice and put into a ciphertext */
            AHomomorphicCiphertext encryptedChoice = byteCrypter.encrypt(ByteBuffer.allocate(4).putInt(choice).array());

            /* Put the ciphertexts into a map */
            cipherMap.put(candidate, encryptedChoice);
        }

        /* Create a new EncryptedVote from the new ciphertexts */
        return new EncryptedRaceSelection<>(cipherMap, vote.getTitle(), 1);
    }

    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {
        byteCrypter.loadKeys(filePaths);
    }

    public String toString() {
        return "VoteCrypto: " + byteCrypter.toString();
    }

}
