package models;

import play.data.validation.Constraints.*;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;

@Entity
public class SupervisorRecord extends Model {
    
    @Id
    long id;
    
    public String record;
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