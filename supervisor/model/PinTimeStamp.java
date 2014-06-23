package supervisor.model;

/**
 * Keeps track of how long a pin has been issued and expires the pin if pin is older than
 * lifeTimeInSeconds
 */
class PinTimeStamp{

    /** The default lifespan for a PIN, in seconds */
    private static final int DEFAULT_LIFE_TIME = 180;

    /** The time that this PIN came into the world */
    private long startTime;

    /** The lifespan of the PIN in seconds */
    private int lifeTimeInSeconds;

    /**
     * Constructor
     *
     * @param lt the lifespan of the PIN in seconds
     */
    @SuppressWarnings("SameParameterValue")
    public PinTimeStamp(int lt){
        startTime = System.currentTimeMillis();
        lifeTimeInSeconds = lt;
    }

    /**
     * Default constructor, uses the defualt lifespan
     */
    public PinTimeStamp(){
        this(DEFAULT_LIFE_TIME);
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
