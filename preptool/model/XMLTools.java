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

package preptool.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Static methods for reading, writing, and formatting XML documents.
 * @author Corey Shaw
 */
public class XMLTools {

	/**
	 * Adds a property element
     *
	 * @param doc       the document
	 * @param elt       the element to add the property to
	 * @param name      name of the property
	 * @param type      type of the property
	 * @param value     value of the property
	 */
	public static void addProperty(Document doc, Element elt, String name, String type, Object value) {

        /* Create an element for properties */
		Element propElt = doc.createElement("Property");

        /* Set name, type, and value attributes */
		propElt.setAttribute("name", name);
		propElt.setAttribute("type", type);
		propElt.setAttribute("value", value.toString());

        /* Add the property to the element */
		elt.appendChild(propElt);
	}
    
    /**
     * Adds a list property element
     *
     * @param doc       the document
     * @param elt       the element to add the property to
     * @param name      name of the property
     * @param type      type of the property
     * @param values    values of the property
     */
    public static void addListProperty(Document doc, Element elt, String name, String type, Object[] values) {

        /* Create an element for ListProperty */
        Element propElt = doc.createElement("ListProperty");

        /* Set name and type attributes */
        propElt.setAttribute("name", name);
        propElt.setAttribute("type", type);

        /* Go through each of the values and add it to the document as its ListElement */
        for (Object val: values) {
            Element listElt = doc.createElement("ListElement");
            listElt.setAttribute("value", val.toString());
            propElt.appendChild(listElt);
        }

        /* Add the property to the element */
        elt.appendChild(propElt);
    }

	/**
	 * Constructs a new document
     *
	 * @return      the new document
	 */
	public static Document createDocument() throws ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	}

	/**
	 * Writes an XML tree to disk
     *
	 * @param rootElt       the root element
	 * @param file          path of the file to write to
	 */
	public static void writeXML(Element rootElt, String file) throws IllegalArgumentException, TransformerFactoryConfigurationError, TransformerException {

        /* Normalise the root element */
		rootElt.normalize();

        /* Create a new transformer */
		Transformer xformer = TransformerFactory.newInstance().newTransformer();

        /* Set indents as the output property */
		xformer.setOutputProperty("indent", "yes");

        /* Make a new DOMSource from the root element and StreamResult linked to a file for output */
		DOMSource d = new DOMSource(rootElt);
		StreamResult r = new StreamResult(new File(file));

        /* Output the transformed root into the file */
		xformer.transform(d, r);
	}

	/**
	 * Returns a HashMap of all of the properties of an element
     *
	 * @param elt       the element
	 * @return          a HashMap of the properties
	 */
	public static HashMap<String, Object> getProperties(Element elt) {

		HashMap<String, Object> properties = new HashMap<>();

        /* Pull the property elements from the element */
		NodeList list = elt.getElementsByTagName("Property");

        /* Go through the list */
        for (int i = 0; i < list.getLength(); i++) {

            /* Cast each item as a Element */
			Element prop = (Element) list.item(i);

            /* Predefine attribute Strings */
            String type  = prop.getAttribute("type");
            String value = prop.getAttribute("value");
            String name  = prop.getAttribute("name");

            /* Figure out what type it is and put its value and name into the HashMap */
            switch (type) {

                case "String" : properties.put(name, value);
                                break;

                case "Integer": properties.put(name, Integer.parseInt(value));
                                break;

                case "Boolean": properties.put(name, Boolean.parseBoolean(value));
                                break;
            }
		}

        /* Return the HashMap */
		return properties;
	}

	/**
	 * Reads an XML tree from disk into a document
     *
	 * @param file      path of the file
	 * @return          the document
	 */
	public static Document readXML(String file) throws ParserConfigurationException, SAXException, IOException {

        /* Create a new DocumentBuilder */
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        /* Create the file */
        File toParse = new File(file);

        /* Return the Document returned by parsing the file */
		return builder.parse(toParse);
	}

}
