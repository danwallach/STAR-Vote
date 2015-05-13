package crypto;

import crypto.exceptions.BadKeyException;
import crypto.exceptions.CiphertextException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;
import supervisor.model.Ballot;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

/**
 * A crypto class used as a black box operating over Ballots performing
 * cryptographic functions. Behaviour depends on a specified cryptographic
 * protocol contained within the voteEncrypter field which is set upon construction of
 * BallotCrypto.
 *
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class BallotCrypto {

    private static RaceSelectionCrypto raceSelectionCrypter = null;

    /**
     * Sets the base crypto-package
     * @param cryptoType    the base crypto-package used in encryption/decryption
     */
    public static void setCryptoType(ICryptoType cryptoType){

        if(raceSelectionCrypter != null)
            System.err.println("[WARNING]: CryptoType change in BallotCrypto unadvised!");

        raceSelectionCrypter = new RaceSelectionCrypto(cryptoType);
    }

    /**
     * Decrypts a Ballot containing EncryptedVotes
     *
     * @param ballot    a Ballot containing EncryptedVotes
     * @return          a Ballot containing PlaintextVotes which are the decrypted EncryptedVotes
     */
    public static Ballot<PlaintextRaceSelection> decrypt(Ballot<EncryptedRaceSelection> ballot)
            throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        List<PlaintextRaceSelection> raceSelections = new ArrayList<>();

        /* Go through each of the EncryptedVotes and decrypt, then add to the list of PlaintextVotes*/
        for(EncryptedRaceSelection ev : ballot.getRaceSelections()) {
            raceSelections.add(raceSelectionCrypter.decrypt(ev));
        }

        /* Create a new Ballot<PlaintextVote> from the original ballot data */
        return new Ballot<>(ballot.getBid(), raceSelections, ballot.getNonce(), ballot.getSize());
    }

    /**
     * Encrypts a Ballot containing PlaintextVotes
     *
     * @param ballot    a Ballot containing PlaintextVotes
     * @return          a Ballot containing EncryptedVotes which are the encrypted PlaintextVotes
     */
    public static Ballot<EncryptedRaceSelection> encrypt(Ballot<PlaintextRaceSelection> ballot)
            throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        List<EncryptedRaceSelection> raceSelections = new ArrayList<>();

        for(PlaintextRaceSelection pv : ballot.getRaceSelections()) {
            raceSelections.add(raceSelectionCrypter.encrypt(pv));
        }

        /* Create a new Ballot<EncryptedVote> from the original ballot data */
        return new Ballot<>(ballot.getBid(), raceSelections, ballot.getNonce(), ballot.getSize());
    }

    /**
     * Loads the keys from the files specified by the filePaths
     *
     * @param filePaths     the file paths of the files from which the keys will be loaded
     * @see crypto.ICryptoType#loadAllKeys(String[])
     */
    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {
        raceSelectionCrypter.loadKeys(filePaths);
    }

    public String toString() {
        return "BallotCrypto: " + raceSelectionCrypter.toString();
    }

}
