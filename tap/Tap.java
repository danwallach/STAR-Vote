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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import auditorium.IAuditoriumParams;
import auditorium.NetworkException;

import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import sexpression.stream.ASEWriter;
import votebox.AuditoriumParams;
import votebox.events.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;



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
    private ASEWriter _output = null;
    private OutputStream _wrappedOut = null;
    private VoteBoxAuditoriumConnector _auditorium = null;
    private static IAuditoriumParams params;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static String ballotDumpHTTPKey = "3FF968A3B47CT34C";

    /**
     * Initializes a new Trapper.<BR>
     *
     * @see Tap#start()
     *
     * @param serial        serial number of this machine.
     * @param out           OutputStream to send selected messages to.
     */
    public Tap(int serial, OutputStream out){

        _mySerial = serial;
        _wrappedOut = out;
        _output = new ASEWriter(_wrappedOut);

        privateKey = (PrivateKey)params.getKeyStore().loadAdderKey("private");
        publicKey = (PublicKey)params.getKeyStore().loadAdderKey("public");
    }

    /**
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
     * @param ballotList    the list of ballotStrings to be dumped to the server
     */
    protected void dumpBallotList(ArrayList<String> ballotList){

        HttpClient client = new DefaultHttpClient();

            HttpPost post = new HttpPost("http://starvote.cs.rice.edu/3FF968A3B47CT34C");

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<>(1);
            /* For each of the ballotStrings... */
            for (String ballotString : ballotList) {

                /* Add them as a BNVP to nameValuePairs */
                nameValuePairs.add(new BasicNameValuePair("message", ballotString));

                /* Set entities for each of the url encoded forms of the NVP */
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                /* Execute the post */
                client.execute(post);
            }

        }
        catch (IOException e) { e.printStackTrace(); }

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
                    AuditoriumParams.Singleton,
                    CommitBallotEvent.getMatcher(),
                    BallotReceivedEvent.getMatcher(),
                    BallotScanAcceptedEvent.getMatcher(),
                    AuthorizedToCastWithNIZKsEvent.getMatcher(),
                    CastBallotUploadEvent.getMatcher(),
                    ChallengedBallotUploadEvent.getMatcher(),
                    PollsClosedEvent.getMatcher()
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
			    /* TODO? BallotStore.addPrecinct(e.getBID().toString(), e.getVotes()); */
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
            public void overrideCast(OverrideCommitEvent e) {}
            public void ballotScanner(BallotScannerEvent e) {}
            public void ballotScanned(BallotScannedEvent e) {}
            public void overrideCancel(OverrideCancelEvent e) {}
            public void ballotRejected(BallotScanRejectedEvent e){}
            public void overrideCastDeny(OverrideCommitDenyEvent e) {}
            public void authorizedToCast(AuthorizedToCastEvent e) {}
            public void tapMachine(TapMachineEvent tapMachineEvent) {}
            public void pollStatus(PollStatusEvent pollStatusEvent) {}
            public void overrideCancelDeny(OverrideCancelDenyEvent e) {}
            public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {}
            public void castCommittedBallot(CastCommittedBallotEvent e) {}
            public void overrideCastConfirm(OverrideCommitConfirmEvent e) {}
            public void scannerStart(StartScannerEvent startScannerEvent) {}
            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {}
            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {}
            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {}
            public void ballotPrintSuccess(BallotPrintSuccessEvent ballotPrintSuccessEvent) {}
            public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent) {}
            public void announceProvisionalBallot(ProvisionalBallotEvent provisionalBallotEvent) {}
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent provisionalAuthorizeEvent) {}
            public void startUpload(StartUploadEvent startUploadEvent) {}
            public void completedUpload(CompletedUploadEvent completedUploadEvent) {}



            public void pollMachines(PollMachinesEvent pollMachinesEvent){
                _auditorium.announce(new TapMachineEvent(_mySerial));
            }


            public void pollsClosed(PollsClosedEvent e) {
            }

            public void uploadCastBallots(CastBallotUploadEvent e) {
                System.out.println("TAP: Uploading Cast Ballots");
                dumpBallotList(e.getDumpList());
            }
            public void uploadChallengedBallots(ChallengedBallotUploadEvent e) {
                System.out.println("TAP: Uploading Challenged Ballots");
                dumpBallotList(e.getDumpList());
                /* TODO? BallotStore.clearBallots(); */
            }
        });

        try {
            System.out.println("Connecting to auditorium...");
            _auditorium.connect();
            _auditorium.announce(new TapMachineEvent(_mySerial));
        }
        catch(NetworkException e){ throw new RuntimeException("Unable to connect to Auditorium network: "+e.getMessage(), e); }
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

        params = new AuditoriumParams("tap.conf");

        String reportAddr;

        int serial;
        int port;

        /* See if there isn't a full argument set */
        if (args.length != 3) {

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
            port = params.getPort();

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
        }

        /* If there is a full argument set... */
        else {

            /* Try to load up the args */
            try {
                serial = Integer.parseInt(args[0]);
                reportAddr = args[1];
                port = Integer.parseInt(args[2]);
            }
            catch (Exception e) { throw new RuntimeException("usage: Tap [serial] [report address] [port]"); }
        }

        try {

            /* Create a new socket address */
            InetSocketAddress addr = new InetSocketAddress(reportAddr, port);

            /* Loop until an exception or tap is started */
            while (true) {

                try {

                    /* Try to establish a socket connection */
                    Socket localCon = new Socket();
                    localCon.connect(addr);

                    /* Start the tap */
                    (new Tap(serial, localCon.getOutputStream())).start();
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
    }
}
