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
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import preptool.model.layout.manager.Spacer;
import supervisor.model.BallotScannerMachine;
import supervisor.model.AMachine;
import supervisor.model.Model;
import supervisor.model.SupervisorMachine;
import supervisor.model.VoteBoxBooth;
import votebox.events.PollMachinesEvent;

/**
 * The view that is shown on an inactive supervisor - only shows the number of
 * machines connected and an Activate button. Other information is not shown
 * because we don't require an inactive supervisor to have the most up-to-date
 * information about the election and network.
 * @author cshaw
 */
@SuppressWarnings("serial")
public class InactiveUI extends JPanel {

    private Model model;

    private JButton activateButton;

    private JPanel textPanel;

    /**
     * Constructs a new InactiveUI
     * @param m the supervisor's model
     */
    public InactiveUI(Model m) {
        model = m;
        setLayout(new GridBagLayout());
        initializeComponents();

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

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paint(g);
    }

    private void initializeComponents() {
        GridBagConstraints c = new GridBagConstraints();

        textPanel = new JPanel();
        textPanel.setLayout(new GridBagLayout());
        updateTextPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 20, 0);
        add(textPanel, c);

        activateButton = new MyJButton("Activate this Console");
        activateButton.setFont(activateButton.getFont().deriveFont(Font.BOLD,
                16f));
        activateButton.setEnabled(model.isConnected());
        activateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.activate();
            }
        });
        c.gridy = 1;
        c.ipady = 100;
        c.ipadx = 200;
        c.insets = new Insets(0, 0, 0, 0);
        add(activateButton, c);
    }

    private void updateTextPanel() {
        try {
            Thread.sleep(1000);    //sleep to make sure that all machines have time to identify themselves
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        textPanel.removeAll();

        boolean tapOn = false;

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
                } else if (m.isOnline() && m.getSerial() == 0 ){
                    /**
                     * We're designating the Tap connection with a serial number of 0 always.
                     * This way we can tell that it is connected
                     */
                    tap++;
                    tapOn = true;
                }
            }
            int unknown = model.getNumConnected() - supervisors - booths - scanners - tap;
            String str = "(" + supervisors + " supervisors, " + booths
                    + " booths, " + scanners + " scanners";
            if (unknown > 0)
                str += ", " + unknown + " unknown)";
            else
                str += ")";


            label = new MyJLabel(str);
            if(tap > 0)
                label2 = new MyJLabel("Tap Connected");
            else
                label2 = new MyJLabel("Tap Offline");
        } else {
            label = new MyJLabel(
                    "You must connect to at least one other machine before you can activate.");
            label.setForeground(Color.RED);
        }
        label.setFont(label.getFont().deriveFont(20f));
        label2.setFont(label.getFont().deriveFont(20f));
        textPanel.add(label, c);
        c.gridy = 2;
        if(tapOn)
            label2.setForeground(Color.GREEN);
        else
            label2.setForeground(Color.GRAY);
        textPanel.add(label2, c);
        revalidate();
        repaint();
    }
}
