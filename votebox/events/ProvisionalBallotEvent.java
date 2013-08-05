package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
import sexpression.StringExpression;

import java.util.HashMap;

/**
 * An event which represents that printing did not occur successfully
 *
 * @author Matt Bernhard
 */
public class ProvisionalBallotEvent implements IAnnounceEvent{

    private int _serial;
    private ASExpression _nonce;
    private ASExpression _bid;

    /**
     * Matcher for the ProvisionalBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(provisional-ballot %nonce:#string %bid:#string)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON)
                return new ProvisionalBallotEvent(serial, result.get("nonce"), result.get("bid"));

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
     * Constructs a new ProvisionalBallotEvent
     *
     * @param serial the serial number of the sender
     * @param nonce  the nonce
     * @param bid identifies the ballot that is cast
     */
    public ProvisionalBallotEvent(int serial, ASExpression nonce, ASExpression bid) {
        _serial = serial;
        _nonce = nonce;
        _bid = bid;
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

    public ASExpression getBID(){
        return _bid;
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
                _nonce, _bid);
    }
}
