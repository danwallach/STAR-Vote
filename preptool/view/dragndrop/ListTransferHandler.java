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

import javax.swing.*;
import java.util.ArrayList;

/**
 * <p>
 * Handler for drag and drop events in a JList. This handler is specific in that
 * it does not package any information in the Transferable, rather it requires
 * the drag be only within the list (by checking if the components are the
 * same), and moves the item from the old index to the new index.
 * </p>
 * Inspired by
 * http://java.sun.com/docs/books/tutorial/uiswing/examples/dnd/ExtendedDnDDemoProject/src/dnd/ListTransferHandler.java
 * <br>
 * Modifications by Corey Shaw
 */
public class ListTransferHandler extends StringTransferHandler {

    private static final long serialVersionUID = 1L;

    /**
     * Index the drag originated from
     */
    private int remIndex;

    /**
     * Index to insert the element at
     */
    private int addIndex;

    /**
     * Array of listeners that are called whenever a successful drag occurs
     */
    private ArrayList<ListTransferListener> listeners = new ArrayList<ListTransferListener>();

    /**
     * Records the current selected index
     */
    @Override
    protected String exportString(JComponent c) {

        JList list = (JList) c;
        remIndex = list.getSelectedIndex();
        addIndex = -1;
        return "";
    }

    /**
     * Records the target selected index. Fails if the indices are the same
     */
    @Override
    protected void importString(JComponent c, String str) {

        JList target = (JList) c;
        DefaultListModel listModel = (DefaultListModel) target.getModel();

        /* Pull the selected index */
        addIndex = target.getSelectedIndex();

        /* Make sure that they are not the same and reset and return if they are */
        if (remIndex == addIndex) {
            remIndex = -1;
            addIndex = -1;
            return;
        }

        /* Make sure that it's not out of bounds -- if it is, set it to maximum */
        int max = listModel.getSize();
        if (addIndex < 0 || addIndex > max - 1)
            addIndex = max - 1;
    }

    /**
     * Performs the move operation by moving the list item from its old index to
     * the new one. Also calls all listeners.
     */
    @Override
    protected void cleanup(JComponent c, boolean remove) {

        /* Check to make sure there is an old index */
        if (remove && remIndex != -1) {

            JList source = (JList) c;
            DefaultListModel model = (DefaultListModel) source.getModel();

            /* Remove the data at the old index*/
            Object item = model.remove(remIndex);

            /* Add the item to the add index */
            model.add(addIndex, item);

            /* For each of the listeners, fire a list item moved event */
            for (ListTransferListener l : listeners)
                l.listItemMoved(remIndex, addIndex);

            /* Set the selected index to the add index and validate */
            source.setSelectedIndex(addIndex);
            c.validate();
        }

        /* Reset the indices */
        remIndex = -1;
        addIndex = -1;
    }

    /**
     * Adds a transfer listener to the list
     * 
     * @param l         the listener
     */
    public void addListTransferListener(ListTransferListener l) {
        listeners.add(l);
    }

}
