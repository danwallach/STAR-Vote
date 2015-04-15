package crypto;

import sexpression.ASExpression;
import sexpression.ListExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedRaceSelection<T extends IHomomorphicCiphertext> extends ARaceSelection implements Provable {

    private Map<String, T> selectionsMap;

    /**
     *
     * @param selectionsMap
     * @param title
     * @param size
     */
    public EncryptedRaceSelection(Map<String, T> selectionsMap, String title, int size) {
        super(title, size);

        this.selectionsMap = selectionsMap;

    }

    /**
     *
     * @param v
     * @param PEK
     * @param <S>
     * @return
     */
    public static <S extends IHomomorphicCiphertext> EncryptedRaceSelection<S> identity(EncryptedRaceSelection<S> v, IPublicKey PEK) {

        /* This will hold the map of identity ciphertexts to put into the identity vote */
        Map<String, S> identityMap = new HashMap<>();

        /* Fill in all the entries with identities of type S */
        for(Map.Entry<String, S> entry : v.selectionsMap.entrySet())
                identityMap.put(entry.getKey(), (S) CiphertextFactory.identity(entry.getValue().getClass(), PEK));

        /* Create new identity with size 0 (i.e. no votes cast) */
        return new EncryptedRaceSelection<>(identityMap, v.getTitle(), 0);
    }

    /**
     *
     * @return
     */
    public Map<String, T> getRaceSelectionsMap(){
        return selectionsMap;
    }

    /**
     * Wrapper for ciphertext operations that combines votes and returns
     * the result
     *
     * @param other    the other vote to be combined with this one
     * @return the result of the operation
     */
    public EncryptedRaceSelection<T> operate(EncryptedRaceSelection<T> other) {

        Map<String, T> resultMap = new HashMap<>();


        resultMap.putAll(other.selectionsMap);
        resultMap.putAll(selectionsMap);

        for(String title : other.getRaceSelectionsMap().keySet()) {
            if(selectionsMap.containsKey(title)) {

                T thisCiphertext = this.selectionsMap.get(title);
                T otherCiphertext = other.selectionsMap.get(title);

                resultMap.put(title, (T) thisCiphertext.operate(otherCiphertext));
            }

        }

        return new EncryptedRaceSelection<>(resultMap, getTitle(), this.size+other.size);
    }

    /**
     *
     * @return
     */
    public String getTitle(){
        return title;
    }

    /**
     *
     * @param selectionsMap
     * @return
     */
    private boolean verifySum(Map<String, T> selectionsMap, int min, int max, IPublicKey PEK) {

        /* Should each Ciphertext have proofs inside or just have all their proofs inside VoteProof (or both)? */
        /* Should each Vote have to pass itself to its sumProof for verification? */
        /* VoteProof should probably contain Ciphertext proofs and Ciphertexts */

        Set<Map.Entry<String,T>> eSet = selectionsMap.entrySet();

        /* Pull out an arbitrary ciphertext to get an instance of T */
        T arbitraryCiphertext = (T)(eSet.toArray(new Map.Entry[eSet.size()])[0].getValue());

        /* Create an identity for T */
        T summed = (T)(CiphertextFactory.identity(arbitraryCiphertext.getClass(),PEK));

        for(Map.Entry<String, T> entry : selectionsMap.entrySet()) {
            summed = (T)(summed.operate(entry.getValue(), PEK));
        }

        return summed.verify(min, max, PEK);
    }

    /**
     *
     * @param min
     * @param max
     * @return
     */
    public boolean verify(int min, int max, IPublicKey PEK) {

        for (Map.Entry<String, T> entry : selectionsMap.entrySet()) {
            if (!entry.getValue().verify(min, max, PEK)) {
                return false;
            }
        }

        return verifySum(selectionsMap, min, max, PEK);
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
