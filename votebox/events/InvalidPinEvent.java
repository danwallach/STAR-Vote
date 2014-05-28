package votebox.events;

import sexpression.*;

import java.math.BigInteger;

/**
 * Event that says that a pin validation request was invalid
 */
public class InvalidPinEvent extends AAnnounceEvent{

    private int serial;

    private byte[] nonce;

    private int node;

    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("invalid-pin"), StringWildcard.SINGLETON, StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                byte[] nonce = new BigInteger(((ListExpression) res)
                        .get( 1 ).toString()).toByteArray();
                int node = Integer.parseInt(((ListExpression) res).get(0).toString());

                return new InvalidPinEvent( serial, node, nonce );
            }

            return null;
        }
    };

    public int getSerial() {
        return serial;
    }

    /**
     * @return nonce associated with this pin request
     */
    public byte[] getNonce() {
        return nonce;
    }

    public int getNode(){
        return node;
    }

    /**
     *
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }

    public InvalidPinEvent(int serial, int node,  byte[] nonce) {
        this.serial = serial;
        this.nonce = nonce;
        this.node = node;
    }

    public void fire(VoteBoxEventListener l) {
        l.invalidPin(this);
    }

    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("invalid-pin"),
                StringExpression.make("" + node),
                StringExpression.makeString( new BigInteger(nonce).toString()));
    }

}