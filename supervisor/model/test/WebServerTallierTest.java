package supervisor.model.test;

import auditorium.SimpleKeyStore;
import crypto.BallotEncrypter;
import crypto.adder.*;
import crypto.interop.AdderKeyManipulator;
import junit.framework.TestCase;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.Ballot;
import supervisor.model.WebServerTallier;

import java.math.BigInteger;
import java.util.*;

/**
 * A test suite for the WebServerTallier class
 *
 * @author  Matthew Kindy II
 * 6/30/2014
 */
public class WebServerTallierTest extends TestCase {

    private BallotEncrypter be;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    protected void setUp() throws Exception {
        super.setUp();
        be = BallotEncrypter.SINGLETON;

        SimpleKeyStore keyStore = new SimpleKeyStore("keys");
        publicKey = keyStore.loadAdderPublicKey();
        privateKey = keyStore.loadAdderPrivateKey();
    }

    /**
     * This is to test that a series of Ballots are tallied properly into one Ballot
     */
    public void testTally(){

        PublicKey finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(publicKey);

        /* ((B0 0)(B1 0)(B2 1)...) */
        List<ASExpression> singleVote = new ArrayList<>();

        /* Load up singleVote with pattern (1 0 0 0 0 1 0 0 0 0) */
        for (int i=0; i<10; i++) {

            int s = (i%5)==0 ? 1 : 0;
            singleVote.add(new ListExpression("B" + i, "" + s));
        }

        List<ASExpression> ballotASEList = new ArrayList<>();

        /* Create a new set of race groups */
        List<List<String>> raceGroups = new ArrayList<>();

        /* Set up two groups in this ballot [B0 B1 B2 B3 B4] [B5 B6 B7 B8 B9] */
        for (int i=0; i<2; i++) {

            /* Set up for the next group */
            List<String> groupList = new ArrayList<>();

            for (int j = 0; j < 5; j++)
                groupList.add("B"+ (5*i+j));

            /* Add this group to the raceGroups */
            raceGroups.add(groupList);
        }

        /* Encrypt 10 new ballotsASEs (all identical) */
        for (int i=0;i<10;i++) {

            ListExpression ballot = new ListExpression(singleVote);
            ballotASEList.add(be.encryptWithProof("00"+i, ballot, raceGroups, publicKey, ASExpression.make("nonce")));
        }

        List<Ballot> ballotList = new ArrayList<>();

        /* Fill the ballotList with 5 of the ASEs converted to Ballots */
        for (int i=0; i<5; i++)
            ballotList.add(Ballot.fromASE(ballotASEList.get(i)));

        /* Tally them */
        Ballot summed = WebServerTallier.tally("TEST1", ballotList, finalPublicKey);

        /* Get the vote totals */
        TreeMap<String, BigInteger> voteTotals = WebServerTallier.getVoteTotals(summed, 5, publicKey, privateKey);

        /* (Sanity) Check the BID and the public key */
        assertEquals(summed.getBid(), "TEST1");
        assertEquals(summed.getPublicKey(), finalPublicKey);

        /* Compare the decrypted totals with the expected totals */
        for (Map.Entry<String, BigInteger> entry : voteTotals.entrySet()) {
            int toCompare = entry.getKey().equals("B0") || entry.getKey().equals("B5") ? 5 : 0;
            assertEquals(entry.getValue().intValue(), toCompare);
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    /**
     * This is to test that a single Ballot is "tallied" properly into one Ballot
     */
    public void testTallySingle(){

        PublicKey finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(publicKey);

        /* ((B0 0)(B1 0)(B2 1)...) */
        List<ASExpression> singleVote = new ArrayList<>();

        /* Load up singleVote with pattern (0 1 0 0 0 0 1 0 0 0) */
        for (int i=0; i<10; i++) {

            int s = (i==1 || i==6) ? 1 : 0;

            /* So each of these is (B0 1), (B1 0), etc. */
            singleVote.add(new ListExpression("B" + i, "" + s));
        }

        /* Create a new set of race groups */
        List<List<String>> raceGroups = new ArrayList<>();

        /* Set up two groups in this ballot [B0 B1 B2 B3 B4] [B5 B6 B7 B8 B9] */
        for (int i=0; i<2; i++) {

            /* Set up for the next group */
            List<String> groupList = new ArrayList<>();

            for (int j = 0; j < 5; j++)
                groupList.add("B"+ (5*i+j));

            /* Add this group to the raceGroups */
            raceGroups.add(groupList);
        }

        /* Encrypt 1 new ballotsASE */
        ListExpression ballot = new ListExpression(singleVote);

        System.out.println("-----PRE  ENCRYPTION-----");
        ASExpression ballotASE = be.encryptWithProof("000", ballot, raceGroups, publicKey, ASExpression.make("nonce"));
        System.out.println("-----POST ENCRYPTION-----");

        List<Ballot> ballotList = new ArrayList<>();

        Ballot toTally = Ballot.fromASE(ballotASE);

        /* Fill the ballotList with the ASE converted to Ballot */
        ballotList.add(toTally);

        /* Tally them */
        Ballot summed = WebServerTallier.tally("TEST1", ballotList, finalPublicKey);

        /* For testing */
        List<AdderInteger> choices = new ArrayList<>();
        Vote total = toTally.getVotes().get(0);

        /* For testing: set this to what the totalled vote should be */
        choices.add(AdderInteger.ZERO);
        choices.add(AdderInteger.ONE);
        choices.add(AdderInteger.ZERO);
        choices.add(AdderInteger.ZERO);
        choices.add(AdderInteger.ZERO);

        System.out.println("-----------------");
        System.out.println("In WebserverTallierTest.testTallySingle() -- Testing single vote computed VoteProof, vote 1: ");
        VoteProof vp = new VoteProof();
        vp.compute(total, finalPublicKey, choices, 0, 1);

        System.out.println("In WebserverTallierTest.testTallySingle() -- Constructing new Vote with choices: " + total.getChoices());

        //[This doesn't verify!] because of no R value:
        Vote test1 = new Vote(total.getCipherList(), total.getChoices(), vp);
        Vote test = new Vote(total);

        System.out.println("In WebserverTallierTest.testTallySingle() -- Single vote computed VoteProof verified: " + test.verifyVoteProof(finalPublicKey, 0, 1));

        System.out.println("-----------------");
        System.out.println("In WebserverTallierTest.testTallySingle() -- Testing single vote computed VoteProof, vote 2: ");

        total = toTally.getVotes().get(1);

        //vp = new VoteProof();
        //vp.compute(total, publicKey, choices, 0, 1);

        System.out.println("In WebserverTallierTest.testTallySingle() -- Constructing new Vote with choices: " + total.getChoices());

        //[This doesn't verify!]:
        // test = new Vote(total.getCipherList(), total.getChoices(), vp);
        test = new Vote(total);

        System.out.println("In WebserverTallierTest.testTallySingle() -- Single vote computed VoteProof verified: " + test.verifyVoteProof(finalPublicKey, 0, 1));
        System.out.println("-----------------");

        /* Get the vote totals */
        TreeMap<String, BigInteger> voteTotals = WebServerTallier.getVoteTotals(summed, 1, publicKey, privateKey);

        /* (Sanity) Check the BID and the public key */
        assertEquals(summed.getBid(), "TEST1");
        assertEquals(summed.getPublicKey(), finalPublicKey);

        /* Compare the decrypted totals with the expected totals */
        for (Map.Entry<String, BigInteger> entry : voteTotals.entrySet()) {
            int toCompare = entry.getKey().equals("B1") || entry.getKey().equals("B6") ? 1 : 0;
            assertEquals(entry.getValue().intValue(), toCompare);
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

    }

    /**
     * This is to test that a single challenged Ballot is decrypted properly
     */
    public void testDecrypt(){

        PublicKey finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(publicKey);
        PrivateKey finalPrivateKey = AdderKeyManipulator.generateFinalPrivateKey(publicKey, privateKey);


        /* ((B0 0)(B1 0)(B2 1)...) */
        List<ASExpression> singleVote = new ArrayList<>();

        /* Load up singleVote with pattern (0 1 0 0 0 0 1 0 0 0) */
        for (int i=0; i<10; i++) {

            int s = (i==1 || i==6) ? 1 : 0;

            /* So each of these is (B0 1), (B1 0), etc. */
            singleVote.add(new ListExpression("B" + i, "" + s));
        }

        /* Create a new set of race groups */
        List<List<String>> raceGroups = new ArrayList<>();

        /* Set up two groups in this ballot [B0 B1 B2 B3 B4] [B5 B6 B7 B8 B9] */
        for (int i=0; i<2; i++) {

            /* Set up for the next group */
            List<String> groupList = new ArrayList<>();

            for (int j = 0; j < 5; j++)
                groupList.add("B"+ (5*i+j));

            /* Add this group to the raceGroups */
            raceGroups.add(groupList);
        }

        /* Encrypt 1 new ballotsASE */
        ListExpression ballot = new ListExpression(singleVote);

        System.out.println("-----PRE  ENCRYPTION-----");
        ASExpression ballotASE = be.encryptWithProof("000", ballot, raceGroups, publicKey, ASExpression.make("nonce"));
        System.out.println("-----POST ENCRYPTION-----");

        Ballot toDecrypt = Ballot.fromASE(ballotASE);

        List<String> decrypted = WebServerTallier.decrypt(toDecrypt, finalPublicKey, finalPrivateKey);
        List<String> expected = new ArrayList<>();

        expected.add("B1");
        expected.add("B6");

        assertEquals(decrypted, expected);
        System.out.println("Selected: "+decrypted);
    }

    /**
     * This is to test that a series of challenged Ballots are decrypted properly
     */
    public void testDecryptAll(){

        PublicKey finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(publicKey);
        PrivateKey finalPrivateKey = AdderKeyManipulator.generateFinalPrivateKey(publicKey, privateKey);
        Random r = new Random();

        List<List<String>> expected = new ArrayList<>();
        List<Ballot> allBallots = new ArrayList<>();

        for(int j=0; j<100; j++) {

            /* ((B0 0)(B1 0)(B2 1)...) */
            List<ASExpression> singleVote = new ArrayList<>();

            int first = r.nextInt(5);
            int second = 5 + r.nextInt(5);

            expected.add(new ArrayList<String>());
            expected.get(j).add("B" + first);
            expected.get(j).add("B" + second);

            /* Load up singleVote with pattern (0 1 0 0 0 0 1 0 0 0) */
            for (int i = 0; i < 10; i++) {

                int s = (i == first || i == second) ? 1 : 0;

                /* So each of these is (B0 1), (B1 0), etc. */
                singleVote.add(new ListExpression("B" + i, "" + s));
            }

            /* Create a new set of race groups */
            List<List<String>> raceGroups = new ArrayList<>();

            /* Set up two groups in this ballot [B0 B1 B2 B3 B4] [B5 B6 B7 B8 B9] */
            for (int i = 0; i < 2; i++) {

                /* Set up for the next group */
                List<String> groupList = new ArrayList<>();

                for (int k = 0; k < 5; k++)
                    groupList.add("B" + (5 * i + k));

                /* Add this group to the raceGroups */
                raceGroups.add(groupList);
            }

            /* Encrypt 1 new ballotsASE */
            ListExpression ballot = new ListExpression(singleVote);

            System.out.println("-----PRE  ENCRYPTION-----");
            ASExpression ballotASE = be.encryptWithProof("000", ballot, raceGroups, publicKey, ASExpression.make("nonce"));
            System.out.println("-----POST ENCRYPTION-----");

            Ballot toDecrypt = Ballot.fromASE(ballotASE);

            allBallots.add(toDecrypt);

        }

        List<List<String>> decrypted = WebServerTallier.decryptAll(allBallots, finalPublicKey, finalPrivateKey);
        assertEquals(decrypted, expected);

        for(List<String> ballot : decrypted)
            System.out.println("Selected: " + ballot);

    }

    /**
     * This is to test that the mapping of candidates to totals is done properly after tallying
     */
    public void testGetVoteTotals(){

        /* ((B0 0)(B1 0)(B2 1)...) */
        List<ASExpression> singleVote = new ArrayList<>();

        /* Load up singleVote with pattern (1 0 0 0 0 1 0 0 0 0) */
        for (int i=0; i<10; i++) {

            int s = (i%5)==0 ? 1 : 0;

            /* So each of these is (B0 0), (B1 1), etc. */
            singleVote.add(new ListExpression("B" + i, "" + s));
        }

        /* Create a new set of race groups */
        List<List<String>> raceGroups = new ArrayList<>();

        /* Set up two groups in this ballot [B0 B1 B2 B3 B4] [B5 B6 B7 B8 B9] */
        for (int i=0; i<2; i++) {

            /* Set up for the next group */
            List<String> groupList = new ArrayList<>();

            for (int j = 0; j < 5; j++)
                groupList.add("B"+ (5*i+j));

            /* Add this group to the raceGroups */
            raceGroups.add(groupList);
        }

        /* Encrypt 1 new ballotsASE */
        ListExpression ballot = new ListExpression(singleVote);
        ASExpression ballotASE = be.encryptWithProof("000", ballot, raceGroups, publicKey, ASExpression.make("nonce"));

        /* Get the vote "totals" */
        Map<String, BigInteger> voteTotals = WebServerTallier.getVoteTotals(Ballot.fromASE(ballotASE), 5, publicKey, privateKey);

        /* Compare the decrypted totals with the expected totals */
        for (Map.Entry<String, BigInteger> entry : voteTotals.entrySet()) {
            int toCompare = entry.getKey().equals("B0") || entry.getKey().equals("B5") ? 1 : 0;
            assertEquals(entry.getValue().intValue(), toCompare);
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

    }

    /**
     * This is to test many times over that mapping vote totals to candidates is working properly under many random scenarios
     */
    public void testGetLotsOfVoteTotals(){

        /* Try this 100 times */
        for (int trial=0; trial<100; trial++) {

            List<String> choiceList = new ArrayList<>();

            /* ((B0 0)(B1 0)(B2 1)...) */
            List<ASExpression> singleVote = new ArrayList<>();

            /* Set the default group and choice */
            int choice = -1;
            int group = 1;

            /* Load up singleVote with pattern (0 1 0 0 0 1 0 0 0 0) */
            for (int i = 0; i < 10; i++) {

                Random rand = new Random();

                /* Reset the choice and set the group */
                if (i == 5) {
                    choice = -1;
                    group = 2;
                }

                /* Set choice [0,5), [5,10) */
                if (choice == -1) {
                    choice = rand.nextInt(5) + (group - 1) * 5;
                    choiceList.add("B"+choice);
                }

                /* Check the choice for the current group */
                int s = i == choice ? 1 : 0;

                /* So each of these is (B0 0), (B1 1), etc. */
                singleVote.add(new ListExpression("B" + i, "" + s));
            }

            /* Create a new set of race groups */
            List<List<String>> raceGroups = new ArrayList<>();

            /* Set up two groups in this ballot [B0 B1 B2 B3 B4] [B5 B6 B7 B8 B9] */
            for (int i = 0; i < 2; i++) {

                /* Set up for the next group */
                List<String> groupList = new ArrayList<>();

                for (int j = 0; j < 5; j++)
                    groupList.add("B" + (5*i+j));

                /* Add this group to the raceGroups */
                raceGroups.add(groupList);
            }

            /* Encrypt 1 new ballotASE */
            ListExpression ballot = new ListExpression(singleVote);
            ASExpression ballotASE = be.encryptWithProof("000", ballot, raceGroups, publicKey, ASExpression.make("nonce"));

            /* Get the vote "totals" */
            Map<String, BigInteger> voteTotals = WebServerTallier.getVoteTotals(Ballot.fromASE(ballotASE), 1, publicKey, privateKey);

            /* Compare the decrypted totals with the expected totals */
            for (Map.Entry<String, BigInteger> entry : voteTotals.entrySet()) {
                int toCompare = choiceList.contains(entry.getKey()) ? 1 : 0;
                assertEquals(entry.getValue().intValue(), toCompare);
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
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
