package supervisor.model;

import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.tallier.EncryptedTallierWithNIZKs;
import supervisor.model.tallier.ITallier;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * The BallotStore class is used to store the results of voter's decisions (often referred to ambiguously as "ballots"
 * but actually representing encrypted voter selections from a ballot). This class keeps track of ballots
 * as they are committed and optionally subsequently cast by the voter. The BallotStore is the key
 * factor in storing all ballots during an election and determining which ballots are declared as Challenged and which
 * are treated as Cast and therefore counted in the election results.
 */
public class BallotStore {

    /*
     * This class is a combination of two separate classes, whose functionalities became similar.
     * The ballot manager is the first of these who held all the ballot files, their corresponding precincts,
     * and which ballotIds corresponded to the precincts and ballots. The second of the two is the BallotStore that
     * held all the hash chain functionality as well as all cast and un-cast ballots that had
     * already been voted on
     */

    /**
     * A list of the nonce values associated with all ballots that have been cast, i.e. that have been scanned
     * and deposited in the ballot box
     */
    private static ArrayList<ASExpression> castNonces = new ArrayList<ASExpression>();

    /** A list of all cast ballot ID's associated with ballots that have been cast */
    private static ArrayList<ASExpression> castBIDs = new ArrayList<ASExpression>();

    /** Map of all ballots that have been committed but not cast, mapped by BID to raw SExpression representation */
    private static HashMap<String, ASExpression> committedBallots = new HashMap<String, ASExpression>();

    private static HashMap<String, ASExpression> challengedBallots = new HashMap<>();

    /** Map of every BID to its corresponding precinct, and therefore the ballot style */
    private static HashMap<String, String> precinctMap = new HashMap<String, String>();

    /* TODO Provide better documentation and examination of the hash chain code? */
    /** initial value passed to hash function to act as a previous node in the chain */
    private static String initialLastHash  = "00000000000000000000000000000000";

    /** Inizialize the hash chain with an initial value that can be traced back to the start of the election */
    private static String lastHash = initialLastHash;

    /** A random generator for generating PINs and hashing */
    private static Random rand = new Random();

    /** A formatter for the hash codes */
    private static DecimalFormat uniquenessFormat = new DecimalFormat("0000000000");

    /** A formatter for hashed serials */
    private static DecimalFormat serialFormat = new DecimalFormat("00");

    /** BID to hash values for chaining */
    private static HashMap<String, String> HashToBID = new HashMap<String, String>();

    /** Machine ID numbers to hash values for chaining */
    private static HashMap<String, String> HashToMID = new HashMap<String, String>();

    /** Maps every PIN to a time stamp so that the PIN can expire */
    private static Map<String, PinTimeStamp> timeStamp = new HashMap<String, PinTimeStamp>();

    /** Holds all active PINs and corresponding ballots */
    private static Map<String, String> ballotByPin = new HashMap<String, String>();

    /** Holds all precincts and corresponding ballot location */
    private static Map<String, String> ballotByPrecinct = new HashMap<String, String>();

    /** An inverse mapping of ballotByPrecinct, maps precincts to ballots */
    private static Map<String, String> precinctByBallot = new HashMap<String, String>();

    /** Maps precincts to their corresponding BID's */
    private static Map<String, String> precinctByBID = new HashMap<String, String>();

    /** A decimal formatter for generating PINs */
    private static DecimalFormat decimalFormat = new DecimalFormat("00000");

    /**
     * Add printed ballot to the ballot store. If not cast before the closing of the elections, this ballot will be
     * considered challenged by the STAR-Vote System.
     *
     * @param ballotID unique ballot identifier
     * @param ballot ballot wrapper class encapsulating hashed ballot and r-values
     */
    public static void addBallot(String ballotID, ASExpression ballot) {
        committedBallots.put(ballotID, ballot);
    }


