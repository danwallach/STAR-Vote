package votebox.events;

import sexpression.*;

/**
 * Created with IntelliJ IDEA.
 * User: matt
 * Date: 6/19/13
 * Time: 3:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class StartScannerEvent implements IAnnounceEvent{

    /**
     * Matcher for the ballotscanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("start-scanner"));

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                return new StartScannerEvent(serial);
            }
            return null;
        }
    };

    private int serial;


    public StartScannerEvent(int serial) {
        this.serial = serial;
    }

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }//getMatcher

    public int getSerial() {
        return serial;
    }

    public void fire(VoteBoxEventListener l) {
        l.scannerstart(this);
    }

    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("start-scanner"));
    }
}
