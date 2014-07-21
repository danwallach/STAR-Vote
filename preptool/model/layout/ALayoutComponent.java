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

import java.awt.Dimension;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import preptool.model.XMLTools;

/**
 * ALayoutComponent is the abstract notion of anything that belongs on a Page,
 * that is anything that can be laid out and displayed on the screen, including
 * labels, buttons, and toggle buttons. An ALayoutComponent records its size and
 * position, and the relative position of other ALayoutComponents on the page.
 *
 * @author Corey Shaw
 */
public abstract class ALayoutComponent implements Cloneable {

	/**
	 * The width of this component
	 */
	protected int width;

	/**
	 * The height of this component
	 */
	protected int height;

	/**
	 * The x-coordinate of this component's position
	 */
	protected int xPos;

	/**
	 * The y-coordinate of this component's position
	 */
	protected int yPos;

	/**
	 * The unique ID of this component, assigned by the LayoutManager
	 */
	protected String uniqueID;

	/**
	 * The component (if any) above this component
	 */
	protected ALayoutComponent up;

	/**
	 * The component (if any) below this component
	 */
	protected ALayoutComponent down;

	/**
	 * The component (if any) to the left of this component
	 */
	protected ALayoutComponent left;

	/**
	 * The component (if any) to the right of this component
	 */
	protected ALayoutComponent right;

	/**
	 * The component (if any) that is next in sequence of this component
	 */
	protected ALayoutComponent next;

	/**
	 * The component (if any) that is previous in sequence of this component
	 */
	protected ALayoutComponent previous;
	
	/**
	 * Creates a new ALayoutComponent with the given unique ID
     *
	 * @param uniqueID      the unique ID
	 */
	public ALayoutComponent(String uniqueID) {
		super();
		this.uniqueID = uniqueID;
	}

	/**
	 * Clones this component, keeping the same parameters (including UID)<br>
	 * Used so that two of the same component can be on the same page, but in
	 * different locations
     *
     * @return      a clone of the Spacer
	 */
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public ALayoutComponent clone() {
		try { return (ALayoutComponent)super.clone(); }
        catch (CloneNotSupportedException e) { e.printStackTrace(); }
		return null;
	}
	
	/**
	 * Executes an ILayoutComponentVisitor on this component.
     *
	 * @param visitor       the visitor
	 * @param param         the parameters
	 * @return              the result of the visitor
	 */
	public abstract <P,R> R execute(ILayoutComponentVisitor<P,R> visitor, P... param);

	/**
	 * @return the component linked to this one in the "down" direction
	 */
	public ALayoutComponent getDown() {
		return down;
	}

	/**
	 * @return the height of this component
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the component linked to this one in the "left" direction
	 */
	public ALayoutComponent getLeft() {
		return left;
	}

	/**
	 * @return the "next" component linked to this one
	 */
	public ALayoutComponent getNext() {
		return next;
	}

	/**
	 * @return the "previous component linked to this one
	 */
	public ALayoutComponent getPrevious() {
		return previous;
	}

	/**
	 * @return the component linked to this one in the "right" direction
	 */
	public ALayoutComponent getRight() {
		return right;
	}

	/**
	 * @return the unique ID
	 */
	public String getUID() {
		return uniqueID;
	}

	/**
	 * @return the component linked to this one in the "up" direction
	 */
	public ALayoutComponent getUp() {
		return up;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the x position
	 */
	public int getXPos() {
		return xPos;
	}

	/**
	 * @return the y position
	 */
	public int getYPos() {
		return yPos;
	}

	/**
	 * @param down the component linked to this one in the "down" direction to set
	 */
	public void setDown(ALayoutComponent down) {
		this.down = down;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @param left the component linked to this one in the "down" direction to set
	 */
	public void setLeft(ALayoutComponent left) {
		this.left = left;
	}

	/**
	 * @param next the "next" component linked to this one in
	 */
	public void setNext(ALayoutComponent next) {
		this.next = next;
	}

	/**
	 * @param previous the "previous" component linked to this one in
	 */
	public void setPrevious(ALayoutComponent previous) {
		this.previous = previous;
	}

	/**
	 * @param right the component linked to this one in the "right" direction to set
	 */
	public void setRight(ALayoutComponent right) {
		this.right = right;
	}

	/**
	 * @param dim the dimension to set
	 */
	public void setSize(Dimension dim) {
		width = dim.width;
		height = dim.height;
	}

	/**
	 * @param up the component linked to this one in the "up" direction to set
	 */
	public void setUp(ALayoutComponent up) {
		this.up = up;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @param pos the x-position to set
	 */
	public void setXPos(int pos) {
		xPos = pos;
	}

	/**
	 * @param pos the y-position to set
	 */
	public void setYPos(int pos) {
		yPos = pos;
	}

	/**
	 * @return a String representation of this component
	 */
	@Override
	public String toString() {
		return super.toString() + "[x = " + xPos + ", y = " + yPos + ", width = "
		+ width + ", height = " + height + "]";
	}
	
	/**
	 * Converts this ALayoutComponent object to XML
     *
	 * @param doc       the document this component is part of
	 * @return          the XML representation of this ALayoutComponent
	 */
	public abstract Element toXML(Document doc);

	/**
	 * Helper method for generating XML: Adds the unique ID, x, and y
	 * attributes, and adds properties for up, down, left, right, next, and
	 * previous.
     *
	 * @param doc           the document this component is part of
	 * @param compElt       the XML representation of this ALayoutComponent
	 */
	protected void addCommonAttributes(Document doc, Element compElt) {

		compElt.setAttribute("uid", uniqueID);
		compElt.setAttribute("x", Integer.toString(xPos));
		compElt.setAttribute("y", Integer.toString(yPos));
		if (up != null)         XMLTools.addProperty(doc, compElt, "Up",       "String", up.getUID());
		if (down != null)       XMLTools.addProperty(doc, compElt, "Down",     "String", down.getUID());
		if (left != null)       XMLTools.addProperty(doc, compElt, "Left",     "String", left.getUID());
		if (right != null)      XMLTools.addProperty(doc, compElt, "Right",    "String", right.getUID());
		if (next != null)       XMLTools.addProperty(doc, compElt, "Next",     "String", next.getUID());
		if (previous != null)   XMLTools.addProperty(doc, compElt, "Previous", "String", previous.getUID());

	}

}
