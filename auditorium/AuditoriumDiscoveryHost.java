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

package auditorium;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import sexpression.*;
import sexpression.stream.*;

/**
 * This class implements the auditorium discovery protocol. An instance of this
 * class represents a host running this protocol.<br>
 * <br>
 * In addition to providing the API for discovering other hosts running the
 * protocol, this class also responds to discover requests. This means you must
 * treat this instance as a keeper of a thread. Calling the start and stop
 * methods take care of launching and cleaning up this thread's resources. This
 * thread listens for incoming UDP traffic on a known port. If it receives a
 * discover request (which contains a contact address and port), then it opens a
 * TCP socket to said address and port and sends a list of all connected hosts.<br>
 * <br>
 * For more information about the format of discovery messages, see the <a
 * href="https://sys.cs.rice.edu/votebox/trac/">project wiki</a>
 * 
 * @author Kyle Derr
 * 
 */
public class AuditoriumDiscoveryHost {

    private final IAuditoriumHost host;
    private final IAuditoriumParams constants;
    private final HostPointer discoverAddress;
    private final HostPointer hostAddress;
    private volatile boolean running;
    private DatagramSocket discoverSocket;

    /**
     * @param host
     *            A discovery host must belong to an auditorium host (it has to
     *            know *what* to tell other hosts is available)>
     * @param constants
     *            Global constants needed in discovery (timeouts, etc) are
     *            defined here.
     */
    public AuditoriumDiscoveryHost(IAuditoriumHost host,
            IAuditoriumParams constants) {
        this.host = host;
        this.constants = constants;
        hostAddress = host.getMe();
        discoverAddress = new HostPointer( this.host.getNodeId(), this.host.getMe()
                .getIP(), constants.getDiscoverReplyPort() );
        running = false;

        try {
            discoverSocket = new DatagramSocket();
            discoverSocket.setSoTimeout(0);
        }
        catch (SocketException e) {
            throw new FatalNetworkException( "Cannot create discover socket.",
                    e );
        }
    }

    /**
     * Start the discovery thread. This allows discovery responses to come from
     * this machine.
     * 
     * @throws NetworkException
     *             This method throws if it cannot bind the discover socket to
     *             the correct port.
     */
    public void start() throws NetworkException {
        Bugout.msg( "Discovery: STARTING" );

        running = true;
        try {
        	discoverSocket = new DatagramSocket( constants.getDiscoverPort() );
        }
        catch (SocketException e) {
            throw new NetworkException( "Could not bind the discovery socket: "
                    + e.getMessage(), e );
        }

        new Thread( new Runnable() {

            public void run() {
                discoverListenerThread();
            }
        } ).start();
    }

    /**
     * Stop the discovery thread.
     */
    public void stop() {
        Bugout.msg( "Discovery: STOPPING" );
        running = false;
        discoverSocket.close();
    }

