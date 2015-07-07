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

package tap;

import auditorium.IAuditoriumParams;
import auditorium.NetworkException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import sexpression.stream.ASEWriter;
import votebox.AuditoriumParams;
import votebox.events.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;


/**
 * Class used to "Tap" outgoing commit and cast messages.<BR>
 * Forwards - ideally across a one-way connection, or diode - these messages to a machine<BR>
 * outside of the Auditorium network, which a voter would then interrogate.<BR>
 * <BR>
 * Must not, under any circumstances, broadcast meaningful messages back onto the Auditorium network.<BR>
 * Keep-alives - or heartbeats - are permissible.
 * @author Montrose
 *
 */
public class Tap {

    private int _mySerial = -1;
    private final String launchCode;
    private ASEWriter _output = null;
    private OutputStream _wrappedOut = null;
    private static VoteBoxAuditoriumConnector _auditorium = null;
    private static String ballotDumpHTTPKey = "3FF968A3B47CT34C";

    private static boolean uploading = false;
    private static long threshold = 0L;

    /** A list of all supervisors who have indicated they are uploading ballots*/
    private ArrayList<Integer> uploadPending;

    /** A list of all supervisors who have finished uploading their ballots */
    private ArrayList<Integer> uploadComplete;

    /** This precinct's supervisor record, which will be uploaded to the bulletin board */
    private Map<String, Serializable> supervisorRecord;


    /**
     * Initializes a new Trapper.<BR>
     *
     * @see Tap#start()
     *
     * @param serial        serial number of this machine.
     * @param out           OutputStream to send selected messages to.
     */
    public Tap(int serial, OutputStream out, String launchCode, IAuditoriumParams params){

        _mySerial = serial;
        _wrappedOut = out;
        _output = new ASEWriter(_wrappedOut);
        this.launchCode = launchCode;

        uploadPending = new ArrayList<>();
        uploadComplete = new ArrayList<>();
        supervisorRecord = new HashMap<>();

    }

    /**
     * TODO is this still used?
     * Forwards the given event.
     *
     * @param event         IAnnounceEvent to forward.
     */
    protected void forward(IAnnounceEvent event){

        /* Write the event serial and convert and write as s-expression */
        try {
            _wrappedOut.write(event.getSerial());
            _output.writeASE(event.toSExp());
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Dumps the ballots to the server TODO more refined explanation
     */
    public void uploadToServer() {

        System.out.println("Uploading Ballots to the server!");

        HttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost("http://localhost:9000/3FF968A3B47CT34C");

        String encoded;

        try {

            System.out.println("Encoding the Supervisors' records... ");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            /* Write the record to the stream */
            objectOutputStream.writeObject(supervisorRecord);
            objectOutputStream.close();

            /* Encode the record as a string */
            encoded = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));

            /* Output encoded to a file for webserver testing */
            try {
                PrintWriter out = new PrintWriter("testdata.txt");
                out.print(encoded);
                out.close();
            } catch (Exception e) {e.printStackTrace();}

            List<BasicNameValuePair> bnvp = new ArrayList<>();

            bnvp.add(new BasicNameValuePair("record", encoded));
            bnvp.add(new BasicNameValuePair("precinctID", Integer.toString((new Random()).nextInt())));

            /* Set entities for each of the url encoded forms of the NVP */
            post.setEntity(new UrlEncodedFormEntity(bnvp));

            System.out.println("Executing post..." + post.getEntity());

            /* Execute the post */
            client.execute(post);

        }
        catch (IOException e) { e.printStackTrace(); }

        System.out.println("Upload complete!");

        /* Shutdown the connection when done */
        client.getConnectionManager().shutdown();
    }

