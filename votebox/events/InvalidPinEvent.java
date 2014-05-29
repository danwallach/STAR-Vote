package votebox.events;

import sexpression.*;

/**
 * Event that says that a pin validation request was invalid
 */
public class InvalidPinEvent extends AAnnounceEvent{


    private int targetSerial;

    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("invalid-pin"), StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                int targetSerial = Integer.parseInt(((ListExpression) res).get(0).toString());

                return new InvalidPinEvent( serial, targetSerial);
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

    public InvalidPinEvent(int serial, int node) {
        super(serial);
        this.targetSerial = node;
    }

    /**
     * @return the serial of the machine who entered an invalid PIN
     */
    public int getTargetSerial(){
        return targetSerial;
    }
    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.invalidPin(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("invalid-pin"),
                StringExpression.make("" + targetSerial));
    }

}