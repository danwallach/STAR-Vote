package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Event representing uploading of cast ballots to post-election audit server
 * These are cast ballots, so only ballot hashes are sent
 *
 * @author Nelson Chen
 *         Date: 12/4/12
 */
public class CastBallotUploadEvent implements IAnnounceEvent {
    private int serial;
    private ASExpression _nonces;


    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(cast-ballot-upload %nonces:#any)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON) {
                return new CastBallotUploadEvent(serial, result.get("nonces"));
            }

            return null;
        }
    };

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }//getMatcher


    public CastBallotUploadEvent(int serial, ASExpression nonces) {
        this.serial = serial;
        this._nonces = nonces;
    }

    public ArrayList<String> getDumpList() {
        ListExpression nonceList = (ListExpression) _nonces;
        ArrayList<String> dumpList = new ArrayList<String>();

        for (ASExpression nonce : nonceList) {
            dumpList.add("cast:" + nonce.toString());
        }
        return dumpList;
    }


    public ASExpression toSExp() {
        return _nonces;
    }

    public void fire(VoteBoxEventListener l) {
        l.uploadCastBallots(this);
    }

    public int getSerial() {
        return this.serial;
    }


}
