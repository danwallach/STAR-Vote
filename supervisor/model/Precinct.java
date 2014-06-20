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
    /** List of ballots that have been committed but not cast or challenged.*/
    private List<Ballot> committed ;

    /** List of ballots that have been cast, not committed or challenged.*/
    private List<Ballot> cast ;

    /** List of ballots that have been challenged but not cast or committed.*/
    private List<Ballot> challenged ;

    /** File path to the ballot style. */
    private String ballotFile;

    /** Three digit precinct code. */
    private final String precinctID;

    /** An object used to homomorphically tally ballots. */
    private ITallier tallier;

    /** Map of all the bids to the corresponding ballot. */
    private Map<String,Ballot> allBallots;


    /**
     * @param precinctID    Three digit precinct code
     * @param ballotFile    The zip file containing the ballot style
     */
    public Precinct(String precinctID,String ballotFile, PublicKey publicKey, PrivateKey privateKey){

        this.precinctID = precinctID;
        this.ballotFile = ballotFile;
        committed = new ArrayList<>();
        cast = new ArrayList<>();
        challenged = new ArrayList<>();
        tallier = new ChallengeDelayedWithNIZKsTallier(publicKey,privateKey);
        allBallots =new HashMap<>();
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
        allBallots.get(bid).getNonce();
    }

    /**
     * @param bid       Ballot Identification Number
     * @return
     */
    public Ballot spoilBallot(String bid){ }

    /**
     * @param bid       Ballot Identification Number
     * @param nonce     Nonce for the ballot
     * @param ballot    Ballot as an ASExpression
     */
    public void commitBallot(String bid, ASExpression nonce, ASExpression ballot){
    }

    /**
     * @param bid       Ballot Identification Number
     * @return
     */
    public boolean castBallot(String bid){
    }

    /**
     * @param bid       Ballot Identification Number
     * @return
     */
    public boolean challengeBallot(String bid){}

    public Ballot getCastBallotTotal(){}

    public ListExpression getChallengedBallots(){}

    public String getPrecinctID() {
        return precinctID;
    }


}
