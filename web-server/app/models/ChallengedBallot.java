package models;

import play.data.validation.Constraints.*;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Model of a Challenged Ballot on the Web-Server. An entity of the database.
 *
 * @author Nelson Chen, Mitchell Douglass
 */

@Entity
public class ChallengedBallot extends Model {
    public static final int BALLOTS_PER_PAGE = 100;

    /**
     * This member object is a finder that aids in retrieving Cat Ballots by their ballot IDs
     */
    public static Finder<Long, ChallengedBallot> find = new Finder<Long, ChallengedBallot>(Long.class, ChallengedBallot.class);

    @Id
    public Long id;

    @Required
    public String ballotid;

    public String hash;

    public String precinct;

    @Column(length = 8000)
    public String decryptedBallot;


    public ChallengedBallot(String bid,String precinct, String hash, String decryptedBallot) {
        ballotid = bid;
        this.hash = hash;
        this.precinct = precinct;
        this.decryptedBallot = decryptedBallot;
    }

    /**
     * @see play.db.ebean.Model.Finder#all()
     */
    public static List<ChallengedBallot> all() {
        return find.all();
    }

    /**
     * Database lookup for a Challenged Ballot with the giver BID.
     *
     * @param bid ballot ID
     * @return the corresponding ballot or null if non-existent
     */
    public static ChallengedBallot getBallot(String bid) {
        ChallengedBallot ballot = find.where().ieq("ballotid", bid).findUnique();
        System.out.println(ballot);
        return ballot;
    }

    /**
     *  Not Used
     */
    private static String decryptBallot(ChallengedBallot challengedBallot){
//      fixme: make this work
        return null;
    }

//        todo: implement for paginated viewing
//  public static List<ChallengedBallot> getBallotList(int pageNum) {
//      return null;
//  }

    /**
     * Store a Challenged Ballot into the database.
     * @param ballot ballot to be stored
     */
    public static void create(ChallengedBallot ballot) {
        System.out.println("Saved Ballot");
        ballot.save();
    }

    /**
     * Remove a ballot from a database
     * @param ballot ballot to be removed
     */
    public static void remove(ChallengedBallot ballot){
        ballot.delete();
    }

    /**
     * @deprecated use remove
     */
    public static void delete(Long id){
        find.ref(id).delete();
    }

    /**
     * @return appropriately formatted String representation
     */
    public String toString(){
         return ballotid + ":" + hash + ":" + decryptedBallot;
    }
}