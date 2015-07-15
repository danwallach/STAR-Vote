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
 * A crypto class used as a black box operating over RaceSelections performing
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

    /**
     *
     * @param raceSelection
     * @return
     * @throws UninitialisedException
     * @throws KeyNotLoadedException
     * @throws InvalidKeyException
     * @throws CipherException
     * @throws CiphertextException
     */
    public <T extends AHomomorphicCiphertext<T>> PlaintextRaceSelection decrypt(
            EncryptedRaceSelection<T> raceSelection) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        /* Get the map from the race selection */
        Map<String, T> cipherMap = raceSelection.getRaceSelectionsMap();
        Map<String, Integer> raceSelectionMap = new HashMap<>();

        /* Cycle over each of the candidates in the cipherMap */
        for(Map.Entry<String, T> cur : cipherMap.entrySet()) {

            AHomomorphicCiphertext encryptedChoice = cur.getValue();
            String candidate = cur.getKey();

            /* Encrypt each choice and put into a ciphertext */
            Integer decryptedChoice = ByteBuffer.wrap(byteCrypter.decrypt(encryptedChoice)).getInt();

            /* Put the ciphertexts into a map */
            raceSelectionMap.put(candidate, decryptedChoice);
        }

        /* Pull out parts from AHomomorphicCiphertext in race selection to pass to byteCrypter in order to contstruct new PlaintextVote */
        return new PlaintextRaceSelection(raceSelectionMap, raceSelection.getTitle(), raceSelection.size);
    }

    /**
     *
     * @param raceSelection
     * @return
     * @throws UninitialisedException
     * @throws KeyNotLoadedException
     * @throws InvalidKeyException
     * @throws CipherException
     * @throws CiphertextException
     */
    public <T extends AHomomorphicCiphertext<T>> EncryptedRaceSelection<T> encrypt(PlaintextRaceSelection raceSelection) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        /* Get the map from the PlaintextVote */
        Map<String, Integer> raceSelectionsMap = raceSelection.getRaceSelectionsMap();
        Map<String, T> cipherMap = new HashMap<>();

        /* Cycle over each of the candidates in the raceSelectionMap */
        for(Map.Entry<String, Integer> cur : raceSelectionsMap.entrySet()) {

            int choice = cur.getValue();
            String candidate = cur.getKey();

            /* Encrypt each choice and put into a ciphertext */
            T encryptedChoice = byteCrypter.encrypt(ByteBuffer.allocate(4).putInt(choice).array());

            /* Put the ciphertexts into a map */
            cipherMap.put(candidate, encryptedChoice);
        }

        /* Create a new EncryptedRaceSelection from the new ciphertexts */
        return new EncryptedRaceSelection<>(cipherMap, raceSelection.getTitle(), 1);
    }

    /**
     *
     * @param filePaths
     * @throws FileNotFoundException
     * @throws BadKeyException
     * @throws UninitialisedException
     */
    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {
        byteCrypter.loadKeys(filePaths);
    }

    public String toString() {
        return "VoteCrypto: " + byteCrypter.toString();
    }

}
