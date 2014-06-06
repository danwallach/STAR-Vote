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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;

import supervisor.Supervisor;

/**
 * A decorator for a JLabel that uses a standardized font, and turns on
 * antialiasing.
 *
 * @author Corey Shaw
 */
@SuppressWarnings("serial")
public class MyJLabel extends JLabel {

    /**
     * Default constructor, just sets up the standardized font
     */
    public MyJLabel() {
        super();
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJLabel with a specified icon and horizontal alignment
     *
     * @param icon the icon the label displays
     * @param horizontalAlignment how the elements of the label should be horizontally aligned
     */
    @SuppressWarnings("unused")
    public MyJLabel(Icon icon, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJLabel with a specified icon
     *
     * @param icon the icon the label displays
     */
    @SuppressWarnings("unused")
    public MyJLabel(Icon icon) {
        super(icon);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJLabel with a specified icon, text, and horizontal alignment
     *
     * @param text the text the label displays
     * @param icon the icon the label displays
     * @param horizontalAlignment how the elements of the label should be horizontally aligned
     */
    @SuppressWarnings("unused")
    public MyJLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJLabel with a specified text and horizontal alignment
     *
     * @param text the text the label displays
     * @param horizontalAlignment how the elements of the label should be horizontally aligned
     */
    @SuppressWarnings("unused")
    public MyJLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        setFont(new Font(Supervisor.FONTNAME, Font.PLAIN, 12));
    }

    /**
     * Creates a new MyJLabel with a specified text
     *
     * @param text the text the label displays
     */
    public MyJLabel(String text) {
        super(text);
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
