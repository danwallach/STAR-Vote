package verifier.test;

import junit.framework.TestCase;
import sexpression.ASExpression;
import verifier.Verifier;
import verifier.auditoriumverifierplugins.AuditoriumLog;

import java.io.FileNotFoundException;
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
        AuditoriumLogGenerator.generateSimpleLog();

        auditoriumLog = new AuditoriumLog();

        args.put("log", "temp");

        v = new Verifier(args);
        auditoriumLog.init(v);
    }

    public void testSimpleRules() {
        ASExpression rule;

        try {
             rule = Verifier.readRule("rules/empty.rules");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        v.eval(rule);

    }

}
