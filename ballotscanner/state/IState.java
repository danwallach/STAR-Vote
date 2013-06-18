package ballotscanner.state;

import ballotscanner.BallotScannerUI;

import java.awt.*;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * An interface outlining the various states of a scanner UI
 */
public interface IState {

    /**
     * Gets the image for this state
     *
     * @return - an image representing this state
     */
    public Image getStateImage();

    /**
     * Gives the name of the state
     *
     * @return
     */
    public String getStateName();

    /**
     * Gives the state's message
     *
     * @return
     */
    public String getStateMessage();

    /**
     * Will go from this state to the next one
     */
    public void updateState(BallotScannerUI host, int updateMode);

}
