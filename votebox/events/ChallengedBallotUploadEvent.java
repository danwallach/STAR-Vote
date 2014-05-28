package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
import sexpression.StringExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Event representing uploading of challenged ballots to post-election audit server
 * These are challenged ballots, so both the ballot hashes and decrypted ballots are sent
 *
 * @author Nelson Chen
 *         Date: 12/4/12
 */
public class ChallengedBallotUploadEvent extends AAnnounceEvent {

    private ASExpression challengedBallots;


    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(chall-ballot-upload %ballots:#any)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON) {
                return new ChallengedBallotUploadEvent(serial, result.get("ballots"));
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


    public ChallengedBallotUploadEvent(int serial, ASExpression challengedBallots) {
        super(serial);
        this.challengedBallots = challengedBallots;
    }

    /**
     * Packages ballot encryptions as well as the plaintext, precincts, and ballot ids of ballots into an array of
     * List Expressions
     *
     * @return A list of Strings representing list expressiong of ballot IDs, precincts, encryptedBallots, and
     *         decryptedBallots
     */
    public ArrayList<String> getDumpList() {
        ListExpression ballotList = (ListExpression) challengedBallots;
        ArrayList<String> dumpList = new ArrayList<String>();

        Iterator<ASExpression> iterator = ballotList.iterator();
        ASExpression[] ballotIDs = ((ListExpression) iterator.next()).getArray();
        ASExpression[] precincts = ((ListExpression) iterator.next()).getArray();
        ASExpression[] encryptedBallots = ((ListExpression) iterator.next()).getArray();
        ASExpression[] decryptedBallots = ((ListExpression) iterator.next()).getArray();

        for (int i = 0; i < encryptedBallots.length; i ++) {
            String ballotString = "chall:" + ballotIDs[i] + ":" + precincts[i] + ":" + encryptedBallots[i].toString() + ";" + decryptedBallots[i].toString() + "\n";
            dumpList.add(ballotString);
        }
        return dumpList;
    }

    /** @see votebox.events.IAnnounceEvent#fire(VoteBoxEventListener) */
    public void fire(VoteBoxEventListener l) {
        l.uploadChallengedBallots(this);
    }

    /** @see votebox.events.IAnnounceEvent#toSExp() */
    public ASExpression toSExp() {
        return new ListExpression(StringExpression.makeString("chall-ballot-upload"),
                challengedBallots);
    }


}
