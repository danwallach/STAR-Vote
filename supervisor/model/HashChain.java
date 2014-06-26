package supervisor.model;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

/**
 * This class is a data structure to hold the hash record of ballots. It chains hashes
 * among ballots in order to create a secure record of ballot-commit events.
 *
 * Created by Matthew Kindy II on 6/25/2014.
 */
public class HashChain {

    /** initial value passed to hash function to act as a previous node in the chain */
    private final String initialLastHash  = "00000000000000000000000000000000";

    /** Inizialize the hash chain with an initial value that can be traced back to the start of the election */
    private String lastHash;

    /** BID to hash values for chaining */
    private HashMap<String, String> HashToBID;

    /** machine ID numbers to hash values for chaining */
    private HashMap<String, String> HashToMID;

    /** A formatter for the hash codes */
    private DecimalFormat uniquenessFormat;

    /** A formatter for hashed serials */
    private DecimalFormat serialFormat;

    /** A boolean that is true when the HashChain has been closed */
    private boolean isClosed;


    /**
     * Default constructor.
     */
    public HashChain(){
        this(new HashMap<String, String>(), new HashMap<String, String>(), "");
        lastHash = initialLastHash;
    }

    public HashChain(HashMap<String,String> hashToBID, HashMap<String, String> hashToMID, String lastHash) {

        HashToBID = hashToBID;
        HashToMID = hashToMID;

        uniquenessFormat = new DecimalFormat("0000000000");
        serialFormat = new DecimalFormat("00");

        this.lastHash = lastHash;
        isClosed = false;
    }

    /**
     * Creates a hash for voting session and saves BID and machine ID (MID) for hash chain checking later,
     * unless the HashChain is closed.
     *
     * @param serialNumber      the serial number of the machine
     * @return                  the resulting hash, or null if the hashchain is closed
     */
    public String hashBallot(int serialNumber){
        return isClosed ? null : hashBallotWithGenerator(serialNumber, new Random());
    }

    /**
     * Creates a hash for voting session and saves BID and machine ID (MID) for hash chain checking later,
     * unless the HashChain is closed.
     *
     * @param serialNumber      the serial number of the machine
     * @param r                 a generator for random integers
     * @return                  the resulting hash, or null if the hashchain is closed
     */
    public String hashBallotWithGenerator(int serialNumber, Random r)
    {
        if(!isClosed) {

            /* This is a random number to let each hash instance to be unique */
            int ballotUniqueness = r.nextInt(Integer.MAX_VALUE);

            /* Concatenate formatted version of the serial number, uniqueness number, and the last hash */
            String elementsToBeHashed = "" + uniquenessFormat.format(ballotUniqueness) + serialFormat.format(serialNumber) + lastHash;

            /* Now hash the concatenated information */
            String hash = hashWithSHA256(elementsToBeHashed);

            /* put the newly created hash in the necessary lists. */
            HashToBID.put(lastHash, uniquenessFormat.format(ballotUniqueness));
            HashToMID.put(lastHash, serialFormat.format(serialNumber));

            /* Update the last hash */
            lastHash = hash;

            /* return this hash */
            return hash;

        } else return null;
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

    /**
     * Goes through all of the ballots in the voting session checking that all ballot's hashes
     * are computed from the previous ballot's hash and that ballot's machineID and BID, proving if any
     * are missing in the chain
     *
     * @return          true if the hash chain is valid, false if it has been compromised.
     */
    public boolean isHashChainCompromised(){

        String previousHash = initialLastHash;
        String elementsToBeHashed;

        /* See if the initialLastHash is in the HashToBID */
        if (!HashToBID.containsKey(previousHash)) {
            return true;
        }
        /* Compute the hash chain from beginning to end using the stored ballot info to reconstruct the chain */

        /* Cycle through the hashes while you're not getting the end string */
        while (!HashToBID.get(previousHash).equals("0000000000")) {

            /* From the previous hash construct "[BID][MID][previousHash]" and hash it to get the next previous hash */
            elementsToBeHashed = HashToBID.get(previousHash) + HashToMID.get(previousHash) + previousHash;
            previousHash = hashWithSHA256(elementsToBeHashed);

            /* If the hash that was computed was not the expected hash, we have a problem. */
            if(!HashToBID.containsKey(previousHash)) {
                return true;
            }
        }

        /* If we got to the end, there is no problem */
        return false;
    }

    /**
     * Adds flag to hash chain signalling end of chain
     */
    public void closeHashChain(){

        if(!isClosed) {
            HashToBID.put(lastHash, "0000000000");
            isClosed = true;
        }

    }

}