    /**
     * Cast a previously committed ballot. This action results from a voter scanning his/her ballot. This ballot is
     * now cast and counted in the tallying of final results in the election.
     *
     * @param ballotID unique ballot identifier
     * @return the S-Expression representation of the ballot that was cast
     */
    public static ASExpression castCommittedBallot(String ballotID){
        /* The ballot must have previously been committed to be case */
        if(committedBallots.containsKey(ballotID)){
            castNonces.add(committedBallots.get(ballotID));
            castBIDs.add(ListExpression.make(ballotID));
            return committedBallots.remove(ballotID);
        }else{
            throw new RuntimeException("Ballot was cast before it was committed");
        }
    }

    /**
     * @return all nonces of cast ballots
     */
    public static ListExpression getCastNonces() {

        List<ASExpression> precincts = new ArrayList<ASExpression>();

        for (ASExpression bid: castBIDs)
            precincts.add(ListExpression.make(precinctMap.get(bid.toString())));

        return new ListExpression(new ListExpression(castBIDs), new ListExpression(precincts), new ListExpression(castNonces));
    }

    /**
     * Creates a mapping between ballot IDs and respective precincts
     *
     * @param bid  ballot ID of voting session
     * @param precinct 3-digit precinct of voting session
     */
    public static void mapPrecinct(String bid, String precinct){
        precinctMap.put(bid, precinct);
    }

    /**
     * @param bid ballot ID of voting session
     * @return precinct associated with this ballot, or null, if none exists.
     */
    public static String getPrecinct(String bid){
        return precinctMap.get(bid);
    }

// --Commented out by Inspection START (5/27/14, 3:25 PM):
//    /**
//     * Re-initializes the list of cast ballot IDs and cast nonces
//     */
//    /* TODO Does this need to be here? */
//    public static void clearBallots(){
//        castBIDs = new ArrayList<ASExpression>();
//        castNonces = new ArrayList<ASExpression>();
//    }
// --Commented out by Inspection STOP (5/27/14, 3:25 PM)

    /**
     * Decrypts and returns unconfirmed (challenged) ballots
     *
     * @param privateKey supervisor key
     * @return ListExpression of hashed ballots and decrypted ballots
     */
    /* TODO Move this to the webserver? */
    public static ListExpression getDecryptedBallots(PublicKey publicKey, PrivateKey privateKey) {
        /* Create a spoof tallier so we can decrypt with all the necessary NIZK proofs */
        ITallier tallier = new EncryptedTallierWithNIZKs(publicKey, privateKey);

        List<ASExpression> hashes = new ArrayList<ASExpression>();
        List<ASExpression> decryptedBallots = new ArrayList<ASExpression>();
        List<ASExpression> ballotIDs = new ArrayList<ASExpression>();
        List<ASExpression> precincts = new ArrayList<ASExpression>();

        /* Move any non-cast ballots on to the challenged list */
        challengedBallots.putAll(committedBallots);

        /* For every uncast ballot, decrypt and tally it */
        for (String ballotID : committedBallots.keySet()) {

            /* First "cast" the vote */
            tallier.recordVotes(committedBallots.get(ballotID).toVerbatim(), StringExpression.make(ballotID));

            /* Now decrypt all "cast" votes */
            Map<String, BigInteger> ballotMap = tallier.getReport();
            ArrayList<ASExpression> decryptedVotes = new ArrayList<ASExpression>();

            /* add the newly decrypted ballot to the list of plaintext challenged ballots */
            for (Map.Entry<String, BigInteger> entry : ballotMap.entrySet()) {
                decryptedVotes.add(new ListExpression(ListExpression.make(entry.getKey()), ListExpression.make(entry.getValue().toString())));
            }

            /* Add the ballot to a hash chain that is used for challenged ballots */
            hashes.add(committedBallots.get(ballotID));
            decryptedBallots.add(new ListExpression(decryptedVotes));
            ballotIDs.add(ListExpression.make(ballotID));
            precincts.add(ListExpression.make(getPrecinct(ballotID)));
        }
        return new ListExpression(new ListExpression(ballotIDs), new ListExpression(precincts), new ListExpression(hashes), new ListExpression(decryptedBallots));
    }

