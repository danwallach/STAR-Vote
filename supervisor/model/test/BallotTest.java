package supervisor.model.test;

import junit.framework.TestCase;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.Ballot;

import java.util.List;

/**
 * A unit test to verify the functionality of the Ballot object.
 *
 * @author Matt Bernhard
 */
public class BallotTest extends TestCase {

    private Ballot ballot;

    protected void setUp() throws Exception {
        super.setUp();
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
    public void testToASESimple(){
        ASExpression bal = getBlob();
        ASExpression nonce = getBlob();

        ballot = new Ballot("0", bal, nonce);

        ListExpression l = new ListExpression(StringExpression.make("0"), bal, nonce);


        assert(l.equals(ballot.toListExpression()));
    }

    /**
     * @see Ballot#toListExpression(String)
     */
    public void testToASEPrecinct(){
        ASExpression bal = getBlob();
        ASExpression nonce = getBlob();

        String precinct = "003";

        ballot = new Ballot("0", bal, nonce);

        ListExpression l = new ListExpression(StringExpression.make(precinct), StringExpression.make("0"), bal, nonce);

        assert(l.equals(ballot.toListExpression(precinct)));
    }
}
