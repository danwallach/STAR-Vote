package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NoMatch;
import sexpression.StringExpression;

/**
 * An event which allows for unique handling of provisionally committed ballots
 *
 * @author Matt Bernhard
 */
public class ProvisionalCommitEvent extends ABallotEvent {

    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(provisional-commit-ballot %nonce:#string %ballot:#any %bid:#string)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            ASExpression res = pattern.match(sexp);

            if (res != NoMatch.SINGLETON) {
                ListExpression result = (ListExpression) sexp;

                ASExpression nonce = result.get(0);

                byte[] ballot = ((StringExpression) result.get(1)).getBytesCopy();

                String bid = result.get(2).toString();

                return new ProvisionalCommitEvent(serial, nonce, ballot, bid);
            }

            return null;
        }
    };

    /**
     *
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }

    /**
     * Constructor
     * @param serial the serial of the sender
     * @param nonce the voting session nonce
     * @param ballot the provisional ballot
     * @param bid the ID of the provisional ballot
     */
    public ProvisionalCommitEvent(int serial, ASExpression nonce, byte[] ballot, String bid) {
        super(serial, nonce, ballot, bid);
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(votebox.events.VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.provisionalCommitBallot(this);
    }

    /**
     * @see votebox.events.IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.make("provisional-commit-ballot"),
                getNonce(),
                StringExpression.makeString(getBallot()),
                StringExpression.make(getBID()));
    }

}
