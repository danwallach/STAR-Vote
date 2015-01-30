package supervisor.model;

import auditorium.Bugout;
import crypto.adder.*;
import crypto.interop.AdderKeyManipulator;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.math.BigInteger;
import java.util.*;

/**
 * This class is the web-server half of the STAR-Vote tallier model. Its function is to
 * decrypt challenged ballots and tally and decrypt final vote counts across and within precincts.
 *
 * @author Matthew Kindy II
 */
public class WebServerTallier {

    /**
     * Sum every vote in cast and return a Ballot with encrypted sums that includes every race
     * across all Ballots.
     *
     * @param ID            the ID to be assigned to this ballot post-tallying, used as a ballot id
     * @param toSum         the list of cast ballots that should be homomorphically summed
     * @param publicKey     the public key used for vote proofs
     * @return              a Ballot containing the encrypted sums for each race
     */
    public static Ballot tally(String ID, List<Ballot> toSum, AdderPublicKey publicKey){

        int size=0;

        /* The results of the election are stored by race ID in this map */
        Map<String, Election> results = new HashMap<>();

        /* For each ballot, get each vote and build a results mapping between race ids and elections */
        for (Ballot bal : toSum) {

            try {

                List<AdderVote> votes = bal.getVotes();

                /* Cycle through each of the races */
                for(AdderVote vote: votes){

                    /* Get all the candidate choices */
                    List<ASExpression> possibleChoices = vote.getChoices();

                    /* Confirm that the vote proof is valid */
                    System.out.println("In WebserverTallier.tally() -- verifying the VoteProofs. ");

                    if (!vote.verifyVoteProof(publicKey, 0, 1)) {
                        Bugout.err("!!!Ballot failed NIZK test!!!");
                        return null;
                    }
                    else
                        System.out.println("Vote was successfully verified!\n");

                    /* Code these results as a subelection so the ciphers can be summed homomorphically */
                    String raceID = makeId(possibleChoices);
                    Election election = results.get(raceID);

                    /* If we haven't seen this specific race before, initialize it */
                    if (election == null)
                        election = new Election(publicKey, possibleChoices);

                    /* This will ready election to homomorphically tally the vote */
                    election.castVote(vote);

                    /* Now save the result until we're ready to decrypt the totals */
                    results.put(raceID, election);
                }

                size += bal.getSize();
            }
            catch (Exception e) {
                Bugout.err("Malformed ballot received <" + e.getMessage() + ">");
                Bugout.err("Rejected ballot:\n" + bal);
            }
        }

        /* This will hold the final list of summed Votes to be put into a Ballot */
        ArrayList<AdderVote> votes = new ArrayList<>();

        /* This will be used to create the nonce eventually */
        ArrayList<ASExpression> voteASE = new ArrayList<>();

        /* Now go through each race */
        for(String id :  results.keySet()) {

            /* Get the race */
            Election thisRace = results.get(id);

            /* Get the homomorphically tallied vote for this race */
            System.out.println("Entering Election.sumVotes(): ");
            AdderVote vote = results.get(id).sumVotes();

            /* Verify the voteProof and error off if bad */

            System.out.println("In WebserverTallier.tally() -- Verifying this vote ");
            if(vote.verifyVoteProof(publicKey, 0, thisRace.getVotes().size())) {
                votes.add(vote);
                System.out.println("Vote: " + vote.getRaceTitle() + "\nVote ASE: " + vote.toASE());
                voteASE.add(vote.toASE());
                System.out.println("This Vote was successfully added to the Ballot!");
            }
            else System.err.println("There was a bad summed vote that was not added to the ballot!");
        }

        /* Create the nonce */
        ListExpression voteList = new ListExpression(voteASE);
        ASExpression nonce = StringExpression.makeString(voteList.getSHA256());

        /* Return the Ballot of all the summed race results */
        return new Ballot(ID, votes, nonce, publicKey, size);
    }

