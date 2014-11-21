package crypto;

import crypto.adder.InvalidVoteException;

import java.util.List;
import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedVote {

    /** This vote's list of cipher texts, i.e. its encrypted selections */
    private Map<String, ICiphertext> cipherMap;

    /** The proof representing the validity of this vote */
    private IProof proof;

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    private String title;

    public EncryptedVote(Map<String, ICiphertext> cipherMap, String title) {

        if (createVoteProof(cipherMap)) {
            this.cipherMap = cipherMap;
            this.title = title;
        }
        else
            throw new InvalidVoteException("An attempt to construct an EncryptedVote with bad values was attempted!");

    }

    public Map<String, ICiphertext> getCipherMap(){
        return cipherMap;
    }

    public String getTitle(){
        return title;
    }

    boolean createVoteProof(Map<String, ICiphertext> cipherMap) { return false; }
}
