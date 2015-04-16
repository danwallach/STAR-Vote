package utilities;

import auditorium.Bugout;
import crypto.AHomomorphicCiphertext;
import crypto.EncryptedRaceSelection;
import crypto.ExponentialElGamalCiphertext;
import crypto.IPublicKey;
import crypto.adder.*;
import crypto.interop.AdderKeyManipulator;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.Ballot;

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
     * @param PEK           the public key used for vote proofs
     * @return              a Ballot containing the encrypted sums for each race
     */
    public static <T extends AHomomorphicCiphertext> Ballot<EncryptedRaceSelection<T>> tally(
            String ID, List<Ballot<EncryptedRaceSelection<T>>> toSum, IPublicKey PEK){

        int size = 0;

        /* The results of the election are stored by race ID in this map */
        Map<String, Race<T>> results = new HashMap<>();

        /* For each ballot, get each vote and build a results mapping between race ids and elections */
        for (Ballot<EncryptedRaceSelection<T>> bal : toSum) {

            try {

                List<EncryptedRaceSelection<T>> raceSelections = bal.getRaceSelections();

                /* Cycle through each of the races */
                for(EncryptedRaceSelection<T> rs: raceSelections){

                    /* Get all the candidate choices */
                    List<String> possibleCandidates = new ArrayList<>(rs.getRaceSelectionsMap().keySet());

                    /* Confirm that the rs proof is valid */
                    System.out.println("In WebserverTallier.tally() -- verifying the race selections. ");

                    if (!rs.verify(0, 1, PEK)) {
                        Bugout.err("!!!Ballot failed NIZK test!!!");
                        return null;
                    }
                    else
                        System.out.println("Race selection was successfully verified!\n");

                    /* Code these results as a race so the ciphers can be summed homomorphically */
                    String raceID = rs.getTitle();
                    Race<T> race = results.get(raceID);

                    /* If we haven't seen this specific race before, initialize it */
                    if (race == null)
                        race = new Race<>(PEK, possibleCandidates);

                    /* This will ready race to homomorphically tally the rs */
                    race.castRaceSelection(rs);

                    /* Now save the result until we're ready to decrypt the totals */
                    results.put(raceID, race);
                }

                size += bal.getSize();
            }
            catch (Exception e) {
                Bugout.err("Malformed ballot received <" + e.getMessage() + ">");
                Bugout.err("Rejected ballot:\n" + bal);
            }
        }

        /* This will hold the final list of summed Votes to be put into a Ballot */
        ArrayList<EncryptedRaceSelection<T>> raceSelections = new ArrayList<>();

        /* This will be used to create the nonce eventually */
        ArrayList<ASExpression> raceSelectionsASE = new ArrayList<>();

        /* Now go through each race */
        for(String id :  results.keySet()) {

            /* Get the race */
            Race<T> thisRace = results.get(id);

            /* Get the homomorphically tallied race selection for this race */
            System.out.println("Entering Race.sumRaceSelections(): ");
            EncryptedRaceSelection<T> summedRS = results.get(id).sumRaceSelections();

            /* Verify the race selection proof and error off if bad */

            System.out.println("In WebserverTallier.tally() -- Verifying this summed race ");
            if(summedRS.verify(0, thisRace.getRaceSelections().size(), PEK)) {
                raceSelections.add(summedRS);
                raceSelectionsASE.add(summedRS.toASE());
                System.out.println("This race was successfully added to the Ballot!");
            }
            else System.err.println("There was a bad summed race that was not added to the ballot!");
        }

        /* Create the nonce */
        ListExpression rsList = new ListExpression(raceSelectionsASE);
        String nonce = StringExpression.makeString(rsList.getSHA256()).toString();

        /* Return the Ballot of all the summed race results */
        return new Ballot<>(ID, raceSelections, nonce, size);
    }

