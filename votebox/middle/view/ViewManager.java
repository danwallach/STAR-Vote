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

import supervisor.model.ObservableEvent;
import votebox.middle.IBallotVars;
import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.ballot.IBallotLookupAdapter;
import votebox.middle.driver.DeselectionException;
import votebox.middle.driver.IAdapter;
import votebox.middle.driver.SelectionException;
import votebox.middle.driver.UnknownUIDException;
import votebox.middle.view.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * The ViewManager is our top level view encapsulation. Here, all top level view
 * initialization and event response behavior is defined. The view manager (both
 * directly and through its supporting classes) talks to (and controls) an
 * abstract view implementation that conforms to IView.
 */

public class ViewManager implements IViewManager {

    private final IBallotLookupAdapter _ballotLookupAdapter;
    private final ArrayList<String> _supportedLanguages;
    private final IAdapter _ballotAdapter;
    private final IBallotVars _variables;
    private final IViewFactory _factory;
    private final ObservableEvent _castBallotEvent;
    private final ObservableEvent _commitEvent;
    private final ObservableEvent _overrideCancelConfirm;
    private final ObservableEvent _overrideCancelDeny;
    private final ObservableEvent _overrideCommitConfirm;
    private final ObservableEvent _overrideCommitDeny;
    private final ObservableEvent _reviewScreenEncountered;
    private final ObservableEvent _pageChanged;

    private IView _view;


    /**
     * A variable that will only allow focusing with orange backgrounds
     * if and only if a key on the keyboard/other input device is pressed,
     * not including the mouse
     */
    private static boolean focusEnabled = false;

    private IFocusable _currentFocusedElement = null;
    private Layout _layout = null;

    private int _page = 0;
    private int _mediaSize = 0;
    private String _language = "en";
    
    /* This is set when we're redrawing and don't want to queue up a bunch of mouse events */
    private boolean _ignoreMouseInput = false;

    /**
     * This is the public constructor for View Manager.
     * 
     * @param adapter           the adapter that this view manager will use to communicate
     *                          with the ballot.
     * @param vars              global variables which contain important path information.
     * @param lookupAdapter     adapter used to fetch the state of the ballot.
     * @param factory           factory used to create the View to display the ballot.
     * 
     * {@see votebox.middle.view.AView}
     */
    public ViewManager(IAdapter adapter, IBallotLookupAdapter lookupAdapter, IBallotVars vars, IViewFactory factory) {

        _factory = factory;
        _view = factory.makeView();

        focusEnabled = _view.focusEnabled();

        _ballotAdapter = adapter;
        _ballotLookupAdapter = lookupAdapter;

        _variables = vars;
        _supportedLanguages      = new ArrayList<>();

        _castBallotEvent         = new ObservableEvent();
        _commitEvent             = new ObservableEvent();

        _overrideCancelConfirm   = new ObservableEvent();
        _overrideCancelDeny      = new ObservableEvent();
        _overrideCommitConfirm = new ObservableEvent();
        _overrideCommitDeny = new ObservableEvent();

        _reviewScreenEncountered = new ObservableEvent();
        _pageChanged             = new ObservableEvent();

        registerQueues();
        setMediaSizes();
        setLanguages();
        
        _language = getSupportedLanguages().get(0);
        System.out.println("(ViewManager) Selected Language: "+_language);
    }

    /**
     * This method essentially launches the UI system in the voting machine.
     * Conceptually, after this call, control diverges into two threads -- one
     * handles responding to events which land in the event queues, and one is
     * given to the view to use.
     */
    public void run() {
        _view.run(new Runnable() {

            public void run() {
                makePages();
                drawPage(0, false);
            }
        });

    }

    /**
     * Call this method to kill the view manager's components.
     * 
     */
    public void dispose() {
        _view.dispose();
    }

