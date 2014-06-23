package supervisor.model;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Matthew Kindy II on 6/23/2014.
 */
public class PINValidator {

    public static PINValidator SINGLETON = new PINValidator();

    /** Maps every PIN to a time stamp so that the PIN can expire */
    private static Map<String, PinTimeStamp> timeStamp = new HashMap<>();

    /** A Map of PINs to Precinct IDs */
    private Map<String, String> precinctIDs;

    /** A decimal formatter for generating PINs */
    private DecimalFormat PINFormat = new DecimalFormat("00000");

    /** A random generator for generating PINs and hashing */
    private Random rand;

    private PINValidator(){
        rand = new Random();
        precinctIDs = new HashMap<>();
    }

    /**
     * this method is used to generate a PIN to be stored and used by a voter
     *
     * @param precinctID      3-digit precinct number
     * @return              new 5-digit pin as String
     */
    public String generatePIN(String precinctID) {

        /* TODO Review this code */
        String PIN = PINFormat.format(rand.nextInt(100000));

        /* Ensure that we don't use a PIN that is already active */
        while(precinctIDs.containsKey(PIN))
            PIN = PINFormat.format(rand.nextInt(100000));

        /* create a new time stamp on this pin */
        timeStamp.put(PIN, new PinTimeStamp());
        precinctIDs.put(PIN, precinctID);

        return PIN;
    }

    public boolean validatePIN(String PIN){
        return precinctIDs.containsKey(PIN) && timeStamp.get(PIN).isValid();
    }

    public String usePIN(String PIN){
        return precinctIDs.remove(PIN);
    }
}
