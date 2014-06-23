package supervisor.model;

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
     * already been voted on.
     */

    /*
     * Decrypts and returns unconfirmed (challenged) ballots
     *
     * @param privateKey supervisor key
     * @return ListExpression of hashed ballots and decrypted ballots
     */
    /* TODO Move this to the webserver? */
//    public static ListExpression getDecryptedBallots(PublicKey publicKey, PrivateKey privateKey) {
//        /* Create a spoof tallier so we can decrypt with all the necessary NIZK proofs */
//        ITallier tallier = new EncryptedTallierWithNIZKs(publicKey, privateKey);
//
//        List<ASExpression> hashes = new ArrayList<ASExpression>();
//        List<ASExpression> decryptedBallots = new ArrayList<ASExpression>();
//        List<ASExpression> ballotIDs = new ArrayList<ASExpression>();
//        List<ASExpression> precincts = new ArrayList<ASExpression>();
//
//        /* Move any non-cast ballots on to the challenged list */
//        challengedBallots.putAll(committedBallots);
//
//        /* For every uncast ballot, decrypt and tally it */
//        for (String ballotID : committedBallots.keySet()) {
//
//            /* First "cast" the vote */
//            tallier.recordVotes(committedBallots.get(ballotID).toVerbatim(), StringExpression.make(ballotID));
//
//            /* Now decrypt all "cast" votes */
//            Map<String, BigInteger> ballotMap = tallier.getReport();
//            ArrayList<ASExpression> decryptedVotes = new ArrayList<ASExpression>();
//
//            /* add the newly decrypted ballot to the list of plaintext challenged ballots */
//            for (Map.Entry<String, BigInteger> entry : ballotMap.entrySet()) {
//                decryptedVotes.add(new ListExpression(ListExpression.make(entry.getKey()), ListExpression.make(entry.getValue().toString())));
//            }
//
//            /* Add the ballot to a hash chain that is used for challenged ballots */
//            hashes.add(committedBallots.get(ballotID));
//            decryptedBallots.add(new ListExpression(decryptedVotes));
//            ballotIDs.add(ListExpression.make(ballotID));
//            precincts.add(ListExpression.make(getPrecinct(ballotID)));
//        }
//        return new ListExpression(new ListExpression(ballotIDs), new ListExpression(precincts), new ListExpression(hashes), new ListExpression(decryptedBallots));
//    }
}

