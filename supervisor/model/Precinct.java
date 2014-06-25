package supervisor.model;

import crypto.adder.PublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Precinct class is a data structure to hold encrypted ballots and ballot style and handle manipulation of the ballots.
 *
 * Created by Matthew Kindy II on 6/20/14.
 */
public class Precinct {

    /** File path to the ballot style. */
    private String ballotFile;

    /** Three digit precinct code. */
    private final String precinctID;

    /** Map of all the bids to the corresponding ballot. */
    private Map<String,Ballot> allBallots;

    /** Map of bids to ballots that have been committed but not cast or challenged.*/
    private Map<String, Ballot> committed;

    /** List of ballots that have been cast, not committed or challenged.*/
    private List<Ballot> cast;

    /** List of ballots that have been challenged but not cast or committed.*/
    private List<Ballot> challenged;

    /** An object used to homomorphically tally ballots. */
    private PublicKey publicKey;

    /**
     * @param precinctID    Three digit precinct code
     * @param ballotFile    The zip file containing the ballot style
     */
    public Precinct(String precinctID, String ballotFile, PublicKey publicKey){

        this.precinctID = precinctID;
        this.ballotFile = ballotFile;
        this.publicKey = publicKey;

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
    public ASExpression getNonce(String bid){
        return allBallots.get(bid).getNonce();
    }

    /**
     * Challenges a Ballot, moving it from committed to challenged
     * and returns the challenged Ballot.
     *
     * @param bid       Ballot Identification Number
     * @return          the ballot that was challenged
     */
    public Ballot challengeBallot(String bid){

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
    public void commitBallot(String bid, ASExpression ballot){
        committed.put(bid, Ballot.fromASE(ballot));
    }

    /**
     * @param bid       Ballot Identification Number
     * @return          true if the BID was a committed ballot and was successfully
     *                  cast, false otherwise
     */
    public boolean castBallot(String bid){

        /* Remove the Ballot from committed */
        Ballot toCast = committed.remove(bid);

        /* Add it to cast and check */
        return toCast != null && cast.add(toCast);
    }

    /**
     * Moves all of the non-cast (i.e. committed) ballots into the challenged list.
     */
    public void closePolls() {

        /* Challenge each committed Ballot */
        for(String bid : committed.keySet())
            challengeBallot(bid);
    }

    /**
     * TODO: Homomorphically tallies all the cast Ballots into one "master" Ballot
     *
     * @return          a Ballot representing the sum total of all of the votes
     *                  cast in this precinct
     */
    public Ballot getCastBallotTotal(){
        return cast.get(0);
    }

    /**
     * Constructs and returns a new ListExpression of each of the challenged ballots
     * as ListExpressions.
     *
     * @return          the list of challenged Ballots as a ListExpression of
     *                  ListExpressions
     */
    public ListExpression getChallengedBallots(){

        List<ASExpression> ballotList = new ArrayList<>();

        /* Add each challenged ballot to the List of ListExpressions */
        for(Ballot b : challenged)
            ballotList.add(b.toListExpression());

        /* Construct a ListExpression from the List and return */
        return new ListExpression(ballotList);
    }

    public String getPrecinctID(){
        return precinctID;
    }

    public String getBallotFile(){
        return ballotFile;
    }

    public Ballot getBallot(String bid) {
        return allBallots.get(bid);
    }
}
