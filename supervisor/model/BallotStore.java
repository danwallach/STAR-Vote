package supervisor.model;

import auditorium.Key;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.tallier.EncryptedTallier;
import supervisor.model.tallier.ITallier;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;

/**
 * @author Nelson Chen
 *         Date: 11/26/12
 */

public class BallotStore {
    public static final String SERVER_IP = "192.168.1.13";
    public static final int SERVER_PORT = 9000;

    private static ArrayList<ASExpression> castNonces = new ArrayList<ASExpression>();
    private static HashMap<String, ASExpression> unconfirmedBallots = new HashMap<String, ASExpression>();


    /**
     * Add printed ballot to the ballot store
     * The ballot is considered challenged at this point
     * @param ballotID - unique ballot identifier
     * @param ballot - ballot wrapper class encapsulating hashed ballot and r-values
     */
    public static void addBallot(String ballotID, ASExpression ballot) {
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
        return new ListExpression(castNonces);
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
        for (ASExpression unconfirmedBallot : getUnconfirmedBallots()) {
            tallier.recordVotes(unconfirmedBallot.toVerbatim(), null);
            Map<String, BigInteger> ballotMap = tallier.getReport();
            ArrayList<ASExpression> decryptedVotes = new ArrayList<ASExpression>();
            for (Map.Entry<String, BigInteger> entry : ballotMap.entrySet()) {
                decryptedVotes.add(new ListExpression(ListExpression.make(entry.getKey()), ListExpression.make(entry.getValue().toString())));
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            hashes.add(unconfirmedBallot);
            decryptedBallots.add(new ListExpression(decryptedVotes));
        }
        return new ListExpression(new ListExpression(hashes), new ListExpression(decryptedBallots));
    }

    public static List<ASExpression> getUnconfirmedBallots() {
        return new ArrayList<ASExpression>(unconfirmedBallots.values());
    }

}
