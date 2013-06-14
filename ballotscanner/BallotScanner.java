package ballotscanner;

import auditorium.NetworkException;
import com.google.zxing.BinaryBitmap;
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

public class BallotScanner {

    private final AuditoriumParams _constants;


    private int numConnections;
    private VoteBoxAuditoriumConnector auditorium;
    private final int mySerial;
    private boolean connected;
    private Timer statusTimer;
    private boolean activated;
    private ObservableEvent activatedObs;
    private JFrame frame;
    private boolean confirmed = false;

    // stores the last found result obtained from a successful code scan
    private String lastFoundBID = "";

    // stores the last time a code was found
    private long lastFoundTime = 0;

    // keeps the path to the "ballot scanned" mp3
    private String bsMp3Path;// = "sound/ballotscanned.mp3"; //move to the .conf file

    // keeps the mp3Player
    private Player mp3Player;

    // how long a result is stored in memory before it is cleared
    private final long DELAY_TIME = 5000;

    //An event to signal that a ballot has been scanned
    private BallotScannedEvent e;


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

        if (_constants.useScanConfirmationSound()) {
            bsMp3Path = _constants.getConfirmationSoundPath();
        }

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

        //Set up the JFrame confirmation screen
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLocation((int)Math.round(frame.getLocation().getX()) - 175, (int)Math.round(frame.getLocation().getY()) - 175);
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
    public void beginScanning() {
        IWebcam webcam = new FrameGrabberWebcam();

        webcam.startCapture();

        MultiFormatDecoder decoder = new MultiFormatDecoder();


        while (true) {
            long currentTime = System.currentTimeMillis();
            BinaryBitmap bitmap = webcam.getBitmap();

            if(frame.isVisible())
                continue;


            lastFoundBID = decoder.decode(bitmap);

            if (currentTime - lastFoundTime > DELAY_TIME) {
                if (lastFoundBID != null) {
                    System.out.println(lastFoundBID);  //TODO Is this needed?
                    lastFoundTime = System.currentTimeMillis();
                    auditorium.announce(new BallotScannedEvent(mySerial, lastFoundBID));
                    bsMp3Path = _constants.getConfirmationSoundPath();

                    // prepare the mp3Player
                    //TODO Should we play a sound?
                    try {

                        FileInputStream fileInputStream = new FileInputStream(bsMp3Path);
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                        mp3Player = new Player(bufferedInputStream);
                    } catch (Exception e) {
                        if (!_constants.useScanConfirmationSound()) {
                            System.out.println("Problem playing audio: " + bsMp3Path);
                            System.out.println(e);
                        }
                    }

                    // play the sound
                    new Thread() {
                        public void run() {
                            try {
                                mp3Player.play();
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }.start();

                    e = new BallotScannedEvent(mySerial, lastFoundBID);
                    auditorium.announce(e);

                    while(!confirmed){
                        if(frame.isVisible()) //reset the confirmed variable once the jframe has shown
                            confirmed = false;
                    }


                }
            }
        }
    }

    /**
     * Main method which right now just goes into an infinite while loop, constantly scanning
     */
    public void start() {

        try {
            auditorium = new VoteBoxAuditoriumConnector(mySerial,
                    _constants, CastBallotEvent.getMatcher()
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

            //When a cast ballot is seen, build a JFRAME Displaying confirmation or denial
            public void castBallot(CastBallotEvent event) {
                if(event.getBid().equals(lastFoundBID)){

                    DateFormat dateFormat = new SimpleDateFormat("MMMM d, y");
                    Date date = new Date();

                    //Code which will display a confirmation screen

                    JPanel panel = new JPanel();
                    JLabel imageLabel = new JLabel();
                    JLabel textLabel = new JLabel("Ballot " + lastFoundBID + " confirmed and cast on ");
                    JLabel dateLabel = new JLabel(dateFormat.format(date));


                    BufferedImage confirmed = null;

                    try{
                        File file = new File("images/confirmation.png");
                        confirmed = ImageIO.read(file);
                    } catch (IOException e){
                        System.out.println("Confirmation image could not be loaded!");
                        throw new RuntimeException(e);
                    }
                    ImageIcon confirmationIcon = new ImageIcon(confirmed);



                    imageLabel.setIcon(confirmationIcon);
                    panel.add(imageLabel);
                    panel.setPreferredSize(new Dimension(350, 350));
                    panel.add(textLabel);
                    panel.add(dateLabel);
                    frame.add(panel);
                    frame.pack();

                    frame.setVisible(true);

                }  else{
                    DateFormat dateFormat = new SimpleDateFormat("MMMM d, y");
                    Date date = new Date();

                    //Code which will display a confirmation screen

                    JPanel panel = new JPanel();
                    JLabel imageLabel = new JLabel();
                    JLabel textLabel = new JLabel("Ballot " + lastFoundBID + " was NOT cast.");
                    JLabel text2Label = new JLabel("Please ensure that this is the correct ballot or seek assistance from an election official.");
                    JLabel dateLabel = new JLabel(dateFormat.format(date));


                    BufferedImage rejected = null;

                    try{
                        File file = new File("images/rejected.png");
                        rejected = ImageIO.read(file);
                    } catch (IOException e){
                        System.out.println("Rejection image could not be loaded!");
                        throw new RuntimeException(e);
                    }
                    ImageIcon rejectionIcon = new ImageIcon(rejected);



                    imageLabel.setIcon(rejectionIcon);
                    panel.add(imageLabel);
                    panel.setPreferredSize(new Dimension(350, 350));
                    panel.add(textLabel);
                    panel.add(text2Label);
                    panel.add(dateLabel);
                    frame.add(panel);
                    frame.pack();

                    frame.setVisible(true);

                    confirmed = true;

                }

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

            public void ballotPrinted(BallotPrintedEvent ballotPrintedEvent) {
            }


        });

        statusTimer.start();

        beginScanning();
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