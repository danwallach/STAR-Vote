package models;

import play.data.validation.Constraints.*;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.*;
import supervisor.model.Precinct;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Model of a VotingRecord on the Web-Server. An entity of the database.
 *
 * @author Matthew Kindy II
 */

@Entity
public class VotingRecord extends Model {

    /**
     * This member object is a finder that aids in retrieving VotingRecords by their precinctIDs and conflict status
     */
    public static Finder<String, VotingRecord> find = new Finder<String, VotingRecord>(String.class, VotingRecord.class);

    @Id
    long id;
    
    public String precinctID;

    public Map<String, String> supervisorRecords = new HashMap<>();
    
    public boolean isConflicted;

    /**
     * Constructor
     *
     * @param precinctID        the ID of the precinct from which this record was collected
     * @param records           the Map of supervisor hash to voting record (if more than one
     *                          hash exist, they are in conflict)
     */
    public VotingRecord(String precinctID, Map<String, Map<String, Precinct>> records) {

        this.precinctID = precinctID;
        isConflicted = (records.size() > 1);
        
        for (Map.Entry<String, Map<String, Precinct>> entry : records.entrySet())
            supervisorRecords.put(entry.getKey(), recordToString(entry.getValue()));
    }


    private String recordToString(Map<String, Precinct> s) {
        
        String encoded = null;
  
        try {
            
           ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
           ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
           
           objectOutputStream.writeObject(s);
           objectOutputStream.close();
           
           encoded = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
        } 
        catch (IOException e) { e.printStackTrace(); }
        
        return encoded;
    }
    
    private Map<String, Precinct> StringtoRecord(String s) {
        
        byte[] bytes = Base64.decodeBase64(s.getBytes());
        Map<String, Precinct> record = null;
  
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            record = (Map<String, Precinct>)objectInputStream.readObject();
        } 
        catch (IOException | ClassNotFoundException | ClassCastException e) { e.printStackTrace(); } 
  
        return record;
    }

    /**
     * Resolves the VotingRecord by deleting all but the chosen voting record
     *
     * @param chosenHash    the hash relating to the voting record that was chosen to resolve this
     *                      conflicted VotingRecord
     */
    public void resolveConflict(String chosenHash) {
        
        // Map.Entry<String, Map<String, Precinct>> chosenEntry = null;
        
        // for (Map.Entry<String, Map<String, Precinct>> entry : records.entrySet())
        //     if (entry.getKey().equals(chosenHash))
        //         chosenEntry = entry;
            
        // records = new HashMap<String, Map<String, Precinct>>();
        // records.put(chosenEntry.getKey(), chosenEntry.getValue());
        
        isConflicted = false;
    }

    /**
     * @return      a list of the Supervisor hashes associated with their voting records
     */
    public ArrayList<String> getHashes() {
        // return new ArrayList<String>(records.keySet());
        ArrayList<String> hashes = new ArrayList<String>();
        
        hashes.add(id + "hash1");
        hashes.add(id + "hash2");
        hashes.add(id + "hash3");
        
        return hashes;
    }

    /**
     * @see play.db.ebean.Model.Finder#all()
     */
    public static List<VotingRecord> all() {
        return find.all();
    }

    /**
     * @return      the list of VotingRecords that are conflicted
     */
    public static List<VotingRecord> getConflicted() {
        return find.where().eq("isConflicted", true).findList();
    }

    /**
     * @return      the list of VotingRecords that are not conflicted
     */
    public static List<VotingRecord> getNonConflicted() {
        return find.where().eq("isConflicted", false).findList();
    }

    /**
     * Database lookup for a VotingRecord with the given precinctID.
     *
     * @param precinctID       the ID of the precinct from which this record was collected
     * @return                 the corresponding VoteRecord or null if non-existent
     */
    public static VotingRecord getRecord(String precinctID) {
        return find.where().ieq("precinctID", precinctID).findUnique();
    }

    /**
     * Store a VotingRecord into the database.
     *
     * @param record        record to be stored
     */
    public static void create(VotingRecord record) { 
        record.save();
    }

    /**
     * Remove a VoteRecord from a database
     *
     * @param record        record to be removed
     */
    public static void remove(VotingRecord record){
        record.delete();
    }

    /**
     * @return appropriately formatted String representation
     */
    public String toString(){
         return id + ":" + supervisorRecords;
    }
}