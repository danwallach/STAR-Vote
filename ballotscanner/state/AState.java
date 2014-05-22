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
 * A default state
 */
public abstract class AState implements IState {

    BufferedImage stateImage;

    String stateName;
    String stateMessage;


    protected AState(String image, String name, String message, String error){
        try
        {
            BufferedImage si = ImageIO.read(ElectionInfoPanel.getFile(image));
            this.stateImage = si;
        }
        catch (IOException e)
        {
            System.err.println(error);
            this.stateImage = null;
        }
        this.stateName = name;
        this.stateMessage = message;
    }

    /**
     * @return an image representing this state
     */
    public BufferedImage getStateImage() {
        return stateImage;
    }

    /**
     * @return the name of the state
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * @return the state's message
     */
    public String getStateMessage() {
        return stateMessage;
    }



    /**
     * Updates the state, changing to the state specified.
     *
     * @param context the context in which this state exists
     * @param updateMode the way this state should update, will be either InactiveState,
     *                   AcceptState, PromptState, or RejectState.
     */
    public void updateState(BallotScannerUI context, int updateMode)
    {
        if(updateMode == BallotScannerUI.TO_INACTIVE_STATE)
        {
            context.state = InactiveState.SINGLETON;
            return;
        }
        if(updateMode == BallotScannerUI.TO_ACCEPT_STATE)
        {
            context.state = AcceptState.SINGLETON;
            AcceptState.SINGLETON.resetStateStartTime();
            return;
        }
        if(updateMode == BallotScannerUI.TO_REJECT_STATE)
        {
            context.state = RejectState.SINGLETON;
            RejectState.SINGLETON.resetStateStartTime();
            return;
        }
        if(updateMode == BallotScannerUI.TO_PROMPT_STATE)
        {
            context.state = PromptState.SINGLETON;
            return;
        }
    }

    /**
     * Adds messages to and updates the frame.
     * @param context the @BallotScannerUI that is in this state
     * @param params Any necessary parameters to allow this state to display itself
     */
    public void displayScreen(BallotScannerUI context, Object... params){
        context.userInfoPanel.clearMessages();

        for(Object message : params){
            context.userInfoPanel.addMessage(message.toString());
        }
        context.updateFrameComponents();
    }


}
