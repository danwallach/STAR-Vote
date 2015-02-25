package crypto.adder;

import crypto.EncryptedVote;
import crypto.IHomomorphicCiphertext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an election.
 *
 * @author David Walluck
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 */
public class Election {

    /** The public key used to encrypt and tally */
    private AdderPublicKey PEK;

    /** The List of all Votes cast in this Election */
    private List<EncryptedVote> votes;

    /** The List of all candidates in this Election (race) */
    private List<String> choices;

    /**
     * Creates a new election.
     *
     * @param publicKey         the public key
     */
    public Election(AdderPublicKey publicKey, List<String> choices) {
        this.PEK = publicKey;
        this.votes = new ArrayList<>();
        this.choices = choices;
    }

    /**
     * Gets the votes of this election.
     *
     * @return          the votes
     */
    public List<EncryptedVote> getVotes() {
        return votes;
    }

    /**
     * Casts the given vote in this election.
     *
     * @param vote      the vote
     */
    public void castVote(EncryptedVote vote) {
        votes.add(vote);
    }

    /**
     * Sums the votes cast in this election.
     * This is the product of the votes modulo <tt>p</tt>).
     *
     * @return          a vote representing the total of the given list of votes
     */
    public <T extends IHomomorphicCiphertext> EncryptedVote sumVotes() {

        /* Pull out the first vote */
        EncryptedVote v = votes.get(0);

        Map<String, T> cipherMap = new HashMap<>();

        cipherMap.putAll(v.getVoteMap());

        /* Construct a bunch of individual multiplicative identities */
        for (String name: choices)
            cipherMap.put(name, T.getHomomorphicIdentity(PEK.getP()));


        /* Create a new multiplicative identity */
        EncryptedVote total = new EncryptedVote(cipherMap, v.getTitle());

        /* Multiply all the votes together */
        for (EncryptedVote vote : votes)
            total = vote.operate(total);

        /* These are aliasing checks */

        /* Compute the sumProof for this totalled Vote and put it into total */
        //MembershipProof sumProof = total.computeSumProof(votes.size(), PEK);
        total.computeSumProof(votes.size(), PEK);

        /*
          Create an empty ArrayList for proofList. This doesn't seem to need to be filled out
          because individual NIZKs are checked pre-sum and we check the sum post-sum
        */
        //List<MembershipProof> proofList = new ArrayList<>();

        /* Construct a new vote with the proofs and all the information */
        //Vote summedVote = new Vote(total.getCipherList(), total.getChoices(), new VoteProof(sumProof, proofList));

        /* ---------------- TESTING ---------------- */

        System.out.println("In Election.sumVotes() -- Testing single vote summed, Max expected value: " + votes.size());
        System.out.println("In Election.sumVotes() -- [Single vote summed] sumProof verfied: " + total.verifyVoteProof(PEK, 0, votes.size()));
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
     *                          votes' h^y values wrapped in an additional list)
     *
     * @param  sum              the sum
     *
     * @param  masterKey        the master public key
     *
     * @return                  the final vote tally
     */
    public List<AdderInteger> getFinalSum(List<AdderInteger> partialSums, EncryptedVote sum, AdderPublicKey masterKey) {

        /*

    	  Adder encrypt is of m (public initial g, p, h) [inferred from code]
    	                    m = {0, 1}
    	                    g' = g^r = g^y
    	                    h' = h^r * f^m = h^y * m'

    	  Quick decrypt (given r) [puzzled out by Kevin Montrose]
    	                    confirm g^r = g'
    	                    m' = (h' / (h^r)) = h' / h^y
    	                    if(m' == f) m = 1
    	                    if(m' == 1) m = 0

    	*/

        /* Get relevant key data */
        AdderInteger q = masterKey.getQ();
        AdderInteger f = masterKey.getF();

        List<AdderInteger> coeffs = new ArrayList<>();
        coeffs.add(AdderInteger.ZERO);

        /* Extract the ciphertexts */
        List<ElgamalCiphertext> cipherList = sum.getCipherList();

        /* Figure out how many ciphertexts there are */
        int csize = cipherList.size();

        List<AdderInteger> results      = new ArrayList<>();

        /* For each cipher (i.e. for each candidate) */
        for (int i = 0; i < csize; i++) {


            /* Pull out the ith partial sum (equals h^y) */
            AdderInteger product = partialSums.get(i);

            /* Get the public value from the ith ciphertext (encrypted sum for ith candidate) (bigH = h' = h^y * f^m) (bigG = g^y) */
            AdderInteger bigH = (cipherList.get(i)).getH();

            /* Divide h' / h^y = f^m, where m = total number of votes for a candidate */
            AdderInteger target = bigH.divide(product);

            /* Indicates if we have successfully resolved the ciphertext */
            boolean gotResult = false;

            AdderInteger j = null;

            /* Possible total number of votes for a candidate */
            int numVotes = votes.size();

            /* Iterate over the number of votes to try to guess n */
            for (int k = 0; k <= numVotes; k++) {

                /* Create a guess */
                j = new AdderInteger(k, q);

                //System.out.println("DOES " + f.pow(j) + " equal " + target + "?");

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
