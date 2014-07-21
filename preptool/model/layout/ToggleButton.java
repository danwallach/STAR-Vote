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
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.language.Language;

/**
 * A ToggleButton is similar to a button, but it holds state in that it can
 * either be selected or deselected. It is usually contained within a
 * ToggleButtonGroup, which specifies a strategy for selection of these buttons.
 * @author Corey Shaw
 */
public class ToggleButton extends ALayoutComponent {

    /** A mapping of language name to CardElement name used to identify names of write-in candidates */
    public static final HashMap<String, String> writeInNames = new HashMap<>();

    /* Fill in our map */
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

	/** The text of this ToggleButton */
	private String text;

	/** The second line of this ToggleButton (used in Presidential Races) */
	private String secondLine = "";
	
	/** The party text of this ToggleButton (used for candidates) */
	private String party = "";

	/** Whether this ToggleButton has bold text */
	private boolean bold;

	/** Whether this ToggleButton has increased font size */
	private boolean increasedFontSize;


	/**
	 * Constructs a new ToggleButton with given unique ID and text
     *
	 * @param uid       the unique ID
	 * @param text      the text
	 */
	public ToggleButton(String uid, String text) {
		super(uid);
		this.text = text;
	}

	/**
	 * Constructs a new ToggleButton with given unique ID, text, and size
	 * visitor, which determines and sets the size.
     *
	 * @param uid               the uniqueID
	 * @param t                 the text
	 * @param sizeVisitor       the size visitor
	 */
	public ToggleButton(String uid, String t, ILayoutComponentVisitor<Object,Dimension> sizeVisitor) {
		this(uid, t);
		setSize(execute(sizeVisitor));
	}

	/**
	 * Calls the forToggleButton method in visitor
	 *
     * @see preptool.model.layout.ALayoutComponent#execute(ILayoutComponentVisitor, Object[])
	 */
	@Override
	public <P,R> R execute(ILayoutComponentVisitor<P,R> visitor, P... param) {
      	return visitor.forToggleButton(this, param);
	}

	/**
	 * @return two lines separated by a newline if necessary
	 */
	public String getBothLines() {
		if (secondLine.equals(""))
			return text;
		else
			return text + " \n " + secondLine;
	}

	/**
	 * @return the party
	 */
	public String getParty() {
		return party;
	}

	/**
	 * @return the secondLine
	 */
	public String getSecondLine() {
		return secondLine;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return if the toggle button is bold
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * @return if the toggle button has increasedFontSize
	 */
	public boolean isIncreasedFontSize() {
		return increasedFontSize;
	}

	/**
	 * @param bold the bold to set
	 */
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	/**
	 * @param increasedFontSize the increasedFontSize to set
	 */
	public void setIncreasedFontSize(boolean increasedFontSize) {
		this.increasedFontSize = increasedFontSize;
	}

	/**
	 * @param party the party to set
	 */
	public void setParty(String party) {
		this.party = party;
	}

	/**
	 * @param secondLine the secondLine to set
	 */
	public void setSecondLine(String secondLine) {
		this.secondLine = secondLine;
	}

	/**
	 * @return the String representation of this ToggleButton
	 */
	@Override
    public String toString() { return "ToggleButton[text=" + text + ",x=" + xPos + ",y=" + yPos + ",width=" + width + ",height=" + height + "]"; }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    public ToggleButton clone(){ return new ToggleButton(getUID(), text); }

	/**
	 * Converts this ToggleButton object to XML
     *
	 * @param doc       the document this component is a part of
     * @return          the XML element representation for this ToggleButton
	 */
	@Override
	public Element toXML(Document doc) {

        /* Assume there isn't a write-in */
        Boolean isWriteIn = false;

        /* Check if the ToggleButton is a write-in candidate, regardless of language. */
        for (Language language : Language.getAllLanguages()) {

            Boolean isValidLanguage = false;
            /* Make sure that this language is a valid language for which write-in candidates are enabled. */
            for (String languageName : writeInNames.keySet()) {
                if (language.getName().equals(languageName)) {
                    isValidLanguage = true;
                    break;
                }
            }

            /* If the language is valid, check the name on this ToggleButton for equality with the default write-in name. */
            if (isValidLanguage && getText().equals(writeInNames.get(language.getName()))) {
                isWriteIn = true;
                break;
            }
        }

        /* If this is a write-in, specify it in the XML */
        if (isWriteIn) {
            Element writeInToggleButtonElt = doc.createElement("WriteInToggleButton");
            addCommonAttributes(doc, writeInToggleButtonElt);
            return writeInToggleButtonElt;
        }

        Element toggleButtonElt = doc.createElement("ToggleButton");
		addCommonAttributes(doc, toggleButtonElt);
		return toggleButtonElt;
	}

}
