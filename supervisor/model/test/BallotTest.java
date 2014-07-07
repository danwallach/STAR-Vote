package supervisor.model.test;

import auditorium.SimpleKeyStore;
import crypto.adder.*;
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

        List<ASExpression> choices = new ArrayList<>();
        choices.add(StringExpression.makeString("B0"));
        choices.add(StringExpression.makeString("B1"));

        Vote vote = new Vote(ctexts, choices);

        List<Vote> votes = new ArrayList<>();
        List<AdderInteger> c = new ArrayList<>();

        c.add(AdderInteger.ZERO);
        c.add(AdderInteger.ONE);


        VoteProof proof = new VoteProof();
        proof.compute(vote, publicKey, c, 0, 1);

        vote = new Vote(ctexts, choices, proof);

        votes.add(vote);



        vote.verifyVoteProof(publicKey, 0, 1);

        ballot = new Ballot("0", votes, nonce, publicKey);

        ListExpression l = new ListExpression(StringExpression.make("ballot"), StringExpression.make("0"), new ListExpression(vote.toASE()), nonce, publicKey.toASE());

        assertEquals(l, ballot.toListExpression());
    }

    /**
     * @see Ballot#fromASE(ASExpression)
     */
    public void testFromASE(){

        ASExpression nonce = getBlob();

        List<ElgamalCiphertext> ctexts = new ArrayList<>();

        ctexts.add(publicKey.encryptPoly(AdderInteger.ZERO));
        ctexts.add(publicKey.encryptPoly(AdderInteger.ONE));

        List<ASExpression> choices = new ArrayList<>();
        choices.add(StringExpression.makeString("B0"));
        choices.add(StringExpression.makeString("B1"));

        List<AdderInteger> c = new ArrayList<>();

        Vote vote = new Vote(ctexts, choices);

        c.add(AdderInteger.ZERO);
        c.add(AdderInteger.ONE);

        VoteProof proof = new VoteProof();
        proof.compute(vote, publicKey, c, 0, 1);

        vote = new Vote(ctexts, choices, proof);

        List<Vote> votes = new ArrayList<>();
        votes.add(vote);

        ballot = new Ballot("0", votes, nonce, publicKey);

        ListExpression l = ballot.toListExpression();

        Ballot newBallot = Ballot.fromASE(l);

        ListExpression newL = newBallot.toListExpression();
        ListExpression oldL = ballot.toListExpression();

        assertEquals(oldL, newL);
    }



}
