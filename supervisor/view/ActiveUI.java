/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package supervisor.view;

import printer.Printer;
import supervisor.model.Machine.AMachine;
import supervisor.model.Model;
import supervisor.model.Machine.TapMachine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * The view that is shown on a supervisor that is active - consists of
 * information about the election, options for controlling the election,
 * and a grid of all of the machines on the network as well as controls
 * for those machines.
 *
 * @author Corey Shaw
 */
@SuppressWarnings("serial")
public class ActiveUI extends JPanel {

    /** The Supervisor model, for reference */
    private Model model;

    /** This allows us to build machine views and put them on the active UI. Uses Factory pattern */
    private MachineViewGenerator viewGen;

    /** Panel which contains election info and general controls, like opening the polls */
    private JPanel electionInfoPanel;

    /** Label for the current time */
    private JLabel timeLbl;

    /**
     * Denotes whether the polls are opened.
     *
     * TODO Is this necessary, since the Open/Close polls button exists?
     */
    private JLabel pollsOpenLbl;

    /** Allows for the opening and closing of the polls */
    private JButton pollsControlButton;

    /** Launch a file chooser to select a ballot */
    private JButton ballotButton;

    /** Issues a PIN so that a voting booth can be authorized to vote and request a ballot */
    private JButton pinButton;

    /**
     * Allows a ballot to be scanned a removed from the list of committed ballots in the
     * event that a voter explicitly wishes to challenge their vote, or in the case of
     * provisional voting.
     *
     * TODO Verify that the provisional process is kosher
     */
    private JButton spoilButton;

    /** String for displaying the ID of the ballot to be spoiled */
    private static String spoiledBallotID;

    /** Contains a view of all the machines connected on the network */
    private JPanel machineViewPanel;

    /** Will allow ballots to be found in the Supervisor machine's file system */
    private JFileChooser ballotLocChooser;

    /** Keeps track of which ballot styles go with which precincts */
    private HashMap<String, String> precinctsToBallots = new HashMap<String, String>();

    /** Displays the connection status of Tap, the data diode */
    private TapView tapView;

    /**
     * Constructs a new ActiveUI, initializing the election info panel and
     * machine panel and adding them to this panel.
     *
     * @param m the supervisor's model, as per MVC
     */
    public ActiveUI(Model m) {
        model = m;

        /* Initizalize the machine view factory */
        viewGen = new MachineViewGenerator();

        /* we are using GridBagLayout for fine-tuned control over which elements get placed where */
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Initialize the election info panel and place it in the correct location on the panel */
        initializeElectionInfoPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(electionInfoPanel, c);

        /* Initizalize the machine view panel and put it in the right place on the panel */
        initializeMachineViewPanel();
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        add(machineViewPanel, c);

        /* Instructs the dispatcher to update the machine views whenever one of the views changes */
        model.registerForMachinesChanged(new Observer() {
            public void update(Observable o, Object arg) {
                updateAllMachineViews();
            }
        });
    }

