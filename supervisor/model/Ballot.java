package supervisor.model;

import crypto.adder.ElgamalCiphertext;
import crypto.adder.PublicKey;
import crypto.adder.Vote;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.util.ArrayList;
import java.util.List;

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
     *                |------------ This is a Vote as an ASExpression -------------|
     * (ballot bid (  ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]))
     *                ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]))
     *                ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]))...  ) (public-key [key]) nonce)
     *
     * */
    private final List<Vote> ballot;

    /** The nonce associated with the voting session when this ballot was committed */
    private final ASExpression nonce;

    /** public key used with the encryption of the ballot */
    private PublicKey publicKey;


    /**
     * Constructor for a ballot, takes in all of the parameters the supervisor receives on committing a ballot.
     *
     * @param bid               the ballot identifier
     * @param ballot            the record of voter intent
     * @param nonce             the nonce associated with the Votebox voting session
     */
    public Ballot(String bid, List<Vote> ballot, ASExpression nonce, PublicKey publicKey){
        this.bid = bid;
        this.ballot = ballot;
        this.nonce = nonce;
        this.publicKey = publicKey;
    }

    /**
     * @return the ballot identifier
     */
    public String getBid() {
        return bid;
    }

    /**
     * @return the ballot as an array of votes
     */
    public List<Vote> getVotes() {
        return ballot;
    }

    /**
     * @return the nonce associated with this ballot's voting session
     */
    public ASExpression getNonce() {
        return nonce;
    }


    /**
     * @return return the public key that was used to encrypt this ballot
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * @return a ListExpression representation of the ballot
     */
    public ListExpression getBallotASE(){
        ArrayList<ASExpression> votes = new ArrayList<>();

        for(Vote v : ballot)
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
        elements.add(getBallotASE());
        elements.add(nonce);
        elements.add(publicKey.toASE());

        /* Build a list expression based on the data here contained */
        return new ListExpression(elements);
    }

    /**
     * Method for interop with VoteBox's S-Expression system.
     *
     * @param ase       S-Expression representation of a ballot
     * @return          the Vote equivalent of ase
     *
     * @see ElgamalCiphertext#fromASE(sexpression.ASExpression)
     */
    public static Ballot fromASE(ASExpression ase){

        ListExpression exp = (ListExpression)ase;

        System.out.println(exp.get(0));

        if(!(exp.get(0)).toString().equals("ballot"))
            throw new RuntimeException("Not ballot");

        String bid = exp.get(1).toString();

        ListExpression vListE = (ListExpression)exp.get(2);

        ArrayList<Vote> vList = new ArrayList<>();

        for(ASExpression vote : vListE)
            vList.add(Vote.fromASE(vote));


        StringExpression nonce = (StringExpression)exp.get(3);

        PublicKey key = PublicKey.fromASE(exp.get(4));

        return new Ballot(bid, vList, nonce, key);
    }
}
