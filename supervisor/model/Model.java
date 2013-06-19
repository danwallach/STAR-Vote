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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import edu.uconn.cse.adder.PrivateKey;
import edu.uconn.cse.adder.PublicKey;

import sexpression.ASExpression;
import sexpression.NoMatch;
import sexpression.StringExpression;
import supervisor.model.tallier.ChallengeDelayedTallier;
import supervisor.model.tallier.ChallengeDelayedWithNIZKsTallier;
import supervisor.model.tallier.EncryptedTallier;
import supervisor.model.tallier.EncryptedTallierWithNIZKs;
import supervisor.model.tallier.ITallier;
import supervisor.model.tallier.Tallier;
import votebox.crypto.interop.AdderKeyManipulator;
import votebox.events.*;
import auditorium.AuditoriumCryptoException;
import auditorium.Key;
import auditorium.NetworkException;
import auditorium.IAuditoriumParams;

/**
 * The main model of the Supervisor. Contains the status of the machines, and of
 * the election in general. Also contains a link to Auditorium, for broadcasting
 * (and hearing) messages on the network.
 * 
 * @author cshaw
 */
public class Model {

    private TreeSet<AMachine> machines;

    private ObservableEvent machinesChangedObs;

    private VoteBoxAuditoriumConnector auditorium;

    private int mySerial;

    private boolean activated;

    private ObservableEvent activatedObs;

    private boolean connected;

    private ObservableEvent connectedObs;

    private boolean pollsOpen;

    private ObservableEvent pollsOpenObs;

    private int numConnected;

    private String keyword;

    private String ballotLocation;

    private ITallier tallier;

    private Timer statusTimer;

    private IAuditoriumParams auditoriumParams;

    private HashMap<String, ASExpression> commitedBids;

    private BallotManager bManager;

    private ArrayList<Integer> expectedBallots;

    //private Key privateKey = null;

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
        
    	if(serial != -1)
        	this.mySerial = serial;
    	else
    		this.mySerial = params.getDefaultSerialNumber();
        
    	if(mySerial == -1)
    		throw new RuntimeException("Expected serial number in configuration file if not on command line");
    	
