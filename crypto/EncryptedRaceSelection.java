package crypto;

import java.io.Serializable;
import java.util.*;

/**
 * An encrypted representation of the selection made for a given race.
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedRaceSelection<T extends AHomomorphicCiphertext<T>> extends ARaceSelection implements Provable, Serializable {

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
    @SuppressWarnings("unchecked")
    public static <S extends AHomomorphicCiphertext<S>> EncryptedRaceSelection<S> identity(EncryptedRaceSelection<S> v, IPublicKey PEK) {

        /* This will hold the map of identity ciphertexts to put into the identity vote */
        Map<String, S> identityMap = new HashMap<>();

        /* Fill in all the entries with identities of type S */
        for(Map.Entry<String, S> entry : v.selectionsMap.entrySet())
            identityMap.put(entry.getKey(), CiphertextFactory.identity((Class<S>)entry.getValue().getClass(), PEK));

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
    @SuppressWarnings("unchecked")
    public EncryptedRaceSelection<T> operate(EncryptedRaceSelection<T> other, IPublicKey PEK) {

        Map<String, T> resultMap = new HashMap<>();

        /* Add all the candidates from both selection maps */
        resultMap.putAll(other.selectionsMap);
        resultMap.putAll(selectionsMap);

        /* If the selections maps contain the same candidates, add their ciphertexts together */
        for(String candidate : other.getRaceSelectionsMap().keySet()) {
            if(selectionsMap.containsKey(candidate)) {

                T thisCiphertext = this.selectionsMap.get(candidate);
                T otherCiphertext = other.selectionsMap.get(candidate);

                resultMap.put(candidate, thisCiphertext.operateIndependent(otherCiphertext, PEK));
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
    @SuppressWarnings("unchecked")
    private boolean verifySum(Map<String, T> selectionsMap, int value, IPublicKey PEK) {

        /* Should each Ciphertext have proofs inside or just have all their proofs inside VoteProof (or both)? */
        /* Should each Vote have to pass itself to its sumProof for verification? */
        /* VoteProof should probably contain Ciphertext proofs and Ciphertexts */

        Set<Map.Entry<String,T>> eSet = selectionsMap.entrySet();

        /* Pull out an arbitrary ciphertext to get an instance of T */
        Iterator<Map.Entry<String,T>> iter = eSet.iterator();
        T arbitraryCiphertext = iter.next().getValue();

        /* Create an identity for T */
        T summed = CiphertextFactory.identity((Class<T>)arbitraryCiphertext.getClass(),PEK);

        summed = summed.operateDependent(new ArrayList<>(selectionsMap.values()), PEK);

        return summed.verify(0, value, PEK);
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

        /* We check when we encrypt that each vote is between 0 and 1.
         * Since we know this, a proof for the sum is sufficient.
         * [1 0 1] will fail validation for 0 to 1 because the sum > 1
         * [2 0 -1] would pass sum validation, but never could
         * occur here because it would be invalidated during encryption.
         * If it were the sum of multiple votes, it would fail due to the
         * fact that we check to make sure sum of votes = number of votes
         */

        /* Otherwise would have to figure out how to add together proofs */

        for (Map.Entry<String, T> entry : selectionsMap.entrySet()) {
            if (!entry.getValue().verify(min, max, PEK)) {
                return false;
            }
        }

        return verifySum(selectionsMap, max, PEK);
    }

}
