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
 * Event that represents the ballot-received message:<br>
 * 
 * <pre>
 * (ballot-received targetSerial-id nonce)
 * </pre>
 * 
 * See <a href="https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages">
 * https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages</a> for a complete
 * list of messages.
 * 
 * @author Corey Shaw
 */
public class BallotReceivedEvent extends ABallotEvent {

    private int targetSerial;

    /**
     * The matcher for the BallotReceivedEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "ballot-received" ), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON, StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {

                int node = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );

                ASExpression nonce = ((ListExpression) res).get(1);

                String bid = ((ListExpression) res).get(2).toString();

                String precinct = ((ListExpression) res).get(3).toString();

                return new BallotReceivedEvent( serial, node, nonce, bid, precinct);
            }
            return null;
        }
    };

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
    	return MATCHER;
    }//getMatcher
    
    /**
     * Constructs a new BallotReceivedEvent.
     *
     * @param serial the serial number of the sender
     * @param targetSerial the serial of the original ballot sender
     * @param nonce a nonce for the voting session
     */
    public BallotReceivedEvent(int serial, int targetSerial, ASExpression nonce, String bid, String precinct) {
        super(serial, nonce, bid, precinct);
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
        l.ballotReceived( this );
    }

    /** @see IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {

    	return new ListExpression( StringExpression.makeString("ballot-received"),
                StringExpression.makeString(Integer.toString(targetSerial)),
                getNonce(),
                StringExpression.makeString(getBID()),
                StringExpression.makeString(getPrecinct()));

    }

}