    /**
     * Broadcast a discover request, and wait for responses. Calling this method
     * will search the network for other reachable Auditorium hosts by
     * broadcasting an "I'm here" message over UDP. The response to this message
     * by other auditorium hosts will the list of hosts to which they are
     * connected.
     * 
     * @return This method returns all the hosts that were found via the
     *         discovery operation.
     */
    public HostPointer[] discover() throws NetworkException {

        // Build the socket infrastructure
        DatagramSocket sendSocket;
        ServerSocket listenSocket;
        try {
        	sendSocket = new DatagramSocket();
        	listenSocket = new ServerSocket( constants.getDiscoverReplyPort() );
        	listenSocket.setSoTimeout(constants.getDiscoverTimeout());
        }
        catch (IOException e) {
            throw new NetworkException( "Cannot bind sockets", e );
        }

        LinkedList<HostPointer> ret = new LinkedList<>();

        // Send the discover message.
        try {
            Message discMsg = new Message( "Discover", hostAddress, host
                    .nextSequence(), discoverAddress.toASE() );
            byte[] discBytes = discMsg.toASE().toVerbatim();
            sendSocket.send(new DatagramPacket(discBytes, discBytes.length,
                    InetAddress.getByName(constants.getBroadcastAddress()),
                    constants.getDiscoverPort()));
            Bugout.msg( "Discover: sending: " + new MessagePointer( discMsg ) );
        }
        catch (UnknownHostException e) {
            throw new FatalNetworkException(
                    "Could not establish the all-ones address.", e );
        }
        catch (IOException e) {
            throw new NetworkException( "Problem sending discover packet.", e );
        }

        // Listen for a set of responses
        while (true) {
            try {
                Bugout.msg( "Discover: waiting for incoming socket connection" );
                MessageSocket socket = new MessageSocket( listenSocket.accept() );
                Bugout.msg( "Discover: awaiting bytes" );
                Message response = socket.receive();
                Bugout.msg( "Discover: received: "
                        + new MessagePointer( response ) );
                if (!response.getType().equals( "discover-reply" ))
                    Bugout.err( "Discover: response of incorrect type: "
                            + response.toASE() );

                for (ASExpression ase : (ListExpression) response.getDatum()) {
                    HostPointer p = new HostPointer( ase );
                    if (!ret.contains( p ))
                        ret.add( p );
                }
                socket.close();
            }
            catch (SocketTimeoutException e) {
                break;
            }
            catch (IOException e) {
                Bugout.err( "Host: IO Error receiving discover response: "
                        + e.getMessage() );
            }
            catch (IncorrectFormatException | ClassCastException e) {
                Bugout
                        .err( "Host: Discover response was not formatted correctly: "
                                + e.getMessage() );
            }
        }

        return ret.toArray(new HostPointer[ret.size()]);
    }

    /**
     * This is the behavior for the thread that listens for incoming discover
     * requests.
     */
    private void discoverListenerThread() {
        Bugout.msg( "Discover: THREAD START" );
        while (running) {
            try {
                // Listen for a packet
                byte[] buf = new byte[1000];
                DatagramPacket p = new DatagramPacket( buf, buf.length );
                Bugout.msg( "Discover: waiting for packet" );
                try {
                    discoverSocket.receive(p);
                }
                catch (IOException e) {
                    Bugout
                            .err( "Discover: could not attempt to receive a packet:"
                                    + e.getMessage() );
                    stop();
                    break;
                }
                Bugout.msg( "Discover: packet received." );
                ASEInputStreamReader reader = new ASEInputStreamReader(
                        new ByteArrayInputStream( buf ) );
                Message message = new Message( reader.read() );
                Bugout.msg( "Discover: received packet:"
                        + new MessagePointer( message ) );

                // Respond to the message.
                HostPointer address = new HostPointer( message.getDatum() );
                if (address.equals(discoverAddress))
                    continue;
                Bugout.msg( "Discover: replying to " + address );
                MessageSocket s = new MessageSocket( address, constants
                        .getDiscoverReplyTimeout() );
                s.send( new Message( "discover-reply", hostAddress, host
                        .nextSequence(), new ListExpression( hostAddress
                        .toASE() ) ) );
                s.close();
                Bugout.msg( "Discover: reply sent." );
            }
            catch (IOException e) {
                Bugout.err( "Discover: problem responding: " + e.getMessage() );
            }
            catch (InvalidVerbatimStreamException e) {
                Bugout
                        .err( "Discover: packet received was not an s-expression." );
            }
            catch (IncorrectFormatException e) {
                Bugout
                        .err( "Discover: packet received was not a correctly formatted s-expression" );
            }
            catch (NetworkException e) {
                e.printStackTrace();
                Bugout.err( "Discover: packet received: " + e.getMessage() );
                Bugout.err("Discover: socket: port - " + discoverSocket.getPort());
                Bugout.err( "Discover: socket: local port - "+ discoverSocket.getLocalPort());
            }
        }
        Bugout.msg( "Discover: THREAD END" );
    }
}
