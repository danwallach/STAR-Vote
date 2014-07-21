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

package preptool.model.ballot.module;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.ballot.CardElement;
import preptool.model.language.Language;
import preptool.model.language.LiteralStrings;
import preptool.view.AModuleView;
import preptool.view.View;

import java.util.ArrayList;


/**
 * A PropositionModule is a module that contains CardElements corresponding to
 * Yes and No options on a card. This module does not have a view.
 *
 * @author Corey Shaw
 */
public class PropositionModule extends AModule {

    /**
     * Parses an XML Element into a PropositionModule
     * 
     * @param elt   the Element
     * @return      the PropositionModule
     */
    public static PropositionModule parseXML(Element elt) {
        /* Ensure that the module is the correct type */
        assert elt.getAttribute("type").equals("PropositionModule");

        /* Get the data */
        String name = elt.getAttribute("name");

        /* Build the object */

        return new PropositionModule(name);
    }

    /** The data contained herein, i.e. the card elements containing yes, no, and none of the above*/
    private ArrayList<CardElement> data;

    /**
     * Constructs a new PropositionModule with the given module name
     * 
     * @param name      the module name
     */
    public PropositionModule(String name) {
        super(name);

        /* Build the data elements */
        data = new ArrayList<CardElement>();

        data.add(new CardElement(LiteralStrings.Singleton.get("YES")));
        data.add(new CardElement(LiteralStrings.Singleton.get("NO")));
        data.add(new CardElement(LiteralStrings.Singleton.get("NONE_OF_ABOVE")));
    }

    /**
     * This module has no view.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public AModuleView generateView(View view) {
        throw new UnsupportedOperationException("PropositionModule has no view");
    }

    /**
     * @return      the data as an array of CardElements
     */
    public ArrayList<CardElement> getData() {
        return data;
    }

    /**
     * This module does not have a view.
     *
     * @return      false
     */
    public boolean hasView() {
        return false;
    }

    /**
     * This module has translation information into every language supported.
     *
     * @return      false
     */
    @Override
    public boolean needsTranslation(Language lang) {
        return false;
    }

    /**
     * Formats this PropositionModule as a savable XML Element
     *
     * @param doc       the document this is an element of
     * @return          an XML representation of this module
     */
    @Override
    public Element toSaveXML(Document doc) {
        Element moduleElt = doc.createElement("Module");
        moduleElt.setAttribute("type", "PropositionModule");
        moduleElt.setAttribute("name", getName());

        return moduleElt;
    }

}
