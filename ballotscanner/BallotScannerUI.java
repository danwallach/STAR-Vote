package ballotscanner;

import ballotscanner.state.AState;
import ballotscanner.state.InactiveState;
import ballotscanner.state.PromptState;
import javazoom.jl.player.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author mrdouglass95, Mircea Berechet
 * 6/17/13
 *
 * A ballot scanner UI.
 */
public class BallotScannerUI extends JFrame {

    public static final int NO_TRANSITION = 0;
    public static final int TO_ACCEPT_STATE = 1;
    public static final int TO_REJECT_STATE = 2;
    public static final int TO_PROMPT_STATE = 3;

    private JPanel mainPanel;

    public ElectionInfoPanel electionInfoPanel;
    public UserInfoPanel userInfoPanel;

    private BufferedImage rejectImage;
    private BufferedImage acceptImage;
    private BufferedImage waitingImage;
    private BufferedImage inactiveImage;
    public BufferedImage responseImage;

    public String electionName;
    public AState state;

    public BallotScannerUI(String electionName, String mp3Path){
        super("STAR-Vote Ballot Scanner");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600,600);
        setResizable(false);
        setLocationRelativeTo(null);
        setLocation((int)Math.round(getLocation().getX()) - getWidth()/2,
                    (int)Math.round(getLocation().getY()) - getHeight()/2);

        this.electionName = electionName;


//        try{
//            acceptImage = ImageIO.read(new File("images/accept_ballot.png"));
//        }catch(IOException ioe){
//            System.out.println("BallotScannerUI: Could not locate accept image");
//            acceptImage = null;
//        }
//
//        try{
//            rejectImage = ImageIO.read(new File("images/reject_ballot.png"));
//        }catch(IOException ioe){
//            System.out.println("BallotScannerUI: Could not locate reject image");
//            rejectImage = null;
//        }
//
//        try{
//            waitingImage = ImageIO.read(new File("images/waiting_ballot.png"));
//        }catch(IOException ioe){
//            System.out.println("BallotScannerUI: Could not locate waiting image");
//            waitingImage = null;
//        }
//
//        try{
//            inactiveImage = ImageIO.read(new File("images/inactive.png"));
//        }catch(IOException ioe){
//            System.out.println("BallotScannerUI: Could not locate inactive image");
//            inactiveImage = null;
//        }

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
    public void updateFrameComponents ()
    {
        electionInfoPanel.repaint();
        userInfoPanel.repaint();
        this.repaint();
    }

    public void updateFrame(int transition){
        state.updateState(this, transition);
        state.displayScreen(this);
        if (transition != 0)
        {
            updateFrame(0);
        }
    }
}
