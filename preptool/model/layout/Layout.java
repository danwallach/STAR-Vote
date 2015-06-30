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
 * The Layout class encapsulates all of the information that sizes and positions
 * items in the ballot. It contains a list of pages, that each contain a number
 * of components that VoteBox can display.
 *
 * @author Corey Shaw
 */
public class Layout {

	/** An array of pages contained within this layout */
	private ArrayList<Page> pages;
    
    /** The page number of the override-cancel page */
    private int overrideCancelPage;
    
    /** The page number of the override-cast page */
    private int overrideCommitPage;

    /** The page number of the provisional success page */
    private int provisionalPage;

    /**
	 * Constructs a blank layout with an empty list of Pages
	 */
	public Layout() {
		pages = new ArrayList<>();
	}

    /**
     * @return the overrideCancelPage
     */
    public int getOverrideCancelPage() {
        return overrideCancelPage;
    }

    /**
     * @return the overrideCommitPage
     */
    public int getOverrideCommitPage() {
        return overrideCommitPage;
    }

    /**
	 * @return the list of pages
	 */
	public ArrayList<Page> getPages() {
		return pages;
	}

    /**
     * @param overrideCancelPage the overrideCancelPage to set
     */
    public void setOverrideCancelPage(int overrideCancelPage) {
        this.overrideCancelPage = overrideCancelPage;
    }

	/**
     * @param overrideCommitPage the overrideCommitPage to set
     */
    public void setOverrideCommitPage(int overrideCommitPage) {
        this.overrideCommitPage = overrideCommitPage;
    }
    
    /**
     * @param provisionalPage the provisionalPage number
     */
    public void setProvisionalPage(int provisionalPage){
        this.provisionalPage = provisionalPage;

    }

	/**
	 * Converts this Layout object to XML
     *
	 * @param doc       the document this component is a part of
     * @return          the XML element representation for this Layout
	 */
	public Element toXML(Document doc) {
		Element layoutElt = doc.createElement("Layout");
        XMLTools.addProperty(doc, layoutElt, "OverrideCancelPage", "Integer", overrideCancelPage);
        XMLTools.addProperty(doc, layoutElt, "OverrideCommitPage", "Integer", overrideCommitPage);
        XMLTools.addProperty(doc, layoutElt, "ProvisionalSuccessPage", "Integer", provisionalPage);
        
		for (Page p : pages) {
			Element pageElt = p.toXML(doc);
			layoutElt.appendChild(pageElt);
		}
		return layoutElt;
	}
}
