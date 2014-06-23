package supervisor.model;

import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;
import supervisor.model.tallier.ChallengeDelayedWithNIZKsTallier;
import supervisor.model.tallier.ITallier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Precinct class is a data structure to hold encrypted ballots , ballot style and handle manipulation of the ballots.
 *
 * Created by arghyac on 6/20/14.
 */
public class Precinct {

    /** File path to the ballot style. */
    private String ballotFile;

    /** Three digit precinct code. */
    private final String precinctID;

    /** Map of all the bids to the corresponding ballot. */
    private Map<String,Ballot> allBallots;

    /** Map of bids to ballots that have been committed but not cast or spoiled.*/
    private Map<String, Ballot> committed;

    /** List of ballots that have been cast, not committed or spoiled.*/
    private List<Ballot> cast;

    /** List of ballots that have been spoiled but not cast or committed.*/
    private List<Ballot> spoiled;

    /** An object used to homomorphically tally ballots. */
    private ITallier tallier;

    /**
     * @param precinctID    Three digit precinct code
     * @param ballotFile    The zip file containing the ballot style
     */
    public Precinct(String precinctID, String ballotFile, PublicKey publicKey, PrivateKey privateKey){

        this.precinctID = precinctID;
        this.ballotFile = ballotFile;

        allBallots = new HashMap<>();
        committed  = new HashMap<>();
        cast       = new ArrayList<>();
        spoiled    = new ArrayList<>();

        tallier = new ChallengeDelayedWithNIZKsTallier(publicKey, privateKey);
    }

    /**
     * @param bid       Ballot Identification Number
     * @return          True if it has the ballot, False otherwise
     */
    public boolean hasBID(String bid){
        return (allBallots.get(bid) != null);
    }

    /**
     * @param bid       Ballot Identification Number
     * @return          Returns the nonce as ASExpressions
     */
    public ASExpression getNonce(String bid){
        return allBallots.get(bid).getNonce();
    }

    /**
     * @param bid       Ballot Identification Number
     * @return
     */
    public Ballot spoilBallot(String bid){

        Ballot toSpoil = committed.remove(bid);

        if(toSpoil != null) spoiled.add(toSpoil);

        return toSpoil;
    }

    /**
     * @param bid       Ballot Identification Number
     * @param nonce     Nonce for the ballot
     * @param ballot    Ballot as an ASExpression
     */
    public void commitBallot(String bid, ASExpression nonce, ASExpression ballot){
        committed.put(bid, new Ballot(bid, ballot, nonce));
    }

    /**
     * @param bid       Ballot Identification Number
     * @return          true if the BID was a committed ballot and was successfully
     *                  cast, false otherwise
     */
    public boolean castBallot(String bid){

        Ballot toCast = committed.remove(bid);

        return toCast != null && cast.add(toCast);
    }

    /**
     *
     * @return          a Ballot representing the sum total of all of the votes
     *                  cast in this precinct
     */
    public Ballot getCastBallotTotal(){
        return cast.get(0);
    }

    /**
     *
     * @return          the list of spoiled Ballots as a ListExpression of
     *                  ListExpressions
     */
    public ListExpression getSpoiledBallots(){

        List<ASExpression> ballotList = new ArrayList<>();

        /* Add each spoiled ballot to the List of ListExpressions */
        for(Ballot b : spoiled)
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