    /**
     * Override of the JPanel paint method that turns on antialiasing
     */
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paint(g);
    }

    /**
     * Updates the view of the list of machines (adding new machines as
     * necessary), and then updates each machine's view
     */
    public void updateAllMachineViews() {
        /* First remove all of the machines */
        machineViewPanel.removeAll();

        /* Create a new panel to add back to the machine view panel */
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        /* Look through all the machines and add their views to the panel */
        int i = 0;
        for (AMachine m : model.getMachines()) {
            /* Tap is a special case, since its machine status is only either connected or not */
            if(m instanceof TapMachine){
                tapView.setMachine((TapMachine)m);
                continue;
            }

            /* Evenly space the views, then generate machine m's view using the factory */
            c.gridx = i % 4;
            c.gridy = i / 4;
            innerPanel.add(viewGen.generateView(model, this, m), c);
            ++i;
        }

        /* Update the Tap status view on the info panel */
        tapView.updateView();

        /* Add back the machines panel after having reconstructed it */
        c.gridx = 0;
        c.gridy = 0;
        machineViewPanel.add(innerPanel, c);
        validate();
        repaint();
    }

    /**
     * Initializes all GUI components pertaining to the panel's election information panel component,
     * including Tap ribbon, ballot selection button, pin generating button, spoil ballot button,
     * and open/close polls button.
     */
    private void initializeElectionInfoPanel() {
        /* create a new info panel, using GridBagLayout */
        electionInfoPanel = new JPanel();
        electionInfoPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* This panel will contain all of the info labels */
        JPanel leftLabelPanel = new JPanel();
        leftLabelPanel.setLayout(new GridBagLayout());
        leftLabelPanel.add(new MyJLabel(model.getParams().getElectionName()), c);
        Date d = new Date();
        c.gridy = 1;

        /* Add the current date to the panel */
        leftLabelPanel.add(new MyJLabel(DateFormat.getDateInstance(
                DateFormat.LONG).format(d)), c);
        c.gridy = 2;
        timeLbl = new MyJLabel(DateFormat.getTimeInstance(DateFormat.LONG)
                .format(d));
        leftLabelPanel.add(timeLbl, c);

        /* This updates the date and time by the second */
        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLbl.setText(DateFormat.getTimeInstance(DateFormat.LONG)
                        .format(new Date()));
            }
        }).start();

        /* Add the poll status label */
        c.gridy = 3;
        c.weighty = .2;
        c.anchor = GridBagConstraints.PAGE_END;
        pollsOpenLbl = new MyJLabel("Polls currently closed");
        leftLabelPanel.add(pollsOpenLbl, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.ipady = 20;
        c.insets = new Insets(20, 20, 80, 20);
        electionInfoPanel.add(leftLabelPanel, c);

        /* Set up the tap status ribbon and add it to the panel */
        tapView = new TapView(null);
        c.ipady = 20;
        c.gridy = 1;
        c.insets = new Insets(20,20,20,20);
        electionInfoPanel.add(tapView, c);

        /* Instantiate the file chooser, using the current working directory as the root path */
        ballotLocChooser = new JFileChooser(System.getProperty("user.dir"));
        ballotLocChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String path = f.getAbsolutePath();
                return (f.isDirectory() || path.length() > 4
                        && path.substring(path.length() - 4).equals(".zip"));
            }

            @Override
            public String getDescription() {
                return "Ballot ZIP files";
            }
        });

        /*
         * Add the Select Ballot button so that it launches the file chooser, and updates the ballots
         * selection panel for voting booth authorization, and maps the precinct to ballot style.
         */
        ballotButton = new MyJButton("Select Ballot");
        ballotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int answer = ballotLocChooser.showOpenDialog(ActiveUI.this);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    /* Extract the relevant ballot information */
                    File selected = ballotLocChooser.getSelectedFile();
                    String ballot = selected.getName();

                    /* By convention, the precinct identifier is the last 3 characters of the zip file's name,
                     * not including the .zip file extension.
                     *
                     * E.g., the precinct for ballot_003.zip would be "003", while the precinct
                     * for ballot.zip would be "lot" (which would be pretty silly
                     *
                     * Note that we also assume the ballot names are at least 3 characters long
                     *
                     * TODO handle the error case when the selected name is a valid file but not the correct length?
                     */
                    String precinct = ballot.substring(ballot.length()-7, ballot.length()-4);
                    System.out.println(precinct);
                    precinctsToBallots.put(precinct, selected.getAbsolutePath());

                    /* Add the ballot to the model */
                    model.addPrecinct(ballotLocChooser.getSelectedFile());
                }
            }
        });
        c.ipady = 50;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(20, 20, 20, 20);
        electionInfoPanel.add(ballotButton, c);

        /* We need a final reference to this (ActiveUI) panel, so that it can be used in an anonymous inner class */
        final JPanel fthis = this;

        /*
         * Set up and add the PIN generation button. This will print out a PIN and
         * allow a voting booth to be authorized to vote.
         *
         * TODO change the name of this button?
         */
        pinButton = new MyJButton("Generate Pin");
        pinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /* If a ballot has been selected, allow PIN generation */
                if (model.getPrecinctIDs().length > 0) {

                    /* This dialog allows for the selection of a ballot
                     * style and ballot type (i.e. provisional or not)
                     */
                    String precinct = "" + JOptionPane.showInputDialog(fthis, "Please choose a precinct", " Pin Generator",
                            JOptionPane.QUESTION_MESSAGE, null, model.getPrecinctIDs(), model.getInitialSelection());

                    String pin;

                    /* If a selection was made, process it */
                    if(!precinct.equals("null")){
                        /* If the selection was for a provisional ballot, generate the appropriate authorization PIN */
                        if(precinct.contains("provisional"))
                            pin  = model.generateProvisionalPIN(precinct);
                        /* Otherwise generate a normal PIN */
                        else
                            pin = model.generatePIN(precinct);

                        /* Print the PIN to a printer connected to the Sueprvisor machine, if there is one */
                        Printer printer = new Printer();
                        printer.printPin(pin);

                        /* Show a dialog containing the PIN */
                        JOptionPane.showMessageDialog(fthis, "Your pin is: " + pin);
                    }

                }
                /* If a ballot has not be selected already, do not allow PIN generation */
                else {
                    JOptionPane.showMessageDialog(fthis, "Please select at least one ballot before generating a pin");
                }
            }
        });

        /* Add the PIN generation button to the panel */
        c.ipady = 50;
        c.gridy = 3;
        electionInfoPanel.add(pinButton, c);
        pinButton.setEnabled(false);

        /*
         * Set up the spoil ballot button, which will launch a dialog for BID input (to be done by keyboard
         * or barcode scanner, and then call the appropriate model method to spoil the corresponding ballot
         */
        spoilButton = new MyJButton("Spoil Ballot");
        spoilButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                /*
                 * We rolled our own dialog, to ensure that the dialog was modal and
                 * didn't lock up the rest of our GUI. The behavior is analogous to that of
                 * a JDialog, but with guaranteed modality.
                 */
                final JFrame frame = new JFrame("Spoil a ballot");
                frame.setPreferredSize(new Dimension(400, 200));
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setBounds(100, 100, 368, 153);
                JPanel contentPane = new JPanel();
                contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                contentPane.setLayout(new BorderLayout(0, 0));
                frame.setContentPane(contentPane);

                JPanel panel = new JPanel();
                panel.setPreferredSize(new Dimension(200, 100));
                contentPane.add(panel, BorderLayout.CENTER);
                panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

                /* Add a text field so thata BID can be typed (or scanned) in */
                final JTextArea txtrTypeABallot = new JTextArea();
                txtrTypeABallot.setRows(1);
                txtrTypeABallot.setPreferredSize(new Dimension(200, 30));
                txtrTypeABallot.setWrapStyleWord(true);
                txtrTypeABallot.setLineWrap(true);
                panel.add(txtrTypeABallot);

                /*
                 * Add a button to submit the BID for spoiling. This will pass the input BID to the
                 * model so that it can be checked for and removed from the list of committed ballots.
                 * If the ballot is there, the @Supervisor.model.Model#spoilBallot(String) method will
                 * return true if the ballot is successfully spoiled, or false if the ballot is not found.
                 */
                final JButton btnSubmitId = new JButton("Submit ID");
                btnSubmitId.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        spoiledBallotID = txtrTypeABallot.getText().split("\n")[0];
                        frame.setVisible(false);
                        boolean spoiled = model.spoilBallot(spoiledBallotID);

                        /* Show a dialog indicating the result of the spoil attempt */
                        if (spoiled) {
                            JOptionPane.showMessageDialog(fthis, "Ballot " + spoiledBallotID + " has been spoiled.");
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(fthis, spoiledBallotID + " is not a valid ballot ID. No ballot was spoiled.");
                            frame.dispose();
                        }
                    }
                });

                /* Add the button to the panel */
                panel.add(btnSubmitId);

                /* This will be a label containing instructions for enetering the BID to spoil */
                JPanel panel_1 = new JPanel();
                panel_1.setPreferredSize(new Dimension(300, 50));
                contentPane.add(panel_1, BorderLayout.NORTH);

                JLabel lblPleaseScanOr = new JLabel("Please scan or type a ballot ID");
                lblPleaseScanOr.setFont(new Font("Arial Unicode", Font.PLAIN, 16));
                panel_1.add(lblPleaseScanOr);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            }
        });

        /* Add the spoil button to the panel */
        c.ipady = 50;
        c.gridy = 4;
        electionInfoPanel.add(spoilButton, c);
        spoilButton.setEnabled(false);

        /*
         * Set up the polls open/close button, which calls a method that informs
         * the model to change the state of the polls accordingly.
         */
        pollsControlButton = new MyJButton("Open Polls Now");
        pollsControlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pollControlButtonPressed();
            }
        });

        /* Enable the text of the poll control button to change dynamically depending on the state of the polls */
        model.registerForPollsOpen(new Observer() {
            public void update(Observable o, Object arg) {
                if (model.arePollsOpen()) {
                    pollsOpenLbl.setText("Polls currently open");
                    pollsControlButton.setText("Close Polls Now");
                    spoilButton.setEnabled(true);
                    pinButton.setEnabled(true);
                } else {
                    pollsOpenLbl.setText("Polls currently closed");
                    pollsControlButton.setText("Open Polls Now");
                    spoilButton.setEnabled(false);
                    pinButton.setEnabled(false);
                }
                updateAllMachineViews();
            }
        });

        /* Add the poll control button to the panel */
        c.ipady = 100;
        c.gridy = 5;
        electionInfoPanel.add(pollsControlButton, c);


    }

    /**
     * Initializes and updates the main center panel where the machine views are displayed.
     */
    private void initializeMachineViewPanel() {
        machineViewPanel = new JPanel();
        machineViewPanel.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0,
                Color.BLACK));
        machineViewPanel.setLayout(new GridBagLayout());

        /* Update the machine views so they get added to the panel */
        updateAllMachineViews();
    }

    /**
     * Called when the left button is pressed; toggles the polls open status
     */
    private void pollControlButtonPressed() {

        /* If the polls are open, pressing the button will close the polls */
        if (model.arePollsOpen()) {

            /* Closing the polls */
           model.closePolls();

        }

        /* If the polls are not open, open them */
        else  model.openPolls();
    }

}
