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

import preptool.model.Party;
import preptool.model.XMLTools;
import preptool.model.language.Language;
import preptool.model.language.LocalizedString;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A CardElement is an option that the user has to choose from, that lies in the
 * ballot. For instance, candidates in a race would be represented using
 * CardElements.
 *
 * @author Corey Shaw, Mircea C. Berechet
 */
public class CardElement {

    /** A mapping of language name to CardElement name, used to identify names of write-in candidates. */
    public static final HashMap<String, String> writeInNames = new HashMap<String, String>();

    /* Populate the map of write-in translations */
    static
    {
        writeInNames.put("English", "Write-In Candidate");
        writeInNames.put("Español", "Escribe el nombre de su selección");
        writeInNames.put("Français", "Écrivez le nom de votre sélection");
        writeInNames.put("Deutsch", "Schreiben Sie die Namen Ihrer Auswahl");
        writeInNames.put("Italiano", "Scrivi il nome della tua selezione");
        writeInNames.put("Русский", "Напишите имя вашего выбора");
        writeInNames.put("中文", "撰写您的选择的名称");
        writeInNames.put("日本語", "あなたの選択の名前を書く");
        writeInNames.put("한국말", "선택의 이름을 작성");
        writeInNames.put("العربية", "كتابة اسم من اختيارك");
    }

	/**
	 * Parses XML into a CardElement object
     *
	 * @param elt the XML element to parse
	 * @param names number of names in the new CardElement
	 * @return the CardElement, parsed from XML
	 */
	public static CardElement parseXML(Element elt, int names) {
        /* Sanity check */
		assert elt.getTagName().equals("CardElement");

        /* Build the new object */
		CardElement ce = new CardElement(names);

        /* Populate the CE with data from the XML */
		NodeList list = elt.getElementsByTagName("LocalizedString");
		for (int i = 0; i < list.getLength(); i++) {
			Element child = (Element) list.item(i);
			if (child.getAttribute("name").equals("Name" + i))
				ce.names[i] = LocalizedString.parseXML(child);
		}

        /* Make sure to parse the party information as well */
		list = elt.getElementsByTagName("Party");
		if (list.getLength() > 0)
			ce.party = Party.parseXML((Element) list.item(0));

		return ce;
	}

    /** A unique identifier for this card element */
	protected String uniqueID;

    /** The number of names contained in this CE*/
	protected int numNames;

    /** All of the names and their translations contained in this CE */
	protected LocalizedString[] names;

    /** The party for this CE */
	protected Party party;

	/**
	 * Creates a new CardElement, with the given number of names
     *
	 * @param num the number of names
	 */
	public CardElement(int num) {
		numNames = num;
		names = new LocalizedString[numNames];
		for (int i = 0; i < numNames; i++) {
			names[i] = new LocalizedString();
		}

        /* Initially we don't have a party */
		party = Party.NO_PARTY;
	}

	/**
	 * Creates a new CardElement with a single name, and sets the given
	 * LocalizedString to that name
     *
	 * @param str the LocalizedString for the name
	 */
	public CardElement(LocalizedString str) {
        /* We now have exactly one string */
		numNames = 1;

        /* Initialize the names array */
		names = new LocalizedString[1];
		names[0] = str;

        /* Initially we don't have a party */
		party = Party.NO_PARTY;
	}

	/**
	 * Copies all names to the given language from the primary language
     *
	 * @param lang the language to copy to
	 * @param primary the primary language
	 */
	public void copyFromPrimary(Language lang, Language primary) {
		for (int i = 0; i < numNames; i++)
			names[i].set(lang, names[i].get(primary));
	}

	/**
	 * Gets the data for the given language and column number. If the column
	 * number is within numNames, that name is returned as a String. If it is
	 * the next column, the party is returned
     *
	 * @param lang the language in which to get the data
	 * @param idx the column index of the desired data
	 * @return the data for this column
	 */
	public Object getColumn(Language lang, int idx) {
		if (idx == numNames)
			return getParty();
		else
			return getName(lang, idx);
	}

