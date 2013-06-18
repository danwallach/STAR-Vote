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
 * A state which prompts user to scan a ballot.
 */
public class PromptState extends AState {

    public static final PromptState SINGLETON = new PromptState("images/waiting_ballot.png", "Prompt State", "Place Ballot Under Scanner to Cast Ballot");
    /**
     * Constructor for a prompt state.
     * @param image
     * @param name
     * @param message
     */
    private PromptState(String image, String name, String message){
        try
        {
            BufferedImage si = ImageIO.read(new File(image));
            this.stateImage = si;
        }
        catch (IOException e)
        {
            System.out.println("BallotScannerUI: Could not locate waiting image");
            this.stateImage = null;
        }
        this.stateName = name;
        this.stateMessage = message;
    }


    public void displayScreen(BallotScannerUI context, Object... params) {
        context.userInfoPanel.clearMessages();
        context.userInfoPanel.addMessage("This is a Ballot Scanning Console.");
        context.userInfoPanel.addMessage("Place Ballot Under Scanner to Cast Ballot.");
        context.responseImage = stateImage;
        context.updateFrameComponents();
    }

    public void updateState(BallotScannerUI context, int updateMode)
    {
        if(updateMode == 1)
        {
            context.state = AcceptState.SINGLETON;
            AcceptState.SINGLETON.resetStateStartTime();
            System.out.println("Transitioning from PROMPT STATE to ACCEPT STATE!");
            return;
        }
        if(updateMode == 2)
        {
            context.state = RejectState.SINGLETON;
            RejectState.SINGLETON.resetStateStartTime();
            System.out.println("Transitioning from PROMPT STATE to REJECT STATE!");
            return;
        }
        if(updateMode == 3)
        {
            context.state = PromptState.SINGLETON;
            System.out.println("Transitioning from PROMPT STATE to PROMPT STATE!");
            return;
        }
    }

}
