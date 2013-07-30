package votebox.events;

import sexpression.*;

/**
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 7/29/13
 */
public class TapMachineEvent implements IAnnounceEvent {

    /**
     * Matcher for the ballotScanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("tapmachine"),
                StringWildcard.SINGLETON, StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp){
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                return new TapMachineEvent(serial);
            }

            return null;
        }


    };

    private int serial;

    public TapMachineEvent(int serial){
        this.serial = serial;
    }

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }//getMatcher

    public int getSerial() {
        return serial;
    }

    public void fire(VoteBoxEventListener l) {
        l.tapMachine(this);
    }

    public ASExpression toSExp() {
        return new ListExpression(
                StringExpression.makeString("tapmachine")
        );
    }
}

