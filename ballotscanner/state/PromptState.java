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
 * A state which prompts user to scan a ballot
 */
public class PromptState extends AState {

    public static final PromptState SINGLETON = new PromptState("images/waiting_ballot.png", "Prompt State", "Place Ballot Under Scanner to Cast Ballot");
    /**
     * constructor for a prompt state
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
            throw new RuntimeException(e);
        }
        this.stateName = name;
        this.stateMessage = message;
    }


    public void updateState(BallotScannerUI context, int updateMode)
    {
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

    }

}
