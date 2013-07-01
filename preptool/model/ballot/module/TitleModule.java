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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * A class very similar to TextFieldModule, only it is uneditable. This is for use with cards that
 * shouldn't allow their names to change, like PartyCards.
 *
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 6/28/13
 */
public class TitleModule extends AModule {

    /**
     * An inner class for the TextFieldModule's view
     *
     * @author cshaw
     */
    private class ModuleView extends AModuleView {

        private static final long serialVersionUID = 1L;
        private JLabel title;
        private Language primaryLanguage;
        private Language language;
        private JMenuItem copyFromItem;

        /**
         * Constructs this module's view
         *
         * @param view
         *            the main view
         */
        protected ModuleView(View view) {
            setLayout( new GridBagLayout() );
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets( 0, 10, 0, 0 );
            c.anchor = GridBagConstraints.LINE_START;
            JLabel prompt = new JLabel( label  );
            add( prompt, c );
            title = new JLabel();

//            setData(language, label);
//            System.out.println(language);


            JPopupMenu contextMenu = new JPopupMenu();
            copyFromItem = new JMenuItem();
            copyFromItem.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setData( primaryLanguage, getData( primaryLanguage ) );
                    title.setText( getData( primaryLanguage ) );
                }
            } );
            contextMenu.add( copyFromItem );
            title.setComponentPopupMenu( contextMenu );
            c.gridx = 1;
            c.weightx = 1;
            c.insets = new Insets( 0, 0, 0, 0 );
            add( title, c );
        }

        /**
         * Updates the language to the new selected language
         */
        public void languageSelected(Language lang) {
            language = lang;
            title.setText( getData( lang ) );
            validate();
            repaint();
        }

        /**
         * Returns true if the module needs translation in the given language
         */
        public boolean needsTranslation(Language lang) {
            return TitleModule.this.needsTranslation( lang );
        }

        /**
         * Updates the primary language
         */
        public void updatePrimaryLanguage(Language lang) {
            primaryLanguage = lang;
            copyFromItem.setText( "Copy from " + lang.getName() );
        }
    }

    /**
     * Parses an XML Element into a TitleModule
     *
     * @param elt
     *            the element
     * @return the TextFieldModule
     */
    public static TitleModule parseXML(Element elt) {
        assert elt.getTagName().equals( "Module" );
        assert elt.getAttribute( "type" ).equals( "TitleModule" );

        HashMap<String, Object> properties = XMLTools.getProperties(elt);

        String name = elt.getAttribute( "name" );
        String label = (String) properties.get( "label" );

        TitleModule module = new TitleModule( name, label );

        NodeList list = elt.getElementsByTagName( "LocalizedString" );
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item( i );
            if (child.getAttribute( "name" ).equals( "Data" ))
                module.data = LocalizedString.parseXML(child);
        }

        return module;
    }

    private LocalizedString data;

    private String label;

    /**
     * Constructs a new TextFieldModule with the given module name and label
     *
     * @param name
     *            the module name
     * @param label
     *            a label that will be shown next to the text field on the view
     */
    public TitleModule(String name, String label) {
        super( name );
        this.label = label;
        data = new LocalizedString();

//        //TODO This is sort of a hack, should probably find a way to better support languages
//        data.set(Language.getLanguageForName("English"), "");
    }

    /**
     * Generates ane returns this module's view
     */
    public AModuleView generateView(View view) {
        return new ModuleView( view );
    }

    /**
     * Returns this module's data as a String in the given language
     *
     * @param lang
     *            the language
     */
    public String getData(Language lang) {
        return data.get( lang );
    }

    /**
     * Returns true if the module needs translation in the given language
     */
    public boolean needsTranslation(Language lang) {
        return getData( lang ).equals( "" );
    }

    /**
     * Sets the data to the given string
     *
     * @param lang
     *            the language
     * @param s
     *            the string
     */
    public void setData(Language lang, String s) {
        System.out.println("Setting data with string " + s + " in language " + lang);
        data.set( lang, s );
        setChanged();
        notifyObservers();
    }

    /**
     * Formats this TextFieldModule as a savable XML Element
     */
    public Element toSaveXML(Document doc) {
        Element moduleElt = doc.createElement( "Module" );
        moduleElt.setAttribute( "type", "TitleModule" );
        moduleElt.setAttribute( "name", getName() );

        for(Language lang : Language.getAllLanguages()){
            System.out.println(">>>> Data: " + data.get(lang) + " in language " + lang.getName()) ;
        }

        XMLTools.addProperty( doc, moduleElt, "label", "String", label );

        moduleElt.appendChild( data.toSaveXML( doc, "Data" ) );

        return moduleElt;
    }

}
