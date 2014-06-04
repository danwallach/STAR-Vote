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

package votebox.middle.view.widget;

import javazoom.jl.player.Player;
import votebox.middle.Event;
import votebox.middle.IBallotVars;
import votebox.middle.Properties;
import votebox.middle.ballot.IBallotLookupAdapter;
import votebox.middle.driver.IAdapter;
import votebox.middle.view.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * This is the simplest implementation of an IDrawable. Labels are simply window
 * decoration. Labels cannot be interacted with by the voter.
 */

public class FocusableLabel implements IFocusable {

    private final String _uniqueID;
    private final Properties _properties;

    private RenderPage _parent;
    private int _x;
    private int _y;

    private IViewImage _reviewImage, _focusedReviewImage;

    private NavigationLinks _links = new NavigationLinks();

    /**
     * This event is raised when the element is focused.
     */
    private Event _focusedEvent = new Event();

    /**
     * This event is raised when the element loses Focus.
     */
    private Event _unfocusedEvent = new Event();

     /**
     * This is the image that the button should display when it is in the
     * focused state.
     */
    private IViewImage _focusedImage = null;

    /**
     * This is the image that the button should display when it is in the
     * default (non-focused) state.
     */
    private IViewImage _defaultImage = null;


    ALabelState _state = DefaultLabelState.Singleton;

    protected IBallotLookupAdapter _ballot;
    protected IViewManager _viewManager;
    protected IViewFactory _factory;
    protected IBallotVars _vars;


    /**
     * A player that plays the corresponding sound when this button is selected
     */
    public static Player mp3Player;

    private Thread soundThread;

    /**
     * This is the public constructor for FocusableLabel.
     * 
     * @param uid           the drawable's UID.
     * @param properties    the properties that were defined for this label.
     */
    public FocusableLabel(String uid, Properties properties) {
        _uniqueID = uid;
        _properties = properties;
    }

    /**
     * Call this method to set the parent of this label.
     */
    public void setParent(RenderPage page) {
        _parent = page;
    }

    /**
     * Call this method to get the parent of this label.
     * 
     * @return      the parent of this label.
     */
    protected RenderPage getParent() {
        return _parent;
    }

    /**
     * @see votebox.middle.view.IDrawable#getImage()
     */
    public IViewImage getImage() {
       return _state.getImage(this);
    }

    /**
     * @see votebox.middle.view.IDrawable#getImage()
     */
    public IViewImage getReviewImage() {

        _reviewImage = _factory.makeImage(imagePath(_vars, _uniqueID + "_review", _viewManager.getSize(),
                                                    _viewManager.getLanguage()), false);

        return _reviewImage;
    }

    /**
     * Returns the focused review image of the label
     */
    public IViewImage getFocusedReviewImage(){

        _focusedReviewImage = _factory.makeImage(imagePath(_vars, _uniqueID + "_review_focused", _viewManager.getSize(),
                                                           _viewManager.getLanguage()), false);

        return _focusedReviewImage;

    }

    /**
     * This is the getter for _uniqueID.
     * 
     * @return      _uniqueID
     */
    public String getUniqueID() {
        return _uniqueID;
    }

    /**
     * This is the getter for _properties.
     * 
     * @return      _properties
     */
    public Properties getProperties() {
        return _properties;
    }

    /**
     * Call this method to get the x-coordinate at which this drawable should be
     * drawn.
     * 
     * @return      the x-coordinate at which this drawable should be drawn.
     */
    public int getX() {
        return _x;
    }

    /**
     * Call this method to set the x-coordinate at which this drawable should be
     * drawn.
     * 
     * @param x     the x-coordinate at which the drawable should be drawn
     */
    public void setX(int x) {
        _x = x;
    }

    /**
     * Call this method to get the y-coordinate at which this drawable should be
     * drawn.
     * 
     * @return      the y-coordinate at which this drawable should be drawn.
     */
    public int getY() {
        return _y;
    }

    /**
     * Call this method to set the y-coordinate at which this drawable should be
     * drawn.
     * 
     * @param y     the y-coordinate at which this drawable should be drawn.
     */
    public void setY(int y) {
        _y = y;
    }

    public ToggleButtonGroup getGroup() throws MeaninglessMethodException {
        throw new MeaninglessMethodException( "Labels do not define a group." );
    }

    /**
     * The location on disk that the image wrappers will use to do the loading
     * of the images depends on the uid, size, language. There is *one* special
     * case here: if the uid is "reviewtitle", append the number of selections
     * the ballot thinks have been made onto the uid. Therefore, if there have
     * been 4 selections made in the ballot, the uid of this element should be
     * interpreted as "reviewtitle4". Because we need a reference to the ballot
     * lookup adapter in order to check the number of selections, we must insert
     * this behavior here.
     * TODO someone more knowledgeable comment these params
     */
    public void initFromViewManager(IViewManager viewManagerAdapter, IBallotLookupAdapter ballotLookupAdapter,
                                    IAdapter ballotAdapter, IViewFactory factory, IBallotVars ballotVars) {

        _factory = factory;
        _vars = ballotVars;
        _ballot = ballotLookupAdapter;
        _viewManager = viewManagerAdapter;
    }

    /**
     * Construct the full path to an image given several parameters.
     * 
     * @param vars      the vars object that has the ballot bath.
     * @param uid       the image's unique id
     * @param size      the image's size index
     * @param lang      the image's language abbreviation.
     * @return          the path to the image.
     */
    protected String imagePath(IBallotVars vars, String uid, int size, String lang) {
        return vars.getBallotPath() + "/media/" + uid + "_" + size + "_" + lang + ".png";
    }

