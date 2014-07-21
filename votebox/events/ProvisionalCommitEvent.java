package votebox.events;

import sexpression.*;

/**
 * An event which allows for unique handling of provisionally committed ballots
 *
 * @author Matt Bernhard
 */
public class ProvisionalCommitEvent extends ABallotEvent {

    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression.makeString("commit-provisional-ballot"),
                StringWildcard.SINGLETON, Wildcard.SINGLETON, StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            ASExpression res = pattern.match(sexp);

            if (res != NoMatch.SINGLETON) {
                ListExpression result = (ListExpression) sexp;

                ASExpression nonce = result.get(1);

                byte[] ballot = ((StringExpression) result.get(2)).getBytesCopy();

                String bid = result.get(3).toString();

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
        super(serial, nonce, bid, ballot);
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
        return new ListExpression(StringExpression.makeString("commit-provisional-ballot"),
                getNonce(),
                StringExpression.makeString(getBallot()),
                StringExpression.make(getBID()));
    }

}
