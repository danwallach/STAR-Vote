package ballotscanner;

import ballotscanner.state.AState;
import ballotscanner.state.InactiveState;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * @author mrdouglass95, Mircea Berechet
 * 6/17/13
 *
 * A ballot scanner UI.
 */
public class BallotScannerUI extends JFrame {

    /* Transition states to be used with the updateFrame method. */
    public static final int NO_TRANSITION = 0;
    public static final int TO_ACCEPT_STATE = 1;
    public static final int TO_REJECT_STATE = 2;
    public static final int TO_PROMPT_STATE = 3;

    /* The main panel for the UI. */
    private JPanel mainPanel;

    /* Panels that display election/status information on the UI. */
    public ElectionInfoPanel electionInfoPanel;
    public UserInfoPanel userInfoPanel;

    /* The image that represents the scanner/UI's current state. It is now "deprecated" and should be removed.
    * TODO All uses of responseImage should be replaced with calls to state.getStateImage()*/
    public BufferedImage responseImage;

    /* The name of the election for which the scanner/UI is currently being used. */
    public String electionName;

    /* The current state of this scanner/UI. */
    public AState state;

    /**
     * Constructor for a BallotScannerUI.
     * @param electionName - the name of the election, to be displayed on the GUI.
     */
    public BallotScannerUI(String electionName){
        super("STAR-Vote Ballot Scanner");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600,600);
        setResizable(false);
        setLocationRelativeTo(null);
        setLocation((int)Math.round(getLocation().getX()) - getWidth()/2,
                    (int)Math.round(getLocation().getY()) - getHeight()/2);

        this.electionName = electionName;


        userInfoPanel = new UserInfoPanel(this);
        electionInfoPanel = new ElectionInfoPanel(this);
        state = InactiveState.SINGLETON;
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(electionInfoPanel);
        mainPanel.add(userInfoPanel);
        add(mainPanel);
        pack();
        setVisible(true);
        //displayInactiveScreen();

        this.updateFrame(NO_TRANSITION);
    }

    /*public void displayPromptScreen(){
        userInfoPanel.clearMessages();
        userInfoPanel.addMessage("This is a Ballot Scanning Console.");
        userInfoPanel.addMessage("Place Ballot Under Scanner to Cast Ballot.");
        responseImage = waitingImage;
        updateFrame();
    }

    public void displayInactiveScreen(){
        userInfoPanel.clearMessages();
        userInfoPanel.addMessage("This is a Ballot Scanning Console.");
        userInfoPanel.addMessage("Console Currently Not Ready For Use.");
        responseImage = inactiveImage;
        updateFrame();
    }

    public void displayBallotAcceptedScreen(String bid){
        userInfoPanel.clearMessages();
        userInfoPanel.addMessage("Ballot " + bid  + " Confirmed and Cast");
        userInfoPanel.addMessage("Your Vote Will be Counted");
        userInfoPanel.addMessage("Thank You for Voting!");
        responseImage = state.getStateImage();
        updateFrame();
    }

    public void displayBallotRejectedScreen(){
        userInfoPanel.clearMessages();
        userInfoPanel.addMessage("Ballot has been rejected");
        userInfoPanel.addMessage("Hold Ballot Still Under the Scanner");
        userInfoPanel.addMessage("If This Problem Persists, Contact an Election Official");
        responseImage = rejectImage;
        updateFrame();
    }*/

    /**
     * An update method that repaints the JFrame and two of its embedded components.
     * ElectionInfoPanel and UserInfoPanel are now public, stand-alone classes.
     */
    public void updateFrameComponents ()
    {
        electionInfoPanel.repaint();
        userInfoPanel.repaint();
        this.repaint();
    }

    /**
     * An update method that transitions the scanner/UI between states and calls the displayScreen method of the state.
     * This should set the user info panel to contain the appropriate messages for that particular state.
     * // TODO Find a way to pass in bid to the AcceptState (maybe by adding it as a parameter to the updateFrame function
     * // TODO which is not used by any state except AcceptState.
     * @param transition - the type of transition to be performed by the update method.
     */
    public void updateFrame(int transition){
        state.updateState(this, transition);
        state.displayScreen(this);
        if (transition != 0)
        {
            updateFrame(0);
        }
    }
}
