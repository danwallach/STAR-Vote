package verifier.test;

import auditorium.IncorrectFormatException;
import junit.framework.TestCase;
import sexpression.ASExpression;
import verifier.HashChainCompromisedException;
import verifier.InvalidLogEntryException;
import verifier.Verifier;
import verifier.auditoriumverifierplugins.AuditoriumLog;
import verifier.auditoriumverifierplugins.HashChainVerifier;
import verifier.auditoriumverifierplugins.IncrementalAuditoriumLog;
import verifier.value.False;
import verifier.value.Value;
import votebox.events.SupervisorEvent;

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
    IncrementalAuditoriumLog incrementalAuditoriumLog;
    HashChainVerifier hashChainVerifier;

    /** Arguments to the verifier, such as log location */
    HashMap<String, String> args = new HashMap<>();

    protected void setUp() throws Exception {
        super.setUp();

        AuditoriumLogGenerator.setUp();

        auditoriumLog = new AuditoriumLog();
        hashChainVerifier = new HashChainVerifier();
        incrementalAuditoriumLog = new IncrementalAuditoriumLog(hashChainVerifier);

        args.put("log", "test.out");

        v = new Verifier(args);
        incrementalAuditoriumLog.init(v);
        IncrementalAuditoriumLogGenerator.setUp(incrementalAuditoriumLog);
    }

    private void assertGood(Value value) {
        assertNotSame(False.SINGLETON, value);
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

    public void testIncrementalSimpleSupervisorHash() {
        ASExpression incRule, rule;

        try {
            incRule = Verifier.readRule("rules/STARVotingIncremental.rules");
            rule = Verifier.readRule("rules/STARVoting.rules");

            IncrementalAuditoriumLogGenerator.start3Machines();

            assertGood(v.eval(incRule));

            IncrementalAuditoriumLogGenerator.vote();

            assertGood(v.eval(incRule));

            IncrementalAuditoriumLogGenerator.close();

            assertGood(v.eval(incRule));

            hashChainVerifier.verify();

            System.out.println("Now verifying the whole log!");

            auditoriumLog.init(v);

            assertGood(v.eval(rule));

            hashChainVerifier.verify();


        } catch (IOException | InvalidLogEntryException | HashChainCompromisedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    public void testIncrementalLotsOfVotes() {
        ASExpression incRule, rule;


        try {
            incRule = Verifier.readRule("rules/STARVotingIncremental.rules");
            rule = Verifier.readRule("rules/STARVoting.rules");


            IncrementalAuditoriumLogGenerator.start3Machines();

            assertGood(v.eval(incRule));

            for(int i = 0; i < 100; i++) {
                IncrementalAuditoriumLogGenerator.vote();
                if(i%10 == 0)
                    IncrementalAuditoriumLogGenerator.logDatum(new SupervisorEvent(0, 0, "active").toSExp());
                assertGood(v.eval(incRule));
            }

            assertGood(v.eval(incRule));

            IncrementalAuditoriumLogGenerator.close();

            assertGood(v.eval(incRule));

            hashChainVerifier.verify();

            auditoriumLog.init(v);

            assertGood(v.eval(rule));

            hashChainVerifier.verify();



        } catch (IOException | InvalidLogEntryException | HashChainCompromisedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
