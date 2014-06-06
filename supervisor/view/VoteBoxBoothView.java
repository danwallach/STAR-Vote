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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import supervisor.model.Model;
import supervisor.model.VoteBoxBooth;

/**
 * The view of a VoteBox booth on the network. Shows information such as the
 * label, status, battery level, and public/protected counts. It also has a
 * button for authorization or override. The button is hidden when the polls are
 * closed.
 *
 * @author Corey Shaw
 */
@SuppressWarnings("serial")
public class VoteBoxBoothView extends AMachineView {

    /** A reference to the Supervisor's main model */
    private Model model;

    /** A reference to the Supervisor's ActiveUI (i.e. its view ) */
    private ActiveUI view;

    /** A reference to the Votebox mini-model */
    private VoteBoxBooth machine;

    /** A label for the name of the machine */
    private JLabel nameLabel;

    /** A label for the serial number of the machine */
    private JLabel serialLabel;

    /** A label for the batter status of the machine */
    private JLabel batteryLabel;

    /** A label for the status of the machine (online, voting, etc.) */
    private JLabel statusLabel;

    /* TODO Sort out public and private counts... */
    /** A label for the machine's public count */
    private JLabel publicCountLabel;

    /** A Label for the machine's protected count */
    private JLabel protectedCountLabel;

    /** A button to allow for overrides */
    private JButton overrideButton;

    /** A panel to contain the override button */
    private JPanel buttonPanel;

    /** A panel to display the status on */
    private JPanel statusPanel;

    /** A panel to display the name on */
    private JPanel namePanel;

