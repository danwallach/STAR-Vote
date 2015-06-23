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

import supervisor.Supervisor;

import javax.swing.*;
import java.awt.*;

/**
 * A decorator for a JButton that uses a standardized font, and turns on
 * antialiasing.
 *
 * @author Corey Shaw
 */
@SuppressWarnings("serial")
public class MyJButton extends JButton {

    /**
     * Default constructor, only sets the font
     */
    public MyJButton() {
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJButton with a given Icon.
     *
     * @param icon icon for the button to display
     */
    @SuppressWarnings("unused")
    public MyJButton(Icon icon) {
        super(icon);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJButton with a given text
     *
     * @param text text for the button to display
     */
    public MyJButton(String text) {
        super(text);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJButton with a given default action
     *
     * @param action the action this button performs when clicked.
     */
    @SuppressWarnings("unused")
    public MyJButton(Action action) {
        super(action);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJButton with given text and icon
     *
     * @param text text for the button to display
     * @param icon icon for the button to display
     */
    @SuppressWarnings("unused")
    public MyJButton(String text, Icon icon) {
        super(text, icon);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Overridden paint method that turns on antialiasing
     */
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paint(g);
    }

}
