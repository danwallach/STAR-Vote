package crypto;

import crypto.adder.InvalidVoteException;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.lexer.Hash;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedVote<T extends IHomomorphicCiphertext> extends AVote implements Provable {

    /** The proof representing the validity of this vote */
    private VoteProof<T> proof;

    private Map<String, T> cipherMap;

    public EncryptedVote(Map<String, T> cipherMap, String title) {
        super(title);

        this.cipherMap = cipherMap;

        if (!createVoteProof(cipherMap))
            throw new InvalidVoteException("An attempt to construct an EncryptedVote with invalid values was attempted!");

    }

    public static <S extends IHomomorphicCiphertext> EncryptedVote<S> identity(EncryptedVote<S> v, APublicKey PEK) {

        Map<String, S> identityMap = new HashMap<>();

        /* Fill in all the entries with identities of type S */
        for(Map.Entry<String, S> entry : v.cipherMap.entrySet())
                identityMap.put(entry.getKey(), CiphertextFactory.identity(entry.getValue().getClass(),PEK.getP()));

        /*  */
        return new EncryptedVote<>(identityMap, v.getTitle());
    }

    /**
     *
     * @return
     */
    public Map<String, T> getVoteMap(){
        return cipherMap;
    }

    /**
     * Wrapper for ciphertext operations that combines votesand returns
     * the result
     *
     * @param other    the other vote to be combined with this one
     * @return the result of the operation
     */
    public EncryptedVote<T> operate(EncryptedVote<T> other) {

        Map<String, T> resultMap = new HashMap<>();


        resultMap.putAll(other.cipherMap);
        resultMap.putAll(cipherMap);

        for(String title : other.getVoteMap().keySet()) {
            if(cipherMap.containsKey(title)) {
                resultMap.put(title, cipherMap.get(title).operate(other.cipherMap.get(title)));
            }

        }

        return new EncryptedVote<>(resultMap, getTitle());
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
    /* Does this need to be typed or will it just have to be homomorphic? */
    public VoteProof<T> getProof(){
        return proof;
    }

    /**
     *
     * @param cipherMap
     * @return
     */
    private boolean createVoteProof(Map<String, T> cipherMap) {

        /* Should each Ciphertext have proofs inside or just have all their proofs inside VoteProof (or both)? */
        /* Should each Vote have to pass itself to its proof for verification? */
        /* VoteProof should probably contain Ciphertext proofs and Ciphertexts */

        return false;
    }

    public boolean verifyVoteProof(int min, int max) {
        return proof.verify(min, max);
    }

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
