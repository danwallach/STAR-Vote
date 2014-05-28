package votebox.events;

import sexpression.*;

import java.math.BigInteger;

/**
 * Event class for when a pin is entered on a VoteBox. Entered Pin is included in event
 */
public class PINEnteredEvent extends AAnnounceEvent {

    /** The PIN that was entered */
    private String pin;

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

                return new PINEnteredEvent( serial, pin );
            }

            return null;
        }
    };

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }

    /**
     * Constructor.
     *
     * @param serial the sender's serial number
     * @param pin the PIN that was entered
     */
    public PINEnteredEvent(int serial, String pin) {
        super(serial);
        this.pin = pin;
    }

    /**
     * @return the pin entered at the console
     */
    public String getPin() {
        return pin;
    }

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.pinEntered(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("pin-entered"),
                StringExpression.makeString( pin));
    }

}