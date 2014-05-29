/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package votebox.events;

import sexpression.*;

/**
 * Event that represents the authorized-to-cast message:<br>
 * 
 * <pre>
 * (authorized-to-cast targetSerial-id nonce ballot)
 * </pre>
 * 
 * See <a href="https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages">
 * https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages</a> for a complete
 * list of messages.
 * 
 * @author cshaw
 */
public class AuthorizedToCastEvent extends ABallotEvent {

    /** Serial number of the machine being authorized */
    private int targetSerial;


    /**
     * The matcher for the AuthorizedToCastEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        /* This message will be of the form (authorized-to-cast targetSerial nonce precinct ballot)*/
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "authorized-to-cast" ), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            /* Match the expression */
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {

                /* The first field is the serial of the machine being authorized */
                int otherSerial = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );

                /* The second field is the session nonce */
                ASExpression nonce = ((ListExpression) res)
                        .get( 1 );

                /* The third field is the ballot style */
                String precinct = ((ListExpression) res).get( 2 ).toString();

                /* The final field is the ballot data */
                byte[] ballot = ((StringExpression) ((ListExpression) res)
                        .get( 3 )).getBytesCopy();

                /* Put the de-serialized data into an object */
                return new AuthorizedToCastEvent( serial, otherSerial, nonce,  precinct, ballot );
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
     * Constructs a new AuthorizedToCastEvent.
     *
     * @param serial the serial number of the sender
     * @param targetSerial the targetSerial id
     * @param nonce the nonce (or authorization code), an array of bytes
     * @param ballot the ballot data
     */
    public AuthorizedToCastEvent(int serial, int targetSerial, ASExpression nonce, String precinct, byte[] ballot) {
        super(serial, nonce, ballot, precinct);

        this.targetSerial = targetSerial;
    }


    /**
     * @return the serial number for the machine being authorized
     */
    public int getTargetSerial() {
        return targetSerial;
    }


    /**
     * @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.authorizedToCast( this );
    }

    /**
     * @see IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
    	return new ListExpression( StringExpression
                .makeString( "authorized-to-cast" ), StringExpression
                .makeString( Integer.toString(targetSerial) ), getNonce(), StringExpression
                .makeString( getPrecinct() ), StringExpression.makeString( getBallot() ));
    }
    
}
