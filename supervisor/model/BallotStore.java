package supervisor.model;

import auditorium.Key;
import edu.uconn.cse.adder.PrivateKey;
import edu.uconn.cse.adder.PublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.tallier.ChallengeDelayedWithNIZKsTallier;
import supervisor.model.tallier.EncryptedTallier;
import supervisor.model.tallier.EncryptedTallierWithNIZKs;
import supervisor.model.tallier.ITallier;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * The BallotStore class is used to store the results of voter's decisions (often referred to ambiguously as "ballots"
 * but actually representing encrypted voter selections from a ballot). This class keeps track of ballots
 * (again, selections) as they are committed and optionally subsequently cast by the voter. The BallotStore is the key
 * factor in storing all ballots during an election and determining which ballots are declared as Challenged and which
 * are treated as Cast and therefore counted in the election results.
 */
public class BallotStore {

    //This class is a combination of two seperate classes, whose functionalities became similar. The ballot manager is the first of these who held all the ballot files, their corresponding precincts,
    // and which ballotIds corresponded to the precincts and ballots. The second of the two is the Ballot store that held all the hash chain functionality as well as all cast and uncast ballots that had
    // already been voted on

    public static final String SERVER_IP = "192.168.1.13";
    public static final int SERVER_PORT = 9000;

    private static ArrayList<ASExpression> castNonces = new ArrayList<ASExpression>();
    private static ArrayList<ASExpression> castBIDs = new ArrayList<ASExpression>();
    private static HashMap<String, ASExpression> unconfirmedBallots = new HashMap<String, ASExpression>();
    private static HashMap<String, String> precinctMap = new HashMap<String, String>();
    private static String initialLastHash  = "00000000000000000000000000000000";     //initial value passed to hash function to act as a previous node in the chain
    private static String lastHash = initialLastHash;
    private static Random rand = new Random();
    private static DecimalFormat uniquenessFormat = new DecimalFormat("0000000000");
    private static DecimalFormat serialFormat = new DecimalFormat("00");
    private static DecimalFormat hashFormat = new DecimalFormat("00000000000000000000000000000000");
    private static HashMap<String, String> HashToBID = new HashMap<String, String>();       //BID to hash values for chaining
    private static HashMap<String, String> HashToMID = new HashMap<String, String>();       //Machine ID numbers to hash values for chaining

    //manages the pins for the supervisor as well as all corresponding ballots

    private static Map<String, PinTimeStamp> timeStamp = new HashMap<String, PinTimeStamp>();
    private static Map<String, String> ballotByPin = new HashMap<String, String>();       //Holds all active pins and corresponding ballot location
    private static Map<String, String> ballotByPrecinct = new HashMap<String, String>();       //Holds all precincts and corresponding ballot location
    private static Map<String, String> precinctByBallot = new HashMap<String, String>();
    private static Map<String, String> precinctByBID = new HashMap<String, String>();

    private static DecimalFormat decimalFormat = new DecimalFormat("0000");



    /**
     * Add printed ballot to the ballot store. If not cast before the closing of the elections, this ballot will be
     * considered Challenged by the STAR-Vote System.
     *
     * @param ballotID - unique ballot identifier
     * @param ballot - ballot wrapper class encapsulating hashed ballot and r-values
     */
    public static void addBallot(String ballotID, ASExpression ballot) {
        //System.err.println("Adding BID " + ballotID + " to the unconfirmedBallots in the BallotStore. It is now ready to be cast.");
        unconfirmedBallots.put(ballotID, ballot);
    }


    /**
     * Cast of a previously committed ballot. This action results from a voter scanner his/her ballot. This ballot is
     * now cast and counted in the tallying of final results in the election.
     *
     * @param ballotID - unique ballot identifier
     */
    public static ASExpression castCommittedBallot(String ballotID){
        if(unconfirmedBallots.containsKey(ballotID)){
            System.out.println("A committed ballot was cast");
            castNonces.add(unconfirmedBallots.get(ballotID));
            castBIDs.add(ListExpression.make(ballotID));
            return unconfirmedBallots.remove(ballotID);
        }else{
            throw new RuntimeException("Ballot was cast before it was committed");
        }
    }