    /**
     * Draw a given page to the display.
     *
     * @param pagenum   the page number to be drawn to the display
     * @param previous  a boolean denoting whether or not we have selected next
     *                  or previous to get to this page
     */
    public void drawPage(int pagenum, boolean previous) {

        _view.clearDisplay();

        if(pagenum != _page) {

        	List<String> affectedUIDs = _layout.getPages().get(_page).getUniqueIDs();
        	_pageChanged.notifyObservers(affectedUIDs);
        }
        
        _page = pagenum;
        setInitialFocus(previous);


        _layout.initFromViewManager(_page, this, _ballotLookupAdapter, _ballotAdapter, _factory, _variables);


        boolean postNotice = false;
        
        try {

        	String isReviewPage = _layout.getPages().get(pagenum).getProperties().getString("IsReviewPage"); 

        	if(isReviewPage != null && isReviewPage.equals("yes")){
        		_reviewScreenEncountered.notifyObservers(new Object[]{false, _ballotLookupAdapter.getCastBallot()});
        		postNotice = true;
        	}
        }
        catch (IncorrectTypeException e) { e.printStackTrace(); }
        
        _layout.draw( pagenum, _view );
        
        if(postNotice)
        	_reviewScreenEncountered.notifyObservers(new Object[]{true, _ballotLookupAdapter.getCastBallot()});
    }

    /**
     * Switch the focus to a given drawable. This method performs checks to
     * ensure that the given drawable is valid before doing anything with it.
     * 
     * @param dt    the RenderCardElement to which to switch
     */
    private void switchFocus(IFocusable dt) {

        /* Check the validity of the reference we got */
        if (dt != null) {

            /* Tell the view to change the focus to the new element */
            _currentFocusedElement.unfocus();

            if (_view.focusEnabled()) dt.focus();

            _view.invalidate(dt);
            _view.invalidate(_currentFocusedElement);

            /* Keep track of who the new element is */
            _currentFocusedElement = dt;
        }
    }

    /**
     * Ask the view to invalidate all the currently displayed buttons.
     */
    private void invalidateAll() {
        for (IDrawable d : getCurrentPage().getChildren())
            if (d instanceof ToggleButton)
                _view.invalidate(d) ;
    }

    /**
     * This method is called when the voter selects a specific candidate in a
     * race or selects a navigation control. This method defines the response of
     * the view to report that an actual vote has been recorded, or that the
     * visual ballot representation has been changed.
     * 
     * @param element       the element which has been selected
     */
    public void select(IDrawable element) {

        if (element instanceof IFocusable) {

            ((IFocusable) element).select();

            if (element instanceof ToggleButton)
                invalidateAll();
        }
    }

    /**
     * This method is called when the voter uses a hardware key to Select a
     * specific candidate in a race or a navigation control. Here, the currently
     * focused ADrawable is assumed to be the element that the user intends to
     * Select.
     */
    public void select() {
        select( _currentFocusedElement );
    }

    /**
     * This method is called when the voter focuses on a specific candidate in a
     * race. This method usually is called when something is moused over.
     * 
     * @param ce        the drawable which has been focused on.
     */
    public void focus(IDrawable ce) {

        if (ce instanceof IFocusable && ce != _currentFocusedElement) {
            switchFocus((IFocusable) ce);
        }
    }

    /**
     * This method is called when the voter indicates that he would like to cast
     * his ballot. This behavior can also be defined in the ViewManager as a
     * special case of the select(...) method. We include an explicit method for
     * the notion of ballot casting so that separate hardware can be used to
     * convey the notion of a voter being done.<br>
     * XXX: Making the cast ballot an event is potentially unsafe design. When
     * releasing, remove the event and put all code in this function, so that an
     * outside party cannot register malicious code.
     */
    public void castCommittedBallot() {

    	Object[] toPass = new Object[]{
    		_ballotLookupAdapter.getCastBallot(),
    		_ballotLookupAdapter.getRaceGroups()
    	};

        _castBallotEvent.notifyObservers(toPass);
    }

    /**
     * Allows an observer to register for the Cast Ballot event. This way,
     * different entry points (such as the standalone version versus the
     * auditorium implementation) can do different things with the cast ballot.
     * 
     * @param obs       the observer that gets registered
     */
    public void registerForCastBallot(Observer obs) {
        _castBallotEvent.addObserver(obs);
    }

    /**
     * Call this method if the voter has proceeded past the review screen.
     */
    public void commitBallot() {

    	Object[] toPass = new Object[]{
        	_ballotLookupAdapter.getCastBallot(),
        	_ballotLookupAdapter.getRaceGroups(),
            _ballotLookupAdapter.getTitles()
    	};

        _commitEvent.notifyObservers(toPass);
    }

