package ballotscanner.state;

import ballotscanner.BallotScannerUI;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * A state which indicates that the @BAllotScanner is inactive and should not be used to scan ballots.
 */
public class InactiveState extends AState {

    /** Singleton pattern */
    public static final InactiveState SINGLETON = new InactiveState("images" + System.getProperty("file.separator") + "inactive.png",
                                                                    "Inactive State",
                                                                    "Scanner is inactive",
                                                                    "BallotScannerUI: Could not locate inactive image");

    /**
     * Constructor for an inactive state.
     *
     * @param image The image that this state will display
     * @param name the explicit name for this state
     * @param message the message this state will display
     */
    private InactiveState(String image, String name, String message, String error){
       super(image, name, message, error, "This is a Ballot Scanning Console.", "Console Currently Not Ready For Use.");
    }

    /**
     * @see ballotscanner.state.AState#displayScreen(ballotscanner.BallotScannerUI, String...)
     */
    public void displayScreen(BallotScannerUI context, String... params) {
        super.displayScreen(context);
    }

}
