package supervisor.model;

import crypto.ARaceSelection;
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
public class Ballot<T extends ARaceSelection> implements Serializable {

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

    /**
     * @return a ListExpression representation of the ballot
     */
    public ListExpression getRaceSelectionsASE(){
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
        elements.add(getRaceSelectionsASE());
        elements.add(StringExpression.makeString(nonce));
        elements.add(StringExpression.makeString(Integer.toString(size)));

        /* Build a list expression based on the data here contained */
        return new ListExpression(elements);
    }

        /**
         * Method for interop with VoteBox's S-Expression system.
         *
         * @param ase       S-Expression representation of a ballot
         * @return          the Vote equivalent of ase
         *
         */
        public static <A extends ARaceSelection> Ballot<A> fromASE(ASExpression ase){

            ListExpression exp = (ListExpression)ase;

            System.out.println(exp.get(0));

            if(!(exp.get(0)).toString().equals("ballot"))
                throw new RuntimeException("Not ballot");

            String bid = exp.get(1).toString();

            ListExpression vListE = (ListExpression)exp.get(2);

            ArrayList<A> aList = new ArrayList<>();

            for(ASExpression vote : vListE)
                aList.add(A.fromASE(vote));

            StringExpression nonce = (StringExpression)exp.get(3);

            Integer size = Integer.parseInt(exp.get(4).toString());

            return new Ballot<>(bid, aList, nonce.toString(), size);
        }

}
