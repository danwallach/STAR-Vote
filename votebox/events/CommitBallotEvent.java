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
 * This event represents what gets sent out on the network when the machine
 * needs to commit to a cipher representation of the ballot.<br>
 * <br>
 * Format: (commit-ballot [nonce] [ballot])
 * 
 * @author kyle
 * 
 */
public class CommitBallotEvent extends ABallotEvent {

    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression.makeString("commit-ballot"),
                StringWildcard.SINGLETON, Wildcard.SINGLETON, StringWildcard.SINGLETON,
                StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {


                ListExpression lsexp = (ListExpression) sexp;

                ASExpression nonce = lsexp.get(1);

                byte[] ballot = ((StringExpression) lsexp.get(2)).getBytesCopy();

                String bid = lsexp.get(3).toString();

                String precinct = lsexp.get(4).toString();


                return new CommitBallotEvent(serial, nonce, ballot, bid, precinct);

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
     * Constructor.
     *
     * @see votebox.events.ABallotEvent#ABallotEvent(int, sexpression.ASExpression, byte[], String, String)
     */
    public CommitBallotEvent(int serial, ASExpression nonce, byte[] ballot, String bid, String precinct) {
        super(serial, nonce, ballot, bid, precinct);
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(votebox.events.VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.commitBallot(this);
    }


    /**
     * @see votebox.events.IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.make("commit-ballot"),
                getNonce(),
                StringExpression.makeString(getBallot()),
                StringExpression.make(getBID()),
                StringExpression.make(getPrecinct()));

    }

}