    /**
     * Register for the even that gets fired when the voter has passed the
     * review screen. There should be behavior registered which commits the
     * ballot to the network.
     * 
     * @param observer      the observer to be registered for the commit event
     */
    public void registerForCommit(Observer observer) {
        _commitEvent.addObserver(observer);
    }

    /**
     * Looks up the override cancel page, and goes to it. The page that the
     * voter was previously on is returned, so that if the override is denied,
     * the caller can go back to that page.
     */
    public int overrideCancel() throws IncorrectTypeException {

    	if (!_layout.getProperties().contains(Properties.OVERRIDE_CANCEL_PAGE))
            throw new BallotBoxViewException("Override Cancel Page does not exist", null);

        int newPage = _layout.getProperties().getInteger(Properties.OVERRIDE_CANCEL_PAGE);
        int currentPage = _page;

        drawPage(newPage, false);

        return currentPage;
    }

    /**
     * Looks up the override cast page, and goes to it. The page that the voter
     * was previously on is returned, so that if the override is denied, the
     * caller can go back to that page.
     */
    public int overrideCommit() throws IncorrectTypeException {

        if (!_layout.getProperties().contains(Properties.OVERRIDE_COMMIT_PAGE))
            throw new BallotBoxViewException("Override Cast Page does not exist", null);

        int newPage = _layout.getProperties().getInteger(Properties.OVERRIDE_COMMIT_PAGE);
        int currentPage = _page;

        drawPage(newPage, false);

        return currentPage;
    }

    /**
     * Fired when the override-cancel operation is confirmed on the booth.
     */
    public void overrideCancelConfirm() {
        _overrideCancelConfirm.notifyObservers();
    }

    /**
     * Fired when the override-cancel operation is denied from the booth.
     */
    public void overrideCancelDeny() {
        _overrideCancelDeny.notifyObservers();
    }

    /**
     * Fired when the override-cast operation is confirmed on the booth.
     */
    public void overrideCommitConfirm() {


        Object[] toPass = new Object[]{
                _ballotLookupAdapter.getCastBallot(),
                _ballotLookupAdapter.getRaceGroups(),
                _ballotLookupAdapter.getTitles()
        };

        _overrideCommitConfirm.notifyObservers(toPass);
    }

    /**
     * Fired when the override-cast operation is confirmed on the booth.
     */
    public void overrideCommitDeny() {
        _overrideCommitDeny.notifyObservers();
    }

    /**
     * Register for the page change event.
     * 
     * @param obs       the observer
     */
    public void registerForPageChanged(Observer obs) {
    	_pageChanged.addObserver(obs);
    }
    
    /**
     * Register for the override cancel confirm event
     * 
     * @param obs       the observer
     */
    public void registerForOverrideCancelConfirm(Observer obs) {
        _overrideCancelConfirm.addObserver(obs);
    }

    /**
     * Register for the override cancel deny event
     * 
     * @param obs       the observer
     */
    public void registerForOverrideCancelDeny(Observer obs) {
        _overrideCancelDeny.addObserver(obs);
    }

    /**
     * Register for the override cast confirm event
     * 
     * @param obs       the observer
     */
    public void registerForOverrideCommitConfirm(Observer obs) {
        _overrideCommitConfirm.addObserver(obs);
    }

    /**
     * Register for the override cast deny event
     * 
     * @param obs       the observer
     */
    public void registerForOverrideCommitDeny(Observer obs) {
        _overrideCommitDeny.addObserver(obs);
    }

    /**
     * This method is called when the view needs to be killed. This usually
     * happens as a result of a Kill event.
     */
    public void kill() {
        _view.dispose();
    }

    /**
     * This method is called when the voter presses the left arrow key (or
     * equivalent hardware) on the machine. This method is called when the user
     * has indicated that he would like to Focus the item directly to the left
     * of the one which is currently focused.
     */
    public void moveFocusLeft() {

        if(_currentFocusedElement == null)
            setInitialFocus(true);

        switchFocus( _currentFocusedElement.getLeft() );
    }

