package verifier.test;

import auditorium.IncorrectFormatException;
import junit.framework.TestCase;
import sexpression.ASExpression;
import verifier.Verifier;
import verifier.auditoriumverifierplugins.AuditoriumLog;
import verifier.value.True;

import java.io.IOException;
import java.util.HashMap;

/**
 * A test used to check the AuditoriumLog plugin to the Verifier
 *
 * @author Matt Bernhard
 */
public class AuditoriumLogTest extends TestCase {

    /** The verifier we will use to run the plugin */
    Verifier v;

    /** The plugin itself */
    AuditoriumLog auditoriumLog;

    /** Arguments to the verifier, such as log location */
    HashMap<String, String> args = new HashMap<>();

    protected void setUp() throws Exception {
        super.setUp();

        auditoriumLog = new AuditoriumLog();

        args.put("log", "test.out");

        v = new Verifier(args);

    }

    public void testSimpleLogVoting() {
        ASExpression rule;

        AuditoriumLogGenerator.setUp();

        try {
            AuditoriumLogGenerator.generateSimpleLog();
            rule = Verifier.readRule("rules/STARVoting.rules");
        } catch (IncorrectFormatException | IOException e) {
            fail(e.getMessage());
            return;
        }

        auditoriumLog.init(v);
        assertEquals(True.SINGLETON, v.eval(rule));

    }

    public void testSimpleSupervisorLog() {
        ASExpression rule;

        AuditoriumLogGenerator.setUp();


        try {
            AuditoriumLogGenerator.generateSimpleSupervisorLog();
            rule = Verifier.readRule("rules/STARVoting.rules");
        } catch (IncorrectFormatException | IOException e) {
            fail(e.getMessage());
            return;
        }

        auditoriumLog.init(v);
        assertEquals(True.SINGLETON, v.eval(rule));

    }


    public void testSimpleSupervisorLogMoreVotes() {
        ASExpression rule;

        AuditoriumLogGenerator.setUp();


        try {
            AuditoriumLogGenerator.generateLotsOfVotesLog();
            rule = Verifier.readRule("rules/STARVoting.rules");
        } catch (IncorrectFormatException | IOException e) {
            fail(e.getMessage());
            return;
        }

        auditoriumLog.init(v);
        assertEquals(True.SINGLETON, v.eval(rule));

    }



}
