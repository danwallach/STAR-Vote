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
import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.ballot.CardException;
import votebox.middle.ballot.IBallotLookupAdapter;
import votebox.middle.ballot.NonCardException;
import votebox.middle.driver.IAdapter;
import votebox.middle.driver.UnknownUIDException;
import votebox.middle.view.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;


/**
 * Buttons, like labels, are defined in the ballot layout. In addition to the
 * functionality that a label has, a button is also focusable, so that it can be
 * focused on and selected by the user. Buttons usually serve as navigational
 * controls and delegate to a strategy for what to do when they are pressed.
 * Buttons utilize the state pattern to determine which image to return for
 * GetImage. They can exist in two states: focused and default.
 */

public class Button extends FocusableLabel {
    /**
     * This event is raised when the element is focused.
     */
    private Event _focusedEvent = new Event();

    /**
     * This event is raised when the element loses Focus.
     */
    private Event _unfocusedEvent = new Event();

    /**
     * This event is raised when the element is selected.
     */
    private Event _selectedEvent = new Event();

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

    /**
     * This is the image which represents this drawable if it is referenced on a
     * review screen.
     */
    private IViewImage _reviewImage = null;

    /**
     * This is the button's state. Calls to focus and unfocus are delegated to
     * this instance.
     */
    private AButtonState _state = DefaultButtonState.Singleton;

    /**
     * This is the button's strategy. Calls to select are delegated to this
     * instance.
     */
    private IButtonStrategy _buttonStrategy = null;

    /**
     * These are the links that dictate in which direction arrow key events pull
     * the focus.
     */
    private NavigationLinks _links = new NavigationLinks();


    private Thread soundThread;

    /**
     * This is the public constructor for Button. It simply invokes FocusableLabel's
     * constructor.
     */
    public Button(String uid, Properties properties) {
        super( uid, properties );
    }


    @Override
    public void initFromViewManager(IViewManager viewManagerAdapter, IBallotLookupAdapter ballotLookupAdapter,
                                    IAdapter ballotAdapter, IViewFactory factory, IBallotVars ballotVars) {

        super.initFromViewManager(viewManagerAdapter, ballotLookupAdapter, ballotAdapter, factory, ballotVars);

        try { setStrategy(viewManagerAdapter); }
        catch (UnknownStrategyException e) { throw new BallotBoxViewException("There was a problem initializing the strategy for button with UID "+ getUniqueID(), e); }
    }

    /**
     * Call this method to make this Button's strategy agree with what is
     * defined in this Button's properties.
     *
     * @param viewManager       reference to the ViewManager if needed for the button
     *                          strategies to do something
     *
     * @throws UnknownStrategyException if the strategy that is set in this button's
     *                                  properties is unknown.
     */
    private void setStrategy(final IViewManager viewManager) throws UnknownStrategyException {

        String strategy;

        /* Retrieve the strategy from properties */
        if (this.getProperties().contains(Properties.BUTTON_STRATEGY))

            try { strategy = this.getProperties().getString(Properties.BUTTON_STRATEGY); }
            catch (IncorrectTypeException e1) { throw new UnknownStrategyException("The strategy defined was of incorrect type."); }

        else throw new UnknownStrategyException("There was no strategy defined." );

        /* Set the strategy based on what was defined in properties. */
        switch (strategy) {

            case "NextPage":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {
                        viewManager.nextPage();
                    }
                };
                break;

            case "PreviousPage":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {
                        viewManager.previousPage();
                    }
                };
                break;