    /**
     * @return all nonces of cast ballots
     */
    public static ListExpression getCastNonces() {
        List<ASExpression> precincts = new ArrayList<ASExpression>();
        for(ASExpression bid: castBIDs){
            precincts.add(ListExpression.make(precinctMap.get(bid.toString())));
        }
        return new ListExpression(new ListExpression(castBIDs), new ListExpression(precincts), new ListExpression(castNonces));
    }

    /**
     * Creates a mapping between ballotids and respective precincts
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

    /**
     * Re-initializes the list of cast ballot IDs and cast nonces
     */
    public static void clearBallots(){
        castBIDs = new ArrayList<ASExpression>();
        castNonces = new ArrayList<ASExpression>();
    }

    /**
     * Decrypts and returns unconfirmed (challenged) ballots
     * @param privateKey - supervisor key
     * @return LE of 2 LEs: hashed ballots and decrypted ballots
     */
    public static ListExpression getDecryptedBallots(PublicKey publicKey, PrivateKey privateKey) {
        ITallier tallier = new EncryptedTallierWithNIZKs(publicKey, privateKey);
        List<ASExpression> hashes = new ArrayList<ASExpression>();
        List<ASExpression> decryptedBallots = new ArrayList<ASExpression>();
        List<ASExpression> ballotIDs = new ArrayList<ASExpression>();
        List<ASExpression> precincts = new ArrayList<ASExpression>();
        for (String ballotID : unconfirmedBallots.keySet()) {
            tallier.recordVotes(unconfirmedBallots.get(ballotID).toVerbatim(), StringExpression.make(ballotID));
            Map<String, BigInteger> ballotMap = tallier.getReport();
            ArrayList<ASExpression> decryptedVotes = new ArrayList<ASExpression>();
            for (Map.Entry<String, BigInteger> entry : ballotMap.entrySet()) {
                decryptedVotes.add(new ListExpression(ListExpression.make(entry.getKey()), ListExpression.make(entry.getValue().toString())));
            }
            hashes.add(unconfirmedBallots.get(ballotID));
            decryptedBallots.add(new ListExpression(decryptedVotes));
            ballotIDs.add(ListExpression.make(ballotID));
            precincts.add(ListExpression.make(getPrecinct(ballotID)));
        }
        return new ListExpression(new ListExpression(ballotIDs), new ListExpression(precincts), new ListExpression(hashes), new ListExpression(decryptedBallots));
    }

    /**
     * @return List of unconfirmed (not Cast) ballots so far in the system
     */
    public static List<ASExpression> getUnconfirmedBallots() {
        return new ArrayList<ASExpression>(unconfirmedBallots.values());
    }