    /**
     * Connects the Trapper instance to the Auditorium network and listens for<BR>
     * Commit and EncryptedCastBallot messages to forward.
     */
    public void start(){

        try{

            _auditorium = new VoteBoxAuditoriumConnector(_mySerial,
                    AuditoriumParams.Singleton, launchCode,
                    CommitBallotEvent.getMatcher(),
                    BallotReceivedEvent.getMatcher(),
                    BallotScanAcceptedEvent.getMatcher(),
                    PollsClosedEvent.getMatcher(),
                    StartUploadEvent.getMatcher(),
                    BallotUploadEvent.getMatcher()
            );

        }
        catch(NetworkException e){ /* TODO runtime exception */
            throw new RuntimeException("Unable to connect to Auditorium: " + e.getMessage(), e);
        }

        _auditorium.addListener(new VoteBoxEventListener(){

            public void ballotAccepted(BallotScanAcceptedEvent e){
                /* TODO? BallotStore.castCommittedBallot(e.getBID().toString()); */
            }

            public void commitBallot(CommitBallotEvent e) {
			    /* TODO? BallotStore.addPrecinct(e.getBID().toString(), e.getRaceSelections()); */
            }

            public void ballotReceived(BallotReceivedEvent e){
                /* TODO? BallotStore.mapPrecinct(e.getBID(), e.getPrecinct()); */
            }

            /* Ignored events */
            public void left(LeaveEvent e) {}
            public void joined(JoinEvent e) {}
            public void votebox(VoteBoxEvent e) {}
            public void pollsOpen(PollsOpenEvent e) {}
            public void activated(ActivatedEvent e) {}
            public void pollsOpenQ(PollsOpenQEvent e) {}
            public void supervisor(SupervisorEvent e) {}
            public void assignLabel(AssignLabelEvent e) {}
            public void pinEntered(PINEnteredEvent event) {}
            public void invalidPin(InvalidPinEvent event) {}
            public void lastPollsOpen(LastPollsOpenEvent e) {}
            public void overrideCommit(OverrideCommitEvent e) {}
            public void ballotScanner(BallotScannerEvent e) {}
            public void ballotScanned(BallotScannedEvent e) {}
            public void overrideCancel(OverrideCancelEvent e) {}
            public void ballotRejected(BallotScanRejectedEvent e){}
            public void overrideCommitDeny(OverrideCommitDenyEvent e) {}
            public void authorizedToCast(AuthorizedToCastEvent e) {}
            public void tapMachine(TapMachineEvent tapMachineEvent) {}
            public void pollStatus(PollStatusEvent pollStatusEvent) {}
            public void overrideCancelDeny(OverrideCancelDenyEvent e) {}
            public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {}
            public void castCommittedBallot(CastCommittedBallotEvent e) {}
            public void overrideCommitConfirm(OverrideCommitConfirmEvent e) {}
            public void scannerStart(StartScannerEvent startScannerEvent) {}
            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {}
            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {}
            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {}
            public void ballotPrintSuccess(BallotPrintSuccessEvent ballotPrintSuccessEvent) {}
            public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent) {}
            public void announceProvisionalBallot(ProvisionalBallotEvent provisionalBallotEvent) {}
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent provisionalAuthorizeEvent) {}
            public void completedUpload(CompletedUploadEvent completedUploadEvent) {}

            public void startUpload(StartUploadEvent startUploadEvent) {
                System.out.println("Supervisor started upload...");
                uploadPending.add(startUploadEvent.getSerial());
            }

            @Override
            public void uploadBallots(BallotUploadEvent ballotUploadEvent) {

                System.out.println("A Supervisor is trying to upload ballots! ");

                /* TODO check this edge case waiting for slow connectors */

                /* If this method gets called while we're uploading, chill out for a sec or 5 */
                /* TODO perhaps fix this so that it doesn't always increment by 5 seconds if there are multiple slow connections */
                if(uploading) {
                    System.out.println("Delaying upload for 5 seconds while we load ballots... ");
                    threshold += 5000L;
                }

                int serial = ballotUploadEvent.getSerial();

                /* Check if the event trying to upload ballots to Tap didn't notify first */
                if(!uploadPending.contains(ballotUploadEvent.getSerial())) {
                    System.err.println("Supervisor " + serial + " tried to upload without first indicating it would!");
                    return;
                }

                /* Remove the serial from the pending uploads*/
                uploadPending.remove(new Integer(serial));

                /* Put the serial and map into the record map */
                supervisorRecord.put(Integer.toString(serial), ballotUploadEvent.getMap());

                /* Start uploading if we're done making the map (i.e. if no more pending and not currently uploading) */
                if(uploadPending.size()==0 && !uploading) {
                    /* TODO collapse identical maps */
                    System.out.println("Upload to server pending... ");
                    startUploadToServer();
                }

            }


            public void pollMachines(PollMachinesEvent pollMachinesEvent){
                _auditorium.announce(new TapMachineEvent(_mySerial));
            }


            public void pollsClosed(PollsClosedEvent e) {
            }

        });

        try {
            System.out.println("Connecting to auditorium...");
            _auditorium.connect();
            _auditorium.announce(new TapMachineEvent(_mySerial));
        }
        catch(NetworkException e){ throw new RuntimeException("Unable to connect to Auditorium network: "+e.getMessage(), e); }
    }

    private void startUploadToServer() {

        System.out.println("Starting uploading to server...");
        /* TODO test this */

        /* Change uploading status */
        uploading = true;

        long cur = System.currentTimeMillis();

        /* Wait for 5 seconds, and if another call to uploadBallots() is made, it will delay 5 more seconds */
        for(threshold = cur+5000L; cur < threshold; cur = System.currentTimeMillis());

        /* Execute upload*/
        uploadToServer();
    }

    private static void testMethod() {
        try {

            String testdata = new Scanner(new File("testdata.txt")).useDelimiter("\\A").next();

            System.out.println("Uploading Ballots to the server!");

            HttpClient client = new DefaultHttpClient();

            HttpPost post = new HttpPost("http://localhost:9000/3FF968A3B47CT34C");

            List<BasicNameValuePair> bnvp = new ArrayList();

            bnvp.add(new BasicNameValuePair("record", testdata));
            bnvp.add(new BasicNameValuePair("precinctID", Integer.toString((new Random()).nextInt())));

            /* Set entities for each of the url encoded forms of the NVP */
            post.setEntity(new UrlEncodedFormEntity(bnvp));

            System.out.println("Executing post..." + post.getEntity());

            /* Execute the post */
            client.execute(post);

            System.out.println("Upload complete!");

            /* Shutdown the connection when done */
            client.getConnectionManager().shutdown();

        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Usage:<BR> FIXME?
     * 		java votebox.Tap [serial] [report address] [port]
     *
     * @param args      arguments to be used
     *
     * @throws RuntimeException if there is an issue parsing values, the port used is bad,
     *                          or there is an interruption in the process
     */
    public static void main(String[] args){

        IAuditoriumParams params = new AuditoriumParams("tap.conf");

        System.out.println(params.getReportAddress());

        String reportAddr;

        int serial;
        int port;
        String launchCode;

        /* See if there isn't a full argument set */
        if (args.length != 4) {

            int p = 0;

            /* Assign the default serial */
            serial = params.getDefaultSerialNumber();

            /* If the serial is still bad... */
            if (serial == -1) {

                /* Try the first of the given arguments */
                try {
                    serial = Integer.parseInt(args[p]);
                    p++;
                }
                catch (Exception e) {
                    throw new RuntimeException("usage: Tap [serial] [report address] [port]\nExpected valid serial.");
                }
            }

            /* Assign the report address */
            reportAddr = params.getReportAddress();

            /* If no valid address... */
            if (reportAddr.length() == 0) {

                /* Try one of the given arguments (first or second, depending) */
                try {
                    reportAddr = args[p];
                    p++;
                }
                catch (Exception e) { throw new RuntimeException("usage: Tap [serial] [report address] [port]"); }
            }

            /* Assign the port */
            //port = params.getPort();
            port = Integer.parseInt(args[p]);

            /* If the port is still bad... */
            if (port == -1) {

                /* Try one of the given arguments (first, second, or third, depending) */
                try {
                    port = Integer.parseInt(args[p]);
                }
                catch (Exception e) {
                    throw new RuntimeException("usage: Tap [serial] [report address] [port]\nExpected valid port.");
                }
            }

            launchCode = "0000000000";
        }

        /* If there is a full argument set... */
        else {

            /* Try to load up the args */
            try {
                serial = Integer.parseInt(args[0]);
                reportAddr = args[1];
                port = Integer.parseInt(args[2]);
                launchCode = args[3];
            }
            catch (Exception e) { throw new RuntimeException("usage: Tap [serial] [report address] [port]"); }
        }

        try {

            /* Create a new socket address */
            System.out.println(reportAddr + port);
            InetSocketAddress addr = new InetSocketAddress(reportAddr, port);

            /* Loop until an exception or tap is started */
            while (true) {

                try {

                    /* Try to establish a socket connection */
                    Socket localCon = new Socket();
                    localCon.connect(addr);

                    /* Start the tap */
                    (new Tap(serial, localCon.getOutputStream(), launchCode, params)).start();
                    System.out.println("Connection successful to " + addr);
                    break;
                }
                catch (IOException e) { /* If no good, retry */
                    System.out.println("Connection failed: " + e.getMessage());
                    System.out.println("Retry in 5 seconds...");
                    Thread.sleep(5000);
                }
            }
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("usage: Tap [serial] [report address] [port]; where port is between 1 and 65335 & [serial] is a positive integer", e);
        }
        catch (InterruptedException e) { throw new RuntimeException(e); }

        /* TEST CODE */

        //testMethod();

        /* END TEST CODE */

    }
}
