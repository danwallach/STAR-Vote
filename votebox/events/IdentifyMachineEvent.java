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
 * Event that represents the polls-open message:<br>
 *
 * <pre>
 * (polls-open local-timestamp keyword)
 * </pre>
 *
 * See <a href="https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages">
 * https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages</a> for a complete
 * list of messages.
 *
 * @author cshaw
 */
public class IdentifyMachineEvent implements IAnnounceEvent {

    private int serial;

    private long timestamp;

    private static String machineType;

    /**
     * Matcher for the PollsOpenEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "here-i-am" ), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                long timestamp = Long.parseLong( ((ListExpression) res).get( 0 )
                        .toString() );
                String keyword = ((ListExpression) res).get( 1 ).toString();
                return new IdentifyMachineEvent( serial,timestamp,machineType);
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

    /**
     * Constructs a new PollsOpenEvent
     *
     * @param serial
     *            the serial number
     * @param timestamp
     *            the local timestamp
     */
    public IdentifyMachineEvent(int serial, long timestamp, String machineType) {
        this.serial = serial;
        this.machineType = machineType;
        this.timestamp = timestamp;
    }

    /**
     * @return the keyword
     */

    public int getSerial() {
        return serial;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    public String getMachineType(){
        return machineType;
    }

    public void fire(VoteBoxEventListener l) {
        l.identifyMachine( this );
    }

    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString( "here-i-am" ),
                StringExpression.makeString(machineType),
                StringExpression.makeString( Long.toString( timestamp ) ));
    }

}
