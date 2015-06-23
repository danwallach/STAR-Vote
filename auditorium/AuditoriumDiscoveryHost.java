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

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

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

    /** The AuditoriumHost to whom this discovery host belongs */
    private final IAuditoriumHost host;

    /** Parameters specifying port numbers, etc. */
    private final IAuditoriumParams constants;

    /** The port through which discoveries are handled */
    private final HostPointer discoverAddress;

    /** The address of the AuditoriumHost*/
    private final HostPointer hostAddress;

    /** Denotes if this host is running */
    private volatile boolean running;

    /** The socket through which discovery communications occur */
    private DatagramSocket discoverSocket;

    /**
     * Constructor.
     *
     * @param host              A discovery host must belong to an auditorium host (it has to know
     *                          *what* to tell other hosts is available)
     * @param constants         Global constants needed in discovery (timeouts, etc) are defined here.
     */
    public AuditoriumDiscoveryHost(IAuditoriumHost host, IAuditoriumParams constants) {

        /* Initialize the fields */
        this.host = host;
        this.constants = constants;

        /* figure out the host's address */
        hostAddress = host.getMe();

        /* Create an address for discovery */
        discoverAddress = new HostPointer(this.host.getNodeId(), this.host.getMe().getIP(), constants.getDiscoverReplyPort());

        /* We aren't running until we are */
        running = false;

        try {
            discoverSocket = new DatagramSocket();
            discoverSocket.setSoTimeout(0);
        }
        catch (SocketException e) { throw new FatalNetworkException("Cannot create discover socket", e); }
    }

    /**
     * Start the discovery thread. This allows discovery responses to come from
     * this machine.
     * 
     * @throws NetworkException Thrown if we cannot bind the discover socket to the correct port.
     */
    public void start() throws NetworkException {
        /* Prints output to the console */
        Bugout.msg( "Discovery: STARTING" );

        /* No we're running! */
        running = true;

        /* Try to bind the socket */
        try { discoverSocket = new DatagramSocket(constants.getDiscoverPort()); }
        catch (SocketException e) {
            throw new NetworkException( "Could not bind the discovery socket: " + e.getMessage(), e );
        }

        /* Start listening in a thread */
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

        /* Close the socket */
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

        /* Build the socket infrastructure */
        DatagramSocket sendSocket;
        ServerSocket listenSocket;
        try {
            /* Bind the outgoing socket */
        	sendSocket = new DatagramSocket();

            /* bind the incoming socket based on the specified ports */
        	listenSocket = new ServerSocket( constants.getDiscoverReplyPort() );
        	listenSocket.setSoTimeout(constants.getDiscoverTimeout());
        }
        catch (IOException e) {
            throw new NetworkException( "Cannot bind sockets", e );
        }

        LinkedList<HostPointer> ret = new LinkedList<>();

        /* Send the discover message.*/
        try {

            /* Build the message */
            Message discMsg = new Message("Discover", hostAddress, host.nextSequence(), discoverAddress.toASE());

            /* Serialize the message */
            byte[] discBytes = discMsg.toASE().toVerbatim();

            /* Send the message in a packet */
            sendSocket.send(new DatagramPacket(discBytes, discBytes.length, InetAddress.getByName(constants.getBroadcastAddress()), constants.getDiscoverPort()));

            /* Report that we're sending a message to the console */
            Bugout.msg("Discover: sending: " + new MessagePointer(discMsg));
        }
        catch (UnknownHostException e) {
            throw new FatalNetworkException("Could not establish the all-ones address.", e);
        }
        catch (IOException e) {
            throw new NetworkException( "Problem sending discover packet.", e );
        }

        /* Listen for a set of responses */
        while (true) {

            try {
                /* try to bind an incoming connected socket */
                Bugout.msg( "Discover: waiting for incoming socket connection" );
                MessageSocket socket = new MessageSocket( listenSocket.accept() );

                /* Read in the message */
                Bugout.msg( "Discover: awaiting bytes" );
                Message response = socket.receive();

                /* Report that we received a message */
                Bugout.msg("Discover: received: " + new MessagePointer(response));

                /* Check that our message is the one we were looking for */
                if (!response.getType().equals( "discover-reply" ))
                    Bugout.err("Discover: response of incorrect type: " + response.toASE());

                /* Now iterate over the list of hosts that were included in the discover-reply message */
                for (ASExpression ase : (ListExpression) response.getDatum()) {

                    /* Build HostPointers and add them to our list of connections */
                    HostPointer p = new HostPointer( ase );
                    if (!ret.contains( p ))
                        ret.add( p );
                }

                /* close the socket */
                socket.close();
            }
            catch (SocketTimeoutException e) {
                /* If we time out, just get out of the loop */
                break;
            }
            catch (IOException e) {
                Bugout.err("Host: IO Error receiving discover response: " + e.getMessage());
            } catch (IncorrectFormatException | ClassCastException e) {
                Bugout.err("Host: Discover response was not formatted correctly: " + e.getMessage());
            }
        }

        /* Return the received HostPointers in an array */
        return ret.toArray(new HostPointer[ret.size()]);
    }

    /**
     * This is the behavior for the thread that listens for incoming discover requests. It is
     * designed to run on its own dedicated thread.
     */
    private void discoverListenerThread() {
        Bugout.msg( "Discover: THREAD START" );

        /* Try to discover new hosts as long as we're running */
        while (running) {
            try {
                /* Listen for a packet on this socket */
                byte[] buf = new byte[1000];
                DatagramPacket p = new DatagramPacket( buf, buf.length );
                Bugout.msg("Discover: waiting for packet");

                /* Set p as the listen socket */
                try { discoverSocket.receive(p); }
                catch (IOException e) {
                    /* If we hit an exception, note it and close down the socket */
                    Bugout.err("Discover: could not attempt to receive a packet:" + e.getMessage());
                    stop();
                    break;
                }

                /* If we've successfully received a packet, note it and prepare to read in its data */
                Bugout.msg("Discover: packet received.");
                ASEInputStreamReader reader = new ASEInputStreamReader(new ByteArrayInputStream(buf));

                /* Read in the message from the socket */
                Message message = new Message( reader.read() );
                Bugout.msg("Discover: received packet:" + new MessagePointer(message));

                /* Respond to the message. */
                HostPointer address = new HostPointer(message.getDatum());

                /* If it's our packet, ignore it */
                if (address.equals(discoverAddress))
                    continue;

                /* Note that we're replying, and then bind the socket to reply on*/
                Bugout.msg( "Discover: replying to " + address );
                MessageSocket s = new MessageSocket(address, constants.getDiscoverReplyTimeout());

                /* Send the reply message */
                s.send( new Message("discover-reply", hostAddress, host.nextSequence(), new ListExpression(hostAddress.toASE())));

                /* Close the socket and note that we replied */
                s.close();
                Bugout.msg("Discover: reply sent.");
            }
            catch (IOException e) {                    Bugout.err("Discover: problem responding: " + e.getMessage()); }
            catch (InvalidVerbatimStreamException e) { Bugout.err("Discover: packet received was not an s-expression."); }
            catch (IncorrectFormatException e) {       Bugout.err("Discover: packet received was not a correctly formatted s-expression"); }
            catch (NetworkException e) {
                e.printStackTrace();
                Bugout.err( "Discover: packet received: " + e.getMessage() );
                Bugout.err("Discover: socket: port - " + discoverSocket.getPort());
                Bugout.err( "Discover: socket: local port - "+ discoverSocket.getLocalPort());
            }
        }

        /* Note the end of the thread */
        Bugout.msg( "Discover: THREAD END" );
    }
}
