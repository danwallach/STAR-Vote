package crypto;

import crypto.adder.InvalidVoteException;
import sexpression.ASExpression;
import sexpression.ListExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedVote extends AVote {

    /** The proof representing the validity of this vote */
    private IProof proof;

    private Map<String, ICiphertext> cipherMap;

    public EncryptedVote(Map<String, ICiphertext> cipherMap, String title) {
        super(title);

        this.cipherMap = cipherMap;

        if (!createVoteProof(cipherMap))
            throw new InvalidVoteException("An attempt to construct an EncryptedVote with bad values was attempted!");

    }

    public Map<String, ICiphertext> getVoteMap(){
        return cipherMap;
    }

    public String getTitle(){
        return super.getTitle();
    }

    public IProof getProof(){
        return proof;
    }

    boolean createVoteProof(Map<String, ICiphertext> cipherMap) { return false; }

    public ASExpression toASE(){
        return new ListExpression("");
    }

    public String toString() {
        return "";
    }
}
