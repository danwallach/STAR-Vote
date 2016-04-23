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

package votebox;

import auditorium.AuditoriumCryptoException;
import auditorium.Bugout;
import auditorium.Event;
import auditorium.NetworkException;
import crypto.*;
import crypto.adder.AdderInteger;
import printer.Printer;
import sexpression.ASEConverter;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NoMatch;
import sexpression.stream.InvalidVerbatimStreamException;
import supervisor.model.Ballot;
import votebox.events.*;
import votebox.middle.IncorrectTypeException;
import votebox.middle.driver.Driver;

import javax.swing.*;
import javax.swing.Timer;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * This is the top level votebox main class. This class organizes and connects
 * the components of VoteBox, namely:<br>
 * 1) The auditorium network<br>
 * 2) The vote storage backend<br>
 * 3) The votebox "middle" (that is, the link between the voter and the backend,
 * the gui)
 *
 * @author derrley, Corey Shaw
 */
public class VoteBox{

    private final AuditoriumParams _constants;
    private VoteBoxAuditoriumConnector auditorium;
    private Driver voteboxuiDriver;
    private IVoteBoxInactiveUI inactiveUI;
    private final int mySerial;
    private boolean connected;
    private boolean voting;
    private boolean override;
    private boolean committedBallot;
    private boolean finishedVoting;
    private boolean isProvisional;
    private int label;
    private String bid;
    private Event<Integer> labelChangedEvent;
    private int protectedCount;
    private int publicCount;
    private int numConnections;
    private ASExpression nonce;
    private int pageBeforeOverride;
    private Timer killVBTimer;
    private Timer statusTimer;
    boolean superOnline;
    private int superSerial;
    private String precinct;
    private final String launchCode;
    private BallotCrypter<ExponentialElGamalCiphertext> ballotCrypter;

    /** Will keep the short code - nonce pairings to send over when the polls close */
    private HashMap<ASExpression, VotePair> plaintextAuditCommits;

    private  Printer printer;
    private Random rand;

    /**
     * Default constructor: Equivalent to new VoteBox(-1) which sets a default serial of -1
     */
    public VoteBox(){
    	this(-1, "0000000000");
    }

    /**
     * Constructs a new instance of a persistent VoteBox booth. This
     * implementation runs in the background, on an auditorium network, and
     * waits to receive an authorization before launching the VoteBox middle.
     * For a standalone implementation, see
     *
     * @param serial the serial number of the votebox
     */
    public VoteBox(int serial, String launchCode) {

        rand = new Random(System.currentTimeMillis());
        _constants = new AuditoriumParams("vb.conf");
        this.launchCode = launchCode;

        /* If serial is not a good value, set mySerial to the default */
        mySerial = (serial != -1) ? serial : _constants.getDefaultSerialNumber();

        /* If mySerial ends up with a bad number anyway, throw an error */
        if(mySerial == -1)
        	throw new RuntimeException("usage: VoteBox <machineID>");

        numConnections = 0;
        labelChangedEvent = new Event<>();

        /* Announce to auditorium the status */
        statusTimer = new Timer(300000, e -> {
            if (connected)
                auditorium.announce(getStatus());
        });

        /* Destroys any previous commits */
        plaintextAuditCommits = new HashMap<>();

        killVBTimer = new Timer(_constants.getViewRestartTimeout(), arg0 -> {

            voteboxuiDriver.kill();
            voteboxuiDriver = null;

                            /* Show inactive screen */
            inactiveUI.setVisible(true);

            killVBTimer.stop();
        });

        killVBTimer.setRepeats(false);

    }

    /**
     * Broadcasts this VoteBox booth's status, and resets the status timer
     */
    public void broadcastStatus() {
        auditorium.announce(getStatus());
        statusTimer.restart();
    }

    /**
     * Returns this booth's label, assigned by a supervisor over auditorium. If
     * unassigned, will return 0.
     *
     * @return the label
     */
    public int getLabel() {
        return label;
    }

    /**
     * Returns this booth's status as a VoteBoxEvent, used for periodic broadcasts
     *
     * @return the status as a @VoteBoxEvent
     */
    public VoteBoxEvent getStatus() {

        int battery = BatteryStatus.read(_constants.getOS());

        /* Create the status */
        String status = voting ? ( isProvisional ? "provisional-in-use" : "in-use" ) : "ready";

        System.out.println("Protected count: " + protectedCount + " Public count: " + publicCount);

        /* Create the event corresponding to the status */
        return new VoteBoxEvent(mySerial, label, status, battery, protectedCount, publicCount);
    }

