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

    public ABallotEvent(int serial, String bid, ASExpression nonce){
        super(serial);
        this.bid = bid;
        this.nonce = nonce;
    }

    public ABallotEvent(int serial, ASExpression nonce, byte[] ballot, String precinct){
        this(serial, ballot, nonce);

        this.precinct = precinct;
    }

    public ABallotEvent(int serial, ASExpression nonce, String bid, byte[] ballot){
        this(serial, ballot, nonce);

        this.bid = bid;
    }

    public ABallotEvent(int serial, ASExpression nonce, String bid, String precinct){
        this(serial, bid, nonce);

        this.precinct = precinct;
    }

    public ABallotEvent(int serial, String bid) {
        super(serial);
        this.bid = bid;
    }

    public ABallotEvent(int serial, ASExpression nonce) {
        super(serial);
        this.nonce = nonce;
    }

    public ABallotEvent(int serial, ASExpression nonce, byte[] ballot, String bid, String precinct) {
        this(serial, nonce, bid, precinct);

        this.ballot = ballot;
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
