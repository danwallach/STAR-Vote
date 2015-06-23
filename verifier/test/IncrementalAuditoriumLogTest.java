package verifier.test;

import auditorium.IncorrectFormatException;
import junit.framework.TestCase;
import sexpression.ASExpression;
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
public class IncrementalAuditoriumLogTest extends TestCase {

    /** The verifier we will use to run the plugin */
    Verifier v;

    /** The plugin itself */
    IncrementalAuditoriumLog incAuditoriumLog;
    AuditoriumLog auditoriumLog;

    /** Arguments to the verifier, such as log location */
    HashMap<String, String> args = new HashMap<>();

    protected void setUp() throws Exception {
        super.setUp();

        HashChainVerifier hash = new HashChainVerifier();
        incAuditoriumLog = new IncrementalAuditoriumLog(hash);
        auditoriumLog = new AuditoriumLog();

        args.put("log", "test.out");

        v = new Verifier(args);

        incAuditoriumLog.init(v);

    }

    private void assertGood(Value value) {
        assertNotSame(False.SINGLETON, value);
    }


    public void testIncrementalVoting() {

        ASExpression rule;
        IncrementalAuditoriumLogGenerator.setUp(incAuditoriumLog);

        try {
            rule = Verifier.readRule("rules/STARVotingIncremental.rules");

            IncrementalAuditoriumLogGenerator.start3Machines();

            assertGood(v.eval(rule));

            IncrementalAuditoriumLogGenerator.vote();

            assertGood(v.eval(rule));

            IncrementalAuditoriumLogGenerator.vote();

            assertGood(v.eval(rule));

            IncrementalAuditoriumLogGenerator.close();

            assertGood(v.eval(rule));

        } catch (IOException | InvalidLogEntryException e) {
            e.printStackTrace();
            return;
        }

        assertGood(v.eval(rule));

    }

    public void testSimpleLog() {
        ASExpression rule, incRule;

        IncrementalAuditoriumLogGenerator.setUp(incAuditoriumLog);

        try {
            rule = Verifier.readRule("rules/STARVoting.rules");
            incRule = Verifier.readRule("rules/STARVotingIncremental.rules");

            IncrementalAuditoriumLogGenerator.start3Machines();
            assertGood(v.eval(incRule));

            SupervisorEvent e = new SupervisorEvent(0, 0, "activated");

            IncrementalAuditoriumLogGenerator.logDatum(e.toSExp());

            IncrementalAuditoriumLogGenerator.close();

            assertGood(v.eval(incRule));

        } catch ( IOException | InvalidLogEntryException e) {
            fail(e.getMessage());
            return;
        }

        assertGood(v.eval(rule));

    }

    public void testSimpleSupervisorLog() {
        ASExpression rule, incRule;

        IncrementalAuditoriumLogGenerator.setUp(incAuditoriumLog);

        try {
            rule = Verifier.readRule("rules/STARVoting.rules");
            IncrementalAuditoriumLogGenerator.generateSimpleSupervisorLog();
            incRule = Verifier.readRule("rules/STARVotingIncremental.rules");
            assertGood(v.eval(incRule));
        } catch (IncorrectFormatException | IOException | InvalidLogEntryException e) {
            fail(e.getMessage());
            return;
        }

        assertGood(v.eval(rule));


    }


    public void testSimpleSupervisorLogMoreVotes() {
        ASExpression rule, incRule;

        IncrementalAuditoriumLogGenerator.setUp(incAuditoriumLog);

        try {
            rule = Verifier.readRule("rules/STARVoting.rules");
            IncrementalAuditoriumLogGenerator.generateLotsOfVotesLog();
            incRule = Verifier.readRule("rules/STARVotingIncremental.rules");
            assertGood(v.eval(incRule));
        } catch (IncorrectFormatException | IOException | InvalidLogEntryException e) {
            fail(e.getMessage());
            return;
        }

        assertGood(v.eval(rule));


    }

    public void testSimpleSupervisorLogMoreVotesIncremental() {
        ASExpression rule, incRule;

        IncrementalAuditoriumLogGenerator.setUp(incAuditoriumLog);

        try {
            rule = Verifier.readRule("rules/STARVoting.rules");
            incRule = Verifier.readRule("rules/STARVotingIncremental.rules");

            IncrementalAuditoriumLogGenerator.start3Machines();

            for(int i = 0; i < 100; i++) {
                IncrementalAuditoriumLogGenerator.vote();
                if(i%10 == 0)
                    IncrementalAuditoriumLogGenerator.logDatum(new SupervisorEvent(0, 0, "active").toSExp());
                assertGood(v.eval(incRule));
            }

            IncrementalAuditoriumLogGenerator.close();
        } catch (IOException | InvalidLogEntryException e) {
            fail(e.getMessage());
            return;
        }

        assertGood(v.eval(rule));
    }

    public void testSimpleSupervisorIncrementalMoreVotesProvisional() {

        ASExpression rule, incRule;

        IncrementalAuditoriumLogGenerator.setUp(incAuditoriumLog);

        try {
            rule = Verifier.readRule("rules/STARVoting.rules");
            incRule = Verifier.readRule("rules/STARVotingIncremental.rules");

            IncrementalAuditoriumLogGenerator.start3Machines();

            for(int i = 0; i < 100; i++) {
                IncrementalAuditoriumLogGenerator.vote();
                if(i%10 == 0)
                    IncrementalAuditoriumLogGenerator.provisionalVote();
                assertGood(v.eval(incRule));
            }

            IncrementalAuditoriumLogGenerator.close();
        } catch (IOException | InvalidLogEntryException e) {
            fail(e.getMessage());
            return;
        }

        assertGood(v.eval(rule));

    }


}
