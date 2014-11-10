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
import crypto.adder.PublicKey;
import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;
import sexpression.ASExpression;
import sexpression.StringExpression;
import sexpression.stream.InvalidVerbatimStreamException;
import votebox.events.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
                OverrideCommitDenyEvent.getMatcher(), OverrideCommitEvent.getMatcher(),
                PollsClosedEvent.getMatcher(), PollsOpenEvent.getMatcher(),
                SupervisorEvent.getMatcher(), VoteBoxEvent.getMatcher(),
                PINEnteredEvent.getMatcher(), InvalidPinEvent.getMatcher(),
                BallotScanAcceptedEvent.getMatcher(), BallotScannedEvent.getMatcher(),
                BallotScanRejectedEvent.getMatcher(), BallotPrintFailEvent.getMatcher(),
                BallotPrintingEvent.getMatcher(), BallotPrintSuccessEvent.getMatcher(),
                BallotScannerEvent.getMatcher(), AuthorizedToCastWithNIZKsEvent.getMatcher(),
                CommitBallotEvent.getMatcher(), EncryptedCastBallotEvent.getMatcher(),
                EncryptedCastBallotWithNIZKsEvent.getMatcher(), PollMachinesEvent.getMatcher(),
                PollsOpenQEvent.getMatcher(), PollStatusEvent.getMatcher(),
                ProvisionalAuthorizeEvent.getMatcher(), ProvisionalCommitEvent.getMatcher(),
                ProvisionalCommitEvent.getMatcher(),
                SpoilBallotEvent.getMatcher(), StartScannerEvent.getMatcher(),
                StatusEvent.getMatcher(), TapMachineEvent.getMatcher(), ProvisionalBallotEvent.getMatcher(),
                CompletedUploadEvent.getMatcher(), StartUploadEvent.getMatcher(),
                BallotUploadEvent.getMatcher());

        keyStore = new SimpleKeyStore("keys");
    }

    public ASExpression getBlob() {
        int n = (int) (Math.random() * 100);
        byte[] array = new byte[n];
        for (int i = 0; i < n; i++)
            array[i] = (byte) (Math.random() * 256);

        return StringExpression.makeString(array);
    }

    public void checkBallotEvent(ABallotEvent event, ABallotEvent event2){
        assertEquals(event.getSerial(), event2.getSerial());

        if(event.getBID() != null)
            assertEquals(event.getBID(), event2.getBID());

        if(event.getNonce() != null)
            assertEquals(event.getNonce(), event2.getNonce());

        if(event.getBallot() != null)
            assertTrue(Arrays.equals(event.getBallot(), event2.getBallot()));

        if(event.getPrecinct() != null)
            assertEquals(event.getPrecinct(), event2.getPrecinct());
    }

    public void testActivated() {
        ArrayList<StatusEvent> statuses = new ArrayList<>();
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

        checkBallotEvent(event, event2);
    }

    public void testAuthorizedToCastWithNIZKs() {

        PublicKey key = AdderKeyManipulator.generateFinalPublicKey((PublicKey) keyStore.loadAdderPublicKey());

        ASExpression nonce = getBlob();

        byte[] ballot = getBlob().toVerbatim();
        AuthorizedToCastWithNIZKsEvent event = new AuthorizedToCastWithNIZKsEvent(50, 65, nonce,
                "007", ballot, key);
        ASExpression sexp = event.toSExp();
        assertEquals("(authorized-to-cast-with-nizks 65 "
                + nonce.toString() + " "
                + StringExpression.makeString(ballot).toString() + " 007 " + key.toASE() + ")", sexp
                .toString());

        AuthorizedToCastWithNIZKsEvent event2 = (AuthorizedToCastWithNIZKsEvent) matcher.match(
                50, sexp);

        checkBallotEvent(event, event2);

        assertEquals(event.getFinalPubKey(), event2.getFinalPubKey());
    }

    public void testBallotPrintFail(){
        ASExpression nonce = getBlob();

        BallotPrintFailEvent event = new BallotPrintFailEvent(0, "123456789", nonce);

        ASExpression sexp = event.toSExp();

        assertEquals("(ballot-print-fail 123456789 " + nonce +")", sexp.toString());

        BallotPrintFailEvent event2 = (BallotPrintFailEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }

    public void testBallotPrinting(){
        ASExpression nonce = getBlob();

        BallotPrintingEvent event = new BallotPrintingEvent(0, "123456789", nonce);

        ASExpression sexp = event.toSExp();

        assertEquals("(ballot-printing 123456789 " + nonce +")", sexp.toString());

        BallotPrintingEvent event2 = (BallotPrintingEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }

    public void testBallotPrintSuccess(){
        ASExpression nonce = getBlob();

        BallotPrintSuccessEvent event = new BallotPrintSuccessEvent(0, "123456789", nonce);

        ASExpression sexp = event.toSExp();

        assertEquals("(ballot-print-success 123456789 " + nonce + ")", sexp.toString());

        BallotPrintSuccessEvent event2 = (BallotPrintSuccessEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }

    public void testBallotReceived(){
        ASExpression nonce = getBlob();
        BallotReceivedEvent event = new BallotReceivedEvent(50, 65, nonce, "123", "456");
        ASExpression sexp = event.toSExp();
        assertEquals("(ballot-received 65 "
                + nonce.toString() + " 123 " + "456" + ")", sexp.toString());

        BallotReceivedEvent event2 = (BallotReceivedEvent) matcher.match(50,
                sexp);

        checkBallotEvent(event, event2);
    }

    public void testBallotScanAccepted(){
        BallotScanAcceptedEvent event = new BallotScanAcceptedEvent(0, "123456789");

        ASExpression sexp = event.toSExp();

        assertEquals("(ballot-accepted 123456789)", sexp.toString());

        BallotScanAcceptedEvent event2 = (BallotScanAcceptedEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getBID(), event2.getBID());
    }
    
    public void testBallotScanned(){
        BallotScannedEvent event = new BallotScannedEvent(0, "123456789");

        ASExpression sexp = event.toSExp();

        assertEquals("(ballot-scanned 123456789)", sexp.toString());

        BallotScannedEvent event2 = (BallotScannedEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }
    
    public void testBallotScanner(){
        BallotScannerEvent event = new BallotScannerEvent(50, 3, "ready", 75, 20, 30);
        ASExpression sexp = event.toSExp();
        assertEquals("(ballotscanner 3 ready 75 20 30)", sexp.toString());

        BallotScannerEvent event2 = (BallotScannerEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getStatus(), event2.getStatus());
        assertEquals(event.getLabel(), event2.getLabel());
        assertEquals(event.getBattery(), event2.getBattery());
        assertEquals(event.getPublicCount(), event2.getPublicCount());
        assertEquals(event.getProtectedCount(), event2.getProtectedCount());
    }

    public void testBallotScanRejected(){
        BallotScanRejectedEvent event = new BallotScanRejectedEvent(0, "123456789");

        ASExpression sexp = event.toSExp();

        assertEquals("(ballot-rejected 123456789)", sexp.toString());

        BallotScanRejectedEvent event2 = (BallotScanRejectedEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getBID(), event2.getBID());
    }

    public void testBallotUpload() throws IOException, ClassNotFoundException {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("eight", 8);
        map.put("six", 6);
        map.put("seven", 7);
        map.put("five", 5);
        map.put("three", 3);
        map.put("zero", 0);
        map.put("nine", 9);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(map);
        objectOutputStream.close();

        String encoded = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));

        BallotUploadEvent event = new BallotUploadEvent(0, map);

        ASExpression sexp = event.toSExp();

        assertEquals("(ballot-upload " + encoded + ")", sexp.toString());





        BallotUploadEvent event2 = (BallotUploadEvent)matcher.match(0, sexp);



        assertEquals(event.getSerial(), event2.getSerial());

        //Deserialize the map to check that it worked properly
        String data = event2.getMap().toString();

        byte[] bytes = Base64.decodeBase64(data.getBytes());
        HashMap<String, Integer> record = null;

        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        //noinspection unchecked
        record = (HashMap<String, Integer>)objectInputStream.readObject();


        assertEquals(event.getMap(), record);
    }

    public void testCastBallotUpload(){
        /*List<ASExpression> noncesList = new ArrayList<ASExpression>();

        /* We generated 4 random nonces for checking the message
        for(int i = 0; i < 4; i++)
            noncesList.add(getBlob());

        ListExpression nonces = new ListExpression(noncesList);

        CastBallotUploadEvent event = new CastBallotUploadEvent(0, nonces);

        ASExpression sexp = event.toSExp();

        CastBallotUploadEvent event2 = (CastBallotUploadEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
        assertTrue(event.getDumpList().equals(event2.getDumpList()));
        */

        /* TODO Write this case when the upload events are better written */
    }

    public void testCastCommittedBallot(){
        ASExpression nonce = getBlob();

        CastCommittedBallotEvent event = new CastCommittedBallotEvent(0, nonce, "123456789", 0);

        ASExpression sexp = event.toSExp();

        assertEquals("(cast-ballot " + nonce + " 123456789)", sexp.toString());

        CastCommittedBallotEvent event2 = (CastCommittedBallotEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);

    }

    public void testChallengedBallotUpload(){
        /* TODO When this method makes more sense, write a test case */
    }

    public void testCommitBallot(){
        ASExpression nonce = getBlob();
        byte[] ballot = getBlob().toVerbatim();

        CommitBallotEvent event = new CommitBallotEvent(0, nonce, ballot, "123", "456");

        ASExpression sexp = event.toSExp();


        assertEquals("(commit-ballot " +
                nonce +  " " +
                StringExpression.makeString(ballot) + " 123 456)", event.toSExp().toString());

        CommitBallotEvent event2 = (CommitBallotEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }

    public void testCompletedUpload() {
        CompletedUploadEvent event = new CompletedUploadEvent(0);

        ASExpression sexp = event.toSExp();

        CompletedUploadEvent event2 = (CompletedUploadEvent) matcher.match(0, sexp);

        System.out.println();
        System.out.println(sexp);
        System.out.println(event2.toSExp());

        assertEquals("(completed-upload)", sexp.toString());

        assertEquals(event.getSerial(), event2.getSerial());
    }

    public void testEncryptedCastBallotEvent(){
        ASExpression nonce = getBlob();
        byte[] ballot = getBlob().toVerbatim();

        EncryptedCastBallotEvent event = new EncryptedCastBallotEvent(0, nonce, ballot, "123456789", 0);

        ASExpression sexp = event.toSExp();

        assertEquals("(encrypted-cast-ballot " +
                nonce + " " +
                StringExpression.makeString(ballot) +
                " 123456789)", sexp.toString());

        EncryptedCastBallotEvent event2 = (EncryptedCastBallotEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }

    public void testEncryptedCastBallotWithNIZKs(){
        ASExpression nonce = getBlob();
        byte[] ballot = getBlob().toVerbatim();

        EncryptedCastBallotWithNIZKsEvent event = new EncryptedCastBallotWithNIZKsEvent(0, nonce, ballot, "123456789", 0);

        ASExpression sexp = event.toSExp();

        assertEquals("(encrypted-cast-ballot-with-nizks " +
                nonce + " " +
                StringExpression.makeString(ballot) +
                " 123456789)", sexp.toString());

        EncryptedCastBallotWithNIZKsEvent event2 = (EncryptedCastBallotWithNIZKsEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }

    public void testInvalidPIN() {
        InvalidPinEvent event = new InvalidPinEvent(0, 1);

        ASExpression sexp = event.toSExp();

        assertEquals("(invalid-pin 1)", sexp.toString());

        InvalidPinEvent event2 = (InvalidPinEvent) matcher.match(0, sexp);


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
        OverrideCommitDenyEvent event = new OverrideCommitDenyEvent(50, nonce);
        ASExpression sexp = event.toSExp();
        assertEquals("(override-commit-deny "
                + nonce.toString() + ")", sexp.toString());

        OverrideCommitDenyEvent event2 = (OverrideCommitDenyEvent) matcher.match(
                50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertTrue(Arrays.equals(event.getNonce().toVerbatim(), event2.getNonce().toVerbatim()));
    }

    public void testOverrideCommit() {
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

    public void testPINEntered() {
        PINEnteredEvent event = new PINEnteredEvent(0, "12345");

        ASExpression sexp = event.toSExp();

        PINEnteredEvent event2 = (PINEnteredEvent) matcher.match(0, sexp);

        assertEquals("(pin-entered 12345)", sexp.toString());

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getPin(), event2.getPin());
    }

    public void testPollMachines(){
        long curTime = System.currentTimeMillis();
        PollMachinesEvent event = new PollMachinesEvent(0, curTime, "keyword");

        ASExpression sexp = event.toSExp();

        assertEquals("(poll-machines " + curTime + " keyword)", sexp.toString());

        PollMachinesEvent event2 = (PollMachinesEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getKeyword(), event2.getKeyword());
        assertEquals(event.getTimestamp(), event2.getTimestamp());
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

    public void testPollsOpenQ(){
        PollsOpenQEvent event = new PollsOpenQEvent(50, "hi");
        ASExpression sexp = event.toSExp();
        assertEquals("(polls-open? hi)", sexp.toString());

        PollsOpenQEvent event2 = (PollsOpenQEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getKeyword(), event2.getKeyword());
    }

    public void testPollStatus(){
        PollStatusEvent event = new PollStatusEvent(50, 65, 1);
        ASExpression sexp = event.toSExp();
        assertEquals("(poll-status 65 1)", sexp.toString());

        PollStatusEvent event2 = (PollStatusEvent) matcher.match(50, sexp);
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getPollStatus(), event2.getPollStatus());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
    }

    public void testProvisionalAuthorize(){
        ASExpression nonce = getBlob();

        byte[] ballot = getBlob().toVerbatim();

        ProvisionalAuthorizeEvent event = new ProvisionalAuthorizeEvent(50, 65, nonce, ballot);
        ASExpression sexp = event.toSExp();
        assertEquals("(provisional-authorized-to-cast 65 "
                + nonce + " "
                + StringExpression.makeString(ballot).toString() + ")", sexp
                .toString());

        ProvisionalAuthorizeEvent event2 = (ProvisionalAuthorizeEvent) matcher.match(
                50, sexp);

        checkBallotEvent(event, event2);
    }

    public void testProvisionalBallot(){
        ASExpression nonce = getBlob();
        ProvisionalBallotEvent event = new ProvisionalBallotEvent(50, nonce, "123456789");
        ASExpression sexp = event.toSExp();
        assertEquals("(provisional-ballot "
                + nonce.toString() + " 123456789)", sexp.toString());

        ProvisionalBallotEvent event2 = (ProvisionalBallotEvent) matcher.match(50,
                sexp);

        checkBallotEvent(event, event2);
    }

    public void testProvisionalCommit(){
        ASExpression nonce = getBlob();
        byte[] ballot = getBlob().toVerbatim();

        ProvisionalCommitEvent event = new ProvisionalCommitEvent(0, nonce, ballot, "123");

        ASExpression sexp = event.toSExp();


        assertEquals("(commit-provisional-ballot " +
                nonce + " " +
                StringExpression.makeString(ballot) + " 123)", event.toSExp().toString());

        ProvisionalCommitEvent event2 = (ProvisionalCommitEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);
    }

    public void testSpoilBallot(){
        ASExpression nonce = getBlob();

        byte[] ballot = getBlob().toVerbatim();

        SpoilBallotEvent event = new SpoilBallotEvent(0, nonce, "123456789", ballot);

        ASExpression sexp = event.toSExp();

        try {
            assertEquals("(spoil-ballot " + nonce + " 123456789 " + ASExpression.makeVerbatim(ballot) + ")", sexp.toString());
        } catch (InvalidVerbatimStreamException e) {
            e.printStackTrace();
        }

        SpoilBallotEvent event2 = (SpoilBallotEvent)matcher.match(0, sexp);

        checkBallotEvent(event, event2);

    }

    public void testStartScanner(){
        StartScannerEvent event = new StartScannerEvent(0);

        ASExpression sexp = event.toSExp();

        assertEquals("(start-scanner)", sexp.toString());

        StartScannerEvent event2 = (StartScannerEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
    }

    public void testStartUpload() {
        StartUploadEvent event = new StartUploadEvent(0);

        ASExpression sexp = event.toSExp();

        StartUploadEvent event2 = (StartUploadEvent) matcher.match(0, sexp);

        assertEquals("(start-upload)", sexp.toString());

        assertEquals(event.getSerial(), event2.getSerial());
    }

    public void testStatus(){
        StatusEvent event = new StatusEvent(0, 65, new VoteBoxEvent(0, 1, "active", 100, 0, 0));

        ASExpression sexp = event.toSExp();

        assertEquals("(status 65 (votebox 1 active 100 0 0))", sexp.toString());
        
        StatusEvent event2 = (StatusEvent)matcher.match(0, sexp);
        
        assertEquals(event.getSerial(), event2.getSerial());
        assertEquals(event.getTargetSerial(), event2.getTargetSerial());
        
        VoteBoxEvent subEvent = (VoteBoxEvent)event.getStatus();
        VoteBoxEvent subEvent2 = (VoteBoxEvent)event2.getStatus();

        assertEquals(subEvent.getSerial(), subEvent2.getSerial());
        assertEquals(subEvent.getStatus(), subEvent2.getStatus());
        assertEquals(subEvent.getLabel(), subEvent2.getLabel());
        assertEquals(subEvent.getBattery(), subEvent2.getBattery());
        assertEquals(subEvent.getPublicCount(), subEvent2.getPublicCount());
        assertEquals(subEvent.getProtectedCount(), subEvent2.getProtectedCount());
        
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

    public void testTapMachine(){
        TapMachineEvent event = new TapMachineEvent(0);

        ASExpression sexp = event.toSExp();

        assertEquals("(tap-machine)", sexp.toString());

        TapMachineEvent event2 = (TapMachineEvent)matcher.match(0, sexp);

        assertEquals(event.getSerial(), event2.getSerial());
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
