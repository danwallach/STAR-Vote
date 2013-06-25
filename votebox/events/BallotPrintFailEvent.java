package votebox.events;

import sexpression.*;

import java.math.BigInteger;

/**
 * @author Matt Bernhard
 * 6/17/13
 *
 * An event which represents that printing did not occur successfully
 */
public class BallotPrintFailEvent implements IAnnounceEvent {

    private int serial;

    private String bID;

    private byte[] nonce;

    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("ballot-print-fail"), StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                String bID = ((ListExpression) res).get(0).toString();
                byte[] nonce = new BigInteger(((ListExpression) res)
                        .get( 1 ).toString()).toByteArray();
                return new BallotPrintFailEvent( serial, bID, nonce );
            }

            return null;
        }
    };

    public int getSerial() {
        return serial;
    }

    public byte[] getNonce() {
        return nonce;
    }

    /**
     *
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }

    public String getBID() {
        return bID;
    }

    public BallotPrintFailEvent(int serial, String bID, byte[] nonce) {
        this.serial = serial;
        this.bID = bID;
        this.nonce = nonce;
    }

    public void fire(VoteBoxEventListener l) {
        l.ballotPrintFail(this);
    }

    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("ballot-print-fail"),
                StringExpression.makeString( bID ),
                StringExpression.makeString( new BigInteger(nonce).toString()));
    }

}
