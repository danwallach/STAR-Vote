package crypto;

import crypto.adder.InvalidVoteException;
import crypto.adder.MembershipProof;
import sexpression.ASExpression;
import sexpression.ListExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedVote<T extends IHomomorphicCiphertext> extends AVote implements Provable {

    /** The sumProof representing the validity of this vote */
    private MembershipProof<T> sumProof;

    private Map<String, T> cipherMap;

    public EncryptedVote(Map<String, T> cipherMap, String title) {
        super(title);

        this.cipherMap = cipherMap;

        if (!createVoteProof(cipherMap))
            throw new InvalidVoteException("An attempt to construct an EncryptedVote with invalid values was attempted!");

    }

    public static <S extends IHomomorphicCiphertext> EncryptedVote<S> identity(EncryptedVote<S> v, APublicKey PEK) {

        /* This will hold the map of identity ciphertexts to put into the identity vote */
        Map<String, S> identityMap = new HashMap<>();

        /* Fill in all the entries with identities of type S */
        for(Map.Entry<String, S> entry : v.cipherMap.entrySet())
                identityMap.put(entry.getKey(), (S) CiphertextFactory.identity(entry.getValue().getClass(), PEK));

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
     * Wrapper for ciphertext operations that combines votes and returns
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

                T thisCiphertext = this.cipherMap.get(title);
                T otherCiphertext = other.cipherMap.get(title);

                resultMap.put(title, (T) thisCiphertext.operate(otherCiphertext));
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
     * @param cipherMap
     * @return
     */
    private boolean createVoteProof(Map<String, T> cipherMap) {

        /* Should each Ciphertext have proofs inside or just have all their proofs inside VoteProof (or both)? */
        /* Should each Vote have to pass itself to its sumProof for verification? */
        /* VoteProof should probably contain Ciphertext proofs and Ciphertexts */

        return false;
    }

    public boolean verify(int min, int max) {

        for (Map.Entry<String, T> entry : cipherMap.entrySet()) {
            if (!entry.getValue().verify()) {
                return false;
            }
        }

        return sumProof.verify(min, max);
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
