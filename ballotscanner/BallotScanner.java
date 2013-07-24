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
import java.util.InputMismatchException;
import java.util.Observer;
import java.util.Scanner;

public class BallotScanner{

    private final AuditoriumParams _constants;


    private int numConnections;
    private VoteBoxAuditoriumConnector auditorium;
    private final int mySerial;
    private boolean connected;
    private Timer statusTimer;
    private boolean activated;
    private ObservableEvent activatedObs;
    private BallotScannerUI frame;

    /* Event variables. */
    private int label;
    private int protectedCount;
    private int publicCount;
    private int battery = 100;
    private Event<Integer> labelChangedEvent;


    // stores the last found result obtained from a successful code scan
    private String lastFoundBID = "";

//    private IWebcam webcam;
//    private Code128Decoder decoder;

    // keeps the path to the "ballot scanned" mp3
//    private String bsMp3Path = "sound/ballotscanned.mp3"; //move to the .conf file

    // keeps the mp3Player
//    private Player mp3Player;

    // how long a result is stored in memory before it is cleared
    private boolean receivedResponse;


    /**
     * Equivalent to new BallotScanner(-1).
     */
    public BallotScanner() {
        this(-1);
    }

    /**
     * Constructs a new instance of a persistent ballot scanner.  This
     * implementation runs in the background, on an auditorium network.
     *
     * @param serial the serial number of the votebox
     */
    public BallotScanner(int serial) {
        _constants = new AuditoriumParams("bs.conf");

//        if (_constants.useScanConfirmationSound()) {
//            bsMp3Path = _constants.getConfirmationSoundPath();
//        }



        if (serial != -1)
            mySerial = serial;
        else
            mySerial = _constants.getDefaultSerialNumber();

        if (mySerial == -1)
            throw new RuntimeException("usage: BallotScanner <machineID>");

        numConnections = 0;

        activatedObs = new ObservableEvent();

        labelChangedEvent = new Event<Integer>();

        statusTimer = new Timer(300000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isConnected()) {
                    auditorium.announce(getStatus());
                }
            }
        });

        //webcam = new FrameGrabberWebcam();

        //webcam.startCapture();

        //decoder = new Code128Decoder();

        //Set up the JFrame confirmation screen
        frame = new BallotScannerUI(_constants.getElectionName());
    }

    /**
     * Register to be notified when this BallotScanner's active status changes
     *
     * @param obs the observer
     */
    public void registerForActivated(Observer obs) {
        activatedObs.addObserver(obs);
    }

    /**
     * Returns this booth's status as a VoteBoxEvent, used for periodic
     * broadcasts
     *
     * @return the status
     */
    public BallotScannerEvent getStatus() {
        BallotScannerEvent event;
        // choosing to not require bs to be activated (for now)
        if (isActivated()) {
            event = new BallotScannerEvent(mySerial, label, "active", battery, protectedCount, publicCount);
        }
        else {
            event = new BallotScannerEvent(mySerial, label, "inactive", battery, protectedCount, publicCount);
        }
        return event;
    }

    /**
     * @return whether this BallotScanner is active
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Sets this BallotScanner's active status
     *
     * @param activated the activated to set
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
        activatedObs.notifyObservers();
        if(activated)
            frame.updateFrame(BallotScannerUI.TO_PROMPT_STATE);
        else
            frame.updateFrame(BallotScannerUI.TO_INACTIVE_STATE);
    }

    /**
     * @return whether this ballot scanner is connected to any machines
     */
    public boolean isConnected() {
        return connected;
    }

    public void beginScanning(){
        new Thread(new Runnable() {
            public void run() {

                Scanner scanner = new Scanner(System.in);
                long lastFoundTime = 0;

                while(true){

                    //BinaryBitmap bitmap = webcam.getBitmap();
                    try{
                        lastFoundBID = scanner.nextInt()+"";
                    } catch (InputMismatchException e){
                        lastFoundBID = "-1";
                    }

                    if(frame.state.getStateName().equals(PromptState.SINGLETON.getStateName()) && receivedResponse){

                        //decoder = new Code128Decoder();

                        long start = System.currentTimeMillis();

                        //lastFoundBID = decoder.decode(bitmap);

                        if(start - lastFoundTime > 5000){
                            if(lastFoundBID != null){
                                receivedResponse = false;

                                auditorium.announce(new BallotScannedEvent(mySerial, lastFoundBID));

                                //This is sort of redundant now that our scanners beep
                                // play confirmation sound
                               /* new Thread() {
                                    public void run() {

                                        // prepare the mp3Player
                                        try {
                                            FileInputStream fileInputStream = new FileInputStream(bsMp3Path);
                                            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                                            mp3Player = new Player(bufferedInputStream);
                                            mp3Player.play();
                                        } catch (Exception e) {
                                            mp3Player = null;
                                            System.out.println("Problem playing audio: " + bsMp3Path);
                                            System.out.println(e);
                                        }

                                    }
                                }.start();*/

                                lastFoundTime = System.currentTimeMillis();
                            }
                        }
                    }
            }
        }}).start();


    }



    /**
     * Main method which right now just goes into an infinite while loop, constantly scanning
     */
    public void start() {

        try {
            auditorium = new VoteBoxAuditoriumConnector(mySerial,
                    _constants, BallotScanAcceptedEvent.getMatcher(),
                    BallotScanRejectedEvent.getMatcher(),
                    StartScannerEvent.getMatcher(),
                    PollMachinesEvent.getMatcher()
            );
        } catch (NetworkException e1) {
            //NetworkException represents a recoverable error
            //  so just note it and continue
            System.err.println("Recoverable error occurred: " + e1.getMessage());
            e1.printStackTrace(System.err);
        }

        try {
            auditorium.connect();
            auditorium.announce(getStatus());
        } catch (NetworkException e1) {
            throw new RuntimeException(e1);
        }

        auditorium.addListener(new VoteBoxEventListener() {
            public void ballotCounted(BallotCountedEvent e) {
            }


            public void castCommittedBallot(CastCommittedBallotEvent event) {
            }

            public void commitBallot(CommitBallotEvent e) {
            }

            public void activated(ActivatedEvent e) {
            }

            public void assignLabel(AssignLabelEvent e) {
                if (e.getNode() == mySerial){
                    label = e.getLabel();
                }//if

                labelChangedEvent.notify(label);
            }

            public void authorizedToCast(AuthorizedToCastEvent e) {
            }

            public void ballotReceived(BallotReceivedEvent e) {
            }

            /**
             * Increment the number of connections
             */
            public void joined(JoinEvent e) {
                ++numConnections;
                connected = true;

            }

            public void lastPollsOpen(LastPollsOpenEvent e) {
            }

            /**
             * Decrement the number of connections
             */
            public void left(LeaveEvent e) {
                --numConnections;
                if (numConnections == 0) connected = false;
            }

            public void overrideCancel(OverrideCancelEvent e) {
            }

            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {
            }

            public void overrideCancelDeny(OverrideCancelDenyEvent e) {
            }

            public void overrideCast(OverrideCastEvent e) {
            }

            public void overrideCastConfirm(OverrideCastConfirmEvent e) {
            }

            public void overrideCastDeny(OverrideCastDenyEvent e) {
            }

            public void pollsClosed(PollsClosedEvent e) {
            }

            public void pollsOpen(PollsOpenEvent e) {
            }

            public void pollsOpenQ(PollsOpenQEvent e) {
            }

            public void supervisor(SupervisorEvent e) {
            }

            public void ballotScanner(BallotScannerEvent e) {
            }

            public void votebox(VoteBoxEvent e) {
            }

            public void ballotScanned(BallotScannedEvent e) {
            }

            public void pinEntered(PINEnteredEvent event) {
            }

            public void invalidPin(InvalidPinEvent event) {
            }

            public void pollStatus(PollStatusEvent pollStatusEvent) {
            }

            public void ballotPrintSuccess(BallotPrintSuccessEvent e) {
            }

            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {}

            public void uploadCastBallots(CastBallotUploadEvent castBallotUploadEvent) {}

            public void uploadChallengedBallots(ChallengedBallotUploadEvent challengedBallotUploadEvent) {}

            public void pollMachines(PollMachinesEvent pollMachinesEvent) {
                auditorium.announce(getStatus());
            }

            public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {
            }

            public void announceProvisionalBallot(ProvisionalBallotEvent provisionalBallotEvent) {
            }

            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent provisionalAuthorizeEvent) {
            }

            public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent) {
            }

            public void authorizedToCastWithNIZKS(AuthorizedToCastWithNIZKsEvent e) {
            }

            public void ballotAccepted(BallotScanAcceptedEvent event){

                System.out.println("Accepted event: Event BID: " + event.getBID());
                System.out.println("Accepted event: Last BID: " + lastFoundBID);

                //If this event corresponds with our last scanned ballot, display a confirmation message
                if(lastFoundBID.equals(event.getBID())){
                    receivedResponse = true;
                    //frame.displayBallotAcceptedScreen(lastFoundBID);
                    frame.updateFrame(BallotScannerUI.TO_ACCEPT_STATE);
                    long start = System.currentTimeMillis();
                    while(System.currentTimeMillis() - start < 5000);
                    //frame.displayPromptScreen();
                    frame.updateFrame(BallotScannerUI.TO_PROMPT_STATE);

                }

            }

            public void ballotRejected(BallotScanRejectedEvent event){
                System.out.println("Rejected event: Event BID: " + event.getBID());
                System.out.println("Rejected event: Last BID: " + lastFoundBID);

                //If our ballot was rejected, display a message
                if(lastFoundBID.equals(event.getBID())){
                    receivedResponse = true;
                    //frame.displayBallotRejectedScreen();
                    frame.updateFrame(BallotScannerUI.TO_REJECT_STATE);
                    long start = System.currentTimeMillis();
                    while(System.currentTimeMillis() - start < 5000);
                    //frame.displayPromptScreen();
                    frame.updateFrame(BallotScannerUI.TO_PROMPT_STATE);
                }
            }


            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {}

            public void scannerStart(StartScannerEvent startScannerEvent) {
               setActivated(true);
            }


        });

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
        BallotScanner bs;
        if (args.length == 1)
            bs = new BallotScanner(Integer.parseInt(args[0]));
        else
            //Tell VoteBox to refer to its config file for the serial number
            bs = new BallotScanner();

//        while(!bs.activated);

        bs.start();
    }
}