    /**
     * Decrypts a Ballot.
     *
     * @param toDecrypt     the Ballot to be decrypted -- it is expected that this is a challenged ballot with size of 1
     * @return              a list of candidates that were selected
     */
    public static List<String> decrypt(Ballot toDecrypt, AdderPublicKey publicKey, AdderPrivateKeyShare privateKey) {

        /* Get the mapping of candidates to votes for this Ballot */
        Map<String, BigInteger> candidatesToTotals = getVoteTotals(toDecrypt, 1, publicKey, privateKey);
        List<String> selections = new ArrayList<>();

        /* Add the selected candidates to the list */
        for (String s : candidatesToTotals.keySet())
            if (candidatesToTotals.get(s).equals(BigInteger.ONE))
                selections.add(s);

        return selections;
    }

    /**
     * Decrypts all the Ballots in a List.
     *
     * @param toDecrypt     the List of Ballots to be decrypted -- it is expected that
     *                      these are challenged ballots with sizes of 1
     */
    public static List<List<String>> decryptAll(List<Ballot> toDecrypt, AdderPublicKey publicKey, AdderPrivateKeyShare privateKey) {

        List<List<String>> decryptedList = new ArrayList<>();

        for (Ballot ballot : toDecrypt)
            decryptedList.add(decrypt(ballot, publicKey, privateKey));

        return decryptedList;
    }

    /**
     * Calculates the individual vote totals for each of the candidates in each of the races in the Ballot
     *
     * @see Election#getFinalSum(List, crypto.adder.AdderVote, crypto.adder.AdderPublicKey)
     * @see crypto.BallotEncrypter#adderDecryptWithKey(Election, crypto.adder.AdderPublicKey, crypto.adder.AdderPrivateKeyShare)
     *
     * @param toTotal       the previously tallied Ballot from which to extract the candidate sums
     * @param size          the "size" of the Ballot (the number of combined Ballots tallied to create this Ballot)
     * @param publicKey     the public key
     * @param privateKey    the private key
     * @return              a mapping of candidates to vote totals for all of the races in toTotal
     */
    public static TreeMap<String, BigInteger> getVoteTotals(Ballot toTotal, int size, AdderPublicKey publicKey, AdderPrivateKeyShare privateKey) {

        /* Generate the final private and public keys */
        AdderPublicKey finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(publicKey);
        AdderPrivateKeyShare finalPrivateKey = AdderKeyManipulator.generateFinalPrivateKey(publicKey, privateKey);

        TreeMap<String, BigInteger> voteTotals = new TreeMap<>();

        /* Iterate over each of the races */
        for (AdderVote v: toTotal.getVotes()) {

            /* Get the candidates */
            List<ASExpression> raceCandidates = v.getChoices();

            /* Partially Decrypt the partial sums */
            List<AdderInteger> partialSum = finalPrivateKey.partialDecrypt(v);

            /* Get the final sums */
            List<AdderInteger> finalSum = getDecryptedFinalSum(partialSum, v, size, finalPublicKey);

            /* Map the candidates to their results in this race */
            for(int i=0; i< raceCandidates.size(); i++)
                voteTotals.put(raceCandidates.get(i).toString(), finalSum.get(i).bigintValue());
        }

        return voteTotals;
    }

    /**
     *
     * @param partialSums           the list of partial sums gathered from the summed Vote
     * @param sum                   the summed Vote for which to get the decrypted totals
     * @param size                  the possible total number of votes for a candidate in a race
     * @param masterKey             the public key
     * @return                      a vector of vote totals for each candidate in this Vote (race)
     */
    private static List<AdderInteger> getDecryptedFinalSum(List<AdderInteger> partialSums, AdderVote sum, int size, AdderPublicKey masterKey) {

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

        /* Extract the ciphertexts */
        List<ElgamalCiphertext> cipherList = sum.getCipherList();

        /* Figure out how many ciphertexts there are */
        int csize = cipherList.size();

        List<AdderInteger> results = new ArrayList<>();

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

            /* Iterate over the number of votes to try to guess n */
            for (int k = 0; k <= size; k++) {

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

    /**
     * Using NIZKs, imposes structure on our race format we haven't had before.
     *
     * @param voteIds       a list of strings representing vote identifiers
     * @return              a string representation of the list of voteIDs
     */
    private static String makeId(List<ASExpression> voteIds){
        String str = voteIds.get(0).toString();
        for(int i = 1; i < voteIds.size(); i++)
            str+=","+voteIds.get(i);

        return str;
    }
}