//    /**
//     * Decrypts a Ballot.
//     *
//     * @param toDecrypt     the Ballot to be decrypted -- it is expected that this is a challenged ballot
//     * @return              a list of candidates that were selected
//     */
//    public static <T extends AHomomorphicCiphertext> List<String> decrypt(
//            Ballot<EncryptedRaceSelection<T>> toDecrypt, IPublicKey publicKey, IPrivateKey privateKeyShare) {
//
//        /* Get the mapping of candidates to votes for this Ballot */
//        Map<String, Map<String, BigInteger>> racesToCandidateTotals = getVoteTotals(toDecrypt, 1, publicKey, privateKey);
//        List<String> selections = new ArrayList<>();
//
//        /* Add the selected candidates to the list */
//        for (String s : racesToCandidateTotals.keySet()) {
//
//            Map<String, BigInteger> candidatesToTotals = racesToCandidateTotals.get(s);
//
//            for (String candidate : candidatesToTotals.keySet()) {
//                if (candidatesToTotals.get(s).equals(BigInteger.ONE)) {
//                    selections.add(s);
//                    break;
//                }
//            }
//        }
//
//        return selections;
//    }
//
//    /**
//     * Calculates the individual vote totals for each of the candidates in each of the races in the Ballot
//     *
//     * @see crypto.adder.Race#getFinalSum(java.util.List, crypto.adder.AdderVote, crypto.adder.AdderPublicKey)
//     * @see crypto.BallotEncrypter#adderDecryptWithKey(crypto.adder.Race, crypto.adder.AdderPublicKey, crypto.adder.AdderPrivateKeyShare)
//     *
//     * @param toTotal       the previously tallied Ballot from which to extract the candidate sums
//     * @param size          the "size" of the Ballot (the number of combined Ballots added to create this Ballot)
//     * @param finalPublicKey     the public key
//     * @param privateKey    the private key
//     * @return              a mapping of candidates to vote totals (mapped to race names) for each race in a Ballot
//     */
//    public static Map<String, Map<String,BigInteger>> getVoteTotals(Ballot toTotal, int size, AdderPublicKey finalPublicKey, AdderPrivateKeyShare privateKey) {
//
//        /* Generate the final private key */
//        AdderPrivateKeyShare finalPrivateKey = AdderKeyManipulator.generateFinalPrivateKey(finalPublicKey, privateKey);
//
//        /* Currently we can't generate the proper finalPrivateKey due to AdderKeyManipulator not having generated the finalPublicKey/polynomial
//         * To fix this, somehow the finalPrivateKey needs to be generated with the same information in AdderKeyManipulator as when the
//         * finalPublicKey was generated
//         */
//
//        Map<String, Map<String,BigInteger>> voteTotals = new TreeMap<>();
//
//        /* Iterate over each of the races */
//        for (AdderVote v: toTotal.getVotes()) {
//
//            String raceName = v.getRaceTitle();
//
//            /* Get the candidates */
//            List<ASExpression> raceCandidates = v.getChoices();
//
//            /* Partially Decrypt the partial sums */
//            List<AdderInteger> partialSum = finalPrivateKey.partialDecrypt(v);
//
//            /* Get the final sums */
//            System.out.println("I'm the size! " + size);
//            List<AdderInteger> finalSum = getDecryptedFinalSum(partialSum, v, size, finalPublicKey);
//
//            voteTotals.put(raceName, new TreeMap<String, BigInteger>());
//
//            /* Map the candidates to their results in this race */
//            for(int i=0; i< raceCandidates.size(); i++)
//                voteTotals.get(raceName).put(raceCandidates.get(i).toString(), finalSum.get(i).bigintValue());
//
//        }
//
//        return voteTotals;
//    }
//
//    /**
//     *
//     * @param partialSums           the list of partial sums gathered from the summed Vote
//     * @param sum                   the summed Vote for which to get the decrypted totals
//     * @param size                  the possible total number of votes for a candidate in a race
//     * @param masterKey             the public key
//     * @return                      a vector of vote totals for each candidate in this Vote (race)
//     */
//    private static List<AdderInteger> getDecryptedFinalSum(List<AdderInteger> partialSums, AdderVote sum, int size, AdderPublicKey masterKey) {
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
//        /* Extract the ciphertexts */
//        List<AdderElgamalCiphertext> cipherList = sum.getCipherList();
//
//        /* Figure out how many ciphertexts there are */
//        int csize = cipherList.size();
//
//        List<AdderInteger> results = new ArrayList<>();
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
//            /* Divide h' / h^y = f^m, where m = total number of votes for a candidate */
//            AdderInteger target = bigH.divide(product);
//
//            /* Indicates if we have successfully resolved the ciphertext */
//            boolean gotResult = false;
//
//            AdderInteger j = null;
//
//            /* Iterate over the number of votes to try to guess n */
//            for (int k = 0; k <= size; k++) {
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

    /**
     * Using NIZKs, imposes structure on our race format we haven't had before.
     *
     * @param voteIds       a list of strings representing vote identifiers
     * @return              a string representation of the list of voteIDs
     */
    private static String makeId(List<String> voteIds){
        String str = voteIds.get(0);
        for(int i = 1; i < voteIds.size(); i++)
            str+=","+voteIds.get(i);

        return str;
    }
}