package ballotscanner;

import auditorium.Event;
import auditorium.NetworkException;
import ballotscanner.state.PromptState;
import supervisor.model.ObservableEvent;
import votebox.AuditoriumParams;
import votebox.events.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class  BallotScanner{

    private final AuditoriumParams _constants;


    private int numConnections;
    private VoteBoxAuditoriumConnector auditorium;
    private final int mySerial;

    private final String launchCode;
    private boolean connected;
    private Timer statusTimer;
    private boolean isActivated;
    private ObservableEvent activatedObs;
    private BallotScannerUI frame;

    /* Event variables. */
    private int label;
    private int protectedCount;
    private int publicCount;
    private int battery = 100;
    private Event<Integer> labelChangedEvent;


    /** slabelChangedEvent the last found result obtained from a successful code scan **/
    private String lastFoundBID = "";

    /** how long a result is stored in memory before it is cleared **/
    private boolean receivedResponse;

    /**
     * This is an empty constructor that sets the serial number to -1.
     * Equivalent to new BallotScanner(-1).
     *
     * This is used by default when no serial number is provided.
     */
    public BallotScanner() {
        this(-1, "0000000000");
    }

    /**
     * Constructs a new instance of a @BallotScanner.  This
     * implementation runs in the background on an auditorium network.
     *
     * @param serial the serial number to be assigned to the @BallotScanner
     *               input from the command line.
     */
    public BallotScanner(int serial, String launchCode) {

        /* Reads in the configuration file for the BallotScanner */
        _constants = new AuditoriumParams("bs.conf");

        /* TODO revise code */
        /* Check validity of serial before assigning */
        if (serial != -1)
            mySerial = serial;
        else 
        /* Try to get a new serial */
            mySerial = _constants.getDefaultSerialNumber();

        /* If there's still an invalid serial, throw a runtime exception(?) */    
        if (mySerial == -1)
            throw new RuntimeException("usage: BallotScanner <machineID>");

        /* Initialisations to set up network communication */
        numConnections = 0;
        activatedObs = new ObservableEvent();

        this.launchCode = launchCode;
        
        /* Allows BallotScanner to change label on AssignLabelEvent */
        labelChangedEvent = new Event<>();

        /* Sets up the heartbeat */
        statusTimer = new Timer(300000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isConnected()) {
                    auditorium.announce(getStatus());
                }
            }
        });

        /* Set up the BallotScanner UI */
        frame = new BallotScannerUI(_constants.getElectionName());
    }

    /**
     * TODO
     * Register to be notified when this BallotScanner's active status changes
     *
     * @param obs the observer
     *
     * public void registerForActivated(Observer obs) {
     *    activatedObs.addObserver(obs);
     *  }
     */

    /**
     * Returns this booth's status as a VoteBoxEvent, used for periodic
     * broadcasts (heartbeat).
     *
     * @return the status of the @BallotScanner
     */
    public BallotScannerEvent getStatus() {

        BallotScannerEvent event;
        String status = (isActivated()) ? "active" : "inactive";

        event = new BallotScannerEvent(mySerial, label, status, battery, protectedCount, publicCount);
        return event;
    }

    /**
     * @return whether this @BallotScanner is (in)active
     */
    public boolean isActivated() {
        return isActivated;
    }

    /**
     * Sets the BallotScanner state to active/inactive dependent on @isActivated;
     * broadcasts new status
     *
     * @param isActivated the isActivated to set
     */
    public void setActivated(boolean isActivated) {

        /* Set the status and broadcast new status to the network */
        this.isActivated = isActivated;
        activatedObs.notifyObservers();

        /* Update UI to reflect change */
        int state = isActivated ? BallotScannerUI.TO_PROMPT_STATE : BallotScannerUI.TO_INACTIVE_STATE;
        frame.updateFrame(state);
    }

    /**
     * @return whether this ballot scanner is connected to any machines
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Continuously checks for input from barcode scanner and broadcasts BallotScannedEvent
     * when a valid BID is input.
     */
    private void beginScanning(){

        /* Create a new Thread to continuously check for input */
        new Thread(new Runnable() {
            public void run() {

                /* Setting up for barcode scanning */
                Scanner scanner = new Scanner(System.in);
                long lastFoundTime = 0; // amount of time since last BID scanned

                while(true){
                    /* Check for new input from barcode scanner */
                    try{
                        lastFoundBID = scanner.nextLine();
                    } catch (NoSuchElementException e){ lastFoundBID = "-1"; }

                    /* TODO add a getter for frame.state, maybe verifyState() method */
                    String frameStateName   = frame.state.getStateName();
                    String promptStateName  = PromptState.SINGLETON.getStateName();

                    if(frameStateName.equals(promptStateName) && receivedResponse){

                        /* Set current time */
                        long curTime = System.currentTimeMillis();

                        /* If greater than 5 seconds have passed since the Supervisor responded to
                         * the last scanned valid BID, set flag and broadcast that a ballot was scanned
                         * along with the BID and wait for a response.
                         * 
                         * Waiting for 5 seconds to allow UI thread to display accept/reject and catch up.
                         */
                        if(curTime - lastFoundTime > 5000){
                            if(lastFoundBID != null){

                                /* Broadcast the BallotScannedEvent and change status to false 
                                  (awaiting response) */
                                auditorium.announce(new BallotScannedEvent(mySerial, lastFoundBID));
                                receivedResponse = false;

                                /* Update time since last BID scanned */
                                lastFoundTime = System.currentTimeMillis();

                            }
                        }

                    }
                }
            }
        }).start();

    }

    /**
     * Starting point for @BallotScanner. Sets up Listeners, starts Timers, starts thread for scanning
     * and connects the @BallotScanner to the network.
     */
    public void start() {

        /* Creates a VoteBoxAuditoriumConnector which will be used to connect to Auditorium */
        try {
            auditorium = new VoteBoxAuditoriumConnector(mySerial, _constants, launchCode,
                    BallotScanAcceptedEvent.getMatcher(),
                    BallotScanRejectedEvent.getMatcher(),
                    StartScannerEvent.getMatcher(),
                    PollMachinesEvent.getMatcher()
            );
        } catch (NetworkException e1) {
            /* NetworkException represents a recoverable error
               so just note it and continue */
            System.err.println("Recoverable error occurred: " + e1.getMessage());
            e1.printStackTrace(System.err);
        }

        /* Connects the BallotScanner to auditorium. BallotScanner announces its status to the network */
        try {
            auditorium.connect();
            auditorium.announce(getStatus());
        } catch (NetworkException e1) {
            throw new RuntimeException(e1);
        }

        auditorium.addListener(new VoteBoxEventListener() {

            public void castCommittedBallot(CastCommittedBallotEvent event) {}
            public void commitBallot(CommitBallotEvent e) {}
            public void activated(ActivatedEvent e) {}
            public void authorizedToCast(AuthorizedToCastEvent e) {}
            public void ballotReceived(BallotReceivedEvent e) {}
            public void overrideCancel(OverrideCancelEvent e) {}
            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {}
            public void overrideCancelDeny(OverrideCancelDenyEvent e) {}
            public void overrideCommit(OverrideCommitEvent e) {}
            public void overrideCommitConfirm(OverrideCommitConfirmEvent e) {}
            public void overrideCommitDeny(OverrideCommitDenyEvent e) {}
            public void pollsClosed(PollsClosedEvent e) {}
            public void pollsOpen(PollsOpenEvent e) {}
            public void pollsOpenQ(PollsOpenQEvent e) {}
            public void supervisor(SupervisorEvent e) {}
            public void ballotScanner(BallotScannerEvent e) {}
            public void votebox(VoteBoxEvent e) {}
            public void ballotScanned(BallotScannedEvent e) {}
            public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {}
            public void announceProvisionalBallot(ProvisionalBallotEvent provisionalBallotEvent) {}
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent provisionalAuthorizeEvent) {}
            public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent) {}
            public void tapMachine(TapMachineEvent tapMachineEvent) {}
            public void startUpload(StartUploadEvent startUploadEvent) {}
            public void completedUpload(CompletedUploadEvent completedUploadEvent) {}
            public void uploadBallots(BallotUploadEvent ballotUploadEvent) {}

            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {}
            public void pinEntered(PINEnteredEvent event) {}
            public void invalidPin(InvalidPinEvent event) {}
            public void pollStatus(PollStatusEvent pollStatusEvent) {}
            public void ballotPrintSuccess(BallotPrintSuccessEvent e) {}
            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {}
            public void lastPollsOpen(LastPollsOpenEvent e) {}

            public void assignLabel(AssignLabelEvent e) {
                if (e.getTargetSerial() == mySerial)
                    label = e.getLabel();

                labelChangedEvent.notify(label);
            }

            /** Increment the number of connections when other machines join the network */
            public void joined(JoinEvent e) {
                numConnections++;
                connected = true;
            }

            /** Decrement the number of connections when other machines leave the network */
            public void left(LeaveEvent e) {
                numConnections--;
                if (numConnections == 0) connected = false;
            }

            /** Responds to polling by announcing status */
            public void pollMachines(PollMachinesEvent pollMachinesEvent) {
                auditorium.announce(getStatus());
            }
            
            /** 
             * Responds to Supervisor announcing an accepted ballot by updating the GUI 
             * and setting @receivedResponse to true 
             */
            public void ballotAccepted(BallotScanAcceptedEvent event){

                System.out.println("Accepted event: Event BID: " + event.getBID());
                System.out.println("Accepted event: Last BID: " + lastFoundBID);

                /* If this event corresponds with our last scanned ballot, display a confirmation message. */
                if(lastFoundBID.equals(event.getBID())){
                    
                    /* Changes to reflect that the Supervisor responded to the BallotScannedEvent */
                    receivedResponse = true;

                    /* Changes UI to accepting state */
                    frame.updateFrame(BallotScannerUI.TO_ACCEPT_STATE);

                    /* Waits 5 seconds */
                    long start = System.currentTimeMillis();
                    while(System.currentTimeMillis() - start < 5000);

                    /* Changes UI back to prompt for a new BID to scan */
                    frame.updateFrame(BallotScannerUI.TO_PROMPT_STATE);

                }

            }

            /** 
             * Responds to Supervisor announcing a rejected ballot by updating the GUI 
             * and setting @receivedResponse to true 
             */
            public void ballotRejected(BallotScanRejectedEvent event){
                System.out.println("Rejected event: Event BID: " + event.getBID());
                System.out.println("Rejected event: Last BID: " + lastFoundBID);

                /* If this event corresponds with our last scanned ballot, display a rejection message. */
                if(lastFoundBID.equals(event.getBID())){
                    
                    /* Changes to reflect that the Supervisor responded to the BallotScannedEvent */
                    receivedResponse = true;

                    /* Changes UI to rejecting state */
                    frame.updateFrame(BallotScannerUI.TO_REJECT_STATE);

                    /* Waits 5 seconds */
                    long start = System.currentTimeMillis();
                    while(System.currentTimeMillis() - start < 5000);

                    /* Changes UI back to prompt for a new BID to scan */
                    frame.updateFrame(BallotScannerUI.TO_PROMPT_STATE);

                }
            }

            /** 
             * Changes the activated state to true when Supervisor announces a StartScanner Event
             */
            public void scannerStart(StartScannerEvent startScannerEvent) {
               setActivated(true);
            }


        });
    
        /* Starts the heartbeat timer and begins scanning */
        statusTimer.start();
        receivedResponse = true;
        beginScanning();
    }

    /**
     * Main entry point into the program. If an argument is given, it will be
     * the serial number, otherwise VoteBox will load a serial from its config file.
     *
     * @param args - arguments to be passed to the main method, from the command line
     */
    public static void main(String[] args) {

        String launchCode = "";
        while (launchCode == null || launchCode.equals(""))
            launchCode = JOptionPane.showInputDialog(null,
                    "Please enter today's election launch code:", "Launch Code",
                    JOptionPane.QUESTION_MESSAGE);
        
        BallotScanner bs;
        if (args.length == 1)
            bs = new BallotScanner(Integer.parseInt(args[0]), launchCode);
        else
            /* Tell VoteBox to refer to its config file for the serial number */
            bs = new BallotScanner();

        bs.start();
    }
}