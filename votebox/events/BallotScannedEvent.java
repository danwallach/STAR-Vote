package votebox.events;

import sexpression.*;

/**
 * Event class for when a ballot is scanned
 */
public class BallotScannedEvent extends ABallotEvent {

    /**
     * Matcher for the ballotscanned message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("ballot-scanned"), StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                String bid = ((ListExpression) res).get(0).toString();
                return new BallotScannedEvent(serial, bid);
            }

            return null;
        }
    };

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }

    /**
     * Constructor of ballot
     *
     * @see votebox.events.ABallotEvent#ABallotEvent(int, String)
     */
    public BallotScannedEvent(int serial, String bid) {
        super(serial, bid);
    }

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.ballotScanned(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("ballot-scanned"),
                StringExpression.makeString(getBID()));
    }

}
