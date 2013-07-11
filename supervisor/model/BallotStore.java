package supervisor.model;

import auditorium.Key;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.tallier.EncryptedTallier;
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
 * @author Nelson Chen
 *         Date: 11/26/12
 */

public class BallotStore {
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



    /**
     * Add printed ballot to the ballot store
     * The ballot is considered challenged at this point
     * @param ballotID - unique ballot identifier
     * @param ballot - ballot wrapper class encapsulating hashed ballot and r-values
     */
    public static void addBallot(String ballotID, ASExpression ballot) {
        //System.err.println("Adding BID " + ballotID + " to the unconfirmedBallots in the BallotStore. It is now ready to be cast.");
        unconfirmedBallots.put(ballotID, ballot);
    }


    /**
     * Cast unconfirmed ballot
     * @param ballotID - unique ballot identifier
     */
    public static void castCommittedBallot(String ballotID){
        if(unconfirmedBallots.containsKey(ballotID)){
            System.out.println("A committed ballot was cast");
            castNonces.add(unconfirmedBallots.get(ballotID));
            castBIDs.add(ListExpression.make(ballotID));
            unconfirmedBallots.remove(ballotID);
        }else{
            throw new RuntimeException("Ballot was cast before it was committed");
        }
    }

    /**
     * Retrieves nonces of cast ballots
     * @return
     */
    public static ListExpression getCastNonces() {
        List<ASExpression> precincts = new ArrayList<ASExpression>();
        for(ASExpression bid: castBIDs){
            precincts.add(ListExpression.make(precinctMap.get(bid.toString())));
        }
        return new ListExpression(new ListExpression(castBIDs), new ListExpression(precincts), new ListExpression(castNonces));
    }

    public static void mapPrecinct(String bid, String precinct){
        precinctMap.put(bid, precinct);
    }

    public static String getPrecinct(String bid){
        return precinctMap.get(bid);
    }


    /**
     * Decrypts and returns unconfirmed (challenged) ballots
     * @param privateKey - supervisor key
     * @return LE of 2 LEs: hashed ballots and decrypted ballots
     */
    public static ListExpression getDecryptedBallots(Key privateKey) {
        ITallier tallier = new EncryptedTallier(privateKey);
        List<ASExpression> hashes = new ArrayList<ASExpression>();
        List<ASExpression> decryptedBallots = new ArrayList<ASExpression>();
        List<ASExpression> ballotIDs = new ArrayList<ASExpression>();
        List<ASExpression> precincts = new ArrayList<ASExpression>();
        for (String ballotID : unconfirmedBallots.keySet()) {
            tallier.recordVotes(unconfirmedBallots.get(ballotID).toVerbatim(), null);
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

    public static List<ASExpression> getUnconfirmedBallots() {
        return new ArrayList<ASExpression>(unconfirmedBallots.values());
    }

    public static String createBallotHash(int serialNumber){
        //creates a hash for voting session and saves BID and MID for hash Chain checking later
        String elementsToBeHashed = "";
        int ballotUniqueness = rand.nextInt(Integer.MAX_VALUE);
        elementsToBeHashed+=uniquenessFormat.format(ballotUniqueness)+serialFormat.format(serialNumber)+hashFormat.format(Integer.parseInt(lastHash));
        String hash = hashWithSHA256(elementsToBeHashed);
        HashToBID.put(lastHash, String.valueOf(ballotUniqueness));     // mapped to last hash so that with previous hash value can compute next hash value
        HashToMID.put(lastHash, String.valueOf(serialNumber));
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
            hash = digest.digest(toBeHashed.getBytes("UTF-8")).toString();
        } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        return hash;
    }
    public static Boolean isHashChainCompromised(){
        // Method goes through ballots in voting session checking that all ballot's hashes are computed from the preious ballots' Hash and that ballot's machineID and BID, proving if any
        // are missing in the chain
        boolean compromised = false;
        String previousHash = initialLastHash;
        String elementsToBeHashed = "";
        while(Integer.valueOf(HashToBID.get(previousHash))!=0&&!compromised){      //size minus one because can not check last hash since it will not be stored in Ballot Store
            if(HashToBID.containsKey(previousHash)){
                elementsToBeHashed =uniquenessFormat.format(HashToBID.get(previousHash))+serialFormat.format(HashToMID.get(previousHash))+hashFormat.format(Integer.parseInt(previousHash));
                previousHash = hashWithSHA256(elementsToBeHashed);
            }
            else{
                compromised = true;
            }
        }
        return compromised;
    }

    public static void closeHashChain(){
        HashToBID.put(lastHash, "0");   //adds flag to hash chain signalling end of chain
    }

}
