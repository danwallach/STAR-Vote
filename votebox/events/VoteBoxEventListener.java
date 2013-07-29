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

/**
 * A listener that handles various VoteBox message events on an auditorium
 * network. Events for a machine joining and leaving are included as well.
 * 
 * @author cshaw
 */
public interface VoteBoxEventListener {

    /**
     * Fired when the activated message is received
     * 
     * @param e
     *            the event
     */
    public void activated(ActivatedEvent e);

    /**
     * Fired when the assign-label message is received
     * 
     * @param e
     *            the event
     */
    public void assignLabel(AssignLabelEvent e);

    /**
     * Fired when the authorized-to-cast message is received
     * 
     * @param e
     *            the event
     */
    public void authorizedToCast(AuthorizedToCastEvent e);

    /**
     * Fired when the ballot-received message is received
     * 
     * @param e
     *            the event
     */
    public void ballotReceived(BallotReceivedEvent e);

    /**
     * Fired when the cast-ballot message is received
     * 
     * @param e
     *            the event
     */
    public void castCommittedBallot(CastCommittedBallotEvent e);

    public void commitBallot(CommitBallotEvent e);

    /**
     * Fired when a connection is established with another machine on the
     * network (regardless of which machine initiated it)
     * 
     * @param e
     *            the event
     */
    public void joined(JoinEvent e);

    /**
     * Fired when the last-polls-open message is received
     * 
     * @param e
     *            the event
     */
    public void lastPollsOpen(LastPollsOpenEvent e);

    /**
     * Fired when a connection with another machine is lost for any reason
     * 
     * @param e
     *            the event
     */
    public void left(LeaveEvent e);

    /**
     * Fired when the override-cancel message is received
     * 
     * @param e
     *            the event
     */
    public void overrideCancel(OverrideCancelEvent e);

    /**
     * Fired when the override-cancel-confirm message is received
     * 
     * @param e
     *            the event
     */
    public void overrideCancelConfirm(OverrideCancelConfirmEvent e);

    /**
     * Fired when the override-cancel-deny message is received
     * 
     * @param e
     *            the event
     */
    public void overrideCancelDeny(OverrideCancelDenyEvent e);

    /**
     * Fired when the override-cast message is received
     * 
     * @param e
     *            the event
     */
    public void overrideCast(OverrideCastEvent e);

    /**
     * Fired when the override-cast-confirm message is received
     * 
     * @param e
     *            the event
     */
    public void overrideCastConfirm(OverrideCastConfirmEvent e);

    /**
     * Fired when the override-cast-deny message is received
     * 
     * @param e
     *            the event
     */
    public void overrideCastDeny(OverrideCastDenyEvent e);

    /**
     * Fired when the polls-closed message is received
     * 
     * @param e
     *            the event
     */
    public void pollsClosed(PollsClosedEvent e);

    /**
     * Fired when the polls-open message is received
     * 
     * @param e
     *            the event
     */
    public void pollsOpen(PollsOpenEvent e);

    /**
     * Fired when the polls-open? message is received
     * 
     * @param e
     *            the event
     */
    public void pollsOpenQ(PollsOpenQEvent e);

    /**
     * Fired when the supervisor message is received
     * 
     * @param e
     *            the event
     */
    public void supervisor(SupervisorEvent e);

    /**
     * Fired when the votebox message is received
     * 
     * @param e
     *            the event
     */
    public void votebox(VoteBoxEvent e);

    /**
     * Fired when the ballotScanner message is received
     *
     * @param e
     *            the event
     */
    public void ballotScanner(BallotScannerEvent e);
    
    /**
     * Fired when the ballot counted message is received
     * 
     * @param e
     * 			  the event
     */
    public void ballotCounted(BallotCountedEvent e);

    /**
     * Fired when a ballot gets scanned
     */
    public void ballotScanned(BallotScannedEvent e);

    /**
     * called to signal the input of a pin
     */

    public void pinEntered(PINEnteredEvent event);

    /**
     * called to signal that the input pin is invalid
     */

    public void invalidPin(InvalidPinEvent event);

    /**
     * called to tell new box about the status of the polls (open or closed)
     */

    public void pollStatus(PollStatusEvent pollStatusEvent);

    /**
     * Supervisor confirmation that a scanned ballot has been cast
     */
    public void ballotAccepted(BallotScanAcceptedEvent e);

    /**
     * Supervisor message that a scanned ballot has been rejected
     */
    public void ballotRejected(BallotScanRejectedEvent e);

    /**
     * Votebox message that a ballot has been submitted for printing
     */
    public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent);

    /**
     * called to inform the supervisor that a ballot has been successfully printed and needs to be scanned
     */
    public void ballotPrintSuccess(BallotPrintSuccessEvent ballotPrintSuccessEvent);

    /**
     * called to inform the supervisor that a ballot has not been successfully printed
     */
    public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent);

    /**
     * Will upload a ballot to the webserver once it is cast so that a voter can verify their ballot
     */
    public void uploadCastBallots(CastBallotUploadEvent castBallotUploadEvent);

    /**
     * Will upload a ballot upon being challenged to the webserver so that it can be reviewed
     */
    public void uploadChallengedBallots(ChallengedBallotUploadEvent challengedBallotUploadEvent);

    public void scannerStart(StartScannerEvent startScannerEvent);

    /**
     * Polls all machines when an unknown connection is detected and attempts add that machine to known machines
     */
    public void pollMachines(PollMachinesEvent pollMachinesEvent);

    public void spoilBallot(SpoilBallotEvent spoilBallotEvent);

    /**
     * Announces when the supervisor gets a provisional ballot
     */
    public void announceProvisionalBallot(ProvisionalBallotEvent provisionalBallotEvent);

    /**
     * Sends over the ballot for a provisional voting session
     */
    public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent provisionalAuthorizeEvent);

    /**
     * Announces the end of a provisional voting session on the booth-end
     */
    public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent);

    public void authorizedToCastWithNIZKS(AuthorizedToCastWithNIZKsEvent e);

    public void tapMachine(TapMachineEvent tapMachineEvent);
}
