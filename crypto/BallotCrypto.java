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

    private static VoteCrypto voteCrypter = null;

    /**
     *
     * @param cryptoType
     */
    public static void setCryptoType(ICryptoType cryptoType){

        if(voteCrypter != null)
            System.err.println("[WARNING]: CryptoType change in BallotCrypto unadvised!");

        voteCrypter = new VoteCrypto(cryptoType);
    }

    /**
     * Decrypts a Ballot containing EncryptedVotes
     *
     * @param ballot    a Ballot containing EncryptedVotes
     * @return          a Ballot containing PlaintextVotes which are the decrypted EncryptedVotes
     */
    public static Ballot<PlaintextVote> decrypt(Ballot<EncryptedVote> ballot)
            throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        List<PlaintextVote> votes = new ArrayList<>();

        /* Go through each of the EncryptedVotes and decrypt, then add to the list of PlaintextVotes*/
        for(EncryptedVote ev : ballot.getVotes()) {
            votes.add(voteCrypter.decrypt(ev));
        }

        /* Create a new Ballot<PlaintextVote> from the original ballot data */
        return new Ballot<>(ballot.getBid(), votes, ballot.getNonce(), ballot.getSize());
    }

    /**
     * Encrypts a Ballot containing PlaintextVotes
     *
     * @param ballot    a Ballot containing PlaintextVotes
     * @return          a Ballot containing EncryptedVotes which are the encrypted PlaintextVotes
     */
    public static Ballot<EncryptedVote> encrypt(Ballot<PlaintextVote> ballot)
            throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        List<EncryptedVote> votes = new ArrayList<>();

        for(PlaintextVote pv : ballot.getVotes()) {
            votes.add(voteCrypter.encrypt(pv));
        }

        /* Create a new Ballot<EncryptedVote> from the original ballot data */
        return new Ballot<>(ballot.getBid(), votes, ballot.getNonce(), ballot.getSize());
    }

    /**
     *
     * @param filePaths
     */
    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {
        voteCrypter.loadKeys(filePaths);
    }

    public String toString() {
        return "BallotCrypto: " + voteCrypter.toString();
    }

}
