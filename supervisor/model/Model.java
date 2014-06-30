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

package supervisor.model;

import auditorium.IAuditoriumParams;
import auditorium.NetworkException;
import crypto.adder.PublicKey;
import crypto.interop.AdderKeyManipulator;
import sexpression.ASExpression;
import sexpression.StringExpression;
import sexpression.stream.Base64;
import supervisor.model.machine.*;
import votebox.events.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

/**
 * The main model of the Supervisor in the model-view-controller. Contains the status of the machines, and of
 * the election in general. Manages nearly all administrative functions of the election, including accepting machines
 * onto the network, generating and tracking voters and pins, authorizing machines, opening and closing elections, and
 * much more. Also contains a link to Auditorium, for broadcasting (and hearing) messages on the network.
 * 
 * @author Corey Shaw
 */
public class Model {

    /** The set of all machines that this console is connected to */
    private TreeSet<AMachine> machines;

    /** The event observer that updates the state of the machines */
    private ObservableEvent machinesChangedObs;

    /** This connects the Supervisor to the network */
    private VoteBoxAuditoriumConnector auditorium;

    /** The serial number used by this machine in all network communication */
    private final int mySerial;

    /** Whether or not this machine is active */
    private boolean isActivated;

    /** The event observer for the activation status of this machine */
    private ObservableEvent activatedObs;

    /** Whether or not this machine is connected to the network */
    private boolean isConnected;

    /** The event observer for the network connection status */
    private ObservableEvent connectedObs;

    /** Whether the polls are open or not */
    private boolean arePollsOpen;

    /** An observer for the status of the polls */
    private ObservableEvent pollsOpenObs;

    /** The number of machines that are connected to this one */
    private int numConnected;

    /** The election keyword */  /* TODO Implement this */
    private String keyword;

    /** A timer to update various parts of the system with */
    private Timer statusTimer;

    /** The configuration parameters for this machine */
    private final IAuditoriumParams auditoriumParams;

    /** A map of all committed ballot ID's to their nonce values */
    private HashMap<String, ASExpression> committedBids;

    /** A Map of Precinct IDs to Precincts */
    private TreeMap<String, Precinct> precincts;

    /** Keeps track of the last heard polls open event so that new machines can be updated when they come online */
    private PollsOpenEvent lastPollsOpenHeard;

    private PINValidator pinValidator = PINValidator.SINGLETON;

    /** An object that maintains a hash chain record of voting */
    private HashChain hashChain;

    /**
     * Equivalent to Model(-1, params);
     * 
     * @param params IAuditoriumParams to use for determining settings on this Supervisor model.
     */
    public Model(IAuditoriumParams params){
    	this(-1, params);
    }
    
