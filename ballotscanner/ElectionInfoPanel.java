package ballotscanner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ElectionInfoPanel extends JPanel {

    private final BallotScannerUI context;

    private JPanel logoPanel;
    private JPanel infoPanel;

    private BufferedImage logo;
    private Font fontBig   = new Font("Arial Unicode", Font.BOLD, 20);
    private Font fontSmall = new Font("Arial Unicode", Font.PLAIN, 18);

    private DateFormat dateFormat;
    private Date date;

    public ElectionInfoPanel(BallotScannerUI ballotScannerUI){
        context = ballotScannerUI;
        setPreferredSize(new Dimension(600,200));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        try{
            logo = ImageIO.read(new File("images/logo.png"));
        }catch(IOException ioe){
            System.err.println("BallotScannerUI: Could not locate logo image");
            logo = null;
        }

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

        info = new JLabel(context.electionName);
        info.setFont(fontBig);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(info);

        dateFormat = new SimpleDateFormat("MMMM d, y");
        date = new Date();

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