    public static String createBallotHash(int serialNumber){
        //creates a hash for voting session and saves BID and MID for hash Chain checking later
        String elementsToBeHashed = "";
        int ballotUniqueness = rand.nextInt(Integer.MAX_VALUE);
        elementsToBeHashed+=uniquenessFormat.format(ballotUniqueness)+serialFormat.format(serialNumber)+lastHash;
        String hash = hashWithSHA256(elementsToBeHashed);
        HashToBID.put(lastHash, uniquenessFormat.format(ballotUniqueness));     // mapped to last hash so that with previous hash value can compute next hash value
        HashToMID.put(lastHash, serialFormat.format(serialNumber));
        lastHash = hash;
        return hash;
    }
    public static String hashWithSHA256(String toBeHashed){
        //SHA256 hashing algorithm for string "toBeHashed"
        String hash = "";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        try {
            digest.update(toBeHashed.getBytes("UTF-8"));
            byte[] arrOut = digest.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < arrOut.length; i++) {
                sb.append(Integer.toString((arrOut[i] & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        return hash;
    }
    public static Boolean isHashChainCompromised(){
        // Method goes through ballots in voting session checking that all ballot's hashes are computed from the preious ballots' Hash and that ballot's machineID and BID, proving if any
        // are missing in the chain
        boolean compromised = false;
        String previousHash = initialLastHash;
        String elementsToBeHashed = "";
        if(!HashToBID.containsKey(previousHash)){
            compromised = true;
        }
        while((!compromised)&&(!HashToBID.get(previousHash).equals("0000000000"))){
            elementsToBeHashed =HashToBID.get(previousHash)+HashToMID.get(previousHash)+previousHash;
            previousHash = hashWithSHA256(elementsToBeHashed);
            if(!HashToBID.containsKey(previousHash)){
                compromised = true;
            }
        }
        return compromised;
    }

    public static void closeHashChain(){
        HashToBID.put(lastHash, "0000000000");   //adds flag to hash chain signalling end of chain
    }

//ballot Manager

    //generates a random pin and adds it to the list of pins and its corresponding ballot based on its precinct
    public static String generatePin(String precinct){
        String pin = decimalFormat.format(rand.nextInt(10000));


        while(ballotByPin.containsKey(pin))
            pin = decimalFormat.format(rand.nextInt(10000));

        String ballot = ballotByPrecinct.remove(precinct);

        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);

        //create a new time stamp on this pin
        timeStamp.put(pin, new PinTimeStamp());
        ballotByPin.put(pin, ballotByPrecinct.get(precinct));

        return pin;
    }

    public static String generateProvisionalPin(String precinct){
        System.err.println(">>> Generating provisional pin for precinct " + precinct);
        String provisionalPin = decimalFormat.format(rand.nextInt(10000));

        while(ballotByPin.containsKey(provisionalPin))
            provisionalPin = decimalFormat.format(rand.nextInt(10000));

        //Move the mappings from one to the other
        String ballot = ballotByPrecinct.remove(precinct);


        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);



        timeStamp.put(provisionalPin, new PinTimeStamp());
        ballotByPin.put(provisionalPin, ballot);

        return provisionalPin;
    }

    //returns ballot mapped to pin and null if pin is not in Map
    public static String getBallotByPin(String pin){
        String s = null;
        if(ballotByPin.containsKey(pin)){
            if(timeStamp.get(pin).isValid()){
                s = ballotByPin.get(pin);
            }
            ballotByPin.remove(pin);
        }
        return s;
    }

    /**
     * Gets a precinct name via a ballot
     */
    public static String getPrecinctByBallot(String ballot){
        if(precinctByBallot.containsKey(ballot)){
            return precinctByBallot.get(ballot);

        }
        return null;
    }

    //adds a newly selected ballot to ballotByPrecinct
    public static void addBallot(String precinct, String ballot){
        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.put(ballot, precinct);
        ballotByPrecinct.put(precinct+"-provisional", ballot);
    }

    public static void setPrecinctByBID(String bID, String precinct){
        System.out.println("BAllot manager setting BID: " + bID + " to precinct: "+ precinct);
        precinctByBID.put(bID, precinct);
    }

    public static String getPrecinctByBID(String bID){
        if(precinctByBID.containsKey(bID)){
            return precinctByBID.get(bID);
        }
        return null;
    }

    //returns array of precincts
    public static String[] getSelections(){
        return ballotByPrecinct.keySet().toArray(new String[0]);
    }

    //returns first precinct in set of precincts
    public static String getInitialSelection(){
        Iterator i = ballotByPrecinct.keySet().iterator();
        return (String) i.next();
    }

    public static void testMapPrint(){
        System.out.println(precinctByBID);
    }
}


// keeps track of how long a pin has been issued and expires the pin if pin is older than
// lifeTimeInSeconds

class PinTimeStamp{

    private static final int DEFAULT_LIFE_TIME = 180;

    private long startTime;
    private int lifeTimeInSeconds;

    public PinTimeStamp(int lt){
        startTime = System.currentTimeMillis();
        lifeTimeInSeconds = lt;
    }

    public PinTimeStamp(){
        this(DEFAULT_LIFE_TIME);
    }

    public boolean isValid(){
        return (System.currentTimeMillis()-startTime) < 1000*lifeTimeInSeconds;
    }
}

