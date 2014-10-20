package ballotscanner.state;

import ballotscanner.BallotScannerUI;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * A state which prompts user to scan a ballot.
 */
public class PromptState extends AState {

    public static final PromptState SINGLETON = new PromptState("images" + System.getProperty("file.separator") + "waiting_ballot.png",
                                                                "Prompt State",
                                                                "Place Ballot Under Scanner to Cast Ballot",
                                                                "BallotScannerUI: Could not locate waiting image");
    /**
     * Constructor for a prompt state.
     *
     * @param image The image that this state will display
     * @param name the explicit name for this state
     * @param message the message this state will display
     */
    private PromptState(String image, String name, String message, String error){
        super(image, name, message, error, "This is a Ballot Scanning Console.", "Place Ballot Under Scanner to Cast Ballot.");
    }

    /**
     * @see AState#displayScreen(ballotscanner.BallotScannerUI, String...)
     */
    public void displayScreen(BallotScannerUI context, String... params) {
        super.displayScreen(context);
    }



}
