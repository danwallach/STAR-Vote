package votebox.events;

import sexpression.*;

/**
* Event that represents the ballotScanner message
*
* @author aroe, Matt Bernhard
*/
public class BallotScannerEvent extends AAnnounceEvent {

    /**
     * Matcher for the ballotScanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("ballotscanner"), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp){
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

    /** The sending machine's status that it announces */
    private String status;

    /** The battery status of the sending machine */
    private int battery;

    private int protectedCount;
    private int publicCount;

    /** The label for this machine */
    private int label;

    /**
     * Constructor for the event
     *
     * @param label this machine's label
     * @param status the machine's status
     * @param battery the machine's batter level
     * @param protectedCount the machine's protected count
     * @param publicCount the machine's public count
     */
    public BallotScannerEvent(int serial, int label, String status, int battery,
                              int protectedCount, int publicCount) {
        super(serial);
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

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.ballotScanner(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression(

                StringExpression.makeString("ballotscanner"),
                StringExpression.makeString( Integer.toString( label ) ),
                StringExpression.makeString(status),
                StringExpression.makeString( Integer.toString( battery ) ),
                StringExpression.makeString( Integer.toString( protectedCount ) ),
                StringExpression.makeString( Integer.toString( publicCount ) ));
    }

}