    /**
     * Construct the full path to an image given several parameters.
     *
     * @param vars      the vars object that has the ballot bath.
     * @param uid       the image's unique id
     * @param lang      the image's language abbreviation.
     * @return          the path to the image
     */
    protected String soundPath(IBallotVars vars, String uid, String lang) {
        return vars.getBallotPath() + "/media/" + uid + "_" + lang + ".mp3";
    }

    /**
     * Construct the full path to an image given several parameters.
     * It loads an image to represent the selected options (except No Selection)
     * for the review page.
     *
     * @param vars      the vars object that has the ballot bath.
     * @param uid       the image's unique id
     * @param lang      the image's language abbreviation.
     * @return          the path to the image.
     */
    protected String imageToggleButtonPath (IBallotVars vars, String uid, String lang) {
        return vars.getBallotPath() + "/media/vvpat/" + uid + "_" + lang + ".png";
    }

    /* NO OPS */
    public void select() { }
    public void makeSelected() { }
    public void makeDeselected(boolean playSound) { }

    /**
     * This method focuses the element. An element is considered to be focused
     * when the user used an input device (directional hardware buttons or a
     * mouse) to dictate that he is currently wanting to "look" (and possibly
     * select) the item.
     *
     * @see votebox.middle.view.IFocusable#focus()
     */
    public void focus() {

        soundThread  = new Thread(){

            public void run() {

                /* Prepare the mp3Player */
                try {

                    FileInputStream fileInputStream = new FileInputStream(soundPath(_vars, getUniqueID(), _viewManager.getLanguage()));
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                    mp3Player = new Player(bufferedInputStream);
                    mp3Player.play();

                }
                catch (Exception e) { mp3Player = null; e.printStackTrace(); }

            }
        };

        soundThread.start();

        _state.focus(this);
    }

    /**
     * Call this method to unfocus the element. An element can only be unfocused
     * if it has previously been focused.
     *
     * @see votebox.middle.view.IFocusable#unfocus()
     */
    public void unfocus() {

        _state.unfocus(this);

        if (mp3Player != null)
            mp3Player.close();

    }



    /**
     * This is the setter for _state.
     *
     * @param state     _state's new value
     */
    public void setState(ALabelState state) {
        _state = state;
    }


    /**
     * This is the getter for _defaultImage
     *
     * @return      _defaultImage
     */
    public IViewImage getDefaultImage() {

            _defaultImage = _factory.makeImage(imagePath(_vars, getUniqueID(), _viewManager.getSize(), _viewManager.getLanguage()), false);

        return _defaultImage;
    }



    /**
     * This is the getter for _focusedImage
     *
     * @return      _focusedImage
     */
    public IViewImage getFocusedImage() {

            _focusedImage = _factory.makeImage(imagePath(_vars, getUniqueID() + "_focused", _viewManager.getSize(), _viewManager.getLanguage()), false);

        return _focusedImage;
    }


    /**
     * This is the getter method for _focusedEvent.
     *
     * @return      the event that is raised when this toggle buttons switches to the focused state.
     */
    public Event getFocusedEvent() {
        return _focusedEvent;
    }

    /**
     * This is the getter method for _unfocusedEvent.
     *
     * @return      the event that is raised when this toggle buttons switches to the unfocused state.
     */
    public Event getUnfocusedEvent() {
        return _unfocusedEvent;
    }


    /**
     * @see votebox.middle.view.IFocusable#getUp()
     */
    public IFocusable getUp() {
        return _links.Up;
    }

    /**
     * @see votebox.middle.view.IFocusable#setUp(votebox.middle.view.IFocusable)
     */
    public void setUp(IFocusable focusable) {
        _links.Up = focusable;
    }

    /**
     * @see votebox.middle.view.IFocusable#getDown()
     */
    public IFocusable getDown() {
        return _links.Down;
    }

    /**
     * @see votebox.middle.view.IFocusable#setDown(votebox.middle.view.IFocusable)
     */
    public void setDown(IFocusable focusable) {
        _links.Down = focusable;
    }

    /**
     * @see votebox.middle.view.IFocusable#getLeft()
     */
    public IFocusable getLeft() {
        return _links.Left;
    }

    /**
     * @see votebox.middle.view.IFocusable#setLeft(votebox.middle.view.IFocusable)
     */
    public void setLeft(IFocusable focusable) {
        _links.Left = focusable;
    }

    /**
     * @see votebox.middle.view.IFocusable#getRight()
     */
    public IFocusable getRight() {
        return _links.Right;
    }

    /**
     * @see votebox.middle.view.IFocusable#setRight(votebox.middle.view.IFocusable)
     */
    public void setRight(IFocusable focusable) {
        _links.Right = focusable;
    }

    /**
     * @see votebox.middle.view.IFocusable#getNext()
     */
    public IFocusable getNext() {
        return _links.Next;
    }

    /**
     * @see votebox.middle.view.IFocusable#setNext(votebox.middle.view.IFocusable)
     */
    public void setNext(IFocusable focusable) {
        _links.Next = focusable;
    }

    /**
     * @see votebox.middle.view.IFocusable#getPrevious()
     */
    public IFocusable getPrevious() {
        return _links.Previous;
    }

    /**
     * @see votebox.middle.view.IFocusable#setPrevious(votebox.middle.view.IFocusable)
     */
    public void setPrevious(IFocusable focusable) {
        _links.Previous = focusable;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return _uniqueID;
    }
}