    /**
     * Allows the VoteBox inactive UI (what is shown when a user isn't voting)
     * to register for a label changed event, and update itself accordingly
     *
     * @param obs the observer
     */
    public void registerForLabelChanged(Observer obs) {
        labelChangedEvent.addObserver(obs);
    }

    public void launchChromeUI() {
        voteboxuiDriver = new Driver();
        voteboxuiDriver.launchView();
    }

    /**
     * Launch the VoteBox middle. Registers for events that we would want to
     * know about (such as cast ballot, so we can send the message over
     * auditorium)
     *
     * @param location the location on disk of the ballot
     */
    public void run(String location) {

        /* This driver will need to take messages from the new ui */
        voteboxuiDriver.loadPath(location);
        voting = true;
        voteboxuiDriver.run();

        /* Listen for commit UI events.  When received, send out an encrypted vote. */
        voteboxuiDriver.getView().registerForCommit((o, argTemp) -> {

            if (!connected)
                throw new RuntimeException("Attempted to cast ballot when not connected to any machines");

            if (!voting || voteboxuiDriver == null)
                throw new RuntimeException("VoteBox attempted to cast ballot, but was not currently voting");

            if (finishedVoting)
                throw new RuntimeException("This machine has already finished voting, but attempted to vote again");

            committedBallot = true;

            /* Can we just get Ballot<Plaintext> type objects? */
            Object[] arg = (Object[]) argTemp;

            /* arg0 should be the cast ballot structure, check. TODO Fix handling */
            //if (RuntimeBallot.BALLOT_PATTERN.match((ASExpression) arg[0]) == NoMatch.SINGLETON)
            //    throw new RuntimeException("Incorrectly expected a cast-ballot");

            /* Convert Ballot from ASE to Ballot object TODO check if this is right, should be able to do something similar */
            List<PlaintextRaceSelection> ballotForm = ASEConverter.convertFromASE((ListExpression)arg[0]);

            Ballot<PlaintextRaceSelection> ballot = new Ballot<>(bid, ballotForm, nonce.toString());

            /* Encrypt Ballot */
            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> encBallot;

            try { encBallot = ballotCrypter.encrypt(ballot); }
            catch (Exception e) { e.printStackTrace(); throw new RuntimeException("Could not encrypt the ballot because of " + e.getClass()); }


            /* Check if provisional and choose announcement format */
            if (!isProvisional) {

                auditorium.announce(new CommitBallotEvent(mySerial, nonce, ASEConverter.convertToASE(encBallot).toVerbatim(), bid, precinct));

            }

            /* Provisional */
            else {
                auditorium.announce(new ProvisionalCommitEvent(mySerial, nonce, ASEConverter.convertToASE(encBallot).toVerbatim(), bid));
            }

            /* Announce ballot printing and print */
            List<List<String>> races = voteboxuiDriver.getView().getRaceGroups();
            auditorium.announce(new BallotPrintingEvent(mySerial, bid, nonce));

            /* TODO Printer needs to be refactored to expect the ballot selections, not a path to prerendered images */
            printer = new Printer(null, races);

            boolean success = printer.printCommittedBallot(ballot.getRaceSelections(), bid);
            printer.printedReceipt(bid);

            /* By this time, the voter is done voting */
            /* Wait before returning to inactive */

            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < 5000) ;

            finishedVoting = true;

            System.out.println("\nBID: " + bid + "\n");

            if (success)
                auditorium.announce(new BallotPrintSuccessEvent(mySerial, bid, nonce));

        });

        voteboxuiDriver.getView().registerForOverrideCancelConfirm((o, arg) -> {

            if (voting && override && !finishedVoting && voteboxuiDriver != null) {

                /* Announce the override */
                auditorium.announce(new OverrideCancelConfirmEvent(mySerial, nonce));

                /* TODO In Driver, kill the UI process */
                /* Kills the voting session */
                voteboxuiDriver.kill();
                voteboxuiDriver = null;
                nonce = null;
                voting = false;
                override = false;

                /* Broadcast the new status */
                broadcastStatus();

                /* TODO simply call Driver to restart voting process */
                //promptForPin("Enter Voting Authentication PIN");

            }
            else { /* TODO runtime exception */
                throw new RuntimeException("Received an override-cancel-confirm event at the incorrect time");
            }
        });

