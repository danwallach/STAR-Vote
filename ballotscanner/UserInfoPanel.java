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

    private int messageLines;

    ArrayList<String> messages;

    public UserInfoPanel(BallotScannerUI ballotScannerUI){
        context = ballotScannerUI;
        setPreferredSize(new Dimension(600,300));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        scanResponsePanel = new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                if(context.state.getStateImage() != null){
                    g.drawImage(context.state.getStateImage(), 100, 15, 400, 90, null);
                }
            }
        };
        scanResponsePanel.setBorder(BorderFactory.createEtchedBorder());
        scanResponsePanel.setPreferredSize(new Dimension(600, 100));

        messages = new ArrayList<String>();

        messagesPanel = new JPanel();
        messagesPanel.setPreferredSize(new Dimension(600, 150));
        //messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
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
            lab.setHorizontalAlignment(SwingConstants.CENTER);
            lab.setFont(fontSmall);

            int textHeight = 0;
            textHeight += (fontSmall.getStringBounds(messages.get(i),new FontRenderContext(new AffineTransform(), true, true))).getHeight();

            lab.setPreferredSize(new Dimension (messagesPanel.getWidth(), textHeight));
            //lab.setAlignmentX(Component.CENTER_ALIGNMENT);
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