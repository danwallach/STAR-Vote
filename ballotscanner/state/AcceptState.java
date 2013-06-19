package ballotscanner.state;

import ballotscanner.BallotScannerUI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public static final AcceptState SINGLETON = new AcceptState("images/accept_ballot.png", "Accept State", "Your ballot has been cast and will be counted.");
    /**
     * Constructor for an accept state.
     * @param image
     * @param name
     * @param message
     */
    private AcceptState(String image, String name, String message){
        try
        {
            BufferedImage si = ImageIO.read(new File(image));
            this.stateImage = si;
        }
        catch (IOException e)
        {
            System.out.println("BallotScannerUI: Could not locate accept image");
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
//        context.userInfoPanel.addMessage("Ballot " + Integer.parseInt(params[0].toString())  + " Confirmed and Cast");
        context.userInfoPanel.addMessage("Your Vote Will be Counted");
        context.userInfoPanel.addMessage("Thank You for Voting!");
        context.responseImage = stateImage;
        context.updateFrameComponents();
    }

    public void updateState(BallotScannerUI context, int updateMode)
    {
        if (System.currentTimeMillis() - stateStartTime > stateActiveDelay)
        {
            context.state = PromptState.SINGLETON;
            System.out.println("Transitioning from ACCEPT STATE to PROMPT STATE!");
            return;
        }
        if(updateMode == -1)
        {
            context.state = InactiveState.SINGLETON;
            System.out.println("Transitioning from ACCEPT STATE to INACTIVE STATE!");
            return;
        }
        if(updateMode == 1)
        {
            context.state = AcceptState.SINGLETON;
            AcceptState.SINGLETON.resetStateStartTime();
            System.out.println("Transitioning from ACCEPT STATE to ACCEPT STATE!");
            return;
        }
        if(updateMode == 2)
        {
            context.state = RejectState.SINGLETON;
            RejectState.SINGLETON.resetStateStartTime();
            System.out.println("Transitioning from ACCEPT STATE to REJECT STATE!");
            return;
        }
        if(updateMode == 3)
        {
            context.state = PromptState.SINGLETON;
            System.out.println("Transitioning from ACCEPT STATE to PROMPT STATE!");
            return;
        }
    }

}
