package votebox.events;

import sexpression.*;

/**
 * @author Matt Bernhard
 *
 * An event which represents that printing did not occur successfully
 */
public class BallotPrintFailEvent extends ABallotEvent {


    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("ballot-print-fail"), StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                String bID = ((ListExpression) res).get(0).toString();
                ASExpression nonce = ((ListExpression) res).get( 1 );
                return new BallotPrintFailEvent( serial, bID, nonce );
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
     * Constructor
     *
     * @param serial the serial number of the sender
     * @see votebox.events.ABallotEvent
     */
    public BallotPrintFailEvent(int serial, String bid, ASExpression nonce) {
        super(bid, nonce);
        this.serial = serial;
    }

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.ballotPrintFail(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("ballot-print-fail"),
                StringExpression.makeString( getBID() ),
                getNonce());
    }

}
