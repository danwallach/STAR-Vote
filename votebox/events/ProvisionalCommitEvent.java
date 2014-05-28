package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
import sexpression.StringExpression;
import votebox.events.IAnnounceEvent;

import java.util.HashMap;

/**
 * An event which allows for unique handling of provionally committed ballots
 *
 * @author Matt Bernhard
 */
public class ProvisionalCommitEvent extends AAnnounceEvent {
    private final int _serial;
    private final ASExpression _nonce;
    private final ASExpression _ballot;
    private final ASExpression _bid;

    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(provisional-commit-ballot %nonce:#string %ballot:#any %bid:#string)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON)
                return new ProvisionalCommitEvent(serial, result.get("nonce"), result
                        .get("ballot"), result.get("bid"));

            return null;
        };
    };

    /**
     *
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }//getMatcher

    public ProvisionalCommitEvent(int serial, ASExpression nonce, ASExpression ballot, ASExpression bid) {
        _serial = serial;
        _nonce = nonce;
        _ballot = ballot;
        _bid = bid;
    }

    /**
     * @return the nonce
     */
    public ASExpression getNonce(){
        return _nonce;
    }

    /**
     * @return An S-Expression of the ballot
     */
    public ASExpression getBallot(){
        return _ballot;
    }

    /**
     * @return the ballot's ID
     */
    public ASExpression getBID(){
        return _bid;
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(votebox.events.VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.provisionalCommitBallot(this);
    }

    /**
     * @see votebox.events.IAnnounceEvent#getSerial()
     */
    public int getSerial() {
        return _serial;
    }

    /**
     * @see votebox.events.IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.make("provisional-commit-ballot"),
                _nonce, _ballot, _bid);
    }

}
