package supervisor.model;

import auditorium.Bugout;
import crypto.EncryptedVote;
import crypto.adder.AdderPublicKey;
import crypto.adder.AdderVote;
import crypto.adder.Election;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is one half of the STAR-Vote tallier model. Its job is to homorphically tally a
 * list of Ballot objects and return one Ballot object with encrypted sums for each vote.
 *
 * @author Matt Bernhard
 */
public class SupervisorTallier implements Serializable {

    /**
     * Sum every vote in cast and return a ballot with encrypted sums.
     *
     * @param precinctID    the ID of the precinct constructing this tallied Ballot, used as a ballot id
     * @param cast          the list of cast ballots that should be homomorphically summed
     * @param finalPublicKey     the public key used for vote proofs
     * @return              a Ballot containing the encrypted sums for each race
     */
    public static Ballot<EncryptedVote> tally(String precinctID, List<Ballot<EncryptedVote>> cast){

        int size=0;

        /* The results of the election are stored by race ID in this map */
        Map<String, Election> results = new HashMap<>();

        /* For each ballot, get each vote and build a results mapping between race ids and elections */
        for (Ballot bal : cast) {

            try {

                List<AdderVote> votes = bal.getVotes();

                /* Cycle through each of the races */
                for(AdderVote vote: votes){

                    /* Get all the candidate choices */
                    List<ASExpression> possibleChoices = vote.getChoices();

                    /* TODO just get PEK */
                    AdderPublicKey ballotKey = bal.getPublicKey();

                    /* Confirm that the keys are the same */
                    if (!(ballotKey.equals(finalPublicKey))) {
                        Bugout.err("!!!Expected supplied final PublicKey to match generated\nSupplied: " + ballotKey + "\nGenerated: " + finalPublicKey + "!!!");
                        return null;
                    }

                    /* Confirm that the vote proof is valid */
                    if (!vote.verifyVoteProof(finalPublicKey, 0, 1)) {
                        Bugout.err("!!!Ballot failed NIZK test!!!");
                        return null;
                    }

                    /* Code these results as a subelection so the ciphers can be summed homomorphically */
                    String raceID = makeId(possibleChoices);
                    Election election = results.get(raceID);

                    /* If we haven't seen this specific race before, initialize it */
                    if (election == null)
                        election = new Election(finalPublicKey, possibleChoices);

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
                e.printStackTrace();
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
            AdderVote vote = results.get(id).sumVotes();


            /* Verify the voteProof and error off if bad */
            if(vote.verifyVoteProof(finalPublicKey, 0, thisRace.getVotes().size())) {
                votes.add(vote);
                voteASE.add(vote.toASE());
            }
            else System.err.println("There was a bad summed vote that was not added to the ballot!");

        }

        /* Create the nonce */
        ListExpression voteList = new ListExpression(voteASE);
        ASExpression nonce = StringExpression.makeString(voteList.getSHA256());

        /* Return the Ballot of all the summed race results */
        return new Ballot(precinctID, votes, nonce, finalPublicKey,size);
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
