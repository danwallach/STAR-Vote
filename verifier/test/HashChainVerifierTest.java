package verifier.test;

import auditorium.IncorrectFormatException;
import junit.framework.TestCase;
import verifier.HashChainCompromisedException;
import verifier.Verifier;
import verifier.auditoriumverifierplugins.AuditoriumLog;
import verifier.auditoriumverifierplugins.HashChainVerifier;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Matt Bernhard
 */
public class HashChainVerifierTest extends TestCase {


    /** The verifier we will use to run the plugin */
    Verifier v;

    /** The plugin itself */
    AuditoriumLog auditoriumLog;
    HashChainVerifier hashChainVerifier;

    /** Arguments to the verifier, such as log location */
    HashMap<String, String> args = new HashMap<>();

    protected void setUp() throws Exception {
        super.setUp();

        AuditoriumLogGenerator.setUp("temp");


        auditoriumLog = new AuditoriumLog();
        hashChainVerifier = new HashChainVerifier();

        args.put("log", "temp");

        v = new Verifier(args);
        auditoriumLog.init(v);
        hashChainVerifier.init(v);
    }

    /**
     * Test a simple case with one hashed message
     */
    public void testSimpleHash(){
        try {
            AuditoriumLogGenerator.generateSimpleLog();

            hashChainVerifier.verify();

        } catch (IOException | IncorrectFormatException | HashChainCompromisedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * test a relatively simple case with several hashed messages
     */
    public void testSimpleSupervisorHash(){
        try {
            AuditoriumLogGenerator.generateSimpleSupervisorLog();

            hashChainVerifier.verify();

        } catch (IOException | IncorrectFormatException | HashChainCompromisedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * test a relatively simple case with several hashed messages, where one was invalid
     */
    public void testBadSimpleSupervisorHash(){
        try {
            AuditoriumLogGenerator.generateSimpleSupervisorCompromisedHashLog();

            hashChainVerifier.verify();
            fail("expected exception!");

        } catch (IOException | IncorrectFormatException e) {
            fail(e.getMessage());
        } catch (HashChainCompromisedException e) {
            //Expected
        }
    }
}
