package votebox.events;

import java.util.HashMap;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
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
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON)
                return new EncryptedCastBallotWithNIZKsEvent(serial, result.get("nonce"), result
                        .get("ballot"), result.get("bid"));

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

    public EncryptedCastBallotWithNIZKsEvent(int serial, ASExpression nonce, ASExpression ballot, ASExpression bid){
        super(serial, nonce, ballot, bid);
    }

    public ASExpression toSExp(){
        return new ListExpression(StringExpression.makeString("encrypted-cast-ballot-with-nizks"),
                getNonce(), getBallot(), getBID());
    }
}