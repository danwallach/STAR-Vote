package supervisor.model.test;

import junit.framework.TestCase;
import supervisor.model.HashChain;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * A test suite for the HashChain class
 *
 * Created by Matthew Kindy II on 6/25/2014.
 */
public class HashChainTest extends TestCase {

    private HashChain h;
    private Pattern pattern;

    /**
     * Test case setup
     */
    protected void setUp() throws Exception {

        super.setUp();

        pattern = Pattern.compile("[A-Fa-f0-9]{64}");
    }

    /**
     * Here we check that the hash is what it is supposed to be:
     * An SHA-256 hash of a random integer concatenated with the serial number and previousHash
     */
    public void testHashBallot() {

        h = new HashChain();

        /* Create a seeded random generator */
        Random r = new Random(10L);

        /* Set up the format for uniqueID */
        DecimalFormat d = new DecimalFormat("0000000000");

        /* Manually construct the string to hash */
        String toHash = "" + d.format(r.nextInt(Integer.MAX_VALUE)) + "10" + "00000000000000000000000000000000";

        /* Hash both */
        String hash1 = h.hashBallotWithGenerator(10, new Random(10L));
        String hash2 = hashWithSHA256(toHash);

        /* Check if the returned hash matches SHA-256 */
        if(!pattern.matcher(hash1).matches())
            fail("Generated hash " + hash1 + " failed to match the pattern!");

        /* Check that both hashes are identical */
        assertEquals(hash1,hash2);
    }

    /**
     * Here we check that the hash is what it is supposed to be over 1000 iterations for random serials:
     * An SHA-256 hash of a random integer concatenated with the serial number and perviousHash
     */
    public void testHashLotsOfBallots() {

        h = new HashChain();

        String previousHash = "00000000000000000000000000000000";
        int serial;

        for(int i=0; i<1000; i++) {


            /* Create a random serial number generator */
            Random sr = new Random();
            serial = sr.nextInt(100);

            /* Create a seeded random generator */
            Random r = new Random(i);

            /* Set up the format for uniqueID and serial */
            DecimalFormat d = new DecimalFormat("0000000000");
            DecimalFormat s = new DecimalFormat("00");

            /* Manually construct the string to hash */
            String toHash = "" + d.format(r.nextInt(Integer.MAX_VALUE)) + s.format(serial) + previousHash;

            System.out.println("Iteration " + (i+1) + " of testHashLotsOfBallots() with toHash of " + toHash);

            /* Hash both */
            String hash1 = h.hashBallotWithGenerator(serial, new Random(i));
            String hash2 = hashWithSHA256(toHash);

            /* Check if the returned hash matches SHA-256 */
            if (!pattern.matcher(hash1).matches())
                fail("Generated hash " + hash1 + " failed to match the pattern!");

            /* Check that both hashes are identical */
            assertEquals(hash1, hash2);

            previousHash = hash1;
        }
    }

    public void testCloseHash() {

        /* Test 10 different instantiations */
        for(int i = 0; i < 10; i++) {

            boolean isClosed = false;
            h = new HashChain();

            /* Test 10 different lengths before closing */
            for (int j = 0; j < 10; j++) {

                /* Close the hash chain when i==j */
                if(i==j) {
                    h.closeHashChain();
                    isClosed = true;
                }

                /* Try to add 100 hashes and make sure hashing is occurring/not occurring for each one */
                for (int k = 0; k < 100; k++) {

                    if(isClosed)
                        assertEquals(h.hashBallot(10), null);
                    else
                        assertNotNull(h.hashBallot(10));

                    System.out.println("Iteration (" + (i + 1) + ", " + (j + 1) + ", " + (k+1) + ") of testCloseHash");
                }
            }
        }
    }

