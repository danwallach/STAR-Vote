package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Model of a SupervisorRecord on the Web-Server. An entity of the database.
 *
 * @author Matthew Kindy II
 */

@Entity
public class SupervisorRecord extends Model {
    
    @Id
    long id;

    @Column(columnDefinition = "TEXT")
    public String record;

    @JoinColumn(name="hash_id")
    public String hash;
    
    @ManyToOne
    public VotingRecord owner;
    
    public SupervisorRecord(VotingRecord parent, String hash, String record) {
        this.record = record;
        this.hash = hash;
        owner = parent;
    }
    
    /**
     * Store a SupervisorRecord into the database.
     *
     * @param record        record to be stored
     */
    public static void create(SupervisorRecord record) { 
        record.save();
    }

    /**
     * Remove a SupervsiorRecord from a database
     *
     * @param record        record to be removed
     */
    public static void remove(SupervisorRecord record){
        record.delete();
    }

    /**
     * @return appropriately formatted String representation
     */
    public String toString(){
         return id + ":" + hash + ":" + record;
    }

}