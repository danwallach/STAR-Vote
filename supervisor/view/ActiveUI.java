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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import printer.Printer;
import supervisor.model.AMachine;
import supervisor.model.Model;
import supervisor.model.TapMachine;
import votebox.AuditoriumParams;

/**
 * The view that is shown on a supervisor that is active - consists of
 * information about the election, and a grid of all of the machines on the
 * network.
 * @author cshaw
 */
@SuppressWarnings("serial")
public class ActiveUI extends JPanel {

    private Model model;

    private MachineViewGenerator viewGen;

    private JPanel leftPanel;

    private JLabel timeLbl;

    private JLabel pollsOpenLbl;

    private JLabel tapConnectedLabel;

    private JButton leftButton;

    private JButton ballotButton;

    private JButton pinButton;

    private JButton spoilButton;

    private JPanel mainPanel;

    private JFileChooser ballotLocChooser;

    private HashMap<String, String> precinctsToBallots;

    private static String ballotID;

    private TapView tapView;

    private static boolean scanned = false;

//    private static final Scanner scanner = new Scanner(System.in);



    /**
     * Constructs a new ActiveUI
     * @param m the supervisor's model
     */
    public ActiveUI(Model m) {
        model = m;
        viewGen = new MachineViewGenerator();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        precinctsToBallots = new HashMap<String, String>();

        initializeLeftPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(leftPanel, c);

        initializeMainPanel();
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        add(mainPanel, c);

        model.registerForMachinesChanged(new Observer() {
            public void update(Observable o, Object arg) {
                updateAllMachineViews();
            }
        });
    }

    /**
     * Turns on antialiasing
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
        mainPanel.removeAll();
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        int i = 0;
        for (AMachine m : model.getMachines()) {
            if(m instanceof TapMachine){
                tapView.setMachine((TapMachine)m);
                continue;
            }
            c.gridx = i % 4;
            c.gridy = i / 4;
            innerPanel.add(viewGen.generateView(model, this, m), c);
            ++i;
        }

        System.out.println("Updating Tap's View");

        tapView.updateView();

        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(innerPanel, c);
        validate();
        repaint();
    }


    private void initializeLeftPanel() {
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel leftLabelPanel = new JPanel();
        leftLabelPanel.setLayout(new GridBagLayout());
        leftLabelPanel.add(new MyJLabel(model.getParams().getElectionName()), c);
        Date d = new Date();
        c.gridy = 1;
        leftLabelPanel.add(new MyJLabel(DateFormat.getDateInstance(
                DateFormat.LONG).format(d)), c);
        c.gridy = 2;
        timeLbl = new MyJLabel(DateFormat.getTimeInstance(DateFormat.LONG)
                .format(d));
        leftLabelPanel.add(timeLbl, c);

        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLbl.setText(DateFormat.getTimeInstance(DateFormat.LONG)
                        .format(new Date()));
            }
        }).start();

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
        leftPanel.add(leftLabelPanel, c);

        tapView = new TapView(null);
        c.ipady = 20;
        c.gridy = 1;
        c.insets = new Insets(20,20,20,20);
        leftPanel.add(tapView, c);

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

        ballotButton = new MyJButton("Select Ballot");
        ballotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int answer = ballotLocChooser.showOpenDialog(ActiveUI.this);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    File selected = ballotLocChooser.getSelectedFile();
                    String ballot = selected.getName();
                    String precinct = ballot.substring(ballot.length()-7, ballot.length()-4);
                    System.out.println(precinct);
                    precinctsToBallots.put(precinct, selected.getAbsolutePath());
                    model.addBallot(ballotLocChooser.getSelectedFile());
                }
            }
        });
        c.ipady = 50;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(20, 20, 20, 20);
        leftPanel.add(ballotButton, c);

        final JPanel fthis = this;

        pinButton = new MyJButton("Generate Pin");
        pinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (model.getSelections().length > 0) {
                    String precinct = "" + JOptionPane.showInputDialog(fthis, "Please choose a precinct", " Pin Generator",
                            JOptionPane.QUESTION_MESSAGE, null, model.getSelections(), model.getInitialSelection());

                    String pin;

                    if(!precinct.equals("null")){
                        if(precinct.contains("provisional"))
                            pin  = model.generateProvisionalPin(precinct);
                        else
                            pin = model.generatePin(precinct);

                        Printer printer = new Printer();

                        printer.printPin(pin);
                        JOptionPane.showMessageDialog(fthis, "Your pin is: " + pin);
                    }

                } else {
                    JOptionPane.showMessageDialog(fthis, "Please select at least one ballot before generating a pin");
                }
            }
        });
        c.ipady = 50;
        c.gridy = 3;
        leftPanel.add(pinButton, c);

        spoilButton = new MyJButton("Spoil Ballot");
        spoilButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String bid = "-1";

                final JFrame frame = new JFrame ("Spoil a ballot");
                frame.setPreferredSize(new Dimension(400, 200));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setBounds(100, 100, 368, 153);
                JPanel contentPane = new JPanel();
                contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                contentPane.setLayout(new BorderLayout(0, 0));
                frame.setContentPane(contentPane);

                JPanel panel = new JPanel();
                panel.setPreferredSize(new Dimension(200, 100));
                contentPane.add(panel, BorderLayout.CENTER);
                panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

                final JTextArea txtrTypeABallot = new JTextArea();
                txtrTypeABallot.setRows(1);
                txtrTypeABallot.setPreferredSize(new Dimension(200, 30));
                txtrTypeABallot.setWrapStyleWord(true);
                txtrTypeABallot.setLineWrap(true);
                panel.add(txtrTypeABallot);


                final JButton btnSubmitId = new JButton("Submit ID");
                btnSubmitId.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        ballotID = txtrTypeABallot.getText().split("\n")[0];
                        frame.setVisible(false);
                        scanned = true;
                        boolean spoiled = model.spoilBallot(ballotID);
                        if(spoiled)
                        {
                            JOptionPane.showMessageDialog(fthis, "Ballot " + ballotID + " has been spoiled.");
                            frame.dispose();
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(fthis, ballotID + " is not a valid ballot ID. No ballot was spoiled.");
                            frame.dispose();
                        }
                    }
                });

                panel.add(btnSubmitId);

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

        c.ipady = 50;
        c.gridy = 4;
        leftPanel.add(spoilButton, c);

        leftButton = new MyJButton("Open Polls Now");
        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                leftButtonPressed();
            }
        });
        model.registerForPollsOpen(new Observer() {
            public void update(Observable o, Object arg) {
                if (model.isPollsOpen()) {
                    pollsOpenLbl.setText("Polls currently open");
                    leftButton.setText("Close Polls Now");
                } else {
                    pollsOpenLbl.setText("Polls currently closed");
                    leftButton.setText("Open Polls Now");
                }
                updateAllMachineViews();
            }
        });
        c.ipady = 100;
        c.gridy = 5;
        leftPanel.add(leftButton, c);


    }

    private void initializeMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0,
                Color.BLACK));
        mainPanel.setLayout(new GridBagLayout());
        updateAllMachineViews();
    }

    /**
     * Called when the left button is pressed; toggles the polls open status
     */
    private void leftButtonPressed() {
        if (model.isPollsOpen()) {
            Map<String, Map<String, BigInteger>> tally = model.closePolls();

            for(String precinct : tally.keySet()){
                TallyResultsFrame tallyDlg = new TallyResultsFrame(this, tally.get(precinct), precinctsToBallots.get(precinct));
            }

        } else
            model.openPolls();
    }

}
