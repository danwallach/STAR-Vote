package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
import sexpression.StringExpression;

import java.util.HashMap;

/**
 * @author Matt Bernhard
 * Date: 6/21/13
 *
 * This is an event that gets fired when the supervisor spoils a voter's ballot
 */
public class SpoilBallotEvent implements IAnnounceEvent {

    private int _serial;
    private ASExpression _nonce;
    private ASExpression _ballot;

    /**
     * Matcher for the SpoilBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(spoil-ballot %nonce:#string %ballot:#any)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON)
                return new CastBallotEvent(serial, result.get("nonce"), result
                        .get("ballot"));

            return null;
        }

    };

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }//getMatcher

    /**
     * Constructs a new SpoilBallotEvent
     *
     * @param serial the serial number of the sender
     * @param nonce  the nonce
     * @param ballot the encrypted ballot, as an array of bytes
     */
    public SpoilBallotEvent(int serial, ASExpression nonce, ASExpression ballot) {
        _serial = serial;
        _nonce = nonce;
        _ballot = ballot;
    }

    /**
     * @return the ballot
     */
    public ASExpression getBallot() {
        return _ballot;
    }

    /**
     * @return the nonce
     */
    public ASExpression getNonce() {
        return _nonce;
    }

    public int getSerial() {
        return _serial;
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
                _nonce, _ballot);
    }
}
