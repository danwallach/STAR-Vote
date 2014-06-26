package ballotscanner;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class UserInfoPanel extends JPanel {

    private JPanel messagesPanel;
    private JPanel scanResponsePanel;
    private final BallotScannerUI context;
    private Font fontSmall = new Font("Arial Unicode", Font.PLAIN, 18);
    private ArrayList<String> messages;

    /**
     * This constructor sets up the UserInfoPanel corresponding to the state of the BallotScannerUI.
     * @param ballotScannerUI is the @BallotScannerUI that corresponds to this @UserInfoPanel
     */
    public UserInfoPanel(BallotScannerUI ballotScannerUI){

        context = ballotScannerUI;
        setPreferredSize(new Dimension(600,300));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        scanResponsePanel = new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);

                /* TODO fix state */
                /* If the state is defined, draw the window corresponding to the right state */
                if(context.state.getStateImage() != null)
                    g.drawImage(context.state.getStateImage(), 100, 15, 400, 90, null);

            }
        };

        /* Set up for scanning */
        scanResponsePanel.setBorder(BorderFactory.createEtchedBorder());
        scanResponsePanel.setPreferredSize(new Dimension(600, 100));

        messages = new ArrayList<>();

        messagesPanel = new JPanel();
        messagesPanel.setPreferredSize(new Dimension(600, 150));
        messagesPanel.setBorder(BorderFactory.createEtchedBorder());

        /* Clear out any old messages */
        clearMessages();

        add(messagesPanel);
        add(scanResponsePanel);
    }

    /* TODO this method seems a lot more specific than it should be... part of this could be a separate method. */
    /**
     * Adds a graphics component to @UserInfoPanel and adds messages to the panel as well.
     * @param g the graphics component to be added to the @UserInfoPanel
     */
    public void paintComponent(Graphics g){

        super.paintComponent(g);

        messagesPanel.removeAll();

        /* Enforce spacing between components */
        int gap = (200 - messages.size()*20)/2;
        messagesPanel.add(Box.createRigidArea(new Dimension(600,gap/3)));

        /* For all messages in the ArrayList... */
        for(String message : messages){

            /* Create a new JLabel in the centre of the screen with the current message */
            JLabel lab = new JLabel(message);
            lab.setHorizontalAlignment(SwingConstants.CENTER);
            lab.setFont(fontSmall);

            /* TODO feel like this needs to be changed... pretty ugly, and I think there's a simpler way to do this. */
            /* Define the font size */
            int textHeight = 0;
            textHeight += (fontSmall.getStringBounds(message,new FontRenderContext(new AffineTransform(), true, true))).getHeight();

            /* Set the overall size of the JLabel */
            lab.setPreferredSize(new Dimension (messagesPanel.getWidth(), textHeight));

            /* Add the JLabel to the panel */
            messagesPanel.add(lab);
        }

        /* Continue enforcing spacing between components */
        messagesPanel.add(Box.createRigidArea(new Dimension(600,gap*2/3)));

        messagesPanel.validate();
    }

    /**
     * Clears all messages from the messages ArrayList and the JPanel
     */
    public void clearMessages(){
        messages.clear();
        messagesPanel.removeAll();
    }

    /**
     * Adds a message to @messages which will later be added to the @UserInfoPanel
     * @param mes the message to be added to @messages
     */
    public void addMessage(String mes){
        messages.add(mes);
    }
}