package supervisor.model;

import java.text.DecimalFormat;
import java.util.*;

/**
 * This class is used my the Model class to store relevant information about ballots, pins, precincts, and ballotids.
 * It is a collection of mappings that serve as a look-up table for various related entities. Used by the supervisor's
 * Model class. Also used for generation of unique 4-digit PINs and provisional pins.
 *
 * User: martinnikol
 * Date: 6/12/13
 * Time: 10:58 AM
 */
public class BallotManager {

    //manages the pins for the supervisor as well as all corresponding ballots

    private Map<String, PinTimeStamp> timeStamp = new HashMap<String, PinTimeStamp>();
    private Map<String, String> ballotByPin = new HashMap<String, String>();       //Holds all active pins and corresponding ballot location
    private Map<String, String> ballotByPrecinct = new HashMap<String, String>();       //Holds all precincts and corresponding ballot location
    private Map<String, String> precinctByBallot = new HashMap<String, String>();
    private Map<String, String> precinctByBID = new HashMap<String, String>();

    private Random rand = (new Random());
    private DecimalFormat decimalFormat = new DecimalFormat("0000");

    /**
     * Generates a random, unique pin and a corresponding timestamp and adds them to corresponding internal storage
     * @param precinct the 3-digit precinct of a voter using a particular ballot
     * @return String representation of generated pin
     */
    public String generatePin(String precinct){
        String pin = decimalFormat.format(rand.nextInt(10000));


        while(ballotByPin.containsKey(pin))
            pin = decimalFormat.format(rand.nextInt(10000));

        // TODO: This code seems to be useless and is confusing. Is this behavior not implemented in method addBallot()?

        String ballot = ballotByPrecinct.remove(precinct);

        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);

        //create a new time stamp on this pin
        timeStamp.put(pin, new PinTimeStamp());
        ballotByPin.put(pin, ballotByPrecinct.get(precinct));

        return pin;
    }

    /**
     * Generates pins used by voters operating under a provisional vote. Provisional voting is a special case in voting.
     *
     * @param precinct 3-digit value identifying regional precinct of voter
     * @return String representation of generated pin
     * @see <a href="http://en.wikipedia.org/wiki/Provisional_voting">Wikipeida article: provisional voting</>
     */
    public String generateProvisionalPin(String precinct){
        System.err.println(">>> Generating provisional pin for precinct " + precinct);
        String provisionalPin = decimalFormat.format(rand.nextInt(10000));

        while(ballotByPin.containsKey(provisionalPin))
            provisionalPin = decimalFormat.format(rand.nextInt(10000));

        //Move the mappings from one to the other
        String ballot = ballotByPrecinct.remove(precinct);


        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);



        timeStamp.put(provisionalPin, new PinTimeStamp());
        ballotByPin.put(provisionalPin, ballot);

        return provisionalPin;
    }

    /**
     * @param pin 4-digit PIN, each associated to a ballot
     * @return ballot mapped to pin and null if no mapping present for this pin
     */
    public String getBallotByPin(String pin){
        String s = null;
        if(ballotByPin.containsKey(pin)){
            if(timeStamp.get(pin).isValid()){
                s = ballotByPin.get(pin);
            }
            ballotByPin.remove(pin);
        }
        return s;
    }

    /**
     * @param ballot absolute path to ballot
     * @return String representation of 3-digit precinct associated with ballot
     */
    public String getPrecinctByBallot(String ballot){
        if(precinctByBallot.containsKey(ballot)){
           return precinctByBallot.get(ballot);

        }
        return null;
    }

    /**
     * Creates a mapping between a ballot and it's respective precinct
     *
     * @param precinct 3-digit precint
     * @param ballot absolute file path to ballot being used
     */
    public void addBallot(String precinct, String ballot){
        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.put(ballot, precinct);
        ballotByPrecinct.put(precinct+"-provisional", ballot);
    }

    /**
     * Creates mapping from bID to the precint
     * @param bID ballotid, one associated with each voting session
     * @param precinct 3-digit precint
     */
    public void setPrecinctByBID(String bID, String precinct){
        System.out.println("BAllot manager setting BID: " + bID + " to precinct: "+ precinct);
        precinctByBID.put(bID, precinct);
    }

    /**
     * @param bID ballotid associated to voting session
     * @return 3-digit precinct representation or null if no mapping present
     */
    public String getPrecinctByBID(String bID){
        if(precinctByBID.containsKey(bID)){
            return precinctByBID.get(bID);
        }
        return null;
    }

    /**
     * @return array of precinct values
     */
    public String[] getSelections(){
        return ballotByPrecinct.keySet().toArray(new String[0]);
    }

    /**
     * @return first precinct in set of precincts
     */
    public String getInitialSelection(){
        Iterator i = ballotByPrecinct.keySet().iterator();
        return (String) i.next();
    }

    /**
     * Debugging method. Displays contents of Map mapping ballotids to precincts
     */
    public void testMapPrint(){
        System.out.println(precinctByBID);
    }//TODO: remove this?
}


/**
 * Simple Implementation of a time stamp that spoils after a specified number of seconds. A time stamp is associated
 * with each pin and indicates that a pin is invalidated if the time period has expired.
 */
class PinTimeStamp{

    private static final int DEFAULT_LIFE_TIME = 180;

    private long startTime;
    private int lifeTimeInSeconds;

    /**
     * Constructs a new time stamp with a specified life time
     *
     * @param lt lifetime of timestamp, in seconds
     */
    public PinTimeStamp(int lt){
        startTime = System.currentTimeMillis();
        lifeTimeInSeconds = lt;
    }

    /**
     * constructs a time stamp with a default time stamp of 180 seconds
     */
    public PinTimeStamp(){
        this(DEFAULT_LIFE_TIME);
    }

    /**
     * @return returns true if (currentTime - constructionTime) is less than this PinTimeStamps lifespan in seconds
     */
    public boolean isValid(){
        return (System.currentTimeMillis()-startTime) < 1000*lifeTimeInSeconds;
    }
}
