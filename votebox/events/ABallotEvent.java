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
    private String bid;

    /** The ballot style (i.e. precinct) */
    private String precinct;


    public ABallotEvent(int serial, byte[] ballot, ASExpression nonce) {
        super(serial);
        this.ballot = ballot;
        this.nonce = nonce;
    }

    public ABallotEvent(int serial, String bID, ASExpression nonce){
        super(serial);
        this.bid = bID;
        this.nonce = nonce;
    }

    public ABallotEvent(int serial, byte[] ballot, ASExpression nonce, String precinct){
        this(serial, ballot, nonce);

        this.precinct = precinct;
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
    public String getBID() { return bid; }

    /** Return the precinct (and therefore ballot style */
    public String getPrecinct() { return precinct; }


}
