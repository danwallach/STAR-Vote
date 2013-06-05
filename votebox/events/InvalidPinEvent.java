package votebox.events;

import sexpression.*;

import java.math.BigInteger;

/**
 * Event that says that a pin validation request was invalid
 */
public class InvalidPinEvent implements IAnnounceEvent{

    private int serial;

    private byte[] nonce;

    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("invalid-pin"), StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                byte[] nonce = new BigInteger(((StringExpression) ((ListExpression) res)
                        .get( 0 )).toString()).toByteArray();
                return new InvalidPinEvent( serial, nonce );
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

    public InvalidPinEvent(int serial, byte[] nonce) {
        this.serial = serial;
        this.nonce = nonce;
    }

    public void fire(VoteBoxEventListener l) {
        l.invalidPin(this);
    }

    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("invalid-pin"),
                StringExpression.makeString( new BigInteger(nonce).toString()));
    }

}