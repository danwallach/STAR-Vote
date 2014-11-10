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
 * An event resulting from the receipt of a encrypted-cast-ballot event.<BR>
 * Form:<BR>
 * (encrypted-cast-ballot [nonce] [ballot])<BR>
 * Where [ballot] is in the form ((cand-#1-vote-race1 cand-#2-vote-race1) (cand-#1-vote-race2 ...))
 * with a whole message of E(...) thrown in.
 * @author Montrose
 */
public class EncryptedCastBallotEvent extends CastCommittedBallotEvent{

    /**
     * Matcher for the EncryptedCastBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression.makeString("encrypted-cast-ballot"),
                StringWildcard.SINGLETON, Wildcard.SINGLETON, StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {

                ListExpression lsexp = (ListExpression) sexp;

                ASExpression nonce = lsexp.get(1);

                byte[] ballot = ((StringExpression) lsexp.get(2)).getBytesCopy();

                String bid = lsexp.get(3).toString();

                int source = Integer.parseInt(lsexp.get(4).toString());

                return new EncryptedCastBallotEvent(serial, nonce, ballot, bid, source);
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
     * @param serial the serial number of the sender
     * @param nonce the nonce
     * @param ballot the encrypted ballot, as an array of bytes
     */
    public EncryptedCastBallotEvent(int serial, ASExpression nonce, byte[] ballot, String bid, int source) {
        super(serial, nonce, ballot, bid, source);

    }

    /**
     * @see votebox.events.IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("encrypted-cast-ballot"),
                getNonce(),
                StringExpression.makeString(getBallot()),
                StringExpression.makeString(getBID()),
                StringExpression.makeString(getSource() + ""));
    }
}