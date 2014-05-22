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
 * A state which prompts user that a ballot has been rejected.
 */
public class RejectState extends AState {

    private long stateStartTime = 0;
    private final long stateActiveDelay = 5000;

    public static final RejectState SINGLETON = new RejectState("images/reject_ballot.png",
                                                                "Reject State",
                                                                "Your ballot has been rejected and will not be counted.",
                                                                "BallotScannerUI: Could not locate reject image");
    /**
     * Constructor for a reject state.
     *
     * @see ballotscanner.state.AState#AState(String, String, String, String)
     */
    private RejectState(String image, String name, String message, String error){
        super(image, name, message, error);
    }

    public void resetStateStartTime()
    {
        stateStartTime = System.currentTimeMillis();
    }

    /**
     * @see ballotscanner.state.AState#displayScreen(ballotscanner.BallotScannerUI, Object...)
     */
    public void displayScreen(BallotScannerUI context, Object... params) {
        super.displayScreen(context,
                            "Ballot has been rejected",
                            "Hold Ballot Still Under the Scanner",
                            "If This Problem Persists, Contact an Election Official");

    }


}
