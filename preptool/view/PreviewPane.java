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

package preptool.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel that contains the live preview of the current card.
 * 
 * @author Corey Shaw
 */
public class PreviewPane extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Button to refresh the current preview
     */
    private JButton refreshButton;

    /**
     * Main scrollbar pane that holds the preview
     */
    private JScrollPane mainScrollPane;

    /**
     * Generator that gives the PreviewPane a list of JPanels to show when the
     * user clicks the refresh button
     */
    private IPreviewPaneGenerator generator;

    /**
     * Constructs a new preview pane with the given preview pane generator
     * 
     * @param generator         the preview pane generator
     */
    public PreviewPane(IPreviewPaneGenerator generator) {

        /* Setup */
        super();
        this.generator = generator;

        /* Create a new layout */
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Create a new label and setup font, positioning, etc., then add */
        JLabel previewLabel = new JLabel("Preview");
        previewLabel.setFont(new Font("Arial", Font.BOLD, 16));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10, 10, 10, 0);
        add(previewLabel, c);

        /* Initialise and add the refresh button */
        initializeRefreshButton();
        c.gridx = 1;
        c.weightx = 0;
        c.insets = new Insets(10, 10, 10, 10);
        add(refreshButton, c);

        /* Create and add a scroll pane */
        mainScrollPane = new JScrollPane();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        c.insets = new Insets(0, 10, 10, 10);
        add(mainScrollPane, c);
    }

    /**
     * Initializes the refresh button
     */
    private void initializeRefreshButton() {

        ImageIcon icon;

        /* Try to load the image icon */
        try { icon = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/view-refresh.png")); }
        catch (Exception e) { icon = null; }

        /* Initialise the button */
        refreshButton = new JButton(new AbstractAction("Refresh", icon) {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                refreshButtonPressed();
            }

        } );
    }

    /**
     * Called when the refresh button is pressed
     */
    public void refreshButtonPressed() {
        ArrayList<JPanel> panel = generator.getPreviewPanels();
        setScrollPane(panel);
    }

    /**
     * Sets the scroll pane to the given list of panels
     * 
     * @param panels        the panels
     */
    public void setScrollPane(ArrayList<JPanel> panels) {

        /* Remove the current pane and create a new set of constraints */
        remove(mainScrollPane);
        GridBagConstraints c = new GridBagConstraints();

        /* Create a new label */
        JLabel label = new JLabel();

        /* See if there are more than one panel */
        if (panels.size() > 1) {

            /* If so, set up the preview pane with dashed lines between cards */
            BufferedImage dashedLine = new BufferedImage(600, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = dashedLine.createGraphics();
            graphics.setColor(Color.BLACK);
            BasicStroke stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[] {1f}, 0f);

            /* Dashed line */
            graphics.setStroke(stroke);
            graphics.drawLine(0, 0, 600, 0);
            label.setIcon(new ImageIcon(dashedLine));
        }

        /* Create a new panel */
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.validate();
        panel.repaint();

        /* Take each panel */
        for (int i = 0; i < panels.size(); i++) {

            /* Add the dashed line separator */
            if (i >= 1) {
                c.gridy = 2 * i - 1;
                c.insets = new Insets(0, 0, 0, 0);
                panel.add(label, c);
            }

            /* Add the panels */
            c.gridx = 0;
            c.gridy = 2 * i;
            c.insets = new Insets(15, 0, 15, 0);
            panel.add(panels.get(i), c);
        }

        /* Setup positioning, colour, etc. and add to the form */
        mainScrollPane = new JScrollPane(panel);
        panel.setBackground(Color.WHITE);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 10, 10, 10);
        add(mainScrollPane, c);
        validate();
        repaint();
    }

    /**
     * Clears the preview pane (when a user navigates away from the current
     * card)
     */
    public void clear() {
        setScrollPane( new ArrayList<JPanel>() );
    }

}
