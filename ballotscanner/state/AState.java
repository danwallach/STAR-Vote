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
     * Gets the image for this state
     *
     * @return - an image representing this state
     */
    public BufferedImage getStateImage(){

        return stateImage;
    }

    /**
     * Gives the name of the state
     *
     * @return
     */
    public String getStateName(){

        return stateName;
    }

    /**
     * Gives the state's message
     *
     * @return
     */
    public String getStateMessage(){

        return stateMessage;
    }

    /**
     * Adds messages and updates the frame.
     * @param context
     * @param params
     */
    public abstract void displayScreen(BallotScannerUI context, Object... params);

    /**
     * Updates the state, if needed.
     * @param context
     * @param updateMode
     */
    public abstract void updateState(BallotScannerUI context, int updateMode);

}
