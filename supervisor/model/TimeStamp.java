package supervisor.model;

/**
 * Keeps track of how long anything been issued and expires if it is older than
 * lifeTimeInSeconds
 */
class TimeStamp {


    /** The time that this PIN came into the world */
    private long startTime;

    /** The lifespan of the PIN in seconds */
    private int lifeTimeInSeconds;

    /**
     * Constructor
     *
     * @param seconds the lifespan of the PIN in seconds
     */
    public TimeStamp(int seconds){
        startTime = System.currentTimeMillis();
        lifeTimeInSeconds = seconds;
    }

    /**
     * Determines if a PIN is "alive"
     *
     * @return true if the PIN hasn't been around for more than lifeTimeInSeconds time, false otherwise
     */
    public boolean isValid(){
        return (System.currentTimeMillis()-startTime) < 1000*lifeTimeInSeconds;
    }
}
