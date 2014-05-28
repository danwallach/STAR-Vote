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
 * Event that represents the assign-label message:<br>
 * 
 * <pre>
 * (assign-label node-id new-label)
 * </pre>
 * 
 * See <a href="https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages">
 * https://sys.cs.rice.edu/votebox/trac/wiki/VotingMessages</a> for a complete
 * list of messages.
 * 
 * @author cshaw
 */
public class AssignLabelEvent extends AAnnounceEvent {

    /** The serial of the machine that is being labeled */
    private int otherSerial;

    /** The new label for the other machine */
    private int label;

    /**
     * Matcher for the AssignLabelEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        /*
         * The pattern for this message is "assign-label" followed by
         * the serial for the machine to be labelled and the new label
         * i.e. (assignlabel otherSerial newLabel)
         */
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString( "assign-label" ), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON );

        /** @see votebox.events.MatcherRule#match(int, sexpression.ASExpression) */
        public IAnnounceEvent match(int serial, ASExpression sexp) {
            /* Attempt to match the expression */
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {

                /* The first field is the serial number of the other machine */
                int otherSerial = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );

                /* The second field is the new label for the aforementioned machine */
                int label = Integer.parseInt( ((ListExpression) res).get( 1 )
                        .toString() );

                /* On matching the message, return a new event object */
                return new AssignLabelEvent( serial, otherSerial, label );
            }

            /* If the pattern wasn't matched, return nothing */
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
     * Constructs a new AssignLabelEvent.
     * 
     * @param serial the serial number of the sender
     * @param node the node id
     * @param label the new label
     */
    public AssignLabelEvent(int serial, int node, int label) {
        this.serial = serial;
        this.otherSerial = node;
        this.label = label;
    }

    /**
     * @return the new label
     */
    public int getLabel(){
        return label;
    }

    /**
     * @return the node
     */
    public int getOtherSerial(){
        return otherSerial;
    }

    /**
     * @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener)
     */
    public void fire(VoteBoxEventListener l){
        l.assignLabel( this );
    }

    /**
     * @see IAnnounceEvent#toSExp()
     */
    public ASExpression toSExp(){
        return new ListExpression(
                StringExpression.makeString( "assign-label" ), StringExpression
                        .makeString( Integer.toString(otherSerial) ),
                StringExpression.makeString( Integer.toString( label ) ) );
    }

}
