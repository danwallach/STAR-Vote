package votebox.events;

import sexpression.*;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * An event which signals that a scanned ballot was rejected, i.e. not recognized by the supervisor
 */
public class BallotScanRejectedEvent implements IAnnounceEvent {

    private ASExpression _bid;

    /**
     * The matcher for the BallotReceivedEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("ballot-rejected %bid:#string"));

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON) {
                return new BallotScanRejectedEvent(result.get("bid"));

            }
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

    /**
     * Constructs a new BallotReceivedEvent.
     *
     * @param bid
     *          The rejected ballot's id
     */
    public BallotScanRejectedEvent(ASExpression bid) {
        _bid = bid;
    }

    /**
     * @return
     */

    /**
     * @param l the listener
     */
    public void fire(VoteBoxEventListener l) {
       // l.ballotReceived( this );
    }

    public int getSerial(){
       return -1;
    }

    public ASExpression toSExp() {
        /*return new ListExpression( StringExpression
                .makeString( "ballot-received" ), StringExpression
                .makeString( Integer.toString( node ) ), StringExpression
                .makeString( nonce ) );*/

        return new ListExpression( StringExpression
                .makeString( "ballot-rejected" ),
                _bid );
    }
}
