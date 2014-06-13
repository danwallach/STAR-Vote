package crypto.adder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents an election.
 *
 * @author David Walluck
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 */
public class Election {
    private AdderInteger p;
    private List<Vote> votes;

    /**
     * Creates a new election.
     *
     * @param p         the prime
     */
    public Election(AdderInteger p) {
        this.p = p;
        this.votes = new ArrayList<>();
    }

    /**
     * Gets the votes of this election.
     *
     * @return          the votes
     */
    public List<Vote> getVotes() {
        return votes;
    }

    /**
     * Casts the given vote in this election.
     *
     * @param vote      the vote
     */
    public void castVote(Vote vote) {
        votes.add(vote);
    }

    /**
     * Sums the votes cast in this election.
     * This is the product of the votes modulo <tt>p</tt>).
     *
     * @return          a vote representing the total of the given list of votes
     */
    public Vote sumVotes() {

        /* Pull out the first vote */
        Vote v = votes.get(0);

        List<ElgamalCiphertext> initList = new ArrayList<>();

        /*
          Load up the initList with new ciphertexts only from the first Vote in the Election
          This specific vote will be used a query used to calculate the total number of votes.
        */
        for (ElgamalCiphertext ignored : v.getCipherList()) {
            ElgamalCiphertext ciphertext = new ElgamalCiphertext(AdderInteger.ONE, AdderInteger.ONE, p);
            initList.add(ciphertext);
        }

        /* Create a single ballot from the initList*/
        Vote total = new Vote(initList);

        /* Homomorphically tally the encrypted votes */
        for (Vote vote : votes)
            total = vote.multiply(total);

        return total;
    }

    /**
     * Gets the final sum given the partial sums, the coefficients, the vote
     * representing the sum, and the master public key.
     * TODO make this less stupid List<List<>>
     * @param  partialSums      the partial sums
     * @param  coeffs           the coefficients
     * @param  sum              the sum
     * @param  masterKey        the master public key
     * @return                  the final vote tally
     */
    public List<AdderInteger> getFinalSum(List<List<AdderInteger>> partialSums, List<AdderInteger> coeffs, Vote sum, PublicKey masterKey) {

        /* Get relevant key data */
        AdderInteger p = masterKey.getP();
        AdderInteger q = masterKey.getQ();
        AdderInteger g = masterKey.getG();
        AdderInteger f = masterKey.getF();

        /* Build a LaGrange polynomial */
        Polynomial poly = new Polynomial(p, g, f, coeffs);
        List<AdderInteger> lagrangeCoeffs = poly.lagrange();

        /* Get the number of LaGrange coefficients (==1)*/
        int lsize = lagrangeCoeffs.size();

        /* Extract the ciphertexts */
        List<ElgamalCiphertext> cipherList = sum.getCipherList();

        /* Figure out how many ciphertexts there are */
        int csize = cipherList.size();

        List<AdderInteger> productList  = new ArrayList<>();
        List<AdderInteger> results      = new ArrayList<>();

        /* For each cipher (i.e. for each race) */
        for (int i = 0; i < csize; i++) {

            /* Adding a one to the product list so that we don't get a null pointer or zero later */
            productList.add(new AdderInteger(AdderInteger.ONE, p));

            /* Iterate over the LaGrange coefficients */
            for (int j = 0; j < lsize; j++) {

                /* Get out the ith product */
                AdderInteger pli =  productList.get(i);

                /* Pull out the jth partial sum (cast to List), which is the first encrypted result for the race */
                List ps = (List) partialSums.get(j);

                /* Pull out the ith ciphertext and the jth LaGrange coefficient */
                AdderInteger psi = (AdderInteger) ps.get(i);
                AdderInteger lcj = lagrangeCoeffs.get(j);

                /* Multiply these together */
                AdderInteger product = psi.pow(lcj).multiply(pli);

                /* Store the updated product back in the list */
                productList.set(i, product);
            }

            /* Get the public value from the ith ciphertext (bigH = h' = h^y * f^m) (bigG = g^y) */
            AdderInteger bigH = (cipherList.get(i)).getH();

            /* Divide h' / (h^y * f^n) = f^(m-n), where n = total number of votes for a candidate */
            AdderInteger target = bigH.divide(productList.get(i));

            /* Indicates if we have successfully resolved the ciphertext */
            boolean gotResult = false;

            AdderInteger j = null;

            /* Possible total number of votes for a candidate */
            int numVotes = votes.size();

            /* Iterate over the number of votes to try to guess n */
            for (int k = 0; k <= numVotes; k++) {

                /* Create a guess */
                j = new AdderInteger(k, q);

                System.out.println("DOES " + f.pow(j) + " equal " + target + "?");

                /* Check the guess and get out when found */
                if (f.pow(j).equals(target)) {
                    gotResult = true;
                    break;
                }
            }

            /* Keep track of found result, otherwise error */
            if (gotResult) results.add(j);
            else throw new SearchSpaceExhaustedException("Error searching for " + target);

        }

        return results;
    }
}