    /**
     * This method is called when the voter presses the right arrow key (or
     * equivalent hardware) on the machine. This method is called when the user
     * has indicated that he would like to Focus the item directly to the right
     * of the one which is currently focused.
     */
    public void moveFocusRight() {

        if(_currentFocusedElement == null)
            setInitialFocus(true);

        switchFocus( _currentFocusedElement.getRight() );
    }

    /**
     * This method is called when the voter presses the up arrow key (or
     * equivalent hardware) on the machine. This method is called when the user
     * has indicated that he would like to Focus the item directly above the one
     * that is currently focused.
     */
    public void moveFocusUp() {

        if(_currentFocusedElement == null)
            setInitialFocus(true);

        switchFocus( _currentFocusedElement.getUp() );
    }

    /**
     * This method is called when the voter presses the down arrow key (or
     * equivalent hardware) on the machine. This method is called when the user
     * has indicated that he would like to Focus the item directly below the one
     * which is currently focused.
     */
    public void moveFocusDown() {

        if(_currentFocusedElement == null)
            setInitialFocus(true);

        switchFocus( _currentFocusedElement.getDown() );
    }

    /**
     * This method is called when the voter, using a voting machine with only
     * two buttons, presses the forward arrow key (or equivalent hardware) on
     * the machine. This method is called when the user has indicated that he
     * would like to Focus the item directly after the one which is currently
     * focused.
     */
    public void moveFocusNext() {

        if(_currentFocusedElement == null)
            setInitialFocus(true);

        switchFocus( _currentFocusedElement.getNext() );
    }

    /**
     * This method is called when the voter, using a voting machine with only
     * two buttons, presses the backwards arrow key (or equivalent hardware) on
     * the machine. This method is called when the user has indicated that he
     * would like to Focus the item directly before the one which is currently
     * focused.
     */
    public void moveFocusBack() {

        if(_currentFocusedElement == null)
            setInitialFocus(true);

        switchFocus( _currentFocusedElement.getPrevious() );
    }

    /**
     * This method is called when the voter communicates his interest to go to
     * the next page.
     */
    public void nextPage() {
        /* TODO check if this needs to be equality */
        if (_page + 1 < _layout.getPages().size()) drawPage(_page + 1, false);
    }

    /**
     * This method is called when the voter communicates his interest to go to
     * the previous page.
     */
    public void previousPage() {
        if (_page > 0) drawPage( _page - 1, true);
    }

    /**
     * This is the getter method for the media size. Implementers of IView must
     * check this value to determine which size index to hand to a drawable when
     * the IView asks it for a representative image.
     * 
     * @return          the currently set media size index.
     */
    public int getSize() {
        return _mediaSize;
    }

    /**
     * This is the getter method for the language. Implementers of IView must
     * check this value to determine which language value to hand to a drawable
     * when the IView asks it for a representative image.
     * 
     * @return          the currently set language value.
     */
    public String getLanguage() {
        return _language;
    }

    /**
     * This method is the getter for the view that this manager is controlling.
     * 
     * @return          the view that this manager is controlling.
     */
    public IView getView() {
        return _view;
    }

    /**
     * This is the getter method for the current page (the page that the voter
     * is currently viewing).
     * 
     * @return          the page that the voter is currently viewing.
     */
    public RenderPage getCurrentPage() {
        List<RenderPage> pages = _layout.getPages();
        //System.out.println("Page:" + _page);
        return pages.get( _page );
    }

    /**
     * This is the getter method for _ballotAdapter.
     * 
     * @return          _ballotAdapter
     */
    public IAdapter getBallotAdapter() {
        return _ballotAdapter;
    }

    /**
     * This is the getter method for _layout.
     * 
     * @return _layouts
     */
    public Layout getCurrentLayout() {
        return _layout;
    }

    /**
     * Call this method to set the language that the view will use when it asks
     * a drawable for its image.
     * 
     * @param lang      the language that wishes to be used.
     */
    public void setLanguage(String lang) {
        if ( _supportedLanguages.contains(lang) ) {
            _language = lang;
            makePages();
            //System.out.println("Trying to draw page " + _page);
            drawPage( _page, false);
        }
    }

    /**
     * Switch the size index that is currently being used to display images on
     * the screen.
     * 
     * @param size      the size to which to change the size index
     */
    public void setSize(int size) {
        _mediaSize = size;
        makePages();
        drawPage( _page, false);
    }

