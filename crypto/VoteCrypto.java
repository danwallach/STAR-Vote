package crypto;

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
public class VoteCrypto {


    private ByteCrypto byteCrypter;

    public VoteCrypto(ICryptoType cryptoType) {
        byteCrypter = new ByteCrypto(cryptoType);
    }

    public PlaintextVote decrypt(EncryptedVote vote) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        /* Get the map from the vote */
        Map<String, ICiphertext> cipherMap = vote.getCipherMap();
        Map<String, Integer> voteMap = new HashMap<>();

        /* Cycle over each of the candidates in the cipherMap */
        for(Map.Entry<String, ICiphertext> cur : cipherMap.entrySet()) {

            ICiphertext encryptedChoice = cur.getValue();
            String candidate = cur.getKey();

            /* Encrypt each choice and put into a ciphertext */
            Integer decryptedChoice = ByteBuffer.wrap(byteCrypter.decrypt(encryptedChoice)).getInt();

            /* Put the ciphertexts into a map */
            voteMap.put(candidate, decryptedChoice);
        }

        /* Pull out parts from ICiphertext in vote to pass to byteCrypter in order to contstruct new PlaintextVote */
        return new PlaintextVote(voteMap, vote.getTitle());
    }

    public EncryptedVote encrypt(PlaintextVote vote) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        /* Get the map from the PlaintextVote */
        Map<String, Integer> voteMap = vote.getVoteMap();
        Map<String, ICiphertext> cipherMap = new HashMap<>();

        /* Cycle over each of the candidates in the voteMap */
        for(Map.Entry<String, Integer> cur : voteMap.entrySet()) {

            int choice = cur.getValue();
            String candidate = cur.getKey();

            /* Encrypt each choice and put into a ciphertext */
            ICiphertext encryptedChoice = byteCrypter.encrypt(ByteBuffer.allocate(4).putInt(choice).array());

            /* Put the ciphertexts into a map */
            cipherMap.put(candidate, encryptedChoice);
        }

        /* Create a new EncryptedVote from the new ciphertexts */
        return new EncryptedVote(cipherMap, vote.getTitle());
    }

    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {
        byteCrypter.loadKeys(filePaths);
    }

    public String toString() {
        return "VoteCrypto: " + byteCrypter.toString();
    }

}
