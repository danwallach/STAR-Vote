package votebox.events;

import sexpression.*;

/**
 * Event that represents the ballotscanner message
 *
 * @author aroe, Matt Bernhard
 */
public class BallotScannerEvent implements IAnnounceEvent {

    /**
     * Matcher for the ballotscanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("ballotscanner"), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON,
                StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                int label = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );
                String status = ((ListExpression) res).get(1).toString();
                int battery = Integer.parseInt(((ListExpression)res).get(2).toString());
                int protectedCount = Integer.parseInt( ((ListExpression) res)
                        .get(3).toString() );
                int publicCount = Integer.parseInt( ((ListExpression) res)
                        .get(4).toString() );
                return new BallotScannerEvent(serial, label, status, battery, protectedCount, publicCount);
            }

            return null;
        }


    };
    private int serial;
    private String status;
    private int battery;
    private int protectedCount;
    private int publicCount;
    private int label;

    public BallotScannerEvent(int serial, int label, String status, int battery,
                              int protectedCount, int publicCount) {
        this.serial = serial;
        this.label = label;
        this.status = status;
        this.battery = battery;
        this.protectedCount = protectedCount;
        this.publicCount = publicCount;
    }

    /**
     * @return the label
     */
    public int getLabel() {
        return label;
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

    /**
     * @return the battery level
     */
    public int getBattery() {
        return battery;
    }

    /**
     * @return the protected count
     */
    public int getProtectedCount() {
        return protectedCount;
    }

    /**
     * @return the public count
     */
    public int getPublicCount() {
        return publicCount;
    }

    public void fire(VoteBoxEventListener l) {
        l.ballotscanner(this);
    }

    public ASExpression toSExp() {
        return new ListExpression(

                StringExpression.makeString("ballotscanner"),
                StringExpression.makeString( Integer.toString( label ) ),
                StringExpression.makeString(status),
                StringExpression.makeString(battery + ""),
                StringExpression.makeString( Integer.toString( protectedCount ) ),
                StringExpression.makeString( Integer.toString( publicCount ) ));
    }

}