	/**
	 * Returns the name for the given index
     *
	 * @param lang the language the name is in
	 * @param idx the index of the name
	 * @return the name
	 */
	public String getName(Language lang, int idx) {
		return names[idx].get(lang);
	}

	/**
	 * @return the number of names here contained
	 */
	public int getNumNames() {
		return numNames;
	}

	/**
	 * @return the party
	 */
	public Party getParty() {
		return party;
	}

	/**
	 * @return the unique ID of this Card Element, set by the LayoutManager when
	 *         exporting.
	 */
	public String getUID() {
		return uniqueID;
	}

	/**
	 * Returns whether or not this card element is missing translation
	 * information for the given language
     *
	 * @param lang the language in question
	 * @return the result
	 */ /* TODO I think this should iterate through the names and look at their languages? */
	public boolean needsTranslation(Language lang) {
		return false;
	}

	/**
	 * Sets the column to the given data.  See {@link #getColumn(Language, int)} for explanation
	 * on column numbers.  If the party column is specified, but the data is
	 * "Edit...", then the user has selected to edit the parties, and this
	 * call is ignored.
     *
	 * @param lang the language of the data to set
	 * @param idx the column number to set data in
	 * @param val the data to set
	 */
	public void setColumn(Language lang, int idx, Object val) {

        if (idx == numNames) {
             /* If this is the edit parties dialog option, just ignore it */
			if (!val.equals("Edit..."))
                party = (Party) val;
        }
		else
			names[idx].set(lang, (String) val);
	}

    /**
     * @param p the party to set
     */
    public void setParty(Party p) {
        party = p;
    }
    
	/**
	 * @param uid the unique ID to set
	 */
	public void setUID(String uid) {
		uniqueID = uid;
	}

	/**
	 * Converts this element to a savable XML representation, to be opened later by the preptool
     *
     * @param doc the document that this CE is a part of
     * @return this CE as a preptool XML Element
	 */
	public Element toSaveXML(Document doc) {
		Element cardElementElt = doc.createElement("CardElement");
		for (int i = 0; i < numNames; i++)
			cardElementElt.appendChild(names[i].toSaveXML(doc, "Name" + i));
		cardElementElt.appendChild(party.toSaveXML(doc));
		return cardElementElt;
	}

	/**
	 * Converts this card element to an XML representation for use by VoteBox
     *
     * @param doc the document that this CE is a part of
     * @return this CE as a VoteBox XML Element
	 */
	public Element toXML(Document doc) {
        Element cardElementElt;
        if (numNames > 0)
        {
            boolean isWriteInCandidate = false;
            /* Get the list of all the languages. */
            ArrayList<Language> languages = Language.getAllLanguages();

            for (Language language : languages)
            {
                /* If the name of this Element is a write-in candidate's generic name, then mark it as being a write-in candidate. */
                if (getName(language, 0).equals(writeInNames.get(language.getName())))
                {
                    isWriteInCandidate = true;
                    break;
                }
            }

            if (isWriteInCandidate)
            {
                /* This is a write-in candidate: create a special Element, so that the VoteBox would treat it differently from a SelectableCardElement. */
                cardElementElt = doc.createElement("WriteInCardElement");
                /* Add in the unique ID. */
                cardElementElt.setAttribute("uid", uniqueID);
                /* Add a property that specifies the type of race that this write-in is part of ("Regular" Race or "Presidential" Race). */
                XMLTools.addProperty(doc, cardElementElt, "WriteInType", "String", (numNames == 1) ? "Regular" : "Presidential");
                return cardElementElt;
            }
        }

        /* The default operation on a CardElement used to be only the code below. It will be kept as a default for anything that is not a write-in candidate. */
        cardElementElt = doc.createElement("SelectableCardElement");
        cardElementElt.setAttribute("uid", uniqueID);
        if(!party.equals(Party.NO_PARTY))
            XMLTools.addProperty(doc, cardElementElt, "Party", "String", getParty().getName(Language.getLanguageForName("English")));
        return cardElementElt;
	}
}