    /**
     * This is the getter method for _supportedLanguages
     * 
     * @return      _supportedLanguages.
     */
    public List<String> getSupportedLanguages() {
        return _supportedLanguages;
    }

    /**
     * Call this method to explicitly make one of the toggle buttons that is
     * currently in the layout selected. This method is implemented with a
     * best-effort approach. This method is most notably used by a
     * straight-ticket card strategy. Straight-ticket behavior can be though of
     * as a macro for physically navigating through every page and selecting
     * members of a given party. This method is precisely how that is done.
     * 
     * @param uid       the uid of the element that wants to be selected.
     * @return          false if the element given is not a toggle button or
     *                  doesn't exist, true otherwise TODO where is the false return?
     *
     * @throws SelectionException if there was a problem selecting something
     */
    public boolean select(String uid) throws UnknownUIDException, SelectionException {

        try {
            for (IDrawable d : getCurrentLayout().lookup(uid))
                d.getGroup().select((ToggleButton) d);
        }
        catch (ClassCastException e) {
            throw new SelectionException("There was a problem deselecting something in the view. " + uid + " is not a toggle button.", e);
        }
        catch (BallotBoxViewException | MeaninglessMethodException e) {
            throw new SelectionException("There was a problem selecting something in the view. : " + e.getMessage(), e);
        }

        return true;
    }

    /**
     * Call this method to explicitly make one of the toggle buttons that is
     * currently in the layout be deselected. This method is implemented with a
     * best-effort approach.
     *
     * @param uid           the uid of the element that wants to be deselected.
     * @param playSound     whether a sound is to be played
     * @return              false if the element does not exist or is not a toggle
     *                      button, true otherwise TODO false return?
     */
    public boolean deselect(String uid, boolean playSound) throws UnknownUIDException, DeselectionException {

        try {

            for (IDrawable d : getCurrentLayout().lookup(uid))
                d.getGroup().deselect((ToggleButton) d, playSound);
        }
        catch (ClassCastException e) {
            throw new DeselectionException("There was a problem deselecting something in the view. " + uid + " is not a toggle button.", e);
        }
        catch (BallotBoxViewException | MeaninglessMethodException  e) {
            throw new DeselectionException("There was a problem deselecting something in the view. : " + e.getMessage(), e);
        }

        return true;
    }

    /**
     * This method takes the first IFocusable in the current page and sets it to
     * focused. This is useful as a focus initialization method: with every new
     * page that is displayed, there should be an initial element that is
     * focused from which the voter can move. This method takes care of the job
     * of focusing that one initial element.
     *
     * @param previous      a boolean denoting whether or not we have selected next
     *                      or previous to get to this page
     */
    private void setInitialFocus(boolean previous) {

        /* Find a focusable element, Focus it, Unfocus the rest of the elements. */
        if(_view.focusEnabled()){
            ArrayList<IDrawable> children = (ArrayList<IDrawable>) getCurrentPage().getChildren();

            IDrawable first = null;
            IDrawable second = null;

            for (IDrawable drawable : children) {

                if (drawable instanceof IFocusable) {

                    if (first == null)
                        first = drawable;
                    else if (second == null)
                        second = drawable;

                    ((IFocusable) drawable).unfocus();
                }
            }


            if (previous && second != null) {
                _currentFocusedElement =  (IFocusable) second;
                ((IFocusable) second).focus();

            }
            else if (!previous && first != null) {
                _currentFocusedElement = (IFocusable) first;
                ((IFocusable) first).focus();
            }


        }
    }

    /**
     * Call this method to construct a list of drawable pages which are
     * representative of the current ballot. This list will be assigned to the
     * pages field.
     */
    private void makePages() {

        try {
            _layout = new LayoutParser().getLayout(_variables, getSize(), getLanguage(), _view);
            _layout.initFromViewManager(this, _ballotLookupAdapter, _ballotAdapter, _factory, _variables);
        }
        catch (LayoutParserException e) {
            throw new BallotBoxViewException("While attempting to parse the layout for size " + getSize() +
                                             " and language " + getLanguage() + ", the parser encountered an error.", e );
        }
    }

