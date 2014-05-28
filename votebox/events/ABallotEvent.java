package votebox.events;

import sexpression.ASExpression;

/**
 * An abstract method pertaining to a ballot. It may or may not have all of its fields filled in as necessary,
 * as was largely created as a convenience to cut down on javadoc'ing and general redundancy.
 *
 * @author Matt Bernhard
 */
public abstract class ABallotEvent extends AAnnounceEvent{


    /** The serialized ballot that the authorized machine will used to vote on */
    private byte[] ballot;


    /** A nonce associated with the authorized session */
    private ASExpression nonce;

    /** The ballot ID */
    private String bID;


    public ABallotEvent(byte[] ballot, ASExpression nonce) {
        this.ballot = ballot;
        this.nonce = nonce;
    }

    public ABallotEvent(String bID, ASExpression nonce){
        this.bID = bID;
        this.nonce = nonce;
    }

    /**
     * @return the ballot
     */
    public byte[] getBallot() {
        return ballot;
    }

    /**
     * @return the nonce, or authorization code
     */
    public ASExpression getNonce() {
        return nonce;
    }

    /** @return the ballot ID */
    public String getBID() {
        return bID;
    }


}
