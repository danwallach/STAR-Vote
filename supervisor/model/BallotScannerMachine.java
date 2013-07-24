package supervisor.model;

import supervisor.model.AMachine;

/**
 * Concrete class for a BallotScanner Machine
 */
public class BallotScannerMachine extends AMachine {

    /**
     * Represents a supervisor that is active
     */
    public static final int ACTIVE = 6;

    /**
     * Represents a supervisor that is inactive
     */
    public static final int INACTIVE = 7;

    /**
     * represents this machine's battery level
     */
    private int battery;

    private int protectedCount;

    private int publicCount;

    /**
     * Constructs a new supervisor machine
     * @param serial the serial number of the ballotScanner
     */
    public BallotScannerMachine(int serial) {
        super(serial);
    }

    /**
     * @return the battery level, as an integer [0..100]
     */
    public int getBattery() {
        return battery;
    }

    /**
     * @return the protected count
     */
    public int getProtectedCount() {
        return protectedCount;
    }

    /**
     * @return the public count
     */
    public int getPublicCount() {
        return publicCount;
    }

    /**
     * Sets the battery level
     * @param battery the battery to set
     */
    public void setBattery(int battery) {
        this.battery = battery;
        obs.notifyObservers();
    }


    /**
     * Sets the machine's protected count
     * @param protectedCount the protectedCount to set
     */
    public void setProtectedCount(int protectedCount) {
        this.protectedCount = protectedCount;
        obs.notifyObservers();
    }

    /**
     * Sets the machine's public count
     * @param publicCount the publicCount to set
     */
    public void setPublicCount(int publicCount) {
        this.publicCount = publicCount;
        obs.notifyObservers();
    }
}
