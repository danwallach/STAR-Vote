package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
import sexpression.StringExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Event representing uploading of cast ballots to post-election audit server
 * These are cast ballots, so only ballot hashes are sent
 *
 * @author Nelson Chen
 *         Date: 12/4/12
 *
 */
public class CastBallotUploadEvent extends AAnnounceEvent {
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

    /**
     * used to retrieve all nonces that must be published as a result of the election
     * @return a collection of nonces of cast ballots
     */
    public ArrayList<String> getDumpList() {
        ListExpression ballotList = (ListExpression)_nonces;
        Iterator<ASExpression> iterator = ballotList.iterator();
        ASExpression[] ballotIDs = ((ListExpression) iterator.next()).getArray();
        ASExpression[] precincts = ((ListExpression) iterator.next()).getArray();
        ASExpression[] nonceList = ((ListExpression) iterator.next()).getArray();
        ArrayList<String> dumpList = new ArrayList<String>();

        for (int i=0; i<ballotIDs.length; i++) {
            dumpList.add("cast:" + ballotIDs[i] + ":" + precincts[i] + ":" + nonceList[i].toString());
        }
        return dumpList;
    }


    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("cast-ballot-upload"),
                _nonces);
    }

    public void fire(VoteBoxEventListener l) {
        l.uploadCastBallots(this);
    }

    public int getSerial() {
        return this.serial;
    }


}
