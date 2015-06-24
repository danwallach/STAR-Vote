package models;

import crypto.AHomomorphicCiphertext;
import org.apache.commons.codec.binary.Base64;
import play.db.ebean.Model;
import supervisor.model.Precinct;

import javax.persistence.*;
import java.io.*;
import java.util.*;

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
    public static Finder<Long, VotingRecord> find = new Finder<>(Long.class, VotingRecord.class);

    @Id
    public long id;
    
    public String precinctID;

    @OneToMany(mappedBy="owner", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @MapKey(name="hash")
    public Map<String, SupervisorRecord> supervisorRecords = new HashMap<>();
    
    public boolean isConflicted;
    public boolean isPublished=false;

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
            supervisorRecords.put(entry.getKey(), new SupervisorRecord(this, entry.getKey(), recordToString(entry.getValue())));
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
    
    private Map<String, Precinct> StringToRecord(String s) {
        
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
    public VotingRecord resolveConflict(String chosenHash) {
        
        for (Map.Entry<String, SupervisorRecord> entry : supervisorRecords.entrySet()) {
            if (!entry.getKey().equals(chosenHash)) {
                SupervisorRecord.remove(entry.getValue());
            }
        }

        isConflicted = false;

        this.save();

        return this;
    }

    /**
     * Publishes this VotingRecord which makes its PrecinctMap available
     */
    public void publish() {
        
        /* Check that it is valid to publish this */
        if (!isConflicted) {
            isPublished = true;
            this.save();
        }
    }

    /**
     * Retrieves the map of Precincts for this VotingRecord if there are no conflicts
     * and the VotingRecord has been published
     */
    public Map<String, Precinct> getPrecinctMap() {
        
        if (isPublished) {

            Collection<SupervisorRecord> var = supervisorRecords.values();
            List<SupervisorRecord> sr = Arrays.asList(var.toArray(new SupervisorRecord[var.size()]));
        
            if (sr.size()>1) return null;
            else             return StringToRecord(sr.get(0).record);
        }
        else return null;
    }

    /**
     * @return      a list of the Supervisor hashes associated with their voting records
     */
    public ArrayList<String> getHashes() {

        System.out.println(supervisorRecords.size());
        return new ArrayList<>(supervisorRecords.keySet());
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
     * @return      the list of VotingRecords that are not conflicted and not published
     */
    public static List<VotingRecord> getUnpublished() {
        return find.where().eq("isConflicted", false).eq("isPublished", false).findList();
    }

    /**
     * @return      the list of VotingRecords that are published
     */
    public static List<VotingRecord> getPublished() {
        return find.where().eq("isPublished", true).findList();
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
         return id + ":" + supervisorRecords.size() + ":" + isConflicted + ":" + isPublished;
    }
    
}