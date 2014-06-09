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

package votebox.middle.view;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import votebox.AuditoriumParams;
import votebox.middle.*;
import votebox.middle.view.widget.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;


/**
 * This class encapsulates the layout XML parser. The layout xml stores view
 * information -- it defines a set of pages, each which defines a set of widgets
 * (label, button, togglebutton, etc.) which have been arranged on the page for
 * some purpose (display a candidate's name, display a "next page" button,
 * etc.). This class's job is translate this information into a Layout object.
 * This class might be better thought of as a Layout deserializer, but we have
 * avoided this term so as not to mislead the reader into thinking that we are
 * using java object serialization.<br>
 * <br>
 * To deserialize a Layout object, this class first uses java's xerces wrapper
 * to parse a given layout xml file into a w3c dom tree. This dom tree is first
 * validated with our schema and then interpreted recursively from the top down.<br>
 * <br>
 * It is important to note that for any given ballot package there are multiple
 * layout files. Each layout file is representative of one (size, language)
 * tuple. The layout parser will need to know which layout file needs to be
 * parsed -- when you ask for a layout, you need to tell the parser which size
 * and language you desire.
 * 
 * @author derrley
 * 
 */
public class LayoutParser {

    private HashMap<String, LinkedList<IDrawable>> _drawables;

    private AuditoriumParams constants;

    /**
     * A constructor so we can get election parameters in to this class
     *
     * @param constants     election parameters
     */
    public LayoutParser(AuditoriumParams constants){
        this.constants = constants;
    }

    /**
     * A constructor so we don't have to rely on the constants if we don't want to
     *
     * Note that this will, in the long run, become useless, but since Texas doesn't like
     * shuffled candidate order it's fine for now.
     */
    public LayoutParser(){
        /*
          Since this is always run with Votebox, we can sort of cheat and assume there will be a
          vb.conf file. Of course if there isn't, people will be confused about why they see two
          "Could not parse..." messages
        */

        constants = new AuditoriumParams("vb.conf");
    }

    /**
     * This is the core method for the layout parser. Call this method to
     * translate a layout xml file to a dom tree (as an intermediate
     * representation), then, primarily, to a ballot object.
     * 
     * @param vars      where to look for xml files and media
     * @param size      the size index of the layout xml file to read
     * @param lang      the language of the layout xml file to read
     * @return          the Layout object which represents the parsed and translated layout xml file.
     *
     * @throws LayoutParserException if the xml file or schema could not be read, if the schema did not validate,
     *                               or upon further testing of our own, the layout content is not valid.
     */
    public Layout getLayout(IBallotVars vars, int size, String lang, IView view) throws LayoutParserException {

        _drawables = new HashMap<String, LinkedList<IDrawable>>();

        Document document;

        try { document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); }
        catch (ParserConfigurationException e) {
            throw new LayoutParserException("Internal Error. Could not get a new XML 'Document'.", e);
        }

        /* Parse layout xml -> dom tree. */
        try {
            TransformerFactory.newInstance().newTransformer().transform(
                new StreamSource(new File(vars.getLayoutFile() + "_" + size + "_" + lang + ".xml")), new DOMResult(document));
        }
        catch (TransformerConfigurationException e) {
            throw new LayoutParserException("Internal Error. Could not get a new 'transformer'.", e);
        }
        catch (TransformerException e) {
            throw new LayoutParserException("The XML you have given for size " + size + ", language " + lang +
                                            " is unparseable. The XML is probably not formed correctly.", e);
        }

        /* Validate the dom tree against our schema */
        try {
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(vars.getLayoutSchema())
                    .newValidator().validate(new DOMSource(document));
        }
        catch (SAXException e) { throw new LayoutParserException("Could not validate the XML against the schema.", e); }
        catch (IOException e)  { throw new LayoutParserException("Internal Error. The schema against which the XML is validated could not be loaded.", e); }

