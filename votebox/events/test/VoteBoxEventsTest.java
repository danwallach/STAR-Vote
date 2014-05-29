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

package votebox.events.test;

import auditorium.IKeyStore;
import auditorium.SimpleKeyStore;
import crypto.interop.AdderKeyManipulator;
import edu.uconn.cse.adder.PublicKey;
import junit.framework.TestCase;
import sexpression.ASExpression;
import sexpression.StringExpression;
import votebox.events.*;

import java.util.ArrayList;
import java.util.Arrays;

public class VoteBoxEventsTest extends TestCase {

    private VoteBoxEventMatcher matcher;

    private IKeyStore keyStore;

    protected void setUp() throws Exception {
        super.setUp();
        matcher = new VoteBoxEventMatcher(ActivatedEvent.getMatcher(),
                AssignLabelEvent.getMatcher(), AuthorizedToCastEvent.getMatcher(),
                BallotReceivedEvent.getMatcher(), CastCommittedBallotEvent.getMatcher(),
                LastPollsOpenEvent.getMatcher(), OverrideCancelConfirmEvent.getMatcher(),
                OverrideCancelDenyEvent.getMatcher(), OverrideCancelEvent.getMatcher(),
                OverrideCommitConfirmEvent.getMatcher(),
                OverrideCastDenyEvent.getMatcher(), OverrideCommitEvent.getMatcher(),
                PollsClosedEvent.getMatcher(), PollsOpenEvent.getMatcher(),
                SupervisorEvent.getMatcher(), VoteBoxEvent.getMatcher(),
                PINEnteredEvent.getMatcher(), InvalidPinEvent.getMatcher());

        keyStore = new SimpleKeyStore("keys");
    }

    public ASExpression getBlob() {
        int n = (int) (Math.random() * 100);
        byte[] array = new byte[n];
        for (int i = 0; i < n; i++)
            array[i] = (byte) (Math.random() * 256);

        return StringExpression.makeString(array);
    }


    public void testActivated() {
        ArrayList<StatusEvent> statuses = new ArrayList<StatusEvent>();
        SupervisorEvent supStatus = new SupervisorEvent(0, 123456, "active");
        VoteBoxEvent vbStatus = new VoteBoxEvent(0, 3, "ready", 75, 20, 30);
        statuses.add(new StatusEvent(0, 50, supStatus));
        statuses.add(new StatusEvent(0, 65, vbStatus));

        ActivatedEvent event = new ActivatedEvent(50, statuses);
        ASExpression sexp = event.toSExp();
        assertEquals(
                "(activated ((status 50 (supervisor 123456 active)) (status 65 (votebox 3 ready 75 20 30))))",
                sexp.toString());

        ActivatedEvent event2 = (ActivatedEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());

        SupervisorEvent supStatus2 = (SupervisorEvent) event2.getStatuses()
                .get(0).getStatus();
        assertEquals(supStatus.getTimestamp(), supStatus2.getTimestamp());
        assertEquals(supStatus.getStatus(), supStatus2.getStatus());

        VoteBoxEvent vbStatus2 = (VoteBoxEvent) event2.getStatuses().get(1).getStatus();
        assertEquals(vbStatus.getStatus(), vbStatus2.getStatus());
        assertEquals(vbStatus.getLabel(), vbStatus2.getLabel());
        assertEquals(vbStatus.getBattery(), vbStatus2.getBattery());
        assertEquals(vbStatus.getPublicCount(), vbStatus2.getPublicCount());
        assertEquals(vbStatus.getProtectedCount(), vbStatus2
                .getProtectedCount());

        event = new ActivatedEvent(50, new ArrayList<StatusEvent>());
        sexp = event.toSExp();
        assertEquals("(activated ())", sexp.toString());

        event2 = (ActivatedEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getStatuses(), event2.getStatuses());
    }

