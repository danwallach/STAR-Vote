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

package preptool.model.language;

import preptool.view.View;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Language class encapsulates an icon (flag), name, and short name
 * (abbreviation) of a language.
 *
 * @author Corey Shaw
 */ /* TODO This class is really dumb. Fix it. */
public class Language {

	/**
	 * The icon representing this language
	 */
	private ImageIcon icon;

	/**
	 * The name of this language
	 */
	private String name;

	/**
	 * The short name or abbreviation of this language
	 */
	private String shortName;

    /**
	 * Constructs a new language with given name, short name, and icon
     *
	 * @param name the name of the language
	 * @param shortName the short name (e.g. for English, the short name would be "en")
	 * @param icon the icon of the flag associated with that language
	 */
	public Language(String name, String shortName, ImageIcon icon) {
		this.icon = icon;
		this.name = name;
		this.shortName = shortName;
	}

	/**
	 * @return the icon
	 */
	public ImageIcon getIcon() {
		return icon;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Static array of all languages
	 */
	private static ArrayList<Language> allLanguages;

	/**
	 * Returns an array of all languages available to this program
     *
	 * @return the array of languages
	 */
	public static ArrayList<Language> getAllLanguages() {
        /* If the array has already been constructed, just return it. */
		if (allLanguages != null) return allLanguages;

		allLanguages = new ArrayList<>();

		allLanguages.add(new Language("English", "en", View.loadImage("en.png")));
		allLanguages.add(new Language("Español", "es", View.loadImage("es.png")));
		allLanguages.add(new Language("Français", "fr", View.loadImage("fr.png")));
		allLanguages.add(new Language("Deutsch", "de", View.loadImage("de.png")));
		allLanguages.add(new Language("Italiano", "it", View.loadImage("it.png")));
		allLanguages.add(new Language("Русский", "ru", View.loadImage("ru.png")));
		allLanguages.add(new Language("中文", "zh", View.loadImage("zh.png")));
		allLanguages.add(new Language("日本語", "jp", View.loadImage("jp.png")));
		allLanguages.add(new Language("한국말", "kr", View.loadImage("kr.png")));
		allLanguages.add(new Language("العربية", "sa", View.loadImage("sa.png")));

		return allLanguages;
	}

	/**
	 * A static map from language name to language
	 */
	private static HashMap<String, Language> allLanguagesMap;

	/**
	 * Retrieves the Language object by name
     *
	 * @param name the string representation of the language name
	 * @return the Language object
	 */
	public static Language getLanguageForName(String name) {

        /* If the map is null, initialize it and add all of the languages we support to it */
		if (allLanguagesMap == null) {
			allLanguagesMap = new HashMap<>();
			ArrayList<Language> languages = getAllLanguages();
			for (Language lang : languages)
				allLanguagesMap.put(lang.getName(), lang);
		}

		return allLanguagesMap.get(name);
	}


}
