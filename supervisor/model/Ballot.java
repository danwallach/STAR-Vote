package supervisor.model;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.util.ArrayList;

/**
 * Document of voter intent, used by the supervisor to manipulate ballots after they've been committed until the
 * election ends and the results are tallied and uploaded. These object will be explicitly handled by Precincts.
 *
 * @author Matt Bernhard
 */
public class Ballot {

    /** The identifier for this ballot */
    private final String bid;

    /** A representation of the ballot (document of voter intent) as an ASExpression
     *
     *                |------------ This is a Vote as an ASExpression -------------| (missing the public-key)
     * (ballot bid (  ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]))
     *                ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]))
     *                ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]))...  ) (public-key [key]) nonce)
     *
     * */
    private final ASExpression ballot;

    /** The nonce associated with the voting session when this ballot was committed */
    private final ASExpression nonce;

    /**
     * Constructor for a ballot, takes in all of the parameters the supervisor receives on committing a ballot.
     *
     * @param bid               the ballot identifier
     * @param ballot            the record of voter intent
     * @param nonce             the nonce associated with the Votebox voting session
     */
    public Ballot(String bid, ASExpression ballot, ASExpression nonce){
        this.bid = bid;
        this.ballot = ballot;
        this.nonce = nonce;
    }

    /**
     * @return the ballot identifier
     */
    public String getBid() {
        return bid;
    }

    /**
     * @return the raw ballot data
     */
    public ASExpression getBallot() {
        return ballot;
    }

    /**
     * @return the nonce associated with this ballot's voting session
     */
    public ASExpression getNonce() {
        return nonce;
    }

    /**
     * @return the ballot serialized as a ListExpression
     */
    public ListExpression toListExpression(){

        /* Add all of the elements to a list */
        ArrayList<ASExpression> elements = new ArrayList<>();

        elements.add(StringExpression.makeString(bid));
        elements.add(ballot);
        elements.add(nonce);

        /* Build a list expression based on the data here contained */
        return new ListExpression(elements);
    }

    /**
     * @param precinctID the identifier for the precinct this ballot belongs to
     * @return the ballot serialized as a ListExpression
     */
    public ListExpression toListExpression(String precinctID){

        /* Add all of the elements to a list */
        ArrayList<ASExpression> elements = new ArrayList<>();

        elements.add(StringExpression.makeString(precinctID));
        elements.add(StringExpression.makeString(bid));
        elements.add(ballot);
        elements.add(nonce);

        /* Build a list expression based on the data here contained */
        return new ListExpression(elements);
    }

}
