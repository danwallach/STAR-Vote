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

package preptool.model.ballot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import preptool.controller.exception.BallotOpenException;
import preptool.model.Party;
import preptool.model.XMLTools;
import preptool.model.ballot.module.AModule;
import preptool.model.ballot.module.CandidatesModule;
import preptool.model.ballot.module.TextFieldModule;
import preptool.model.language.Language;
import preptool.model.layout.manager.ALayoutManager;
import preptool.model.layout.manager.ALayoutManager.ICardLayout;

import java.util.ArrayList;
import java.util.Observer;


/**
 * An ACard is the abstract representation of a choice that the user must make,
 * that lies in the ballot. For instance, a race for Mayor or a proposition to
 * cut taxes would be represented using an ACard. An ACard contains a list of
 * AModules, which contain information specific to the type of card, i.e. a
 * title or a list of candidates
 * @author Corey Shaw
 */
public abstract class ACard {

    /**
     * Parses an XML element into an ACard
     *
     * @param elt       the XML Element to be parsed
     * @return          the ACard object parsed from XML
     */
    public static ACard parseXML(Element elt) {
        assert elt.getTagName().equals("Card");
        ACard card;

        /* Figure out what kind of card this is */
        String type = elt.getAttribute("type");
        switch (type) {
            case "Race":
                card = new RaceCard();
                break;
            case "Presidential Race":
                card = new PresidentialRaceCard();
                break;
            case "Proposition":
                card = new PropositionCard();
                break;
            case "Party":
                card = new PartyCard();
                break;
            default:
                throw new BallotOpenException("Unknown card type: "
                        + elt.getAttribute("type"));
        }

        card.modules.clear();

        /* Populate the modules on the card with the pertinent information from the XML */
        NodeList list = elt.getElementsByTagName("Module");
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item(i);
            card.modules.add(AModule.parseXML(child));
        }

        return card;
    }

    /** This card's unique identifier*/
    protected String uniqueID;

    /** The string for labeling this card */
    protected String titleLabelID = "";

    /** This card's type, e.g. race, proposition, party, etc. */
    protected String type;

    /** The modules contained on this card */
    protected ArrayList<AModule> modules;

    /**
     * Constructs a blank ACard with an empty list of modules.
     *
     * @param type      the type of card to construct
     */
    public ACard(String type) {
        this.type = type;
        this.modules = new ArrayList<>();
    }

    /**
     * Adds an observer to the module with the given name, if it exists
     *
     * @param moduleName        the name of the module to add the observer to
     * @param obs               the observer to add to the module
     */
    public void addModuleObserver(String moduleName, Observer obs) {
        AModule module = getModuleByName(moduleName);
        if (module != null) module.addObserver(obs);
    }

    /**
     * Assigns the UIDs to this card and its elements
     *
     *  @param manager      the layout manager which assigns the UIDs in the context of other cards
     */
    public abstract void assignUIDsToBallot(ALayoutManager manager);

    /**
     * Looks for a module with the given name in this cards list of modules, and
     * returns it if it exists, null otherwise
     *
     * @param name      the name of the module to lookup
     * @return          the module, if it exists, or null
     */
    public AModule getModuleByName(String name) {

        for (AModule m : modules)
            if (m.getName().equals(name)) return m;

        return null;
    }

    /**
     * Returns the list of modules that contain information and behavior for this card
     *
     * @return      the list of modules
     */
    public ArrayList<AModule> getModules() {
        return modules;
    }

    /**
     * Returns the text to show on the review screen if the user has not made a
     * selection on this card
     *
     * @param language      the language of the string whose translation we want
     */
    public abstract String getReviewBlankText(Language language);

    /**
     * @param language      the language of the String we want
     * @return              the review title for this card
     */
    public abstract String getReviewTitle(Language language);

    /**
     * Returns this card's title, by checking to see if there is a title module
     * and returning its data. If there is no title, returns the empty string
     *
     * @param lang      the language to get the title in
     * @return          the title, if any
     */
    public String getTitle(Language lang) {
        AModule module = getModuleByName("Title");
        if (module != null)
            return ((TextFieldModule) module).getData(lang);
        else
            return "";
    }

    /**
     * @return      the type name (as a String) of this ballot, e.g. "Race", "Party", etc.
     */
    public String getType() {
        return type;
    }

    /**
     * @return      the unique ID of this card, set by the LayoutManager when exporting.
     */
    public String getUID() {
        return uniqueID;
    }

    /**
     * Lays out this card in the given ICardLayout
     *
     * @param manager           the layout manager
     * @param cardLayout        the card layout object
     * @return                  the finished card layout object
     */
    public abstract ICardLayout layoutCard(ALayoutManager manager, ICardLayout cardLayout);

    /**
     * Returns whether this card needs translation information for the given language
     *
     * @param lang      the language to check
     * @return          true if translations are needed, false if not
     */
    public boolean needsTranslation(Language lang) {
        boolean res = false;

        /* We must look at all modules to see if anything needs translation */
        for (AModule m : modules)
            res |= m.needsTranslation(lang);
        return res;
    }

    /**
     * @param uid       the unique ID of this card to set
     */
    public void setUID(String uid) {
        uniqueID = uid;
    }


    /**
     * @param titleID       the title of this card to set
     */
    public void setTitleID(String titleID){
    	titleLabelID = titleID;
    }

    /**
     * @param language      the language in which to get the data
     * @return              the pertinent internal data for this module (i.e. the candidates, proposition selection, etc)
     */
    @SuppressWarnings("unused")
    public abstract ArrayList<String> getCardData(Language language);


    /**
     * Formats this ACard as a savable XML element
     *
     * @param doc       the document that this element belongs to
     * @return          this ACard as an XML Element
     */
    public Element toSaveXML(Document doc) {
        Element cardElt = doc.createElement("Card");
        cardElt.setAttribute("type", type);

        /* Call each module's toXML method */
        for (AModule m : modules)
            cardElt.appendChild(m.toSaveXML(doc));

        return cardElt;
    }

    /**
     * Formats this ACard as a VoteBox XML element. Note that these
     * XML files are different than the ballot.bal files the preptool uses.
     *
     * @param doc       the document that this card is a part of
     * @return          this ACard as a VoteBox XML Element
     */
    public Element toXML(Document doc){
    	Element cardElt = doc.createElement("Card");
        cardElt.setAttribute("uid", uniqueID);
        XMLTools.addProperty(doc, cardElt, "CardStrategy", "String",
                "RadioButton");

        XMLTools.addProperty(doc, cardElt, "TitleLabelUID", "String",
                titleLabelID);

        return cardElt;
    }

    /**
     * Updates references in candidates' parties so they are all the
     * same as the parties in the ballot.
     *
     * @param parties       the most up-to-date list of parties
     */
    public void fixParties(ArrayList<Party> parties) {
        AModule m = getModuleByName("Candidates");
        if (m != null) {
            CandidatesModule candidates = (CandidatesModule) m;
            ArrayList<CardElement> elements = candidates.getData();
            for (CardElement elt : elements) {
                int idx = parties.indexOf(elt.getParty());
                if (idx != -1) elt.setParty(parties.get(idx));
            }
        }
    }
}
