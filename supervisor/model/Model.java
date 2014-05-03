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
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;
import javax.swing.Timer;

import edu.uconn.cse.adder.PrivateKey;
import edu.uconn.cse.adder.PublicKey;

import sexpression.ASExpression;
import sexpression.NoMatch;
import sexpression.StringExpression;
import sexpression.stream.Base64;
import supervisor.model.tallier.ChallengeDelayedTallier;
import supervisor.model.tallier.ChallengeDelayedWithNIZKsTallier;
import supervisor.model.tallier.ITallier;
import crypto.interop.AdderKeyManipulator;
import votebox.events.*;
import auditorium.AuditoriumCryptoException;
import auditorium.Key;
import auditorium.NetworkException;
import auditorium.IAuditoriumParams;

/**
 * The main model of the Supervisor in the model-view-controller. Contains the status of the machines, and of
 * the election in general. Manages nearly all administrative functions of the election, including accepting machines
 * onto the network, generating and traking voters and pins, authorizing machines, opening and closing elections, and
 * much more. Also contains a link to Auditorium, for broadcasting (and hearing) messages on the network.
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

    /**
     * A mapping of precinct names to their talliers
     * that is thread-safe
     */
    private ConcurrentHashMap<String, ITallier> talliers;

    private Timer statusTimer;

    private IAuditoriumParams auditoriumParams;

    private HashMap<String, ASExpression> committedBids;

    //private Key privateKey = null;

    private ASExpression testBallot;
    private ASExpression testNonce;
    private String testBallotFilename = "BallotSExpression.out";
    private String testNonceFilename = "NonceByteArray.out";

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
        keyword = "";
        ballotLocation = "ballot.zip";
