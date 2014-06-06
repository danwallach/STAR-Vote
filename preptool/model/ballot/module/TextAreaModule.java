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
import org.w3c.dom.NodeList;
import preptool.model.XMLTools;
import preptool.model.language.Language;
import preptool.model.language.LocalizedString;
import preptool.view.AModuleView;
import preptool.view.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;


/**
 * A TextAreaModule is a module that stores localized text on a card. The view
 * for this module is a large text area for the user to enter a large amount of
 * text. This is used for propositions on ballots.
 * 
 * @author Corey Shaw
 */
public class TextAreaModule extends AModule {

    /**
     * An inner class for the TextAreaModule's view
     * 
     * @author Corey Shaw
     */
    private class ModuleView extends AModuleView {

        /** The actual field where text will be entered into the module by the user */
        private JTextArea field;

        /**
         * Constructs a new ModuleView with the given main view
         * 
         * @param view The main view, necessary for completing the mini-MVC
         */
        @SuppressWarnings("UnusedParameters")
        protected ModuleView(View view) {
            /* Set up the layout */
            setLayout( new GridBagLayout() );
            GridBagConstraints c = new GridBagConstraints();

            /* Initialize the text area */
            field = new JTextArea( getData( getLanguage() ) );
            field.setBorder( BorderFactory.createTitledBorder( label ) );

            /* Set up the module to update everytime a key is pressed inside the text field */
            field.addKeyListener( new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            setData( getLanguage(), field.getText() );
                        }
                    } );
                }
            } );

            /* Set up the right-click menu */
            JPopupMenu contextMenu = new JPopupMenu();
            setCopyFromItem(new JMenuItem());
            getCopyFromItem().addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setData( getLanguage(), getData( getPrimaryLanguage() ) );
                    field.setText( getData( getLanguage() ) );
                }
            } );
            contextMenu.add( getCopyFromItem() );
            field.setComponentPopupMenu( contextMenu );

            /* Format and add the text field */
            c.gridx = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.insets = new Insets( 0, 0, 0, 0 );
            c.fill = GridBagConstraints.BOTH;
            add( field, c );
        }

        /**
         * Updates the language to the new selected language
         *
         * @param lang the new selected language
         */
        public void languageSelected(Language lang) {
            setLanguage(lang);
            field.setText( getData( lang ) );
        }

        /**
         * Returns true if the module needs to be translated in the given
         * language
         *
         * @param lang the language in question
         * @return Whether the module has a translation for all of its data in language lang
         */
        public boolean needsTranslation(Language lang) {
            return TextAreaModule.this.needsTranslation( lang );
        }

        /**
         * Updates the primary language
         *
         * @param lang the new primary language
         */
        public void updatePrimaryLanguage(Language lang) {
            setLanguage(lang);
            getCopyFromItem().setText( "Copy from " + lang.getName() );
        }
    }

    /**
     * Parses an XML Element into a TextAreaModule
     * 
     * @param elt the element that a module is being parsed from
     * @return the newly constructed TextAreaModule from XML
     */
    public static TextAreaModule parseXML(Element elt) {
        /* Ensure that this is in fact a TextAreaModule */
        assert elt.getAttribute( "type" ).equals( "TextAreaModule" );

        /* Get the properties from the XML */
        HashMap<String, Object> properties = XMLTools.getProperties( elt );

        /* Set up the names and labels*/
        String name = elt.getAttribute( "name" );
        String label = (String) properties.get( "label" );

        /* Build the actual object */
        TextAreaModule module = new TextAreaModule( name, label );

        /* Fill in the requisite data in the module */
        NodeList list = elt.getElementsByTagName( "LocalizedString" );
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item( i );
            if (child.getAttribute( "name" ).equals( "Data" ))
                module.data = LocalizedString.parseXML( child );
        }

        return module;
    }

    /** The data contained in this module */
    private LocalizedString data;

    /** The label for this module */
    private String label;

    /**
     * Constructs a new TextAreaModule with given module name and label, and initialize the data
     * 
     * @param name the module name
     * @param label the label to be shown next to the text area on the view
     */
    public TextAreaModule(String name, String label) {
        super( name );
        this.label = label;
        data = new LocalizedString();
    }

    /**
     * @param view the view that this mini-view will be part of
     * @return The generated view for this module
     */
    @Override
    public AModuleView generateView(View view) {
        return new ModuleView( view );
    }

    /**
     * @param lang the language
     * @return the data as a String in the given language
     */
    public String getData(Language lang) {
        return data.get( lang );
    }

    /**
     * @param lang the language to check
     * @return true if the module needs to be translated in the given language, false if not
     */
    @Override
    public boolean needsTranslation(Language lang) {
        return getData( lang ).equals( "" );
    }

    /**
     * Sets the data to the given string and updates the UI
     * 
     * @param lang the language that the data is in
     * @param s the string the actual data itself
     */
    public void setData(Language lang, String s) {
        data.set( lang, s );
        setChanged();
        notifyObservers();
    }

    /**
     * Formats this TextAreaModule as a savable XML Element
     *
     * @param doc the context for this XML element
     * @return the newly constructed XML element
     */
    @Override
    public Element toSaveXML(Document doc) {
        Element moduleElt = doc.createElement( "Module" );
        moduleElt.setAttribute( "type", "TextAreaModule" );
        moduleElt.setAttribute( "name", getName() );

        XMLTools.addProperty( doc, moduleElt, "label", "String", label );

        moduleElt.appendChild( data.toSaveXML( doc, "Data" ) );

        return moduleElt;
    }

}
