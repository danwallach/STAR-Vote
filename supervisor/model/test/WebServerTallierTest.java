package supervisor.model.test;

import auditorium.SimpleKeyStore;
import crypto.BallotEncrypter;
import crypto.adder.PublicKey;
import crypto.adder.Vote;
import junit.framework.TestCase;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.Ballot;

import java.util.ArrayList;
import java.util.List;

/**
 * A test suite for the WebServerTallier class
 *
 * @author  Matthew Kindy II
 * 6/30/2014
 */
public class WebServerTallierTest extends TestCase {

    private BallotEncrypter be;

    protected void setUp() throws Exception {
        super.setUp();
        be = BallotEncrypter.SINGLETON;
    }

    /**
     * This is to test that a series of Ballots are tallied properly into one Ballot
     */
    public void testTally(){

        SimpleKeyStore keyStore = new SimpleKeyStore("keys");
        PublicKey publicKey = (PublicKey)keyStore.loadAdderKey("public");

        List<Vote> voteList = new ArrayList<>();

        List<ASExpression> ballotASEList = new ArrayList<>();

        for (int i=0;i<10;i++) {

            ListExpression ballot = new ListExpression("");
            List<List<String>> raceGroups = new ArrayList<>();
            ballotASEList.add(be.encryptWithProof("00"+i, ballot, raceGroups, publicKey, ASExpression.make("nonce")));

        }

        List<Ballot> ballotList = new ArrayList<>();

        /* Fill the ballotList with Ballots */
        for (int i=0; i<5; i++)
            ballotList.add(Ballot.fromASE(ballotASEList.get(i)));

    }

    /**
     *
     */
    public void testTallySingle(){

    }

    /**
     *
     */
    public void testDecrypt(){

    }

    /**
     *
     */
    public void testDecryptAll(){

    }

    /**
     *
     */
    public void testGetVoteTotals(){

    }

    /**
     *
     */
    public void testGetLotsOfVoteTotals(){

    }

    /**
     * @return a random SExpression
     */
    private ASExpression getBlob() {
        int n = (int) (Math.random() * 100);
        byte[] array = new byte[n];
        for (int i = 0; i < n; i++)
            array[i] = (byte) (Math.random() * 256);

        return StringExpression.makeString(array);
    }

}
