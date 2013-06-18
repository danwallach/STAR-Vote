package ballotscanner.state;

import ballotscanner.BallotScannerUI;

import java.awt.*;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * A default state
 */
public abstract class AState implements IState {

    Image stateImage;

    String stateName;
    String stateMessage;

    /**
     * Gets the image for this state
     *
     * @return - an image representing this state
     */
    public Image getStateImage(){

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

    public abstract void nextState(BallotScannerUI context, boolean whichState);

}
