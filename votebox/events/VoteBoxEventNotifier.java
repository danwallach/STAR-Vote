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

package votebox.events;

import java.util.ArrayList;

/**
 * The VoteBoxEventNotifier can be treated as a VoteBoxEventListener, as it
 * implements all of the methods. However it differs in that it contains an
 * array of listeners, and forwards events received onto those listeners (in the
 * order they were registered).
 * 
 * @author cshaw
 */
public class VoteBoxEventNotifier implements VoteBoxEventListener {

    ArrayList<VoteBoxEventListener> listeners;

    /**
     * Constructs a new VoteBoxEventNotifier with an empty list of listeners.
     */
    public VoteBoxEventNotifier() {
        listeners = new ArrayList<VoteBoxEventListener>();
    }

    public void activated(ActivatedEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.activated(e);
    }

    /**
     * Adds a listener to this notifier.
     * 
     * @param l
     *            the listener
     */
    public void addListener(VoteBoxEventListener l) {
        listeners.add(l);
    }

    public void assignLabel(AssignLabelEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.assignLabel(e);
    }

    public void authorizedToCast(AuthorizedToCastEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.authorizedToCast(e);
    }

    public void ballotReceived(BallotReceivedEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.ballotReceived(e);
    }

    public void castCommittedBallot(CastCommittedBallotEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.castCommittedBallot(e);
    }

    public void joined(JoinEvent e) {
        System.out.println("Joined!");
        for (VoteBoxEventListener l : listeners)
            l.joined(e);
    }

    public void lastPollsOpen(LastPollsOpenEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.lastPollsOpen(e);
    }

    public void left(LeaveEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.left(e);
    }

    public void overrideCancel(OverrideCancelEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.overrideCancel(e);
    }

    public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.overrideCancelConfirm(e);
    }

    public void overrideCancelDeny(OverrideCancelDenyEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.overrideCancelDeny(e);
    }

    public void overrideCast(OverrideCastEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.overrideCast(e);
    }

    public void overrideCastConfirm(OverrideCastConfirmEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.overrideCastConfirm(e);
    }

    public void overrideCastDeny(OverrideCastDenyEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.overrideCastDeny(e);
    }

    public void pollsClosed(PollsClosedEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.pollsClosed(e);
    }

    public void pollsOpen(PollsOpenEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.pollsOpen(e);
    }

    public void pollsOpenQ(PollsOpenQEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.pollsOpenQ(e);
    }

    public void pollStatus(PollStatusEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.pollStatus(e);
    }

    /**
     * Removes a listener from this notifier.
     * 
     * @param l
     *            the listener
     */
    public void removeListener(VoteBoxEventListener l) {
        listeners.remove(l);
    }

    public void supervisor(SupervisorEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.supervisor(e);
    }

    public void votebox(VoteBoxEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.votebox(e);
    }

    public void ballotscanner(BallotScannerEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.ballotscanner(e);
    }

    public void commitBallot(CommitBallotEvent e) {
    	for (VoteBoxEventListener l : listeners)
            l.commitBallot(e);
    }

	public void ballotCounted(BallotCountedEvent e) {
		for(VoteBoxEventListener l : listeners)
			l.ballotCounted(e);
	}

    public void ballotScanned(BallotScannedEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.ballotScanned(e);
    }

    public void pinEntered(PINEnteredEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.pinEntered(e);
    }

    public void invalidPin(InvalidPinEvent e){
        for (VoteBoxEventListener l : listeners)
            l.invalidPin(e);
    }
    public void ballotAccepted(BallotScanAcceptedEvent e){
        for(VoteBoxEventListener l : listeners)
            l.ballotAccepted(e);
    }

    public void ballotRejected(BallotScanRejectedEvent e){
        System.out.println("Sending event: " + e.toSExp().toString());
        for(VoteBoxEventListener l : listeners)
            l.ballotRejected(e);
    }

    public void ballotPrinting(BallotPrintingEvent e) {
        for(VoteBoxEventListener l : listeners)
            l.ballotPrinting(e);
    }

    public void ballotPrintSuccess(BallotPrintSuccessEvent e) {
        for(VoteBoxEventListener l : listeners)
            l.ballotPrintSuccess(e);
    }

    public void ballotPrintFail(BallotPrintFailEvent e) {
        for(VoteBoxEventListener l : listeners)
            l.ballotPrintFail(e);
    }

    public void uploadCastBallots(CastBallotUploadEvent castBallotUploadEvent) {
        for(VoteBoxEventListener l : listeners)
            l.uploadCastBallots(castBallotUploadEvent);
    }

    public void uploadChallengedBallots(ChallengedBallotUploadEvent challengedBallotUploadEvent) {
        for(VoteBoxEventListener l : listeners)
            l.uploadChallengedBallots(challengedBallotUploadEvent);
    }

    public void scannerstart(StartScannerEvent e){
        for(VoteBoxEventListener l : listeners)
            l.scannerstart(e);
    }

    public void pollMachines(PollMachinesEvent pollMachinesEvent) {
        for(VoteBoxEventListener l : listeners)
            l.pollMachines(pollMachinesEvent);
    }

    public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {
        for(VoteBoxEventListener l : listeners)
            l.spoilBallot(spoilBallotEvent);
    }

    public void announceProvisionalBallot(ProvisionalBallotEvent e) {
        for(VoteBoxEventListener l : listeners)
            l.announceProvisionalBallot(e);
    }

    public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent e) {
        for(VoteBoxEventListener l : listeners)
            l.provisionalAuthorizedToCast(e);
    }

    public void provisionalCommitBallot(ProvisionalCommitEvent e) {
        for(VoteBoxEventListener l : listeners)
            l.provisionalCommitBallot(e);
    }

    public void authorizedToCastWithNIZKS(AuthorizedToCastWithNIZKsEvent e) {
        for (VoteBoxEventListener l : listeners)
            l.authorizedToCastWithNIZKS(e);
    }

}