        voteboxuiDriver.getView().registerForOverrideCancelDeny((o, arg) -> {

            if (voting && override && !finishedVoting && voteboxuiDriver != null) {

                /* Announce the denial of the override and go back */
                auditorium.announce(new OverrideCancelDenyEvent(mySerial, nonce));
                override = false;
            }
            else { /* TODO runtime exception */
                throw new RuntimeException("Received an override-cancel-deny event at the incorrect time");
            }
        });

        voteboxuiDriver.getView().registerForOverrideCommitConfirm((o, argTemp) -> {

            /* Check to see if voting is still in progress after the override commit selection */
            if (voting && override && !finishedVoting && voteboxuiDriver != null) {


                Object[] arg = (Object[]) argTemp;

                /* arg1 should be the cast ballot structure, check  TODO */
                //if (RuntimeBallot.BALLOT_PATTERN.match((ASExpression) arg[0]) == NoMatch.SINGLETON)
                //    throw new RuntimeException("Incorrectly expected a cast-ballot");

                //TODO use this if logging fails and requires ASE -- this might make threads wait long enough for ASEParser
                /* Need to make the thread wait for the PDF to get created. */
                //long start = System.currentTimeMillis();
                //while (System.currentTimeMillis() - start < 1000);

                /* Convert Ballot from ASE to Ballot object TODO check if this is right, should be able to do something similar */
                List<PlaintextRaceSelection> ballotForm = ASEConverter.convertFromASE((ListExpression)arg[0]);

                Ballot<PlaintextRaceSelection> ballot = new Ballot<>(bid, ballotForm, nonce.toString());

                /* Encrypt Ballot */
                Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> encBallot;

                try { encBallot = ballotCrypter.encrypt(ballot); }
                catch (Exception e) { e.printStackTrace(); throw new RuntimeException("Could not encrypt the ballot because of "+ e.getClass()); }

                committedBallot = true;

                /* Announce that we're commiting this ballot as override to auditorium and commit it */
                auditorium.announce(new OverrideCommitConfirmEvent(mySerial, nonce, ASEConverter.convertToASE(ballot).toVerbatim()));
                auditorium.announce(new CommitBallotEvent(mySerial, nonce, ASEConverter.convertToASE(encBallot).toVerbatim(), bid, precinct));

                /* Broadcast new status */
                broadcastStatus();

                /* Announce a new printing event and print */
                List<List<String>> races = voteboxuiDriver.getView().getRaceGroups();
                auditorium.announce(new BallotPrintingEvent(mySerial, bid, nonce));

                /* TODO Printer needs to be refactored to expect the ballot selections, not a path to prerendered images */
                printer = new Printer(null, races);

                /* Check for success */
                boolean success = printer.printCommittedBallot(ballot.getRaceSelections(), bid);
                printer.printedReceipt(bid);

                /* By this time, the voter is done voting. Wait before returning to inactive. */
                long start = System.currentTimeMillis();

                while (System.currentTimeMillis() - start < 5000) ;

                finishedVoting = true;

                /* Announce successful print event or throw an error */
                if (success)
                    auditorium.announce(new BallotPrintSuccessEvent(mySerial, bid, nonce));

            }

            else { /* TODO runtime error should be graceful */
                throw new RuntimeException("Received an override-commit-confirm event at the incorrect time");
            }
        });

