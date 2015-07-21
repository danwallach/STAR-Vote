package supervisor.model;

import auditorium.Bugout;
import crypto.AHomomorphicCiphertext;
import crypto.EncryptedRaceSelection;
import crypto.IPublicKey;
import crypto.adder.Race;
import sexpression.ASEConverter;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Precinct class is a data structure to hold encrypted ballots and ballot style and handle manipulation of the ballots.
 *
 * Created by Matthew Kindy II on 6/20/14.
 */
public class Precinct<T extends AHomomorphicCiphertext<T>> implements Serializable {

    /** File path to the ballot style. */
    private String ballotFile;

    /** Three digit precinct code. */
    private final String precinctID;

    /** Map of all the bids to the corresponding ballot. */
    private Map<String,Ballot<EncryptedRaceSelection<T>>> allBallots;

    /** Map of bids to ballots that have been committed but not cast or challenged.*/
    private Map<String, Ballot<EncryptedRaceSelection<T>>> committed;

    /** List of ballots that have been cast, not committed or challenged.*/
    private List<Ballot<EncryptedRaceSelection<T>>> cast;

    /** List of ballots that have been challenged but not cast or committed.*/
    private List<Ballot<EncryptedRaceSelection<T>>> challenged;

    /**
     * @param precinctID    Three digit precinct code
     * @param ballotFile    The zip file containing the ballot style
     */
    public Precinct(String precinctID, String ballotFile){

        this.precinctID = precinctID;
        this.ballotFile = ballotFile;

        allBallots = new HashMap<>();
        committed  = new HashMap<>();
        cast       = new ArrayList<>();
        challenged = new ArrayList<>();
    }

    /**
     * Checks if this precinct own this BID
     *
     * @param bid       Ballot Identification Number
     * @return          True if it has the ballot, False otherwise
     */
    public boolean hasBID(String bid){
        return (allBallots.get(bid) != null);
    }

    /**
     * Gets the nonce of the Ballot with this bid
     *
     * @param bid       Ballot Identification Number
     * @return          Returns the nonce as ASExpressions
     */
    public String getNonce(String bid){
        return allBallots.get(bid).getNonce();
    }

    /**
     * Challenges a Ballot, moving it from committed to challenged
     * and returns the challenged Ballot.
     *
     * @param bid       Ballot Identification Number
     * @return          the ballot that was challenged
     */
    public Ballot<EncryptedRaceSelection<T>> challengeBallot(String bid){

        /* Remove the Ballot from committed */
        Ballot<EncryptedRaceSelection<T>> toChallenge = committed.remove(bid);

        /* Add the Ballot to challenged */
        if(toChallenge != null) challenged.add(toChallenge);

        /* Return the ballot that was challenged */
        return toChallenge;
    }

    /**
     * Commits a new Ballot given the bid, nonce, and ballot contents as an ASExpression
     *
     * @param bid       Ballot Identification Number
     * @param ballot    Ballot as an ASExpression
     */
    public void commitBallot(String bid, Ballot<EncryptedRaceSelection<T>> ballot){

        committed.put(bid, ballot);

        allBallots.put(bid, ballot);
    }

    /**
     * @param bid       Ballot Identification Number
     * @return          true if the BID was a committed ballot and was successfully
     *                  cast, false otherwise
     */
    public Ballot<EncryptedRaceSelection<T>> castBallot(String bid){

        /* Remove the Ballot from committed */
        Ballot<EncryptedRaceSelection<T>> toCast = committed.remove(bid);

        System.out.println(toCast);

        if(toCast!=null)
            cast.add(toCast);

        /* Add it to cast and check */
        return toCast;
    }

    /**
     * Moves all of the non-cast (i.e. committed) ballots into the challenged list.
     */
    public void closePolls() {

        /* Move each ballot in committed to the challenged list */
        challenged.addAll(committed.values());
        committed.clear();
    }

    /**
     * Sums all Ballots in this precinct and returns the summed (encrypted) Ballot
     * @return          a Ballot representing the sum total of all of the votes
     *                  cast in this precinct
     */
    public Ballot<EncryptedRaceSelection<T>> getCastBallotTotal(IPublicKey PEK){

        int size=0;

        /* The results of the election are stored by race ID in this map */
        Map<String, Race<T>> results = new HashMap<>();

        /* For each ballot, get each vote and build a results mapping between race ids and elections */
        for (Ballot<EncryptedRaceSelection<T>> bal : cast) {

            try {

                List<EncryptedRaceSelection<T>> raceSelections = bal.getRaceSelections();

                /* Cycle through each of the races */
                for(EncryptedRaceSelection<T> ers: raceSelections){

                    /* Get all the candidate choices */
                    String raceID = ers.getTitle();

                    /* Confirm that the vote proof is valid */
                    if (!ers.verify(0, 1, PEK)) {
                        Bugout.err("!!!Ballot failed NIZK test!!! " + bal.getSize() + " " + ers.getRaceSelectionsMap());
                        return null;
                    }

                    /* Code these results as a subelection so the ciphers can be summed homomorphically */
                    Race<T> race = results.get(raceID);

                    /* If we haven't seen this specific race before, initialize it */
                    if (race == null)
                        race = new Race<>(PEK, new ArrayList<>(ers.getRaceSelectionsMap().keySet()));

                    /* This will ready race to homomorphically tally the vote */
                    race.castRaceSelection(ers);

                    /* Now save the result until we're ready to decrypt the totals */
                    results.put(raceID, race);
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
        ArrayList<EncryptedRaceSelection<T>> votes = new ArrayList<>();

        /* This will be used to create the nonce eventually */
        ArrayList<ASExpression> voteASE = new ArrayList<>();

        /* Now go through each race */
        for(String id :  results.keySet()) {

            /* Get the race */
            Race<T> thisRace = results.get(id);

            /* Get the homomorphically tallied vote for this race */
            EncryptedRaceSelection<T> vote = thisRace.sumRaceSelections();


            /* Verify the voteProof and error off if bad */
            if(vote.verify(0, thisRace.getRaceSelections().size(), PEK)) {
                votes.add(vote);
                voteASE.add(ASEConverter.convertToASE(vote));
            }
            else System.err.println("There was a bad summed vote that was not added to the ballot!");

        }

        /* Create the nonce */
        ListExpression voteList = new ListExpression(voteASE);
        String nonce = StringExpression.makeString(voteList.getSHA256()).toString();

        /* Return the Ballot of all the summed race results */
        return new Ballot<>(precinctID, votes, nonce, size);
    }

    /**
     * Constructs and returns a new ListExpression of each of the challenged ballots
     * as ListExpressions.
     *
     * @return          the list of challenged Ballots as a ListExpression of
     *                  ListExpressions
     */
    public List<Ballot<EncryptedRaceSelection<T>>> getChallengedBallots(){

        List<Ballot<EncryptedRaceSelection<T>>> ballotList = new ArrayList<>();

        /* Add each challenged ballot to the List */
        ballotList.addAll(challenged.stream().collect(Collectors.toList()));

        /* Construct a ListExpression from the List and return */
        return ballotList;
    }

    public String getPrecinctID(){
        return precinctID;
    }

    public String getBallotFile(){
        return ballotFile;
    }

    public Ballot<EncryptedRaceSelection<T>> getChallengedBallot(String bid) {

        if(allBallots.containsKey(bid) && challenged.contains(allBallots.get(bid)))
            return allBallots.get(bid);

        /* If it's either not a challenged ballot or doesn't exist */
        else return null;
    }

}