        System.out.println("In getLayout: " + document + " | " + document.getElementsByTagName("Layout") + " | " + document.getElementsByTagName("Layout").item(0));
        /* Translate dom tree -> Layout object. */
        return parseLayout(document.getElementsByTagName("Layout").item(0), view);
    }

    /**
     * This method interprets a given dom node as being of the layout type. This
     * method will convert the given dom node to a Layout object
     * 
     * @param node      the dom node that is interpreted as being of type layout *
     * @return          the newly constructed Layout object.
     */
    private Layout parseLayout(Node node, IView view) throws LayoutParserException {

        System.out.println("In parseLayout: " + node);
        NodeList children           = node.getChildNodes();
        ArrayList<RenderPage> pages = new ArrayList<RenderPage>();
        Properties properties       = new Properties();

        /* Children can either be properties or pages */
        for (int lcv = 0; lcv < children.getLength(); lcv++) {

            Node child = children.item(lcv);
            String childName = child.getNodeName();

            switch(childName) {

                case "Property":        parseProperties(child, properties);
                                        break;

                case "ListProperty":    parseListProperties(child, properties);
                                        break;

                case "Page":            pages.add(parsePage(child, view));
                                        break;

                case "#text":           break;

                default:                throw new LayoutParserException("I don't recognize " + child.getNodeName() +
                                                                        " as being a Property or Page.", null );

            }

        }

        return new Layout(properties, pages, _drawables);
    }

    /**
     * This method is a helper to parseLayout. This method converts a dom node
     * into a RenderPage object.
     * 
     * @param node      the page node which is interpreted as a page.
     * @return          the RenderPage object that represents the given dom page.
     */
    private RenderPage parsePage(Node node, IView view) throws LayoutParserException {

        System.out.println("In parsePage: " + node);

        NodeList children = node.getChildNodes();
        ArrayList<IDrawable> drawables = new ArrayList<IDrawable>();
        Properties properties = new Properties();

        for (int lcv = 0; lcv < children.getLength(); lcv++) {

            Node child = children.item(lcv);

            String childName = child.getNodeName();

            switch(childName) {

                case "ToggleButtonGroup":   parseToggleGroup(drawables, child, view);
                                            break;

                case "Button":
                case "FocusableLabel":
                case "Label":               drawables.add(parseDrawable(child, null, view));
                                            break;

                case "#text":               break;

                case "Property":            parseProperties(child, properties);
                                            break;

                case "ListProperty":        parseListProperties(child, properties);
                                            break;

                default:                    throw new LayoutParserException("I don't recognize " + child.getNodeName() +
                                            " as being a ToggleButtonGroup, Button, or Label", null);

            }

        }

        RenderPage rp = new RenderPage(drawables, properties);

        rp.setNavigation(_drawables);
        rp.setBackgroundImage(_drawables);

        return rp;
    }

    /**
     * This method is a helper to parsePage. This method converts a dom node into a ToggleButtonGroup.
     * Membership in a group does not place toggle buttons as members of the layout. Groups only serve
     * the purpose of implementing select/deselect strategies.
     * 
     * @param list      the list of togglebuttons to which all children toggle buttons will be added
     * @param node      the node that is interpreted as the toggle button group.
     */
    private void parseToggleGroup(ArrayList<IDrawable> list, Node node, IView view) throws LayoutParserException {

        NodeList children       = node.getChildNodes();
        Properties properties   = new Properties();
        ToggleButtonGroup group = new ToggleButtonGroup( properties );

        ArrayList<ToggleButton> buttons = new ArrayList<ToggleButton>();
        ArrayList<Integer> verticals    = new ArrayList<Integer>();

        for (int lcv = 0; lcv < children.getLength(); lcv++) {

            Node child = children.item(lcv);
            String childName = child.getNodeName();

            switch(childName) {

                case "ToggleButton":   ToggleButton button = (ToggleButton) parseDrawable(child, group, view);
                                            verticals.add(button.getY());
                                            buttons.add(button);
                                            break;

                case "#text":               break;

                case "Property":            parseProperties(child, properties);
                                            break;

                case "ListProperty":        parseListProperties(child, properties);
                                            break;

                case "WriteInToggleButton": WriteInToggleButton wButton = (WriteInToggleButton) parseDrawable(child, group, view);
                                            verticals.add(wButton.getY());
                                            buttons.add(wButton);

                default:                    throw new LayoutParserException("I don't recognize " + child.getNodeName() +
                                                                            " as a property or toggle button.", null );

            }

        }

        /* Now mix the buttons so that they are displayed in random order if this election is configured that way
           (it most likely won't be in Texas) */
        if(constants.shuffleCandidates()) Collections.shuffle(verticals);


        int i = 0;

        for(ToggleButton b : buttons){

            b.setY(verticals.get(i));
            group.getButtons().add(b);
            list.add(b);

            i++;
        }
    }

    /**
     * This method is a helper to parseToggleGroup and to parsePage. This method
     * converts a dom node into an IDrawable.
     * 
     * @param node      the node which will be interpreted as an IDrawable.
     * @param group     the group that this node needs to belong to. Pass null
     *                  here if this drawable does not belong to a group (or if a
     *                  group makes no sense -- IE label, etc.)
     * @return          the newly constructed drawable which represents the given dom node.
     *
     * @throws LayoutParserException if its helpers throw or if one of its children is not a property.
     */
    private IDrawable parseDrawable(Node node, ToggleButtonGroup group, IView view) throws LayoutParserException {

        NamedNodeMap nodeAttributes = node.getAttributes();
        NodeList children = node.getChildNodes();

        /* Extract the drawable's unique id. */
        String uniqueID = nodeAttributes.getNamedItem("uid").getNodeValue();

        Properties properties = new Properties();

        /* Parse all the properties. */
        for (int lcv = 0; lcv < children.getLength(); lcv++) {

            Node child = children.item(lcv);
            String childName = child.getNodeName();

            switch(childName) {

                case "Property":        parseProperties(child, properties);
                                        break;

                case "ListProperty":    parseListProperties(child, properties);
                                        break;

                case "#text":           break;

                default:                throw new LayoutParserException("I don't recognize " + child.getNodeName() +
                                                                        " as a property.", null);

            }
        }

        /* Create the new drawable */
        IDrawable drawable;
        String nodeName = node.getNodeName();

        switch(nodeName) {

            case "Label":

                try{

                    boolean allNull = properties.getString("Down") == null && properties.getString("Up")        == null &&
                                      properties.getString("Left") == null && properties.getString("Right")     == null &&
                                      properties.getString("Next") == null && properties.getString("Previous")  == null;

                    /* If this node does not have any direction properties, it shouldn't be focusable */
                    /* TODO This isn't used, in fact, Label isn't used. Dispose of it? */
                    drawable = allNull ? new Label(uniqueID, properties) : new FocusableLabel(uniqueID, properties);

                }
                catch(IncorrectTypeException e){ throw new RuntimeException(e); }

                break;

            case "Button":

                drawable = new Button(uniqueID, properties);
                break;

            case "WriteInToggleButton":

                drawable = new WriteInToggleButton(group, uniqueID,  properties, view);
                break;

            default:

                drawable = new ToggleButton(group, uniqueID, properties);

        }

        /* Extract the drawable's position */
        drawable.setX(Integer.parseInt(nodeAttributes.getNamedItem("x").getNodeValue()));
        drawable.setY(Integer.parseInt(nodeAttributes.getNamedItem("y").getNodeValue()));

        /* Add this drawable to the hash table. */
        if (_drawables.containsKey(uniqueID))
            _drawables.get(uniqueID).add(0, drawable);
        else {
            LinkedList<IDrawable> l = new LinkedList<IDrawable>();
            l.add(drawable);
            _drawables.put(uniqueID, l);
        }

        return drawable;
    }

    /**
     * This is a helper method to parseLayout and parseElement. It is called
     * when a dom node is encountered that represents a property. This method
     * will add an entry representative of the given dom node to a given
     * Properties object
     * 
     * @param node          the dom node which represents a property
     * @param properties    the Properties object to which the given property should be added.
     *
     * @throws LayoutParserException if the property has been defined with an incorrect type.
     */
    private void parseProperties(Node node, Properties properties) throws LayoutParserException {

        NamedNodeMap nodeAttributes = node.getAttributes();

        String key    = nodeAttributes.getNamedItem("name") .getNodeValue();
        String value  = nodeAttributes.getNamedItem("value").getNodeValue();
        String type   = nodeAttributes.getNamedItem("type") .getNodeValue();

        try { properties.add(key, value, type); }
        catch (UnknownTypeException | UnknownFormatException e) {
            throw new LayoutParserException("There was an error while parsing the property " + key + " with type " +
                                            type + " and value " + value + e.getMessage(), e);
        }
    }

    /**
     * Given an XML node whose type is "ListProperty", add all its children to
     * the given Properties instance.
     * 
     * @param node          the list property node.
     * @param properties    the children of the given node to this properties instance.
     *
     * @throws LayoutParserException if the XML is not formatted correctly
     */
    private void parseListProperties(Node node, Properties properties) throws LayoutParserException {

        NamedNodeMap nodeAttributes = node.getAttributes();

        String key  = nodeAttributes.getNamedItem("name").getNodeValue();
        String type = nodeAttributes.getNamedItem("type").getNodeValue();

        NodeList children = node.getChildNodes();
        ArrayList<String> elts = new ArrayList<String>();

        for (int lcv = 0; lcv < children.getLength(); lcv++) {
            Node child = children.item(lcv);
            elts.add(child.getAttributes().getNamedItem("value").getNodeValue());
        }

        try { properties.add(key, elts, type); }
        catch (UnknownTypeException | UnknownFormatException e) {
            throw new LayoutParserException("While parsing the property " + key + " of type " + type + " and value " +
                                            elts + ", the parser encountered an error: " + e.getMessage(), e);
        }
    }
}
