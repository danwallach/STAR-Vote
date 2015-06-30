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

package sim.autobooth;

import auditorium.NetworkException;
import sexpression.StringExpression;
import sim.utils.ArgParse;
import sim.utils.Time;
import votebox.AuditoriumParams;
import votebox.events.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;

public class Booth {
    public static final String OPT_ID = "id";
    public static final String OPT_VOTE_MIN_TIME = "vote-min-time";
    public static final String OPT_VOTE_MAX_TIME = "vote-max-time";
    public static final String OPT_AUDITORIUM_PARAMS_FILE = "vb.conf";

    private VoteBoxAuditoriumConnector auditorium;
    private Timer statusTimer;

    private AuditoriumParams auditoriumParams;
    private int serial = -1;
    private int label = -1;

    private boolean voting = false;
    private int protectedCount = 0;
    private int publicCount = 0;

    private byte[] ballotNonce;

    private int voteMinTime;
    private int voteMaxTime;

    public Booth(HashMap<String, Object> opts) {
        serial = new Integer(opts.get(OPT_ID).toString());
        voteMinTime = new Integer(opts.get(OPT_VOTE_MIN_TIME).toString());
        voteMaxTime = new Integer(opts.get(OPT_VOTE_MAX_TIME).toString());
        auditoriumParams = new AuditoriumParams(
        		opts.get(OPT_AUDITORIUM_PARAMS_FILE).toString());
    }

