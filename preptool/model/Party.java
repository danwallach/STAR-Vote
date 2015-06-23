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

package preptool.model;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import preptool.model.language.Language;
import preptool.model.language.LocalizedString;

import javax.swing.*;

/**
 * Encapsulates a party's localized name and abbreviation.
 * @author Corey Shaw
 */
public class Party extends JLabel {

	/**
	 * A blank party, signifies a candidate without a party
	 */
	public static Party NO_PARTY = new Party();

    /** A special party that will allow parties to be added to the list of parties */
    private final static Party EDIT_PARTY = new Party("Edit...", "Edit...");

	/**
	 * The name of the party
	 */
	private LocalizedString name;

	/**
	 * The abbreviation
	 */
	private LocalizedString abbrev;

	/**
	 * Constructs a new Party.
	 */
	public Party() {

        /* Initialise the fields */
		clear();
	}

    public Party(String name, String abbrev) {

        /* Initialise the fields */
        this();

        /* Set the name and abbreviations */
        setName(Language.getLanguageForName("English"), name);
        setAbbrev(Language.getLanguageForName("English"), abbrev);
    }

	/**
	 * Clears the party so it is identical to NO_PARTY - used when deleting a
	 * party
	 */
	public void clear() {
		name = new LocalizedString();
		abbrev = new LocalizedString();
	}

	/**
	 * Checks the name and abbreviation localized strings
	 */
	@Override
	public boolean equals(Object obj) {

        /* Return false if not a Party */
		if (!(obj instanceof Party)) return false;

        /* Now we know it is, so go ahead and cast it */
        Party rhs = (Party) obj;

        /* Return the evaluation of the names and abbreviations are the same */
		return name.equals(rhs.name) && abbrev.equals(rhs.abbrev);
	}

	/**
	 * @return      the abbrev
	 */
	public String getAbbrev(Language lang) {
		return abbrev.get(lang);
	}

	/**
	 * @return      the name
	 */
	public String getName(Language lang) {
		return name.get(lang);
	}

    /**
     * @return      the edit party constant
     */
    public static Party getEditParty() {
        return EDIT_PARTY;
    }

    /**
	 * @param lang      the language of this abbreviation
	 * @param abbrev    the abbreviation to set
	 */
	public void setAbbrev(Language lang, String abbrev) {
		this.abbrev.set(lang, abbrev);
	}

	/**
	 * @param abbrev    the abbreviation to set
	 */
	public void setAbbrev(LocalizedString abbrev) {
		this.abbrev = abbrev;
	}

	/**
	 * @param lang      the language this name is in
	 * @param name      the name to set
	 */
	public void setName(Language lang, String name) {
		this.name.set(lang, name);
	}

	/**
	 * @param name      the name to set
	 */
	public void setName(LocalizedString name) {
		this.name = name;
	}

	/**
	 * Converts this party to a savable XML representation, to be opened later
     *
	 * @param doc       the document
	 * @return          the element for this Party
	 */
	public Element toSaveXML(Document doc) {

        /* Create a party element for the document */
		Element elt = doc.createElement("Party");

        /* Add the name and abbreviation to the party element */
		elt.appendChild(name.toSaveXML(doc, "Name"));
		elt.appendChild(abbrev.toSaveXML(doc, "Abbrev"));

        /* Return the party element */
		return elt;
	}

	/**
	 * Parses XML into a Party object
     *
	 * @param elt       the element
	 * @return          the Party
	 */
	public static Party parseXML(Element elt) {

        /* Make sure the element being loaded is a party */
		assert elt.getTagName().equals("Party");

		Party party = new Party();

        /* Gets a list of all the parties (by searching for LocalizedStrings) */
		NodeList list = elt.getElementsByTagName("LocalizedString");


        /* Go through the list */
		for (int i = 0; i < list.getLength(); i++) {

            /* Pull the item out */
			Element child = (Element) list.item(i);

            /* Get its name */
            String name = child.getAttribute("name");

            /* Set up booleans */
            Boolean isName = name.equals("Name");
            Boolean isAbbrev = name.equals("Abbrev");

            /* See if it is "Name" or "Abbrev" */
            if(isName || isAbbrev) {

                /* If it is, parse the XML */
                LocalizedString parsedName = LocalizedString.parseXML(child);

                /* Set name or abbreviation accordingly*/
                if (isName)   party.setName(parsedName);
                if (isAbbrev) party.setAbbrev(parsedName);
            }
		}

        /* Return the party */
		return party;
	}
}
