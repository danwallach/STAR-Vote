package votebox.events;

import sexpression.*;

/**
 * This event confirms that a Supervisor has cast a ballot that a scanner has announced
 *
 * @author Matt Bernhard
 */
public class BallotScanAcceptedEvent extends AAnnounceEvent{

    private String _bid;
    private int serial;

    /**
     * The matcher for the BallotReceivedEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("ballot-accepted"), StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                String BID = ((ListExpression) res).get(0).toString();
                return new BallotScanAcceptedEvent(serial,  BID );
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
    }//getMatcher

    /**
     * Constructs a new BallotReceivedEvent.
     *
     * @param bid
     *          The rejected ballot's id
     */
    public BallotScanAcceptedEvent(int serial, String bid) {
        super(serial);
        _bid = bid;
    }

    /**
     * @return the ballot id
     */
    public String getBID(){
        return _bid;
    }

    /**
     * @param l the listener
     */
    public void fire(VoteBoxEventListener l) {
        l.ballotAccepted( this );
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression( StringExpression
                .makeString( "ballot-accepted" ),
                StringExpression.makeString(_bid) );
    }
}
