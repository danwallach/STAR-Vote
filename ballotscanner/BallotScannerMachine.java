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

  /**
   * Constructs a new supervisor machine
   * @param serial the serial number of the ballotscanner
   */
  public BallotScannerMachine(int serial) {
    super(serial);
  }
}
