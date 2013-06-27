package ballotscanner;

import ballotscanner.state.AState;
import ballotscanner.state.InactiveState;

import javax.swing.*;

/**
 * @author Mircea Berechet, mrdouglass95
 *
 * A Ballot Scanner UI.
 */
public class BallotScannerUI extends JFrame {

    /* Transition states to be used with the updateFrame method. */
    public static final int TO_INACTIVE_STATE = -1;
    public static final int NO_TRANSITION = 0;
    public static final int TO_ACCEPT_STATE = 1;
    public static final int TO_REJECT_STATE = 2;
    public static final int TO_PROMPT_STATE = 3;

    /* Panels that display election/status information on the UI. */
    public ElectionInfoPanel electionInfoPanel;
    public UserInfoPanel userInfoPanel;

    /* The image that represents the scanner/UI's current state. It is now "deprecated" and should be removed. */
//    public BufferedImage responseImage;

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

        /* The main panel for the UI. */
        JPanel mainPanel;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600,600);
        setResizable(false);
        setLocationRelativeTo(null);
        //setLocation((int)Math.round(getLocation().getX()) - getWidth()/2, (int)Math.round(getLocation().getY()) - getHeight()/2);

        this.electionName = electionName;
        // Set the initial state of the Scanner to the InactiveState.
        state = InactiveState.SINGLETON;

        // Create the two panels of the GUI.
        userInfoPanel = new UserInfoPanel(this);
        electionInfoPanel = new ElectionInfoPanel(this);

        // Update the state and panels.
        this.updateFrame(NO_TRANSITION);

        // Add the panels to the GUI.
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(electionInfoPanel);
        mainPanel.add(userInfoPanel);
        add(mainPanel);
        pack();

        // Make the GUI visible.
        setVisible(true);
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
        repaint();
    }

    /**
     * An update method that transitions the scanner/UI between states and calls the displayScreen method of the state.
     * This should set the user info panel to contain the appropriate messages for that particular state.
     *
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
