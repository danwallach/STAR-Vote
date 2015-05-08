package votebox.events;

import sexpression.*;

/**
 * Event to authorize a VoteBox booth to cast using NIZKs (Non-interactive, zero-knowledge proofs)
 * 
 * @author Montrose
 *
 */
public class AuthorizedToCastWithNIZKsEvent extends AuthorizedToCastEvent {
	/**
     * The matcher for the AuthorizedToCastEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        /* These have the same format as AuthorizedToCast, except they also include a public key */
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "authorized-to-cast-with-nizks" ), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON, StringWildcard.SINGLETON, Wildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {

                /* This is the serial of the machine being authorized */
                int otherSerial = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );

                /* This is the session nonce */
                ASExpression nonce = ((ListExpression) res)
                        .get( 1 );

                /* This is the ballot data */
                byte[] ballot = ((StringExpression) ((ListExpression) res)
                        .get( 2 )).getBytesCopy();

                /* This is the ballot style, per precinct */
                String precinct = ((ListExpression) res).get( 3 )
                        .toString();


                /* De-serialize and return an object representation of the aforementioned data */
                return new AuthorizedToCastWithNIZKsEvent(serial, otherSerial, nonce, precinct, ballot);
            }
            return null;
        }
    };

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
    	return MATCHER;
    }


    /**
     * @see votebox.events.AuthorizedToCastEvent#AuthorizedToCastEvent(int, int, sexpression.ASExpression, String, byte[])
     */
    public AuthorizedToCastWithNIZKsEvent(int serial, int node, ASExpression nonce, String precinct, byte[] ballot){
    	super(serial, node, nonce, precinct, ballot);

    }


    /**
     * @see IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {

    	return new ListExpression( 
    			StringExpression.makeString( "authorized-to-cast-with-nizks" ), 
                StringExpression.makeString( Integer.toString( getTargetSerial() ) ),
                getNonce(),
                StringExpression.makeString( getBallot() ),
                StringExpression.makeString( getPrecinct() ));
    }
}
