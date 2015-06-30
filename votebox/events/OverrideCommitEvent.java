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
 * Event that represents the override-cast message:<br>
 * 
 * <pre>
 * (override-cast targetSerial-id nonce)
 * </pre>
 * 
 * See <a href="https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages">
 * https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages</a> for a complete
 * list of messages.
 * 
 * @author Corey Shaw
 */
public class OverrideCommitEvent extends ABallotEvent {

    /** The serial for the machine being overridden */
    private int targetSerial;

    /**
     * Matcher for the OverrideCommitEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "override-cast" ), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                int node = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );
                ASExpression nonce = ((ListExpression) res).get(1);
                return new OverrideCommitEvent( serial, node, nonce );
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
     * Constructs a new OverrideCommitEvent
     * 
     * @param serial the serial number of the sender
     * @param node the targetSerial id
     * @param nonce the nonce
     */
    public OverrideCommitEvent(int serial, int node, ASExpression nonce) {
        super(serial, nonce);
        this.targetSerial = node;
    }

    /**
     * @return the targetSerial
     */
    public int getTargetSerial() {
        return targetSerial;
    }

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.overrideCommit(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
    	return new ListExpression( StringExpression
                .makeString( "override-cast" ), StringExpression
                .makeString( Integer.toString(targetSerial) ), getNonce());
    }

}