        /* TODO replace with Driver call */
        voteboxuiDriver.getView().registerForOverrideCommitDeny((o, arg) -> {
            if (voting && override && !finishedVoting && voteboxuiDriver != null) {

                auditorium.announce(new OverrideCommitDenyEvent(mySerial, nonce));
                override = false;
            }

            else {
                throw new RuntimeException("Received an override-cast-deny event at the incorrect time");
            }
        });
    }

    /**
     * This method generates a short code for each vote on this machine, which will
     * eventually be sent over the wire to the controller, who will then publish the
     * short code - plaintext pairs and document the nonces generated here so that the
     * votes can be audited and verified
     *
     * @param ballot - the ballot containing all of the votes to be short coded
     *
     */
    private void shortCodes(ListExpression ballot, List<List<String>> raceGroups) {
        try {

            for (ASExpression x : ballot) {

                ListExpression choice = (ListExpression) x;

                /* If this is who the vote selected in this race, make a short code */
                if(ASEConverter.convertFromASE((ListExpression) choice.get(1)) == AdderInteger.ONE){

                    ASExpression raceId = null;

                    /* Find the race to which the choice belongs */
                    for(List<String> race : raceGroups)
                        if(race.contains(choice.get(0).toString()))
                            raceId = ASExpression.make(race.get(0));

                    if(raceId == null)
                        throw new RuntimeException("Found a vote with no race?");

                    /* Create a new shortcode and votepair from the raceId */
                    ListExpression code =  new ListExpression(raceId, ASExpression.make(bid), nonce);
                    ASExpression shortCode = ASExpression.makeVerbatim(code.getSHA256());

                    VotePair pair = new VotePair(shortCode, choice);

                    /* Pair this ballot's nonce with each of its short codes and votes */
                    plaintextAuditCommits.put(nonce, pair);
                }
            }

        } catch (InvalidVerbatimStreamException e) { throw new RuntimeException("Malformed nonce!", e); }
    }


    /**
     * Starts Auditorium, registers the listener, and connects to the network.
     */
    public void start() {

        /* TODO probably want to keep this view and open new UI on active */
        inactiveUI = new VoteBoxInactiveUI(this);

        inactiveUI.setVisible(true);

        try {

            auditorium = new VoteBoxAuditoriumConnector(mySerial, _constants, launchCode,
                    ActivatedEvent.getMatcher(), AssignLabelEvent.getMatcher(),
                    AuthorizedToCastEvent.getMatcher(), BallotReceivedEvent.getMatcher(),
                    OverrideCancelEvent.getMatcher(), OverrideCommitEvent.getMatcher(),
                    PollsOpenQEvent.getMatcher(),
                    PINEnteredEvent.getMatcher(), InvalidPinEvent.getMatcher(),
                    PollsOpenEvent.getMatcher(), PollStatusEvent.getMatcher(),
                    BallotPrintingEvent.getMatcher(), BallotPrintSuccessEvent.getMatcher(),
                    BallotPrintFailEvent.getMatcher(), PollMachinesEvent.getMatcher(),
                    ProvisionalAuthorizeEvent.getMatcher());

        } catch (NetworkException e1) {

        	/* NetworkException represents a recoverable error so just note it and continue */
            System.out.println("Recoverable error occurred: "+e1.getMessage());
            e1.printStackTrace(System.err);

        }

        auditorium.addListener(new VoteBoxEventListener() {

            /* These are all NO-OPs because we don't respond to these */
            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {}
            public void castCommittedBallot(CastCommittedBallotEvent e) {
                if(e.getSource() == mySerial) {
                    publicCount++;
                    protectedCount++;
                }
            }
            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {}
            public void overrideCancelDeny(OverrideCancelDenyEvent e) {}
            public void lastPollsOpen(LastPollsOpenEvent e) {}
            public void overrideCommitConfirm(OverrideCommitConfirmEvent e) {}
            public void overrideCommitDeny(OverrideCommitDenyEvent e) {}
            public void pollsClosed(PollsClosedEvent e) {}
            public void supervisor(SupervisorEvent e) {}
            public void votebox(VoteBoxEvent e) {}
            public void commitBallot(CommitBallotEvent e) {}
            public void pinEntered(PINEnteredEvent e) {}
            public void ballotPrintFail(BallotPrintFailEvent e){}
            public void scannerStart(StartScannerEvent startScannerEvent) {}
            public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {}
            public void announceProvisionalBallot(ProvisionalBallotEvent e) {}
            public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent) {}
            public void tapMachine(TapMachineEvent tapMachineEvent) {}
            public void startUpload(StartUploadEvent startUploadEvent) {}
            public void completedUpload(CompletedUploadEvent completedUploadEvent) {}
            public void ballotScanned(BallotScannedEvent e) {}
            public void ballotScanner(BallotScannerEvent e) {}
            public void ballotAccepted(BallotScanAcceptedEvent e){}
            public void ballotRejected(BallotScanRejectedEvent e){}
            public void uploadBallots(BallotUploadEvent ballotUploadEvent) {}

            /**
             * Handler for the activated message. Look to see if this VoteBox's
             * status exists (and is correct), and if not, broadcast its status
             *
             * @see votebox.events.ActivatedEvent
             */
            public void activated(ActivatedEvent e) {

                boolean found = false;

                for (StatusEvent ae : e.getStatuses()) {

                     /* TODO NODE */
                    if (ae.getTargetSerial() == mySerial) {

                        VoteBoxEvent ve = (VoteBoxEvent) ae.getStatus();
                        VoteBoxEvent status = getStatus();

                        /* Checking to make sure the status is correct */
                        boolean checkStatus = !ve.getStatus().equals(status.getStatus()) || ve.getBattery() != status.getBattery() ||
                                               ve.getLabel() != status.getLabel() || ve.getProtectedCount() != status.getProtectedCount() ||
                                               ve.getPublicCount() != status.getPublicCount();

                        /* Broadcast the status if any of these are not correct */
                        if (checkStatus) broadcastStatus();

                        found = true;
                    }
                }

                /* If the StatusEvent was not found in the list of statuses, broadcast */
                if (!found) broadcastStatus();

                superSerial = e.getSerial();
                superOnline = true;
            }

            /**
             * Handler for the assign-label message. If it is referring to this
             * booth, set the label.
             *
             * @see votebox.events.AssignLabelEvent
             */
            public void assignLabel(AssignLabelEvent e) {

                /* See if this message is directed towards this machine and, if so, change the label */
                if (e.getTargetSerial() == mySerial)
                	label = e.getLabel();

                /* Notify that the label was changed */
                labelChangedEvent.notify(label);
            }

            /**
             * Handler for the authorized-to-cast message. If it is for this
             * booth, and it is not already voting, unzip the ballot and fire
             * the VoteBox runtime. Also announce the new status.
             *
             * @see votebox.events.AuthorizedToCastEvent
             */
            public void authorizedToCast(AuthorizedToCastEvent e) {

                System.out.println("Detected AuthorizedToCastEvent for machine with serial " + e.getTargetSerial());

                /* See if this is directed towards this machine */
                if (e.getTargetSerial() == mySerial) {
                    isProvisional = false;

                    /* Make sure not already voting TFODO runtime exception */

                    System.out.println("Currently this machine is " + (voting ? "voting already!" : "not voting already!"));

                    if (voting || (voteboxuiDriver != null && killVBTimer == null))
                        throw new RuntimeException( "VoteBox was authorized-to-cast, but was already voting");

                    /* If last VB runtime is on thank you screen and counting down to when it disappears, kill it
                    prematurely without showing inactive UI */
                    if (killVBTimer != null && voteboxuiDriver != null) {

                        /* Kill the runtime */
                        killVBTimer.stop();

                        /* Kill voting session */
                        /* TODO replace with call to Driver */
                        voteboxuiDriver.kill();
                        voteboxuiDriver = null;
                    }

                    nonce = e.getNonce();

                    bid = String.valueOf(rand.nextInt(Integer.MAX_VALUE));
                    precinct = e.getPrecinct();

                    System.out.println("Dealing with ballot " + bid + " in precinct " + precinct + "...");
                    System.out.println("Initialising crypto...");

                    DHExponentialElGamalCryptoType cryptoType = new DHExponentialElGamalCryptoType();

                    try { cryptoType.loadPublicKey(_constants.getKeyStore().loadPEK()); }
                    catch (AuditoriumCryptoException ex) { throw new RuntimeException("Error loading the PEK from the KeyStore."); }

                    ballotCrypter = new BallotCrypter<>(cryptoType);

                    System.out.println("Crypto set!");

                    /* TODO path fixing */
                    //File path   = new File(System.getProperty("user.dir"));
                    //path        = new File(path, "tmp");
                    //path        = new File(path, "ballots");
                    //path        = new File(path, "ballot"/* + protectedCount*/);
                    //path.mkdirs();
                    bid = String.valueOf(rand.nextInt(Integer.MAX_VALUE));

                    byte[] ballot = e.getBallot();

                    //run(new File(path, "data").getAbsolutePath());

                    /* Broadcast current status */
                    broadcastStatus();
                }
            }

            /**
             * Handler for the ballot-received message. Show the next page on
             * the VB runtime (the ballot is being printed screen), and start a timer that
             * kills the runtime after a set amount of time (5 seconds), and
             * then shows the inactive screen. Also responds with its status.
             *
             * @see votebox.events.BallotReceivedEvent
             */
            public void ballotReceived(BallotReceivedEvent e) {
                if (e.getTargetSerial() == mySerial
                        && Arrays.equals(e.getNonce().toVerbatim(), nonce.toVerbatim())) {

                    if (e.getTargetSerial() == mySerial && Arrays.equals(e.getNonce().toVerbatim(), nonce.toVerbatim())) {

                        if (!committedBallot)
                            throw new RuntimeException("Someone said the ballot was received, but this machine hasn't committed it yet. Maybe the supervisor is not configured properly?");

                        /* TODO kill all below pretty much and replace with Driver call to message for kill */
                        if (isProvisional) {

                                /* Show provisional success page */
                                voteboxuiDriver.getView().sendMessage("Provisional Ballot Received");
                        }
                        else
                            voteboxuiDriver.getView().sendMessage("Ballot Received");

                        /* Show printing page */
                        nonce = null;
                        voting = false;
                        finishedVoting = false;
                        committedBallot = false;
                        broadcastStatus();

                        /* Create a timer to kill the runtime after 5 seconds */
                        killVBTimer.restart();
                    }
                }
            }

            /**
             * Increment the number of connections when a machine joins the network
             * @see votebox.events.JoinEvent
             */
            public void joined(JoinEvent e) {
                numConnections++;
                connected = true;
                if(e.getSerial()==superSerial) superOnline = true;
            }

            /**
             * Decrement the number of connections when a machine leaves the network
             * @see votebox.events.LeaveEvent
             */
            public void left(LeaveEvent e) {
                numConnections--;
                if (numConnections == 0) connected = false;
                if(e.getSerial()==superSerial) superOnline = false;
            }

            /**
             * Handler for the override-cancel message. If it is referring to
             * this booth, and it is in a state that it can be overridden, send
             * the runtime to the proper override page and record the page the
             * user was previously on.
             *
             * @see votebox.events.OverrideCancelEvent
             */
                public void overrideCancel(OverrideCancelEvent e) {

                if (mySerial == e.getTargetSerial() && e.getNonce().equals(nonce)) {

                    try {

                        /* TODO replace by Driver call */
                        /* Make sure voting is in progress */
                        if (voting && !finishedVoting && voteboxuiDriver != null) {

                            /* Save the last page the voter was on before the override */
                            voteboxuiDriver.getView().overrideCancel();

                        } else { throw new RuntimeException("Received an override-cancel message when the user wasn't voting"); }

                    } catch (IncorrectTypeException e1) { Bugout.err("Incorrect type in overrideCancel handler"); }
                }
            }


            /**
             * Handler for the override-cast message. If it is referring to this
             * booth, and it is in a state that it can be overridden, send the
             * runtime to the proper override page and record the page the user
             * was previously on.
             *
             * @see votebox.events.OverrideCommitEvent
             */
            public void overrideCommit(OverrideCommitEvent e) {
                /* See if this is the target of the event */
                if(e.getTargetSerial() == mySerial){

                    try {

                        /* TODO replace by Driver call */
                        /* Make sure voting is in progress */
                        if (voting && !finishedVoting && voteboxuiDriver != null) {

                            /* Saves the last page the voter was on */
                            voteboxuiDriver.getView().overrideCommit();
                        }  else { throw new RuntimeException("Received an override-cast message when the user wasn't voting"); }

                    } catch (IncorrectTypeException e1) {
                        /* We don't want to bail once VoteBox is up and running, so report and continue in this case */
                        System.out.println("Incorrect type received in overrideCommit event: "+e1.getMessage());
                        e1.printStackTrace(System.err);
                    }
                }
            }


            /**
             * Handler for Polls Open. If not voting, booth prompts for pin
             * @see votebox.events.PollsOpenEvent
             */
            public void pollsOpen(PollsOpenEvent e) {
                launchChromeUI();
            }

            /**
             * Handler for the polls-open? event. Searches the machine's log,
             * and replies with a last-polls-open message if an appropriate
             * polls-open message is found.
             *
             * @see votebox.events.PollsOpenQEvent
             */ /* TODO: Search the log and extract an appropriate polls open message */
            public void pollsOpenQ(PollsOpenQEvent e) {

                if (e.getSerial() != mySerial) {

                    ASExpression res = null;
                    /* TODO need to know what this is for */
                    if (res != null && res != NoMatch.SINGLETON) {

                        VoteBoxEventMatcher matcher = new VoteBoxEventMatcher(PollsOpenEvent.getMatcher());
                        PollsOpenEvent event = (PollsOpenEvent) matcher.match(0, res);

                        if (event != null && event.getKeyword().equals(e.getKeyword()))
                            auditorium.announce(new LastPollsOpenEvent(mySerial, event));
                    }
                }
            }

            /**
             * Handles InvalidPinEvent and reprompts for PIN
             * @see votebox.events.InvalidPinEvent
             */
            public void invalidPin(InvalidPinEvent e) {
                if(e.getTargetSerial() == mySerial)
                    voteboxuiDriver.getView().sendMessage("Invalid PIN: Enter Valid PIN");
            }

            /**
             * Received by VoteBox booth when it joins the network after a polls open event. PIN prompt is executed.
             * @see votebox.events.PollStatusEvent
             */
            public void pollStatus(PollStatusEvent pollStatusEvent) {

                /* TODO Replace by Driver call*/
                /* Check if machine is voting, sitting with polls opened */
                if(!voting && pollStatusEvent.getPollStatus() == 1)
                    launchChromeUI();
            }

            /**
             * This indicates that the ballot was successfully printed and the voting session can safely end
             * @see votebox.events.BallotPrintSuccessEvent
             */
            public void ballotPrintSuccess(BallotPrintSuccessEvent e){
                if (e.getSerial() != mySerial && e.getBID().equals(bid) && Arrays.equals(e.getNonce().toVerbatim(), nonce.toVerbatim())) {

                    /* This should never happen... */
                    if (!finishedVoting)
                        throw new RuntimeException("Someone said the ballot was printed, but this machine hasn't finished voting yet");

                    broadcastStatus();

                }
            }

            /**
             * Used as an intermittent poll on the status of this booth through auditorium
             * @see votebox.events.PollMachinesEvent
             */
            public void pollMachines(PollMachinesEvent pollMachinesEvent) {

                broadcastStatus();

                try { Thread.sleep(100); }
                catch (InterruptedException e) { e.printStackTrace(); }
            }

            /**
             * Handler for ProvisionalAuthorizeEvent from supervisor. Generates ballot file path and stores ballot
             * @see votebox.events.ProvisionalAuthorizeEvent
             */
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent e) {

                if (e.getTargetSerial() == mySerial) {

                    isProvisional = true;

                    /* Check if already voting */
                    if (voting || voteboxuiDriver != null && killVBTimer == null)
                        throw new RuntimeException("VoteBox was authorized-to-cast, but was already voting");


                    /* If last VB runtime is on thank you screen and counting  down to when it disappears,
                        kill it prematurely without showing inactive UI */
                    if (killVBTimer != null && voteboxuiDriver != null) {

                        killVBTimer.stop();

                        voteboxuiDriver.kill();
                        voteboxuiDriver = null;
                    }

                    nonce = e.getNonce();

                    /* Current working directory */
                    /* TODO path fixing */
                    File path   = new File(System.getProperty("user.dir"));
                    path        = new File(path, "tmp");
                    path        = new File(path, "ballots");
                    path        = new File(path, "ballot"/* + protectedCount*/);
                    path.mkdirs();

                    bid = String.valueOf(rand.nextInt(Integer.MAX_VALUE));


                    byte[] ballot = e.getBallot();
                    run(new File(path, "data").getAbsolutePath());
                    broadcastStatus();
                }

            }

        });

        try {
            auditorium.connect();
            auditorium.announce(getStatus());
        }
        catch (NetworkException e1) { throw new RuntimeException(e1); }

        statusTimer.start();
    }

    /**
     * Generates a new PINEnteredEvent and sends over the network for validation by supervisor.
     * @param pin 4-digit, decimal PIN to be validated
     */
    public void validatePin(String pin) {
        auditorium.announce(new PINEnteredEvent(mySerial, pin));
    }


    /**
     * Main entry point into the program. If an argument is given, it will be
     * the serial number, otherwise VoteBox will load a serial from its config file.
     * 
     * @param args
     */
    public static void main(String[] args) {
        String launchCode = "";

        while (launchCode == null || launchCode.equals(""))
            launchCode = JOptionPane.showInputDialog(null,
                    "Please enter today's election launch code:", "Launch Code",
                    JOptionPane.QUESTION_MESSAGE);

        if (args.length == 1)
            new VoteBox(Integer.parseInt(args[0]), launchCode).start();
        else /* Tell VoteBox to refer to its config file for the serial number */
            new VoteBox().start();
    }
}
