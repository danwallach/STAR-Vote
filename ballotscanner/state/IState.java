package ballotscanner.state;

import ballotscanner.BallotScannerUI;

import java.awt.image.BufferedImage;

/**
 * @author Matt Bernhard, Mircea Berechet
 * 6/18/13
 *
 * An interface outlining the various states of the @BallotScannerUI
 */
public interface IState {

    /**
     * @return an image representing this state
     */
    public BufferedImage getStateImage();

    /**
     * @return  the name of the state
     */
    public String getStateName();

    /**
     * @return  the state's message
     */
    public String getStateMessage();

    /**
     * Will go from this state to the next one
     */
    public void updateState(BallotScannerUI host, int updateMode);

}