        machines = new TreeSet<AMachine>();
        machinesChangedObs = new ObservableEvent();
        activatedObs = new ObservableEvent();
        connectedObs = new ObservableEvent();
        pollsOpenObs = new ObservableEvent();
        expectedBallots = new ArrayList<Integer>();
        bManager = new BallotManager();
        keyword = "";
        ballotLocation = "ballot.zip";
        tallier = new Tallier();
        commitedBids = new HashMap<String, ASExpression>();
        statusTimer = new Timer(300000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isConnected()) {
                    auditorium.announce(getStatus());
                }
            }
        });
    }

    /**
     * Activates this supervisor.<br>
     * Formats the activated message (containing the statuses of known
     * machines), and labels any unlabeled machines.
     */
    public void activate() {
        ArrayList<StatusEvent> statuses = new ArrayList<StatusEvent>();
        ArrayList<VoteBoxBooth> unlabeled = new ArrayList<VoteBoxBooth>();
        int maxlabel = 0;
        for (AMachine m : machines) {
            if (m.isOnline()) {
                IAnnounceEvent s = null;
                if (m instanceof SupervisorMachine) {
                    SupervisorMachine ma = (SupervisorMachine) m;
                    if (ma.getStatus() == SupervisorMachine.ACTIVE)
                        s = new SupervisorEvent(0, 0, "active");
                    else if (ma.getStatus() == SupervisorMachine.INACTIVE)
                        s = new SupervisorEvent(0, 0, "inactive");
                } else if (m instanceof VoteBoxBooth) {
                    VoteBoxBooth ma = (VoteBoxBooth) m;
                    if (ma.getStatus() == VoteBoxBooth.READY)
                        s = new VoteBoxEvent(0, ma.getLabel(), "ready", ma
                                .getBattery(), ma.getProtectedCount(), ma
                                .getPublicCount());
                    else if (ma.getStatus() == VoteBoxBooth.IN_USE)
                        s = new VoteBoxEvent(0, ma.getLabel(), "in-use", ma
                                .getBattery(), ma.getProtectedCount(), ma
                                .getPublicCount());
                    if (ma.getLabel() == 0)
                        unlabeled.add(ma);
                    else if (ma.getLabel() > maxlabel)
                        maxlabel = ma.getLabel();
                } else if(m instanceof BallotScannerMachine){

                    BallotScannerMachine ma = (BallotScannerMachine)m;
                    if(ma.getStatus() == BallotScannerMachine.ACTIVE){
                        s = new BallotScannerEvent(ma.getSerial(), ma.getLabel(), "active",
                                ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());
                    } else if(ma.getStatus() == BallotScannerMachine.INACTIVE){
                        s = new BallotScannerEvent(ma.getSerial(), ma.getLabel(), "inactive",
                                ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());
                    }
                }
                if (s == null)
                    throw new IllegalStateException("Unknown machine or status");
                statuses.add(new StatusEvent(0, m.getSerial(), s));
            }
        }
        auditorium.announce(new ActivatedEvent(mySerial, statuses));
        for (VoteBoxBooth b : unlabeled) {
            auditorium.announce(new AssignLabelEvent(mySerial, b.getSerial(),
                    ++maxlabel));
        }
        if (!pollsOpen)
            auditorium.announce(new PollsOpenQEvent(mySerial, keyword));
    }

    /**
     * Authorizes a VoteBox booth
     * 
     * @param node
     *            the serial number of the booth
     * @throws IOException
     */
    public void authorize(int node) throws IOException {
        byte[] nonce = new byte[256];
        for (int i = 0; i < 256; i++)
            nonce[i] = (byte) (Math.random() * 256);

        File file = new File(ballotLocation);
        FileInputStream fin = new FileInputStream(file);
        byte[] ballot = new byte[(int) file.length()];
        fin.read(ballot);

        if(!this.auditoriumParams.getEnableNIZKs()){
        	auditorium.announce(new AuthorizedToCastEvent(mySerial, node, nonce,
                ballot));
        }else{
        	auditorium.announce(new AuthorizedToCastWithNIZKsEvent(mySerial, node,
        			nonce, ballot,
        			AdderKeyManipulator.generateFinalPublicKey((PublicKey)auditoriumParams.getKeyStore().loadAdderKey("public"))));
        }
    }

    /**
     * Closes the polls
     * 
     * @return the output from the tally
     */
    public Map<String, BigInteger> closePolls() {
        auditorium
                .announce(new PollsClosedEvent(mySerial, new Date().getTime()));
        //return tallier.getReport(privateKey);
        return tallier.getReport();
    }

    /**
     * Gets the index in the list of machines of the machine with the given
     * serial
     * 
     * @param serial
     * @return the index
     */
    public int getIndexForSerial(int serial) {
        int i = 0;
        for (AMachine m : machines)
            if (m.getSerial() == serial)
                return i;
            else
                ++i;
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
     * @param serial
     * @return the machine
     */
    public AMachine getMachineForSerial(int serial) {
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
        SupervisorEvent event;
        if (isActivated())
            event = new SupervisorEvent(mySerial, new Date().getTime(),
                    "active");
        else
            event = new SupervisorEvent(mySerial, new Date().getTime(),
                    "inactive");
        return event;
    }

    /**
     * @return whether this supervisor is active
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * @return whether this supervisor is connected to any machines
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return whether the polls are open
     */
    public boolean isPollsOpen() {
        return pollsOpen;
    }

    /**
     * Opens the polls.
     */
    public void openPolls() {
        auditorium.announce(new PollsOpenEvent(mySerial, new Date().getTime(),
                keyword));
    }

    /**
     * Sends an override-cancel request to a VoteBox booth
     * 
     * @param node
     *            the serial number of the booth
     */
    public void overrideCancel(int node) {
        byte[] nonce = ((VoteBoxBooth) getMachineForSerial(node)).getNonce();
        if (nonce == null)
        {
            System.err.println("ERROR: VoteBox machine has no associated nonce!");
            throw new RuntimeException("VoteBox machine has no associated nonce!");
        }
        auditorium.announce(new OverrideCancelEvent(mySerial, node, nonce));
    }

    /**
     * Sends an override-cast request to a VoteBox booth
     * 
     * @param node
     *            the serial number of the booth
     */
    public void overrideCast(int node) {
        byte[] nonce = ((VoteBoxBooth) getMachineForSerial(node)).getNonce();
        auditorium.announce(new OverrideCastEvent(mySerial, node, nonce));
    }

    /**
     * Register to be notified when this supervisor's active status changes
     * 
     * @param obs
     *            the observer
     */
    public void registerForActivated(Observer obs) {
        activatedObs.addObserver(obs);
    }

    /**
     * Register to be notified when this supervisor's connected status changes
     * 
     * @param obs
     *            the observer
     */
    public void registerForConnected(Observer obs) {
        connectedObs.addObserver(obs);
    }

    /**
     * Register to be notified when the list of machines changes
     * 
     * @param obs
     *            the observer
     */
    public void registerForMachinesChanged(Observer obs) {
        machinesChangedObs.addObserver(obs);
    }

    /**
     * Register to be notified when the polls open status changes
     * 
     * @param obs
     *            the observer
     */
    public void registerForPollsOpen(Observer obs) {
        pollsOpenObs.addObserver(obs);
    }

    /**
     * Sets this supervisor's active status
     * 
     * @param activated
     *            the activated to set
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
        activatedObs.notifyObservers();
    }

    /**
     * Sets this supervisor's ballot location
     * 
     * @param newLoc
     *            the ballot location
     */
    public void setBallotLocation(String newLoc) {
        ballotLocation = newLoc;
    }

    /**
     * Sets this supervisor's connected status
     * 
     * @param connected
     *            the connected to set
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
        connectedObs.notifyObservers();
    }

    /**
     * Sets the election keyword
     * 
     * @param keyword
     *            the keyword to set
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Sets this supervisor's polls open status
     * 
     * @param pollsOpen
     *            the pollsOpen to set
     */
    public void setPollsOpen(boolean pollsOpen) {
        this.pollsOpen = pollsOpen;
        pollsOpenObs.notifyObservers();
    }

    /**
     * Starts auditorium, registers the event listener, and connects to the
     * network.
     */
    public void start() {
        try {
            auditorium = new VoteBoxAuditoriumConnector(mySerial,
                    auditoriumParams, ActivatedEvent.getMatcher(),
                    AssignLabelEvent.getMatcher(), AuthorizedToCastEvent.getMatcher(),
                    CastBallotEvent.getMatcher(), LastPollsOpenEvent.getMatcher(),
                    OverrideCastConfirmEvent.getMatcher(), PollsClosedEvent.getMatcher(),
                    PollsOpenEvent.getMatcher(), PollsOpenQEvent.getMatcher(),
                    SupervisorEvent.getMatcher(), VoteBoxEvent.getMatcher(),
                    EncryptedCastBallotEvent.getMatcher(), CommitBallotEvent.getMatcher(),
                    CastCommittedBallotEvent.getMatcher(), ChallengeResponseEvent.getMatcher(),
                    ChallengeEvent.getMatcher(), EncryptedCastBallotWithNIZKsEvent.getMatcher(),
                    AuthorizedToCastWithNIZKsEvent.getMatcher(), AdderChallengeEvent.getMatcher(),
                    PinEnteredEvent.getMatcher(), InvalidPinEvent.getMatcher(),
                    PollStatusEvent.getMatcher(), BallotPrintSuccessEvent.getMatcher(),
                    BallotScannedEvent.getMatcher(), BallotScannerEvent.getMatcher());


        } catch (NetworkException e1) {
            throw new RuntimeException(e1);
        }

        auditorium.addListener(new VoteBoxEventListener() {

        	public void ballotCounted(BallotCountedEvent e){
        		//NO-OP
        	}
        	
            /**
             * Handler for the activated message. Sets all other supervisors
             * (including this one, if necessary) to the inactive state. Also
             * checks to see if this machine's status is listed, and responds
             * with it if not.
             */
            public void activated(ActivatedEvent e) {            	
                for (AMachine m : machines) {
                    if (m instanceof SupervisorMachine) {
                        if (m.getSerial() == e.getSerial())
                            m.setStatus(SupervisorMachine.ACTIVE);
                        else
                            m.setStatus(SupervisorMachine.INACTIVE);
                    }
                }
                if (e.getSerial() == mySerial)
                    setActivated(true);
                else {
                    setActivated(false);
                    boolean found = false;
                    for (StatusEvent ae : e.getStatuses()) {
                        if (ae.getNode() == mySerial) {
                            SupervisorEvent se = (SupervisorEvent) ae
                                    .getStatus();
                            if (!se.getStatus().equals("inactive"))
                                broadcastStatus();
                            found = true;
                        }
                    }
                    if (!found) broadcastStatus();
                }
            }

            /**
             * Handler for the assign-label message. Sets that machine's label.
             */
            public void assignLabel(AssignLabelEvent e) {
                AMachine m = getMachineForSerial(e.getNode());
                if (m != null) {
                    m.setLabel(e.getLabel());
                    machines.remove(m);
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }
            }

            /**
             * Handler for the authorized-to-cast message. Sets the nonce for
             * that machine.
             */
            public void authorizedToCast(AuthorizedToCastEvent e) {
                AMachine m = getMachineForSerial(e.getNode());
                if (m != null && m instanceof VoteBoxBooth) {
                    ((VoteBoxBooth) m).setNonce(e.getNonce());
                }
            }

            public void ballotReceived(BallotReceivedEvent e) {
            	//NO-OP
            }

            /**
             * Handler for the cast-ballot message. Increments the booth's
             * public and protected counts, replies with ballot-received, and
             * stores the votes in the tallier.
             */
            public void castBallot(CastBallotEvent e) {
            	AMachine m = getMachineForSerial(e.getSerial());
                if (m != null && m instanceof VoteBoxBooth) {
                    
                    //If we're using the commit-challenge model, then the ballot is already cached and we
                    // just need to confirm it
                    if(auditoriumParams.getUseCommitChallengeModel()){
                    	auditorium.announce(new BallotCountedEvent(mySerial, e
                                .getSerial(), ((StringExpression) e.getNonce())
                                .getBytes()));
                    	
                    	tallier.confirmed(e.getNonce());
                    }else{
                    	auditorium.announce(new BallotReceivedEvent(mySerial, e
                                .getSerial(), ((StringExpression) e.getNonce())
                                .getBytes()));
                    	
                    	//Otherwise, we need to count the whole thing.
                        VoteBoxBooth booth = (VoteBoxBooth) m;
                        booth.setPublicCount(booth.getPublicCount() + 1);
                        booth.setProtectedCount(booth.getProtectedCount() + 1);
                    	tallier.recordVotes(e.getBallot().toVerbatim(), e.getNonce());	
                    }
                }
            }

            /**
             * Handler for a joined event. When a new machine joins, check and
             * see if it exists, and set it to online if so. Also increment the
             * number of connections.
             */
            public void joined(JoinEvent e) {
                System.out.println(">>> model reported a machine joined!");
                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null) {
                    m.setOnline(true);
                }
                numConnected++;
                setConnected(true);
                machinesChangedObs.notifyObservers();
            }

            /**
             * Handler for the last-polls-open message. If the keywords match,
             * set the polls to open (without sending a message).
             */
            public void lastPollsOpen(LastPollsOpenEvent e) {
                PollsOpenEvent e2 = e.getPollsOpenMsg();
                if (e2.getKeyword().equals(keyword))
                    setPollsOpen(true);
            }

            /**
             * Handler for a left event. Set the machine to offline, and
             * decrement the number of connections. If we are no longer
             * connected to any machines, assume we're offline and deactivate.<br>
             * The supervisor needs to deactivate when it goes offline so that
             * when it comes back on, it needs to be activated again so it can
             * get a fresh list of machines and their statuses. Also, that way
             * you cannot have two machines activate separately and then join
             * the network, giving you two active supervisors.
             */
            public void left(LeaveEvent e) {            	
                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null) {
                    m.setOnline(false);
                }else{
                    throw new RuntimeException("WARNING: Machine left without having been registered");
                }
                numConnected--;
                if (numConnected == 0) {
                    setConnected(false);
                    setActivated(false);
                }
                machinesChangedObs.notifyObservers();
            }

            public void overrideCancel(OverrideCancelEvent e) {
                // NO-OP
            }

            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {
                // NO-OP
            }

            public void overrideCancelDeny(OverrideCancelDenyEvent e) {
                // NO-OP
            }

            public void overrideCast(OverrideCastEvent e) {
                // NO-OP
            }
            
            /**
             * Handler for the override-cast-confirm event. Similar to
             * cast-ballot, but no received reply is sent.
             */
            public void overrideCastConfirm(OverrideCastConfirmEvent e) {            	
                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null && m instanceof VoteBoxBooth) {
                    VoteBoxBooth booth = (VoteBoxBooth) m;
                    booth.setPublicCount(booth.getPublicCount() + 1);
                    booth.setProtectedCount(booth.getProtectedCount() + 1);
                    tallier.recordVotes(e.getBallot(), StringExpression.makeString(e.getNonce()));
                }
            }

            public void overrideCastDeny(OverrideCastDenyEvent e) {
                // NO-OP
            }

            /**
             * Handler for the polls-closed event. Sets the polls to closed.
             */
            public void pollsClosed(PollsClosedEvent e) {
                setPollsOpen(false);
            }

            /**
             * Handler for the polls-open event. Sets the polls to open, and
             * gets a fresh tallier.
             * @throws AuditoriumCryptoException 
             */
            public void pollsOpen(PollsOpenEvent e){
                
            	if(auditoriumParams.getUseCommitChallengeModel()){
    				try {
						if(!auditoriumParams.getEnableNIZKs()){
							//Loading privateKey well in advance so the whole affair is "fail-fast"
							Key privateKey = auditoriumParams.getKeyStore().loadKey("private");
							tallier = new ChallengeDelayedTallier(privateKey);
						}else{
							//Loading privateKey well in advance so the whole affair is "fail-fast"
							PrivateKey privateKey = (PrivateKey)auditoriumParams.getKeyStore().loadAdderKey("private");
							PublicKey publicKey = (PublicKey)auditoriumParams.getKeyStore().loadAdderKey("public");
							tallier = new ChallengeDelayedWithNIZKsTallier(publicKey, privateKey);
						}//if
					} catch (AuditoriumCryptoException e1) {
						System.err.println("Crypto error encountered: "+e1.getMessage());
						e1.printStackTrace();
					}
            	}else{
            		//If Encryption is not enabled, use a vanilla tallier
            		if(!auditoriumParams.getCastBallotEncryptionEnabled()){
            			if(auditoriumParams.getEnableNIZKs())
            				throw new RuntimeException("Encryption must be enabled to use NIZKs");
            			
            			//privateKey = null;
            			tallier = new Tallier();
            		}else{
            			//Otherwise, grab the private key and allocate an encrypted tallier
            			try{
            				if(!auditoriumParams.getEnableNIZKs()){
	            				//Loading privateKey well in advance so the whole affair is "fail-fast"
	            				Key privateKey = auditoriumParams.getKeyStore().loadKey("private");
	            				tallier = new EncryptedTallier(privateKey);
            				}else{
            					//Loading privateKey well in advance so the whole affair is "fail-fast"
            					PrivateKey privateKey = (PrivateKey)auditoriumParams.getKeyStore().loadAdderKey("private");
            					PublicKey publicKey = (PublicKey)auditoriumParams.getKeyStore().loadAdderKey("public");
            					tallier = new EncryptedTallierWithNIZKs(publicKey, privateKey);
            				}//if
            			}catch(AuditoriumCryptoException e1){
            				System.err.println("Crypto error encountered: "+e1.getMessage());
    						e1.printStackTrace();
            			}//catch
            		}//if
            	}//if
            	
                setPollsOpen(true);
            }

            /**
             * Handler for the polls-open? event. Searches the machine's log,
             * and replies with a last-polls-open message if an appropriate
             * polls-open message is found.
             */
            public void pollsOpenQ(PollsOpenQEvent e) {
                if (e.getSerial() != mySerial) {
                    // TODO: Search the log and extract an appropriate polls-open message

                    ASExpression res = null;
                    if (res != null && res != NoMatch.SINGLETON) {
                        VoteBoxEventMatcher matcher = new VoteBoxEventMatcher(
                                PollsOpenEvent.getMatcher());
                        PollsOpenEvent event = (PollsOpenEvent) matcher.match(
                                0, res);
                        if (event != null
                                && event.getKeyword().equals(e.getKeyword()))
                            auditorium.announce(new LastPollsOpenEvent(
                                    mySerial, event));
                    }
                }
            }

            /**
             * Handler for a ballotscanner (status) event. Adds the machine if it
             * hasn't been seen, and updates its status if it has.
             */
            public void ballotscanner(BallotScannerEvent e) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>If you cannot read this, then program execution never gets here!");
                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null && !(m instanceof BallotScannerMachine))
                    throw new IllegalStateException(
                            "Machine "
                                    + e.getSerial()
                                    + " is not a ballotscanner, but broadcasted ballotscanner message");
                if (m == null) {
                    m = new BallotScannerMachine(e.getSerial());
                    System.out.println("Ballot Scanner Added: " + m);
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }
                BallotScannerMachine bsm = (BallotScannerMachine) m;
                if(e.getStatus().equals("active")) {
                    bsm.setStatus(BallotScannerMachine.ACTIVE);
                } else if (e.getStatus().equals("inactive"))
                    bsm.setStatus(BallotScannerMachine.INACTIVE);
                else
                    throw new IllegalStateException("Invalid BallotScanner Status: "
                            + e.getStatus());
                bsm.setBattery(e.getBattery());
                bsm.setProtectedCount(e.getProtectedCount());
                bsm.setPublicCount(e.getPublicCount());
                bsm.setOnline(true);
                System.out.println(">>>>>>>>>>>>>>>>>>>>>Trying to get a label to assign to scanner");
                //Check to see if this votebox has a conflicting label

                //TODO Apparently this doesn't do what it says it does....
                if (e.getLabel() > 0){
                    for(AMachine machine : machines){
                        if(machine.getLabel() == e.getLabel() && machine != m){
                            //If there is a conflict, relabel this (the event generator) machine.
                            int maxlabel = 0;
                            for(AMachine ma : machines){
                                if(ma instanceof BallotScannerMachine)
                                    maxlabel = (int)Math.max(maxlabel, ma.getLabel());
                            }//for

                            auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), maxlabel + 1));
                            return;
                        }
                    }
                }//if
                System.out.println(">>>>>>>>>>>>>>>>>>>>>Trying to assign label.");
                if (e.getLabel() > 0)
                    bsm.setLabel(e.getLabel());
                else {
                    if (activated) {
                        if (bsm.getLabel() > 0)
                            auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), bsm.getLabel()));
                        else {
                            int maxlabel = 0;
                            for (AMachine ma : machines) {
                                if (ma instanceof BallotScannerMachine && ((BallotScannerMachine) ma).getLabel() > maxlabel)
                                {
                                    maxlabel = ((BallotScannerMachine) ma).getLabel();
                                }
                            }
                            auditorium.announce(new AssignLabelEvent(mySerial, e
                                    .getSerial(), maxlabel + 1));
                        }
                        System.out.println(">>>>>>>>>>>>>>>>>>>>>Sending PollStatusEvent");
                        auditorium.announce(new PollStatusEvent(mySerial, e.getSerial(), pollsOpen ? 1:0 ));
                    }
                }
            }



            /**
             * Handler for a supervisor (status) event. Adds the machine if it
             * hasn't been seen, and updates its status if it has.
             */
            public void supervisor(SupervisorEvent e) {
                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null && !(m instanceof SupervisorMachine))
                    throw new IllegalStateException(
                            "Machine "
                                    + e.getSerial()
                                    + " is not a supervisor, but broadcasted supervisor message");
                if (m == null) {
                    m = new SupervisorMachine(e.getSerial(),
                            e.getSerial() == mySerial);
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }
                SupervisorMachine sup = (SupervisorMachine) m;
                if (e.getStatus().equals("active")) {
                    sup.setStatus(SupervisorMachine.ACTIVE);
                    if (e.getSerial() != mySerial)
                        setActivated(false);
                } else if (e.getStatus().equals("inactive"))
                    sup.setStatus(SupervisorMachine.INACTIVE);
                else
                    throw new IllegalStateException(
                            "Invalid Supervisor Status: " + e.getStatus());
                sup.setOnline(true);
            }

            /**
             * Handler for a votebox (status) event. Adds the machine if it
             * hasn't been seen, or updates the status if it has. Also, if the
             * booth is unlabeled and this is the active supervisor, labels the
             * booth with its previous label if known, or the next available
             * number.
             */
            public void votebox(VoteBoxEvent e) {
                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null && !(m instanceof VoteBoxBooth))
                    throw new IllegalStateException(
                            "Machine "
                                    + e.getSerial()
                                    + " is not a booth, but broadcasted votebox message");
                if (m == null) {
                    m = new VoteBoxBooth(e.getSerial());
                    System.out.println("Vote Box Added: " + m);
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }
                VoteBoxBooth booth = (VoteBoxBooth) m;
                if (e.getStatus().equals("ready"))
                    booth.setStatus(VoteBoxBooth.READY);
                else if (e.getStatus().equals("in-use"))
                    booth.setStatus(VoteBoxBooth.IN_USE);
                else
                    throw new IllegalStateException("Invalid VoteBox Status: "
                            + e.getStatus());
                booth.setBattery(e.getBattery());
                booth.setProtectedCount(e.getProtectedCount());
                booth.setPublicCount(e.getPublicCount());
                booth.setOnline(true);
                
                //Check to see if this votebox has a conflicting label
                //TODO Apparently this doesn't do what it says it does....
                if (e.getLabel() > 0){
                	for(AMachine machine : machines){
                		if(machine.getLabel() == e.getLabel() && machine != m){
                			//If there is a conflict, relabel this (the event generator) machine.
                			int maxlabel = 0;
                			for(AMachine ma : machines){
                				if(ma instanceof VoteBoxBooth)
                					maxlabel = (int)Math.max(maxlabel, ma.getLabel());
                			}//for
                			
                                auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), maxlabel + 1));
                			return;
                		}
                	}
                }//if
                
                if (e.getLabel() > 0)
                    booth.setLabel(e.getLabel());
                else {
                    if (activated) {
                        if (booth.getLabel() > 0)
                            auditorium.announce(new AssignLabelEvent(mySerial, e
                                    .getSerial(), booth.getLabel()));
                        else {
                            int maxlabel = 0;
                            for (AMachine ma : machines) {
                                if (ma instanceof VoteBoxBooth
                                        && ((VoteBoxBooth) ma).getLabel() > maxlabel)
                                    maxlabel = ((VoteBoxBooth) ma).getLabel();
                            }
                            auditorium.announce(new AssignLabelEvent(mySerial, e
                                    .getSerial(), maxlabel + 1));
                        }
                        auditorium.announce(new PollStatusEvent(mySerial, e.getSerial(), pollsOpen ? 1:0 ));
                    }
                }
            }

            /**
             * Indicate to the tallier that the vote in question is being challenged,
             * and as such should be excluded from the final tally.
             */
            public void challengeResponse(ChallengeResponseEvent e) {
            	//NO-OP
            }
            
            /**
             * Indicate to the tallier that the vote in question is being challenged,
             * and as such should be excluded from the final tally.
             */
            public void challenge(ChallengeEvent e) {
            	System.out.println("Received challenge: "+e);
            	
            	tallier.challenged(e.getNonce());
            	auditorium.announce(new ChallengeResponseEvent(mySerial, 
            			e.getSerial(), e.getNonce()));
            }

            /**
             * Record the vote received in the commit event.
             * It should not yet be tallied.
             */
            public void commitBallot(CommitBallotEvent e) {
            	AMachine m = getMachineForSerial(e.getSerial());
                if (m != null && m instanceof VoteBoxBooth) {
                    VoteBoxBooth booth = (VoteBoxBooth) m;
                    booth.setPublicCount(booth.getPublicCount() + 1);
                    booth.setProtectedCount(booth.getProtectedCount() + 1);
                    auditorium.announce(new BallotReceivedEvent(mySerial, e
                            .getSerial(), ((StringExpression) e.getNonce())
                            .getBytes()));
                    tallier.recordVotes(e.getBallot().toVerbatim(), e.getNonce());
                    String bid = e.getBID().toString();
                    commitedBids.put(bid, e.getNonce());
                }
            }

            public void ballotScanned(BallotScannedEvent e) {
                String bid = e.getBID();
                int serial = e.getSerial();
                if (commitedBids.containsKey(bid)){
                    ASExpression nonce = commitedBids.get(bid);
                    BallotStore.castBallot(e.getBID(), nonce);
                    // used to be in voteBox registerForCommit listener.
                    auditorium.announce(new CastCommittedBallotEvent(serial, nonce));
                    // that should trigger my own castBallot listener.
                    System.out.println("Sending scan confirmation!");
                    auditorium.announce(new BallotScanAcceptedEvent(StringExpression.makeString(bid)));
                } else {
                    System.out.println("Sending scan rejection!");
                    auditorium.announce(new BallotScanRejectedEvent(mySerial, bid));
                }
            }

            public void pinEntered(PinEnteredEvent e){
                if(isPollsOpen()) {
                    String ballot = bManager.getBallotByPin(e.getPin());
                    if(ballot!=null){
                        try {
                            System.out.println(ballot + "!");
                            setBallotLocation(ballot);
                            authorize(e.getSerial());
                        }
                        catch(IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                    else {
                        auditorium.announce(new InvalidPinEvent(mySerial, e.getNonce()));
                    }
                }
            }

            public void invalidPin(InvalidPinEvent e) {}

            public void pollStatus(PollStatusEvent pollStatusEvent) {
                pollsOpen = pollStatusEvent.getPollStatus()==1;
            }


            public void ballotAccepted(BallotScanAcceptedEvent e){
                //NO-OP
            }

            public void ballotRejected(BallotScanRejectedEvent e){
                //NO-OP
            }


            public void ballotPrinting(BallotPrintingEvent e) {
                //NO-OP
            }

            public void ballotPrintSuccess(BallotPrintSuccessEvent e) {
                expectedBallots.add(Integer.valueOf(e.getBID()));
                //System.out.println("V"  + expectedBallots);
            }

            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {
                //NO-OP
            }

            public void uploadCastBallots(CastBallotUploadEvent castBallotUploadEvent) {
                // NO-OP
            }

            public void uploadChallengedBallots(ChallengedBallotUploadEvent challengedBallotUploadEvent) {
                // NO-OP
            }

        });

        try {
            auditorium.connect();
            auditorium.announce(getStatus());
        } catch (NetworkException e1) {
        	//NetworkException represents a recoverable error
        	//  so just note it and continue
            System.out.println("Recoverable error occurred: "+e1.getMessage());
            e1.printStackTrace(System.err);
        }

        statusTimer.start();
    }

    /**
     * Broadcasts this supervisor's status, and resets the status timer
     */
    public void broadcastStatus() {
        auditorium.announce(getStatus());
        statusTimer.restart();
    }

    public VoteBoxAuditoriumConnector getAuditoriumConnector() {
        return auditorium;
    }

    /**
     * A method for retrieving the parameters of the election
     */
    public IAuditoriumParams getParams(){
        return auditoriumParams;
    }

    //adds a new ballot to the ballot manager
    public void addBallot(File fileIn) {
        String fileName = fileIn.getName();
        try{
        int precinct = Integer.parseInt(fileName.substring(fileName.length()-7,fileName.length()-4));
        bManager.addBallot(precinct, fileIn.getAbsolutePath());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Please choose a valid ballot");
        }
    }

    public int generatePin(int precinct){
        return bManager.generatePin(precinct);
    }

    public Integer[] getSelections(){
        return bManager.getSelections();
    }

    public Integer getInitialSelection(){
        return bManager.getInitialSelection();
    }

    /**
     * A method that will generate a random pin for the voter to enter into his votebox machine
     */

}