    public void testAssignLabel() {
        AssignLabelEvent event = new AssignLabelEvent(50, 65, 3);
        ASExpression sexp = event.toSExp();
        assertEquals("(assign-label 65 3)", sexp.toString());

        AssignLabelEvent event2 = (AssignLabelEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
        assertEquals(event.getLabel(), event2.getLabel());
    }

    public void testAuthorizedToCast() {
        ASExpression nonce = getBlob();

        byte[] ballot = getBlob().toVerbatim();
        AuthorizedToCastEvent event = new AuthorizedToCastEvent(50, 65, nonce,
                "007", ballot);
        ASExpression sexp = event.toSExp();
        assertEquals("(authorized-to-cast 65 "
                + nonce + " 007 "
                + StringExpression.makeString(ballot).toString() + ")", sexp
                .toString());

        AuthorizedToCastEvent event2 = (AuthorizedToCastEvent) matcher.match(
                50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
        assertTrue(Arrays.equals(event.getBallot(), event2.getBallot()));
    }

    public void testAuthorizedToCastWithNIZKs() {

        PublicKey key = AdderKeyManipulator.generateFinalPublicKey((PublicKey) keyStore.loadAdderKey("public"));

        ASExpression nonce = getBlob();

        byte[] ballot = getBlob().toVerbatim();
        AuthorizedToCastWithNIZKsEvent event = new AuthorizedToCastWithNIZKsEvent(50, 65, nonce,
                "007", ballot, key);
        ASExpression sexp = event.toSExp();
        assertEquals("(authorized-to-cast 65 "
                + nonce + " 007 "
                + StringExpression.makeString(ballot).toString() + ")", sexp
                .toString(), key.toString());

        AuthorizedToCastWithNIZKsEvent event2 = (AuthorizedToCastWithNIZKsEvent) matcher.match(
                50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
        assertTrue(Arrays.equals(event.getBallot(), event2.getBallot()));
        assertEquals(event.getFinalPubKey(), event2.getFinalPubKey());
    }

    public void testBallotPrintFail(){
        ASExpression nonce = getBlob();

        BallotPrintFailEvent event = new BallotPrintFailEvent(0, "123456789", nonce);

        ASExpression sexp = event.toSExp();

        BallotPrintFailEvent event2 = (BallotPrintFailEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getBID(), event2.getBID());
        assertEquals(event.getNonce(), event2.getNonce());
    }

    public void testBallotPrinting(){
        ASExpression nonce = getBlob();

        BallotPrintingEvent event = new BallotPrintingEvent(0, "123456789", nonce);

        ASExpression sexp = event.toSExp();

        BallotPrintingEvent event2 = (BallotPrintingEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getBID(), event2.getBID());
        assertEquals(event.getNonce(), event2.getNonce());
    }

    public void testBallotPrintSuccess(){
        ASExpression nonce = getBlob();

        BallotPrintSuccessEvent event = new BallotPrintSuccessEvent(0, "123456789", nonce);

        ASExpression sexp = event.toSExp();

        BallotPrintSuccessEvent event2 = (BallotPrintSuccessEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getBID(), event2.getBID());
        assertEquals(event.getNonce(), event2.getNonce());
    }

    public void testBallotReceived() {
        ASExpression nonce = getBlob();
        BallotReceivedEvent event = new BallotReceivedEvent(50, 65, nonce, "123", "456");
        ASExpression sexp = event.toSExp();
        assertEquals("(ballot-received 65 "
                + nonce.toString() + " 123 " + "456" + ")", sexp.toString());

        BallotReceivedEvent event2 = (BallotReceivedEvent) matcher.match(50,
                sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
    }

    public void testInvalidPIN() {
        InvalidPinEvent event = new InvalidPinEvent(0, 1);

        InvalidPinEvent event2 = (InvalidPinEvent) matcher.match(0, event.toSExp());

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());

    }

    public void testLastPollsOpen() {
        LastPollsOpenEvent event = new LastPollsOpenEvent(50,
                new PollsOpenEvent(0, 123456, "hi"));
        ASExpression sexp = event.toSExp();
        assertEquals("(last-polls-open (polls-open 123456 hi))", sexp
                .toString());

        LastPollsOpenEvent event2 = (LastPollsOpenEvent) matcher
                .match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getPollsOpenMsg().getTimestamp(), event2
                .getPollsOpenMsg().getTimestamp());
        assertEquals(event.getPollsOpenMsg().getKeyword(), event2
                .getPollsOpenMsg().getKeyword());
    }

    public void testOverrideCancel() {
        ASExpression nonce = getBlob();
        OverrideCancelEvent event = new OverrideCancelEvent(50, 65, nonce);
        ASExpression sexp = event.toSExp();
        assertEquals("(override-cancel 65 "
                + nonce.toString() + ")", sexp.toString());

        OverrideCancelEvent event2 = (OverrideCancelEvent) matcher.match(50,
                sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
    }

    public void testOverrideCast() {
        ASExpression nonce = getBlob();
        OverrideCommitEvent event = new OverrideCommitEvent(50, 65, nonce);
        ASExpression sexp = event.toSExp();
        assertEquals("(override-cast 65 "
                + nonce.toString() + ")", sexp.toString());

        OverrideCommitEvent event2 = (OverrideCommitEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
    }

    public void testOverrideCancelConfirm() {
        ASExpression nonce = getBlob();
        OverrideCancelConfirmEvent event = new OverrideCancelConfirmEvent(50,
                nonce);
        ASExpression sexp = event.toSExp();
        assertEquals("(override-cancel-confirm "
                + nonce.toString() + ")", sexp.toString());

        OverrideCancelConfirmEvent event2 = (OverrideCancelConfirmEvent) matcher
                .match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
    }

    public void testOverrideCancelDeny() {
        ASExpression nonce = getBlob();
        OverrideCancelDenyEvent event = new OverrideCancelDenyEvent(50, nonce);
        ASExpression sexp = event.toSExp();
        assertEquals("(override-cancel-deny "
                + nonce.toString() + ")", sexp.toString());

        OverrideCancelDenyEvent event2 = (OverrideCancelDenyEvent) matcher
                .match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
    }

    public void testOverrideCommitConfirm() {
        ASExpression nonce = getBlob();
        byte[] ballot = getBlob().toVerbatim();
        OverrideCommitConfirmEvent event = new OverrideCommitConfirmEvent(50,
                nonce, ballot);
        ASExpression sexp = event.toSExp();
        assertEquals("(override-commit-confirm "
                + nonce.toString() + " "
                + StringExpression.makeString(ballot).toString() + ")", sexp
                .toString());

        OverrideCommitConfirmEvent event2 = (OverrideCommitConfirmEvent) matcher
                .match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
        assertTrue(Arrays.equals(event.getBallot(), event2.getBallot()));
    }

    public void testOverrideCommitDeny() {
        ASExpression nonce = getBlob();
        OverrideCastDenyEvent event = new OverrideCastDenyEvent(50, nonce);
        ASExpression sexp = event.toSExp();
        assertEquals("(override-commit-deny "
                + nonce.toString() + ")", sexp.toString());

        OverrideCastDenyEvent event2 = (OverrideCastDenyEvent) matcher.match(
                50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
    }

    public void testPINEntered() {
        PINEnteredEvent event = new PINEnteredEvent(0, "12345");

        PINEnteredEvent event2 = (PINEnteredEvent) matcher.match(0, event.toSExp());

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getPin(), event2.getPin());
    }

    public void testPollsClosed() {
        PollsClosedEvent event = new PollsClosedEvent(50, 123456);
        ASExpression sexp = event.toSExp();
        assertEquals("(polls-closed 123456)", sexp.toString());

        PollsClosedEvent event2 = (PollsClosedEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTimestamp(), event2.getTimestamp());
    }

    public void testPollsOpen() {
        PollsOpenEvent event = new PollsOpenEvent(50, 123456, "hi");
        ASExpression sexp = event.toSExp();
        assertEquals("(polls-open 123456 hi)", sexp.toString());

        PollsOpenEvent event2 = (PollsOpenEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTimestamp(), event2.getTimestamp());
        assertEquals(event.getKeyword(), event2.getKeyword());
    }

    public void testSupervisor() {
        SupervisorEvent event = new SupervisorEvent(50, 123456, "active");
        ASExpression sexp = event.toSExp();
        assertEquals("(supervisor 123456 active)", sexp.toString());

        SupervisorEvent event2 = (SupervisorEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTimestamp(), event2.getTimestamp());
        assertEquals(event.getStatus(), event2.getStatus());
    }

    public void testVoteBox() {
        VoteBoxEvent event = new VoteBoxEvent(50, 3, "ready", 75, 20, 30);
        ASExpression sexp = event.toSExp();
        assertEquals("(votebox 3 ready 75 20 30)", sexp.toString());

        VoteBoxEvent event2 = (VoteBoxEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getStatus(), event2.getStatus());
        assertEquals(event.getLabel(), event2.getLabel());
        assertEquals(event.getBattery(), event2.getBattery());
        assertEquals(event.getPublicCount(), event2.getPublicCount());
        assertEquals(event.getProtectedCount(), event2.getProtectedCount());
    }





}
