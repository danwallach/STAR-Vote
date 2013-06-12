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

    private Map<Integer, String> ballotByPin = new HashMap<Integer, String>();       //Holds all active pins and corresponding ballot location
    private Map<Integer, String> ballotByPrecinct = new HashMap<Integer, String>();       //Holds all precincts and corresponding ballot location


    public int generatePin(int precinct){
        Random rand = (new Random());
        int pin = rand.nextInt(10000);
        while(ballotByPin.containsKey(pin))
            pin = rand.nextInt(10000);
        ballotByPin.put(pin, ballotByPrecinct.get(precinct));
        return pin;
    }

    public String getBallotByPin(int pin){
        String s = null;
        if(ballotByPin.containsKey(pin)){
            s = ballotByPin.get(pin);
            ballotByPin.remove(pin);
        }
        return s;
    }

    public void addBallot(int precinct, String ballot){
        ballotByPrecinct.put(precinct, ballot);
    }

    public Integer[] getSelections(){
        return (Integer[])ballotByPrecinct.keySet().toArray(new Integer[0]);
    }

    public Integer getInitialSelection(){
        Iterator i = ballotByPrecinct.keySet().iterator();
        return (Integer) i.next();
    }
}
