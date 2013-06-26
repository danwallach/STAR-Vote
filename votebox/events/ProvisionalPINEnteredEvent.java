package votebox.events;

import sexpression.*;

import java.math.BigInteger;

/**
 * @author Matt Bernhard
 * 6/26/13
 *
 * An event which allows a provisional voter to vote in a "special" votebox session
 */
public class ProvisionalPINEnteredEvent extends PINEnteredEvent {

    private int serial;

    private String pin;

    private byte[] nonce;

    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("provisional-pin-entered"), StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                String pin = ((ListExpression) res).get(0).toString();
                byte[] nonce = new BigInteger(((ListExpression) res)
                        .get( 1 ).toString()).toByteArray();
                return new ProvisionalPINEnteredEvent( serial, pin, nonce );
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

    public String getPin() {
        return pin;
    }

    public ProvisionalPINEnteredEvent(int serial, String pin, byte[] nonce) {
        super(serial, pin, nonce);
        this.serial = serial;
        this.pin = pin;
        this.nonce = nonce;
    }

    public void fire(VoteBoxEventListener l) {
        l.provisionalPinEntered(this);
    }

    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("provisional-pin-entered"),
                StringExpression.makeString( pin ),
                StringExpression.makeString( new BigInteger(nonce).toString()));
    }
}
