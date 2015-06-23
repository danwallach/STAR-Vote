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
import votebox.middle.driver.UnknownUIDException;
import votebox.middle.view.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;


/**
 * A ToggleButton is a focusable label whose select behavior represents
 * "toggle." For the purposes of ToggleButtons that represent candidates in
 * races, toggling can be thought of as the voter's expression of preference.
 * Focusing is strictly a gui capability. The "focused" element simply is the
 * element which the user is currently looking at. <br>
 * <br>
 *
 * In order to gain the focusing capability, this class must implement the
 * IFocusable interface via FocusableLabel.
 */
public class ToggleButton extends FocusableLabel {

    private Event _selectedEvent        = new Event();
    private Event _deselectedEvent      = new Event();
    private Event _focusedEvent         = new Event();
    private Event _unfocusedEvent       = new Event();

    private NavigationLinks _links      = new NavigationLinks();

    private AToggleButtonState _state   = DefaultToggleButtonState.Singleton;

    private ToggleButtonGroup _group;

    protected IViewImage _focusedSelectedImage;
    protected IViewImage _defaultImage;
    protected IViewImage _selectedImage;
    protected IViewImage _focusedImage;
    protected IViewImage _reviewImage;

    private Thread soundThread;

    /**
     * This is the public constructor for ToggleButton. It invokes super.
     *
     * @param group         the group to which this ToggleButton will belong.
     * @param uid           the Universal identifier of this ToggleButton.
     * @param properties    the properties associated with this ToggleButton.
     */
    public ToggleButton(ToggleButtonGroup group, String uid, Properties properties) {

        super(uid, properties);

        _group = group;
    }

    /**
     *
     * @param viewManagerAdapter
     * @param ballotLookupAdapter
     * @param ballotAdapter
     * @param factory
     * @param vars
     * TODO someone knowledgeable comment
     */
    @Override
    public void initFromViewManager(IViewManager viewManagerAdapter, IBallotLookupAdapter ballotLookupAdapter,
                                    IAdapter ballotAdapter, IViewFactory factory, IBallotVars vars) {

        super.initFromViewManager(viewManagerAdapter, ballotLookupAdapter, ballotAdapter, factory, vars);

        try {
            /* Check that the UID is in the adapter and that it is selected */
            if (ballotLookupAdapter.exists(getUniqueID()) && ballotLookupAdapter.isSelected(getUniqueID()))
                setState(SelectedToggleButtonState.Singleton);
        }
        catch (UnknownUIDException e) {
            throw new BallotBoxViewException("Internal Error. The ballot lookup adapter claims " + getUniqueID() +
                                             " exists but isSelected throws an unknown exception", e);
        }

        try { _group.setStrategy(viewManagerAdapter, ballotAdapter); } /* Set the strategy */
        catch (UnknownStrategyException e) {
            throw new BallotBoxViewException("There was a problem setting the strategy on a toggle button group:", e);
        }
    }


    /**
     * This method is called by the view manager when this element gets chosen
     * by the voter as intending to be toggled. What happens next depends on
     * state, so this behavior is delegated.
     *
     * @see votebox.middle.view.IFocusable#select()
     */
    public void select() { _state.select(this); }

    /**
     * This method is called by the toggle button group when it decides that it
     * needs to make this element's state be selected
     */
    public void makeSelected() {

        soundThread  = new Thread(){

            public void run() {

                /* Prepare the mp3Player */
                try {

                    FileInputStream fileInputStream = new FileInputStream(soundPath(_vars, "Selected", _viewManager.getLanguage()));
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                    mp3Player = new Player(bufferedInputStream);
                    mp3Player.play();

                    if(this.isInterrupted()) mp3Player.close();
                }
                catch (Exception e) { mp3Player = null; e.printStackTrace(); }



            }
        };

        soundThread.start();

        _state.makeSelected( this );
    }

    /**
     * This method is called by the toggle button group when it decides that it
     * needs to make this element's state be deselected
     *
     * @param playSound     whether or not a sound should be played
     */
    public void makeDeselected(boolean playSound) {

        /* Only do this if this button is selected */
        if(playSound){

            soundThread  = new Thread(){

                public void run() {

                    /* Prepare the mp3Player */
                    try {

                        FileInputStream fileInputStream = new FileInputStream(soundPath(_vars, "Deselected", _viewManager.getLanguage()));
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                        mp3Player = new Player(bufferedInputStream);
                        mp3Player.play();

                        /* Close the mp3Player if interrupted */
                        if(this.isInterrupted()) mp3Player.close();
                    }
                    catch (Exception e) { mp3Player = null; e.printStackTrace(); }

                }
            };

            soundThread.start();
        }

        _state.makeDeselected(this);
    }

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

                    /* Close the mp3Player if interrupted */
                    if(this.isInterrupted()) mp3Player.close();
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
        if(mp3Player != null) mp3Player.close();
    }

    /**
     * @see FocusableLabel#getImage()
     */
    @Override
    public IViewImage getImage() { return _state.getImage(this); }

    /**
     * @see FocusableLabel#getReviewImage()
     */
    @Override
    public IViewImage getReviewImage() {

        String imgPath = imageToggleButtonPath(_vars, getUniqueID() + "_review", _viewManager.getLanguage());
        _reviewImage = _factory.makeImage(imgPath, false);

        return _reviewImage;
    }

    /**
     * This is the setter for _state.
     * 
     * @param state     _state's new value
     */
    public void setState(AToggleButtonState state) {
        _state = state;
    }

    /**
     * This is the getter for _group
     * 
     * @return      _group
     */
    public ToggleButtonGroup getGroup() {
        return _group;
    }

    /**
     * This is the getter for _defaultImage
     * 
     * @return      _defaultImage
     */
    public IViewImage getDefaultImage() {

        String imgPath = imagePath(_vars, getUniqueID(), _viewManager.getLanguage());
        _defaultImage = _factory.makeImage(imgPath, false);

        return _defaultImage;
    }

    /**
     * This is the getter for _selectedImage
     * 
     * @return      _selectedImage
     */
    public IViewImage getSelectedImage() {

        String imgPath = imagePath(_vars, getUniqueID() + "_selected", _viewManager.getLanguage());
        _selectedImage = _factory.makeImage(imgPath, false);

        return _selectedImage;
    }

    /**
     * This is the getter for _focusedImage
     * 
     * @return      _focusedImage
     */
    public IViewImage getFocusedImage() {

        String imgPath = imagePath(_vars, getUniqueID() + "_focused", _viewManager.getLanguage());
       _focusedImage = _factory.makeImage(imgPath, false);

        return _focusedImage;
    }

    /**
     * This is the getter for _focusedSelectedImage
     * 
     * @return _focusedSelectedImage
     */
    public IViewImage getFocusedSelectedImage() {

        String imgPath = imagePath(_vars, getUniqueID() + "_focusedSelected", _viewManager.getLanguage());
        _focusedSelectedImage = _factory.makeImage(imgPath, false);

        return _focusedSelectedImage;
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
     * This is the getter method for _selectedEvent.
     * 
     * @return      the event that is raised when this toggle buttons switches to the selected state.
     */
    public Event getSelectedEvent() {
        return _selectedEvent;
    }

    /**
     * This is the getter method for _deselectedEvent.
     * 
     * @return      the event that is raised when this toggle buttons switches to the deselected state.
     */
    public Event getDeselectedEvent() {
        return _deselectedEvent;
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
    public void setNext(IFocusable focusable) { _links.Next = focusable; }

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
}
