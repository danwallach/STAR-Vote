package votebox.events;

import java.math.BigInteger;

import edu.uconn.cse.adder.PublicKey;

import sexpression.*;
import crypto.interop.AdderKeyManipulator;

//TODO: We need a way to send the final public key to the VoteBoxes (as well as preserve such a key
//TODO: in the record, as it changes from run to run).  Alongside the ballot seems as good a place as any other.

/**
 * Event to authorize a VoteBox booth to cast using NZIKS (Non-interactive, zero-knowledge proofs)
 * 
 * @author Montrose
 *
 */
public class AuthorizedToCastWithNIZKsEvent extends AuthorizedToCastEvent {
	/**
     * The matcher for the AuthorizedToCastEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "authorized-to-cast-with-nizks" ), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON, StringWildcard.SINGLETON, Wildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                int node = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );
                ASExpression nonce = ((ListExpression) res)
                        .get( 1 );

                byte[] ballot = ((StringExpression) ((ListExpression) res)
                        .get( 2 )).getBytesCopy();
                String precinct = ((ListExpression) res).get( 3 )
                        .toString();
                PublicKey finalPubKey = PublicKey.fromASE(((ListExpression) res).get(4));
                
                return new AuthorizedToCastWithNIZKsEvent( serial, node, nonce, precinct, ballot,  finalPubKey );
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
    
    private PublicKey _finalPubKey;
    
    public AuthorizedToCastWithNIZKsEvent(int serial, int node, ASExpression nonce, String precinct, byte[] ballot,  PublicKey finalPubKey){
    	super(serial, node, nonce, precinct, ballot);
    	
    	_finalPubKey = finalPubKey;
    	
    	//This is a global value, on both the VoteBox and Supervisor side.
    	AdderKeyManipulator.setCachedKey(finalPubKey);
    }
    
    public ASExpression toSExp() {
        /*return new ListExpression( StringExpression
                .makeString( "authorized-to-cast" ), StringExpression
                .makeString( Integer.toString( node ) ), StringExpression
                .makeString( nonce ), StringExpression.makeString( ballot ) );*/
    	return new ListExpression( 
    			StringExpression.makeString( "authorized-to-cast-with-nizks" ), 
                StringExpression.makeString( Integer.toString( getNode() ) ), 
                getNonce(),
                StringExpression.makeString( getBallot() ),
                StringExpression.makeString( precinct),

                //StringExpression.makeString(_finalPubKey.toString()));
                _finalPubKey.toASE());
    }
}
