package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NoMatch;
import sexpression.StringExpression;

/**
 * An event by which Tap announces its status
 * @author Matt Bernhard
 */
public class TapMachineEvent extends AAnnounceEvent {

    /**
     * Matcher for the ballotScanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("tap-machine"));

        public IAnnounceEvent match(int serial, ASExpression sexp){
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                return new TapMachineEvent(serial);
            }

            return null;
        }


    };

    public TapMachineEvent(int serial){
        super(serial);
    }

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }//getMatcher

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.tapMachine(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression(
                StringExpression.makeString("tap-machine")
        );
    }
}

