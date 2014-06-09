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

package preptool.view;

import preptool.model.language.Language;

import javax.swing.*;

/**
 * A superclass for module views.  It subclasses JPanel and also includes
 * the IMultiLanguageEditor interface.
 * @author Corey Shaw
 */
public abstract class AModuleView extends JPanel implements
		IMultiLanguageEditor {

    /** The primary language for the input, will be used in the right-click "copy from" option */
    private Language primaryLanguage;

    /** The current language that this module contains data for */
    private Language language;

    /** The right-click option to copy translated text from the primary language */
    private JMenuItem copyFromItem;

    /* Getters and Setters */

    public Language getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(Language primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public JMenuItem getCopyFromItem() {
        return copyFromItem;
    }

    public void setCopyFromItem(JMenuItem copyFromItem) {
        this.copyFromItem = copyFromItem;
    }
}
