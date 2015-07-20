package models;

import crypto.EncryptedRaceSelection;
import crypto.ExponentialElGamalCiphertext;
import org.apache.commons.codec.binary.Base64;
import play.db.ebean.Model;
import supervisor.model.Ballot;

import javax.persistence.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model of a DecryptedResult on the Web-Server. An entity of the database
 * that stores the encrypted running total Ballot and decrypted vote totals
 * for the candidates of a given precinct.
 *
 * Created by Matthew Kindy II on 10/5/2014.
 */

@Entity
public class DecryptedResult extends Model {

    /**
     * This member object is a finder that aids in retrieving DecryptedResults by their precinctIDs
     */
    public static Finder<Long, DecryptedResult> find = new Finder<>(Long.class, DecryptedResult.class);

    @Id
    public long id;

    public String precinctID;

    @OneToMany(mappedBy="owner", cascade= CascadeType.ALL, fetch= FetchType.EAGER)
    @MapKey(name="raceName")
    public Map<String, RaceResult> raceResults = new HashMap<>();

    @Column(columnDefinition = "TEXT")
    public String precinctResultsBallot;

    /**
     * Constructor
     *
     * @param precinctID                the ID of the precinct of these results
     * @param raceResults               the map of race names to mapping of candidates to their totals
     * @param precinctResultsBallot     the encrypted totalled ballot for this precinct
     */
    public DecryptedResult(String precinctID, Map<String, Map<String, Integer>> raceResults, Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> precinctResultsBallot) {

        this.precinctID             = precinctID;
        this.precinctResultsBallot  = ballotToString(precinctResultsBallot);


        for(Map.Entry<String, Map<String, Integer>> raceResultMap : raceResults.entrySet()) {
            RaceResult raceResult = new RaceResult(this, raceResultMap.getKey(), raceResultMap.getValue());
            System.out.println(raceResult);
            this.raceResults.put(raceResultMap.getKey(), raceResult);
            System.out.println(raceResults);
        }
    }

    /**
     * @see play.db.ebean.Model.Finder#all()
     */
    public static List<DecryptedResult> all() {
        return find.all();
    }

    /**
     * Database lookup for a DecryptedResult with the given precinctID.
     * todo integrate this into the view for tallied results
     *
     * @param precinctID       the ID of the precinct from which this record was collected
     * @return                 the corresponding VoteRecord or null if non-existent
     */

    /**
     *
     * @param precinctID
     * @return
     */
    public static DecryptedResult getDecryptedResult(String precinctID) {
        return find.where().ieq("precinctID", precinctID).findUnique();
    }

    public Map<String, RaceResult> getResults() {
        return raceResults;
    }

    public Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> getResultsBallot() {
        return stringToBallot(this.precinctResultsBallot);
    }

    private String ballotToString(Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> b) {

        String encoded = null;

        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(b);
            objectOutputStream.close();

            encoded = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
        }
        catch (IOException e) { e.printStackTrace(); }

        return encoded;
    }

    private Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> stringToBallot(String s) {

        byte[] bytes = Base64.decodeBase64(s.getBytes());
        Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> ballot = null;


        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            ballot = (Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>)objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException | ClassCastException e) { e.printStackTrace(); }

        return ballot;
    }

    /**
     * Store a DecryptedResult into the database.
     *
     * @param record        record to be stored
     */
    public static void create(DecryptedResult record) {
        record.save();
    }

    /**
     * Remove a DecryptedResult from a database
     *
     * @param record        record to be removed
     */
    public static void remove(DecryptedResult record){
        record.delete();
    }

    /**
     * @return appropriately formatted String representation
     */
    public String toString() {
        return "PrecinctID: " + precinctID + ", Race Results: " + raceResults.toString() + ", Ballot Form: " + precinctResultsBallot.toString();
    }

}