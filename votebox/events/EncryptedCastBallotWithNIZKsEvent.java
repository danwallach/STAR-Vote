package votebox.events;

import sexpression.*;

/**
 * equivalent to EncryptedCastBallotEvent, however with functionality to deal with Non-Interactive, Zero-Knowledge proofs.
 */
public class EncryptedCastBallotWithNIZKsEvent extends EncryptedCastBallotEvent {

    /**
     * Matcher for the EncryptedCastBallotEvent
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression.makeString("encrypted-cast-ballot-with-nizks"),
                StringWildcard.SINGLETON, Wildcard.SINGLETON, StringWildcard.SINGLETON);

        public IAnnounceEvent match(int serial, ASExpression sexp) {

            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {

                ListExpression lsexp = (ListExpression) sexp;

                ASExpression nonce = lsexp.get(1);

                byte[] ballot = ((StringExpression) lsexp.get(2)).getBytesCopy();

                String bid = lsexp.get(3).toString();

                return new EncryptedCastBallotWithNIZKsEvent(serial, nonce, ballot, bid);
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