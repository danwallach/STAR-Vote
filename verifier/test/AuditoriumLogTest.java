package verifier.test;

import auditorium.IncorrectFormatException;
import junit.framework.TestCase;
import sexpression.ASExpression;
import verifier.Verifier;
import verifier.auditoriumverifierplugins.AuditoriumLog;

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

        AuditoriumLogGenerator.setUp("temp");


        auditoriumLog = new AuditoriumLog();

        args.put("log", "temp");

        v = new Verifier(args);
        auditoriumLog.init(v);
    }

    public void testSimpleLogVoting2Rules() {
        ASExpression rule;

        try {
            AuditoriumLogGenerator.generateSimpleLog();
            rule = Verifier.readRule("rules/voting2.rules");
        } catch (IncorrectFormatException | IOException e) {
            fail(e.getMessage());
            return;
        }

        v.eval(rule);
    }

    public void testSimpleSupervisorLogVoting2Rules() {
        ASExpression rule;

        try {
            AuditoriumLogGenerator.generateSimpleSupervisorLog();
            rule = Verifier.readRule("rules/voting2.rules");
        } catch (IncorrectFormatException | IOException e) {
            fail(e.getMessage());
            return;
        }

        v.eval(rule);
    }

}
