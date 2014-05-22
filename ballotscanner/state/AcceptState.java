package ballotscanner.state;

import ballotscanner.BallotScannerUI;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * A state which prompts user that a ballot has been accepted.
 */
public class AcceptState extends AState {


    public static final AcceptState SINGLETON = new AcceptState("images/accept_ballot.png",
                                                                "Accept State",
                                                                "Your ballot has been cast and will be counted.",
                                                                "BallotScannerUI: Could not locate accept image");
    /**
     * Constructor for an accept state.
     *
     * @param image The image that this state will display
     * @param name the explicit name for this state
     * @param message the message this state will display
     */
    private AcceptState(String image, String name, String message, String error){
        super(image, name, message, error, "Your Vote Will be Counted", "Thank You for Voting!");
    }

    /**
     * @see AState#displayScreen(ballotscanner.BallotScannerUI, String...)
     */
    public void displayScreen(BallotScannerUI context, String... params) {
        super.displayScreen(context);

    }

}
