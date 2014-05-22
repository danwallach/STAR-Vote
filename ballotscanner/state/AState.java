package ballotscanner.state;

import ballotscanner.BallotScannerUI;

import java.awt.image.BufferedImage;

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
     * Adds messages to and updates the frame.
     * @param context the @BallotScannerUI that is in this state
     * @param params Any necessary parameters to allow this state to display itself
     */
    public abstract void displayScreen(BallotScannerUI context, Object... params);



    /**
     * Updates the state, changing to the state specified.
     *
     * @param context the context in which this state exists
     * @param updateMode the way this state should update, will be either InactiveState,
     *                   AcceptState, PromptState, or RejectState.
     */
    public abstract void updateState(BallotScannerUI context, int updateMode);

}
