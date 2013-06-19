package supervisor.view;

import supervisor.model.BallotScannerMachine;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 6/14/13
 * Time: 11:23 AM
 * To change this template use File | Settings | File Templates.
 */

public class BallotScannerMachineView extends AMachineView{

    private BallotScannerMachine machine;

    private JLabel scannerLabel;

    private JLabel serialLabel;

    private JLabel statusLabel;

    private JLabel batteryLabel;


    public BallotScannerMachineView(BallotScannerMachine b){

        machine = b;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p;

        batteryLabel = new JLabel();
        p = new JPanel();
        p.setBackground(Color.green);
        batteryLabel.setAlignmentX(Container.RIGHT_ALIGNMENT);
        p.add(new JLabel("                               ")); //Janky allignment help
        p.add(batteryLabel);
        add(p);

        scannerLabel = new MyJLabel("Scanner");
        scannerLabel.setFont(scannerLabel.getFont().deriveFont(Font.BOLD,
                16f));
        p = new JPanel();
        p.setBackground(Color.green);
        p.add(scannerLabel);
        add(p);

        serialLabel = new MyJLabel("#" + machine.getSerial());
        p = new JPanel();
        p.setBackground(Color.green);
        p.add(serialLabel);
        add(p);

        statusLabel = new MyJLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        p = new JPanel();
        p.setBackground(Color.green);
        p.add(statusLabel);
        add(p);


        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setSize(180, 160);
        setMinimumSize(new Dimension(180, 175));
        setPreferredSize(new Dimension(180, 175));
        setMaximumSize(new Dimension(180, 175));

        machine.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                updateView();
            }
        });

        updateView();
    }

    /**
     * Updates the background to a given color
     * @param c the color
     */
    public void updateBackground(Color c) {
        setBackground(c);
    }

    public void updateView(){

        BallotScannerMachine m = (BallotScannerMachine)machine;

        if (machine.isOnline()) {
            updateBackground(Color.green);
            if (machine.getStatus() == BallotScannerMachine.ACTIVE)
                statusLabel.setText("Active");
            else
                statusLabel.setText("Inactive");
        } else {
            updateBackground(Color.LIGHT_GRAY);
            statusLabel.setText("Offline");
        }

        batteryLabel.setVisible(true);
        String batteryIcon = "images/batt" + ((m.getBattery() + 10) / 20)
                + ".png";

        URL url = ClassLoader.getSystemClassLoader().getResource(
                batteryIcon);
        if (url != null) batteryLabel.setIcon(new ImageIcon(url));

        revalidate();
        repaint();
    }
}
