package votebox.events;

import sexpression.*;

/**
 * @author Matt Bernhard
 * 6/17/13
 */
public class BallotPrintingEvent extends ABallotEvent {

    /**
     * Matcher for the pinEntered message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression( StringExpression
                .makeString("ballot-printing"), StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ASExpression res = pattern.match( sexp );
            if (res != NoMatch.SINGLETON) {
                String bID = ((ListExpression) res).get(0).toString();
                ASExpression nonce = ((ListExpression) res).get( 1 );
                return new BallotPrintingEvent( serial, bID, nonce );
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
    }


    public BallotPrintingEvent(int serial, String bid, ASExpression nonce) {
        super(serial, bid, nonce);
    }

    public void fire(VoteBoxEventListener l) {
        l.ballotPrinting(this);
    }

    public ASExpression toSExp() {
        return new ListExpression( StringExpression.makeString("ballot-printing"),
                StringExpression.makeString( getBID() ),
                getNonce());
    }
}
