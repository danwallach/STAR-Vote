package supervisor.model.test;

import auditorium.SimpleKeyStore;
import crypto.BallotEncrypter;
import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import crypto.interop.AdderKeyManipulator;
import junit.framework.TestCase;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.Ballot;
import supervisor.model.WebServerTallier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A test suite for the WebServerTallier class
 *
 * @author  Matthew Kindy II
 * 6/30/2014
 */
public class WebServerTallierTest extends TestCase {

    private BallotEncrypter be;

    protected void setUp() throws Exception {
        super.setUp();
        be = BallotEncrypter.SINGLETON;
    }

    /**
     * This is to test that a series of Ballots are tallied properly into one Ballot
     */
    public void testTally(){

        /* Load up the public and private keys */
        SimpleKeyStore keyStore = new SimpleKeyStore("keys");
        PublicKey publicKey = (PublicKey)keyStore.loadAdderKey("public");
        PrivateKey privateKey = (PrivateKey)keyStore.loadAdderKey("private");

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
        Map<String, BigInteger> voteTotals = WebServerTallier.getVoteTotals(summed, 5, finalPublicKey, privateKey);

        /* (Sanity) Check the BID and the public key */
        assertEquals(summed.getBid(), "TEST1");
        assertEquals(summed.getPublicKey(), finalPublicKey);

        /* Compare the decrypted totals with the expected totals */
        for (Map.Entry<String, BigInteger> entry : voteTotals.entrySet()) {
            int toCompare = entry.getKey().equals("B0") || entry.getKey().equals("B5") ? 5 : 0;
            assertEquals(entry.getValue().intValue(), toCompare);
        }
    }

    /**
     * This is to test that a single Ballot is "tallied" properly into one Ballot
     */
    public void testTallySingle(){

        /* Load up the public and private keys */
        SimpleKeyStore keyStore = new SimpleKeyStore("keys");
        PublicKey publicKey = (PublicKey)keyStore.loadAdderKey("public");
        PrivateKey privateKey = (PrivateKey)keyStore.loadAdderKey("private");

        PublicKey finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(publicKey);

        /* ((B0 0)(B1 0)(B2 1)...) */
        List<ASExpression> singleVote = new ArrayList<>();

        /* Load up singleVote with pattern (1 0 0 0 0 1 0 0 0 0) */
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

        /* Fill the ballotList with the ASE converted to Ballot */
        ballotList.add(Ballot.fromASE(ballotASE));

        /* Tally them */
        Ballot summed = WebServerTallier.tally("TEST1", ballotList, finalPublicKey);

        /* Get the vote totals */
        Map<String, BigInteger> voteTotals = WebServerTallier.getVoteTotals(summed, 5, finalPublicKey, privateKey);

        /* (Sanity) Check the BID and the public key */
        assertEquals(summed.getBid(), "TEST1");
        assertEquals(summed.getPublicKey(), finalPublicKey);

        /* Compare the decrypted totals with the expected totals */
        for (Map.Entry<String, BigInteger> entry : voteTotals.entrySet()) {
            int toCompare = entry.getKey().equals("B0") || entry.getKey().equals("B5") ? 1 : 0;
            assertEquals(entry.getValue().intValue(), toCompare);
        }

    }

    /**
     * This is to test that a single challenged Ballot is decrypted properly
     */
    public void testDecrypt(){

    }

    /**
     * This is to test that a series of challenged Ballots are decrypted properly
     */
    public void testDecryptAll(){

    }

    /**
     * This is to test that the mapping of candidates to totals is done properly after tallying
     */
    public void testGetVoteTotals(){

        /* Load up the public and private keys */
        SimpleKeyStore keyStore = new SimpleKeyStore("keys");
        PublicKey publicKey = (PublicKey)keyStore.loadAdderKey("public");
        PrivateKey privateKey = (PrivateKey)keyStore.loadAdderKey("private");

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
        Map<String, BigInteger> voteTotals = WebServerTallier.getVoteTotals(Ballot.fromASE(ballotASE), 1, publicKey, privateKey);

        /* Compare the decrypted totals with the expected totals */
        for (Map.Entry<String, BigInteger> entry : voteTotals.entrySet()) {
            int toCompare = entry.getKey().equals("B0") || entry.getKey().equals("B5") ? 1 : 0;
            assertEquals(entry.getValue().intValue(), toCompare);
        }

    }

    /**
     * This is to test many times over that mapping vote totals to candidates is working properly under many random scenarios
     */
    public void testGetLotsOfVoteTotals(){

        /* Load up the public and private keys */
        SimpleKeyStore keyStore = new SimpleKeyStore("keys");
        PublicKey publicKey = (PublicKey)keyStore.loadAdderKey("public");
        PrivateKey privateKey = (PrivateKey)keyStore.loadAdderKey("private");

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
