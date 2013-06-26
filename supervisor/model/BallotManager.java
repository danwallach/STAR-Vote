package supervisor.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: martinnikol
 * Date: 6/12/13
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class BallotManager {

    //manages the pins for the supervisor as well as all corresponding ballots

    private Map<Integer, PinTimeStamp> timeStamp = new HashMap<Integer, PinTimeStamp>();
    private Map<Integer, String> ballotByPin = new HashMap<Integer, String>();       //Holds all active pins and corresponding ballot location
    private Map<Integer, String> ballotByPrecinct = new HashMap<Integer, String>();       //Holds all precincts and corresponding ballot location
    private Random rand = (new Random());

    //generates a random pin and adds it to the list of pins and its corresponding ballot based on its precinct
    public int generatePin(int precinct){
        int pin = rand.nextInt(10000);


        while(ballotByPin.containsKey(pin))
            pin = rand.nextInt(10000);

        //create a new time stamp on this pin
        timeStamp.put(pin, new PinTimeStamp());
        ballotByPin.put(pin, ballotByPrecinct.get(precinct));

        return pin;
    }

    public int generateProvisionalPin(int precinct){
        int provisionalPin = rand.nextInt(10000);

        while(ballotByPin.containsKey(provisionalPin))
            provisionalPin = rand.nextInt(10000);

        timeStamp.put(provisionalPin, new PinTimeStamp());
        ballotByPin.put(provisionalPin, ballotByPin.get(precinct) + " - provisional");

        return provisionalPin;
    }

    //returns ballot mapped to pin and null if pin is not in Map
    public String getBallotByPin(int pin){
        String s = null;
        if(ballotByPin.containsKey(pin)){
            if(timeStamp.get(pin).isValid()){
                s = ballotByPin.get(pin);
            }
            ballotByPin.remove(pin);
        }
        return s;
    }

    //adds a newly sellected ballot to ballotByPrecinct
    public void addBallot(int precinct, String ballot){
        ballotByPrecinct.put(precinct, ballot);
    }

    //returns array of precincts
    public Integer[] getSelections(){
        return ballotByPrecinct.keySet().toArray(new Integer[0]);
    }

    //returns first precinct in set of precincts
    public Integer getInitialSelection(){
        Iterator i = ballotByPrecinct.keySet().iterator();
        return (Integer) i.next();
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