    /**
     * @param bid the bid of the ballot to spoil
     * @return the ballot with the corresponding bid
     */
    public static ASExpression spoilBallot(String bid) {

        /* Move this ballot to the challenge list */
        challengedBallots.put(bid, committedBallots.get(bid));

        /* Spoil the ballot by removing it from the list of unconfirmed (committed) ballots*/
        return committedBallots.remove(bid);
    }

    /**
     * Creates a hash for voting session and saves BID and Machine ID (MID) for hash chain checking later
     *
     * @param serialNumber the serial number of the machine
     * @return the resulting hash
     */
    public static String createBallotHash(int serialNumber){
        /* We will concatenate all the elements to hash together */
        String elementsToBeHashed = "";

        /* This is a random number to let each hash instance to be unique */
        int ballotUniqueness = rand.nextInt(Integer.MAX_VALUE);

        /* Concatenate formatted version of the serial number, uniqueness number, and the last hash */
        elementsToBeHashed += uniquenessFormat.format(ballotUniqueness) + serialFormat.format(serialNumber) + lastHash;

        /* Now hash the concatenated information */
        String hash = hashWithSHA256(elementsToBeHashed);

        /* put the newly created hash in the necessary lists. */
        HashToBID.put(lastHash, uniquenessFormat.format(ballotUniqueness));
        HashToMID.put(lastHash, serialFormat.format(serialNumber));

        /* Update the last hash */
        lastHash = hash;

        /* return this hash */
        return hash;
    }

