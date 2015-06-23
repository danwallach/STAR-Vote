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

package preptool.model.layout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.XMLTools;

import java.util.ArrayList;

/**
 * A ToggleButtonGroup is a set of ToggleButtons that follow a strategy when
 * selected. For instance, a standard strategy is to only allow one ToggleButton
 * to be selected at a time; when another is clicked, the others are deselected.
 * This is analogous to a Card in the ballot, but can be used for things that
 * aren't in the ballot as well (such as language selection).
 *
 * @author Corey Shaw
 */
public class ToggleButtonGroup extends ALayoutComponent {

	/** The strategy of this ToggleButtonGroup */
	private String strategy;

	/** The array of ToggleButtons in this group  */
	private ArrayList<ToggleButton> buttons;

	/**
	 * Constructs a new ToggleButtonGroup with given strategy
     *
	 * @param strategy      the strategy
	 */
	public ToggleButtonGroup(String strategy) {
		super("");
		this.strategy = strategy;
		buttons = new ArrayList<>();
	}

	/**
	 * Calls the forToggleButtonGroup method in visitor
     *
	 * @see preptool.model.layout.ALayoutComponent#execute(ILayoutComponentVisitor, Object[])
	 */
	@Override
	public <P,R> R execute(ILayoutComponentVisitor<P,R> visitor, P... param) {
		return visitor.forToggleButtonGroup(this, param);
	}

	/**
	 * @return the buttons
	 */
	public ArrayList<ToggleButton> getButtons() {
		return buttons;
	}

	/**
	 * @return the strategy
	 */
	public String getStrategy() {
		return strategy;
	}

	/**
	 * Converts this ToggleButtonGroup object to XML
     *
	 * @param doc       the document this component is a part of
     * @return          the XML element representation for this ToggleButtonGroup
	 */
	@Override
	public Element toXML(Document doc) {
		Element toggleButtonGroupElt = doc.createElement("ToggleButtonGroup");
		XMLTools.addProperty(doc, toggleButtonGroupElt, "ToggleButtonGroupStrategy", "String", strategy);

        /* Call each button's toXML method */
		for (ToggleButton b : buttons) {
			Element toggleButtonElt = b.toXML(doc);
			toggleButtonGroupElt.appendChild(toggleButtonElt);
		}

		return toggleButtonGroupElt;
	}

}
