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
                int battery = Integer.parseInt( ((ListExpression) res).get( 1 )
                        .toString() );
                return new BallotScannerEvent(serial, status, battery);
            }

            return null;
        }


    };
    private int serial;
    private String status;

    private int battery;

    public BallotScannerEvent(int serial, String status, int battery) {
        this.serial = serial;
        this.status = status;
        this.battery = battery;
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

    public int getBattery(){
        return battery;
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
                StringExpression.makeString(status),
                StringExpression.makeString(Integer.toString( battery ) ));
    }

}
