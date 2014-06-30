package supervisor.model;

import auditorium.Bugout;
import crypto.adder.Election;
import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import crypto.adder.Vote;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the web-server half of the STAR-Vote tallier model. Its function is to
 * decrypt challenged ballots and tally and decrypt final vote counts across and within precincts.
 *
 * @author Matthew Kindy II [shamelessly copied from SupervisorTallier.java currently]
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
    public static Ballot tally(String ID, List<Ballot> toSum, PublicKey publicKey){

        /* The results of the election are stored by race ID in this map */
        Map<String, Election> results = new HashMap<>();

        /* For each ballot, get each vote and build a results mapping between race ids and elections */
        for (Ballot bal : toSum) {

            try {

                List<Vote> votes = bal.getVotes();

                /* Cycle through each of the races */
                for(Vote vote: votes){

                    /* Get all the candidate choices */
                    List<ASExpression> possibleChoices = vote.getChoices();

                    PublicKey ballotKey = bal.getPublicKey();

                    /* Confirm that the keys are the same */
                    if (!(ballotKey.equals(publicKey))) {
                        Bugout.err("!!!Expected supplied final PublicKey to match generated\nSupplied: " + ballotKey + "\nGenerated: " + publicKey + "!!!");
                        return null;
                    }

                    /* Confirm that the vote proof is valid */
                    if (!vote.verify(publicKey, 0, 1)) {
                        Bugout.err("!!!Ballot failed NIZK test!!!");
                        return null;
                    }

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
            }
            catch (Exception e) {
                Bugout.err("Malformed ballot received <" + e.getMessage() + ">");
                Bugout.err("Rejected ballot:\n" + bal);
            }
        }

        /* This will hold the final list of summed Votes to be put into a Ballot */
        ArrayList<Vote> votes = new ArrayList<>();

        /* This will be used to create the nonce eventually */
        ArrayList<ASExpression> voteASE = new ArrayList<>();

        /* Now go through each race */
        for(String id :  results.keySet()) {

            /* Get the race */
            Election thisRace = results.get(id);

            /* Get the homomorphically tallied vote for this race */
            Vote vote = results.get(id).sumVotes();

            /* Verify the voteProof and error off if bad */
            if(vote.verifyVoteProof(publicKey, 0, thisRace.getVotes().size())) {
                votes.add(vote);
                voteASE.add(vote.toASE());
            }
            else System.err.println("There was a bad summed vote that was not added to the ballot!");
        }

        /* Create the nonce */
        ListExpression voteList = new ListExpression(voteASE);
        ASExpression nonce = StringExpression.makeString(voteList.getSHA256());

        /* Return the Ballot of all the summed race results */
        return new Ballot(ID, votes, nonce, publicKey);
    }

    /**
     * Decrypts a Ballot.
     *
     * @param toDecrypt     the Ballot to be decrypted -- it is expected that this is a challenged ballot
     */
    public static Ballot decrypt(Ballot toDecrypt, PublicKey publicKey, PrivateKey privateKey) {


                /* Something like this? */
//
//        /* For each race group (analogous to each race), decrypt the sums */
//        for(String group : _results.keySet()){
//
//            /* Here our races are represented as "Elections", a class provided in the UConn encryption code */
//            Election election = _results.get(group);
//
//            /* From the election, we can get the sum of cipher texts */
//            Vote cipherSum = election.sumVotes();
//
//            /*
//             * As per the Adder decryption process, partially decrypt the ciphertext to generate some necessary
//             * information for the final decryption.
//             */
//            List<AdderInteger> partialSum = _finalPrivateKey.partialDecrypt(cipherSum);
//
//            /* This is a LaGrange coefficient used as part of the decryption computations */
//            AdderInteger coeff = AdderInteger.ZERO;
//
//            /* Rely on the Adder election class to perform the final decryption of the election sums */
//            List<AdderInteger> results = election.getFinalSum(partialSum, cipherSum, _finalPublicKey);
//
//            /* Split off the results by candidate ID*/
//            String[] ids = group.split(",");
//
//            /* For each candidate in the race, put the decrypted sums in the results map */
//            for(int i = 0; i < ids.length; i++)
//                report.put(ids[i], results.get(i).bigintValue());
//        }

        return null;
    }

    /**
     * Decrypts all the Ballots in a List.
     *
     * @param toDecrypt     the List of Ballots to be decrypted -- it is expected that
     *                      these are challenged ballots
     */
    public static List<Ballot> decryptAll(List<Ballot> toDecrypt, PublicKey publicKey, PrivateKey privateKey) {

        List<Ballot> decryptedList = new ArrayList<>();

        for (Ballot ballot : toDecrypt)
            decryptedList.add(decrypt(ballot, publicKey, privateKey));

        return decryptedList;
    }

    /**
     * Calculates the individual vote totals for each of the candidates in each of the races in the Ballot
     *
     * @see Election#getFinalSum(List, Vote, PublicKey)
     * @see crypto.BallotEncrypter#adderDecryptWithKey(Election, PublicKey, PrivateKey)
     *
     * @param toTotal       the previously tallied Ballot from which to extract the candidate sums
     * @return              a mapping of candidates to vote totals for all of the races in toTotal
     */
    public static Map<String, BigInteger> getVoteTotals(Ballot toTotal, PublicKey publicKey) {

        /* Create an election */

        /* Generate the final private and public keys */

        /* Partially Decrypt the partial sums */

        /* Add and decrypt to get the final sums */

        return null;
    }

    /**
     * Confirms that the vote, voteIds, proof, and publicKey fields pulled out of a ballot are well-formed.
     */
    private static void confirmValid(ListExpression vote, ListExpression voteIds, ListExpression proof, ListExpression publicKey){
        if(!vote.get(0).toString().equals("vote"))
            throw new RuntimeException("Missing \"vote\"");

        if(!voteIds.get(0).toString().equals("vote-ids"))
            throw new RuntimeException("Missing \"vote-ids\"");

        if(!proof.get(0).toString().equals("proof"))
            throw new RuntimeException("Missing \"proof\"");

        if(!publicKey.get(0).toString().equals("public-key"))
            throw new RuntimeException("Missing \"public-key\"");
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