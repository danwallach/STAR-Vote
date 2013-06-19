package ballotscanner;

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

  private boolean currentMachine;

  private int battery;

  /**
   * Constructs a new supervisor machine
   * @param serial the serial number of the ballotscanner
   * @param current whether the ballotscanner is the current ballotscanner
   */
  public BallotScannerMachine(int serial, boolean current) {
    super(serial);
    this.currentMachine = current;
  }

  /**
   * @return whether this is the current machine
   */
  public boolean isCurrentMachine() {
    return currentMachine;
  }

  public int getBattery(){
       return battery;
  }

  public void setBattery(int battery) {
      this.battery = battery;
      obs.notifyObservers();
  }

}
