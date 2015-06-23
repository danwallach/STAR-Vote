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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * <p>
 * Abstract handler for drag and drop events that uses Strings as its
 * Transferable representation. This class also checks to see if the drag and
 * drop was on the same component, only then does it complete.
 * </p>
 * Inspired by Copied from
 * http://java.sun.com/docs/books/tutorial/uiswing/examples/dnd/ExtendedDnDDemoProject/src/dnd/StringTransferHandler.java
 * <br>
 * Modifications by Corey Shaw
 */
public abstract class StringTransferHandler extends TransferHandler {

	/**
	 * Component that originated the drag
	 */
	protected JComponent fromComponent;

	/**
	 * Whether the data was dragged to the same component
	 */
	protected boolean sameComponent;

	/**
	 * Abstract method to export a string from the component
     *
	 * @param c         the component
	 * @return          a String
	 */
	protected abstract String exportString(JComponent c);

	/**
	 * Imports a String into a component
     *
	 * @param c         the component
	 * @param str       the String
	 */
	protected abstract void importString(JComponent c, String str);

	/**
	 * Finishes a successful drag and drop operation
     *
	 * @param c         the component
	 * @param remove    whether to move the data
	 */
	protected abstract void cleanup(JComponent c, boolean remove);

	/**
	 * Creates a StringSelection from the return value of exportString
	 */
	@Override
    protected Transferable createTransferable(JComponent c) {

		fromComponent = c;
		sameComponent = false;
		return new StringSelection(exportString(c));
	}

	/**
	 * @return MOVE
	 */
	@Override
    public int getSourceActions(JComponent c) {
		return MOVE;
	}

	/**
	 * Imports the data from a transferable to a component
	 */
	@Override
    public boolean importData(JComponent c, Transferable t) {

        /* Check the component to see if it came from transferable and can import */
		if (c == fromComponent && canImport(c, t.getTransferDataFlavors())) {

            /* Try tp transfer data to a String and import the string into the component */
			try {
				String str = (String) t.getTransferData(DataFlavor.stringFlavor);
				importString(c, str);
				sameComponent = true;
				return true;
			}
            catch (UnsupportedFlavorException | IOException e) { e.printStackTrace(); }
        }

		return false;
	}

	/**
	 * Called when the drag and drop operation is complete
	 */
	@Override
    protected void exportDone(JComponent c, Transferable data, int action) {

        /* Checks for same component and cleans up if so */
		if (sameComponent) cleanup(c, action == MOVE);

        /* Reset values */
        fromComponent = null;
		sameComponent = false;
	}

	/**
	 * Checks if the component can accept data from a drag
	 */
	@Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {

        /* Make sure c is the from component */
        if (c == fromComponent) {

            /* Cycle through the flavors */
            for (DataFlavor flavor : flavors) {

                /* If the right one is found, return true */
                if (DataFlavor.stringFlavor.equals(flavor))
                    return true;
            }
        }

		return false;
	}
}
