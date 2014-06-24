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


    /** The default lifespan for a PIN, in seconds */
    private static final int DEFAULT_LIFE_TIME = 180;

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
        return generatePIN(precinctID, DEFAULT_LIFE_TIME);
    }

    /**
     * this method is used to generate a PIN to be stored and used by a voter
     *
     * @param precinctID      3-digit precinct number
     * @param lifespan        the life time, in seconds, of the PIN
     * @return                new 5-digit pin as String
     */
    public String generatePIN(String precinctID, int lifespan) {

        /* Generate a random PIN */
        String PIN = PINFormat.format(rand.nextInt(100000));

        /* Ensure that we don't use a PIN that is already active */
        while(precinctIDs.containsKey(PIN))
            PIN = PINFormat.format(rand.nextInt(100000));

        /* Create a new time stamp on this pin */
        timeStamp.put(PIN, new PinTimeStamp(lifespan));
        precinctIDs.put(PIN, precinctID);

        return PIN;
    }

    /**
     * Checks that this PIN is a valid PIN.
     *
     * @param PIN       5-digit pin to be validated
     * @return          true if the PIN exists and has not yet expired, false otherwise
     */
    public boolean validatePIN(String PIN){

        System.out.println(PIN + ": " + precinctIDs.containsKey(PIN) + " | " + timeStamp.get(PIN).isValid());

        /* Check the timestamp of the PIN */
        if(timeStamp.get(PIN) == null || !timeStamp.get(PIN).isValid())
            precinctIDs.remove(PIN);

        return precinctIDs.containsKey(PIN);
    }

    /**
     * Use a PIN to get the precinctID. After use, a PIN is removed. This method
     * should only be called AFTER a separate call to validatePIN() so that the
     * result of validatePIN() can be explicitly known, although this method behaves
     * correctly if this is not done.
     *
     * @param PIN       5-digit pin to be validated
     * @return          the 3-digit precinct ID corresponding to that PIN or null
     *                  if this method was used without prior validation of PIN and
     *                  PIN was invalid at the time of calling.
     */
    public String usePIN(String PIN){

        /* Check for expiration and that the PIN exists */
        validatePIN(PIN);

        return precinctIDs.remove(PIN);
    }

    /**
     * Clears the random and map of PINs
     */
    public void clear() {
        SINGLETON = new PINValidator();
    }
}
