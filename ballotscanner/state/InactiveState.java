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
 * A state which prompts user that scanner is inactive.
 */
public class InactiveState extends AState {

    public static final InactiveState SINGLETON = new InactiveState("images/inactive.png", "Inactive State", "Scanner is inactive");
    /**
     * Constructor for an inactive state.
     * @param image
     * @param name
     * @param message
     */
    private InactiveState(String image, String name, String message){
        try
        {
            BufferedImage si = ImageIO.read(ElectionInfoPanel.getFile(image));
            this.stateImage = si;
        }
        catch (IOException e)
        {
            System.err.println("BallotScannerUI: Could not locate inactive image");
            this.stateImage = null;
        }
        this.stateName = name;
        this.stateMessage = message;
    }

    public void displayScreen(BallotScannerUI context, Object... params) {
        context.userInfoPanel.clearMessages();
        context.userInfoPanel.addMessage("This is a Ballot Scanning Console.");
        context.userInfoPanel.addMessage("Console Currently Not Ready For Use.");
        //context.responseImage = stateImage;
        context.updateFrameComponents();
    }

    public void updateState(BallotScannerUI context, int updateMode)
    {
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
