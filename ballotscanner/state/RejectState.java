package ballotscanner.state;

import ballotscanner.BallotScannerUI;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * A state which prompts user that a ballot has been rejected.
 */
public class RejectState extends AState {


    public static final RejectState SINGLETON = new RejectState("images" + System.getProperty("file.separator") + "reject_ballot.png",
                                                                "Reject State",
                                                                "Your ballot has been rejected and will not be counted.",
                                                                "BallotScannerUI: Could not locate reject image");
    /**
     * Constructor for a reject state.
     *
     * @see AState#AState(String, String, String, String, String[])
     */
    private RejectState(String image, String name, String message, String error){
        super(image, name, message, error,
                "Ballot has been rejected", "Hold Ballot Still Under the Scanner", "If This Problem Persists, Contact an Election Official");
    }

    /**
     * @see AState#displayScreen(ballotscanner.BallotScannerUI, String...)
     */
    public void displayScreen(BallotScannerUI context, String... params) {
        super.displayScreen(context);

    }


}