            case "GoToPage":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {

                        int pagenum;

                        if (context.getProperties().contains(Properties.PAGE_NUMBER))

                            try { pagenum = context.getProperties().getInteger(Properties.PAGE_NUMBER); }
                            catch (IncorrectTypeException e) {
                                throw new BallotBoxViewException("The page number property for button with UID" +
                                        context.getUniqueID() + " is not defined as an integer", null);
                            }

                        else
                            throw new BallotBoxViewException("Button with UID: " + context.getUniqueID() + " attempted to" +
                                    " jump to new page, but the button does not define a page.", null);

                        viewManager.drawPage(pagenum, true);
                    }
                };
                break;

            case "GoToPageContaining":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {

                    /* Retrieve the UIID of the drawable which is on the page that needs to be jumped to */

                        String uid;
                        try {
                            if (context.getProperties().contains(Properties.UID))
                                uid = context.getProperties().getString(Properties.UID);
                            else
                                throw new BallotBoxViewException("Button with UID " + context.getUniqueID() + " set its " +
                                        "strategy to GoToPageContaining but did not set the UID property", null);
                        }
                        catch (IncorrectTypeException e) {
                            throw new BallotBoxViewException("The UID property for button with uid " + context.getUniqueID() +
                                    " has the incorrect type", e);
                        }

                    /* Find the page and jump there. */
                        try {
                            int jumpToPage = Button.this.getParent().getParent().lookupPage(uid);
                            viewManager.drawPage(jumpToPage, true);
                        }
                        catch (UnknownUIDException e) {
                            throw new BallotBoxViewException("Button with UID " + context.getUniqueID() + " declared the " +
                                    "GoToPageContaing strategy, but declared an invalid ID", e);
                        }
                        catch (DuplicateUIDException e) {
                            throw new BallotBoxViewException("Button with UID " + context.getUniqueID() + " declared the " +
                                    "GoToPageContaining strategy, but wishes to jump to a page " +
                                    "containing an ambiguous UID. Only UIDs which are declared " +
                                    "once in the layout are considered unambiguous.", e);
                        }
                    }

                };
                break;

            case "CastBallot":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {
                        viewManager.castCommittedBallot();
                    }
                };
                break;

            case "CommitBallot":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {
                        viewManager.commitBallot();
                    }
                };
                break;

            case "OverrideCancelConfirm":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {
                        viewManager.overrideCancelConfirm();
                    }
                };
                break;

            case "OverrideCancelDeny":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {
                        viewManager.overrideCancelDeny();
                    }
                };
                break;

            case "OverrideCastConfirm":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context)
                            throws BallotBoxViewException {
                        viewManager.overrideCastConfirm();
                    }
                };
                break;

            case "OverrideCastDeny":

                _buttonStrategy = new IButtonStrategy() {

                    public void execute(Button context) throws BallotBoxViewException {
                        viewManager.overrideCastDeny();
                    }
                };
                break;


            default:
                throw new UnknownStrategyException(strategy);
        }
    }

    /**
     * Call this method when the voter has "selected" or "chosen" this button.
     * This method will delegate to the strategy that is connected to it to
     * determine the correct behavior.
     */
    public void select() {
        getSelectedEvent().notifyObservers();
        _buttonStrategy.execute( this );
    }

    /**
     * Call this method when the voter has "focused on" or "moused over" this
     * button. This method will delegate to the state instance to determine if
     * the image that GetImage(...) returns will need to be changed.
     */
    public void focus() {

        String uniqueID;

        /*
          If this is not a label, we need to figure out what is actually being displayed (i.e. which candidate has
          been selected) so that the proper sound file can be loaded. This is necessary because the review screen only
          has one button per race, but the look of the button changes if the race it reflects changes.
         */
        if(getUniqueID().contains("B")){

            try { uniqueID = this.getParent().getParent().lookup(_ballot.selectedElement(getUniqueID())).get(0).getUniqueID(); }
            catch (CardException | NonCardException | UnknownUIDException e) { throw new RuntimeException(e); }
        }
        else uniqueID = getUniqueID();

        final String uid = uniqueID;

        soundThread = new Thread(){

            public void run() {

                    /*  Prepare the mp3Player */
                    try {

                        FileInputStream fileInputStream = new FileInputStream(soundPath(_vars, uid, _viewManager.getLanguage()));
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                        mp3Player = new Player(bufferedInputStream);
                        mp3Player.play();

                    }
                    catch (Exception e) { e.printStackTrace(); mp3Player = null; }

            }
        };

        soundThread.start();
        _state.focus(this);
    }

    /**
     * Call this method when the voter has shown that this button should not be
     * focused. This method will delegate to the state instance to determine if
     * the image that GetImage(...) returns will need to be changed.
     */
    public void unfocus() {

        _state.unfocus(this);

        if(mp3Player != null)
            mp3Player.close();

    }

    /**
     * @see FocusableLabel#getImage()
     */
    @Override
    public IViewImage getImage() {

        /*
          If this particular button is a review screen button (if its UID is the same as
          a card's UID in the ballot), then the image which is associated with this
          element is the reviewScreenImage of the currently selected drawable in the card.
        */
        try {

            boolean ballotCheck = _ballot.exists(getUniqueID()) && _ballot.isCard(getUniqueID()) &&
                                !(_ballot.selectedElement(getUniqueID()).equals(getUniqueID()));

            if (ballotCheck) {
                if (_state.equals(DefaultButtonState.Singleton))
                    return this.getParent().getParent().lookup(_ballot.selectedElement(getUniqueID())).get(0).getReviewImage();

                else if (_state.equals(FocusedButtonState.Singleton))
                    return ((FocusableLabel) this.getParent().getParent().lookup(_ballot.selectedElement(getUniqueID())).get(0)).getFocusedReviewImage();
            }
        }
        catch (UnknownUIDException | NonCardException e) {
            throw new BallotBoxViewException("While attempting to connect uid " + getUniqueID() + " with a ballot element, an error occurred.", e);
        }
        catch (CardException e) {
            throw new BallotBoxViewException(getUniqueID() + " was connected with the corresponding ballot element, but this element had an inconsistency.", e);
        }

        /* If this particular button is not a review screen button, then the image is dictated by the state.*/
        return _state.getImage( this );
    }

    @Override
    public IViewImage getReviewImage() {
        if (_reviewImage == null)
            _reviewImage = _factory.makeImage(imagePath(_vars, getUniqueID() + "_review_", _viewManager.getLanguage()), false);

        return _reviewImage;
    }

    /**
     * This is the getter method for _focusedEvent.
     * 
     * @return      the event that gets raised when this button's state changes to "focused"
     */
    public Event getFocusedEvent() {
        return _focusedEvent;
    }

    /**
     * This is the getter method for _unfocusedEvent.
     * 
     * @return      the event that gets raised when this button's state changes to "default"
     */
    public Event getUnfocusedEvent() {
        return _unfocusedEvent;
    }

    /**
     * This is the getter method for _selectedEvent.
     * 
     * @return      the event that gets raised when this button's state changes to "default"
     */
    public Event getSelectedEvent() {
        return _selectedEvent;
    }

    /**
     * Call this method to set the state of this button.
     * 
     * @param value     the state to which to set the button
     */
    public void setState(AButtonState value) {
        _state = value;
    }

    /**
     * This is the getter method for _defaultImage.
     * 
     * @return _defaultImages
     */
    public IViewImage getDefaultImage() {

        if (_defaultImage == null) {
            if (getUniqueID().contains("B"))
                _defaultImage = _factory.makeImage(imageToggleButtonPath(_vars, getUniqueID() + "_review", _viewManager.getLanguage()), false);
            else
                _defaultImage = _factory.makeImage( imagePath( _vars, getUniqueID(), _viewManager.getLanguage() ), false);
        }

        return _defaultImage;
    }

    /**
     * This is the getter method for _focusedImage.
     * 
     * @return _focusedImages
     */
    public IViewImage getFocusedImage() {

        if (_focusedImage == null)
            _focusedImage = _factory.makeImage(imagePath(_vars, getUniqueID() + "_focused", _viewManager.getLanguage()), false);

        return _focusedImage;
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
}
