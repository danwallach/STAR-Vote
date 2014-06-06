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

import java.util.ArrayList;
import java.util.List;

/**
 * Event that represents the activated message:<br>
 * 
 * <pre>
 * (activated ((status)*))
 * </pre>
 * 
 * See <a href="https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages">
 * https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages</a> for a complete
 * list of messages.
 * 
 * @author Corey Shaw
 */
public class ActivatedEvent extends AAnnounceEvent {


    /** The statuses of all the machines connected to the incurring machine when it is activate */
    private List<StatusEvent> statuses;

    /**
     * Matcher for the ActivatedEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        /* The pattern for this message is the string "activating" followed by optional StatusEvent messages */
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "activated" ), new ListWildcard( new ListWildcard(
                Wildcard.SINGLETON ) ) );

        private VoteBoxEventMatcher statusMatcher = new VoteBoxEventMatcher(
                StatusEvent.getMatcher() );

        /**
         * @see votebox.events.MatcherRule#match(int, sexpression.ASExpression)
         */
        public IAnnounceEvent match(int serial, ASExpression sexp) {
            /* Attempt to match the incoming S-Expression with this event's patter */
            ASExpression res = pattern.match(sexp);

            /* If the pattern matched, return a new event with the necessary fields filled in */
            if (res != NoMatch.SINGLETON) {

                /* We will build a list of the connected machine's statuses */
                ArrayList<StatusEvent> statuses = new ArrayList<>();

                /* each subexpression after "activated" should represent StatusEvents */
                for (ASExpression s : (ListExpression) ((ListExpression) res).get( 0 )) {
                    StatusEvent status = (StatusEvent) statusMatcher.match( 0, s);

                    /* If there is something after this message that isn't a StatusEvent, we have a malformed message */
                    if (status == null)
                        return null;

                    /* Add the newly build status to our list of statuses */
                    statuses.add( status );
                }

                /* Now that we've matched the message, return an Event representation of it */
                return new ActivatedEvent( serial, statuses );
            }

            /* If we failed to match, return nothing */
            return null;
        }
    };
    
    /**
     * 
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
    	return MATCHER;
    }

    /**
     * Constructs a new ActivatedEvent with given serial number and list of
     * known statuses
     * 
     * @param serial the serial number
     * @param statuses the list of known statuses
     */
    public ActivatedEvent(int serial, List<StatusEvent> statuses) {
        super(serial);
        this.statuses = statuses;
    }

    /**
     * @return the list of known statuses
     */
    public List<StatusEvent> getStatuses() {
        return statuses;
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l) {
        l.activated( this );
    }

    /**
     * @see IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp() {
        /* Build a list of statuses from this event */
        ArrayList<ASExpression> statusList = new ArrayList<>();
        for (IAnnounceEvent s : statuses)
            statusList.add( s.toSExp() );

        /* Convert everything to S-Expressions and then return it */
        return new ListExpression( StringExpression.makeString( "activated" ),
                new ListExpression( statusList ) );
    }

}
