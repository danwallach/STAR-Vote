package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

/**
 * equivalent to EncryptedCastBallotEvent, however with functionality to deal with Non-Interactive, Zero-Knowledge proofs.
 */
public class EncryptedCastBallotWithNIZKsEvent extends EncryptedCastBallotEvent {

    /**
     * Matcher for the EncryptedCastBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(encrypted-cast-ballot-with-nizks %nonce:#string %ballot:#any %bid:#any)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            ListExpression lsexp = (ListExpression) sexp;

            ASExpression nonce = lsexp.get(0);

            byte[] ballot = ((StringExpression) lsexp.get(1)).getBytesCopy();

            String bid = lsexp.get( 2 ).toString();

            return new EncryptedCastBallotWithNIZKsEvent(serial, nonce, ballot, bid);
        }
    };

    /**
     *
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }//getMatcher

    public EncryptedCastBallotWithNIZKsEvent(int serial, ASExpression nonce, byte[] ballot, String bid){
        super(serial, nonce, ballot, bid);
    }

    public ASExpression toSExp(){
        return new ListExpression(StringExpression.makeString("encrypted-cast-ballot-with-nizks"),
                getNonce(),
                StringExpression.makeString(getBallot()),
                StringExpression.makeString(getBID()));
    }
}