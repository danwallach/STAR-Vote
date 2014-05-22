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
 * A state which prompts user that a ballot has been accepted.
 */
public class AcceptState extends AState {

    private long stateStartTime = 0;
    private final long stateActiveDelay = 5000;

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
        super(image, name, message, error);
    }

    /**
     * Reset the start time to now so that the state times out @stateActiveDelay seconds from now
     */
    public void resetStateStartTime()
    {
        stateStartTime = System.currentTimeMillis();
    }

    /**
     * @see ballotscanner.state.AState#displayScreen(ballotscanner.BallotScannerUI, Object...)
     */
    public void displayScreen(BallotScannerUI context, Object... params) {
        super.displayScreen(context,
                            "Your Vote Will be Counted",
                            "Thank You for Voting!");

    }

}
