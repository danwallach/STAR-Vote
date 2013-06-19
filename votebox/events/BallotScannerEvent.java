package votebox.events;

import sexpression.*;

/**
 * Event that represents the ballotscanner message
 *
 * @author aroe
 */
public class BallotScannerEvent implements IAnnounceEvent {

    /**
     * Matcher for the ballotscanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("ballotscanner"), StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                String status = ((ListExpression) res).get(0).toString();
                return new BallotScannerEvent(serial, status);
            }

            return null;
        }


    };
    private int serial;
    private String status;

    public BallotScannerEvent(int serial, String status) {
        this.serial = serial;
        this.status = status;
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

    /**
     * @return the status, either "active" or "inactive"
     */
    public String getStatus() {
        return status;
    }

    public void fire(VoteBoxEventListener l) {
        l.ballotscanner(this);
    }

    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("ballotscanner"),
                StringExpression.makeString(status));
    }

}
