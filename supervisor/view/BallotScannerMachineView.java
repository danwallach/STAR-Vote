package supervisor.view;

import supervisor.model.Model;
import supervisor.model.machine.BallotScannerMachine;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

/**
 * This class is the machine tile view of the Ballot Scanner when it is on the network. It is updated when the Scanner
 * comes online and when it leaves.
 *
 * @author Mircea C. Berechet, mrdouglass95, Matt Bernhard
 */

public class BallotScannerMachineView extends AMachineView{

    /** A reference to the supervisor's model */
    private Model model;

    /** A reference to the machine (mini-model) representing the BallotScanner */
    private BallotScannerMachine machine;

    /** A label indicating the name of this machine  (Ballot Scanner) */
    private JLabel nameLabel;

    /** The panel containing the name label */
    private JPanel namePanel;

    /** A label denoting the serial number of this machine, the same one used in the Auditorium logs */
    private JLabel serialLabel;

    /** A label for displaying the battery status of the ballot scanner */
    private JLabel batteryLabel;

    /** A label indicating the status of the ballot scanner (active or inactive) */
    private JLabel statusLabel;

    /** The panel containing the status label */
    private JPanel statusPanel;

    /**
     * We use a JButton to allows us to "disable" the view, i.e. grey it out when the polls are close
     * or the scanner is inactive. It is easier than creating a notion of UI state for each machine view.
     */
    private JButton button;

    /** This panel contains the "button", so it can be properly spaced on the machine view panel */
    private JPanel buttonPanel;

    /**
     * Constructor for the BallotScannerMachineView. sets up the graphical components and references
     * to necessary models and contexts.
     *
     * @param m the supervisor's model
     * @param b the supervisor's stored internal representation of this machine
     */
    public BallotScannerMachineView(Model m, BallotScannerMachine b){
        /* Instantiate the model and mini-models, as per MVC */
        model = m;
        machine = b;

        /* Again, we are (lamentably) using GridBag for fine-grain control of the GUI layout */
        GridBagConstraints c = new GridBagConstraints();

        /* Create the name panel and set up its layout */
        namePanel = new JPanel();
        namePanel.setLayout(new GridBagLayout());

        /* Create the name label and specify its font */
        nameLabel = new MyJLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 24));

        /* Add the nameLabel to the panel */
        c.weighty = .8;
        c.anchor = GridBagConstraints.PAGE_END;
        namePanel.add(nameLabel, c);

        /* Set up the serial number label and its font */
        serialLabel = new MyJLabel();
        serialLabel.setFont(serialLabel.getFont().deriveFont(9f));

        /* Add the serial label to the panel */
        c.weighty = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        namePanel.add(serialLabel, c);

        /*
         * Add a border to this panel, mainly clean lines to separate
         * it from the rest of the info on the machine view
         */
        namePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1,
                Color.BLACK));

        c = new GridBagConstraints();

        /* Set up the status panel */
        statusPanel = new JPanel();
        statusPanel.setLayout(new GridBagLayout());

        /* Set up the battery label and add it */
        batteryLabel = new JLabel();
        batteryLabel.setVisible(false);

        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5, 0, 5, 0);
        statusPanel.add(batteryLabel, c);

        /* Set up the status label and add it */
        statusLabel = new MyJLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        c.gridy = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        statusPanel.add(statusLabel, c);

        /* Set the grid constraints and add a border to separate the status info from the rest of the panel */
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridy = 2;
        c.weighty = 0;
        c.gridy = 3;
        c.weighty = 1;

        statusPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.BLACK));
        statusPanel.setPreferredSize(new Dimension(70, 70));

        /* Set up the "button" and its corresponding panel, and add the button to the panel */
        c = new GridBagConstraints();
        button = new MyJButton();
        button.setLayout(new GridBagLayout());

        button.setVisible(model.arePollsOpen());
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(button, BorderLayout.CENTER);

        setLayout(new GridBagLayout());

        /* Add the name panel to this (BallotScannerMachine View) view */
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        add(namePanel, c);

        /* Add the status panel to this (BallotScannerMachine View) view */
        c.gridx = 1;
        add(statusPanel, c);

        /* Add the button panel to this (BallotScannerMachine View) view */
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        c.insets = new Insets(8, 8, 8, 8);
        add(buttonPanel, c);

        /* Add a border around the view, largely for aesthetic appeal */
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setSize(180, 160);
        setMinimumSize(new Dimension(180, 175));
        setPreferredSize(new Dimension(180, 175));
        setMaximumSize(new Dimension(180, 175));

        /* Register this view so that it updates when necessary */
        machine.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                updateView();
            }
        });

        /* update the view so that all of the information (status, batter, etc.) gets filled in */
        updateView();
    }

    /**
     * Updates the background to a given color, first setting this (BallotScannerMachineView) background
     * and then the background for each of the panels here contained (name, status, and button).
     *
     * @param c the color
     */
    public void updateBackground(Color c) {
        setBackground(c);
        namePanel.setBackground(c);
        statusPanel.setBackground(c);
        buttonPanel.setBackground(c);
    }

    /**
     * Called when the state of this view's corresponding machine changes.
     */
    public void updateView(){
        /* Prints the label in large font in the upper left section of the tile. */
        nameLabel.setText(Integer.toString(machine.getLabel()));

        /* Adds the serial number */
        serialLabel.setText("#" + machine.getSerial());

        /* Grabs the machine (mini-model to this mini-view) to fill in more relevant information */
        BallotScannerMachine m = machine;

        /* If the machine is online, reflect that in the view */
        if (machine.isOnline())
        {
            /* Dump all of the old information */
            button.removeAll();

            /* Set the status info based on the actual status */
            if (machine.getStatus() == BallotScannerMachine.ACTIVE)
            {
                statusLabel.setText("Active");
            }
            else
            {
                statusLabel.setText("Inactive");
            }

            /* Since our "button" doesn't actually do anything, make sure of it */
            button.setEnabled(false);
            button.setVisible(false);

            /* Note that we use AWT's Green for the Ballot Scanner, which is quite lime */
            updateBackground(Color.GREEN);

            /*
             * Load the battery image that corresponds to the amount of battery the machine reports
             *
             * TODO Figure out why this arithmetic always results in 0.
             * TODO Is it m.getBattery reporting 0 or is it the actual arithmetic?
             */
            batteryLabel.setVisible(true);
            String batteryIcon = "images/batt" + ((m.getBattery() + 10) / 20)
                    + ".png";

            URL url = ClassLoader.getSystemClassLoader().getResource(
                    batteryIcon);
            if (url != null) batteryLabel.setIcon(new ImageIcon(url));
        }
        /* If the machine is inactive, grey our everything */
        else
        {
            /* Theinactive color is AWT light gray, the lovely boring gray of most Java applications */
            updateBackground(Color.LIGHT_GRAY);
            batteryLabel.setVisible(false);
            statusLabel.setText("Offline");
            button.setVisible(false);
        }

        /* Repaint everything so it updates onscreen */
        revalidate();
        repaint();
    }

}
