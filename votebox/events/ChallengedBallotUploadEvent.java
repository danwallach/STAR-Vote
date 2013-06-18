package votebox.events;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;

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
public class ChallengedBallotUploadEvent implements IAnnounceEvent {
    private int serial;
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


    public ChallengedBallotUploadEvent(int serial, ASExpression challengedBallots) {
        this.serial = serial;
        this.challengedBallots = challengedBallots;
    }

    public ArrayList<String> getDumpList() {
        ListExpression ballotList = (ListExpression) challengedBallots;
        ArrayList<String> dumpList = new ArrayList<String>();

        Iterator<ASExpression> iterator = ballotList.iterator();
        ASExpression[] encryptedBallots = ((ListExpression) iterator.next()).getArray();
        ASExpression[] decryptedBallots = ((ListExpression) iterator.next()).getArray();

        for (int i = 0; i < encryptedBallots.length; i ++) {
            String ballotString = "chall:" + encryptedBallots[i].toString() + ";" + decryptedBallots[i].toString() + "\n";
            dumpList.add(ballotString);
        }
        return dumpList;
    }


    public ASExpression toSExp() {
        return challengedBallots;
    }

    public void fire(VoteBoxEventListener l) {
        l.uploadChallengedBallots(this);
    }

    public int getSerial() {
        return this.serial;
    }


}
