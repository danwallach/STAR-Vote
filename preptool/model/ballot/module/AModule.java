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
import preptool.controller.exception.BallotOpenException;
import preptool.model.language.Language;
import preptool.view.AModuleView;
import preptool.view.View;

import java.util.Observable;


/**
 * A Module is a component of an ACard that holds some data and (usually) has an editor
 * view associated with it in the preptool. Adding new information to cards is as "simple" as adding the
 * Module corresponding to that type of information.
 *
 * @author Corey Shaw
 */
public abstract class AModule extends Observable {

    /**
     * Parses an XML Element into a module
     *
     * @param elt       the element from an xml file
     * @return the      AModule that the xml element represents
     */
    public static AModule parseXML(Element elt) {
        /* Ensure that the element is in fact a modules*/
        assert elt.getTagName().equals("Module");

        /* Pull out the type of the element and figure out what kind of module type it is */
        String type = elt.getAttribute("type");

        switch (type) {

            case "CandidatesModule":
                return CandidatesModule.parseXML(elt);
            case "CheckBoxModule":
                return CheckBoxModule.parseXML(elt);
            case "TextAreaModule":
                return TextAreaModule.parseXML(elt);
            case "TextFieldModule":
                return TextFieldModule.parseXML(elt);
            case "PropositionModule":
                return PropositionModule.parseXML(elt);

            default:
                throw new BallotOpenException("Invalid module: " + type);
        }
    }

    /**
     * The unique identifier of this module that the
     * ACard (or user of the card) can use to access this module.
     */
    private String name;

    /**
     * Creates a new Module with the given unique name
     *
     * @param       name the name of the module
     */
    public AModule(String name) {
        this.name = name;
    }
    
    /**
     * Abstract method for generating the view of this module
     *
     * @param       view the main view
     * @return      an AModuleView for this module
     */
    public abstract AModuleView generateView(View view);

    /**
     * @return      this module's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if this module has an editor view, defaults to true
     *
     * @return      true
     */
    public boolean hasView() {
        return true;
    }

    /**
     * Checks whether the information in this module is missing any translations
     *
     * @param lang      the language to check
     * @return          true if missing translation information
     */
    public abstract boolean needsTranslation(Language lang);

    /**
     * Formats this module as a savable XML Element
     *
     * @param doc       the xml document that provides context for the XML write of the module
     * @return          the Element, an xml representation of a module
     */
    public abstract Element toSaveXML(Document doc);

}
