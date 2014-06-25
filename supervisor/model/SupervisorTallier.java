package supervisor.model;

import auditorium.Bugout;
import crypto.adder.*;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

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
public class SupervisorTallier {

    /**
     * Sum every vote in cast and return a ballot with encrypted sums.
     *
     * @param precinctID    the ID of the precinct constructing this ballot, used as a ballot id
     * @param cast          the list of cast ballots that should be homomorphically summed
     * @param publicKey     the public key used for vote proofs
     * @return              a Ballot containing the encrypted sums for each race
     */
    public static Ballot tally(String precinctID, List<Ballot> cast, PublicKey publicKey){

        /* The results of the election are stored by race ID in this map */
        Map<String, Election> results = new HashMap<>();

        /* Foe each ballot, get each vote add build a results mapping between race id's and elections */
        for (Ballot bal : cast) {
            try {
                /* Check that the ballot is well-formed */
                ListExpression ballot = bal.toListExpression();

                /* Iterate through each of the races on the ballot */
                for (int i = 0; i < ballot.size(); i++) {
                    /* Retrieve the corresponding race information from this selection */
                    ListExpression raceGroup = (ListExpression) ballot.get(i);

                    /* The first entry in ballot is the vote itself */
                    ListExpression voteE = (ListExpression) raceGroup.get(0);

                    /* The second entry is the candidate identifier */
                    ListExpression voteIdsE = (ListExpression) raceGroup.get(1);

                    /* The third entry is a validity (TODO validity or integrity?) proof for the vote */
                    ListExpression proofE = (ListExpression) raceGroup.get(2);

                    /* The final entry is the public key that the vote was encrypted with */
                    ListExpression publicKeyE = (ListExpression) raceGroup.get(3);

                    /* Ensure that all of these fields are valid */
                    confirmValid(voteE, voteIdsE, proofE, publicKeyE);

                    /* Now that we know the vote is valid, read it in as an Adder Vote object */
                    Vote vote = Vote.fromASE(voteE.get(1));
                    List<String> voteIds = new ArrayList<>();

                    /* Add the candidates to a list */
                    for (int j = 0; j < voteIdsE.get(1).size(); j++)
                        voteIds.add(((ListExpression) voteIdsE.get(1)).get(j).toString());

                    /* Compute the validity proof */
                    VoteProof voteProof = VoteProof.fromASE(proofE.get(1));

                    /* Grab the supplied public key */
                    PublicKey suppliedPublicKey = PublicKey.fromASE(publicKeyE.get(1));

                    /* Confirm that the keys are the same */
                    if (!(suppliedPublicKey.toString().trim().equals(publicKey.toString().trim()))) {
                        Bugout.err("!!!Expected supplied final PublicKey to match generated\nSupplied: " + suppliedPublicKey + "\nGenerated: " + publicKey + "!!!");
                        return null;
                    }

                    /* Confirm that the vote proof is valid */
                    if (!voteProof.verify(vote, publicKey, 0, 1)) {
                        Bugout.err("!!!Ballot failed NIZK test!!!");
                        return null;
                    }

                    /* Code these results as a subelection so the ciphers can be summed homomorphically */
                    String subElectionId = makeId(voteIds);
                    Election election = results.get(subElectionId);

                    /* If we haven't seen this specific election before, initialize it */
                    if (election == null)
                        election = new Election(publicKey.getP());

                    /* This will homomorphically tally the vote */
                    election.castVote(vote);

                    /* Now save the result until we're ready to decrypt the totals */
                    results.put(subElectionId, election);
                }
            } catch (Exception e) {
                Bugout.err("Malformed ballot received <" + e.getMessage() + ">");
                Bugout.err("Rejected ballot:\n" + bal);
            }
        }

        /*
         * Build a list of sums for each race so we can put them in a ListExpression
         * of the form ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]) (public-key [key]))
         */
        ArrayList<ASExpression> votes = new ArrayList<>();

        /* These are the only choices on a ballot that we need for a vote proof */
        ArrayList<AdderInteger> choices = new ArrayList<>();
        choices.add(AdderInteger.ZERO);
        choices.add(AdderInteger.ONE);

        for(String id :  results.keySet()) {
            ArrayList<ASExpression> voteASE = new ArrayList<>();

            Vote vote = results.get(id).sumVotes();
            VoteProof proof = new VoteProof();
            proof.compute(vote, publicKey, choices, 0, cast.size());

            voteASE.add(vote.toASE());
            voteASE.add(new ListExpression(StringExpression.makeString("vote-ids"), StringExpression.makeString(id)));
            voteASE.add(proof.toASE());
            voteASE.add(publicKey.toASE());

            /* Add our newly tallied ciphers, race id's, and */
            votes.add(new ListExpression(voteASE));
        }

        ListExpression voteList = new ListExpression(votes);
        ASExpression nonce = StringExpression.makeString(voteList.getSHA256());


        //return new Ballot(precinctID, votes, nonce);
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
     * @param voteIds a list of strings representing vote identifiers
     * @return a string representation of the list of voteIDs
     */
    private static String makeId(List<String> voteIds){
        String str = voteIds.get(0);
        for(int i = 1; i < voteIds.size(); i++)
            str+=","+voteIds.get(i);

        return str;
    }
}
