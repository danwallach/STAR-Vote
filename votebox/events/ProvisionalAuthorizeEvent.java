package votebox.events;

import sexpression.*;


/**
 * An event which sends a ballot to a votebox in a provisional voting session
 *
 * @author Matt Bernhard
 */
public class ProvisionalAuthorizeEvent extends ABallotEvent {

    /** The serial for the machine being provisionally authorized */
    private int targetSerial;

    /**
     * The matcher for the ProvisionalAuthorizeEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("provisional-authorized-to-cast"), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {

                int node = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );

                ASExpression nonce = ((ListExpression) res).get(1);

                byte[] ballot = ((StringExpression) ((ListExpression) res)
                        .get( 2 )).getBytesCopy();

                return new ProvisionalAuthorizeEvent( serial, node, nonce, ballot );
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
     * Constructs a new ProvisionalAuthorizeEvent.
     *
     * @param serial the serial number of the sender
     * @param targetSerial the target's serial
     * @param nonce  the nonce (or authorization code), an array of bytes
     * @param ballot the ballot in zip format, stored as an array of bytes
     */
    public ProvisionalAuthorizeEvent(int serial, int targetSerial, ASExpression nonce,
                                 byte[] ballot) {
        super(serial, ballot, nonce);
        this.targetSerial = targetSerial;
    }

    /**
     * @return the targetSerial
     */
    public int getTargetSerial() {
        return targetSerial;
    }

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.provisionalAuthorizedToCast( this );
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression( StringExpression
                .makeString( "provisional-authorized-to-cast" ), StringExpression
                .makeString( Integer.toString(targetSerial) ), getNonce(), StringExpression.makeString( getBallot() ) );
    }

}