    public void reset() {
        voting = false;
        ballotNonce = null;

        statusTimer = new Timer(30 * Time.MINUTES, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                auditorium.announce(makeStatusEvent());
            }
        });
    }

    public VoteBoxEvent makeStatusEvent() {
        int _battery = 42;
        return new VoteBoxEvent(serial, label,
                (voting ? "in-use" : "ready"),
                _battery, protectedCount, publicCount);
    }

    public void broadcastStatus() {
        auditorium.announce(makeStatusEvent());
        statusTimer.restart();
    }

    public void doCastBallot() {
        System.out.println("Casting ballot.");

        auditorium.announce(new CommitBallotEvent(serial, StringExpression
                .makeString(ballotNonce), StringExpression.makeString("Cast ballot goes here").toVerbatim(), "bid", "precinct"));


        auditorium.announce(new CastCommittedBallotEvent(serial, StringExpression
                .makeString(ballotNonce), "bid", 0));
        protectedCount++;
        publicCount++;
        voting = false;
    }

    public void start() {
        reset();

        try {
            auditorium = new VoteBoxAuditoriumConnector(serial,
                    auditoriumParams, "0000000000", ActivatedEvent.getMatcher(), AssignLabelEvent.getMatcher(),
                    AuthorizedToCastEvent.getMatcher(), BallotReceivedEvent.getMatcher(),
                    OverrideCancelEvent.getMatcher(), OverrideCommitEvent.getMatcher(),
                    PollsOpenQEvent.getMatcher(),
                    PINEnteredEvent.getMatcher(), InvalidPinEvent.getMatcher(),
                    PollsOpenEvent.getMatcher(), PollStatusEvent.getMatcher(),
                    BallotPrintingEvent.getMatcher(), BallotPrintSuccessEvent.getMatcher(),
                    BallotPrintFailEvent.getMatcher(), PollMachinesEvent.getMatcher(),
                    ProvisionalAuthorizeEvent.getMatcher());
        } catch (NetworkException e1) {
            System.err.println("Error while initializing Auditorium:");
            e1.printStackTrace();
            System.exit(-1);
        }

        auditorium.addListener(new VoteBoxEventListener() {
            // Much of this is cloned from votebox/VoteBox.java
            // [dsandler 09/23/2007]

            /**
             * Handler for the activated message. Look to see if this VoteBox's
             * status exists (and is correct), and if not, broadcast its status
             */
            public void activated(ActivatedEvent e) {
                System.out.println("*** activated: " + e.toString());

                boolean found = false;
                for (StatusEvent ae : e.getStatuses()) {
                    if (ae.getSerial() == serial) {
                        VoteBoxEvent ve = (VoteBoxEvent) ae.getStatus();
                        VoteBoxEvent status = makeStatusEvent();
                        if (!ve.getStatus().equals(status.getStatus())
                                || ve.getBattery() != status.getBattery()
                                || ve.getLabel() != status.getLabel()
                                || ve.getProtectedCount() != status
                                .getProtectedCount()
                                || ve.getPublicCount() != status
                                .getPublicCount())
                            broadcastStatus();
                        found = true;
                    }
                }
                if (!found)
                    broadcastStatus();
            }

            /**
             * Handler for the assign-label message. If it is referring to this
             * booth, set the label.
             */
            public void assignLabel(AssignLabelEvent e) {
                System.out.println("*** assign-label: " + e.toString());

                if (e.getSerial() == serial)
                    label = e.getLabel();
            }

            /**
             * Handler for the authorized-to-cast message. If it is for this
             * booth, and it is not already voting, unzip the ballot and fire
             * the VoteBox runtime. Also announce the new status.
             */
            public void authorizedToCast(AuthorizedToCastEvent e) {
                System.out.println("*** authorized-to-cast: " + e.toString());

                if (e.getSerial() == serial) {
                    System.out.println("*** it's for me!");
                    ballotNonce = e.getNonce().toVerbatim();
                    voting = true;
                    broadcastStatus();
                    int voteTime = voteMinTime
                            + (int) (Math.random() * (voteMaxTime - voteMinTime));
                    System.out.println("*** will send cast ballot in "
                            + voteTime + " ms.");
                    Timer t = new Timer(voteTime,
                            new ActionListener() {
                                public void actionPerformed(ActionEvent foo) {
                                    doCastBallot();
                                }
                            });
                    t.setRepeats(false);
                    t.start();
                }
            }

            public void pollsClosed(PollsClosedEvent e) {
                System.out.println("*** polls-closed. Shutting down in 15 seconds...");

                Timer t = new Timer(15 * Time.SECONDS,
                        new ActionListener() {
                            public void actionPerformed(ActionEvent foo) {
                                try {
                                    Thread.sleep(15 * Time.SECONDS);
                                } catch (InterruptedException ignored) {
                                }
                                System.out.println("Exiting.");
                                System.exit(0);
                            }
                        });
                t.setRepeats(false);
                t.start();
            }

            public void pollsOpen(PollsOpenEvent e) {
                System.out.println("*** polls-open: " + e.toString());
            }

            public void ballotReceived(BallotReceivedEvent e) {
            }

            @Override
            public void castCommittedBallot(CastCommittedBallotEvent e) {

            }


            public void joined(JoinEvent e) {
            }

            public void lastPollsOpen(LastPollsOpenEvent e) {
            }

            public void left(LeaveEvent e) {
            }

            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {
            }

            public void overrideCancelDeny(OverrideCancelDenyEvent e) {
            }

            @Override
            public void overrideCommit(OverrideCommitEvent e) {

            }

            public void uploadBallots(BallotUploadEvent ballotUploadEvent) {}

            @Override
            public void overrideCommitConfirm(OverrideCommitConfirmEvent e) {
                System.out.println("*** override-cast: " + e.toString());

                if (voting && serial == e.getSerial()
                        && Arrays.equals(e.getNonce().toVerbatim(), ballotNonce)) {
                    // pass
                }

            }

            @Override
            public void overrideCommitDeny(OverrideCommitDenyEvent e) {

            }

            public void pollsOpenQ(PollsOpenQEvent e) {
            }

            public void supervisor(SupervisorEvent e) {
            }

            public void votebox(VoteBoxEvent e) {
            }

            @Override
            public void ballotScanner(BallotScannerEvent e) {

            }

            @Override
            public void ballotScanned(BallotScannedEvent e) {

            }

            @Override
            public void pinEntered(PINEnteredEvent event) {

            }

            @Override
            public void invalidPin(InvalidPinEvent event) {

            }

            @Override
            public void pollStatus(PollStatusEvent pollStatusEvent) {

            }

            @Override
            public void ballotAccepted(BallotScanAcceptedEvent e) {

            }

            @Override
            public void ballotRejected(BallotScanRejectedEvent e) {

            }

            @Override
            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {

            }

            @Override
            public void ballotPrintSuccess(BallotPrintSuccessEvent ballotPrintSuccessEvent) {

            }

            @Override
            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {

            }

            @Override
            public void scannerStart(StartScannerEvent startScannerEvent) {

            }

            @Override
            public void pollMachines(PollMachinesEvent pollMachinesEvent) {

            }

            @Override
            public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {

            }

            @Override
            public void announceProvisionalBallot(ProvisionalBallotEvent provisionalBallotEvent) {

            }

            @Override
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent provisionalAuthorizeEvent) {

            }

            @Override
            public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent) {

            }

            @Override
            public void tapMachine(TapMachineEvent tapMachineEvent) {

            }

            @Override
            public void startUpload(StartUploadEvent startUploadEvent) {

            }

            @Override
            public void completedUpload(CompletedUploadEvent completedUploadEvent) {

            }

            public void overrideCancel(OverrideCancelEvent e) {
                System.out.println("*** override-cancel: " + e.toString());

                if (voting
                        && serial == e.getSerial()
                        && Arrays.equals(e.getNonce().toVerbatim(), ballotNonce)) {
                    // pass
                }
            }

            public void commitBallot(CommitBallotEvent e) {
                // NO-OP

            }

        });

        try {
            System.out.println(">>> connecting to Auditorium...");

            auditorium.connect();
            auditorium.announce(makeStatusEvent());
        } catch (NetworkException e1) {
        	//NetworkException represents a recoverable error
        	//  so just note it and continue
            System.out.println("Recoverable error occured: "+e1.getMessage());
            e1.printStackTrace(System.err);
        }

        statusTimer.start();
    }

    public static void main(String[] args) {
        HashMap<String, Object> opts = new HashMap<String, Object>();
        opts.put(OPT_AUDITORIUM_PARAMS_FILE, "vb.conf");
        opts.put(OPT_VOTE_MIN_TIME, 3 * Time.MINUTES);
        opts.put(OPT_VOTE_MAX_TIME, 15 * Time.MINUTES);

        ArgParse.addArgsToMap(args, opts);

        if (!opts.containsKey("id")) {
            System.err.println("error: node id not set; use id=X on command line");
            System.exit(-1);
        }

        new Booth(opts).start();
    }
}
