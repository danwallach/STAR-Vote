package votebox.events;

import crypto.adder.PublicKey;

import sexpression.*;
import crypto.interop.AdderKeyManipulator;

//TODO: We need a way to send the final public key to the VoteBoxes (as well as preserve such a key
//TODO: in the record, as it changes from run to run).  Alongside the ballot seems as good a place as any other.

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

                /* This is the public key with which all votes are encrypted so they can be verified by NIZKs */
                PublicKey finalPubKey = PublicKey.fromASE(((ListExpression) res).get(4));

                /* De-serialize and return an object representation of the aforementioned data */
                return new AuthorizedToCastWithNIZKsEvent(serial, otherSerial, nonce, precinct, ballot, finalPubKey);
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

    /** The public key to use for encryption and NIZKs */
    private PublicKey finalPubKey;



    /**
     * @see votebox.events.AuthorizedToCastEvent#AuthorizedToCastEvent(int, int, sexpression.ASExpression, String, byte[])
     * @param finalPubKey the public key that will be used to encrypt ballots
     */
    public AuthorizedToCastWithNIZKsEvent(int serial, int node, ASExpression nonce, String precinct, byte[] ballot,  PublicKey finalPubKey){
    	super(serial, node, nonce, precinct, ballot);

        this.finalPubKey = finalPubKey;
    	
    	/* This is a global value, on both the VoteBox and Supervisor side. */
    	AdderKeyManipulator.setCachedKey(finalPubKey);
    }

    /** @return the public key used to encrypt ballots sent with this authorization method */
    public PublicKey getFinalPubKey() {
        return finalPubKey;
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
                StringExpression.makeString( getPrecinct() ),
                finalPubKey.toASE());
    }
}
