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
     * Wrapper for ciphertext operations that combines votesand returns
     * the result
     *
     * @param other    the other vote to be combined with this one
     * @return the result of the operation
     */
    public EncryptedVote operate(EncryptedVote other) {
        HashMap<String, IHomomorphicCiphertext> resultMap = new HashMap<>();

        resultMap.putAll(other.cipherMap);
        resultMap.putAll(cipherMap);

        for(String title : other.getVoteMap().keySet()) {
            if(cipherMap.containsKey(title)) {
                resultMap.put(title, cipherMap.get(title).operate(other.cipherMap.get(title)));
            }

        }

        return new EncryptedVote(resultMap, getTitle());
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
