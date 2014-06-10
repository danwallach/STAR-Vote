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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import preptool.view.ProgressInfo;


/**
 * A progress dialog that corresponds to an asynchronous task
 * 
 * @author Corey Shaw
 */
public class ProgressDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * The title label on the dialog
     */
    private JLabel titleLabel;

    /**
     * The sub task label on the dialog
     */
    private JLabel subTaskLabel;

    /**
     * The progress bar on the dialog
     */
    private JProgressBar progressBar;

    /**
     * The sub sub task label on the dialog
     */
    private JLabel subSubTaskLabel;

    /**
     * The sub progress bar on the dialog
     */
    private JProgressBar subProgressBar;

    /**
     * The cancel button on the dialog
     */
    private JButton cancelButton;

    /**
     * The OK button on the dialog
     */
    private JButton okButton;

    private ProgressInfo pInfo;

    /**
     * Constructs a new ProgressTask with the given parent frame and title
     * 
     * @param parent        the parent frame
     * @param title         the title of the task
     */
    public ProgressDialog(JFrame parent, String title) {

        /* Call super and setup the GUI */
        super(parent, title, true);
        this.setSize(500, 250);
        this.setLocationRelativeTo(parent);
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Create a new label for the title and position and add it to the form */
        titleLabel = new JLabel(title + "...");
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(15, 15, 15, 15);
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        this.add(titleLabel, c);

        /* Create a new progress bar and position and add it to the form */
        progressBar = new JProgressBar();
        c.gridy = 1;
        c.insets = new Insets(0, 15, 10, 15);
        c.weighty = 0;
        this.add(progressBar, c);

        /* Create a new label for the subtask and position and add it to the form */
        subTaskLabel = new JLabel("Initializing");
        c.gridy = 2;
        this.add(subTaskLabel, c);

        /* Create a new subtask progress bar and positoin and add it to the form */
        subProgressBar = new JProgressBar();
        c.gridy = 3;
        this.add(subProgressBar, c);

        /* Create a new label for subsubtask and position and add it to the form */
        subSubTaskLabel = new JLabel("Initializing");
        c.gridy = 4;
        this.add(subSubTaskLabel, c);

        /* Create a new button panel */
        JPanel buttonPanel = new JPanel();

        /* Create a new cancel button */
        cancelButton = new JButton("Cancel");

        /* Add a new action listener for the cancel button */
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pInfo.cancel();
            }
        } );

        /* Add the cancel button to the panel */
        buttonPanel.add(cancelButton);

        /* Create a new OK button */
        okButton = new JButton("OK");

        /* Create a new action listener for the OK button and disable */
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible( false );
            }
        } );
        okButton.setEnabled(false);

        /* Add the OK button to the panel */
        buttonPanel.add(okButton);

        /* Position new insets and add the button panel to the form */
        c.gridy = 5;
        c.insets = new Insets( 0, 15, 15, 15 );
        c.weighty = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.PAGE_END;
        this.add(buttonPanel, c);

        /* Setup for progress monitoring */
        pInfo = new ProgressInfo();
        pInfo.addObserver(new Observer() {

            public void update(Observable o, Object arg) {

                if      (pInfo.isFinished())  finished();
                else if (pInfo.isCancelled()) setVisible(false);
                else  updateProgress(pInfo.getSubTaskName(), pInfo.getTaskProgress(), pInfo.getSubSubTaskName(), pInfo.getSubTaskProgress());
            }
        } );
    }

    /**
     * Returns the progress info, which users can check to see this task's
     * progress, and update the progress which will cause the dialog to update
     * as well
     */
    public ProgressInfo getProgressInfo() {
        return pInfo;
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

    private void finished() {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                /* Set the subtask label and progress bar to complete */
                subTaskLabel.setText("Finished.");
                progressBar.setValue(100);

                /* Set the subsubtask label and progress bar to complete */
                subSubTaskLabel.setText("Finished.");
                subProgressBar.setValue(100);

                /* Disable the cancel button and enable the OK button */
                cancelButton.setEnabled(false);
                okButton.setEnabled(true);
            }
        } );
    }

    private void updateProgress(final String subTask, final int percentComplete, final String subSubTask, final int subPercentComplete) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                /* Update the subtask label and progress bar */
                subTaskLabel.setText(subTask);
                progressBar.setValue(percentComplete);

                /* Update the subsubtask label and progress bar */
                subSubTaskLabel.setText(subSubTask);
                subProgressBar.setValue(subPercentComplete);
            }
        } );
    }
}
