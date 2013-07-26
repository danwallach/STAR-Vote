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

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * The Language class encapsulates an icon (flag), name, and short name
 * (abbreviation) of a language.
 * @author cshaw
 */
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
	 * @param name the name
	 * @param shortName the short name
	 * @param icon the icon
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
	 * @return the array of languages
	 */
	public static ArrayList<Language> getAllLanguages() {
		if (allLanguages != null) return allLanguages;

		allLanguages = new ArrayList<Language>();
		ImageIcon icon;

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/en.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("English", "en", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/es.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("Español", "es", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/fr.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("Français", "fr", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/de.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("Deutsch", "de", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/it.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("Italiano", "it", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/ru.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("Русский", "ru", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/zh.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("中文", "zh", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/jp.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("日本語", "jp", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/kr.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("한국말", "kr", icon));

		try {
			icon = new ImageIcon(ClassLoader.getSystemClassLoader()
					.getResource("images/sa.png"));
		} catch (Exception e) {
			icon = null;
		}
		allLanguages.add(new Language("العربية", "sa", icon));

		return allLanguages;
	}

	/**
	 * A static map from language name to language
	 */
	private static HashMap<String, Language> allLanguagesMap;

	/**
	 * Retrieves the Language object by name
	 * @param name the language name
	 * @return the Language
	 */
	public static Language getLanguageForName(String name) {
		if (allLanguagesMap == null) {
			allLanguagesMap = new HashMap<String, Language>();
			ArrayList<Language> languages = getAllLanguages();
			for (Language lang : languages)
				allLanguagesMap.put(lang.getName(), lang);
		}
		return allLanguagesMap.get(name);
	}

}