    /**
     * Constructs a new VoteBoxBoothView.
     *
     * @param m the Supervisor model
     * @param v the active UI (the Supervisor View)
     * @param mach the booth's model (The mini-model)
     */
    public VoteBoxBoothView(Model m, ActiveUI v, VoteBoxBooth mach) {
        /* Set up the mini-MVC stuff */
        model = m;
        view = v;
        machine = mach;
        GridBagConstraints c = new GridBagConstraints();

        /* Create all the labels using the standardized font */
        nameLabel = new MyJLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 24));

        serialLabel = new MyJLabel();
        serialLabel.setFont(serialLabel.getFont().deriveFont(9f));

        /* Add the labels to their corresponding panels, and lay them out */
        namePanel = new JPanel();
        namePanel.setLayout(new GridBagLayout());
        c.weighty = .8;
        c.anchor = GridBagConstraints.PAGE_END;
        namePanel.add(nameLabel, c);

        c.weighty = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        namePanel.add(serialLabel, c);

        /* Add a border to separate the name, serial, etc */
        namePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1,
                Color.BLACK));

        c = new GridBagConstraints();

        /* Set up the status labels and counts, and lay them out */
        batteryLabel = new JLabel();
        batteryLabel.setVisible(false);

        statusLabel = new MyJLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        publicCountLabel = new MyJLabel();
        publicCountLabel.setFont(publicCountLabel.getFont().deriveFont(9f));

        protectedCountLabel = new MyJLabel();
        protectedCountLabel.setFont(protectedCountLabel.getFont()
                .deriveFont(9f));

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
        statusPanel.add(publicCountLabel, c);

        c.gridy = 3;
        c.weighty = 1;
        statusPanel.add(protectedCountLabel, c);

        /* Add another border between the counts, and status labels */
        statusPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                Color.BLACK));
        statusPanel.setPreferredSize(new Dimension(70, 70));

        /* Now add the overrideButton */
        c = new GridBagConstraints();
        overrideButton = new MyJButton();
        overrideButton.setLayout(new GridBagLayout());

        /* When pressed the button will launch a selector for the desired override action */
        overrideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                overrideButtonPressed();
            }
        });
        overrideButton.setVisible(model.arePollsOpen());
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(overrideButton, BorderLayout.CENTER);


        /* Now lay out all of the components on this panel */
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

        /* Add a border around for aesthetics */
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        /* Set size constraints */
        setSize(180, 160);
        setMinimumSize(new Dimension(180, 175));
        setPreferredSize(new Dimension(180, 175));
        setMaximumSize(new Dimension(180, 175));

        /* Register this view to update when its corresponding mini-model does */
        machine.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                updateView();
            }
        });

        /* Update to render and populate everything */
        updateView();
    }



    /**
     * Updates the background to a given color
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
     * Queries information from the VoteBox booth's model, and updates the view
     * accordingly. Also called whenever the observer is notified.
     */
    public void updateView() {
        /* Set the name and serial number labels */
        nameLabel.setText(Integer.toString(machine.getLabel()));
        serialLabel.setText("#" + machine.getSerial());

        GridBagConstraints c = new GridBagConstraints();

        VoteBoxBooth m = machine;

        /* If the machine is online, reflect that in the view */
        if (machine.isOnline()) {

            /* Show the pulbic and protected counts */
            publicCountLabel.setVisible(true);
            protectedCountLabel.setVisible(true);
            publicCountLabel.setText("Public Count: " + m.getPublicCount());
            protectedCountLabel.setText("Protected Count: "
                    + m.getProtectedCount());
            overrideButton.removeAll();

            /* If the machine is voting, show the overrideButton and change the color */
            if (machine.getStatus() == VoteBoxBooth.IN_USE)
            {
                overrideButton.setEnabled(true);
                overrideButton.setVisible(true);
                updateBackground(Color.YELLOW);
                statusLabel.setText("In Use");
                overrideButton.add(new MyJLabel("Override"), c);
            }

            /* Provisional shall be treated just like voting, with the exception of a different status */
            if (machine.getStatus() == VoteBoxBooth.PROVISIONAL)
            {
                overrideButton.setEnabled(true);
                overrideButton.setVisible(true);
                updateBackground(Color.YELLOW);
                statusLabel.setText("Provisional Voting");
                overrideButton.add(new MyJLabel("Override"), c);
            }

            /* If the machine is not voting, set its color to white and show that it is awaiting a voter */
            if (machine.getStatus() == VoteBoxBooth.READY)
            {
                overrideButton.setEnabled(false);
                overrideButton.setVisible(false);
                updateBackground(Color.white);
                statusLabel.setText("Ready");
            }

            /* Show the battery label */
            batteryLabel.setVisible(true);
            String batteryIcon = "images/batt" + ((m.getBattery() + 10) / 20)
                    + ".png";

            URL url = ClassLoader.getSystemClassLoader().getResource(
                    batteryIcon);
            if (url != null) batteryLabel.setIcon(new ImageIcon(url));

        }

        /* If the machine is not active (i.e. disconnected), color it gray and set its status to offline */
        else {
            updateBackground(Color.LIGHT_GRAY);
            batteryLabel.setVisible(false);
            publicCountLabel.setVisible(false);
            protectedCountLabel.setVisible(false);
            statusLabel.setText("Offline");
            overrideButton.setVisible(false);
        }

        /* Re-render everything */
        revalidate();
        repaint();
    }

    /**
     * Called whenever the main button on the view is pressed, and determines
     * whether the Supervisor is attempting to override cancel, override commit, or do nothing
     */
    private void overrideButtonPressed() {
        /* This should only work if the machine is online */
        /* TODO Throw an error otherwise? */
        if (machine.isOnline()) {
            /* I think this was used for authorization. TODO Take it out */
            if (machine.getStatus() == VoteBoxBooth.READY) {
                try {
                    overrideButton.setEnabled(false);
                    overrideButton.removeAll();
                    overrideButton.add(new MyJLabel("Waiting"));
                    repaint();
                    model.authorize(machine.getSerial());
                } catch (IOException e) {
                    System.err.println("Error encountered while authorizing <"+e.getMessage()+">");
                    e.printStackTrace();
                }
            }

            /* If the machine is is in use, show the override dialog and act accordingly */
            else {
                JDialog dlg = new OverrideDialog(view, model, machine
                        .getSerial(), machine.getLabel(), machine.getStatus() == VoteBoxBooth.PROVISIONAL);
                dlg.setVisible(true);
            }
        }
    }

}
