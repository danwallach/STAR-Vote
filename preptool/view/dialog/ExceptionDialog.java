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

package preptool.view.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExceptionDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ProgressTask with the given parent frame and title
     * 
     * @param parent            the parent frame
     * @param stackTrace        the exception's stack trace
     */
    public ExceptionDialog(JFrame parent, String message, StackTraceElement[] stackTrace) {

        /* Call super */
        super(parent, "Unhandled Exception", true);

        /* Set the GUI up */
        this.setSize(700, 500);
        this.setLocationRelativeTo(parent);
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Create, position, and add the title label on the dialogue */
        JLabel titleLabel = new JLabel(message);
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(15, 15, 15, 15);
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        this.add(titleLabel, c);

        /* Initialise the stack trace String */
        String stackTraceString = "";

        /* Build the String based on the stack trace */
        for (StackTraceElement s : stackTrace)
            stackTraceString += s + "\n";

        /* Create, position, and add the sub task label and scroll pane on the dialogue */
        JTextArea textArea = new JTextArea();
        textArea.setText(stackTraceString);
        c.gridy = 1;
        c.insets = new Insets(0, 15, 15, 15);
        c.weighty = 1;
        JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setBorder(BorderFactory.createTitledBorder("Stack Trace:"));
        this.add(textAreaScrollPane, c);

        /* Create a new button panel */
        JPanel buttonPanel = new JPanel();

        /* Create the OK button on the dialogue */
        JButton okButton = new JButton("OK");

        /* Create an action listener for the OK button */
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        /* Add the OK button to the panel */
        buttonPanel.add(okButton);

        /* Add the panel to the dialogue */
        c.gridy = 2;
        c.insets = new Insets(0, 15, 15, 15);
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.PAGE_END;
        this.add(buttonPanel, c);
    }

    /**
     * Shows the dialog.
     */
    public void showDialog() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                setVisible( true );
            }
        } );
    }

}
