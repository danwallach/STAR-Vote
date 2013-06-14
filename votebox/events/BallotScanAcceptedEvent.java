package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
import sexpression.StringExpression;

import java.util.HashMap;

/**
 * @author Matt Bernhard
 * 6/14/13
 *
 * This event confirms that a Supervisor has cast a ballot that a scanner has announced
 */
public class BallotScanAcceptedEvent implements IAnnounceEvent{

    private ASExpression _bid;

    /**
     * The matcher for the BallotReceivedEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("ballot-accepted %bid:#string"));

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON) {
                return new BallotScanAcceptedEvent(result.get("bid"));

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
    public BallotScanAcceptedEvent(ASExpression bid) {
        _bid = bid;
    }

    /**
     * @return the ballot id
     */
    public String getBID(){
        return _bid.toString();
    }

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
                .makeString( "ballot-accepted" ),
                _bid );
    }
}
