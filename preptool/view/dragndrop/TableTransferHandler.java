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

package preptool.view.dragndrop;

import preptool.view.IMovableTableModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;


/**
 * <p>
 * Handler for drag and drop events in a JTable. This handler is specific in
 * that it does not package any information in the Transferable, rather it
 * requires the drag be only within the table (by checking if the components are
 * the same), and moves the row from the old index to the new index.
 * </p>
 * Inspired by Copied from
 * http://java.sun.com/docs/books/tutorial/uiswing/examples/dnd/ExtendedDnDDemoProject/src/dnd/TableTransferHandler.java
 * <br>
 * Modifications by Corey Shaw
 */
public class TableTransferHandler extends StringTransferHandler {

    private static final long serialVersionUID = 1L;

    /**
     * Index the drag originated from
     */
    private int remIndex;

    /**
     * Index to insert the row at
     */
    private int addIndex;

    /**
     * Records the current selected row index
     */
    @Override
    protected String exportString(JComponent c) {
        JTable table = (JTable) c;
        remIndex = table.getSelectedRow();
        return "";
    }

    /**
     * Records the target selected index. Fails if the indices are the same
     */
    @Override
    protected void importString(JComponent c, String str) {

        JTable target = (JTable) c;
        AbstractTableModel model = (AbstractTableModel) target.getModel();

        /* Pull the selected row */
        addIndex = target.getSelectedRow();

        /* Check if the indices are the same and, if so, set them to -1 and return */
        if (remIndex == addIndex) {
            remIndex = -1;
            addIndex = -1;
            return;
        }

        /* Check to make sure not out of bounds, and, if so, puts it at the end */
        int max = model.getRowCount();
        if (addIndex < 0 || addIndex > max - 1)
            addIndex = max - 1;
    }

    /**
     * Performs the move operation by moving the row from its old index to the
     * new one.
     */
    @Override
    protected void cleanup(JComponent c, boolean remove) {

        JTable source = (JTable) c;

        /* Make sure there is a origin index */
        if (remove && remIndex != -1) {

            AbstractTableModel model = (AbstractTableModel) source.getModel();

            /* See what type of model it is and move the row */
            if (model instanceof IMovableTableModel)
                ((IMovableTableModel) model).moveRow(remIndex, addIndex);

            else if (model instanceof DefaultTableModel)
                ((DefaultTableModel) model).moveRow(remIndex, remIndex, addIndex);

            /* Validate and repaint */
            c.validate();
            c.repaint();
        }

        /* Reset values */
        remIndex = -1;
        addIndex = -1;
    }
}
