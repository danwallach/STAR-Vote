package votebox.events;

import sexpression.*;

/**
 * An event which represents that printing did not occur successfully
 *
 * @author Matt Bernhard
 */
@SuppressWarnings("unused")
public class ProvisionalBallotEvent extends ABallotEvent{

    /**
     * Matcher for the ProvisionalBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression.makeString("provisional-ballot"),
                StringWildcard.SINGLETON, Wildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);

            if(res != NoMatch.SINGLETON) {
                ListExpression list = (ListExpression) sexp;

                return new ProvisionalBallotEvent(serial, list.get(1), list.get(2).toString());
            }

            return null;
        }
    };

    /** @return the mathcher rule */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }

    /**
     * Constructs a new ProvisionalBallotEvent
     *
     * @param serial the serial number of the sender
     * @param nonce  the nonce
     * @param bid identifies the ballot that is cast
     */
    public ProvisionalBallotEvent(int serial, ASExpression nonce, String bid) {
        super(serial, bid, nonce);
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(votebox.events.VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.announceProvisionalBallot(this);
    }

    /**
     * @see votebox.events.IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("provisional-ballot"),
                getNonce(), StringExpression.make(getBID()));
    }
}
