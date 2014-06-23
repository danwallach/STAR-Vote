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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import supervisor.model.*;
import supervisor.model.Machine.*;

/**
 * The view that is shown on an inactive supervisor - only shows the number of
 * machines connected, what kind of machines are connected, and an Activate button.
 * Other information is not shown because we don't require an inactive supervisor
 * to have the most up-to-date information about the election and network.
 *
 * TODO We may revise this to include password protection or some form of authentication
 *
 * @author Corey Shaw
 */
@SuppressWarnings("serial")
public class InactiveUI extends JPanel {

    /** Usual MVC model reference */
    private Model model;

    /** Allows users to activate the supervisor console after it has connected to another machine */
    private JButton activateButton;

    /** Contains all of the text labels with info about the network status, etc. */
    private JPanel textPanel;

    /**
     * Constructs a new InactiveUI
     *
     * @param m the supervisor's model
     */
    public InactiveUI(Model m) {
        model = m;
        setLayout(new GridBagLayout());
        initializeComponents();

        /*
         * The two following registers allow  the inactive ui to
         * update based on changes in network connections
         */
        model.registerForConnected(new Observer() {
            public void update(Observable o, Object arg) {
                activateButton.setEnabled(model.isConnected());
            }
        });

        model.registerForMachinesChanged(new Observer() {
            public void update(Observable o, Object arg) {
                updateTextPanel();
            }
        });
    }

    /**
     * Overrides paint(Graphics g) from jPanel. Turns antialiasing on.
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
     * Adds button and initial text to the frame
     */
    private void initializeComponents() {
        /* Using GridBag for fine-grained layout control */
        GridBagConstraints c = new GridBagConstraints();

        /* Set up the text panel and add it to the panel */
        textPanel = new JPanel();
        textPanel.setLayout(new GridBagLayout());
        updateTextPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 20, 0);
        add(textPanel, c);

        /* Set up activateButton */
        activateButton = new MyJButton("Activate this Console");
        activateButton.setFont(activateButton.getFont().deriveFont(Font.BOLD,
                16f));

        /* Whether this button is enabled depends on whether it is connected to other machines */
        activateButton.setEnabled(model.isConnected());

        /* Activate the model if this button is pressed */
        activateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.activate();
            }
        });

        /* Add the activate button */
        c.gridy = 1;
        c.ipady = 100;
        c.ipadx = 200;
        c.insets = new Insets(0, 0, 0, 0);
        add(activateButton, c);
    }

    /**
     * Updates the text that displays type and quantity of machines on the network. Uses supervisors getMachines() method
     * to identify all known machines on the network.
     */
    private void updateTextPanel() {
        try {
            Thread.sleep(1000);    /* sleep to make sure that all machines have time to identify themselves */
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* clear the panel */
        textPanel.removeAll();

        /* This will be used to determine Tap's status */
        boolean tapOn = false;

        /* Set up the text label */
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);
        JLabel label = new MyJLabel("Currently connected to "
                + model.getNumConnected() + " machines");
        JLabel label2 = new JLabel();
        label.setFont(label.getFont().deriveFont(20f));
        textPanel.add(label, c);
        textPanel.add(label2, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);

        /*
         * If the machine is connected to others, iterate through them and
         * figure out what kinds of machines are connected and how many of each there are
         */
        if (model.isConnected()){
            int supervisors = 0;
            int booths = 0;
            int scanners = 0;
            int tap = 0;

            for (AMachine m : model.getMachines()) {
                if (m instanceof SupervisorMachine && m.isOnline() && m.getSerial() != model.getMySerial()) {
                    supervisors++;
                } else if (m instanceof VoteBoxBooth && m.isOnline()){
                    booths++;
                } else if (m instanceof BallotScannerMachine && m.isOnline()){
                    scanners++;
                } else if ((m instanceof TapMachine) && m.isOnline()){
                    /*
                     * NOTE: We're designating the Tap connection with a serial number of 0 always.
                     * This way we can tell that it is connected
                     */
                    tap++;
                    tapOn = true;
                }
            }

            /* Whatever hasn't been found must be an unknown kind of machine */
            int unknown = model.getNumConnected() - supervisors - booths - scanners - tap;
            String str = "(" + supervisors + " supervisors, " + booths
                    + " booths, " + scanners + " scanners";

            /* If there are any unknown machines, change the output to reflect that */
            if (unknown > 0)
                str += ", " + unknown + " unknown)";
            else
                str += ")";

            /* Now fill in the status of Tap */
            label = new MyJLabel(str);
            if(tap > 0)
                label2 = new MyJLabel("Tap Connected");
            else
                label2 = new MyJLabel("Tap Offline");
        }
        /* If there are no connections, display a message indicating that connections are necessary for activation */
        else {
            label = new MyJLabel(
                    "You must connect to at least one other machine before you can activate.");
            label.setForeground(Color.RED);
        }

        /* Now add the labels */
        label.setFont(label.getFont().deriveFont(20f));
        label2.setFont(label.getFont().deriveFont(20f));
        textPanel.add(label, c);
        c.gridy = 2;

        /* If the tap is connected, color the second label green. Otherwise color it gray */
        if(tapOn)
            label2.setForeground(new Color(0, 150, 0));
        else
            label2.setForeground(Color.GRAY);
        textPanel.add(label2, c);

        /* update the graphical components */
        revalidate();
        repaint();
    }
}
