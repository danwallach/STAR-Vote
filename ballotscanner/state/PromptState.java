package ballotscanner.state;

import ballotscanner.BallotScannerUI;
import ballotscanner.ElectionInfoPanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * A state which prompts user to scan a ballot.
 */
public class PromptState extends AState {

    public static final PromptState SINGLETON = new PromptState("images/waiting_ballot.png",
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
        super(image, name, message, error);
    }

    /**
     * @see ballotscanner.state.AState#displayScreen(ballotscanner.BallotScannerUI, Object...)
     */
    public void displayScreen(BallotScannerUI context, Object... params) {
        super.displayScreen(context,
                            "This is a Ballot Scanning Console.",
                            "Place Ballot Under Scanner to Cast Ballot.");
    }



}
