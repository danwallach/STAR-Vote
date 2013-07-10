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
    private static String initialLastHash  = "00000000000000000000000000000000";
    private static String lastHash = initialLastHash;
    private static Random rand = new Random();
    private static DecimalFormat uniquenessFormat = new DecimalFormat("0000000000");
    private static DecimalFormat serialFormat = new DecimalFormat("00");
    private static DecimalFormat hashFormat = new DecimalFormat("00000000000000000000000000000000");


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
        String elementsToBeHashed = "";
        int ballotUniqueness = rand.nextInt(Integer.MAX_VALUE);
        elementsToBeHashed+=uniquenessFormat.format(ballotUniqueness)+serialFormat.format(serialNumber)+hashFormat.format(Integer.parseInt(lastHash));
        String hash = "";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        try {
            hash = digest.digest(elementsToBeHashed.getBytes("UTF-8")).toString();
        } catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        return hash;

    }

}
