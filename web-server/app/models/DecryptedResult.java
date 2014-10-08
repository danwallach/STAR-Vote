package models;

import play.db.ebean.Model;
import supervisor.model.Ballot;

import javax.persistence.*;
import java.math.BigInteger;
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

    public Ballot precinctResultsBallot;

    /**
     * Constructor
     *
     * @param precinctID                the ID of the precinct of these results
     * @param raceResults               the map of race names to mapping of candidates to their totals
     * @param precinctResultsBallot     the encrypted totalled ballot for this precinct
     */
    public DecryptedResult(String precinctID, Map<String, Map<String, BigInteger>> raceResults, Ballot precinctResultsBallot) {

        this.precinctID             = precinctID;
        this.precinctResultsBallot  = precinctResultsBallot;


        for(Map.Entry<String, Map<String, BigInteger>> raceResult : raceResults.entrySet())
            this.raceResults.put(raceResult.getKey(), new RaceResult(this, raceResult.getKey(), raceResult.getValue()));
    }

    /**
     * @see play.db.ebean.Model.Finder#all()
     */
    public static List<DecryptedResult> all() {
        return find.all();
    }

    /**
     * Database lookup for a DecryptedResult with the given precinctID.
     *
     * @param precinctID       the ID of the precinct from which this record was collected
     * @return                 the corresponding VoteRecord or null if non-existent
     */
    public static Map<String, RaceResult> getResults(String precinctID) {
        return find.where().ieq("precinctID", precinctID).findUnique().raceResults;
    }

    public static Ballot getResultsBallot(String precinctID) {
        return find.where().ieq("precinctID", precinctID).findUnique().precinctResultsBallot;
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