//        talliers = new Tallier();
        talliers = new ConcurrentHashMap<String, ITallier>();
        committedBids = new HashMap<String, ASExpression>();
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
        ArrayList<AMachine> unlabeled = new ArrayList<AMachine>();
        int maxlabel = 0;
        for (AMachine m : machines) {
            if (m.isOnline()) {
                IAnnounceEvent s = null;
                if (m instanceof SupervisorMachine)
                {
                    SupervisorMachine ma = (SupervisorMachine) m;
                    if (ma.getStatus() == SupervisorMachine.ACTIVE)
                        s = new SupervisorEvent(0, 0, "active");
                    else if (ma.getStatus() == SupervisorMachine.INACTIVE)
                        s = new SupervisorEvent(0, 0, "inactive");
                }
                else if (m instanceof VoteBoxBooth)
                {
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
                }
                else if(m instanceof BallotScannerMachine)
                {
                        BallotScannerMachine ma = (BallotScannerMachine)m;
                        if(ma.getStatus() == BallotScannerMachine.ACTIVE)
                        {
                            s = new BallotScannerEvent(ma.getSerial(), ma.getLabel(), "active",
                                    ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());
                        }
                        else if(ma.getStatus() == BallotScannerMachine.INACTIVE)
                        {
                            s = new BallotScannerEvent(ma.getSerial(), ma.getLabel(), "inactive",
                                    ma.getBattery(), ma.getProtectedCount(), ma.getPublicCount());
                        }
                        if (ma.getLabel() == 0)
                            unlabeled.add(ma);
                        else if (ma.getLabel() > maxlabel)
                            maxlabel = ma.getLabel();
                }
                else if(m instanceof  TapMachine){
                    TapMachine machine = (TapMachine)m;

                    s = new TapMachineEvent(machine.getSerial());
                }
                if (s == null)
                    throw new IllegalStateException("Unknown machine or status");
                statuses.add(new StatusEvent(0, m.getSerial(), s));
            }
        }
        auditorium.announce(new ActivatedEvent(mySerial, statuses));
        for (AMachine machine : unlabeled)
        {
            auditorium.announce(new AssignLabelEvent(mySerial, machine.getSerial(), ++maxlabel));
        }


        if (!pollsOpen)
            auditorium.announce(new PollsOpenQEvent(mySerial, keyword));

        sendStartScannerEvent();
    }

    /**
     * Sends a StartScannerEvent.
     */
    public void sendStartScannerEvent ()
    {
        auditorium.announce(new StartScannerEvent( mySerial ));
    }


    /**
     * Authorizes a VoteBox booth
     * 
     * @param node the serial number of the booth
     * @throws IOException
     */
    public void authorize(int node) throws IOException {
        byte[] nonce = new byte[256];
        for (int i = 0; i < 256; i++)
            nonce[i] = (byte) (Math.random() * 256);

        File file = new File(ballotLocation);
        String precinct = BallotStore.getPrecinctByBallot(ballotLocation);
        String ballotHash = BallotStore.createBallotHash(node);
        BallotStore.mapPrecinct(ballotHash, precinct);
//        System.out.println(ballotLocation);
//        System.out.println("<+++++++"+ precinct);
        FileInputStream fin = new FileInputStream(file);
        byte[] ballot = new byte[(int) file.length()];
        fin.read(ballot);

        ASExpression ASENonce = StringExpression.makeString(nonce);

        //try{
            if(!this.auditoriumParams.getEnableNIZKs()){
                auditorium.announce(new AuthorizedToCastEvent(mySerial, node, /*ASExpression.makeVerbatim(nonce)*/ASENonce,
                        precinct, ballot));
            }else{
                auditorium.announce(new AuthorizedToCastWithNIZKsEvent(mySerial, node,
                        /*ASExpression.makeVerbatim(nonce)*/ASENonce, precinct, ballot,
                        AdderKeyManipulator.generateFinalPublicKey((PublicKey)auditoriumParams.getKeyStore().loadAdderKey("public"))));
            }
        /*} catch (InvalidVerbatimStreamException e) {
            throw new RuntimeException(e);
        }*/
    }

    /**
     * Authorizes a VoteBox booth for a provisional voting session
     *
     * @param node
     *            the serial number of the booth
     * @throws IOException
     */
    private void provisionalAuthorize(int node) throws IOException{
        byte[] nonce = new byte[256];
        for (int i = 0; i < 256; i++)
            nonce[i] = (byte) (Math.random() * 256);

        File file = new File(ballotLocation);
        FileInputStream fin = new FileInputStream(file);
        byte[] ballot = new byte[(int) file.length()];
        fin.read(ballot);

        auditorium.announce(new ProvisionalAuthorizeEvent(mySerial, node, nonce, ballot));
    }

    /**
     * Closes the polls
     * 
     * @return the output from the tally
     */
    public Map<String, Map<String, BigInteger>> closePolls() {
        auditorium
                .announce(new PollsClosedEvent(mySerial, new Date().getTime()));
        //return tallier.getReport(privateKey);
        HashMap<String, Map<String, BigInteger>> out = new HashMap<String, Map<String, BigInteger>>();

        for(String t : talliers.keySet()){
            out.put(t, talliers.get(t).getReport());
        }

        auditorium.announce(new CastBallotUploadEvent(mySerial, BallotStore.getCastNonces()));
        auditorium.announce(new ChallengedBallotUploadEvent(mySerial, BallotStore.getDecryptedBallots((PublicKey) auditoriumParams.getKeyStore().loadAdderKey("public"),
                (PrivateKey) auditoriumParams.getKeyStore().loadAdderKey("private"))));

        return out;
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
                    CastCommittedBallotEvent.getMatcher(), LastPollsOpenEvent.getMatcher(),
                    OverrideCastConfirmEvent.getMatcher(), PollsClosedEvent.getMatcher(),
                    PollsOpenEvent.getMatcher(), PollsOpenQEvent.getMatcher(),
                    SupervisorEvent.getMatcher(), VoteBoxEvent.getMatcher(),
                    EncryptedCastBallotEvent.getMatcher(), CommitBallotEvent.getMatcher(),
                    EncryptedCastBallotWithNIZKsEvent.getMatcher(), AuthorizedToCastWithNIZKsEvent.getMatcher(),
                    PINEnteredEvent.getMatcher(), InvalidPinEvent.getMatcher(),
                    PollStatusEvent.getMatcher(), BallotPrintSuccessEvent.getMatcher(),
                    BallotScannedEvent.getMatcher(), BallotScannerEvent.getMatcher(),
                    ProvisionalCommitEvent.getMatcher(), ProvisionalAuthorizeEvent.getMatcher(),
                    TapMachineEvent.getMatcher());

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
                    ((VoteBoxBooth) m).setNonce(e.getNonce().toVerbatim());
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
            public void castCommittedBallot(CastCommittedBallotEvent e) {
//            	AMachine m = getMachineForSerial(e.getSerial());
//                if (m != null && m instanceof BallotScannerMachine) {
//                    auditorium.announce(new BallotCountedEvent(mySerial, e
//                            .getSerial(), ((StringExpression) e.getNonce())
//                            .getBytes(), "", ""));
//
//
//                    String precinct = BallotStore.getPrecinctByBID(e.getBID().toString());
//                    talliers.get(precinct).confirmed(e.getNonce());
//                }
            }

            /**
             * Handler for a joined event. When a new machine joins, check and
             * see if it exists, and set it to online if so. Also increment the
             * number of connections.
             */
            public void joined(JoinEvent e) {

                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null ) {
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
//                if (numConnected == 0) {
//                    setConnected(false);
//                    setActivated(false);
//                }
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
                if (m != null && m instanceof BallotScannerMachine) {
                    //TODO Make this work with ballot hashes
                    /*String precinct = bManager.getPrecinctByBID(e.getBID().toString());
                    talliers.get(precinct).confirmed(e.getNonce());*/
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
                BallotStore.closeHashChain();
                System.out.println("Polls Closing!");
                if(BallotStore.isHashChainCompromised()){
                    JOptionPane.showMessageDialog(null, "ERROR: The hash chain is incomplete, votes may have been removed or tampered with!");
                }
            }

            /**
             * Handler for the polls-open event. Sets the polls to open.
             */
            public void pollsOpen(PollsOpenEvent e){

                //Moving this code so that a new tallier is created when a new ballot is
                //So we can have precinct-by-precinct tallying
                
/*            	if(auditoriumParams.getUseCommitChallengeModel()){
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
            	}//if*/
            	
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
             * Handler for a ballotScanner (status) event. Adds the machine if it
             * hasn't been seen, and updates its status if it has.
             */
            public void ballotScanner(BallotScannerEvent e) {
                AMachine m = getMachineForSerial(e.getSerial());

                if (m == null) {
                    m = new BallotScannerMachine(e.getSerial());
                    System.out.println("Ballot Scanner Added: " + m);
                    machines.add(m);
                    machinesChangedObs.notifyObservers();
                }

                if (m != null && !(m instanceof BallotScannerMachine))
                    throw new IllegalStateException(
                            "Machine "
                                    + e.getSerial()
                                    + " is not a ballotScanner, but broadcast ballotScanner message");

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
                //Check to see if this votebox has a conflicting label

                if (e.getLabel() > 0){
                    for(AMachine machine : machines){
                        if(machine.getLabel() == e.getLabel() && machine != m){
                            //If there is a conflict, relabel this (the event generator) machine.
                            int maxlabel = 0;
                            for(AMachine ma : machines){
                                if(ma instanceof BallotScannerMachine)
                                    maxlabel = Math.max(maxlabel, ma.getLabel());
                            }//for

                            auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), maxlabel + 1));
                            return;
                        }
                    }
                }//if
                if (e.getLabel() > 0)
                    bsm.setLabel(e.getLabel());
                else {
                    if (activated) {
                        if (bsm.getLabel() > 0)
                        {
                            auditorium.announce(new AssignLabelEvent(mySerial, e.getSerial(), bsm.getLabel()));
                        }
                        else {
                            int maxlabel = 0;
                            for (AMachine ma : machines) {
                                if (ma instanceof BallotScannerMachine && ma.getLabel() > maxlabel)
                                {
                                    maxlabel = ma.getLabel();
                                }
                            }
                            auditorium.announce(new AssignLabelEvent(mySerial, e
                                    .getSerial(), maxlabel + 1));
                        }
                        auditorium.announce(new PollStatusEvent(mySerial, e.getSerial(), pollsOpen ? 1:0 ));

                    }
                }
            }



            /**
             * Handler for a supervisor (status) event. Adds the machine if it
             * hasn't been seen, and updates its status if it has.
             */
            public void supervisor(SupervisorEvent e) {
                auditorium.announce(new PollMachinesEvent(mySerial, new Date().getTime(),
                        keyword));
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
                else if (e.getStatus().equals("provisional-in-use"))
                    booth.setStatus(VoteBoxBooth.PROVISIONAL);
                else
                    throw new IllegalStateException("Invalid VoteBox Status: "
                            + e.getStatus());
                booth.setBattery(e.getBattery());
                booth.setProtectedCount(e.getProtectedCount());
                booth.setPublicCount(e.getPublicCount());
                booth.setOnline(true);
                
                //Check to see if this votebox has a conflicting label
                if (e.getLabel() > 0){
                	for(AMachine machine : machines){
                		if(machine.getLabel() == e.getLabel() && machine != m){
                			//If there is a conflict, relabel this (the event generator) machine.
                			int maxlabel = 0;
                			for(AMachine ma : machines){
                				if(ma instanceof VoteBoxBooth)
                					maxlabel = Math.max(maxlabel, ma.getLabel());
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
                                        && ma.getLabel() > maxlabel)
                                    maxlabel = ma.getLabel();
                            }
                            auditorium.announce(new AssignLabelEvent(mySerial, e
                                    .getSerial(), maxlabel + 1));
                        }
                        auditorium.announce(new PollStatusEvent(mySerial, e.getSerial(), pollsOpen ? 1:0 ));
                    }
                }
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
                    BallotStore.addBallot(e.getBID().toString(), e.getBallot());
                    BallotStore.mapPrecinct(e.getBID().toString(), e.getPrecinct().toString());
                    BallotStore.setPrecinctByBID(e.getBID().toString(), e.getPrecinct().toString());
                    BallotStore.testMapPrint();
                    auditorium.announce(new BallotReceivedEvent(mySerial, e.getSerial(),
                            ((StringExpression) e.getNonce())
                            .getBytes(), e.getBID().toString(), e.getPrecinct().toString()));

                    String precinct = BallotStore.getPrecinctByBID(e.getBID().toString());
                    talliers.get(precinct).recordVotes(e.getBallot().toVerbatim(), e.getNonce());
                    String bid = e.getBID().toString();
                    committedBids.put(bid, e.getNonce());

                    /*/ Write the nonce and ballot to files, for testing purposes. /////////////////////////////////////////////////////////////////////////////////////////////////
                    // Open the file.
                    File file1 = new File (testBallotFilename);
                    File file2 = new File (testNonceFilename);

                    // If the file does not exist, then print error.
                    boolean file1Existed = file1.exists();
                    boolean file2Existed = file2.exists();
                    boolean newFile1Created = false;
                    boolean newFile2Created = false;
                    if (!file1Existed)
                    {
                        try
                        {
                            newFile1Created = file1.createNewFile();
                        }
                        catch (IOException eio)
                        {
                            System.out.println("Unable to create new file " + testBallotFilename);
                            eio.printStackTrace();
                            return;
                        }
                    }
                    if (!file2Existed)
                    {
                        try
                        {
                            newFile2Created = file2.createNewFile();
                        }
                        catch (IOException eio)
                        {
                            System.out.println("Unable to create new file " + testNonceFilename);
                            eio.printStackTrace();
                            return;
                        }
                    }
                    // Create the writer.
                    BufferedWriter writer1;
                    BufferedWriter writer2;
                    try
                    {
                        if ((file1Existed || newFile1Created) && (file2Existed || newFile2Created))
                        {
                            writer1 = new BufferedWriter(new FileWriter(file1.getAbsoluteFile()));
                            writer1.write(e.getBallot().toString());
                            writer1.close();
                            writer2 = new BufferedWriter(new FileWriter(file2.getAbsoluteFile()));
                            writer2.write(e.getNonce().toString());
                            writer2.close();
                        }
                    }
                    catch (IOException eio)
                    {
                        System.out.println("Unable to write to file.");
                        eio.printStackTrace();
                        return;
                    }//*/
                }
            }

            /**
             * Handler for the ProvisionalCommmitEvent. Recieves ballot as it would with a normal ballot
             */
            public void provisionalCommitBallot(ProvisionalCommitEvent e) {

                AMachine m = getMachineForSerial(e.getSerial());
                if (m != null && m instanceof VoteBoxBooth) {
                    VoteBoxBooth booth = (VoteBoxBooth) m;
                    booth.setPublicCount(booth.getPublicCount() + 1);
                    booth.setProtectedCount(booth.getProtectedCount() + 1);
                    auditorium.announce(new BallotReceivedEvent(mySerial, e
                            .getSerial(), ((StringExpression) e.getNonce())
                            .getBytes(), e.getBID().toString(), BallotStore.getPrecinctByBallot(e.getBID().toString())));
                }
            }

            public void authorizedToCastWithNIZKS(AuthorizedToCastWithNIZKsEvent e) {
                // NO-OP
            }

            /**
             * Ocurrs once when tap joins the network and sends it's respective TapMachineEvent. Model adds a new
             * instance of a TapMachine to it's list of machines for further reference.
             */
            public void tapMachine(TapMachineEvent tapMachineEvent) {
                AMachine m = getMachineForSerial(tapMachineEvent.getSerial());
                if(m != null && !(m instanceof TapMachine)){
                    throw new IllegalStateException("Machine " +
                                                   tapMachineEvent.getSerial() +
                                                   " is not a Tap but broadcasted TapMachineEvent");
                }else if(m == null){
                    TapMachine tap = new TapMachine(tapMachineEvent.getSerial());
                    tap.setOnline(true);
                    machines.add(tap);
                    machinesChangedObs.notifyObservers();
                } else {
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
                BallotStore.addBallot("711567939", testBallot);
                bManager.setPrecinctByBID("711567939", "007");
                talliers.get("007").recordVotes(testBallot.toVerbatim(), testNonce);*/

                String bid = e.getBID();
                int serial = e.getSerial();
                if (committedBids.containsKey(bid)){
                    //System.err.println("Got inside the if clause");
                    //ASExpression nonce = committedBids.get(bid);
                    ASExpression nonce = committedBids.remove(bid);
                    ASExpression ballot = BallotStore.castCommittedBallot(e.getBID());




                    String precinct = BallotStore.getPrecinctByBID(e.getBID());
                        talliers.get(precinct).confirmed(nonce);

                    // used to be in voteBox registerForCommit listener.
                    if(auditoriumParams.getCastBallotEncryptionEnabled()){
                        if(auditoriumParams.getEnableNIZKs()){
                            System.out.println("announcing an EncryptedCastBallotWithNIZKsEvent");
                            auditorium.announce(new EncryptedCastBallotWithNIZKsEvent(serial, nonce, ballot, StringExpression.makeString(e.getBID())));
                        } else{
                            System.out.println("announcing an EncryptedCastBallotEvent");
                            auditorium.announce(new EncryptedCastBallotEvent(serial, nonce, ballot, StringExpression.makeString(e.getBID())));
                        }
                    }
                    else{
                        System.out.println("Announcing a CastCommittedBallotEvent");
                        auditorium.announce(new CastCommittedBallotEvent(serial, nonce, StringExpression.makeString(e.getBID())));
                    // that should trigger my own castBallot listener.
                    }

                    System.out.println("Sending scan confirmation!");
                    System.out.println("BID: " + bid);
                    auditorium.announce(new BallotScanAcceptedEvent(mySerial, bid));
                } else {
                    System.err.println("Got inside the else clause");
                    System.out.println("Sending scan rejection!");
                    System.out.println("BID: " + bid);
                    auditorium.announce(new BallotScanRejectedEvent(mySerial, bid));
                }

            }

            /**
             * Handler for the PinEnteredEvent. Retrieves the entered pin from the event and authorizes the booth if the
             * pin is valid.
             */
            public synchronized void pinEntered(PINEnteredEvent e){
                if(isPollsOpen()) {
                    System.out.println(">>> PIN entered: " + e.getPin());
                    String ballot = BallotStore.getBallotByPin(e.getPin());
                    if(ballot!=null){
                        try {
                            System.out.println(BallotStore.getPrecinctByBallot(ballot));
                            setBallotLocation(ballot);
                            if(BallotStore.getPrecinctByBallot(ballot).contains("provisional")) {
                                provisionalAuthorize(e.getSerial());
                                System.out.println(">>>>>>> It's working!");
                            }
                            else
                                authorize(e.getSerial());
                        }
                        catch(IOException ex) {
                            System.err.println(ex.getMessage());
                        }
                    }
                    else {
                        auditorium.announce(new InvalidPinEvent(mySerial, e.getSerial(), e.getNonce()));
                    }
                }
            }

            public void invalidPin(InvalidPinEvent e) {}

            /**
             * Handler for PollStatusEvent
             */
            public void pollStatus(PollStatusEvent pollStatusEvent) {
                pollsOpen = pollStatusEvent.getPollStatus()==1;
                sendStartScannerEvent();
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
                //NO-OP
            }

            public void ballotPrintFail(BallotPrintFailEvent e) {
                //NO-OP
            }

            public void uploadCastBallots(CastBallotUploadEvent e) {
                // NO-OP
            }

            public void uploadChallengedBallots(ChallengedBallotUploadEvent e) {
                // NO-OP
            }

            /**
             * Handler for StartScannerEvent. Activates scanner if present.
             */
            public void scannerStart(StartScannerEvent e) {
                for (AMachine machine:machines)
                {
                    if (machine instanceof BallotScannerMachine)
                    {
                        machine.setStatus(BallotScannerMachine.ACTIVE);
                    }
                }
            }
            public void pollMachines(PollMachinesEvent e) {
                // NO-OP
            }

            public void spoilBallot(SpoilBallotEvent e) {
                // NO-OP
            }

            public void announceProvisionalBallot(ProvisionalBallotEvent e) {
                // NO-OP
            }

            /**
             * Handler for the provisional-authorize message. Sets the nonce for
             * that machine.
             */
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent e) {
                AMachine m = getMachineForSerial(e.getNode());
                if (m != null && m instanceof VoteBoxBooth) {
                    ((VoteBoxBooth) m).setNonce(e.getNonce());
                }
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

    /**
     * Introduces a new ballot to the ballotmanager for use in the election. Extracts precinct name and creates a mapping
     * between them
     *
     * @param fileIn java File object referencing a new ballot to be handled by this STAR-Vote election
     */
    public void addBallot(File fileIn) {
        String fileName = fileIn.getName();
        try{
            String precinct = fileName.substring(fileName.length()-7,fileName.length()-4);
            ITallier tallier = null;

                try {
                    if(!auditoriumParams.getEnableNIZKs()){
                        //Loading privateKey well in advance so the whole affair is "fail-fast"
                        Key privateKey = auditoriumParams.getKeyStore().loadKey(mySerial + "-private");
                        tallier = new ChallengeDelayedTallier(privateKey);
                    }else{
                        //Loading privateKey well in advance so the whole affair is "fail-fast"
                        PrivateKey privateKey = (PrivateKey)auditoriumParams.getKeyStore().loadAdderKey("private");
                        PublicKey publicKey = (PublicKey) auditoriumParams.getKeyStore().loadAdderKey("public");

                        tallier = new ChallengeDelayedWithNIZKsTallier(publicKey, privateKey);

                    }//if
                } catch (AuditoriumCryptoException e1) {
                    System.err.println("Crypto error encountered: "+e1.getMessage());
                    e1.printStackTrace();
                }


            if(tallier != null && !talliers.keySet().contains(tallier))
                talliers.put(precinct, tallier);
            else
                throw new RuntimeException("Tallier was not properly initialized for precinct " + precinct);

            BallotStore.addBallot(precinct, fileIn.getAbsolutePath());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Please choose a valid ballot");
        }
    }

    /**
     * Will spoil ballot by removing it from the commtedBids structure, return true if a bid was removed
     * @param bid
     * @return whether or not a bid was actually spoiled
     */
    public boolean spoilBallot(String bid){
        if(committedBids.containsKey(bid)){
            ASExpression nonce = committedBids.remove(bid);
            auditorium.announce(new SpoilBallotEvent(mySerial, bid, nonce));
            return true;
        }
        else
            return false;

    }

    /**
     * this method is used to generate a pin to be stored and used by a voter
     *
     * @param precinct 3-digit precinct number
     * @return new pin as String
     */
    public String generatePin(String precinct){
        return BallotStore.generatePin(precinct);
    }

    /**
     * same as generatePin but for provisional ballots
     *
     * @param precinct
     * @return
     */
    public String generateProvisionalPin(String precinct){
        return BallotStore.generateProvisionalPin(precinct);
    }

    /**
     *  @return array of precincts
     */
    public String[] getSelections(){
        return BallotStore.getSelections();
    }

    /**
     * @return first precinct in the ballot manager's precinct list
     */
    public String getInitialSelection(){
        return BallotStore.getInitialSelection();
    }

    /**
     * A test method for reading a testBallot from file
     */
    public void readTestBallot(){
        // Open the files.
        File file1 = new File ("CurrentSession" + testBallotFilename);
        File file2 = new File (testNonceFilename);

        // Create the readers.
        BufferedReader reader1;
        BufferedReader reader2;
        try
        {
            reader1 = new BufferedReader(new FileReader(file1.getAbsoluteFile()));
            String ballotString = "";
            String currentLine;
            while((currentLine = reader1.readLine()) != null)
            {
                ballotString += currentLine;
            }
            reader1.close();
            ASExpression ballot = StringExpression.make(ballotString);

            reader2 = new BufferedReader(new FileReader(file2.getAbsoluteFile()));
            String nonceString = "";
            while((currentLine = reader2.readLine()) != null)
            {
                nonceString += currentLine;
            }
            reader2.close();
            nonceString = nonceString.substring(1, nonceString.length()-1);
            byte[] nonce = Base64.decode(nonceString);
            testBallot = ballot;
            testNonce = StringExpression.makeString(nonce);

            System.out.println(testBallot);
            System.out.println("============================");
            System.out.println(testNonce);
        }
        catch (IOException e)
        {
            System.out.println("Unable to read from files.");
            e.printStackTrace();
        }
    }

}
