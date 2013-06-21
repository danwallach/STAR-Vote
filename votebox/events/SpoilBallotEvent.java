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
    private String _bid;
    private ASExpression _nonce;

    /**
     * Matcher for the SpoilBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(spoil-ballot %bid:#string %nonce:#any)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON)
                return new CastBallotEvent(serial, result.get("bid"), result
                        .get("nonce"));

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
     * @param bid the ballot to be spoiled
     * @param nonce  the nonce of the ballot
     */
    public SpoilBallotEvent(int serial,String bid, ASExpression nonce) {
        _serial = serial;
        _bid = bid;
        _nonce = nonce;
    }

    /**
     * @return the ballot
     */
    public String getBID() {
        return _bid;
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
                StringExpression.make(_bid), _nonce);
    }
}
