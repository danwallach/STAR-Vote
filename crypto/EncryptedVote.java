package crypto;

import crypto.adder.InvalidVoteException;
import sexpression.ASExpression;
import sexpression.ListExpression;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedVote extends AVote {

    /** The proof representing the validity of this vote */
    private IProof proof;

    private Map<String, IHomomorphicCiphertext> cipherMap;

    public EncryptedVote(Map<String, IHomomorphicCiphertext> cipherMap, String title) {
        super(title);

        this.cipherMap = cipherMap;

        if (!createVoteProof(cipherMap))
            throw new InvalidVoteException("An attempt to construct an EncryptedVote with bad values was attempted!");

    }

    /**
     *
     * @return
     */
    public Map<String, IHomomorphicCiphertext> getVoteMap(){
        return cipherMap;
    }

    /**
     *
     * @return
     */
    public String getTitle(){
        return super.getTitle();
    }

    /**
     *
     * @return
     */
    public IProof getProof(){
        return proof;
    }

    /**
     *
     * @param cipherMap
     * @return
     */
    private boolean createVoteProof(Map<String, IHomomorphicCiphertext> cipherMap) { return false; }

    /**
     *
     * @return
     */
    public ASExpression toASE(){
        return new ListExpression("");
    }

    /**
     *
     * @return
     */
    public String toString() {
        return "";
    }
}
