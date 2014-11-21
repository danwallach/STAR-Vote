package supervisor.model;

import crypto.AVote;
import crypto.adder.AdderVote;
import crypto.adder.ElgamalCiphertext;
import crypto.adder.PublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Document of voter intent, used by the supervisor to manipulate ballots after they've been committed until the
 * election ends and the results are tallied and uploaded. These object will be explicitly handled by Precincts.
 *
 * @author Matt Bernhard
 */
public class Ballot<T extends AVote> implements Serializable {

    /** The identifier for this ballot */
    private final String bid;

    /** A representation of the ballot (document of voter intent) as an ASExpression
     *
     *                |------------ This is a Vote as an ASExpression -------------|
     * (ballot bid (  ((vote [vote]) (vote-ids ([id1], [id2], ...)) (vote-proof [proof]))
     *                ((vote [vote]) (vote-ids ([id1], [id2], ...)) (vote-proof [proof]))
     *                ((vote [vote]) (vote-ids ([id1], [id2], ...)) (vote-proof [proof]))...  ) [nonce] [size])
     *
     * */
    private final List<T> ballot;

    /** The nonce associated with the voting session when this ballot was committed */
    private final String nonce;

    private final Integer size;


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
    public List<T> getVotes() {
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

    /**
     * @return a ListExpression representation of the ballot
     */
    public ListExpression getVoteASE(){
        ArrayList<ASExpression> votes = new ArrayList<>();

        for(T v : ballot)
            votes.add(v.toASE());

        return new ListExpression(votes);
    }

    /**
     * @return the ballot serialized as a ListExpression
     */
    public ListExpression toListExpression(){

        /* Add all of the elements to a list */
        ArrayList<ASExpression> elements = new ArrayList<>();

        elements.add(StringExpression.makeString("ballot"));
        elements.add(StringExpression.makeString(bid));
        elements.add(getVoteASE());
        elements.add(StringExpression.makeString(nonce));
        elements.add(StringExpression.makeString(size.toString()));

        /* Build a list expression based on the data here contained */
        return new ListExpression(elements);
    }

}
