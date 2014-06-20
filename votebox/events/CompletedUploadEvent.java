package votebox.events;

import sexpression.*;

/**
 * An event that signifies to Tap that the supervisor has finished uploading
 *
 * @author Matt Bernhard
 */
public class CompletedUploadEvent extends AAnnounceEvent {

    /**
     * Matcher for the StartUploadEvent message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("completed-upload"));

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {

                return new CompletedUploadEvent(serial);
            }

            return null;
        }
    };

    /**
     * @see AAnnounceEvent#AAnnounceEvent(int)
     */
    public CompletedUploadEvent(int serial) {
        super(serial);
    }

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public MatcherRule getMatcher(){ return MATCHER; }

    /**
     * Fires this event on a listener
     *
     * @param l the listener
     */
    @Override
    public void fire(VoteBoxEventListener l) { l.completedUpload(this); }

    /**
     * Converts this event to an sexpression so it can be serialized over the network.
     *
     * @return the sexp
     */
    @Override
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.make("completed-upload"));
    }


}
