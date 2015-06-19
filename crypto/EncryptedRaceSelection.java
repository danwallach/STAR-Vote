package crypto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An encrypted representation of the selection made for a given race.
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedRaceSelection<T extends AHomomorphicCiphertext> extends ARaceSelection implements Provable {

    private Map<String, T> selectionsMap;

    /**
     * @param selectionsMap     the map of candidate IDs to ciphertext values for this race
     * @param title             the title of this race
     * @param size              the number of raceselections accumulated in this race selection (default: 1)
     */
    public EncryptedRaceSelection(Map<String, T> selectionsMap, String title, int size) {
        super(title, size);

        this.selectionsMap = selectionsMap;
    }

    /**
     * Creates a multiplicative identity
     * @param v     the encrypted race selection to use to extract the class of S
     * @param PEK   the public encryption key used to encrypt the entire election
     * @param <S>   the homomorphic ciphertext type, dependent on crypto-type
     *
     * @return      a multiplicative identity based on this encrypted race selection
     */
    public static <S extends AHomomorphicCiphertext> EncryptedRaceSelection<S> identity(EncryptedRaceSelection<S> v, IPublicKey PEK) {

        /* This will hold the map of identity ciphertexts to put into the identity vote */
        Map<String, S> identityMap = new HashMap<>();

        /* Fill in all the entries with identities of type S */
        for(Map.Entry<String, S> entry : v.selectionsMap.entrySet())
            identityMap.put(entry.getKey(), CiphertextFactory.identity(entry.getValue().getClass(), PEK));

        /* Create new identity with size 0 (i.e. no votes cast) */
        return new EncryptedRaceSelection<>(identityMap, v.getTitle(), 0);
    }

    /**
     * @return  the map of candidate IDs to ciphertexts
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
    public EncryptedRaceSelection<T> operate(EncryptedRaceSelection<T> other, IPublicKey PEK) {

        Map<String, T> resultMap = new HashMap<>();


        resultMap.putAll(other.selectionsMap);
        resultMap.putAll(selectionsMap);

        for(String title : other.getRaceSelectionsMap().keySet()) {
            if(selectionsMap.containsKey(title)) {

                T thisCiphertext = this.selectionsMap.get(title);
                T otherCiphertext = other.selectionsMap.get(title);

                resultMap.put(title, (T) thisCiphertext.operate(otherCiphertext, PEK));
            }

        }

        return new EncryptedRaceSelection<>(resultMap, getTitle(), this.size+other.size);
    }

    /**
     * @return  the race title
     */
    public String getTitle(){
        return title;
    }

    /**
     * Checks that the sum of the selections is within a range by verifying the proof of the summed ciphertexts
     * @param   selectionsMap the map of candidate IDs to ciphertext values for this race
     *
     * @return  true if the number of 'for' votes (including abstentions) is within a range, false otherwise
     */
    private boolean verifySum(Map<String, T> selectionsMap, int value, IPublicKey PEK) {

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

        return summed.verify(value, value, PEK);
    }

    /**
     * Checks that each ciphertext is within a range and that the sum exactly the maximum value
     * due to the way that abstentions are handled
     *
     * @param min   the minimum value for an individual ciphertext (default: 0)
     * @param max   the maximum value for an individual ciphertext (default: 1 or size)
     *
     * @return      whether the encrypted race selections satisfies these requirements
     */
    public boolean verify(int min, int max, IPublicKey PEK) {

        for (Map.Entry<String, T> entry : selectionsMap.entrySet()) {
            if (!entry.getValue().verify(min, max, PEK)) {
                return false;
            }
        }

        return verifySum(selectionsMap, max, PEK);
    }

}
