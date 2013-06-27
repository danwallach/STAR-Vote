package supervisor.model;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: martinnikol
 * Date: 6/12/13
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class BallotManager {

    //manages the pins for the supervisor as well as all corresponding ballots

    private Map<String, PinTimeStamp> timeStamp = new HashMap<String, PinTimeStamp>();
    private Map<String, String> ballotByPin = new HashMap<String, String>();       //Holds all active pins and corresponding ballot location
    private Map<String, String> ballotByPrecinct = new HashMap<String, String>();       //Holds all precincts and corresponding ballot location
    private Map<String, String> precinctByBallot = new HashMap<String, String>();

    private Random rand = (new Random());

    //generates a random pin and adds it to the list of pins and its corresponding ballot based on its precinct
    public String generatePin(String precinct){
        System.out.println(">>> Generating pin for precinct " + precinct);
        String pin = "" + rand.nextInt(10000);


        while(ballotByPin.containsKey(pin))
            pin = rand.nextInt(10000) + "";

        String ballot = ballotByPrecinct.remove(precinct);

        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);

        //create a new time stamp on this pin
        timeStamp.put(pin, new PinTimeStamp());
        ballotByPin.put(pin, ballotByPrecinct.get(precinct));

        return pin;
    }

    public String generateProvisionalPin(String precinct){
        System.err.println(">>> Generating provisional pin for precinct " + precinct);
        String provisionalPin = rand.nextInt(10000) + "";

        while(ballotByPin.containsKey(provisionalPin))
            provisionalPin = rand.nextInt(10000) + "";

        //Move the mappings from one to the other
        String ballot = ballotByPrecinct.remove(precinct);


        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.remove(ballot);
        precinctByBallot.put(ballot, precinct);



        timeStamp.put(provisionalPin, new PinTimeStamp());
        ballotByPin.put(provisionalPin, ballot);

        return provisionalPin;
    }

    //returns ballot mapped to pin and null if pin is not in Map
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
     * Gets a precinct name via a ballot
     */
    public String getPrecinctByBallot(String ballot){
        if(precinctByBallot.containsKey(ballot)){
           return precinctByBallot.get(ballot);

        }
        return null;
    }

    //adds a newly selected ballot to ballotByPrecinct
    public void addBallot(String precinct, String ballot){
        ballotByPrecinct.put(precinct, ballot);
        precinctByBallot.put(ballot, precinct);
        ballotByPrecinct.put(precinct+"-provisional", ballot);

    }

    //returns array of precincts
    public String[] getSelections(){
        return ballotByPrecinct.keySet().toArray(new String[0]);
    }

    //returns first precinct in set of precincts
    public String getInitialSelection(){
        Iterator i = ballotByPrecinct.keySet().iterator();
        return (String) i.next();
    }
}


// keeps track of how long a pin has been issued and expires the pin if pin is older than
// lifeTimeInSeconds

class PinTimeStamp{

    private static final int DEFAULT_LIFE_TIME = 180;

    private long startTime;
    private int lifeTimeInSeconds;

    public PinTimeStamp(int lt){
        startTime = System.currentTimeMillis();
        lifeTimeInSeconds = lt;
    }

    public PinTimeStamp(){
        this(DEFAULT_LIFE_TIME);
    }

    public boolean isValid(){
        return (System.currentTimeMillis()-startTime) < 1000*lifeTimeInSeconds;
    }
}