    public void testIsHashChainCompromised()
    {

        HashMap<String, String> HashToBID = new HashMap<>();
        HashMap<String, String> HashToMID = new HashMap<>();

        String lastHash = "00000000000000000000000000000000";

        /* Set up the format for uniqueID and serial */
        DecimalFormat d = new DecimalFormat("0000000000");
        DecimalFormat s = new DecimalFormat("00");

        /* Add 10 "good" hashes */
        for(int i = 0; i < 10; i++) {

            /* Create a seeded random generator */
            Random r = new Random(i);

            /* Create a random serial number generator */
            Random sr = new Random();
            int serial = sr.nextInt(100);

            /* This is a random number to let each hash instance to be unique */
            int ballotUniqueness = r.nextInt(Integer.MAX_VALUE);

            /* Concatenate formatted version of the serial number, uniqueness number, and the last hash */
            String elementsToBeHashed = "" + d.format(ballotUniqueness) + s.format(serial) + lastHash;

            /* Now hash the concatenated information */
            String hash = hashWithSHA256(elementsToBeHashed);

            /* put the newly created hash in the necessary lists. */
            HashToBID.put(lastHash, d.format(ballotUniqueness));
            HashToMID.put(lastHash, s.format(serial));

            System.out.println("HashToBID Entry: (" + lastHash + ", " + d.format(ballotUniqueness) + ")");
            System.out.println("HashToDID Entry: (" + lastHash + ", " + s.format(serial) + ")");

            /* Update the last hash */
            lastHash = hash;
        }

        HashChain goodChain = new HashChain(HashToBID, HashToMID);
        goodChain.closeHashChain();
        assertFalse(goodChain.isHashChainCompromised());

        HashToBID = new HashMap<>();
        HashToMID = new HashMap<>();

        /* Add 10 "good" hashes */
        for(int i = 0; i < 10; i++) {

            /* Create a seeded random generator */
            Random r = new Random(i);

            /* Create a random serial number generator */
            Random sr = new Random();
            int serial = sr.nextInt(100);

            /* This is a random number to let each hash instance to be unique */
            int ballotUniqueness = r.nextInt(Integer.MAX_VALUE);

            /* Concatenate formatted version of the serial number, uniqueness number, and the last hash */
            String elementsToBeHashed = "" + d.format(ballotUniqueness) + s.format(serial) + lastHash;

            /* Now hash the concatenated information */
            String hash = hashWithSHA256(elementsToBeHashed);

            String toSet = (i!=8) ? lastHash : hashWithSHA256("This is an evil hash!");

            /* put the newly created hash in the necessary lists. */
            HashToBID.put(toSet, d.format(ballotUniqueness));
            HashToMID.put(toSet, s.format(serial));

            System.out.println("HashToBID Entry: (" + lastHash + ", " + d.format(ballotUniqueness) + ")");
            System.out.println("HashToMID Entry: (" + lastHash + ", " + s.format(serial) + ")");

            /* Update the last hash */
            lastHash = hash;
        }

        HashChain badChain = new HashChain(HashToBID, HashToMID);
        badChain.closeHashChain();
        assertTrue(badChain.isHashChainCompromised());
    }

    /**
     * A wrapper for the raw SHA256 hashing function provided in the Java libraries
     *
     * @param toBeHashed        a string to be hashed
     * @return                  the result of hashing the string with the SHA256 algorithm
     */
    private String hashWithSHA256(String toBeHashed){

        String hash = "";
        MessageDigest digest = null;

        try { digest = MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException e) { e.printStackTrace(); }

        try {

            /* Hash the bytes of the input string, encoded in UTF-8 */
            assert(digest != null);
            digest.update(toBeHashed.getBytes("UTF-8"));

            /* Get the results of the hash */
            byte[] arrOut = digest.digest();

            /* Now convert the hashed value back into a string */
            StringBuilder sb = new StringBuilder();

            /* TODO Explain why this masks and offsets */
            for (byte anArrOut : arrOut)
                sb.append(Integer.toString((anArrOut & 0xff) + 0x100, 16).substring(1));

            hash = sb.toString();

        }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        return hash;
    }


}
