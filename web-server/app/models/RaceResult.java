package models;

import play.db.ebean.Model;

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

    @JoinColumn(name="race_id")
    public String raceName;

    @ElementCollection
    /* TODO use the below if the key is broken for some reason */
    //@MapKeyColumn(name="name")
    //@Column(name="value")
    public Map<String, Integer> candidateResults = new HashMap<>();

    @ManyToOne
    public DecryptedResult owner;

    /**
     * Constructor
     *
     * @param parent                the DecryptedResult (precinct) with which this RaceResult (race) is associated
     * @param raceName              the name/id of the race for this RaceResult
     * @param candidateResults      the map of candidates to their vote totals
     */
    public RaceResult(DecryptedResult parent, String raceName, Map<String, Integer> candidateResults) {

        owner                   = parent;
        this.raceName           = raceName;
        this.candidateResults   = candidateResults;
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