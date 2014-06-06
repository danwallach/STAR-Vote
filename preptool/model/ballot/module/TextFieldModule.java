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
 * A TextFieldModule is a module that contains some localized text on a card.
 * The view for this module is a one-line text field. This is where candidate and
 * party names, as well as race and proposition titles, will be entered
 * 
 * @author Corey Shaw
 */ /* TODO These classes could use a heavier dose of inheritance */
public class TextFieldModule extends AModule {

    /**
     * An inner class for the TextFieldModule's view
     * 
     * @author Corey Shaw
     */
    private class ModuleView extends AModuleView {

        /** The actual text field where user-input is taken in */
        private JTextField field;


        /**
         * Constructs this module's view
         * 
         * @param view the main view for the mini-MVC
         */
        protected ModuleView(View view) {
            /* Set up the layout */
            setLayout( new GridBagLayout() );
            GridBagConstraints c = new GridBagConstraints();

            /* Add the field's accompanying label */
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets( 0, 10, 0, 0 );
            c.anchor = GridBagConstraints.LINE_START;
            JLabel prompt = new JLabel( label + ":  " );
            add( prompt, c );

            /* Build the field itself */
            field = new JTextField( 25 );

            /* Set the field to update the module whenever a key is typed */
            field.addKeyListener( new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            field.validate();
                            setData( getLanguage(), field.getText() );
                        }
                    } );
                }
            } );

            /* Set up and add the right-click menu */
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

            /* Now layout and add the text field */
            c.gridx = 1;
            c.weightx = 1;
            c.insets = new Insets( 0, 0, 0, 0 );
            add( field, c );
        }

        /**
         * Updates the language to the new selected language
         *
         * @param lang the new language
         */
        public void languageSelected(Language lang) {
            setLanguage(lang);
            field.setText( getData( lang ) );
        }

        /**
         * @param lang the language that the module's data should be translated into
         * @return true if the module needs translation in the given language, false if not
         */
        public boolean needsTranslation(Language lang) {
            return TextFieldModule.this.needsTranslation( lang );
        }

        /**
         * Updates the primary language
         *
         * @param lang the new primary language
         */
        public void updatePrimaryLanguage(Language lang) {
            setPrimaryLanguage(lang);
            getCopyFromItem().setText( "Copy from " + lang.getName() );
        }
    }

    /**
     * Parses an XML Element into a TextFieldModule
     * 
     * @param elt the XML representation of this module
     * @return the TextFieldModule object, built from XML
     */
    public static TextFieldModule parseXML(Element elt) {
        /* ensure that we have the right kind of module */
        assert elt.getAttribute( "type" ).equals( "TextFieldModule" );

        /* Grab the properties out of the XML */
        HashMap<String, Object> properties = XMLTools.getProperties( elt );

        /* Set up the name and label */
        String name = elt.getAttribute( "name" );
        String label = (String) properties.get( "label" );

        /* build the actual object */
        TextFieldModule module = new TextFieldModule( name, label );

        /* populate the object with information read in from the XML */
        NodeList list = elt.getElementsByTagName( "LocalizedString" );
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item( i );
            if (child.getAttribute( "name" ).equals( "Data" ))
                module.data = LocalizedString.parseXML( child );
        }

        return module;
    }

    /** The data contained in the text field */
    private LocalizedString data;

    /** The label shown next to the text field */
    private String label;

    /**
     * Constructs a new TextFieldModule with the given module name and label
     * 
     * @param name the module name
     * @param label a label that will be shown next to the text field on the view
     */
    public TextFieldModule(String name, String label) {
        super( name );
        this.label = label;
        data = new LocalizedString();
    }

    /**
     * @param view the view that this mini-view will be part of
     * @return the newly generated view for this module
     */
    public AModuleView generateView(View view) {
        return new ModuleView( view );
    }

    /**
     * @param lang the language we want the data translated into
     * @return this module's data as a String in the given language
     */
    public String getData(Language lang) {
        return data.get( lang );
    }

    /**
     * @return true if the module needs translation in the given language, false if not
     */
    public boolean needsTranslation(Language lang) {
        return getData( lang ).equals( "" );
    }

    /**
     * Sets the data (i.e. the text field text) to the given string
     * 
     * @param lang the language the new data is translated to
     * @param s the data itself
     */
    public void setData(Language lang, String s) {
        data.set( lang, s );
        setChanged();
        notifyObservers();
    }

    /**
     * Formats this TextFieldModule as a savable XML Element
     *
     * @param doc the document this will be an element of
     * @return an XML representation of this object
     */
    public Element toSaveXML(Document doc) {
        Element moduleElt = doc.createElement( "Module" );
        moduleElt.setAttribute( "type", "TextFieldModule" );
        moduleElt.setAttribute( "name", getName() );

        XMLTools.addProperty( doc, moduleElt, "label", "String", label );

        moduleElt.appendChild( data.toSaveXML( doc, "Data" ) );

        return moduleElt;
    }

}
