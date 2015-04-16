package crypto.adder;

import crypto.EncryptedRaceSelection;
import crypto.AHomomorphicCiphertext;
import crypto.IPublicKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an election.
 *
 * @author David Walluck
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 */
public class Race<T extends AHomomorphicCiphertext> {

    /** The public key used to encrypt and tally */
    private IPublicKey PEK;

    /** The List of all race selections cast in this Race */
    private List<EncryptedRaceSelection<T>> raceSelections;

    /** The List of all candidates in this Race */
    private List<String> candidates;

    /**
     * Creates a new election.
     *
     * @param publicKey         the public key
     */
    public Race(IPublicKey publicKey, List<String> candidates) {
        this.PEK = publicKey;
        this.raceSelections = new ArrayList<>();
        this.candidates = candidates;
    }

    /**
     * Gets the raceSelections of this election.
     *
     * @return          the raceSelections
     */
    public List<EncryptedRaceSelection<T>> getRaceSelections() {
        return raceSelections;
    }

    /**
     * Casts the given raceSelection in this election.
     *
     * @param raceSelection      the raceSelection
     */
    public void castRaceSelection(EncryptedRaceSelection<T> raceSelection) {
        raceSelections.add(raceSelection);
    }

    /**
     * Sums the raceSelections cast in this election.
     * This is the product of the raceSelections modulo <tt>p</tt>).
     *
     * @return          a vote representing the total of the given list of raceSelections
     */
    public EncryptedRaceSelection<T> sumRaceSelections() {

        /* Pull out the first vote */
        EncryptedRaceSelection<T> v = raceSelections.get(0);

        /* Create a new multiplicative identity */
        EncryptedRaceSelection<T> total = EncryptedRaceSelection.identity(v, PEK);

        /* Multiply all the raceSelections together and recompute proof */
        for (EncryptedRaceSelection<T> rs : raceSelections)
            total = rs.operate(total, PEK);

        /* ---------------- TESTING ---------------- */

        System.out.println("In Race.sumRaceSelections() -- Testing single race selection summed, Max expected value: " + raceSelections.size());
        System.out.println("In Race.sumRaceSelections() -- [Single vote summed] sum verfied: " + total.verify(0, raceSelections.size(), PEK));
        System.out.println("-----------------");

        /* ------------------------------------------ */

        return total;
    }

    /**
     * Gets the final sum given the partial sums, the coefficients, the vote
     * representing the sum, and the master public key.
     * TODO make this less stupid List<List<>> and LaGrange coefficients, among others
     *
     * @param  partialSums      the partial sums (the list of products of the encrypted
     *                          raceSelections' h^y values wrapped in an additional list)
     *
     * @param  sum              the sum
     *
     * @param  masterKey        the master public key
     *
     * @return                  the final vote tally
     */

//
//    public List<AdderInteger> getFinalSum(List<AdderInteger> partialSums, EncryptedRaceSelection sum, AdderPublicKey masterKey) {
//
//        /*
//
//    	  Adder encrypt is of m (public initial g, p, h) [inferred from code]
//    	                    m = {0, 1}
//    	                    g' = g^r = g^y
//    	                    h' = h^r * f^m = h^y * m'
//
//    	  Quick decrypt (given r) [puzzled out by Kevin Montrose]
//    	                    confirm g^r = g'
//    	                    m' = (h' / (h^r)) = h' / h^y
//    	                    if(m' == f) m = 1
//    	                    if(m' == 1) m = 0
//
//    	*/
//
//        /* Get relevant key data */
//        AdderInteger q = masterKey.getQ();
//        AdderInteger f = masterKey.getF();
//
//        List<AdderInteger> coeffs = new ArrayList<>();
//        coeffs.add(AdderInteger.ZERO);
//
//        /* Extract the ciphertexts */
//        List<ElgamalCiphertext> cipherList = sum.getRaceSelectionsMap();
//
//        /* Figure out how many ciphertexts there are */
//        int csize = cipherList.size();
//
//        List<AdderInteger> results      = new ArrayList<>();
//
//        /* For each cipher (i.e. for each candidate) */
//        for (int i = 0; i < csize; i++) {
//
//
//            /* Pull out the ith partial sum (equals h^y) */
//            AdderInteger product = partialSums.get(i);
//
//            /* Get the public value from the ith ciphertext (encrypted sum for ith candidate) (bigH = h' = h^y * f^m) (bigG = g^y) */
//            AdderInteger bigH = (cipherList.get(i)).getH();
//
//            /* Divide h' / h^y = f^m, where m = total number of raceSelections for a candidate */
//            AdderInteger target = bigH.divide(product);
//
//            /* Indicates if we have successfully resolved the ciphertext */
//            boolean gotResult = false;
//
//            AdderInteger j = null;
//
//            /* Possible total number of raceSelections for a candidate */
//            int numVotes = raceSelections.size();
//
//            /* Iterate over the number of raceSelections to try to guess n */
//            for (int k = 0; k <= numVotes; k++) {
//
//                /* Create a guess */
//                j = new AdderInteger(k, q);
//
//                //System.out.println("DOES " + f.pow(j) + " equal " + target + "?");
//
//                /* Check the guess and get out when found */
//                if (f.pow(j).equals(target)) {
//                    gotResult = true;
//                    break;
//                }
//            }
//
//            /* Keep track of found result, otherwise error */
//            if (gotResult) results.add(j);
//            else throw new SearchSpaceExhaustedException("Error searching for " + target);
//        }
//
//        return results;
//    }
}
