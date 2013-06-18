package ballotscanner;

import auditorium.NetworkException;
import com.google.zxing.BinaryBitmap;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import javazoom.jl.player.Player;
import sexpression.*;
import supervisor.model.ObservableEvent;
import votebox.AuditoriumParams;
import votebox.events.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Observer;

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

    // stores the last found result obtained from a successful code scan
    private String lastFoundBID = "";

    private IWebcam webcam;
    private Code128Decoder decoder;

    // how long a result is stored in memory before it is cleared
    private final long DELAY_TIME = 5000;


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

        if (serial != -1)
            mySerial = serial;
        else
            mySerial = _constants.getDefaultSerialNumber();

        if (mySerial == -1)
            throw new RuntimeException("usage: BallotScanner <machineID>");

        numConnections = 0;

        activatedObs = new ObservableEvent();

        statusTimer = new Timer(300000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isConnected()) {
                    auditorium.announce(getStatus());
                }
            }
        });

        webcam = new FrameGrabberWebcam();

        webcam.startCapture();

        decoder = new Code128Decoder();

        //Set up the JFrame confirmation screen
        frame = new BallotScannerUI(_constants.getElectionName(), _constants.getConfirmationSoundPath());
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
//    if (isActivated()) {
        event = new BallotScannerEvent(mySerial, "active");
//    }
//    else {
//      event = new BallotScannerEvent(mySerial,"inactive");
//    }
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
    }

    /**
     * @return whether this ballot scanner is connected to any machines
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * method that starts the scanning process
     */
    public void scanBallot() {

        frame.displayPromptScreen();

        BinaryBitmap bitmap;

        do{
            long start = System.currentTimeMillis();
            while(System.currentTimeMillis() - start < 100);
            bitmap = webcam.getBitmap();
        }while((lastFoundBID = decoder.decode(bitmap)) == null);

        System.out.println(lastFoundBID);  //TODO Is this needed?

        auditorium.announce(new BallotScannedEvent(mySerial, lastFoundBID));
    }

    /**
     * Main method which right now just goes into an infinite while loop, constantly scanning
     */
    public void start() {

        try {
            auditorium = new VoteBoxAuditoriumConnector(mySerial,
                    _constants, BallotScanAcceptedEvent.getMatcher(),
                    BallotScanRejectedEvent.getMatcher()
            );
        } catch (NetworkException e1) {
            //NetworkException represents a recoverable error
            //  so just note it and continue
            System.out.println("Recoverable error occurred: " + e1.getMessage());
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


            public void castBallot(CastBallotEvent event) {
            }

            public void challenge(ChallengeEvent e) {
            }

            public void commitBallot(CommitBallotEvent e) {
            }

            public void activated(ActivatedEvent e) {
            }

            public void assignLabel(AssignLabelEvent e) {
            }

            public void authorizedToCast(AuthorizedToCastEvent e) {
            }

            public void ballotReceived(BallotReceivedEvent e) {
            }

            public void challengeResponse(ChallengeResponseEvent e) {
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

            public void ballotscanner(BallotScannerEvent e) {
            }

            public void votebox(VoteBoxEvent e) {
            }

            public void ballotScanned(BallotScannedEvent e) {
            }

            public void pinEntered(PinEnteredEvent event) {
            }

            public void invalidPin(InvalidPinEvent event) {
            }

            public void pollStatus(PollStatusEvent pollStatusEvent) {
            }

            public void ballotPrintSuccess(BallotPrintSuccessEvent e) {
            }

            @Override
            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void ballotAccepted(BallotScanAcceptedEvent event){

                //If this event corresponds with our last scanned ballot, display a confirmation message
                if(lastFoundBID.equals(event.getBID())){
                    frame.displayBallotAcceptedScreen(lastFoundBID);
                    Timer t = new Timer(5000, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Calling Scan Ballot After accepted ballot");
                            scanBallot();
                        }
                    });
                    t.setRepeats(false);
                    t.start();
                }

            }

            public void ballotRejected(BallotScanRejectedEvent event){
                System.out.println("Rejected event: Event BID: " + event.getBID());
                System.out.println("Rejected event: Last BID: " + lastFoundBID);

                //If our ballot was rejected, display a message
                if(lastFoundBID.equals(event.getBID())){
                    frame.displayBallotRejectedScreen();
                    Timer t = new Timer(5000, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Calling Scan Ballot After accepted ballot");
                            scanBallot();
                        }
                    });
                    t.setRepeats(false);
                    t.start();
                }
            }


            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {
            }


        });

        statusTimer.start();

        scanBallot();
    }

    /**
     * Main entry point into the program. If an argument is given, it will be
     * the serial number, otherwise VoteBox will load a serial from its config file.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 1)
            new BallotScanner(Integer.parseInt(args[0])).start();
        else
            //Tell VoteBox to refer to its config file for the serial number
            new BallotScanner().start();
    }
}