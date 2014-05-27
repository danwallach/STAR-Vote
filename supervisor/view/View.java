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

import java.util.Observable;
import java.util.Observer;

import javax.swing.*;

import supervisor.model.Model;

/**
 * The Supervisor's view. The common denominator is simply the frame, and the
 * observer that switches views between the active and inactive UI.
 * @author cshaw
 */
@SuppressWarnings("serial")
public class View extends JFrame {

    /** A reference to the inactive UI, to show when the Supervisor is inactive */
    InactiveUI inactiveUI;

    /** A reference to the active UI to show when the Supervisor is active */
    ActiveUI activeUI;

    /* TODO Make these more configurable? */
    /** A default width for the view */
    public static final int WINDOW_WIDTH = 1600;

    /** A default height for the view */
    public static final int WINDOW_HEIGHT = 900;

    /**
     * Constructs a new View
     *
     * @param model the Supervisor model
     */
    public View(final Model model) {
        /* Set the window label */
        super("Supervisor Console");

        /* Set the size and close operations */
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        /* Instantiate the UI's */
        activeUI = new ActiveUI(model);
        inactiveUI = new InactiveUI(model);

        /* Register the view to update depending on the state of the machine, e.g. active or inactive */
        model.registerForActivated(new Observer() {
            public void update(Observable o, Object arg) {
                /* If the model is active, set the pane to the active UI */
                if (model.isActivated())
                    setContentPane(activeUI);

                /* Otherwise dispose of the active UI and switch to the inactive pane */
                else {
                    activeUI.setVisible(false);
                    setContentPane(inactiveUI);
                }

                /* revalidate and repaint to render everything */
                validate();
                repaint();
            }
        });
    }

    /**
     * Shows the inactive UI (called after the keyword has been entered).<br>
     * The view is blank until this is called.
     */
    public void display() {
        setContentPane(inactiveUI);
        validate();
        repaint();
    }

}
