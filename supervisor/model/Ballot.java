package supervisor.model;

import crypto.ARaceSelection;
import sexpression.ASEParser;
import sexpression.ASExpression;
import sexpression.ListExpression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Document of voter intent, used by the supervisor to manipulate ballots after they've been committed until the
 * election ends and the results are tallied and uploaded. These object will be explicitly handled by Precincts.
 *
 * @author Matt Bernhard
 */
public class Ballot<T extends ARaceSelection> implements Serializable {

    /** The identifier for this ballot */
    private final String bid;

    /** A representation of the ballot (document of voter intent) as an ASExpression */
    private final List<T> ballot;

    /** The nonce associated with the voting session when this ballot was committed */
    private final String nonce;

    /** The number of accumulated ballots held in this one (after summation) */
    private final int size;

    /**
     * Constructor for a ballot, takes in all of the parameters the supervisor receives on committing a ballot.
     *
     * @param bid               the ballot identifier
     * @param ballot            the record of voter intent
     * @param nonce             the nonce associated with the Votebox voting session
     */
    public Ballot(String bid, List<T> ballot, String nonce){
        this(bid, ballot, nonce, 1);
    }

    public Ballot(String bid, List<T> ballot, String nonce, Integer size) {
        this.bid = bid;
        this.ballot = ballot;
        this.nonce = nonce;
        this.size = size;
    }

    /**
     * @return  the ballot identifier
     */
    public String getBid() {
        return bid;
    }

    /**
     * @return  the ballot as an array of votes
     */
    public List<T> getRaceSelections() {
        return ballot;
    }

    /**
     * @return  the nonce associated with this ballot's voting session
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * @return  the number of ballots tallied into this ballot (default: 1)
     */
    public Integer getSize() { return size; }


}
