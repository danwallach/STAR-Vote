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

import preptool.model.ballot.module.AModule;
import preptool.model.language.Language;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


/**
 * A CardView is a panel that is shown in the card editor pane. It contains a
 * list of module views, displaying them in rows on the panel.
 * 
 * @author Corey Shaw
 */
public class CardView extends JPanel implements IMultiLanguageEditor {

    private static final long serialVersionUID = 1L;
    private ArrayList<AModuleView> views;

    /**
     * Constructs a new CardView
     * 
     * @param view          the main view
     * @param type          the type name of the card
     * @param modules       list of the modules on the card
     */
    public CardView(View view, String type, ArrayList<AModule> modules) {

        /* Set up grid bag stuff */
        views = new ArrayList<>();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Set up and add the label for the card */
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Lucida Sans", Font.BOLD, 16));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10, 10, 0, 0);
        add(typeLabel, c);

        c.insets = new Insets(10, 0, 0, 0);

        /* For each module... */
        for (AModule module : modules) {

            /* Check if the module has a view */
            if (module.hasView()) {

                /* Generate and add the view */
                AModuleView v = module.generateView(view);
                views.add(v);
            }
        }

        /* Go through each of the views */
        for (int i = 0; i < views.size(); i++) {

            /* c.gridy starts from 1 */
            c.gridy = i + 1;

            /* If we get to the last one, Set c.weighty */
            if (i == views.size() - 1) c.weighty = 1;

            /* Add the views given the layout constraints */
            add(views.get(i), c);
        }
    }

    /**
     * Forwards the call onto all modules in this view that the language was
     * updated
     */
    public void languageSelected(Language lang) {
        for (AModuleView mod : views)
            mod.languageSelected(lang);
    }

    /**
     * Checks all modules on this view and reports if any need translation
     * information in the given language
     * 
     * @param lang      the language
     */
    public boolean needsTranslation(Language lang) {

        boolean res = false;

        /* For each module in views, look and see if it needs translation */
        for (AModuleView mod : views)
            res |= mod.needsTranslation(lang);

        return res;
    }

    /**
     * Forwards the call onto all modules in this view that the primary language
     * was updated
     */
    public void updatePrimaryLanguage(Language lang) {

        /* For each module in views, update the primary language */
        for (AModuleView mod : views)
            mod.updatePrimaryLanguage(lang);
    }

}
