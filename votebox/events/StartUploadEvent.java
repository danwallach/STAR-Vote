package votebox.events;

import sexpression.*;

/**
 * An event that signifies to Tap that the supervisor will be sending over encrypted tallied ballots,
 * one ballot per precinct.
 *
 * @author Matt Bernhard
 */
public class StartUploadEvent extends AAnnounceEvent {

    /**
     * Matcher for the StartUploadEvent message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("start-upload"));

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {

                return new StartUploadEvent(serial);
            }

            return null;
        }
    };

    /**
     * @see AAnnounceEvent#AAnnounceEvent(int)
     */
    public StartUploadEvent(int serial) {
        super(serial);
    }

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){ return MATCHER; }

    /**
     * Fires this event on a listener
     *
     * @param l the listener
     */
    @Override
    public void fire(VoteBoxEventListener l) { l.startUpload(this); }

    /**
     * Converts this event to an sexpression so it can be serialized over the network.
     *
     * @return the sexp
     */
    @Override
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.make("start-upload"));
    }


}
