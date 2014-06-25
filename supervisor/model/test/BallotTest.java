package supervisor.model.test;

import auditorium.SimpleKeyStore;
import crypto.adder.AdderInteger;
import crypto.adder.ElgamalCiphertext;
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
 * A unit test to verify the functionality of the Ballot object.
 *
 * @author Matt Bernhard
 */
public class BallotTest extends TestCase {

    private Ballot ballot;

    private PublicKey publicKey;

    protected void setUp() throws Exception {
        super.setUp();

        SimpleKeyStore ks = new SimpleKeyStore("keys");
        publicKey = (PublicKey) ks.loadAdderKey("public");
    }

    /**
     * @return a random SExpression
     */
    public ASExpression getBlob() {
        int n = (int) (Math.random() * 100);
        byte[] array = new byte[n];
        for (int i = 0; i < n; i++)
            array[i] = (byte) (Math.random() * 256);

        return StringExpression.makeString(array);
    }

    /**
     * @see Ballot#toListExpression()
     */
    public void testToASE(){

        ASExpression nonce = getBlob();

        List<ElgamalCiphertext> ctexts = new ArrayList<>();

        ctexts.add(publicKey.encryptPoly(AdderInteger.ZERO));
        ctexts.add(publicKey.encryptPoly(AdderInteger.ONE));

        Vote vote = new Vote(ctexts);

        List<Vote> votes = new ArrayList<>();
        votes.add(vote);

        ballot = new Ballot("0", votes, nonce, publicKey);

        ListExpression l = new ListExpression(StringExpression.make("ballot"), StringExpression.make("0"), new ListExpression(vote.toASE()), nonce, publicKey.toASE());

        assertEquals(l.toString(), ballot.toListExpression().toString());
    }

    /**
     * @see Ballot#fromASE(ASExpression)
     */
    public void testFromASE(){

        ASExpression nonce = getBlob();

        List<ElgamalCiphertext> ctexts = new ArrayList<>();

        ctexts.add(publicKey.encryptPoly(AdderInteger.ZERO));
        ctexts.add(publicKey.encryptPoly(AdderInteger.ONE));

        Vote vote = new Vote(ctexts);

        List<Vote> votes = new ArrayList<>();
        votes.add(vote);

        ballot = new Ballot("0", votes, nonce, publicKey);

        ListExpression l = ballot.toListExpression();

        System.out.println(l);

        Ballot newBallot = Ballot.fromASE(l);

        assertEquals(ballot.toListExpression().toString(), newBallot.toListExpression().toString());
    }



}
