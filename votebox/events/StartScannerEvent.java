package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NoMatch;
import sexpression.StringExpression;

/**
 * @author Matt Bernhard
 * 6/19/13
 */
public class StartScannerEvent extends AAnnounceEvent{

    /**
     * Matcher for the ballotScanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("start-scanner"));

        public IAnnounceEvent match(int serial, ASExpression sexp){
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                return new StartScannerEvent(serial);
            }
            return null;
        }
    };



    public StartScannerEvent(int serial) {
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
        l.scannerStart(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("start-scanner"));
    }
}
