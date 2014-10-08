package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model of a DecryptedResult on the Web-Server. An entity of the database
 * that stores the encrypted running total Ballot and decrypted vote totals
 * for the candidates of a given precinct.
 *
 * Created by Matthew Kindy II on 10/8/2014.
 */

@Entity
public class RaceResult extends Model {

    /**
     * This member object is a finder that aids in retrieving DecryptedResults by their precinctIDs
     */
    public static Finder<Long, RaceResult> find = new Finder<>(Long.class, RaceResult.class);

    @Id
    public long id;

    public String raceName;

    @OneToMany
    public Map<String, BigInteger> candidateResults = new HashMap<>();

    /**
     * Constructor
     *
     * @param raceName
     * @param candidateResults          the map of candidates to their vote totals
     */
    public RaceResult(String raceName, Map<String, BigInteger> candidateResults) {

        this.raceName             = raceName;
        this.candidateResults       = candidateResults;
    }


    /**
     * @see play.db.ebean.Model.Finder#all()
     */
    public static List<RaceResult> all() {
        return find.all();
    }

    /**
     * Store a VotingRecord into the database.
     *
     * @param record        record to be stored
     */
    public static void create(RaceResult record) {
        record.save();
    }

    /**
     * Remove a VoteRecord from a database
     *
     * @param record        record to be removed
     */
    public static void remove(RaceResult record){
        record.delete();
    }

    /**
     * @return appropriately formatted String representation
     */
    public String toString() {
        return "Race Name: " + raceName + ", Candidate Results: " + candidateResults.toString();
    }

}