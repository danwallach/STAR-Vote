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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import votebox.middle.IBallotVars;
import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.ballot.IBallotLookupAdapter;
import votebox.middle.driver.IAdapter;


/**
 * A RenderPage consists of a list of drawables. Each drawble contains all the
 * information which the ViewManager needs to draw this drawable to the view,
 * including their location.
 */

public class RenderPage {

    /**
     * This class is used internally for error handling. If a set method cant
     * find an element, it will throw this exception.
     * 
     * @author derrley
     * 
     */
    public class ElementNotFoundException extends Exception {
        private static final long serialVersionUID = 1L;

        private String _uid;

        public ElementNotFoundException(String uid) {
            super("When attempting to resolve 'direction links' in a page, the uid "+ uid + " was not recognized to be valid." );
            _uid = uid;
        }

        public String getUID() {
            return _uid;
        }
    }

    /**
     * The elements which make up the page are held here.
     */
    private List<IDrawable> _children = null;

    /**
     * These are the properties which have been defined on this page.
     */
    private Properties _properties = null;

    /**
     * This is the reference to the parent page.
     */
    private Layout _parent = null;

    /**
     * This is the reference to the drawable
     */
    private IDrawable _background;

    /**
     * This is RenderPage's constructor
     * 
     * @param children      the children to be assigned to the layout for this RenderPage.
     * @param properties    the properties to be associated with this page.
     */
    public RenderPage(List<IDrawable> children, Properties properties) {
        _children = children;
        _properties = properties;
        setChildren();
    }

    /**
     * Accessor for this RenderPage's properties.
     * 
     * @return      properties for this RenderPage
     */
    public Properties getProperties(){
    	return _properties;
    }
    
    /**
     * Set the parent pointer on all the children.
     */
    private void setChildren() {
        for (IDrawable d : _children)
            d.setParent( this );
    }

    /**
     * Set the parent reference.
     * 
     * @param parent    the layout that will get set as the parent.
     */
    public void setParent(Layout parent) {
        _parent = parent;
    }

    /**
     * Get a reference to the parent layout.
     * 
     * @return      a reference to the parent layout.
     */
    public Layout getParent() {
        return _parent;
    }

    /**
     * Call this method to set the navigational links for all the elements that
     * belong to this page. Elements, when parsed from the layout file, will
     * simply have properties set to denote the UID of the element who is "up,
     * down, left, right, next, and previous." This method effectively converts
     * those UID properties into actual pointers. After this method is called,
     * the elements will be able to function properly as focusable elements on
     * the screen.
     * 
     * 
     * @param uidmap    mapping of uid->element used to "connect the dots." When an
     *                  element references another element, it will use this map to
     *                  determine what the actual object reference is for that element.
     *
     * @throws LayoutParserException if any element references a UID that is not in uidmap.
     */
    public void setNavigation(HashMap<String, LinkedList<IDrawable>> uidmap)
            throws LayoutParserException {

        try {

            /* Go through each card and "connect the dots." */
            for (IDrawable drawable : _children) {

                if (drawable instanceof IFocusable) {

                    IFocusable rce  = (IFocusable) drawable;
                    Properties rceP = rce.getProperties();

                    /* TODO can we get rid of some of this by adding a function into rce? */
                    if (rceP.contains(Properties.UP))
                        rce.setUp(getFromDictionary(rceP.getString(Properties.UP), uidmap));

                    if (rceP.contains(Properties.DOWN))
                        rce.setDown(getFromDictionary(rceP.getString(Properties.DOWN), uidmap));

                    if (rceP.contains(Properties.LEFT))
                        rce.setLeft(getFromDictionary(rceP.getString(Properties.LEFT), uidmap));

                    if (rceP.contains(Properties.RIGHT))
                        rce.setRight(getFromDictionary(rceP.getString(Properties.RIGHT), uidmap));

                    if (rceP.contains(Properties.NEXT))
                        rce.setNext(getFromDictionary(rceP.getString(Properties.NEXT), uidmap));

                    if (rceP.contains(Properties.PREVIOUS))
                        rce.setPrevious(getFromDictionary(rceP.getString(Properties.PREVIOUS), uidmap));
                }
            }
        }
        catch (ElementNotFoundException e) {
            throw new LayoutParserException("While setting the navigation links, " + e.getUID() + " was found to be an invalid ID", e);
        }
        catch (IncorrectTypeException e) {
            throw new LayoutParserException("While setting the navigation links, " + e.getActual() + " was the found" +
                                            " type of a direction property. Please ensure that all direction properties" +
                                            " have type 'String'", e);
        }
    }

