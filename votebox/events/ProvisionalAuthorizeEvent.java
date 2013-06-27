package votebox.events;

import sexpression.*;

import java.math.BigInteger;


/**
 * @author Matt Bernhard
 * 6/26/13
 *
 * An event which sends a ballot to a votebox in a provisional voting session
 */
public class ProvisionalAuthorizeEvent implements IAnnounceEvent {

    private int serial;

    private int node;

    private byte[] nonce;

    private byte[] ballot;

    /**
     * The matcher for the ProvisionalAuthorizeEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("provisional-authorized-to-cast"), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                int node = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );
                /*byte[] nonce = ((StringExpression) ((ListExpression) res)
                        .get( 1 )).getBytesCopy();*/
                byte[] nonce = new BigInteger(((ListExpression) res)
                        .get( 1 ).toString()).toByteArray();
                byte[] ballot = ((StringExpression) ((ListExpression) res)
                        .get( 2 )).getBytesCopy();
                return new ProvisionalAuthorizeEvent( serial, node, nonce, ballot );
            }
            return null;
        };
    };

    /**
     *
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }//getMatcher

    /**
     * Constructs a new ProvisionalAuthorizeEvent.
     *
     * @param serial
     *            the serial number of the sender
     * @param node
     *            the node id
     * @param nonce
     *            the nonce (or authorization code), an array of bytes
     * @param ballot
     *            the ballot in zip format, stored as an array of bytes
     */
    public ProvisionalAuthorizeEvent(int serial, int node, byte[] nonce,
                                 byte[] ballot) {
        this.serial = serial;
        this.node = node;
        this.nonce = nonce;
        this.ballot = ballot;
    }

    /**
     * @return the ballot
     */
    public byte[] getBallot() {
        return ballot;
    }

    /**
     * @return the node
     */
    public int getNode() {
        return node;
    }

    /**
     * @return the nonce, or authorization code
     */
    public byte[] getNonce() {
        return nonce;
    }

    public int getSerial() {
        return serial;
    }

    public void fire(VoteBoxEventListener l) {
        l.provisionalAuthorizedToCast( this );
    }

    public ASExpression toSExp() {
        /*return new ListExpression( StringExpression
                .makeString( "authorized-to-cast" ), StringExpression
                .makeString( Integer.toString( node ) ), StringExpression
                .makeString( nonce ), StringExpression.makeString( ballot ) );*/
        return new ListExpression( StringExpression
                .makeString( "provisional-authorized-to-cast" ), StringExpression
                .makeString( Integer.toString( node ) ), StringExpression
                .makeString( new BigInteger(nonce).toString() ), StringExpression.makeString( ballot ) );
    }

}
