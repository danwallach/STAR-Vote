package ballotscanner.state;

import ballotscanner.BallotScannerUI;
import ballotscanner.ElectionInfoPanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public static final RejectState SINGLETON = new RejectState("images/reject_ballot.png", "Reject State", "Your ballot has been rejected and will not be counted.");
    /**
     * Constructor for a reject state.
     * @param image
     * @param name
     * @param message
     */
    private RejectState(String image, String name, String message){
        try
        {
            BufferedImage si = ImageIO.read(ElectionInfoPanel.getFile(image));
            this.stateImage = si;
        }
        catch (IOException e)
        {
            System.err.println("BallotScannerUI: Could not locate reject image");
            this.stateImage = null;
        }
        this.stateName = name;
        this.stateMessage = message;
    }

    public void resetStateStartTime()
    {
        stateStartTime = System.currentTimeMillis();
    }

    public void displayScreen(BallotScannerUI context, Object... params) {
        context.userInfoPanel.clearMessages();
        context.userInfoPanel.addMessage("Ballot has been rejected");
        context.userInfoPanel.addMessage("Hold Ballot Still Under the Scanner");
        context.userInfoPanel.addMessage("If This Problem Persists, Contact an Election Official");
//        context.responseImage = stateImage;
        context.updateFrameComponents();
    }

    public void updateState(BallotScannerUI context, int updateMode)
    {
        if (System.currentTimeMillis() - stateStartTime > stateActiveDelay)
        {
            context.state = PromptState.SINGLETON;
            return;
        }
        if(updateMode == -1)
        {
            context.state = InactiveState.SINGLETON;
            return;
        }
        if(updateMode == 1)
        {
            context.state = AcceptState.SINGLETON;
            AcceptState.SINGLETON.resetStateStartTime();
            return;
        }
        if(updateMode == 2)
        {
            context.state = RejectState.SINGLETON;
            RejectState.SINGLETON.resetStateStartTime();
            return;
        }
        if(updateMode == 3)
        {
            context.state = PromptState.SINGLETON;
            return;
        }
    }

}