    /**
     * Invoking this method will mandate that if there is a property declared on
     * this page named by Properties.BACKGROUND_LABEL, the value of that
     * property will be the first element in this page's layout. This is useful
     * for declaring a background image, because the first item in the list will
     * be the first item that is drawn to the display, meaning that all items
     * drawn after the first will be layered on top of the first item.
     * 
     * @param uidmap    a mapping from uid->drawable
     *
     * @throws LayoutParserException if the property was declared incorrectly.
     */
    public void setBackgroundImage(HashMap<String, LinkedList<IDrawable>> uidmap) throws LayoutParserException {

        try {

            if (_properties.contains( Properties.BACKGROUND_LABEL )) {

                String uid = _properties.getString(Properties.BACKGROUND_LABEL);

                if (uidmap.containsKey(uid))
                    _background = uidmap.get(uid).get(0);
                else
                    throw new LayoutParserException("BackgroundLabel property set to uid " + uid + " which does not exist.", null);
            }

        }
        catch (IncorrectTypeException e) {
            throw new LayoutParserException("We found that the 'BackgroundLabel' property was set to type " +
                                            e.getActual() + ". This should be a string.", e);
        }
    }

    /**
     * This is a private helper method for setNavigation. It's purpose is to do
     * a lookup in the uidmap provided and return the RenderCardElement that is
     * mapped to a given uid.
     * 
     * @param uid       the UID to be looked up by this method
     * @param uidmap    the mapping of UID->element where the method will look for the UID
     * @return          the RenderCardElement which is mapped to the UID.
     *
     * @throws ElementNotFoundException if there is no mapping in uidmap for UID.
     */
    private IFocusable getFromDictionary(String uid, HashMap<String, LinkedList<IDrawable>> uidmap) throws ElementNotFoundException {

        if (uidmap.containsKey(uid))
            if(uidmap.get(uid).get(0) instanceof IFocusable)
                return (IFocusable) uidmap.get( uid ).get( 0 );

        throw new ElementNotFoundException( uid );
    }

    /**
     * 
     * Call this method to draw this RenderPage to a view.
     * 
     * @param view  the view to which the RenderPage will be drawn
     */
    public void draw(IView view) {

        view.setBackground(_background);
        for (IDrawable dt : _children)
            view.draw(dt);
    }

    /**
     * Call this method to get the elements that make up this page.
     * 
     * @return      _children (a list of all the elements that belong to this page)
     */
    public List<IDrawable> getChildren() {
        return _children;
    }

    /**
     * The ViewManager will call this method when it wishes to initialize the
     * state of this drawable. This should include things like strategy/state
     * setting/syncing with the ballot. Here, we simply call initFromViewManager
     * on each of the child elements.
     * 
     * @param vmadapter         an adapter to be used by the child elements as
     *                          a reference to the view manager
     *
     * @param lookupadapter     an adapter to be used by the child elements to
     *                          make queries on the state of the ballot
     *
     * @param ballotadapter     an adapter to be used by the child elements to
     *                          make changes to the state of the ballot
     *
     * @param factory           a factory used to make new images
     *
     * @param ballotvars        a container used to find the path to image files
     */
    public void initFromViewManager(IViewManager vmadapter, IBallotLookupAdapter lookupadapter, IAdapter ballotadapter,
            IViewFactory factory, IBallotVars ballotvars) {

        for (IDrawable d : _children)
            d.initFromViewManager(vmadapter, lookupadapter, ballotadapter, factory, ballotvars);
    }
    
    /**
     * @return a list of all the unique ids of elements on this page
     */
    public List<String> getUniqueIDs(){
    	List<String> ret = new ArrayList<>();
    	
    	for(IDrawable child : _children)
    		ret.add(child.getUniqueID());

    	return ret;
    }
}
