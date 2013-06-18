package ballotscanner;

import ballotscanner.state.AState;
import ballotscanner.state.PromptState;
import javazoom.jl.player.Player;
import votebox.AuditoriumParams;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 6/17/13
 * Time: 10:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class BallotScannerUI extends JFrame {

    private JPanel mainPanel;

    private ElectionInfoPanel electionInfoPanel;
    private UserInfoPanel userInfoPanel;

    private BufferedImage logo;
    private BufferedImage rejectImage;
    private BufferedImage acceptImage;
    private BufferedImage waitingImage;
    private BufferedImage inactiveImage;
    private BufferedImage responseImage;

    private JLabel scanResultLabel;
    private JLabel dateLabel;

    private DateFormat dateFormat;
    private Date date;

    private Thread delay;

    private String electionName;
    public AState state;

    private Font fontBig   = new Font("Arial Unicode", Font.BOLD, 20);
    private Font fontSmall = new Font("Arial Unicode", Font.PLAIN, 18);

    // keeps the path to the "ballot scanned" mp3
    private String bsMp3Path;// = "sound/ballotscanned.mp3"; //move to the .conf file

    // keeps the mp3Player
    private Player mp3Player;

    public BallotScannerUI(String electionName, String mp3Path){
        super("STAR-Vote Ballot Scanner");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600,600);
        setResizable(false);
        setLocationRelativeTo(null);
        setLocation((int)Math.round(getLocation().getX()) - getWidth()/2,
                    (int)Math.round(getLocation().getY()) - getHeight()/2);

        dateFormat = new SimpleDateFormat("MMMM d, y");
        date = new Date();

        this.electionName = electionName;

        bsMp3Path = mp3Path;

        state = PromptState.SINGLETON;

        try{
            logo = ImageIO.read(new File("images/logo.png"));
        }catch(IOException ioe){
            System.out.println("BallotScannerUI: Could not locate logo image");
            logo = null;
        }

        try{
            acceptImage = ImageIO.read(new File("images/accept_ballot.png"));
        }catch(IOException ioe){
            System.out.println("BallotScannerUI: Could not locate accept image");
            acceptImage = null;
        }

        try{
            rejectImage = ImageIO.read(new File("images/reject_ballot.png"));
        }catch(IOException ioe){
            System.out.println("BallotScannerUI: Could not locate reject image");
            rejectImage = null;
        }

        try{
            waitingImage = ImageIO.read(new File("images/waiting_ballot.png"));
        }catch(IOException ioe){
            System.out.println("BallotScannerUI: Could not locate waiting image");
            waitingImage = null;
        }

        try{
            inactiveImage = ImageIO.read(new File("images/inactive.png"));
        }catch(IOException ioe){
            System.out.println("BallotScannerUI: Could not locate inactive image");
            inactiveImage = null;
        }

        userInfoPanel = new UserInfoPanel();
        electionInfoPanel = new ElectionInfoPanel();

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(electionInfoPanel);
        mainPanel.add(userInfoPanel);
        add(mainPanel);
        pack();
        setVisible(true);
        displayInactiveScreen();
    }

    private class ElectionInfoPanel extends JPanel{

        private JPanel logoPanel;
        private JPanel infoPanel;

        public ElectionInfoPanel(){
            setPreferredSize(new Dimension(600,200));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEtchedBorder());

            logoPanel = new JPanel(){
                public void paintComponent(Graphics g){
                    super.paintComponent(g);
                    if(logo != null){
                        g.drawImage(logo, 100, 5, 400, 90, null);
                    }
                }
            };

            logoPanel.setPreferredSize(new Dimension(600, 100));

            infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setPreferredSize(new Dimension(600, 100));

            JLabel info;

            info = new JLabel(electionName);
            info.setFont(fontBig);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(info);

            info = new JLabel(dateFormat.format(date));
            info.setFont(fontSmall);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(info);

            info = new JLabel("Welcome to this Ballot Scanner Console");
            info.setFont(fontSmall);
            info.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(info);

            add(logoPanel);
            add(infoPanel);
            //add(Box.createRigidArea(new Dimension(600, 50)));
        }
    }

    private class UserInfoPanel extends JPanel{

        private JPanel messagesPanel;
        private JPanel scanResponsePanel;

        private int messageLines;

        ArrayList<String> messages;

        public UserInfoPanel(){
            setPreferredSize(new Dimension(600,300));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEtchedBorder());

            scanResponsePanel = new JPanel(){
                public void paintComponent(Graphics g){
                    super.paintComponent(g);
                    if(responseImage != null){
                        g.drawImage(responseImage, 100, 15, 400, 90, null);
                    }
                }
            };
            scanResponsePanel.setBorder(BorderFactory.createEtchedBorder());
            scanResponsePanel.setPreferredSize(new Dimension(600, 100));

            messages = new ArrayList<String>();

            messagesPanel = new JPanel();
            messagesPanel.setPreferredSize(new Dimension(600, 150));
            messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
            messagesPanel.setBorder(BorderFactory.createEtchedBorder());

            clearMessages();

            add(messagesPanel);
            add(scanResponsePanel);
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);

            messagesPanel.removeAll();

            int gap = (200 - messages.size()*20)/2;

            messagesPanel.add(Box.createRigidArea(new Dimension(600,gap/3)));
            for(int i=0; i<messages.size(); i++){
                JLabel lab = new JLabel(messages.get(i));
                lab.setFont(fontSmall);
                lab.setAlignmentX(Component.CENTER_ALIGNMENT);
                messagesPanel.add(lab);
            }
            messagesPanel.add(Box.createRigidArea(new Dimension(600,gap*2/3)));

            messagesPanel.validate();
        }

        public void clearMessages(){
            messages.clear();
            messagesPanel.removeAll();
        }

        public void addMessage(String mes){
           messages.add(messages.size(), mes);
        }
    }

    public void displayPromptScreen(){
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
        responseImage = acceptImage;
        updateFrame();


    }

    public void displayBallotRejectedScreen(){
        userInfoPanel.clearMessages();
        userInfoPanel.addMessage("Ballot has been rejected");
        userInfoPanel.addMessage("Hold Ballot Still Under the Scanner");
        userInfoPanel.addMessage("If This Problem Persists, Contact an Election Official");
        responseImage = rejectImage;
        updateFrame();
    }

    public void updateFrame(){
        electionInfoPanel.repaint();
        userInfoPanel.repaint();
        state.updateState(this, 0);
    }
}
