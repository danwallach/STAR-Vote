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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import supervisor.model.machine.SupervisorMachine;

/**
 * The view for another supervisor on the network.
 *
 * NOTE: no supervisor will have a machine representation of itself,
 *
 * @author Corey Shaw
 */
@SuppressWarnings("serial")
public class SupervisorMachineView extends AMachineView {

    /** The mini-model for the supervisor */
    private SupervisorMachine machine;

    /** Label for the machine's status */
    private JLabel statusLabel;

    /** Label indicating that this is the Supervisor currently in control */
    /* TODO This should not be necessary, as multiple Supervisors should be able to be active and functional */
    private JLabel currentLabel;

    /**
     * Constructs a new SupervisorMachineView by instantiating graphical components and adding observers
     *
     * @param mach the supervisor machine model
     */
    public SupervisorMachineView(SupervisorMachine mach) {
        machine = mach;

        /* Set up the view's layout */
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Create and add a Supervisor label, using the standardized font */
        JLabel supervisorLabel = new MyJLabel("Supervisor");
        supervisorLabel.setFont(supervisorLabel.getFont().deriveFont(Font.BOLD,
                16f));
        c.gridy = 0;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        add(supervisorLabel, c);

        /* Create and add a serial number label, using the machine's serial */
        JLabel serialLabel = new MyJLabel("#" + mach.getSerial());
        c.gridy = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);
        add(serialLabel, c);

        /* Create and add the status label */
        statusLabel = new MyJLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        add(statusLabel, c);

        /* Create and add the current label TODO Get rid of this? */
        currentLabel = new MyJLabel();
        c.gridy = 3;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_START;
        add(currentLabel, c);

        /* Add a border around the panel for aesthetics */
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setSize(180, 160);
        setMinimumSize(new Dimension(180, 175));
        setPreferredSize(new Dimension(180, 175));
        setMaximumSize(new Dimension(180, 175));

        /* Add an observer that will update this view when the state of the mini-model (mach) changes */
        machine.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                updateView();
            }
        });

        /* Update the view and populate the labels, etc. */
        updateView();
    }

    /**
     * Queries information from the supervisor machine's model, and updates the
     * view accordingly. Also called whenever the observer is notified.
     */
    public void updateView() {
        /* If the machine is online, set its color to AWT cyan and set its activity label */
        if (machine.isOnline()) {
            updateBackground(Color.CYAN);
            if (machine.getStatus() == SupervisorMachine.ACTIVE)
                statusLabel.setText("Active");
            else
                statusLabel.setText("Inactive");
        } else {
            updateBackground(Color.LIGHT_GRAY);
            statusLabel.setText("Offline");
        }

        /* If this is the Supervisor in charge, set the label accordingly */
        /* TODO Get rid of this? */
        if (machine.isCurrentMachine())
            currentLabel.setText("(Current machine)");
        else
            currentLabel.setText("");

        /* Refresh all the graphics */
        revalidate();
        repaint();
    }

    /**
     * Updates the background to a given color to reflect the current status
     *
     * @param c the color
     */
    private void updateBackground(Color c) {
        setBackground(c);
    }

}
