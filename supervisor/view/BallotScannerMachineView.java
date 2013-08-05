package supervisor.view;

import supervisor.model.BallotScannerMachine;
import supervisor.model.Model;
import supervisor.model.VoteBoxBooth;
import votebox.events.StartScannerEvent;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

/**
 * This class is the machine tile view of the Ballot Scanner when it is on the network. It is updated when the Scanner
 * comes online and when it leaves.
 *
 * @author Mircea C. Berechet, mrdouglass95
 */

public class BallotScannerMachineView extends AMachineView{

    private Model model;

    private ActiveUI view;

    private BallotScannerMachine machine;

    private JLabel nameLabel;

    private JLabel serialLabel;

    private JLabel batteryLabel;

    private JLabel statusLabel;

    private JPanel statusPanel;

    private JPanel namePanel;

    private JButton button;

    private JPanel buttonPanel;

    /**
     * Only constructor
     *
     * @param m the supervisor's model
     * @param v the supervisor's active UI
     * @param b the supervisor's stored internal representation of this machine
     */
    public BallotScannerMachineView(Model m, ActiveUI v, BallotScannerMachine b){
        /*
        machine = b;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p;

        batteryLabel = new JLabel();
        p = new JPanel();
        p.setBackground(Color.GREEN);
        batteryLabel.setAlignmentX(Container.RIGHT_ALIGNMENT);
        p.add(new JLabel("                               ")); //Janky allignment help
        p.add(batteryLabel);
        add(p);

        scannerLabel = new MyJLabel("Scanner");
        scannerLabel.setFont(scannerLabel.getFont().deriveFont(Font.BOLD,
                16f));
        p = new JPanel();
        p.setBackground(Color.GREEN);
        p.add(scannerLabel);
        add(p);

        serialLabel = new MyJLabel("#" + machine.getSerial());
        p = new JPanel();
        p.setBackground(Color.GREEN);
        p.add(serialLabel);
        add(p);

        statusLabel = new MyJLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        p = new JPanel();
        if (statusLabel.equals("Offline"))
        {
            setBackground(Color.DARK_GRAY);
        }
        else
        {
            p.setBackground(Color.GREEN);
        }
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
        */


        model = m;
        view = v;
        machine = b;
        GridBagConstraints c = new GridBagConstraints();

        nameLabel = new MyJLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 24));
        serialLabel = new MyJLabel();
        serialLabel.setFont(serialLabel.getFont().deriveFont(9f));
        namePanel = new JPanel();
        namePanel.setLayout(new GridBagLayout());
        c.weighty = .8;
        c.anchor = GridBagConstraints.PAGE_END;
        namePanel.add(nameLabel, c);
        c.weighty = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        namePanel.add(serialLabel, c);
        namePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1,
                Color.BLACK));

        c = new GridBagConstraints();

        batteryLabel = new JLabel();
        batteryLabel.setVisible(false);

        statusLabel = new MyJLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        statusPanel = new JPanel();
        statusPanel.setLayout(new GridBagLayout());
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5, 0, 5, 0);
        statusPanel.add(batteryLabel, c);
        c.gridy = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        statusPanel.add(statusLabel, c);
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridy = 2;
        c.weighty = 0;

        c.gridy = 3;
        c.weighty = 1;

        statusPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.BLACK));
        statusPanel.setPreferredSize(new Dimension(70, 70));

        c = new GridBagConstraints();
        button = new MyJButton();
        button.setLayout(new GridBagLayout());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonPressed();
            }
        });
        button.setVisible(model.isPollsOpen());
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(button, BorderLayout.CENTER);

        setLayout(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        add(namePanel, c);

        c.gridx = 1;
        add(statusPanel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        c.insets = new Insets(8, 8, 8, 8);
        add(buttonPanel, c);

        // setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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
        namePanel.setBackground(c);
        statusPanel.setBackground(c);
        buttonPanel.setBackground(c);
    }

    /**
     * called when this view needs to change as a result of a change in the state of
     * this view's machine member
     */
    public void updateView(){
        // Prints the label in large font in the upper left section of the tile.
        nameLabel.setText(Integer.toString(machine.getLabel()));
        serialLabel.setText("#" + machine.getSerial());
        BallotScannerMachine m = machine;
        GridBagConstraints c = new GridBagConstraints();
        if (machine.isOnline())
        {
            button.removeAll();
            if (machine.getStatus() == BallotScannerMachine.ACTIVE)
            {
                //button.add(new MyJLabel("Deactivate Scanner"), c);
                statusLabel.setText("Active");
            }
            else
            {
                //button.add(new MyJLabel("Activate Scanner"), c);
                statusLabel.setText("Inactive");
            }
            button.setEnabled(false);
            button.setVisible(false);
            updateBackground(Color.GREEN);
            batteryLabel.setVisible(true);
            String batteryIcon = "images/batt" + ((m.getBattery() + 10) / 20)
                    + ".png";

            URL url = ClassLoader.getSystemClassLoader().getResource(
                    batteryIcon);
            if (url != null) batteryLabel.setIcon(new ImageIcon(url));
        }
        else
        {
            updateBackground(Color.LIGHT_GRAY);
            batteryLabel.setVisible(false);
            statusLabel.setText("Offline");
            button.setVisible(false);
        }

        revalidate();
        repaint();
    }

    /**
     * Called whenever the main button on the view is pressed.
     */
    private void buttonPressed() {
        // NO-OP
        /*if (machine.isOnline())
        {
            if (machine.getStatus() == BallotScannerMachine.INACTIVE)
            {
                model.sendStartScannerEvent();

            }
        }*/
    }
}
