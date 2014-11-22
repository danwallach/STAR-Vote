package supervisor.model;

import crypto.EncryptedVote;
import crypto.adder.PublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;

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
public class Precinct implements Serializable {

    /** File path to the ballot style. */
    private String ballotFile;

    /** Three digit precinct code. */
    private final String precinctID;

    /** Map of all the bids to the corresponding ballot. */
    private Map<String,Ballot<EncryptedVote>> allBallots;

    /** Map of bids to ballots that have been committed but not cast or challenged.*/
    private Map<String, Ballot<EncryptedVote>> committed;

    /** List of ballots that have been cast, not committed or challenged.*/
    private List<Ballot<EncryptedVote>> cast;

    /** List of ballots that have been challenged but not cast or committed.*/
    private List<Ballot<EncryptedVote>> challenged;

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
    public Ballot<EncryptedVote> challengeBallot(String bid){

        /* Remove the Ballot from committed */
        Ballot toChallenge = committed.remove(bid);

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
    public void commitBallot(String bid, Ballot<EncryptedVote> ballot){

        committed.put(bid, ballot);

        allBallots.put(bid, ballot);
    }

    /**
     * @param bid       Ballot Identification Number
     * @return          true if the BID was a committed ballot and was successfully
     *                  cast, false otherwise
     */
    public Ballot castBallot(String bid){

        /* Remove the Ballot from committed */
        Ballot toCast = committed.remove(bid);

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

        /* Challenge each committed Ballot */
        committed.keySet().forEach(this::challengeBallot);
    }

    /**
     *
     * @return          a Ballot representing the sum total of all of the votes
     *                  cast in this precinct
     */
    public Ballot getCastBallotTotal(){

        return SupervisorTallier.tally(precinctID, cast);
    }

    /**
     * Constructs and returns a new ListExpression of each of the challenged ballots
     * as ListExpressions.
     *
     * @return          the list of challenged Ballots as a ListExpression of
     *                  ListExpressions
     */
    public List<Ballot<EncryptedVote>> getChallengedBallots(){

        List<Ballot<EncryptedVote>> ballotList = new ArrayList<>();

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

    public Ballot<EncryptedVote> getChallengedBallot(String bid) {

        if(allBallots.containsKey(bid) && challenged.contains(allBallots.get(bid)))
            return allBallots.get(bid);

        /* If it's either not a challenged ballot or doesn't exist */
        else return null;
    }

}
