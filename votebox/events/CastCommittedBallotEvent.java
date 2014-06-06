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
 * Event that represents the cast-ballot message:<br>
 *
 * <pre>
 * (cast-ballot nonce encrypted-ballot)
 * </pre>
 *
 * See <a href="https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages">
 * https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages</a> for a complete
 * list of messages.
 *
 * @author Corey Shaw
 */
public class CastCommittedBallotEvent extends ABallotEvent {

    /**
     * Matcher for the CastCommittedBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("cast-ballot"), StringWildcard.SINGLETON, StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);

            if(res != NoMatch.SINGLETON) {
                ListExpression list = (ListExpression) sexp;

                ASExpression nonce = list.get(1);

                String bid = list.get(2).toString();

                return new CastCommittedBallotEvent(serial, nonce, bid);
            }

            return null;

        }
    };

    public CastCommittedBallotEvent(int serial, ASExpression nonce, byte[] ballot, String bid) {
        super(serial, nonce, bid, ballot);
    }

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }//getMatcher

    /**
     * Constructs a new CastCommittedBallotEvent
     *
     * @param serial the serial number of the sender
     * @param nonce  the nonce
     * @param bid identifies the ballot that is cast
     */
    public CastCommittedBallotEvent(int serial, ASExpression nonce, String bid) {
        super(serial, bid, nonce);
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(votebox.events.VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.castCommittedBallot(this);
    }

    /**
     * @see votebox.events.IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("cast-ballot"),
                getNonce(),
                StringExpression.makeString(getBID()));
    }
}