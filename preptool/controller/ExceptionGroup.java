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

package preptool.controller;

import preptool.view.dialog.ExceptionDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Copied from http://www.javaspecialists.co.za/archive/Issue081.html
 */
public class ExceptionGroup extends ThreadGroup {
    public ExceptionGroup() {
        super("ExceptionGroup");
    }

    public void uncaughtException(Thread t, Throwable e) {

        new ExceptionDialog(findActiveFrame(), e.toString(), e.getStackTrace()).showDialog();

        e.printStackTrace();
    }

    /**
     * I hate ownerless dialogs. With this method, we can find the currently
     * visible frame and attach the dialog to that, instead of always attaching
     * it to null.
     */
    private JFrame findActiveFrame() {

        Frame[] frames = JFrame.getFrames();

        /* For every frame, check if it's active and return it if it is */
        for (Frame frame : frames)
            if (frame.isVisible())
                return (JFrame) frame;

        return null;
    }
}
