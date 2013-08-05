package votebox.events;

import sexpression.*;

/*
 * Created with IntelliJ IDEA.
 * User: martinnikol
 * Date: 6/10/13
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * An event that holds information about the polls, mainly whether or not the polls are currently open. Used to
 * notify VoteBox booths that join the network late the the polls are open and that they should prompt for a user PIN
 */
public class PollStatusEvent implements IAnnounceEvent{

    private int serial;

    private int node;

    private int pollsOpen;

    /**
     * Matcher for the AssignLabelEvent.
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("poll-status"), StringWildcard.SINGLETON,
                StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                int node = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );
                int pollsOpen = Integer.parseInt( ((ListExpression) res).get( 1 )
                        .toString() );
                return new PollStatusEvent(serial, node, pollsOpen);
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
     * Constructs a new AssignLabelEvent.
     *
     * @param serial
     *            the serial number of the sender
     * @param node
     *            the node id
     * @param pollsOpen
     *            the new label
     */
    public PollStatusEvent(int serial, int node, int pollsOpen) {
        this.serial = serial;
        this.node = node;
        this.pollsOpen = pollsOpen;
    }

    /**
     * @return the new label
     */
    public int getPollStatus() {
        return pollsOpen;
    }

    /**
     * @return the node
     */
    public int getNode() {
        return node;
    }

    public int getSerial() {
        return serial;
    }

    public void fire(VoteBoxEventListener l) {
        l.pollStatus( this );
    }

    public ASExpression toSExp() {
        return new ListExpression(
                StringExpression.makeString( "poll-status" ), StringExpression
                .makeString( Integer.toString( node ) ),
                StringExpression.makeString( Integer.toString( pollsOpen ) ) );
    }
}
