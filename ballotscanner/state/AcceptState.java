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
     * constructor for a prompt state
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
            throw new RuntimeException(e);
        }
        this.stateName = name;
        this.stateMessage = message;
    }


    public void nextState(BallotScannerUI context, boolean whichState)
    {
        if (System.currentTimeMillis() - stateStartTime > stateActiveDelay)
            context.state = PromptState.SINGLETON;
    }

}
