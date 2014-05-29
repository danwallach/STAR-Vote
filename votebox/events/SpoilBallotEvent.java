package votebox.events;

import sexpression.*;

/**
 * This is an event that gets fired when the supervisor spoils a voter's ballot
 *
 * @author Matt Bernhard
 */
@SuppressWarnings("unused")
public class SpoilBallotEvent extends ABallotEvent {

    /**
     * Matcher for the SpoilBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression.makeString("spoil-ballot"),
                StringWildcard.SINGLETON, StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);

            if(res != NoMatch.SINGLETON) {
                ListExpression list = (ListExpression) sexp;

                String bid = list.get(1).toString();

                ASExpression nonce = list.get(2);

                return new SpoilBallotEvent(serial, bid, nonce);
            }

            return null;
        }

    };

    /**
     * Constructs a new SpoilBallotEvent
     *
     * @param serial the serial number of the sender
     * @param bid the ballot to be spoiled
     * @param nonce  the nonce of the ballot
     */
    public SpoilBallotEvent(int serial, String bid, ASExpression nonce) {
        super(serial, bid, nonce);
    }

    /** @return the matcher rule */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(votebox.events.VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.spoilBallot(this);
    }

    /**
     * @see votebox.events.IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("spoil-ballot"),
                StringExpression.make(getBID()), getNonce());
    }
}
