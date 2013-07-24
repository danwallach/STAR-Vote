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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import auditorium.AuditoriumCryptoException;
import auditorium.IAuditoriumParams;
import auditorium.Key;
import auditorium.NetworkException;

import de.roderick.weberknecht.*;
import sexpression.ASExpression;
import sexpression.stream.ASEWriter;
import supervisor.model.BallotStore;
import votebox.AuditoriumParams;
import votebox.events.*;

import org.apache.http.HttpResponse;
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
    private Key privateKey;
    private static String ballotDumpHTTPKey = "3FF968A3B47CT34C";

    /**
     * Initializes a new Trapper.<BR>
     *
     * @see Tap#start()
     * @param serial - Serial number of this machine.
     * @param out - OutputStream to send selected messages to.
     */
    public Tap(int serial, OutputStream out){
        _mySerial = serial;
        _wrappedOut = out;
        _output = new ASEWriter(_wrappedOut);

        try{
            privateKey = params.getKeyStore().loadKey(_mySerial + "-private");
        }catch(AuditoriumCryptoException ex){
            ex.printStackTrace();
        }
    }//Trapper

    /**
     * Forwards the given event.
     * @param event - IAnnounceEvent to forward.
     */
    protected void forward(IAnnounceEvent event){
        try {
            _wrappedOut.write(event.getSerial());
            _output.writeASE(event.toSExp());
        } catch (IOException e) {
            System.err.println("Failed forwarding the message:\n"+event.toSExp()+"\nbecause: "+e.getMessage());
        }
    }//forward

    protected void dumpBallotList(ArrayList<String> ballotList){

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://starvote.cs.rice.edu/3FF968A3B47CT34C");
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

            for (String ballotString : ballotList) {
                nameValuePairs.add(new BasicNameValuePair("message", ballotString));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                client.execute(post);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects the Trapper instance to the Auditorium network and listens for<BR>
     * Commit and EncryptedCastBallot messages to forward.
     *
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
        }catch(NetworkException e){
            throw new RuntimeException("Unable to connect to Auditorium: "+e.getMessage(), e);
        }//catch

        _auditorium.addListener(new VoteBoxEventListener(){
            public void ballotCounted(BallotCountedEvent e){
                //NO-OP
            }

            public void ballotAccepted(BallotScanAcceptedEvent e){
                BallotStore.castCommittedBallot(e.getBID().toString());
            }

            public void commitBallot(CommitBallotEvent e) {
			    BallotStore.addBallot(e.getBID().toString(), e.getBallot());
                System.out.println("TAP: committing ballot " + e.getBID().toString());
            }

            public void ballotReceived(BallotReceivedEvent e){
                BallotStore.mapPrecinct(e.getBID(), e.getPrecinct());
                System.out.println("Mapping BID: " + e.getBID() + "to precinct: " + e.getPrecinct());
            }

            //Ignored events
            public void activated(ActivatedEvent e) {}
            public void assignLabel(AssignLabelEvent e) {}
            public void authorizedToCast(AuthorizedToCastEvent e) {}
            public void joined(JoinEvent e) {}
            public void lastPollsOpen(LastPollsOpenEvent e) {}
            public void left(LeaveEvent e) {}
            public void overrideCancel(OverrideCancelEvent e) {}
            public void overrideCancelConfirm(OverrideCancelConfirmEvent e) {}
            public void overrideCancelDeny(OverrideCancelDenyEvent e) {}
            public void overrideCast(OverrideCastEvent e) {}
            public void overrideCastConfirm(OverrideCastConfirmEvent e) {}
            public void overrideCastDeny(OverrideCastDenyEvent e) {}
            public void pollsOpen(PollsOpenEvent e) {}
            public void pollsOpenQ(PollsOpenQEvent e) {}
            public void supervisor(SupervisorEvent e) {}
            public void votebox(VoteBoxEvent e) {}
            public void ballotScanner(BallotScannerEvent e) {}
            public void ballotScanned(BallotScannedEvent e) {}
            public void castCommittedBallot(CastCommittedBallotEvent e) {}
            public void pollStatus(PollStatusEvent pollStatusEvent) {}
            public void pinEntered(PINEnteredEvent event) {}
            public void invalidPin(InvalidPinEvent event) {}
            public void ballotRejected(BallotScanRejectedEvent e){}
            public void ballotPrinting(BallotPrintingEvent ballotPrintingEvent) {}
            public void ballotPrintSuccess(BallotPrintSuccessEvent ballotPrintSuccessEvent) {}
            public void ballotPrintFail(BallotPrintFailEvent ballotPrintFailEvent) {}
            public void scannerStart(StartScannerEvent startScannerEvent) {}
            public void pollMachines(PollMachinesEvent pollMachinesEvent){}
            public void spoilBallot(SpoilBallotEvent spoilBallotEvent) {}
            public void announceProvisionalBallot(ProvisionalBallotEvent provisionalBallotEvent) {}
            public void provisionalAuthorizedToCast(ProvisionalAuthorizeEvent provisionalAuthorizeEvent) {}
            public void provisionalCommitBallot(ProvisionalCommitEvent provisionalCommitEvent) {}
            public void authorizedToCastWithNIZKS(AuthorizedToCastWithNIZKsEvent e) {}


            public void pollsClosed(PollsClosedEvent e) {
                _auditorium.announce(new CastBallotUploadEvent(_mySerial, BallotStore.getCastNonces()));
            }

            public void uploadCastBallots(CastBallotUploadEvent e) {
                dumpBallotList(e.getDumpList());
                System.out.println("TAP: Uploading Cast Ballots");
                _auditorium.announce(new ChallengedBallotUploadEvent(_mySerial, BallotStore.getDecryptedBallots(privateKey)));
            }
            public void uploadChallengedBallots(ChallengedBallotUploadEvent e) {
                dumpBallotList(e.getDumpList());
                System.out.println("TAP: Uploading Challenged Ballots");
                BallotStore.clearBallots();
            }
        });

        try{
            System.out.println("Connecting to auditorium...");
            _auditorium.connect();
        }catch(NetworkException e){
            throw new RuntimeException("Unable to connect to Auditorium network: "+e.getMessage(), e);
        }//catch
    }//start

    /**
     * Usage:<BR>
     * 		java votebox.Tap [serial] [report address] [port]
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args){
        params = new AuditoriumParams("tap.conf");

        int serial = -1;
        String reportAddr = null;
        int port = -1;

        if(args.length != 3){
            int p = 0;
            serial = params.getDefaultSerialNumber();

            if(serial == -1){
                try{
                    serial = Integer.parseInt(args[p]);
                    p++;
                }catch(Exception e){
                    throw new RuntimeException("usage: Tap [serial] [report address] [port]\nExpected valid serial.");
                }
            }

            reportAddr = params.getReportAddress();

            if(reportAddr.length() == 0){
                try{
                    reportAddr = args[p];
                    p++;
                }catch(Exception e){
                    throw new RuntimeException("usage: Tap [serial] [report address] [port]");
                }
            }

            port = params.getPort();

            if(port == -1){
                try{
                    port = Integer.parseInt(args[p]);
                    p++;
                }catch(Exception e){
                    throw new RuntimeException("usage: Tap [serial] [report address] [port]\nExpected valid port.");
                }
            }
        }else{
            try{
                serial = Integer.parseInt(args[0]);
                reportAddr = args[1];
                port = Integer.parseInt(args[2]);
            }catch(Exception e){
                throw new RuntimeException("usage: Tap [serial] [report address] [port]");
            }
        }

        System.out.println("Using settings:\n\tSerial: "+serial+"\n\tReport Address: "+reportAddr+"\n\tPort: "+port);

        try{
            InetSocketAddress addr = new InetSocketAddress(reportAddr, port);

            while(true){
                try{
                    Socket localCon = new Socket();
                    localCon.connect(addr);

                    (new Tap(serial, localCon.getOutputStream())).start();
                    break;
                }catch(IOException e){
                    System.out.println("Connection failed: "+e.getMessage());
                    System.out.println("Retry in 5 seconds...");
                    Thread.sleep(5000);
                }//catch
            }//while
        }catch(NumberFormatException e){
            throw new RuntimeException("usage: Tap [serial] [report address] [port]; where port is between 1 and 65335 & [serial] is a positive integer", e);
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }//main
}
