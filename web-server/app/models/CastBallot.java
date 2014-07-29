package models;

import play.data.validation.Constraints.*;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Model of a Cast Ballot. This class is an entity of the web-server's database.
 *
 * @author Nelson Chen, Mitchell Douglass
 */
@Entity
public class CastBallot extends Model {

    public static final int BALLOTS_PER_PAGE = 100;

    @Id
    public Long id;

    @Required
    public String ballotid;

    public String hash;


    public CastBallot(String bid, String hash) {

        ballotid = bid;
        this.hash = hash;
    }

    /**
     * This member object is a finder that aids in retrieving Cast Ballots by their ballot IDs
     */
    public static Finder<Long, CastBallot> find = new Finder<Long, CastBallot>(Long.class, CastBallot.class);

    /**
     * @see play.db.ebean.Model.Finder#all()
     */
    public static List<CastBallot> all() {
        return find.all();
    }

    /**
     * A database lookup for a Cast Ballot by Ballot ID.
     *
     * @param bid       the ballot ID
     * @return          the matching Cast Ballot or null if not found
     */
    public static CastBallot getBallot(String bid){
        return find.where().ieq("ballotid", bid).findUnique();
    }

/*
    todo: implement for paginated viewing
    public static List<CastBallot> getBallotList(int pageNum) {
        return null;
    }
*/
    /**
     * Saves a ballot into the ebean database
     *
     * @param ballot        ballot to be saved
     */
    public static void create(CastBallot ballot) {
        ballot.save();
    }

    /**
     * removes a Ballot from the database
     *
     * @param ballot        ballot to be removed
     */
    public static void remove(CastBallot ballot){
        ballot.delete();
    }

    /**
     * @deprecated use remove instead.
     */
    public static void delete(Long id) {
        find.ref(id).delete();
    }
}