    /**
     * Constructs a new model with the given serial
     * 
     * @param serial serial number to identify as
     * @param params parameters to use for configuration purposes
     */
    public Model(int serial, IAuditoriumParams params) {
        auditoriumParams = params;

        /* If we have a valid serial, continue on */
    	if(serial != -1)
        	this.mySerial = serial;

        /* Otherwise see if a serial number was specified by the configuration file */
    	else
    		this.mySerial = params.getDefaultSerialNumber();

        /* If an invalid serial has not been replaced at this point, we cannot proceed */
    	if(mySerial == -1)
    		throw new RuntimeException("Expected serial number in configuration file if not on command line");

        /* Initialize all of the fields to their defaults */
        machines = new TreeSet<>();
        machinesChangedObs = new ObservableEvent();
        activatedObs = new ObservableEvent();
        connectedObs = new ObservableEvent();
        pollsOpenObs = new ObservableEvent();
        keyword = "";

        committedBids = new HashMap<>();
        precincts = new TreeMap<>();

        /* This is the heartbeat timer, it announces a status event every 5 minutes */
        statusTimer = new Timer(300000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isConnected()) {
                    auditorium.announce(getStatus());
                }
            }
        });

        hashChain = new HashChain();
    }

    /**
     * Activates this supervisor.<br>
     * Formats the activated message (containing the statuses of known
     * machines), and labels any unlabeled machines.
     */
    public void activate() {
        /* Lists that will temporarily house statuses and machines before they are added to their proper lists */
        ArrayList<StatusEvent> statuses = new ArrayList<>();
        ArrayList<AMachine> unlabeled   = new ArrayList<>();

        /* This will be used to label unlabeled machines, based on the highest label yet seen */
        int maxLabel = 0;

        /* Iterate through all of the connected machines and add them to the UI */
        for (AMachine m : machines) {
            /* If the machine is online, figure out what it is */
            if (m.isOnline()) {

                /* We use this as a dumb event to store relevant information about the machines we connect to */
                IAnnounceEvent s = null;

                /* If the machine is a supervisor, figure out if it is active */
                if (m instanceof SupervisorMachine) {
                    SupervisorMachine ma = (SupervisorMachine) m;

                    /* Set the dummy status event to reflect if the machine is active or not */
                    if (ma.isActive())        s = new SupervisorEvent(0, 0, "active");
                    else if (ma.isInactive()) s = new SupervisorEvent(0, 0, "inactive");
                }

                /* If the machine is a votebox, figure out if it is idle or voting */
                else if (m instanceof VoteBoxBooth) {

                    VoteBoxBooth ma = (VoteBoxBooth) m;

                    if (ma.isReady())       s = new VoteBoxEvent(0, ma.getLabel(), "ready",  ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());
                    else if (ma.isInUse())  s = new VoteBoxEvent(0, ma.getLabel(), "in-use", ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());

                    /* If the machine is not labeled add it to the list of machines to label */
                    if (ma.getLabel() == 0)
                        unlabeled.add(ma);

                    /* Otherwise, see if it is the largest label we've seen yet */
                    else if (ma.getLabel() > maxLabel)
                        maxLabel = ma.getLabel();
                }

                /* If the machine is a ballot scanner, figure out if it is active or inactive */
                else if (m instanceof BallotScannerMachine) {

                    BallotScannerMachine ma = (BallotScannerMachine)m;

                    if (ma.isActive())        s = new BallotScannerEvent(ma.getSerial(), ma.getLabel(),   "active", ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());
                    else if (ma.isInactive()) s = new BallotScannerEvent(ma.getSerial(), ma.getLabel(), "inactive", ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());


                    /* As with Voteboxes, figure out the label info for this machine */
                    if (ma.getLabel() == 0)            unlabeled.add(ma);
                    else if (ma.getLabel() > maxLabel) maxLabel = ma.getLabel();
                }

                /* If the machine is a tap, note it */
                else if(m instanceof  TapMachine){

                    TapMachine machine = (TapMachine)m;

                    s = new TapMachineEvent(machine.getSerial());
                }

                /*
                 * If we haven't assigned something to s yet, then we've encountered a machine that is not part of
                 * the set of known machines. This is a bad thing
                 *
                 * TODO Better erroring
                 */
                if (s == null)
                    throw new IllegalStateException("Unknown machine or status");

                /* Add our newly constructed dummy status to the list */
                statuses.add(new StatusEvent(0, m.getSerial(), s));
            }
        }

        /* Announce to the world that we're an active Supervisor, connected to all of the machines in statuses */
        auditorium.announce(new ActivatedEvent(mySerial, statuses));

        /* Now label any machines that haven't been labeled yet by announcing to them their new label */
        for (AMachine machine : unlabeled)
            auditorium.announce(new AssignLabelEvent(mySerial, machine.getSerial(), ++maxLabel));

        /*
         * If the polls aren't marked open yet, we don't know if they are open or not.
         * poll the network to see if they are or not open
         */
        if (!arePollsOpen)
            auditorium.announce(new PollsOpenQEvent(mySerial, keyword));

        /* Tell the ballot scanners to start themselves */
        sendStartScannerEvent();
    }

    /**
     * Sends a StartScannerEvent.
     */
    public void sendStartScannerEvent () {
        auditorium.announce(new StartScannerEvent(mySerial));
    }

    /**
     * Authorizes a VoteBox booth to vote with a specific ballot style
     * 
     * @param otherSerial       the serial number of the booth
     *
     * @throws IOException if the ballot cannot be serialized correctly
     */
    public void authorize(int otherSerial, String ballotFile) throws IOException {

        /* Build a nonce to associate with this ballot and voting session */
        byte[] nonce = new byte[256];

        for (int i = 0; i < 256; i++)
            nonce[i] = (byte) (Math.random() * 256);

        /* Open the ballot */
        File file = new File(ballotFile);

        /* Pare off the precinct information */
        Precinct p = getPrecinctWithBallot(ballotFile);

        /* Serialize the ballot to send over the network */
        FileInputStream fin = new FileInputStream(file);
        byte[] ballot = new byte[(int) file.length()];
        int error = fin.read(ballot);

        /* TODO better erroring */
        if(error != ballot.length)
            throw new RuntimeException("Error in serializing ballot!");

        /* Put the nonce in an S-Expression to send over the network */
        ASExpression ASENonce = StringExpression.makeString(nonce);

        /*
         * Announce that we're authorizing a voting booth, depending on the crypto requirements specified in this machine's
         * configuration file. This is a parameter of the election, but will typically always be NIZK enabled.
         */
        if (!this.auditoriumParams.getEnableNIZKs()) {
            auditorium.announce(new AuthorizedToCastEvent(mySerial, otherSerial, ASENonce, p.getPrecinctID(), ballot));

        }
        else {
            /* For NIZKs to work, we have to establish the public key before the voting can start */
            auditorium.announce(new AuthorizedToCastWithNIZKsEvent(mySerial, otherSerial, ASENonce, p.getPrecinctID(), ballot,
                    AdderKeyManipulator.generateFinalPublicKey((PublicKey)auditoriumParams.getKeyStore().loadAdderKey("public"))));
        }
    }

    /**
     * Authorizes a VoteBox booth for a provisional voting session
     *
     * @param targetSerial      the serial number of the booth being provisionally authorized
     *
     * @throws IOException if the ballot cannot be serialized correctly
     */
    private void provisionalAuthorize(int targetSerial, String ballotFile) throws IOException{

        /* Generate a nonce for this ballot */
        byte[] nonce = new byte[256];

        for (int i = 0; i < 256; i++)
            nonce[i] = (byte) (Math.random() * 256);

        /* Load and serialize the ballot */
        File file = new File(ballotFile);
        FileInputStream fin = new FileInputStream(file);

        byte[] ballot = new byte[(int) file.length()];
        int error = fin.read(ballot);

        /* TODO better erroring */
        if(error != ballot.length)
            throw new RuntimeException("Error in serializing ballot!");

        /* Send out a provisional authorize event */
        auditorium.announce(new ProvisionalAuthorizeEvent(mySerial, targetSerial, StringExpression.makeString(nonce), ballot));
    }

    /**
     * Closes the polls
     */
    public boolean closePolls() {

        boolean canClose = !isActiveVotingSession();

        if(canClose)
            /* Announce that the polls are closing */
            auditorium.announce(new PollsClosedEvent(mySerial, new Date().getTime()));

        return canClose;
    }

    /**
     * Gets the index in the list of machines of the machine with the given serial
     * 
     * @param serial        the serial number of the machine
     * @return              the index
     */
    public int getIndexForSerial(int serial) {
        int i = 0;

        for (AMachine m : machines)
            if (m.getSerial() == serial) return i;
            else i++;

        return -1;
    }

    /**
     * Gets today's election keyword (entered at program launch)
     * 
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Gets an AMachine from the list of machines by its serial number
     * 
     * @param serial        the serial for the machine to retrieve
     * @return              the machine
     */
    public AMachine getMachineForSerial(int serial){

        for (AMachine m : machines)
            if (m.getSerial() == serial)
                return m;

        return null;
    }

    /**
     * Gets the list of machines, in serial number order
     * 
     * @return the machines
     */
    public TreeSet<AMachine> getMachines() {
        return machines;
    }

    /**
     * @return this machine's serial
     */
    public int getMySerial() {
        return mySerial;
    }

    /**
     * @return the number of active connections
     */
    public int getNumConnected() {
        return numConnected;
    }

    /**
     * @return a SupervisorEvent with this machine's status (used for periodic
     *         broadcasts)
     */
    public SupervisorEvent getStatus() {

        String active = isActivated ? "active" : "inactive";

        return new SupervisorEvent(mySerial, new Date().getTime(), active);
    }

    /**
     * @return whether this supervisor is active
     */
    public boolean isActivated() {
        return isActivated;
    }

    /**
     * @return whether this supervisor is connected to any machines
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * @return whether the polls are open
     */
    public boolean arePollsOpen() {
        return arePollsOpen;
    }

    /**
     * Opens the polls by announcing a PollsOpenEvent.
     */
    public void openPolls() {
        auditorium.announce(new PollsOpenEvent(mySerial, new Date().getTime(), keyword));
    }

    /**
     * Sends an override-cancel request to a VoteBox booth
     * 
     * @param targetSerial      the serial number of the booth
     */
    public void overrideCancel(int targetSerial) {
        /* Get the nonce for the voting session at that machine */
        ASExpression nonce = StringExpression.makeString(((VoteBoxBooth) getMachineForSerial(targetSerial)).getNonce());

        /* Announce the event to the network, effectively telling the machine that is is being overridden */
        auditorium.announce(new OverrideCancelEvent(mySerial, targetSerial, nonce));
    }

    /**
     * Sends an override-commit request to a VoteBox booth
     * 
     * @param targetSerial      the serial number of the booth
     */
    public void overrideCommit(int targetSerial) {
        /* Get the nonce for the booth to override */
        ASExpression nonce = StringExpression.makeString(((VoteBoxBooth) getMachineForSerial(targetSerial)).getNonce());

        /* Announce the event to the network, effectively telling the machine that is is being overridden */
        auditorium.announce(new OverrideCommitEvent(mySerial, targetSerial, nonce));
    }

    /**
     * Register to be notified when this supervisor's active status changes
     * 
     * @param obs               the observer
     */
    public void registerForActivated(Observer obs) {
        activatedObs.addObserver(obs);
    }

    /**
     * Register to be notified when this supervisor's connected status changes
     * 
     * @param obs               the observer
     */
    public void registerForConnected(Observer obs) {
        connectedObs.addObserver(obs);
    }

    /**
     * Register to be notified when the list of machines changes
     * 
     * @param obs               the observer
     */
    public void registerForMachinesChanged(Observer obs) {
        machinesChangedObs.addObserver(obs);
    }

    /**
     * Register to be notified when the polls open status changes
     * 
     * @param obs               the observer
     */
    public void registerForPollsOpen(Observer obs) {
        pollsOpenObs.addObserver(obs);
    }

    /**
     * Sets this supervisor's active status and notifies the observers
     *
     * @param activated         the new active status
     */
    public void setActivated(boolean activated) {
        this.isActivated = activated;
        activatedObs.notifyObservers();
    }

    /**
     * Sets this supervisor's connected status
     * 
     * @param connected         the connected to set
     */
    @SuppressWarnings("SameParameterValue")
    public void setConnected(boolean connected) {
        this.isConnected = connected;
        connectedObs.notifyObservers();
    }

    /**
     * Sets the election keyword
     * 
     * @param keyword           the keyword to set
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Sets this supervisor's polls open status and notifies the observers
     * 
     * @param arePollsOpen      the polls' status
     */
    public void setArePollsOpen(boolean arePollsOpen) {
        this.arePollsOpen = arePollsOpen;
        pollsOpenObs.notifyObservers();
    }

    /**
     * Starts auditorium, registers the event listener, and connects to the
     * network.
     */
    public void start() {
        try {
            /* These are all the events we are expecting to hear and handle, and matchers for their formatting */
            auditorium = new VoteBoxAuditoriumConnector(mySerial,
                    auditoriumParams, ActivatedEvent.getMatcher(),
                    AssignLabelEvent.getMatcher(), AuthorizedToCastEvent.getMatcher(),
                    CastCommittedBallotEvent.getMatcher(), LastPollsOpenEvent.getMatcher(),
                    OverrideCommitConfirmEvent.getMatcher(), PollsClosedEvent.getMatcher(),
                    PollsOpenEvent.getMatcher(), PollsOpenQEvent.getMatcher(),
                    SupervisorEvent.getMatcher(), VoteBoxEvent.getMatcher(),
                    EncryptedCastBallotEvent.getMatcher(), CommitBallotEvent.getMatcher(),
                    EncryptedCastBallotWithNIZKsEvent.getMatcher(), AuthorizedToCastWithNIZKsEvent.getMatcher(),
                    PINEnteredEvent.getMatcher(), InvalidPinEvent.getMatcher(),
                    PollStatusEvent.getMatcher(), BallotPrintSuccessEvent.getMatcher(),
                    BallotScannedEvent.getMatcher(), BallotScannerEvent.getMatcher(),
                    ProvisionalCommitEvent.getMatcher(), ProvisionalAuthorizeEvent.getMatcher(),
                    TapMachineEvent.getMatcher());

        }
        catch (NetworkException e1) { throw new RuntimeException(e1); }

        /* This is what listens for all the events and reacts to them as they are heard */
        auditorium.addListener(new VoteBoxEventListener() {

            /* These are all no-ops that are necessary for the anonymous inner class */
            public void ballotReceived(BallotReceivedEvent e) {}
            public void overrideCancel(OverrideCancelEvent e) {}
            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {}
            public void overrideCancelDeny(OverrideCancelDenyEvent e) {}
            public void overrideCast(OverrideCommitEvent e) {}
            public void overrideCastDeny(OverrideCommitDenyEvent e) {}

            public void invalidPin(InvalidPinEvent e) {}
            public void ballotAccepted(BallotScanAcceptedEvent e){}
            public void ballotRejected(BallotScanRejectedEvent e){}
            public void ballotPrinting(BallotPrintingEvent e) {}
            public void ballotPrintSuccess(BallotPrintSuccessEvent e) {}
            public void ballotPrintFail(BallotPrintFailEvent e) {}
            public void uploadCastBallots(CastBallotUploadEvent e) {}
            public void uploadChallengedBallots(ChallengedBallotUploadEvent e) {}
            public void pollMachines(PollMachinesEvent e) {}
            public void spoilBallot(SpoilBallotEvent e) {}
            public void announceProvisionalBallot(ProvisionalBallotEvent e) {}
            public void startUpload(StartUploadEvent startUploadEvent) {}
            public void completedUpload(CompletedUploadEvent completedUploadEvent) {}


            /**
             * Handler for the activated message. Sets all other supervisors
             * (including this one, if necessary) to the inactive state. Also
             * checks to see if this machine's status is listed, and responds
             * with it if not.
             */
            public void activated(ActivatedEvent e) {

                /* Iterate through all the machines and set the supervisors to inactive if they aren't this one */
                for (AMachine m : machines) {
                    if (m instanceof SupervisorMachine) {
                        /*if (m.getSerial() == e.getSerial())*/
                            m.setStatus(SupervisorMachine.ACTIVE);
                        /*else
                            m.setStatus(SupervisorMachine.INACTIVE);*/
                    }
                }

                setActivated(true);
            }

            /**
             * Handler for the assign-label message. Sets that machine's label.
             */
            public void assignLabel(AssignLabelEvent e) {

                /* Find the mini-model of the machine */
                AMachine m = getMachineForSerial(e.getTargetSerial());

                if (m != null) {
                    /* If we find the machine, set its label to the label specified */
                    m.setLabel(e.getLabel());

                    /* reload the machine to update it in the UI */
                    machines.remove(m);
                    machines.add(m);

                    /* Notify the observers that the state of this machine has changed */
                    machinesChangedObs.notifyObservers();
                }
            }

            /**
             * Handler for the authorized-to-cast message. Sets the nonce for
             * that machine.
             */
            public void authorizedToCast(AuthorizedToCastEvent e) {
                AMachine m = getMachineForSerial(e.getTargetSerial());

                /* Set the local copy of the votebox to have the corresponding nonce value of the voting session */
                if (m != null && m instanceof VoteBoxBooth) {
                    ((VoteBoxBooth) m).setNonce(e.getNonce().toVerbatim());
                }
            }

            /**
             * Handler for the cast-ballot message. Increments the booth's
             * public and protected counts, replies with ballot-received, and
             * stores the votes in the tallier.
             */
            public void castCommittedBallot(CastCommittedBallotEvent e) {

                AMachine m = getMachineForSerial(e.getSerial());

                if (m != null && m instanceof SupervisorMachine) {
                    /* TODO null check? */
                    Precinct p = getPrecinctWithBID(e.getBID());
                    p.castBallot(e.getBID());
                }
            }

            /**
             * Handler for a joined event. When a new machine joins, check and
             * see if it exists, and set it to online if so. Also increment the
             * number of connections.
             */
            public void joined(JoinEvent e) {

                /* If the machine is found in the list of machines, update its state */
                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null ) m.setOnline(true);

                /*
                 * If we didn't find the machine, it can still connect, and it will be handled when the
                 * observers are updated.
                 */
                numConnected++;
                setConnected(true);
                machinesChangedObs.notifyObservers();

                /* Announce an event to notify the new machine who is on the network */
                auditorium.announce(new SupervisorEvent(mySerial, new Date().getTime(),  isActivated ? "active" : "inactive"));
            }

            /**
             * Handler for the last-polls-open message. If the keywords match,
             * set the polls to open (without sending a message).
             */
            public void lastPollsOpen(LastPollsOpenEvent e) {

                if(e.getPollsOpenMsg() == null)
                    return;

                PollsOpenEvent e2 = e.getPollsOpenMsg();

                if (e2.getKeyword().equals(keyword))
                    setArePollsOpen(true);
            }

            /**
             * Handler for a left event. Set the machine to offline, and
             * decrement the number of connections. If we are no longer
             * isConnected to any machines, assume we're offline and deactivate.<br>
             * The supervisor needs to deactivate when it goes offline so that
             * when it comes back on, it needs to be activated again so it can
             * get a fresh list of machines and their statuses. Also, that way
             * you cannot have two machines activate separately and then join
             * the network, giving you two active supervisors.
             */
            public void left(LeaveEvent e) {

                /* Get the machine and set its online status to offline */
                AMachine m = getMachineForSerial(e.getSerial());

                if (m != null) m.setOnline(false);
                else throw new RuntimeException("WARNING: machine left without having been registered");

                /* Decrement the number of connected machines and then notify the observers */
                numConnected--;
                machinesChangedObs.notifyObservers();
            }

            /**
             * Handler for the override-cast-confirm event. Similar to
             * cast-ballot, but no received reply is sent.
             */ /* TODO Is this okay?*/
            public void overrideCastConfirm(OverrideCommitConfirmEvent e) {
                //AMachine m = getMachineForSerial(e.getSerial());
                //if (m != null && m instanceof BallotScannerMachine) {
                    //TODO Make this work with ballot hashes
                    /*String precinct = bManager.getPrecinctByBID(e.getBID().toString());
                    talliers.get(precinct).confirmed(e.getNonce());*/
                //}
            }

            /**
             * Handler for the polls-closed event. Sets the polls to closed.
             */
            public void pollsClosed(PollsClosedEvent e) {

                /* Set the polls closed variable */
                setArePollsOpen(false);

                /* Close the hash chain for this polling session */
                hashChain.closeHashChain();


                /* Check the hash chain for consistency */
                if(hashChain.isHashChainCompromised())
                    JOptionPane.showMessageDialog(null, "ERROR: The hash chain is incomplete, votes may have been removed or tampered with!");

                /* Announce that this Supervisor is going to start sending ballots to Tap */
                auditorium.announce(new StartUploadEvent(mySerial));

                /* Go through all the precincts about which this Supervisor knows */
                for (Map.Entry<String, Precinct> m : precincts.entrySet()) {

                    Precinct p = m.getValue();

                    /* Challenge all the committed ballots */
                    p.closePolls();

                    /* Send all of the cast ballots to Tap */
                    auditorium.announce(new CastBallotUploadEvent(mySerial, p.getCastBallotTotal().toListExpression()));

                    /* Send all of the challenged ballots to Tap */
                   auditorium.announce(new ChallengedBallotUploadEvent(mySerial, p.getChallengedBallots()));
                }

                /* Announce that this Supervisor has completed sending ballots to Tap */
                auditorium.announce(new CompletedUploadEvent(mySerial));
            }

            /**
             * Handler for the polls-open event. Sets the polls to open.
             */
            public void pollsOpen(PollsOpenEvent e){
                lastPollsOpenHeard = e;
                setArePollsOpen(true);
            }

            /**
             * Handler for the polls-open? event. Searches the machine's log,
             * and replies with a last-polls-open message if an appropriate
             * polls-open message is found.
             */
            public void pollsOpenQ(PollsOpenQEvent e) {
                if (e.getSerial() != mySerial) {
                    /* XXX: This code should do what the following says, but it involves a lot of heavy lifting. */
                    /* TODO: Search the log and extract an appropriate polls-open message */

                  /*  ASExpression res = null;
                    if (res != null && res != NoMatch.SINGLETON) {
                        VoteBoxEventMatcher matcher = new VoteBoxEventMatcher(
                                PollsOpenEvent.getMatcher());
                        PollsOpenEvent event = (PollsOpenEvent) matcher.match(
                                0, res);
                        if (event != null
                                && event.getKeyword().equals(e.getKeyword()))
                            auditorium.announce(new LastPollsOpenEvent(
                                    mySerial, event));
                    }*/

                    if(arePollsOpen())
                        auditorium.announce(new LastPollsOpenEvent(mySerial, lastPollsOpenHeard));
                }
            }

            /**
             * Handler for a ballotScanner (status) event. Adds the machine if it
             * hasn't been seen, and updates its status if it has.
             */
            @SuppressWarnings("ConstantConditions")
            public void ballotScanner(BallotScannerEvent e) {
                /* First grab the ballot scanner mini-model */
                AMachine m = getMachineForSerial(e.getSerial());

                /* If there isn't one, initialize it */
                if (m == null) {
                    m = new BallotScannerMachine(e.getSerial());
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }

                /*
                 * If we pulled out a machine that isn't a ballot scanner, something funny is going on,
                 * probably with serial numbers. Bugout.
                 */
                if (m != null && !(m instanceof BallotScannerMachine))
                    throw new IllegalStateException("machine " + e.getSerial() + " is not a ballotScanner, but broadcast ballotScanner message");

                /* Now we're sure that the machine is a ballot scanner, enforce its type */
                BallotScannerMachine bsm = (BallotScannerMachine) m;

                /* Figure out and set the activated status of the machine */
                if(e.getStatus().equals("active"))         bsm.setStatus(BallotScannerMachine.ACTIVE);
                else if (e.getStatus().equals("inactive")) bsm.setStatus(BallotScannerMachine.INACTIVE);
                else throw new IllegalStateException("Invalid BallotScanner Status: " + e.getStatus());

                /* Set the battery and counts appropriately */
                bsm.setBattery(e.getBattery());
                bsm.setProtectedCount(e.getProtectedCount());
                bsm.setPublicCount(e.getPublicCount());

                /* Set the mini-model to the online state */
                bsm.setOnline(true);

                /* Check to see if this ballot scanner has a conflicting label */
                if (e.getLabel() > 0) {

                    /* Look at every known machine's labels */
                    for (AMachine machine : machines) {

                        if (machine.getLabel() == e.getLabel() && machine != m) {

                            /* If there is a conflict, relabel this (the event generator) machine. */
                            int maxLabel = 0;

                            for (AMachine ma : machines)
                                if (ma instanceof BallotScannerMachine)
                                    maxLabel = Math.max(maxLabel, ma.getLabel());

                            /* Announce the new label */
                            auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), maxLabel + 1));
                            return;
                        }
                    }
                }

                /* Now update the corrected label information */

                /* If the event has the label, go ahead and set it to that */
                if (e.getLabel() > 0)
                    bsm.setLabel(e.getLabel());

                /* If not, check if it's activated and give it a label */
                else if (isActivated) {

                    /* If the scanner was labelled, assign it that label */
                    if (bsm.getLabel() > 0)
                        auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), bsm.getLabel()));

                    /* Otherwise... */
                    else {

                        int maxLabel = 0;

                        /* Find the highest label */
                        for (AMachine ma : machines)
                            if (ma instanceof BallotScannerMachine && ma.getLabel() > maxLabel)
                                maxLabel = ma.getLabel();

                        /* And assign it a label one greater */
                        auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), maxLabel + 1));
                    }

                    /* Get the poll status */
                    auditorium.announce(new PollStatusEvent(mySerial, e.getSerial(), arePollsOpen ? 1:0));
                }
            }

            /**
             * Handler for a supervisor (status) event. Adds the machine if it
             * hasn't been seen, and updates its status if it has.
             */
            public void supervisor(SupervisorEvent e) {

                /* On getting one of these, poll all of the machines */
                auditorium.announce(new PollMachinesEvent(mySerial, new Date().getTime(), keyword));

                /* Grab the machine's mini-model */
                AMachine m = getMachineForSerial(e.getSerial());

                /* Check that the sender of the message was actually a Supervisor */
                if (m != null && !(m instanceof SupervisorMachine))
                    throw new IllegalStateException("machine " + e.getSerial() + " is not a supervisor, but broadcasted supervisor message");

                /* If the machine hasn't been seen before, add it to the list of machines and initialize it */
                if (m == null) {
                    m = new SupervisorMachine(e.getSerial(), e.getSerial() == mySerial);
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }

                /* Now we can enforce the type */
                SupervisorMachine sup = (SupervisorMachine) m;

                String status = e.getStatus();

                /* Check and set the activation status of this machine. */
                switch (status) {
                    case "active":
                        sup.setStatus(SupervisorMachine.ACTIVE);
                        break;
                    case "inactive":
                        sup.setStatus(SupervisorMachine.INACTIVE);
                        break;
                    default:
                        throw new IllegalStateException( "Invalid Supervisor Status: " + e.getStatus());
                }

                /* Set the mini-model to show as online */
                sup.setOnline(true);
            }

            /**
             * Handler for a Votebox (status) event. Adds the machine if it
             * hasn't been seen, or updates the status if it has. Also, if the
             * booth is unlabeled and this is the active supervisor, labels the
             * booth with its previous label if known, or the next available
             * number.
             */
            public void votebox(VoteBoxEvent e) {

                /* Get the mini model */
                AMachine m = getMachineForSerial(e.getSerial());

                /* If this isn't a votebox, bugout */
                if (m != null && !(m instanceof VoteBoxBooth))
                    throw new IllegalStateException("machine " + e.getSerial() + " is not a booth, but broadcasted votebox message");

                /* If we haven't seen this machine before, initialize and add it */
                if (m == null) {
                    m = new VoteBoxBooth(e.getSerial());
                    System.out.println("Vote Box Added: " + m);
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }

                /* Enforce the machine's type */
                VoteBoxBooth booth = (VoteBoxBooth) m;

                String status = e.getStatus();

                /* Set the status of the machine */
                switch (status) {
                    case "ready":
                        booth.setStatus(VoteBoxBooth.READY);
                        break;
                    case "in-use":
                        booth.setStatus(VoteBoxBooth.IN_USE);
                        break;
                    case "provisional-in-use":
                        booth.setStatus(VoteBoxBooth.PROVISIONAL);
                        break;
                    default:
                        throw new IllegalStateException("Invalid VoteBox Status: " + e.getStatus());
                }

                /* Set the parameters for the machine */
                booth.setBattery(e.getBattery());
                booth.setProtectedCount(e.getProtectedCount());
                booth.setPublicCount(e.getPublicCount());

                /* Reflect the machine's online status */
                booth.setOnline(true);
                
                /* Check to see if this votebox has a conflicting label */
                if (e.getLabel() > 0){

                    /* Cycle through the machines and find the machine with the event label */
                	for (AMachine machine : machines) {

                		/* If there is a conflict, relabel this (the event generator) machine. */
                		if (machine.getLabel() == e.getLabel() && machine != m) {

                            int maxLabel = 0;

                            for (AMachine ma : machines)
                				if(ma instanceof VoteBoxBooth)
                					maxLabel = Math.max(maxLabel, ma.getLabel());

                                auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), maxLabel + 1));

                            /* Now that we've fixed the label, we're done */
                			return;
                		}
                	}
                }

                /* Set the local machine's label */
                if (e.getLabel() > 0)
                    booth.setLabel(e.getLabel());

                /* If the machine doesn't have a label, give it one */
                else {

                    if (isActivated) {

                        /* Announce the new label */
                        if (booth.getLabel() > 0)
                            auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), booth.getLabel()));

                        else {

                            int maxLabel = 0;

                            for (AMachine ma : machines)
                                if (ma instanceof VoteBoxBooth && ma.getLabel() > maxLabel)
                                    maxLabel = ma.getLabel();

                            /* Announce the new label */
                            auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), maxLabel + 1));
                        }

                        /* Announce the status of the newly added machine */
                        auditorium.announce(new PollStatusEvent(mySerial, e.getSerial(), arePollsOpen ? 1:0));
                    }
                }
            }

            /**
             * Record the vote received in the commit event.
             * It should not yet be tallied.
             */
            public void commitBallot(CommitBallotEvent e) {

                /* Get the mini-model for the machine that committed the ballot */
            	AMachine m = getMachineForSerial(e.getSerial());

                /* Ensure that the machine committing the booth is actually a votebox */
                if (m != null && m instanceof VoteBoxBooth && !((VoteBoxBooth) m).isProvisional() ) {

                    /* Enforce the machine's type */
                    VoteBoxBooth booth = (VoteBoxBooth) m;

                    /* Update the public and protected counts of the machine */
                    booth.setPublicCount(booth.getPublicCount() + 1);
                    booth.setProtectedCount(booth.getProtectedCount() + 1);

                    /* Put the committed ballot in the ballot store, in all the proper places */
                    Precinct thisPrecinct = precincts.get(e.getPrecinct());

                    try {

                        ASExpression ballot = ASExpression.makeVerbatim(e.getBallot());

                        thisPrecinct.commitBallot(e.getBID(), ballot);
                    }
                    catch(Exception ex) { ex.printStackTrace(); }

                    /* Create a hash chain record of this ballot and voting session */
                    String ballotHash = hashChain.hashBallot(e.getSerial());

                    /* Announce that the ballot was received, so it's logged */
                    auditorium.announce(new BallotReceivedEvent(mySerial, e.getSerial(), e.getNonce(), e.getBID(), e.getPrecinct()));
                }

                /* TODO Log and don't count this -- inform the user with a popup */
                else throw new RuntimeException("Bad commit attempt: machine only authorised provisionally.");

                /* TODO Should we report if a non-votebox attempts to commit? */
            }

            /**
             * Handler for the ProvisionalCommitEvent. Receives ballot as it would with a normal ballot
             */
            public void provisionalCommitBallot(ProvisionalCommitEvent e) {

                /* Get the machine's mini-model */
                AMachine m = getMachineForSerial(e.getSerial());

                /* Check that it's a votebox */
                if (m != null && m instanceof VoteBoxBooth && ((VoteBoxBooth) m).isProvisional()) {
                    /* Enforce the type */
                    VoteBoxBooth booth = (VoteBoxBooth) m;

                    /* Update the counts */
                    booth.setPublicCount(booth.getPublicCount() + 1);
                    booth.setProtectedCount(booth.getProtectedCount() + 1);

                    Precinct thisPrecinct = getPrecinctWithBID(e.getBID());

                    /* Announce that the provisional ballot was received */
                    auditorium.announce(new BallotReceivedEvent(mySerial, e.getSerial(), e.getNonce(), e.getBID(), thisPrecinct.getPrecinctID()));
                }

                /* TODO Log and disregard, popup to inform the user */
                else throw new RuntimeException("Bad commit attempt: machine not authorised provisionally.");
            }

            /**
             * Occurs once when tap joins the network and sends it's respective TapMachineEvent. Model adds a new
             * instance of a TapMachine to it's list of machines for further reference.
             */
            public void tapMachine(TapMachineEvent tapMachineEvent) {

                /* Get the mini-model and check that it's a TapMachine */
                AMachine m = getMachineForSerial(tapMachineEvent.getSerial());

                if (m != null && !(m instanceof TapMachine))
                    throw new IllegalStateException("machine " + tapMachineEvent.getSerial() + " is not a Tap but broadcasted TapMachineEvent");

                /* If the machine doesn't exist yet, add and initialize it */
                else if (m == null) {

                    TapMachine tap = new TapMachine(tapMachineEvent.getSerial());
                    tap.setOnline(true);

                    machines.add(tap);
                    machinesChangedObs.notifyObservers();
                }

                /* If the machine does exist, update its state locally */
                else {
                    m.setOnline(true);
                    machinesChangedObs.notifyObservers();
                }
            }

            /**
             * Handler for the BallotScannerEvent. Receives ballot ID from scan event and fires appropriate events in response,
             * namely an EncryptedBastBallotWithNIZKsEvent and a BallotScanAccepted/BallotScanRejected events as needed.
             */
            public void ballotScanned(BallotScannedEvent e) {

                // Test ballot stuff... TODO: Might want to implement a way to rapidly cast votes without going through multiple VoteBox sessions.
                /*readTestBallot();
                committedBids.put("711567939", testNonce);
                BallotStore.addPrecinct("711567939", testBallot);
                bManager.setPrecinctByBID("711567939", "007");
                talliers.get("007").recordVotes(testBallot.toVerbatim(), testNonce);*/

                /* Ensure that a ballot scanner sent this */
                if(!(getMachineForSerial(e.getSerial()) instanceof BallotScannerMachine)) {
                    System.err.println("A machine other than a ballot scanner attempted to broadcast a ballot scanned event! ");
                    return;
                }

                /* Get the ballot information from the event */
                String bid = e.getBID();
                int serial = e.getSerial();

                /* If the ballot was actually committed, handle it */
                Precinct p = getPrecinctWithBID(bid);

                /* First move it out of the committed list */
                ASExpression nonce = committedBids.remove(bid);

                /* Tell the ballot store to cast the ballot */
                boolean wasCast = p.castBallot(bid);

                if (wasCast) auditorium.announce(new EncryptedCastBallotWithNIZKsEvent(serial, nonce, e.getBallot(), bid));
                else throw new RuntimeException("Found the precinct with the bid, but couldn't cast the ballot...");

                /* Now tell the ballot scanner that this ballot was accepted */
                System.out.println("Sending scan confirmation!");
                System.out.println("BID: " + bid);

                auditorium.announce(new BallotScanAcceptedEvent(mySerial, bid));
            }

            /**
             * Handler for the PinEnteredEvent. Retrieves the entered pin from the event and authorizes the booth if the
             * pin is valid.
             */
            public synchronized void pinEntered(PINEnteredEvent e){

                String PIN = e.getPin();

                /* This only works if the polls are open */
                if(arePollsOpen()) {

                    /* Validate the PIN */
                    boolean isValidPIN = pinValidator.validatePIN(PIN);

                    /* Check that there is a record of this PIN and ballot style */
                    if (isValidPIN) {

                        /* Get the precinct and then ballot style for this PIN */
                        String PID = pinValidator.usePIN(PIN);
                        String ballotFile = precincts.get(PID).getBallotFile();

                        try {

                            /* If the ballot is provisional, authorize provisionally */
                            if(PID.contains("provisional"))
                                provisionalAuthorize(e.getSerial(), ballotFile);

                            /* Otherwise authorize a normal voting session */
                            else authorize(e.getSerial(), ballotFile);
                        }
                        catch(IOException ex) {
                            /* TODO Better error handling here */
                            System.err.println(ex.getMessage());
                        }
                    }

                    /* If there isn't, announce that a bad PIN was entered */
                    else auditorium.announce(new InvalidPinEvent(mySerial, e.getSerial()));
                }

                /* TODO provide error handling if the polls aren't open? */
            }

            /**
             * Handler for PollStatusEvent
             */
            public void pollStatus(PollStatusEvent pollStatusEvent) {

                /* Set the polls open flag accordingly */
                arePollsOpen = pollStatusEvent.getPollStatus()==1;

                /* Tell the scanner to start, in case it was connected after the polls opened */
                sendStartScannerEvent();
            }


            /**
             * Handler for StartScannerEvent. Activates scanner if present.
             */
            public void scannerStart(StartScannerEvent e) {

                /* Look through all the machines and activate all the scanners */
                for (AMachine machine:machines)
                    if (machine instanceof BallotScannerMachine)
                        machine.setStatus(BallotScannerMachine.ACTIVE);
            }


            /**
             * Handler for the provisional-authorize message. Sets the nonce for
             * that machine's voting session.
             */
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent e) {

                AMachine m = getMachineForSerial(e.getTargetSerial());

                if (m != null && m instanceof VoteBoxBooth)
                    ((VoteBoxBooth) m).setNonce(e.getNonce().toVerbatim());

            }
        });

        try {
            /* Connect to the network and announce this machine's status */
            auditorium.connect();
            auditorium.announce(getStatus());
        }
        catch (NetworkException e1) {
        	/* NetworkException represents a recoverable error so just note it and continue */
            System.out.println("Recoverable error occurred: "+e1.getMessage());
            e1.printStackTrace(System.err);
        }

        /* Start the heartbeat timer */
        statusTimer.start();
    }

    /**
     * Broadcasts this supervisor's status, and resets the status timer
     */
    public void broadcastStatus() {
        auditorium.announce(getStatus());
        statusTimer.restart();
    }

    /**
     * A method for retrieving the parameters of the election
     */
    public IAuditoriumParams getParams(){
        return auditoriumParams;
    }

    /**
     * Introduces a new ballot to the BallotStore for use in the election. Extracts precinct name and creates a mapping
     * between them
     *
     * @param ballotFile        java File object referencing a new ballot to be handled by this STAR-Vote election
     */
    public void addPrecinct (File ballotFile) {

        /* Get the file name */
        String fileName = ballotFile.getName();

        try {

            /* Pare off the precinct information */
            String precinctID = fileName.substring(fileName.length()-7,fileName.length()-4);

            PublicKey publicKey = (PublicKey) auditoriumParams.getKeyStore().loadAdderKey("public");

            Precinct precinct = new Precinct(precinctID, ballotFile.getAbsolutePath(), publicKey);

            precincts.put(precinctID, precinct);
        }
        /* If we get an exception on the file, show a dialog indicating as much. This is good error handling, methinks */
        catch(NumberFormatException e){ JOptionPane.showMessageDialog(null, "Please choose a valid ballot"); }
    }

    /**
     * Will spoil ballot by removing it from the committedBids structure, return true if a bid was removed
     *
     * @param bid       the ID of the ballot to be removed
     * @return          whether or not a bid was actually spoiled
     */
    public boolean spoilBallot(String bid) {

        ASExpression nonce;
        Ballot ballot;
        Precinct p = getPrecinctWithBID(bid);

        if (p != null) {
            nonce = p.getNonce(bid);
            ballot = p.challengeBallot(bid);

            /* Announce that a ballot was spoiled */
            auditorium.announce(new SpoilBallotEvent(mySerial, nonce, bid, ballot.toListExpression().toVerbatim()));

            return true;
        }
        else return false;
    }

    /**
     *
     * @param bid
     * @return
     */
    private Precinct getPrecinctWithBID(String bid) {

        /* If we have the ballot, return it */
        for (Map.Entry<String,Precinct> m : precincts.entrySet())
            if (m.getValue().hasBID(bid)) return m.getValue();

        return null;
    }

    /**
     *
     * @param ballotFile
     * @return
     */
    private Precinct getPrecinctWithBallot(String ballotFile){

        /* If we have the ballotFile, remove it */
        for (Map.Entry<String,Precinct> m : precincts.entrySet())
            if (m.getValue().getBallotFile().equals(ballotFile)) return m.getValue();

        return null;
    }

    /**
     * @return          Set of entries of mappings of PIDs to Precincts
     */
    public String[] getPrecinctIDs(){
        return (String[]) precincts.keySet().toArray();
    }

    /**
     * @return          first precinct in the ballot manager's precinct list
     */
    public Precinct getInitialSelection(){
        return precincts.firstEntry().getValue();
    }

    /**
     * this method is used to generate a PIN to be stored and used by a voter
     *
     * @param precinctID        3-digit precinct number
     * @return                  new 5-digit pin as String
     */
    public String generatePIN(String precinctID){
        return pinValidator.generatePIN(precinctID);
    }

    /**
     * Checks if any of the VoteBoxes are currently in a voting session
     *
     * @return      true if there is an active voting session, false otherwise
     */
    public boolean isActiveVotingSession(){

        for (AMachine machine : machines)
            if(machine instanceof VoteBoxBooth && ((VoteBoxBooth) machine).isInUse())
                return true;

        return false;
    }


    /**
     * A test method for reading a testBallot from file
     */
    public void readTestBallot(){

        /* Open the files. */
        String testBallotFilename = "BallotSExpression.out";
        File file1 = new File ("CurrentSession" + testBallotFilename);
        String testNonceFilename = "NonceByteArray.out";
        File file2 = new File (testNonceFilename);

        /* Create the readers. */
        BufferedReader reader1;
        BufferedReader reader2;

        try {
            reader1 = new BufferedReader(new FileReader(file1.getAbsoluteFile()));

            String ballotString = "";
            String currentLine;

            while((currentLine = reader1.readLine()) != null)
                ballotString += currentLine;

            reader1.close();
            ASExpression ballot = StringExpression.make(ballotString);

            reader2 = new BufferedReader(new FileReader(file2.getAbsoluteFile()));
            String nonceString = "";

            while((currentLine = reader2.readLine()) != null)
                nonceString += currentLine;

            reader2.close();
            nonceString = nonceString.substring(1, nonceString.length()-1);

            byte[] nonce = Base64.decode(nonceString);
            ASExpression testNonce = StringExpression.makeString(nonce);

            System.out.println(ballot);
            System.out.println("============================");
            System.out.println(testNonce);
        }
        catch (IOException e) {
            System.out.println("Unable to read from files.");
            e.printStackTrace();
        }
    }

}
