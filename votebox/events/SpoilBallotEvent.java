package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

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
        private ASExpression pattern = ASExpression
                .make("(spoil-ballot %bid:#string %nonce:#any)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ListExpression list = (ListExpression)sexp;

            String bid = list.get(0).toString();

            ASExpression nonce = list.get(1);

            return new SpoilBallotEvent(serial, bid, nonce);
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