    /**
     * A wrapper for the raw SHA256 hashing function provided in the Java libraries
     *
     * @param toBeHashed a string to be hashed
     * @return the result of hashing the string with the SHA256 algorithm
     */
    public static String hashWithSHA256(String toBeHashed){
        String hash = "";
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
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
        } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        return hash;
    }

    /**
     * Goes through all of the ballots in the voting session checking that all ballot's hashes
     * are computed from the previous ballot's hash and that ballot's machineID and BID, proving if any
     * are missing in the chain
     *
     * @return true if the hash chain is valid, false if it has been compromised.
     */
    public static Boolean isHashChainCompromised(){

        String previousHash = initialLastHash;
        String elementsToBeHashed;
        if(!HashToBID.containsKey(previousHash))
            return true;

        /* Compute the hash chain from beginning to end using the stored ballot info to reconstruct the chain */
        while(!HashToBID.get(previousHash).equals("0000000000")){
            elementsToBeHashed = HashToBID.get(previousHash) + HashToMID.get(previousHash) + previousHash;
            previousHash = hashWithSHA256(elementsToBeHashed);

            /* If the hash that was computed was not the expected hash, we have a problem. */
            if(!HashToBID.containsKey(previousHash))
                return true;

        }

        return false;
    }

    /**
     * Adds flag to hash chain signalling end of chain
     */
    public static void closeHashChain(){
        HashToBID.put(lastHash, "0000000000");
    }

    /* This concludes all of the code previously held in the BallotManager */



    /**
     * Generates a random pin and adds it to the list of pins and its corresponding ballot based on its precinct
     *
     * @param precinct the precinct to generate a PIN for
     * @return a randomly generated PIN
     */
    public static String generatePin(String precinct){
        /* TODO Review this code */
        String pin = decimalFormat.format(rand.nextInt(100000));

        /* Ensure that we don't use a PIN that is already active */
        while(ballotByPin.containsKey(pin))
            pin = decimalFormat.format(rand.nextInt(100000));


        String ballot = ballotByPrecinct.remove(precinct);

        /* Overwrite any prexisting copies of this ballot (though there shouldn't be any */
        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);

        /* create a new time stamp on this pin */
        timeStamp.put(pin, new PinTimeStamp());
        ballotByPin.put(pin, ballotByPrecinct.get(precinct));

        return pin;
    }

    /**
     * Generate a PIN for a provisional voting session
     *
     * @param precinct the precinct (i.e. ballot style) for this voting session
     * @return a provisional PIN
     */
    public static String generateProvisionalPin(String precinct) {

        String provisionalPin = decimalFormat.format(rand.nextInt(10000));

        while(ballotByPin.containsKey(provisionalPin))
            provisionalPin = decimalFormat.format(rand.nextInt(10000));

        //Move the mappings from one to the other
        String ballot = ballotByPrecinct.remove(precinct);

        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);

        /* Generate a new time stamp for this PIN so it can expire */
        timeStamp.put(provisionalPin, new PinTimeStamp());
        ballotByPin.put(provisionalPin, ballot);

        return provisionalPin;
    }

    /**
     * Returns ballot mapped to pin and null if pin is not recognized
     *
     * @param pin the PIN to get the ballot out of the map with
     * @return the newly retrieved ballot
     */
    public static String getBallotByPin(String pin){
        String ballot = null;
        if(ballotByPin.containsKey(pin)){
            if(timeStamp.get(pin).isValid()){
                ballot = ballotByPin.get(pin);
            }
            ballotByPin.remove(pin);
        }

        return ballot;
    }

    /**
     * Gets a precinct name via a ballot
     *
     * @param ballot the ballot to lookup the precinct with
     * @return the precinct for the provided ballot
     */
    public static String getPrecinctByBallot(String ballot){

        if(precinctByBallot.containsKey(ballot))
            return precinctByBallot.get(ballot);

        return null;
    }

    /**
     * adds a newly selected ballot to ballotByPrecinct
     *
     * @param precinct the ballot's precinct
     * @param ballot the ballot to map to the given precinct
     */
    public static void addBallot(String precinct, String ballot){
        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.put(ballot, precinct);
        ballotByPrecinct.put(precinct+"-provisional", ballot);
    }

    /**
     * Maps precincts to BID's
     *
     * @param bID the ballot ID to map
     * @param precinct the ballot's precinct
     */
    public static void setPrecinctByBID(String bID, String precinct){
        precinctByBID.put(bID, precinct);
    }

    /**
     * Looks up a ballot's precinct, given its BID
     *
     * @param bID the ballotID to look up
     * @return the precinct corresponding to the ballot ID'd by bID
     */
    public static String getPrecinctByBID(String bID){

        if(precinctByBID.containsKey(bID))
            return precinctByBID.get(bID);

        return null;
    }

    /**
     * @return an array of all the precincts
     */
    public static String[] getPrecincts(){
        Set<String> var = ballotByPrecinct.keySet();
        return var.toArray(new String[var.size()]);
    }

    /**
     * @return first precinct in set of precincts
     */
    public static String getInitialPrecinct(){
        Iterator i = ballotByPrecinct.keySet().iterator();
        return (String) i.next();
    }

    /**
     * A test method that prints the precinct by BID map
     */
    public static void testMapPrint(){
        System.out.println(precinctByBID);
    }
}

/**
 * Keeps track of how long a pin has been issued and expires the pin if pin is older than
 * lifeTimeInSeconds
 */
class PinTimeStamp{

    /** The default lifespan for a PIN, in seconds */
    private static final int DEFAULT_LIFE_TIME = 180;

    /** The time that this PIN came into the world */
    private long startTime;

    /** The lifespan of the PIN in seconds */
    private int lifeTimeInSeconds;

    /**
     * Constructor
     *
     * @param lt the lifespan of the PIN in seconds
     */
    @SuppressWarnings("SameParameterValue")
    public PinTimeStamp(int lt){
        startTime = System.currentTimeMillis();
        lifeTimeInSeconds = lt;
    }

    /**
     * Default constructor, uses the defualt lifespan
     */
    public PinTimeStamp(){
        this(DEFAULT_LIFE_TIME);
    }

    /**
     * Determines if a PIN is "alive"
     *
     * @return true if the PIN hasn't been around for more than lifeTimeInSeconds time, false otherwise
     */
    public boolean isValid(){
        return (System.currentTimeMillis()-startTime) < 1000*lifeTimeInSeconds;
    }
}

