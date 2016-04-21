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

import sexpression.ASEConverter;
import supervisor.model.ObservableEvent;
import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.ballot.IBallotLookupAdapter;
import votebox.middle.driver.IAdapter;

import java.io.IOException;
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
    private final ObservableEvent _castBallotEvent;
    private final ObservableEvent _commitEvent;
    private final ObservableEvent _overrideCancelConfirm;
    private final ObservableEvent _overrideCancelDeny;
    private final ObservableEvent _overrideCommitConfirm;
    private final ObservableEvent _overrideCommitDeny;
    private final ObservableEvent _reviewScreenEncountered;

    /**
     * This is the public constructor for View Manager.
     *
     * @param lookupAdapter     adapter used to fetch the state of the ballot.
     *
     * {@see votebox.middle.view.AView}
     */
    public ViewManager(IBallotLookupAdapter lookupAdapter) {

        _ballotLookupAdapter = lookupAdapter;

        _castBallotEvent         = new ObservableEvent();
        _commitEvent             = new ObservableEvent();

        _overrideCancelConfirm   = new ObservableEvent();
        _overrideCancelDeny      = new ObservableEvent();
        _overrideCommitConfirm = new ObservableEvent();
        _overrideCommitDeny = new ObservableEvent();
        _reviewScreenEncountered = new ObservableEvent();
    }

    /**
     * This method essentially launches the UI system in the voting machine.
     * Conceptually, after this call, control diverges into two threads -- one
     * handles responding to events which land in the event queues, and one is
     * given to the view to use.
     */
    public void run() {
    }

    /**
     * Draw a given page to the display.
     *
     * @param pagenum   the page number to be drawn to the display
     * @param previous  a boolean denoting whether or not we have selected next
     *                  or previous to get to this page
     */
   /* public void drawPage(int pagenum, boolean previous) {

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
        		_reviewScreenEncountered.notifyObservers(new Object[]{false, ASEConverter.convertToASE(_ballotLookupAdapter.inRaceSelectionForm())});
        		postNotice = true;
        	}
        }
        catch (IncorrectTypeException e) { e.printStackTrace(); }

        _layout.draw( pagenum, _view );

        if(postNotice)
        	_reviewScreenEncountered.notifyObservers(new Object[]{true, ASEConverter.convertToASE(_ballotLookupAdapter.inRaceSelectionForm())});
    }
*/

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
                ASEConverter.convertToASE(_ballotLookupAdapter.inRaceSelectionForm()),
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
        	ASEConverter.convertToASE(_ballotLookupAdapter.inRaceSelectionForm()),
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
        return -1;
    }

    /**
     * Looks up the override cast page, and goes to it. The page that the voter
     * was previously on is returned, so that if the override is denied, the
     * caller can go back to that page.
     */
    public int overrideCommit() throws IncorrectTypeException {
        return -1;
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
                ASEConverter.convertToASE(_ballotLookupAdapter.inRaceSelectionForm()),
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
     * Registers an observer for when a review screen is encountered.
     *
     * @param reviewScreenObserver - observer to register
     */
    public void registerForReview(Observer reviewScreenObserver) {
        _reviewScreenEncountered.addObserver(reviewScreenObserver);
    }

    /**
     * This method is called when the view needs to be killed. This usually
     * happens as a result of a Kill event.
     */
    public void kill() {

    }


	/**
	 * Gets the IBallotLookupAdapter for this ViewManager.
	 * @return the IBallotLookupAdapter
	 */
    public IBallotLookupAdapter getBallotLookupAdapter() {
    	return _ballotLookupAdapter;
    }

    public String readMessage() {
        String msg = "";
        try {
            int c, t = 0;
            for (int i = 0; i <= 3; i++) {
                t += Math.pow(256.0f, i) * System.in.read();
            }

            for (int i = 0; i < t; i++) {
                c = System.in.read();
                msg += (char) c;
            }
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
        return msg;
    }

    public void sendMessage(String msgdata) {
        try {
            int dataLength = msgdata.length();
            System.out.write((byte) (dataLength & 0xFF));
            System.out.write((byte) ((dataLength >> 8) & 0xFF));
            System.out.write((byte) ((dataLength >> 16) & 0xFF));
            System.out.write((byte) ((dataLength >> 24) & 0xFF));

            // Writing the message itself
            System.out.write(msgdata.getBytes());
            System.out.flush();
        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        }
    }

}
