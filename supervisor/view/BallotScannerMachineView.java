package supervisor.view;

import ballotscanner.BallotScannerMachine;
import supervisor.model.Model;
import supervisor.model.SupervisorMachine;
import supervisor.model.VoteBoxBooth;

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

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        scannerLabel = new MyJLabel("Scanner");
        scannerLabel.setFont(scannerLabel.getFont().deriveFont(Font.BOLD,
                16f));
        c.gridy = 0;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        add(scannerLabel, c);

        serialLabel = new MyJLabel("#" + machine.getSerial());
        c.gridy = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);
        add(serialLabel, c);

        statusLabel = new MyJLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        add(statusLabel, c);

        batteryLabel = new MyJLabel();
        c.gridy = 3;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        add(batteryLabel, c);

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
