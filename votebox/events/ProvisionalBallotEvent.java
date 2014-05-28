package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

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
        private ASExpression pattern = ASExpression
                .make("(provisional-ballot %nonce:#string %bid:#string)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            ListExpression list = (ListExpression) sexp;

            return new ProvisionalBallotEvent(serial, list.get(0), list.get(1).toString());

        }
    };

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