    /**
     * Call this method to tell the view where to put input events when they
     * come up.
     */
    private void registerQueues() {
    	
    	/* Registering for the cast ballot button being pressed */
        _view.register( EventType.CAST_BALLOT, new IEventHandler() {

            public void handle(InputEvent event) throws BallotBoxViewException {
                castCommittedBallot();
            }
        } );

        /* Registering for the kill key */
        _view.register( EventType.KILL, new IEventHandler() {

            public void handle(InputEvent event) throws BallotBoxViewException {
                kill();
            }
        } );

        /* Registering for the mouse button being pressed. We ignore this event if the view is currently being redrawn */
        _view.register( EventType.MOUSE_DOWN, new IEventHandler() {

            public void handle(InputEvent event) throws BallotBoxViewException {
            	if(!_ignoreMouseInput)
            		select( event.focusedDrawable() );    }
        } );

        _view.register( EventType.MOUSE_MOVE, new IEventHandler() {

            public void handle(InputEvent event) {
                if(!_ignoreMouseInput && focusEnabled)
                    focus( event.focusedDrawable() );
            }
        } );

        _view.register( EventType.LEFT, new IEventHandler() {
            public void handle(InputEvent event) {
                moveFocusLeft();
            }
        });

        _view.register( EventType.RIGHT, new IEventHandler() {
            public void handle(InputEvent event) {
                moveFocusRight();
            }
        });

        _view.register( EventType.UP, new IEventHandler() {
            public void handle(InputEvent event) {
                moveFocusUp();
            }
        });

        _view.register( EventType.DOWN, new IEventHandler() {
            public void handle(InputEvent event) {
                moveFocusDown();
            }
        });

        _view.register( EventType.NEXT, new IEventHandler() {
            public void handle(InputEvent event) {
                moveFocusNext();
            }
        });

        _view.register( EventType.PREVIOUS, new IEventHandler() {
            public void handle(InputEvent event) {
                moveFocusBack();
            }
        });

        _view.register( EventType.SELECT, new IEventHandler() {
            public void handle(InputEvent event) {
                select();
            }
        });

        /* Registering for notice that the view is being redrawn */
        _view.register( EventType.BEGIN_PAGE_REDRAW, new IEventHandler() {
        	public void handle(InputEvent event) throws BallotBoxViewException {
        		_ignoreMouseInput = true;
        	}
        });
        
        /* Registering for notice that the view has finished being drawn */
        _view.register( EventType.END_PAGE_REDRAW, new IEventHandler(){
        	public void handle(InputEvent event) throws BallotBoxViewException {
        		_ignoreMouseInput = false;
        	}
        });        
    }

    /**
     * Call this method to set the media size fields for this view manager based
     * on properties that are defined in the ballot.
     */
    private void setMediaSizes() {

        if (_ballotAdapter.getProperties().contains( Properties.START_IMAGE_SIZE ))
            try { _mediaSize = _ballotAdapter.getProperties().getInteger(Properties.START_IMAGE_SIZE); }
            catch (IncorrectTypeException e) { System.err.println( "StartImageSize property was malformed. Using 0..." ); }
    }

    /**
     * Call this method to figure which languages are allowed. These should be
     * declared in the ballot's properties.
     */
    private void setLanguages() {

        if (_ballotAdapter.getProperties().contains(Properties.LANGUAGES)) {
            try { for (String s : _ballotAdapter.getProperties().getStringList(Properties.LANGUAGES)) _supportedLanguages.add(s); }
            catch (IncorrectTypeException e) { System.err.println("Languages property mal formed. Using \"en\"..."); }
        }
    }

    /**
     * Registers an observer for when a review screen is encountered.
     * 
     * @param reviewScreenObserver - observer to register
     */
	public void registerForReview(Observer reviewScreenObserver) {
		_reviewScreenEncountered.addObserver(reviewScreenObserver);
	}

	/**
	 * Gets the IBallotLookupAdapter for this ViewManager.
	 * @return the IBallotLookupAdapter
	 */
    public IBallotLookupAdapter getBallotLookupAdapter() {
    	return _ballotLookupAdapter;
    }

    public void setView(IView view) {
        _view = view;
    }
}
