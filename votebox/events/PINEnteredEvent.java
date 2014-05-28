package votebox.events;

import sexpression.*;

import java.math.BigInteger;

/**
 * Event class for when a pin is entered on a VoteBox. Entered Pin is included in event
 */
public class PINEnteredEvent extends AAnnounceEvent {

    private int serial;

    private String pin;

    private byte[] nonce;


    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "pin-entered" ), StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                String pin =((ListExpression) res).get(0).toString();
                byte[] nonce = new BigInteger(((ListExpression) res)
                        .get( 1 ).toString()).toByteArray();
                return new PINEnteredEvent( serial, pin, nonce );
            }

            return null;
        }
    };

    public int getSerial() {
        return serial;
    }

    /**
     * @return the nonce
     */
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

    /**
     * @return the pin entered at the console
     */
    public String getPin() {
        return pin;
    }

    public PINEnteredEvent(int serial, String pin, byte[] nonce) {
        this.serial = serial;
        this.pin = pin;
        this.nonce = nonce;
    }

    public void fire(VoteBoxEventListener l) {
        l.pinEntered(this);
    }

    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("pin-entered"),
                StringExpression.makeString( pin) ,
                StringExpression.makeString( new BigInteger(nonce).toString()));
    }

}